'use client';

import { useState, useEffect } from 'react';
import { getCurrentUser, syncUserPremiumStatus } from '@/lib/auth';
import type { AuthUser } from '@/lib/auth';

export function useAuthUser() {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;

    async function load() {
      try {
        const currentUser = await getCurrentUser();
        if (isMounted) {
          setUser(currentUser);
          if (currentUser) {
            await syncUserPremiumStatus(currentUser);
          }
        }
      } catch (e) {
        console.warn('Auth user load failed:', e);
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    load();

    const onCustomAuthChange = () => {
      load();
    };

    window.addEventListener('fc-auth-state-changed', onCustomAuthChange);
    window.addEventListener('storage', onCustomAuthChange);

    return () => {
      isMounted = false;
      window.removeEventListener('fc-auth-state-changed', onCustomAuthChange);
      window.removeEventListener('storage', onCustomAuthChange);
    };
  }, []);

  return { user, isLoading };
}
