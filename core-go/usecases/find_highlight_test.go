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
