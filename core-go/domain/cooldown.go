package domain

import (
	"fmt"
	"sort"
	"time"
)

var compPriority = map[string]int{
	"UNL": 1, "WCQ_CONMEBOL": 2, "WCQ_AFC": 3, "CNL": 4, "WCQ_UEFA": 5, "FRI": 6,
	"CL": 7, "EL": 8, "ECL": 9,
	"PL": 10, "PD": 11, "SA": 12, "BL1": 13, "FL1": 14,
	"FAC": 15, "CDR": 16, "CIT": 17, "DFB": 18, "EFL": 19,
	"DED": 20, "PPL": 21, "SPL": 22, "MLS": 23,
	"BRA": 24, "ARG": 25, "LIB": 26, "SUD": 27, "REC": 28, "IDN": 29,
	"WCQ_CONCACAF": 30, "WCQ_CAF": 31,
}

// ShouldCheckYouTubeForMatch implements staged sweep intervals aligned with broadcast embargo windows:
// Tier 0 (<6h): check every 45m (fast-track immediate post-match uploads)
// Tier 1a (6-24h): check every 2h (clubs embargo lift window at midnight / next morning)
// Tier 1b (24-72h): check every 6h (club multi-angle and worldwide highlights)
// Tier 1c (3-14d): check every 12h
// Tier 2 (14-30d): check every 24h
// Tier 3 (>30d): check every 48h
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
		tierName = "Tier 0 (<6h)"
		cooldownMin = 45
	case sinceMatch < 24:
		tierName = "Tier 1a (6-24h)"
		cooldownMin = 120
	case sinceMatch < 72:
		tierName = "Tier 1b (24-72h)"
		cooldownMin = 360
	case sinceMatch < 14*24:
		tierName = "Tier 1c (3-14d)"
		cooldownMin = 720
	case sinceMatch < 30*24:
		tierName = "Tier 2 (14-30d)"
		cooldownMin = 1440
	default:
		tierName = "Tier 3 (>30d)"
		cooldownMin = 2880
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
