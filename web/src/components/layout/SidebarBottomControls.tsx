'use client';

import React from 'react';
import { Eye, EyeOff, Gem, CheckCircle2, Smartphone } from 'lucide-react';

interface SidebarBottomControlsProps {
  isCollapsed: boolean;
  isSpoilerFree: boolean;
  hasPro: boolean;
  onToggleSpoilerFree: () => void;
  onOpenPaywall: () => void;
}

export default function SidebarBottomControls({
  isCollapsed,
  isSpoilerFree,
  hasPro,
  onToggleSpoilerFree,
  onOpenPaywall,
}: SidebarBottomControlsProps) {
  return (
    <div className="space-y-2 pt-3 border-t border-[#334155]/60 w-full">
      {/* Minimalist Spoiler-Free Toggle (Single Row, saves space) */}
      <button
        onClick={onToggleSpoilerFree}
        title={isSpoilerFree ? 'Spoiler-Free Active (Scores hidden)' : 'Spoiler-Free Inactive (Scores visible)'}
        className={`w-full flex items-center ${
          isCollapsed ? 'justify-center p-2.5' : 'justify-between px-3 py-2'
        } rounded-2xl text-xs font-bold transition-all border cursor-pointer ${
          isSpoilerFree
            ? 'bg-[#1E3A8A]/40 text-[#4FC3F7] border-[#7098F8]/40 shadow-sm'
            : 'bg-[#181C20] text-[#94A3B8] hover:text-white hover:bg-[#1E2227] border border-[#334155]'
        }`}
      >
        <div className="flex items-center gap-2 min-w-0">
          {isSpoilerFree ? (
            <EyeOff className="w-4 h-4 text-[#4FC3F7] shrink-0" />
          ) : (
            <Eye className="w-4 h-4 text-[#94A3B8] shrink-0" />
          )}
          {!isCollapsed && <span className="truncate">Spoiler-Free</span>}
        </div>
        {!isCollapsed && (
          <span
            className={`text-[9px] font-black px-1.5 py-0.5 rounded shrink-0 ${
              isSpoilerFree ? 'bg-emerald-500/20 text-emerald-400' : 'bg-zinc-800 text-zinc-400'
            }`}
          >
            {isSpoilerFree ? 'ON' : 'OFF'}
          </span>
        )}
      </button>

      {/* RevenueCat & Paddle PRO Button (Android Parity: Gold #D4AF37 with Gem Icon vs Emerald #4CAF50) */}
      {!hasPro ? (
        <button
          onClick={onOpenPaywall}
          title={isCollapsed ? 'GO PREMIUM (Paddle)' : undefined}
          className={`w-full py-2.5 rounded-2xl bg-[#D4AF37] hover:bg-[#C29B27] text-black font-black text-xs shadow-[0_0_16px_rgba(212,175,55,0.35)] transition-all flex items-center justify-center gap-1.5 cursor-pointer ${
            isCollapsed ? 'px-0' : 'px-3'
          }`}
        >
          <Gem className="w-4 h-4 fill-current shrink-0 text-black" />
          {!isCollapsed && <span className="truncate tracking-wide">GO PREMIUM</span>}
        </button>
      ) : (
        <button
          onClick={onOpenPaywall}
          title={isCollapsed ? 'PREMIUM Active' : undefined}
          className={`w-full py-2.5 rounded-2xl bg-[#4CAF50] hover:bg-[#43A047] text-white font-black text-xs shadow-[0_0_16px_rgba(76,175,80,0.35)] transition-all flex items-center justify-center gap-1.5 cursor-pointer ${
            isCollapsed ? 'px-0' : 'px-3'
          }`}
        >
          <CheckCircle2 className="w-4 h-4 shrink-0 text-white" />
          {!isCollapsed && <span className="tracking-wide">PREMIUM</span>}
        </button>
      )}

      {/* Android Native App on Google Play */}
      <a
        href="https://play.google.com/store/apps/details?id=com.dirzaaulia.footballclips"
        target="_blank"
        rel="noopener noreferrer"
        title={isCollapsed ? 'Get Android App on Google Play' : undefined}
        className={`w-full py-2 rounded-2xl bg-[#181C20] hover:bg-[#1E2227] text-[#94A3B8] hover:text-[#7098F8] font-bold text-xs border border-[#334155]/80 hover:border-[#7098F8]/50 transition-all flex items-center justify-center gap-1.5 cursor-pointer ${
          isCollapsed ? 'px-0' : 'px-3'
        }`}
      >
        <Smartphone className="w-4 h-4 text-[#7098F8] shrink-0" />
        {!isCollapsed && <span className="truncate">Google Play App</span>}
      </a>
    </div>
  );
}
