package espn

import (
	"fmt"
	"strconv"
	"strings"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/domain"
)

// RawTeam models team node in ESPN event response.
type RawTeam struct {
	ID          string `json:"id"`
	Name        string `json:"name"`
	DisplayName string `json:"displayName"`
	Logo        string `json:"logo"`
}

// RawCompetitor models home/away competitor in ESPN event response.
type RawCompetitor struct {
	HomeAway      string  `json:"homeAway"`
	Score         *string `json:"score"`
	ShootoutScore *string `json:"shootoutScore"`
	Team          RawTeam `json:"team"`
}

// RawStatus models status object in ESPN event response.
type RawStatus struct {
	DisplayClock string `json:"displayClock"`
	Type         struct {
		State  string `json:"state"`
		Detail string `json:"detail"`
	} `json:"type"`
}

// RawCompetition models the competition block inside an ESPN event.
type RawCompetition struct {
	Date         string          `json:"date"`
	Competitors  []RawCompetitor `json:"competitors"`
	Status       RawStatus       `json:"status"`
	Type         struct {
		Text string `json:"text"`
	} `json:"type"`
}

// RawEvent models an ESPN scoreboard event.
type RawEvent struct {
	ID           string           `json:"id"`
	Date         string           `json:"date"`
	Competitions []RawCompetition `json:"competitions"`
	Season       struct {
		Year int    `json:"year"`
		Slug string `json:"slug"`
	} `json:"season"`
}

// NormalizeStatus converts ESPN raw state and detail to standard status string.
func NormalizeStatus(state, detail string) string {
	stateUpper := strings.ToUpper(state)
	detailUpper := strings.ToUpper(detail)

	switch {
	case strings.Contains(detailUpper, "ABANDONED"):
		return "ABANDONED"
	case strings.Contains(detailUpper, "SUSPENDED") || strings.Contains(detailUpper, "INTERRUPTED"):
		return "INTERRUPTED"
	case strings.Contains(detailUpper, "POSTPONED"):
		return "POSTPONED"
	case strings.Contains(detailUpper, "CANCELLED") || strings.Contains(detailUpper, "CANCELED"):
		return "CANCELLED"
	case stateUpper == "PRE":
		return "TIMED"
	case stateUpper == "IN":
		if strings.Contains(detailUpper, "HALFTIME") || detailUpper == "HT" {
			return "PAUSED"
		}
		return "IN_PLAY"
	case stateUpper == "POST":
		return "FINISHED"
	default:
		return "SCHEDULED"
	}
}

// FormatRoundSlug transforms round slugs into Title Case.
func FormatRoundSlug(slug string) string {
	if slug == "" {
		return "Main Round"
	}
	words := strings.Fields(strings.ReplaceAll(strings.ReplaceAll(slug, "-", " "), "_", " "))
	for i, w := range words {
		if len(w) > 0 {
			words[i] = strings.ToUpper(w[:1]) + strings.ToLower(w[1:])
		}
	}
	return strings.Join(words, " ")
}

// ParseEvent transforms raw ESPN event into standard domain.Match entity.
func ParseEvent(ev RawEvent, leagueCode, leagueName string) (*domain.Match, error) {
	if len(ev.Competitions) == 0 {
		return nil, fmt.Errorf("no competitions in event %s", ev.ID)
	}
	eventID, err := strconv.ParseInt(ev.ID, 10, 64)
	if err != nil {
		return nil, fmt.Errorf("invalid event id %s: %w", ev.ID, err)
	}

	comp := ev.Competitions[0]
	parsedDate, err := time.Parse(time.RFC3339, strings.ReplaceAll(ev.Date, "Z", "+00:00"))
	if err != nil {
		parsedDate = time.Now().UTC()
	}

	status := NormalizeStatus(comp.Status.Type.State, comp.Status.Type.Detail)
	displayClock := comp.Status.DisplayClock

	match := &domain.Match{
		ID:              eventID,
		CompetitionID:   leagueCode,
		CompetitionName: leagueName,
		UTCDate:         parsedDate.UTC(),
		Status:          status,
		Season:          ev.Season.Year,
		DisplayClock:    &displayClock,
		UpdatedAt:       time.Now().UTC(),
	}

	populateTeamsAndScores(match, comp.Competitors, status)
	return match, nil
}

func populateTeamsAndScores(m *domain.Match, competitors []RawCompetitor, status string) {
	for _, c := range competitors {
		teamID, _ := strconv.ParseInt(c.Team.ID, 10, 64)
		name := c.Team.DisplayName
		if name == "" {
			name = c.Team.Name
		}
		crest := c.Team.Logo

		if c.HomeAway == "home" {
			m.HomeTeamID = &teamID
			m.HomeTeamName = name
			m.HomeTeamCrest = &crest
			if m.IsLive() || m.IsFinished() {
				if c.Score != nil {
					score, _ := strconv.Atoi(*c.Score)
					m.HomeScore = &score
				}
				if c.ShootoutScore != nil {
					pen, _ := strconv.Atoi(*c.ShootoutScore)
					m.HomePenalties = &pen
				}
			}
		} else {
			m.AwayTeamID = &teamID
			m.AwayTeamName = name
			m.AwayTeamCrest = &crest
			if m.IsLive() || m.IsFinished() {
				if c.Score != nil {
					score, _ := strconv.Atoi(*c.Score)
					m.AwayScore = &score
				}
				if c.ShootoutScore != nil {
					pen, _ := strconv.Atoi(*c.ShootoutScore)
					m.AwayPenalties = &pen
				}
			}
		}
	}
}
