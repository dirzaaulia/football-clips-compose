package domain_test

import (
	"encoding/json"
	"testing"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/domain"
)

func TestMatchLifecycle(t *testing.T) {
	now := time.Now().UTC()
	m := domain.Match{
		ID:              12345,
		CompetitionID:   "PL",
		CompetitionName: "Premier League",
		UTCDate:         now,
		Status:          "IN_PLAY",
		HomeTeamName:    "Arsenal",
		AwayTeamName:    "Chelsea",
		Season:          2026,
		UpdatedAt:       now,
	}

	if !m.IsLive() {
		t.Errorf("expected match to be live, got status %s", m.Status)
	}

	if m.IsFinished() {
		t.Errorf("expected match not to be finished, got status %s", m.Status)
	}

	if m.NeedsHighlight() {
		t.Errorf("in-play match should not need highlight yet")
	}

	m.Status = "FINISHED"
	if !m.IsFinished() {
		t.Errorf("expected match to be finished")
	}

	if !m.NeedsHighlight() {
		t.Errorf("finished match without highlight should need highlight")
	}

	videoID := "abcdef12345"
	m.HighlightVideoID = &videoID
	if m.NeedsHighlight() {
		t.Errorf("finished match with highlight should not need highlight")
	}
}

func TestMatchJSONParity(t *testing.T) {
	now := time.Date(2026, 11, 8, 20, 0, 0, 0, time.UTC)
	scoreHome := 2
	scoreAway := 1
	m := domain.Match{
		ID:              999,
		CompetitionID:   "CL",
		CompetitionName: "UEFA Champions League",
		UTCDate:         now,
		Status:          "FINISHED",
		HomeTeamName:    "Real Madrid",
		AwayTeamName:    "AC Milan",
		HomeScore:       &scoreHome,
		AwayScore:       &scoreAway,
		Season:          2026,
		UpdatedAt:       now,
	}

	bytes, err := json.Marshal(m)
	if err != nil {
		t.Fatalf("failed to marshal match: %v", err)
	}

	var decoded domain.Match
	if err := json.Unmarshal(bytes, &decoded); err != nil {
		t.Fatalf("failed to unmarshal match: %v", err)
	}

	if decoded.ID != 999 || *decoded.HomeScore != 2 || *decoded.AwayScore != 1 {
		t.Errorf("decoded values mismatch: %+v", decoded)
	}
}
