package ports

import (
	"context"

	"github.com/dirzaaulia/footballclips/core-go/domain"
)

// FCMClient defines topic-based push notification dispatch.
type FCMClient interface {
	SendTopic(ctx context.Context, topic, title, body string, data map[string]string) error
	NotifyMatchEvent(ctx context.Context, match domain.Match, eventType string, homeClubID, awayClubID int) error
	NotifyHighlightLinked(ctx context.Context, match domain.Match, videoID, title string, homeClubID, awayClubID int) error
}
