import pytest
from worker.clients.ai_gateway_client import ai_gateway_client
from worker.services.highlight.filters import check_forbidden_title
from worker.services.highlight.normalizer import get_team_aliases, matches_alias, normalize_for_alias_matching


def test_oracle_ai_gateway_live_selection():
    """Verify Oracle VM AI Gateway successfully selects authentic Celta vs USG highlights."""
    candidates = [
        {
            "id": "vk_mHwk_Agg",
            "title": "Celta vs Union Saint-Gilloise | Goals and highlights | UEFA Europa League",
            "duration": 119,
            "channel": "@rccelta",
        },
        {
            "id": "7RPhpyOveo8",
            "title": "Celta de Vigo 1-2 Union Saint-Gilloise | Europa League 26/27 Match Highlights",
            "duration": 503,
            "channel": "@beINSPORTSAsia",
        },
    ]
    best_idx, reason, evaluator = ai_gateway_client.rerank_highlights(
        "Celta Vigo", "Union St.-Gilloise", "UEFA Europa League", candidates
    )
    assert best_idx is not None
    assert 0 <= best_idx < len(candidates)
    assert any(tag in evaluator for tag in ["ORACLE GATEWAY", "GEMINI", "OPENROUTER", "ORACLE_LOCAL_OLLAMA"])
    assert reason is not None


def test_oracle_ai_gateway_greek_press_conference_rejection():
    """Verify Oracle VM AI Gateway rejects Greek press conferences."""
    candidates = [
        {
            "id": "abc12345",
            "title": "Η Συνέντευξη Τύπου του Χρ. Κόντη μετά το Hapoel Beer Sheva - ΟΦΗ 0-2",
            "duration": 600,
            "channel": "@oficretefc",
        }
    ]
    best_idx, reason, evaluator = ai_gateway_client.rerank_highlights(
        "Hapoel Beer Sheva", "OFI Crete", "Club Friendly", candidates
    )
    assert best_idx == -1
    assert "press conference" in reason.lower() or "interview" in reason.lower()


def test_greek_title_blacklist_filter():
    """Verify deterministic regex immediately flags Greek press conference titles."""
    title = "Η Συνέντευξη Τύπου του Χρ. Κόντη μετά το Hapoel Beer Sheva - ΟΦΗ 0-2"
    is_forb, category = check_forbidden_title(title)
    assert is_forb is True
    assert category in ["Press Conference", "Interview/Profile", "Post/Pre-Match Talk"]


def test_st_and_saint_abbreviation_expansion():
    """Verify St. and Saint expand bidirectionally."""
    aliases = get_team_aliases("Union St.-Gilloise")
    norm_broadcaster_title = normalize_for_alias_matching("Union Saint-Gilloise highlights")
    assert any(matches_alias(a, norm_broadcaster_title) for a in aliases)

    celta_aliases = get_team_aliases("Celta Vigo")
    norm_celta_broadcaster = normalize_for_alias_matching("Celta de Vigo highlights")
    assert any(matches_alias(a, norm_celta_broadcaster) for a in celta_aliases)


def test_hapoel_beer_sheva_apostrophe_and_alias_matching():
    """Verify Hapoel Be'er matches Beer-Sheva and intra-word apostrophes collapse."""
    h_aliases = get_team_aliases("Hapoel Be'er")
    norm_broadcaster = normalize_for_alias_matching("Hapoel Beer-Sheva 0-2 OFI | Europa League 26/27 Match Highlights")
    assert any(matches_alias(a, norm_broadcaster) for a in h_aliases)

    # Test intra-word apostrophe collapse
    assert "beer" in normalize_for_alias_matching("Hapoel Be'er")
    assert "newells" in normalize_for_alias_matching("Newell's Old Boys")
    assert "ohiggins" in normalize_for_alias_matching("O'Higgins")


def test_hapoel_vs_ofi_ai_gateway_selection():
    """Verify Oracle VM AI Gateway selects genuine Hapoel Beer-Sheva vs OFI highlight."""
    candidates = [
        {
            "id": "OcsQSSopwvc",
            "title": "Hapoel Beer-Sheva 0-2 OFI | Europa League 26/27 Match Highlights",
            "duration": 503,
            "channel": "@beINSPORTSAsia",
        },
        {
            "id": "Gtm5XPUNX9s",
            "title": "James Maddison: How Spurs Ended Their Trophy Drought! | The Pursuit Podcast",
            "duration": 1800,
            "channel": "@HBSFCS",
        },
    ]
    best_idx, reason, evaluator = ai_gateway_client.rerank_highlights(
        "Hapoel Be'er", "OFI Crete", "UEFA Europa League", candidates
    )
    assert best_idx == 0
    assert any(tag in evaluator for tag in ["ORACLE GATEWAY", "GEMINI", "OPENROUTER", "ORACLE_LOCAL_OLLAMA"])
    assert reason is not None


def test_local_ollama_direct_call():
    """Verify local Ollama on Oracle VM Super-Node can evaluate highlight candidates."""
    candidates = [
        {
            "id": "6zQ622ayzVE",
            "title": "HIGHLIGHTS | A chance-filled night 😬",
            "duration": 125,
            "channel": "@psv",
            "description": "PSV heeft het UEFA Champions League-duel met Club Brugge in eigen huis verloren (1-2)...",
        },
        {
            "id": "Fyq0a3OVjHM",
            "title": "INTERVIEWS | We have to look forward",
            "duration": 200,
            "channel": "@psv",
            "description": "Post match interviews...",
        },
    ]
    prompt = ai_gateway_client._build_prompt("PSV Eindhoven", "Club Brugge", "UEFA Champions League", candidates)
    best_idx, reason, failed = ai_gateway_client._call_gateway_model(
        "local/qwen3.5:4b", prompt, len(candidates), timeout=45.0
    )
    assert not failed
    assert best_idx == 0
    assert reason is not None


def test_quota_failover_cascade_to_local_ollama(monkeypatch):
    """Verify that when all cloud models hit HTTP 429 quota exhaustion, pipeline cascades to local Ollama."""
    candidates = [
        {
            "id": "6zQ622ayzVE",
            "title": "HIGHLIGHTS | A chance-filled night 😬",
            "duration": 125,
            "channel": "@psv",
            "description": "PSV heeft het UEFA Champions League-duel met Club Brugge in eigen huis verloren (1-2)...",
        },
    ]

    original_call = ai_gateway_client._call_gateway_model

    def mock_call_gateway_model(model_name: str, prompt: str, count: int, timeout: float = 15.0):
        # Simulate all cloud & openrouter models failing with 429 quota errors
        if not model_name.startswith("local/"):
            return None, None, True
        # Local Ollama succeeds!
        return original_call(model_name, prompt, count, timeout=45.0)

    monkeypatch.setattr(ai_gateway_client, "_call_gateway_model", mock_call_gateway_model)
    monkeypatch.setattr(ai_gateway_client, "_evaluate_gemini_fallback", lambda p, c: (None, None))

    best_idx, reason, evaluator = ai_gateway_client.rerank_highlights(
        "PSV Eindhoven", "Club Brugge", "UEFA Champions League", candidates
    )
    assert best_idx == 0
    assert "ORACLE_LOCAL_OLLAMA" in evaluator
    assert reason is not None



