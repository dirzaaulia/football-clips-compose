'use client';

import React from 'react';
import {
  Eye,
  EyeOff,
  Gem,
  CheckCircle2,
  Smartphone,
} from 'lucide-react';

interface SidebarBottomControlsProps {
  isCollapsed?: boolean;
  isSpoilerFree: boolean;
  hasPro: boolean;
  onToggleSpoilerFree: () => void;
  onOpenPaywall: () => void;
}

export default function SidebarBottomControls({
  isCollapsed = false,
  isSpoilerFree,
  hasPro,
  onToggleSpoilerFree,
  onOpenPaywall,
}: SidebarBottomControlsProps) {
  return (
    <div className={`space-y-1.5 pt-2.5 border-t border-[#334155]/60 w-full ${isCollapsed ? 'flex flex-col items-center' : ''}`}>
      {/* Minimalist Spoiler-Free Toggle */}
      <button
        onClick={onToggleSpoilerFree}
        title={isSpoilerFree ? 'Spoiler-Free: ON' : 'Spoiler-Free: OFF'}
        className={`flex items-center transition-all border cursor-pointer ${
          isCollapsed
            ? 'w-10 h-10 rounded-xl justify-center mx-auto'
            : 'w-full justify-between px-3 py-1.5 rounded-xl text-xs font-bold'
        } ${
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
            className={`text-[9px] font-black px-1.5 py-0.2 rounded shrink-0 ${
              isSpoilerFree ? 'bg-emerald-500/20 text-emerald-400' : 'bg-zinc-800 text-zinc-400'
            }`}
          >
            {isSpoilerFree ? 'ON' : 'OFF'}
          </span>
        )}
      </button>

      {/* RevenueCat & Paddle PRO Button */}
      {!hasPro ? (
        <button
          onClick={onOpenPaywall}
          title="GO PREMIUM"
          className={`rounded-xl bg-[#D4AF37] hover:bg-[#C29B27] text-black font-black text-xs shadow-[0_0_12px_rgba(212,175,55,0.3)] transition-all flex items-center justify-center gap-1.5 cursor-pointer ${
            isCollapsed ? 'w-10 h-10 mx-auto' : 'w-full py-2 px-3'
          }`}
        >
          <Gem className="w-4 h-4 fill-current shrink-0 text-black" />
          {!isCollapsed && <span className="truncate tracking-wide text-xs">GO PREMIUM</span>}
        </button>
      ) : (
        <button
          onClick={onOpenPaywall}
          title="PREMIUM Active"
          className={`rounded-xl bg-[#4CAF50] hover:bg-[#43A047] text-white font-black text-xs shadow-[0_0_12px_rgba(76,175,80,0.3)] transition-all flex items-center justify-center gap-1.5 cursor-pointer ${
            isCollapsed ? 'w-10 h-10 mx-auto' : 'w-full py-2 px-3'
          }`}
        >
          <CheckCircle2 className="w-4 h-4 shrink-0 text-white" />
          {!isCollapsed && <span className="tracking-wide text-xs">PREMIUM</span>}
        </button>
      )}

      {/* Collapsed vs Expanded Utility Row */}
      {isCollapsed ? (
        <div className="pt-1 border-t border-[#334155]/40 w-full flex flex-col items-center">
          <a
            href="https://play.google.com/store/apps/details?id=com.dirzaaulia.footballclips"
            target="_blank"
            rel="noopener noreferrer"
            title="Get Android App on Google Play"
            className="w-10 h-10 rounded-xl bg-[#181C20] hover:bg-[#1E2227] text-[#94A3B8] hover:text-[#7098F8] border border-[#334155] transition-all flex items-center justify-center cursor-pointer"
          >
            <Smartphone className="w-4 h-4 text-[#7098F8]" />
          </a>
        </div>
      ) : (
        <div className="pt-1.5 border-t border-[#334155]/40 flex items-center justify-between text-xs text-[#94A3B8]">
          <a
            href="https://play.google.com/store/apps/details?id=com.dirzaaulia.footballclips"
            target="_blank"
            rel="noopener noreferrer"
            title="Get Android App on Google Play"
            className="inline-flex items-center gap-1.5 hover:text-[#7098F8] transition-colors py-0.5"
          >
            <Smartphone className="w-3.5 h-3.5 text-[#7098F8]" />
            <span className="text-[11px] font-bold">Android App</span>
          </a>
        </div>
      )}
    </div>
  );
}
