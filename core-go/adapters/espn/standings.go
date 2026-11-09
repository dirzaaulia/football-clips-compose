package espn

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"strconv"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/domain"
)

type standingsPayload struct {
	Children []struct {
		Standings struct {
			Entries []rawStandingEntry `json:"entries"`
		} `json:"standings"`
	} `json:"children"`
	Standings []struct {
		Entries []rawStandingEntry `json:"entries"`
	} `json:"standings"`
}

type rawStandingEntry struct {
	Team struct {
		ID               string `json:"id"`
		DisplayName      string `json:"displayName"`
		ShortDisplayName string `json:"shortDisplayName"`
		Logos            []struct {
			HREF string `json:"href"`
		} `json:"logos"`
	} `json:"team"`
	Stats []struct {
		Name        string  `json:"name"`
		Value       float64 `json:"value"`
		DisplayName string  `json:"displayName"`
		Summary     string  `json:"summary"`
	} `json:"stats"`
}

// FetchStandings downloads and parses the league table for a given competition code.
func (c *Client) FetchStandings(ctx context.Context, compCode string, season int) ([]domain.StandingEntry, error) {
	comp, ok := defaultCompetitions[compCode]
	if !ok {
		return nil, fmt.Errorf("unknown competition code: %s", compCode)
	}

	url := fmt.Sprintf("https://site.api.espn.com/apis/v2/sports/soccer/%s/standings", comp.Slug)
	if season > 0 && season != 2026 {
		url = fmt.Sprintf("%s?season=%d", url, season)
	}

	req, err := http.NewRequestWithContext(ctx, http.MethodGet, url, nil)
	if err != nil {
		return nil, fmt.Errorf("create standings req %s: %w", compCode, err)
	}
	req.Header.Set("User-Agent", "FootballClips-Go/1.0 (Zero-Quota ESPN Engine)")

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("fetch standings %s: %w", compCode, err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("standings http status %d for %s", resp.StatusCode, compCode)
	}

	var payload standingsPayload
	if err := json.NewDecoder(resp.Body).Decode(&payload); err != nil {
		return nil, fmt.Errorf("decode standings %s: %w", compCode, err)
	}

	var rawEntries []rawStandingEntry
	if len(payload.Children) > 0 {
		rawEntries = payload.Children[0].Standings.Entries
	} else if len(payload.Standings) > 0 {
		rawEntries = payload.Standings[0].Entries
	}

	if season == 0 {
		season = 2026
	}
	now := time.Now().UTC()

	var entries []domain.StandingEntry
	for _, it := range rawEntries {
		teamID, _ := strconv.ParseInt(it.Team.ID, 10, 64)
		if teamID == 0 {
			continue
		}

		stats := make(map[string]float64)
		var formStr *string
		for _, s := range it.Stats {
			stats[s.Name] = s.Value
			if s.Name == "overall" && s.Summary != "" {
				summary := s.Summary
				formStr = &summary
			}
		}

		var crest *string
		if len(it.Team.Logos) > 0 && it.Team.Logos[0].HREF != "" {
			c := it.Team.Logos[0].HREF
			crest = &c
		}

		var shortName *string
		if it.Team.ShortDisplayName != "" {
			sn := it.Team.ShortDisplayName
			shortName = &sn
		}

		rowID := fmt.Sprintf("%s_%d", compCode, teamID)
		if season != 2026 {
			rowID = fmt.Sprintf("%s_%d_%d", compCode, season, teamID)
		}

		entries = append(entries, domain.StandingEntry{
			ID:              rowID,
			CompetitionCode: compCode,
			Position:        int(stats["rank"]),
			TeamID:          teamID,
			TeamName:        it.Team.DisplayName,
			TeamShortName:   shortName,
			CrestURL:        crest,
			PlayedGames:     int(stats["gamesPlayed"]),
			Won:             int(stats["wins"]),
			Draw:            int(stats["ties"]),
			Lost:            int(stats["losses"]),
			Points:          int(stats["points"]),
			GoalsFor:        int(stats["pointsFor"]),
			GoalsAgainst:    int(stats["pointsAgainst"]),
			GoalDifference:  int(stats["pointDifferential"]),
			Season:          season,
			Form:            formStr,
			UpdatedAt:       now,
		})
	}

	return entries, nil
}
