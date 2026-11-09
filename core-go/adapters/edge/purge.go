package edge

import (
	"context"
	"net/http"
	"strings"
	"time"
)

// PurgeClient dispatches non-blocking cache invalidations to Cloudflare Edge.
type PurgeClient struct {
	endpoint   string
	httpClient *http.Client
}

// NewPurgeClient instantiates the Cloudflare edge cache invalidation client.
func NewPurgeClient(endpoint string) *PurgeClient {
	if endpoint == "" {
		endpoint = "https://edge.fc.dirzaaulia.com/api/purge-cache"
	}
	return &PurgeClient{
		endpoint: endpoint,
		httpClient: &http.Client{
			Timeout: 3 * time.Second,
		},
	}
}

// PurgeMatchesCache notifies Cloudflare edge worker to purge cached responses asynchronously.
func (c *PurgeClient) PurgeMatchesCache(ctx context.Context) {
	go func() {
		req, err := http.NewRequestWithContext(context.Background(), http.MethodPost, c.endpoint, strings.NewReader("{}"))
		if err != nil {
			return
		}
		req.Header.Set("Content-Type", "application/json")
		resp, err := c.httpClient.Do(req)
		if err == nil {
			_ = resp.Body.Close()
		}
	}()
}
