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
    Format a telemetry event with millisecond-precision WIB timestamp,
    severity level, subsystem tag, and action verb without emojis.
    """
    now_wib = datetime.now(settings.wib_tz)
    ts = now_wib.strftime("%H:%M:%S.%f")[:-3] + " WIB"
    sub_action = f"{subsystem}:{action}" if subsystem else action
    clean_msg = EMOJI_REGEX.sub("", message).strip()
    return f"[{ts}] [{level}] [{sub_action}] {clean_msg}"


class EventLine(str):
    def __new__(cls, text, record):
        value = super().__new__(cls,text)
        value.record = record
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
