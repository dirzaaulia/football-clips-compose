package youtube

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"regexp"
	"strconv"
	"strings"

	"github.com/dirzaaulia/footballclips/core-go/domain"
)

var isoDurationRegex = regexp.MustCompile(`PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?`)

// parseISODuration parses an ISO 8601 duration (e.g. PT10M15S) into seconds.
func parseISODuration(iso string) int {
	matches := isoDurationRegex.FindStringSubmatch(iso)
	if len(matches) == 0 {
		return 0
	}
	var hours, minutes, seconds int
	if matches[1] != "" {
		hours, _ = strconv.Atoi(matches[1])
	}
	if matches[2] != "" {
		minutes, _ = strconv.Atoi(matches[2])
	}
	if matches[3] != "" {
		seconds, _ = strconv.Atoi(matches[3])
	}
	return hours*3600 + minutes*60 + seconds
}

type youtubeVideosResponse struct {
	Items []struct {
		ID             string `json:"id"`
		ContentDetails struct {
			Duration          string `json:"duration"`
			RegionRestriction struct {
				Allowed []string `json:"allowed"`
				Blocked []string `json:"blocked"`
			} `json:"regionRestriction"`
		} `json:"contentDetails"`
		Status struct {
			Embeddable bool `json:"embeddable"`
		} `json:"status"`
	} `json:"items"`
}

// EnrichCandidateDetails fetches real contentDetails and region restrictions from YouTube API.
func (c *Client) EnrichCandidateDetails(
	ctx context.Context,
	candidates []domain.VideoCandidate,
) ([]domain.VideoCandidate, error) {
	if len(candidates) == 0 || c.apiKey == "" {
		return candidates, nil
	}

	var ids []string
	idMap := make(map[string]int)
	for i, cand := range candidates {
		if cand.VideoID != "" {
			ids = append(ids, cand.VideoID)
			idMap[cand.VideoID] = i
		}
		if len(ids) >= 50 {
			break
		}
	}

	url := fmt.Sprintf(
		"https://www.googleapis.com/youtube/v3/videos?part=contentDetails,status&id=%s&key=%s",
		strings.Join(ids, ","),
		c.apiKey,
	)

	req, err := http.NewRequestWithContext(ctx, http.MethodGet, url, nil)
	if err != nil {
		return candidates, fmt.Errorf("create videos.list req: %w", err)
	}

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return candidates, fmt.Errorf("execute videos.list: %w", err)
	}
	defer resp.Body.Close()

	c.recordQuota(1)

	if resp.StatusCode != http.StatusOK {
		return candidates, fmt.Errorf("videos.list failed with status %d", resp.StatusCode)
	}

	var payload youtubeVideosResponse
	if err := json.NewDecoder(resp.Body).Decode(&payload); err != nil {
		return candidates, fmt.Errorf("decode videos.list: %w", err)
	}

	for _, it := range payload.Items {
		idx, exists := idMap[it.ID]
		if !exists {
			continue
		}
		cand := &candidates[idx]
		cand.DurationSeconds = parseISODuration(it.ContentDetails.Duration)
		cand.IsEmbeddable = it.Status.Embeddable

		reg := &domain.RegionInfo{
			Allowed: it.ContentDetails.RegionRestriction.Allowed,
			Blocked: it.ContentDetails.RegionRestriction.Blocked,
		}
		cand.WatchabilityPct = domain.CalculateWorldwideWatchability(reg)
	}

	return candidates, nil
}

// normalizeText converts accents and diacritics to basic ascii lowercase.
func normalizeText(s string) string {
	s = strings.ToLower(s)
	replacements := map[string]string{
		"á": "a", "à": "a", "ã": "a", "â": "a", "ä": "a",
		"é": "e", "è": "e", "ê": "e", "ë": "e",
		"í": "i", "ì": "i", "î": "i", "ï": "i",
		"ó": "o", "ò": "o", "õ": "o", "ô": "o", "ö": "o",
		"ú": "u", "ù": "u", "û": "u", "ü": "u",
		"ç": "c", "ñ": "n",
	}
	for k, v := range replacements {
		s = strings.ReplaceAll(s, k, v)
	}
	return s
}

// matchTeamTokens checks whether key identifying words in a team name match the title.
func matchTeamTokens(title, team string) bool {
	words := strings.Fields(team)
	for _, w := range words {
		if len(w) >= 4 && strings.Contains(title, w) {
			return true
		}
	}
	return false
}
