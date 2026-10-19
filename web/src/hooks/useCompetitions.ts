'use client';

import { useState, useEffect } from 'react';
import { BIG_LEAGUES, BigLeague } from '@/lib/constants';

interface DynamicCompetitionDto {
  id: string;
  name: string;
  short_name: string;
  country_code: string | null;
  emblem_url: string | null;
  league_id: number | null;
}

const CACHE_KEY = 'fc_cached_dynamic_competitions';

export function useCompetitions() {
  const [competitions, setCompetitions] = useState<BigLeague[]>(() => {
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
    return BIG_LEAGUES;
  });

  useEffect(() => {
    let isCancelled = false;

    async function fetchCompetitions() {
      try {
        const res = await fetch('/api/competitions');
        if (!res.ok) return;
        const data: DynamicCompetitionDto[] = await res.json();
        if (isCancelled || !Array.isArray(data) || data.length === 0) return;

        const allLeaguesOption: BigLeague = {
          name: 'All Competitions',
          leagueId: null,
          countryCode: null,
          competitionId: 'ALL',
          logo: '',
        };

        const mapped: BigLeague[] = [
          allLeaguesOption,
          ...data.map((c) => ({
            name: c.name,
            leagueId: c.league_id,
            countryCode: c.country_code,
            competitionId: c.id,
            logo: c.emblem_url || '',
          })),
        ];

        setCompetitions(mapped);
        if (typeof window !== 'undefined') {
          try {
            localStorage.setItem(CACHE_KEY, JSON.stringify(mapped));
          } catch {
            // Storage quota ignore
          }
        }
      } catch {
        // Keep existing fallback
      }
    }

    fetchCompetitions();
    return () => {
      isCancelled = true;
    };
  }, []);

  return competitions;
}
