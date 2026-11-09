package youtube

import (
	"context"
	"encoding/xml"
	"fmt"
	"html"
	"net/http"
	"sync"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/domain"
)

// AtomFeed maps standard YouTube XML RSS response.
type AtomFeed struct {
	XMLName xml.Name    `xml:"feed"`
	Entries []AtomEntry `xml:"entry"`
}

// AtomEntry models single video item in RSS feed.
type AtomEntry struct {
	VideoID   string `xml:"videoId"`
	Title     string `xml:"title"`
	Published string `xml:"published"`
	Author    struct {
		Name string `xml:"name"`
	} `xml:"author"`
	ChannelID string `xml:"channelId"`
}

type cachedFeed struct {
	fetchedAt time.Time
	videos    []domain.VideoCandidate
}

// RSSClient fetches public YouTube video uploads with 0 quota consumption.
type RSSClient struct {
	baseURL      string
	httpClient   *http.Client
	cacheTTL     time.Duration
	deadTTL      time.Duration
	cacheMu      sync.RWMutex
	cache        map[string]cachedFeed
	deadChannels map[string]time.Time
}

// NewRSSClient instantiates the zero-quota RSS discovery client.
func NewRSSClient() *RSSClient {
	return &RSSClient{
		baseURL: "https://www.youtube.com/feeds/videos.xml",
		httpClient: &http.Client{
			Timeout: 10 * time.Second,
		},
		cacheTTL:     180 * time.Second,
		deadTTL:      7 * 24 * time.Hour,
		cache:        make(map[string]cachedFeed),
		deadChannels: make(map[string]time.Time),
	}
}

// IsChannelDead returns true if the channel previously returned 404 or 410.
func (c *RSSClient) IsChannelDead(channelID string) bool {
	c.cacheMu.RLock()
	defer c.cacheMu.RUnlock()
	expiry, exists := c.deadChannels[channelID]
	return exists && time.Now().Before(expiry)
}

func (c *RSSClient) markChannelDead(channelID string) {
	c.cacheMu.Lock()
	defer c.cacheMu.Unlock()
	c.deadChannels[channelID] = time.Now().Add(c.deadTTL)
}

// FetchRecentVideos fetches latest 15 uploads from a channel via RSS.
func (c *RSSClient) FetchRecentVideos(ctx context.Context, channelID string) ([]domain.VideoCandidate, error) {
	if channelID == "" || c.IsChannelDead(channelID) {
		return nil, nil
	}

	c.cacheMu.RLock()
	cached, ok := c.cache[channelID]
	if ok && time.Since(cached.fetchedAt) < c.cacheTTL {
		c.cacheMu.RUnlock()
		return cached.videos, nil
	}
	c.cacheMu.RUnlock()

	url := fmt.Sprintf("%s?channel_id=%s", c.baseURL, channelID)
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, url, nil)
	if err != nil {
		return nil, fmt.Errorf("create rss req %s: %w", channelID, err)
	}
	req.Header.Set("User-Agent", "FootballClips-Go/1.0 (RSS Atom; Zero-Quota)")

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("fetch rss %s: %w", channelID, err)
	}
	defer resp.Body.Close()

	if resp.StatusCode == http.StatusNotFound || resp.StatusCode == http.StatusGone {
		c.markChannelDead(channelID)
		return nil, fmt.Errorf("channel %s dead (HTTP %d)", channelID, resp.StatusCode)
	}
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("rss unexpected status %d for channel %s", resp.StatusCode, channelID)
	}

	var feed AtomFeed
	if err := xml.NewDecoder(resp.Body).Decode(&feed); err != nil {
		return nil, fmt.Errorf("decode rss xml %s: %w", channelID, err)
	}

	var candidates []domain.VideoCandidate
	for _, entry := range feed.Entries {
		if entry.VideoID == "" {
			continue
		}
		pubTime, _ := time.Parse(time.RFC3339, entry.Published)
		channelTitle := entry.Author.Name
		cID := entry.ChannelID
		if cID == "" {
			cID = channelID
		}

		candidates = append(candidates, domain.VideoCandidate{
			VideoID:         entry.VideoID,
			Title:           html.UnescapeString(entry.Title),
			ChannelTitle:    &channelTitle,
			ChannelHandle:   entry.Author.Name,
			ChannelID:       &cID,
			PublishedAt:     &pubTime,
			IsEmbeddable:    true,
			WatchabilityPct: 100.0,
			Source:          "RSS",
		})
	}

	c.cacheMu.Lock()
	c.cache[channelID] = cachedFeed{fetchedAt: time.Now(), videos: candidates}
	c.cacheMu.Unlock()

	return candidates, nil
}
