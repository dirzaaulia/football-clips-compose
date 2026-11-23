'use client';

import React, { useState, useEffect, useRef, useCallback } from 'react';
import Image from 'next/image';
import { ChevronUp, ChevronDown } from 'lucide-react';
import { FootballShortRecord } from '@/lib/shorts';
import ShortsReelItem from './ShortsReelItem';
import ShortsActionRail from './ShortsActionRail';

interface ShortsReelContainerProps {
  shorts: FootballShortRecord[];
}

async function shareShortRecord(short: FootballShortRecord) {
  const shareUrl = `https://fc.dirzaaulia.com/shorts?v=${short.youtube_video_id}`;
  if (typeof navigator !== 'undefined' && navigator.share) {
    try {
      await navigator.share({
        title: short.title,
        text: `Watch ${short.title} on FootballClips!`,
        url: shareUrl,
      });
    } catch {}
  } else if (typeof navigator !== 'undefined' && navigator.clipboard) {
    await navigator.clipboard.writeText(shareUrl);
    alert('Short link copied to clipboard!');
  }
}

export default function ShortsReelContainer({ shorts }: ShortsReelContainerProps) {
  const [activeIndex, setActiveIndex] = useState(0);
  const [isMuted, setIsMuted] = useState(true);
  const containerRef = useRef<HTMLDivElement>(null);
  const itemRefs = useRef<(HTMLDivElement | null)[]>([]);

  const scrollToIndex = useCallback(
    (index: number) => {
      if (index >= 0 && index < shorts.length) {
        itemRefs.current[index]?.scrollIntoView({ behavior: 'smooth', block: 'center' });
        setActiveIndex(index);
      }
    },
    [shorts.length],
  );

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'ArrowDown') {
        e.preventDefault();
        scrollToIndex(activeIndex + 1);
      } else if (e.key === 'ArrowUp') {
        e.preventDefault();
        scrollToIndex(activeIndex - 1);
      } else if (e.key.toLowerCase() === 'm') {
        setIsMuted((prev) => !prev);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [activeIndex, scrollToIndex]);

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            const index = Number(entry.target.getAttribute('data-index'));
            if (!isNaN(index)) setActiveIndex(index);
          }
        }
      },
      { threshold: 0.65 },
    );
    itemRefs.current.forEach((el) => {
      if (el) observer.observe(el);
    });
    return () => observer.disconnect();
  }, [shorts]);

  const activeShort = shorts[activeIndex] || shorts[0];

  return (
    <div className="relative w-full h-[calc(100vh-3rem)] md:h-[calc(100vh-4rem)] overflow-hidden bg-[#0A0A0C] flex justify-center items-center">
      {activeShort && (
        <div className="absolute inset-0 pointer-events-none -z-10 overflow-hidden">
          <Image
            src={activeShort.thumbnail_url}
            alt="Ambient Backdrop"
            fill
            className="object-cover blur-3xl opacity-15 scale-125 transform transition-all duration-700"
            priority={false}
          />
          <div className="absolute inset-0 bg-gradient-to-t from-[#0A0A0C] via-[#0A0A0C]/60 to-[#0A0A0C]" />
        </div>
      )}

      <div className="relative flex items-center justify-center gap-4 sm:gap-6 h-full py-2 overflow-x-hidden">
        <div
          ref={containerRef}
          className="w-[360px] sm:w-[410px] md:w-[430px] h-[calc(100vh-5rem)] max-h-[820px] overflow-x-hidden overflow-y-scroll snap-y snap-mandatory no-scrollbar rounded-3xl border border-white/10 shadow-2xl bg-[#121316]"
        >
          {shorts.map((short, idx) => (
            <div
              key={short.id}
              ref={(el) => {
                itemRefs.current[idx] = el;
              }}
              data-index={idx}
              className="w-full h-full snap-start snap-always flex items-center justify-center overflow-x-hidden"
            >
              <ShortsReelItem
                short={short}
                isActive={idx === activeIndex}
                isMuted={isMuted}
                onToggleMute={() => setIsMuted((prev) => !prev)}
                onShare={() => shareShortRecord(short)}
              />
            </div>
          ))}
        </div>

        {activeShort && (
          <div className="hidden md:flex flex-col items-center justify-end pb-3">
            <ShortsActionRail
              videoId={activeShort.youtube_video_id}
              isMuted={isMuted}
              onToggleMute={() => setIsMuted((prev) => !prev)}
              onShare={() => shareShortRecord(activeShort)}
            />
          </div>
        )}

        <div className="hidden md:flex flex-col gap-2">
          <button
            type="button"
            onClick={() => scrollToIndex(activeIndex - 1)}
            disabled={activeIndex === 0}
            className="w-10 h-10 rounded-full bg-[#1A1C22] hover:bg-[#252830] disabled:opacity-30 border border-white/10 text-white flex items-center justify-center transition-all cursor-pointer"
            title="Previous Short"
          >
            <ChevronUp className="w-5 h-5" />
          </button>
          <button
            type="button"
            onClick={() =>
              scrollToIndex(activeIndex >= shorts.length - 1 ? 0 : activeIndex + 1)
            }
            disabled={shorts.length <= 1}
            className="w-10 h-10 rounded-full bg-[#1A1C22] hover:bg-[#252830] disabled:opacity-30 border border-white/10 text-white flex items-center justify-center transition-all cursor-pointer"
            title="Next Short"
          >
            <ChevronDown className="w-5 h-5" />
          </button>
        </div>
      </div>
    </div>
  );
}
