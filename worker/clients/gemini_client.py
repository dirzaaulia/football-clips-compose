try:
    from google import genai
    from google.genai import types
except ImportError:
    genai = None
    types = None

import json
import time
from collections import deque

from loguru import logger
from pydantic import BaseModel
from worker.config.settings import settings


class HighlightRerankResult(BaseModel):
    best_index: int | None
    reason: str


class GeminiClient:
    """
    Modern Gemini AI Client using Google GenAI SDK with structured outputs,
    in-memory sliding window Token Bucket rate limiter (12 RPM),
    and automatic failover across latest Gemini 3.x Flash models.
    """

    MAX_RPM: int = 12  # Strict ceiling below Google's 15 RPM free tier limit
    WINDOW_SECONDS: float = 60.0

    MODELS_TO_TRY = [
        "gemini-3.5-flash",
        "gemini-3.8-flash",
        "gemini-3.7-flash",
        "gemini-3.6-flash",
        "gemini-3.1-flash-lite",
        "gemini-3.5-flash-lite",
        "gemini-flash-latest",
        "gemini-flash-lite-latest",
    ]

    def __init__(self):
        self.client = None
        self._request_timestamps: deque[float] = deque()
        if settings.gemini_api_key and genai is not None:
            try:
                self.client = genai.Client(api_key=settings.gemini_api_key)
                logger.info("Gemini AI Client initialized successfully.")
            except Exception as e:
                logger.warning(f"Failed to initialize Gemini Client: {e}")

    def _acquire_token(self) -> bool:
        """Sliding-window token bucket to prevent exceeding free 15 RPM quota."""
        now = time.monotonic()
        while self._request_timestamps and now - self._request_timestamps[0] > self.WINDOW_SECONDS:
            self._request_timestamps.popleft()

        if len(self._request_timestamps) >= self.MAX_RPM:
            logger.warning(
                f"[Gemini TokenBucket] Rate limit reached ({len(self._request_timestamps)}/{self.MAX_RPM} req/min). "
                "Transparently delegating to mathematical heuristic scorer."
            )
            return False

        self._request_timestamps.append(now)
        return True

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

        # Check token bucket before consuming API call
        if not self._acquire_token():
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
Select the SINGLE best candidate number (1-based index) that is the genuine official match highlight or recap video of THIS specific match.
STRICT REJECTION CRITERIA:
- Reject full matches, full match replays, 90-minute recordings, or full games (we ONLY want highlights or extended highlights).
- Reject any training session, practice goal, warm-up, or prep video (e.g. "Prep", "trains ahead").
- Reject any press conference, interview, press briefing, or player quote (e.g. "point presse", "PK", "parole di", "conférence", "mister").
- Reject any quiz, badge game, behind-the-scenes, food/chef video, or social challenge.
- Reject any retro, historic match, or compilation from previous years (e.g. "CR7 hat-trick 2021", "gol da ricordare", "bangers against").
- Reject any match not between these two teams.
If NO candidate is a genuine match highlight of this fixture, set best_index to null. Never compromise or pick a non-highlight.
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
