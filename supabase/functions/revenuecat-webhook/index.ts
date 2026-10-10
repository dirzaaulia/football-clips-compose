// Supabase Edge Function for RevenueCat Webhooks
import { serve } from 'https://deno.land/std@0.177.0/http/server.ts';
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2.39.0';

const supabaseUrl = Deno.env.get('SUPABASE_URL') || '';
const supabaseKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') || '';
const webhookSecret = Deno.env.get('REVENUECAT_WEBHOOK_SECRET') || '';

const supabaseAdmin = createClient(supabaseUrl, supabaseKey);

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

serve(async (req) => {
  try {
    if (webhookSecret) {
      const authHeader = req.headers.get('authorization');
      if (authHeader !== `Bearer ${webhookSecret}`) {
        return new Response(JSON.stringify({ error: 'Unauthorized' }), { status: 401 });
      }
    }

    const body = await req.json();
    const event = body?.event;

    if (!event || !event.type) {
      return new Response(JSON.stringify({ error: 'Invalid event payload' }), { status: 400 });
    }

    const eventType: string = event.type;
    const appUserId: string = event.app_user_id;

    if (eventType === 'TRANSFER') {
      const transferredFrom: string[] = event.transferred_from || [];
      const transferredTo: string[] = event.transferred_to || [];

      for (const oldUid of transferredFrom) {
        if (!oldUid.startsWith('$RCAnonymousID:')) {
          await supabaseAdmin.from('profiles').update({ is_premium: false }).eq('id', oldUid);
        }
      }

      for (const newUid of transferredTo) {
        if (!newUid.startsWith('$RCAnonymousID:')) {
          await supabaseAdmin.from('profiles').update({ is_premium: true }).eq('id', newUid);
        }
      }

      return new Response(JSON.stringify({ received: true, action: 'transfer_processed' }), { status: 200 });
    }

    if (!appUserId || appUserId.startsWith('$RCAnonymousID:')) {
      return new Response(JSON.stringify({ received: true, note: 'Skipped anonymous user' }), { status: 200 });
    }

    if (GRANT_EVENTS.has(eventType)) {
      await supabaseAdmin.from('profiles').update({ is_premium: true }).eq('id', appUserId);
      return new Response(JSON.stringify({ received: true, action: 'premium_granted' }), { status: 200 });
    }

    if (REVOKE_EVENTS.has(eventType)) {
      await supabaseAdmin.from('profiles').update({ is_premium: false }).eq('id', appUserId);
      return new Response(JSON.stringify({ received: true, action: 'premium_revoked' }), { status: 200 });
    }

    return new Response(JSON.stringify({ received: true, action: 'unhandled_event_type' }), { status: 200 });
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : String(err);
    return new Response(JSON.stringify({ error: msg }), { status: 500 });
  }
});
