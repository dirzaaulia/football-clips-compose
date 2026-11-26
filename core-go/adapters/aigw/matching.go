package aigw

import (
	"strings"
	"unicode"
)

var knownTeamAliases = map[string][]string{
	"internazionale":          {"inter", "internazionale"},
	"inter milan":             {"inter", "internazionale"},
	"psv eindhoven":           {"psv", "eindhoven"},
	"lask linz":               {"lask"},
	"bodo/glimt":              {"bodo", "glimt"},
	"bodo glimt":              {"bodo", "glimt"},
	"hamburg sv":              {"hamburg", "hsv"},
	"hamburger sv":            {"hamburg", "hsv"},
	"werder bremen":           {"bremen", "werder"},
	"celta vigo":              {"celta"},
	"ajax amsterdam":          {"ajax"},
	"atletico madrid":         {"atleti", "atletico"},
	"athletic club":           {"athletic", "bilbao"},
	"real sociedad":           {"sociedad", "la real"},
	"paris saint-germain":     {"psg", "paris"},
	"borussia dortmund":       {"dortmund", "bvb"},
	"bayern munich":           {"bayern"},
	"bayern munchen":          {"bayern"},
	"bayer leverkusen":        {"leverkusen"},
	"sporting cp":             {"sporting"},
	"benfica":                 {"benfica"},
	"porto":                   {"porto"},
	"juventus":                {"juve", "juventus"},
	"ac milan":                {"milan"},
	"as roma":                 {"roma"},
	"ss lazio":                {"lazio"},
	"ssc napoli":              {"napoli"},
	"argentinos juniors":      {"argentinos"},
	"independiente":           {"independiente"},
	"palmeiras":               {"palmeiras"},
	"flamengo":                {"flamengo"},
	"manchester united":       {"man utd", "man united", "united"},
	"manchester city":         {"man city", "city"},
	"tottenham hotspur":       {"tottenham", "spurs"},
	"wolverhampton wanderers": {"wolves"},
	"brighton & hove albion":  {"brighton"},
	"nottingham forest":       {"forest"},
	"racing santander":        {"racing", "real racing", "r. racing"},
	"stade brestois":          {"brest", "stade brestois"},
	"brest":                   {"brest", "stade brestois"},
	"strasbourg":              {"strasbourg", "rc strasbourg"},
	"fc cincinnati":           {"cincinnati", "fc cincinnati"},
}

func normalizeText(s string) string {
	s = strings.ToLower(s)
	replacer := strings.NewReplacer(
		"ø", "o", "æ", "ae", "å", "a",
		"é", "e", "è", "e", "ê", "e", "ë", "e",
		"á", "a", "à", "a", "â", "a", "ä", "a",
		"í", "i", "ì", "i", "î", "i", "ï", "i",
		"ó", "o", "ò", "o", "ô", "o", "ö", "o",
		"ú", "u", "ù", "u", "û", "u", "ü", "u",
		"ñ", "n", "ç", "c", "ß", "ss",
	)
	return replacer.Replace(s)
}

func containsWordToken(text, token string) bool {
	idx := 0
	for {
		pos := strings.Index(text[idx:], token)
		if pos == -1 {
			return false
		}
		start := idx + pos
		end := start + len(token)

		leftOk := start == 0 || !unicode.IsLetter(rune(text[start-1]))
		rightOk := end == len(text) || !unicode.IsLetter(rune(text[end]))

		if leftOk && rightOk {
			return true
		}
		idx = start + 1
		if idx >= len(text) {
			return false
		}
	}
}

func matchesTeam(title, team string) bool {
	tNorm := normalizeText(title)
	tmNorm := normalizeText(team)

	if strings.Contains(tNorm, tmNorm) {
		return true
	}

	if aliases, ok := knownTeamAliases[tmNorm]; ok {
		for _, alias := range aliases {
			if containsWordToken(tNorm, alias) {
				return true
			}
		}
	}

	for _, w := range strings.Fields(tmNorm) {
		cleanW := strings.TrimFunc(w, func(r rune) bool {
			return !unicode.IsLetter(r) && !unicode.IsNumber(r)
		})
		if len(cleanW) >= 5 {
			if strings.Contains(tNorm, cleanW) {
				return true
			}
		} else if len(cleanW) >= 3 {
			if containsWordToken(tNorm, cleanW) {
				return true
			}
		}
	}
	return false
}
