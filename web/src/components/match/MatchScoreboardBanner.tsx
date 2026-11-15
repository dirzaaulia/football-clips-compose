'use client';

import React, { useState } from 'react';
import { Eye, EyeOff, Radio, ArrowLeftRight } from 'lucide-react';
import { MatchRecord, MatchEvent } from '@/lib/database';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';

function GoalBallIcon({ className = 'w-3 h-3' }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="10" className="stroke-[#DFFF00] fill-[#DFFF00]/20" />
      <polygon points="12 6 15 9 14 13 10 13 9 9 12 6" className="fill-[#DFFF00] stroke-none" />
      <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83" className="stroke-[#DFFF00]" />
    </svg>
  );
}

interface MatchScoreboardBannerProps {
  match: MatchRecord;
  homeName: string;
  awayName: string;
  isSpoilerFree: boolean;
  isScoreRevealed: boolean;
  onRevealScore: () => void;
  onHideScore: () => void;
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
  const [hoveredEvent, setHoveredEvent] = useState<MatchEvent | null>(null);

  const isFinished = match.status === 'FINISHED' || match.status === 'AWARDED';
  const isLive = ['IN_PLAY', 'PAUSED', 'LIVE'].includes(match.status);
  const isScheduled = ['TIMED', 'SCHEDULED'].includes(match.status);
  const isHidden = isSpoilerFree && !isScoreRevealed && isFinished;

  const homeEvents = (match.events || []).filter(
    (e) => (e.side === 'home' || (e.text || '').toLowerCase().includes(homeName.toLowerCase())) &&
      (e.type || '').toLowerCase().includes('goal')
  );

  const awayEvents = (match.events || []).filter(
    (e) => (e.side === 'away' || (e.text || '').toLowerCase().includes(awayName.toLowerCase())) &&
      (e.type || '').toLowerCase().includes('goal')
  );

  const keyEvents = (match.events || []).filter((e) => {
    const t = (e.type || '').toLowerCase();
    const txt = (e.text || '').toLowerCase();
    return t.includes('goal') || t.includes('card') || t.includes('sub') || txt.includes('goal');
  });

  return (
    <div className="w-full bg-[#121316] border border-white/10 rounded-2xl p-5 sm:p-6 shadow-xl flex flex-col gap-5 select-none relative overflow-hidden">
      {/* Background Subtle Ambience Glow */}
      <div className="absolute -top-12 -left-12 w-48 h-48 bg-[#DFFF00]/5 rounded-full blur-3xl pointer-events-none" />

      {/* Main Face-Off Scoreboard Grid */}
      <div className="grid grid-cols-12 items-center gap-3">
        {/* Home Team */}
        <div className="col-span-5 flex items-center gap-3 sm:gap-4 min-w-0">
          <GlassLogoBadge
            src={match.home_team_crest}
            alt={homeName}
            size={52}
            className="shrink-0"
            imageClassName="object-contain p-1.5 drop-shadow-md"
          />
          <div className="flex flex-col min-w-0">
            <div className="flex items-center gap-2">
              <span className="font-scoreboard text-base sm:text-2xl text-white tracking-wide truncate">
                {homeName}
              </span>
              <span className="px-1.5 py-0.5 rounded bg-white/5 text-[9px] font-bold text-white/60 uppercase">
                HOME
              </span>
            </div>
            {!isHidden && homeEvents.length > 0 ? (
              <div className="font-tech text-xs text-white/60 truncate mt-0.5 flex items-center gap-1.5 flex-wrap">
                {homeEvents.map((e, idx) => (
                  <span key={idx} className="inline-flex items-center gap-1">
                    <GoalBallIcon className="w-3 h-3 text-[#DFFF00]" />
                    <span>{e.player || e.text || 'Goal'} {e.clock && `(${e.clock})`}</span>
                    {idx < homeEvents.length - 1 && <span className="text-white/30">•</span>}
                  </span>
                ))}
              </div>
            ) : (
              <div className="font-tech text-xs text-white/40 truncate mt-0.5">
                {match.stage || match.round_name || 'Matchday'}
              </div>
            )}
          </div>
        </div>

        {/* Score Centerpiece */}
        <div className="col-span-2 flex flex-col items-center justify-center">
          {isScheduled && !isLive ? (
            <span className="px-3 py-1 rounded-xl bg-[#1A1C22] border border-white/10 text-xs font-tech font-black text-white/60 tracking-widest">
              VS
            </span>
          ) : (
            <div
              onClick={() => (isHidden ? onRevealScore() : onHideScore())}
              className={`flex flex-col items-center cursor-pointer transition-all ${
                isHidden ? 'filter blur-md hover:blur-none opacity-40' : ''
              }`}
              title={isHidden ? 'Click to reveal score' : 'Click to hide score'}
            >
              <div className="flex items-baseline gap-1 text-center font-scoreboard text-3xl sm:text-5xl text-white tracking-tight">
                <span>{match.home_score ?? 0}</span>
                <span className="text-[#DFFF00] opacity-80 mx-1">:</span>
                <span>{match.away_score ?? 0}</span>
              </div>
              <div className="flex items-center gap-1 mt-1">
                <span className="font-tech text-[10px] font-black uppercase tracking-wider px-2 py-0.5 rounded bg-[#1A1C22] text-[#DFFF00] border border-white/5">
                  {isLive ? `LIVE ${match.display_clock || ''}` : 'FULL TIME'}
                </span>
                {isHidden ? <Eye className="w-3 h-3 text-[#DFFF00]" /> : <EyeOff className="w-3 h-3 text-white/30" />}
              </div>
            </div>
          )}
        </div>

        {/* Away Team */}
        <div className="col-span-5 flex items-center justify-end gap-3 sm:gap-4 min-w-0 text-right">
          <div className="flex flex-col min-w-0 items-end">
            <div className="flex items-center gap-2">
              <span className="px-1.5 py-0.5 rounded bg-white/5 text-[9px] font-bold text-white/60 uppercase">
                AWAY
              </span>
              <span className="font-scoreboard text-base sm:text-2xl text-white tracking-wide truncate">
                {awayName}
              </span>
            </div>
            {!isHidden && awayEvents.length > 0 ? (
              <div className="font-tech text-xs text-white/60 truncate mt-0.5 flex items-center justify-end gap-1.5 flex-wrap">
                {awayEvents.map((e, idx) => (
                  <span key={idx} className="inline-flex items-center gap-1">
                    <GoalBallIcon className="w-3 h-3 text-[#DFFF00]" />
                    <span>{e.player || e.text || 'Goal'} {e.clock && `(${e.clock})`}</span>
                    {idx < awayEvents.length - 1 && <span className="text-white/30">•</span>}
                  </span>
                ))}
              </div>
            ) : (
              <div className="font-tech text-xs text-white/40 truncate mt-0.5">
                Official Broadcast
              </div>
            )}
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

      {/* Minute-by-Minute Visual Key Event Scrubber Strip */}
      {keyEvents.length > 0 && !isHidden && (
        <div className="pt-2 border-t border-white/5">
          <div className="flex items-center justify-between text-white/50 font-tech text-[10px] uppercase tracking-wider mb-2">
            <span>Match Timeline Scrubber</span>
            <span>90 Minutes Regulation</span>
          </div>

          <div className="relative w-full h-8 bg-[#0A0A0C] border border-white/10 rounded-lg flex items-center px-4 overflow-visible">
            <div className="absolute left-0 right-0 h-0.5 bg-white/10" />
            <div className="absolute left-1/2 top-0 bottom-0 w-0.5 bg-white/20 flex items-center justify-center">
              <span className="text-[8px] bg-[#121316] px-1 text-white/40 font-mono">HT</span>
            </div>

            {keyEvents.map((evt, idx) => {
              const minute = parseMinute(evt.clock);
              const percent = Math.min(Math.max((minute / 95) * 100, 4), 96);
              const isGoal = (evt.type || '').toLowerCase().includes('goal') || (evt.text || '').toLowerCase().includes('goal');
              const isCard = (evt.type || '').toLowerCase().includes('card');
              const isYellow = isCard && (evt.type || '').toLowerCase().includes('yellow');
              const isRed = isCard && (evt.type || '').toLowerCase().includes('red');

              return (
                <div
                  key={evt.id || idx}
                  style={{ left: `${percent}%` }}
                  className="absolute -translate-x-1/2 flex flex-col items-center cursor-pointer group hover:scale-125 transition-transform z-10"
                  onMouseEnter={() => setHoveredEvent(evt)}
                  onMouseLeave={() => setHoveredEvent(null)}
                >
                  {isGoal ? (
                    <GoalBallIcon className="w-3.5 h-3.5 text-[#DFFF00] drop-shadow-[0_0_6px_rgba(223,255,0,0.6)]" />
                  ) : isRed ? (
                    <span className="w-2.5 h-3.5 rounded-[1px] bg-rose-500 shadow-sm" />
                  ) : isYellow ? (
                    <span className="w-2.5 h-3.5 rounded-[1px] bg-amber-400 shadow-sm" />
                  ) : (
                    <ArrowLeftRight className="w-3 h-3 text-[#38BDF8]" />
                  )}
                  <span className="text-[8px] font-mono text-white/70 mt-0.5">{evt.clock || `${minute}'`}</span>

                  {/* Tooltip on Hover */}
                  <div className="absolute -top-7 left-1/2 -translate-x-1/2 hidden group-hover:flex px-2 py-0.5 rounded bg-[#1A1C22] border border-white/15 text-[9px] font-tech text-white whitespace-nowrap shadow-xl z-30">
                    {evt.clock ? `${evt.clock} ` : ''}{evt.player || evt.text || evt.type}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}

function parseMinute(clock?: string): number {
  if (!clock) return 45;
  const clean = clock.replace(/'/g, '').trim();
  if (clean.includes('+')) {
    const parts = clean.split('+');
    return (parseInt(parts[0], 10) || 45) + (parseInt(parts[1], 10) || 1);
  }
  return Math.min(Math.max(parseInt(clean, 10) || 1, 1), 120);
}
