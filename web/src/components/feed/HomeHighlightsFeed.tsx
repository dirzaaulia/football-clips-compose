'use client';

import React from 'react';
import { MatchRecord } from '@/lib/database';
import HighlightGridCard from '@/components/feed/HighlightGridCard';
import NativeInFeedAdCard from '@/components/ads/NativeInFeedAdCard';
import AdSenseBanner from '@/components/ads/AdSenseBanner';
import HomeShortsRail from '@/components/shorts/HomeShortsRail';
import { Trophy } from 'lucide-react';

interface HomeHighlightsFeedProps {
  displayedMatches: MatchRecord[];
  onPlay: (match: MatchRecord) => void;
}

export default function HomeHighlightsFeed({
  displayedMatches,
  onPlay,
}: HomeHighlightsFeedProps) {
  if (displayedMatches.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center p-12 text-center rounded-2xl bg-[#121316] border border-white/10 my-6">
        <Trophy className="w-10 h-10 text-white/30 mb-3" />
        <h3 className="text-base font-bold text-white font-scoreboard uppercase tracking-wider">
          No Highlights Found
        </h3>
        <p className="text-xs text-white/50 mt-1 max-w-sm font-tech">
          No highlights match your selected competition or filters. Try clearing your filters or selecting another league.
        </p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 sm:gap-5 my-4 animate-in fade-in duration-300">
      {displayedMatches.map((m, idx) => (
        <React.Fragment key={m.id}>
          <HighlightGridCard match={m} onPlay={onPlay} />

          {/* 9:16 Shorts Rail injected cleanly across full grid width after 4 cards */}
          {idx === 3 && (
            <div className="col-span-full my-2">
              <HomeShortsRail />
            </div>
          )}

          {/* Native In-Feed Ad Card every 8 items */}
          {idx > 0 && idx % 8 === 0 && (
            <NativeInFeedAdCard slotId="5616816386" />
          )}

          {/* Responsive AdSense Banner across full width every 16 items */}
          {idx > 0 && idx % 16 === 0 && idx < displayedMatches.length - 1 && (
            <div className="col-span-full my-2">
              <AdSenseBanner slotId="5616816386" format="auto" />
            </div>
          )}
        </React.Fragment>
      ))}
    </div>
  );
}
