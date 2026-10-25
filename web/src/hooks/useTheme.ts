'use client';

import { useSyncExternalStore, useCallback } from 'react';

export type Theme = 'dark' | 'light';

function subscribe(callback: () => void) {
  if (typeof window === 'undefined') return () => {};
  window.addEventListener('fc-theme-change', callback);
  window.addEventListener('storage', callback);
  return () => {
    window.removeEventListener('fc-theme-change', callback);
    window.removeEventListener('storage', callback);
  };
}

function getSnapshot(): Theme {
  if (typeof window === 'undefined') return 'dark';
  const saved = localStorage.getItem('fc-theme');
  if (saved === 'light' || saved === 'dark') return saved;
  return 'dark';
}

function getServerSnapshot(): Theme {
  return 'dark';
}

export function setThemePreference(theme: Theme) {
  if (typeof window === 'undefined') return;
  localStorage.setItem('fc-theme', theme);
  document.cookie = `fc-theme=${theme}; path=/; max-age=31536000; SameSite=Lax`;
  if (theme === 'dark') {
    document.documentElement.classList.add('dark');
    document.documentElement.classList.remove('light');
  } else {
    document.documentElement.classList.remove('dark');
    document.documentElement.classList.add('light');
  }
  window.dispatchEvent(new Event('fc-theme-change'));
}

export function useTheme() {
  const theme = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);
  const isDark = theme === 'dark';

  const toggleTheme = useCallback(() => {
    if (typeof window === 'undefined') return;
    const nextTheme: Theme = getSnapshot() === 'dark' ? 'light' : 'dark';
    setThemePreference(nextTheme);
  }, []);

  return { theme, isDark, toggleTheme, setTheme: setThemePreference };
}
