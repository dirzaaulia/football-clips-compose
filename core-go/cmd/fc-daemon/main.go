package main

import (
	"context"
	"encoding/json"
	"log/slog"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/adapters/aigw"
	"github.com/dirzaaulia/footballclips/core-go/adapters/edge"
	"github.com/dirzaaulia/footballclips/core-go/adapters/espn"
	"github.com/dirzaaulia/footballclips/core-go/adapters/fcm"
	"github.com/dirzaaulia/footballclips/core-go/adapters/postgres"
	"github.com/dirzaaulia/footballclips/core-go/adapters/websub"
	"github.com/dirzaaulia/footballclips/core-go/adapters/youtube"
	"github.com/dirzaaulia/footballclips/core-go/usecases"
)

var bootTime = time.Now().UTC()

func main() {
	logger := slog.New(slog.NewJSONHandler(os.Stdout, &slog.HandlerOptions{Level: slog.LevelInfo}))
	slog.SetDefault(logger)

	dsn := os.Getenv("DATABASE_URL")
	if dsn == "" {
		dsn = os.Getenv("FC_DATABASE_DSN")
	}
	if dsn == "" {
		logger.Error("DATABASE_URL or FC_DATABASE_DSN environment variable is required")
		os.Exit(1)
	}

	isShadow := os.Getenv("SHADOW_MODE") != "false"
	logger.Info("[FC_DAEMON] Starting micro-daemon",
		slog.Bool("shadow_mode", isShadow),
		slog.String("version", "2026.11.0"),
	)

	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	repo, err := postgres.NewRepository(ctx, dsn)
	if err != nil {
		logger.Error("Failed to initialize postgres repository", slog.String("err", err.Error()))
		os.Exit(1)
	}
	defer repo.Close()

	credPath := os.Getenv("GOOGLE_APPLICATION_CREDENTIALS")
	if credPath == "" {
		credPath = "/home/ubuntu/jarvis/credentials/gcp-highlightdata.json"
	}
	var fcmClient *fcm.Client
	if tp, err := fcm.NewTokenProvider(credPath); err == nil {
		fcmClient = fcm.NewClient(tp, repo, logger)
		logger.Info("[FCM] Initialized topic push client", slog.String("project", tp.ProjectID()))
	} else {
		fcmClient = fcm.NewClient(nil, repo, logger)
		logger.Warn("[FCM] Running in fallback mode", slog.String("err", err.Error()))
	}

	espnClient := espn.NewClient()
	rssClient := youtube.NewRSSClient()
	taxonomyRegistry := youtube.NewTaxonomyRegistry(logger)
	if err := taxonomyRegistry.LoadFromDB(context.Background(), repo); err != nil {
		logger.Warn("[TAXONOMY] DB load failed, running on fallback seed", slog.String("err", err.Error()))
	}
	taxonomyRegistry.StartAutoRefresh(context.Background(), repo, 15*time.Minute)

	ytClient := youtube.NewClient(os.Getenv("YOUTUBE_API_KEY"), rssClient, taxonomyRegistry)
	aiClient := aigw.NewClient(os.Getenv("AI_GATEWAY_URL"), os.Getenv("AI_GATEWAY_TOKEN"))
	purgeClient := edge.NewPurgeClient(os.Getenv("EDGE_PURGE_URL"))

	syncUseCase := usecases.NewSyncScoresUseCase(espnClient, repo, logger, isShadow).WithFCM(fcmClient)
	highlightUseCase := usecases.NewFindHighlightUseCase(ytClient, aiClient, repo, logger, isShadow).WithFCM(fcmClient)
	standingsUseCase := usecases.NewSyncStandingsUseCase(espnClient, repo, logger)
	shortsUseCase := usecases.NewIngestShortsUseCase(rssClient, repo, logger)

	websubHandler := websub.NewHandler(logger, func(n websub.Notification) {
		logger.Info("[WEBSUB_PUSH_TRIGGER] Instant match highlight sweep",
			slog.String("video_id", n.VideoID),
			slog.String("title", n.Title),
		)
		go runHighlightCycle(context.Background(), repo, highlightUseCase, purgeClient, logger)
	})

	server := startHTTPServer(isShadow, websubHandler, logger)
	defer func() { _ = server.Shutdown(context.Background()) }()

	targetComps := []string{
		"PL", "PD", "SA", "BL1", "FL1", "CL", "EL", "ECL",
		"FAC", "EFL", "CDR", "DFB", "CIT", "SPL", "MLS", "DED", "PPL",
	}
	standingsComps := []string{"PL", "PD", "SA", "BL1", "FL1", "CL", "SPL", "MLS", "DED", "PPL", "IDN", "BRA", "ARG"}

	scoreTicker := time.NewTicker(60 * time.Second)
	defer scoreTicker.Stop()
	hlTicker := time.NewTicker(5 * time.Minute)
	defer hlTicker.Stop()
	shortsTicker := time.NewTicker(30 * time.Minute)
	defer shortsTicker.Stop()
	standingsTicker := time.NewTicker(6 * time.Hour)
	defer standingsTicker.Stop()

	// Initial bootstrap sweep
	runSyncCycle(ctx, syncUseCase, targetComps, purgeClient, logger)
	runHighlightCycle(ctx, repo, highlightUseCase, purgeClient, logger)
	runShortsCycle(ctx, shortsUseCase, logger)
	runStandingsCycle(ctx, standingsUseCase, standingsComps, logger)

	for {
		select {
		case <-ctx.Done():
			logger.Info("[FC_DAEMON] Shutting down gracefully...")
			return
		case <-scoreTicker.C:
			runSyncCycle(ctx, syncUseCase, targetComps, purgeClient, logger)
		case <-hlTicker.C:
			runHighlightCycle(ctx, repo, highlightUseCase, purgeClient, logger)
		case <-shortsTicker.C:
			runShortsCycle(ctx, shortsUseCase, logger)
		case <-standingsTicker.C:
			runStandingsCycle(ctx, standingsUseCase, standingsComps, logger)
		}
	}
}

func startHTTPServer(isShadow bool, websubHandler http.Handler, logger *slog.Logger) *http.Server {
	port := os.Getenv("PORT")
	if port == "" {
		port = "8094"
	}
	mux := http.NewServeMux()
	mux.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		_ = json.NewEncoder(w).Encode(map[string]any{
			"status":      "ok",
			"shadow_mode": isShadow,
			"uptime_sec":  time.Since(bootTime).Seconds(),
			"version":     "2026.11.0",
		})
	})
	if websubHandler != nil {
		mux.Handle("/websub", websubHandler)
	}

	srv := &http.Server{
		Addr:         ":" + port,
		Handler:      mux,
		ReadTimeout:  5 * time.Second,
		WriteTimeout: 5 * time.Second,
	}
	go func() {
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			logger.Warn("HTTP status server error", slog.String("err", err.Error()))
		}
	}()
	return srv
}
