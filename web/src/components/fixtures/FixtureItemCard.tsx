import React from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { Play, Radio, Shield } from 'lucide-react';
import { MatchRecord } from '@/lib/supabase';

interface FixtureItemCardProps {
  match: MatchRecord;
}

export default function FixtureItemCard({ match }: FixtureItemCardProps) {
  const isFinished = match.status === 'FINISHED';
  const isLive = match.status === 'IN_PLAY' || match.status === 'PAUSED' || match.status === 'LIVE';
  const isPaused = match.status === 'PAUSED';
  const homeName = match.home_team_name || match.home_team;
  const awayName = match.away_team_name || match.away_team;
  const matchTime = new Date(match.utc_date).toLocaleTimeString('en-GB', {
    hour: '2-digit',
    minute: '2-digit',
  });

  return (
    <div
      className={`rounded-2xl p-4 flex flex-col sm:flex-row items-center justify-between gap-4 transition-all ${
        isLive
          ? 'bg-gradient-to-r from-[#1E2227] via-[#211d24] to-[#1E2227] border border-red-500/40 shadow-[0_0_15px_rgba(239,68,68,0.15)] hover:border-red-500/60'
          : 'bg-[#1E2227] border border-[#334155] hover:border-[#7098F8]/50'
      }`}
    >
      {/* Left: League & Match Timing */}
      <div className="flex items-center gap-3 w-full sm:w-auto">
        <div className="flex flex-col text-left">
          <span className="text-[10px] uppercase font-bold text-[#7098F8] tracking-wider">
            {match.competition_name || match.competition_id}
          </span>
          {isLive ? (
            <span className="flex items-center gap-1.5 text-xs font-black text-red-400 tracking-wider uppercase mt-0.5">
              <span className="w-2 h-2 rounded-full bg-red-500 animate-pulse" />
              <span>{isPaused ? 'Half Time' : 'LIVE IN PLAY'}</span>
            </span>
          ) : (
            <span className="text-xs text-[#94A3B8] font-medium mt-0.5">
              {isFinished ? 'Full Time' : matchTime}
            </span>
          )}
        </div>
      </div>

      {/* Centre: Teams & Score */}
      <div className="flex items-center justify-center gap-3 sm:gap-4 flex-1 w-full sm:w-auto">
        {/* Home Team */}
        <div className="flex items-center gap-2 flex-1 justify-end min-w-0">
          <span className="text-sm font-bold text-white text-right truncate">{homeName}</span>
          {match.home_team_crest ? (
            <div className="relative w-6 h-6 shrink-0">
              <Image src={match.home_team_crest} alt={homeName} fill className="object-contain" unoptimized />
            </div>
          ) : null}
        </div>

        {/* Score Badge */}
        <div
          className={`px-3 py-1 rounded-xl text-center min-w-[70px] ${
            isLive
              ? 'bg-[#101418] border border-red-500/50 shadow-[0_0_10px_rgba(239,68,68,0.25)]'
              : 'bg-[#101418] border border-[#334155]'
          }`}
        >
          <span
            className={`text-sm font-black tracking-wider ${
              isLive ? 'text-red-400' : 'text-white'
            }`}
          >
            {isFinished || isLive ? `${match.home_score ?? 0} - ${match.away_score ?? 0}` : 'vs'}
          </span>
        </div>

        {/* Away Team */}
        <div className="flex items-center gap-2 flex-1 justify-start min-w-0">
          {match.away_team_crest ? (
            <div className="relative w-6 h-6 shrink-0">
              <Image src={match.away_team_crest} alt={awayName} fill className="object-contain" unoptimized />
            </div>
          ) : null}
          <span className="text-sm font-bold text-white text-left truncate">{awayName}</span>
        </div>
      </div>

      {/* Right: Action CTA */}
      <div className="flex items-center gap-2 w-full sm:w-auto justify-end">
        {isLive ? (
          <Link
            href={`/match/${match.id}`}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-gradient-to-r from-red-600 to-red-500 hover:from-red-500 hover:to-red-400 text-white text-xs font-black shadow-[0_0_12px_rgba(239,68,68,0.35)] transition-all cursor-pointer"
          >
            <Radio className="w-3.5 h-3.5 animate-pulse" />
            <span>Match Centre</span>
          </Link>
        ) : match.highlight_video_id ? (
          <Link
            href={`/match/${match.id}`}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white text-xs font-bold shadow-[0_0_10px_rgba(112,152,248,0.3)] hover:brightness-110 transition-all cursor-pointer"
          >
            <Play className="w-3.5 h-3.5 fill-current" />
            <span>Match Centre</span>
          </Link>
        ) : (
          <Link
            href={`/match/${match.id}`}
            className="text-[11px] text-[#94A3B8] hover:text-white font-medium px-2.5 py-1 bg-[#101418] rounded-lg border border-[#334155]/60 hover:border-[#7098F8]/40 transition-colors cursor-pointer"
          >
            {isFinished ? 'Match Centre' : 'Preview'}
          </Link>
        )}
      </div>
    </div>
  );
}

