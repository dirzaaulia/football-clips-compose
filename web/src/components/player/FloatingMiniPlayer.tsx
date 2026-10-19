'use client';

import React, { useState } from 'react';
import Image from 'next/image';
import { Maximize2, X, Eye, EyeOff, Tv } from 'lucide-react';
import { useVideoPlayer } from '@/hooks/useVideoPlayer';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';
import { useUserRegion } from '@/hooks/useUserRegion';
import { isRegionSupported } from '@/lib/database';

export default function FloatingMiniPlayer() {
  const { activeMatch, isMinimized, restore, closePlayer } = useVideoPlayer();
  const { isSpoilerFree } = useSpoilerFree();
  const { country } = useUserRegion();
  const [isScoreRevealed, setIsScoreRevealed] = useState(false);

  if (!isMinimized || !activeMatch) {
    return null;
  }

  const isPrimaryAllowed = isRegionSupported(activeMatch.youtube_region_info, country);
  const isBackupAllowed = isRegionSupported(activeMatch.backup_youtube_region_info, country);

  const videoId = (!isPrimaryAllowed && isBackupAllowed && activeMatch.backup_highlight_video_id)
    ? activeMatch.backup_highlight_video_id
    : activeMatch.highlight_video_id;

  if (!videoId) {
    return null;
  }

  const homeName = activeMatch.home_team_name || activeMatch.home_team;
  const awayName = activeMatch.away_team_name || activeMatch.away_team;
  const hasScores = activeMatch.home_score !== null && activeMatch.away_score !== null;
  const isHidden = isSpoilerFree && !isScoreRevealed;

  return (
    <div
      role="dialog"
      aria-label="Floating Video Player"
      className="fixed bottom-4 right-4 sm:bottom-6 sm:right-6 z-50 w-[360px] sm:w-[384px] bg-[#101418] border border-[#7098F8]/50 rounded-2xl shadow-[0_12px_40px_rgba(0,0,0,0.85)] overflow-hidden animate-in fade-in slide-in-from-bottom-5 duration-300"
    >
      {/* Header Controls */}
      <div className="flex items-center justify-between px-3 py-2 bg-[#181C20] border-b border-[#334155]/60 text-xs">
        <div className="flex items-center gap-1.5 min-w-0 flex-1 mr-2">
          <Tv className="w-3.5 h-3.5 text-[#7098F8] shrink-0" />
          <span className="font-extrabold text-white truncate text-[11px]">
            {homeName} vs {awayName}
          </span>
        </div>

        <div className="flex items-center gap-1 shrink-0">
          <button
            onClick={restore}
            className="p-1 rounded-lg hover:bg-[#1E2227] text-[#94A3B8] hover:text-white transition-colors cursor-pointer"
            title="Expand to Full Player"
            aria-label="Expand to Full Player"
          >
            <Maximize2 className="w-3.5 h-3.5 text-[#7098F8]" />
          </button>
          <button
            onClick={closePlayer}
            className="p-1 rounded-lg hover:bg-[#1E2227] text-[#94A3B8] hover:text-white transition-colors cursor-pointer"
            title="Close Player"
            aria-label="Close Player"
          >
            <X className="w-3.5 h-3.5 text-[#94A3B8]" />
          </button>
        </div>
      </div>

      {/* 16:9 Video Box */}
      <div className="relative w-full pt-[56.25%] bg-black">
        <iframe
          className="absolute inset-0 w-full h-full"
          src={`https://www.youtube.com/embed/${videoId}?autoplay=1&rel=0&modestbranding=1`}
          title={`${homeName} vs ${awayName} Highlights`}
          allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
          allowFullScreen
        />
      </div>

      {/* Compact Mini Footer */}
      <div className="px-3 py-2 bg-[#101418] flex items-center justify-between text-[11px] border-t border-[#334155]/40">
        <span className="text-[#7098F8] font-bold uppercase tracking-wider truncate text-[10px]">
          {activeMatch.competition_name}
        </span>

        {hasScores && (
          <div className="flex items-center gap-1.5 shrink-0">
            {isHidden ? (
              <button
                onClick={() => setIsScoreRevealed(true)}
                className="flex items-center gap-1 px-2 py-0.5 rounded-full bg-[#181C20] border border-[#7098F8]/40 text-[#4FC3F7] font-bold text-[10px] hover:bg-[#1E2227] transition-all cursor-pointer"
              >
                <Eye className="w-3 h-3" />
                <span>Score</span>
              </button>
            ) : (
              <div
                onClick={() => isSpoilerFree && setIsScoreRevealed(false)}
                className="flex items-center gap-1 px-2 py-0.5 rounded-full bg-[#181C20] border border-[#334155] text-white font-extrabold text-[10px] cursor-pointer"
                title={isSpoilerFree ? 'Click to hide score' : undefined}
              >
                <span>
                  {activeMatch.home_score} - {activeMatch.away_score}
                </span>
                {isSpoilerFree && <EyeOff className="w-3 h-3 text-[#94A3B8] ml-0.5" />}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
