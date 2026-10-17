'use client';

import { useSyncExternalStore, useCallback } from 'react';

export interface SeasonOption {
  year: number;
  label: string;
  shortLabel: string;
  isCurrent: boolean;
}

export const AVAILABLE_SEASONS: SeasonOption[] = [
  { year: 2026, label: '2026/27 (Current)', shortLabel: '2026/27', isCurrent: true },
  { year: 2025, label: '2025/26 (Archive)', shortLabel: '2025/26', isCurrent: false },
];

export const DEFAULT_SEASON = 2026;

function subscribe(callback: () => void) {
  if (typeof window === 'undefined') return () => {};
  window.addEventListener('fc-season-change', callback);
  window.addEventListener('storage', callback);
  return () => {
    window.removeEventListener('fc-season-change', callback);
    window.removeEventListener('storage', callback);
  };
}

function getSnapshot(): number {
  if (typeof window === 'undefined') return DEFAULT_SEASON;
  const stored = localStorage.getItem('fc-selected-season');
  if (stored) {
    const parsed = parseInt(stored, 10);
    if (!isNaN(parsed) && AVAILABLE_SEASONS.some((s) => s.year === parsed)) {
      return parsed;
    }
  }
  return DEFAULT_SEASON;
}

function getServerSnapshot(): number {
  return DEFAULT_SEASON;
}

export function setSeasonPreference(season: number) {
  if (typeof window === 'undefined') return;
  localStorage.setItem('fc-selected-season', String(season));
  document.cookie = `fc-selected-season=${season}; path=/; max-age=31536000; SameSite=Lax`;
  window.dispatchEvent(new CustomEvent('fc-season-change', { detail: { season } }));
}

export function useSeason() {
  const selectedSeason = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);

  const setSeason = useCallback((season: number) => {
    setSeasonPreference(season);
  }, []);

  const currentOption = AVAILABLE_SEASONS.find((s) => s.year === selectedSeason) || AVAILABLE_SEASONS[0];
  const isArchiveSeason = selectedSeason !== DEFAULT_SEASON;

  return {
    selectedSeason,
    setSeason,
    currentOption,
    isArchiveSeason,
    availableSeasons: AVAILABLE_SEASONS,
  };
}
