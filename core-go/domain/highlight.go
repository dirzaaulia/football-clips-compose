package domain

import (
	"fmt"
	"time"
)

// CandidateEvaluation models the evaluation of a candidate video by AI or deterministic fallback.
type CandidateEvaluation struct {
	VideoID   string `json:"video_id"`
	Verdict   string `json:"verdict"` // ACCEPTED | REJECTED | SKIPPED
	Score     int    `json:"score"`
	Reason    string `json:"reason"`
	Evaluator string `json:"evaluator"` // ORACLE_GATEWAY | GEMINI_FLASH | DETERMINISTIC_FALLBACK
	Source    string `json:"source,omitempty"`    // RSS | WEBSUB | API
}

// VideoCandidate models a discovered YouTube video candidate.
type VideoCandidate struct {
	VideoID           string               `json:"video_id"`
	Title             string               `json:"title"`
	ChannelTitle      *string              `json:"channel_title,omitempty"`
	ChannelHandle     string               `json:"channel_handle"`
	ChannelID         *string              `json:"channel_id,omitempty"`
	DurationSeconds   int                  `json:"duration_seconds"`
	DurationFormatted *string              `json:"duration_formatted,omitempty"`
	ThumbnailURL      *string              `json:"thumbnail_url,omitempty"`
	PublishedAt       *time.Time           `json:"published_at,omitempty"`
	IsEmbeddable      bool                 `json:"is_embeddable"`
	Tier              int                  `json:"tier"`
	WatchabilityPct   float64              `json:"watchability_pct"`
	AIRank            *int                 `json:"ai_rank,omitempty"`
	AIReason          *string              `json:"ai_reason,omitempty"`
	Evaluation        *CandidateEvaluation `json:"evaluation,omitempty"`
	Source            string               `json:"source"` // RSS | WEBSUB | API
}

// MatchWaterfallTrace records the full diagnostic telemetry of a highlight search.
type MatchWaterfallTrace struct {
	MatchID               int64            `json:"match_id"`
	HomeTeam              string           `json:"home_team"`
	AwayTeam              string           `json:"away_team"`
	Score                 string           `json:"score"`
	Competition           string           `json:"competition"`
	UTCDate               string           `json:"utc_date"`
	StartedAt             time.Time        `json:"started_at"`
	DurationMs            int64            `json:"duration_ms"`
	ChannelsSearched      []string         `json:"channels_searched"`
	Candidates            []VideoCandidate `json:"candidates"`
	Outcome               string           `json:"outcome"` // LINKED | NOT_FOUND | COOLDOWN_PROTECTED | UNAVAILABLE
	LinkedVideoID         *string          `json:"linked_video_id,omitempty"`
	LinkedVideoTitle      *string          `json:"linked_video_title,omitempty"`
	GlobalWatchabilityPct float64          `json:"global_watchability_pct"`
	QuotaUsed             int              `json:"quota_used"`
}

// IsOptimalHighlight returns true if a highlight satisfies maximum authority and global watchability.
func IsOptimalHighlight(cand VideoCandidate) bool {
	return cand.Tier == 1 && cand.WatchabilityPct >= 99.0 && cand.DurationSeconds >= 180
}

// IsBetterHighlight evaluates if a new candidate is objectively superior to current linked highlight.
func IsBetterHighlight(newCand VideoCandidate, current Match) (bool, string) {
	if current.HighlightVideoID == nil || *current.HighlightVideoID == "" {
		return true, "Initial highlight link"
	}
	newWW := newCand.WatchabilityPct
	currWW := CalculateWorldwideWatchability(current.RegionInfo)

	// Rule 1: Worldwide Watchability Upgrade
	if newWW >= 99.0 && currWW < 99.0 {
		return true, fmt.Sprintf("Worldwide upgrade: %.1f%% (>=99%%) vs current %.1f%%", newWW, currWW)
	}
	if newWW-currWW >= 15.0 {
		return true, fmt.Sprintf("Substantial worldwide gain: +%.1f%% (%.1f%% vs %.1f%%)", newWW-currWW, newWW, currWW)
	}
	if currWW-newWW >= 15.0 {
		return false, fmt.Sprintf("Current has superior worldwide reach: %.1f%% vs %.1f%%", currWW, newWW)
	}

	// Rule 2: Channel Tier Authority (1: League/Club > 2: Global Bcast > 3: Domestic Bcast)
	if isTierUpgrade(newCand, current, newWW, currWW) {
		return true, fmt.Sprintf("Authority tier upgraded: Tier %d beats Tier %d", newCand.Tier, current.RegionInfo.Tier)
	}
	if isDurationUpgrade(newCand, current) {
		return true, fmt.Sprintf("Extended highlights preferred: %ds vs %ds", newCand.DurationSeconds, current.RegionInfo.DurationSeconds)
	}

	return false, "Current highlight is sufficient and comparable in quality"
}

func isTierUpgrade(newCand VideoCandidate, current Match, newWW, currWW float64) bool {
	if newCand.Tier <= 0 || current.RegionInfo == nil || current.RegionInfo.Tier <= 0 {
		return false
	}
	return newCand.Tier < current.RegionInfo.Tier && newWW >= currWW-5.0
}

func isDurationUpgrade(newCand VideoCandidate, current Match) bool {
	if current.RegionInfo == nil {
		return false
	}
	currDur := current.RegionInfo.DurationSeconds
	return newCand.DurationSeconds >= 480 && currDur > 0 && currDur < 180
}
