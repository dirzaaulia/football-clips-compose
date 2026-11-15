'use client';

import React, { useState } from 'react';
import Image from 'next/image';
import { Flame, Play, Activity, ChevronRight, ChevronLeft, Eye } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';

interface HeroCinematicBannerProps {
  matches: MatchRecord[];
  onPlay: (match: MatchRecord) => void;
  isLoading?: boolean;
}

export default function HeroCinematicBanner({
  matches,
  onPlay,
  isLoading = false,
}: HeroCinematicBannerProps) {
  const { isSpoilerFree } = useSpoilerFree();
  const [activeIndex, setActiveIndex] = useState(0);
  const [revealedMatchIds, setRevealedMatchIds] = useState<Set<string>>(new Set());

  const topMatches = matches.slice(0, 5);
  const activeMatch = topMatches[activeIndex] || matches[0];

  if (isLoading || !activeMatch) {
    return <HeroSkeleton />;
  }

  const homeName = activeMatch.home_team_name || activeMatch.home_team;
  const awayName = activeMatch.away_team_name || activeMatch.away_team;
  const videoId = activeMatch.highlight_video_id;
  const thumbUrl = videoId
    ? `https://i.ytimg.com/vi/${videoId}/maxresdefault.jpg`
    : '/favicon.png';

  const isRevealed = revealedMatchIds.has(activeMatch.id);
  const isHidden = isSpoilerFree && !isRevealed;
  const hasScores = activeMatch.home_score !== null && activeMatch.away_score !== null;

  const toggleReveal = (e: React.MouseEvent) => {
    e.stopPropagation();
    setRevealedMatchIds((prev) => {
      const next = new Set(prev);
      if (next.has(activeMatch.id)) next.delete(activeMatch.id);
      else next.add(activeMatch.id);
      return next;
    });
  };

  return (
    <div className="relative w-full rounded-3xl overflow-hidden bg-[#0A0A0C] border border-white/10 shadow-2xl min-h-[380px] sm:min-h-[460px] md:min-h-[520px] flex flex-col justify-end p-6 sm:p-10 md:p-12 mb-8 group select-none">
      {/* Background Cinematic Thumbnail & Dual Scrim */}
      <div className="absolute inset-0 z-0">
        <Image
          src={thumbUrl}
          alt={`${homeName} vs ${awayName}`}
          fill
          priority
          className="object-cover object-center scale-105 group-hover:scale-100 transition-transform duration-700 opacity-60"
          unoptimized
        />
        {/* Pitch-Black Dual Vignette */}
        <div className="absolute inset-0 bg-gradient-to-t from-[#0A0A0C] via-[#0A0A0C]/70 to-transparent" />
        <div className="absolute inset-0 bg-gradient-to-r from-[#0A0A0C] via-[#0A0A0C]/80 to-transparent w-full md:w-3/4" />
      </div>

      {/* Hero Content Layer */}
      <div className="relative z-10 max-w-3xl space-y-4">
        {/* Top Tags: Competition & Excitement Flame */}
        <div className="flex flex-wrap items-center gap-2.5">
          <span className="px-3 py-1 rounded-full text-xs font-tech font-bold uppercase tracking-wider bg-black/80 text-white/90 border border-white/15 backdrop-blur-md">
            {activeMatch.competition_name || 'FEATURED MATCH'}
          </span>
          <div className="flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-tech font-black uppercase tracking-wider bg-[#DFFF00]/15 text-[#DFFF00] border border-[#DFFF00]/50 shadow-[0_0_15px_rgba(223,255,0,0.3)] backdrop-blur-md">
            <Flame className="w-3.5 h-3.5 fill-[#DFFF00]" />
            <span>9.8 HYPE SCORE</span>
          </div>
          {activeMatch.status === 'IN_PLAY' && (
            <div className="flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-tech font-black uppercase tracking-wider bg-[#FF2E4D]/20 text-[#FF2E4D] border border-[#FF2E4D]/60 animate-pulse">
              <span className="w-2 h-2 rounded-full bg-[#FF2E4D]" />
              <span>LIVE</span>
            </div>
          )}
        </div>

        {/* Stadium Scoreboard Showdown Title */}
        <div className="space-y-1">
          <div className="flex items-center gap-3">
            {activeMatch.home_team_crest && (
              <GlassLogoBadge src={activeMatch.home_team_crest} alt={homeName} size={36} />
            )}
            <h1 className="text-3xl sm:text-5xl md:text-6xl font-scoreboard uppercase tracking-wider text-white drop-shadow-md leading-none">
              {homeName} <span className="text-[#DFFF00]">VS</span> {awayName}
            </h1>
            {activeMatch.away_team_crest && (
              <GlassLogoBadge src={activeMatch.away_team_crest} alt={awayName} size={36} />
            )}
          </div>
        </div>

        {/* Score & Match Status Pill */}
        <div className="flex items-center gap-3 pt-1">
          {hasScores && (
            <div className="flex items-center gap-2 px-3.5 py-1.5 rounded-xl bg-[#121316] border border-white/10 font-tech">
              {isHidden ? (
                <button
                  onClick={toggleReveal}
                  className="flex items-center gap-1.5 text-xs font-bold text-[#DFFF00] hover:text-white transition-colors"
                >
                  <Eye className="w-3.5 h-3.5" />
                  <span>Reveal Score</span>
                </button>
              ) : (
                <span className="text-sm font-bold text-white tracking-wide">
                  FT: {activeMatch.home_score} - {activeMatch.away_score}
                </span>
              )}
            </div>
          )}
          <span className="text-xs text-white/60 font-tech">
            {new Date(activeMatch.utc_date).toLocaleDateString('en-GB', {
              weekday: 'short',
              day: 'numeric',
              month: 'short',
            })}
          </span>
        </div>

        {/* Action Triggers */}
        <div className="flex flex-wrap items-center gap-3 pt-2">
          <button
            onClick={() => onPlay(activeMatch)}
            className="flex items-center gap-2 px-6 py-3 rounded-xl bg-[#DFFF00] hover:bg-[#cbe600] text-[#0A0A0C] font-tech font-black text-sm uppercase tracking-wider shadow-[0_0_24px_rgba(223,255,0,0.4)] transition-all transform hover:scale-105 active:scale-95 cursor-pointer"
          >
            <Play className="w-4 h-4 fill-[#0A0A0C]" />
            <span>Watch Highlight</span>
          </button>
          <button
            onClick={() => onPlay(activeMatch)}
            className="flex items-center gap-2 px-5 py-3 rounded-xl bg-[#1A1C22]/80 hover:bg-[#1A1C22] text-white border border-white/15 font-tech font-bold text-sm tracking-wide transition-all cursor-pointer backdrop-blur-md"
          >
            <Activity className="w-4 h-4 text-[#38BDF8]" />
            <span>Match Centre</span>
          </button>
        </div>
      </div>

      {/* Multi-Match Carousel Switcher */}
      {topMatches.length > 1 && (
        <div className="absolute bottom-6 right-6 z-20 hidden sm:flex items-center gap-2">
          <button
            onClick={() => setActiveIndex((prev) => (prev > 0 ? prev - 1 : topMatches.length - 1))}
            className="p-2 rounded-full bg-black/60 hover:bg-[#1A1C22] text-white border border-white/15 transition-all"
            aria-label="Previous Match"
          >
            <ChevronLeft className="w-4 h-4" />
          </button>
          <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-black/70 border border-white/15 backdrop-blur-md">
            {topMatches.map((_, idx) => (
              <button
                key={idx}
                onClick={() => setActiveIndex(idx)}
                className={`h-1.5 rounded-full transition-all ${
                  idx === activeIndex ? 'w-6 bg-[#DFFF00]' : 'w-2 bg-white/30 hover:bg-white/60'
                }`}
                aria-label={`Match ${idx + 1}`}
              />
            ))}
          </div>
          <button
            onClick={() => setActiveIndex((prev) => (prev < topMatches.length - 1 ? prev + 1 : 0))}
            className="p-2 rounded-full bg-black/60 hover:bg-[#1A1C22] text-white border border-white/15 transition-all"
            aria-label="Next Match"
          >
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>
      )}
    </div>
  );
}

function HeroSkeleton() {
  return (
    <div className="w-full rounded-3xl min-h-[380px] sm:min-h-[460px] bg-[#121316] border border-white/10 animate-pulse p-8 flex flex-col justify-end space-y-4 mb-8">
      <div className="w-32 h-6 rounded-full bg-white/10" />
      <div className="w-2/3 h-12 rounded-xl bg-white/10" />
      <div className="w-48 h-8 rounded-lg bg-white/10" />
      <div className="flex gap-3 pt-2">
        <div className="w-40 h-12 rounded-xl bg-white/20" />
        <div className="w-32 h-12 rounded-xl bg-white/10" />
      </div>
    </div>
  );
}
