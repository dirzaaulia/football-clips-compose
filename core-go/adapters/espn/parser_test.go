package espn_test

import (
	"testing"

	"github.com/dirzaaulia/footballclips/core-go/adapters/espn"
)

func TestNormalizeStatus(t *testing.T) {
	tests := []struct {
		state    string
		detail   string
		expected string
	}{
		{"pre", "", "TIMED"},
		{"in", "Halftime", "PAUSED"},
		{"in", "HT", "PAUSED"},
		{"in", "45' +2", "IN_PLAY"},
		{"post", "FT", "FINISHED"},
		{"post", "Match Abandoned", "ABANDONED"},
		{"pre", "Postponed", "POSTPONED"},
		{"pre", "Cancelled", "CANCELLED"},
		{"unknown", "", "SCHEDULED"},
	}

	for _, tt := range tests {
		t.Run(tt.state+"_"+tt.detail, func(t *testing.T) {
			got := espn.NormalizeStatus(tt.state, tt.detail)
			if got != tt.expected {
				t.Errorf("NormalizeStatus(%q, %q) = %q; want %q", tt.state, tt.detail, got, tt.expected)
			}
		})
	}
}

func TestFormatRoundSlug(t *testing.T) {
	tests := []struct {
		slug     string
		expected string
	}{
		{"", "Main Round"},
		{"group-stage", "Group Stage"},
		{"quarter_finals", "Quarter Finals"},
		{"round-of-16", "Round Of 16"},
	}

	for _, tt := range tests {
		t.Run(tt.slug, func(t *testing.T) {
			got := espn.FormatRoundSlug(tt.slug)
			if got != tt.expected {
				t.Errorf("FormatRoundSlug(%q) = %q; want %q", tt.slug, got, tt.expected)
			}
		})
	}
}

func TestParseEvent(t *testing.T) {
	scoreHome := "3"
	scoreAway := "1"

	ev := espn.RawEvent{
		ID:   "704512",
		Date: "2026-11-08T19:00:00Z",
	}
	ev.Season.Year = 2026

	var comp espn.RawCompetition
	comp.Status.Type.State = "post"
	comp.Status.Type.Detail = "FT"
	comp.Status.DisplayClock = "90'"

	var homeComp espn.RawCompetitor
	homeComp.HomeAway = "home"
	homeComp.Score = &scoreHome
	homeComp.Team.ID = "359"
	homeComp.Team.DisplayName = "Arsenal"

	var awayComp espn.RawCompetitor
	awayComp.HomeAway = "away"
	awayComp.Score = &scoreAway
	awayComp.Team.ID = "363"
	awayComp.Team.DisplayName = "Chelsea"

	comp.Competitors = []espn.RawCompetitor{homeComp, awayComp}
	ev.Competitions = []espn.RawCompetition{comp}

	match, err := espn.ParseEvent(ev, "PL", "Premier League")
	if err != nil {
		t.Fatalf("ParseEvent returned error: %v", err)
	}

	if match.ID != 704512 {
		t.Errorf("expected ID 704512, got %d", match.ID)
	}
	if match.Status != "FINISHED" {
		t.Errorf("expected FINISHED, got %s", match.Status)
	}
	if *match.HomeScore != 3 || *match.AwayScore != 1 {
		t.Errorf("expected 3-1, got %d-%d", *match.HomeScore, *match.AwayScore)
	}
	if match.HomeTeamName != "Arsenal" || match.AwayTeamName != "Chelsea" {
		t.Errorf("team names mismatch: %s vs %s", match.HomeTeamName, match.AwayTeamName)
	}
}
