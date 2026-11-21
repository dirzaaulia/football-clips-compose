package usecases_test

import (
	"context"
	"testing"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/domain"
	"github.com/dirzaaulia/footballclips/core-go/usecases"
)

type mockDiscovery struct {
	candidates []domain.VideoCandidate
}

func (m *mockDiscovery) DiscoverCandidates(ctx context.Context, match domain.Match, channelIDs []string) ([]domain.VideoCandidate, error) {
	return m.candidates, nil
}

func (m *mockDiscovery) EnrichCandidateDetails(ctx context.Context, candidates []domain.VideoCandidate) ([]domain.VideoCandidate, error) {
	for i := range candidates {
		if candidates[i].WatchabilityPct == 0 {
			candidates[i].WatchabilityPct = 100.0
		}
	}
	return candidates, nil
}

func (m *mockDiscovery) GetRemainingQuota() int {
	return 50000
}

type mockAIGateway struct {
	evaluation *domain.CandidateEvaluation
}

func (m *mockAIGateway) EvaluateCandidates(ctx context.Context, match domain.Match, candidates []domain.VideoCandidate) (*domain.CandidateEvaluation, error) {
	return m.evaluation, nil
}

func (m *mockAIGateway) IsCircuitOpen() bool {
	return false
}

func TestFindHighlightShadowMode(t *testing.T) {
	ctx := context.Background()
	match := domain.Match{
		ID:           991,
		Status:       "FINISHED",
		HomeTeamName: "Arsenal",
		AwayTeamName: "Chelsea",
		UTCDate:      time.Now().UTC(),
	}

	candidates := []domain.VideoCandidate{
		{VideoID: "vid_arsenal_chelsea", Title: "Arsenal vs Chelsea Highlights"},
	}
	eval := &domain.CandidateEvaluation{
		VideoID:   "vid_arsenal_chelsea",
		Verdict:   "ACCEPTED",
		Score:     95,
		Evaluator: "ORACLE_GATEWAY",
	}

	disc := &mockDiscovery{candidates: candidates}
	aigw := &mockAIGateway{evaluation: eval}
	repo := &mockMatchRepo{}

	uc := usecases.NewFindHighlightUseCase(disc, aigw, repo, nil, true) // Shadow Mode = TRUE

	trace, err := uc.Execute(ctx, match)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}

	if trace.Outcome != "LINKED" {
		t.Errorf("expected outcome LINKED, got %s", trace.Outcome)
	}
	if *trace.LinkedVideoID != "vid_arsenal_chelsea" {
		t.Errorf("expected video ID vid_arsenal_chelsea, got %v", trace.LinkedVideoID)
	}
}

func TestFindHighlightActiveMode(t *testing.T) {
	ctx := context.Background()
	match := domain.Match{
		ID:           992,
		Status:       "FINISHED",
		HomeTeamName: "Liverpool",
		AwayTeamName: "Real Madrid",
		UTCDate:      time.Now().UTC(),
	}

	candidates := []domain.VideoCandidate{
		{VideoID: "vid_liv_rm", Title: "Liverpool vs Real Madrid Highlights"},
	}
	eval := &domain.CandidateEvaluation{
		VideoID:   "vid_liv_rm",
		Verdict:   "ACCEPTED",
		Score:     98,
		Evaluator: "ORACLE_GATEWAY",
	}

	disc := &mockDiscovery{candidates: candidates}
	aigw := &mockAIGateway{evaluation: eval}
	repo := &mockMatchRepo{}

	uc := usecases.NewFindHighlightUseCase(disc, aigw, repo, nil, false) // Shadow Mode = FALSE

	trace, err := uc.Execute(ctx, match)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}

	if trace.Outcome != "LINKED" {
		t.Errorf("expected outcome LINKED, got %s", trace.Outcome)
	}
}

func TestFindHighlight_UpgradeWorldwide(t *testing.T) {
	ctx := context.Background()
	currentVid := "old_vid_uk_only"
	match := domain.Match{
		ID:               993,
		Status:           "FINISHED",
		HomeTeamName:     "Bayern Munich",
		AwayTeamName:     "FC Cologne",
		UTCDate:          time.Now().UTC(),
		HighlightVideoID: &currentVid,
		RegionInfo: &domain.RegionInfo{
			SweepCount: 2,
			Blocked:    []string{"US", "ID", "DE", "FR", "ES"}, // Has geo deficit (<99%)
		},
	}

	betterCand := domain.VideoCandidate{
		VideoID:         "new_vid_worldwide",
		Title:           "Bayern Munich vs FC Cologne Highlights",
		Tier:            1,
		WatchabilityPct: 100.0,
		DurationSeconds: 600,
	}
	eval := &domain.CandidateEvaluation{
		VideoID:   "new_vid_worldwide",
		Verdict:   "ACCEPTED",
		Score:     99,
		Evaluator: "ORACLE_GATEWAY",
	}

	disc := &mockDiscovery{candidates: []domain.VideoCandidate{betterCand}}
	aigw := &mockAIGateway{evaluation: eval}
	repo := &mockMatchRepo{}

	uc := usecases.NewFindHighlightUseCase(disc, aigw, repo, nil, false)
	trace, err := uc.Execute(ctx, match)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}

	if trace.Outcome != "LINKED" {
		t.Errorf("expected outcome LINKED, got %s", trace.Outcome)
	}
	if trace.LinkedVideoID == nil || *trace.LinkedVideoID != "new_vid_worldwide" {
		t.Errorf("expected upgrade to new_vid_worldwide, got %v", trace.LinkedVideoID)
	}
}

func TestIsBetterHighlight(t *testing.T) {
	currentVid := "curr123"
	baseMatch := domain.Match{
		HighlightVideoID: &currentVid,
		RegionInfo: &domain.RegionInfo{
			Tier:            2,
			WatchabilityPct: 75.0,
			DurationSeconds: 120,
		},
	}

	tests := []struct {
		name       string
		candidate  domain.VideoCandidate
		wantBetter bool
	}{
		{
			name: "Worldwide upgrade to >=99%",
			candidate: domain.VideoCandidate{
				VideoID:         "better1",
				Tier:            2,
				WatchabilityPct: 99.5,
				DurationSeconds: 120,
			},
			wantBetter: true,
		},
		{
			name: "Authority Tier upgrade (Tier 1 vs Tier 2)",
			candidate: domain.VideoCandidate{
				VideoID:         "better2",
				Tier:            1,
				WatchabilityPct: 75.0,
				DurationSeconds: 120,
			},
			wantBetter: true,
		},
		{
			name: "Extended duration upgrade (8m vs 2m)",
			candidate: domain.VideoCandidate{
				VideoID:         "better3",
				Tier:            2,
				WatchabilityPct: 75.0,
				DurationSeconds: 600,
			},
			wantBetter: true,
		},
		{
			name: "Inferior candidate rejected",
			candidate: domain.VideoCandidate{
				VideoID:         "worse1",
				Tier:            3,
				WatchabilityPct: 50.0,
				DurationSeconds: 90,
			},
			wantBetter: false,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			got, _ := domain.IsBetterHighlight(tt.candidate, baseMatch)
			if got != tt.wantBetter {
				t.Errorf("IsBetterHighlight() = %v, want %v", got, tt.wantBetter)
			}
		})
	}
}
