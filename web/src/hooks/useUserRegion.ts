'use client';

import { useState, useEffect } from 'react';

let cachedCountry: string | null = null;

export function useUserRegion(): { country: string | null; isLoading: boolean } {
  const [country, setCountry] = useState<string | null>(cachedCountry);
  const [isLoading, setIsLoading] = useState<boolean>(!cachedCountry);

  useEffect(() => {
    if (cachedCountry) {
      setCountry(cachedCountry);
      setIsLoading(false);
      return;
    }

    let isMounted = true;

    async function detectCountry() {
      try {
        const stored = typeof window !== 'undefined' ? sessionStorage.getItem('fc_user_country') : null;
        if (stored) {
          cachedCountry = stored;
          if (isMounted) {
            setCountry(stored);
            setIsLoading(false);
          }
          return;
        }

        const res = await fetch('https://edge.fc.dirzaaulia.com/api/geo', {
          headers: { Accept: 'application/json' },
          cache: 'force-cache',
        });
        if (res.ok) {
          const data = await res.json();
          if (data?.country && typeof data.country === 'string') {
            const countryCode = data.country.toUpperCase();
            cachedCountry = countryCode;
            if (typeof window !== 'undefined') {
              sessionStorage.setItem('fc_user_country', countryCode);
            }
            if (isMounted) {
              setCountry(countryCode);
              setIsLoading(false);
            }
            return;
          }
        }
      } catch {
        // Fallback gracefully on network failure
      }

      // Fallback: estimate from locale or default to US
      try {
        const locale = typeof navigator !== 'undefined' ? navigator.language : 'en-US';
        const parts = locale.split('-');
        const detected = parts.length > 1 ? parts[1].toUpperCase() : 'US';
        cachedCountry = detected;
        if (isMounted) {
          setCountry(detected);
          setIsLoading(false);
        }
      } catch {
        if (isMounted) {
          setCountry('US');
          setIsLoading(false);
        }
      }
    }

    detectCountry();

    return () => {
      isMounted = false;
    };
  }, []);

  return { country, isLoading };
}
