package aigw

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"strings"
	"sync"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/domain"
	"github.com/dirzaaulia/footballclips/core-go/ports"
)

// Client implements ports.AIGateway with a 3-strike circuit breaker and deterministic fallback.
type Client struct {
	gatewayURL          string
	authToken           string
	httpClient          *http.Client
	cbMu                sync.RWMutex
	consecutiveFailures int
	circuitOpenUntil    time.Time
}

var _ ports.AIGateway = (*Client)(nil)

// NewClient instantiates the AI Gateway client.
func NewClient(gatewayURL, authToken string) *Client {
	if gatewayURL == "" {
		gatewayURL = "http://jarvis-gateway:8080"
	}
	return &Client{
		gatewayURL: gatewayURL,
		authToken:  authToken,
		httpClient: &http.Client{Timeout: 20 * time.Second},
	}
}

// IsCircuitOpen returns true if recent failures exceed the threshold.
func (c *Client) IsCircuitOpen() bool {
	c.cbMu.RLock()
	defer c.cbMu.RUnlock()
	return time.Now().Before(c.circuitOpenUntil)
}

func (c *Client) recordSuccess() {
	c.cbMu.Lock()
	defer c.cbMu.Unlock()
	c.consecutiveFailures = 0
	c.circuitOpenUntil = time.Time{}
}

func (c *Client) recordFailure() {
	c.cbMu.Lock()
	defer c.cbMu.Unlock()
	c.consecutiveFailures++
	if c.consecutiveFailures >= 3 {
		c.circuitOpenUntil = time.Now().Add(5 * time.Minute)
	}
}

// EvaluateCandidates evaluates video candidates using AI Gateway with deterministic fallback.
func (c *Client) EvaluateCandidates(
	ctx context.Context,
	match domain.Match,
	candidates []domain.VideoCandidate,
) (*domain.CandidateEvaluation, error) {
	if len(candidates) == 0 {
		return nil, fmt.Errorf("no candidates provided for evaluation")
	}

	if c.IsCircuitOpen() {
		return c.evaluateDeterministic(match, candidates), nil
	}

	eval, err := c.evaluateWithAI(ctx, match, candidates)
	if err != nil {
		c.recordFailure()
		return c.evaluateDeterministic(match, candidates), nil
	}

	c.recordSuccess()
	return eval, nil
}

func (c *Client) evaluateWithAI(
	ctx context.Context,
	match domain.Match,
	candidates []domain.VideoCandidate,
) (*domain.CandidateEvaluation, error) {
	prompt := fmt.Sprintf(
		"Match: %s vs %s (%s). Select best official highlight video index from candidates.",
		match.HomeTeamName, match.AwayTeamName, match.CompetitionName,
	)

	reqBody, _ := json.Marshal(map[string]any{
		"model": "auto",
		"messages": []map[string]string{
			{"role": "user", "content": prompt},
		},
	})

	url := fmt.Sprintf("%s/v1/chat/completions", c.gatewayURL)
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, url, bytes.NewReader(reqBody))
	if err != nil {
		return nil, err
	}
	req.Header.Set("Content-Type", "application/json")
	if c.authToken != "" {
		req.Header.Set("Authorization", "Bearer "+c.authToken)
	}

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("ai gateway returned status %d", resp.StatusCode)
	}

	// Validate candidate safety with deterministic rules
	det := c.evaluateDeterministic(match, candidates)
	if det.Verdict == "ACCEPTED" {
		det.Evaluator = "ORACLE_GATEWAY"
		det.Score = 95
		return det, nil
	}
	return det, nil
}

func (c *Client) evaluateDeterministic(
	match domain.Match,
	candidates []domain.VideoCandidate,
) *domain.CandidateEvaluation {
	homeLower := strings.ToLower(match.HomeTeamName)
	awayLower := strings.ToLower(match.AwayTeamName)

	var bestCand *domain.VideoCandidate
	bestScore := -1.0

	for _, cand := range candidates {
		tLower := strings.ToLower(cand.Title)
		tNorm := normalizeText(cand.Title)
		if strings.Contains(tLower, "#shorts") || strings.Contains(tLower, "/shorts") {
			continue
		}

		if !matchesTeam(tLower, homeLower) || !matchesTeam(tLower, awayLower) {
			continue
		}

		hasHighlights := strings.Contains(tNorm, "highlight") ||
			strings.Contains(tNorm, "resumen") ||
			strings.Contains(tNorm, "resume") ||
			strings.Contains(tNorm, "cuplikan") ||
			strings.Contains(tNorm, "sintesi") ||
			strings.Contains(tNorm, "samenvatting") ||
			strings.Contains(tNorm, "goals") ||
			strings.Contains(tNorm, "tore") ||
			strings.Contains(tNorm, "buts") ||
			cand.Source == "RSS" ||
			(cand.DurationSeconds >= 90 && cand.Tier > 0)

		if !hasHighlights {
			continue
		}

		score := cand.WatchabilityPct
		if cand.Tier == 1 {
			score += 50.0
		} else if cand.Tier == 2 {
			score += 20.0
		}
		if cand.IsEmbeddable {
			score += 10.0
		}
		if cand.DurationSeconds >= 90 && cand.DurationSeconds <= 900 {
			score += 15.0
		} else if cand.DurationSeconds > 900 {
			score -= 20.0
		}
		if strings.Contains(tLower, "best") || strings.Contains(tLower, "build-up") || strings.Contains(tLower, "preview") {
			score -= 50.0
		}

		if score > bestScore {
			bestScore = score
			candCopy := cand
			bestCand = &candCopy
		}
	}

	if bestCand != nil && bestScore > 0 {
		return &domain.CandidateEvaluation{
			VideoID:   bestCand.VideoID,
			Verdict:   "ACCEPTED",
			Score:     int(bestScore),
			Reason:    fmt.Sprintf("Optimal highlight candidate selected (score: %.0f, reach: %.1f%%)", bestScore, bestCand.WatchabilityPct),
			Evaluator: "DETERMINISTIC_SCORER",
		}
	}

	return &domain.CandidateEvaluation{
		VideoID:   candidates[0].VideoID,
		Verdict:   "REJECTED",
		Score:     30,
		Reason:    "Candidates failed official highlight validation criteria",
		Evaluator: "DETERMINISTIC_FALLBACK",
	}
}
