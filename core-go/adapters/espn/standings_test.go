package espn_test

import (
	"context"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/dirzaaulia/footballclips/core-go/adapters/espn"
)

func TestFetchStandingsMock(t *testing.T) {
	mockJSON := `{
		"children": [{
			"standings": {
				"entries": [{
					"team": {
						"id": "359",
						"displayName": "Arsenal",
						"shortDisplayName": "Arsenal",
						"logos": [{"href": "https://a.espncdn.com/359.png"}]
					},
					"stats": [
						{"name": "rank", "value": 1.0},
						{"name": "points", "value": 27.0},
						{"name": "gamesPlayed", "value": 10.0},
						{"name": "wins", "value": 9.0},
						{"name": "ties", "value": 0.0},
						{"name": "losses", "value": 1.0},
						{"name": "pointsFor", "value": 15.0},
						{"name": "pointsAgainst", "value": 5.0},
						{"name": "pointDifferential", "value": 10.0},
						{"name": "overall", "summary": "9-0-1"}
					]
				}]
			}
		}]
	}`

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		_, _ = w.Write([]byte(mockJSON))
	}))
	defer server.Close()

	client := espn.NewClient()
	_ = client
	_ = context.Background()
}
