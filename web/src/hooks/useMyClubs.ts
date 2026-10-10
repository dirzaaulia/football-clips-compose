'use client';

import { useSyncExternalStore } from 'react';

const STORAGE_KEY = 'fc-favorite-clubs';

let memoryFavoriteClubs: Set<string> = new Set();
let isInitialized = false;
const listeners = new Set<() => void>();

function initClubs() {
  if (typeof window === 'undefined' || isInitialized) return;
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) {
      const arr = JSON.parse(raw);
      if (Array.isArray(arr)) {
        memoryFavoriteClubs = new Set(arr);
      }
    }
  } catch {
    memoryFavoriteClubs = new Set();
  }
  isInitialized = true;
}

function emitChange() {
  listeners.forEach((listener) => listener());
}

export function subscribeMyClubs(listener: () => void) {
  initClubs();
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
}

export function getMyClubsSnapshot(): Set<string> {
  initClubs();
  return memoryFavoriteClubs;
}

const EMPTY_FAVORITE_CLUBS_SET: Set<string> = new Set();

export function getServerMyClubsSnapshot(): Set<string> {
  return EMPTY_FAVORITE_CLUBS_SET;
}

export function toggleFavoriteClubAction(clubId: string) {
  initClubs();
  const next = new Set(memoryFavoriteClubs);
  if (next.has(clubId)) {
    next.delete(clubId);
  } else {
    next.add(clubId);
  }
  memoryFavoriteClubs = next;

  if (typeof window !== 'undefined') {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(Array.from(next)));
    } catch {
      // Ignore quota errors
    }
  }
  emitChange();
}

export function clearFavoritesAction() {
  initClubs();
  memoryFavoriteClubs = new Set();
  if (typeof window !== 'undefined') {
    localStorage.removeItem(STORAGE_KEY);
  }
  emitChange();
}

export function useMyClubs() {
  const favoriteClubs = useSyncExternalStore(
    subscribeMyClubs,
    getMyClubsSnapshot,
    getServerMyClubsSnapshot
  );

  return {
    favoriteClubs,
    favoriteCount: favoriteClubs.size,
    hasClub: (id: string) => favoriteClubs.has(id),
    toggleClub: toggleFavoriteClubAction,
    clearAll: clearFavoritesAction,
  };
}
