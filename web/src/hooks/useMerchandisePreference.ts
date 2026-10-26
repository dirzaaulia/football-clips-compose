'use client';

import { useSyncExternalStore } from 'react';
import { useProStatus } from '@/hooks/useProStatus';

const STORAGE_KEY = 'fc-merchandise-enabled';
const EVENT_NAME = 'fc-merchandise-toggle';

function subscribe(callback: () => void) {
  if (typeof window === 'undefined') return () => {};
  window.addEventListener(EVENT_NAME, callback);
  window.addEventListener('storage', callback);
  return () => {
    window.removeEventListener(EVENT_NAME, callback);
    window.removeEventListener('storage', callback);
  };
}

function getStoredPreference(): boolean {
  if (typeof window === 'undefined') return true;
  const stored = localStorage.getItem(STORAGE_KEY);
  if (stored === null) return true;
  return stored === 'true';
}

function getServerSnapshot(): boolean {
  return true;
}

export function setMerchandisePreference(enabled: boolean) {
  if (typeof window === 'undefined') return;
  localStorage.setItem(STORAGE_KEY, String(enabled));
  window.dispatchEvent(new Event(EVENT_NAME));
}

export function useMerchandisePreference(): {
  isMerchandiseEnabled: boolean;
  rawPreference: boolean;
  setMerchandiseEnabled: (enabled: boolean) => void;
} {
  const isPro = useProStatus();
  const rawPreference = useSyncExternalStore(subscribe, getStoredPreference, getServerSnapshot);

  // For Pro subscribers, respect their explicit setting; for free users, merchandise remains active
  const isMerchandiseEnabled = isPro ? rawPreference : true;

  return {
    isMerchandiseEnabled,
    rawPreference,
    setMerchandiseEnabled: setMerchandisePreference,
  };
}
