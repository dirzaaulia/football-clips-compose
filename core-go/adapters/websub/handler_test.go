package websub

import (
	"bytes"
	"io"
	"net/http"
	"net/http/httptest"
	"sync"
	"testing"
	"time"
)

func TestHandler_VerificationHandshake(t *testing.T) {
	handler := NewHandler(nil, nil)

	req := httptest.NewRequest(
		http.MethodGet,
		"/websub?hub.mode=subscribe&hub.topic=https://www.youtube.com/xml/feeds/videos.xml?channel_id=UC123&hub.challenge=test-challenge-12345&hub.lease_seconds=86400",
		nil,
	)
	w := httptest.NewRecorder()

	handler.ServeHTTP(w, req)
	resp := w.Result()
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		t.Fatalf("expected status 200, got %d", resp.StatusCode)
	}

	body, _ := io.ReadAll(resp.Body)
	if string(body) != "test-challenge-12345" {
		t.Fatalf("expected challenge 'test-challenge-12345', got '%s'", string(body))
	}
}

func TestHandler_PushNotification(t *testing.T) {
	var wg sync.WaitGroup
	var received Notification

	wg.Add(1)
	handler := NewHandler(nil, func(n Notification) {
		received = n
		wg.Done()
	})

	xmlData := `<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns:yt="http://www.youtube.com/xml/schemas/2015" xmlns="http://www.w3.org/2005/Atom">
  <link rel="hub" href="https://pubsubhubbub.appspot.com"/>
  <title>YouTube video feed</title>
  <entry>
    <id>yt:video:abc123xyz</id>
    <yt:videoId>abc123xyz</yt:videoId>
    <yt:channelId>UC_arsenal_official</yt:channelId>
    <title>Arsenal vs Chelsea 3-1 Extended Highlights</title>
    <link rel="alternate" href="https://www.youtube.com/watch?v=abc123xyz"/>
    <published>2026-11-08T20:30:00+00:00</published>
  </entry>
</feed>`

	req := httptest.NewRequest(http.MethodPost, "/websub", bytes.NewBufferString(xmlData))
	req.Header.Set("Content-Type", "application/atom+xml")
	w := httptest.NewRecorder()

	handler.ServeHTTP(w, req)
	resp := w.Result()
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		t.Fatalf("expected status 200, got %d", resp.StatusCode)
	}

	done := make(chan struct{})
	go func() {
		wg.Wait()
		close(done)
	}()

	select {
	case <-done:
	case <-time.After(2 * time.Second):
		t.Fatal("timed out waiting for onNotify callback")
	}

	if received.VideoID != "abc123xyz" {
		t.Fatalf("expected videoID 'abc123xyz', got '%s'", received.VideoID)
	}
	if received.ChannelID != "UC_arsenal_official" {
		t.Fatalf("expected channelID 'UC_arsenal_official', got '%s'", received.ChannelID)
	}
	if received.Title != "Arsenal vs Chelsea 3-1 Extended Highlights" {
		t.Fatalf("expected title 'Arsenal vs Chelsea 3-1 Extended Highlights', got '%s'", received.Title)
	}
}
