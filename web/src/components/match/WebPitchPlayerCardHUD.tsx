'use client';

import React from 'react';
import Image from 'next/image';
import { X } from 'lucide-react';
import { FormationNode } from './WebPitchFormationHelper';

interface WebPitchPlayerCardHUDProps {
  node: FormationNode;
  onClose: () => void;
  teamColor: string;
}

export default function WebPitchPlayerCardHUD({
  node,
  onClose,
  teamColor,
}: WebPitchPlayerCardHUDProps) {
  const p = node.player;
  const stats = p.stats || {};
  const isGK = p.position === 'GK' || p.position === 'G';

  return (
    <div className="absolute bottom-3 left-3 z-30 max-w-xs w-[calc(100%-1.5rem)] bg-[#0B0D11]/95 border border-white/15 backdrop-blur-xl rounded-2xl p-3 shadow-2xl space-y-2 select-none animate-in fade-in slide-in-from-bottom-2 duration-200">
      <div className="flex items-start justify-between gap-2">
        <div className="flex items-center gap-2.5 min-w-0">
          {p.jerseyImage ? (
            <div className="relative w-9 h-9 rounded-lg overflow-hidden bg-white/5 border border-white/10 shrink-0">
              <Image
                src={p.jerseyImage}
                alt={p.name}
                fill
                sizes="36px"
                className="object-contain p-0.5"
                unoptimized
              />
            </div>
          ) : (
            <div
              className="w-8 h-8 rounded-lg flex items-center justify-center font-mono font-black text-xs border shrink-0"
              style={{
                backgroundColor: `${teamColor}20`,
                color: teamColor,
                borderColor: `${teamColor}60`,
              }}
            >
              {p.jersey || '•'}
            </div>
          )}

          <div className="min-w-0">
            <h4 className="text-xs sm:text-sm font-bold text-white truncate">{p.name}</h4>
            <div className="flex items-center gap-1.5 text-xs text-white/60 font-tech">
              <span className="uppercase font-semibold text-[#DFFF00]">{p.position || 'Player'}</span>
              <span>•</span>
              <span>No. {p.jersey || '-'}</span>
            </div>
          </div>
        </div>

        <button
          onClick={onClose}
          className="text-white/40 hover:text-white p-1 rounded-lg transition-colors cursor-pointer"
          aria-label="Close player card"
        >
          <X className="w-4 h-4" />
        </button>
      </div>

      <div className="grid grid-cols-4 gap-1.5 pt-1.5 border-t border-white/10 text-center text-xs font-mono">
        <div className="bg-white/5 rounded-lg p-1">
          <span className="text-white/50 block text-[10px] font-bold uppercase">{isGK ? 'Saves' : 'Shots'}</span>
          <span className="font-extrabold text-white text-xs sm:text-sm">{isGK ? stats.saves ?? 0 : stats.totalShots ?? 0}</span>
        </div>
        <div className="bg-white/5 rounded-lg p-1">
          <span className="text-white/50 block text-[10px] font-bold uppercase">{isGK ? 'Against' : 'Target'}</span>
          <span className="font-extrabold text-white text-xs sm:text-sm">{isGK ? stats.goalsConceded ?? 0 : stats.shotsOnTarget ?? 0}</span>
        </div>
        <div className="bg-white/5 rounded-lg p-1">
          <span className="text-white/50 block text-[10px] font-bold uppercase">Goals</span>
          <span className="font-extrabold text-[#DFFF00] text-xs sm:text-sm">{stats.totalGoals ?? 0}</span>
        </div>
        <div className="bg-white/5 rounded-lg p-1">
          <span className="text-white/50 block text-[10px] font-bold uppercase">{isGK ? 'Fouls' : 'Assists'}</span>
          <span className="font-extrabold text-white text-xs sm:text-sm">{isGK ? stats.foulsCommitted ?? 0 : stats.goalAssists ?? 0}</span>
        </div>
      </div>
    </div>
  );
}
