'use client';

import React from 'react';
import Image from 'next/image';
import { MatchRecord } from '@/lib/database';
import { Play, Trophy, Calendar, Award } from 'lucide-react';
import { useVideoPlayer } from '@/hooks/useVideoPlayer';

interface KnockoutBracketViewProps {
  matches: MatchRecord[];
  competitionName: string;
}

// Canonical round ordering for tournament trees
const ROUND_ORDER = [
  'round of 32',
  'fourth round',
  'knockout round playoffs',
  'round of 16',
  'cup round',
  'quarterfinals',
  'quarter-finals',
  'semifinals',
  'semi-finals',
  'final'
];

function getRoundWeight(roundName?: string | null): number {
  if (!roundName) return 50;
  const lower = roundName.toLowerCase().trim();
  for (let i = 0; i < ROUND_ORDER.length; i++) {
    if (lower.includes(ROUND_ORDER[i])) return i + 1;
  }
  return 50;
}

function formatRoundDisplay(roundName?: string | null): string {
  if (!roundName) return 'Knockout Round';
  const lower = roundName.toLowerCase();
  if (lower.includes('final') && !lower.includes('semi') && !lower.includes('quarter')) return 'Final';
  if (lower.includes('semi')) return 'Semi-finals';
  if (lower.includes('quarter')) return 'Quarter-finals';
  if (lower.includes('round of 16') || lower.includes('cup round')) return 'Round of 16';
  if (lower.includes('playoff')) return 'Knockout Play-offs';
  return roundName;
}

export default function KnockoutBracketView({ matches, competitionName }: KnockoutBracketViewProps) {
  const { playVideo } = useVideoPlayer();

  // Filter out any league phase matches
  const knockoutMatches = matches.filter((m) => {
    const r = (m.round_name || '').toLowerCase();
    return !r.includes('league phase');
  });

  // Group matches by normalized round
  const roundsMap = new Map<string, MatchRecord[]>();
  for (const m of knockoutMatches) {
    const label = formatRoundDisplay(m.round_name);
    if (!roundsMap.has(label)) {
      roundsMap.set(label, []);
    }
    roundsMap.get(label)!.push(m);
  }

  // Sort rounds chronologically towards the Final
  const sortedRounds = Array.from(roundsMap.entries()).sort(
    (a, b) => getRoundWeight(a[0]) - getRoundWeight(b[0])
  );

  if (knockoutMatches.length === 0) {
    return (
      <div className="py-16 text-center text-[#94A3B8] bg-[#181C20] border border-[#334155] rounded-3xl p-6">
        <Trophy className="w-12 h-12 text-[#7098F8]/50 mx-auto mb-3" />
        <h3 className="text-base font-bold text-white">Knockout Stage Pending</h3>
        <p className="text-xs text-[#94A3B8] mt-1 max-w-md mx-auto">
          Knockout fixtures will populate once qualified clubs are drawn for {competitionName}.
        </p>
      </div>
    );
  }

  return (
    <div className="w-full overflow-x-auto pb-6 pt-2">
      <div className="flex items-stretch gap-6 sm:gap-10 min-w-max px-2">
        {sortedRounds.map(([roundTitle, roundMatches], roundIdx) => {
          const isFinal = roundTitle.toLowerCase() === 'final';
          return (
            <div key={roundTitle} className="flex flex-col min-w-[260px] sm:min-w-[300px]">
              {/* Round Header */}
              <div className="flex items-center gap-2 pb-3 mb-4 border-b border-[#334155]/80">
                {isFinal ? (
                  <Award className="w-5 h-5 text-amber-400 shrink-0" />
                ) : (
                  <Trophy className="w-4 h-4 text-[#7098F8] shrink-0" />
                )}
                <span className={`text-sm font-black uppercase tracking-wider ${isFinal ? 'text-amber-400' : 'text-white'}`}>
                  {roundTitle}
                </span>
                <span className="text-[10px] font-black px-2 py-0.5 rounded-full bg-[#181C20] text-[#94A3B8] border border-[#334155] ml-auto">
                  {roundMatches.length} {roundMatches.length === 1 ? 'Match' : 'Ties'}
                </span>
              </div>

              {/* Match Cards in Column with Vertical Centering Spacing */}
              <div className="flex-1 flex flex-col justify-around gap-4">
                {roundMatches.map((m) => {
                  const hasHighlight = Boolean(m.highlight_video_id || m.backup_highlight_video_id);
                  const isFinished = m.status === 'FINISHED';
                  const isLive = ['IN_PLAY', 'PAUSED', 'LIVE'].includes(m.status);
                  const homeWinner = isFinished && (m.home_score ?? 0) > (m.away_score ?? 0);
                  const awayWinner = isFinished && (m.away_score ?? 0) > (m.home_score ?? 0);

                  return (
                    <div
                      key={m.id}
                      className={`relative flex flex-col p-3 rounded-2xl bg-[#181C20] border transition-all duration-200 ${
                        isFinal
                          ? 'border-amber-500/40 shadow-[0_0_16px_rgba(245,158,11,0.15)] bg-gradient-to-b from-[#1E2228] to-[#181C20]'
                          : isLive
                          ? 'border-emerald-500/50 shadow-[0_0_12px_rgba(16,185,129,0.15)]'
                          : 'border-[#334155] hover:border-[#7098F8]/50 shadow-sm'
                      }`}
                    >
                      {/* Top Match Info Row */}
                      <div className="flex items-center justify-between text-[10px] text-[#94A3B8] font-bold mb-2">
                        <span className="flex items-center gap-1">
                          <Calendar className="w-3 h-3 text-[#7098F8]" />
                          {m.utc_date ? new Date(m.utc_date).toLocaleDateString('en-GB', { day: 'numeric', month: 'short' }) : 'TBD'}
                        </span>
                        {isLive && (
                          <span className="px-1.5 py-0.5 rounded bg-emerald-500/20 text-emerald-400 border border-emerald-500/40 font-black animate-pulse">
                            LIVE {m.display_clock || ''}
                          </span>
                        )}
                        {isFinished && (
                          <span className="px-1.5 py-0.5 rounded bg-zinc-800 text-zinc-400">
                            FT
                          </span>
                        )}
                        {!isLive && !isFinished && (
                          <span className="text-zinc-500 font-medium">
                            {m.utc_date ? new Date(m.utc_date).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'Upcoming'}
                          </span>
                        )}
                      </div>

                      {/* Home Team */}
                      <div className={`flex items-center justify-between py-1.5 px-2 rounded-xl transition-colors ${homeWinner ? 'bg-emerald-500/10 font-bold' : ''}`}>
                        <div className="flex items-center gap-2.5 min-w-0">
                          {m.home_team_crest ? (
                            <Image src={m.home_team_crest} alt={m.home_team_name || m.home_team} width={20} height={20} className="object-contain shrink-0" />
                          ) : (
                            <div className="w-5 h-5 rounded-full bg-zinc-800 shrink-0" />
                          )}
                          <span className={`text-xs truncate ${homeWinner ? 'text-white font-extrabold' : 'text-[#E2E8F0]'}`}>
                            {m.home_team_name || m.home_team}
                          </span>
                        </div>
                        <span className={`text-xs font-black ml-2 ${homeWinner ? 'text-emerald-400' : isFinished ? 'text-white' : 'text-zinc-500'}`}>
                          {m.home_score !== null && m.home_score !== undefined ? m.home_score : '-'}
                        </span>
                      </div>

                      {/* Away Team */}
                      <div className={`flex items-center justify-between py-1.5 px-2 rounded-xl transition-colors mt-0.5 ${awayWinner ? 'bg-emerald-500/10 font-bold' : ''}`}>
                        <div className="flex items-center gap-2.5 min-w-0">
                          {m.away_team_crest ? (
                            <Image src={m.away_team_crest} alt={m.away_team_name || m.away_team} width={20} height={20} className="object-contain shrink-0" />
                          ) : (
                            <div className="w-5 h-5 rounded-full bg-zinc-800 shrink-0" />
                          )}
                          <span className={`text-xs truncate ${awayWinner ? 'text-white font-extrabold' : 'text-[#E2E8F0]'}`}>
                            {m.away_team_name || m.away_team}
                          </span>
                        </div>
                        <span className={`text-xs font-black ml-2 ${awayWinner ? 'text-emerald-400' : isFinished ? 'text-white' : 'text-zinc-500'}`}>
                          {m.away_score !== null && m.away_score !== undefined ? m.away_score : '-'}
                        </span>
                      </div>

                      {/* Highlight Video Replay Button */}
                      {hasHighlight && (
                        <button
                          type="button"
                          onClick={() => playVideo(m)}
                          className="mt-2.5 w-full flex items-center justify-center gap-1.5 py-1.5 rounded-xl text-[11px] font-extrabold bg-[#1E3A8A]/40 hover:bg-[#1E3A8A] border border-[#7098F8]/40 text-[#4FC3F7] hover:text-white transition-all cursor-pointer shadow-sm group"
                        >
                          <Play className="w-3 h-3 fill-current text-[#4FC3F7] group-hover:text-white transition-colors" />
                          <span>Watch Highlights</span>
                        </button>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
