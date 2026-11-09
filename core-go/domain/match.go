package domain

import (
	"time"
)

// Match represents a football fixture entity in the domain layer.
type Match struct {
	ID               int64      `json:"id"`
	CompetitionID    string     `json:"competition_id"`
	CompetitionName  string     `json:"competition_name"`
	UTCDate          time.Time  `json:"utc_date"`
	Status           string     `json:"status"` // TIMED, SCHEDULED, IN_PLAY, PAUSED, FINISHED, POSTPONED, CANCELLED
	Matchday         *int       `json:"matchday,omitempty"`
	HomeTeamID       *int64     `json:"home_team_id,omitempty"`
	HomeTeamName     string     `json:"home_team_name"`
	HomeTeamCrest    *string    `json:"home_team_crest,omitempty"`
	AwayTeamID       *int64     `json:"away_team_id,omitempty"`
	AwayTeamName     string     `json:"away_team_name"`
	AwayTeamCrest    *string    `json:"away_team_crest,omitempty"`
	HomeScore        *int       `json:"home_score,omitempty"`
	AwayScore        *int       `json:"away_score,omitempty"`
	HomePenalties    *int       `json:"home_penalties,omitempty"`
	AwayPenalties    *int       `json:"away_penalties,omitempty"`
	HighlightVideoID *string     `json:"highlight_video_id,omitempty"`
	LastYouTubeCheck *time.Time  `json:"last_youtube_check,omitempty"`
	RegionInfo       *RegionInfo `json:"youtube_region_info,omitempty"`
	Season           int         `json:"season"`
	DisplayClock     *string     `json:"display_clock,omitempty"`
	UpdatedAt        time.Time   `json:"updated_at"`
}

// RegionInfo stores YouTube availability and geo-restriction metadata.
type RegionInfo struct {
	Terminal              bool     `json:"terminal,omitempty"`
	Status                string   `json:"status,omitempty"`
	Reason                string   `json:"reason,omitempty"`
	SweepCount            int      `json:"sweep_count,omitempty"`
	Allowed               []string `json:"allowed,omitempty"`
	Blocked               []string `json:"blocked,omitempty"`
	TargetMarketExhausted bool     `json:"target_market_exhausted,omitempty"`
}

// IsLive returns true if the match is actively in progress.
func (m Match) IsLive() bool {
	return m.Status == "IN_PLAY" || m.Status == "PAUSED"
}

// IsFinished returns true if the match has concluded.
func (m Match) IsFinished() bool {
	return m.Status == "FINISHED"
}

// NeedsHighlight returns true if match is finished, not terminal, and has no highlight linked.
func (m Match) NeedsHighlight() bool {
	if !m.IsFinished() {
		return false
	}
	if m.RegionInfo != nil && m.RegionInfo.Terminal {
		return false
	}
	return m.HighlightVideoID == nil || *m.HighlightVideoID == ""
}
