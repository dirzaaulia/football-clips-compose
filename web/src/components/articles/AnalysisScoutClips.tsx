'use client';

import React, { useState, useMemo } from 'react';
import { Film, Play, ShieldAlert, ArrowLeftRight, Crosshair, Award } from 'lucide-react';
import { MatchRecord, MatchEvent } from '@/lib/database';
import { useVideoPlayer } from '@/hooks/useVideoPlayer';

interface AnalysisScoutClipsProps {
  match: MatchRecord;
}

type EventFilter = 'ALL' | 'GOALS' | 'SHOTS' | 'CARDS' | 'SUBS';

function getEventBadge(ev: MatchEvent) {
  const type = (ev.type || ev.type_text || '').toLowerCase();
  if (ev.scoring_play || type.includes('goal')) {
    return {
      icon: Award,
      color: 'text-[#DFFF00] bg-[#DFFF00]/15 border-[#DFFF00]/40',
      label: 'GOAL',
    };
  }
  if (type.includes('card') || type.includes('yellow') || type.includes('red')) {
    const isRed = type.includes('red');
    return {
      icon: ShieldAlert,
      color: isRed
        ? 'text-rose-400 bg-rose-500/15 border-rose-500/40'
        : 'text-amber-400 bg-amber-500/15 border-amber-500/40',
      label: isRed ? 'RED CARD' : 'YELLOW',
    };
  }
  if (type.includes('sub')) {
    return {
      icon: ArrowLeftRight,
      color: 'text-sky-400 bg-sky-500/15 border-sky-500/40',
      label: 'SUB',
    };
  }
  return {
    icon: Crosshair,
    color: 'text-white/60 bg-white/10 border-white/20',
    label: 'ATTEMPT',
  };
}

export default function AnalysisScoutClips({ match }: AnalysisScoutClipsProps) {
  const { playVideo } = useVideoPlayer();
  const [filter, setFilter] = useState<EventFilter>('ALL');
  const hasHighlight = Boolean(match.highlight_video_id);

  const filteredEvents = useMemo(() => {
    const all = match.events || [];
    if (filter === 'ALL') return all.slice(0, 8);
    return all
      .filter((ev) => {
        const t = (ev.type || ev.type_text || '').toLowerCase();
        if (filter === 'GOALS') return ev.scoring_play || t.includes('goal');
        if (filter === 'SHOTS') return t.includes('shot');
        if (filter === 'CARDS') return t.includes('card') || t.includes('yellow') || t.includes('red');
        if (filter === 'SUBS') return t.includes('sub');
        return true;
      })
      .slice(0, 8);
  }, [match.events, filter]);

  return (
    <div className="w-full bg-[#1A1C22] rounded-2xl border border-white/10 p-5 shadow-xl flex flex-col gap-4">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <Film className="w-4 h-4 text-[#DFFF00]" />
          <h3 className="font-scoreboard text-lg sm:text-xl text-white uppercase tracking-wider">
            Verified Tactical Incidents &amp; Match Clips
          </h3>
        </div>
        {hasHighlight && (
          <button
            type="button"
            onClick={() => playVideo(match)}
            className="self-start sm:self-auto px-3 py-1.5 rounded-lg bg-[#DFFF00] hover:bg-[#c8e600] text-[#0A0A0C] font-tech text-xs font-bold uppercase tracking-wider flex items-center gap-1.5 transition-all shadow-[0_0_12px_rgba(223,255,0,0.25)] cursor-pointer"
          >
            <Play className="w-3.5 h-3.5 fill-[#0A0A0C]" />
            <span>Play Match Highlight</span>
          </button>
        )}
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center gap-1.5 overflow-x-auto pb-1 scrollbar-none font-tech text-xs">
        {(['ALL', 'GOALS', 'SHOTS', 'CARDS', 'SUBS'] as EventFilter[]).map((tab) => (
          <button
            key={tab}
            type="button"
            onClick={() => setFilter(tab)}
            className={`px-2.5 py-1 rounded-md uppercase font-bold transition-colors cursor-pointer ${
              filter === tab
                ? 'bg-[#DFFF00] text-[#0A0A0C]'
                : 'bg-[#121316] text-white/60 hover:text-white border border-white/5'
            }`}
          >
            {tab}
          </button>
        ))}
      </div>

      {/* Events List */}
      <div className="divide-y divide-white/5 min-h-[220px]">
        {filteredEvents.length > 0 ? (
          filteredEvents.map((ev) => {
            const badge = getEventBadge(ev);
            const BadgeIcon = badge.icon;
            return (
              <div
                key={ev.id || `${ev.clock}-${ev.player}`}
                className="py-3 flex flex-col sm:flex-row sm:items-center justify-between gap-3"
              >
                <div className="flex items-start gap-3 min-w-0">
                  <span className="px-2 py-1 rounded bg-[#0A0A0C] border border-white/10 text-[#DFFF00] font-tech text-xs font-bold shrink-0">
                    {ev.clock || "90'"}
                  </span>
                  <div className="flex flex-col min-w-0">
                    <div className="flex items-center gap-2">
                      <span className="font-tech text-sm font-bold text-white truncate">
                        {ev.player || 'Match Event'}
                      </span>
                      <span
                        className={`text-[9px] font-tech font-extrabold uppercase px-1.5 py-0.5 rounded border ${badge.color}`}
                      >
                        <span className="inline-flex items-center gap-1">
                          <BadgeIcon className="w-2.5 h-2.5" />
                          {badge.label}
                        </span>
                      </span>
                    </div>
                    <span className="text-xs text-white/60 leading-relaxed font-tech truncate">
                      {ev.text || ev.type_text || 'Key tactical phase recorded'}
                    </span>
                  </div>
                </div>

                <div className="flex items-center gap-2 shrink-0 self-end sm:self-auto font-tech">
                  <span className="text-[10px] uppercase text-white/40 bg-[#121316] px-2 py-1 rounded border border-white/5">
                    {ev.side ? `${ev.side.toUpperCase()} FLANK` : 'TRANSITION'}
                  </span>
                </div>
              </div>
            );
          })
        ) : (
          <div className="py-12 text-center font-tech text-xs text-white/50 space-y-1">
            <p>No incidents recorded matching the &apos;{filter}&apos; filter for this fixture.</p>
            {hasHighlight && <p className="text-[#DFFF00]/70">Full highlight video available above.</p>}
          </div>
        )}
      </div>
    </div>
  );
}
