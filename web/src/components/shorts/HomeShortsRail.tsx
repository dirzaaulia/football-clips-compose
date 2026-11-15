'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { Zap, ArrowRight } from 'lucide-react';
import { FootballShortRecord, getShorts } from '@/lib/shorts';

export default function HomeShortsRail() {
  const [shorts, setShorts] = useState<FootballShortRecord[]>([]);

  useEffect(() => {
    getShorts({ limit: 6 }).then((data) => {
      if (data && data.length > 0) {
        setShorts(data);
      }
    });
  }, []);

  if (shorts.length === 0) return null;

  return (
    <section className="w-full py-6 space-y-4 select-none">
      {/* Header */}
      <div className="w-full flex items-end justify-between px-1">
        <div>
          <div className="flex items-center gap-2">
            <Zap className="w-5 h-5 text-[#DFFF00]" />
            <h2 className="font-scoreboard text-xl sm:text-2xl uppercase text-white tracking-wide leading-none">
              Shorts & Fast Clips
            </h2>
          </div>
          <p className="font-tech text-xs text-white/50 mt-1">
            Instant vertical snips: 60-second wondergoals, VAR controversies, and viral tactical press moments.
          </p>
        </div>

        <Link
          href="/shorts"
          className="font-tech text-xs uppercase tracking-wider text-[#DFFF00] hover:text-white transition-colors flex items-center gap-1 shrink-0"
        >
          <span>Open Shorts Vault</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </Link>
      </div>

      {/* 9:16 Vertical Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3 sm:gap-4">
        {shorts.map((short) => {
          const thumbUrl =
            short.thumbnail_url ||
            `https://i.ytimg.com/vi/${short.youtube_video_id}/hqdefault.jpg`;
          const loopsCount = (((short.like_count || 100) * 12 + 400) / 1000).toFixed(1);

          return (
            <Link
              key={short.id}
              href={`/shorts?id=${short.id}`}
              className="group relative aspect-[9/16] rounded-xl overflow-hidden bg-[#121316] border border-white/10 hover:border-[#DFFF00]/50 transition-transform duration-300 hover:scale-[1.03] shadow-md flex flex-col justify-end p-3 cursor-pointer"
            >
              <Image
                src={thumbUrl}
                alt={short.title}
                fill
                sizes="(max-width: 640px) 50vw, (max-width: 1024px) 33vw, 16vw"
                className="object-cover group-hover:scale-110 transition-transform duration-500 opacity-80"
                unoptimized
              />
              <div className="absolute inset-0 bg-gradient-to-t from-[#0A0A0C] via-[#0A0A0C]/40 to-transparent" />

              <div className="relative z-10 space-y-1">
                <span className="inline-block px-1.5 py-0.5 rounded bg-[#DFFF00] text-[#0A0A0C] font-tech text-[10px] uppercase font-black tracking-wider">
                  {short.category || 'FAST CLIP'}
                </span>

                <h4 className="font-tech text-xs font-bold text-white line-clamp-2 leading-tight group-hover:text-[#DFFF00] transition-colors">
                  {short.title}
                </h4>

                <span className="font-tech text-[10px] text-white/50 block">
                  {loopsCount}K loops
                </span>
              </div>
            </Link>
          );
        })}
      </div>
    </section>
  );
}
