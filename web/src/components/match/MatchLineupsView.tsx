'use client';

import React from 'react';
import { MatchFormations } from '@/lib/database';

interface MatchLineupsViewProps {
  formations?: MatchFormations | null;
  homeName: string;
  awayName: string;
}

export default function MatchLineupsView({
  formations,
  homeName,
  awayName,
}: MatchLineupsViewProps) {
  if (!formations) return null;

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
      {/* Home Team Lineup */}
      <div className="bg-[#101418] border border-[#334155]/70 rounded-2xl p-4 space-y-3">
        <div className="flex items-center justify-between pb-2 border-b border-[#334155]/50">
          <span className="font-bold text-white text-xs sm:text-sm truncate">{homeName}</span>
          {formations?.home?.formation && (
            <span className="px-2 py-0.5 rounded-lg bg-[#1E3A8A] text-[#7098F8] text-[10px] font-mono font-black">
              {formations.home.formation}
            </span>
          )}
        </div>

        <div className="space-y-1.5">
          {(formations?.home?.starters || []).map((p, idx) => (
            <div
              key={idx}
              className="flex items-center justify-between text-xs py-1 px-1.5 rounded-lg hover:bg-[#1E2227] transition-colors"
            >
              <div className="flex items-center gap-2">
                <span className="w-5 text-center font-mono font-bold text-[#7098F8] text-[11px]">
                  {p.jersey || '-'}
                </span>
                <span className="font-medium text-white">{p.name}</span>
              </div>
              <span className="text-[10px] text-[#94A3B8] font-bold font-mono">
                {p.position || ''}
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* Away Team Lineup */}
      <div className="bg-[#101418] border border-[#334155]/70 rounded-2xl p-4 space-y-3">
        <div className="flex items-center justify-between pb-2 border-b border-[#334155]/50">
          <span className="font-bold text-white text-xs sm:text-sm truncate">{awayName}</span>
          {formations?.away?.formation && (
            <span className="px-2 py-0.5 rounded-lg bg-[#0E7490] text-[#4FC3F7] text-[10px] font-mono font-black">
              {formations.away.formation}
            </span>
          )}
        </div>

        <div className="space-y-1.5">
          {(formations?.away?.starters || []).map((p, idx) => (
            <div
              key={idx}
              className="flex items-center justify-between text-xs py-1 px-1.5 rounded-lg hover:bg-[#1E2227] transition-colors"
            >
              <div className="flex items-center gap-2">
                <span className="w-5 text-center font-mono font-bold text-[#4FC3F7] text-[11px]">
                  {p.jersey || '-'}
                </span>
                <span className="font-medium text-white">{p.name}</span>
              </div>
              <span className="text-[10px] text-[#94A3B8] font-bold font-mono">
                {p.position || ''}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
