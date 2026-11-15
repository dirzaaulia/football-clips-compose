'use client';

import React, { useState } from 'react';
import Image from 'next/image';
import { Flame, Play, Eye } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import { RankedMatch } from '@/lib/excitement';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';

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
      className="group relative w-[310px] sm:w-[360px] h-auto min-h-[310px] sm:min-h-[330px] shrink-0 rounded-3xl bg-[#121316] border border-white/10 hover:border-[#DFFF00]/50 transition-all duration-300 overflow-hidden shadow-2xl hover:shadow-[0_12px_36px_rgba(223,255,0,0.22)] cursor-pointer flex flex-col justify-between select-none"
    >
      {/* 16:9 Cinema Thumbnail Area */}
      <div className="relative w-full pt-[56.25%] bg-[#0A0A0C] overflow-hidden shrink-0">
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
        <div className="absolute inset-0 bg-gradient-to-t from-[#121316] via-transparent to-black/60" />

        {/* Dynamic Excitement Badge */}
        <div className="absolute top-3 left-3 flex items-center gap-1.5 px-3 py-1 rounded-full text-[10px] font-tech font-black uppercase tracking-wider bg-black/80 backdrop-blur-md text-[#DFFF00] border border-[#DFFF00]/40 shadow-lg">
          <Flame className="w-3.5 h-3.5 text-[#DFFF00] fill-[#DFFF00]" />
          <span>{tag}</span>
        </div>

        {/* High-Voltage Volt Play Icon Overlay */}
        <div className="absolute inset-0 flex items-center justify-center opacity-90 group-hover:opacity-100 group-hover:scale-110 transition-all duration-300">
          <div className="w-12 h-12 rounded-full bg-[#DFFF00] flex items-center justify-center text-[#0A0A0C] shadow-[0_0_24px_rgba(223,255,0,0.5)] border border-[#DFFF00]">
            <Play className="w-5 h-5 ml-0.5 fill-[#0A0A0C] text-[#0A0A0C]" />
          </div>
        </div>

        {/* Top-Right Competition Pill */}
        <div className="absolute top-3 right-3 text-[10px] font-tech font-bold text-[#E3E2E6] bg-black/70 backdrop-blur-md px-2.5 py-0.5 rounded-full border border-white/10">
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
              <GlassLogoBadge
                src={match.home_team_crest}
                alt={homeName}
                size={26}
                imageClassName="object-contain p-0.5"
              />
            )}
            <span className="font-extrabold text-xs text-white leading-tight break-words">{homeName}</span>
          </div>

          {/* Scoreline Pill */}
          <div className="shrink-0 px-2.5 py-1 rounded-xl bg-[#1A1C22] border border-white/10 text-center font-tech">
            {hasScores ? (
              isHidden ? (
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    onPlay(match);
                  }}
                  className="flex items-center gap-1 text-[10px] font-black text-[#DFFF00] hover:text-white"
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
              <span className="font-bold text-[10px] text-slate-400">VS</span>
            )}
          </div>

          {/* Away */}
          <div className="flex items-center justify-end gap-2 flex-1 min-w-0 text-right">
            <span className="font-extrabold text-xs text-white leading-tight break-words">{awayName}</span>
            {match.away_team_crest && (
              <GlassLogoBadge
                src={match.away_team_crest}
                alt={awayName}
                size={26}
                imageClassName="object-contain p-0.5"
              />
            )}
          </div>
        </div>

        {/* Footer info: Kickoff date */}
        <div className="flex items-center justify-between text-[11px] text-slate-400 pt-2 border-t border-white/10">
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
