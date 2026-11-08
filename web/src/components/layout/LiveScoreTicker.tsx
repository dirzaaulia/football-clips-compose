'use client';

import React, { useEffect, useState, useMemo } from 'react';
import Link from 'next/link';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';

interface LiveMatchItem {
  id: number;
  home_team_name: string;
  home_team_crest?: string | null;
  away_team_name: string;
  away_team_crest?: string | null;
  home_score?: number | null;
  away_score?: number | null;
  display_clock?: string | null;
  status: string;
  competition_name: string;
}

export default function LiveScoreTicker() {
  const [liveMatches, setLiveMatches] = useState<LiveMatchItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;

    async function fetchLive() {
      try {
        const res = await fetch(
          'https://edge.fc.dirzaaulia.com/rest/v1/matches?status=in.(IN_PLAY,PAUSED,LIVE)&order=utc_date.desc&limit=10',
          { cache: 'no-store' }
        );
        if (!res.ok) return;
        const data = await res.json();
        if (active && Array.isArray(data)) {
          setLiveMatches(data);
        }
      } catch {
        // Fallback silently if offline or edge unreachable
      } finally {
        if (active) setLoading(false);
      }
    }

    fetchLive();
    const timer = setInterval(fetchLive, 30_000);
    return () => {
      active = false;
      clearInterval(timer);
    };
  }, []);

  const marqueeDuration = useMemo(() => {
    // Proportional speed: 10s per 3 matches, minimum 20s
    return Math.max(20, Math.round(liveMatches.length * 8));
  }, [liveMatches.length]);

  if (loading || liveMatches.length === 0) {
    return null; // Keep layout completely clean when no matches are currently in play
  }

  const renderTickerList = (ariaHidden: boolean = false) => (
    <div
      aria-hidden={ariaHidden}
      className="flex items-center gap-3 shrink-0 animate-marquee hover:[animation-play-state:paused]"
      style={{ animationDuration: `${marqueeDuration}s` }}
    >
      {liveMatches.map((match, idx) => (
        <Link
          key={`${match.id}-${ariaHidden ? 'dup' : 'orig'}-${idx}`}
          href={`/match/${match.id}`}
          className="flex items-center gap-2 bg-[#161B22]/90 hover:bg-[#21262D] border border-white/10 hover:border-[#7098F8]/50 transition-all rounded-xl px-2.5 py-1 text-xs shrink-0 group shadow-sm select-none"
        >
          <span className="text-[#EF4444] text-[10px] font-mono font-bold tracking-tight shrink-0">
            {match.display_clock || 'LIVE'}
          </span>

          <div className="flex items-center gap-1.5 font-medium text-gray-200 shrink-0">
            <GlassLogoBadge
              src={match.home_team_crest}
              alt={match.home_team_name}
              size={18}
              imageClassName="object-contain p-0.5"
            />
            <span className="whitespace-nowrap font-bold text-xs text-white max-w-[120px] truncate">
              {match.home_team_name}
            </span>

            <span className="font-black text-xs text-white bg-black/60 px-1.5 py-0.5 rounded-md border border-white/10 shrink-0">
              {match.home_score ?? 0} - {match.away_score ?? 0}
            </span>

            <span className="whitespace-nowrap font-bold text-xs text-white max-w-[120px] truncate">
              {match.away_team_name}
            </span>
            <GlassLogoBadge
              src={match.away_team_crest}
              alt={match.away_team_name}
              size={18}
              imageClassName="object-contain p-0.5"
            />
          </div>
        </Link>
      ))}
    </div>
  );

  return (
    <>
      <style jsx global>{`
        @keyframes ticker-marquee {
          0% {
            transform: translateX(0%);
          }
          100% {
            transform: translateX(-100%);
          }
        }
        .animate-marquee {
          animation: ticker-marquee linear infinite;
        }
      `}</style>
      <div className="sticky top-0 z-40 bg-[#0B0F13]/95 backdrop-blur-md border-b border-red-500/25 px-3 py-1.5 overflow-hidden">
        <div className="flex items-center gap-3 max-w-7xl mx-auto overflow-hidden">
          {/* Pulsing Live Pill (Pinned left) */}
          <div className="flex items-center gap-1.5 bg-red-500/15 border border-red-500/40 px-2.5 py-1 rounded-full shrink-0 z-10 shadow-sm backdrop-blur-md">
            <span className="relative flex h-2 w-2">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2 w-2 bg-red-500"></span>
            </span>
            <span className="text-[10px] font-black tracking-wider text-red-400 uppercase">
              LIVE NOW ({liveMatches.length})
            </span>
          </div>

          {/* Marquee Track (Smooth looping marquee like TV ticker) */}
          <div className="flex items-center gap-3 overflow-hidden flex-1 relative [mask-image:linear-gradient(to_right,transparent,black_20px,black_calc(100%-20px),transparent)]">
            {renderTickerList(false)}
            {renderTickerList(true)}
          </div>
        </div>
      </div>
    </>
  );
}
