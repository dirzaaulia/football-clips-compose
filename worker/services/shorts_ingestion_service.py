import time
from datetime import UTC, datetime
from loguru import logger

from worker.clients.ai_gateway_client import ai_gateway_client
from worker.clients.database_client import db_client as supabase_client
from worker.clients.youtube_rss_client import youtube_rss_client
from worker.models.short_video import (
    FootballShortCandidate,
    FootballShortRecord,
    ShortCategory,
    ShortEvaluationResult,
    ShortsWaterfallTrace,
)
from worker.repositories.channel_repository import channel_repository
from worker.utils.telemetry_logger import append_event


class ShortsIngestionService:
    """Service to discover, classify, and ingest football shorts with 0 API quota."""

    def __init__(self):
        self.ai_client = ai_gateway_client
        self.rss_client = youtube_rss_client

    @staticmethod
    def _extract_video_fields(video: dict) -> tuple[str, str, str, int, str]:
        snippet = video.get("snippet") if isinstance(video.get("snippet"), dict) else {}
        raw_id = video.get("id")
        if isinstance(raw_id, dict):
            vid = str(raw_id.get("videoId") or "")
        else:
            vid = str(raw_id or video.get("video_id") or "")
        title = str(video.get("title") or snippet.get("title") or "")
        desc = str(video.get("description") or snippet.get("description") or "")
        dur = int(video.get("duration") or 0)
        pub = str(video.get("published_at") or snippet.get("publishedAt") or datetime.now(UTC).isoformat())
        return vid, title, desc, dur, pub

    def _is_candidate_short(self, video: dict) -> bool:
        """Determines if a video candidate qualifies as a short-form video."""
        _, title, desc, dur, _ = self._extract_video_fields(video)
        title_l = title.lower()
        desc_l = desc.lower()

        if dur > 65 and "#shorts" not in title_l and "#short" not in title_l and "#shorts" not in desc_l:
            return False
        if dur > 0 and dur <= 65:
            return True
        if "#shorts" in title_l or "#short" in title_l or "#shorts" in desc_l or "youtube.com/shorts" in desc_l:
            return True
        long_form_markers = [
            "highlights", "resumen", "samenvatting", "extended", "full match",
            "press conference", "episode", "documentary", "matchday live", "training session",
        ]
        if dur == 0 and title_l and len(title_l) <= 85 and not any(m in title_l for m in long_form_markers):
            return any(w in title_l for w in ["goal", "skill", "save", "gol", "assist", "nutmeg", "strike", "curler", "moment", "watch", "wait for", "POV"])
        return False

    def _evaluate_candidate(self, candidate: FootballShortCandidate) -> ShortEvaluationResult:
        """Evaluates video candidate via AI Gateway to filter out sponsor/kit spam."""
        prompt = (
            "You are an expert official football shorts curator.\n"
            f"Video Title: \"{candidate.title}\"\n"
            f"Channel: \"{candidate.channel_name}\"\n"
            f"Description: \"{candidate.description[:250]}\"\n\n"
            "Task: Classify this clip into one of the following categories:\n"
            "1. ON_PITCH_ACTION: Goals, skills, assists, saves, tackles, celebrations -> ACCEPT\n"
            "2. BEHIND_THE_SCENES: Tunnel cam, arrival, training drills -> ACCEPT\n"
            "3. COMMERCIAL_SPAM: Kit reveals, merchandise sales, sponsor promos, ticket ads, mascot skits -> REJECT\n\n"
            "Return JSON format:\n"
            '{"verdict": "ACCEPTED" or "REJECTED", "category": "GOALS_SKILLS" or "MOMENTS" or "BEHIND_THE_SCENES" or "COMMERCIAL_SPAM", "confidence": 0-100, "reason": "brief reason"}'
        )

        try:
            res_text = self.ai_client._call_provider("auto", prompt)
            if res_text:
                import json
                cleaned = res_text.strip()
                if "```json" in cleaned:
                    cleaned = cleaned.split("```json")[1].split("```")[0].strip()
                elif "```" in cleaned:
                    cleaned = cleaned.split("```")[1].split("```")[0].strip()

                parsed = json.loads(cleaned)
                verdict = parsed.get("verdict", "REJECTED").upper()
                cat_str = parsed.get("category", "COMMERCIAL_SPAM").upper()
                category = ShortCategory.COMMERCIAL_SPAM
                if cat_str in ShortCategory.__members__:
                    category = ShortCategory[cat_str]

                return ShortEvaluationResult(
                    video_id=candidate.video_id,
                    verdict=verdict,
                    category=category,
                    confidence=int(parsed.get("confidence", 70)),
                    reason=str(parsed.get("reason", "AI Evaluated")),
                    evaluator="ORACLE_AI_GATEWAY",
                )
        except Exception as e:
            logger.debug(f"[Shorts AI] Evaluation fallback for {candidate.video_id}: {e}")

        title_lower = candidate.title.lower()
        spam_keywords = ["kit", "store", "shop", "merch", "tickets", "sponsor", "partner", "sale", "discount"]
        press_keywords = ["press conference", "interview", "conference", "embargo", "pre-match press", "post-match press"]
        action_keywords = ["goal", "skill", "save", "gol", "assist", "highlight", "nutmeg", "strike", "curler"]

        if any(w in title_lower for w in spam_keywords):
            return ShortEvaluationResult(
                video_id=candidate.video_id,
                verdict="REJECTED",
                category=ShortCategory.COMMERCIAL_SPAM,
                confidence=85,
                reason="Matched commercial keyword in title",
                evaluator="DETERMINISTIC_FALLBACK",
            )

        if any(w in title_lower for w in press_keywords):
            return ShortEvaluationResult(
                video_id=candidate.video_id,
                verdict="REJECTED",
                category=ShortCategory.PRESS_CONFERENCE,
                confidence=85,
                reason="Matched press conference / interview keywords",
                evaluator="DETERMINISTIC_FALLBACK",
            )

        if any(w in title_lower for w in action_keywords):
            return ShortEvaluationResult(
                video_id=candidate.video_id,
                verdict="ACCEPTED",
                category=ShortCategory.GOALS_SKILLS,
                confidence=80,
                reason="Matched on-pitch action keywords",
                evaluator="DETERMINISTIC_FALLBACK",
            )

        return ShortEvaluationResult(
            video_id=candidate.video_id,
            verdict="ACCEPTED",
            category=ShortCategory.MOMENTS,
            confidence=60,
            reason="Default accepted football moment",
            evaluator="DETERMINISTIC_FALLBACK",
        )

    def ingest_recent_shorts(
        self,
        max_clubs: int = 25,
        log_events: list[dict] | None = None,
    ) -> ShortsWaterfallTrace:
        """Ingests and classifies shorts from top supported club channels."""
        start_time = time.time()
        channel_repository.ensure_loaded()

        trace = ShortsWaterfallTrace()
        clubs = list(channel_repository._clubs_by_id.values())[:max_clubs]

        for club in clubs:
            ch_id = club.get("youtube_channel_id")
            if not ch_id:
                continue

            c_name = club.get("name", "Unknown Club")
            c_handle = club.get("youtube_handle") or c_name
            trace.channels_queried.append(f"{c_name} ({c_handle})")

            recent_videos = self.rss_client.fetch_channel_recent_videos(ch_id)
            for v in recent_videos:
                if not self._is_candidate_short(v):
                    continue

                vid, title, desc, dur, pub = self._extract_video_fields(v)
                if not vid:
                    continue

                candidate = FootballShortCandidate(
                    video_id=vid,
                    title=title or "Football Clip",
                    channel_id=ch_id,
                    channel_name=c_name,
                    description=desc,
                    thumbnail_url=v.get("thumbnail_url") or f"https://img.youtube.com/vi/{vid}/hqdefault.jpg",
                    duration_seconds=dur if dur > 0 else 45,
                    published_at=pub,
                    club_id=club.get("id"),
                )

                evaluation = self._evaluate_candidate(candidate)
                candidate.evaluation = evaluation
                trace.candidates.append(candidate)

                if evaluation.verdict == "ACCEPTED":
                    trace.accepted_count += 1
                    numeric_club_id = int(candidate.club_id) if str(candidate.club_id or "").isdigit() else None
                    record = FootballShortRecord(
                        youtube_video_id=candidate.video_id,
                        title=candidate.title,
                        channel_id=candidate.channel_id,
                        channel_name=candidate.channel_name,
                        club_id=numeric_club_id,
                        category=evaluation.category.value,
                        thumbnail_url=candidate.thumbnail_url,
                        duration_seconds=candidate.duration_seconds,
                        published_at=candidate.published_at,
                    )
                    try:
                        supabase_client.table("football_shorts").upsert(
                            record.model_dump(exclude_none=True),
                            on_conflict="youtube_video_id",
                        ).execute()
                        trace.ingested_video_ids.append(candidate.video_id)
                        append_event(
                            log_events,
                            f"[SHORTS ACCEPTED] {c_name}: \"{candidate.title}\" ({evaluation.category.value}, {evaluation.confidence}%)",
                            "SUCCESS",
                            "SHORTS_PIPELINE",
                            "SHORT_ACCEPTED",
                            attributes={
                                "video_id": candidate.video_id,
                                "title": candidate.title,
                                "channel_name": c_name,
                                "channel_handle": c_handle,
                                "category": evaluation.category.value,
                                "confidence": evaluation.confidence,
                                "reason": evaluation.reason,
                                "evaluator": evaluation.evaluator,
                                "duration_seconds": candidate.duration_seconds,
                                "published_at": candidate.published_at,
                            },
                        )
                    except Exception as e:
                        logger.error(f"[Shorts] Upsert failed for {candidate.video_id}: {e}")
                else:
                    trace.rejected_count += 1
                    append_event(
                        log_events,
                        f"[SHORTS REJECTED] {c_name}: \"{candidate.title}\" — {evaluation.reason} ({evaluation.category.value})",
                        "INFO",
                        "SHORTS_PIPELINE",
                        "SHORT_REJECTED",
                        attributes={
                            "video_id": candidate.video_id,
                            "title": candidate.title,
                            "channel_name": c_name,
                            "channel_handle": c_handle,
                            "category": evaluation.category.value,
                            "confidence": evaluation.confidence,
                            "reason": evaluation.reason,
                            "evaluator": evaluation.evaluator,
                            "duration_seconds": candidate.duration_seconds,
                            "published_at": candidate.published_at,
                        },
                    )

        trace.duration_ms = int((time.time() - start_time) * 1000)
        append_event(
            log_events,
            f"[SHORTS SUMMARY] Scanned {len(trace.channels_queried)} official club RSS feeds: {trace.accepted_count} accepted, {trace.rejected_count} rejected ({trace.duration_ms}ms, 0 API quota)",
            "INFO",
            "SHORTS_PIPELINE",
            "SHORTS_SUMMARY",
            attributes={
                "channels_queried": len(trace.channels_queried),
                "accepted_count": trace.accepted_count,
                "rejected_count": trace.rejected_count,
                "duration_ms": trace.duration_ms,
            },
        )
        logger.info(
            f"[Shorts] Ingestion complete: {trace.accepted_count} accepted, {trace.rejected_count} rejected "
            f"across {len(trace.channels_queried)} channels in {trace.duration_ms}ms"
        )
        return trace


shorts_service = ShortsIngestionService()

