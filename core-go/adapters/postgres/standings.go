package postgres

import (
	"context"
	"fmt"
	"strings"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/domain"
	"github.com/jackc/pgx/v5"
)

// UpsertStandings batch upserts league table entries into PostgreSQL standings.
func (r *Repository) UpsertStandings(ctx context.Context, entries []domain.StandingEntry) error {
	if len(entries) == 0 {
		return nil
	}

	batch := &pgx.Batch{}
	query := `
		INSERT INTO standings (
			id, competition_code, position, team_id, team_name, team_short_name,
			crest_url, played_games, won, draw, lost, points,
			goals_for, goals_against, goal_difference, season, form, updated_at
		) VALUES (
			$1, $2, $3, $4, $5, $6,
			$7, $8, $9, $10, $11, $12,
			$13, $14, $15, $16, $17, $18
		)
		ON CONFLICT (id) DO UPDATE SET
			position = EXCLUDED.position,
			played_games = EXCLUDED.played_games,
			won = EXCLUDED.won,
			draw = EXCLUDED.draw,
			lost = EXCLUDED.lost,
			points = EXCLUDED.points,
			goals_for = EXCLUDED.goals_for,
			goals_against = EXCLUDED.goals_against,
			goal_difference = EXCLUDED.goal_difference,
			form = COALESCE(EXCLUDED.form, standings.form),
			updated_at = EXCLUDED.updated_at`

	now := time.Now().UTC()
	for _, e := range entries {
		batch.Queue(query,
			e.ID, e.CompetitionCode, e.Position, e.TeamID, e.TeamName, e.TeamShortName,
			e.CrestURL, e.PlayedGames, e.Won, e.Draw, e.Lost, e.Points,
			e.GoalsFor, e.GoalsAgainst, e.GoalDifference, e.Season, e.Form, now,
		)
	}

	br := r.pool.SendBatch(ctx, batch)
	defer br.Close()

	for range entries {
		if _, err := br.Exec(); err != nil {
			return fmt.Errorf("batch upsert standings: %w", err)
		}
	}
	return nil
}

// GetTeamFormFromMatches computes 5-match form guide (W, D, L) for clubs in a league.
func (r *Repository) GetTeamFormFromMatches(ctx context.Context, compCode string) (map[string]string, error) {
	query := `
		SELECT home_team_name, away_team_name, home_score, away_score
		FROM matches
		WHERE competition_id = $1
		  AND status = 'FINISHED'
		  AND home_score IS NOT NULL
		  AND away_score IS NOT NULL
		ORDER BY utc_date DESC
		LIMIT 150`

	rows, err := r.pool.Query(ctx, query, compCode)
	if err != nil {
		return nil, fmt.Errorf("query form matches %s: %w", compCode, err)
	}
	defer rows.Close()

	teamHistory := make(map[string][]string)
	for rows.Next() {
		var home, away string
		var hScore, aScore int
		if err := rows.Scan(&home, &away, &hScore, &aScore); err != nil {
			continue
		}

		hNorm := strings.ToLower(strings.TrimSpace(home))
		aNorm := strings.ToLower(strings.TrimSpace(away))

		var hRes, aRes string
		if hScore > aScore {
			hRes, aRes = "W", "L"
		} else if aScore > hScore {
			hRes, aRes = "L", "W"
		} else {
			hRes, aRes = "D", "D"
		}

		if len(teamHistory[hNorm]) < 5 {
			teamHistory[hNorm] = append(teamHistory[hNorm], hRes)
		}
		if len(teamHistory[aNorm]) < 5 {
			teamHistory[aNorm] = append(teamHistory[aNorm], aRes)
		}
	}

	formMap := make(map[string]string)
	for team, results := range teamHistory {
		formMap[team] = strings.Join(results, ",")
	}
	return formMap, nil
}
