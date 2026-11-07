'use client';

const DEVICE_ID_KEY = 'fc-telemetry-device-id';
const ANON_USER_ID_KEY = 'fc-telemetry-anon-user-id';
const SYNC_ENDPOINT = 'https://fc-device-sync-519310318914.us-central1.run.app/api/device-sync';

function getOrGenerateId(key: string, prefix: string): string {
  if (typeof window === 'undefined') return `${prefix}_server`;
  let val = localStorage.getItem(key);
  if (!val) {
    val = `${prefix}_${Math.random().toString(36).substring(2, 10)}${Date.now().toString(36)}`;
    try {
      localStorage.setItem(key, val);
    } catch {
      // Ignore storage errors
    }
  }
  return val;
}

export function getWebDeviceId(): string {
  return getOrGenerateId(DEVICE_ID_KEY, 'dev_web');
}

export function getWebUserId(authUserId?: string | null): string {
  if (authUserId) return authUserId;
  return getOrGenerateId(ANON_USER_ID_KEY, 'usr_anon');
}

export async function getClientGeo(): Promise<{ ip?: string; country?: string }> {
  if (typeof window === 'undefined') return {};
  try {
    const cached = sessionStorage.getItem('fc_client_geo');
    if (cached) return JSON.parse(cached);
    const res = await fetch('https://api.country.is/', { signal: AbortSignal.timeout(2000) });
    if (res.ok) {
      const data = await res.json();
      sessionStorage.setItem('fc_client_geo', JSON.stringify(data));
      return data;
    }
  } catch {
    // Non-blocking geo lookup
  }
  return {};
}

export function getClientDeviceName(): string {
  if (typeof window === 'undefined') return 'Web Browser';
  const ua = navigator.userAgent;
  let browser = 'Browser';
  if (ua.includes('Edg/')) browser = 'Edge';
  else if (ua.includes('Chrome/')) browser = 'Chrome';
  else if (ua.includes('Safari/') && !ua.includes('Chrome')) browser = 'Safari';
  else if (ua.includes('Firefox/')) browser = 'Firefox';

  let os = 'Web';
  if (ua.includes('Windows NT 10.0')) os = 'Windows 10/11';
  else if (ua.includes('Windows')) os = 'Windows';
  else if (ua.includes('Macintosh')) os = 'macOS';
  else if (ua.includes('iPhone')) os = 'iPhone';
  else if (ua.includes('iPad')) os = 'iPad';
  else if (ua.includes('Android')) os = 'Android Web';
  else if (ua.includes('Linux')) os = 'Linux';

  return `${browser} on ${os}`;
}

export async function syncWebDeviceClubs(
  clubs: number[],
  authUserId?: string | null,
): Promise<void> {
  if (typeof window === 'undefined') return;

  const deviceId = getWebDeviceId();
  const userId = getWebUserId(authUserId);
  const deviceName = getClientDeviceName();
  const geo = await getClientGeo();

  try {
    await fetch(SYNC_ENDPOINT, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        device_id: deviceId,
        user_id: userId,
        platform: 'web',
        device_name: deviceName,
        favorite_clubs: clubs,
        app_version: '2.14.8',
        country: geo.country,
        ip_address: geo.ip,
      }),
      keepalive: true,
    });
  } catch (err) {
    // Non-blocking telemetry sync failure
  }
}

