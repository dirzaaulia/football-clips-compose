package usecases

import (
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

func calculateHypeScore(title, channelName string, publishedAt time.Time) int {
	score := 50
	tLow := strings.ToLower(title)
	cLow := strings.ToLower(channelName)

	tier1 := []string{
		"real madrid", "barcelona", "arsenal", "manchester city", "liverpool",
		"manchester united", "chelsea", "bayern", "juventus", "inter", "milan",
		"psg", "tottenham", "dortmund", "atletico", "leverkusen", "napoli",
		"al nassr", "inter miami", "persija", "persib", "boca", "river",
	}
	for _, club := range tier1 {
		if strings.Contains(cLow, club) || strings.Contains(tLow, club) {
			score += 20
			break
		}
	}

	for _, kw := range []string{"golazo", "screamer", "banger", "curler", "rocket", "impossible", "insane", "worldie", "magic"} {
		if strings.Contains(tLow, kw) {
			score += 20
			break
		}
	}

	for _, kw := range []string{"goal", "gol", "strike", "assist", "nutmeg", "dribble", "save", "tekkers", "winner", "finish"} {
		if strings.Contains(tLow, kw) {
			score += 15
			break
		}
	}

	if time.Since(publishedAt).Hours() < 168.0 {
		score += 10
	}
	if score > 100 {
		return 100
	}
	return score
}
