'use client';

import { useSyncExternalStore } from 'react';
import { isProUser } from '@/lib/revenuecat';

function subscribe(callback: () => void) {
  if (typeof window === 'undefined') return () => {};
  window.addEventListener('fc-pro-status-changed', callback);
  window.addEventListener('storage', callback);
  return () => {
    window.removeEventListener('fc-pro-status-changed', callback);
    window.removeEventListener('storage', callback);
  };
}

function getSnapshot(): boolean {
  return isProUser();
}

function getServerSnapshot(): boolean {
  return false;
}

export function useProStatus(): boolean {
  return useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);
}
