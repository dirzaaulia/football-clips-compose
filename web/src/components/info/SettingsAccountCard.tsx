'use client';

import React, { useState } from 'react';
import Image from 'next/image';
import {
  User,
  LogOut,
  LogIn,
  Sparkles,
  RefreshCw,
  ShieldCheck,
} from 'lucide-react';
import { useAuthUser } from '@/hooks/useAuthUser';
import {
  signInWithGoogle,
  signOutUser,
  syncUserPremiumStatus,
  AuthUser,
} from '@/lib/auth';
import { useProStatus } from '@/hooks/useProStatus';
import {
  trackSignIn,
  trackSignOut,
  trackPaywallView,
} from '@/lib/analytics';

interface SettingsAccountCardProps {
  onOpenPaywall?: () => void;
}

interface AccountActionsProps {
  user: AuthUser | null;
  isPro: boolean;
  isSyncing: boolean;
  onRestoreSync: () => void;
  onOpenPaywall?: () => void;
}

function AccountActionButtons({
  user,
  isPro,
  isSyncing,
  onRestoreSync,
  onOpenPaywall,
}: AccountActionsProps) {
  return (
    <div className="relative z-10 flex flex-wrap items-center gap-2.5 shrink-0">
      {!isPro && onOpenPaywall && (
        <button
          type="button"
          onClick={() => {
            trackPaywallView('settings_account_card');
            onOpenPaywall();
          }}
          className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-[#DFFF00]/15 hover:bg-[#DFFF00]/25 border border-[#DFFF00]/40 text-[#DFFF00] text-xs font-tech font-bold uppercase tracking-wider transition-all cursor-pointer"
        >
          <Sparkles className="w-3.5 h-3.5" />
          <span>Unlock PRO</span>
        </button>
      )}
      {user && (
        <button
          type="button"
          onClick={onRestoreSync}
          disabled={isSyncing}
          className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-[#121316] hover:bg-white/5 border border-white/10 text-white/80 text-xs font-tech font-semibold transition-all cursor-pointer"
          title="Verify & Sync Entitlements"
        >
          <RefreshCw className={`w-3.5 h-3.5 text-[#DFFF00] ${isSyncing ? 'animate-pulse' : ''}`} />
          <span>{isSyncing ? 'Syncing...' : 'Sync Status'}</span>
        </button>
      )}
      {user ? (
        <button
          type="button"
          onClick={() => {
            trackSignOut(user.id);
            signOutUser();
          }}
          className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl border border-rose-500/30 hover:border-rose-500/60 bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 text-xs font-tech font-bold transition-all cursor-pointer"
        >
          <LogOut className="w-3.5 h-3.5" />
          <span>Sign Out</span>
        </button>
      ) : (
        <button
          type="button"
          onClick={() => {
            trackSignIn();
            signInWithGoogle();
          }}
          className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-[#DFFF00] hover:bg-[#c8e600] text-[#0A0A0C] text-xs font-tech font-bold uppercase tracking-wider shadow-[0_0_16px_rgba(223,255,0,0.25)] transition-all cursor-pointer"
        >
          <LogIn className="w-3.5 h-3.5" />
          <span>Sign In</span>
        </button>
      )}
    </div>
  );
}

export default function SettingsAccountCard({
  onOpenPaywall,
}: SettingsAccountCardProps) {
  const { user, isLoading: isAuthLoading } = useAuthUser();
  const isPro = useProStatus();
  const [isSyncing, setIsSyncing] = useState(false);

  const handleRestoreSync = async () => {
    setIsSyncing(true);
    try {
      await syncUserPremiumStatus();
    } finally {
      setTimeout(() => setIsSyncing(false), 600);
    }
  };

  return (
    <div className="p-5 sm:p-6 rounded-2xl bg-[#1A1C22] border border-white/10 shadow-xl flex flex-col sm:flex-row sm:items-center justify-between gap-4 relative overflow-hidden">
      <div className="absolute -right-16 -top-16 w-56 h-56 rounded-full bg-[#DFFF00]/5 blur-3xl pointer-events-none" />
      <div className="relative z-10 flex items-center gap-4 min-w-0">
        <div className="relative shrink-0">
          <div className="w-14 h-14 rounded-2xl bg-[#121316] border border-[#DFFF00]/30 flex items-center justify-center overflow-hidden shadow-md">
            {user?.avatar_url ? (
              <Image
                src={user.avatar_url}
                alt={user.display_name || 'Account avatar'}
                width={56}
                height={56}
                className="w-full h-full object-cover"
              />
            ) : (
              <User className="w-6 h-6 text-[#DFFF00]" />
            )}
          </div>
          {isPro && (
            <div className="absolute -bottom-1 -right-1 w-5 h-5 rounded-full bg-[#DFFF00] flex items-center justify-center shadow">
              <ShieldCheck className="w-3.5 h-3.5 text-[#0A0A0C]" />
            </div>
          )}
        </div>
        <div className="min-w-0 space-y-1">
          <div className="flex flex-wrap items-center gap-2">
            <h2 className="font-scoreboard text-xl sm:text-2xl text-white tracking-wider uppercase leading-none truncate">
              {user?.display_name ||
                user?.email ||
                (isAuthLoading ? 'Checking Session...' : 'Account')}
            </h2>
            <span
              className={`text-[10px] font-tech font-bold px-2.5 py-0.5 rounded-full uppercase tracking-wider ${
                isPro
                  ? 'bg-[#DFFF00] text-[#0A0A0C]'
                  : 'bg-white/5 text-white/70 border border-white/10'
              }`}
            >
              {isPro ? 'PRO Subscriber' : user ? 'Free Account' : 'Guest Session'}
            </span>
          </div>
          <p className="text-xs text-white/60 truncate font-tech">
            {user
              ? isPro
                ? `${user.email || 'Linked account'} • Ad-free OTT & Merchandise Unlocked`
                : `${user.email || 'Signed in'} • Favorite clubs & entitlements synced`
              : 'Sign in to sync your favorite clubs and restore PRO access across devices'}
          </p>
        </div>
      </div>
      <AccountActionButtons
        user={user}
        isPro={isPro}
        isSyncing={isSyncing}
        onRestoreSync={handleRestoreSync}
        onOpenPaywall={onOpenPaywall}
      />
    </div>
  );
}
