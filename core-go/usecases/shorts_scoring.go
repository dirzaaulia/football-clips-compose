package usecases

import (
	"math"
	"strings"
	"time"
)

func isCommercialSpam(title string) bool {
	tLow := strings.ToLower(title)
	for _, w := range []string{"merchandise", "kit reveal", "buy tickets", "shop now", "sponsor", "discount", "code"} {
		if strings.Contains(tLow, w) {
			return true
		}
	}
	return false
}

func classifyShortCategory(title string) string {
	tLow := strings.ToLower(title)
	if strings.Contains(tLow, "moment") || strings.Contains(tLow, "celebration") || strings.Contains(tLow, "fans") {
		return "MOMENTS"
	}
	return "GOALS_SKILLS"
}

// ClassifyHypeLevel maps numeric hype score (0-100) to human-readable hype tiers.
func ClassifyHypeLevel(score int) string {
	switch {
	case score >= 90:
		return "LEGENDARY"
	case score >= 80:
		return "VIRAL"
	case score >= 70:
		return "TRENDING"
	default:
		return "FRESH"
	}
}

func calculateHypeScore(title, channelName string, publishedAt time.Time, views, likes int64) int {
	score := 40 + clubTierBonus(title, channelName) + actionKeywordBonus(title)
	score += engagementVelocityBonus(views, likes) + freshnessBonus(publishedAt)
	if score > 100 {
		return 100
	}
	return score
}

func clubTierBonus(title, channelName string) int {
	tLow := strings.ToLower(title)
	cLow := strings.ToLower(channelName)
	tier1 := []string{
		"real madrid", "barcelona", "arsenal", "manchester city", "liverpool",
		"manchester united", "chelsea", "bayern", "juventus", "inter", "milan",
		"psg", "tottenham", "dortmund", "atletico", "leverkusen", "napoli",
		"al nassr", "al hilal", "inter miami", "persija", "persib", "boca", "river",
		"galatasaray", "fenerbahce", "j.league", "liga mx", "club brugge",
	}
	for _, club := range tier1 {
		if strings.Contains(cLow, club) || strings.Contains(tLow, club) {
			return 18
		}
	}
	return 0
}

func actionKeywordBonus(title string) int {
	tLow := strings.ToLower(title)
	for _, kw := range []string{"golazo", "screamer", "banger", "curler", "rocket", "bicycle", "impossible", "insane", "worldie", "magic"} {
		if strings.Contains(tLow, kw) {
			return 15
		}
	}
	for _, kw := range []string{"goal", "gol", "strike", "assist", "nutmeg", "dribble", "save", "tekkers", "winner", "finish", "penalty"} {
		if strings.Contains(tLow, kw) {
			return 10
		}
	}
	return 0
}

func engagementVelocityBonus(views, likes int64) int {
	bonus := 0
	if views > 100 {
		// Log10(10k) = 4 -> +10; Log10(1M) = 6 -> +22
		logV := math.Log10(float64(views))
		if logV >= 3.0 {
			bonus += int(math.Min(22.0, (logV-2.5)*6.0))
		}
	}
	if views > 0 && likes > 0 {
		ratio := float64(likes) / float64(views)
		if ratio >= 0.04 {
			bonus += 6
		} else if ratio >= 0.02 {
			bonus += 3
		}
	}
	return bonus
}

func freshnessBonus(publishedAt time.Time) int {
	ageHours := time.Since(publishedAt).Hours()
	switch {
	case ageHours < 24.0:
		return 15
	case ageHours < 72.0:
		return 10
	case ageHours < 168.0:
		return 5
	default:
		return 0
	}
}
