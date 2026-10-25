'use client';

import React, { useState } from 'react';
import Image from 'next/image';
import { Play, Sparkles, Trophy, Flame, EyeOff } from 'lucide-react';
import Link from 'next/link';
import { MatchRecord } from '@/lib/database';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';
import AdSenseBanner from '../ads/AdSenseBanner';
import HeroCarouselSkeleton from './HeroCarouselSkeleton';

interface HeroCarouselProps {
  featuredMatch: MatchRecord | null;
  trendingMatches: MatchRecord[];
  onPlay: (match: MatchRecord) => void;
  isLoading?: boolean;
}

function TrendingItemCard({
  match,
  isSpoilerFree,
  onPlay,
}: {
  match: MatchRecord;
  isSpoilerFree: boolean;
  onPlay: (match: MatchRecord) => void;
}) {
  const [isRevealed, setIsRevealed] = useState(false);
  const isHidden = isSpoilerFree && !isRevealed;

  const miniThumb = match.highlight_video_id
    ? `https://i.ytimg.com/vi/${match.highlight_video_id}/hqdefault.jpg`
    : '/favicon.png';
  const homeName = match.home_team_name || match.home_team;
  const awayName = match.away_team_name || match.away_team;
  const hasScores = match.home_score !== null && match.away_score !== null;

  return (
    <div
      onClick={() => onPlay(match)}
      className="group flex items-center gap-3 p-2.5 rounded-2xl bg-[#1E2227] border border-[#334155] hover:border-[#7098F8]/60 cursor-pointer transition-all hover:bg-[#1E2227]/90 hover:shadow-[0_0_16px_rgba(112,152,248,0.12)]"
    >
      {/* Mini Thumbnail with Spoiler Blur (NO play icon button) */}
      <div className="relative w-[110px] sm:w-[120px] h-[64px] sm:h-[68px] rounded-xl overflow-hidden shrink-0 bg-[#101418]">
        <Image
          src={miniThumb}
          alt={`${homeName} vs ${awayName}`}
          width={120}
          height={68}
          className={`object-cover w-full h-full group-hover:scale-105 transition-all duration-500 ${
            isHidden ? 'blur-md scale-110 opacity-70' : 'opacity-90'
          }`}
          unoptimized
        />
        {isHidden && (
          <div
            onClick={(e) => {
              e.stopPropagation();
              setIsRevealed(true);
            }}
            className="absolute inset-0 bg-black/45 backdrop-blur-[2px] flex items-center justify-center p-1"
          >
            <span className="text-[9px] font-black tracking-wider uppercase px-2 py-0.5 rounded-full bg-black/70 text-[#4FC3F7] border border-[#7098F8]/40 shadow-sm">
              Spoiler Free
            </span>
          </div>
        )}
      </div>

      {/* Info Details (NO floating VS label) */}
      <div className="flex-1 min-w-0 pr-1">
        <span className="text-[10px] font-bold text-[#7098F8] uppercase tracking-wider truncate block">
          {match.competition_name}
        </span>
        <h3 className="text-xs font-bold text-white group-hover:text-[#7098F8] transition-colors leading-snug break-words line-clamp-2">
          {homeName} vs {awayName}
        </h3>
        <div className="flex items-center gap-2 mt-1 text-[11px] text-[#94A3B8]">
          {!isHidden && hasScores && (
            <>
              <span className="font-extrabold text-white">
                {match.home_score} - {match.away_score}
              </span>
              <span>&bull;</span>
            </>
          )}
          <span className="text-[10px]">
            {new Date(match.utc_date).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })}
          </span>
        </div>
      </div>
    </div>
  );
}

export default function HeroCarousel({
  featuredMatch,
  trendingMatches = [],
  onPlay,
  isLoading = false,
}: HeroCarouselProps) {
  const { isSpoilerFree } = useSpoilerFree();
  const [isRevealed, setIsRevealed] = useState(false);

  if (isLoading || !featuredMatch) {
    if (isLoading) return <HeroCarouselSkeleton />;
    return null;
  }

  const isScoreHidden = isSpoilerFree && !isRevealed;

  const thumbnailUrl = featuredMatch.highlight_video_id
    ? `https://i.ytimg.com/vi/${featuredMatch.highlight_video_id}/hqdefault.jpg`
    : '/favicon.png';

  const homeName = featuredMatch.home_team_name || featuredMatch.home_team;
  const awayName = featuredMatch.away_team_name || featuredMatch.away_team;

  return (
    <section className="relative w-full my-4">
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-4 items-stretch">
        {/* Main Featured Highlight */}
        <div className="lg:col-span-7 relative rounded-3xl overflow-hidden bg-[#1E2227] border border-[#334155] shadow-[0_4px_24px_rgba(0,0,0,0.4)] group flex flex-col justify-end min-h-[340px] sm:min-h-[380px] p-5 sm:p-7">
          {/* Background Image with Dark Vignette & Spoiler Blur */}
          <div className="absolute inset-0 z-0 overflow-hidden">
            <Image
              src={thumbnailUrl}
              alt={`${homeName} vs ${awayName}`}
              fill
              priority
              className={`object-cover group-hover:scale-105 transition-all duration-700 opacity-55 ${
                isScoreHidden ? 'blur-xl scale-110' : ''
              }`}
              unoptimized
            />
            <div className="absolute inset-0 bg-gradient-to-t from-[#101418] via-[#101418]/65 to-transparent" />
          </div>

          {/* Spoiler Free Glass Pill in Hero (if hidden) */}
          {isScoreHidden && (
            <div className="absolute top-4 right-4 z-20">
              <button
                onClick={() => setIsRevealed(true)}
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-black/60 hover:bg-black/80 border border-[#7098F8]/40 text-[#4FC3F7] hover:text-white text-xs font-bold backdrop-blur-md transition-all cursor-pointer shadow-lg"
              >
                <EyeOff className="w-3.5 h-3.5" />
                <span>Spoiler Free &bull; Reveal</span>
              </button>
            </div>
          )}

          {/* Overlay Content */}
          <div className="relative z-10 space-y-3">
            {/* Top Pill Badges */}
            <div className="flex flex-wrap items-center gap-2">
              <span className="flex items-center gap-1.5 px-3 py-1 rounded-full text-[10px] font-black bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white border border-[#7098F8]/40 shadow-sm">
                <Trophy className="w-3 h-3 text-[#4FC3F7]" />
                <span className="tracking-wider uppercase">FEATURED MATCH</span>
              </span>
              <span className="px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-[#101418]/80 text-[#94A3B8] border border-[#334155] backdrop-blur-md">
                {featuredMatch.competition_name}
              </span>
            </div>

            {/* Headline Teams & Score */}
            <div>
              <h1 className="text-xl sm:text-3xl font-black text-white tracking-tight leading-snug">
                {homeName}{' '}
                {isScoreHidden ? (
                  <button
                    onClick={() => setIsRevealed(true)}
                    className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-lg bg-[#181C20]/90 border border-[#7098F8]/40 text-xs font-bold text-[#7098F8] hover:text-white cursor-pointer transition-colors align-middle shadow-sm"
                    title="Click to reveal score"
                  >
                    <span>VS</span>
                  </button>
                ) : (
                  <span className="text-[#7098F8]">
                    {featuredMatch.home_score ?? 0} - {featuredMatch.away_score ?? 0}
                  </span>
                )}{' '}
                {awayName}
              </h1>
              <p className="text-xs text-[#94A3B8] line-clamp-2 mt-1 max-w-xl">
                Official high-definition European matchday highlights and tactical report.
              </p>
            </div>

            {/* Action Buttons */}
            <div className="flex flex-wrap items-center gap-2.5 pt-1">
              <button
                onClick={() => onPlay(featuredMatch)}
                className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-gradient-to-r from-[#7098F8] to-[#4FC3F7] text-[#101418] font-bold text-xs shadow-[0_0_16px_rgba(112,152,248,0.35)] hover:brightness-110 transition-all cursor-pointer"
              >
                <Play className="w-3.5 h-3.5 fill-current ml-0.5" />
                <span>Watch Highlights</span>
              </button>

              {featuredMatch.id && !String(featuredMatch.id).startsWith('mw-') && (
                <button
                  onClick={() => onPlay(featuredMatch)}
                  className="flex items-center gap-1.5 px-4 py-2.5 rounded-xl bg-[#181C20]/90 hover:bg-[#1E2227] text-white font-semibold text-xs border border-[#334155] hover:border-[#7098F8]/50 transition-all cursor-pointer"
                >
                  <Sparkles className="w-3.5 h-3.5 text-[#4FC3F7]" />
                  <span>Match Centre</span>
                </button>
              )}
            </div>
          </div>
        </div>

        {/* Right Column: Trending Items + Compact Sponsor Banner */}
        <div className="lg:col-span-5 flex flex-col justify-between gap-3">
          <div className="flex items-center justify-between px-1">
            <div className="flex items-center gap-1.5">
              <Flame className="w-3.5 h-3.5 text-[#7098F8]" />
              <span className="text-xs font-bold text-white uppercase tracking-wider">Trending Highlights</span>
            </div>
            <span className="text-[10px] text-[#94A3B8]">Quick Play</span>
          </div>

          {/* Quick Highlight Cards */}
          <div className="flex flex-col gap-2.5 flex-1">
            {trendingMatches.slice(0, 2).map((match) => (
              <TrendingItemCard
                key={match.id}
                match={match}
                isSpoilerFree={isSpoilerFree}
                onPlay={onPlay}
              />
            ))}
          </div>

          <div className="mt-1">
            <AdSenseBanner slotId="5616816386" format="horizontal" className="my-0 py-2" />
          </div>
        </div>
      </div>
    </section>
  );
}
