package usecases_test

import (
	"context"
	"testing"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/domain"
	"github.com/dirzaaulia/footballclips/core-go/usecases"
)

type mockShortsRepo struct {
	existing map[string]bool
	upserted []domain.FootballShort
}

func (m *mockShortsRepo) GetExistingShortVideoIDs(ctx context.Context, limit int) (map[string]bool, error) {
	if m.existing == nil {
		m.existing = make(map[string]bool)
	}
	return m.existing, nil
}

func (m *mockShortsRepo) UpsertShorts(ctx context.Context, shorts []domain.FootballShort) error {
	m.upserted = append(m.upserted, shorts...)
	return nil
}

func (m *mockShortsRepo) GetActiveClubChannels(ctx context.Context, limit int) ([]struct {
	ClubID    int64
	Name      string
	ChannelID string
}, error) {
	return []struct {
		ClubID    int64
		Name      string
		ChannelID string
	}{
		{ClubID: 1, Name: "Arsenal", ChannelID: "UC_arsenal"},
	}, nil
}

func (m *mockShortsRepo) GetBroadcasterAndLeagueChannels(ctx context.Context) ([]struct {
	Name      string
	ChannelID string
}, error) {
	return nil, nil
}

func (m *mockShortsRepo) MarkChannelsChecked(ctx context.Context, clubIDs []int64) error {
	return nil
}

func (m *mockShortsRepo) PruneStaleShorts(ctx context.Context, maxAgeDays int, maxCapacity int) (int64, error) {
	return 0, nil
}

type mockShortsRSS struct {
	videos []domain.VideoCandidate
}

func (m *mockShortsRSS) FetchRecentVideos(ctx context.Context, channelID string) ([]domain.VideoCandidate, error) {
	return m.videos, nil
}

type mockEnricher struct {
	results map[string]domain.VideoCandidate
}

func (m *mockEnricher) HasAPIKey() bool { return true }

func (m *mockEnricher) EnrichCandidateDetails(
	ctx context.Context,
	candidates []domain.VideoCandidate,
) ([]domain.VideoCandidate, error) {
	out := make([]domain.VideoCandidate, len(candidates))
	for i, c := range candidates {
		if r, ok := m.results[c.VideoID]; ok {
			out[i] = r
		} else {
			out[i] = c
		}
	}
	return out, nil
}

func TestIngestShortsEmbeddableFilter(t *testing.T) {
	now := time.Now().UTC()
	rss := &mockShortsRSS{
		videos: []domain.VideoCandidate{
			{VideoID: "vid_ok", Title: "Amazing Saka Golazo", PublishedAt: &now},
			{VideoID: "vid_noembed", Title: "Rice Strike", PublishedAt: &now},
			{VideoID: "vid_geoblock", Title: "Odegaard Assist", PublishedAt: &now},
			{VideoID: "vid_toolong", Title: "Training Session", PublishedAt: &now},
			{VideoID: "vid_embed_unplayable", Title: "UEFA Golazo", PublishedAt: &now},
			{VideoID: "vid_fake_hashtag", Title: "Full Clip #shorts", PublishedAt: &now},
		},
	}
	enricher := &mockEnricher{
		results: map[string]domain.VideoCandidate{
			"vid_ok":               {VideoID: "vid_ok", IsEmbeddable: true, WatchabilityPct: 100.0, DurationSeconds: 32, ViewCount: 850000, LikeCount: 45000},
			"vid_noembed":          {VideoID: "vid_noembed", IsEmbeddable: false, WatchabilityPct: 100.0, DurationSeconds: 28},
			"vid_geoblock":         {VideoID: "vid_geoblock", IsEmbeddable: true, WatchabilityPct: 15.0, DurationSeconds: 25},
			"vid_toolong":          {VideoID: "vid_toolong", IsEmbeddable: true, WatchabilityPct: 100.0, DurationSeconds: 240},
			"vid_embed_unplayable": {VideoID: "vid_embed_unplayable", IsEmbeddable: true, WatchabilityPct: 100.0, DurationSeconds: 30},
		},
	}
	repo := &mockShortsRepo{}
	uc := usecases.NewIngestShortsUseCase(rss, repo, nil).
		WithEnricher(enricher).
		WithShortFormChecker(func(_ context.Context, vid, _ string) bool {
			return vid != "vid_fake_hashtag"
		}).
		WithEmbedPageChecker(func(_ context.Context, vid string) bool {
			return vid != "vid_embed_unplayable"
		})

	count, err := uc.Execute(context.Background(), 10)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if count != 1 || len(repo.upserted) != 1 {
		t.Fatalf("expected 1 embeddable short, got count=%d upserted=%d", count, len(repo.upserted))
	}
	got := repo.upserted[0]
	if got.YouTubeVideoID != "vid_ok" || got.DurationSeconds != 32 || got.LikeCount != 45000 || got.HypeScore < 90 {
		t.Fatalf("unexpected upserted short: %+v", got)
	}
	if usecases.ClassifyHypeLevel(got.HypeScore) != "LEGENDARY" {
		t.Fatalf("expected LEGENDARY hype level for score=%d", got.HypeScore)
	}
}
