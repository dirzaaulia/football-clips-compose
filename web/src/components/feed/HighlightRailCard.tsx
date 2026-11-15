'use client';

import React, { useState } from 'react';
import Image from 'next/image';
import { Play, Activity, Share2, Eye, EyeOff, Clock } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';

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

  const homeName = match.home_team_name || match.home_team;
  const awayName = match.away_team_name || match.away_team;
  const videoId = match.highlight_video_id;
  const thumbUrl = videoId
    ? `https://i.ytimg.com/vi/${videoId}/hqdefault.jpg`
    : '/favicon.png';

  const isHidden = isSpoilerFree && !isRevealed;
  const hasScores = match.home_score !== null && match.away_score !== null;

  const handleShare = (e: React.MouseEvent) => {
    e.stopPropagation();
    const url = typeof window !== 'undefined' ? `${window.location.origin}/match/${match.id}` : '';
    if (navigator.clipboard) {
      navigator.clipboard.writeText(url);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handleToggleReveal = (e: React.MouseEvent) => {
    e.stopPropagation();
    setIsRevealed((prev) => !prev);
  };

  return (
    <div
      onClick={() => onPlay(match)}
      className={`group relative w-[300px] sm:w-[340px] md:w-[380px] shrink-0 rounded-2xl bg-[#121316] border border-white/10 hover:border-[#DFFF00]/60 transition-all duration-200 hover:scale-105 hover:z-20 hover:shadow-[0_0_24px_rgba(223,255,0,0.25)] flex flex-col overflow-hidden cursor-pointer select-none ${className}`}
    >
      {/* 16:9 Cinema Thumbnail Container */}
      <div className="relative w-full pt-[56.25%] bg-[#0A0A0C] overflow-hidden">
        <Image
          src={thumbUrl}
          alt={`${homeName} vs ${awayName}`}
          fill
          className={`object-cover transition-transform duration-300 ${
            isHidden ? 'blur-md scale-105 opacity-70' : 'opacity-90 group-hover:scale-105'
          }`}
          unoptimized
        />

        {/* Dual Vignette Scrim */}
        <div className="absolute inset-0 bg-gradient-to-t from-[#121316] via-transparent to-black/50" />

        {/* Video Duration / Live Tag */}
        <div className="absolute top-2.5 left-2.5 flex items-center gap-1.5">
          {match.status === 'IN_PLAY' ? (
            <span className="flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-tech font-black uppercase tracking-wider bg-[#FF2E4D] text-white animate-pulse">
              <span className="w-1.5 h-1.5 rounded-full bg-white" />
              <span>LIVE</span>
            </span>
          ) : (
            <span className="flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-tech font-bold uppercase tracking-wider bg-black/80 text-white/90 backdrop-blur-md border border-white/10">
              <Clock className="w-3 h-3 text-[#38BDF8]" />
              <span>1080P HD</span>
            </span>
          )}
        </div>

        {/* Competition Pill */}
        <div className="absolute top-2.5 right-2.5 text-[10px] font-tech font-bold text-white/80 bg-black/80 px-2.5 py-0.5 rounded-full border border-white/10 backdrop-blur-md">
          {match.competition_name}
        </div>

        {/* Center High-Voltage Play Trigger */}
        <div className="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-200">
          <div className="w-12 h-12 rounded-full bg-[#DFFF00] flex items-center justify-center text-[#0A0A0C] shadow-[0_0_20px_rgba(223,255,0,0.6)] transform scale-90 group-hover:scale-100 transition-transform">
            <Play className="w-5 h-5 ml-0.5 fill-[#0A0A0C]" />
          </div>
        </div>

        {/* Bottom Quick-Action Bar on Hover */}
        <div className="absolute bottom-2.5 right-2.5 hidden group-hover:flex items-center gap-1.5 transition-all">
          <button
            onClick={handleShare}
            className="p-1.5 rounded-full bg-black/80 hover:bg-[#1A1C22] text-white border border-white/20 backdrop-blur-md text-xs font-tech transition-colors"
            title="Share match link"
          >
            <Share2 className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>

      {/* Card Body: Teams, Scores & Time */}
      <div className="p-3.5 flex flex-col justify-between flex-1 gap-2.5">
        <div className="flex items-center justify-between gap-2">
          {/* Home Team */}
          <div className="flex items-center gap-2 flex-1 min-w-0">
            {match.home_team_crest && (
              <GlassLogoBadge src={match.home_team_crest} alt={homeName} size={24} />
            )}
            <span className="font-extrabold text-xs text-white truncate">{homeName}</span>
          </div>

          {/* Stadium Scoreboard Pill with Frosted Blur Support */}
          <div className="shrink-0 px-2.5 py-1 rounded-lg bg-[#1A1C22] border border-white/10 font-tech text-center">
            {hasScores ? (
              isHidden ? (
                <button
                  onClick={handleToggleReveal}
                  className="flex items-center gap-1 text-[10px] font-black text-[#DFFF00] hover:text-white"
                  title="Click to reveal score"
                >
                  <Eye className="w-3 h-3" />
                  <span>Reveal</span>
                </button>
              ) : (
                <div className="flex items-center gap-1">
                  <span className="text-xs font-black text-white tracking-wide">
                    {match.home_score} - {match.away_score}
                  </span>
                  {isSpoilerFree && (
                    <button
                      onClick={handleToggleReveal}
                      className="text-white/40 hover:text-white ml-0.5"
                      title="Hide score"
                    >
                      <EyeOff className="w-2.5 h-2.5" />
                    </button>
                  )}
                </div>
              )
            ) : (
              <span className="text-[10px] font-bold text-white/50">VS</span>
            )}
          </div>

          {/* Away Team */}
          <div className="flex items-center justify-end gap-2 flex-1 min-w-0 text-right">
            <span className="font-extrabold text-xs text-white truncate">{awayName}</span>
            {match.away_team_crest && (
              <GlassLogoBadge src={match.away_team_crest} alt={awayName} size={24} />
            )}
          </div>
        </div>

        {/* Footer: Date & Kickoff */}
        <div className="flex items-center justify-between text-[10px] font-tech text-white/50 pt-2 border-t border-white/5">
          <span>
            {new Date(match.utc_date).toLocaleDateString('en-GB', {
              day: 'numeric',
              month: 'short',
            })}
          </span>
          {copied ? (
            <span className="text-[#DFFF00] font-bold">Link Copied!</span>
          ) : (
            <span className="text-[#38BDF8] flex items-center gap-1">
              <Activity className="w-3 h-3" />
              <span>Full Highlights</span>
            </span>
          )}
        </div>
      </div>
    </div>
  );
}
