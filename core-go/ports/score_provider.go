package ports

import (
	"context"

	"github.com/dirzaaulia/footballclips/core-go/domain"
)

// ScoreProvider defines the external contract for retrieving fixtures and live scores.
type ScoreProvider interface {
	FetchScoreboard(ctx context.Context, competitions []string, dateFrom, dateTo string) ([]domain.Match, error)
	FetchMatchSummary(ctx context.Context, eventID int64, competitionCode string) (map[string]any, error)
}
