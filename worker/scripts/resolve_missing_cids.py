import sys, os
sys.stdout.reconfigure(encoding='utf-8')
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
import yt_dlp
from worker.clients.database_client import db_client

clubs_to_search = [
    ('Platense', 'Club Atlético Platense Oficial'),
    ('Boca Juniors', 'Boca Juniors Oficial'),
    ('Racing Club', 'Racing Club Oficial'),
    ('San Lorenzo', 'San Lorenzo de Almagro Oficial'),
    ('Vélez Sarsfield', 'Vélez Sarsfield Oficial'),
    ('Independiente', 'Club Atlético Independiente Oficial'),
    ("Newell's Old Boys", "Newell's Old Boys Oficial"),
    ('Belgrano (Córdoba)', 'Club Atlético Belgrano Oficial'),
    ('Vasco da Gama', 'Vasco TV Oficial'),
    ('Palmeiras', 'TV Palmeiras Fam Oficial'),
    ('Corinthians', 'Corinthians TV Oficial'),
    ('São Paulo', 'São Paulo FC SPFC TV Oficial'),
    ('Internacional', 'Sport Club Internacional TV Inter Oficial'),
    ('Atlético Mineiro', 'Galo TV Atlético Mineiro Oficial'),
    ('Botafogo', 'Botafogo TV Oficial'),
    ('Cruzeiro', 'Cruzeiro Oficial TV'),
    ('AGF', 'AGF Fodbold'),
    ("Inter D'Escaldes", "Inter Club d'Escaldes"),
    ('Jablonec', 'FK Jablonec'),
]

ydl_opts = {
    'quiet': True,
    'extract_flat': True,
    'skip_download': True,
}

resolved = {}
with yt_dlp.YoutubeDL(ydl_opts) as ydl:
    for name, search_query in clubs_to_search:
        try:
            res = ydl.extract_info(f"ytsearch1:{search_query} youtube channel", download=False)
            entries = res.get('entries') or []
            if entries:
                first = entries[0]
                ch_id = first.get('channel_id')
                ch_url = first.get('channel_url')
                uploader = first.get('uploader')
                if ch_id and ch_id.startswith('UC') and len(ch_id) == 24:
                    resolved[name] = ch_id
                    print(f"[FOUND] {name:20} -> {ch_id} (via {uploader})")
                else:
                    print(f"[FAIL]  {name:20} -> No valid channel_id in result")
            else:
                print(f"[NONE]  {name:20} -> No entries")
        except Exception as e:
            print(f"[ERROR] {name:20} -> {e}")

print(f"\nResolved {len(resolved)}/{len(clubs_to_search)} channels. Updating database...")
for name, cid in resolved.items():
    try:
        db_client.table('supported_clubs').update({
            'youtube_channel_id': cid
        }).eq('name', name).execute()
        print(f"  [UPDATED DB] {name} -> {cid}")
    except Exception as e:
        print(f"  [DB ERROR] {name}: {e}")
