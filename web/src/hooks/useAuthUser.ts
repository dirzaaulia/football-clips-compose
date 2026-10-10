'use client';

import { useState, useEffect } from 'react';
import { supabase } from '@/lib/supabase';
import { getCurrentUser, syncUserPremiumStatus } from '@/lib/auth';
import type { User } from '@supabase/supabase-js';

export function useAuthUser() {
  const [user, setUser] = useState<User | null>(null);
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

    const {
      data: { subscription },
    } = supabase.auth.onAuthStateChange(async (_event, session) => {
      const authUser = session?.user ?? null;
      if (isMounted) {
        setUser(authUser);
        if (authUser) {
          await syncUserPremiumStatus(authUser);
          if (typeof window !== 'undefined' && sessionStorage.getItem('fc-open-paywall-after-auth') === 'true') {
            sessionStorage.removeItem('fc-open-paywall-after-auth');
            window.dispatchEvent(new CustomEvent('fc-open-paywall'));
          }
        }
      }
    });

    const onCustomAuthChange = () => {
      load();
    };

    window.addEventListener('fc-auth-state-changed', onCustomAuthChange);

    return () => {
      isMounted = false;
      subscription.unsubscribe();
      window.removeEventListener('fc-auth-state-changed', onCustomAuthChange);
    };
  }, []);

  return { user, isLoading };
}
