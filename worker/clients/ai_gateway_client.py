import json
from typing import Any

import httpx
from loguru import logger
from pydantic import BaseModel

from worker.config.settings import settings


class HighlightRerankResult(BaseModel):
    best_index: int | None
    reason: str


class AiGatewayClient:
    """
    Clean Architecture AI Client with 2-tier pipeline:
    1. Primary: Oracle VM AI Gateway (jarvis-gateway / Ollama ARM64 on Oracle Super-Node)
    2. Fallback: Transparent delegation to deterministic Mathematical Heuristic Scorer
    """

    def __init__(self):
        self._http_client = httpx.Client(timeout=8.0)

    def _build_prompt(
        self,
        home_team: str,
        away_team: str,
        competition: str,
        candidates: list[dict[str, Any]],
    ) -> str:
        prompt = (
            f"You are an expert official football highlights curator.\n"
            f"Match: {home_team} vs {away_team}\n"
            f"Competition: {competition}\n\n"
            f"Candidate video highlight list:\n"
        )
        for i, c in enumerate(candidates, 1):
            dur = c.get("duration", 0)
            dur_str = f"{dur // 60}m {dur % 60}s"
            prompt += f"{i}. Title: \"{c['title']}\" | Duration: {dur_str} | Channel: {c.get('channel', 'Official')}\n"

        prompt += (
            "\nTask:\n"
            "Select the SINGLE best candidate number (1-based index) that is the genuine official match highlight or recap video of THIS specific match.\n"
            "STRICT REJECTION CRITERIA:\n"
            "- Reject full matches, full match replays, 90-minute recordings (only highlights allowed).\n"
            "- Reject training sessions, warm-ups, or prep videos (e.g. 'Prep', 'trains ahead').\n"
            "- Reject press conferences, interviews, or player quotes ('press conference', 'point presse', 'PK', 'conférence').\n"
            "- Reject quizzes, badge games, fan reactions, or behind-the-scenes.\n"
            "- Reject retro/historic matches or compilations from previous years.\n"
            "- Reject matches not between these two teams.\n"
            "Respond strictly in JSON format: {\"best_index\": <1-based int or null>, \"reason\": \"<brief explanation>\"}\n"
        )
        return prompt

    def _evaluate_oracle_gateway(
        self,
        prompt: str,
        candidates_count: int,
    ) -> tuple[int | None, str | None]:
        """Evaluates candidates using Oracle VM AI Gateway (jarvis-gateway / Ollama)."""
        if not settings.ai_gateway_url:
            return None, None

        url = f"{settings.ai_gateway_url.rstrip('/')}/v1/chat/completions"
        headers = {"Content-Type": "application/json"}
        if settings.ai_gateway_token:
            headers["Authorization"] = f"Bearer {settings.ai_gateway_token}"

        payload = {
            "model": settings.ai_gateway_model,
            "messages": [{"role": "user", "content": prompt}],
            "temperature": 0.1,
            "response_format": {"type": "json_object"},
        }

        try:
            resp = self._http_client.post(url, json=payload, headers=headers)
            if resp.status_code == 200:
                data = resp.json()
                content = data.get("choices", [{}])[0].get("message", {}).get("content", "").strip()
                parsed = json.loads(content)
                best_idx = parsed.get("best_index")
                reason = parsed.get("reason") or "Selected by Oracle VM AI Gateway"
                if isinstance(best_idx, int) and 1 <= best_idx <= candidates_count:
                    logger.info(f"[AI EVAL] [ORACLE GATEWAY] Selected candidate #{best_idx}: {reason}")
                    return best_idx - 1, reason
                elif best_idx is None:
                    logger.info(f"[AI EVAL] [ORACLE GATEWAY] Explicit rejection: {reason}")
                    return -1, reason
        except Exception as e:
            logger.debug(f"[AI EVAL] Oracle AI Gateway evaluation unavailable: {e}")

        return None, None

    def rerank_highlights(
        self,
        home_team: str,
        away_team: str,
        competition: str,
        candidates: list[dict[str, Any]],
    ) -> tuple[int | None, str | None, str]:
        """
        Curates candidate highlight videos through Oracle AI Gateway -> Heuristic fallback.
        Returns:
            (best_idx, reason, evaluator_name)
        """
        if not candidates:
            return None, None, "NONE"

        prompt = self._build_prompt(home_team, away_team, competition, candidates)

        # 1. Primary: Oracle VM AI Gateway
        res_oracle, reason_oracle = self._evaluate_oracle_gateway(prompt, len(candidates))
        if res_oracle is not None:
            return res_oracle, reason_oracle, f"ORACLE GATEWAY ({settings.ai_gateway_model})"

        # 2. Fallback: Deterministic Mathematical Heuristic Scorer
        logger.debug("[AI EVAL] Oracle AI Gateway unavailable. Transparently delegating to deterministic scorer.")
        return None, None, "DETERMINISTIC_FALLBACK"


ai_gateway_client = AiGatewayClient()
