'use client';

import React, { useState, useMemo } from 'react';
import Image from 'next/image';
import { useRouter } from 'next/navigation';
import { Play, Bookmark, BookmarkCheck, Share2, Eye, EyeOff } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';

interface HighlightGridCardProps {
  match: MatchRecord;
  onPlay?: (match: MatchRecord) => void;
  className?: string;
}

function formatMatchKickoff(utcDate?: string): string {
  if (!utcDate) return 'FT';
  try {
    const dt = new Date(utcDate);
    const now = new Date();
    const isToday = dt.toDateString() === now.toDateString();
    const timeStr = dt.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    if (isToday) return timeStr;
    const dateStr = dt.toLocaleDateString([], { month: 'short', day: 'numeric' });
    return `${dateStr} • ${timeStr}`;
  } catch {
    return 'FT';
  }
}

export default function HighlightGridCard({
  match,
  className = '',
}: HighlightGridCardProps) {
  const router = useRouter();
  const { isSpoilerFree } = useSpoilerFree();
  const [isRevealed, setIsRevealed] = useState(false);
  const [isBookmarked, setIsBookmarked] = useState(false);
  const [copied, setCopied] = useState(false);
  const [imageLoaded, setImageLoaded] = useState(false);

  const homeName = match.home_team_name || match.home_team || 'Home';
  const awayName = match.away_team_name || match.away_team || 'Away';
  const videoId = match.highlight_video_id || match.external_video_id;
  const thumbUrl = videoId ? `https://i.ytimg.com/vi/${videoId}/hqdefault.jpg` : null;

  const isHidden = isSpoilerFree && !isRevealed;
  const hasScores = match.home_score !== null && match.away_score !== null;
  const kickoffFormatted = useMemo(() => formatMatchKickoff(match.utc_date), [match.utc_date]);

  const handleCardClick = () => {
    router.push(`/match/${match.id}`);
  };

  const handleToggleReveal = (e: React.MouseEvent) => {
    e.stopPropagation();
    setIsRevealed((prev) => !prev);
  };

  const handleBookmark = (e: React.MouseEvent) => {
    e.stopPropagation();
    setIsBookmarked((prev) => !prev);
  };

  const handleShare = (e: React.MouseEvent) => {
    e.stopPropagation();
    const url = typeof window !== 'undefined' ? `${window.location.origin}/match/${match.id}` : '';
    if (navigator.clipboard) {
      navigator.clipboard.writeText(url);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <div
      onClick={handleCardClick}
      className={`group flex flex-col bg-[#121316] rounded-2xl overflow-hidden border border-white/10 hover:border-[#DFFF00]/40 transition-all duration-300 hover:shadow-[0_8px_24px_rgba(0,0,0,0.6)] hover:-translate-y-1 cursor-pointer select-none ${className}`}
    >
      {/* 16:9 Media Aspect Container */}
      <div className="relative w-full aspect-video overflow-hidden bg-[#0A0A0C]">
        {!imageLoaded && (
          <div className="absolute inset-0 bg-gradient-to-r from-white/5 via-white/10 to-white/5 animate-pulse" />
        )}

        {thumbUrl ? (
          <Image
            src={thumbUrl}
            alt={`${homeName} vs ${awayName}`}
            fill
            sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 25vw"
            onLoad={() => setImageLoaded(true)}
            className={`object-cover group-hover:scale-105 transition-all duration-500 ${
              isHidden ? 'blur-xl scale-110 opacity-50' : 'opacity-90'
            }`}
            unoptimized
          />
        ) : (
          <div className="absolute inset-0 bg-gradient-to-br from-[#121316] via-[#1A1C22] to-[#0A0A0C] flex items-center justify-center">
            <div className="w-12 h-12 rounded-full bg-white/5 border border-white/10 flex items-center justify-center">
              <Play className="w-5 h-5 text-[#DFFF00]" />
            </div>
          </div>
        )}

        {/* Dual Vignette Scrim */}
        <div className="absolute inset-0 bg-gradient-to-t from-[#121316] via-transparent to-black/40" />

        {/* Top-Left: Competition Badge */}
        <div className="absolute top-2.5 left-2.5 px-2.5 py-0.5 rounded-full bg-black/80 backdrop-blur-md font-tech text-[10px] text-[#DFFF00] uppercase tracking-wider font-mono border border-white/10">
          {match.competition_name || 'UCL'}
        </div>

        {/* Top-Right: Localized Kickoff Time / Live Badge */}
        <div className="absolute top-2.5 right-2.5 px-2.5 py-0.5 rounded-full bg-black/80 backdrop-blur-md font-tech text-[10px] text-white/90 font-mono border border-white/10">
          {match.status === 'IN_PLAY' ? 'LIVE' : kickoffFormatted}
        </div>

        {/* Center: High Voltage Kinetic Play Glyph */}
        <div className="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-200">
          <div className="w-12 h-12 rounded-full bg-[#DFFF00] flex items-center justify-center text-[#0A0A0C] shadow-[0_0_20px_rgba(223,255,0,0.7)] transform scale-90 group-hover:scale-100 transition-transform">
            <Play className="w-5 h-5 ml-0.5 fill-[#0A0A0C]" />
          </div>
        </div>

        {/* Bottom Bar: Frosted Glass Score Pill */}
        {hasScores && (
          <div className="absolute bottom-2.5 right-2.5">
            <div
              onClick={handleToggleReveal}
              className={`px-3 py-1 rounded-full font-tech text-xs font-bold transition-all cursor-pointer backdrop-blur-md ${
                isHidden
                  ? 'bg-black/90 text-[#DFFF00] filter blur-sm hover:blur-none border border-[#DFFF00]/40'
                  : 'bg-black/80 text-[#DFFF00] border border-white/10'
              }`}
              title={isHidden ? 'Click to reveal score' : 'Click to hide score'}
            >
              <div className="flex items-center gap-1.5">
                <span>
                  {match.home_score} - {match.away_score}
                </span>
                {isHidden ? (
                  <Eye className="w-3.5 h-3.5 text-[#DFFF00]" />
                ) : (
                  <EyeOff className="w-3 h-3 text-white/40 ml-0.5" />
                )}
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Card Info Body */}
      <div className="p-3.5 flex flex-col flex-1 justify-between gap-2">
        <div className="space-y-1.5">
          <div className="flex items-center gap-2">
            {match.home_team_crest ? (
              <div className="relative w-4 h-4 shrink-0">
                <Image
                  src={match.home_team_crest}
                  alt={homeName}
                  fill
                  className="object-contain"
                  unoptimized
                />
              </div>
            ) : null}
            <span className="font-bold text-xs sm:text-sm text-white group-hover:text-[#DFFF00] transition-colors leading-tight">
              {homeName}
            </span>
          </div>

          <div className="flex items-center gap-2">
            {match.away_team_crest ? (
              <div className="relative w-4 h-4 shrink-0">
                <Image
                  src={match.away_team_crest}
                  alt={awayName}
                  fill
                  className="object-contain"
                  unoptimized
                />
              </div>
            ) : null}
            <span className="font-bold text-xs sm:text-sm text-white group-hover:text-[#DFFF00] transition-colors leading-tight">
              {awayName}
            </span>
          </div>
        </div>

        {match.recap_analysis && (
          <p className="text-[11px] font-tech text-white/60 line-clamp-2 leading-relaxed">
            {match.recap_analysis}
          </p>
        )}

        {/* Card Footer: Match Centre Link & Actions */}
        <div className="flex items-center justify-between text-white/50 font-tech text-xs pt-2 border-t border-white/5">
          <span className="text-[#DFFF00] font-bold group-hover:underline">
            Match Centre &rarr;
          </span>

          <div className="flex items-center gap-2">
            {copied ? (
              <span className="text-[10px] text-[#DFFF00] font-bold">Copied!</span>
            ) : null}

            <button
              onClick={handleBookmark}
              className="p-1 hover:text-[#DFFF00] transition-colors cursor-pointer"
              title={isBookmarked ? 'Remove Bookmark' : 'Bookmark Match'}
            >
              {isBookmarked ? (
                <BookmarkCheck className="w-4 h-4 text-[#DFFF00]" />
              ) : (
                <Bookmark className="w-4 h-4" />
              )}
            </button>

            <button
              onClick={handleShare}
              className="p-1 hover:text-[#DFFF00] transition-colors cursor-pointer"
              title="Share Highlight"
            >
              <Share2 className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
