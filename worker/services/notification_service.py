from datetime import UTC, datetime
from typing import Any

from loguru import logger
from worker.utils.telemetry_logger import append_event

try:
    import firebase_admin
    from firebase_admin import credentials, messaging
except ImportError:
    firebase_admin = None
    messaging = None


class NotificationService:
    """
    Real-time push notification service dispatching FCM Topic alerts
    to subscribed clubs across Android and Web without per-user database querying.
    """

    def __init__(self):
        self._initialized = False
        self._sent_keys: set[str] = set()
        self._init_firebase()

    def _init_firebase(self):
        if not firebase_admin:
            logger.warning("[NotificationService] firebase-admin package is not installed.")
            return

        try:
            if not firebase_admin._apps:
                firebase_admin.initialize_app(options={"projectId": "highlightdata"})
            self._initialized = True
            logger.info("[NotificationService] Firebase Admin initialized for project 'highlightdata'.")
        except Exception as e:
            logger.warning(f"[NotificationService] Firebase Admin initialization failed: {e}")
            self._initialized = False

    def _send_to_club_topics(
        self,
        club_ids: list[Any],
        title: str,
        body: str,
        data_payload: dict[str, str],
        channel_id: str = "channel_live_scores"
    ) -> int:
        if not self._initialized or not messaging:
            return 0

        dispatched = 0
        for cid in club_ids:
            if not cid:
                continue
            topic = f"club_{cid}"
            try:
                message = messaging.Message(
                    topic=topic,
                    notification=messaging.Notification(
                        title=title,
                        body=body
                    ),
                    data=data_payload,
                    android=messaging.AndroidConfig(
                        priority="high",
                        notification=messaging.AndroidNotification(
                            channel_id=channel_id,
                            icon="ic_notification",
                            color="#00E5FF",
                            click_action="OPEN_MATCH_DETAIL"
                        )
                    ),
                    webpush=messaging.WebpushConfig(
                        notification=messaging.WebpushNotification(
                            title=title,
                            body=body,
                            icon="/icons/icon-192.png",
                            badge="/icons/badge-72.png"
                        ),
                        fcm_options=messaging.WebpushFCMOptions(
                            link=data_payload.get("url", "/")
                        )
                    )
                )
                messaging.send(message)
                dispatched += 1
            except Exception as e:
                logger.warning(f"[NotificationService] Failed to send to topic {topic}: {e}")

        return dispatched

    def notify_match_event(
        self,
        match: dict,
        event_type: str,
        log_events: list | None = None
    ) -> bool:
        match_id = str(match.get("id", ""))
        home = match.get("home_team", "Home")
        away = match.get("away_team", "Away")
        h_score = match.get("home_score")
        a_score = match.get("away_score")
        h_id = match.get("home_team_id")
        a_id = match.get("away_team_id")
        comp = match.get("competition_name") or match.get("competition_id") or "League"

        score_text = f"{home} {h_score if h_score is not None else 0} - {a_score if a_score is not None else 0} {away}"
        dedup_key = ""
        title = ""
        body = ""
        channel_id = "channel_live_scores"

        if event_type == "kickoff":
            dedup_key = f"{match_id}:kickoff"
            title = f"Kickoff: {home} vs {away}"
            body = f"The match has kicked off in {comp}!"
        elif event_type == "score":
            dedup_key = f"{match_id}:score:{h_score}_{a_score}"
            title = f"GOAL! {score_text}"
            body = f"Live score update in {comp}."
        elif event_type == "fulltime":
            dedup_key = f"{match_id}:fulltime"
            title = f"Full Time: {score_text}"
            body = f"Final whistle! The match has concluded."
        elif event_type == "upcoming":
            dedup_key = f"{match_id}:upcoming"
            title = f"Match Starting Soon: {home} vs {away}"
            body = f"Kickoff in 30 minutes in {comp}. Starting lineups are out!"
            channel_id = "channel_match_reminders"
        else:
            return False

        if dedup_key in self._sent_keys:
            return False

        data_payload = {
            "match_id": match_id,
            "event_type": event_type,
            "home_team": home,
            "away_team": away,
            "score": score_text,
            "url": f"/match/{match_id}"
        }

        sent = self._send_to_club_topics([h_id, a_id], title, body, data_payload, channel_id)
        self._sent_keys.add(dedup_key)

        if sent > 0 and log_events is not None:
            append_event(
                log_events,
                f"[NOTIFY - {event_type.upper()}] {title} (topics: club_{h_id}, club_{a_id})",
                "SUCCESS",
                "FCM_NOTIFY",
                "DISPATCHED"
            )
        return sent > 0

    def notify_highlight_ready(
        self,
        match: dict,
        video_id: str,
        video_title: str,
        log_events: list | None = None
    ) -> bool:
        match_id = str(match.get("id", ""))
        home = match.get("home_team", "Home")
        away = match.get("away_team", "Away")
        h_id = match.get("home_team_id")
        a_id = match.get("away_team_id")

        dedup_key = f"{match_id}:highlight:{video_id}"
        if dedup_key in self._sent_keys:
            return False

        title = f"Highlights Ready: {home} vs {away}"
        body = f"Official match video highlights are now ready to stream!"
        data_payload = {
            "match_id": match_id,
            "video_id": video_id,
            "event_type": "highlight",
            "url": f"/match/{match_id}?video={video_id}"
        }

        sent = self._send_to_club_topics([h_id, a_id], title, body, data_payload, "channel_highlights")
        self._sent_keys.add(dedup_key)

        if sent > 0 and log_events is not None:
            append_event(
                log_events,
                f"[NOTIFY - HIGHLIGHT] {title} (topics: club_{h_id}, club_{a_id})",
                "SUCCESS",
                "FCM_NOTIFY",
                "DISPATCHED"
            )
        return sent > 0

    def check_upcoming_fixtures(
        self,
        matches: list[dict],
        log_events: list | None = None
    ):
        now = datetime.now(UTC)
        for m in matches:
            if m.get("status") not in ["TIMED", "SCHEDULED"]:
                continue
            utc_str = m.get("utc_date")
            if not utc_str:
                continue
            try:
                clean_str = utc_str.replace("Z", "+00:00")
                match_time = datetime.fromisoformat(clean_str)
                diff_minutes = (match_time - now).total_seconds() / 60.0
                if 10.0 <= diff_minutes <= 35.0:
                    self.notify_match_event(m, "upcoming", log_events)
            except Exception:
                continue


notification_service = NotificationService()
