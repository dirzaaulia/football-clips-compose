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

function getOrCreateDeviceId(): string {
  if (typeof window === 'undefined') return 'web-ssr';
  let deviceId = localStorage.getItem('fc_device_id');
  if (!deviceId) {
    deviceId = `web-${Math.random().toString(36).substring(2, 11)}-${Date.now().toString(36)}`;
    localStorage.setItem('fc_device_id', deviceId);
  }
  return deviceId;
}

function getOrCreateSessionId(): string {
  if (typeof window === 'undefined') return 'session-ssr';
  let sessionId = sessionStorage.getItem('fc_session_id');
  if (!sessionId) {
    sessionId = `sess-${Math.random().toString(36).substring(2, 9)}-${Date.now().toString(36)}`;
    sessionStorage.setItem('fc_session_id', sessionId);
  }
  return sessionId;
}

export async function trackEvent(
  eventName: string,
  properties: Record<string, any> = {},
  userId?: string | null,
): Promise<void> {
  const deviceId = getOrCreateDeviceId();
  const sessionId = getOrCreateSessionId();

  // 1. Google Analytics
  if (typeof window !== 'undefined' && (window as any).gtag) {
    try {
      (window as any).gtag('event', eventName, {
        ...properties,
        session_id: sessionId,
        device_id: deviceId,
        user_id: userId || undefined,
      });
    } catch {
      // Ignore
    }
  }

  // 2. Dual-stream to PostgreSQL via Edge PostgREST
  try {
    let deviceName: string | undefined;
    let country: string | undefined;
    let ipAddress: string | undefined;
    if (typeof window !== 'undefined') {
      try {
        const { getClientDeviceName, getClientGeo } = await import('@/lib/telemetry/device-sync');
        deviceName = getClientDeviceName();
        const geo = await getClientGeo();
        country = geo.country;
        ipAddress = geo.ip;
      } catch {
        // Ignore client detection error
      }
    }

    const payload = {
      event_name: eventName,
      user_id: userId || null,
      device_id: deviceId,
      platform: 'web',
      device_name: deviceName,
      country,
      ip_address: ipAddress,
      payload: {
        ...properties,
        session_id: sessionId,
      },
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

export function trackScreenView(screenName: string, tab?: string) {
  trackEvent('screen_view', { screen_name: screenName, tab });
}

export function trackMatchClick(matchId: string, matchTitle: string, competitionName?: string) {
  trackEvent('match_click', {
    match_id: matchId,
    match_title: matchTitle,
    competition_name: competitionName,
  });
}

export function trackVideoPlay(videoId: string, matchTitle: string, duration?: number) {
  trackEvent('video_play', {
    video_id: videoId,
    match_title: matchTitle,
    duration,
  });
}

export function trackWatchOnYouTube(videoId: string, matchTitle?: string) {
  trackEvent('watch_on_youtube', {
    video_id: videoId,
    match_title: matchTitle,
  });
}

export function trackCompetitionFilter(competitionId: string, competitionName: string) {
  trackEvent('competition_filter', {
    competition_id: competitionId,
    competition_name: competitionName,
  });
}

export function trackClubFavorite(clubId: number | string, clubName: string, action: 'add' | 'remove') {
  trackEvent('club_favorite', {
    club_id: clubId,
    club_name: clubName,
    action,
  });
}

export function trackTabSwitch(tabName: string) {
  trackEvent('tab_switch', { tab_name: tabName });
}

export function trackSessionStart() {
  trackEvent('session_start', {
    app_version: '3.0.1',
    user_agent: typeof navigator !== 'undefined' ? navigator.userAgent : 'unknown',
  });
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
