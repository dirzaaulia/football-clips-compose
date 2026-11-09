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
// Tier 1 (<6h): check at most once every 30m.
// Tier 2 (6-24h): check at most once every 2h (120m).
// Tier 3 (24-72h): check at most once every 6h (360m).
// Tier 4 (>72h): check at most once every 24h (1440m).
func ShouldCheckYouTubeForMatch(m Match, now time.Time) (bool, string) {
	if m.LastYouTubeCheck == nil {
		return true, "Never checked"
	}

	sinceCheck := now.Sub(*m.LastYouTubeCheck).Minutes()
	sinceMatch := now.Sub(m.UTCDate).Hours()

	var tierName string
	var cooldownMin float64

	switch {
	case sinceMatch < 6:
		tierName = "Tier 1 (<6h)"
		cooldownMin = 30
	case sinceMatch < 24:
		tierName = "Tier 2 (6-24h)"
		cooldownMin = 120
	case sinceMatch < 72:
		tierName = "Tier 3 (24-72h)"
		cooldownMin = 360
	default:
		tierName = "Tier 4 (>72h)"
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
		return 2 // P3: Older missing (>24h)
	}

	// Has video: check watchability
	if m.RegionInfo != nil && len(m.RegionInfo.Blocked) > 0 {
		return 1 // P2: High Geo Deficit
	}
	return 4 // P5: Satisfied
}
