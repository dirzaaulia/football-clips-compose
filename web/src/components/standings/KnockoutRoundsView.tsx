'use client';

import React, { useState } from 'react';
import Image from 'next/image';
import { MatchRecord } from '@/lib/database';
import { Play, Trophy, Calendar } from 'lucide-react';
import { useVideoPlayer } from '@/hooks/useVideoPlayer';

interface KnockoutRoundsViewProps {
  matches: MatchRecord[];
  competitionName: string;
}

export default function KnockoutRoundsView({ matches, competitionName }: KnockoutRoundsViewProps) {
  const { playVideo } = useVideoPlayer();
  const [selectedRoundFilter, setSelectedRoundFilter] = useState('ALL');

  // Filter out league phase matches
  const knockoutMatches = matches.filter((m) => {
    const r = (m.round_name || '').toLowerCase();
    return !r.includes('league phase');
  });

  // Extract distinct rounds
  const distinctRounds = Array.from(
    new Set(knockoutMatches.map((m) => m.round_name || 'Knockout Round'))
  );

  const displayedMatches = selectedRoundFilter === 'ALL'
    ? knockoutMatches
    : knockoutMatches.filter((m) => (m.round_name || 'Knockout Round') === selectedRoundFilter);

  if (knockoutMatches.length === 0) {
    return (
      <div className="py-16 text-center text-[#94A3B8] bg-[#181C20] border border-[#334155] rounded-3xl p-6">
        <Trophy className="w-12 h-12 text-[#7098F8]/50 mx-auto mb-3" />
        <h3 className="text-base font-bold text-white">No Knockout Fixtures Scheduled</h3>
        <p className="text-xs text-[#94A3B8] mt-1 max-w-md mx-auto">
          Tournament rounds for {competitionName} will appear as matches are scheduled.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Round Filter Tabs */}
      {distinctRounds.length > 1 && (
        <div className="flex items-center gap-2 overflow-x-auto pb-2">
          <button
            onClick={() => setSelectedRoundFilter('ALL')}
            className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer whitespace-nowrap ${
              selectedRoundFilter === 'ALL'
                ? 'bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white shadow-sm'
                : 'bg-[#181C20] text-[#94A3B8] hover:text-white border border-[#334155]'
            }`}
          >
            All Rounds ({knockoutMatches.length})
          </button>
          {distinctRounds.map((rnd) => (
            <button
              key={rnd}
              onClick={() => setSelectedRoundFilter(rnd)}
              className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer whitespace-nowrap ${
                selectedRoundFilter === rnd
                  ? 'bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white shadow-sm'
                  : 'bg-[#181C20] text-[#94A3B8] hover:text-white border border-[#334155]'
              }`}
            >
              {rnd}
            </button>
          ))}
        </div>
      )}

      {/* Match Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-3.5">
        {displayedMatches.map((m) => {
          const hasHighlight = Boolean(m.highlight_video_id || m.backup_highlight_video_id);
          const isFinished = m.status === 'FINISHED';
          const isLive = ['IN_PLAY', 'PAUSED', 'LIVE'].includes(m.status);

          return (
            <div
              key={m.id}
              className={`flex flex-col justify-between p-4 rounded-2xl bg-[#181C20] border transition-all duration-200 ${
                isLive
                  ? 'border-emerald-500/50 shadow-[0_0_12px_rgba(16,185,129,0.15)]'
                  : 'border-[#334155] hover:border-[#7098F8]/50 shadow-sm'
              }`}
            >
              <div>
                <div className="flex items-center justify-between text-xs text-[#94A3B8] font-bold mb-3 pb-2 border-b border-[#334155]/60">
                  <span className="text-[#7098F8] truncate max-w-[160px]">
                    {m.round_name || 'Knockout'}
                  </span>
                  <div className="flex items-center gap-1.5 shrink-0">
                    <Calendar className="w-3.5 h-3.5 text-[#94A3B8]" />
                    <span>{m.utc_date ? new Date(m.utc_date).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' }) : 'TBD'}</span>
                  </div>
                </div>

                {/* Home Team */}
                <div className="flex items-center justify-between py-1 px-1">
                  <div className="flex items-center gap-2.5 min-w-0">
                    {m.home_team_crest ? (
                      <Image src={m.home_team_crest} alt={m.home_team_name || m.home_team} width={22} height={22} className="object-contain shrink-0" />
                    ) : (
                      <div className="w-5 h-5 rounded-full bg-zinc-800 shrink-0" />
                    )}
                    <span className="text-sm font-bold text-white truncate">
                      {m.home_team_name || m.home_team}
                    </span>
                  </div>
                  <span className={`text-sm font-black ml-2 ${isFinished ? 'text-white' : 'text-zinc-500'}`}>
                    {m.home_score !== null && m.home_score !== undefined ? m.home_score : '-'}
                  </span>
                </div>

                {/* Away Team */}
                <div className="flex items-center justify-between py-1 px-1 mt-1">
                  <div className="flex items-center gap-2.5 min-w-0">
                    {m.away_team_crest ? (
                      <Image src={m.away_team_crest} alt={m.away_team_name || m.away_team} width={22} height={22} className="object-contain shrink-0" />
                    ) : (
                      <div className="w-5 h-5 rounded-full bg-zinc-800 shrink-0" />
                    )}
                    <span className="text-sm font-bold text-white truncate">
                      {m.away_team_name || m.away_team}
                    </span>
                  </div>
                  <span className={`text-sm font-black ml-2 ${isFinished ? 'text-white' : 'text-zinc-500'}`}>
                    {m.away_score !== null && m.away_score !== undefined ? m.away_score : '-'}
                  </span>
                </div>
              </div>

              {/* Action: Watch Highlights or Match Status */}
              <div className="mt-3 pt-2.5 border-t border-[#334155]/50 flex items-center justify-between">
                <div>
                  {isLive && (
                    <span className="px-2 py-0.5 rounded-full text-[10px] font-black bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 animate-pulse">
                      LIVE {m.display_clock || ''}
                    </span>
                  )}
                  {isFinished && (
                    <span className="text-[11px] font-bold text-[#94A3B8]">
                      Full Time
                    </span>
                  )}
                  {!isLive && !isFinished && (
                    <span className="text-[11px] font-semibold text-zinc-500">
                      {m.utc_date ? new Date(m.utc_date).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'Upcoming'}
                    </span>
                  )}
                </div>

                {hasHighlight && (
                  <button
                    type="button"
                    onClick={() => playVideo(m)}
                    className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold bg-[#1E3A8A]/50 hover:bg-[#1E3A8A] text-[#4FC3F7] hover:text-white border border-[#7098F8]/40 transition-all cursor-pointer shadow-sm group"
                  >
                    <Play className="w-3.5 h-3.5 fill-current text-[#4FC3F7] group-hover:text-white transition-colors" />
                    <span>Highlights</span>
                  </button>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
