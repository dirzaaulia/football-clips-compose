'use client';

import React from 'react';
import Image from 'next/image';
import { Play, Flame } from 'lucide-react';
import { FootballShortRecord } from '@/lib/shorts';

interface ShortsUpNextQueueProps {
  shorts: FootballShortRecord[];
  activeIndex: number;
  onSelectIndex: (idx: number) => void;
}

export default function ShortsUpNextQueue({
  shorts,
  activeIndex,
  onSelectIndex,
}: ShortsUpNextQueueProps) {
  if (shorts.length <= 1) return null;

  const upcomingItems = [1, 2, 3].map((offset) => {
    const targetIdx = (activeIndex + offset) % shorts.length;
    return { short: shorts[targetIdx], index: targetIdx };
  });

  return (
    <aside className="hidden xl:flex flex-col w-72 shrink-0 gap-3 self-center">
      <div className="bg-[#121316] border border-white/10 rounded-2xl p-4 shadow-xl space-y-3">
        <div className="flex items-center justify-between border-b border-white/10 pb-2.5">
          <div className="flex items-center gap-2">
            <Flame className="w-4 h-4 text-[#DFFF00]" />
            <span className="font-scoreboard text-base uppercase tracking-wider text-white">
              Up Next in Reel
            </span>
          </div>
          <span className="text-[10px] font-tech font-bold text-[#DFFF00] bg-[#DFFF00]/10 px-2 py-0.5 rounded-full border border-[#DFFF00]/30">
            {activeIndex + 1}/{shorts.length}
          </span>
        </div>

        <div className="space-y-2.5">
          {upcomingItems.map(({ short, index }) => (
            <button
              key={`${short.id}-${index}`}
              type="button"
              onClick={() => onSelectIndex(index)}
              className="w-full flex items-center gap-3 p-2 rounded-xl bg-[#1A1C22] hover:bg-[#22252D] border border-white/5 hover:border-[#DFFF00]/40 transition-all text-left group cursor-pointer"
            >
              <div className="relative w-12 h-20 rounded-lg overflow-hidden bg-[#0A0A0C] shrink-0 border border-white/10">
                <Image
                  src={short.thumbnail_url}
                  alt={short.title}
                  fill
                  className="object-cover group-hover:scale-105 transition-transform"
                  unoptimized
                />
                <div className="absolute inset-0 bg-black/30 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                  <Play className="w-4 h-4 text-[#DFFF00] fill-current" />
                </div>
              </div>
              <div className="min-w-0 flex-1 space-y-1">
                <span className="text-[10px] font-tech font-bold text-[#DFFF00] uppercase tracking-wider block truncate">
                  {short.channel_name || 'Official Short'}
                </span>
                <p className="text-xs font-bold text-white line-clamp-2 leading-snug group-hover:text-[#DFFF00] transition-colors">
                  {short.title}
                </p>
              </div>
            </button>
          ))}
        </div>
      </div>
    </aside>
  );
}
