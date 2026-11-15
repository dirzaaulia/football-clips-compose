'use client';

import React from 'react';
import { MatchFormations } from '@/lib/database';
import { ensureLegibleHex } from '@/lib/colors';

interface MatchLineupsViewProps {
  formations?: MatchFormations | null;
  homeName: string;
  awayName: string;
  homeColor?: string | null;
  awayColor?: string | null;
}

export default function MatchLineupsView({
  formations,
  homeName,
  awayName,
  homeColor,
  awayColor,
}: MatchLineupsViewProps) {
  if (!formations) return null;

  const hColor = ensureLegibleHex(formations?.home?.color || homeColor, '#DFFF00');
  const aColor = ensureLegibleHex(formations?.away?.color || awayColor, '#38BDF8');

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 select-none">
      {/* Home Team Lineup */}
      <div className="bg-[#121316] border border-white/10 rounded-2xl p-4 sm:p-5 space-y-3 shadow-md">
        <div className="flex items-center justify-between pb-2.5 border-b border-white/10">
          <span className="font-tech font-bold text-white text-xs sm:text-sm truncate uppercase tracking-wider">{homeName}</span>
          {formations?.home?.formation && (
            <span
              className="px-2.5 py-0.5 rounded-lg text-[10px] font-mono font-black border"
              style={{ color: hColor, borderColor: `${hColor}40`, backgroundColor: `${hColor}18` }}
            >
              {formations.home.formation}
            </span>
          )}
        </div>

        <div className="space-y-1.5">
          {(formations?.home?.starters || []).map((p, idx) => (
            <div
              key={idx}
              className="flex items-center justify-between text-xs py-1.5 px-2 rounded-xl hover:bg-[#1A1C22] transition-colors"
            >
              <div className="flex items-center gap-2.5 min-w-0">
                <span
                  className="w-5 text-center font-mono font-bold text-[11px] shrink-0"
                  style={{ color: hColor }}
                >
                  {p.jersey || '-'}
                </span>
                <span className="font-tech font-medium text-white truncate">{p.name}</span>
              </div>
              <span className="text-[10px] text-white/40 font-bold font-mono shrink-0 ml-2">
                {p.position || ''}
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* Away Team Lineup */}
      <div className="bg-[#121316] border border-white/10 rounded-2xl p-4 sm:p-5 space-y-3 shadow-md">
        <div className="flex items-center justify-between pb-2.5 border-b border-white/10">
          <span className="font-tech font-bold text-white text-xs sm:text-sm truncate uppercase tracking-wider">{awayName}</span>
          {formations?.away?.formation && (
            <span
              className="px-2.5 py-0.5 rounded-lg text-[10px] font-mono font-black border"
              style={{ color: aColor, borderColor: `${aColor}40`, backgroundColor: `${aColor}18` }}
            >
              {formations.away.formation}
            </span>
          )}
        </div>

        <div className="space-y-1.5">
          {(formations?.away?.starters || []).map((p, idx) => (
            <div
              key={idx}
              className="flex items-center justify-between text-xs py-1.5 px-2 rounded-xl hover:bg-[#1A1C22] transition-colors"
            >
              <div className="flex items-center gap-2.5 min-w-0">
                <span
                  className="w-5 text-center font-mono font-bold text-[11px] shrink-0"
                  style={{ color: aColor }}
                >
                  {p.jersey || '-'}
                </span>
                <span className="font-tech font-medium text-white truncate">{p.name}</span>
              </div>
              <span className="text-[10px] text-white/40 font-bold font-mono shrink-0 ml-2">
                {p.position || ''}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
