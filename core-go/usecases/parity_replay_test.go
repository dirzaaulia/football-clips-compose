package usecases_test

import (
	"context"
	"testing"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/adapters/espn"
	"github.com/dirzaaulia/footballclips/core-go/domain"
	"github.com/dirzaaulia/footballclips/core-go/usecases"
)

func TestAcceleratedParityReplay(t *testing.T) {
	ctx := context.Background()
	now := time.Now().UTC()

	// 10 diverse real-world scenario edge cases
	testCases := []struct {
		name         string
		evID         string
		state        string
		detail       string
		clock        string
		homeScore    string
		awayScore    string
		homePen      *string
		awayPen      *string
		expectedStat string
		expectedPen  bool
	}{
		{
			name:         "EPL Standard Full Time",
			evID:         "1001",
			state:        "post",
			detail:       "FT",
			clock:        "90'",
			homeScore:    "2",
			awayScore:    "1",
			expectedStat: "FINISHED",
		},
		{
			name:         "UCL Penalty Shootout Conclusion",
			evID:         "1002",
			state:        "post",
			detail:       "FT - Penalties (5-4)",
			clock:        "120'",
			homeScore:    "1",
			awayScore:    "1",
			homePen:      strPtr("5"),
			awayPen:      strPtr("4"),
			expectedStat: "FINISHED",
			expectedPen:  true,
		},
		{
			name:         "Live Halftime Pause",
			evID:         "1003",
			state:        "in",
			detail:       "Halftime",
			clock:        "45'",
			homeScore:    "0",
			awayScore:    "0",
			expectedStat: "PAUSED",
		},
		{
			name:         "Live Second Half Stoppage Time",
			evID:         "1004",
			state:        "in",
			detail:       "90' +4",
			clock:        "90' +4",
			homeScore:    "3",
			awayScore:    "2",
			expectedStat: "IN_PLAY",
		},
		{
			name:         "Severe Weather Postponement",
			evID:         "1005",
			state:        "pre",
			detail:       "Postponed - Heavy Rain",
			clock:        "",
			expectedStat: "POSTPONED",
		},
		{
			name:         "Pitch Invasion Match Abandoned",
			evID:         "1006",
			state:        "post",
			detail:       "Match Abandoned",
			clock:        "68'",
			homeScore:    "1",
			awayScore:    "0",
			expectedStat: "ABANDONED",
		},
		{
			name:         "Upcoming Scheduled Fixture",
			evID:         "1007",
			state:        "pre",
			detail:       "Scheduled",
			clock:        "",
			expectedStat: "TIMED",
		},
	}

	var parsedMatches []domain.Match
	for _, tc := range testCases {
		ev := espn.RawEvent{
			ID:   tc.evID,
			Date: now.Format(time.RFC3339),
		}
		ev.Season.Year = 2026

		var comp espn.RawCompetition
		comp.Status.Type.State = tc.state
		comp.Status.Type.Detail = tc.detail
		comp.Status.DisplayClock = tc.clock

		var homeComp, awayComp espn.RawCompetitor
		homeComp.HomeAway = "home"
		homeComp.Team.DisplayName = "Home Club"
		homeComp.Team.ID = "10"
		if tc.homeScore != "" {
			homeComp.Score = &tc.homeScore
		}
		if tc.homePen != nil {
			homeComp.ShootoutScore = tc.homePen
		}

		awayComp.HomeAway = "away"
		awayComp.Team.DisplayName = "Away Club"
		awayComp.Team.ID = "20"
		if tc.awayScore != "" {
			awayComp.Score = &tc.awayScore
		}
		if tc.awayPen != nil {
			awayComp.ShootoutScore = tc.awayPen
		}

		comp.Competitors = []espn.RawCompetitor{homeComp, awayComp}
		ev.Competitions = []espn.RawCompetition{comp}

		m, err := espn.ParseEvent(ev, "PL", "Premier League")
		if err != nil {
			t.Fatalf("ParseEvent failed on %s: %v", tc.name, err)
		}
		if m.Status != tc.expectedStat {
			t.Errorf("[%s] expected status %s, got %s", tc.name, tc.expectedStat, m.Status)
		}
		if tc.expectedPen && (m.HomePenalties == nil || *m.HomePenalties != 5) {
			t.Errorf("[%s] expected home penalties 5, got %v", tc.name, m.HomePenalties)
		}
		parsedMatches = append(parsedMatches, *m)
	}

	// Verify Parity Check Interactor with 100% match against simulated DB
	repo := &mockMatchRepo{dbMatches: parsedMatches}
	sp := &mockScoreProvider{matches: parsedMatches}
	uc := usecases.NewSyncScoresUseCase(sp, repo, nil, true)

	report, err := uc.Execute(ctx, []string{"PL"}, "2026-11-09", "2026-11-09")
	if err != nil {
		t.Fatalf("SyncScoresUseCase failed: %v", err)
	}

	if report.DriftCount != 0 {
		t.Errorf("expected 0 drift in accelerated replay, got %d", report.DriftCount)
	}
	if report.ParityMatches != len(testCases) {
		t.Errorf("expected %d parity matches, got %d", len(testCases), report.ParityMatches)
	}
}

func strPtr(s string) *string {
	return &s
}
