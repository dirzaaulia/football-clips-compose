'use client';

import React from 'react';
import { ExternalLink, ShieldCheck } from 'lucide-react';

interface MatchDetailVideoPlayerProps {
  videoId: string;
  playbackMode?: string | null;
  homeName: string;
  awayName: string;
}

export default function MatchDetailVideoPlayer({
  videoId,
  playbackMode,
  homeName,
  awayName,
}: MatchDetailVideoPlayerProps) {
  return (
    <div className="space-y-3 relative group">
      {/* Ambient Glow Backlight */}
      <div className="absolute -inset-1 bg-gradient-to-r from-[#7098F8]/20 via-blue-600/10 to-[#7098F8]/20 rounded-3xl blur-xl pointer-events-none -z-10 opacity-70 group-hover:opacity-100 transition-opacity duration-500" />

      {/* Glass Chassis Video Frame */}
      <div className="rounded-2xl overflow-hidden bg-black border border-white/10 shadow-[0_0_40px_rgba(0,0,0,0.8)]">
        <div className="relative w-full pt-[56.25%] bg-black">
          {playbackMode === 'external' ? (
            <div className="absolute inset-0 flex flex-col items-center justify-center gap-4 p-6 text-center bg-[#0A0E14]">
              <p className="text-white text-sm font-medium">
                The rights holder requires this highlight to be watched directly on official YouTube channels.
              </p>
              <a
                href={`https://www.youtube.com/watch?v=${videoId}`}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-2 rounded-xl bg-red-600 hover:bg-red-500 px-6 py-3 font-semibold text-white text-sm shadow-lg transition"
              >
                <ExternalLink className="w-4 h-4" />
                Watch on YouTube
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

      {/* Compliant External Control Pill Desk */}
      <div className="flex items-center justify-between gap-3 px-3 py-2 bg-[#181C20]/90 backdrop-blur-md border border-white/10 rounded-xl text-xs">
        <div className="flex items-center gap-2 text-emerald-400 font-medium">
          <ShieldCheck className="w-4 h-4 text-emerald-400" />
          <span>Official Match Highlight</span>
        </div>

        <a
          href={`https://www.youtube.com/watch?v=${videoId}`}
          target="_blank"
          rel="noopener noreferrer"
          className="flex items-center gap-1.5 px-3 py-1 rounded-lg bg-red-600/20 hover:bg-red-600/30 text-red-400 border border-red-500/30 font-semibold transition text-[11px]"
          title="Watch on Official YouTube"
        >
          <ExternalLink className="w-3.5 h-3.5" />
          <span>Watch on YouTube</span>
        </a>
      </div>
    </div>
  );
}
