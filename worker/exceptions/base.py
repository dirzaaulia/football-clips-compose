"""
Domain exception hierarchy for FootballClips backend services.
"""


class DomainException(Exception):
    """Base exception for all FootballClips domain and application errors."""

    def __init__(self, message: str, code: str = "INTERNAL_ERROR"):
        super().__init__(message)
        self.code = code
        self.message = message


class EntityNotFoundError(DomainException):
    """Raised when a requested domain entity (match, club, competition) does not exist."""

    def __init__(self, entity: str, entity_id: str | int):
        super().__init__(f"{entity} with ID '{entity_id}' not found.", code="NOT_FOUND")


class ExternalServiceError(DomainException):
    """Raised when an external dependency (ESPN, YouTube API, AI Gateway) fails or times out."""

    def __init__(self, service_name: str, detail: str):
        super().__init__(f"External service '{service_name}' failed: {detail}", code="EXTERNAL_UNAVAILABLE")


class QuotaExhaustedError(DomainException):
    """Raised when an external API quota (e.g. YouTube Data API v3) is depleted."""

    def __init__(self, quota_type: str, limit: int):
        super().__init__(f"{quota_type} quota limit ({limit}) exhausted for current window.", code="QUOTA_EXHAUSTED")


class ValidationError(DomainException):
    """Raised when data fails domain validation invariants."""

    def __init__(self, reason: str):
        super().__init__(f"Validation failed: {reason}", code="VALIDATION_ERROR")
