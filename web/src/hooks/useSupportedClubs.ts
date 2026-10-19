'use client';

import { useState, useEffect } from 'react';
import { SUPPORTED_CLUBS, SupportedClub } from '@/lib/clubs';

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
    return SUPPORTED_CLUBS;
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
        // Fallback intact
      }
    }

    fetchClubs();
    return () => {
      isCancelled = true;
    };
  }, []);

  return clubs;
}
