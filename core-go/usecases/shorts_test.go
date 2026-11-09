package usecases_test

import (
	"context"
	"testing"
	"time"

	"github.com/dirzaaulia/footballclips/core-go/adapters/youtube"
	"github.com/dirzaaulia/footballclips/core-go/domain"
	"github.com/dirzaaulia/footballclips/core-go/usecases"
)

type mockShortsRepo struct {
	existing map[string]bool
	upserted []domain.FootballShort
}

func (m *mockShortsRepo) GetExistingShortVideoIDs(ctx context.Context, limit int) (map[string]bool, error) {
	if m.existing == nil {
		m.existing = make(map[string]bool)
	}
	return m.existing, nil
}

func (m *mockShortsRepo) UpsertShorts(ctx context.Context, shorts []domain.FootballShort) error {
	m.upserted = append(m.upserted, shorts...)
	return nil
}

func (m *mockShortsRepo) GetActiveClubChannels(ctx context.Context, limit int) ([]struct {
	ClubID    int64
	Name      string
	ChannelID string
}, error) {
	return []struct {
		ClubID    int64
		Name      string
		ChannelID string
	}{
		{ClubID: 1, Name: "Arsenal", ChannelID: "UC_arsenal"},
	}, nil
}
func (m *mockShortsRepo) MarkChannelsChecked(ctx context.Context, clubIDs []int64) error {
	return nil
}
func TestIngestShortsUseCase(t *testing.T) {
	repo := &mockShortsRepo{}
	rss := youtube.NewRSSClient()
	uc := usecases.NewIngestShortsUseCase(rss, repo, nil)

	_ = uc
	_ = time.Now()
}
