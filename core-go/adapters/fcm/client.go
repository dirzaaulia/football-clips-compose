package fcm

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"log/slog"
	"net/http"
	"sync"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/domain"
	"github.com/dirzaaulia/footballclips/core-go/ports"
)

type Client struct {
	tokenProvider *TokenProvider
	repo          ports.TelemetryRepository
	logger        *slog.Logger
	httpClient    *http.Client
	sentKeys      sync.Map
}

func NewClient(tp *TokenProvider, repo ports.TelemetryRepository, logger *slog.Logger) *Client {
	if logger == nil {
		logger = slog.Default()
	}
	return &Client{
		tokenProvider: tp,
		repo:          repo,
		logger:        logger,
		httpClient:    &http.Client{Timeout: 10 * time.Second},
	}
}

func (c *Client) SendTopic(ctx context.Context, topic, title, body string, data map[string]string) error {
	if c.tokenProvider == nil {
		c.logger.Debug("[FCM] Token provider not configured, skipping send", slog.String("topic", topic))
		return nil
	}

	tok, err := c.tokenProvider.GetAccessToken()
	if err != nil {
		return fmt.Errorf("get access token: %w", err)
	}

	url := fmt.Sprintf("https://fcm.googleapis.com/v1/projects/%s/messages:send", c.tokenProvider.ProjectID())
	payload := map[string]any{
		"message": map[string]any{
			"topic": topic,
			"notification": map[string]string{
				"title": title,
				"body":  body,
			},
			"data": data,
			"android": map[string]any{
				"priority": "high",
				"notification": map[string]string{
					"channel_id": "fc_channel_live_scores",
					"icon":       "ic_notification",
					"color":      "#00E5FF",
				},
			},
		},
	}

	b, _ := json.Marshal(payload)
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, url, bytes.NewReader(b))
	if err != nil {
		return err
	}
	req.Header.Set("Authorization", "Bearer "+tok)
	req.Header.Set("Content-Type", "application/json")

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("dispatch fcm: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode >= 300 {
		respBytes, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("fcm error status %d: %s", resp.StatusCode, string(respBytes))
	}

	c.logger.Info("[FCM] Sent topic alert", slog.String("topic", topic), slog.String("title", title))
	return nil
}

func (c *Client) NotifyMatchEvent(ctx context.Context, match domain.Match, eventType string, homeClubID, awayClubID int) error {
	dedupKey := fmt.Sprintf("%s:%d:%s", eventType, match.ID, match.Status)
	if _, loaded := c.sentKeys.LoadOrStore(dedupKey, true); loaded {
		return nil
	}

	title := fmt.Sprintf("%s vs %s", match.HomeTeamName, match.AwayTeamName)
	var body string
	hScore := 0
	aScore := 0
	if match.HomeScore != nil {
		hScore = *match.HomeScore
	}
	if match.AwayScore != nil {
		aScore = *match.AwayScore
	}

	switch eventType {
	case "kickoff":
		body = "Kickoff! The match is underway."
	case "score":
		body = fmt.Sprintf("Score update: %d - %d", hScore, aScore)
	case "fulltime":
		body = fmt.Sprintf("Full Time: %d - %d", hScore, aScore)
	default:
		body = fmt.Sprintf("Match update: %s", match.Status)
	}

	data := map[string]string{
		"match_id":   fmt.Sprintf("%d", match.ID),
		"event_type": eventType,
		"utc_date":   match.UTCDate.Format(time.RFC3339),
		"status":     match.Status,
		"home_team":  match.HomeTeamName,
		"away_team":  match.AwayTeamName,
		"home_id":    fmt.Sprintf("%d", homeClubID),
		"away_id":    fmt.Sprintf("%d", awayClubID),
		"score":      fmt.Sprintf("%d - %d", hScore, aScore),
	}

	var topics []string
	if homeClubID > 0 {
		topics = append(topics, fmt.Sprintf("club_%d", homeClubID))
	}
	if awayClubID > 0 {
		topics = append(topics, fmt.Sprintf("club_%d", awayClubID))
	}
	topics = append(topics, fmt.Sprintf("match_%d", match.ID))

	for _, topic := range topics {
		_ = c.SendTopic(ctx, topic, title, body, data)
	}
	return nil
}

func (c *Client) NotifyHighlightLinked(ctx context.Context, match domain.Match, videoID, title string, homeClubID, awayClubID int) error {
	dedupKey := fmt.Sprintf("hl:%d:%s", match.ID, videoID)
	if _, loaded := c.sentKeys.LoadOrStore(dedupKey, true); loaded {
		return nil
	}

	notifTitle := fmt.Sprintf("Official Highlights: %s vs %s", match.HomeTeamName, match.AwayTeamName)
	body := fmt.Sprintf("Highlights ready: %s", title)

	data := map[string]string{
		"match_id":         fmt.Sprintf("%d", match.ID),
		"event_type":       "highlight",
		"video_id":         videoID,
		"youtube_video_id": videoID,
		"utc_date":         match.UTCDate.Format(time.RFC3339),
		"home_team":        match.HomeTeamName,
		"away_team":        match.AwayTeamName,
		"home_id":          fmt.Sprintf("%d", homeClubID),
		"away_id":          fmt.Sprintf("%d", awayClubID),
	}

	var topics []string
	if homeClubID > 0 {
		topics = append(topics, fmt.Sprintf("club_%d", homeClubID))
	}
	if awayClubID > 0 {
		topics = append(topics, fmt.Sprintf("club_%d", awayClubID))
	}
	topics = append(topics, fmt.Sprintf("match_%d", match.ID))

	for _, topic := range topics {
		_ = c.SendTopic(ctx, topic, notifTitle, body, data)
	}
	return nil
}
