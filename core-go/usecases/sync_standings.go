package usecases

import (
	"context"
	"log/slog"
	"strings"

	"github.com/dirzaaulia/footballclips/core-go/adapters/espn"
	"github.com/dirzaaulia/footballclips/core-go/domain"
	"github.com/dirzaaulia/footballclips/core-go/ports"
)

// SyncStandingsUseCase coordinates downloading, form enrichment, and upserting league tables.
type SyncStandingsUseCase struct {
	espnClient    *espn.Client
	standingsRepo ports.StandingsRepository
	logger        *slog.Logger
}

// NewSyncStandingsUseCase instantiates the standings synchronization interactor.
func NewSyncStandingsUseCase(
	client *espn.Client,
	repo ports.StandingsRepository,
	logger *slog.Logger,
) *SyncStandingsUseCase {
	if logger == nil {
		logger = slog.Default()
	}
	return &SyncStandingsUseCase{
		espnClient:    client,
		standingsRepo: repo,
		logger:        logger,
	}
}

// Execute synchronizes standings tables for the specified competitions.
func (uc *SyncStandingsUseCase) Execute(ctx context.Context, compCodes []string, season int) (int, error) {
	totalRows := 0

	for _, comp := range compCodes {
		entries, err := uc.espnClient.FetchStandings(ctx, comp, season)
		if err != nil {
			uc.logger.Warn("[STANDINGS_WARN] Failed to fetch standings", slog.String("comp", comp), slog.String("err", err.Error()))
			continue
		}

		if len(entries) == 0 {
			continue
		}

		// Enrich 5-match letter form (W,D,L) from finished matches in DB
		formMap, err := uc.standingsRepo.GetTeamFormFromMatches(ctx, comp)
		if err == nil && len(formMap) > 0 {
			enrichForm(entries, formMap)
		}

		if err := uc.standingsRepo.UpsertStandings(ctx, entries); err != nil {
			uc.logger.Error("[STANDINGS_ERROR] Failed to upsert standings", slog.String("comp", comp), slog.String("err", err.Error()))
			continue
		}

		totalRows += len(entries)
		uc.logger.Info("[STANDINGS_OK]", slog.String("comp", comp), slog.Int("clubs", len(entries)))
	}

	return totalRows, nil
}

func enrichForm(entries []domain.StandingEntry, formMap map[string]string) {
	for i := range entries {
		tNameLow := strings.ToLower(strings.TrimSpace(entries[i].TeamName))
		if form, ok := formMap[tNameLow]; ok && form != "" {
			entries[i].Form = &form
		}
	}
}
