package youtube

import "testing"

func TestParseISODuration(t *testing.T) {
	tests := []struct {
		iso  string
		want int
	}{
		{"PT10M15S", 615},
		{"PT2M30S", 150},
		{"PT1H", 3600},
		{"PT1H2M3S", 3723},
		{"PT45S", 45},
		{"", 0},
		{"INVALID", 0},
	}

	for _, tt := range tests {
		got := parseISODuration(tt.iso)
		if got != tt.want {
			t.Errorf("parseISODuration(%q) = %d, want %d", tt.iso, got, tt.want)
		}
	}
}
