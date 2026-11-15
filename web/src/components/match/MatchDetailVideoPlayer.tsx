'use client';

import React from 'react';
import { ExternalLink, ShieldCheck, CheckCircle2 } from 'lucide-react';

interface MatchDetailVideoPlayerProps {
  videoId: string;
  playbackMode?: string | null;
  homeName: string;
  awayName: string;
  competitionName?: string;
}

export default function MatchDetailVideoPlayer({
  videoId,
  playbackMode,
  homeName,
  awayName,
  competitionName = 'OFFICIAL LEAGUE',
}: MatchDetailVideoPlayerProps) {
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
        <div className="relative w-full pt-[56.25%] bg-[#0A0A0C]">
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
          ) : (
            <iframe
              className="absolute inset-0 w-full h-full"
              src={`https://www.youtube.com/embed/${videoId}?rel=0&modestbranding=1&autoplay=0&enablejsapi=1`}
              title={`${homeName} vs ${awayName} Highlights`}
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
              allowFullScreen
            />
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
