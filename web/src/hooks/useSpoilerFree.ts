'use client';

import { useSyncExternalStore, useCallback } from 'react';

function subscribe(callback: () => void) {
  if (typeof window === 'undefined') return () => {};
  window.addEventListener('fc-spoiler-toggle', callback);
  window.addEventListener('storage', callback);
  return () => {
    window.removeEventListener('fc-spoiler-toggle', callback);
    window.removeEventListener('storage', callback);
  };
}

function getSnapshot(): boolean {
  if (typeof window === 'undefined') return false;
  return (
    localStorage.getItem('fc-spoiler-free') === 'true' ||
    localStorage.getItem('is_spoiler_free_mode') === 'true'
  );
}

function getServerSnapshot(): boolean {
  return false;
}

export function setSpoilerPreference(enabled: boolean) {
  if (typeof window === 'undefined') return;
  localStorage.setItem('fc-spoiler-free', String(enabled));
  localStorage.setItem('is_spoiler_free_mode', String(enabled));
  localStorage.setItem('is_spoiler_free_configured', 'true');
  document.cookie = `fc-spoiler-free=${enabled}; path=/; max-age=31536000; SameSite=Lax`;
  window.dispatchEvent(new Event('fc-spoiler-toggle'));
}

export function useSpoilerFree() {
  const isSpoilerFree = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);

  const toggleSpoilerFree = useCallback(() => {
    if (typeof window === 'undefined') return;
    const current = getSnapshot();
    setSpoilerPreference(!current);
  }, []);

  return { isSpoilerFree, toggleSpoilerFree, setSpoilerPreference };
}

