'use client';

import { useSyncExternalStore, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { MatchRecord } from '@/lib/database';

interface VideoPlayerState {
  activeMatch: MatchRecord | null;
  isMinimized: boolean;
  isSideSheetOpen: boolean;
}

let playerState: VideoPlayerState = {
  activeMatch: null,
  isMinimized: false,
  isSideSheetOpen: false,
};

const listeners = new Set<() => void>();

function emitChange() {
  for (const listener of listeners) {
    listener();
  }
}

function subscribe(callback: () => void) {
  listeners.add(callback);
  return () => {
    listeners.delete(callback);
  };
}

function getSnapshot(): VideoPlayerState {
  return playerState;
}

const serverSnapshot: VideoPlayerState = {
  activeMatch: null,
  isMinimized: false,
  isSideSheetOpen: false,
};

function getServerSnapshot(): VideoPlayerState {
  return serverSnapshot;
}

export function playVideoGlobal(match: MatchRecord) {
  playerState = {
    activeMatch: match,
    isMinimized: false,
    isSideSheetOpen: true,
  };
  emitChange();
}

export function minimizeVideoGlobal() {
  if (!playerState.activeMatch) return;
  playerState = {
    ...playerState,
    isMinimized: true,
    isSideSheetOpen: false,
  };
  emitChange();
}

export function restoreVideoGlobal() {
  if (!playerState.activeMatch) return;
  playerState = {
    ...playerState,
    isMinimized: false,
    isSideSheetOpen: true,
  };
  emitChange();
}

export function closeVideoGlobal() {
  playerState = {
    activeMatch: null,
    isMinimized: false,
    isSideSheetOpen: false,
  };
  emitChange();
}

export function useVideoPlayer() {
  const router = useRouter();
  const state = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);

  const playVideo = useCallback((match: MatchRecord) => {
    // Navigate directly to the dedicated full-page Match Centre screen
    if (match?.id) {
      const params = new URLSearchParams();
      if (match.id.startsWith('mw-')) {
        params.set('home', match.home_team_name || match.home_team);
        params.set('away', match.away_team_name || match.away_team);
        params.set('comp', match.competition_name);
        params.set('vid', match.highlight_video_id || '');
        params.set('date', match.utc_date);
        if (match.home_team_crest) params.set('homeCrest', match.home_team_crest);
        if (match.away_team_crest) params.set('awayCrest', match.away_team_crest);
      }
      router.push(`/match/${match.id}${params.size ? `?${params}` : ''}`);
    }
  }, [router]);

  const minimize = useCallback(() => {
    minimizeVideoGlobal();
  }, []);

  const restore = useCallback(() => {
    restoreVideoGlobal();
  }, []);

  const closePlayer = useCallback(() => {
    closeVideoGlobal();
  }, []);

  return {
    activeMatch: state.activeMatch,
    isMinimized: state.isMinimized,
    isSideSheetOpen: state.isSideSheetOpen,
    playVideo,
    minimize,
    restore,
    closePlayer,
  };
}
