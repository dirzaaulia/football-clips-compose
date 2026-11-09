package usecases

import (
	"context"
	"fmt"
	"log/slog"
	"strings"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/adapters/youtube"
	"github.com/dirzaaulia/footballclips/core-go/domain"
	"github.com/dirzaaulia/footballclips/core-go/ports"
)

// IngestShortsUseCase coordinates discovering, classifying, and ingesting vertical shorts clips.
type IngestShortsUseCase struct {
	rssClient  *youtube.RSSClient
	shortsRepo ports.ShortsRepository
	logger     *slog.Logger
}

// NewIngestShortsUseCase instantiates the shorts ingestion interactor.
func NewIngestShortsUseCase(
	rss *youtube.RSSClient,
	repo ports.ShortsRepository,
	logger *slog.Logger,
) *IngestShortsUseCase {
	if logger == nil {
		logger = slog.Default()
	}
	return &IngestShortsUseCase{
		rssClient:  rss,
		shortsRepo: repo,
		logger:     logger,
	}
}

// Execute discovers and ingests new vertical shorts from official club channels.
func (uc *IngestShortsUseCase) Execute(ctx context.Context, channelLimit int) (int, error) {
	existingIDs, err := uc.shortsRepo.GetExistingShortVideoIDs(ctx, 1000)
	if err != nil {
		return 0, fmt.Errorf("fetch existing shorts IDs: %w", err)
	}

	channels, err := uc.shortsRepo.GetActiveClubChannels(ctx, channelLimit)
	if err != nil {
		return 0, fmt.Errorf("fetch active club channels: %w", err)
	}

	var discovered []domain.FootballShort
	now := time.Now().UTC()

	for _, ch := range channels {
		vids, err := uc.rssClient.FetchRecentVideos(ctx, ch.ChannelID)
		if err != nil || len(vids) == 0 {
			continue
		}

		for _, v := range vids {
			if existingIDs[v.VideoID] {
				continue
			}

			if !isShortFormVideo(v.Title) {
				continue
			}

			if isCommercialSpam(v.Title) {
				continue
			}

			category := classifyShortCategory(v.Title)
			thumb := fmt.Sprintf("https://img.youtube.com/vi/%s/hqdefault.jpg", v.VideoID)
			pub := now
			if v.PublishedAt != nil {
				pub = *v.PublishedAt
			}

			clubID := ch.ClubID
			hype := calculateHypeScore(v.Title, ch.Name, pub)
			discovered = append(discovered, domain.FootballShort{
				YouTubeVideoID:  v.VideoID,
				Title:           v.Title,
				ChannelID:       ch.ChannelID,
				ChannelName:     ch.Name,
				ClubID:          &clubID,
				Category:        category,
				ThumbnailURL:    thumb,
				DurationSeconds: 45,
				LikeCount:       0,
				HypeScore:       hype,
				Source:          "RSS",
				IsFeatured:      hype >= 80,
				PublishedAt:     pub,
				CreatedAt:       now,
			})
			existingIDs[v.VideoID] = true
		}
	}

	var checkedClubIDs []int64
	for _, ch := range channels {
		checkedClubIDs = append(checkedClubIDs, ch.ClubID)
	}
	_ = uc.shortsRepo.MarkChannelsChecked(ctx, checkedClubIDs)

	if len(discovered) == 0 {
		return 0, nil
	}

	if err := uc.shortsRepo.UpsertShorts(ctx, discovered); err != nil {
		return 0, fmt.Errorf("upsert shorts: %w", err)
	}

	uc.logger.Info("[SHORTS_INGESTED]", slog.Int("count", len(discovered)))
	return len(discovered), nil
}

func isShortFormVideo(title string) bool {
	tLow := strings.ToLower(title)
	if strings.Contains(tLow, "#shorts") || strings.Contains(tLow, "#short") {
		return true
	}
	longMarkers := []string{
		"highlights", "resumen", "full match", "press conference",
		"interview", "extended", "samenvatting", "episode", "documentary",
	}
	for _, m := range longMarkers {
		if strings.Contains(tLow, m) {
			return false
		}
	}
	if len(title) <= 85 {
		shortMarkers := []string{"goal", "skill", "save", "gol", "assist", "nutmeg", "strike", "curler", "moment"}
		for _, sm := range shortMarkers {
			if strings.Contains(tLow, sm) {
				return true
			}
		}
	}
	return false
}

func isCommercialSpam(title string) bool {
	tLow := strings.ToLower(title)
	spamWords := []string{"merchandise", "kit reveal", "buy tickets", "shop now", "sponsor", "discount", "code"}
	for _, w := range spamWords {
		if strings.Contains(tLow, w) {
			return true
		}
	}
	return false
}

func classifyShortCategory(title string) string {
	tLow := strings.ToLower(title)
	if strings.Contains(tLow, "training") || strings.Contains(tLow, "tunnel") || strings.Contains(tLow, "behind the scenes") || strings.Contains(tLow, "arrival") {
		return "BEHIND_THE_SCENES"
	}
	if strings.Contains(tLow, "moment") || strings.Contains(tLow, "celebration") || strings.Contains(tLow, "fans") {
		return "MOMENTS"
	}
	return "GOALS_SKILLS"
}

func calculateHypeScore(title, channelName string, publishedAt time.Time) int {
	score := 50
	tLow := strings.ToLower(title)
	cLow := strings.ToLower(channelName)

	superExciting := []string{"golazo", "screamer", "banger", "curler", "rocket", "impossible", "insane"}
	for _, kw := range superExciting {
		if strings.Contains(tLow, kw) {
			score += 25
			break
		}
	}

	exciting := []string{"goal", "gol", "strike", "assist", "nutmeg", "dribble", "save", "tekkers", "winner"}
	for _, kw := range exciting {
		if strings.Contains(tLow, kw) {
			score += 15
			break
		}
	}

	tier1Clubs := []string{
		"real madrid", "barcelona", "arsenal", "manchester city", "liverpool",
		"manchester united", "chelsea", "bayern", "juventus", "inter", "milan", "psg",
	}
	for _, club := range tier1Clubs {
		if strings.Contains(cLow, club) || strings.Contains(tLow, club) {
			score += 20
			break
		}
	}

	if time.Since(publishedAt).Hours() < 24.0 {
		score += 10
	} else if time.Since(publishedAt).Hours() < 72.0 {
		score += 5
	}

	if score > 100 {
		return 100
	}
	if score < 10 {
		return 10
	}
	return score
}
