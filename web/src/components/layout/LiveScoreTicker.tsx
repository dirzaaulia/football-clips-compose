'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';

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

  if (loading || liveMatches.length === 0) {
    return null; // Keep layout completely clean when no matches are currently in play
  }

  return (
    <div className="sticky top-0 z-40 bg-[#0B0F13]/95 backdrop-blur-md border-b border-red-500/20 px-3 py-2">
      <div className="flex items-center gap-3 overflow-x-auto no-scrollbar max-w-7xl mx-auto">
        {/* Pulsing Live Pill */}
        <div className="flex items-center gap-1.5 bg-red-500/15 border border-red-500/40 px-2 py-0.5 rounded-full shrink-0">
          <span className="relative flex h-2 w-2">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-2 w-2 bg-red-500"></span>
          </span>
          <span className="text-[10px] font-black tracking-wider text-red-400 uppercase">
            LIVE NOW ({liveMatches.length})
          </span>
        </div>

        {/* Ticker Items */}
        <div className="flex items-center gap-2">
          {liveMatches.map((match) => (
            <Link
              key={match.id}
              href={`/match/${match.id}`}
              className="flex items-center gap-2 bg-[#161B22]/80 hover:bg-[#21262D] border border-white/5 hover:border-white/20 transition-all rounded-lg px-2.5 py-1 text-xs shrink-0 group"
            >
              <span className="text-gray-400 text-[10px] font-mono font-bold group-hover:text-red-400 transition-colors">
                {match.display_clock || 'LIVE'}
              </span>

              <div className="flex items-center gap-1.5 font-medium text-gray-200 shrink-0">
                <span className="whitespace-nowrap">{match.home_team_name}</span>
                <span className="font-bold text-white bg-black/40 px-1 rounded shrink-0">
                  {match.home_score ?? 0} - {match.away_score ?? 0}
                </span>
                <span className="whitespace-nowrap">{match.away_team_name}</span>
              </div>
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
}
