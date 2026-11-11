package postgres

import (
	"context"
	"encoding/json"
	"fmt"

	"github.com/dirzaaulia/footballclips/core-go/domain"
)

// GetPendingHighlights returns finished matches needing a highlight video.
func (r *Repository) GetPendingHighlights(ctx context.Context, limit int) ([]domain.Match, error) {
	query := `
		SELECT id, competition_id, competition_name, utc_date, status,
		       home_team_name, away_team_name, home_score, away_score,
		       season, updated_at, home_team_id, away_team_id,
		       last_youtube_check, youtube_region_info
		FROM matches
		WHERE status = 'FINISHED'
		  AND (highlight_video_id IS NULL OR highlight_video_id = '')
		  AND (youtube_region_info IS NULL OR (youtube_region_info->>'terminal') IS DISTINCT FROM 'true')
		  AND season >= 2026
		  AND utc_date >= NOW() - INTERVAL '30 days'
		ORDER BY utc_date DESC
		LIMIT $1`

	rows, err := r.pool.Query(ctx, query, limit)
	if err != nil {
		return nil, fmt.Errorf("query pending highlights: %w", err)
	}
	defer rows.Close()

	var matches []domain.Match
	for rows.Next() {
		var m domain.Match
		var regStr *string
		err := rows.Scan(
			&m.ID, &m.CompetitionID, &m.CompetitionName, &m.UTCDate, &m.Status,
			&m.HomeTeamName, &m.AwayTeamName, &m.HomeScore, &m.AwayScore,
			&m.Season, &m.UpdatedAt, &m.HomeTeamID, &m.AwayTeamID,
			&m.LastYouTubeCheck, &regStr,
		)
		if err != nil {
			return nil, fmt.Errorf("scan pending match: %w", err)
		}
		if regStr != nil && *regStr != "" {
			var reg domain.RegionInfo
			if err := json.Unmarshal([]byte(*regStr), &reg); err == nil {
				m.RegionInfo = &reg
			}
		}
		matches = append(matches, m)
	}
	return matches, nil
}

// UpdateMatchHighlight updates the linked YouTube highlight video ID for a match.
func (r *Repository) UpdateMatchHighlight(ctx context.Context, matchID int64, videoID string, reg *domain.RegionInfo, videoMeta ...map[string]any) error {
	var regJSON []byte
	if reg != nil {
		regJSON, _ = json.Marshal(reg)
	}
	var vidsJSON []byte
	if len(videoMeta) > 0 && videoMeta[0] != nil {
		vidsJSON, _ = json.Marshal([]map[string]any{videoMeta[0]})
	}
	query := `
		UPDATE matches
		SET highlight_video_id = $1,
		    last_youtube_check = NOW(),
		    youtube_region_info = $2::jsonb,
		    highlight_videos = CASE WHEN $3::jsonb IS NOT NULL THEN $3::jsonb ELSE highlight_videos END,
		    updated_at = NOW()
		WHERE id = $4 AND season >= 2026`
	_, err := r.pool.Exec(ctx, query, videoID, regJSON, vidsJSON, matchID)
	if err != nil {
		return fmt.Errorf("update highlight for match %d: %w", matchID, err)
	}
	return nil
}

// UpdateMatchCheck records an ongoing sweep check without changing the linked video.
func (r *Repository) UpdateMatchCheck(ctx context.Context, matchID int64, reg *domain.RegionInfo) error {
	var regJSON []byte
	if reg != nil {
		regJSON, _ = json.Marshal(reg)
	}
	query := `
		UPDATE matches
		SET last_youtube_check = NOW(),
		    youtube_region_info = COALESCE($1::jsonb, youtube_region_info),
		    updated_at = NOW()
		WHERE id = $2 AND season >= 2026`
	_, err := r.pool.Exec(ctx, query, regJSON, matchID)
	if err != nil {
		return fmt.Errorf("update check for match %d: %w", matchID, err)
	}
	return nil
}

// MarkMatchUnavailable marks a match as terminal when official highlights are unavailable.
func (r *Repository) MarkMatchUnavailable(ctx context.Context, matchID int64, reason string, reg *domain.RegionInfo) error {
	if reg == nil {
		reg = &domain.RegionInfo{}
	}
	reg.Terminal = true
	reg.Status = "UNAVAILABLE"
	reg.Reason = reason
	regJSON, _ := json.Marshal(reg)

	query := `
		UPDATE matches
		SET last_youtube_check = NOW(),
		    youtube_region_info = $1::jsonb,
		    updated_at = NOW()
		WHERE id = $2 AND season >= 2026`
	_, err := r.pool.Exec(ctx, query, regJSON, matchID)
	if err != nil {
		return fmt.Errorf("mark unavailable for match %d: %w", matchID, err)
	}
	return nil
}

// GetMatchChannels returns official YouTube channel IDs ordered: League -> Clubs -> Broadcasters.
func (r *Repository) GetMatchChannels(ctx context.Context, match domain.Match) ([]string, error) {
	var channels []string
	seen := make(map[string]struct{})

	addChannel := func(cid string) {
		if cid != "" {
			if _, exists := seen[cid]; !exists {
				seen[cid] = struct{}{}
				channels = append(channels, cid)
			}
		}
	}

	// 1. Official League & Primary Broadcaster Channels from competitions
	if match.CompetitionID != "" {
		compQuery := `
			SELECT COALESCE(youtube_channel_id, ''),
			       COALESCE(backup_channel_ids, '{}')
			FROM competitions
			WHERE id = $1`
		var primaryCID string
		var backupCIDs []string
		if err := r.pool.QueryRow(ctx, compQuery, match.CompetitionID).Scan(&primaryCID, &backupCIDs); err == nil {
			addChannel(primaryCID)
			for _, bcid := range backupCIDs {
				addChannel(bcid)
			}
		}
	}

	// 2. Official Club Channels from supported_clubs
	if match.HomeTeamID != nil || match.AwayTeamID != nil {
		clubQuery := `
			SELECT DISTINCT youtube_channel_id
			FROM supported_clubs
			WHERE team_id IN ($1, $2)
			  AND youtube_channel_id IS NOT NULL
			  AND youtube_channel_id != ''
			  AND is_active = true`
		rows, err := r.pool.Query(ctx, clubQuery, match.HomeTeamID, match.AwayTeamID)
		if err == nil {
			defer rows.Close()
			for rows.Next() {
				var cid string
				if err := rows.Scan(&cid); err == nil {
					addChannel(cid)
				}
			}
		}
	}

	// 3. Licensed Regional Broadcasters strictly mapped to this competition
	if match.CompetitionID != "" {
		bcastQuery := `
			SELECT DISTINCT channel_id
			FROM broadcasters
			WHERE competitions ? $1
			  AND channel_id IS NOT NULL
			  AND channel_id != ''
			  AND status = 'active'`
		rows, err := r.pool.Query(ctx, bcastQuery, match.CompetitionID)
		if err == nil {
			defer rows.Close()
			for rows.Next() {
				var cid string
				if err := rows.Scan(&cid); err == nil {
					addChannel(cid)
				}
			}
		}
	}

	return channels, nil
}
