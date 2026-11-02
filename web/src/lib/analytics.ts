/**
 * Dual-stream analytics tracker for Web:
 * 1. Dispatches to Google Analytics (gtag) if loaded in browser
 * 2. Writes to public.user_analytics_events via edge PostgREST
 */

export interface AnalyticsEventParams {
  eventName: string;
  userId?: string | null;
  properties?: Record<string, any>;
}

const EDGE_POSTGREST_URL = 'https://edge.fc.dirzaaulia.com/rest/v1';

export async function trackEvent(
  eventName: string,
  properties: Record<string, any> = {},
  userId?: string | null
): Promise<void> {
  // 1. Google Analytics
  if (typeof window !== 'undefined' && (window as any).gtag) {
    try {
      (window as any).gtag('event', eventName, {
        ...properties,
        user_id: userId || undefined,
      });
    } catch {
      // Ignore
    }
  }

  // 2. Dual-stream to PostgreSQL via Edge PostgREST
  try {
    const payload = {
      event_name: eventName,
      user_id: userId || null,
      platform: 'web',
      payload: properties || {},
    };

    if (typeof window !== 'undefined') {
      fetch(`${EDGE_POSTGREST_URL}/user_analytics_events`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Prefer': 'return=minimal',
        },
        body: JSON.stringify(payload),
      }).catch(() => {});
    }
  } catch {
    // Analytics failure must never disrupt UX
  }
}

export function trackMatchView(matchId: string, homeTeam: string, awayTeam: string) {
  trackEvent('match_view', { match_id: matchId, home: homeTeam, away: awayTeam });
}

export function trackSignIn(userId?: string | null, email?: string | null) {
  trackEvent('sign_in_success', { email }, userId);
}

export function trackSignOut(userId?: string | null) {
  trackEvent('sign_out', {}, userId);
}

export function trackPaywallView(source: string) {
  trackEvent('paywall_view', { source });
}

export function trackPurchaseAttempt(productId: string) {
  trackEvent('purchase_attempted', { product_id: productId });
}

export function trackPurchaseSuccess(productId: string) {
  trackEvent('purchase_completed', { product_id: productId });
}

export function trackClubFollow(clubName: string, followed: boolean) {
  trackEvent('favorite_club_toggle', {
    club: clubName,
    action: followed ? 'follow' : 'unfollow',
  });
}

export function trackCheckUpdates() {
  trackEvent('check_for_updates_clicked', { platform: 'web' });
}
