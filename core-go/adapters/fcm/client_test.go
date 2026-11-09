package fcm

import (
	"context"
	"crypto/rand"
	"crypto/rsa"
	"crypto/x509"
	"encoding/json"
	"encoding/pem"
	"net/http"
	"net/http/httptest"
	"os"
	"path/filepath"
	"sync/atomic"
	"testing"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/domain"
)

func TestClient_TopicDispatch(t *testing.T) {
	var fcmCalls int32
	var tokenCalls int32

	fcmServer := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.URL.Path == "/token" {
			atomic.AddInt32(&tokenCalls, 1)
			w.Header().Set("Content-Type", "application/json")
			_ = json.NewEncoder(w).Encode(map[string]any{
				"access_token": "mock-test-access-token",
				"expires_in":   3600,
			})
			return
		}

		atomic.AddInt32(&fcmCalls, 1)
		w.Header().Set("Content-Type", "application/json")
		_ = json.NewEncoder(w).Encode(map[string]any{
			"name": "projects/mock-proj/messages/123",
		})
	}))
	defer fcmServer.Close()

	privKey, err := rsa.GenerateKey(rand.Reader, 2048)
	if err != nil {
		t.Fatalf("generate rsa key: %v", err)
	}
	privDER, err := x509.MarshalPKCS8PrivateKey(privKey)
	if err != nil {
		t.Fatalf("marshal priv key: %v", err)
	}
	pemBytes := pem.EncodeToMemory(&pem.Block{Type: "PRIVATE KEY", Bytes: privDER})

	saMap := map[string]string{
		"type":         "service_account",
		"project_id":   "mock-proj",
		"private_key":  string(pemBytes),
		"client_email": "test@mock-proj.iam.gserviceaccount.com",
		"token_uri":    fcmServer.URL + "/token",
	}
	saData, _ := json.Marshal(saMap)

	tmpDir := t.TempDir()
	credPath := filepath.Join(tmpDir, "sa.json")
	if err := os.WriteFile(credPath, saData, 0600); err != nil {
		t.Fatalf("write sa file: %v", err)
	}

	tp, err := NewTokenProvider(credPath)
	if err != nil {
		t.Fatalf("new token provider: %v", err)
	}

	client := NewClient(tp, nil, nil)

	ctx := context.Background()
	scoreH := 2
	scoreA := 1
	match := domain.Match{
		ID:           401915371,
		HomeTeamName: "Arsenal",
		AwayTeamName: "Chelsea",
		HomeScore:    &scoreH,
		AwayScore:    &scoreA,
		Status:       "IN_PLAY",
		UTCDate:      time.Now().UTC(),
	}

	err = client.NotifyMatchEvent(ctx, match, "score", 42, 45)
	if err != nil {
		t.Fatalf("notify match event error: %v", err)
	}

	// Deduplication check: second call should be ignored
	err = client.NotifyMatchEvent(ctx, match, "score", 42, 45)
	if err != nil {
		t.Fatalf("notify second call error: %v", err)
	}

	if atomic.LoadInt32(&tokenCalls) == 0 {
		t.Fatalf("expected token endpoint to be called at least once")
	}
}
