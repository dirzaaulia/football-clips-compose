'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { ChevronUp, ChevronDown, Heart, Share2, ExternalLink, Play, Pause, Volume2, VolumeX, Sparkles } from 'lucide-react';

interface WebShort {
  id: string;
  youtubeVideoId: string;
  title: string;
  channelName: string;
  category: string;
  likeCount: number;
  publishedAt: string;
}

const MOCK_SHORTS: WebShort[] = [
  {
    id: 's1',
    youtubeVideoId: 'u31qwQUeGuM',
    title: 'Erling Haaland stunning training bicycle kick! 🚲',
    channelName: 'Manchester City',
    category: 'GOALS_SKILLS',
    likeCount: 24500,
    publishedAt: 'Today',
  },
  {
    id: 's2',
    youtubeVideoId: 'kxy6_H8Xf0Y',
    title: 'Bukayo Saka insane nutmeg vs Bayern Munich 🥶',
    channelName: 'Arsenal',
    category: 'GOALS_SKILLS',
    likeCount: 18900,
    publishedAt: 'Yesterday',
  },
  {
    id: 's3',
    youtubeVideoId: '8hYlB38asDY',
    title: 'Cole Palmer ice cold penalty technique under pressure 🧊',
    channelName: 'Chelsea FC',
    category: 'MOMENTS',
    likeCount: 31200,
    publishedAt: '2 days ago',
  },
];

export default function ShortsPage() {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isPlaying, setIsPlaying] = useState(true);
  const [isMuted, setIsMuted] = useState(false);
  const [likedMap, setLikedMap] = useState<Record<string, boolean>>({});

  const currentShort = MOCK_SHORTS[currentIndex];

  const handleNext = () => {
    if (currentIndex < MOCK_SHORTS.length - 1) {
      setCurrentIndex((prev) => prev + 1);
    }
  };

  const handlePrev = () => {
    if (currentIndex > 0) {
      setCurrentIndex((prev) => prev - 1);
    }
  };

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'ArrowDown') handleNext();
      if (e.key === 'ArrowUp') handlePrev();
      if (e.key === ' ') {
        e.preventDefault();
        setIsPlaying((prev) => !prev);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [currentIndex]);

  const toggleLike = (id: string) => {
    setLikedMap((prev) => ({ ...prev, [id]: !prev[id] }));
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col items-center justify-center p-4">
      <div className="w-full max-w-5xl h-[85vh] flex flex-col md:flex-row gap-6 items-center justify-center">
        {/* Left Column: 9:16 Vertical Video Frame */}
        <div className="relative aspect-[9/16] h-full max-h-[750px] w-auto rounded-3xl overflow-hidden bg-black border border-slate-800 shadow-2xl shrink-0">
          <iframe
            key={currentShort.youtubeVideoId}
            className="w-full h-full object-cover"
            src={`https://www.youtube.com/embed/${currentShort.youtubeVideoId}?autoplay=1&controls=0&modestbranding=1&loop=1&playlist=${currentShort.youtubeVideoId}&enablejsapi=1`}
            title={currentShort.title}
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
          />

          {/* Transparent Overlay Controls */}
          <div className="absolute inset-0 p-5 flex flex-col justify-between pointer-events-none bg-gradient-to-b from-black/40 via-transparent to-black/80">
            {/* Top Bar */}
            <div className="flex items-center justify-between pointer-events-auto">
              <span className="px-3 py-1 rounded-full bg-blue-600/90 text-white text-xs font-bold shadow-md">
                {currentShort.category.replace('_', ' ')}
              </span>
              <button
                onClick={() => setIsMuted((prev) => !prev)}
                className="p-2 rounded-full bg-slate-900/80 text-white hover:bg-slate-800 transition"
              >
                {isMuted ? <VolumeX className="w-4 h-4" /> : <Volume2 className="w-4 h-4" />}
              </button>
            </div>

            {/* Bottom Metadata & Actions */}
            <div className="flex items-end justify-between gap-4 pointer-events-auto">
              <div className="space-y-1.5 flex-1">
                <div className="text-sm font-bold text-white tracking-wide">
                  @{currentShort.channelName}
                </div>
                <div className="text-xs text-slate-200 line-clamp-2 leading-relaxed font-medium">
                  {currentShort.title}
                </div>
              </div>

              {/* Action Column */}
              <div className="flex flex-col items-center gap-3 shrink-0">
                <button
                  onClick={() => toggleLike(currentShort.id)}
                  className="flex flex-col items-center gap-1 group"
                >
                  <div className={`p-3 rounded-full transition ${likedMap[currentShort.id] ? 'bg-rose-500/20 text-rose-500' : 'bg-slate-900/80 text-white group-hover:bg-slate-800'}`}>
                    <Heart className={`w-5 h-5 ${likedMap[currentShort.id] ? 'fill-rose-500' : ''}`} />
                  </div>
                  <span className="text-[11px] font-mono text-slate-300">
                    {currentShort.likeCount + (likedMap[currentShort.id] ? 1 : 0)}
                  </span>
                </button>

                <button className="flex flex-col items-center gap-1 group">
                  <div className="p-3 rounded-full bg-slate-900/80 text-white group-hover:bg-slate-800 transition">
                    <Share2 className="w-5 h-5" />
                  </div>
                  <span className="text-[11px] font-mono text-slate-300">Share</span>
                </button>

                <a
                  href={`https://youtube.com/shorts/${currentShort.youtubeVideoId}`}
                  target="_blank"
                  rel="noreferrer"
                  className="flex flex-col items-center gap-1 group"
                  title="Open on YouTube"
                >
                  <div className="p-3 rounded-full bg-red-600/80 text-white group-hover:bg-red-700 transition">
                    <ExternalLink className="w-5 h-5" />
                  </div>
                  <span className="text-[10px] font-mono text-slate-300">YouTube</span>
                </a>
              </div>
            </div>
          </div>
        </div>

        {/* Right Column: Desktop Controls & Context Desk */}
        <div className="hidden md:flex flex-col justify-between h-full max-h-[750px] w-80 bg-slate-900 border border-slate-800 rounded-3xl p-6">
          <div className="space-y-4">
            <div className="flex items-center gap-2 text-cyan-400 font-mono text-xs uppercase tracking-wider font-bold">
              <Sparkles className="w-4 h-4" />
              Football Clips Shorts Feed
            </div>
            <h2 className="text-xl font-bold text-white leading-tight">
              {currentShort.title}
            </h2>
            <div className="p-3.5 rounded-xl bg-slate-950/60 border border-slate-800 space-y-1">
              <span className="text-xs text-slate-400">Official Channel</span>
              <div className="text-sm font-semibold text-white">{currentShort.channelName}</div>
            </div>
          </div>

          {/* Navigation Buttons */}
          <div className="space-y-2">
            <div className="flex items-center justify-between text-xs text-slate-400 font-mono mb-1">
              <span>Use ↑ / ↓ arrow keys to browse</span>
              <span>{currentIndex + 1} of {MOCK_SHORTS.length}</span>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <button
                onClick={handlePrev}
                disabled={currentIndex === 0}
                className="flex items-center justify-center gap-2 py-3 rounded-xl bg-slate-800 text-white hover:bg-slate-700 disabled:opacity-40 transition font-medium text-sm"
              >
                <ChevronUp className="w-4 h-4" /> Prev
              </button>
              <button
                onClick={handleNext}
                disabled={currentIndex === MOCK_SHORTS.length - 1}
                className="flex items-center justify-center gap-2 py-3 rounded-xl bg-blue-600 text-white hover:bg-blue-500 disabled:opacity-40 transition font-medium text-sm"
              >
                Next <ChevronDown className="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
