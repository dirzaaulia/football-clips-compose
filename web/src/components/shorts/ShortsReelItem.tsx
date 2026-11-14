'use client';

import React, { useState, useEffect, useRef, useCallback } from 'react';
import Image from 'next/image';
import { Heart, Play, Pause } from 'lucide-react';
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
  const [isPlaying, setIsPlaying] = useState(true);
  const [pulseIndicator, setPulseIndicator] = useState<'play' | 'pause' | null>(null);
  const [isFrameLoaded, setIsFrameLoaded] = useState(false);

  const iframeRef = useRef<HTMLIFrameElement>(null);
  const clickTimerRef = useRef<NodeJS.Timeout | null>(null);
  const pulseTimerRef = useRef<NodeJS.Timeout | null>(null);

  // Send command to YouTube iframe via postMessage
  const postCommand = useCallback((func: string, args: unknown[] = []) => {
    iframeRef.current?.contentWindow?.postMessage(
      JSON.stringify({ event: 'command', func, args }),
      '*'
    );
  }, []);

  // Seamless volume sync without reloading content
  useEffect(() => {
    if (isActive && isFrameLoaded) {
      postCommand(isMuted ? 'mute' : 'unMute');
    }
  }, [isMuted, isActive, isFrameLoaded, postCommand]);

  // Reset playback and frame loaded status when becoming active/inactive
  useEffect(() => {
    if (isActive) {
      setIsPlaying(true);
      setIsFrameLoaded(false);
    } else {
      setIsPlaying(false);
      setIsFrameLoaded(false);
    }
  }, [isActive]);

  const togglePlayPause = () => {
    const nextState = !isPlaying;
    setIsPlaying(nextState);
    postCommand(nextState ? 'playVideo' : 'pauseVideo');
    setPulseIndicator(nextState ? 'play' : 'pause');
    if (pulseTimerRef.current) clearTimeout(pulseTimerRef.current);
    pulseTimerRef.current = setTimeout(() => setPulseIndicator(null), 450);
  };

  const handleCanvasClick = () => {
    if (clickTimerRef.current) {
      clearTimeout(clickTimerRef.current);
      clickTimerRef.current = null;
      if (!isLiked) onToggleLike();
      setShowHeartBurst(true);
      setTimeout(() => setShowHeartBurst(false), 850);
    } else {
      clickTimerRef.current = setTimeout(() => {
        clickTimerRef.current = null;
        togglePlayPause();
      }, 220);
    }
  };

  return (
    <div className="h-full w-full flex items-center justify-center p-2 sm:p-4 snap-center">
      {/* Outer Chassis */}
      <div className="relative aspect-[9/16] h-full max-h-[820px] w-auto rounded-3xl overflow-hidden bg-black border border-white/10 shadow-[0_0_50px_rgba(0,0,0,0.8)] select-none shrink-0 group">
        {/* Ambient Rim Lighting */}
        <div className="absolute -inset-0.5 bg-gradient-to-b from-[#7098F8]/30 via-transparent to-[#7098F8]/10 rounded-3xl pointer-events-none -z-10 blur-sm" />

        {/* Video Player or Poster Thumbnail */}
        {isActive ? (
          <div className="relative w-full h-full bg-black overflow-hidden">
            {/* Shimmer Poster placeholder while iframe initialises */}
            <div
              className={`absolute inset-0 z-0 transition-opacity duration-500 ${
                isFrameLoaded ? 'opacity-0 pointer-events-none' : 'opacity-100'
              }`}
            >
              <Image
                src={short.thumbnail_url}
                alt={short.title}
                fill
                className="object-cover"
                sizes="(max-width: 768px) 100vw, 420px"
                priority
              />
              <div className="absolute inset-0 bg-gradient-to-b from-black/30 via-transparent to-black/70 animate-pulse" />
            </div>

            <iframe
              ref={iframeRef}
              key={short.youtube_video_id}
              className="w-full h-full object-cover pointer-events-none relative z-10"
              src={`https://www.youtube.com/embed/${short.youtube_video_id}?autoplay=1&mute=${isMuted ? 1 : 0}&loop=1&playlist=${short.youtube_video_id}&controls=0&modestbranding=1&rel=0&playsinline=1&enablejsapi=1&iv_load_policy=3&disablekb=1&fs=0`}
              title={short.title}
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
              onLoad={() => setIsFrameLoaded(true)}
            />

            {/* Tap Interceptor Layer to bypass default YouTube UI chrome */}
            <div
              onClick={handleCanvasClick}
              className="absolute inset-0 z-20 cursor-pointer pointer-events-auto"
              title="Tap to toggle play/pause, double-tap to like"
            />
          </div>
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

        {/* Center Pulsing Play/Pause Indicator */}
        {pulseIndicator && (
          <div className="absolute inset-0 z-30 flex items-center justify-center pointer-events-none">
            <div className="p-5 rounded-full bg-black/60 backdrop-blur-md text-white border border-white/20 shadow-2xl animate-in zoom-in-75 duration-200">
              {pulseIndicator === 'play' ? (
                <Play className="w-10 h-10 fill-white text-white translate-x-0.5" />
              ) : (
                <Pause className="w-10 h-10 fill-white text-white" />
              )}
            </div>
          </div>
        )}

        {/* Double Click Heart Burst Animation */}
        {showHeartBurst && (
          <div className="absolute inset-0 z-30 flex items-center justify-center pointer-events-none animate-ping">
            <Heart className="w-24 h-24 text-rose-500 fill-rose-500 drop-shadow-[0_0_25px_rgba(244,63,94,0.9)]" />
          </div>
        )}

        {/* Gradient Scrim & Overlays */}
        <div className="absolute inset-0 z-30 p-4 sm:p-5 flex flex-col justify-between pointer-events-none bg-gradient-to-b from-black/40 via-transparent to-black/90">
          <div className="h-6" />

          {/* Bottom Metadata & Side Rail */}
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
