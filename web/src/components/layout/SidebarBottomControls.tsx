'use client';

import React from 'react';
import {
  Eye,
  EyeOff,
  Gem,
  CheckCircle2,
  Smartphone,
} from 'lucide-react';
import GooglePlayBadge from '@/components/common/GooglePlayBadge';

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
    <div className={`space-y-1.5 pt-2.5 border-t border-white/10 w-full ${isCollapsed ? 'flex flex-col items-center' : ''}`}>
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
            ? 'bg-[#DFFF00]/15 text-[#DFFF00] border-[#DFFF00]/40 shadow-sm'
            : 'bg-[#121316] text-[#A1A1AA] hover:text-white hover:bg-[#1A1C22] border border-white/10'
        }`}
      >
        <div className="flex items-center gap-2 min-w-0">
          {isSpoilerFree ? (
            <EyeOff className="w-4 h-4 text-[#DFFF00] shrink-0" />
          ) : (
            <Eye className="w-4 h-4 text-[#A1A1AA] shrink-0" />
          )}
          {!isCollapsed && <span className="truncate">Spoiler-Free</span>}
        </div>
        {!isCollapsed && (
          <span
            className={`text-[9px] font-tech font-black px-1.5 py-0.2 rounded shrink-0 ${
              isSpoilerFree ? 'bg-[#DFFF00]/25 text-[#DFFF00]' : 'bg-zinc-800 text-zinc-400'
            }`}
          >
            {isSpoilerFree ? 'ON' : 'OFF'}
          </span>
        )}
      </button>

      {/* Nike Pro Style High-Energy Volt PRO Button */}
      {!hasPro ? (
        <button
          onClick={onOpenPaywall}
          title="GO PREMIUM"
          className={`rounded-xl bg-[#DFFF00] hover:bg-[#c8e600] text-[#0A0A0C] font-black text-xs shadow-[0_0_16px_rgba(223,255,0,0.3)] transition-all flex items-center justify-center gap-1.5 cursor-pointer ${
            isCollapsed ? 'w-10 h-10 mx-auto' : 'w-full py-2 px-3'
          }`}
        >
          <Gem className="w-4 h-4 fill-current shrink-0 text-[#0A0A0C]" />
          {!isCollapsed && <span className="truncate font-scoreboard text-sm tracking-wide">GO PREMIUM</span>}
        </button>
      ) : (
        <button
          onClick={onOpenPaywall}
          title="PREMIUM Active"
          className={`rounded-xl bg-emerald-400 hover:bg-emerald-500 text-[#0A0A0C] font-black text-xs shadow-[0_0_12px_rgba(52,211,153,0.3)] transition-all flex items-center justify-center gap-1.5 cursor-pointer ${
            isCollapsed ? 'w-10 h-10 mx-auto' : 'w-full py-2 px-3'
          }`}
        >
          <CheckCircle2 className="w-4 h-4 shrink-0 text-[#0A0A0C]" />
          {!isCollapsed && <span className="font-scoreboard text-sm tracking-wide">PREMIUM ACTIVE</span>}
        </button>
      )}

      {/* Collapsed vs Expanded Utility Row */}
      {isCollapsed ? (
        <div className="pt-1.5 border-t border-white/10 w-full flex flex-col items-center">
          <a
            href="https://play.google.com/store/apps/details?id=com.dirzaaulia.footballclips"
            target="_blank"
            rel="noopener noreferrer"
            title="Get Android App on Google Play"
            className="w-10 h-10 rounded-xl bg-[#121316] hover:bg-[#1A1C22] text-[#A1A1AA] hover:text-[#DFFF00] border border-white/10 transition-all flex items-center justify-center cursor-pointer"
          >
            <Smartphone className="w-4 h-4 text-[#DFFF00]" />
          </a>
        </div>
      ) : (
        <div className="pt-2 border-t border-white/10 flex items-center justify-center">
          <GooglePlayBadge height={32} />
        </div>
      )}
    </div>
  );
}
