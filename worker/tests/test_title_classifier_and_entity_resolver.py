"""Unit tests for Stage 0 Entity-Masking, MultilingualTitleClassifier, 3-Stage ClubEntityResolver,
typed BroadcasterEntity models, 180s RSS TTL caching, and the 3-Strike AI Circuit Breaker.
"""

from worker.clients.ai_gateway_client import AiGatewayClient
from worker.clients.youtube_rss_client import YouTubeRSSClient
from worker.models.taxonomy_models import BroadcasterEntity, TaxonomyCategory
from worker.repositories.channel_repository import channel_repository
from worker.services.highlight.club_entity_resolver import club_entity_resolver
from worker.services.highlight.title_classifier import title_classifier


def test_stage0_entity_masking_preserves_clubs_and_prevents_false_positives():
    """Verify Stage 0 masks matched club names as [HOME_CLUB]/[AWAY_CLUB] before classification."""
    # 'Talleres' and 'Estudiantes' are masked as [HOME_CLUB] and [AWAY_CLUB]
    masked = club_entity_resolver.mask_club_entities(
        "TALLERES 1 - 1 ESTUDIANTES | Resumen del partido | #TorneoMercadoLibre",
        "Talleres",
        "Estudiantes de La Plata",
    )
    assert "[HOME_CLUB]" in masked
    assert "[AWAY_CLUB]" in masked

    # 'Newell's Old Boys' and 'BSC Young Boys' must NOT trigger any youth/forbidden rejection
    res_nob = title_classifier.classify(
        "INSTITUTO 2 - 2 NEWELL'S OLD BOYS | Resumen del partido",
        home_team="Instituto",
        away_team="Newell's Old Boys",
    )
    assert res_nob.is_forbidden is False
    assert res_nob.has_positive_highlight_keyword is True
    assert "resumen" in res_nob.positive_keywords_matched

    res_yb = title_classifier.classify(
        "BSC Young Boys 1 - 2 Aston Villa | Extended Highlights",
        home_team="BSC Young Boys",
        away_team="Aston Villa",
    )
    assert res_yb.is_forbidden is False
    assert res_yb.has_positive_highlight_keyword is True


def test_multilingual_positive_and_negative_taxonomy_across_11_languages():
    """Verify positive highlight terms and negative press/interview terms across all 11 languages."""
    positive_samples = [
        "Arsenal 2-1 Chelsea | Extended Highlights",
        "Real Madrid 3-0 Barcelona | Resumen y Goles",
        "Flamengo 2-0 Palmeiras | Melhores Momentos",
        "PSG 3-1 Marseille | Le résumé du match",
        "SC Paderborn 07 vs Eintracht Frankfurt | Zusammenfassung",
        "Inter 2-1 Juventus | Sintesi Serie A",
        "Samenvatting: Ajax - PSV (3-2) | Eredivisie",
        "Ολυμπιακός - Παναθηναϊκός 2-0 | Στιγμιότυπα",
        "Galatasaray 3-1 Fenerbahçe | Maç Özeti",
        "ملخص مباراة الهلال والنصر 2-1",
        "Cuplikan Pertandingan Persib vs Persija 2-0",
    ]
    for title in positive_samples:
        res = title_classifier.classify(title)
        assert res.is_forbidden is False, f"Unexpectedly rejected: {title}"
        assert res.has_positive_highlight_keyword is True, f"Missed positive keyword in: {title}"

    negative_samples = [
        ("Rueda de prensa de Carlo Ancelotti", TaxonomyCategory.PRESS_CONFERENCE),
        ("Persconferentie Peter Bosz na Ajax - PSV", TaxonomyCategory.PRESS_CONFERENCE),
        ("Nabeschouwing met Kenneth Taylor", TaxonomyCategory.INTERVIEW),
        ("Voorbeschouwing Feyenoord - Ajax", TaxonomyCategory.POST_PRE_MATCH),
        ("Arsenal U21 vs Chelsea U21 Highlights", TaxonomyCategory.WOMEN_YOUTH),
        ("Barcelona vs Real Madrid | EA Sports FC 26 Simulation", TaxonomyCategory.GAMING_SIM),
    ]
    for title, expected_cat in negative_samples:
        res = title_classifier.classify(title)
        assert res.is_forbidden is True, f"Failed to reject: {title}"
        assert res.matched_category == expected_cat


def test_structural_override_rule_for_full_match_highlights():
    """Verify 'Full Match Highlights' is allowed via structural override while 'Full Match Replay' is rejected."""
    res_allowed = title_classifier.classify("Arsenal vs Chelsea | Full Match Highlights")
    assert res_allowed.is_forbidden is False
    assert res_allowed.has_positive_highlight_keyword is True

    res_es_allowed = title_classifier.classify("Real Madrid vs Betis | Resumen del Partido Completo")
    assert res_es_allowed.is_forbidden is False

    res_rejected = title_classifier.classify("Arsenal vs Chelsea | Full Match Replay (90 Minutes)")
    assert res_rejected.is_forbidden is True
    assert res_rejected.matched_category == TaxonomyCategory.FULL_REPLAY


def test_three_stage_club_entity_resolver_and_fuzzy_token_matching():
    """Verify NFKD ligatures, affix stripping, seed aliases, and >=90% token-set similarity fallback."""
    norm_hashtag = club_entity_resolver.normalize_for_alias_matching("#NapoliBodøGlimt")
    assert "napoli bodo glimt" in norm_hashtag

    # Seed aliases from club_aliases_seed.json
    assert "gladbach" in club_entity_resolver.get_team_aliases("Borussia Mönchengladbach")
    assert "psg" in club_entity_resolver.get_team_aliases("Paris Saint-Germain")
    assert "xeneize" in club_entity_resolver.get_team_aliases("Boca Juniors")

    # Token-set similarity fallback (>=90%) for minor multi-word transliteration differences
    norm_title = club_entity_resolver.normalize_for_alias_matching(
        "Highlights: Eintracht Francfort vs Borussia Dortmund",
    )
    assert club_entity_resolver.matches_alias("eintracht frankfurt", norm_title) is True


def test_typed_broadcaster_entities_and_rss_ttl_cache_and_circuit_breaker(monkeypatch):
    """Verify BroadcasterEntity models, 180s RSS TTL cache, and 3-strike AI Gateway circuit breaker."""
    broadcasters = channel_repository.get_broadcasters()
    assert len(broadcasters) >= 15
    assert all(isinstance(b, BroadcasterEntity) for b in broadcasters)
    espn_nl = next(b for b in broadcasters if b.broadcaster_id == "espn_nl")
    assert espn_nl.channel_handle == "@espnnl"
    assert "DED" in espn_nl.competitions

    # Verify 180s RSS TTL Cache avoids duplicate HTTP requests
    rss_client = YouTubeRSSClient()
    call_count = 0
    sample_xml = b"""<?xml version="1.0" encoding="UTF-8"?>
    <feed xmlns:yt="http://www.youtube.com/xml/schemas/2015" xmlns:media="http://search.yahoo.com/mrss/" xmlns="http://www.w3.org/2005/Atom">
      <entry>
        <yt:videoId>7TtxnvjwYmo</yt:videoId>
        <yt:channelId>UCXnPiEv1DoUCDAqDUXT9shQ</yt:channelId>
        <title>Samenvatting: Ajax - PSV</title>
        <published>2026-11-08T15:00:00+00:00</published>
        <author><name>ESPN NL</name></author>
        <media:group><media:description>Eredivisie samenvatting</media:description></media:group>
      </entry>
    </feed>"""

    class DummyResp:
        status_code = 200
        content = sample_xml

    def mock_get(url, timeout=10.0):
        nonlocal call_count
        call_count += 1
        return DummyResp()

    monkeypatch.setattr(rss_client._http_client, "get", mock_get)
    items_1 = rss_client.fetch_channel_recent_videos("UCXnPiEv1DoUCDAqDUXT9shQ")
    items_2 = rss_client.fetch_channel_recent_videos("UCXnPiEv1DoUCDAqDUXT9shQ")
    assert len(items_1) == 1
    assert len(items_2) == 1
    assert call_count == 1  # Second call served in <0.01ms from 180s TTL cache!

    # Verify 3-Strike Circuit Breaker in AiGatewayClient
    ai_client = AiGatewayClient()
    monkeypatch.setattr(ai_client, "_call_gateway_model", lambda *a, **kw: (None, None, True))
    monkeypatch.setattr(ai_client, "_evaluate_gemini_fallback", lambda *a, **kw: (None, None))

    for i in range(3):
        _, _, ev = ai_client.rerank_highlights("Ajax", "PSV", "DED", [{"id": f"v{i}", "title": "Ajax - PSV"}])
        assert ev == "DETERMINISTIC_FALLBACK"

    assert ai_client.is_circuit_open() is True
    ai_client.reset_circuit_breaker()
    assert ai_client.is_circuit_open() is False
