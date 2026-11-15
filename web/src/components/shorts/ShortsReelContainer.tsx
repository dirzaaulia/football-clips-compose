'use client';

import React, { useState, useEffect, useRef, useCallback } from 'react';
import Image from 'next/image';
import { ChevronUp, ChevronDown } from 'lucide-react';
import { FootballShortRecord } from '@/lib/shorts';
import ShortsReelItem from './ShortsReelItem';
import ShortsChannelRail from './ShortsChannelRail';
import ShortsUpNextRail from './ShortsUpNextRail';

interface ShortsReelContainerProps {
  shorts: FootballShortRecord[];
}

export default function ShortsReelContainer({ shorts }: ShortsReelContainerProps) {
  const [activeIndex, setActiveIndex] = useState(0);
  const [isMuted, setIsMuted] = useState(true);
  const [activeCategory, setActiveCategory] = useState('all');
  const [likedMap, setLikedMap] = useState<Record<string, boolean>>({});
  const containerRef = useRef<HTMLDivElement>(null);
  const itemRefs = useRef<(HTMLDivElement | null)[]>([]);

  useEffect(() => {
    try {
      const saved = localStorage.getItem('fc_liked_shorts');
      if (saved) setLikedMap(JSON.parse(saved));
    } catch {}
  }, []);

  const toggleLike = useCallback((id: string) => {
    setLikedMap((prev) => {
      const updated = { ...prev, [id]: !prev[id] };
      try {
        localStorage.setItem('fc_liked_shorts', JSON.stringify(updated));
      } catch {}
      return updated;
    });
  }, []);

  const scrollToIndex = useCallback((index: number) => {
    if (index >= 0 && index < shorts.length) {
      itemRefs.current[index]?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      setActiveIndex(index);
    }
  }, [shorts.length]);

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

  const handleShare = async (short: FootballShortRecord) => {
    const shareUrl = `https://footballclips.web.app/shorts?v=${short.youtube_video_id}`;
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
  };

  const activeShort = shorts[activeIndex];

  return (
    <div className="relative w-full h-[calc(100vh-3rem)] md:h-screen overflow-hidden bg-[#0A0A0C] flex justify-center items-center">
      {/* Dynamic Ambient Backdrop */}
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

      {/* 3-Column Desktop OTT Canvas */}
      <div className="w-full max-w-7xl mx-auto px-4 flex justify-center items-center gap-6 h-full">
        {/* Left Category Rail */}
        <ShortsChannelRail
          activeCategory={activeCategory}
          onSelectCategory={setActiveCategory}
        />

        {/* Center Vertical OTT Player */}
        <div className="relative flex items-center justify-center h-full py-2">
          <div
            ref={containerRef}
            className="w-[380px] sm:w-[420px] h-[calc(100vh-5rem)] max-h-[820px] overflow-y-scroll snap-y snap-mandatory no-scrollbar rounded-2xl border border-white/10 shadow-2xl bg-[#121316]"
          >
            {shorts.map((short, idx) => (
              <div
                key={short.id}
                ref={(el) => { itemRefs.current[idx] = el; }}
                data-index={idx}
                className="w-full h-full snap-start snap-always flex items-center justify-center"
              >
                <ShortsReelItem
                  short={short}
                  isActive={idx === activeIndex}
                  isMuted={isMuted}
                  isLiked={!!likedMap[short.id]}
                  onToggleLike={() => toggleLike(short.id)}
                  onToggleMute={() => setIsMuted((prev) => !prev)}
                  onShare={() => handleShare(short)}
                />
              </div>
            ))}
          </div>

          {/* Desktop Up/Down Navigation Buttons */}
          <div className="hidden md:flex flex-col gap-2 ml-3">
            <button
              onClick={() => scrollToIndex(activeIndex - 1)}
              disabled={activeIndex === 0}
              className="w-10 h-10 rounded-full bg-[#1A1C22] hover:bg-[#252830] disabled:opacity-30 border border-white/10 text-white flex items-center justify-center transition-all cursor-pointer"
              title="Previous Short"
            >
              <ChevronUp className="w-5 h-5" />
            </button>
            <button
              onClick={() => scrollToIndex(activeIndex + 1)}
              disabled={activeIndex >= shorts.length - 1}
              className="w-10 h-10 rounded-full bg-[#1A1C22] hover:bg-[#252830] disabled:opacity-30 border border-white/10 text-white flex items-center justify-center transition-all cursor-pointer"
              title="Next Short"
            >
              <ChevronDown className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Right Up-Next Queue */}
        <ShortsUpNextRail
          shorts={shorts}
          activeIndex={activeIndex}
          onSelectIndex={scrollToIndex}
        />
      </div>
    </div>
  );
}
