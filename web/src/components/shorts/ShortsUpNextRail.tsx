'use client';

import React from 'react';
import Image from 'next/image';
import { FootballShortRecord } from '@/lib/shorts';

interface ShortsUpNextRailProps {
  shorts: FootballShortRecord[];
  activeIndex: number;
  onSelectIndex: (index: number) => void;
}

export default function ShortsUpNextRail({
  shorts,
  activeIndex,
  onSelectIndex,
}: ShortsUpNextRailProps) {
  // Show next 4 shorts in the queue
  const queue = shorts.slice(activeIndex + 1, activeIndex + 5);
  if (queue.length === 0) return null;

  return (
    <aside className="hidden xl:flex flex-col gap-3 w-64 shrink-0 pt-2 select-none">
      <div className="flex items-center justify-between px-1">
        <span className="font-tech text-xs uppercase tracking-wider text-white font-bold">
          Up Next
        </span>
        <span className="text-[10px] font-tech text-[#DFFF00] font-bold tracking-wider uppercase">
          Auto-Play
        </span>
      </div>

      <div className="flex flex-col gap-2">
        {queue.map((item, relIdx) => {
          const targetIndex = activeIndex + 1 + relIdx;
          return (
            <div
              key={item.id}
              onClick={() => onSelectIndex(targetIndex)}
              className="flex gap-2.5 p-2 bg-[#1A1C22]/60 hover:bg-[#1A1C22] rounded-xl cursor-pointer transition-all border border-white/5 group"
            >
              <div className="w-14 h-20 rounded-lg overflow-hidden bg-[#121316] shrink-0 relative">
                <Image
                  src={item.thumbnail_url}
                  alt={item.title}
                  fill
                  className="object-cover group-hover:scale-105 transition-transform"
                  unoptimized
                />
              </div>
              <div className="flex flex-col justify-between py-0.5 min-w-0 flex-1">
                <p className="text-xs text-white/90 font-medium line-clamp-2 leading-snug group-hover:text-[#DFFF00] transition-colors">
                  {item.title}
                </p>
                <span className="text-[10px] font-tech text-white/40 truncate">
                  {item.channel_name || '@footballclips'}
                </span>
              </div>
            </div>
          );
        })}
      </div>
    </aside>
  );
}
