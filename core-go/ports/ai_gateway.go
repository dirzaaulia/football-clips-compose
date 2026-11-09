package ports

import (
	"context"

	"github.com/dirzaaulia/footballclips/core-go/domain"
)

// AIGateway defines the contract for AI evaluation and reranking of highlight candidates.
type AIGateway interface {
	EvaluateCandidates(ctx context.Context, match domain.Match, candidates []domain.VideoCandidate) (*domain.CandidateEvaluation, error)
	IsCircuitOpen() bool
}
