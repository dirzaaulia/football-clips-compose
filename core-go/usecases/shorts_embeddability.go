package usecases

import (
	"context"
	"fmt"
	"io"
	"log/slog"
	"net/http"
	"strings"
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
			return uc.buildVerifiedShorts(ctx, pending, enriched, now)
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

func (uc *IngestShortsUseCase) buildVerifiedShorts(
	ctx context.Context,
	pending []pendingShort,
	enriched []domain.VideoCandidate,
	now time.Time,
) []domain.FootballShort {
	var result []domain.FootballShort
	for i, p := range pending {
		c := enriched[i]
		if !c.IsEmbeddable || c.WatchabilityPct < 50.0 || c.DurationSeconds > 180 {
			continue
		}
		if uc.embedPageChecker != nil && !uc.embedPageChecker(ctx, c.VideoID) {
			continue
		}
		p.hype = calculateHypeScore(p.candidate.Title, p.target.Name, p.pub, c.ViewCount, c.LikeCount)
		if p.hype < 55 {
			continue
		}
		dur := c.DurationSeconds
		if dur <= 0 {
			dur = 45
		}
		result = append(result, toFootballShort(p, dur, int(c.LikeCount), now))
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
		if p.hype < 55 || !uc.isEmbeddableOEmbed(ctx, p.candidate.VideoID) {
			continue
		}
		if uc.embedPageChecker != nil && !uc.embedPageChecker(ctx, p.candidate.VideoID) {
			continue
		}
		result = append(result, toFootballShort(p, 45, 0, now))
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

func (uc *IngestShortsUseCase) isEmbedPagePlayable(ctx context.Context, videoID string) bool {
	embedURL := "https://www.youtube.com/embed/" + videoID
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, embedURL, nil)
	if err != nil {
		return false
	}
	req.Header.Set("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36")
	req.Header.Set("Referer", "https://footballclips.web.app/")
	resp, err := uc.httpClient.Do(req)
	if err != nil {
		return false
	}
	defer resp.Body.Close()
	if resp.StatusCode != http.StatusOK {
		return false
	}
	bodyBytes, err := io.ReadAll(io.LimitReader(resp.Body, 512*1024))
	if err != nil {
		return false
	}
	html := string(bodyBytes)
	blockedMarkers := []string{
		"UNPLAYABLE",
		"LOGIN_REQUIRED",
		`"errorScreen"`,
		"Playback on other websites has been disabled",
		"Video unavailable",
	}
	for _, marker := range blockedMarkers {
		if strings.Contains(html, marker) {
			return false
		}
	}
	return true
}

func toFootballShort(p pendingShort, durationSeconds, likeCount int, now time.Time) domain.FootballShort {
	return domain.FootballShort{
		YouTubeVideoID:  p.candidate.VideoID,
		Title:           p.candidate.Title,
		ChannelID:       p.target.ChannelID,
		ChannelName:     p.target.Name,
		ClubID:          p.target.ClubID,
		Category:        classifyShortCategory(p.candidate.Title),
		ThumbnailURL:    fmt.Sprintf("https://img.youtube.com/vi/%s/hqdefault.jpg", p.candidate.VideoID),
		DurationSeconds: durationSeconds,
		LikeCount:       likeCount,
		HypeScore:       p.hype,
		Source:          "RSS",
		IsFeatured:      p.hype >= 85,
		PublishedAt:     p.pub,
		CreatedAt:       now,
	}
}
