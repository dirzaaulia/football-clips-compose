package main

import (
	"context"
	"flag"
	"fmt"
	"log/slog"
	"os"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/adapters/aigw"
	"github.com/dirzaaulia/footballclips/core-go/adapters/espn"
	"github.com/dirzaaulia/footballclips/core-go/adapters/postgres"
	"github.com/dirzaaulia/footballclips/core-go/adapters/youtube"
	"github.com/dirzaaulia/footballclips/core-go/domain"
	"github.com/dirzaaulia/footballclips/core-go/usecases"
)

func main() {
	fromFlag := flag.String("from", "2026-11-11", "Start date (YYYY-MM-DD)")
	toFlag := flag.String("to", "2026-11-20", "End date (YYYY-MM-DD)")
	testHighlightsFlag := flag.Bool("highlights", true, "Execute highlight discovery on finished matches")
	maxHighlightsFlag := flag.Int("max-highlights", 100, "Maximum pending matches to discover highlights for")
	dryRunFlag := flag.Bool("dry-run", false, "Simulate scoreboard fetch without writing to DB")
	flag.Parse()

	logger := slog.New(slog.NewTextHandler(os.Stdout, &slog.HandlerOptions{Level: slog.LevelInfo}))
	slog.SetDefault(logger)

	dsn := os.Getenv("DATABASE_URL")
	if dsn == "" {
		dsn = os.Getenv("FC_DATABASE_DSN")
	}

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Minute)
	defer cancel()

	intComps := []string{"UNL", "WCQ_CONMEBOL", "WCQ_AFC", "WCQ_UEFA", "CNL", "WCQ_CONCACAF", "WCQ_CAF", "FRI"}
	espnClient := espn.NewClient()

	if *dryRunFlag || dsn == "" {
		logger.Info("[DRY_RUN_MODE] Running match ingestion simulation directly from ESPN API",
			slog.String("from", *fromFlag),
			slog.String("to", *toFlag),
			slog.Int("competitions", len(intComps)),
		)

		matches, err := espnClient.FetchScoreboard(ctx, intComps, *fromFlag, *toFlag)
		if err != nil {
			logger.Error("[ESPN_FETCH_ERROR] Scoreboard fetch failed", slog.String("err", err.Error()))
			os.Exit(1)
		}

		logger.Info("[ESPN_FETCH_SUCCESS]",
			slog.Int("total_matches_found", len(matches)),
		)

		var finished []domain.Match
		for i, m := range matches {
			if m.IsFinished() {
				finished = append(finished, m)
			}
			if i < 15 || m.IsFinished() {
				logger.Info("[MATCH_DETAIL]",
					slog.Int64("id", m.ID),
					slog.String("competition", m.CompetitionID),
					slog.String("home", m.HomeTeamName),
					slog.String("away", m.AwayTeamName),
					slog.String("status", m.Status),
					slog.String("utc_date", m.UTCDate.Format(time.RFC3339)),
				)
			}
		}

		logger.Info("[SUMMARY]",
			slog.Int("total", len(matches)),
			slog.Int("finished", len(finished)),
		)
		return
	}

	repo, err := postgres.NewRepository(ctx, dsn)
	if err != nil {
		logger.Error("Failed to connect to postgres", slog.String("err", err.Error()))
		os.Exit(1)
	}
	defer repo.Close()

	comps, err := repo.GetActiveCompetitionIDs(ctx)
	if err != nil || len(comps) == 0 {
		logger.Warn("Failed to resolve dynamic competitions, using international default", slog.Any("err", err))
		comps = intComps
	}

	syncUC := usecases.NewSyncScoresUseCase(espnClient, repo, logger, false)
	logger.Info("[INGEST_START] Syncing matches for window",
		slog.String("from", *fromFlag),
		slog.String("to", *toFlag),
		slog.Int("competitions", len(comps)),
	)

	report, err := syncUC.Execute(ctx, comps, *fromFlag, *toFlag)
	if err != nil {
		logger.Error("[INGEST_ERROR] Match synchronization failed", slog.String("err", err.Error()))
		os.Exit(1)
	}

	logger.Info("[INGEST_COMPLETE]",
		slog.Int("total_ingested", report.TotalIngested),
		slog.Int("live_matches", report.LiveMatches),
		slog.Int64("duration_ms", report.DurationMs),
	)

	if !*testHighlightsFlag {
		return
	}

	// Initialize YouTube & AI Clients for Highlight Discovery Test
	rssClient := youtube.NewRSSClient()
	taxonomyRegistry := youtube.NewTaxonomyRegistry(logger)
	if err := taxonomyRegistry.LoadFromDB(ctx, repo); err != nil {
		logger.Warn("[TAXONOMY] DB taxonomy load failed", slog.String("err", err.Error()))
	}

	ytClient := youtube.NewClient(os.Getenv("YOUTUBE_API_KEY"), rssClient, taxonomyRegistry)
	aiURL := os.Getenv("AI_GATEWAY_URL")
	if aiURL == "" {
		aiURL = "http://footballclips-oracle:8080"
	}
	aiClient := aigw.NewClient(aiURL, os.Getenv("AI_GATEWAY_TOKEN"))
	hlUC := usecases.NewFindHighlightUseCase(ytClient, aiClient, repo, logger, false)

	logger.Info("[HIGHLIGHT_PIPELINE_START] Discovering highlights for finished matches...", slog.Int("limit", *maxHighlightsFlag))
	pending, err := repo.GetPendingHighlights(ctx, *maxHighlightsFlag)
	if err != nil {
		logger.Error("[HIGHLIGHT_ERROR] Failed to query pending matches", slog.String("err", err.Error()))
		return
	}

	logger.Info("[HIGHLIGHT_CANDIDATES_FOUND]", slog.Int("count", len(pending)))
	for i, m := range pending {
		matchCtx, matchCancel := context.WithTimeout(ctx, 45*time.Second)
		trace, err := hlUC.Execute(matchCtx, m)
		matchCancel()

		if err != nil {
			logger.Warn("[HIGHLIGHT_EVAL_ERROR]",
				slog.Int64("match_id", m.ID),
				slog.String("match", fmt.Sprintf("%s vs %s", m.HomeTeamName, m.AwayTeamName)),
				slog.String("err", err.Error()),
			)
			continue
		}

		if trace == nil {
			continue
		}

		linkedVideo := ""
		if trace.LinkedVideoID != nil {
			linkedVideo = *trace.LinkedVideoID
		}

		logger.Info("[HIGHLIGHT_TRACE]",
			slog.Int("index", i+1),
			slog.Int64("match_id", m.ID),
			slog.String("competition", m.CompetitionID),
			slog.String("match", fmt.Sprintf("%s vs %s", m.HomeTeamName, m.AwayTeamName)),
			slog.String("outcome", trace.Outcome),
			slog.String("video_id", linkedVideo),
			slog.Float64("worldwide_percent", trace.GlobalWatchabilityPct),
		)
	}
}
