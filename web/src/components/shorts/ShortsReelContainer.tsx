'use client';

import React, { useState, useEffect, useRef, useCallback } from 'react';
import Image from 'next/image';
import { ChevronUp, ChevronDown } from 'lucide-react';
import { FootballShortRecord } from '@/lib/shorts';
import ShortsReelItem from './ShortsReelItem';

interface ShortsReelContainerProps {
  shorts: FootballShortRecord[];
}

export default function ShortsReelContainer({ shorts }: ShortsReelContainerProps) {
  const [activeIndex, setActiveIndex] = useState(0);
  const [isMuted, setIsMuted] = useState(true);
  const [likedMap, setLikedMap] = useState<Record<string, boolean>>({});
  const containerRef = useRef<HTMLDivElement>(null);
  const itemRefs = useRef<(HTMLDivElement | null)[]>([]);

  // Load liked items from localStorage
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

  // Keyboard navigation
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

  // Track active item with IntersectionObserver
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
      { threshold: 0.65 }
    );

    itemRefs.current.forEach((el) => {
      if (el) observer.observe(el);
    });

    return () => observer.disconnect();
  }, [shorts]);

  const handleShare = async (short: FootballShortRecord) => {
    const shareUrl = `https://fc.dirzaaulia.com/shorts?v=${short.youtube_video_id}`;
    if (typeof navigator !== 'undefined' && navigator.share) {
      try {
        await navigator.share({
          title: short.title,
          text: `Watch ${short.title} on FootballClips Shorts!`,
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
    <div className="relative w-full h-[calc(100vh-4rem)] md:h-screen overflow-hidden bg-[#0A0E14]">
      {/* Ambient Dynamic Backdrop Glow */}
      {activeShort && (
        <div className="absolute inset-0 pointer-events-none -z-10 overflow-hidden">
          <Image
            src={activeShort.thumbnail_url}
            alt="Ambient Backdrop"
            fill
            className="object-cover blur-3xl opacity-20 scale-125 transform transition-all duration-700"
            priority={false}
          />
          <div className="absolute inset-0 bg-gradient-to-t from-[#0A0E14] via-transparent to-[#0A0E14]" />
        </div>
      )}

      {/* Vertical Snap Scroll Feed */}
      <div
        ref={containerRef}
        className="w-full h-full overflow-y-scroll snap-y snap-mandatory no-scrollbar"
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

      {/* Floating Desktop Navigation Arrows */}
      <div className="hidden md:flex flex-col gap-2 absolute right-6 top-1/2 -translate-y-1/2 z-20">
        <button
          onClick={() => scrollToIndex(activeIndex - 1)}
          disabled={activeIndex === 0}
          className="p-3 rounded-full bg-white/10 hover:bg-white/20 disabled:opacity-20 text-white backdrop-blur-md transition border border-white/10"
          title="Previous Short (Arrow Up)"
        >
          <ChevronUp className="w-5 h-5" />
        </button>
        <button
          onClick={() => scrollToIndex(activeIndex + 1)}
          disabled={activeIndex === shorts.length - 1}
          className="p-3 rounded-full bg-white/10 hover:bg-white/20 disabled:opacity-20 text-white backdrop-blur-md transition border border-white/10"
          title="Next Short (Arrow Down)"
        >
          <ChevronDown className="w-5 h-5" />
        </button>
      </div>
    </div>
  );
}
