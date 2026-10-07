try:
    from google import genai
    from google.genai import types
except ImportError:
    genai = None
    types = None

import json

from loguru import logger
from worker.config.settings import settings


class GeminiClient:
    MODELS_TO_TRY = [
        "gemini-3.8-flash",
        "gemini-3.7-flash",
        "gemini-3.5-flash-lite",
        "gemini-3.6-flash"
    ]

    def __init__(self):
        self.client = None
        if settings.gemini_api_key:
            try:
                self.client = genai.Client(api_key=settings.gemini_api_key)
                logger.info("Gemini AI Client initialized successfully.")
            except Exception as e:
                logger.warning(f"Gagal inisialisasi Gemini Client: {e}")

    def rerank_highlights(
        self,
        home_team: str,
        away_team: str,
        competition: str,
        candidates: list[dict]
    ) -> int | None:
        """
        Menggunakan Gemini AI untuk mengevaluasi judul kandidat video
        dan mengembalikan index kandidat terbaik (1-based), atau None jika ragu/tidak valid.
        """
        if not self.client or not candidates:
            return None

        prompt = f"""Kamu adalah pakar kurasi video highlight sepak bola resmi.
Pertandingan: {home_team} vs {away_team}
Kompetisi: {competition}

Daftar kandidat video highlight:
"""
        for i, c in enumerate(candidates, 1):
            dur = c.get("duration", 0)
            dur_str = f"{dur // 60}m {dur % 60}s"
            prompt += f"{i}. Judul: \"{c['title']}\" | Durasi: {dur_str} | Channel: {c.get('channel', 'Official')}\n"

        prompt += """
Tugas:
Pilih SATU nomor kandidat yang paling tepat merupakan highlight pertandingan resmi (bukan wawancara, bukan shorts, bukan analisis taktik, bukan live reaction).
Berikan output HANYA format JSON valid berikut:
{"best_index": <nomor_pilihan_atau_null>, "reason": "<alasan_singkat>"}
"""
        for model_name in self.MODELS_TO_TRY:
            try:
                response = self.client.models.generate_content(
                    model=model_name,
                    contents=prompt,
                    config=types.GenerateContentConfig(
                        temperature=0.1,
                        response_mime_type="application/json"
                    )
                )
                text = response.text.strip()
                parsed = json.loads(text)
                best_idx = parsed.get("best_index")
                if isinstance(best_idx, int) and 1 <= best_idx <= len(candidates):
                    logger.debug(f"[Gemini AI ({model_name})] Terpilih opsi #{best_idx}: {parsed.get('reason')}")
                    return best_idx - 1  # convert to 0-based index
            except Exception as e:
                logger.warning(f"[Gemini AI] Gagal evaluasi dengan {model_name}: {e}")

        return None

gemini_client = GeminiClient()
