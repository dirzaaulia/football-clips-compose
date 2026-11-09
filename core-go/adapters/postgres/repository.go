package postgres

import (
	"context"
	"fmt"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/domain"
	"github.com/dirzaaulia/footballclips/core-go/ports"
	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

// Repository implements ports.MatchRepository and ports.TelemetryRepository using pgxpool.
type Repository struct {
	pool *pgxpool.Pool
}

var _ ports.MatchRepository = (*Repository)(nil)
var _ ports.TelemetryRepository = (*Repository)(nil)

// NewRepository initializes a PostgreSQL connection pool.
func NewRepository(ctx context.Context, dsn string) (*Repository, error) {
	config, err := pgxpool.ParseConfig(dsn)
	if err != nil {
		return nil, fmt.Errorf("parse dsn: %w", err)
	}
	config.MaxConns = 10
	config.MinConns = 2
	config.MaxConnLifetime = 30 * time.Minute

	pool, err := pgxpool.NewWithConfig(ctx, config)
	if err != nil {
		return nil, fmt.Errorf("connect db pool: %w", err)
	}
	if err := pool.Ping(ctx); err != nil {
		return nil, fmt.Errorf("ping db: %w", err)
	}
	return &Repository{pool: pool}, nil
}

// Close closes the underlying pool.
func (r *Repository) Close() {
	if r.pool != nil {
		r.pool.Close()
	}
}

// GetMatchesForSync fetches matches within the given time window.
func (r *Repository) GetMatchesForSync(ctx context.Context, start, end time.Time) ([]domain.Match, error) {
	query := `
		SELECT id, competition_id, competition_name, utc_date, status,
		       home_team_name, away_team_name, home_score, away_score,
		       highlight_video_id, season, updated_at
		FROM matches
		WHERE utc_date >= $1 AND utc_date <= $2
		ORDER BY utc_date ASC`

	rows, err := r.pool.Query(ctx, query, start, end)
	if err != nil {
		return nil, fmt.Errorf("query matches for sync: %w", err)
	}
	defer rows.Close()

	var matches []domain.Match
	for rows.Next() {
		var m domain.Match
		err := rows.Scan(
			&m.ID, &m.CompetitionID, &m.CompetitionName, &m.UTCDate, &m.Status,
			&m.HomeTeamName, &m.AwayTeamName, &m.HomeScore, &m.AwayScore,
			&m.HighlightVideoID, &m.Season, &m.UpdatedAt,
		)
		if err != nil {
			return nil, fmt.Errorf("scan match: %w", err)
		}
		matches = append(matches, m)
	}
	return matches, nil
}

// UpsertMatches batch upserts matches into the database using pgx.Batch.
func (r *Repository) UpsertMatches(ctx context.Context, matches []domain.Match) error {
	if len(matches) == 0 {
		return nil
	}
	batch := &pgx.Batch{}
	query := `
		INSERT INTO matches (
			id, competition_id, competition_name, utc_date, status,
			matchday, home_team_id, home_team_name, home_team_crest,
			away_team_id, away_team_name, away_team_crest,
			home_score, away_score, home_penalties, away_penalties,
			season, display_clock, updated_at
		) VALUES (
			$1, $2, $3, $4, $5,
			$6, $7, $8, $9,
			$10, $11, $12,
			$13, $14, $15, $16,
			$17, $18, $19
		)
		ON CONFLICT (id) DO UPDATE SET
			status = EXCLUDED.status,
			home_score = EXCLUDED.home_score,
			away_score = EXCLUDED.away_score,
			home_penalties = EXCLUDED.home_penalties,
			away_penalties = EXCLUDED.away_penalties,
			display_clock = EXCLUDED.display_clock,
			updated_at = EXCLUDED.updated_at
		WHERE matches.season >= 2026`

	for _, m := range matches {
		batch.Queue(query,
			m.ID, m.CompetitionID, m.CompetitionName, m.UTCDate, m.Status,
			m.Matchday, m.HomeTeamID, m.HomeTeamName, m.HomeTeamCrest,
			m.AwayTeamID, m.AwayTeamName, m.AwayTeamCrest,
			m.HomeScore, m.AwayScore, m.HomePenalties, m.AwayPenalties,
			m.Season, m.DisplayClock, time.Now().UTC(),
		)
	}

	br := r.pool.SendBatch(ctx, batch)
	defer br.Close()

	for range matches {
		if _, err := br.Exec(); err != nil {
			return fmt.Errorf("batch upsert match: %w", err)
		}
	}
	return nil
}
