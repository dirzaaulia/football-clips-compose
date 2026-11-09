package domain

import "time"

// StandingEntry models a single club rank in a league table.
type StandingEntry struct {
	ID              string    `json:"id"`
	CompetitionCode string    `json:"competition_code"`
	Position        int       `json:"position"`
	TeamID          int64     `json:"team_id"`
	TeamName        string    `json:"team_name"`
	TeamShortName   *string   `json:"team_short_name,omitempty"`
	CrestURL        *string   `json:"crest_url,omitempty"`
	PlayedGames     int       `json:"played_games"`
	Won             int       `json:"won"`
	Draw            int       `json:"draw"`
	Lost            int       `json:"lost"`
	Points          int       `json:"points"`
	GoalsFor        int       `json:"goals_for"`
	GoalsAgainst    int       `json:"goals_against"`
	GoalDifference  int       `json:"goal_difference"`
	Season          int       `json:"season"`
	Form            *string   `json:"form,omitempty"`
	UpdatedAt       time.Time `json:"updated_at"`
}
