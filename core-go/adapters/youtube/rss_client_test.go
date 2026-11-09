package youtube_test

import (
	"context"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/dirzaaulia/footballclips/core-go/adapters/youtube"
)

const sampleAtomXML = `<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns:yt="http://www.youtube.com/xml/schemas/2015" xmlns="http://www.w3.org/2005/Atom">
  <entry>
    <id>yt:video:test_video_123</id>
    <yt:videoId>test_video_123</yt:videoId>
    <yt:channelId>UC_arsenal_official</yt:channelId>
    <title>Arsenal vs Chelsea Highlights | Premier League</title>
    <published>2026-11-08T22:00:00+00:00</published>
    <author>
      <name>Arsenal</name>
    </author>
  </entry>
</feed>`

func TestRSSClient(t *testing.T) {
	ts := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/atom+xml")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(sampleAtomXML))
	}))
	defer ts.Close()

	client := youtube.NewRSSClient()
	if client.IsChannelDead("UC_arsenal_official") {
		t.Errorf("channel should not be dead initially")
	}

	ctx := context.Background()
	_ = ctx
	// Basic structure & blacklist checks
	if client.IsChannelDead("") {
		t.Errorf("empty channel should not be marked dead")
	}
}
