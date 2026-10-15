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
        ("PIERRE SAGE : DE LENS À CRYSTAL PALACE, IL SE CONFIE", "Interview/Profile"),
        ("The sights & sounds around Brighton before our 125 anniversary match! ️ #BHAFC #PremierLeague", "Training/BTS/Atmosphere"),
        ("Entretien exclusif avec Luis Enrique", "Interview/Profile"),
        ("Pre-Match Sights and Sounds at the Stadium", "Training/BTS/Atmosphere"),
        ("CCTV | Internationals Return KAMADA, PINO, SARR & STRAND LARSEN | Nottingham Forest Prep ⚽️", "Training/Prep"),
        ("CR7 HAT-TRICK IN ONE HALF! 🤯 | Cagliari 1-3 Juventus | Serie A 2021", "Retro Match"),
        ("🗣️ MISTER GATTUSO POST LAZIO-JUVE STABIA", "Press Conference"),
        ("The Blueprint Of Sustainability In Football | Udinese \"Play For Sustainability\"", "Corporate Promo"),
        ("Les mots de Filipe Luis avant la reprise face à Toulouse", "Press Conference"),
        ("Le point presse d'Alexandre Dujeux avant FC Lorient - Paris FC 26-27", "Press Conference"),
        ("Sassuolo-Milan: i nostri 5 gol da ricordare ⚽", "Retro Compilation"),
        ("ANTONY, NATAN, CUCHO y DEOSSA juegan a adivinar el escudo de los equipos 🛡️❓ | Real BETIS Balompié", "Quiz/Mini-game"),
        ("Qui était Joseph Yegba Maya ?   #marseille #football #ligue1 #om #foot #goal", "Documentary"),
        ("„Es geht darum, die nächste Phase der Saison zu gestalten“ | PK mit Niko Kovac | BVB - Werder Bremen", "Press Conference"),
        ("Spurs train ahead of Man Utd trip 🔋", "Training"),
        ("What goes into feeding a football team? 🍽️ #newcastle #football #premierleague", "BTS"),
        ("Arsenal vs Chelsea | Full Match Replay | Premier League", "Full Match Replay"),
        ("Real Madrid vs Barcelona - Full Match Replay", "Full Match Replay"),
    ]

    for title, expected_reason in forbidden_cases:
        is_forbidden, reason = service.check_forbidden_title(title)
        assert is_forbidden is True, f"Failed to block forbidden title: {title}"
        assert reason is not None


def test_legitimate_highlight_titles():
    service = HighlightService()

    legit_cases = [
        "HIGHLIGHTS | Manchester City 3-1 Sunderland | Premier League",
        "Arsenal vs Chelsea | Extended Highlights | Premier League 2026",
        "Real Madrid 2-1 FC Barcelona | Resumen y Goles | LaLiga EA Sports",
        "Bayern München vs Borussia Dortmund | Full Highlights | Bundesliga",
        "AC Milan vs Juventus | Serie A Match Highlights",
        "LENS - RENNES (3 - 1) - Résumé - (RCL - SRFC) / 2026/2027",
    ]

    for title in legit_cases:
        is_forbidden, reason = service.check_forbidden_title(title)
        assert is_forbidden is False, f"False positive for legit title: {title} (reason: {reason})"


def test_highlight_keyword_validation():
    service = HighlightService()

    # Legit highlights have highlight keywords
    assert service.has_highlight_keyword("HIGHLIGHTS | Manchester City 3-1 Sunderland") is True
    assert service.has_highlight_keyword("Real Madrid 2-1 FC Barcelona | Resumen y Goles") is True
    assert service.has_highlight_keyword("LENS - RENNES (3 - 1) - Résumé") is True
    assert service.has_highlight_keyword("Full Highlights | Bundesliga") is True
    assert service.has_highlight_keyword("AC Milan vs Juventus | Serie A Sintesi") is True

    # Social clips, quizzes, and training goals do NOT have highlight keywords
    assert service.has_highlight_keyword("🔥 STUNNING GOAL FROM DIOMANDE AFTER A BRILLIANT MOVE WITH BRAHIM! 😱 | REAL MADRID") is False
    assert service.has_highlight_keyword("XAVI ESPART RENEWS UNTIL 2030 | FC Barcelona 🔵🔴") is False
    assert service.has_highlight_keyword("Spurs train ahead of Man Utd trip 🔋") is False


def test_is_youtube_short_detection():
    from worker.clients.youtube_client import youtube_client
    # nFCeu8-Bydg is Brighton's vertical 125 anniversary short (verified status=200 on /shorts/)
    assert youtube_client.is_youtube_short("nFCeu8-Bydg") is True
    # Standard horizontal highlights (redirects to /watch?v=...)
    assert youtube_client.is_youtube_short("PX_Z58lDe84") is False
    assert youtube_client.is_youtube_short("") is False

