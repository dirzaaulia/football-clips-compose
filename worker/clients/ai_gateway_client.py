"""Clean Architecture Multi-Tier Resilient AI Client with 3-Strike Circuit Breaker and Verdict Cache."""

import hashlib
import json
import time
from typing import Any

import httpx
from loguru import logger
from pydantic import BaseModel

from worker.config.settings import settings


class HighlightRerankResult(BaseModel):
    best_index: int | None
    reason: str


PRIMARY_CLOUD_MODELS: list[str] = [
    "auto",
    "cloudflare/@cf/zai-org/glm-4.7-flash",
    "groq/openai/gpt-oss-20b",
    "gemini/gemini-3.5-flash-lite",
    "groq/openai/gpt-oss-120b",
    "groq/qwen/qwen3.8-27b",
    "gemini/gemini-3.8-flash",
    "cloudflare/@cf/qwen/qwen3.8-27b",
    "cloudflare/@cf/google/gemma-4-26b-a4b-it",
    "cloudflare/@cf/nvidia/nemotron-3-120b-a12b",
]

OPENROUTER_FREE_MODELS: list[str] = [
    "openrouter/free",
    "nvidia/nemotron-3-ultra-550b-a55b:free",
    "google/gemma-4-31b-it:free",
    "minimax/minimax-m3.1-flash:free",
    "cohere/north-mini-code:free",
    "poolside/laguna-s-2.1:free",
]

LOCAL_OLLAMA_MODELS: list[tuple[str, float]] = [
    ("local/qwen3.5:4b", 45.0),
    ("local/qwen3.5:9b", 60.0),
]


class AiGatewayClient:
    """5-Tier Resilient AI Client with 3-Strike Circuit Breaker and Self-Learning Verdict Cache."""

    CIRCUIT_BREAKER_THRESHOLD = 3
    CIRCUIT_COOLDOWN_SECONDS = 300.0

    def __init__(self):
        self._http_client = httpx.Client(timeout=25.0)
        self._cloud_rr_index = 0
        self._consecutive_failures = 0
        self._circuit_open_until = 0.0
        self._verdict_cache: dict[str, tuple[int | None, str | None, str]] = {}

    def reset_circuit_breaker(self):
        self._consecutive_failures = 0
        self._circuit_open_until = 0.0
        self._verdict_cache.clear()

    def is_circuit_open(self) -> bool:
        return time.time() < self._circuit_open_until

    def _record_success(self, cache_key: str, result: tuple[int | None, str | None, str]):
        self._consecutive_failures = 0
        self._circuit_open_until = 0.0
        self._verdict_cache[cache_key] = result

    def _record_full_failure(self):
        self._consecutive_failures += 1
        if self._consecutive_failures >= self.CIRCUIT_BREAKER_THRESHOLD:
            self._circuit_open_until = time.time() + self.CIRCUIT_COOLDOWN_SECONDS
            logger.warning(
                f"[AI CIRCUIT BREAKER] Tripped after {self._consecutive_failures} consecutive full failures. "
                f"Routing to deterministic scorer for {self.CIRCUIT_COOLDOWN_SECONDS:.0f}s."
            )

    @staticmethod
    def _build_cache_key(home_team: str, away_team: str, candidates: list[dict[str, Any]]) -> str:
        c_sig = "|".join(f"{c.get('id', '')}:{c.get('title', '')}" for c in candidates)
        raw = f"{home_team.lower()}::{away_team.lower()}::{c_sig}"
        return hashlib.sha256(raw.encode("utf-8")).hexdigest()[:24]

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
            desc = (c.get("description") or "").replace("\n", " ").strip()
            desc_part = f' | Description: "{desc[:300]}"' if desc else ""
            prompt += f'{i}. Title: "{c["title"]}" | Duration: {dur_str} | Channel: {c.get("channel", "Official")}{desc_part}\n'

        prompt += (
            "\nTask:\n"
            "Select the SINGLE best candidate number (1-based index) that is the genuine official match highlight or recap video of THIS specific match.\n"
            "Note: Official club channels often upload official highlights with stylized/creative titles (e.g. 'HIGHLIGHTS | A chance-filled night'). "
            "Verify opponent and match details in the description snippet.\n"
            "STRICT REJECTION CRITERIA:\n"
            "- Reject studio analysis, talk shows, pundit debates, opinion clips, or question-based titles (e.g. 'Is Como Setting the Standard...', '#UCLToday', 'The Verdict').\n"
            "- Reject full matches, full match replays, 90-minute recordings (only highlights allowed).\n"
            "- Reject training sessions, warm-ups, or prep videos (e.g. 'Prep', 'trains ahead').\n"
            "- Reject press conferences, interviews, or player quotes ('press conference', 'point presse', 'PK', 'conférence', 'συνέντευξη').\n"
            "- Reject quizzes, badge games, fan reactions, or behind-the-scenes.\n"
            "- Reject retro/historic matches or compilations from previous years.\n"
            "- Reject matches not between these two teams.\n"
            "- Candidate MUST be authentic match footage highlights.\n"
            'If NO candidate is a genuine match highlight, return: {"best_index": null, "reason": "<brief explanation>"}\n'
            'Respond strictly in JSON format: {"best_index": <1-based int or null>, "reason": "<brief explanation>"}\n'
        )
        return prompt

    def _call_gateway_model(
        self,
        model_name: str,
        prompt: str,
        candidates_count: int,
        timeout: float = 15.0,
    ) -> tuple[int | None, str | None, bool]:
        if not settings.ai_gateway_url:
            return None, None, True

        url = f"{settings.ai_gateway_url.rstrip('/')}/v1/chat/completions"
        headers = {"Content-Type": "application/json"}
        if settings.ai_gateway_token:
            headers["Authorization"] = f"Bearer {settings.ai_gateway_token}"

        payload = {
            "model": model_name,
            "messages": [{"role": "user", "content": prompt}],
            "temperature": 0.1,
            "response_format": {"type": "json_object"},
        }

        try:
            resp = self._http_client.post(url, json=payload, headers=headers, timeout=timeout)
            if resp.status_code == 200:
                content = resp.json().get("choices", [{}])[0].get("message", {}).get("content", "").strip()
                parsed = json.loads(content)
                best_idx = parsed.get("best_index")
                reason = parsed.get("reason") or f"Selected by {model_name}"
                if isinstance(best_idx, int) and 1 <= best_idx <= candidates_count:
                    return best_idx - 1, reason, False
                if best_idx is None:
                    return -1, reason, False
                return None, None, True
            return None, None, True
        except Exception as e:
            logger.debug(f"[AI RETRY] {model_name} failed: {e}")
            return None, None, True

    def _evaluate_oracle_gateway(self, prompt: str, candidates_count: int) -> tuple[int | None, str | None]:
        idx, reason, failed = self._call_gateway_model(settings.ai_gateway_model, prompt, candidates_count, timeout=15.0)
        return (idx, reason) if not failed else (None, None)

    def _evaluate_gemini_fallback(self, prompt: str, candidates_count: int) -> tuple[int | None, str | None]:
        if not settings.gemini_api_key:
            return None, None
        url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-lite-latest:generateContent?key={settings.gemini_api_key}"
        payload = {"contents": [{"parts": [{"text": prompt}]}], "generationConfig": {"responseMimeType": "application/json"}}
        try:
            resp = self._http_client.post(url, json=payload, headers={"Content-Type": "application/json"}, timeout=15.0)
            if resp.status_code == 200:
                text = resp.json().get("candidates", [{}])[0].get("content", {}).get("parts", [{}])[0].get("text", "").strip()
                parsed = json.loads(text)
                best_idx = parsed.get("best_index")
                reason = parsed.get("reason") or "Selected by Gemini Fallback"
                if isinstance(best_idx, int) and 1 <= best_idx <= candidates_count:
                    return best_idx - 1, reason
                if best_idx is None:
                    return -1, reason
        except Exception as e:
            logger.debug(f"[AI EVAL] Gemini fallback unavailable: {e}")
        return None, None

    def rerank_highlights(
        self,
        home_team: str,
        away_team: str,
        competition: str,
        candidates: list[dict[str, Any]],
    ) -> tuple[int | None, str | None, str]:
        if not candidates:
            return None, None, "NONE"

        cache_key = self._build_cache_key(home_team, away_team, candidates)
        if cache_key in self._verdict_cache:
            return self._verdict_cache[cache_key]

        if self.is_circuit_open():
            return None, None, "DETERMINISTIC_FALLBACK"

        prompt = self._build_prompt(home_team, away_team, competition, candidates)
        num_cands = len(candidates)

        start_idx = self._cloud_rr_index
        self._cloud_rr_index = (start_idx + 1) % len(PRIMARY_CLOUD_MODELS)
        ordered_cloud = PRIMARY_CLOUD_MODELS[start_idx:] + PRIMARY_CLOUD_MODELS[:start_idx]

        for model in ordered_cloud:
            idx, reason, failed = self._call_gateway_model(model, prompt, num_cands, timeout=15.0)
            if not failed:
                res = (idx, reason, f"ORACLE GATEWAY ({model})")
                self._record_success(cache_key, res)
                return res

        for model in OPENROUTER_FREE_MODELS:
            idx, reason, failed = self._call_gateway_model(model, prompt, num_cands, timeout=12.0)
            if not failed:
                res = (idx, reason, f"OPENROUTER ({model})")
                self._record_success(cache_key, res)
                return res

        res_gemini, reason_gemini = self._evaluate_gemini_fallback(prompt, num_cands)
        if res_gemini is not None:
            res = (res_gemini, reason_gemini, "GEMINI_DIRECT")
            self._record_success(cache_key, res)
            return res

        for model, timeout in LOCAL_OLLAMA_MODELS:
            idx, reason, failed = self._call_gateway_model(model, prompt, num_cands, timeout=timeout)
            if not failed:
                res = (idx, reason, f"ORACLE_LOCAL_OLLAMA ({model})")
                self._record_success(cache_key, res)
                return res

        self._record_full_failure()
        return None, None, "DETERMINISTIC_FALLBACK"


ai_gateway_client = AiGatewayClient()
