'use client';

import { supabase } from './database';
import { setProUser, getRevenueCatClient, isProUser, REVENUECAT_API_KEY } from './revenuecat';

export interface AuthUser {
  id: string;
  email: string;
  display_name?: string;
  avatar_url?: string;
  is_premium?: boolean;
}

export type User = AuthUser;

export interface UserProfile {
  id: string;
  email?: string;
  is_premium: boolean;
}

export const GOOGLE_CLIENT_ID =
  process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID ||
  '519310318914-pushb78f44ug1a28m2oggunpck5annl8.apps.googleusercontent.com';

const STORAGE_KEY = 'fc-auth-user';

/**
 * Safely decodes a Google JWT ID Token without external dependencies
 */
export function decodeGoogleJwt(token: string): {
  sub: string;
  email: string;
  name?: string;
  picture?: string;
} | null {
  try {
    const base64Url = token.split('.')[1];
    if (!base64Url) return null;
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (err) {
    console.error('Failed to decode Google ID Token:', err);
    return null;
  }
}

/**
 * Signs in with Google ID Token (Google Identity Services) directly to self-hosted PostgreSQL
 */
export async function signInWithGoogleIdToken(idToken: string): Promise<AuthUser | null> {
  if (typeof window === 'undefined') return null;

  const payload = decodeGoogleJwt(idToken);
  if (!payload || !payload.sub) {
    throw new Error('Invalid Google credential token received.');
  }

  const user: AuthUser = {
    id: payload.sub,
    email: payload.email || '',
    display_name: payload.name || '',
    avatar_url: payload.picture || '',
  };

  // Persist session locally
  localStorage.setItem(STORAGE_KEY, JSON.stringify(user));

  // Upsert profile in self-hosted PostgreSQL 16 via PostgREST
  try {
    await supabase.from('profiles').upsert(
      {
        id: user.id,
        email: user.email,
        display_name: user.display_name,
        avatar_url: user.avatar_url,
      },
      { onConflict: 'id' }
    );
  } catch (err) {
    console.warn('Failed to upsert profile in PostgreSQL 16:', err);
  }

  // Sync RevenueCat subscription
  await syncUserPremiumStatus(user);

  window.dispatchEvent(new Event('fc-auth-state-changed'));
  return user;
}

/**
 * Initiates Google OAuth login via Google Identity Services One Tap prompt
 */
export async function signInWithGoogle() {
  if (typeof window === 'undefined') return;
  // @ts-expect-error Google Identity Services global
  if (window.google?.accounts?.id?.prompt) {
    // @ts-expect-error Google Identity Services global
    window.google.accounts.id.prompt();
  }
}

/**
 * Signs out current user and resets local state
 */
export async function signOutUser() {
  if (typeof window === 'undefined') return;
  localStorage.removeItem(STORAGE_KEY);
  setProUser(false);
  window.dispatchEvent(new Event('fc-auth-state-changed'));
}

/**
 * Gets current authenticated user from local storage
 */
export async function getCurrentUser(): Promise<AuthUser | null> {
  if (typeof window === 'undefined') return null;
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    return JSON.parse(raw) as AuthUser;
  } catch {
    return null;
  }
}

/**
 * Syncs premium status across self-hosted PostgreSQL profile and RevenueCat
 * 1. Checks PostgreSQL `profiles` table (e.g., if bought via Google Play in Android app)
 * 2. Checks RevenueCat Web SDK (if bought via Paddle Web Checkout)
 * 3. Bidirectionally syncs entitlement between both sources and updates app state
 */
export async function syncUserPremiumStatus(customUser?: AuthUser | null): Promise<boolean> {
  if (typeof window === 'undefined') return false;

  const user = customUser !== undefined ? customUser : await getCurrentUser();

  if (!user) {
    return isProUser();
  }

  // 1. Query PostgreSQL profiles table on Oracle VM
  let cachedPremium = false;
  try {
    const { data: dbProfile, error } = await supabase
      .from('profiles')
      .select('id, is_premium, email')
      .eq('id', user.id)
      .maybeSingle();

    if (!error && dbProfile?.is_premium) {
      cachedPremium = true;
    }
  } catch (err) {
    console.warn('Error reading PostgreSQL profile:', err);
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
  const finalPremium = rcVerified !== null ? rcVerified : cachedPremium;

  // 4. Bidirectional Sync: update PostgreSQL cache if mismatch detected (grant or revoke)
  if (rcVerified !== null && rcVerified !== cachedPremium) {
    try {
      await supabase.from('profiles').upsert(
        {
          id: user.id,
          email: user.email,
          is_premium: rcVerified,
        },
        { onConflict: 'id' }
      );
    } catch (e) {
      console.warn('Error syncing PostgreSQL profiles cache:', e);
    }
  }

  // 5. Update Tier 3 application PRO state (disables AdSense ads globally)
  setProUser(finalPremium);
  return finalPremium;
}
