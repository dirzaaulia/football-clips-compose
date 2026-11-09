package espn

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"sync"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/domain"
	"github.com/dirzaaulia/footballclips/core-go/ports"
)

// CompetitionInfo models competition slug and metadata.
type CompetitionInfo struct {
	Slug string
	Name string
	Type string
}

// Client implements ports.ScoreProvider against the ESPN Soccer API.
type Client struct {
	baseURL       string
	httpClient    *http.Client
	competitions  map[string]CompetitionInfo
	backoffMu     sync.RWMutex
	leagueBackoff map[string]time.Time
}

var defaultCompetitions = map[string]CompetitionInfo{
	"PL":  {Slug: "eng.1", Name: "Premier League", Type: "league"},
	"PD":  {Slug: "esp.1", Name: "LaLiga", Type: "league"},
	"SA":  {Slug: "ita.1", Name: "Serie A", Type: "league"},
	"BL1": {Slug: "ger.1", Name: "Bundesliga", Type: "league"},
	"FL1": {Slug: "fra.1", Name: "Ligue 1", Type: "league"},
	"CL":  {Slug: "uefa.champions", Name: "UEFA Champions League", Type: "cup"},
	"EL":  {Slug: "uefa.europa", Name: "UEFA Europa League", Type: "cup"},
	"ECL": {Slug: "uefa.europa.conf", Name: "UEFA Conference League", Type: "cup"},
	"FAC": {Slug: "eng.fa", Name: "FA Cup", Type: "cup"},
	"EFL": {Slug: "eng.league_cup", Name: "Carabao Cup", Type: "cup"},
	"CDR": {Slug: "esp.copa_del_rey", Name: "Copa del Rey", Type: "cup"},
	"DFB": {Slug: "ger.dfb_pokal", Name: "DFB-Pokal", Type: "cup"},
	"CIT": {Slug: "ita.coppa_italia", Name: "Coppa Italia", Type: "cup"},
	"SPL": {Slug: "ksa.1", Name: "Saudi Pro League", Type: "league"},
	"MLS": {Slug: "usa.1", Name: "Major League Soccer", Type: "league"},
	"DED": {Slug: "ned.1", Name: "Eredivisie", Type: "league"},
	"PPL": {Slug: "por.1", Name: "Liga Portugal", Type: "league"},
	"IDN": {Slug: "idn.1", Name: "Liga 1 Indonesia", Type: "league"},
	"ACL": {Slug: "afc.champions", Name: "AFC Champions League Elite", Type: "cup"},
	"LIB": {Slug: "conmebol.libertadores", Name: "CONMEBOL Libertadores", Type: "cup"},
	"SUD": {Slug: "conmebol.sudamericana", Name: "CONMEBOL Sudamericana", Type: "cup"},
	"REC": {Slug: "conmebol.recopa", Name: "CONMEBOL Recopa", Type: "cup"},
	"BRA": {Slug: "bra.1", Name: "Brasileirão Série A", Type: "league"},
	"ARG": {Slug: "arg.1", Name: "Argentine Liga Profesional", Type: "league"},
}

// NewClient returns a new ESPN API client with default timeout and connection limits.
func NewClient() *Client {
	return &Client{
		baseURL: "https://site.api.espn.com/apis/site/v2/sports/soccer",
		httpClient: &http.Client{
			Timeout: 15 * time.Second,
		},
		competitions:  defaultCompetitions,
		leagueBackoff: make(map[string]time.Time),
	}
}

var _ ports.ScoreProvider = (*Client)(nil)

func (c *Client) isBackedOff(slug string) bool {
	c.backoffMu.RLock()
	defer c.backoffMu.RUnlock()
	expiry, exists := c.leagueBackoff[slug]
	return exists && time.Now().Before(expiry)
}

func (c *Client) setBackoff(slug string, d time.Duration) {
	c.backoffMu.Lock()
	defer c.backoffMu.Unlock()
	c.leagueBackoff[slug] = time.Now().Add(d)
}

// FetchScoreboard fetches matches across targeted competitions within the date range.
func (c *Client) FetchScoreboard(ctx context.Context, compCodes []string, dateFrom, dateTo string) ([]domain.Match, error) {
	var allMatches []domain.Match
	var mu sync.Mutex
	sem := make(chan struct{}, 4)
	var wg sync.WaitGroup

	for _, code := range compCodes {
		info, ok := c.competitions[code]
		if !ok || c.isBackedOff(info.Slug) {
			continue
		}

		wg.Add(1)
		go func(leagueCode string, leagueInfo CompetitionInfo) {
			defer wg.Done()
			sem <- struct{}{}
			defer func() { <-sem }()

			matches, err := c.fetchLeagueMatches(ctx, leagueCode, leagueInfo)
			if err == nil && len(matches) > 0 {
				mu.Lock()
				allMatches = append(allMatches, matches...)
				mu.Unlock()
			}
		}(code, info)
	}

	wg.Wait()
	return allMatches, nil
}

func (c *Client) fetchLeagueMatches(ctx context.Context, code string, info CompetitionInfo) ([]domain.Match, error) {
	url := fmt.Sprintf("%s/%s/scoreboard", c.baseURL, info.Slug)
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, url, nil)
	if err != nil {
		return nil, fmt.Errorf("create req %s: %w", info.Slug, err)
	}
	req.Header.Set("User-Agent", "FootballClips-Go/1.0")

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("do req %s: %w", info.Slug, err)
	}
	defer resp.Body.Close()

	if resp.StatusCode == http.StatusTooManyRequests || resp.StatusCode == http.StatusServiceUnavailable {
		c.setBackoff(info.Slug, 3*time.Minute)
		return nil, fmt.Errorf("rate limited on %s (code: %d)", info.Slug, resp.StatusCode)
	}
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("unexpected status %d for %s", resp.StatusCode, info.Slug)
	}

	var payload struct {
		Events []RawEvent `json:"events"`
	}
	if err := json.NewDecoder(resp.Body).Decode(&payload); err != nil {
		return nil, fmt.Errorf("decode scoreboard for %s: %w", info.Slug, err)
	}

	var results []domain.Match
	for _, ev := range payload.Events {
		m, err := ParseEvent(ev, code, info.Name)
		if err == nil && m != nil {
			results = append(results, *m)
		}
	}
	return results, nil
}

// FetchMatchSummary fetches live stats and player details for an event.
func (c *Client) FetchMatchSummary(ctx context.Context, eventID int64, competitionCode string) (map[string]any, error) {
	info, ok := c.competitions[competitionCode]
	slug := "eng.1"
	if ok {
		slug = info.Slug
	}
	url := fmt.Sprintf("%s/%s/summary?event=%d", c.baseURL, slug, eventID)
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, url, nil)
	if err != nil {
		return nil, fmt.Errorf("create req summary: %w", err)
	}
	req.Header.Set("User-Agent", "FootballClips-Go/1.0")

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("fetch summary %d: %w", eventID, err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("summary status %d for event %d", resp.StatusCode, eventID)
	}

	var summary map[string]any
	if err := json.NewDecoder(resp.Body).Decode(&summary); err != nil {
		return nil, fmt.Errorf("decode summary: %w", err)
	}
	return summary, nil
}
