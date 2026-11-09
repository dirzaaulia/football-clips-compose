package usecases_test

import (
	"context"
	"testing"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/domain"
	"github.com/dirzaaulia/footballclips/core-go/ports"
	"github.com/dirzaaulia/footballclips/core-go/usecases"
)

type mockScoreProvider struct {
	matches []domain.Match
}

func (m *mockScoreProvider) FetchScoreboard(ctx context.Context, compCodes []string, dateFrom, dateTo string) ([]domain.Match, error) {
	return m.matches, nil
}

func (m *mockScoreProvider) FetchMatchSummary(ctx context.Context, eventID int64, competitionCode string) (map[string]any, error) {
	return map[string]any{}, nil
}

type mockMatchRepo struct {
	upsertCalled   bool
	upsertedMatches []domain.Match
	dbMatches       []domain.Match
}

var _ ports.MatchRepository = (*mockMatchRepo)(nil)

func (m *mockMatchRepo) GetMatchesForSync(ctx context.Context, start, end time.Time) ([]domain.Match, error) {
	return m.dbMatches, nil
}

func (m *mockMatchRepo) UpsertMatches(ctx context.Context, matches []domain.Match) error {
	m.upsertCalled = true
	m.upsertedMatches = matches
	return nil
}

func (m *mockMatchRepo) GetPendingHighlights(ctx context.Context, limit int) ([]domain.Match, error) {
	return nil, nil
}

func (m *mockMatchRepo) UpdateMatchHighlight(ctx context.Context, matchID int64, videoID string, reg *domain.RegionInfo, videoMeta ...map[string]any) error {
	return nil
}

func (m *mockMatchRepo) UpdateMatchCheck(ctx context.Context, matchID int64, reg *domain.RegionInfo) error {
	return nil
}

func (m *mockMatchRepo) MarkMatchUnavailable(ctx context.Context, matchID int64, reason string, reg *domain.RegionInfo) error {
	return nil
}

func (m *mockMatchRepo) SaveWaterfallTrace(ctx context.Context, trace domain.MatchWaterfallTrace) error {
	return nil
}

func (m *mockMatchRepo) GetMatchChannels(ctx context.Context, match domain.Match) ([]string, error) {
	return []string{"UC_arsenal", "UC_chelsea"}, nil
}

func TestSyncScoresShadowMode(t *testing.T) {
	ctx := context.Background()
	now := time.Now().UTC()

	incoming := []domain.Match{
		{ID: 101, Status: "FINISHED", UTCDate: now},
		{ID: 102, Status: "IN_PLAY", UTCDate: now},
	}
	dbExisting := []domain.Match{
		{ID: 101, Status: "FINISHED", UTCDate: now},
		{ID: 102, Status: "TIMED", UTCDate: now}, // Status drift
	}

	sp := &mockScoreProvider{matches: incoming}
	repo := &mockMatchRepo{dbMatches: dbExisting}
	uc := usecases.NewSyncScoresUseCase(sp, repo, nil, true) // Shadow Mode = TRUE

	report, err := uc.Execute(ctx, []string{"PL"}, "2026-11-08", "2026-11-08")
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}

	if repo.upsertCalled {
		t.Errorf("upsert should NOT be called in shadow mode")
	}
	if report.TotalIngested != 2 {
		t.Errorf("expected 2 matches, got %d", report.TotalIngested)
	}
	if report.ParityMatches != 1 {
		t.Errorf("expected 1 parity match, got %d", report.ParityMatches)
	}
	if report.DriftCount != 1 {
		t.Errorf("expected 1 drift match, got %d", report.DriftCount)
	}
}

func TestSyncScoresActiveMode(t *testing.T) {
	ctx := context.Background()
	now := time.Now().UTC()

	incoming := []domain.Match{
		{ID: 201, Status: "IN_PLAY", UTCDate: now},
	}

	sp := &mockScoreProvider{matches: incoming}
	repo := &mockMatchRepo{}
	uc := usecases.NewSyncScoresUseCase(sp, repo, nil, false) // Shadow Mode = FALSE

	report, err := uc.Execute(ctx, []string{"PL"}, "2026-11-08", "2026-11-08")
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}

	if !repo.upsertCalled {
		t.Errorf("upsert should be called in active mode")
	}
	if len(repo.upsertedMatches) != 1 {
		t.Errorf("expected 1 upserted match, got %d", len(repo.upsertedMatches))
	}
	if report.LiveMatches != 1 {
		t.Errorf("expected 1 live match, got %d", report.LiveMatches)
	}
}
