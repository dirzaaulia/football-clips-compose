import os
import time
import json
import html
import requests
import re
import socket
from datetime import datetime, timedelta, timezone
from zoneinfo import ZoneInfo

socket.setdefaulttimeout(15)

from supabase import create_client, Client
from googleapiclient.discovery import build
from google import genai
from google.genai import types

# ==============================================================================
# 1. KONFIGURASI & CLIENT INITIALIZATION
# ==============================================================================
SUPABASE_URL = os.environ.get("SUPABASE_URL")
SUPABASE_KEY = os.environ.get("SUPABASE_SERVICE_ROLE_KEY")
FOOTBALL_DATA_API_KEY = os.environ.get("FOOTBALL_DATA_API_KEY")
YOUTUBE_API_KEY = os.environ.get("YOUTUBE_API_KEY")
GEMINI_API_KEY = os.environ.get("GEMINI_API_KEY")

if not all([SUPABASE_URL, SUPABASE_KEY, FOOTBALL_DATA_API_KEY, YOUTUBE_API_KEY]):
    raise ValueError("Pastikan SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY, FOOTBALL_DATA_API_KEY, dan YOUTUBE_API_KEY sudah diset.")

supabase: Client = create_client(SUPABASE_URL, SUPABASE_KEY)
youtube = build("youtube", "v3", developerKey=YOUTUBE_API_KEY)

# Inisialisasi Gemini Client
ai_client = None
if GEMINI_API_KEY:
    try:
        ai_client = genai.Client(api_key=GEMINI_API_KEY)
    except Exception as e:
        print(f"Peringatan: Gagal inisialisasi Gemini Client: {e}")

TARGET_COMPETITIONS = ["PL", "PD", "SA", "BL1", "FL1", "CL", "EL"]
try:
    WIB = ZoneInfo("Asia/Jakarta")
    PACIFIC_TZ = ZoneInfo("America/Los_Angeles")
except Exception:
    WIB = timezone(timedelta(hours=7))
    PACIFIC_TZ = timezone(timedelta(hours=-7))

# Rentang durasi paten video highlight: 90 detik (1.5 menit) s/d 1500 detik (25 menit)
MIN_DURATION_SECONDS = 90
MAX_DURATION_SECONDS = 1500

# Pola kata terlarang murni untuk filter judul video (bukan parameter deskripsi API)
FORBIDDEN_TITLE_PATTERNS = [
    (r'#shorts|\bshorts\b', 'Shorts'),
    (r'\b(press conference|presser|press)\b', 'Press Conference'),
    (r'\b(interviews?)\b', 'Interview'),
    (r'\b(training|behind the scenes|inside matchday|inside anfield)\b', 'Training/BTS'),
    (r'\b(fan reaction|reactions?|watchalong)\b', 'Reaction'),
    (r'\b(previews?|pre-match|previa)\b', 'Preview'),
    (r'\b(wsl|women|féminine|feminina)\b', 'Women Match'),
    (r'\bu[- ]?(?:18|19|21|23)\b', 'Youth Match'),
    (r'\b(tous les buts|all goals|all highlights)\b', 'Compilation/All Goals'),
    (r'\brésumé\s+.*\bjournée\b', 'Weekly Round Review'),
]

def check_forbidden_title(title: str) -> tuple[bool, str | None]:
    t = title.lower()
    for pattern, reason in FORBIDDEN_TITLE_PATTERNS:
        if re.search(pattern, t, flags=re.IGNORECASE):
            return True, reason
    return False, None

# ==============================================================================
# 2. PEMETAAN KLUB EROPA RESMI (PL, UCL & UEL) BESERTA CHANNEL ID PERMANEN
# ==============================================================================
EUROPEAN_CLUB_HANDLES = {
    # England - Premier League
    "arsenal": "@arsenal",
    "aston villa": "@avfcofficial",
    "bournemouth": "@afcbournemouth",
    "brentford": "@brentfordfc",
    "brighton": "@OfficialBHAFC",
    "chelsea": "@chelseafc",
    "coventry city": "@coventrycityfc",
    "crystal palace": "@OfficialCPFC",
    "everton": "@everton",
    "fulham": "@fulhamfc",
    "hull city": "@hullcityofficial",
    "ipswich town": "@ipswichtown",
    "ipswich": "@ipswichtown",
    "leeds united": "@leedsunited",
    "liverpool": "@liverpoolfc",
    "manchester city": "@mancity",
    "manchester united": "@manutd",
    "newcastle united": "@newcastleunited",
    "newcastle": "@newcastleunited",
    "nottingham forest": "@NottinghamForestFC",
    "sunderland": "@sunderlandafc",
    "tottenham": "@tottenhamhotspur",
    "west ham": "@westham",
    "wolverhampton": "@wolves",
    "wolves": "@wolves",

    # Spain - LaLiga & UEFA
    "real madrid": "@realmadrid",
    "barcelona": "@fcbarcelona",
    "atletico madrid": "@atleticodemadrid",
    "atlético madrid": "@atleticodemadrid",
    "girona": "@GironaFC",
    "real sociedad": "@realsociedad",
    "athletic club": "@athleticclubtv",
    "athletic bilbao": "@athleticclubtv",
    "villarreal": "@villarrealcf",
    "real betis": "@realbetis",
    "sevilla": "@sevillafc",

    # Germany - Bundesliga & UEFA
    "bayern munich": "@fcbayern",
    "bayern münchen": "@fcbayern",
    "bayern": "@fcbayern",
    "borussia dortmund": "@bvb",
    "dortmund": "@bvb",
    "bayer 04 leverkusen": "@bayer04fussball",
    "leverkusen": "@bayer04fussball",
    "rb leipzig": "@rbleipzig",
    "leipzig": "@rbleipzig",
    "vfb stuttgart": "@vfb",
    "stuttgart": "@vfb",
    "eintracht frankfurt": "@eintracht",
    "frankfurt": "@eintracht",
    "hoffenheim": "@tsghoffenheim1899",

    # Italy - Serie A & UEFA
    "inter milan": "@inter",
    "internazionale": "@inter",
    "inter": "@inter",
    "juventus": "@juventus",
    "ac milan": "@acmilan",
    "milan": "@acmilan",
    "atalanta": "@atalantabc",
    "bologna": "@siamobolognatv",
    "as roma": "@officialasroma",
    "roma": "@officialasroma",
    "ss lazio": "@officialsslazio",
    "lazio": "@officialsslazio",
    "napoli": "@officialsscnapoli",
    "fiorentina": "@acffiorentina",

    # France - Ligue 1 & UEFA
    "paris saint-germain": "@psg",
    "psg": "@psg",
    "as monaco": "@asmonaco",
    "monaco": "@asmonaco",
    "lille": "@LOSC",
    "stade brestois": "@SB29",
    "brest": "@SB29",
    "olympique lyonnais": "@olympiquelyonnais",
    "lyon": "@olympiquelyonnais",
    "olympique de marseille": "@om",
    "marseille": "@om",
    "ogc nice": "@ogcnice",
    "nice": "@ogcnice",

    # Portugal, Netherlands, Scotland, Turkey & Other UEFA League Phase
    "sporting cp": "@sportingcp",
    "sporting": "@sportingcp",
    "sl benfica": "@slbenfica",
    "benfica": "@slbenfica",
    "fc porto": "@fcporto",
    "porto": "@fcporto",
    "sc braga": "@scbragaoficial",
    "braga": "@scbragaoficial",
    "feyenoord": "@feyenoord",
    "psv eindhoven": "@psv",
    "psv": "@psv",
    "afc ajax": "@afcajax",
    "ajax": "@afcajax",
    "celtic": "@celticfc",
    "rangers": "@rangersfc",
    "club brugge": "@ClubBruggeKV",
    "salzburg": "@fcrbs",
    "sturm graz": "@sksturmtv",
    "dinamo zagreb": "@gnkdinamo",
    "crvena zvezda": "@crvenazvezdafk",
    "red star": "@crvenazvezdafk",
    "sparta praha": "@ACSpartaPraha",
    "sparta prague": "@ACSpartaPraha",
    "slovan bratislava": "@skslovanbratislava",
    "shakhtar donetsk": "@fcshakhtar",
    "shakhtar": "@fcshakhtar",
    "young boys": "@bscyb_offiziell",
    "galatasaray": "@galatasaray",
    "fenerbahce": "@fenerbahce"
}

# CHANNEL ID PERMANEN STATIS (0 KUOTA SELAMANYA)
PERMANENT_CHANNEL_IDS = {
    # England
    "@arsenal": "UCpryVRk_VDudG8SHXgWcG0w",
    "@avfcofficial": "UCICNP0mvtr0prFwGUQIABfQ",
    "@afcbournemouth": "UCfhnXQeejF2jKkK_zIiqe3Q",
    "@brentfordfc": "UCr_14iGZ3_13_x2qP08h_0A",
    "@OfficialBHAFC": "UCU_1zP6K1K92fLd_f5L-iTw",
    "@chelseafc": "UCU2PacFf99vhb3hNiYDmxww",
    "@coventrycityfc": "UCaX3T0aK9_3F5WzI-vWvM-A",
    "@OfficialCPFC": "UC_0_5K0Y5f2sM9P5-Q8fJqA",
    "@everton": "UCVp6vUfWf_M2f4_0z4x1_8w",
    "@fulhamfc": "UCc0c9q8u5gG7gGzX-M8N2fw",
    "@hullcityofficial": "UC5p8_ZfVpE_M6t4Q5T1m4dA",
    "@ipswichtown": "UCM0o_7ZpP_V7l3zK0G4x7Yw",
    "@leedsunited": "UCc6gYc8YmF6hT7dF_f8N2qA",
    "@liverpoolfc": "UC9LQwHZoucFT94I2h6JOcjw",
    "@mancity": "UCkzCjdRMrW2vXLx8mvPVLdQ",
    "@manutd": "UC6yW44UGJJBvYTlfC7CRg2Q",
    "@newcastleunited": "UCs_9GjU6_rC9tK_Z5mXy1Zw",
    "@NottinghamForestFC": "UCwE7XpY3eJkM5M6wQ8R1e7A",
    "@sunderlandafc": "UCzYvB_G4p_8kC-Q2Y5W4v-g",
    "@tottenhamhotspur": "UCEg25rdRZXg32iwai6N6l0w",
    "@westham": "UCt_e0W-3P5vC9Lg2Zp3H3Aw",
    "@wolves": "UCv0M9uC6Y7L8s9-y3_G2FSw",
    
    # Spain
    "@realmadrid": "UCWV3obpZVGgJ3j9FVhEjF2Q",
    "@fcbarcelona": "UC14UlmYlSNiQCBe9Eookf_A",
    "@atleticodemadrid": "UCuzKFwdh7z2GHcIOX_tXgxA",
    "@GironaFC": "UC6x5gKUZpXuKDujmaHc3Xhg",
    "@realsociedad": "UCkNMta6lLRCaOuXl3t9iG-w",
    "@athleticclubtv": "UCUiLE_NqFKarAXFhhmXiIFA",
    "@villarrealcf": "UCv2Yh1kF8Pj_3L7Z0M0-e6A",
    "@realbetis": "UCh8U5D_hQ9N-Gk2P5-j4N4w",
    "@sevillafc": "UCpT8G_X7G1y3K_9j8R2t7Lw",
    
    # Germany
    "@fcbayern": "UCZkcxFIsqW5htimoUQKA0iA",
    "@bvb": "UCK8rTVgp3-MebXkmeJcQb1Q",
    "@bayer04fussball": "UC_DjAsoxu-gvjAahj0yiZJw",
    "@rbleipzig": "UCkZwB4IGoNBvRmVT2gaO4XA",
    "@vfb": "UCNjHTx_URHNiZvjW-uzIf4Q",
    "@eintracht": "UCDFp4bscTFm8hYxCBx-tIXg",
    "@tsghoffenheim1899": "UCYnzpk_ECf4E2QWc9DBWjBQ",
    
    # Italy
    "@inter": "UCvXzEblUa0cfny4HAJ_ZOWw",
    "@juventus": "UCLzKhsxrExAC6yAdtZ-BOWw",
    "@acmilan": "UCKcx1uK38H4AOkmfv4ywlrg",
    "@atalantabc": "UC0R-isVeRhMDe3vFTWP5Spg",
    "@siamobolognatv": "UCaUywe79ysewBvPXljIZ__w",
    "@officialasroma": "UC5jJFSjh9rq91_m71YTOafA",
    "@officialsslazio": "UCVtDCsB0UlIkDn2kjsva3WA",
    "@officialsscnapoli": "UC4Q6K-4f-4Jb9hT2p4W5gAw",
    "@acffiorentina": "UCY6fE4c-j4Y_L8k0R7v2L5w",
    
    # France
    "@psg": "UCt9a_qP9CqHCNwilf-iULag",
    "@asmonaco": "UCHy548EHHX9f-ETJlm18Jiw",
    "@LOSC": "UCae9u1pNGzaklyZC8OKkeCQ",
    "@SB29": "UCau2KOow38YFyBV-wFRsJQA",
    "@olympiquelyonnais": "UCzHCZXmqIdjqRnpdp0l_T6g",
    "@om": "UCZ44s4zH3jUj8Y8-k1M2wDA",
    "@ogcnice": "UCAvm8jHWe-8K2kZK-7ynIHA",
    
    # Netherlands, Portugal, Scotland & Others
    "@feyenoord": "UCg_DGzRRIQlXpHxCrMMiAIQ",
    "@psv": "UC_2ynsXrRrKP8zYrU7Hc06A",
    "@afcajax": "UCGpf7WX7R1one-NwOvg_PbQ",
    "@sportingcp": "UCnJj6L93JX3Jrhzv81ayywA",
    "@slbenfica": "UC8zrah5cNf2c3jKKeD_Z3fw",
    "@fcporto": "UCQegzQwEExHgXvm_yHptzQg",
    "@scbragaoficial": "UCp2R9JyHO-MFFR1ARzrLkMA",
    "@celticfc": "UCBN-bb-hE7jYlcp4exwXRsQ",
    "@rangersfc": "UCVaGyBPoEAZItDjlFPsRcSA",
    "@ClubBruggeKV": "UCF3xdWlzDt_OVFCPbt5fa4Q",
    "@fcrbs": "UCNXjAsLzro7bnZVWqnnkgsg",
    "@sksturmtv": "UCcReHK9o6bc5NT2cj4mpJKQ",
    "@gnkdinamo": "UCldRDEKHfzNuigJy7WlUchQ",
    "@crvenazvezdafk": "UCgONdflkLRAluyngeXsmT4w",
    "@ACSpartaPraha": "UCJcXzTZcKukYq9O4ZBtVxdw",
    "@skslovanbratislava": "UC7ldMqVVX6CD6NMZaqsihTw",
    "@fcshakhtar": "UCmPCqUih--EyT2oxUn72MtA",
    "@bscyb_offiziell": "UCooboQGJOqDy1-jdBNhweUg",
    "@galatasaray": "UCQpeujIamj2ZOKXZnrxTRhA",
    "@fenerbahce": "UCgqlho3-8a6FmDqQm7Q6gJw",
    
    # Broadcaster resmi liga domestik (global)
    "@LaLiga": "UCTv-XvfzLX3i4IGWAm4sbmA",
    "@seriea": "UCBJeMCIeLQos7wacox4hmLQ",
    "@bundesliga": "UC6UL29enLNe4mqwTfAyeNuw",
    "@Ligue1": "UCQsH5XtIc9hONE1BQjucM0g"
}

CUSTOM_KEYWORD_ALIASES = {
    "brighton & hove albion": ["brighton", "bhafc", "albion"],
    "aston villa": ["aston villa", "villa", "avfc"],
    "wolverhampton wanderers": ["wolves", "wolverhampton"],
    "manchester united": ["man united", "man utd", "united", "mufc"],
    "manchester city": ["man city", "city", "mcfc"],
    "tottenham hotspur": ["tottenham", "spurs"],
    "nottingham forest": ["forest", "nottingham"],
    "west ham united": ["west ham", "whufc", "hammers"],
    "newcastle united": ["newcastle", "nufc", "magpies"],
    "paris saint-germain": ["psg", "paris"],
    "internazionale": ["inter", "inter milan"],
    "atletico madrid": ["atletico", "atleti"],
    "club atletico de madrid": ["atletico", "atleti"],
    "athletic club": ["bilbao", "athletic bilbao"],
    "rayo vallecano de madrid": ["rayo vallecano", "rayo"],
    "rayo vallecano": ["rayo vallecano", "rayo"],
    "real madrid cf": ["real madrid"],
    "fc barcelona": ["barcelona", "barca"],
    "es troyes ac": ["troyes", "estac"],
    "estac troyes": ["troyes", "estac"],
    "aj auxerre": ["auxerre"],
    "angers sco": ["angers"],
    "rc strasbourg alsace": ["strasbourg"],
    "racing de lens": ["lens", "rc lens"],
    "racing club de lens": ["lens", "rc lens"],
    "stade brestois 29": ["brest", "brestois"],
    "stade brestois": ["brest", "brestois"],
    "toulouse fc": ["toulouse"],
    "parma calcio 1913": ["parma"],
    "juventus fc": ["juventus"],
    "1. fc köln": ["köln", "koln"],
    "fc köln": ["köln", "koln"],
    "tsg 1899 hoffenheim": ["hoffenheim"],
    "1899 hoffenheim": ["hoffenheim"],
    "07 elversberg": ["elversberg"],
    "sv 07 elversberg": ["elversberg"],
    "bayer 04 leverkusen": ["leverkusen", "bayer"],
    "as monaco fc": ["monaco"],
    "as monaco": ["monaco"],
    "olympique de marseille": ["marseille"],
    "olympique lyonnais": ["lyon"],
    "ss lazio": ["lazio"],
    "genoa cfc": ["genoa"],
    "atalanta bc": ["atalanta"],
    "hertha bsc": ["hertha"],
    "us lecce": ["lecce"],
    "ac milan": ["milan"],
    # European League Clubs
    "sporting clube de portugal": ["sporting cp", "sporting"],
    "sporting cp": ["sporting cp", "sporting"],
    "sl benfica": ["benfica"],
    "sport lisboa e benfica": ["benfica"],
    "fc porto": ["porto"],
    "feyenoord rotterdam": ["feyenoord"],
    "psv eindhoven": ["psv"],
    "afc ajax": ["ajax"],
    "celtic fc": ["celtic"],
    "rangers fc": ["rangers"],
    "club brugge kv": ["club brugge", "brugge"],
    "fc red bull salzburg": ["salzburg"],
    "red bull salzburg": ["salzburg"],
    "sk sturm graz": ["sturm graz"],
    "sturm graz": ["sturm graz"],
    "gnk dinamo zagreb": ["dinamo zagreb", "dinamo"],
    "dinamo zagreb": ["dinamo zagreb", "dinamo"],
    "fk crvena zvezda": ["crvena zvezda", "red star"],
    "crvena zvezda": ["crvena zvezda", "red star"],
    "ac sparta praha": ["sparta praha", "sparta prague"],
    "sparta praha": ["sparta praha", "sparta prague"],
    "sk slovan bratislava": ["slovan bratislava", "slovan"],
    "slovan bratislava": ["slovan bratislava", "slovan"],
    "fc shakhtar donetsk": ["shakhtar donetsk", "shakhtar"],
    "shakhtar donetsk": ["shakhtar donetsk", "shakhtar"],
    "bsc young boys": ["young boys"],
    "young boys": ["young boys"],
    "borussia dortmund": ["dortmund", "bvb"],
    "fc bayern münchen": ["bayern", "bayern munich"],
    "fc bayern munich": ["bayern", "bayern munich"],
    "rb leipzig": ["leipzig", "rb leipzig"],
    "vfb stuttgart": ["stuttgart"],
    "eintracht frankfurt": ["frankfurt"],
    "bologna fc 1909": ["bologna"],
    "as roma": ["roma"],
    # Spanish LaLiga Clubs
    "real sociedad de fútbol": ["real sociedad"],
    "real sociedad": ["real sociedad"],
    "rc celta de vigo": ["celta"],
    "celta de vigo": ["celta"],
    "rc celta": ["celta"],
    "ca osasuna": ["osasuna"],
    "rcd mallorca": ["mallorca"],
    "rcd espanyol de barcelona": ["espanyol"],
    "rcd espanyol": ["espanyol"],
    "deportivo alavés": ["alaves"],
    "deportivo alaves": ["alaves"],
    "ud las palmas": ["las palmas"],
    "cd leganés": ["leganes"],
    "cd leganes": ["leganes"],
    "real valladolid cf": ["valladolid"],
    "real valladolid": ["valladolid"],
    "getafe cf": ["getafe"],
    "valencia cf": ["valencia"]
}

# BROADCASTER MAP (Hanya untuk Liga Domestik Non-Klub)
# UEFA (CL & EL) dihilangkan karena 100% murni diproses via Official Club Channels
BROADCASTER_MAP = {
    "PD": ["@LaLiga"],
    "SA": ["@seriea"],
    "BL1": ["@bundesliga"],
    "FL1": ["@Ligue1"],
    "CL": [],
    "EL": []
}

# Caching Channel ID Persisten di VM (0 Kuota)
CHANNEL_CACHE_FILE = os.path.expanduser("~/channel_cache.json")
DISK_CHANNEL_CACHE = {}

def load_disk_channel_cache():
    global DISK_CHANNEL_CACHE
    if os.path.exists(CHANNEL_CACHE_FILE):
        try:
            with open(CHANNEL_CACHE_FILE, "r", encoding="utf-8") as f:
                DISK_CHANNEL_CACHE = json.load(f)
        except Exception:
            DISK_CHANNEL_CACHE = {}

def save_disk_channel_cache():
    try:
        with open(CHANNEL_CACHE_FILE, "w", encoding="utf-8") as f:
            json.dump(DISK_CHANNEL_CACHE, f, ensure_ascii=False, indent=2)
    except Exception:
        pass

load_disk_channel_cache()
LOG_FILE_PATH = os.path.expanduser("~/web/logs.json")
QUOTA_FILE = os.path.expanduser("~/worker_quota.json")
MAX_QUOTA_PER_DAY = 9500

def get_current_wib_time():
    return datetime.now(WIB)

def save_cycle_log(cycle_data):
    history = []
    if os.path.exists(LOG_FILE_PATH):
        try:
            with open(LOG_FILE_PATH, "r", encoding="utf-8") as f:
                history = json.load(f)
        except Exception:
            history = []
    
    history.insert(0, cycle_data)
    history = history[:150]
    
    os.makedirs(os.path.dirname(LOG_FILE_PATH), exist_ok=True)
    with open(LOG_FILE_PATH, "w", encoding="utf-8") as f:
        json.dump(history, f, ensure_ascii=False, indent=2)

# ==============================================================================
# 2. HELPER FUNCTIONS: QUOTA, ISO & DURATION
# ==============================================================================
def get_next_quota_reset_time_utc():
    now_pt = datetime.now(PACIFIC_TZ)
    reset_today_pt = now_pt.replace(hour=0, minute=0, second=0, microsecond=0) + timedelta(days=1)
    return reset_today_pt.astimezone(timezone.utc)

def check_and_reset_quota():
    now_pt = datetime.now(PACIFIC_TZ)
    pt_date_str = now_pt.strftime("%Y-%m-%d")
    quota_data = {"pt_date": pt_date_str, "hits": 0}
    
    if os.path.exists(QUOTA_FILE):
        try:
            with open(QUOTA_FILE, "r") as f:
                quota_data = json.load(f)
            if quota_data.get("pt_date") != pt_date_str:
                quota_data = {"pt_date": pt_date_str, "hits": 0}
        except Exception:
            pass
            
    with open(QUOTA_FILE, "w") as f:
        json.dump(quota_data, f)
    return quota_data["hits"]

def increment_quota(cost=100):
    now_pt = datetime.now(PACIFIC_TZ)
    pt_date_str = now_pt.strftime("%Y-%m-%d")
    hits = check_and_reset_quota() + cost
    with open(QUOTA_FILE, "w") as f:
        json.dump({"pt_date": pt_date_str, "hits": hits}, f)

def safe_parse_iso(date_str):
    if not date_str: 
        return datetime.now(timezone.utc)
    try:
        cleaned = str(date_str).replace('Z', '+00:00')
        if '+' in cleaned: cleaned = cleaned.split('+')[0]
        if '.' in cleaned: cleaned = cleaned.split('.')[0]
        return datetime.fromisoformat(cleaned).replace(tzinfo=timezone.utc)
    except Exception:
        return datetime.now(timezone.utc)

def parse_iso8601_duration_seconds(duration_str: str) -> int:
    match = re.match(r'PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?', duration_str or "")
    if not match:
        return 0
    hours = int(match.group(1) or 0)
    minutes = int(match.group(2) or 0)
    seconds = int(match.group(3) or 0)
    return hours * 3600 + minutes * 60 + seconds

def get_channel_id_from_handle(handle: str, log_events: list) -> str | None:
    # 1. Cek kamus statis permanen (0 kuota)
    if handle in PERMANENT_CHANNEL_IDS:
        return PERMANENT_CHANNEL_IDS[handle]

    # 2. Cek disk cache persisten di VM (0 kuota)
    if handle in DISK_CHANNEL_CACHE:
        return DISK_CHANNEL_CACHE[handle]

    # 3. Fallback: Hit API 1 kali saja, lalu simpan permanen ke disk
    url = "https://www.googleapis.com/youtube/v3/channels"
    params = {"part": "id", "forHandle": handle, "key": YOUTUBE_API_KEY}
    try:
        res = requests.get(url, params=params)
        if res.status_code == 200 and res.json().get("items"):
            c_id = res.json()["items"][0]["id"]
            DISK_CHANNEL_CACHE[handle] = c_id
            save_disk_channel_cache()
            increment_quota(1)
            return c_id
        else:
            log_events.append(f"      ⚠️ Gagal resolve channel ID untuk {handle} (Status: {res.status_code})")
    except Exception as e:
        log_events.append(f"      🚨 Error resolve handle {handle}: {e}")
    return None

def clean_team_name_for_search(name: str) -> str:
    cleaned = name.lower().strip()
    for pattern, aliases in CUSTOM_KEYWORD_ALIASES.items():
        if pattern == cleaned or pattern in cleaned:
            return aliases[0]
            
    # Buang prefiks angka awal (1. , 07 , dsb.)
    cleaned = re.sub(r'^\d+\.\s*', '', cleaned)
    
    # Buang suffix / frasa 'de madrid'
    cleaned = re.sub(r'\bde madrid\b', '', cleaned, flags=re.IGNORECASE)
    
    # Buang angka tahun/departemen (1913, 1899, 29, 07, 04, dsb.)
    cleaned = re.sub(r'\b(18\d{2}|19\d{2}|20\d{2}|\d{2})\b', '', cleaned)
    
    # Buang imbuhan umum klub lengkap (SS, AS, US, AC, BC, CFC, BSC, Olympique, TSG, VfB, VfL, 07, 04, dll)
    cleaned = re.sub(
        r'\b(Racing Club de|Racing de|SS|AS|US|AC|BC|CFC|BSC|Olympique|TSG|VfB|VfL|07|04|FC|CF|AFC|SSC|SV|OGC|RCD|Club|Stade|ES|AJ|SCO|ESTAC|Calcio|Fútbol|Futbol|Balompié|Balompie|RC|de|la|le)\b', 
        '', 
        cleaned, 
        flags=re.IGNORECASE
    )
    return re.sub(r'\s+', ' ', cleaned).strip()

def get_club_handle(team_name: str) -> str | None:
    cleaned = team_name.lower().strip()
    for key, handle in EUROPEAN_CLUB_HANDLES.items():
        if key in cleaned: 
            return handle
    return None

def get_geoblock_penalty(region_info: dict | None) -> tuple[int, str]:
    """
    Menghitung skor penalti pembatasan wilayah YouTube:
    - None / Global: penalti 0 (Terbuka 100% di 195 negara).
    - Blacklist (blocked): penalti len(blocked).
    - Whitelist (allowed): penalti 1000 - len(allowed) (Sangat tertutup).
    """
    if not region_info:
        return 0, "100% Global (Unrestricted)"
    blocked = region_info.get("blocked", [])
    if blocked:
        return len(blocked), f"Global kecuali {len(blocked)} negara (blocked: {blocked})"
    allowed = region_info.get("allowed", [])
    if allowed:
        return 1000 - len(allowed), f"Restricted Whitelist: {len(allowed)} negara (allowed: {allowed})"
    return 0, "100% Global (Unrestricted)"

def fallback_score_video(title: str, duration_sec: int, home_team: str, away_team: str, geo_penalty: int = 0) -> int:
    t = title.lower()
    score = 0
    if "extended" in t: score += 15
    elif any(w in t for w in ["highlight", "highlights", "resumen", "résumé", "sintesi", "zusammenfassung"]): score += 10
    elif "(" in t and ")" in t: score += 8

    h_clean = clean_team_name_for_search(home_team).lower()
    a_clean = clean_team_name_for_search(away_team).lower()
    if h_clean and h_clean in t: score += 6
    if a_clean and a_clean in t: score += 6

    # Skor bobot durasi highlight optimal (2m – 15m)
    if 120 <= duration_sec <= 900:
        score += 5
    elif MIN_DURATION_SECONDS <= duration_sec <= MAX_DURATION_SECONDS:
        score += 2

    # Bonus keterbukaan global
    if geo_penalty == 0:
        score += 3
    elif geo_penalty >= 900:
        score -= 50

    # Penalti konten sampingan / non-highlight
    if any(w in t for w in ["tous les buts", "all highlights", "inside", "press", "interview", "training", "previa", "preview", "reaction"]):
        score -= 30
    return score

# ==============================================================================
# 3. AI RERANKER WITH SMART FALLBACK (GEMINI 3.6 FLASH)
# ==============================================================================
def ai_pick_best_highlight(home_team: str, away_team: str, competition: str, video_candidates: list, log_events: list) -> str | None:
    if not video_candidates:
        return None

    if not ai_client:
        log_events.append("      ℹ️ AI Client belum aktif, menggunakan fallback rule-based.")
        return fallback_pick(home_team, away_team, video_candidates, log_events)

    prompt = f"""
Kamu adalah juri sistem verifikasi video sepak bola resmi.
Pertandingan: {home_team} vs {away_team} (Kompetisi: {competition})

Daftar kandidat video dari YouTube API:
{json.dumps([{'id': v['id'], 'title': v['title'], 'duration_sec': v['duration_sec']} for v in video_candidates], ensure_ascii=False, indent=2)}

Tugas:
Pilih SATU video yang merupakan highlight resmi pertandingan tersebut (extended highlights, match recap, resumen, atau cuplikan resmi laga ini).

Kriteria Penolakan (TOLAK jika):
1. Video Shorts, press conference, post-match interview, reaksi fans/reaction, training, behind-the-scenes (Inside Anfield, dsb), vlog.
2. Video kompilasi seluruh pekan / ringkasan semua gol pekanan (contoh: "Résumé 2ème journée", "Tous les buts de la journée").
3. Video pertandingan tim lain atau musim lain.

Balas HANYA format JSON valid berikut:
{{"chosen_id": "VIDEO_ID_YANG_LOLOS_ATAU_NULL", "reason": "Alasan singkat"}}
"""
    try:
        # Gunakan gemini-3.6-flash resmi yang aktif
        response = ai_client.models.generate_content(
            model='gemini-3.6-flash',
            contents=prompt,
            config=types.GenerateContentConfig(
                response_mime_type='application/json',
                temperature=0.1
            )
        )
        res_json = json.loads(response.text)
        chosen_id = res_json.get("chosen_id")
        reason = res_json.get("reason", "")
        
        valid_candidate_ids = {v["id"] for v in video_candidates}
        if chosen_id and chosen_id in valid_candidate_ids:
            matched_cand = next((v for v in video_candidates if v["id"] == chosen_id), None)
            c_title = matched_cand["title"] if matched_cand else "Unknown"
            c_dur = matched_cand.get("duration_sec", 0) if matched_cand else 0
            dur_str = f"{c_dur // 60}m {c_dur % 60}s" if c_dur else ""
            c_idx = (video_candidates.index(matched_cand) + 1) if matched_cand else "?"
            log_events.append(f"      🤖 [AI GEMINI MATCH] Terpilih Video #{c_idx}: '{c_title}'")
            log_events.append(f"         └─ ID: {chosen_id} (⏱️ {dur_str}) ➔ https://youtu.be/{chosen_id}")
            log_events.append(f"         └─ Alasan AI: {reason}")
            return chosen_id
        else:
            log_events.append(f"      🤖 [AI GEMINI REJECT] Tidak ada video yang cocok menurut AI. (Alasan: {reason})")
            return None
    except Exception as e:
        log_events.append(f"      🚨 [AI ERROR] {e}. Mengaktifkan fallback scoring.")
        return fallback_pick(home_team, away_team, video_candidates, log_events)

def fallback_pick(home_team: str, away_team: str, video_candidates: list, log_events: list) -> str | None:
    scored = []
    for c in video_candidates:
        geo_penalty = c.get("geo_penalty", 0)
        s = fallback_score_video(c["title"], c.get("duration_sec", 0), home_team, away_team, geo_penalty)
        if s > 0:
            scored.append((s, c))
    if scored:
        best = max(scored, key=lambda x: x[0])[1]
        b_dur = best.get("duration_sec", 0)
        dur_str = f"{b_dur // 60}m {b_dur % 60}s" if b_dur else ""
        log_events.append(f"      🛡️ [FALLBACK SELECT] Terpilih: '{best['title']}'")
        log_events.append(f"         └─ ID: {best['id']} (⏱️ {dur_str}) ➔ https://youtu.be/{best['id']}")
        return best["id"]
    return None

# ==============================================================================
# 4. YOUTUBE API SEARCH & DURATION FETCHER (90s – 1500s)
# ==============================================================================
def search_channel_for_highlight(channel_handle: str, query: str, home_team: str, away_team: str, match_date: str, comp_id: str, log_events: list) -> tuple[str | None, dict | None]:
    channel_id = get_channel_id_from_handle(channel_handle, log_events)
    if not channel_id: return None, None

    match_start = safe_parse_iso(match_date)
    pub_after = match_start.strftime("%Y-%m-%dT%H:%M:%SZ")
    pub_before = (match_start + timedelta(hours=72)).strftime("%Y-%m-%dT%H:%M:%SZ")

    log_events.append(f"      🔎 Hit API YouTube | Channel: {channel_handle} | Query: '{query}'")
        
    try:
        items = []
        for attempt in range(1, 4):
            try:
                req = youtube.search().list(
                    q=query,
                    channelId=channel_id,
                    part="snippet",
                    type="video",
                    videoEmbeddable="true",
                    publishedAfter=pub_after,
                    publishedBefore=pub_before,
                    order="relevance",
                    maxResults=5
                )
                res = req.execute()
                increment_quota(100)
                items = res.get("items", [])
                break
            except Exception as e:
                err_msg = str(e).lower()
                if ("timed out" in err_msg or "timeout" in err_msg or "connection" in err_msg or "reset" in err_msg) and attempt < 3:
                    log_events.append(f"      ⚠️ YouTube API timeout/koneksi (percobaan {attempt}/3). Mencoba ulang...")
                    time.sleep(2)
                    continue
                else:
                    log_events.append(f"      🚨 Error YouTube API: {e}")
                    return "ERROR", None

        if not items:
            log_events.append(f"      🚫 YouTube tidak menemukan hasil video apapun untuk query ini.")
            return None, None

        # Ambil durasi dan data regionRestriction via videos().list (1 unit kuota) dengan retry
        video_ids = [item["id"]["videoId"] for item in items]
        durations_map = {}
        regions_map = {}
        for attempt in range(1, 4):
            try:
                dur_req = youtube.videos().list(part="contentDetails", id=",".join(video_ids))
                dur_res = dur_req.execute()
                increment_quota(1)
                for v_item in dur_res.get("items", []):
                    c_details = v_item.get("contentDetails", {})
                    dur_sec = parse_iso8601_duration_seconds(c_details.get("duration", ""))
                    durations_map[v_item["id"]] = dur_sec
                    regions_map[v_item["id"]] = c_details.get("regionRestriction")
                break
            except Exception as e:
                err_msg = str(e).lower()
                if ("timed out" in err_msg or "timeout" in err_msg or "connection" in err_msg or "reset" in err_msg) and attempt < 3:
                    time.sleep(2)
                    continue
                else:
                    log_events.append(f"      🚨 Error fetch durasi/region video: {e}")
                    return "ERROR", None

        log_events.append(f"      📋 [DEBUG YOUTUBE] Ditemukan {len(items)} video mentah:")
        
        raw_candidates = []
        for idx, item in enumerate(items):
            v_id = item["id"]["videoId"]
            raw_title = html.unescape(item["snippet"]["title"])
            duration_sec = durations_map.get(v_id, 0)
            dur_text = f"{duration_sec // 60}m {duration_sec % 60}s"
            yt_link = f"https://youtu.be/{v_id}"
            reg_info = regions_map.get(v_id)
            geo_penalty, geo_desc = get_geoblock_penalty(reg_info)
            
            # 1. Filter kata terlarang murni di judul (Python title filter)
            is_forbidden, reason = check_forbidden_title(raw_title)
            if is_forbidden:
                log_events.append(f"         -> {idx+1}. [❌ TITLE FILTER: {reason}] [ID: {v_id}] '{raw_title}' ({yt_link})")
                continue

            # 2. Filter durasi paten: 90 detik s/d 1500 detik (1.5m – 25m)
            if not (MIN_DURATION_SECONDS <= duration_sec <= MAX_DURATION_SECONDS):
                log_events.append(f"         -> {idx+1}. [❌ DURATION FILTER: {dur_text}] [ID: {v_id}] '{raw_title}' ({yt_link})")
                continue

            # 3. Filter geoblocking ekstrem (whitelist negara tertutup, penalti >= 900)
            if geo_penalty >= 900:
                log_events.append(f"         -> {idx+1}. [❌ GEOBLOCK FILTER: {geo_desc}] [ID: {v_id}] '{raw_title}' ({yt_link})")
                continue

            geo_tag = " [🌐 100% Global]" if geo_penalty == 0 else f" [⚠️ {geo_desc}]"
            log_events.append(f"         -> {idx+1}. [⏱️ {dur_text}]{geo_tag} [ID: {v_id}] '{raw_title}' ({yt_link})")
            raw_candidates.append({
                "id": v_id, 
                "title": raw_title, 
                "duration_sec": duration_sec,
                "region_restriction": reg_info,
                "geo_penalty": geo_penalty
            })

        if not raw_candidates:
            log_events.append(f"      🚫 Semua video tereliminasi oleh filter judul, durasi, atau pembatasan wilayah global.")
            return None, None

        # Rerank kandidat video via Gemini AI (dengan Fallback Otomatis)
        chosen_video_id = ai_pick_best_highlight(home_team, away_team, comp_id, raw_candidates, log_events)
        if chosen_video_id:
            chosen_reg = next((c.get("region_restriction") for c in raw_candidates if c["id"] == chosen_video_id), None)
            return chosen_video_id, chosen_reg
        return None, None

    except Exception as e:
        log_events.append(f"      🚨 Error YouTube API: {e}")
        return "ERROR", None

def find_and_link_match_highlight(match, log_events):
    home, away, match_date, comp_id = match["home_team_name"], match["away_team_name"], match["utc_date"], match["competition_id"]
    target_channels = []

    # 1. Premier League & Kompetisi UEFA (CL & EL): Cross-Opponent Query ke Kanal Klub Resmi
    if comp_id in ["PL", "CL", "EL"]:
        h_handle = get_club_handle(home)
        a_handle = get_club_handle(away)
        
        h_clean = clean_team_name_for_search(home)
        a_clean = clean_team_name_for_search(away)
        
        if h_handle: 
            target_channels.append((h_handle, f'{a_clean} highlights'))
        if a_handle and a_handle != h_handle: 
            target_channels.append((a_handle, f'{h_clean} highlights'))
            
    # 2. Broadcaster & Liga Domestik Lainnya (LaLiga, Serie A, Bundesliga, Ligue 1)
    else:
        for b_handle in BROADCASTER_MAP.get(comp_id, []): 
            h_clean = clean_team_name_for_search(home)
            a_clean = clean_team_name_for_search(away)
                
            target_channels.append((b_handle, f'{h_clean} {a_clean}'))

    if not target_channels:
        log_events.append(f"   ⚠️ Channel tujuan untuk kompetisi {comp_id} ({home} vs {away}) tidak terdaftar.")
        return None, None

    has_network_error = False
    for handle, query in target_channels:
        if check_and_reset_quota() >= MAX_QUOTA_PER_DAY:
            log_events.append("   🚨 [LIMIT] Quota YouTube API harian habis! Skip antrean.")
            return "QUOTA_REACHED", None

        log_events.append(f"   -> Memeriksa channel: {handle}")
        video_id, region_info = search_channel_for_highlight(handle, query, home, away, match_date, comp_id, log_events)
        if video_id == "ERROR":
            has_network_error = True
            continue
        if video_id:
            return video_id, region_info

    if has_network_error:
        return "ERROR", None
    return None, None

# ==============================================================================
# 5. MATCH SYNCING LOGIC
# ==============================================================================
def sync_matches_from_football_data(is_full_sync=False, log_events=None):
    if log_events is None: log_events = []
    now_utc = datetime.now(timezone.utc)
    
    mode_text = "FULL SYNC (H-2 s/d H+7)" if is_full_sync else "LIVE SYNC (H-1 s/d H+1)"
    log_events.append(f"📡 Mode Sinkronisasi: {mode_text}")

    if is_full_sync:
        date_chunks = [(now_utc - timedelta(days=2), now_utc + timedelta(days=7))]
    else:
        date_chunks = [(now_utc - timedelta(days=1), now_utc + timedelta(days=1))]

    url = "https://api.football-data.org/v4/matches"
    headers = {"X-Auth-Token": FOOTBALL_DATA_API_KEY}
    payload_batch, api_match_ids = [], []

    for start_date, end_date in date_chunks:
        params = {"competitions": ",".join(TARGET_COMPETITIONS), "dateFrom": start_date.strftime("%Y-%m-%d"), "dateTo": end_date.strftime("%Y-%m-%d")}
        response = requests.get(url, headers=headers, params=params)
        
        if response.status_code == 200:
            matches_data = response.json().get("matches", [])
            log_events.append(f"📥 Menerima {len(matches_data)} data mentah dari API Football-Data (Range: {start_date.strftime('%Y-%m-%d')} s/d {end_date.strftime('%Y-%m-%d')})")
            
            for m in matches_data:
                comp_name = m.get("competition", {}).get("name", "Unknown")
                if comp_name == "Primera Division": comp_name = "LaLiga"
                    
                match_start = safe_parse_iso(m["utcDate"])
                elapsed_minutes = (now_utc - match_start).total_seconds() / 60
                
                api_status = m["status"]
                if api_status in ["IN_PLAY", "PAUSED"] and elapsed_minutes > 140:
                    api_status = "FINISHED"

                payload = {
                    "id": m["id"], "competition_id": m.get("competition", {}).get("code", "OTHER"), "competition_name": comp_name,
                    "utc_date": m["utcDate"], "status": api_status, "matchday": m.get("matchday"),
                    "home_team_id": m.get("homeTeam", {}).get("id"), "home_team_name": m.get("homeTeam", {}).get("name", ""), "home_team_crest": m.get("homeTeam", {}).get("crest"),
                    "away_team_id": m.get("awayTeam", {}).get("id"), "away_team_name": m.get("awayTeam", {}).get("name", ""), "away_team_crest": m.get("awayTeam", {}).get("crest"),
                    "home_score": m.get("score", {}).get("fullTime", {}).get("home"), "away_score": m.get("score", {}).get("fullTime", {}).get("away"),
                    "updated_at": now_utc.isoformat()
                }
                payload_batch.append(payload)
                api_match_ids.append(m["id"])
        else:
            log_events.append(f"🚨 Gagal mengambil data API Football-Data. Status code: {response.status_code}")

    unique_payloads = list({p['id']: p for p in payload_batch}.values())
    
    if unique_payloads:
        try:
            old_data_map = {}
            for i in range(0, len(api_match_ids), 50):
                res_db = supabase.table("matches").select("id, status, home_score, away_score").in_("id", api_match_ids[i:i+50]).execute()
                for row in res_db.data: old_data_map[row["id"]] = row

            changes_found = False
            for p in unique_payloads:
                old = old_data_map.get(p["id"])
                h_team, a_team = p["home_team_name"], p["away_team_name"]
                
                if old:
                    is_regression = False
                    if old["status"] in ["FINISHED", "AWARDED"] and p["status"] not in ["FINISHED", "AWARDED"]: is_regression = True
                    elif old["status"] in ["IN_PLAY", "PAUSED"] and p["status"] in ["TIMED", "SCHEDULED"]: is_regression = True

                    if is_regression:
                        log_events.append(f"🛡️ [BLOCKED REGRESSION] {h_team} vs {a_team} tetap {old['status']}")
                        p["status"] = old["status"]
                        if p.get("home_score") is None: p["home_score"] = old.get("home_score")
                        if p.get("away_score") is None: p["away_score"] = old.get("away_score")
                    
                    elif old["status"] != p["status"]:
                        if p["status"] in ["FINISHED", "AWARDED"]:
                            log_events.append(f"🏁 [FULL TIME] {h_team} {p.get('home_score') or 0} - {p.get('away_score') or 0} {a_team}")
                        else:
                            log_events.append(f"🔄 [STATUS] {h_team} vs {a_team} | {old['status']} ➔ {p['status']}")
                        changes_found = True

                    if p["status"] in ["IN_PLAY", "PAUSED", "FINISHED"]:
                        h_score_old, a_score_old = old.get("home_score") or 0, old.get("away_score") or 0
                        h_score_new, a_score_new = p.get("home_score") or 0, p.get("away_score") or 0
                        
                        if h_score_old != h_score_new or a_score_old != a_score_new:
                            log_events.append(f"⚽ [SKOR!] {h_team} {h_score_new} - {a_score_new} {a_team}")
                            changes_found = True
                else:
                    changes_found = True
                    log_events.append(f"✨ [NEW MATCH] Ditemukan jadwal baru: {h_team} vs {a_team}")
            
            if not changes_found: log_events.append("⚪ Tidak ada perubahan skor atau status.")
            supabase.table("matches").upsert(unique_payloads, on_conflict="id").execute()
            log_events.append(f"✅ Berhasil sinkronisasi {len(unique_payloads)} jadwal ke Supabase.")
        except Exception as e: log_events.append(f"🚨 [ERROR DATABASE] {e}")

# ==============================================================================
# 6. GLOBAL SCAN SMART YOUTUBE ENGINE & OPPORTUNISTIC BURNER
# ==============================================================================
def sync_targeted_highlights(log_events=None):
    if log_events is None: log_events = []
    
    res = supabase.table("matches") \
        .select("id, home_team_name, away_team_name, competition_id, utc_date, last_youtube_check") \
        .eq("status", "FINISHED") \
        .is_("highlight_video_id", "null") \
        .order("last_youtube_check", desc=False, nullsfirst=True) \
        .order("utc_date", desc=True) \
        .execute()

    unlinked_matches = res.data
    if not unlinked_matches:
        log_events.append("⚪ YouTube: Semua match FINISHED sudah memiliki video highlight.")
        return

    now_utc = datetime.now(timezone.utc)
    next_reset_utc = get_next_quota_reset_time_utc()
    hours_until_reset = (next_reset_utc - now_utc).total_seconds() / 3600
    
    current_quota = check_and_reset_quota()
    remaining_quota = MAX_QUOTA_PER_DAY - current_quota
    
    log_events.append(f"🔍 YouTube Engine: Memindai {len(unlinked_matches)} antrean match (Sisa Quota: {remaining_quota}, Reset PT dalam: {hours_until_reset:.1f} jam):")

    processed_count = 0
    skipped_count = 0

    batch_matches = unlinked_matches[:10]

    for match in batch_matches:
        home, away = match["home_team_name"], match["away_team_name"]
        comp_id = match.get("competition_id")
        match_start = safe_parse_iso(match["utc_date"])
        match_age_hours = (now_utc - match_start).total_seconds() / 3600
        
        # Embargo window untuk kompetisi UEFA (CL & EL): Laga < 2.5 jam belum dirilis oleh media klub
        if comp_id in ["CL", "EL"] and match_age_hours < 2.5:
            skipped_count += 1
            log_events.append(f"   ⏳ [SKIP EMBARGO] {home} vs {away} ➔ Alasan: Laga baru usai {match_age_hours:.1f} jam lalu (Video resmi klub UEFA belum rilis). Menunggu H+2.5 jam.")
            continue

        last_check = match.get("last_youtube_check")
        if last_check:
            last_check_dt = safe_parse_iso(last_check)
            hours_since_check = (now_utc - last_check_dt).total_seconds() / 3600
        else:
            hours_since_check = float('inf')

        # HITUNG COOLDOWN TIER
        required_cooldown_hours = 0
        skip_reason = None
        if match_age_hours < 4:
            required_cooldown_hours = 4 - match_age_hours
            skip_reason = f"Inkubasi (< 4 jam, umur {match_age_hours:.1f} jam)"
        elif 4 <= match_age_hours <= 24 and hours_since_check < 4:
            required_cooldown_hours = 4 - hours_since_check
            skip_reason = f"Tier 1 Cooldown (Baru dicek {hours_since_check:.1f} jam lalu)"
        elif 24 < match_age_hours <= 48 and hours_since_check < 12:
            required_cooldown_hours = 12 - hours_since_check
            skip_reason = f"Tier 2 Cooldown (Baru dicek {hours_since_check:.1f} jam lalu)"
        elif match_age_hours > 48 and hours_since_check < 24:
            required_cooldown_hours = 24 - hours_since_check
            skip_reason = f"Tier 3 Cooldown (Baru dicek {hours_since_check:.1f} jam lalu)"

        # SMART GLOBAL CHECK
        force_execute = False
        if skip_reason and remaining_quota >= 500:
            if required_cooldown_hours >= hours_until_reset and not (comp_id in ["CL", "EL"] and match_age_hours < 2.5):
                force_execute = True
                log_events.append(f"   ⚡ [OPPORTUNISTIC HIT] {home} vs {away}: Cooldown ({required_cooldown_hours:.1f} jam) melewati waktu reset ({hours_until_reset:.1f} jam). Memaksa pencarian!")

        if skip_reason and not force_execute:
            skipped_count += 1
            log_events.append(f"   ⏳ [SKIP] {home} vs {away} ➔ Alasan: {skip_reason}")
            continue
            
        processed_count += 1
        if not force_execute:
            log_events.append(f"   🎥 [PROSES] Mencari video untuk: {home} vs {away} (Umur Match: {match_age_hours:.1f} jam)")
        
        video_id, region_info = find_and_link_match_highlight(match, log_events)
        
        if video_id == "QUOTA_REACHED":
            log_events.append(f"   ⏸️ [LIMIT] Kuota harian habis. Eksekusi dihentikan.")
            break
        elif video_id == "ERROR":
            log_events.append(f"   ⚠️ [SKIP COOLDOWN] {home} vs {away} mengalami timeout jaringan. Cooldown tidak dicatat agar dicoba lagi pada siklus berikutnya.")
            continue

        if video_id:
            geo_text = " [🌐 100% Global]" if not region_info else f" [⚠️ {region_info}]"
            log_events.append(f"   ✅ [BERHASIL LINK] {home} vs {away} ➔ https://youtu.be/{video_id} (ID: {video_id}){geo_text} disimpan ke Supabase.")
        else:
            log_events.append(f"   ❌ [NIHIL] Video resmi belum tersedia.")

        # Simpan ke Supabase dengan aman (mendukung kolom region_restriction jika sudah dibuat)
        try:
            update_data = {"last_youtube_check": now_utc.isoformat()}
            if video_id: 
                update_data["highlight_video_id"] = video_id
                update_data["region_restriction"] = region_info
            supabase.table("matches").update(update_data).eq("id", match["id"]).execute()
        except Exception as e:
            # Fallback jika kolom region_restriction belum dibuat di Supabase
            if "region_restriction" in str(e).lower() or "42703" in str(e):
                fallback_update = {"last_youtube_check": now_utc.isoformat()}
                if video_id: fallback_update["highlight_video_id"] = video_id
                supabase.table("matches").update(fallback_update).eq("id", match["id"]).execute()
            else:
                raise e

    log_events.append(f"📊 Ringkasan YouTube: {processed_count} diproses, {skipped_count} dilewati.")

def cleanup_old_data(log_events=None):
    if log_events is None: log_events = []
    cutoff_date = (datetime.now(timezone.utc) - timedelta(days=15)).isoformat()
    try: 
        res = supabase.table("matches").delete().lt("utc_date", cutoff_date).execute()
        if res.data: 
            log_events.append(f"🧹 Menghapus {len(res.data)} pertandingan kadaluarsa (>15 hari).")
        else: 
            log_events.append("⚪ Database bersih (Tidak ada data >15 hari).")
    except Exception as e: 
        log_events.append(f"🚨 [ERROR CLEANUP] {e}")

# ==============================================================================
# 7. DYNAMIC SMART COUNTDOWN SCHEDULER
# ==============================================================================
def get_dynamic_sleep_info():
    res_live = supabase.table("matches").select("id").in_("status", ["IN_PLAY", "PAUSED"]).limit(1).execute()
    if res_live.data:
        return 60, "🔥 ADA MATCH LIVE! Interval 1 MENIT."
    
    now_utc_iso = datetime.now(timezone.utc).isoformat()
    res_next = supabase.table("matches") \
        .select("utc_date") \
        .eq("status", "TIMED") \
        .gte("utc_date", now_utc_iso) \
        .order("utc_date", desc=False) \
        .limit(1) \
        .execute()
    
    if res_next.data:
        next_match_time = safe_parse_iso(res_next.data[0]["utc_date"])
        seconds_to_start = (next_match_time - datetime.now(timezone.utc)).total_seconds()
        
        if seconds_to_start <= 1800:
            return 120, f"⏳ KICK-OFF SEBENTAR LAGI ({int(seconds_to_start // 60)} menit). Interval 2 MENIT."
        elif seconds_to_start <= 7200:
            return 600, "⏳ MATCH HARI INI (< 2 JAM). Interval 10 MENIT."
        else:
            return 1800, "⏳ JADWAL AMAN. Interval 30 MENIT."
            
    return 7200, "😴 OFF-PEAK / TIDAK ADA JADWAL. Tidur 2 JAM."

# ==============================================================================
# MAIN WORKER LOOP
# ==============================================================================
if __name__ == "__main__":
    print("\n" + "="*60)
    print("    ⚽ FOOTBALL CLIPS SMART WORKER 4.2 (PRECISION & RESILIENT AI) ⚽")
    print("="*60)
    
    last_full_sync_time = None
    
    while True:
        cycle_start_wib = get_current_wib_time()
        step1_events, step2_events, step3_events = [], [], []
        is_full = False
        
        try:
            now_utc = datetime.now(timezone.utc)
            if last_full_sync_time is None or (now_utc - last_full_sync_time).total_seconds() >= 43200:
                is_full, last_full_sync_time = True, now_utc
                sync_matches_from_football_data(True, step1_events)
            else:
                sync_matches_from_football_data(False, step1_events)

            sync_targeted_highlights(step2_events)
            cleanup_old_data(step3_events)

        except Exception as e:
            step3_events.append(f"🚨 [CRITICAL ERROR] {e}")
            
        sleep_seconds, sleep_desc = get_dynamic_sleep_info()
        
        cycle_record = {
            "timestamp": cycle_start_wib.strftime("%Y-%m-%d %H:%M:%S WIB"),
            "mode": "FULL SYNC" if is_full else "LIVE SYNC",
            "next_sleep": f"{sleep_seconds // 60} Menit ({sleep_desc})",
            "steps": [
                {"title": "1. Football Data", "status": "OK", "events": step1_events},
                {"title": "2. YouTube & AI Engine", "status": "WARN" if any("🚨" in x for x in step2_events) else "OK", "events": step2_events},
                {"title": "3. Maintenance", "status": "OK", "events": step3_events}
            ]
        }
        save_cycle_log(cycle_record)
        print(f"🚀 Siklus selesai. Tidur {sleep_seconds} detik ({sleep_desc})...")
        time.sleep(sleep_seconds)