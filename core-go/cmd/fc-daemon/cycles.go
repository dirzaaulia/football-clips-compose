package main

import (
	"context"
	"log/slog"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/adapters/edge"
	"github.com/dirzaaulia/footballclips/core-go/adapters/postgres"
	"github.com/dirzaaulia/footballclips/core-go/domain"
	"github.com/dirzaaulia/footballclips/core-go/usecases"
)

func runSyncCycle(
	ctx context.Context,
	uc *usecases.SyncScoresUseCase,
	comps []string,
	purge *edge.PurgeClient,
	logger *slog.Logger,
) {
	cycleCtx, cancel := context.WithTimeout(ctx, 45*time.Second)
	defer cancel()

	today := time.Now().UTC().Format("2006-01-02")
	report, err := uc.Execute(cycleCtx, comps, today, today)
	if err != nil {
		logger.Error("[SYNC_CYCLE_ERROR] Cycle execution failed", slog.String("err", err.Error()))
		return
	}

	if report.LiveMatches > 0 && purge != nil {
		purge.PurgeMatchesCache(cycleCtx)
	}

	logger.Info("[SYNC_CYCLE_OK]",
		slog.Int("total", report.TotalIngested),
		slog.Int("live", report.LiveMatches),
		slog.Int("parity", report.ParityMatches),
		slog.Int("drift", report.DriftCount),
		slog.Int64("duration_ms", report.DurationMs),
	)
}

func runHighlightCycle(
	ctx context.Context,
	repo *postgres.Repository,
	uc *usecases.FindHighlightUseCase,
	purge *edge.PurgeClient,
	logger *slog.Logger,
) {
	cycleCtx, cancel := context.WithTimeout(ctx, 120*time.Second)
	defer cancel()

	pending, err := repo.GetPendingHighlights(cycleCtx, 25)
	if err != nil {
		logger.Error("[HL_CYCLE_ERROR] Failed to get pending highlights", slog.String("err", err.Error()))
		return
	}
	if len(pending) == 0 {
		return
	}

	sorted := domain.SortMatchesByPriority(pending, time.Now().UTC())
	limit := 5
	if len(sorted) < limit {
		limit = len(sorted)
	}

	var cooldownCount, processedCount, linkedCount int
	for _, m := range sorted[:limit] {
		trace, err := uc.Execute(cycleCtx, m)
		if err != nil {
			logger.Warn("[HL_CYCLE_WARN]", slog.Int64("match_id", m.ID), slog.String("err", err.Error()))
			continue
		}
		if trace != nil {
			if trace.Outcome == "COOLDOWN_PROTECTED" {
				cooldownCount++
			} else {
				processedCount++
				if trace.Outcome == "LINKED" {
					linkedCount++
				}
				logger.Info("[HL_CYCLE_RESULT]",
					slog.Int64("match_id", m.ID),
					slog.String("outcome", trace.Outcome),
					slog.Int("candidates", len(trace.Candidates)),
					slog.Int64("duration_ms", trace.DurationMs),
				)
			}
		}
	}

	if linkedCount > 0 && purge != nil {
		purge.PurgeMatchesCache(cycleCtx)
	}

	if cooldownCount > 0 {
		logger.Info("[HL_COOLDOWN_SUMMARY]", slog.Int("protected_matches", cooldownCount), slog.Int("evaluated_matches", processedCount))
	}
}

func runShortsCycle(ctx context.Context, uc *usecases.IngestShortsUseCase, logger *slog.Logger) {
	cycleCtx, cancel := context.WithTimeout(ctx, 60*time.Second)
	defer cancel()

	count, err := uc.Execute(cycleCtx, 50)
	if err != nil {
		logger.Warn("[SHORTS_CYCLE_WARN]", slog.String("err", err.Error()))
		return
	}
	if count > 0 {
		logger.Info("[SHORTS_CYCLE_OK]", slog.Int("ingested", count))
	}
}

func runStandingsCycle(ctx context.Context, uc *usecases.SyncStandingsUseCase, comps []string, logger *slog.Logger) {
	cycleCtx, cancel := context.WithTimeout(ctx, 90*time.Second)
	defer cancel()

	total, err := uc.Execute(cycleCtx, comps, 2026)
	if err != nil {
		logger.Warn("[STANDINGS_CYCLE_WARN]", slog.String("err", err.Error()))
		return
	}
	logger.Info("[STANDINGS_CYCLE_OK]", slog.Int("total_clubs", total))
}
