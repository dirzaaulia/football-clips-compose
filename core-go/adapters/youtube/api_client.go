package youtube

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"net/url"
	"strings"
	"sync"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/domain"
	"github.com/dirzaaulia/footballclips/core-go/ports"
)

// Client implements ports.HighlightDiscovery with RSS-first zero-quota tiered fallback.
type Client struct {
	apiKey         string
	rssClient      *RSSClient
	taxonomy       *TaxonomyRegistry
	httpClient     *http.Client
	quotaMu        sync.Mutex
	quotaUsedToday int
	dailyLimit     int
	targetQuota    int
}

var _ ports.HighlightDiscovery = (*Client)(nil)

// NewClient instantiates the YouTube Highlight discovery client.
func NewClient(apiKey string, rss *RSSClient, taxonomy ...*TaxonomyRegistry) *Client {
	if rss == nil {
		rss = NewRSSClient()
	}
	var tax *TaxonomyRegistry
	if len(taxonomy) > 0 && taxonomy[0] != nil {
		tax = taxonomy[0]
	} else {
		tax = NewTaxonomyRegistry(nil)
	}
	return &Client{
		apiKey:      apiKey,
		rssClient:   rss,
		taxonomy:    tax,
		httpClient:  &http.Client{Timeout: 15 * time.Second},
		dailyLimit:  60000,
		targetQuota: 54000,
	}
}

// SetTaxonomy sets or updates the active taxonomy registry.
func (c *Client) SetTaxonomy(t *TaxonomyRegistry) {
	if t != nil {
		c.taxonomy = t
	}
}

// GetRemainingQuota returns available quota units for today.
func (c *Client) GetRemainingQuota() int {
	c.quotaMu.Lock()
	defer c.quotaMu.Unlock()
	return c.dailyLimit - c.quotaUsedToday
}

func (c *Client) recordQuota(units int) {
	c.quotaMu.Lock()
	defer c.quotaMu.Unlock()
	c.quotaUsedToday += units
}

// DiscoverCandidates searches YouTube for highlight candidates via RSS first, then API.
func (c *Client) DiscoverCandidates(
	ctx context.Context,
	match domain.Match,
	channelIDs []string,
) ([]domain.VideoCandidate, error) {
	var discovered []domain.VideoCandidate

	// 1. Zero-Quota RSS Discovery across official club & broadcaster channels
	if len(channelIDs) > 0 {
		var wg sync.WaitGroup
		var mu sync.Mutex

		for _, cid := range channelIDs {
			wg.Add(1)
			go func(channelID string) {
				defer wg.Done()
				vids, err := c.rssClient.FetchRecentVideos(ctx, channelID)
				if err != nil || len(vids) == 0 {
					return
				}
				matched := c.filterCandidates(vids, match)
				if len(matched) > 0 {
					mu.Lock()
					discovered = append(discovered, matched...)
					mu.Unlock()
				}
			}(cid)
		}
		wg.Wait()
	}

	if len(discovered) > 0 {
		return discovered, nil
	}

	// 2. Targeted API Search Fallback (Costs 100 quota units)
	if c.apiKey == "" || c.GetRemainingQuota() < 100 {
		return discovered, nil
	}

	// Quota Guard: Only query API for recent matches (within 72h)
	if time.Since(match.UTCDate).Hours() > 72.0 {
		return discovered, nil
	}

	query := fmt.Sprintf("%s vs %s highlights", match.HomeTeamName, match.AwayTeamName)
	if match.CompetitionName != "" {
		query = fmt.Sprintf("%s vs %s highlights %s", match.HomeTeamName, match.AwayTeamName, match.CompetitionName)
	}

	apiCandidates, err := c.searchAPI(ctx, query)
	if err == nil {
		matched := c.filterCandidates(apiCandidates, match)
		discovered = append(discovered, matched...)
	}
	return discovered, nil
}

func (c *Client) filterCandidates(videos []domain.VideoCandidate, match domain.Match) []domain.VideoCandidate {
	homeLow := strings.ToLower(match.HomeTeamName)
	awayLow := strings.ToLower(match.AwayTeamName)

	var filtered []domain.VideoCandidate
	for _, v := range videos {
		tLow := strings.ToLower(v.Title)
		hasHome := strings.Contains(tLow, homeLow)
		hasAway := strings.Contains(tLow, awayLow)

		if !hasHome && !hasAway {
			continue
		}

		if c.taxonomy != nil {
			verdict := c.taxonomy.Evaluate(v.Title)
			if verdict.IsNegative {
				continue
			}
		}

		filtered = append(filtered, v)
	}
	return filtered
}

func (c *Client) searchAPI(ctx context.Context, query string) ([]domain.VideoCandidate, error) {
	endpoint := fmt.Sprintf(
		"https://www.googleapis.com/youtube/v3/search?part=snippet&q=%s&type=video&maxResults=5&key=%s",
		url.QueryEscape(query),
		c.apiKey,
	)

	req, err := http.NewRequestWithContext(ctx, http.MethodGet, endpoint, nil)
	if err != nil {
		return nil, fmt.Errorf("create youtube search req: %w", err)
	}

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("execute youtube search: %w", err)
	}
	defer resp.Body.Close()

	c.recordQuota(100)

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("youtube search failed with status %d", resp.StatusCode)
	}

	var payload struct {
		Items []struct {
			ID struct {
				VideoID string `json:"videoId"`
			} `json:"id"`
			Snippet struct {
				Title        string `json:"title"`
				ChannelTitle string `json:"channelTitle"`
				ChannelID    string `json:"channelId"`
				PublishedAt  string `json:"publishedAt"`
			} `json:"snippet"`
		} `json:"items"`
	}

	if err := json.NewDecoder(resp.Body).Decode(&payload); err != nil {
		return nil, fmt.Errorf("decode youtube search: %w", err)
	}

	var candidates []domain.VideoCandidate
	for _, it := range payload.Items {
		vid := it.ID.VideoID
		if vid == "" {
			continue
		}
		channelTitle := it.Snippet.ChannelTitle
		channelID := it.Snippet.ChannelID
		pubTime, _ := time.Parse(time.RFC3339, it.Snippet.PublishedAt)

		candidates = append(candidates, domain.VideoCandidate{
			VideoID:         vid,
			Title:           strings.TrimSpace(it.Snippet.Title),
			ChannelTitle:    &channelTitle,
			ChannelHandle:   it.Snippet.ChannelTitle,
			ChannelID:       &channelID,
			PublishedAt:     &pubTime,
			IsEmbeddable:    true,
			WatchabilityPct: 100.0,
			Source:          "API",
		})
	}
	return candidates, nil
}
