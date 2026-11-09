package domain

import "time"

// QuotaMetrics tracks YouTube Data API v3 quota usage.
type QuotaMetrics struct {
	UsedToday      int        `json:"used_today"`
	DailyLimit     int        `json:"daily_limit"` // Approved: 60,000 | Target: 54,000
	PercentageUsed float64    `json:"percentage_used"`
	ResetsAt       *time.Time `json:"resets_at,omitempty"`
}

// PipelineStatus tracks the health of an operational pipeline.
type PipelineStatus struct {
	Name           string     `json:"name"`
	Status         string     `json:"status"` // IDLE | RUNNING | DEGRADED | ERROR
	LastRun        *time.Time `json:"last_run,omitempty"`
	ItemsProcessed int        `json:"items_processed"`
	ErrorsCount    int        `json:"errors_count"`
}

// EventRecord models a granular diagnostic log event.
type EventRecord struct {
	ID        string    `json:"id,omitempty"`
	CreatedAt time.Time `json:"created_at"`
	Pipeline  string    `json:"pipeline"`
	Service   string    `json:"service"`
	Level     string    `json:"level"` // INFO | WARN | ERROR
	MatchID   *int64    `json:"match_id,omitempty"`
	JobID     *string   `json:"job_id,omitempty"`
	TraceID   *string   `json:"trace_id,omitempty"`
	Message   string    `json:"message"`
}

// CycleTelemetryPayload models the aggregated telemetry state persisted to PostgreSQL.
type CycleTelemetryPayload struct {
	CycleID    string           `json:"cycle_id"`
	StartedAt  time.Time        `json:"started_at"`
	EndedAt    *time.Time       `json:"ended_at,omitempty"`
	DurationMs int64            `json:"duration_ms"`
	Quota      QuotaMetrics     `json:"quota"`
	Pipelines  []PipelineStatus `json:"pipelines"`
	Events     []EventRecord    `json:"events"`
}
