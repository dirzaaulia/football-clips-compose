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

	trace.Candidates = candidates
	if len(candidates) == 0 {
		return uc.handleNotFound(ctx, match, trace, start)
	}

	eval, err := uc.aiGateway.EvaluateCandidates(ctx, match, candidates)
	if err != nil || eval == nil || eval.Verdict != "ACCEPTED" {
		return uc.handleNotFound(ctx, match, trace, start)
	}

	var candTitle string
	var candChannel string
	var candSource string
	for _, c := range candidates {
		if c.VideoID == eval.VideoID {
			candTitle = c.Title
			candChannel = c.ChannelHandle
			candSource = c.Source
			eval.Source = c.Source
			break
		}
	}
	trace.LinkedVideoTitle = &candTitle

	reg := match.RegionInfo
	if reg == nil {
		reg = &domain.RegionInfo{}
	}
	reg.SweepCount++

	watchability := domain.CalculateWorldwideWatchability(reg)
	trace.GlobalWatchabilityPct = watchability

	targetCatered := domain.IsTargetMarketCatered(reg)
	if !targetCatered || watchability < 99.0 {
		exhausted, exhaustReason := domain.CheckMatchTargetMarketCutoff(match, start, reg.SweepCount)
		if exhausted {
			reg.TargetMarketExhausted = true
			reg.Terminal = true
			reg.Reason = exhaustReason
		}
	}

	trace.Outcome = "LINKED"
	trace.LinkedVideoID = &eval.VideoID
	trace.DurationMs = time.Since(start).Milliseconds()

	if uc.isShadowMode {
		uc.logger.Info("[SHADOW_HIGHLIGHT] Evaluated match candidate",
			slog.Int64("match_id", match.ID),
			slog.String("video_id", eval.VideoID),
			slog.String("evaluator", eval.Evaluator),
			slog.String("source", eval.Source),
			slog.Int("score", eval.Score),
		)
		_ = uc.matchRepo.SaveWaterfallTrace(ctx, *trace)
		return trace, nil
	}

	videoMeta := map[string]any{
		"video_id":       eval.VideoID,
		"title":          candTitle,
		"channel":        candChannel,
		"channel_handle": candChannel,
		"source":         candSource,
		"is_worldwide":   true,
	}

	if err := uc.matchRepo.UpdateMatchHighlight(ctx, match.ID, eval.VideoID, reg, videoMeta); err != nil {
		return trace, fmt.Errorf("link match highlight: %w", err)
	}

	if uc.fcmClient != nil {
		var hID, aID int
		if match.HomeTeamID != nil {
			hID = int(*match.HomeTeamID)
		}
		if match.AwayTeamID != nil {
			aID = int(*match.AwayTeamID)
		}
		_ = uc.fcmClient.NotifyHighlightLinked(ctx, match, eval.VideoID, candTitle, hID, aID)
	}

	_ = uc.matchRepo.SaveWaterfallTrace(ctx, *trace)
	return trace, nil
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
