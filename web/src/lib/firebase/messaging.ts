import { initializeApp, getApps } from 'firebase/app';
import { getMessaging, getToken, onMessage, Messaging } from 'firebase/messaging';

const firebaseConfig = {
  projectId: 'highlightdata',
  messagingSenderId: '768846387082',
  appId: '1:768846387082:web:71d9d9361a0b3c66099b22'
};

function getFirebaseMessaging(): Messaging | null {
  if (typeof window === 'undefined' || !('Notification' in window) || !('serviceWorker' in navigator)) {
    return null;
  }
  const app = getApps().length === 0 ? initializeApp(firebaseConfig) : getApps()[0];
  try {
    return getMessaging(app);
  } catch {
    return null;
  }
}

export async function requestWebNotificationPermission(): Promise<{
  granted: boolean;
  token: string | null;
  status: NotificationPermission;
}> {
  if (typeof window === 'undefined' || !('Notification' in window)) {
    return { granted: false, token: null, status: 'denied' };
  }

  const currentPermission = Notification.permission;
  if (currentPermission === 'denied') {
    return { granted: false, token: null, status: 'denied' };
  }

  const permission = await Notification.requestPermission();
  if (permission !== 'granted') {
    return { granted: false, token: null, status: permission };
  }

  try {
    const messaging = getFirebaseMessaging();
    if (!messaging) {
      return { granted: true, token: null, status: 'granted' };
    }

    const registration = await navigator.serviceWorker.register('/firebase-messaging-sw.js');
    const token = await getToken(messaging, {
      serviceWorkerRegistration: registration
    });

    if (token && typeof window !== 'undefined') {
      localStorage.setItem('fc_fcm_web_token', token);
    }

    return { granted: true, token, status: 'granted' };
  } catch (err) {
    console.warn('[FCM Web] Failed to acquire token:', err);
    return { granted: true, token: null, status: 'granted' };
  }
}

export function getStoredWebPushToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('fc_fcm_web_token');
}

const WORKER_SUBSCRIPTION_URL = 'https://fc-worker-519310318914.us-central1.run.app/notifications/subscribe';

export async function syncWebPushSubscription(
  token: string,
  clubIds: (string | number)[],
  action: 'subscribe' | 'unsubscribe' = 'subscribe'
): Promise<boolean> {
  if (!token || clubIds.length === 0) return false;
  try {
    const res = await fetch(WORKER_SUBSCRIPTION_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        token,
        club_ids: clubIds,
        action,
        platform: 'web',
        device_name: typeof navigator !== 'undefined' ? `${navigator.userAgent.slice(0, 50)}` : 'Web Browser'
      })
    });
    return res.ok;
  } catch (err) {
    console.warn('[FCM Web] Failed to sync subscription with worker:', err);
    return false;
  }
}

export function setupForegroundMessageListener(onNotification: (payload: { title: string; body: string; url?: string }) => void) {
  const messaging = getFirebaseMessaging();
  if (!messaging) return () => {};

  return onMessage(messaging, (payload) => {
    const title = payload.notification?.title || payload.data?.title || 'Football Highlights & Clips Alert';
    const body = payload.notification?.body || payload.data?.body || '';
    const url = payload.data?.url || '/';
    onNotification({ title, body, url });
  });
}
