package domain

import "time"

// FootballShort models a vertical mobile video highlight (<65s).
type FootballShort struct {
	ID              string    `json:"id"`
	YouTubeVideoID  string    `json:"youtube_video_id"`
	Title           string    `json:"title"`
	ChannelID       string    `json:"channel_id"`
	ChannelName     string    `json:"channel_name"`
	ClubID          *int64    `json:"club_id,omitempty"`
	Category        string    `json:"category"` // GOALS_SKILLS, MOMENTS, BEHIND_THE_SCENES
	ThumbnailURL    string    `json:"thumbnail_url"`
	DurationSeconds int       `json:"duration_seconds"`
	LikeCount       int       `json:"like_count"`
	HypeScore       int       `json:"hype_score"`
	Source          string    `json:"source,omitempty"`
	IsFeatured      bool      `json:"is_featured"`
	PublishedAt     time.Time `json:"published_at"`
	CreatedAt       time.Time `json:"created_at"`
}
