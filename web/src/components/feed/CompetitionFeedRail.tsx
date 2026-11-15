'use client';

import React, { useRef, useState } from 'react';
import Image from 'next/image';
import { ChevronLeft, ChevronRight, Trophy, ArrowRight } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import HighlightGridCard from '@/components/feed/HighlightGridCard';
import NativeInFeedAdCard from '@/components/ads/NativeInFeedAdCard';

interface CompetitionFeedRailProps {
  compId: string;
  name: string;
  emblemUrl?: string;
  matches: MatchRecord[];
  onPlay: (match: MatchRecord) => void;
  onSeeAll?: (compId: string) => void;
}

export default function CompetitionFeedRail({
  compId,
  name,
  emblemUrl,
  matches,
  onPlay,
  onSeeAll,
}: CompetitionFeedRailProps) {
  const scrollRef = useRef<HTMLDivElement | null>(null);
  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(true);

  if (matches.length === 0) return null;

  const displayMatches = matches.slice(0, 20);

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
    <section className="w-full py-4 space-y-3 group/rail select-none">
      {/* Section Header */}
      <div className="w-full flex items-end justify-between px-1">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-lg bg-[#121316] flex items-center justify-center border border-[#DFFF00]/30 shrink-0">
            {emblemUrl ? (
              <div className="relative w-6 h-6">
                <Image
                  src={emblemUrl}
                  alt={name}
                  fill
                  className="object-contain"
                  unoptimized
                />
              </div>
            ) : (
              <Trophy className="w-5 h-5 text-[#DFFF00]" />
            )}
          </div>

          <div>
            <div className="flex items-center gap-2">
              <h2 className="font-scoreboard text-xl sm:text-2xl uppercase text-white tracking-wide leading-none">
                {name}
              </h2>
              <span className="px-2 py-0.5 rounded bg-[#DFFF00]/10 border border-[#DFFF00]/20 text-[#DFFF00] font-tech text-[10px] uppercase font-mono">
                2026/27
              </span>
            </div>
            <p className="font-tech text-xs text-white/50 mt-0.5">
              Latest {displayMatches.length} Matches • Official Verified Highlights
            </p>
          </div>
        </div>

        {onSeeAll && (
          <button
            onClick={() => onSeeAll(compId)}
            className="px-3.5 py-1.5 rounded-lg bg-[#121316] hover:bg-[#1A1C22] text-[#DFFF00] hover:text-white font-tech text-xs uppercase tracking-wider flex items-center gap-1.5 border border-white/10 transition-colors shadow-sm cursor-pointer"
            type="button"
          >
            <span>See More ({matches.length})</span>
            <ArrowRight className="w-3.5 h-3.5 text-[#DFFF00]" />
          </button>
        )}
      </div>

      {/* Horizontal Carousel Track with Controls */}
      <div className="relative">
        {canScrollLeft && (
          <button
            onClick={() => handleScroll('left')}
            className="absolute left-0 top-1/2 -translate-y-1/2 z-30 w-10 h-10 rounded-full bg-black/80 hover:bg-[#1A1C22] text-white border border-white/20 shadow-xl flex items-center justify-center opacity-0 group-hover/rail:opacity-100 transition-opacity backdrop-blur-md cursor-pointer -ml-3"
            aria-label="Scroll left"
          >
            <ChevronLeft className="w-5 h-5" />
          </button>
        )}

        {canScrollRight && (
          <button
            onClick={() => handleScroll('right')}
            className="absolute right-0 top-1/2 -translate-y-1/2 z-30 w-10 h-10 rounded-full bg-black/80 hover:bg-[#1A1C22] text-white border border-white/20 shadow-xl flex items-center justify-center opacity-0 group-hover/rail:opacity-100 transition-opacity backdrop-blur-md cursor-pointer -mr-3"
            aria-label="Scroll right"
          >
            <ChevronRight className="w-5 h-5" />
          </button>
        )}

        <div
          ref={scrollRef}
          onScroll={checkScroll}
          className="flex gap-4 sm:gap-5 overflow-x-auto no-scrollbar scroll-smooth py-2 px-1"
        >
          {displayMatches.map((m, idx) => (
            <React.Fragment key={m.id}>
              <div className="w-[280px] sm:w-[320px] md:w-[360px] shrink-0">
                <HighlightGridCard match={m} onPlay={onPlay} />
              </div>
              {idx > 0 && idx % 4 === 0 && (
                <div className="w-[280px] sm:w-[320px] shrink-0 flex items-stretch">
                  <NativeInFeedAdCard slotId="5616816386" />
                </div>
              )}
            </React.Fragment>
          ))}
        </div>
      </div>
    </section>
  );
}
