'use client';

import React, { useState } from 'react';
import Image from 'next/image';
import { ExternalLink, ShieldCheck, CheckCircle2, Play, EyeOff, Eye } from 'lucide-react';

interface MatchDetailVideoPlayerProps {
  videoId: string;
  playbackMode?: string | null;
  homeName: string;
  awayName: string;
  competitionName?: string;
  isSpoilerFree?: boolean;
  isScoreRevealed?: boolean;
  onRevealScore?: () => void;
}

export default function MatchDetailVideoPlayer({
  videoId,
  playbackMode,
  homeName,
  awayName,
  competitionName = 'OFFICIAL LEAGUE',
  isSpoilerFree = false,
  isScoreRevealed = false,
  onRevealScore,
}: MatchDetailVideoPlayerProps) {
  const [isPlaying, setIsPlaying] = useState(false);
  const isProtected = isSpoilerFree && !isScoreRevealed;
  const thumbnailUrl = `https://i.ytimg.com/vi/${videoId}/hqdefault.jpg`;

  return (
    <div className="space-y-3 relative group select-none">
      {/* Cinema Frame Container */}
      <div className="rounded-2xl overflow-hidden bg-black border border-white/10 shadow-[0_0_30px_rgba(0,0,0,0.8)] relative">
        {/* Top OTT Player Badges */}
        <div className="absolute top-3 left-3 right-3 flex items-center justify-between z-10 pointer-events-none">
          <div className="flex items-center gap-2">
            <span className="px-2.5 py-0.5 rounded bg-[#DFFF00] text-[#0A0A0C] font-tech text-[10px] uppercase tracking-wider font-black">
              {competitionName}
            </span>
            <span className="px-2.5 py-0.5 rounded bg-black/70 backdrop-blur-md text-white font-tech text-[10px] uppercase tracking-wider flex items-center gap-1 border border-white/10">
              <CheckCircle2 className="w-3 h-3 text-[#DFFF00]" />
              <span>Verified Broadcast</span>
            </span>
          </div>

          <div className="hidden sm:flex items-center gap-2">
            <span className="px-2 py-0.5 rounded bg-black/60 backdrop-blur-md text-white/70 font-tech text-[10px] border border-white/10">
              HD
            </span>
          </div>
        </div>

        {/* 16:9 Viewport */}
        <div className="relative w-full pt-[56.25%] bg-[#0A0A0C] overflow-hidden">
          {playbackMode === 'external' ? (
            <div className="absolute inset-0 flex flex-col items-center justify-center gap-3 p-6 text-center bg-[#0A0E14]">
              <p className="text-white text-sm font-medium">
                The rights holder requires this highlight to be watched directly on official YouTube channels.
              </p>
              <a
                href={`https://www.youtube.com/watch?v=${videoId}`}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-2 rounded-xl bg-red-600 hover:bg-red-500 px-5 py-2.5 font-bold text-white text-xs font-tech uppercase tracking-wider shadow-lg transition"
              >
                <ExternalLink className="w-4 h-4" />
                <span>Watch on YouTube</span>
              </a>
            </div>
          ) : isPlaying ? (
            <iframe
              className="absolute inset-0 w-full h-full"
              src={`https://www.youtube.com/embed/${videoId}?rel=0&modestbranding=1&autoplay=1&enablejsapi=1`}
              title={`${homeName} vs ${awayName} Highlights`}
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
              allowFullScreen
            />
          ) : (
            <div
              onClick={() => {
                if (!isProtected) setIsPlaying(true);
              }}
              className="absolute inset-0 w-full h-full cursor-pointer group/poster"
            >
              {/* Poster Thumbnail Image */}
              <div
                className={`w-full h-full transition-all duration-300 ${
                  isProtected ? 'filter blur-[22px] scale-105 opacity-60' : 'opacity-80 group-hover/poster:opacity-95'
                }`}
              >
                <Image
                  src={thumbnailUrl}
                  alt={`${homeName} vs ${awayName} Cover`}
                  fill
                  sizes="(max-width: 1024px) 100vw, 66vw"
                  className="object-cover"
                  unoptimized
                />
              </div>

              {/* Spoiler-Free Frosted Glass Shield Overlay */}
              {isProtected ? (
                <div
                  onClick={(e) => e.stopPropagation()}
                  className="absolute inset-0 z-20 flex flex-col items-center justify-center p-6 bg-black/55 backdrop-blur-[4px] text-center space-y-3"
                >
                  <div className="w-12 h-12 rounded-2xl bg-white/10 border border-white/20 flex items-center justify-center text-white shadow-xl">
                    <EyeOff className="w-6 h-6 text-[#DFFF00]" />
                  </div>
                  <div className="space-y-1">
                    <h3 className="text-sm font-bold text-white font-tech uppercase tracking-wide">
                      Spoiler Free Mode
                    </h3>
                    <p className="text-[11px] text-white/60 max-w-xs">
                      Thumbnail &amp; final score are shielded to protect match suspense.
                    </p>
                  </div>

                  <div className="flex flex-wrap items-center justify-center gap-2 pt-1">
                    <button
                      onClick={() => setIsPlaying(true)}
                      className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-[#DFFF00] hover:bg-[#cbe600] text-[#0A0A0C] font-tech text-xs font-black uppercase tracking-wider shadow-lg transition cursor-pointer"
                    >
                      <Play className="w-3.5 h-3.5 fill-current" />
                      <span>Play Highlights (Spoiler Free)</span>
                    </button>
                    {onRevealScore && (
                      <button
                        onClick={onRevealScore}
                        className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-white/10 hover:bg-white/20 text-white font-tech text-xs font-bold uppercase tracking-wider border border-white/15 transition cursor-pointer"
                      >
                        <Eye className="w-3.5 h-3.5" />
                        <span>Reveal Score</span>
                      </button>
                    )}
                  </div>
                </div>
              ) : (
                /* Normal Mode: Volt Play Button Overlay */
                <div className="absolute inset-0 z-20 flex flex-col items-center justify-center p-4 bg-gradient-to-t from-black/80 via-transparent to-black/40">
                  <div className="w-16 h-16 rounded-full bg-[#DFFF00] text-[#0A0A0C] shadow-[0_0_30px_rgba(223,255,0,0.45)] flex items-center justify-center transition-transform group-hover/poster:scale-110">
                    <Play className="w-7 h-7 fill-current ml-1" />
                  </div>
                  <span className="mt-3 text-xs font-tech font-extrabold uppercase tracking-wider text-white">
                    Watch Official Highlights
                  </span>
                  <span className="text-[10px] text-white/50 font-tech">Click to start playback</span>
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Attribution & Official Link Desk */}
      <div className="flex items-center justify-between gap-3 px-3.5 py-2 bg-[#121316] border border-white/10 rounded-xl text-xs font-tech">
        <div className="flex items-center gap-2 text-white/70">
          <ShieldCheck className="w-4 h-4 text-[#DFFF00]" />
          <span>Official Match Highlights • Verified Attribution</span>
        </div>

        <a
          href={`https://www.youtube.com/watch?v=${videoId}`}
          target="_blank"
          rel="noopener noreferrer"
          className="flex items-center gap-1.5 px-3 py-1 rounded-lg bg-red-600/15 hover:bg-red-600/25 text-red-400 border border-red-500/30 font-bold transition text-[11px]"
          title="Watch on Official YouTube"
        >
          <ExternalLink className="w-3 h-3" />
          <span>YouTube Link</span>
        </a>
      </div>
    </div>
  );
}
