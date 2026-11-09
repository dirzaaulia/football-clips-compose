package postgres

import (
	"context"
	"encoding/json"
	"fmt"

	"github.com/dirzaaulia/footballclips/core-go/domain"
)

// SaveWaterfallTrace records the full diagnostic telemetry into worker_events.
func (r *Repository) SaveWaterfallTrace(ctx context.Context, trace domain.MatchWaterfallTrace) error {
	payload, err := json.Marshal(trace)
	if err != nil {
		return fmt.Errorf("marshal waterfall trace: %w", err)
	}
	msg := fmt.Sprintf("[HIGHLIGHT_TRACE] %s vs %s: %s (%d candidates)",
		trace.HomeTeam, trace.AwayTeam, trace.Outcome, len(trace.Candidates),
	)
	query := `
		INSERT INTO worker_events (level, subsystem, action, message, metadata)
		VALUES ($1, $2, $3, $4, $5)`
	_, err = r.pool.Exec(ctx, query, "INFO", "HIGHLIGHT", "WATERFALL_TRACE", msg, payload)
	if err != nil {
		return fmt.Errorf("save waterfall trace %d: %w", trace.MatchID, err)
	}
	return nil
}

// SaveTelemetryState persists aggregated telemetry payload into telemetry_state.
func (r *Repository) SaveTelemetryState(ctx context.Context, payload domain.CycleTelemetryPayload) error {
	bytes, err := json.Marshal(payload)
	if err != nil {
		return fmt.Errorf("marshal telemetry payload: %w", err)
	}
	query := `
		INSERT INTO telemetry_state (key, payload, updated_at)
		VALUES ('telemetry', $1, NOW())
		ON CONFLICT (key) DO UPDATE SET
			payload = EXCLUDED.payload,
			updated_at = NOW()`
	_, err = r.pool.Exec(ctx, query, bytes)
	if err != nil {
		return fmt.Errorf("upsert telemetry_state: %w", err)
	}
	return nil
}

// RecordEvent logs an individual operational event into worker_events.
func (r *Repository) RecordEvent(ctx context.Context, event domain.EventRecord) error {
	metadata, _ := json.Marshal(map[string]any{
		"match_id": event.MatchID,
		"pipeline": event.Pipeline,
		"service":  event.Service,
	})
	query := `
		INSERT INTO worker_events (level, subsystem, action, message, metadata)
		VALUES ($1, $2, $3, $4, $5)`
	_, err := r.pool.Exec(ctx, query, event.Level, event.Pipeline, event.Service, event.Message, metadata)
	if err != nil {
		return fmt.Errorf("record event: %w", err)
	}
	return nil
}
