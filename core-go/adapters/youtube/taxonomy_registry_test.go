package youtube

import (
	"testing"
)

func TestTaxonomyRegistry_Evaluate(t *testing.T) {
	registry := NewTaxonomyRegistry(nil)

	tests := []struct {
		name         string
		title        string
		wantNegative bool
	}{
		{
			name:         "Italian Sarri Interview",
			title:        "Maurizio Sarri commenta il match | Atalanta-Kairat Almaty 1-1 | UECL MD3 League Phase",
			wantNegative: true,
		},
		{
			name:         "Aftermovie Vlog",
			title:        "Unbeaten. Unbothered. 3/3. - Aftermovie v CSKA Sofia 04.11.2026",
			wantNegative: true,
		},
		{
			name:         "Press Conference",
			title:        "Erik ten Hag Press Conference | Man Utd vs Chelsea",
			wantNegative: true,
		},
		{
			name:         "Official Highlight",
			title:        "Manchester United vs Aston Villa 4-4 | Highlights & Goals",
			wantNegative: false,
		},
		{
			name:         "Spanish Resumen",
			title:        "Real Madrid vs Barcelona 2-1 | Resumen y Goles",
			wantNegative: false,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			verdict := registry.Evaluate(tt.title)
			if verdict.IsNegative != tt.wantNegative {
				t.Errorf("Evaluate(%q) = %v, want %v (reason: %s)", tt.title, verdict.IsNegative, tt.wantNegative, verdict.Reason)
			}
		})
	}
}
