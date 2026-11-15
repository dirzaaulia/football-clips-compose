'use client';

import React from 'react';
import { MatchEvent } from '@/lib/database';
import { ShieldAlert } from 'lucide-react';

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
      <div className="py-8 text-center text-xs text-white/50 font-tech">
        No match events recorded yet for this fixture.
      </div>
    );
  }

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
      <div className="py-8 text-center text-xs text-white/50 font-tech">
        No key goal or card events recorded.
      </div>
    );
  }

  if (isSpoilerFree && !isRevealed) {
    return (
      <div
        onClick={onReveal}
        className="flex items-center justify-between p-4 bg-[#121316] border border-[#DFFF00]/30 hover:border-[#DFFF00] rounded-2xl cursor-pointer transition-all group select-none shadow-lg"
      >
        <div className="flex items-center gap-3">
          <ShieldAlert className="w-5 h-5 text-[#DFFF00] group-hover:scale-110 transition-transform" />
          <div>
            <div className="text-xs font-tech font-bold text-white uppercase tracking-wider">Match Events &amp; Scorers</div>
            <div className="text-[11px] font-tech text-white/50">Spoiler Protected • Tap to reveal timeline</div>
          </div>
        </div>
        <span className="px-3.5 py-1.5 bg-[#DFFF00] text-[#0A0A0C] text-xs font-tech font-black uppercase tracking-wider rounded-xl shadow-md">
          Reveal ▾
        </span>
      </div>
    );
  }

  return (
    <div className="space-y-3 py-2 select-none">
      <div className="text-xs font-tech font-black uppercase tracking-wider text-white/60 px-1">
        Match Timeline &amp; Key Moments
      </div>
      <div className="relative pl-6 border-l border-white/10 space-y-4">
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
                    ? 'bg-emerald-500 border-emerald-400 text-white shadow-[0_0_8px_rgba(16,185,129,0.5)]'
                    : isRed
                    ? 'bg-rose-500 border-rose-400 text-white'
                    : isYellow
                    ? 'bg-amber-400 border-amber-300 text-black'
                    : 'bg-[#1A1C22] border-white/20 text-white/70'
                }`}
              >
                {isGoal ? '⚽' : isRed ? '🟥' : isYellow ? '🟨' : '🔄'}
              </div>

              {/* Event Content */}
              <div className="flex-1 bg-[#121316] border border-white/10 hover:border-white/20 rounded-xl p-3 transition-colors shadow-sm">
                {isSub ? (
                  <div className="space-y-1">
                    <div className="flex items-center justify-between gap-2 text-xs">
                      <div className="flex items-center gap-1.5 font-bold text-white">
                        <span className="text-[10px] px-1.5 py-0.5 rounded bg-emerald-500/20 text-emerald-400 font-mono font-black">IN</span>
                        <span className="font-tech">{e.player || 'Player'}</span>
                      </div>
                      <span className="font-mono text-xs font-black text-[#DFFF00] shrink-0">
                        {e.clock || `${idx + 1}'`}
                      </span>
                    </div>
                    {e.assist && (
                      <div className="flex items-center gap-1.5 text-xs text-white/50 font-tech">
                        <span className="text-[10px] px-1.5 py-0.5 rounded bg-amber-500/20 text-amber-400 font-mono font-black">OUT</span>
                        <span>{e.assist}</span>
                      </div>
                    )}
                  </div>
                ) : (
                  <>
                    <div className="flex items-center justify-between gap-2 text-xs">
                      <div className="flex items-center gap-1.5 font-bold text-white">
                        <span className="font-tech">{e.player || 'Player'}</span>
                        {isGoal && (
                          <span className="text-[10px] px-1.5 py-0.5 rounded bg-emerald-500/20 text-emerald-400 font-mono font-black">
                            GOAL
                          </span>
                        )}
                      </div>
                      <span className="font-mono text-xs font-black text-[#DFFF00] shrink-0">
                        {e.clock || `${idx + 1}'`}
                      </span>
                    </div>

                    {isGoal && e.assist && (
                      <div className="text-[11px] text-white/50 font-tech mt-0.5">
                        Assist: <span className="text-white/80 font-medium">{e.assist}</span>
                      </div>
                    )}
                  </>
                )}

                <div className="flex items-center justify-between text-[10px] text-white/40 mt-1.5 pt-1.5 border-t border-white/5 font-tech">
                  <span className="font-semibold text-white/70">
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
