'use client';

import React from 'react';
import { MatchRecord } from '@/lib/database';
import { Play, Trophy, Calendar, Award } from 'lucide-react';
import { useVideoPlayer } from '@/hooks/useVideoPlayer';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';

interface KnockoutBracketViewProps {
  matches: MatchRecord[];
  competitionName: string;
}

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
  'final',
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
  if (lower.includes('final') && !lower.includes('semi') && !lower.includes('quarter'))
    return 'Final';
  if (lower.includes('semi')) return 'Semi-finals';
  if (lower.includes('quarter')) return 'Quarter-finals';
  if (lower.includes('round of 16') || lower.includes('cup round')) return 'Round of 16';
  if (lower.includes('playoff')) return 'Knockout Play-offs';
  return roundName;
}

function KnockoutTeamRow({
  name,
  crest,
  score,
  penalties,
  isWinner,
  isFinished,
}: {
  name: string;
  crest?: string | null;
  score?: number | null;
  penalties?: number | null;
  isWinner: boolean;
  isFinished: boolean;
}) {
  return (
    <div
      className={`flex items-center justify-between py-1.5 px-2.5 rounded-xl transition-colors ${
        isWinner ? 'bg-[#DFFF00]/10 font-bold border border-[#DFFF00]/25' : ''
      }`}
    >
      <div className="flex items-center gap-2.5 min-w-0">
        <GlassLogoBadge
          src={crest}
          alt={name}
          size={22}
          imageClassName="object-contain p-0.5"
        />
        <span
          className={`text-xs truncate ${
            isWinner ? 'text-white font-extrabold' : 'text-white/80'
          }`}
        >
          {name}
        </span>
      </div>
      <span
        className={`text-xs font-tech font-bold ml-2 ${
          isWinner ? 'text-[#DFFF00]' : isFinished ? 'text-white' : 'text-white/40'
        }`}
      >
        {score !== null && score !== undefined
          ? penalties != null
            ? `${score} (${penalties})`
            : score
          : '-'}
      </span>
    </div>
  );
}

function KnockoutMatchCard({
  match,
  isFinal,
  onPlay,
}: {
  match: MatchRecord;
  isFinal: boolean;
  onPlay: (m: MatchRecord) => void;
}) {
  const hasHighlight = Boolean(
    match.highlight_video_id || match.backup_highlight_video_id,
  );
  const isFinished = match.status === 'FINISHED';
  const isLive = ['IN_PLAY', 'PAUSED', 'LIVE'].includes(match.status);
  const hScore = match.home_score ?? 0;
  const aScore = match.away_score ?? 0;
  const hPen = match.home_penalties ?? 0;
  const aPen = match.away_penalties ?? 0;
  const homeWinner =
    isFinished && (hScore > aScore || (hScore === aScore && hPen > aPen));
  const awayWinner =
    isFinished && (aScore > hScore || (hScore === aScore && aPen > hPen));

  return (
    <div
      className={`relative flex flex-col p-3.5 rounded-2xl bg-[#1A1C22] border transition-all duration-200 ${
        isFinal
          ? 'border-[#DFFF00]/50 shadow-[0_0_20px_rgba(223,255,0,0.15)] bg-gradient-to-b from-[#1A1C22] to-[#121316]'
          : isLive
          ? 'border-[#FF2E4D]/50 shadow-[0_0_12px_rgba(255,46,77,0.15)]'
          : 'border-white/10 hover:border-[#DFFF00]/40 shadow-sm'
      }`}
    >
      <div className="flex items-center justify-between text-[10px] text-white/50 font-tech font-bold mb-2">
        <span className="flex items-center gap-1">
          <Calendar className="w-3 h-3 text-[#DFFF00]" />
          {match.utc_date
            ? new Date(match.utc_date).toLocaleDateString('en-GB', {
                day: 'numeric',
                month: 'short',
                year: 'numeric',
              })
            : 'TBD'}
        </span>
        {isLive && (
          <span className="px-1.5 py-0.5 rounded bg-[#FF2E4D]/20 text-[#FF2E4D] border border-[#FF2E4D]/40 font-bold animate-pulse">
            LIVE {match.display_clock || ''}
          </span>
        )}
        {isFinished && (
          <span className="px-1.5 py-0.5 rounded bg-[#121316] text-white/60 border border-white/10">
            FT
          </span>
        )}
        {!isLive && !isFinished && (
          <span className="text-white/40">
            {match.utc_date
              ? new Date(match.utc_date).toLocaleTimeString([], {
                  hour: '2-digit',
                  minute: '2-digit',
                })
              : 'Upcoming'}
          </span>
        )}
      </div>

      <div className="space-y-1">
        <KnockoutTeamRow
          name={match.home_team_name || match.home_team}
          crest={match.home_team_crest}
          score={match.home_score}
          penalties={match.home_penalties}
          isWinner={homeWinner}
          isFinished={isFinished}
        />
        <KnockoutTeamRow
          name={match.away_team_name || match.away_team}
          crest={match.away_team_crest}
          score={match.away_score}
          penalties={match.away_penalties}
          isWinner={awayWinner}
          isFinished={isFinished}
        />
      </div>

      {hasHighlight && (
        <button
          type="button"
          onClick={() => onPlay(match)}
          className="mt-2.5 w-full flex items-center justify-center gap-1.5 py-1.5 rounded-xl text-[11px] font-tech font-bold uppercase tracking-wider bg-[#DFFF00]/15 hover:bg-[#DFFF00] border border-[#DFFF00]/40 text-[#DFFF00] hover:text-[#0A0A0C] transition-all cursor-pointer shadow-sm"
        >
          <Play className="w-3 h-3 fill-current" />
          <span>Watch Highlights</span>
        </button>
      )}
    </div>
  );
}

export default function KnockoutBracketView({
  matches,
  competitionName,
}: KnockoutBracketViewProps) {
  const { playVideo } = useVideoPlayer();

  const knockoutMatches = matches.filter((m) => {
    const r = (m.round_name || '').toLowerCase();
    return !r.includes('league phase');
  });

  const roundsMap = new Map<string, MatchRecord[]>();
  for (const m of knockoutMatches) {
    const label = formatRoundDisplay(m.round_name);
    if (!roundsMap.has(label)) {
      roundsMap.set(label, []);
    }
    roundsMap.get(label)!.push(m);
  }

  const sortedRounds = Array.from(roundsMap.entries()).sort(
    (a, b) => getRoundWeight(a[0]) - getRoundWeight(b[0]),
  );

  if (knockoutMatches.length === 0) {
    return (
      <div className="py-16 text-center text-white/60 bg-[#1A1C22] border border-white/10 rounded-2xl p-6">
        <Trophy className="w-12 h-12 text-[#DFFF00]/50 mx-auto mb-3" />
        <h3 className="font-scoreboard text-xl uppercase tracking-wider text-white">
          Knockout Stage Pending
        </h3>
        <p className="text-xs text-white/50 font-tech mt-1 max-w-md mx-auto">
          Knockout fixtures will populate once qualified clubs are drawn for{' '}
          {competitionName}.
        </p>
      </div>
    );
  }

  return (
    <div className="w-full overflow-x-auto pb-6 pt-2">
      <div className="flex items-stretch gap-6 sm:gap-8 min-w-max px-1">
        {sortedRounds.map(([roundTitle, roundMatches]) => {
          const isFinal = roundTitle.toLowerCase() === 'final';
          return (
            <div key={roundTitle} className="flex flex-col min-w-[260px] sm:min-w-[300px]">
              <div className="flex items-center gap-2 pb-3 mb-4 border-b border-white/10">
                {isFinal ? (
                  <Award className="w-4 h-4 text-[#DFFF00] shrink-0" />
                ) : (
                  <Trophy className="w-4 h-4 text-[#DFFF00] shrink-0" />
                )}
                <span
                  className={`font-scoreboard text-lg uppercase tracking-wider ${
                    isFinal ? 'text-[#DFFF00]' : 'text-white'
                  }`}
                >
                  {roundTitle}
                </span>
                <span className="text-[10px] font-tech font-bold px-2.5 py-0.5 rounded-full bg-[#121316] text-white/60 border border-white/10 ml-auto">
                  {roundMatches.length} {roundMatches.length === 1 ? 'Match' : 'Ties'}
                </span>
              </div>

              <div className="flex-1 flex flex-col justify-around gap-4">
                {roundMatches.map((m) => (
                  <KnockoutMatchCard
                    key={m.id}
                    match={m}
                    isFinal={isFinal}
                    onPlay={playVideo}
                  />
                ))}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
