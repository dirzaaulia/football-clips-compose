"""Title filtering and keyword constants for highlight discovery."""

# Forbidden patterns for YouTube video title filtering
FORBIDDEN_TITLE_PATTERNS: list[tuple[str, str]] = [
    # 1. Shorts, vertical clips, and social media reels
    (r'#shorts?|\bshorts?\b|#reels?|#tiktok', 'Shorts/Reel'),

    # 2. Press conferences & media conferences (all European languages)
    (r'\b(press conference|presser|conferência|conferencia|prensa|conferenza|stampa|pressekonferenz|conférence|point presse|conf\' de presse|pk mit|pk vor|rueda de prensa|parla mister|mister\b.*post|mister|συνέντευξη τύπου|συνεντευξη τυπου|συνέντευξη|συνεντευξη)\b', 'Press Conference'),

    # 3. Interviews, talks, chats, and personal confession/profile videos
    (r'\b(interviews?|entrevista|entrevistas|declarações|declaraciones|reacciones|réactions|interviste|dopogara|se confie|il se confie|confie|confidences|entretien|face à face|en tête-à-tête|portrait|itw|in conversation with|exclusive interview|im gespräch|a tu per tu|bate-papo|talks? to|chats? with|player profile|meet the|get to know|q&a|ask me anything|les mots de|le parole di|parole di|al termine di|commento|a fine partita|stimmen zum spiel|stimmen nach|trainerstimmen|nach dem spiel|δηλώσεις|δηλωσεις)\b|:\s*["“„][^"”]+["”]|^\s*["“„][^"”]+["”]|["“„][^"”]+["”]\s*\|', 'Interview/Profile'),

    # 4. Post/Pre match talk, debriefs, watchalongs, reaction shows
    (r'\b(rescaldo|post-match|post match|pre-match|pre match|previa|dopopartita|debrief|aftermatch|watchalongs?|reaction show|fan zone|matchday live|half-time:|post\s+[a-z0-9]+-[a-z0-9]+|μετά το|μετα το)\b', 'Post/Pre-Match Talk'),

    # 5. Training, Behind the Scenes, Atmosphere, Vlogs, Sights & Sounds, Anniversaries
    (r'\b(training|trains ahead of|train ahead of|prep|preparations?|cctv|behind the scenes|inside matchday|inside anfield|(?:tunnel|bench|dugout|tactical|manager|boss|player|fan|access|body|ref)\s*cam|alternative commentary|all the angles|uncut exclusive|exclusive footage|vlogs?|sights\s*(?:&|and)\s*sounds|anniversary|celebrations?|unfiltered|access all areas|all access|mic\'d up|on the pitch|warm-up|warm up|arrivals?|matchday experience|behind-the-scenes)\b', 'Training/BTS/Atmosphere'),

    # 6. Fan reactions, player reactions, fan cams
    (r'\b(fan reactions?|reactions?|reacts?|reaction\b|fan cam|supporters? view|the verdict|live from\b|von fans)\b', 'Fan Reaction'),

    # 7. Previews & predictions
    (r'\b(previews?|match preview|avant-match|preview show)\b', 'Preview'),

    # 8. Women matches
    (r'\b(wsl|women|womens?|féminine|feminina|frauen|femminile|femmes)\b', 'Women Match'),

    # 9. Youth / Academy matches
    (r'\b(youth|youths|youth league|u15|u16|u17|u18|u19|u20|u21|u22|u23|primavera|juvenil|sub-15|sub-17|sub-19|sub-20|sub-23|cantera|akademie|junioren|juniors?)\b|\bu[- ]?(?:15|16|17|18|19|20|21|22|23)\b', 'Youth Match'),

    # 10. Compilations, best saves, all goals of the week, round reviews
    (r'\b(tous les buts|all goals|all highlights|best\s+goals?|top\s+goals?|goal\s+of\s+the\s+(?:week|month|season|year)|goals\s+of\s+the\s+(?:week|month)|gotm|gotw|gots|buts de la journée|best saves?|top saves?|gol da ricordare|bangers against|pure magic)\b|\brésumé\s+.*\bjournée\b', 'Compilation/Weekly Review'),

    # 11. Historical / Retro / Documentaries / Mini-games / Quizzes / Corporate
    (r'\b(classic match|retro|rewind|flashback|throwback|documentary|documentaire|history of|memories|tribute|hall of fame|qui était|juegan a|adivinar el escudo|quiz|challenge|crossbar|tier list|sustainability|foundation|charity|mental health|renews until|feeding a football team|trofeo|volksfest|wasen-mehrkampf|2021|2020|2019|2018|2017|2016|2015)\b', 'Documentary/Retro/Social'),

    # 12. Full match replays (only focus on highlights or extended highlights)
    (r'\b(full match|full match replay|match replay|full game|match complet|partido completo|partita intera|ganzes spiel)\b', 'Full Match Replay'),

    # 13. Video games, simulations, PES, FIFA, eFootball, and VFX animations
    (r'\b(gameplay|game\s*play|efootball|pes\s*\d*|fifa\s*\d*|ea\s*sports\s*fc|fc\s*2[4-7]|dls|dream league|vfx|simulation|simulated|mod\b|concept)\b', 'Gaming/Simulation/VFX'),

    # 14. Studio debate, talk shows, punditry, and clickbait opinion/question segments
    (r'\b(the golazo show|ucltoday|#ucltoday|champions club|morning footy|box 2 box|attacking third|espn fc|sky sports news|match of the day|motd|talksport)\b', 'Studio/Talk Show'),
    (r'\|\s*(?:is|did|can|why|how|should|will|who|what|where)\b.*(?:\?|$)', 'Debate Question'),
    (r'\b(destroy|destroys|shocks|slams|praises|questions|discusses|dissects|debate|verdict on|setting the standard)\b', 'Pundit Clickbait/Debate'),
]

import json
from pathlib import Path

# Load externalized multilingual highlight terms taxonomy
TAXONOMY_PATH = Path(__file__).parent / "taxonomy" / "highlight_terms.json"
try:
    with open(TAXONOMY_PATH, encoding="utf-8") as f:
        HIGHLIGHT_TAXONOMY: dict[str, list[str]] = json.load(f)
except Exception:
    HIGHLIGHT_TAXONOMY = {
        "en": ["highlights", "highlight", "extended highlights", "mini match", "all goals", "goals"],
        "es": ["resumen", "goles", "mejores momentos"],
        "pt": ["resumo", "melhores momentos", "gols"],
        "fr": ["résumé", "resume", "buts"],
        "de": ["zusammenfassung", "tore", "alle tore"],
        "it": ["sintesi", "gol"],
        "nl": ["samenvatting"],
        "el": ["στιγμιότυπα", "φάσεις"],
        "tr": ["özet", "goller"],
        "ar": ["ملخص", "أهداف"],
        "id": ["cuplikan pertandingan", "ringkasan pertandingan"],
    }

# Build regex keyword list from taxonomy
_compiled_terms: set[str] = set()
for lang_terms in HIGHLIGHT_TAXONOMY.values():
    for term in lang_terms:
        clean = term.strip().lower()
        if clean:
            _compiled_terms.add(rf"\b{clean}\b")

# Ensure required legacy patterns are present
_compiled_terms.update([
    r"\bhighlights?\b",
    r"\bmini[\s-]*match\b",
    r"\bcuplikan\s+pertandingan\b",
    r"\bringkasan\s+pertandingan\b",
    r"\bgols?\b",
    r"\bextended highlights?\b",
])

HIGHLIGHT_REQUIRED_KEYWORDS: list[str] = sorted(list(_compiled_terms))

__all__ = [
    "FORBIDDEN_TITLE_PATTERNS",
    "HIGHLIGHT_REQUIRED_KEYWORDS",
    "HIGHLIGHT_TAXONOMY",
]

