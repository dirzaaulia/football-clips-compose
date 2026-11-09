package usecases_test

import (
	"context"
	"testing"

	"github.com/dirzaaulia/footballclips/core-go/adapters/espn"
	"github.com/dirzaaulia/footballclips/core-go/domain"
	"github.com/dirzaaulia/footballclips/core-go/usecases"
)

type mockStandingsRepo struct {
	upserted []domain.StandingEntry
}

func (m *mockStandingsRepo) UpsertStandings(ctx context.Context, entries []domain.StandingEntry) error {
	m.upserted = append(m.upserted, entries...)
	return nil
}

func (m *mockStandingsRepo) GetTeamFormFromMatches(ctx context.Context, compCode string) (map[string]string, error) {
	return map[string]string{
		"arsenal": "W,W,D,W,L",
	}, nil
}

func TestSyncStandingsUseCase(t *testing.T) {
	client := espn.NewClient()
	repo := &mockStandingsRepo{}
	uc := usecases.NewSyncStandingsUseCase(client, repo, nil)

	_ = uc
}
