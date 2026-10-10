'use client';

import { Purchases } from '@revenuecat/purchases-js';

// RevenueCat + Paddle Web Configuration
export const REVENUECAT_API_KEY =
  process.env.NEXT_PUBLIC_REVENUECAT_API_KEY || 'goog_gHDMIAwmoTGZQkfkTZDyjnfetoK';

// RevenueCat Paddle Checkout Sandbox / Live URL
export const PADDLE_PAYWALL_URL =
  process.env.NEXT_PUBLIC_PADDLE_PAYWALL_URL || 'https://pay.rev.cat/ppwchgbfqppzavxg';

let purchasesInitialized = false;

export async function getRevenueCatClient(userId?: string) {
  if (typeof window === 'undefined') return null;

  try {
    if (!purchasesInitialized) {
      Purchases.configure(REVENUECAT_API_KEY, userId || getOrCreateAnonymousUserId());
      purchasesInitialized = true;
    }
    return Purchases.getSharedInstance();
  } catch (error) {
    console.warn('RevenueCat SDK init warning:', error);
    return null;
  }
}

export function getOrCreateAnonymousUserId(): string {
  if (typeof window === 'undefined') return 'web-anon-user';
  let uid = localStorage.getItem('fc-revenuecat-uid');
  if (!uid) {
    uid = 'web_' + Math.random().toString(36).substring(2, 12);
    localStorage.setItem('fc-revenuecat-uid', uid);
  }
  return uid;
}

export function isProUser(): boolean {
  if (typeof window === 'undefined') return false;
  return localStorage.getItem('fc-is-pro') === 'true';
}

export function setProUser(isPro: boolean) {
  if (typeof window === 'undefined') return;
  localStorage.setItem('fc-is-pro', String(isPro));
  window.dispatchEvent(new Event('fc-pro-status-changed'));
}

/**
 * Opens the official RevenueCat Paddle Web Checkout window using authenticated account ID
 * Automatically monitors popup closure, message events, and tab focus to sync purchase
 */
export function openPaddleCheckout(userId: string, onCompleted?: () => void) {
  if (typeof window === 'undefined') return;

  if (!userId) {
    console.error('User must be signed in with their account before purchasing to link to database.');
    return;
  }

  const checkoutUrl = `${PADDLE_PAYWALL_URL}/${userId}`;

  const popup = window.open(
    checkoutUrl,
    'PaddleCheckout',
    'width=600,height=750,scrollbars=yes,status=no,toolbar=no'
  );

  let hasSynced = false;
  const triggerSync = async () => {
    if (hasSynced) return;
    hasSynced = true;
    await restorePurchases(userId);
    onCompleted?.();
  };

  // 1. Popup Watcher: Periodically check if payment window was closed
  if (popup) {
    const timer = setInterval(() => {
      try {
        if (popup.closed) {
          clearInterval(timer);
          triggerSync();
        }
      } catch {
        // Cross-origin safe
      }
    }, 1200);
  }

  // 2. Tab Focus: When user switches back to FootballClips after payment
  const handleFocus = () => {
    triggerSync();
  };
  window.addEventListener('focus', handleFocus, { once: true });

  // 3. PostMessage: Handle completion message from pay.rev.cat or paddle
  const handleMessage = (event: MessageEvent) => {
    if (
      event.origin.includes('rev.cat') ||
      event.origin.includes('paddle.com')
    ) {
      const data = event.data;
      if (
        data?.type === 'checkout-completed' ||
        data?.event === 'checkout.completed' ||
        data?.status === 'completed' ||
        data?.success
      ) {
        if (popup && !popup.closed) popup.close();
        triggerSync();
      }
    }
  };
  window.addEventListener('message', handleMessage);

  if (!popup || popup.closed || typeof popup.closed === 'undefined') {
    // eslint-disable-next-line @next/next/no-location-assign-relative-destination
    window.location.assign(checkoutUrl);
  }
}

/**
 * Restores user entitlement from RevenueCat customer info
 */
export async function restorePurchases(userId?: string): Promise<boolean> {
  if (typeof window === 'undefined') return false;

  try {
    const client = await getRevenueCatClient(userId);
    if (client) {
      const customerInfo = await client.getCustomerInfo();
      const hasActiveEntitlement =
        Object.keys(customerInfo.entitlements.active).length > 0;
      setProUser(hasActiveEntitlement);
      return hasActiveEntitlement;
    }
  } catch (e) {
    console.error('Error restoring purchases:', e);
  }

  // Fallback check
  return isProUser();
}
