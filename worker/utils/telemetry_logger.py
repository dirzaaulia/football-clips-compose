import re
from datetime import UTC, datetime
from worker.config.settings import settings

EMOJI_REGEX = re.compile(r"[\U00010000-\U0010ffff\u2600-\u27bf\u2b50]", flags=re.UNICODE)


def format_log_event(
    message: str,
    level: str = "INFO",
    subsystem: str = "WORKER",
    action: str = "RUN"
) -> str:
    """
    Format a telemetry event with WIB timestamp and action verb without duplicate bracket noise or emojis.
    """
    now_wib = datetime.now(settings.wib_tz)
    ts = now_wib.strftime("%H:%M:%S") + " WIB"
    clean_msg = EMOJI_REGEX.sub("", message).strip()
    # Strip redundant leading bracket if it echoes the action, level, or status
    if clean_msg.startswith("[") and "]" in clean_msg:
        lead = clean_msg[1:clean_msg.index("]")].strip().upper()
        if lead in [action.upper(), level.upper(), subsystem.upper(), "STATUS", "INFO", "WARN"]:
            clean_msg = clean_msg[clean_msg.index("]") + 1:].strip()
    return f"[{ts}] [{subsystem}:{action}] {clean_msg}"


class EventLine(str):
    def __new__(cls, text, record):
        value = super().__new__(cls, text)
        value.record = record
        value.match_id = record.get("match_id")
        value.attributes = record.get("attributes") or {}
        value.level = record.get("level", "INFO")
        value.code = record.get("code", "RUN")
        value.service = record.get("service", "WORKER")
        value.created_at = record.get("created_at")
        return value


def append_event(
    log_events: list | None,
    message: str,
    level: str = "INFO",
    subsystem: str = "WORKER",
    action: str = "RUN",
    *, match_id: int | None = None, attributes: dict | None = None,
) -> str:
    line = format_log_event(message, level, subsystem, action)
    line = EventLine(line,{'created_at':datetime.now(UTC).isoformat(),'level':level,'service':subsystem,
                          'code':action,'message':message,'match_id':match_id,'attributes':attributes or {}})
    if log_events is not None:
        log_events.append(line)
    return line
