from worker.services.highlight_service import HighlightService


def test_forbidden_titles():
    service = HighlightService()

    forbidden_cases = [
        ("Arsenal vs Chelsea #shorts", "Shorts"),
        ("Post-Match Press Conference: Mikel Arteta", "Press Conference"),
        ("Pep Guardiola Interview after the derby", "Interview"),
        ("Inside Anfield: Behind The Scenes", "Training/BTS"),
        ("Arsenal Women vs Chelsea Women Highlights", "Women Match"),
        ("Man City U21 vs Liverpool U21 Youth Cup", "Youth Match"),
        ("Fan Reaction to Manchester United loss", "Reaction"),
        ("Tous les buts de la 5e journée", "Weekly Round Review"),
    ]

    for title, expected_reason in forbidden_cases:
        is_forbidden, reason = service.check_forbidden_title(title)
        assert is_forbidden is True, f"Failed for: {title}"
        assert reason is not None

def test_legitimate_highlight_titles():
    service = HighlightService()

    legit_cases = [
        "HIGHLIGHTS | Manchester City 3-1 Sunderland | Premier League",
        "Arsenal vs Chelsea | Extended Highlights | Premier League 2026",
        "Real Madrid 2-1 FC Barcelona | Resumen y Goles | LaLiga EA Sports",
        "Bayern München vs Borussia Dortmund | Full Highlights | Bundesliga",
        "AC Milan vs Juventus | Serie A Match Highlights",
    ]

    for title in legit_cases:
        is_forbidden, reason = service.check_forbidden_title(title)
        assert is_forbidden is False, f"False positive for legit title: {title} (reason: {reason})"
