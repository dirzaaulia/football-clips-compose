'use client';

import React, { useState } from 'react';
import Image from 'next/image';
import { Play, Bookmark, Share2, Check } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';

interface HighlightRailCardProps {
  match: MatchRecord;
  onPlay: (match: MatchRecord) => void;
  className?: string;
}

export default function HighlightRailCard({
  match,
  onPlay,
  className = '',
}: HighlightRailCardProps) {
  const { isSpoilerFree } = useSpoilerFree();
  const [isRevealed, setIsRevealed] = useState(false);
  const [copied, setCopied] = useState(false);
  const [isBookmarked, setIsBookmarked] = useState(false);

  const homeName = match.home_team_name || match.home_team;
  const awayName = match.away_team_name || match.away_team;
  const videoId = match.highlight_video_id;
  const thumbUrl = videoId
    ? `https://i.ytimg.com/vi/${videoId}/hqdefault.jpg`
    : '/favicon.png';

  const isHidden = isSpoilerFree && !isRevealed;
  const homeCode = (match.home_team_name || match.home_team).slice(0, 3).toUpperCase();
  const awayCode = (match.away_team_name || match.away_team).slice(0, 3).toUpperCase();
  const hasScores = match.home_score !== null && match.away_score !== null;
  const scoreLabel = hasScores ? `${homeCode} ${match.home_score} - ${match.away_score} ${awayCode}` : `${homeCode} vs ${awayCode}`;

  const handleShare = (e: React.MouseEvent) => {
    e.stopPropagation();
    const url = typeof window !== 'undefined' ? `${window.location.origin}/match/${match.id}` : '';
    if (navigator.clipboard) {
      navigator.clipboard.writeText(url);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handleBookmark = (e: React.MouseEvent) => {
    e.stopPropagation();
    setIsBookmarked(!isBookmarked);
  };

  const handleToggleReveal = (e: React.MouseEvent) => {
    e.stopPropagation();
    setIsRevealed(!isRevealed);
  };

  return (
    <div
      onClick={() => onPlay(match)}
      className={`group flex flex-col w-[300px] sm:w-[340px] md:w-[360px] shrink-0 bg-[#121316] rounded-xl overflow-hidden transition-all duration-300 hover:shadow-[0_8px_24px_rgba(0,0,0,0.6)] hover:-translate-y-1 border border-white/5 hover:border-[#DFFF00]/40 cursor-pointer select-none ${className}`}
    >
      {/* 16:9 Thumbnail Area */}
      <div className="relative w-full aspect-video overflow-hidden bg-[#0A0A0C]">
        <Image
          src={thumbUrl}
          alt={`${homeName} vs ${awayName}`}
          fill
          className={`object-cover group-hover:scale-105 transition-transform duration-500 ${
            isHidden ? 'filter blur-md scale-105 opacity-70' : 'opacity-90'
          }`}
          unoptimized
        />

        <div className="absolute inset-0 bg-gradient-to-t from-[#0A0A0C] via-transparent to-transparent opacity-80" />

        {/* Top-left: Competition Pill */}
        <div className="absolute top-2 left-2 px-2 py-0.5 rounded bg-[#121316]/75 backdrop-blur-sm font-tech text-[10px] font-bold text-[#DFFF00] uppercase tracking-wider border border-white/10">
          {match.competition_name || match.competition_id || 'UCL'}
        </div>

        {/* Top-right: Duration / Quality */}
        <div className="absolute top-2 right-2 px-2 py-0.5 rounded bg-[#121316]/75 backdrop-blur-sm font-tech text-[10px] text-white font-mono border border-white/10">
          12:40
        </div>

        {/* Play Overlay */}
        <div className="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-200">
          <div className="w-12 h-12 rounded-full bg-[#DFFF00] flex items-center justify-center text-[#0A0A0C] shadow-[0_0_20px_rgba(223,255,0,0.6)] transform scale-90 group-hover:scale-100 transition-transform">
            <Play className="w-5 h-5 ml-0.5 fill-[#0A0A0C]" />
          </div>
        </div>

        {/* Bottom Bar: Quality tag & Score Pill */}
        <div className="absolute bottom-2 left-2 right-2 flex items-center justify-between">
          <span className="px-2 py-0.5 rounded bg-[#121316]/75 backdrop-blur-sm text-white font-tech text-[10px] uppercase font-bold border border-white/10">
            1080p60
          </span>

          <div
            onClick={handleToggleReveal}
            className={`px-2 py-0.5 rounded bg-[#121316]/80 backdrop-blur-sm text-[#DFFF00] font-tech text-xs font-bold transition-all border border-white/10 cursor-pointer ${
              isHidden ? 'filter blur-[3px] hover:blur-none' : ''
            }`}
            title={isHidden ? 'Click to reveal score' : undefined}
          >
            {scoreLabel}
          </div>
        </div>
      </div>

      {/* Card Content Body */}
      <div className="p-3 flex flex-col flex-1 justify-between gap-1.5">
        <div>
          <div className="flex items-center gap-1.5 text-white/50 font-tech text-[10px] uppercase mb-1">
            <span>{homeName} vs {awayName}</span>
            <span>•</span>
            <span>Highlights</span>
          </div>

          <h3 className="font-tech text-xs sm:text-sm font-bold text-white group-hover:text-[#DFFF00] transition-colors line-clamp-1">
            {`${homeName} vs ${awayName} Official Highlights & Decisive Goals`}
          </h3>
        </div>

        <div className="flex items-center justify-between text-white/40 font-tech text-[11px] pt-2 border-t border-white/5">
          <span>
            {new Date(match.utc_date).toLocaleDateString('en-GB', {
              day: 'numeric',
              month: 'short',
            })}
          </span>

          <div className="flex items-center gap-2">
            <button
              onClick={handleBookmark}
              className={`p-1 rounded hover:text-[#DFFF00] transition-colors cursor-pointer ${
                isBookmarked ? 'text-[#DFFF00]' : 'text-white/40'
              }`}
              title="Bookmark"
            >
              <Bookmark className="w-3.5 h-3.5 fill-current" />
            </button>
            <button
              onClick={handleShare}
              className="p-1 rounded hover:text-[#DFFF00] text-white/40 transition-colors cursor-pointer"
              title="Share match link"
            >
              {copied ? <Check className="w-3.5 h-3.5 text-[#DFFF00]" /> : <Share2 className="w-3.5 h-3.5" />}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
