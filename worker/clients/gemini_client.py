try:
    from google import genai
    from google.genai import types
except ImportError:
    genai = None
    types = None

import json

from loguru import logger
from pydantic import BaseModel
from worker.config.settings import settings


class HighlightRerankResult(BaseModel):
    best_index: int | None
    reason: str


class GeminiClient:
    """
    Modern Gemini AI Client using Google GenAI SDK with structured outputs
    and automatic failover across latest Gemini 3.x Flash models.
    """

    MODELS_TO_TRY = [
        "gemini-3.8-flash",
        "gemini-3.7-flash",
        "gemini-3.6-flash",
        "gemini-3.5-flash",
        "gemini-3.5-flash-lite",
        "gemini-3.1-flash-lite",
    ]

    def __init__(self):
        self.client = None
        if settings.gemini_api_key and genai is not None:
            try:
                self.client = genai.Client(api_key=settings.gemini_api_key)
                logger.info("Gemini AI Client initialized successfully.")
            except Exception as e:
                logger.warning(f"Failed to initialize Gemini Client: {e}")

    def rerank_highlights(
        self,
        home_team: str,
        away_team: str,
        competition: str,
        candidates: list[dict]
    ) -> int | None:
        """
        Uses Google Gemini AI to evaluate candidate highlight video titles
        and returns the 0-based index of the best candidate, or None if inconclusive.
        """
        if not self.client or not candidates:
            return None

        prompt = f"""You are an expert official football highlights curator.
Match: {home_team} vs {away_team}
Competition: {competition}

Candidate video highlight list:
"""
        for i, c in enumerate(candidates, 1):
            dur = c.get("duration", 0)
            dur_str = f"{dur // 60}m {dur % 60}s"
            prompt += f"{i}. Title: \"{c['title']}\" | Duration: {dur_str} | Channel: {c.get('channel', 'Official')}\n"

        prompt += """
Task:
Select the SINGLE best candidate number (1-based index) that represents the official match highlight package.
Ignore press conferences, shorts, tactical analyses, fan reactions, and match watchalongs.
If none are valid, set best_index to null.
"""

        for model_name in self.MODELS_TO_TRY:
            try:
                response = self.client.models.generate_content(
                    model=model_name,
                    contents=prompt,
                    config=types.GenerateContentConfig(
                        temperature=0.1,
                        response_mime_type="application/json",
                        response_schema=HighlightRerankResult,
                    )
                )
                text = response.text.strip()
                parsed = json.loads(text)
                best_idx = parsed.get("best_index")
                if isinstance(best_idx, int) and 1 <= best_idx <= len(candidates):
                    logger.debug(f"[Gemini AI ({model_name})] Selected option #{best_idx}: {parsed.get('reason')}")
                    return best_idx - 1  # convert to 0-based index
                elif best_idx is None:
                    logger.debug(f"[Gemini AI ({model_name})] Model determined no valid official highlight.")
                    return None
            except Exception as e:
                logger.warning(f"[Gemini AI] Evaluation failed with {model_name}: {e}")

        return None


gemini_client = GeminiClient()
