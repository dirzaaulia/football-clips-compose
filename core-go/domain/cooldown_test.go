package domain_test

import (
	"testing"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/domain"
)

func TestShouldCheckYouTubeForMatch(t *testing.T) {
	now := time.Date(2026, 11, 8, 12, 0, 0, 0, time.UTC)

	// 1. Never checked
	m1 := domain.Match{
		ID:      1,
		UTCDate: now.Add(-2 * time.Hour),
	}
	ok, reason := domain.ShouldCheckYouTubeForMatch(m1, now)
	if !ok || reason != "Never checked" {
		t.Fatalf("expected true for never checked, got %v (%s)", ok, reason)
	}

	// 2. Tier 1 (<6h): checked 10m ago (should be false, cooldown 30m)
	check10m := now.Add(-10 * time.Minute)
	m2 := domain.Match{
		ID:               2,
		UTCDate:          now.Add(-2 * time.Hour),
		LastYouTubeCheck: &check10m,
	}
	ok, _ = domain.ShouldCheckYouTubeForMatch(m2, now)
	if ok {
		t.Fatalf("expected false for Tier 1 check 10m ago")
	}

	// 3. Tier 1 (<6h): checked 35m ago (should be true)
	check35m := now.Add(-35 * time.Minute)
	m3 := domain.Match{
		ID:               3,
		UTCDate:          now.Add(-2 * time.Hour),
		LastYouTubeCheck: &check35m,
	}
	ok, _ = domain.ShouldCheckYouTubeForMatch(m3, now)
	if !ok {
		t.Fatalf("expected true for Tier 1 check 35m ago")
	}

	// 4. Tier 2 (6-24h): checked 90m ago (cooldown 120m, should be false)
	check90m := now.Add(-90 * time.Minute)
	m4 := domain.Match{
		ID:               4,
		UTCDate:          now.Add(-12 * time.Hour),
		LastYouTubeCheck: &check90m,
	}
	ok, _ = domain.ShouldCheckYouTubeForMatch(m4, now)
	if ok {
		t.Fatalf("expected false for Tier 2 check 90m ago")
	}
}

func TestIsBacklogSafeExhausted(t *testing.T) {
	now := time.Date(2026, 11, 8, 12, 0, 0, 0, time.UTC)

	// 1. Fresh match (age 2 days, 4 sweeps) -> not exhausted
	m1 := domain.Match{
		ID:      1,
		UTCDate: now.Add(-48 * time.Hour),
	}
	exhausted, _ := domain.IsBacklogSafeExhausted(m1, now, 4)
	if exhausted {
		t.Fatalf("expected not exhausted for 2-day-old match")
	}

	// 2. Old match (age 8 days, 2 sweeps) -> not exhausted (< 3 sweeps)
	m2 := domain.Match{
		ID:      2,
		UTCDate: now.Add(-8 * 24 * time.Hour),
	}
	exhausted, _ = domain.IsBacklogSafeExhausted(m2, now, 2)
	if exhausted {
		t.Fatalf("expected not exhausted for match with < 3 sweeps")
	}

	// 3. Old match (age 8 days, 3 sweeps) -> EXHAUSTED
	m3 := domain.Match{
		ID:      3,
		UTCDate: now.Add(-8 * 24 * time.Hour),
	}
	exhausted, _ = domain.IsBacklogSafeExhausted(m3, now, 3)
	if !exhausted {
		t.Fatalf("expected exhausted for match with 3 sweeps and 8 days age")
	}
}

func TestSortMatchesByPriority(t *testing.T) {
	now := time.Date(2026, 11, 8, 12, 0, 0, 0, time.UTC)

	// P1: Missing highlight, < 24h
	mP1 := domain.Match{ID: 1, UTCDate: now.Add(-5 * time.Hour), CompetitionID: "PL"}
	// P3: Missing highlight, > 24h
	mP3 := domain.Match{ID: 2, UTCDate: now.Add(-30 * time.Hour), CompetitionID: "PL"}
	// P5: Satisfied (has video, no block)
	vid := "abc"
	mP5 := domain.Match{ID: 3, UTCDate: now.Add(-2 * time.Hour), HighlightVideoID: &vid, CompetitionID: "PL"}

	sorted := domain.SortMatchesByPriority([]domain.Match{mP5, mP3, mP1}, now)
	if sorted[0].ID != 1 || sorted[1].ID != 2 || sorted[2].ID != 3 {
		t.Fatalf("unexpected priority order: %d, %d, %d", sorted[0].ID, sorted[1].ID, sorted[2].ID)
	}
}
