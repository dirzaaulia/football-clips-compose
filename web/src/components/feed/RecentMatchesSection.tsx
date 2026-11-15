'use client';

import React, { useRef, useState } from 'react';
import Link from 'next/link';
import { MatchRecord } from '@/lib/database';
import { ChevronRight, ChevronLeft } from 'lucide-react';
import HighlightGridCard from '@/components/feed/HighlightGridCard';
import NativeInFeedAdCard from '@/components/ads/NativeInFeedAdCard';

interface RecentMatchesSectionProps {
  matches: MatchRecord[];
  onPlay?: (match: MatchRecord) => void;
  className?: string;
}

export default function RecentMatchesSection({
  matches,
  onPlay,
  className = '',
}: RecentMatchesSectionProps) {
  const scrollRef = useRef<HTMLDivElement | null>(null);
  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(true);

  const recentHighlights = matches
    .filter((m) => {
      const vid = m.highlight_video_id || m.external_video_id;
      return !!vid && vid.trim() !== '';
    })
    .slice(0, 16);

  if (recentHighlights.length === 0) {
    return null;
  }

  const checkScroll = () => {
    const el = scrollRef.current;
    if (!el) return;
    setCanScrollLeft(el.scrollLeft > 20);
    setCanScrollRight(el.scrollLeft + el.clientWidth < el.scrollWidth - 20);
  };

  const handleScroll = (direction: 'left' | 'right') => {
    const el = scrollRef.current;
    if (!el) return;
    const amount = el.clientWidth * 0.75;
    el.scrollBy({
      left: direction === 'left' ? -amount : amount,
      behavior: 'smooth',
    });
  };

  return (
    <section className={`space-y-3.5 select-none group/recent ${className}`}>
      {/* Section Header */}
      <div className="flex items-center justify-between pb-1.5 border-b border-white/10">
        <div className="flex items-center gap-2.5">
          <span className="w-1 h-5 rounded-full bg-[#DFFF00] shadow-[0_0_8px_rgba(223,255,0,0.4)]" />
          <h2 className="font-scoreboard text-xl sm:text-2xl text-white tracking-wider uppercase">
            Recent Highlights
          </h2>
          <span className="font-tech text-[10px] font-bold px-2 py-0.5 rounded-full bg-[#DFFF00]/15 text-[#DFFF00] border border-[#DFFF00]/30 hidden sm:inline-block">
            LATEST RECAPS
          </span>
        </div>
        <Link
          href="/fixtures"
          className="font-tech text-xs font-bold text-[#DFFF00] hover:text-[#cbe800] transition flex items-center gap-1"
        >
          <span>All Fixtures</span>
          <ChevronRight className="w-3.5 h-3.5" />
        </Link>
      </div>

      {/* Horizontal Rail with Chevrons and Interleaved Ads */}
      <div className="relative">
        {canScrollLeft && (
          <button
            onClick={() => handleScroll('left')}
            className="absolute left-0 top-1/2 -translate-y-1/2 z-30 w-10 h-10 rounded-full bg-black/80 hover:bg-[#1A1C22] text-white border border-white/20 shadow-xl flex items-center justify-center opacity-0 group-hover/recent:opacity-100 transition-opacity backdrop-blur-md cursor-pointer -ml-3"
            aria-label="Scroll left"
          >
            <ChevronLeft className="w-5 h-5" />
          </button>
        )}

        {canScrollRight && (
          <button
            onClick={() => handleScroll('right')}
            className="absolute right-0 top-1/2 -translate-y-1/2 z-30 w-10 h-10 rounded-full bg-black/80 hover:bg-[#1A1C22] text-white border border-white/20 shadow-xl flex items-center justify-center opacity-0 group-hover/recent:opacity-100 transition-opacity backdrop-blur-md cursor-pointer -mr-3"
            aria-label="Scroll right"
          >
            <ChevronRight className="w-5 h-5" />
          </button>
        )}

        <div
          ref={scrollRef}
          onScroll={checkScroll}
          className="flex gap-4 sm:gap-5 overflow-x-auto no-scrollbar scroll-smooth py-2 px-0.5"
        >
          {recentHighlights.map((m, idx) => (
            <React.Fragment key={m.id}>
              <div className="w-[280px] sm:w-[320px] shrink-0">
                <HighlightGridCard match={m} onPlay={onPlay} />
              </div>
              {idx > 0 && idx % 3 === 0 && (
                <div className="w-[280px] sm:w-[320px] shrink-0 flex items-stretch">
                  <NativeInFeedAdCard slotId="5616816386" variant="carousel" />
                </div>
              )}
            </React.Fragment>
          ))}
        </div>
      </div>
    </section>
  );
}
