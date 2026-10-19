'use client';

import React, { useMemo, useRef } from 'react';
import { Flame, ChevronLeft, ChevronRight } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';
import { useDragScroll } from '@/hooks/useDragScroll';
import NativeInFeedAdCard from '@/components/ads/NativeInFeedAdCard';
import { calculateExcitementRank } from '@/lib/excitement';
import ExcitementCard from './ExcitementCard';

interface ExcitementCarouselProps {
  matches: MatchRecord[];
  onPlay: (match: MatchRecord) => void;
  isLoading?: boolean;
}

export default function ExcitementCarousel({
  matches,
  onPlay,
  isLoading = false,
}: ExcitementCarouselProps) {
  const { isSpoilerFree } = useSpoilerFree();
  const scrollRef = useDragScroll<HTMLDivElement>();

  // Compute and rank excitement matches
  const rankedItems = useMemo(() => {
    // Only consider matches that actually have a linked highlight video
    const withHighlights = matches.filter((m) => !!m.highlight_video_id);
    const ranked = withHighlights.map(calculateExcitementRank);
    // Sort descending by excitement score
    ranked.sort((a, b) => b.score - a.score);
    // Take the top 8 exciting matches
    return ranked.slice(0, 8);
  }, [matches]);

  const scrollBy = (offset: number) => {
    if (scrollRef.current) {
      scrollRef.current.scrollBy({ left: offset, behavior: 'smooth' });
    }
  };

  if (isLoading) {
    return (
      <div className="mb-8 space-y-4">
        <div className="h-6 w-56 bg-[#181C20] rounded-xl animate-pulse" />
        <div className="flex gap-4 overflow-hidden">
          {[...Array(3)].map((_, i) => (
            <div
              key={i}
              className="w-[340px] h-[280px] rounded-3xl bg-[#1E2227] animate-pulse shrink-0 border border-[#334155]"
            />
          ))}
        </div>
      </div>
    );
  }

  if (rankedItems.length === 0) {
    return null;
  }

  return (
    <section className="mb-8">
      {/* Carousel Header */}
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2.5">
          <div className="p-2 rounded-xl bg-gradient-to-br from-amber-500/20 to-red-500/20 text-amber-400 border border-amber-500/30 shadow-[0_0_12px_rgba(245,158,11,0.2)]">
            <Flame className="w-5 h-5 fill-amber-400" />
          </div>
          <div>
            <h2 className="text-base sm:text-lg font-black text-white flex items-center gap-2">
              <span>Excitement Reel</span>
              <span className="hidden sm:inline px-2 py-0.5 rounded-full text-[10px] font-black uppercase bg-[#1E3A8A]/50 text-[#7098F8] border border-[#7098F8]/40">
                Top Thrillers
              </span>
            </h2>
            <p className="text-xs text-[#94A3B8] hidden sm:block">
              Curated heavyweight duels and goal fests with verified official highlights
            </p>
          </div>
        </div>

        {/* Carousel Navigation Arrows */}
        <div className="flex items-center gap-1.5">
          <button
            onClick={() => scrollBy(-360)}
            className="p-2 rounded-xl bg-[#181C20] hover:bg-[#1E2227] text-[#94A3B8] hover:text-white border border-[#334155] transition-all cursor-pointer"
            title="Scroll Left"
            aria-label="Scroll Left"
          >
            <ChevronLeft className="w-4 h-4" />
          </button>
          <button
            onClick={() => scrollBy(360)}
            className="p-2 rounded-xl bg-[#181C20] hover:bg-[#1E2227] text-[#94A3B8] hover:text-white border border-[#334155] transition-all cursor-pointer"
            title="Scroll Right"
            aria-label="Scroll Right"
          >
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Horizontal Scroll Track with In-Feed Ads Attached */}
      <div
        ref={scrollRef}
        className="flex items-stretch gap-4 sm:gap-5 overflow-x-auto scrollbar-none py-1 px-0.5 cursor-grab active:cursor-grabbing snap-x snap-mandatory"
      >
        {rankedItems.map((item, index) => (
          <React.Fragment key={item.match.id}>
            <div className="snap-start shrink-0">
              <ExcitementCard
                item={item}
                isSpoilerFree={isSpoilerFree}
                onPlay={onPlay}
              />
            </div>

            {/* In-feed Ad placed between carousel items (after 2nd and 5th items) */}
            {(index === 1 || index === 4) && (
              <div className="snap-start shrink-0">
                <NativeInFeedAdCard slotId="5616816386" variant="carousel" />
              </div>
            )}
          </React.Fragment>
        ))}
      </div>
    </section>
  );
}
