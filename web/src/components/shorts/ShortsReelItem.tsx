'use client';

import React, { useState } from 'react';
import Image from 'next/image';
import { Heart } from 'lucide-react';
import { FootballShortRecord } from '@/lib/shorts';
import ShortsActionRail from './ShortsActionRail';
import ShortsBottomInfo from './ShortsBottomInfo';

interface ShortsReelItemProps {
  short: FootballShortRecord;
  isActive: boolean;
  isMuted: boolean;
  isLiked: boolean;
  onToggleLike: () => void;
  onToggleMute: () => void;
  onShare: () => void;
}

export default function ShortsReelItem({
  short,
  isActive,
  isMuted,
  isLiked,
  onToggleLike,
  onToggleMute,
  onShare,
}: ShortsReelItemProps) {
  const [showHeartBurst, setShowHeartBurst] = useState(false);

  const handleDoubleClick = () => {
    if (!isLiked) {
      onToggleLike();
    }
    setShowHeartBurst(true);
    setTimeout(() => setShowHeartBurst(false), 900);
  };

  return (
    <div className="h-full w-full flex items-center justify-center p-2 sm:p-4 snap-center">
      {/* Outer Chassis with Ambient Rim Glow */}
      <div
        onDoubleClick={handleDoubleClick}
        className="relative aspect-[9/16] h-full max-h-[820px] w-auto rounded-3xl overflow-hidden bg-black border border-white/10 shadow-[0_0_50px_rgba(0,0,0,0.8)] select-none shrink-0 group"
      >
        {/* Ambient Rim Lighting */}
        <div className="absolute -inset-0.5 bg-gradient-to-b from-[#7098F8]/30 via-transparent to-[#7098F8]/10 rounded-3xl pointer-events-none -z-10 blur-sm" />

        {/* Video Player or Poster Thumbnail */}
        {isActive ? (
          <iframe
            key={`${short.youtube_video_id}-${isMuted ? 'muted' : 'sound'}`}
            className="w-full h-full object-cover pointer-events-auto"
            src={`https://www.youtube.com/embed/${short.youtube_video_id}?autoplay=1&mute=${isMuted ? 1 : 0}&loop=1&playlist=${short.youtube_video_id}&controls=0&modestbranding=1&rel=0&playsinline=1&enablejsapi=1`}
            title={short.title}
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
          />
        ) : (
          <div className="relative w-full h-full bg-slate-950">
            <Image
              src={short.thumbnail_url}
              alt={short.title}
              fill
              className="object-cover"
              sizes="(max-width: 768px) 100vw, 420px"
              priority={false}
            />
            <div className="absolute inset-0 bg-black/40 backdrop-blur-[2px]" />
          </div>
        )}

        {/* Double Click Heart Burst Animation */}
        {showHeartBurst && (
          <div className="absolute inset-0 flex items-center justify-center pointer-events-none z-30 animate-ping">
            <Heart className="w-24 h-24 text-rose-500 fill-rose-500 drop-shadow-[0_0_20px_rgba(244,63,94,0.8)]" />
          </div>
        )}

        {/* Gradient Scrim & Overlays */}
        <div className="absolute inset-0 p-4 sm:p-5 flex flex-col justify-between pointer-events-none bg-gradient-to-b from-black/50 via-transparent to-black/90">
          {/* Top spacer */}
          <div className="h-8" />

          {/* Bottom Controls & Metadata */}
          <div className="flex items-end justify-between gap-3 pointer-events-auto">
            <ShortsBottomInfo
              channelName={short.channel_name}
              category={short.category}
              title={short.title}
            />

            <ShortsActionRail
              videoId={short.youtube_video_id}
              likeCount={short.like_count}
              isLiked={isLiked}
              isMuted={isMuted}
              onToggleLike={onToggleLike}
              onToggleMute={onToggleMute}
              onShare={onShare}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
