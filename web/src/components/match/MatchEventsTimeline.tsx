'use client';

import React from 'react';
import { MatchEvent } from '@/lib/database';
import { ShieldAlert, ArrowLeftRight } from 'lucide-react';
import { GoalBallIcon, extractScorerDisplayName } from './MatchScrubberStrip';

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
      e.scoring_play ||
      ((t.includes('goal') || text.startsWith('goal!')) && !t.includes('kick')) ||
      t.includes('card') ||
      t.includes('sub')
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
            <div className="text-xs font-tech font-bold text-white uppercase tracking-wider">
              Match Events &amp; Scorers
            </div>
            <div className="text-[11px] font-tech text-white/50">
              Spoiler Protected • Tap to reveal timeline
            </div>
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
          const tLower = (e.type || e.type_text || '').toLowerCase();
          const isGoal =
            e.scoring_play ||
            (tLower.includes('goal') && !tLower.includes('kick') && !tLower.includes('attempt'));
          const isRed = tLower.includes('red');
          const isYellow = tLower.includes('yellow') && !isRed;
          const isSub = tLower.includes('sub');
          const isHome = e.side !== 'away';
          const playerName = extractScorerDisplayName(e);

          return (
            <div key={e.id || idx} className="relative flex items-start gap-3">
              <div
                className={`absolute -left-[31px] top-0.5 flex items-center justify-center w-5 h-5 rounded-full border ${
                  isGoal
                    ? 'bg-[#1A1C22] border-[#DFFF00]/60 shadow-[0_0_8px_rgba(223,255,0,0.35)]'
                    : isRed
                    ? 'bg-rose-500/20 border-rose-500/60'
                    : isYellow
                    ? 'bg-amber-400/20 border-amber-400/60'
                    : 'bg-[#1A1C22] border-white/20'
                }`}
              >
                {isGoal ? (
                  <GoalBallIcon className="w-3 h-3 text-[#DFFF00]" />
                ) : isRed ? (
                  <span className="w-2 h-2.5 rounded-[1px] bg-rose-500" />
                ) : isYellow ? (
                  <span className="w-2 h-2.5 rounded-[1px] bg-amber-400" />
                ) : (
                  <ArrowLeftRight className="w-2.5 h-2.5 text-[#38BDF8]" />
                )}
              </div>

              <div className="flex-1 bg-[#121316] border border-white/10 hover:border-white/20 rounded-xl p-3 transition-colors shadow-sm">
                {isSub ? (
                  <div className="space-y-1">
                    <div className="flex items-center justify-between gap-2 text-xs sm:text-sm">
                      <div className="flex items-center gap-1.5 font-bold text-white">
                        <span className="text-xs px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 font-mono font-black">
                          IN
                        </span>
                        <span className="font-tech text-xs sm:text-sm font-extrabold">{playerName}</span>
                      </div>
                      <span className="font-mono text-xs sm:text-sm font-black text-[#DFFF00] shrink-0">
                        {e.clock || `${idx + 1}'`}
                      </span>
                    </div>
                    {e.assist && (
                      <div className="flex items-center gap-1.5 text-xs text-white/75 font-tech">
                        <span className="text-xs px-2 py-0.5 rounded bg-amber-500/20 text-amber-400 font-mono font-black">
                          OUT
                        </span>
                        <span className="text-white/80 font-semibold">{e.assist}</span>
                      </div>
                    )}
                  </div>
                ) : (
                  <>
                    <div className="flex items-center justify-between gap-2 text-xs sm:text-sm">
                      <div className="flex items-center gap-1.5 font-bold text-white">
                        <span className="font-tech text-xs sm:text-sm font-extrabold">{playerName}</span>
                        {isGoal && (
                          <span className="text-xs px-2 py-0.5 rounded bg-[#DFFF00]/20 text-[#DFFF00] font-mono font-black">
                            GOAL
                          </span>
                        )}
                      </div>
                      <span className="font-mono text-xs sm:text-sm font-black text-[#DFFF00] shrink-0">
                        {e.clock || `${idx + 1}'`}
                      </span>
                    </div>

                    {isGoal && e.assist && (
                      <div className="text-xs text-white/75 font-tech mt-1">
                        Assist: <span className="text-white/90 font-bold">{e.assist}</span>
                      </div>
                    )}
                  </>
                )}

                <div className="flex items-center justify-between text-xs text-white/70 mt-2 pt-2 border-t border-white/10 font-tech">
                  <span className="font-bold text-white/90">
                    {isHome ? homeName : awayName}
                  </span>
                  <span className="font-semibold text-white/75">
                    {e.type_text ||
                      (isGoal ? 'Goal' : isYellow ? 'Yellow Card' : isRed ? 'Red Card' : 'Event')}
                  </span>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
