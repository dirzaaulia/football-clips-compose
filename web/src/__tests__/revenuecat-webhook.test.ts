import { beforeEach, describe, expect, it, vi } from 'vitest';

const processedEvents = new Set<string>();
const userPremiumStatus = new Map<string, boolean>();

vi.mock('@/lib/database', () => {
  return {
    database: {
      from: (table: string) => {
        if (table === 'processed_webhook_events') {
          return {
            select: () => ({
              eq: (_col: string, eventId: string) => ({
                maybeSingle: async () => ({
                  data: processedEvents.has(eventId) ? { id: 1 } : null,
                  error: null,
                }),
              }),
            }),
            insert: async (row: { event_id: string }) => {
              processedEvents.add(row.event_id);
              return { error: null };
            },
          };
        }
        return {};
      },
      rpc: async (fnName: string, args: { p_user_id: string; p_is_premium: boolean; p_secret: string }) => {
        if (fnName === 'set_user_premium_status') {
          userPremiumStatus.set(args.p_user_id, args.p_is_premium);
        }
        return { error: null };
      },
    },
  };
});

import { POST } from '@/app/api/webhooks/revenuecat/route';

describe('RevenueCat Webhook Route Security & Idempotency', () => {
  beforeEach(() => {
    processedEvents.clear();
    userPremiumStatus.clear();
    process.env.REVENUECAT_WEBHOOK_SECRET = 'test_webhook_secret_999';
  });

  it('Rejects unauthorized requests with HTTP 401 when Bearer token is missing or invalid', async () => {
    const req = new Request('http://localhost:3000/api/webhooks/revenuecat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        authorization: 'Bearer wrong_token',
      },
      body: JSON.stringify({ event: { id: 'evt_1', type: 'INITIAL_PURCHASE', app_user_id: 'uid_1' } }),
    });

    const res = await POST(req);
    expect(res.status).toBe(401);
  });

  it('Grants premium on INITIAL_PURCHASE and enforces idempotency on duplicate delivery', async () => {
    const payload = {
      event: {
        id: 'evt_purchase_100',
        type: 'INITIAL_PURCHASE',
        app_user_id: 'firebase_user_abc',
      },
    };

    const req1 = new Request('http://localhost:3000/api/webhooks/revenuecat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        authorization: 'Bearer test_webhook_secret_999',
      },
      body: JSON.stringify(payload),
    });

    const res1 = await POST(req1);
    const json1 = await res1.json();
    expect(res1.status).toBe(200);
    expect(json1.action).toBe('premium_granted');
    expect(userPremiumStatus.get('firebase_user_abc')).toBe(true);

    // Duplicate delivery of the exact same event ID
    const req2 = new Request('http://localhost:3000/api/webhooks/revenuecat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        authorization: 'Bearer test_webhook_secret_999',
      },
      body: JSON.stringify(payload),
    });

    const res2 = await POST(req2);
    const json2 = await res2.json();
    expect(res2.status).toBe(200);
    expect(json2.note).toBe('duplicate_event_skipped');
  });

  it('Handles TRANSFER event by revoking old UID, granting new UID, and skipping $RCAnonymousID', async () => {
    userPremiumStatus.set('user_old', true);

    const req = new Request('http://localhost:3000/api/webhooks/revenuecat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        authorization: 'Bearer test_webhook_secret_999',
      },
      body: JSON.stringify({
        event: {
          id: 'evt_transfer_200',
          type: 'TRANSFER',
          transferred_from: ['user_old', '$RCAnonymousID:anon_123'],
          transferred_to: ['user_new', '$RCAnonymousID:anon_456'],
        },
      }),
    });

    const res = await POST(req);
    const json = await res.json();
    expect(res.status).toBe(200);
    expect(json.action).toBe('transfer_processed');
    expect(userPremiumStatus.get('user_old')).toBe(false);
    expect(userPremiumStatus.get('user_new')).toBe(true);
    expect(userPremiumStatus.has('$RCAnonymousID:anon_123')).toBe(false);
  });

  it('Revokes premium on EXPIRATION', async () => {
    userPremiumStatus.set('firebase_user_abc', true);

    const req = new Request('http://localhost:3000/api/webhooks/revenuecat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        authorization: 'Bearer test_webhook_secret_999',
      },
      body: JSON.stringify({
        event: {
          id: 'evt_expire_300',
          type: 'EXPIRATION',
          app_user_id: 'firebase_user_abc',
        },
      }),
    });

    const res = await POST(req);
    const json = await res.json();
    expect(res.status).toBe(200);
    expect(json.action).toBe('premium_revoked');
    expect(userPremiumStatus.get('firebase_user_abc')).toBe(false);
  });
});
