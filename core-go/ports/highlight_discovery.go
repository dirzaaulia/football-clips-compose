package ports

import (
	"context"

	"github.com/dirzaaulia/footballclips/core-go/domain"
)

// HighlightDiscovery defines the contract for discovering video candidates from YouTube.
type HighlightDiscovery interface {
	DiscoverCandidates(ctx context.Context, match domain.Match, channelIDs []string) ([]domain.VideoCandidate, error)
	GetRemainingQuota() int
}
