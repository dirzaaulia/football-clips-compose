'use client';

import React, { useState, useEffect, useMemo } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { Play, BookmarkPlus, BarChart3, Flame, ChevronLeft, ChevronRight, Eye, EyeOff } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';
import { useProStatus } from '@/hooks/useProStatus';
import { arrangeExcitementSlides, ExcitementSlide } from '@/lib/excitementRanking';
import HeroSponsoredSlide from './HeroSponsoredSlide';

interface HeroBillboardProps {
  matches?: MatchRecord[];
  match?: MatchRecord | null;
  onPlay: (match: MatchRecord) => void;
  isLoading?: boolean;
}

export default function HeroBillboard({
  matches = [],
  match,
  onPlay,
  isLoading = false,
}: HeroBillboardProps) {
  const { isSpoilerFree } = useSpoilerFree();
  const hasPro = useProStatus();
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isRevealed, setIsRevealed] = useState(false);
  const [imgLoaded, setImgLoaded] = useState(false);

  const candidateMatches = useMemo(() => {
    if (matches.length > 0) return matches;
    return match ? [match] : [];
  }, [matches, match]);

  const slides: ExcitementSlide[] = useMemo(() => {
    return arrangeExcitementSlides(candidateMatches, hasPro, isSpoilerFree);
  }, [candidateMatches, hasPro, isSpoilerFree]);

  useEffect(() => {
    if (slides.length <= 1) return;
    const interval = setInterval(() => {
      setCurrentIndex((prev) => (prev + 1) % slides.length);
      setIsRevealed(false);
      setImgLoaded(false);
    }, 6500);
    return () => clearInterval(interval);
  }, [slides.length]);

  if (isLoading || slides.length === 0) {
    return (
      <div className="relative w-full h-[75vh] sm:h-[80vh] lg:h-[88vh] min-h-[580px] lg:min-h-[660px] bg-[#0A0A0C] p-6 sm:p-12 lg:p-16 flex flex-col justify-end animate-pulse select-none overflow-hidden">
        <div className="space-y-4 max-w-2xl pb-4">
          <div className="h-6 w-36 rounded-full bg-[#DFFF00]/25" />
          <div className="h-12 sm:h-16 w-3/4 rounded-xl bg-white/20" />
          <div className="h-4 w-1/2 rounded bg-white/10" />
          <div className="flex items-center gap-3 pt-3">
            <div className="h-12 w-44 rounded-xl bg-[#DFFF00]/25 border border-[#DFFF00]/30" />
            <div className="h-12 w-36 rounded-xl bg-white/5 border border-white/10" />
          </div>
        </div>
      </div>
    );
  }

  const activeSlide = slides[currentIndex] || slides[0];

  if (activeSlide.type === 'ad') {
    return (
      <section className="relative w-full overflow-hidden bg-[#0A0A0C] select-none shadow-2xl">
        <HeroSponsoredSlide />
      </section>
    );
  }

  const currentMatch = activeSlide.match;
  const homeName = currentMatch.home_team_name || currentMatch.home_team;
  const awayName = currentMatch.away_team_name || currentMatch.away_team;
  const videoId = currentMatch.highlight_video_id;
  const thumbUrl = videoId ? `https://i.ytimg.com/vi/${videoId}/maxresdefault.jpg` : null;
  const isHidden = isSpoilerFree && !isRevealed;
  const hasScores = currentMatch.home_score !== null && currentMatch.away_score !== null;

  return (
    <section className="relative w-full overflow-hidden bg-[#0A0A0C] select-none shadow-2xl group">
      <div className="relative w-full h-[75vh] sm:h-[80vh] lg:h-[88vh] min-h-[580px] lg:min-h-[660px] flex flex-col justify-end">
        {/* Shimmer Placeholder before load */}
        {!imgLoaded && (
          <div className="absolute inset-0 bg-gradient-to-r from-white/5 via-white/10 to-white/5 animate-pulse z-0" />
        )}

        {/* Full-Screen Thumbnail Backdrop with Smooth Transition */}
        <div className="absolute inset-0 z-0">
          {thumbUrl ? (
            <Image
              src={thumbUrl}
              alt={`${homeName} vs ${awayName}`}
              fill
              priority
              onLoad={() => setImgLoaded(true)}
              className={`object-cover object-center scale-100 group-hover:scale-[1.02] transition-transform duration-1000 ease-out ${
                isHidden ? 'blur-2xl scale-110 opacity-30' : 'opacity-70'
              }`}
              unoptimized
            />
          ) : (
            <div className="w-full h-full bg-gradient-to-tr from-[#0A0A0C] via-[#121316] to-[#1A1C22]" />
          )}

          {/* DAZN-Style Gradient Scrims: Multi-stage Bottom Fade into #0A0A0C */}
          <div className="absolute inset-0 bg-gradient-to-t from-[#0A0A0C] via-[#0A0A0C]/90 via-[#0A0A0C]/40 to-transparent" />
          <div className="absolute inset-0 bg-gradient-to-r from-[#0A0A0C]/95 via-[#0A0A0C]/75 to-transparent w-full lg:w-3/5" />
          <div className="absolute top-0 left-0 right-0 h-28 bg-gradient-to-b from-[#0A0A0C]/80 to-transparent" />
        </div>

        {/* Carousel Previous / Next Nav Chevrons */}
        {slides.length > 1 && (
          <>
            <button
              onClick={() => {
                setCurrentIndex((prev) => (prev - 1 + slides.length) % slides.length);
                setIsRevealed(false);
              }}
              className="absolute left-4 top-1/2 -translate-y-1/2 z-20 w-12 h-12 rounded-full bg-black/60 hover:bg-[#1A1C22] text-white border border-white/20 backdrop-blur-md flex items-center justify-center opacity-0 group-hover:opacity-100 transition-all cursor-pointer shadow-xl"
              title="Previous Match"
            >
              <ChevronLeft className="w-6 h-6" />
            </button>
            <button
              onClick={() => {
                setCurrentIndex((prev) => (prev + 1) % slides.length);
                setIsRevealed(false);
              }}
              className="absolute right-4 top-1/2 -translate-y-1/2 z-20 w-12 h-12 rounded-full bg-black/60 hover:bg-[#1A1C22] text-white border border-white/20 backdrop-blur-md flex items-center justify-center opacity-0 group-hover:opacity-100 transition-all cursor-pointer shadow-xl"
              title="Next Match"
            >
              <ChevronRight className="w-6 h-6" />
            </button>
          </>
        )}

        {/* Foreground Content Container */}
        <div className="relative z-10 p-6 sm:p-10 lg:p-14 max-w-4xl space-y-3.5">
          {/* Header Badges with Excitement Tag */}
          <div className="flex flex-wrap items-center gap-2">
            <span className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#DFFF00] text-[#0A0A0C] font-tech text-xs font-black uppercase tracking-wider shadow-[0_0_15px_rgba(223,255,0,0.4)]">
              <Flame className="w-3.5 h-3.5 fill-[#0A0A0C]" />
              <span>{activeSlide.tag}</span>
            </span>

            <span className="px-3 py-1 rounded-full bg-black/60 backdrop-blur-md border border-white/10 font-tech text-xs text-white/90 uppercase tracking-wider font-mono">
              {currentMatch.competition_name} • Matchday {currentMatch.matchday || 2026}
            </span>

            {/* Frosted Glass Score Pill */}
            {hasScores && (
              <button
                onClick={() => setIsRevealed((prev) => !prev)}
                className={`px-3 py-1 rounded-full font-tech text-xs font-bold transition-all backdrop-blur-md flex items-center gap-1.5 cursor-pointer ${
                  isHidden
                    ? 'bg-black/90 text-[#DFFF00] filter blur-sm hover:blur-none border border-[#DFFF00]/40'
                    : 'bg-black/80 text-[#DFFF00] border border-white/10'
                }`}
                title={isHidden ? 'Click to reveal score' : 'Click to hide score'}
              >
                <span>{currentMatch.home_score} - {currentMatch.away_score}</span>
                {isHidden ? <Eye className="w-3.5 h-3.5 text-[#DFFF00]" /> : <EyeOff className="w-3 h-3 text-white/40" />}
              </button>
            )}
          </div>

          {/* Showdown Headline */}
          <h1 className="font-scoreboard text-4xl sm:text-6xl lg:text-7xl uppercase text-white tracking-wide leading-none drop-shadow-xl">
            <span>{homeName}</span>
            <span className="text-[#DFFF00] mx-3 lowercase text-2xl sm:text-4xl lg:text-5xl font-mono">
              vs
            </span>
            <span>{awayName}</span>
          </h1>

          {/* Tactical Debrief */}
          <p className="font-tech text-xs sm:text-sm text-white/80 line-clamp-2 max-w-2xl leading-relaxed">
            {currentMatch.recap_analysis ||
              `Tactical masterclass and high-intensity counter-transitions. Watch full match highlights, decisive goals, and strategic press sequences from this European showdown.`}
          </p>

          {/* Action Trigger Row */}
          <div className="flex flex-wrap items-center gap-3 sm:gap-4 pt-2">
            <button
              onClick={() => onPlay(currentMatch)}
              className="px-7 sm:px-9 py-3.5 rounded-xl bg-[#DFFF00] hover:bg-[#cbe800] text-[#0A0A0C] font-tech text-xs sm:text-sm font-black uppercase tracking-wider flex items-center gap-2 shadow-[0_0_20px_rgba(223,255,0,0.5)] transition-all hover:scale-105 active:scale-95 cursor-pointer"
            >
              <Play className="w-4 h-4 fill-[#0A0A0C]" />
              <span>Play Highlights</span>
            </button>

            <Link
              href={`/match/${currentMatch.id}`}
              className="px-5 sm:px-6 py-3.5 rounded-xl bg-[#1A1C22]/90 hover:bg-[#252830] text-white border border-white/10 hover:border-[#DFFF00]/50 font-tech text-xs sm:text-sm font-bold uppercase tracking-wider flex items-center gap-2 backdrop-blur-md transition-all cursor-pointer"
            >
              <BarChart3 className="w-4 h-4 text-[#38BDF8]" />
              <span>Tactical Debrief</span>
            </Link>

            <button
              onClick={() => {
                if (typeof window !== 'undefined') {
                  navigator.clipboard?.writeText(`${window.location.origin}/match/${currentMatch.id}`);
                }
              }}
              className="p-3.5 rounded-xl bg-[#1A1C22]/80 hover:bg-[#252830] text-white/80 hover:text-white border border-white/10 backdrop-blur-md transition-all cursor-pointer"
              title="Copy Match Link"
            >
              <BookmarkPlus className="w-4 h-4" />
            </button>
          </div>

          {/* Slide Indicator Dots */}
          {slides.length > 1 && (
            <div className="flex items-center gap-2 pt-3">
              {slides.map((_, idx) => (
                <button
                  key={idx}
                  onClick={() => setCurrentIndex(idx)}
                  className={`h-1.5 rounded-full transition-all cursor-pointer ${
                    currentIndex === idx ? 'w-8 bg-[#DFFF00]' : 'w-2 bg-white/30 hover:bg-white/60'
                  }`}
                  aria-label={`Go to slide ${idx + 1}`}
                />
              ))}
            </div>
          )}
        </div>
      </div>
    </section>
  );
}
