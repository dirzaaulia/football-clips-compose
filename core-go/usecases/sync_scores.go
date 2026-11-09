package usecases

import (
	"context"
	"fmt"
	"log/slog"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/domain"
	"github.com/dirzaaulia/footballclips/core-go/ports"
)

// SyncReport records operational metrics for a synchronization run.
type SyncReport struct {
	TotalIngested int   `json:"total_ingested"`
	LiveMatches   int   `json:"live_matches"`
	ParityMatches int   `json:"parity_matches"`
	DriftCount    int   `json:"drift_count"`
	DurationMs    int64 `json:"duration_ms"`
}

// SyncScoresUseCase coordinates score fetching and shadow parity verification.
type SyncScoresUseCase struct {
	scoreProvider ports.ScoreProvider
	matchRepo     ports.MatchRepository
	fcmClient     ports.FCMClient
	logger        *slog.Logger
	isShadowMode  bool
}

// NewSyncScoresUseCase instantiates the interactor with injected dependencies.
func NewSyncScoresUseCase(
	sp ports.ScoreProvider,
	repo ports.MatchRepository,
	logger *slog.Logger,
	isShadow bool,
) *SyncScoresUseCase {
	if logger == nil {
		logger = slog.Default()
	}
	return &SyncScoresUseCase{
		scoreProvider: sp,
		matchRepo:     repo,
		logger:        logger,
		isShadowMode:  isShadow,
	}
}

// WithFCM sets the FCM client for topic-based alert dispatches.
func (uc *SyncScoresUseCase) WithFCM(fcmClient ports.FCMClient) *SyncScoresUseCase {
	uc.fcmClient = fcmClient
	return uc
}

// Execute performs match synchronization across competitions for the given date window.
func (uc *SyncScoresUseCase) Execute(ctx context.Context, compCodes []string, dateFrom, dateTo string) (*SyncReport, error) {
	start := time.Now()
	matches, err := uc.scoreProvider.FetchScoreboard(ctx, compCodes, dateFrom, dateTo)
	if err != nil {
		return nil, fmt.Errorf("fetch scoreboard: %w", err)
	}

	report := &SyncReport{
		TotalIngested: len(matches),
	}

	for _, m := range matches {
		if m.IsLive() {
			report.LiveMatches++
		}
	}

	if uc.isShadowMode {
		drift, parity := uc.verifyShadowParity(ctx, matches)
		report.DriftCount = drift
		report.ParityMatches = parity
		uc.logger.Info("[SHADOW_MODE] Parity verification complete",
			slog.Int("total", len(matches)),
			slog.Int("parity", parity),
			slog.Int("drift", drift),
		)
		report.DurationMs = time.Since(start).Milliseconds()
		return report, nil
	}

	if err := uc.matchRepo.UpsertMatches(ctx, matches); err != nil {
		return nil, fmt.Errorf("upsert matches: %w", err)
	}

	uc.dispatchFCM(ctx, matches)

	report.DurationMs = time.Since(start).Milliseconds()
	uc.logger.Info("[SYNC_SCORES] Successfully synchronized matches",
		slog.Int("total", report.TotalIngested),
		slog.Int("live", report.LiveMatches),
		slog.Int64("duration_ms", report.DurationMs),
	)
	return report, nil
}

func (uc *SyncScoresUseCase) dispatchFCM(ctx context.Context, matches []domain.Match) {
	if uc.fcmClient == nil || uc.isShadowMode {
		return
	}
	for _, m := range matches {
		var hID, aID int
		if m.HomeTeamID != nil {
			hID = int(*m.HomeTeamID)
		}
		if m.AwayTeamID != nil {
			aID = int(*m.AwayTeamID)
		}
		if m.IsLive() {
			_ = uc.fcmClient.NotifyMatchEvent(ctx, m, "score", hID, aID)
		} else if m.Status == "FINISHED" {
			_ = uc.fcmClient.NotifyMatchEvent(ctx, m, "fulltime", hID, aID)
		}
	}
}

func (uc *SyncScoresUseCase) verifyShadowParity(ctx context.Context, incoming []domain.Match) (int, int) {
	if len(incoming) == 0 {
		return 0, 0
	}
	now := time.Now().UTC()
	dbMatches, err := uc.matchRepo.GetMatchesForSync(ctx, now.Add(-48*time.Hour), now.Add(48*time.Hour))
	if err != nil {
		uc.logger.Warn("[SHADOW_MODE] Could not fetch DB matches for parity check", slog.String("err", err.Error()))
		return 0, 0
	}

	dbMap := make(map[int64]domain.Match, len(dbMatches))
	for _, m := range dbMatches {
		dbMap[m.ID] = m
	}

	driftCount, parityCount := 0, 0
	for _, in := range incoming {
		dbMatch, exists := dbMap[in.ID]
		if !exists {
			continue
		}
		if dbMatch.Status == in.Status {
			parityCount++
		} else {
			driftCount++
			uc.logger.Warn("[SHADOW_DRIFT] Status divergence detected",
				slog.Int64("match_id", in.ID),
				slog.String("db_status", dbMatch.Status),
				slog.String("espn_status", in.Status),
			)
		}
	}
	return driftCount, parityCount
}
