import os
import sys
import json
import time
import argparse
import requests
import jwt

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

PACKAGE_NAME = "com.dirzaaulia.footballclips"
KEY_FILE = "fastlane/play-console-key.json"

APP_NAME = "Football Highlights & Clips"

# --- SHORT DESCRIPTIONS (Max 80 chars) ---
SHORT_DESC_EN = "Watch official football highlights, video clips, live scores, and fixtures!"
SHORT_DESC_ID = "Nonton highlight sepak bola resmi, cuplikan gol, skor live, & jadwal liga!"
SHORT_DESC_ES = "¡Mira resúmenes de fútbol en video, goles, marcadores en vivo y calendarios!"
SHORT_DESC_PT = "Veja os melhores momentos do futebol, vídeos de gols, placar ao vivo e jogos!"

# --- FULL DESCRIPTIONS ---
FULL_DESC_EN = """Welcome to Football Highlights & Clips (FC) – your ultimate companion for football highlights, match clips, live scores, and fixtures!

Catch the best moments from the top football leagues and competitions around the world. Never miss a goal, dramatic comeback, or match-winning play.

⚡ KEY FEATURES:

• 📺 Official Match Highlights & Clips
Watch high-quality match recaps and best plays sourced and embedded directly via official broadcaster channels using official YouTube Player APIs.

• ⏱️ Real-Time Live Scores & Schedules
Stay updated with match fixtures, kickoff times, live scores, and results across major leagues.

• 🔍 Multi-League Filtering & Search
Quickly find your favorite teams and filter by competition: Premier League, La Liga, Serie A, Bundesliga, Champions League, and more.

• ☁️ Cross-Device Experience
Sign in with Google to seamlessly sync your profile and access Football Clips on both Android and Web (fc.dirzaaulia.com).

• 🚫 Ad-Free Premium (Optional)
Enjoy an uninterrupted, pure football experience with an optional lifetime Remove Ads upgrade.

---
IMPORTANT DISCLAIMERS & LEGAL:
1. Football Highlights & Clips does not host, upload, or broadcast copyrighted video files. All video highlights are embedded directly from official verified channels via the official YouTube Player API in full compliance with YouTube Terms of Service.
2. All team names, logos, trademarks, and league assets belong to their respective copyright owners.

• Privacy Policy: https://fc.dirzaaulia.com/privacy
• Terms of Service: https://fc.dirzaaulia.com/tnc
• Support: dirzaaulia@gmail.com
"""

FULL_DESC_ID = """Selamat datang di Football Highlights & Clips (FC) – aplikasi lengkap untuk cuplikan video pertandingan sepak bola, skor langsung, dan jadwal pertandingan!

Saksikan momen-momen terbaik dari liga dan turnamen sepak bola terkemuka di dunia. Jangan lewatkan setiap gol spektakuler, aksi dramatis, dan hasil pertandingan tim favorit Anda.

⚡ FITUR UTAMA:

• 📺 Cuplikan Pertandingan Resmi
Tonton rangkuman video pertandingan resmi berkualitas tinggi yang disematkan langsung dari saluran penyiar resmi menggunakan YouTube Player API resmi.

• ⏱️ Live Skor & Jadwal Pertandingan
Dapatkan pembaruan langsung jadwal kick-off, skor live, dan hasil akhir pertandingan liga-liga top dunia secara real-time.

• 🔍 Filter Liga & Pencarian Cepat
Temukan klub favorit Anda dengan mudah melalui filter kompetisi: Premier League, La Liga, Serie A, Bundesliga, Liga Champions, dan lainnya.

• ☁️ Sinkronisasi Lintas Perangkat
Masuk dengan Akun Google untuk menyinkronkan profil Anda antara aplikasi Android dan Web (fc.dirzaaulia.com).

• 🚫 Opsi Bebas Iklan (Premium)
Nikmati pengalaman menonton sepak bola tanpa jeda dengan opsi pembelian satu kali Hapus Iklan.

---
INFORMASI HAK CIPTA & KEPATUHAN:
1. Football Highlights & Clips tidak menyimpan, mengunggah, atau menyiarkan file video ilegal/bajakan. Semua cuplikan video disematkan langsung dari saluran resmi yang terverifikasi menggunakan YouTube Player API resmi sesuai dengan Ketentuan Layanan YouTube.
2. Semua nama tim, logo, merek dagang, dan aset kompetisi adalah milik pemegang hak cipta masing-masing.

• Kebijakan Privasi: https://fc.dirzaaulia.com/privacy
• Syarat & Ketentuan: https://fc.dirzaaulia.com/tnc
• Kontak & Bantuan: dirzaaulia@gmail.com
"""

FULL_DESC_ES = """¡Bienvenido a Football Highlights & Clips (FC) – tu aplicación definitiva para ver resúmenes de fútbol, videos de goles, marcadores en vivo y calendarios de partidos!

Disfruta de los mejores momentos y las jugadas más destacadas de las mejores ligas y torneos del mundo. No te pierdas ningún gol, remontada histórica ni jugada decisiva.

⚡ CARACTERÍSTICAS PRINCIPALES:

• 📺 Resúmenes Oficiales y Videos de Goles
Mira resúmenes oficiales de partidos en alta calidad transmitidos directamente desde los canales oficiales mediante la API oficial de YouTube Player.

• ⏱️ Marcadores y Resultados en Vivo
Sigue los resultados en tiempo real, horarios de inicio, marcadores al instante y calendarios de partidos de las principales ligas.

• 🔍 Filtro por Ligas y Búsqueda Rápida
Encuentra fácilmente a tus clubes favoritos filtrando por torneo: LaLiga, Premier League, Serie A, Bundesliga, Champions League y más.

• ☁️ Sincronización Multiplataforma
Inicia sesión con tu cuenta de Google para sincronizar tu perfil entre Android y la versión web (fc.dirzaaulia.com).

• 🚫 Experiencia Premium Sin Publicidad (Opcional)
Disfruta del fútbol sin interrupciones con una actualización opcional de pago único para eliminar anuncios de por vida.

---
INFORMACIÓN LEGAL Y AVISO DE DERECHOS DE AUTOR:
1. Football Highlights & Clips no aloja, transmite ni almacena archivos de video ilegales o con derechos de autor. Todos los resúmenes y jugadas se reproducen e integran directamente desde canales verificados a través de la API oficial de YouTube Player, cumpliendo estrictamente con los Términos de Servicio de YouTube.
2. Todos los nombres de equipos, escudos, marcas registradas y derechos de torneos pertenecen a sus respectivos propietarios.

• Política de Privacidad: https://fc.dirzaaulia.com/privacy
• Términos de Servicio: https://fc.dirzaaulia.com/tnc
• Soporte y Contacto: dirzaaulia@gmail.com
"""

FULL_DESC_PT = """Bem-vindo ao Football Highlights & Clips (FC) – seu aplicativo definitivo para assistir aos melhores momentos do futebol, vídeos de lances, placar ao vivo e calendário de jogos!

Acompanhe os lances mais emocionantes das principais ligas e torneios de futebol do mundo. Não perca nenhum gol espetacular, virada histórica ou resultado do seu time do coração.

⚡ RECURSOS PRINCIPAIS:

• 📺 Melhores Momentos Oficiais e Vídeos de Gols
Assista a vídeos de melhores momentos oficiais com qualidade HD, incorporados diretamente dos canais oficiais por meio da API oficial do YouTube Player.

• ⏱️ Placar ao Vivo e Tabela de Jogos
Fique por dentro dos resultados em tempo real, horários de início de partidas, placar ao vivo e calendário completo dos maiores campeonatos.

• 🔍 Filtro de Ligas e Busca Rápida
Encontre seu clube favorito com facilidade filtrando por competição: Champions League, Premier League, LaLiga, Serie A, Bundesliga e muito mais.

• ☁️ Sincronização em Múltiplos Dispositivos
Faça login com sua Conta Google para sincronizar suas preferências entre o aplicativo Android e a versão Web (fc.dirzaaulia.com).

• 🚫 Versão Premium Sem Anúncios (Opcional)
Aproveite o futebol com foco total e sem interrupções com o upgrade vitalício opcional para Remover Anúncios.

---
INFORMAÇÕES LEGAIS E DIREITOS AUTORAIS:
1. O Football Highlights & Clips não hospeda, transmite nem faz upload de arquivos de vídeo protegidos por direitos autorais. Todos os vídeos e melhores momentos são exibidos diretamente de canais oficiais e verificados através da API oficial do YouTube Player, em total conformidade com os Termos de Serviço do YouTube.
2. Todos os nomes de equipes, escudos, marcas registradas e campeonatos pertencem aos seus respectivos proprietários.

• Política de Privacidade: https://fc.dirzaaulia.com/privacy
• Termos de Serviço: https://fc.dirzaaulia.com/tnc
• Suporte e Contato: dirzaaulia@gmail.com
"""

RELEASE_NOTES = [
    {
        "language": "id",
        "text": """🎉 Football Clips hadir dengan pembaruan besar yang makin seru & ngebut!

✨ Desain M3 Expressive: Tampilan lebih estetik dengan animasi droplet ganti tema yang super mulus!
⚡ Cuplikan & Skor Real-Time: Tonton highlight video resmi & skor langsung liga top dunia lebih instan tanpa lag.
🧭 Floating Bar Modern: Jelajah jadwal pertandingan & klub favorit makin mudah dan asyik.
🛠️ Performa Lebih Gesit: Optimasi sistem & perbaikan bug untuk kenyamanan Anda. Enjoy the game! ⚽🔥"""
    },
    {
        "language": "en-US",
        "text": """🎉 Football Clips is back with a massive, exciting update!

✨ Fresh M3 Expressive UI: Sleek modern look with an ultra-smooth droplet theme transition!
⚡ Instant Highlights & Scores: Watch official HD match recaps and real-time scores across top leagues with zero lag.
🧭 Sleek Floating Bar: Effortlessly browse fixtures and your favorite clubs.
🛠️ Faster & Smoother: Speed boosts and stability fixes for the ultimate matchday experience. Enjoy the game! ⚽🔥"""
    },
    {
        "language": "es-419",
        "text": """🎉 ¡Football Clips llega con una gran actualización más rápida y emocionante!

✨ Diseño M3 Expressive: ¡Aspecto elegante con animación fluida de gota para cambiar de tema!
⚡ Resúmenes y Marcadores al Instante: Mira los mejores momentos oficiales en HD y sigue marcadores en vivo sin demoras.
🧭 Barra Flotante Moderna: Navega fácilmente por partidos y ligas favoritas.
🛠️ Rendimiento Mejorado: Corrección de errores y optimización de velocidad. ¡Disfruta del fútbol! ⚽🔥"""
    },
    {
        "language": "es-ES",
        "text": """🎉 ¡Football Clips llega con una gran actualización más rápida y emocionante!

✨ Diseño M3 Expressive: ¡Aspecto elegante con animación fluida de gota para cambiar de tema!
⚡ Resúmenes y Marcadores al Instante: Mira los mejores momentos oficiales en HD y sigue marcadores en vivo sin demoras.
🧭 Barra Flotante Moderna: Navega fácilmente por partidos y ligas favoritas.
🛠️ Rendimiento Mejorado: Corrección de errores y optimización de velocidad. ¡Disfruta del fútbol! ⚽🔥"""
    },
    {
        "language": "pt-BR",
        "text": """🎉 O Football Clips está de volta com uma super atualização mais rápida e moderna!

✨ Novo Design M3 Expressive: Visual elegante com transição suave em gota para trocar de tema!
⚡ Melhores Momentos e Placar ao Vivo: Assista a lances em HD e acompanhe resultados em tempo real sem lentidão.
🧭 Barra Flutuante Moderna: Navegue facilmente pelos jogos e ligas favoritas.
🛠️ Desempenho Otimizado: Correção de bugs e mais velocidade. Aproveite o futebol! ⚽🔥"""
    }
]

LISTINGS = [
    {
        "lang": "en-US",
        "title": APP_NAME,
        "short": SHORT_DESC_EN,
        "full": FULL_DESC_EN
    },
    {
        "lang": "id",
        "title": APP_NAME,
        "short": SHORT_DESC_ID,
        "full": FULL_DESC_ID
    },
    {
        "lang": "es-419",
        "title": APP_NAME,
        "short": SHORT_DESC_ES,
        "full": FULL_DESC_ES
    },
    {
        "lang": "es-ES",
        "title": APP_NAME,
        "short": SHORT_DESC_ES,
        "full": FULL_DESC_ES
    },
    {
        "lang": "pt-BR",
        "title": APP_NAME,
        "short": SHORT_DESC_PT,
        "full": FULL_DESC_PT
    }
]

SCREENSHOT_FILES = [
    "screenshots/store_listing/01_highlights_banner.png",
    "screenshots/store_listing/02_fixtures_banner.png",
    "screenshots/store_listing/03_leagues_banner.png",
    "screenshots/store_listing/04_premium_banner.png",
]

def get_access_token():
    with open(KEY_FILE, "r") as f:
        key_data = json.load(f)
        
    iat = int(time.time())
    payload = {
        "iss": key_data["client_email"],
        "sub": key_data["client_email"],
        "aud": "https://oauth2.googleapis.com/token",
        "iat": iat,
        "exp": iat + 3600,
        "scope": "https://www.googleapis.com/auth/androidpublisher"
    }
    encoded_jwt = jwt.encode(payload, key_data["private_key"], algorithm="RS256")
    resp = requests.post("https://oauth2.googleapis.com/token", data={
        "grant_type": "urn:ietf:params:oauth:grant-type:jwt-bearer",
        "assertion": encoded_jwt
    })
    return resp.json()["access_token"]

def main():
    parser = argparse.ArgumentParser(description="Update Multilingual Store Listings on Google Play Console.")
    parser.add_argument("--dry-run", action="store_true", help="Validate without committing")
    args = parser.parse_args()

    print("==================================================")
    print("  Google Play Multilingual Listing Updater")
    print(f"  Package: {PACKAGE_NAME}")
    print(f"  Title: {APP_NAME}")
    print(f"  Languages: EN, ID, ES (LatAm), ES (Spain), PT (Brazil)")
    print(f"  Dry Run: {'YES' if args.dry_run else 'NO (LIVE UPDATE)'}")
    print("==================================================")

    token = get_access_token()
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }

    # 1. Create Edit Session
    print("\n[1/4] Creating edit session...")
    edit_resp = requests.post(
        f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits",
        headers=headers
    )
    edit_id = edit_resp.json()["id"]
    print(f"  Active Edit ID: {edit_id}")

    try:
        # 2. Update Listings (Text Metadata)
        print("\n[2/4] Updating store listings for all languages...")
        for item in LISTINGS:
            lang = item["lang"]
            payload = {
                "language": lang,
                "title": item["title"],
                "shortDescription": item["short"],
                "fullDescription": item["full"]
            }
            res = requests.put(
                f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}/listings/{lang}",
                headers=headers,
                json=payload
            )
            if res.status_code in (200, 201):
                print(f"  [OK] Listing '{lang}' updated successfully.")
            else:
                print(f"  [FAIL] Listing '{lang}' failed: {res.status_code} {res.text}")

        # 3. Upload App Icon (512x512)
        icon_path = "app/src/androidMain/ic_launcher-playstore.png"
        if os.path.exists(icon_path):
            print("\n[3/5] Uploading official app icon (512x512)...")
            for item in LISTINGS:
                lang = item["lang"]
                with open(icon_path, "rb") as icon_file:
                    icon_headers = {
                        "Authorization": f"Bearer {token}",
                        "Content-Type": "image/png"
                    }
                    icon_url = (
                        f"https://androidpublisher.googleapis.com/upload/androidpublisher/v3/applications/"
                        f"{PACKAGE_NAME}/edits/{edit_id}/listings/{lang}/icon"
                    )
                    icon_res = requests.post(icon_url, headers=icon_headers, data=icon_file)
                    if icon_res.status_code in (200, 201):
                        print(f"  [OK] Uploaded app icon for '{lang}' successfully.")
                    else:
                        print(f"  [FAIL] Failed to upload app icon for '{lang}': {icon_res.status_code} {icon_res.text}")

        # 4. Upload Screenshots for all languages
        print("\n[4/5] Uploading promotional screenshots...")
        for item in LISTINGS:
            lang = item["lang"]
            # Clear old screenshots
            requests.delete(
                f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}/listings/{lang}/phoneScreenshots",
                headers=headers
            )
            uploaded_count = 0
            for idx, img_path in enumerate(SCREENSHOT_FILES, 1):
                if not os.path.exists(img_path):
                    continue
                with open(img_path, "rb") as img_file:
                    up_headers = {
                        "Authorization": f"Bearer {token}",
                        "Content-Type": "image/png"
                    }
                    img_url = (
                        f"https://androidpublisher.googleapis.com/upload/androidpublisher/v3/applications/"
                        f"{PACKAGE_NAME}/edits/{edit_id}/listings/{lang}/phoneScreenshots"
                    )
                    up_res = requests.post(img_url, headers=up_headers, data=img_file)
                    if up_res.status_code in (200, 201):
                        uploaded_count += 1
            print(f"  [OK] Uploaded {uploaded_count} screenshots for '{lang}'.")

        # 5. Update Release Notes on Production Track
        print("\n[5/5] Updating release notes on Production Track...")
        track_url = f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}/tracks/production"
        track_resp = requests.get(track_url, headers=headers)
        if track_resp.status_code == 200:
            track_data = track_resp.json()
            releases = track_data.get("releases", [])
            if releases:
                releases[0]["releaseNotes"] = RELEASE_NOTES
                track_payload = {
                    "track": "production",
                    "releases": releases
                }
                requests.put(track_url, headers=headers, json=track_payload)
                print("  [OK] Production track release notes updated.")

        # 5. Commit or Validate
        if args.dry_run:
            print("\nValidating edit session (Dry Run)...")
            val_res = requests.post(f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}:validate", headers=headers)
            if val_res.status_code == 200:
                print("  [SUCCESS] All listings and screenshots passed Google Play validation!")
            else:
                print(f"  [FAIL] Validation returned: {val_res.status_code} {val_res.text}")
            requests.delete(f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}", headers=headers)
            print("  Cleaned up edit session.")
        else:
            print("\nCommitting changes to Google Play Console...")
            commit_res = requests.post(f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}:commit", headers=headers)
            if commit_res.status_code == 200:
                print("  [SUCCESS] All multilingual listings, screenshots, and release notes committed LIVE!")
            else:
                print(f"  [FAIL] Commit failed: {commit_res.status_code} {commit_res.text}")

    except Exception as e:
        print("Exception occurred:", e)
        try:
            requests.delete(f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}", headers=headers)
        except:
            pass

if __name__ == "__main__":
    main()
