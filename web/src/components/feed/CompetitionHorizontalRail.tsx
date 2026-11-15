'use client';

import React, { useRef, useState } from 'react';
import Image from 'next/image';
import { ChevronLeft, ChevronRight, Trophy } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import HighlightRailCard from '@/components/feed/HighlightRailCard';

interface CompetitionHorizontalRailProps {
  name: string;
  emblemUrl?: string;
  matches: MatchRecord[];
  onPlay: (match: MatchRecord) => void;
  onSeeAll?: () => void;
}

export default function CompetitionHorizontalRail({
  name,
  emblemUrl,
  matches,
  onPlay,
  onSeeAll,
}: CompetitionHorizontalRailProps) {
  const scrollContainerRef = useRef<HTMLDivElement | null>(null);
  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(true);

  const checkScroll = () => {
    const el = scrollContainerRef.current;
    if (!el) return;
    setCanScrollLeft(el.scrollLeft > 20);
    setCanScrollRight(el.scrollLeft + el.clientWidth < el.scrollWidth - 20);
  };

  const scroll = (direction: 'left' | 'right') => {
    const el = scrollContainerRef.current;
    if (!el) return;
    const scrollAmount = el.clientWidth * 0.75;
    el.scrollBy({
      left: direction === 'left' ? -scrollAmount : scrollAmount,
      behavior: 'smooth',
    });
  };

  if (matches.length === 0) return null;

  return (
    <div className="relative space-y-3 group/rail select-none">
      {/* Rail Header */}
      <div className="flex items-center justify-between gap-3 px-1">
        <div className="flex items-center gap-2.5">
          {emblemUrl ? (
            <div className="relative w-6 h-6 shrink-0">
              <Image src={emblemUrl} alt={name} fill className="object-contain" unoptimized />
            </div>
          ) : (
            <Trophy className="w-5 h-5 text-[#DFFF00]" />
          )}
          <h2 className="text-xl sm:text-2xl font-scoreboard uppercase tracking-wider text-white">
            {name}
          </h2>
          <span className="text-xs font-tech font-bold text-white/40">
            ({matches.length})
          </span>
        </div>

        {onSeeAll && (
          <button
            onClick={onSeeAll}
            className="px-3 py-1 rounded-full text-xs font-tech font-bold text-[#DFFF00] hover:text-[#0A0A0C] hover:bg-[#DFFF00] border border-[#DFFF00]/40 transition-all cursor-pointer shadow-sm"
          >
            See All &gt;
          </button>
        )}
      </div>

      {/* Horizontal Sliding Carousel Container with Overlay Controls */}
      <div className="relative">
        {/* Left Arrow Button */}
        {canScrollLeft && (
          <button
            onClick={() => scroll('left')}
            className="absolute left-0 top-1/2 -translate-y-1/2 z-30 w-10 h-10 rounded-full bg-black/80 hover:bg-[#1A1C22] text-white border border-white/20 shadow-xl flex items-center justify-center opacity-0 group-hover/rail:opacity-100 transition-opacity backdrop-blur-md cursor-pointer -ml-3"
            aria-label="Scroll left"
          >
            <ChevronLeft className="w-5 h-5" />
          </button>
        )}

        {/* Scrollable Track */}
        <div
          ref={scrollContainerRef}
          onScroll={checkScroll}
          className="flex gap-4 sm:gap-5 overflow-x-auto no-scrollbar scroll-smooth py-3 px-1"
        >
          {matches.map((match) => (
            <HighlightRailCard key={match.id} match={match} onPlay={onPlay} />
          ))}
        </div>

        {/* Right Arrow Button */}
        {canScrollRight && (
          <button
            onClick={() => scroll('right')}
            className="absolute right-0 top-1/2 -translate-y-1/2 z-30 w-10 h-10 rounded-full bg-black/80 hover:bg-[#1A1C22] text-white border border-white/20 shadow-xl flex items-center justify-center opacity-0 group-hover/rail:opacity-100 transition-opacity backdrop-blur-md cursor-pointer -mr-3"
            aria-label="Scroll right"
          >
            <ChevronRight className="w-5 h-5" />
          </button>
        )}
      </div>
    </div>
  );
}
