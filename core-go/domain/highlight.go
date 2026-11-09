package domain

import "time"

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
