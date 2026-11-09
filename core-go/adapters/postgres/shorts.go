package postgres

import (
	"context"
	"fmt"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/domain"
	"github.com/jackc/pgx/v5"
)

// GetExistingShortVideoIDs returns map of existing short YouTube video IDs.
func (r *Repository) GetExistingShortVideoIDs(ctx context.Context, limit int) (map[string]bool, error) {
	query := `
		SELECT youtube_video_id
		FROM football_shorts
		ORDER BY created_at DESC
		LIMIT $1`

	rows, err := r.pool.Query(ctx, query, limit)
	if err != nil {
		return nil, fmt.Errorf("query existing shorts: %w", err)
	}
	defer rows.Close()

	existing := make(map[string]bool)
	for rows.Next() {
		var vid string
		if err := rows.Scan(&vid); err == nil && vid != "" {
			existing[vid] = true
		}
	}
	return existing, nil
}

// UpsertShorts inserts new vertical short clips into football_shorts table.
func (r *Repository) UpsertShorts(ctx context.Context, shorts []domain.FootballShort) error {
	if len(shorts) == 0 {
		return nil
	}

	batch := &pgx.Batch{}
	query := `
		INSERT INTO football_shorts (
			youtube_video_id, title, channel_id, channel_name, club_id,
			category, thumbnail_url, duration_seconds, like_count,
			hype_score, is_featured, published_at, created_at
		) VALUES (
			$1, $2, $3, $4, $5,
			$6, $7, $8, $9,
			$10, $11, $12, $13
		)
		ON CONFLICT (youtube_video_id) DO UPDATE SET
			hype_score = EXCLUDED.hype_score,
			is_featured = EXCLUDED.is_featured,
			like_count = EXCLUDED.like_count`

	now := time.Now().UTC()
	for _, s := range shorts {
		batch.Queue(query,
			s.YouTubeVideoID, s.Title, s.ChannelID, s.ChannelName, s.ClubID,
			s.Category, s.ThumbnailURL, s.DurationSeconds, s.LikeCount,
			s.HypeScore, s.IsFeatured, s.PublishedAt, now,
		)
	}

	br := r.pool.SendBatch(ctx, batch)
	defer br.Close()

	for range shorts {
		if _, err := br.Exec(); err != nil {
			return fmt.Errorf("batch upsert short: %w", err)
		}
	}
	return nil
}

// GetActiveClubChannels returns official club YouTube channels for shorts discovery.
func (r *Repository) GetActiveClubChannels(ctx context.Context, limit int) ([]struct {
	ClubID    int64
	Name      string
	ChannelID string
}, error) {
	query := `
		SELECT team_id, name, youtube_channel_id
		FROM supported_clubs
		WHERE is_active = true
		  AND youtube_channel_id IS NOT NULL
		  AND youtube_channel_id != ''
		ORDER BY last_shorts_check ASC NULLS FIRST, channel_tier ASC, team_id ASC
		LIMIT $1`

	rows, err := r.pool.Query(ctx, query, limit)
	if err != nil {
		return nil, fmt.Errorf("query club channels: %w", err)
	}
	defer rows.Close()

	var channels []struct {
		ClubID    int64
		Name      string
		ChannelID string
	}
	for rows.Next() {
		var item struct {
			ClubID    int64
			Name      string
			ChannelID string
		}
		if err := rows.Scan(&item.ClubID, &item.Name, &item.ChannelID); err == nil && item.ChannelID != "" {
			channels = append(channels, item)
		}
	}
	return channels, nil
}

// MarkChannelsChecked updates last_shorts_check timestamp for round-robin rotation.
func (r *Repository) MarkChannelsChecked(ctx context.Context, clubIDs []int64) error {
	if len(clubIDs) == 0 {
		return nil
	}
	query := `UPDATE supported_clubs SET last_shorts_check = NOW() WHERE team_id = ANY($1)`
	_, err := r.pool.Exec(ctx, query, clubIDs)
	if err != nil {
		return fmt.Errorf("update last_shorts_check: %w", err)
	}
	return nil
}
