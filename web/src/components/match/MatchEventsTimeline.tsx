'use client';

import React from 'react';
import { MatchEvent } from '@/lib/database';
import { ArrowRightLeft, ShieldAlert } from 'lucide-react';

interface MatchEventsTimelineProps {
  events?: MatchEvent[] | null;
  homeName: string;
  awayName: string;
  isSpoilerFree?: boolean;
  isRevealed?: boolean;
  onReveal?: () => void;
}

export default function MatchEventsTimeline({
  events,
  homeName,
  awayName,
  isSpoilerFree = false,
  isRevealed = false,
  onReveal,
}: MatchEventsTimelineProps) {
  if (!events || events.length === 0) {
    return (
      <div className="py-8 text-center text-xs text-[#94A3B8]">
        No match events recorded yet for this fixture.
      </div>
    );
  }

  // Filter out noisy delay events, retain goals, cards, and substitutions
  const filteredEvents = events.filter((e) => {
    const t = (e.type || '').toLowerCase();
    const text = (e.text || '').toLowerCase();
    return (
      t.includes('goal') ||
      t.includes('card') ||
      t.includes('sub') ||
      t.includes('penalty') ||
      text.includes('goal') ||
      text.includes('card')
    );
  });

  if (filteredEvents.length === 0) {
    return (
      <div className="py-8 text-center text-xs text-[#94A3B8]">
        No key goal or card events recorded.
      </div>
    );
  }

  if (isSpoilerFree && !isRevealed) {
    return (
      <div
        onClick={onReveal}
        className="flex items-center justify-between p-4 bg-[#181C20] border border-[#7098F8]/40 hover:border-[#7098F8] rounded-2xl cursor-pointer transition-all group"
      >
        <div className="flex items-center gap-3">
          <ShieldAlert className="w-5 h-5 text-[#4FC3F7] group-hover:scale-110 transition-transform" />
          <div>
            <div className="text-xs font-bold text-white">Match Events &amp; Scorers</div>
            <div className="text-[11px] text-[#94A3B8]">Spoiler Protected • Tap to reveal timeline</div>
          </div>
        </div>
        <span className="px-3 py-1 bg-[#7098F8]/10 text-[#7098F8] text-xs font-bold rounded-lg">
          Reveal ▾
        </span>
      </div>
    );
  }

  return (
    <div className="space-y-3 py-2">
      <div className="text-xs font-extrabold uppercase tracking-wider text-[#94A3B8] px-1">
        Match Timeline &amp; Key Moments
      </div>
      <div className="relative pl-6 border-l border-[#334155] space-y-4">
        {filteredEvents.map((e, idx) => {
          const isGoal = e.scoring_play || e.type.includes('goal') || e.type_text.toLowerCase().includes('goal');
          const isRed = e.type.includes('red-card');
          const isYellow = e.type.includes('yellow-card') && !isRed;
          const isSub = e.type.includes('sub');
          const isHome = e.side === 'home';

          return (
            <div key={e.id || idx} className="relative flex items-start gap-3">
              {/* Timeline Node Badge */}
              <div
                className={`absolute -left-[31px] top-0.5 flex items-center justify-center w-5 h-5 rounded-full border text-[10px] font-black ${
                  isGoal
                    ? 'bg-[#10B981] border-[#34D399] text-white shadow-[0_0_8px_rgba(16,185,129,0.5)]'
                    : isRed
                    ? 'bg-[#EF4444] border-[#F87171] text-white'
                    : isYellow
                    ? 'bg-[#F59E0B] border-[#FCD34D] text-black'
                    : 'bg-[#1E293B] border-[#475569] text-[#94A3B8]'
                }`}
              >
                {isGoal ? '⚽' : isRed ? '🟥' : isYellow ? '🟨' : '🔄'}
              </div>

              {/* Event Content */}
              <div className="flex-1 bg-[#181C20]/80 border border-[#334155]/60 hover:border-[#7098F8]/40 rounded-xl p-2.5 transition-colors">
                <div className="flex items-center justify-between gap-2 text-xs">
                  <div className="flex items-center gap-1.5 font-bold text-white">
                    <span>{e.player || 'Player'}</span>
                    {isGoal && (
                      <span className="text-[10px] px-1.5 py-0.5 rounded bg-[#10B981]/20 text-[#10B981] font-black">
                        GOAL
                      </span>
                    )}
                  </div>
                  <span className="font-mono text-xs font-black text-[#7098F8] shrink-0">
                    {e.clock || `${idx + 1}'`}
                  </span>
                </div>

                {e.assist && (
                  <div className="text-[11px] text-[#94A3B8] mt-0.5">
                    Assist: <span className="text-white/80 font-medium">{e.assist}</span>
                  </div>
                )}

                <div className="flex items-center justify-between text-[10px] text-[#64748B] mt-1 pt-1 border-t border-[#334155]/40">
                  <span className="font-semibold text-[#94A3B8]">
                    {isHome ? homeName : awayName}
                  </span>
                  <span>{e.type_text || (isGoal ? 'Goal' : isYellow ? 'Yellow Card' : isRed ? 'Red Card' : 'Event')}</span>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
