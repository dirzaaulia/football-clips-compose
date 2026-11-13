package domain

import (
	"fmt"
	"sort"
	"time"
)

var compPriority = map[string]int{
	"CL": 1, "EL": 2, "ECL": 3,
	"PL": 4, "PD": 5, "SA": 6, "BL1": 7, "FL1": 8,
	"FAC": 9, "CDR": 10, "CIT": 11, "DFB": 12, "EFL": 13,
	"DED": 14, "PPL": 15, "SPL": 16, "MLS": 17,
	"BRA": 18, "ARG": 19, "LIB": 20, "SUD": 21, "REC": 22, "IDN": 23,
}

// ShouldCheckYouTubeForMatch implements the 4-tier quota guardrail:
// Tier 1 (0-14 days / <336h): check at most once every 30m. Top priority for highlights & worldwide availability.
// Tier 2 (14-30 days / 336-720h): check at most once every 2h (120m).
// Tier 3 (30-90 days / 720-2160h): check at most once every 6h (360m).
// Tier 4 (>90 days / >2160h): check at most once every 24h (1440m).
func ShouldCheckYouTubeForMatch(m Match, now time.Time) (bool, string) {
	if m.LastYouTubeCheck == nil {
		return true, "Never checked"
	}

	sinceCheck := now.Sub(*m.LastYouTubeCheck).Minutes()
	sinceMatch := now.Sub(m.UTCDate).Hours()

	var tierName string
	var cooldownMin float64

	switch {
	case sinceMatch < 14*24: // < 14 days (336h)
		tierName = "Tier 1 (<14d)"
		cooldownMin = 30
	case sinceMatch < 30*24: // 14-30 days
		tierName = "Tier 2 (14-30d)"
		cooldownMin = 120
	case sinceMatch < 90*24: // 30-90 days
		tierName = "Tier 3 (30-90d)"
		cooldownMin = 360
	default: // > 90 days
		tierName = "Tier 4 (>90d)"
		cooldownMin = 1440
	}

	if sinceCheck >= cooldownMin {
		return true, fmt.Sprintf("%s expired (%.0fm >= %.0fm)", tierName, sinceCheck, cooldownMin)
	}

	remMin := cooldownMin - sinceCheck
	return false, fmt.Sprintf("%s active (%.0fm remaining of %.0fm)", tierName, remMin, cooldownMin)
}

// IsBacklogSafeExhausted checks whether a match should be marked terminal UNAVAILABLE.
func IsBacklogSafeExhausted(m Match, now time.Time, sweepCount int) (bool, string) {
	return CheckMatchTargetMarketCutoff(m, now, sweepCount)
}

// SortMatchesByPriority orders matches using 5-level priority logic.
func SortMatchesByPriority(matches []Match, now time.Time) []Match {
	sorted := make([]Match, len(matches))
	copy(sorted, matches)

	sort.SliceStable(sorted, func(i, j int) bool {
		ki := getPriorityRank(sorted[i], now)
		kj := getPriorityRank(sorted[j], now)
		if ki != kj {
			return ki < kj
		}
		pi := compPriority[sorted[i].CompetitionID]
		if pi == 0 {
			pi = 99
		}
		pj := compPriority[sorted[j].CompetitionID]
		if pj == 0 {
			pj = 99
		}
		if pi != pj {
			return pi < pj
		}
		return sorted[i].UTCDate.After(sorted[j].UTCDate)
	})

	return sorted
}

func getPriorityRank(m Match, now time.Time) int {
	hasVid := m.HighlightVideoID != nil && *m.HighlightVideoID != ""
	ageHours := now.Sub(m.UTCDate).Hours()

	if !hasVid {
		if ageHours < 24.0 {
			return 0 // P1: Urgent missing (<24h)
		}
		if ageHours < 336.0 {
			return 1 // P2: Recent missing (last 14 days, Tier 1 priority)
		}
		return 3 // P4: Older missing (>14 days)
	}

	// Has video: check watchability (blocked lists or whitelist restrictions)
	if m.HasGeoDeficit() {
		if ageHours < 336.0 {
			return 1 // P2: Recent match with Geo Deficit (<14 days)
		}
		return 2 // P3: Older Geo Deficit
	}
	return 4 // P5: Satisfied
}
