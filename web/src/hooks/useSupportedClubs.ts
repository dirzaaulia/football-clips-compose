'use client';

import { useState, useEffect } from 'react';

export interface SupportedClub {
  id: string;
  team_id?: number | null;
  name: string;
  short_name?: string;
  shortName?: string;
  competition_code?: string | null;
  competition_name?: string | null;
  leagueCode?: string | null;
  leagueName?: string | null;
  crest_url?: string | null;
  crestUrl?: string | null;
  aliases: string[];
}

const CACHE_KEY = 'fc_cached_dynamic_clubs';

export function useSupportedClubs() {
  const [clubs, setClubs] = useState<SupportedClub[]>(() => {
    if (typeof window !== 'undefined') {
      try {
        const cached = localStorage.getItem(CACHE_KEY);
        if (cached) {
          const parsed = JSON.parse(cached);
          if (Array.isArray(parsed) && parsed.length > 0) {
            return parsed;
          }
        }
      } catch {
        // Ignored
      }
    }
    return [];
  });

  useEffect(() => {
    let isCancelled = false;

    async function fetchClubs() {
      try {
        const res = await fetch('/api/clubs');
        if (!res.ok) return;
        const data: SupportedClub[] = await res.json();
        if (isCancelled || !Array.isArray(data) || data.length === 0) return;

        setClubs(data);
        if (typeof window !== 'undefined') {
          try {
            localStorage.setItem(CACHE_KEY, JSON.stringify(data));
          } catch {
            // Storage quota ignore
          }
        }
      } catch {
        // Ignored
      }
    }

    fetchClubs();
    return () => {
      isCancelled = true;
    };
  }, []);

  return clubs;
}

export function isMatchForFavoriteClubs(homeTeam: string | null, awayTeam: string | null, favoriteClubIds: Set<string>, supportedClubs: SupportedClub[]): boolean {
  if (!homeTeam || !awayTeam || favoriteClubIds.size === 0 || supportedClubs.length === 0) return false;
  const hLower = homeTeam.toLowerCase();
  const aLower = awayTeam.toLowerCase();

  const favorites = supportedClubs.filter((c) => favoriteClubIds.has(c.id));
  return favorites.some((club) => {
    const cName = club.name.toLowerCase();
    const cShort = (club.short_name || club.shortName || '').toLowerCase();

    const matchesHome = hLower.includes(cName) || (cShort && hLower.includes(cShort)) || (club.aliases && club.aliases.some((a) => hLower.includes(a.toLowerCase())));
    const matchesAway = aLower.includes(cName) || (cShort && aLower.includes(cShort)) || (club.aliases && club.aliases.some((a) => aLower.includes(a.toLowerCase())));

    return matchesHome || matchesAway;
  });
}
