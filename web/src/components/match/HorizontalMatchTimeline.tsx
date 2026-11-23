'use client';

import React, { useState } from 'react';
import { MatchEvent } from '@/lib/database';
import { ShieldAlert, Users, ArrowLeftRight } from 'lucide-react';
import { GoalBallIcon, extractScorerDisplayName } from './MatchScrubberStrip';

interface HorizontalMatchTimelineProps {
  events?: MatchEvent[] | null;
  homeName: string;
  awayName: string;
  homeColor?: string | null;
  awayColor?: string | null;
  isSpoilerFree?: boolean;
  isLive?: boolean;
}

export default function HorizontalMatchTimeline({
  events,
  homeName,
  awayName,
  homeColor,
  awayColor,
  isSpoilerFree = false,
  isLive = false,
}: HorizontalMatchTimelineProps) {
  const [isRevealed, setIsRevealed] = useState(!isSpoilerFree || isLive);
  const [hoveredEvent, setHoveredEvent] = useState<MatchEvent | null>(null);

  const effectiveHomeColor = homeColor || '#DFFF00';
  const effectiveAwayColor = awayColor || '#38BDF8';

  if (!events || events.length === 0) {
    return (
      <div className="py-6 text-center text-xs font-tech text-white/50 bg-[#121316] rounded-2xl border border-white/10">
        No match events recorded yet for this fixture.
      </div>
    );
  }

  const filteredEvents = events.filter((e) => {
    const t = (e.type || '').toLowerCase();
    const text = (e.text || '').toLowerCase();
    return (
      e.scoring_play ||
      ((t.includes('goal') || text.startsWith('goal!')) && !t.includes('kick')) ||
      t.includes('card') ||
      t.includes('sub')
    );
  });

  if (filteredEvents.length === 0) {
    return (
      <div className="py-6 text-center text-xs font-tech text-white/50 bg-[#121316] rounded-2xl border border-white/10">
        No goals, cards, or key events recorded.
      </div>
    );
  }

  if (isSpoilerFree && !isLive && !isRevealed) {
    return (
      <div
        onClick={() => setIsRevealed(true)}
        className="p-5 bg-[#121316] border border-[#DFFF00]/30 hover:border-[#DFFF00] rounded-2xl cursor-pointer transition-all flex items-center justify-between group shadow-lg select-none"
      >
        <div className="flex items-center gap-3">
          <ShieldAlert className="w-5 h-5 text-[#DFFF00] group-hover:scale-110 transition-transform" />
          <div>
            <div className="text-xs font-tech font-bold text-white uppercase tracking-wider">
              Interactive Match Timeline Protected
            </div>
            <div className="text-[11px] font-tech text-white/50">
              Tap to reveal chronological goals, cards and events
            </div>
          </div>
        </div>
        <span className="px-4 py-2 bg-[#DFFF00] hover:bg-[#cbe800] text-[#0A0A0C] font-tech text-xs font-black uppercase tracking-wider rounded-xl shadow-md">
          Reveal Timeline ▾
        </span>
      </div>
    );
  }

  const parseMinute = (clock?: string): number => {
    if (!clock) return 45;
    const clean = clock.replace(/'/g, '').trim();
    if (clean.includes('+')) {
      const parts = clean.split('+');
      return (parseInt(parts[0], 10) || 45) + (parseInt(parts[1], 10) || 1);
    }
    return Math.min(Math.max(parseInt(clean, 10) || 1, 1), 120);
  };

  const renderBadgeIcon = (e: MatchEvent) => {
    const t = (e.type || '').toLowerCase();
    const txt = (e.text || '').toLowerCase();
    if (e.scoring_play || ((t.includes('goal') || txt.startsWith('goal!')) && !t.includes('kick'))) {
      return {
        node: <GoalBallIcon className="w-3 h-3 text-[#DFFF00]" />,
        color: 'bg-[#DFFF00]/15 text-[#DFFF00] border-[#DFFF00]/50',
        label: 'Goal',
      };
    }
    if (t.includes('red') || txt.includes('red card')) {
      return {
        node: <span className="w-2 h-2.5 rounded-[1px] bg-rose-500 inline-block" />,
        color: 'bg-rose-500/20 text-rose-200 border-rose-500/50',
        label: 'Red Card',
      };
    }
    if (t.includes('yellow') || txt.includes('yellow card')) {
      return {
        node: <span className="w-2 h-2.5 rounded-[1px] bg-amber-400 inline-block" />,
        color: 'bg-amber-400/15 text-amber-200 border-amber-400/50',
        label: 'Yellow Card',
      };
    }
    return {
      node: <ArrowLeftRight className="w-3 h-3 text-[#38BDF8]" />,
      color: 'bg-sky-500/15 text-sky-200 border-sky-400/40',
      label: 'Substitution',
    };
  };

  return (
    <div className="p-4 sm:p-5 bg-[#121316] rounded-2xl border border-white/10 space-y-4 sm:space-y-6 shadow-xl overflow-hidden select-none">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between text-xs pb-3 border-b border-white/10 gap-2">
        <div className="flex items-center gap-2">
          <span className="font-scoreboard text-sm uppercase tracking-wider text-white">
            Match Timeline
          </span>
        </div>
        <div className="flex items-center gap-3 sm:gap-4 text-[11px] text-white/60 font-tech">
          <span className="flex items-center gap-1.5">
            <span className="w-2 h-2 rounded-full" style={{ backgroundColor: effectiveHomeColor }} />
            <span>{homeName} (Top)</span>
          </span>
          <span className="flex items-center gap-1.5">
            <span className="w-2 h-2 rounded-full" style={{ backgroundColor: effectiveAwayColor }} />
            <span>{awayName} (Bottom)</span>
          </span>
        </div>
      </div>

      <div className="overflow-x-auto scrollbar-none py-1 -mx-2 px-2 sm:mx-0 sm:px-0">
        <div className="relative min-w-[540px] sm:min-w-full pt-12 pb-14 px-6 sm:px-8">
          <div className="relative h-2 w-full bg-[#1A1C22] rounded-full overflow-visible border border-white/10">
            <div className="absolute left-[50%] -top-2 bottom-0 w-0.5 h-6 bg-white/20 -translate-x-1/2">
              <span className="absolute -bottom-5 -translate-x-1/2 text-xs font-mono font-bold text-white/80">
                HT
              </span>
            </div>

            <span className="absolute -left-2 -bottom-5 text-xs font-mono font-bold text-white/70">
              0&apos;
            </span>
            <span className="absolute -right-2 -bottom-5 text-xs font-mono font-bold text-white/70">
              90&apos;+
            </span>

            {filteredEvents.map((e, idx) => {
              const minute = parseMinute(e.clock);
              const pct = Math.min(Math.max((minute / 95) * 100, 3), 97);
              const isHome = e.side !== 'away';
              const badge = renderBadgeIcon(e);

              return (
                <div
                  key={e.id || idx}
                  style={{ left: `${pct}%` }}
                  onClick={() => setHoveredEvent(e)}
                  onMouseEnter={() => setHoveredEvent(e)}
                  onMouseLeave={() => setHoveredEvent(null)}
                  className={`absolute -translate-x-1/2 cursor-pointer z-10 transition-transform hover:scale-110 ${
                    isHome ? '-top-10' : 'top-4'
                  }`}
                >
                  <div
                    className={`flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-tech font-black shadow-md border ${badge.color}`}
                  >
                    {badge.node}
                    <span>{e.clock || `${minute}'`}</span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      <div className="min-h-[44px] px-4 py-2.5 rounded-xl bg-[#0A0A0C] border border-white/10 flex flex-col sm:flex-row sm:items-center justify-between text-xs sm:text-sm gap-2">
        {hoveredEvent ? (
          <div className="flex items-center gap-3 min-w-0">
            {renderBadgeIcon(hoveredEvent).node}
            <div className="min-w-0 font-tech">
              <span className="font-extrabold text-white mr-2 text-xs sm:text-sm">
                {extractScorerDisplayName(hoveredEvent)}
              </span>
              <span className="text-white/80 text-xs block sm:inline">
                {hoveredEvent.assist ? ` (Assist: ${hoveredEvent.assist})` : ''}
              </span>
            </div>
          </div>
        ) : (
          <div className="flex items-center gap-2 text-white/75 text-xs font-tech">
            <Users className="w-4 h-4 text-[#DFFF00] shrink-0" />
            <span>Tap or hover any event badge on the timeline to inspect player details</span>
          </div>
        )}
        {hoveredEvent && (
          <span className="text-xs font-tech font-black text-[#DFFF00] uppercase tracking-wider self-start sm:self-auto shrink-0">
            {renderBadgeIcon(hoveredEvent).label}
          </span>
        )}
      </div>
    </div>
  );
}
