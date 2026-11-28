package usecases

import (
	"context"
	"fmt"
	"log/slog"
	"net/http"
	"strings"
	"sync"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/domain"
	"github.com/dirzaaulia/footballclips/core-go/ports"
)

type rssFetcher interface {
	FetchRecentVideos(ctx context.Context, channelID string) ([]domain.VideoCandidate, error)
}

type IngestShortsUseCase struct {
	rssClient        rssFetcher
	enricher         detailsEnricher
	shortsRepo       ports.ShortsRepository
	logger           *slog.Logger
	httpClient       *http.Client
	embedPageChecker func(ctx context.Context, videoID string) bool
	shortFormChecker func(ctx context.Context, videoID, title string) bool
}

type channelTarget struct {
	ClubID    *int64
	Name      string
	ChannelID string
}

func NewIngestShortsUseCase(rss rssFetcher, repo ports.ShortsRepository, logger *slog.Logger) *IngestShortsUseCase {
	if logger == nil {
		logger = slog.Default()
	}
	client := &http.Client{
		Timeout: 3 * time.Second,
		CheckRedirect: func(req *http.Request, via []*http.Request) error {
			return http.ErrUseLastResponse
		},
	}
	uc := &IngestShortsUseCase{rssClient: rss, shortsRepo: repo, logger: logger, httpClient: client}
	uc.embedPageChecker = uc.isEmbedPagePlayable
	uc.shortFormChecker = uc.isShortFormVideo
	return uc
}

// WithEnricher attaches a YouTube Data API details enricher to enforce embeddable verification.
func (uc *IngestShortsUseCase) WithEnricher(enricher detailsEnricher) *IngestShortsUseCase {
	uc.enricher = enricher
	return uc
}

// WithEmbedPageChecker overrides the /embed/ HTML verification probe (used in tests).
func (uc *IngestShortsUseCase) WithEmbedPageChecker(fn func(ctx context.Context, videoID string) bool) *IngestShortsUseCase {
	uc.embedPageChecker = fn
	return uc
}

// WithShortFormChecker overrides the HEAD /shorts/ verification probe (used in tests).
func (uc *IngestShortsUseCase) WithShortFormChecker(fn func(ctx context.Context, videoID, title string) bool) *IngestShortsUseCase {
	uc.shortFormChecker = fn
	return uc
}

func (uc *IngestShortsUseCase) Execute(ctx context.Context, channelLimit int) (int, error) {
	existingIDs, err := uc.shortsRepo.GetExistingShortVideoIDs(ctx, 3000)
	if err != nil {
		return 0, fmt.Errorf("fetch existing shorts IDs: %w", err)
	}

	targets, checkedClubIDs, err := uc.resolveChannelTargets(ctx, channelLimit)
	if err != nil {
		return 0, err
	}

	pending := uc.scanChannelsConcurrently(ctx, targets, existingIDs)
	discovered := uc.filterEmbeddableShorts(ctx, pending, time.Now().UTC())

	if len(checkedClubIDs) > 0 {
		_ = uc.shortsRepo.MarkChannelsChecked(ctx, checkedClubIDs)
	}

	if len(discovered) > 0 {
		if err := uc.shortsRepo.UpsertShorts(ctx, discovered); err != nil {
			return 0, fmt.Errorf("upsert shorts: %w", err)
		}
	}

	pruned, _ := uc.shortsRepo.PruneStaleShorts(ctx, 30, 3000)
	uc.logger.Info("[SHORTS_CYCLE_DONE]",
		slog.Int("candidates", len(pending)),
		slog.Int("embeddable_ingested", len(discovered)),
		slog.Int64("pruned", pruned),
	)
	return len(discovered), nil
}

func (uc *IngestShortsUseCase) resolveChannelTargets(
	ctx context.Context,
	limit int,
) ([]channelTarget, []int64, error) {
	clubChannels, err := uc.shortsRepo.GetActiveClubChannels(ctx, limit)
	if err != nil {
		return nil, nil, fmt.Errorf("fetch active club channels: %w", err)
	}

	var targets []channelTarget
	var checkedClubIDs []int64
	for _, ch := range clubChannels {
		cid := ch.ClubID
		targets = append(targets, channelTarget{
			ClubID:    &cid,
			Name:      ch.Name,
			ChannelID: ch.ChannelID,
		})
		checkedClubIDs = append(checkedClubIDs, ch.ClubID)
	}

	broadcasters, err := uc.shortsRepo.GetBroadcasterAndLeagueChannels(ctx)
	if err == nil {
		for _, b := range broadcasters {
			targets = append(targets, channelTarget{ClubID: nil, Name: b.Name, ChannelID: b.ChannelID})
		}
	}

	return targets, checkedClubIDs, nil
}

func (uc *IngestShortsUseCase) scanChannelsConcurrently(
	ctx context.Context,
	targets []channelTarget,
	existingIDs map[string]bool,
) []pendingShort {
	const workerCount = 10
	jobs := make(chan channelTarget, len(targets))
	for _, t := range targets {
		jobs <- t
	}
	close(jobs)

	var mu sync.Mutex
	var pending []pendingShort
	var wg sync.WaitGroup

	for i := 0; i < workerCount; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			for target := range jobs {
				if ctx.Err() != nil {
					return
				}
				uc.processSingleTarget(ctx, target, existingIDs, &mu, &pending)
			}
		}()
	}
	wg.Wait()
	return pending
}

func (uc *IngestShortsUseCase) processSingleTarget(
	ctx context.Context,
	target channelTarget,
	existingIDs map[string]bool,
	mu *sync.Mutex,
	pending *[]pendingShort,
) {
	subCtx, cancel := context.WithTimeout(ctx, 6*time.Second)
	defer cancel()

	vids, err := uc.rssClient.FetchRecentVideos(subCtx, target.ChannelID)
	if err != nil || len(vids) == 0 {
		return
	}

	now := time.Now().UTC()
	for _, v := range vids {
		mu.Lock()
		already := existingIDs[v.VideoID]
		mu.Unlock()
		if already || isCommercialSpam(v.Title) || !uc.shortFormChecker(subCtx, v.VideoID, v.Title) {
			continue
		}

		pub := now
		if v.PublishedAt != nil {
			pub = *v.PublishedAt
		}
		hype := calculateHypeScore(v.Title, target.Name, pub, 0, 0)

		mu.Lock()
		if !existingIDs[v.VideoID] {
			existingIDs[v.VideoID] = true
			*pending = append(*pending, pendingShort{
				candidate: v,
				target:    target,
				hype:      hype,
				pub:       pub,
			})
		}
		mu.Unlock()
	}
}

func (uc *IngestShortsUseCase) isShortFormVideo(ctx context.Context, videoID, title string) bool {
	tLow := strings.ToLower(title)
	fillers := []string{
		"highlights", "resumen", "full match", "press conference", "interview",
		"extended", "samenvatting", "episode", "documentary", "podcast", "vlog",
	}
	for _, m := range fillers {
		if strings.Contains(tLow, m) {
			return false
		}
	}
	req, err := http.NewRequestWithContext(ctx, http.MethodHead, "https://www.youtube.com/shorts/"+videoID, nil)
	if err != nil {
		return false
	}
	req.Header.Set("User-Agent", "Mozilla/5.0")
	resp, err := uc.httpClient.Do(req)
	if err != nil {
		return false
	}
	_ = resp.Body.Close()
	return resp.StatusCode == http.StatusOK
}
