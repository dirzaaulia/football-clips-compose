'use client';

import { useState, useEffect } from 'react';

export interface Competition {
  name: string;
  leagueId: number | null;
  countryCode: string | null;
  competitionId: string | null;
  logo: string;
  type?: 'league' | 'hybrid' | 'cup';
}

interface DynamicCompetitionDto {
  id: string;
  name: string;
  short_name: string;
  country_code: string | null;
  emblem_url: string | null;
  league_id: number | null;
  type?: 'league' | 'hybrid' | 'cup';
}

const CACHE_KEY = 'fc_cached_dynamic_competitions_v2';

export function useCompetitions() {
  const [competitions, setCompetitions] = useState<Competition[]>(() => {
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
    return [
      {
        name: 'All Competitions',
        leagueId: null,
        countryCode: null,
        competitionId: 'ALL',
        logo: '',
      }
    ];
  });

  useEffect(() => {
    let isCancelled = false;

    async function fetchCompetitions() {
      try {
        const res = await fetch('/api/competitions');
        if (!res.ok) return;
        const data: DynamicCompetitionDto[] = await res.json();
        if (isCancelled || !Array.isArray(data) || data.length === 0) return;

        const allLeaguesOption: Competition = {
          name: 'All Competitions',
          leagueId: null,
          countryCode: null,
          competitionId: 'ALL',
          logo: '',
        };

        const mapped: Competition[] = [
          allLeaguesOption,
          ...data.map((c) => ({
            name: c.name,
            leagueId: c.league_id,
            countryCode: c.country_code,
            competitionId: c.id,
            logo: c.emblem_url || '',
            type: c.type || 'league',
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

export function isLeagueMatch(target?: string | null, candidate?: string | null): boolean {
  if (!target || !candidate) return false;
  const t = target.toLowerCase().trim();
  const c = candidate.toLowerCase().trim();
  if (t === c) return true;

  const plRegex = /\b(pl|epl|premier league)\b/i;
  if (plRegex.test(t) && plRegex.test(c)) return true;

  const laligaRegex = /\b(la liga|laliga|primera division|primera división|pd)\b/i;
  if (laligaRegex.test(t) && laligaRegex.test(c)) return true;

  return t.includes(c) || c.includes(t);
}
