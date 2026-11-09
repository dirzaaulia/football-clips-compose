package websub

import (
	"encoding/xml"
	"io"
	"log/slog"
	"net/http"
	"strings"
	"time"
)

// Notification represents a parsed YouTube push notification.
type Notification struct {
	VideoID   string
	ChannelID string
	Title     string
	Link      string
	Published time.Time
}

// OnNotificationFunc is invoked when a valid push notification is received.
type OnNotificationFunc func(n Notification)

// Handler processes YouTube WebSub (PubSubHubbub) handshakes and push events.
type Handler struct {
	logger   *slog.Logger
	onNotify OnNotificationFunc
}

// NewHandler constructs a new WebSub HTTP handler.
func NewHandler(logger *slog.Logger, onNotify OnNotificationFunc) *Handler {
	if logger == nil {
		logger = slog.Default()
	}
	return &Handler{
		logger:   logger,
		onNotify: onNotify,
	}
}

func (h *Handler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	switch r.Method {
	case http.MethodGet:
		h.handleVerification(w, r)
	case http.MethodPost:
		h.handlePush(w, r)
	default:
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
	}
}

func (h *Handler) handleVerification(w http.ResponseWriter, r *http.Request) {
	query := r.URL.Query()
	mode := query.Get("hub.mode")
	challenge := query.Get("hub.challenge")
	topic := query.Get("hub.topic")

	if mode == "" || challenge == "" {
		h.logger.Warn("[WEBSUB] Missing mode or challenge", slog.String("topic", topic))
		http.Error(w, "Bad Request", http.StatusBadRequest)
		return
	}

	h.logger.Info("[WEBSUB] Verification handshake confirmed",
		slog.String("mode", mode),
		slog.String("topic", topic),
	)

	w.Header().Set("Content-Type", "text/plain; charset=utf-8")
	w.WriteHeader(http.StatusOK)
	_, _ = w.Write([]byte(challenge))
}

type feedPayload struct {
	XMLName xml.Name     `xml:"feed"`
	Entries []entryItem  `xml:"entry"`
}

type entryItem struct {
	VideoID   string    `xml:"videoId"`
	ChannelID string    `xml:"channelId"`
	Title     string    `xml:"title"`
	Link      entryLink `xml:"link"`
	Published string    `xml:"published"`
}

type entryLink struct {
	Href string `xml:"href,attr"`
}

func (h *Handler) handlePush(w http.ResponseWriter, r *http.Request) {
	body, err := io.ReadAll(io.LimitReader(r.Body, 1024*1024))
	if err != nil {
		h.logger.Error("[WEBSUB] Failed reading body", slog.String("err", err.Error()))
		http.Error(w, "Read error", http.StatusBadRequest)
		return
	}
	defer r.Body.Close()

	var feed feedPayload
	if err := xml.Unmarshal(body, &feed); err != nil {
		h.logger.Error("[WEBSUB] XML unmarshal error", slog.String("err", err.Error()))
		http.Error(w, "Invalid XML", http.StatusBadRequest)
		return
	}

	w.WriteHeader(http.StatusOK)

	for _, entry := range feed.Entries {
		videoID := strings.TrimSpace(entry.VideoID)
		if videoID == "" {
			continue
		}

		pubTime, _ := time.Parse(time.RFC3339, entry.Published)
		notif := Notification{
			VideoID:   videoID,
			ChannelID: strings.TrimSpace(entry.ChannelID),
			Title:     strings.TrimSpace(entry.Title),
			Link:      strings.TrimSpace(entry.Link.Href),
			Published: pubTime,
		}

		h.logger.Info("[WEBSUB] Received push notification",
			slog.String("video_id", notif.VideoID),
			slog.String("title", notif.Title),
			slog.String("channel_id", notif.ChannelID),
		)

		if h.onNotify != nil {
			go h.onNotify(notif)
		}
	}
}
