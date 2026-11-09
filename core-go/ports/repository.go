package ports

import (
	"context"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/domain"
)

// MatchRepository defines the data access contract for match entities.
type MatchRepository interface {
	GetMatchesForSync(ctx context.Context, windowStart, windowEnd time.Time) ([]domain.Match, error)
	UpsertMatches(ctx context.Context, matches []domain.Match) error
	GetPendingHighlights(ctx context.Context, limit int) ([]domain.Match, error)
	UpdateMatchHighlight(ctx context.Context, matchID int64, videoID string, reg *domain.RegionInfo, videoMeta ...map[string]any) error
	UpdateMatchCheck(ctx context.Context, matchID int64, reg *domain.RegionInfo) error
	MarkMatchUnavailable(ctx context.Context, matchID int64, reason string, reg *domain.RegionInfo) error
	SaveWaterfallTrace(ctx context.Context, trace domain.MatchWaterfallTrace) error
	GetMatchChannels(ctx context.Context, match domain.Match) ([]string, error)
}

// TelemetryRepository defines the data access contract for telemetry states.
type TelemetryRepository interface {
	SaveTelemetryState(ctx context.Context, payload domain.CycleTelemetryPayload) error
	RecordEvent(ctx context.Context, event domain.EventRecord) error
}

// StandingsRepository defines the data access contract for league standings.
type StandingsRepository interface {
	UpsertStandings(ctx context.Context, entries []domain.StandingEntry) error
	GetTeamFormFromMatches(ctx context.Context, compCode string) (map[string]string, error)
}

// ShortsRepository defines the data access contract for football shorts.
type ShortsRepository interface {
	GetExistingShortVideoIDs(ctx context.Context, limit int) (map[string]bool, error)
	UpsertShorts(ctx context.Context, shorts []domain.FootballShort) error
	GetActiveClubChannels(ctx context.Context, limit int) ([]struct {
		ClubID    int64
		Name      string
		ChannelID string
	}, error)
	MarkChannelsChecked(ctx context.Context, clubIDs []int64) error
}

