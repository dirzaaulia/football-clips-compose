'use client';

import React from 'react';
import { LogOut, Info, User as UserIcon } from 'lucide-react';
import { signOutUser } from '@/lib/auth';
import type { User } from '@/lib/auth';

interface PaywallAccountCardProps {
  user: User | null;
  isLoading: boolean;
  onAuthChanged: () => void;
}

export default function PaywallAccountCard({
  user,
  isLoading,
  onAuthChanged,
}: PaywallAccountCardProps) {
  const handleSignOut = async () => {
    await signOutUser();
    onAuthChanged();
  };

  if (isLoading) {
    return (
      <div className="p-3.5 rounded-2xl bg-[#101418] border border-[#334155]/60 text-xs text-[#94A3B8] text-center animate-pulse">
        Checking account status...
      </div>
    );
  }

  return (
    <div className="p-4 rounded-2xl bg-[#101418]/80 border border-[#334155]/60">
      {user ? (
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2.5 min-w-0">
            <div className="w-7 h-7 rounded-full bg-[#1E3A8A] text-[#7098F8] flex items-center justify-center shrink-0">
              <UserIcon className="w-3.5 h-3.5" />
            </div>
            <div className="min-w-0">
              <div className="text-[10px] text-[#94A3B8] font-bold uppercase tracking-wider">
                Connected Account • Database Linked
              </div>
              <div className="text-xs font-semibold text-white truncate max-w-[200px] sm:max-w-[260px]">
                {user.email}
              </div>
            </div>
          </div>
          <button
            onClick={handleSignOut}
            className="flex items-center gap-1 text-[11px] font-semibold text-rose-400 hover:text-rose-300 transition-colors cursor-pointer px-2 py-1 rounded-lg hover:bg-rose-500/10"
          >
            <LogOut className="w-3 h-3" />
            <span>Sign Out</span>
          </button>
        </div>
      ) : (
        <div className="space-y-1.5">
          <div className="flex items-center gap-2">
            <Info className="w-3.5 h-3.5 text-[#D4AF37] shrink-0" />
            <span className="text-xs font-bold text-[#D4AF37]">Why Sign In?</span>
          </div>
          <p className="text-[11px] text-[#94A3B8] leading-relaxed">
            Sign-in is required before purchase to link your entitlement to your database profile and sync access across Android &amp; Web.
          </p>
        </div>
      )}
    </div>
  );
}
