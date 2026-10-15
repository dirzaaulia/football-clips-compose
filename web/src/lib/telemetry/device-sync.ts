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

export async function syncWebDeviceClubs(
  clubs: number[],
  authUserId?: string | null
): Promise<void> {
  if (typeof window === 'undefined') return;

  const deviceId = getWebDeviceId();
  const userId = getWebUserId(authUserId);
  const navData = navigator as unknown as { userAgentData?: { platform?: string } };
  const platformName = navData?.userAgentData?.platform || navigator.platform || 'Web';
  const browserName = navigator.userAgent.includes('Chrome') ? 'Chrome' : 'Browser';
  const deviceName = `${platformName} (${browserName})`;

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
      }),
      keepalive: true,
    });
  } catch (err) {
    // Non-blocking telemetry sync failure
  }
}
