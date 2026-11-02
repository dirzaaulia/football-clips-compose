'use client';

import React from 'react';
import Image from 'next/image';
import { ArrowRight, Trophy } from 'lucide-react';

interface SeeAllCompetitionCardProps {
  competitionName: string;
  competitionEmblem?: string | null;
  onSeeAll: () => void;
  className?: string;
}

export default function SeeAllCompetitionCard({
  competitionName,
  competitionEmblem,
  onSeeAll,
  className = '',
}: SeeAllCompetitionCardProps) {
  return (
    <div
      onClick={onSeeAll}
      className={`w-full group cursor-pointer rounded-2xl bg-gradient-to-r from-[#181C20]/90 via-[#1E2530]/70 to-[#181C20]/90 border border-white/10 hover:border-[#7098F8]/50 p-4 transition-all duration-200 select-none backdrop-blur-md shadow-md hover:shadow-[0_4px_20px_rgba(112,152,248,0.15)] flex items-center justify-between gap-4 ${className}`}
    >
      <div className="flex items-center gap-3 min-w-0">
        {competitionEmblem ? (
          <div className="relative w-8 h-8 shrink-0 flex items-center justify-center bg-white/5 rounded-xl p-1 border border-white/10">
            <Image
              src={competitionEmblem}
              alt={competitionName}
              width={28}
              height={28}
              className="object-contain max-h-7"
              loading="lazy"
              unoptimized
            />
          </div>
        ) : (
          <div className="w-8 h-8 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center shrink-0">
            <Trophy className="w-4 h-4 text-[#7098F8]" />
          </div>
        )}
        <div className="min-w-0">
          <p className="text-xs text-slate-400 font-medium">Explore complete fixture archive</p>
          <h3 className="text-sm sm:text-base font-bold text-white group-hover:text-[#7098F8] transition-colors truncate">
            See all {competitionName} highlights & clips
          </h3>
        </div>
      </div>

      <div className="flex items-center gap-2 px-3.5 py-2 rounded-xl bg-[#7098F8]/10 group-hover:bg-[#7098F8]/20 border border-[#7098F8]/30 group-hover:border-[#7098F8]/60 text-xs font-bold text-[#7098F8] transition-all shrink-0">
        <span>View all</span>
        <ArrowRight className="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" />
      </div>
    </div>
  );
}
