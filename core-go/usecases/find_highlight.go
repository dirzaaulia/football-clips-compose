package usecases

import (
	"context"
	"fmt"
	"log/slog"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/domain"
	"github.com/dirzaaulia/footballclips/core-go/ports"
)

// FindHighlightUseCase coordinates discovery and evaluation of match highlights.
type FindHighlightUseCase struct {
	discovery    ports.HighlightDiscovery
	aiGateway    ports.AIGateway
	matchRepo    ports.MatchRepository
	fcmClient    ports.FCMClient
	logger       *slog.Logger
	isShadowMode bool
}

// NewFindHighlightUseCase instantiates the highlight search interactor.
func NewFindHighlightUseCase(
	discovery ports.HighlightDiscovery,
	ai ports.AIGateway,
	repo ports.MatchRepository,
	logger *slog.Logger,
	isShadow bool,
) *FindHighlightUseCase {
	if logger == nil {
		logger = slog.Default()
	}
	return &FindHighlightUseCase{
		discovery:    discovery,
		aiGateway:    ai,
		matchRepo:    repo,
		logger:       logger,
		isShadowMode: isShadow,
	}
}

// WithFCM sets the FCM client for topic-based alert dispatches.
func (uc *FindHighlightUseCase) WithFCM(fcmClient ports.FCMClient) *FindHighlightUseCase {
	uc.fcmClient = fcmClient
	return uc
}

// Execute discovers and evaluates highlight candidates for a finished match.
func (uc *FindHighlightUseCase) Execute(ctx context.Context, match domain.Match) (*domain.MatchWaterfallTrace, error) {
	start := time.Now().UTC()
	trace := &domain.MatchWaterfallTrace{
		MatchID:     match.ID,
		HomeTeam:    match.HomeTeamName,
		AwayTeam:    match.AwayTeamName,
		Competition: match.CompetitionName,
		StartedAt:   start,
		Outcome:     "PENDING",
	}

	if !match.NeedsHighlight() {
		trace.Outcome = "COOLDOWN_PROTECTED"
		trace.DurationMs = time.Since(start).Milliseconds()
		return trace, nil
	}

	if ok, _ := domain.ShouldCheckYouTubeForMatch(match, start); !ok {
		trace.Outcome = "COOLDOWN_PROTECTED"
		trace.DurationMs = time.Since(start).Milliseconds()
		return trace, nil
	}

	channels, _ := uc.matchRepo.GetMatchChannels(ctx, match)
	trace.ChannelsSearched = channels

	candidates, err := uc.discovery.DiscoverCandidates(ctx, match, channels)
	if err != nil {
		trace.Outcome = "ERROR"
		trace.DurationMs = time.Since(start).Milliseconds()
		return trace, fmt.Errorf("discover candidates: %w", err)
	}

	if len(candidates) > 0 {
		candidates, _ = uc.discovery.EnrichCandidateDetails(ctx, candidates)
	}

	trace.Candidates = candidates
	if len(candidates) == 0 {
		return uc.handleNotFound(ctx, match, trace, start)
	}

	eval, err := uc.aiGateway.EvaluateCandidates(ctx, match, candidates)
	if err != nil || eval == nil || eval.Verdict != "ACCEPTED" {
		return uc.handleNotFound(ctx, match, trace, start)
	}

	var winningCand domain.VideoCandidate
	for _, c := range candidates {
		if c.VideoID == eval.VideoID {
			winningCand = c
			eval.Source = c.Source
			break
		}
	}

	return uc.handleEvaluation(ctx, match, eval, winningCand, trace, start)
}

func (uc *FindHighlightUseCase) handleEvaluation(
	ctx context.Context,
	match domain.Match,
	eval *domain.CandidateEvaluation,
	cand domain.VideoCandidate,
	trace *domain.MatchWaterfallTrace,
	start time.Time,
) (*domain.MatchWaterfallTrace, error) {
	if match.HighlightVideoID != nil && *match.HighlightVideoID != "" {
		better, reason := domain.IsBetterHighlight(cand, match)
		if !better {
			trace.Outcome = "RETAINED_SUPERIOR"
			trace.DurationMs = time.Since(start).Milliseconds()
			uc.logger.Info("[UPGRADE_REJECTED]", slog.Int64("match_id", match.ID), slog.String("reason", reason))
			return uc.retainCurrentHighlight(ctx, match, trace, start)
		}
	}

	trace.LinkedVideoTitle = &cand.Title
	trace.LinkedVideoID = &eval.VideoID
	trace.GlobalWatchabilityPct = cand.WatchabilityPct
	trace.Outcome = "LINKED"
	trace.DurationMs = time.Since(start).Milliseconds()

	reg := buildLinkedRegionInfo(match, cand, start)
	if uc.isShadowMode {
		_ = uc.matchRepo.SaveWaterfallTrace(ctx, *trace)
		return trace, nil
	}

	videoMeta := map[string]any{
		"video_id":       eval.VideoID,
		"title":          cand.Title,
		"channel":        cand.ChannelHandle,
		"channel_handle": cand.ChannelHandle,
		"source":         cand.Source,
		"is_worldwide":   cand.WatchabilityPct >= 99.0,
	}

	if err := uc.matchRepo.UpdateMatchHighlight(ctx, match.ID, eval.VideoID, reg, videoMeta); err != nil {
		return trace, fmt.Errorf("link match highlight: %w", err)
	}

	uc.dispatchNotification(ctx, match, eval.VideoID, cand.Title)
	_ = uc.matchRepo.SaveWaterfallTrace(ctx, *trace)
	return trace, nil
}

func (uc *FindHighlightUseCase) retainCurrentHighlight(
	ctx context.Context,
	match domain.Match,
	trace *domain.MatchWaterfallTrace,
	start time.Time,
) (*domain.MatchWaterfallTrace, error) {
	sweepCount := 1
	if match.RegionInfo != nil {
		sweepCount = match.RegionInfo.SweepCount + 1
	}
	reg := match.RegionInfo
	if reg == nil {
		reg = &domain.RegionInfo{}
	}
	reg.SweepCount = sweepCount
	if exhausted, reason := domain.CheckMatchTargetMarketCutoff(match, start, sweepCount); exhausted {
		reg.TargetMarketExhausted = true
		reg.Terminal = true
		reg.Reason = reason
	}
	_ = uc.matchRepo.UpdateMatchCheck(ctx, match.ID, reg)
	_ = uc.matchRepo.SaveWaterfallTrace(ctx, *trace)
	return trace, nil
}

func buildLinkedRegionInfo(match domain.Match, cand domain.VideoCandidate, start time.Time) *domain.RegionInfo {
	sweepCount := 1
	if match.RegionInfo != nil {
		sweepCount = match.RegionInfo.SweepCount + 1
	}
	reg := &domain.RegionInfo{
		SweepCount:      sweepCount,
		WatchabilityPct: cand.WatchabilityPct,
		Tier:            cand.Tier,
		DurationSeconds: cand.DurationSeconds,
	}
	if domain.IsOptimalHighlight(cand) {
		reg.Terminal = true
		reg.Status = "OPTIMAL_LINKED"
		reg.Reason = "Optimal Tier 1 highlight with >=99% worldwide reach secured"
		return reg
	}
	if exhausted, reason := domain.CheckMatchTargetMarketCutoff(match, start, sweepCount); exhausted {
		reg.TargetMarketExhausted = true
		reg.Terminal = true
		reg.Reason = reason
	}
	return reg
}

func (uc *FindHighlightUseCase) dispatchNotification(ctx context.Context, match domain.Match, videoID, title string) {
	if uc.fcmClient == nil {
		return
	}
	var hID, aID int
	if match.HomeTeamID != nil {
		hID = int(*match.HomeTeamID)
	}
	if match.AwayTeamID != nil {
		aID = int(*match.AwayTeamID)
	}
	_ = uc.fcmClient.NotifyHighlightLinked(ctx, match, videoID, title, hID, aID)
}

func (uc *FindHighlightUseCase) handleNotFound(
	ctx context.Context,
	match domain.Match,
	trace *domain.MatchWaterfallTrace,
	start time.Time,
) (*domain.MatchWaterfallTrace, error) {
	trace.Outcome = "NOT_FOUND"
	trace.DurationMs = time.Since(start).Milliseconds()

	sweepCount := 1
	if match.RegionInfo != nil {
		sweepCount = match.RegionInfo.SweepCount + 1
	}

	if uc.isShadowMode {
		_ = uc.matchRepo.SaveWaterfallTrace(ctx, *trace)
		return trace, nil
	}

	exhausted, reason := domain.IsBacklogSafeExhausted(match, start, sweepCount)
	if exhausted {
		trace.Outcome = "UNAVAILABLE"
		_ = uc.matchRepo.MarkMatchUnavailable(ctx, match.ID, reason, match.RegionInfo)
	} else {
		reg := match.RegionInfo
		if reg == nil {
			reg = &domain.RegionInfo{}
		}
		reg.SweepCount = sweepCount
		_ = uc.matchRepo.UpdateMatchCheck(ctx, match.ID, reg)
	}

	_ = uc.matchRepo.SaveWaterfallTrace(ctx, *trace)
	return trace, nil
}
