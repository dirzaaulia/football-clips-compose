import React from 'react';
import Link from 'next/link';
import { Play } from 'lucide-react';
import { MatchRecord } from '@/lib/supabase';

interface FixtureItemCardProps {
  match: MatchRecord;
}

export default function FixtureItemCard({ match }: FixtureItemCardProps) {
  const isFinished = match.status === 'FINISHED';
  const homeName = match.home_team_name || match.home_team;
  const awayName = match.away_team_name || match.away_team;
  const matchTime = new Date(match.utc_date).toLocaleTimeString('en-GB', {
    hour: '2-digit',
    minute: '2-digit',
  });

  return (
    <div className="bg-[#1E2227] border border-[#334155] rounded-2xl p-4 flex flex-col sm:flex-row items-center justify-between gap-4 transition-all hover:border-[#7098F8]/50">
      <div className="flex items-center gap-3 w-full sm:w-auto">
        <div className="flex flex-col text-left">
          <span className="text-[10px] uppercase font-bold text-[#7098F8] tracking-wider">
            {match.competition_name}
          </span>
          <span className="text-xs text-[#94A3B8] font-medium">
            {isFinished ? 'Full Time' : matchTime}
          </span>
        </div>
      </div>

      <div className="flex items-center justify-center gap-4 flex-1 w-full sm:w-auto">
        <span className="text-sm font-bold text-white text-right flex-1 truncate">{homeName}</span>
        <div className="px-3 py-1 bg-[#101418] border border-[#334155] rounded-xl text-center min-w-[64px]">
          <span className="text-sm font-black text-white">
            {isFinished ? `${match.home_score ?? 0} - ${match.away_score ?? 0}` : 'vs'}
          </span>
        </div>
        <span className="text-sm font-bold text-white text-left flex-1 truncate">{awayName}</span>
      </div>

      <div className="flex items-center gap-2 w-full sm:w-auto justify-end">
        {match.highlight_video_id ? (
          <Link
            href={`/match/${match.id}`}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white text-xs font-bold shadow-[0_0_10px_rgba(112,152,248,0.3)] hover:brightness-110 transition-all"
          >
            <Play className="w-3.5 h-3.5 fill-current" />
            <span>Match Centre</span>
          </Link>
        ) : (
          <Link
            href={`/match/${match.id}`}
            className="text-[11px] text-[#94A3B8] hover:text-white font-medium px-2.5 py-1 bg-[#101418] rounded-lg border border-[#334155]/60 hover:border-[#7098F8]/40 transition-colors"
          >
            {isFinished ? 'Match Centre' : 'Preview'}
          </Link>
        )}
      </div>
    </div>
  );
}
