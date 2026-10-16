'use client';

import React, { useState } from 'react';
import Image from 'next/image';
import { Play, Sparkles, Trophy } from 'lucide-react';
import { MatchRecord } from '@/lib/supabase';
import Link from 'next/link';
import SpoilerThumbnailBox from '../player/SpoilerThumbnailBox';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';

interface HighlightCardProps {
  match: MatchRecord;
  onPlay: (match: MatchRecord) => void;
}

export default function HighlightCard({ match, onPlay }: HighlightCardProps) {
  const { isSpoilerFree } = useSpoilerFree();
  const [isRevealed, setIsRevealed] = useState(false);

  const thumbnailUrl = match.highlight_video_id
    ? `https://i.ytimg.com/vi/${match.highlight_video_id}/hqdefault.jpg`
    : '/favicon.png';

  const shouldHideScore = isSpoilerFree && !isRevealed;
  const isFinished = match.status === 'FINISHED' || (!match.status && match.home_score !== null);
  const isLive = match.status === 'IN_PLAY' || match.status === 'PAUSED';

  const homeName = match.home_team_name || match.home_team;
  const awayName = match.away_team_name || match.away_team;

  return (
    <div className="group relative bg-[#1E2227] border border-[#334155] hover:border-[#7098F8]/60 rounded-2xl overflow-hidden transition-all duration-300 hover:shadow-[0_4px_24px_rgba(112,152,248,0.18)] hover:-translate-y-1 flex flex-col justify-between">
      {/* Thumbnail Header Area with Glass-Blur Spoiler Box */}
      <SpoilerThumbnailBox
        isSpoilerFree={isSpoilerFree}
        isRevealed={isRevealed}
        onRevealClick={() => setIsRevealed(true)}
        className="w-full aspect-video bg-[#101418] cursor-pointer"
      >
        <div className="relative w-full h-full" onClick={() => onPlay(match)}>
          <Image
            src={thumbnailUrl}
            alt={`${homeName} vs ${awayName} Highlights`}
            fill
            sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
            className="object-cover group-hover:scale-105 transition-transform duration-500 opacity-90"
            unoptimized
          />

          {/* Gradient Vignette Overlays matching Android */}
          <div className="absolute inset-0 bg-gradient-to-b from-black/60 via-transparent to-black/70 pointer-events-none" />

          {/* Top Header Pill Badges */}
          <div className="absolute top-2.5 inset-x-2.5 z-10 flex items-center justify-between pointer-events-none">
            <span className="flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-[#101418]/85 text-[#94A3B8] border border-[#334155] backdrop-blur-md">
              <Trophy className="w-2.5 h-2.5 text-[#7098F8]" />
              <span className="truncate max-w-[130px]">{match.competition_name}</span>
            </span>

            {isLive ? (
              <span className="px-2 py-0.5 rounded-full text-[9px] font-black bg-rose-500/90 text-white animate-pulse">
                LIVE
              </span>
            ) : (
              <span className="px-2 py-0.5 rounded-full text-[9px] font-bold bg-[#101418]/85 text-[#4FC3F7] border border-[#334155] backdrop-blur-md">
                HD
              </span>
            )}
          </div>

          {/* Center Glowing Play Action (hidden after reveal for clean view) */}
          {!isRevealed && (
            <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
              <div className="w-12 h-12 rounded-full bg-gradient-to-r from-[#7098F8] to-[#4FC3F7] text-[#101418] flex items-center justify-center shadow-[0_0_20px_rgba(112,152,248,0.4)] group-hover:scale-110 transition-transform">
                <Play className="w-5 h-5 fill-current ml-0.5" />
              </div>
            </div>
          )}
        </div>
      </SpoilerThumbnailBox>

      {/* Match Details Section (Android ModernHighlightCard layout with NO ELLIPSIS) */}
      <div className="p-3.5 flex-1 flex flex-col justify-between">
        {/* Teams & Score Row */}
        <div className="flex items-center justify-between gap-2.5 my-1">
          {/* Home Team (No ellipsis, soft-wrapped naturally) */}
          <div className="flex items-center gap-2 flex-1 min-w-0">
            {match.home_team_crest ? (
              <div className="relative w-6 h-6 shrink-0">
                <Image
                  src={match.home_team_crest}
                  alt={homeName}
                  fill
                  className="object-contain"
                  unoptimized
                />
              </div>
            ) : (
              <div className="w-6 h-6 rounded-full bg-[#181C20] border border-[#334155] text-[10px] font-bold text-[#7098F8] flex items-center justify-center shrink-0">
                {homeName.charAt(0)}
              </div>
            )}
            <span className="font-extrabold text-xs text-[#F1F5F9] leading-tight break-words group-hover:text-[#7098F8] transition-colors">
              {homeName}
            </span>
          </div>

          {/* Center Score Pill */}
          <div className="px-2.5 py-1 rounded-xl bg-[#181C20] border border-[#334155] text-center shrink-0 min-w-[55px]">
            {shouldHideScore ? (
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  setIsRevealed(true);
                }}
                className="font-black text-xs text-[#94A3B8] hover:text-[#4FC3F7] transition-colors cursor-pointer"
                title="Click to reveal score"
              >
                VS
              </button>
            ) : (
              <span className="font-black text-xs sm:text-sm text-white tracking-wide">
                {isFinished || isLive ? `${match.home_score ?? 0} - ${match.away_score ?? 0}` : 'VS'}
              </span>
            )}
          </div>

          {/* Away Team (No ellipsis, soft-wrapped naturally) */}
          <div className="flex items-center justify-end gap-2 flex-1 min-w-0 text-right">
            <span className="font-extrabold text-xs text-[#F1F5F9] leading-tight break-words group-hover:text-[#7098F8] transition-colors">
              {awayName}
            </span>
            {match.away_team_crest ? (
              <div className="relative w-6 h-6 shrink-0">
                <Image
                  src={match.away_team_crest}
                  alt={awayName}
                  fill
                  className="object-contain"
                  unoptimized
                />
              </div>
            ) : (
              <div className="w-6 h-6 rounded-full bg-[#181C20] border border-[#334155] text-[10px] font-bold text-[#7098F8] flex items-center justify-center shrink-0">
                {awayName.charAt(0)}
              </div>
            )}
          </div>
        </div>

        {/* Footer Meta Action Row */}
        <div className="pt-3 mt-2 border-t border-[#334155]/50 flex items-center justify-between text-xs">
          <span className="text-[11px] text-[#94A3B8]">
            {new Date(match.utc_date).toLocaleDateString('en-GB', { day: 'numeric', month: 'short' })}
          </span>

          <div className="flex items-center gap-3">
            <button
              onClick={() => onPlay(match)}
              className="text-[#7098F8] font-bold hover:underline cursor-pointer text-xs"
            >
              Watch
            </button>
            {match.id && !String(match.id).startsWith('mw-') ? (
              <Link
                href={`/match/${match.id}`}
                className="text-[#94A3B8] hover:text-white flex items-center gap-1 transition-colors text-[11px] font-medium"
              >
                <Sparkles className="w-3 h-3 text-[#4FC3F7]" />
                <span>Match Centre</span>
              </Link>
            ) : null}
          </div>
        </div>
      </div>
    </div>
  );
}
