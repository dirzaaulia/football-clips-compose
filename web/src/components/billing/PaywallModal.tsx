'use client';

import React, { useState, useEffect, useRef } from 'react';
import { Check, ShieldCheck, Zap, RefreshCw, CheckCircle2, Gem, LogIn } from 'lucide-react';
import { syncUserPremiumStatus, signInWithGoogle, signInWithGoogleIdToken, GOOGLE_CLIENT_ID } from '@/lib/auth';
import { useProStatus } from '@/hooks/useProStatus';
import { useAuthUser } from '@/hooks/useAuthUser';
import PaywallAccountCard from './PaywallAccountCard';
import PaywallInlineCheckout from './PaywallInlineCheckout';
import ModalSideSheet from '@/components/common/ModalSideSheet';

interface PaywallModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function PaywallModal({ isOpen, onClose }: PaywallModalProps) {
  const hasPro = useProStatus();
  const { user, isLoading: isAuthLoading } = useAuthUser();
  const [isRestoring, setIsRestoring] = useState(false);
  const [restoreMessage, setRestoreMessage] = useState<string | null>(null);
  const [hasGsiRendered, setHasGsiRendered] = useState(false);
  const [isCheckoutActive, setIsCheckoutActive] = useState(false);
  const googleBtnRef = useRef<HTMLDivElement | null>(null);

  const handleClose = () => {
    setIsCheckoutActive(false);
    onClose();
  };

  // Initialize and mount Google Identity Services in-app button
  useEffect(() => {
    if (!isOpen || user || typeof window === 'undefined') return;

    function renderGsi() {
      // @ts-expect-error Google Identity Services global
      if (window.google?.accounts?.id && googleBtnRef.current) {
        // @ts-expect-error Google Identity Services global
        window.google.accounts.id.initialize({
          client_id: GOOGLE_CLIENT_ID,
          callback: async (res: { credential?: string }) => {
            if (res.credential) {
              await signInWithGoogleIdToken(res.credential);
            }
          },
        });
        googleBtnRef.current.innerHTML = '';
        // @ts-expect-error Google Identity Services global
        window.google.accounts.id.renderButton(googleBtnRef.current, {
          theme: 'outline',
          size: 'large',
          text: 'signin_with',
          shape: 'pill',
          width: 320,
        });
        setHasGsiRendered(true);
      }
    }

    // @ts-expect-error Google Identity Services global
    if (window.google?.accounts?.id) {
      renderGsi();
    } else {
      const timer = setInterval(() => {
        // @ts-expect-error Google Identity Services global
        if (window.google?.accounts?.id) {
          clearInterval(timer);
          renderGsi();
        }
      }, 250);
      return () => clearInterval(timer);
    }
  }, [isOpen, user]);

  const handleSubscribe = async () => {
    if (!user) {
      await signInWithGoogle();
      return;
    }
    setIsCheckoutActive(true);
  };

  const handleRestore = async () => {
    setIsRestoring(true);
    setRestoreMessage(null);
    const isNowPro = await syncUserPremiumStatus(user);
    setIsRestoring(false);
    if (isNowPro) {
      setRestoreMessage('Your PRO subscription is verified! Ads are now disabled across the app.');
    } else {
      setRestoreMessage('No active subscription found. Subscribe below or ensure you are signed in with your purchase account.');
    }
  };

  const benefits = [
    '100% Ad-Free across all highlights, scores, and fixtures',
    'Instant full AI tactical recap debriefs & match reports',
    'High-definition uninterrupted video playback',
    'Cross-platform sync with Football Highlights & Clips Android app',
  ];

  const titleHeader = (
    <div className="flex items-center gap-2">
      <div className="w-6 h-6 rounded-lg bg-[#DFFF00]/15 border border-[#DFFF00]/40 flex items-center justify-center">
        <Gem className="w-3.5 h-3.5 text-[#DFFF00]" />
      </div>
      <span className="text-xs font-black uppercase tracking-wider text-[#DFFF00]">
        {hasPro ? 'Premium Active' : 'Go Premium • Remove Ads'}
      </span>
    </div>
  );

  return (
    <ModalSideSheet
      isOpen={isOpen}
      onClose={handleClose}
      title={titleHeader}
      widthClass={isCheckoutActive ? 'sm:max-w-[640px]' : 'sm:max-w-[500px]'}
    >
      <div className={`p-4 sm:p-6 ${isCheckoutActive ? 'flex-1 flex flex-col h-full' : 'space-y-5'}`}>
        {hasPro ? (
          <div className="text-center py-4 space-y-4">
            <div className="w-16 h-16 mx-auto rounded-full bg-emerald-500/20 border border-emerald-500/50 flex items-center justify-center">
              <CheckCircle2 className="w-9 h-9 text-emerald-400" />
            </div>
            <h2 className="font-scoreboard text-3xl font-black text-emerald-400 tracking-wide uppercase">
              Premium Active
            </h2>
            <p className="text-xs sm:text-sm text-[#A1A1AA] max-w-sm mx-auto">
              Thank you for supporting Football Highlights & Clips! All advertisements have been removed.
            </p>
            <PaywallAccountCard user={user} isLoading={isAuthLoading} onAuthChanged={handleRestore} />
          </div>
        ) : isCheckoutActive && user ? (
          <PaywallInlineCheckout
            userId={user.id}
            onBack={() => setIsCheckoutActive(false)}
            onSuccess={async () => {
              await handleRestore();
              setIsCheckoutActive(false);
            }}
          />
        ) : (
          <div className="space-y-5">
            <div>
              <h2 className="font-scoreboard text-3xl sm:text-4xl text-white tracking-wide uppercase leading-none">
                Pure Football, Zero Interruptions
              </h2>
              <p className="text-xs sm:text-sm text-[#A1A1AA] mt-1.5">
                Support development and unlock an uninterrupted ad-free experience across web & mobile.
              </p>
            </div>

            {/* Account Card (Sign in with Google to sync) */}
            <PaywallAccountCard user={user} isLoading={isAuthLoading} onAuthChanged={handleRestore} />

            {/* Feature List */}
            <div className="space-y-2.5 bg-[#0A0A0C] p-4 rounded-xl border border-white/10">
              <span className="text-[10px] font-tech font-bold uppercase tracking-wider text-[#DFFF00]">
                EXCLUSIVE PRO BENEFITS
              </span>
              {benefits.map((feat, i) => (
                <div key={i} className="flex items-start gap-2.5 text-xs text-[#E3E2E6]">
                  <div className="w-4 h-4 rounded bg-[#DFFF00]/15 text-[#DFFF00] flex items-center justify-center shrink-0 mt-0.5">
                    <Check className="w-2.5 h-2.5 stroke-[3]" />
                  </div>
                  <span className="leading-tight">{feat}</span>
                </div>
              ))}
            </div>

            {/* Restore Status Message */}
            {restoreMessage && (
              <div className="p-3 rounded-xl bg-[#1A1C22] border border-white/10 text-xs text-[#DFFF00] text-center font-medium font-tech">
                {restoreMessage}
              </div>
            )}

            {/* CTA Buttons */}
            <div className="space-y-2.5 pt-1">
              {user ? (
                <button
                  onClick={handleSubscribe}
                  className="w-full py-3.5 px-6 rounded-xl bg-[#DFFF00] hover:bg-[#c8e600] text-[#0A0A0C] font-black text-sm shadow-[0_0_24px_rgba(223,255,0,0.35)] transition-all flex items-center justify-center gap-2 cursor-pointer font-scoreboard tracking-wider text-base"
                >
                  <Zap className="w-4 h-4 fill-current" />
                  <span>Continue to Secure Checkout</span>
                </button>
              ) : (
                <div className="flex flex-col items-center w-full">
                  <div ref={googleBtnRef} className={`w-full flex justify-center ${hasGsiRendered ? 'min-h-[44px]' : 'hidden'}`} />
                  {!hasGsiRendered && (
                    <button
                      onClick={handleSubscribe}
                      className="w-full py-3.5 px-6 rounded-xl bg-white hover:bg-zinc-100 text-black font-black text-sm shadow-[0_0_20px_rgba(255,255,255,0.2)] transition-all flex items-center justify-center gap-2 cursor-pointer font-scoreboard tracking-wider"
                    >
                      <LogIn className="w-4 h-4 text-black" />
                      <span>Sign In with Google to Upgrade</span>
                    </button>
                  )}
                </div>
              )}

              <button
                onClick={handleRestore}
                disabled={isRestoring}
                className="w-full py-2.5 px-4 rounded-xl bg-[#1A1C22] hover:bg-[#252830] text-white/70 hover:text-white font-tech text-xs border border-white/10 transition-colors cursor-pointer flex items-center justify-center gap-2"
              >
                <RefreshCw className={`w-3.5 h-3.5 ${isRestoring ? 'animate-spin' : ''}`} />
                <span>{isRestoring ? 'Verifying Active Subscriptions...' : 'Sync & Restore Previous Purchases'}</span>
              </button>
            </div>

            {/* Secure Checkout Note */}
            <div className="flex items-center justify-center gap-1.5 text-[11px] text-white/50 font-tech">
              <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
              <span>Encrypted checkout securely managed by Paddle & RevenueCat</span>
            </div>
          </div>
        )}
      </div>
    </ModalSideSheet>
  );
}
