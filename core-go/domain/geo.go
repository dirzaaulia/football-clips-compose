package domain

import (
	"fmt"
	"strings"
	"time"
)

// KeyFootballMarkets defines tier 1 high-density global audience countries.
var KeyFootballMarkets = map[string]struct{}{
	"ID": {}, "MY": {}, "TH": {}, "VN": {}, "SG": {}, "PH": {},
	"GB": {}, "ES": {}, "DE": {}, "IT": {}, "FR": {}, "PT": {}, "NL": {}, "TR": {},
	"BR": {}, "AR": {}, "CO": {}, "CL": {}, "UY": {}, "US": {}, "MX": {},
	"SA": {}, "AE": {}, "NG": {}, "EG": {}, "ZA": {}, "GH": {}, "KE": {}, "MA": {},
}

// IsTargetMarketCatered returns true if primary audience markets can stream the video.
func IsTargetMarketCatered(reg *RegionInfo) bool {
	if reg == nil {
		return true
	}
	if len(reg.Blocked) > 0 {
		for _, b := range reg.Blocked {
			if _, exists := KeyFootballMarkets[strings.ToUpper(b)]; exists {
				return false
			}
		}
	}
	if len(reg.Allowed) > 0 {
		for _, a := range reg.Allowed {
			if _, exists := KeyFootballMarkets[strings.ToUpper(a)]; exists {
				return true
			}
		}
		return false
	}
	return true
}

// CalculateWorldwideWatchability calculates global availability percentage (0.0 to 100.0).
func CalculateWorldwideWatchability(reg *RegionInfo) float64 {
	if reg == nil {
		return 100.0
	}
	const totalCountries = 249.0
	if len(reg.Allowed) > 0 {
		return float64(len(reg.Allowed)) / totalCountries * 100.0
	}
	if len(reg.Blocked) > 0 {
		avail := totalCountries - float64(len(reg.Blocked))
		if avail < 0 {
			avail = 0
		}
		return avail / totalCountries * 100.0
	}
	return 100.0
}

// CheckMatchTargetMarketCutoff determines if match should be permanently marked terminal.
func CheckMatchTargetMarketCutoff(m Match, now time.Time, sweepCount int) (bool, string) {
	if m.RegionInfo != nil && m.RegionInfo.Terminal {
		return true, "Already marked terminal"
	}
	ageHours := now.Sub(m.UTCDate).Hours()
	ageDays := ageHours / 24.0

	// Protect active 2-week news cycle: matches under 14 days are never exhausted
	if ageDays < 14.0 {
		return false, fmt.Sprintf("Active Tier 1: age %.1fd (<14d), sweeps %d (protected from exhaustion)", ageDays, sweepCount)
	}

	// Cutoff for matches older than 14 days: 6 sweeps across official channels
	if sweepCount >= 6 && ageDays >= 14.0 {
		return true, fmt.Sprintf("Exhausted: %d sweeps across official channels and match completed %.1f days ago", sweepCount, ageDays)
	}

	return false, fmt.Sprintf("Active: sweeps %d, age %.1fd (cutoff at 6 sweeps + 14 days)", sweepCount, ageDays)
}
