package aigw

import "testing"

func TestMatchesTeam(t *testing.T) {
	tests := []struct {
		title string
		team  string
		want  bool
	}{
		{"Atalanta 4-4 Inter | Pazzesco 4-4 a Bergamo", "Internazionale", true},
		{"Atalanta 4-4 Inter | Pazzesco 4-4 a Bergamo", "Atalanta", true},
		{"Real Madrid 5-1 PSV | Champions League 26/27 Match Highlights", "PSV Eindhoven", true},
		{"Real Madrid 5-1 PSV | Champions League 26/27 Match Highlights", "Real Madrid", true},
		{"Bodø/Glimt 1-0 LASK | Champions League 26/27 Match Highlights", "Bodo/Glimt", true},
		{"Bodø/Glimt 1-0 LASK | Champions League 26/27 Match Highlights", "LASK Linz", true},
		{"Highlights: Liverpool 4-0 Man Utd", "Manchester United", true},
		{"Highlights: Liverpool 4-0 Man Utd", "Liverpool", true},
		{"Werder Bremen 2-1 Hamburg SV", "Werder Bremen", true},
		{"Werder Bremen 2-1 Hamburg SV", "Hamburger SV", true},
		{"Racing Santander 1-2 Real Sociedad", "Real Sociedad", true},
		{"R. RACING CLUB 3 - 2 REAL SOCIEDAD | RESUMEN LALIGA EA SPORTS", "Racing Santander", true},
		{"Real Racing Club 3-2 Real Sociedad ı HIGHLIGHTS", "Racing Santander", true},
		{"RC STRASBOURG ALSACE - STADE BRESTOIS 29 (0-1) | Week 11", "Strasbourg", true},
		{"RC STRASBOURG ALSACE - STADE BRESTOIS 29 (0-1) | Week 11", "Brest", true},
		{"Strasbourg 0-1 Brest : Le résumé et les buts", "Brest", true},
		{"Nashville SC vs FC Cincinnati | Match Highlights", "FC Cincinnati", true},
		{"Celta Vigo 0-3 Barcelona", "Celta Vigo", true},
		{"Ajax Amsterdam 3-0 ADO Den Haag", "Ajax Amsterdam", true},
		{"Some Random Capsule Video 1-0", "PSV Eindhoven", false},
		{"Arsenal 2-0 Chelsea", "Manchester United", false},
	}

	for _, tc := range tests {
		got := matchesTeam(tc.title, tc.team)
		if got != tc.want {
			t.Errorf("matchesTeam(%q, %q) = %v; want %v", tc.title, tc.team, got, tc.want)
		}
	}
}
