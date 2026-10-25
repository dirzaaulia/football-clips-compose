'use client';

import React, { useState } from 'react';
import Image from 'next/image';
import { Flame, Play, Eye } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import { RankedMatch } from '@/lib/excitement';

interface ExcitementCardProps {
  item: RankedMatch;
  isSpoilerFree: boolean;
  onPlay: (match: MatchRecord) => void;
}

export default function ExcitementCard({
  item,
  isSpoilerFree,
  onPlay,
}: ExcitementCardProps) {
  const { match, tag } = item;
  const [isRevealed, setIsRevealed] = useState(false);
  const isHidden = isSpoilerFree && !isRevealed;

  const homeName = match.home_team_name || match.home_team;
  const awayName = match.away_team_name || match.away_team;
  const videoId = match.highlight_video_id;
  const thumbUrl = videoId
    ? `https://i.ytimg.com/vi/${videoId}/hqdefault.jpg`
    : '/favicon.png';
  const hasScores = match.home_score !== null && match.away_score !== null;

  return (
    <div
      onClick={() => onPlay(match)}
      className="group relative w-[310px] sm:w-[360px] h-auto min-h-[310px] sm:min-h-[330px] shrink-0 rounded-3xl bg-[#1E2227] border border-[#334155] hover:border-[#7098F8]/60 transition-all duration-300 overflow-hidden shadow-xl hover:shadow-[0_10px_30px_rgba(112,152,248,0.2)] cursor-pointer flex flex-col justify-between select-none"
    >
      {/* 16:9 Thumbnail Area */}
      <div className="relative w-full pt-[56.25%] bg-[#101418] overflow-hidden shrink-0">
        <Image
          src={thumbUrl}
          alt={`${homeName} vs ${awayName}`}
          fill
          className={`object-cover group-hover:scale-105 transition-all duration-500 ${
            isHidden ? 'blur-md scale-110 opacity-70' : 'opacity-90'
          }`}
          unoptimized
        />

        {/* Gradient Scrim */}
        <div className="absolute inset-0 bg-gradient-to-t from-[#1E2227] via-transparent to-black/40" />

        {/* Dynamic Excitement Badge */}
        <div className="absolute top-3 left-3 flex items-center gap-1.5 px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-wider bg-black/75 backdrop-blur-md text-[#7098F8] border border-[#7098F8]/40 shadow-md">
          <Flame className="w-3.5 h-3.5 text-amber-400 fill-amber-400" />
          <span>{tag}</span>
        </div>

        {/* Play Icon Overlay */}
        <div className="absolute inset-0 flex items-center justify-center opacity-85 group-hover:opacity-100 group-hover:scale-110 transition-all duration-300">
          <div className="w-12 h-12 rounded-full bg-[#1E3A8A]/90 backdrop-blur-sm border border-[#7098F8]/60 flex items-center justify-center text-white shadow-[0_0_20px_rgba(112,152,248,0.4)]">
            <Play className="w-5 h-5 ml-0.5 fill-white" />
          </div>
        </div>

        {/* Top-Right Competition Pill */}
        <div className="absolute top-3 right-3 text-[10px] font-extrabold text-white/90 bg-black/60 backdrop-blur-sm px-2.5 py-0.5 rounded-full border border-white/10">
          {match.competition_name}
        </div>
      </div>

      {/* Match Details Body */}
      <div className="p-3.5 sm:p-4 flex-1 flex flex-col justify-between">
        {/* Teams & Score Row */}
        <div className="flex items-center justify-between gap-2.5 py-1">
          {/* Home */}
          <div className="flex items-center gap-2 flex-1 min-w-0">
            {match.home_team_crest && (
              <div className="relative w-6 h-6 shrink-0">
                <Image src={match.home_team_crest} alt={homeName} fill className="object-contain" unoptimized />
              </div>
            )}
            <span className="font-extrabold text-xs text-white leading-tight">{homeName}</span>
          </div>

          {/* Scoreline Pill */}
          <div className="shrink-0 px-2.5 py-1 rounded-xl bg-[#101418] border border-[#334155] text-center">
            {hasScores ? (
              isHidden ? (
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    onPlay(match);
                  }}
                  className="flex items-center gap-1 text-[10px] font-black text-[#4FC3F7] hover:text-white"
                  title="Open Match Centre"
                >
                  <Eye className="w-3 h-3" />
                  <span>Score</span>
                </button>
              ) : (
                <span className="font-black text-xs text-white tracking-tight">
                  {match.home_score} - {match.away_score}
                </span>
              )
            ) : (
              <span className="font-bold text-[10px] text-[#94A3B8]">VS</span>
            )}
          </div>

          {/* Away */}
          <div className="flex items-center justify-end gap-2 flex-1 min-w-0 text-right">
            <span className="font-extrabold text-xs text-white leading-tight">{awayName}</span>
            {match.away_team_crest && (
              <div className="relative w-6 h-6 shrink-0">
                <Image src={match.away_team_crest} alt={awayName} fill className="object-contain" unoptimized />
              </div>
            )}
          </div>
        </div>

        {/* Footer info: Kickoff date */}
        <div className="flex items-center justify-between text-[11px] text-[#94A3B8] pt-2 border-t border-[#334155]/50">
          <span>
            {new Date(match.utc_date).toLocaleDateString('en-GB', {
              day: 'numeric',
              month: 'short',
              year: 'numeric',
            })}
          </span>
        </div>
      </div>
    </div>
  );
}
