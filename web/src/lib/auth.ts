'use client';

import { supabase } from './supabase';
import { setProUser, getRevenueCatClient, isProUser, REVENUECAT_API_KEY } from './revenuecat';
import type { User } from '@supabase/supabase-js';

export interface UserProfile {
  id: string;
  email?: string;
  is_premium: boolean;
}

export const GOOGLE_CLIENT_ID =
  process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID ||
  '519310318914-pushb78f44ug1a28m2oggunpck5annl8.apps.googleusercontent.com';

/**
 * Signs in with Google ID Token (Google Identity Services) without redirect
 */
export async function signInWithGoogleIdToken(idToken: string): Promise<User | null> {
  if (typeof window === 'undefined') return null;

  const { data, error } = await supabase.auth.signInWithIdToken({
    provider: 'google',
    token: idToken,
  });

  if (error) {
    console.error('Supabase signInWithIdToken error:', error.message);
    throw error;
  }

  if (data?.user) {
    await syncUserPremiumStatus(data.user);
  }

  window.dispatchEvent(new Event('fc-auth-state-changed'));
  return data?.user ?? null;
}

/**
 * Initiates Google OAuth login via Supabase fallback
 */
export async function signInWithGoogle() {
  if (typeof window === 'undefined') return;
  sessionStorage.setItem('fc-open-paywall-after-auth', 'true');
  const redirectTo = window.location.href;
  const { error } = await supabase.auth.signInWithOAuth({
    provider: 'google',
    options: {
      redirectTo,
      queryParams: {
        access_type: 'offline',
        prompt: 'consent',
      },
    },
  });
  if (error) {
    sessionStorage.removeItem('fc-open-paywall-after-auth');
    console.error('Supabase Google Sign-In error:', error.message);
    throw error;
  }
}

/**
 * Signs out current user and resets local state
 */
export async function signOutUser() {
  if (typeof window === 'undefined') return;
  await supabase.auth.signOut();
  setProUser(false);
  window.dispatchEvent(new Event('fc-auth-state-changed'));
}

/**
 * Gets current authenticated user if session exists
 */
export async function getCurrentUser(): Promise<User | null> {
  if (typeof window === 'undefined') return null;
  const { data } = await supabase.auth.getUser();
  return data?.user ?? null;
}

/**
 * Syncs premium status across Supabase profile and RevenueCat
 * 1. Checks Supabase `profiles` table (e.g., if bought via Google Play in Android app)
 * 2. Checks RevenueCat Web SDK (if bought via Paddle Web Checkout)
 * 3. Bidirectionally syncs entitlement between both sources and updates app state
 */
export async function syncUserPremiumStatus(customUser?: User | null): Promise<boolean> {
  if (typeof window === 'undefined') return false;

  const user = customUser !== undefined ? customUser : await getCurrentUser();

  if (!user) {
    return isProUser();
  }

  // 1. Query Supabase profiles table (Tier 2 fast cache)
  let cachedPremium = false;
  try {
    const { data: dbProfile, error } = await supabase
      .from('profiles')
      .select('id, is_premium, email')
      .eq('id', user.id)
      .single();

    if (!error && dbProfile?.is_premium) {
      cachedPremium = true;
    }
  } catch (err) {
    console.warn('Error reading Supabase profile:', err);
  }

  // 2. Query RevenueCat (Tier 1 Single Source of Truth)
  let rcVerified: boolean | null = null;

  try {
    const rcClient = await getRevenueCatClient(user.id);
    if (rcClient) {
      const customerInfo = await rcClient.getCustomerInfo();
      rcVerified = Object.keys(customerInfo.entitlements.active).length > 0;
    }
  } catch (err) {
    console.warn('Error verifying RevenueCat client SDK:', err);
  }

  // Fallback to RevenueCat REST API if SDK check was inconclusive
  if (rcVerified === null && REVENUECAT_API_KEY) {
    try {
      const res = await fetch(`https://api.revenuecat.com/v1/subscribers/${user.id}`, {
        headers: { Authorization: `Bearer ${REVENUECAT_API_KEY}` },
      });
      if (res.ok) {
        const body = await res.json();
        const ents = body?.subscriber?.entitlements || {};
        rcVerified = Object.values(ents).some(
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          (e: any) => !e.expires_date || new Date(e.expires_date) > new Date()
        );
      }
    } catch (e) {
      console.warn('Error checking RevenueCat REST API:', e);
    }
  }

  // 3. Determine final status:
  // RevenueCat is Tier 1 truth (both active & expired). If unreachable, fallback to Tier 2 cache.
  const finalPremium = rcVerified !== null ? rcVerified : cachedPremium;

  // 4. Bidirectional Sync: update Supabase cache if mismatch detected (grant or revoke)
  if (rcVerified !== null && rcVerified !== cachedPremium) {
    try {
      await supabase.from('profiles').upsert({
        id: user.id,
        email: user.email,
        is_premium: rcVerified,
      });
    } catch (e) {
      console.warn('Error syncing Supabase profiles cache:', e);
    }
  }

  // 5. Update Tier 3 application PRO state (disables AdSense ads globally)
  setProUser(finalPremium);
  return finalPremium;
}
