'use client';

import React, { useMemo } from 'react';
import { Eye, EyeOff } from 'lucide-react';
import { MatchRecord, MatchEvent } from '@/lib/database';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';
import MatchScrubberStrip, {
  GoalBallIcon,
  extractScorerDisplayName,
} from './MatchScrubberStrip';

interface MatchScoreboardBannerProps {
  match: MatchRecord;
  homeName: string;
  awayName: string;
  isSpoilerFree: boolean;
  isScoreRevealed: boolean;
  onRevealScore: () => void;
  onHideScore: () => void;
}

function isTrueGoalEvent(e: MatchEvent): boolean {
  if (e.scoring_play === true) return true;
  const t = (e.type || e.type_text || '').toLowerCase();
  if (!t.includes('goal')) return false;
  if (t.includes('kick') || t.includes('attempt') || t.includes('miss') || t.includes('save')) {
    return false;
  }
  return true;
}

function groupGoalsByPlayer(events: MatchEvent[]): Array<{ name: string; minutes: string[] }> {
  const map = new Map<string, string[]>();
  for (const e of events) {
    const name = extractScorerDisplayName(e);
    const isPen = (e.type || e.text || '').toLowerCase().includes('penalty');
    const isOG = (e.type || e.text || '').toLowerCase().includes('own goal');
    const suffix = isOG ? ' (OG)' : isPen ? ' (P)' : '';
    const clock = (e.clock || '').trim();
    const minStr = clock ? `${clock}${suffix}` : `Goal${suffix}`;
    const existing = map.get(name) || [];
    if (!existing.includes(minStr)) existing.push(minStr);
    map.set(name, existing);
  }
  return Array.from(map.entries()).map(([name, minutes]) => ({ name, minutes }));
}

export default function MatchScoreboardBanner({
  match,
  homeName,
  awayName,
  isSpoilerFree,
  isScoreRevealed,
  onRevealScore,
  onHideScore,
}: MatchScoreboardBannerProps) {
  const isFinished = match.status === 'FINISHED' || match.status === 'AWARDED';
  const isLive = ['IN_PLAY', 'PAUSED', 'LIVE'].includes(match.status);
  const isScheduled = ['TIMED', 'SCHEDULED'].includes(match.status);
  const isHidden = isSpoilerFree && !isScoreRevealed && (isFinished || isLive);

  const homeScoreVal = match.home_score ?? 0;
  const awayScoreVal = match.away_score ?? 0;

  const { homeScorers, awayScorers } = useMemo(() => {
    const allGoals = (match.events || []).filter(isTrueGoalEvent);
    const hRaw = homeScoreVal > 0 ? allGoals.filter((e) => e.side === 'home').slice(0, homeScoreVal) : [];
    const aRaw = awayScoreVal > 0 ? allGoals.filter((e) => e.side === 'away').slice(0, awayScoreVal) : [];
    return {
      homeScorers: groupGoalsByPlayer(hRaw),
      awayScorers: groupGoalsByPlayer(aRaw),
    };
  }, [match.events, homeScoreVal, awayScoreVal]);

  const hasScorers = !isHidden && (homeScorers.length > 0 || awayScorers.length > 0);

  return (
    <div className="w-full bg-[#121316] border border-white/10 rounded-2xl p-5 sm:p-6 shadow-xl flex flex-col gap-4 select-none relative overflow-hidden">
      <div className="absolute -top-12 -left-12 w-48 h-48 bg-[#DFFF00]/5 rounded-full blur-3xl pointer-events-none" />

      {/* Main Face-Off Scoreboard Grid (3-Column Symmetric Layout) */}
      <div className="grid grid-cols-[1fr_auto_1fr] items-center gap-3 sm:gap-6">
        {/* Home Team */}
        <div className="flex items-center gap-3 sm:gap-4 min-w-0">
          <GlassLogoBadge
            src={match.home_team_crest}
            alt={homeName}
            size={52}
            className="shrink-0"
            imageClassName="object-contain p-1.5 drop-shadow-md"
          />
          <div className="flex flex-col min-w-0">
            <span className="font-scoreboard text-lg sm:text-3xl text-white tracking-wide leading-none">
              {homeName}
            </span>
            <div className="flex items-center gap-1.5 mt-1">
              <span className="px-2 py-0.5 rounded bg-white/10 text-xs font-bold text-white/90 uppercase">
                HOME
              </span>
              <span className="font-tech text-xs text-white/75 hidden sm:inline">
                {match.stage || match.round_name || 'Matchday'}
              </span>
            </div>
          </div>
        </div>

        {/* Score Centerpiece */}
        <div className="flex flex-col items-center justify-center shrink-0 px-2">
          {isScheduled && !isLive ? (
            <span className="px-4 py-1.5 rounded-xl bg-[#1A1C22] border border-white/10 text-xs font-tech font-black text-white/60 tracking-widest">
              VS
            </span>
          ) : (
            <button
              type="button"
              onClick={() => (isHidden ? onRevealScore() : onHideScore())}
              className="flex flex-col items-center cursor-pointer group"
              title={isHidden ? 'Click to reveal score' : 'Click to hide score'}
            >
              <div
                className={`flex items-baseline justify-center gap-1.5 font-scoreboard text-4xl sm:text-6xl text-white tracking-tight transition-all ${
                  isHidden ? 'blur-md opacity-40 group-hover:opacity-75' : ''
                }`}
              >
                <span>{homeScoreVal}</span>
                <span className="text-[#DFFF00] opacity-80 mx-0.5">:</span>
                <span>{awayScoreVal}</span>
              </div>
              <div className="flex items-center gap-1.5 mt-1">
                <span className="font-tech text-xs font-black uppercase tracking-wider px-2.5 py-0.5 rounded bg-[#1A1C22] text-[#DFFF00] border border-white/10">
                  {isLive ? `LIVE ${match.display_clock || ''}` : 'FULL TIME'}
                </span>
                {isHidden ? (
                  <Eye className="w-3.5 h-3.5 text-[#DFFF00]" />
                ) : (
                  <EyeOff className="w-3.5 h-3.5 text-white/60" />
                )}
              </div>
            </button>
          )}
        </div>

        {/* Away Team */}
        <div className="flex items-center justify-end gap-3 sm:gap-4 min-w-0 text-right">
          <div className="flex flex-col min-w-0 items-end">
            <span className="font-scoreboard text-lg sm:text-3xl text-white tracking-wide leading-none">
              {awayName}
            </span>
            <div className="flex items-center justify-end gap-1.5 mt-1">
              <span className="font-tech text-xs text-white/75 hidden sm:inline">
                {match.competition_name || 'Official Fixture'}
              </span>
              <span className="px-2 py-0.5 rounded bg-white/10 text-xs font-bold text-white/90 uppercase">
                AWAY
              </span>
            </div>
          </div>
          <GlassLogoBadge
            src={match.away_team_crest}
            alt={awayName}
            size={52}
            className="shrink-0"
            imageClassName="object-contain p-1.5 drop-shadow-md"
          />
        </div>
      </div>

      {/* Dedicated Goal Scorers Strip */}
      {hasScorers && (
        <div className="grid grid-cols-[1fr_auto_1fr] items-start gap-3 pt-3 border-t border-white/10 font-tech text-xs">
          <div className="flex flex-col items-end gap-1 text-right min-w-0">
            {homeScorers.map((g, i) => (
              <div key={i} className="inline-flex items-center justify-end gap-2 text-white/90 max-w-full">
                <span className="font-bold text-xs sm:text-sm text-white">{g.name}</span>
                <span className="font-mono text-xs font-black text-[#DFFF00] shrink-0">
                  {g.minutes.join(', ')}
                </span>
              </div>
            ))}
          </div>

          <div className="flex items-center justify-center pt-0.5 px-1">
            <GoalBallIcon className="w-4 h-4 text-[#DFFF00]" />
          </div>

          <div className="flex flex-col items-start gap-1 text-left min-w-0">
            {awayScorers.map((g, i) => (
              <div key={i} className="inline-flex items-center gap-2 text-white/90 max-w-full">
                <span className="font-mono text-xs font-black text-[#DFFF00] shrink-0">
                  {g.minutes.join(', ')}
                </span>
                <span className="font-bold text-xs sm:text-sm text-white">{g.name}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Dual-Lane Key Moments Timeline Scrubber */}
      {!isHidden && match.events && match.events.length > 0 && (
        <MatchScrubberStrip events={match.events} homeName={homeName} awayName={awayName} />
      )}
    </div>
  );
}
