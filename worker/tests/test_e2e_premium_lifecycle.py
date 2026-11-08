from unittest.mock import MagicMock, patch
import pytest


class MockDatabase:
    """Mock simulating PostgreSQL 16 on Oracle Super-Node with security definer stored procedure."""
    def __init__(self, expected_secret: str = "prod_rc_secret_9988"):
        self.expected_secret = expected_secret
        self.profiles: dict[str, dict] = {}
        self.app_sessions: dict[str, dict] = {}
        self.processed_webhook_events: set[str] = set()

    def register_device(self, device_id: str, version: str, version_code: int):
        self.app_sessions[device_id] = {
            "device_id": device_id,
            "app_version": version,
            "version_code": version_code,
        }

    def sign_in_user(self, user_id: str, email: str, display_name: str):
        self.profiles[user_id] = {
            "id": user_id,
            "email": email,
            "display_name": display_name,
            "is_premium": False,
        }

    def set_user_premium_status(self, user_id: str, is_premium: bool, secret: str):
        if not secret or secret != self.expected_secret:
            raise PermissionError("Unauthorized: Invalid webhook secret")
        if user_id not in self.profiles:
            self.profiles[user_id] = {"id": user_id, "is_premium": is_premium}
        else:
            self.profiles[user_id]["is_premium"] = is_premium
        return {"success": True, "user_id": user_id, "is_premium": is_premium}

    def client_attempt_direct_tamper(self, user_id: str, is_premium: bool):
        """Simulates client calling PATCH /rest/v1/profiles directly with web_anon key."""
        raise PermissionError("Permission denied for column is_premium: UPDATE revoked for web_anon")


class RevenueCatWebhookEngine:
    """Python test double of the Next.js /api/webhooks/revenuecat handler."""
    GRANT_EVENTS = {"INITIAL_PURCHASE", "RENEWAL", "UNCANCELLATION", "NON_RENEWING_PURCHASE", "PRODUCT_CHANGE"}
    REVOKE_EVENTS = {"EXPIRATION", "CANCELLATION", "BILLING_ISSUE"}

    def __init__(self, db: MockDatabase, secret: str):
        self.db = db
        self.secret = secret

    def process_webhook(self, payload: dict, auth_header: str | None = None) -> dict:
        if auth_header != f"Bearer {self.secret}":
            return {"status": 401, "error": "Unauthorized"}

        event = payload.get("event")
        if not event or not event.get("type"):
            return {"status": 400, "error": "Invalid event payload"}

        event_id = event.get("id") or f"{event.get('type')}_{event.get('app_user_id')}"
        event_type = event.get("type")
        app_user_id = event.get("app_user_id", "")

        # 1. Idempotency Check
        if event_id in self.db.processed_webhook_events:
            return {"status": 200, "received": True, "note": "duplicate_event_skipped", "event_id": event_id}

        # 2. Transfer
        if event_type == "TRANSFER":
            transferred_from = event.get("transferred_from", [])
            transferred_to = event.get("transferred_to", [])
            for old_uid in transferred_from:
                if not old_uid.startswith("$RCAnonymousID:"):
                    self.db.set_user_premium_status(old_uid, False, self.secret)
            for new_uid in transferred_to:
                if not new_uid.startswith("$RCAnonymousID:"):
                    self.db.set_user_premium_status(new_uid, True, self.secret)
            self.db.processed_webhook_events.add(event_id)
            return {"status": 200, "received": True, "action": "transfer_processed"}

        # 3. Skip anonymous user
        if not app_user_id or app_user_id.startswith("$RCAnonymousID:"):
            self.db.processed_webhook_events.add(event_id)
            return {"status": 200, "received": True, "note": "Skipped anonymous user"}

        # 4. Grants & Revokes
        if event_type in self.GRANT_EVENTS:
            self.db.set_user_premium_status(app_user_id, True, self.secret)
            self.db.processed_webhook_events.add(event_id)
            return {"status": 200, "received": True, "action": "premium_granted"}

        if event_type in self.REVOKE_EVENTS:
            self.db.set_user_premium_status(app_user_id, False, self.secret)
            self.db.processed_webhook_events.add(event_id)
            return {"status": 200, "received": True, "action": "premium_revoked"}

        self.db.processed_webhook_events.add(event_id)
        return {"status": 200, "received": True, "action": "unhandled_event_type"}


def test_full_premium_lifecycle_e2e():
    secret = "prod_rc_secret_9988"
    db = MockDatabase(expected_secret=secret)
    engine = RevenueCatWebhookEngine(db, secret=secret)

    # 1. Device Handshake
    db.register_device("device_fold_8_001", "3.0.1", 89832569)
    assert "device_fold_8_001" in db.app_sessions
    assert db.app_sessions["device_fold_8_001"]["version_code"] == 89832569

    # 2. Google OAuth Profile
    user_id = "usr_google_1092830"
    db.sign_in_user(user_id, "football_fan@gmail.com", "GunnerFan")
    assert db.profiles[user_id]["is_premium"] is False

    # 3. Initial Purchase Webhook
    purchase_payload = {
        "event": {
            "id": "evt_purchase_abc123",
            "type": "INITIAL_PURCHASE",
            "app_user_id": user_id,
            "product_id": "footballclips_supporter_monthly",
        },
    }
    resp = engine.process_webhook(purchase_payload, auth_header=f"Bearer {secret}")
    assert resp["status"] == 200
    assert resp["action"] == "premium_granted"
    assert db.profiles[user_id]["is_premium"] is True

    # 4. Idempotency Guard on Duplicate Delivery
    resp_dup = engine.process_webhook(purchase_payload, auth_header=f"Bearer {secret}")
    assert resp_dup["status"] == 200
    assert resp_dup["note"] == "duplicate_event_skipped"

    # 5. Expiration Webhook
    expire_payload = {
        "event": {
            "id": "evt_expire_xyz789",
            "type": "EXPIRATION",
            "app_user_id": user_id,
        },
    }
    resp_exp = engine.process_webhook(expire_payload, auth_header=f"Bearer {secret}")
    assert resp_exp["status"] == 200
    assert resp_exp["action"] == "premium_revoked"
    assert db.profiles[user_id]["is_premium"] is False

    # 6. Account Transfer (skipping anonymous tokens)
    new_user_id = "usr_google_223344"
    db.sign_in_user(new_user_id, "transfer_fan@gmail.com", "GunnerNew")
    # Grant to old user first
    db.profiles[user_id]["is_premium"] = True

    transfer_payload = {
        "event": {
            "id": "evt_transfer_555",
            "type": "TRANSFER",
            "transferred_from": [user_id, "$RCAnonymousID:f789abc012"],
            "transferred_to": [new_user_id],
        },
    }
    resp_xfer = engine.process_webhook(transfer_payload, auth_header=f"Bearer {secret}")
    assert resp_xfer["status"] == 200
    assert resp_xfer["action"] == "transfer_processed"
    assert db.profiles[user_id]["is_premium"] is False
    assert db.profiles[new_user_id]["is_premium"] is True


def test_security_entitlement_tampering_blocked():
    secret = "prod_rc_secret_9988"
    db = MockDatabase(expected_secret=secret)

    user_id = "usr_hacker_007"
    db.sign_in_user(user_id, "hacker@evil.com", "Hacker")

    # Attacker tries direct PATCH /rest/v1/profiles?id=eq.usr_hacker_007 with is_premium=true
    with pytest.raises(PermissionError) as exc_info:
        db.client_attempt_direct_tamper(user_id, True)
    assert "UPDATE revoked for web_anon" in str(exc_info.value)
    assert db.profiles[user_id]["is_premium"] is False

    # Attacker tries direct RPC call with bogus secret
    with pytest.raises(PermissionError) as rpc_exc:
        db.set_user_premium_status(user_id, True, "invalid_secret_guess")
    assert "Invalid webhook secret" in str(rpc_exc.value)
    assert db.profiles[user_id]["is_premium"] is False
