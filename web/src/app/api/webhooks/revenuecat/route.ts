import { NextResponse } from 'next/server';
import { database } from '@/lib/database';

const GRANT_EVENTS = new Set([
  'INITIAL_PURCHASE',
  'RENEWAL',
  'UNCANCELLATION',
  'NON_RENEWING_PURCHASE',
  'PRODUCT_CHANGE',
]);

const REVOKE_EVENTS = new Set([
  'EXPIRATION',
  'CANCELLATION',
  'BILLING_ISSUE',
]);

export async function POST(req: Request) {
  try {
    const webhookSecret = process.env.REVENUECAT_WEBHOOK_SECRET || 'fc_revenuecat_production_secret';
    if (process.env.REVENUECAT_WEBHOOK_SECRET) {
      const authHeader = req.headers.get('authorization');
      if (authHeader !== `Bearer ${process.env.REVENUECAT_WEBHOOK_SECRET}`) {
        return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
      }
    }

    const body = await req.json();
    const event = body?.event;

    if (!event || !event.type) {
      return NextResponse.json({ error: 'Invalid event payload' }, { status: 400 });
    }

    const eventId: string = event.id || `${event.type}_${event.app_user_id || 'anon'}_${event.event_timestamp_ms || Date.now()}`;
    const eventType: string = event.type;
    const appUserId: string = event.app_user_id || '';

    // 1. Idempotency Check: Guard against duplicate delivery
    const { data: existingEvent } = await database
      .from('processed_webhook_events')
      .select('id')
      .eq('event_id', eventId)
      .maybeSingle();

    if (existingEvent) {
      return NextResponse.json({
        received: true,
        note: 'duplicate_event_skipped',
        event_id: eventId,
      });
    }

    // 2. Handle Transfer of Entitlements between Accounts
    if (eventType === 'TRANSFER') {
      const transferredFrom: string[] = event.transferred_from || [];
      const transferredTo: string[] = event.transferred_to || [];

      for (const oldUid of transferredFrom) {
        if (oldUid && !oldUid.startsWith('$RCAnonymousID:')) {
          await database.rpc('set_user_premium_status', {
            p_user_id: oldUid,
            p_is_premium: false,
            p_secret: webhookSecret,
          });
        }
      }

      for (const newUid of transferredTo) {
        if (newUid && !newUid.startsWith('$RCAnonymousID:')) {
          await database.rpc('set_user_premium_status', {
            p_user_id: newUid,
            p_is_premium: true,
            p_secret: webhookSecret,
          });
        }
      }

      await database.from('processed_webhook_events').insert({
        event_id: eventId,
        event_type: eventType,
        app_user_id: appUserId || 'transfer',
      });

      return NextResponse.json({ received: true, action: 'transfer_processed' });
    }

    // 3. Skip anonymous user identifiers
    if (!appUserId || appUserId.startsWith('$RCAnonymousID:')) {
      await database.from('processed_webhook_events').insert({
        event_id: eventId,
        event_type: eventType,
        app_user_id: appUserId || 'anonymous',
      });
      return NextResponse.json({ received: true, note: 'Skipped anonymous user' });
    }

    // 4. Grant / Revoke via SECURITY DEFINER Stored Procedure
    if (GRANT_EVENTS.has(eventType)) {
      await database.rpc('set_user_premium_status', {
        p_user_id: appUserId,
        p_is_premium: true,
        p_secret: webhookSecret,
      });

      await database.from('processed_webhook_events').insert({
        event_id: eventId,
        event_type: eventType,
        app_user_id: appUserId,
      });

      return NextResponse.json({ received: true, action: 'premium_granted' });
    }

    if (REVOKE_EVENTS.has(eventType)) {
      await database.rpc('set_user_premium_status', {
        p_user_id: appUserId,
        p_is_premium: false,
        p_secret: webhookSecret,
      });

      await database.from('processed_webhook_events').insert({
        event_id: eventId,
        event_type: eventType,
        app_user_id: appUserId,
      });

      return NextResponse.json({ received: true, action: 'premium_revoked' });
    }

    // Record other event types to maintain audit trail
    await database.from('processed_webhook_events').insert({
      event_id: eventId,
      event_type: eventType,
      app_user_id: appUserId,
    });

    return NextResponse.json({ received: true, action: 'unhandled_event_type' });
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : String(err);
    return NextResponse.json({ error: msg }, { status: 500 });
  }
}
