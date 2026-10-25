'use client';

import React, { useState, useEffect } from 'react';
import Image from 'next/image';
import {
  User,
  LogOut,
  LogIn,
  Moon,
  Sun,
  Shield,
  ChevronRight,
  ShoppingBag,
  Sparkles,
} from 'lucide-react';
import { useAuthUser } from '@/hooks/useAuthUser';
import { signInWithGoogle, signOutUser } from '@/lib/auth';
import { useTheme } from '@/hooks/useTheme';
import { useMyClubs } from '@/hooks/useMyClubs';
import { useProStatus } from '@/hooks/useProStatus';

interface SettingsHubCardsProps {
  onOpenMyClubs: () => void;
  onOpenPaywall?: () => void;
}

export default function SettingsHubCards({
  onOpenMyClubs,
  onOpenPaywall,
}: SettingsHubCardsProps) {
  const { user, isLoading: isAuthLoading } = useAuthUser();
  const isPro = useProStatus();
  const { isDark, toggleTheme } = useTheme();
  const { favoriteCount } = useMyClubs();

  const [merchandiseEnabled, setMerchandiseEnabled] = useState(true);

  useEffect(() => {
    if (typeof window !== 'undefined') {
      const stored = localStorage.getItem('fc-merchandise-enabled');
      if (stored !== null) {
        setMerchandiseEnabled(stored === 'true');
      }
    }
  }, []);

  const handleToggleMerchandise = (enabled: boolean) => {
    if (!isPro && onOpenPaywall) {
      onOpenPaywall();
      return;
    }
    setMerchandiseEnabled(enabled);
    localStorage.setItem('fc-merchandise-enabled', String(enabled));
    window.dispatchEvent(new Event('fc-merchandise-toggle'));
  };

  return (
    <div className="space-y-4">
      {/* 1. Account & Identity Card */}
      <div className="p-4 sm:p-5 rounded-2xl bg-[#181C20] border border-[#334155]/80 shadow-md flex items-center justify-between gap-4">
        <div className="flex items-center gap-3.5 min-w-0">
          <div className="w-11 h-11 rounded-xl bg-[#7098F8]/15 border border-[#7098F8]/30 flex items-center justify-center shrink-0 overflow-hidden">
            {user?.avatar_url ? (
              <Image
                src={user.avatar_url}
                alt={user.display_name || 'User avatar'}
                width={44}
                height={44}
                className="w-full h-full object-cover"
              />
            ) : (
              <User className="w-5 h-5 text-[#7098F8]" />
            )}
          </div>
          <div className="min-w-0">
            <div className="flex items-center gap-2">
              <h2 className="text-sm font-bold text-white truncate">
                {user?.display_name || user?.email || (isAuthLoading ? 'Loading profile...' : 'Guest Account')}
              </h2>
              {isPro && (
                <span className="text-[10px] font-black px-2 py-0.5 rounded-full bg-gradient-to-r from-amber-500/20 to-yellow-500/20 text-amber-300 border border-amber-500/40 uppercase tracking-wider">
                  PRO
                </span>
              )}
            </div>
            <p className="text-xs text-[#94A3B8] truncate">
              {user ? (isPro ? 'PRO Subscriber • Ad-free' : user.email || 'Free tier account') : 'Sign in to sync your favorite clubs across devices'}
            </p>
          </div>
        </div>

        <div className="shrink-0">
          {user ? (
            <button
              onClick={() => signOutUser()}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl border border-red-500/30 hover:border-red-500/60 bg-red-500/10 hover:bg-red-500/20 text-red-400 text-xs font-semibold transition-all cursor-pointer"
              title="Sign Out"
            >
              <LogOut className="w-3.5 h-3.5" />
              <span>Sign Out</span>
            </button>
          ) : (
            <button
              onClick={() => signInWithGoogle()}
              className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-xl bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] hover:from-[#2563EB] hover:to-[#60A5FA] text-white text-xs font-bold shadow-sm transition-all cursor-pointer"
            >
              <LogIn className="w-3.5 h-3.5" />
              <span>Sign In</span>
            </button>
          )}
        </div>
      </div>

      {/* 2. Primary Settings Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
        {/* Theme Switcher Card */}
        <div className="p-4 rounded-2xl bg-[#181C20] border border-[#334155]/80 shadow-md flex items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-[#7098F8]/15 border border-[#7098F8]/30 flex items-center justify-center shrink-0">
              {isDark ? (
                <Moon className="w-5 h-5 text-[#7098F8]" />
              ) : (
                <Sun className="w-5 h-5 text-amber-400" />
              )}
            </div>
            <div>
              <div className="text-sm font-bold text-white">Appearance Theme</div>
              <div className="text-xs text-[#94A3B8]">
                {isDark ? 'Dark Mode active' : 'Light Mode active'}
              </div>
            </div>
          </div>
          <button
            onClick={toggleTheme}
            className="px-3 py-1.5 rounded-xl border border-[#334155] hover:border-[#7098F8]/50 bg-[#101418] hover:bg-[#1E2227] text-xs font-semibold text-white transition-all cursor-pointer"
          >
            {isDark ? 'Switch to Light' : 'Switch to Dark'}
          </button>
        </div>

        {/* My Clubs Settings Card */}
        <div
          onClick={onOpenMyClubs}
          className="p-4 rounded-2xl bg-[#181C20] border border-[#334155]/80 hover:border-[#7098F8]/40 shadow-md flex items-center justify-between gap-3 cursor-pointer group transition-all"
        >
          <div className="flex items-center gap-3 min-w-0">
            <div className="w-10 h-10 rounded-xl bg-[#7098F8]/15 border border-[#7098F8]/30 flex items-center justify-center shrink-0 group-hover:scale-105 transition-transform">
              <Shield className="w-5 h-5 text-[#7098F8]" />
            </div>
            <div className="min-w-0">
              <div className="text-sm font-bold text-white">My Clubs</div>
              <div className="text-xs text-[#94A3B8] truncate">
                {favoriteCount > 0
                  ? `${favoriteCount} favorite club${favoriteCount > 1 ? 's' : ''} selected`
                  : 'Personalize tailored highlights'}
              </div>
            </div>
          </div>
          <div className="flex items-center gap-1 text-xs text-[#7098F8] font-semibold shrink-0 group-hover:translate-x-0.5 transition-transform">
            <span>Manage</span>
            <ChevronRight className="w-4 h-4" />
          </div>
        </div>
      </div>

      {/* 3. Merchandise Recommendations Card */}
      <div className="p-4 sm:p-5 rounded-2xl bg-[#181C20] border border-[#334155]/80 shadow-md flex items-center justify-between gap-4">
        <div className="flex items-center gap-3.5 min-w-0">
          <div className="w-10 h-10 rounded-xl bg-amber-500/15 border border-amber-500/30 flex items-center justify-center shrink-0">
            <ShoppingBag className="w-5 h-5 text-amber-400" />
          </div>
          <div className="min-w-0">
            <div className="text-sm font-bold text-white">Merchandise Recommendations</div>
            <p className="text-xs text-[#94A3B8] truncate">
              Show authentic licensed gear and Amazon club store links
            </p>
          </div>
        </div>

        <div className="shrink-0">
          {isPro ? (
            <button
              onClick={() => handleToggleMerchandise(!merchandiseEnabled)}
              className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors cursor-pointer ${
                merchandiseEnabled ? 'bg-[#7098F8]' : 'bg-[#334155]'
              }`}
            >
              <span
                className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                  merchandiseEnabled ? 'translate-x-6' : 'translate-x-1'
                }`}
              />
            </button>
          ) : (
            <button
              onClick={() => onOpenPaywall && onOpenPaywall()}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-amber-500/15 border border-amber-500/30 hover:bg-amber-500/25 text-amber-400 text-xs font-bold transition-all cursor-pointer"
            >
              <Sparkles className="w-3.5 h-3.5" />
              <span>Unlock</span>
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
