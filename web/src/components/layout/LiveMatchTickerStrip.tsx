'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { EyeOff, Eye, CheckCircle2 } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';

interface LiveMatchTickerStripProps {
  liveMatches: MatchRecord[];
  onPlayMatch?: (match: MatchRecord) => void;
}

export default function LiveMatchTickerStrip({
  liveMatches,
  onPlayMatch,
}: LiveMatchTickerStripProps) {
  const { isSpoilerFree, toggleSpoilerFree } = useSpoilerFree();
  const [unblurredIds, setUnblurredIds] = useState<Set<string>>(new Set());

  const toggleUnblur = (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    setUnblurredIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const displayMatches = liveMatches.slice(0, 10);
  if (displayMatches.length === 0) return null;

  return (
    <section className="w-full bg-[#0A0A0C] border-b border-white/10 sticky top-12 z-40 shadow-xl select-none">
      <div className="w-full max-w-[1800px] mx-auto px-4 sm:px-6 lg:px-8 py-1.5 flex items-center justify-between gap-4 overflow-x-auto no-scrollbar">
        {/* Live Fixtures Strip */}
        <div className="flex items-center gap-2 shrink-0">
          {displayMatches.map((m) => {
            const isLive = m.status === 'IN_PLAY';
            const homeCode = (m.home_team_name || m.home_team).slice(0, 3).toUpperCase();
            const awayCode = (m.away_team_name || m.away_team).slice(0, 3).toUpperCase();
            const isUnblurred = unblurredIds.has(m.id);
            const shouldBlur = isSpoilerFree && !isUnblurred;
            const scoreText =
              m.home_score !== null && m.away_score !== null
                ? `${m.home_score} - ${m.away_score}`
                : 'VS';

            return (
              <div
                key={m.id}
                onClick={() => onPlayMatch?.(m)}
                className="flex items-center gap-1.5 px-2.5 py-1 rounded bg-[#121316] hover:bg-[#1A1C22] border border-white/5 hover:border-white/20 transition-colors cursor-pointer group shrink-0"
              >
                {/* Live Indicator or FT */}
                <span className="inline-flex items-center gap-1 font-tech text-[10px] font-black uppercase text-[#FF2E4D]">
                  {isLive ? (
                    <>
                      <span className="w-1.5 h-1.5 rounded-full bg-[#FF2E4D] animate-ping" />
                      <span>LIVE</span>
                    </>
                  ) : (
                    <span className="text-white/40">FT</span>
                  )}
                </span>

                <span className="font-tech text-xs font-bold text-white">
                  {homeCode}
                </span>

                {/* Score Cell with Interactive Click-to-Unblur */}
                <button
                  onClick={(e) => toggleUnblur(m.id, e)}
                  className={`px-1.5 py-0.5 rounded bg-[#1A1C22] font-tech text-[11px] font-black transition-all ${
                    shouldBlur
                      ? 'filter blur-sm hover:blur-none text-[#DFFF00]'
                      : 'text-white'
                  }`}
                  title={shouldBlur ? 'Click to unblur score' : 'Score visible'}
                >
                  {scoreText}
                </button>

                <span className="font-tech text-xs font-bold text-white">
                  {awayCode}
                </span>

                <span className="font-tech text-[9px] text-white/40 uppercase ml-0.5">
                  {m.competition_id || 'MATCH'}
                </span>
              </div>
            );
          })}
        </div>

        {/* Global Controls & Data Compliance Badge */}
        <div className="flex items-center gap-3 shrink-0">
          <button
            onClick={toggleSpoilerFree}
            className={`flex items-center gap-1.5 px-2.5 py-1 rounded font-tech text-[11px] font-bold uppercase tracking-wider transition-colors cursor-pointer ${
              isSpoilerFree
                ? 'bg-[#DFFF00] text-[#0A0A0C] shadow-[0_0_8px_rgba(223,255,0,0.3)]'
                : 'bg-[#1A1C22] text-white/70 hover:text-white border border-white/10'
            }`}
          >
            {isSpoilerFree ? (
              <>
                <EyeOff className="w-3.5 h-3.5" />
                <span>Spoilers: Hidden</span>
              </>
            ) : (
              <>
                <Eye className="w-3.5 h-3.5" />
                <span>Spoilers: Visible</span>
              </>
            )}
          </button>

          <div className="hidden lg:flex items-center gap-1.5 font-tech text-[10px] text-white/50 uppercase tracking-wider">
            <CheckCircle2 className="w-3.5 h-3.5 text-[#38BDF8]" />
            <span>Feed: ESPN FC &amp; YT Compliant</span>
          </div>
        </div>
      </div>
    </section>
  );
}
