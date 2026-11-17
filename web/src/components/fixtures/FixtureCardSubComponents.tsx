'use client';

import React from 'react';
import { EyeOff } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';

export function getScorers(events: MatchRecord['events'], side: 'home' | 'away'): string {
  if (!events || events.length === 0) return '';
  const goalEvents = events.filter((e) => {
    if (e.side !== side) return false;
    const type = (e.type || e.type_text || '').toLowerCase();
    return e.scoring_play === true || type.includes('goal');
  });
  return goalEvents.map((e) => `${e.player}${e.clock ? ` ${e.clock}` : ''}`).join(', ');
}

export function getKeyEvent(match: MatchRecord, isSpoiler: boolean): string {
  if (isSpoiler) return 'Spoiler Protected • Result hidden';
  if (match.events && match.events.length > 0) {
    const last = match.events[match.events.length - 1];
    return `${last.clock ? `${last.clock} ` : ''}${last.text || last.type_text || last.type}`;
  }
  if (match.status === 'FINISHED') return 'Final whistle • Match concluded';
  if (match.status === 'IN_PLAY' || match.status === 'LIVE') return 'Live match telemetry active';
  return `Official fixture • Kickoff ${new Date(match.utc_date).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}`;
}

export function FixtureStatusBlock({
  match,
  isLive,
  isPaused,
  isFinished,
  matchTime,
}: {
  match: MatchRecord;
  isLive: boolean;
  isPaused: boolean;
  isFinished: boolean;
  matchTime: string;
}) {
  return (
    <div className="flex items-center gap-3 shrink-0">
      <div className="flex flex-col items-center justify-center bg-[#0D0E11] px-3.5 py-2 rounded-lg min-w-[88px] border border-white/5">
        {isLive ? (
          <>
            <div className="flex items-center gap-1.5 text-red-400 font-tech text-xs uppercase tracking-wider font-bold">
              <span className="w-2 h-2 rounded-full bg-red-500 animate-ping" />
              <span>{isPaused ? 'HT' : match.display_clock || 'LIVE'}</span>
            </div>
            <span className="font-tech text-[10px] text-white/50 uppercase mt-0.5">
              {isPaused ? 'Half Time' : '2nd Half'}
            </span>
          </>
        ) : isFinished ? (
          <>
            <span className="font-scoreboard text-lg text-white uppercase tracking-wider leading-none">
              FT
            </span>
            <span className="font-tech text-[10px] text-white/50 uppercase mt-1">
              Full Time
            </span>
          </>
        ) : (
          <>
            <span className="font-tech text-sm font-bold text-[#DFFF00] uppercase tracking-wider leading-none">
              {matchTime}
            </span>
            <span className="font-tech text-[10px] text-white/50 uppercase mt-1">
              Kickoff
            </span>
          </>
        )}
      </div>

      <div className="hidden sm:flex flex-col min-w-0">
        <span className="font-tech text-xs font-bold uppercase tracking-widest text-[#DFFF00] truncate">
          {match.competition_name || 'Official Match'}
        </span>
        <span className="font-tech text-[11px] text-white/50 truncate">
          {match.stage || match.round_name || (isFinished ? 'Final Whistle' : 'Matchday')}
        </span>
      </div>
    </div>
  );
}

export function FixtureScoreboard({
  match,
  homeName,
  awayName,
  homeScorers,
  awayScorers,
  shouldHideScore,
  isLive,
  isFinished,
}: {
  match: MatchRecord;
  homeName: string;
  awayName: string;
  homeScorers: string;
  awayScorers: string;
  shouldHideScore: boolean;
  isLive: boolean;
  isFinished: boolean;
}) {
  return (
    <div className="grid grid-cols-12 items-center flex-1 max-w-2xl xl:max-w-3xl mx-auto w-full gap-2 sm:gap-3">
      {/* Home Team */}
      <div className="col-span-5 flex items-center justify-end gap-2.5 sm:gap-3 text-right min-w-0">
        <div className="flex flex-col min-w-0">
          <span className="font-scoreboard text-base sm:text-lg text-white truncate group-hover:text-[#DFFF00] transition-colors">
            {homeName}
          </span>
          {!shouldHideScore && homeScorers ? (
            <span className="font-tech text-[11px] text-white/50 truncate">
              {homeScorers}
            </span>
          ) : (
            <span className="font-tech text-[11px] text-white/40 truncate hidden sm:block">
              Home
            </span>
          )}
        </div>
        <GlassLogoBadge
          src={match.home_team_crest}
          alt={homeName}
          size={40}
          className="shrink-0"
          imageClassName="object-contain p-1"
        />
      </div>

      {/* Center Score Block */}
      <div className="col-span-2 flex flex-col items-center justify-center">
        {shouldHideScore ? (
          <div className="flex items-center justify-center gap-1.5 bg-[#0D0E11] px-3 py-1.5 rounded-lg border border-white/10 text-xs font-tech font-bold text-white/60">
            <EyeOff className="w-3.5 h-3.5 text-[#DFFF00]" />
            <span>? - ?</span>
          </div>
        ) : (
          <div className="flex items-center justify-center gap-2 bg-[#0D0E11] px-3 py-1 rounded-lg border border-white/10 min-w-[68px] sm:min-w-[76px]">
            {isFinished || isLive ? (
              <>
                <span className="font-scoreboard text-xl sm:text-2xl text-white">
                  {match.home_score ?? 0}
                </span>
                <span className="font-scoreboard text-lg text-white/40">-</span>
                <span className="font-scoreboard text-xl sm:text-2xl text-white">
                  {match.away_score ?? 0}
                </span>
              </>
            ) : (
              <span className="font-tech text-xs font-bold text-white/60 tracking-wider">
                VS
              </span>
            )}
          </div>
        )}
        <span className="font-tech text-[10px] text-white/40 uppercase mt-1 tracking-wider">
          {shouldHideScore ? 'Protected' : isLive ? 'Live' : isFinished ? 'Finished' : 'Pre-Match'}
        </span>
      </div>

      {/* Away Team */}
      <div className="col-span-5 flex items-center justify-start gap-2.5 sm:gap-3 text-left min-w-0">
        <GlassLogoBadge
          src={match.away_team_crest}
          alt={awayName}
          size={40}
          className="shrink-0"
          imageClassName="object-contain p-1"
        />
        <div className="flex flex-col min-w-0">
          <span className="font-scoreboard text-base sm:text-lg text-white truncate group-hover:text-[#DFFF00] transition-colors">
            {awayName}
          </span>
          {!shouldHideScore && awayScorers ? (
            <span className="font-tech text-[11px] text-white/50 truncate">
              {awayScorers}
            </span>
          ) : (
            <span className="font-tech text-[11px] text-white/40 truncate hidden sm:block">
              Away
            </span>
          )}
        </div>
      </div>
    </div>
  );
}

export function FixtureSubBar({
  keyEventText,
  hasPossession,
  homePoss,
  awayPoss,
  isLive,
  isFinished,
}: {
  keyEventText: string;
  hasPossession: boolean;
  homePoss: number;
  awayPoss: number;
  isLive: boolean;
  isFinished: boolean;
}) {
  return (
    <div className="mt-1 pt-2 border-t border-white/5 flex flex-col sm:flex-row items-center justify-between gap-2 bg-[#0D0E11]/60 px-3 py-1.5 rounded-lg text-xs font-tech">
      <div className="flex items-center gap-2 w-full sm:w-auto truncate">
        <span className="text-[#DFFF00] font-bold uppercase tracking-wider text-[10px]">
          {isLive ? 'Key Event:' : isFinished ? 'Match Recap:' : 'Preview:'}
        </span>
        <span className="text-white/70 truncate">{keyEventText}</span>
      </div>

      {hasPossession && (
        <div className="flex items-center gap-2 shrink-0 w-full sm:w-auto justify-end text-[11px] text-white/50">
          <span className="font-bold text-white/70">{homePoss}%</span>
          <div className="w-20 h-1.5 bg-white/10 rounded-full overflow-hidden flex">
            <div className="bg-[#DFFF00] h-full" style={{ width: `${homePoss}%` }} />
            <div className="bg-sky-400 h-full" style={{ width: `${awayPoss}%` }} />
          </div>
          <span className="font-bold text-white/70">{awayPoss}%</span>
        </div>
      )}
    </div>
  );
}
