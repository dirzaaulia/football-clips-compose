'use client';

import React from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { Radio, ArrowRight, Shield } from 'lucide-react';
import { MatchRecord } from '@/lib/supabase';

interface LiveMatchSectionProps {
  liveMatches: MatchRecord[];
  className?: string;
}

export default function LiveMatchSection({ liveMatches, className = '' }: LiveMatchSectionProps) {
  if (!liveMatches || liveMatches.length === 0) return null;

  return (
    <section className={`mb-8 ${className}`}>
      {/* Section Header */}
      <div className="flex items-center justify-between pb-3 border-b border-red-500/20 mb-4">
        <div className="flex items-center gap-2.5">
          <div className="relative flex items-center justify-center">
            <span className="absolute w-3.5 h-3.5 rounded-full bg-red-500/40 animate-ping" />
            <span className="relative w-2.5 h-2.5 rounded-full bg-red-500 shadow-[0_0_10px_#ef4444]" />
          </div>
          <div className="flex items-center gap-2">
            <h2 className="text-base sm:text-lg font-black text-white tracking-tight uppercase">
              Live Matches in Progress
            </h2>
            <span className="px-2 py-0.5 rounded-full text-[10px] font-black uppercase bg-red-500/20 text-red-400 border border-red-500/40 tracking-wider">
              {liveMatches.length} Active
            </span>
          </div>
        </div>
        <span className="text-xs text-[#94A3B8] font-medium hidden sm:inline">
          Semi-live scores direct from stadium feeds
        </span>
      </div>

      {/* Live Match Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {liveMatches.map((m) => {
          const homeName = m.home_team_name || m.home_team;
          const awayName = m.away_team_name || m.away_team;
          const isPaused = m.status === 'PAUSED';
          const statusText = isPaused ? 'Half Time' : 'In Play';

          return (
            <div
              key={m.id}
              className="relative overflow-hidden rounded-2xl bg-gradient-to-b from-[#1E2227] via-[#1A1F26] to-[#12151B] border border-red-500/30 p-4 transition-all hover:border-red-500/60 hover:shadow-[0_0_20px_rgba(239,68,68,0.2)] flex flex-col justify-between"
            >
              {/* Top Bar: League & Live Pill */}
              <div className="flex items-center justify-between gap-2 mb-3">
                <span className="text-[10px] uppercase font-bold text-[#7098F8] tracking-wider truncate">
                  {m.competition_name || m.competition_id}
                </span>

                <div className="flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-red-500/15 border border-red-500/40 shadow-[0_0_8px_rgba(239,68,68,0.25)]">
                  <span className="w-1.5 h-1.5 rounded-full bg-red-500 animate-pulse" />
                  <span className="text-[10px] font-black uppercase text-red-400 tracking-wider">
                    {statusText}
                  </span>
                </div>
              </div>

              {/* Match Teams and Big Score */}
              <div className="flex items-center justify-between gap-3 py-2">
                {/* Home Team */}
                <div className="flex flex-col items-center text-center flex-1 min-w-0">
                  <div className="relative w-11 h-11 mb-1.5 flex items-center justify-center">
                    {m.home_team_crest ? (
                      <Image
                        src={m.home_team_crest}
                        alt={homeName}
                        width={44}
                        height={44}
                        className="object-contain max-h-11"
                        unoptimized
                      />
                    ) : (
                      <div className="w-10 h-10 rounded-full bg-[#181C20] border border-[#334155] flex items-center justify-center text-white font-black text-xs">
                        <Shield className="w-5 h-5 text-[#94A3B8]" />
                      </div>
                    )}
                  </div>
                  <span className="text-xs font-bold text-white line-clamp-2 leading-tight">
                    {homeName}
                  </span>
                </div>

                {/* Live Score Counter */}
                <div className="flex flex-col items-center justify-center px-4 py-2 rounded-xl bg-[#101418] border border-red-500/40 shadow-[0_0_12px_rgba(239,68,68,0.2)] shrink-0">
                  <span className="text-xl sm:text-2xl font-black text-white tracking-wider">
                    {m.home_score ?? 0} - {m.away_score ?? 0}
                  </span>
                  <span className="text-[9px] uppercase font-bold text-red-400 mt-0.5 tracking-widest flex items-center gap-1">
                    <Radio className="w-2.5 h-2.5 animate-pulse" />
                    LIVE
                  </span>
                </div>

                {/* Away Team */}
                <div className="flex flex-col items-center text-center flex-1 min-w-0">
                  <div className="relative w-11 h-11 mb-1.5 flex items-center justify-center">
                    {m.away_team_crest ? (
                      <Image
                        src={m.away_team_crest}
                        alt={awayName}
                        width={44}
                        height={44}
                        className="object-contain max-h-11"
                        unoptimized
                      />
                    ) : (
                      <div className="w-10 h-10 rounded-full bg-[#181C20] border border-[#334155] flex items-center justify-center text-white font-black text-xs">
                        <Shield className="w-5 h-5 text-[#94A3B8]" />
                      </div>
                    )}
                  </div>
                  <span className="text-xs font-bold text-white line-clamp-2 leading-tight">
                    {awayName}
                  </span>
                </div>
              </div>

              {/* Action Button */}
              <div className="mt-3 pt-2.5 border-t border-[#334155]/60 flex items-center justify-between">
                <span className="text-[11px] text-[#94A3B8] font-medium">
                  {isPaused ? 'Half-time interval' : 'Match in progress'}
                </span>
                <Link
                  href={`/match/${m.id}`}
                  className="flex items-center gap-1 px-3 py-1 rounded-xl bg-gradient-to-r from-red-600 to-red-500 hover:from-red-500 hover:to-red-400 text-white text-xs font-bold shadow-[0_0_12px_rgba(239,68,68,0.35)] transition-all cursor-pointer"
                >
                  <span>Match Centre</span>
                  <ArrowRight className="w-3 h-3" />
                </Link>
              </div>
            </div>
          );
        })}
      </div>
    </section>
  );
}
