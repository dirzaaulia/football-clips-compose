package usecases

import (
	"context"
	"fmt"
	"log/slog"
	"net/http"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/domain"
)

type detailsEnricher interface {
	HasAPIKey() bool
	EnrichCandidateDetails(ctx context.Context, candidates []domain.VideoCandidate) ([]domain.VideoCandidate, error)
}

type pendingShort struct {
	candidate domain.VideoCandidate
	target    channelTarget
	hype      int
	pub       time.Time
}

func (uc *IngestShortsUseCase) filterEmbeddableShorts(
	ctx context.Context,
	pending []pendingShort,
	now time.Time,
) []domain.FootballShort {
	if len(pending) == 0 {
		return nil
	}
	if uc.enricher != nil && uc.enricher.HasAPIKey() {
		enriched, err := uc.enrichWithAPI(ctx, pending)
		if err == nil {
			return buildVerifiedShorts(pending, enriched, now)
		}
		uc.logger.Warn("[SHORTS_ENRICH_FALLBACK]", slog.String("err", err.Error()))
	}
	return uc.filterWithOEmbedFallback(ctx, pending, now)
}

func (uc *IngestShortsUseCase) enrichWithAPI(
	ctx context.Context,
	pending []pendingShort,
) ([]domain.VideoCandidate, error) {
	cands := make([]domain.VideoCandidate, len(pending))
	for i, p := range pending {
		c := p.candidate
		c.IsEmbeddable = false
		c.WatchabilityPct = 100.0
		cands[i] = c
	}
	return uc.enricher.EnrichCandidateDetails(ctx, cands)
}

func buildVerifiedShorts(
	pending []pendingShort,
	enriched []domain.VideoCandidate,
	now time.Time,
) []domain.FootballShort {
	var result []domain.FootballShort
	for i, p := range pending {
		c := enriched[i]
		if !c.IsEmbeddable || c.WatchabilityPct < 50.0 {
			continue
		}
		if c.DurationSeconds > 180 {
			continue
		}
		dur := c.DurationSeconds
		if dur <= 0 {
			dur = 45
		}
		result = append(result, toFootballShort(p, dur, now))
	}
	return result
}

func (uc *IngestShortsUseCase) filterWithOEmbedFallback(
	ctx context.Context,
	pending []pendingShort,
	now time.Time,
) []domain.FootballShort {
	var result []domain.FootballShort
	for _, p := range pending {
		if uc.isEmbeddableOEmbed(ctx, p.candidate.VideoID) {
			result = append(result, toFootballShort(p, 45, now))
		}
	}
	return result
}

func (uc *IngestShortsUseCase) isEmbeddableOEmbed(ctx context.Context, videoID string) bool {
	oembedURL := fmt.Sprintf(
		"https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=%s&format=json",
		videoID,
	)
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, oembedURL, nil)
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

func toFootballShort(p pendingShort, durationSeconds int, now time.Time) domain.FootballShort {
	return domain.FootballShort{
		YouTubeVideoID:  p.candidate.VideoID,
		Title:           p.candidate.Title,
		ChannelID:       p.target.ChannelID,
		ChannelName:     p.target.Name,
		ClubID:          p.target.ClubID,
		Category:        classifyShortCategory(p.candidate.Title),
		ThumbnailURL:    fmt.Sprintf("https://img.youtube.com/vi/%s/hqdefault.jpg", p.candidate.VideoID),
		DurationSeconds: durationSeconds,
		LikeCount:       0,
		HypeScore:       p.hype,
		Source:          "RSS",
		IsFeatured:      p.hype >= 85,
		PublishedAt:     p.pub,
		CreatedAt:       now,
	}
}
