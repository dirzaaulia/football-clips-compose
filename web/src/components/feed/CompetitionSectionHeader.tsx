'use client';

import React from 'react';
import Image from 'next/image';
import { ChevronRight } from 'lucide-react';

interface CompetitionSectionHeaderProps {
  name: string;
  emblemUrl?: string | null;
  onSeeAll?: () => void;
  seeAllLabel?: string;
  className?: string;
}

export default function CompetitionSectionHeader({
  name,
  emblemUrl,
  onSeeAll,
  seeAllLabel = 'See all',
  className = '',
}: CompetitionSectionHeaderProps) {
  return (
    <div
      onClick={onSeeAll}
      className={`w-full flex items-center justify-between px-4 py-3 rounded-2xl bg-[#181C20]/80 border border-white/10 hover:border-white/20 transition-all select-none backdrop-blur-md shadow-sm ${
        onSeeAll ? 'cursor-pointer active:scale-[0.99] group' : ''
      } ${className}`}
    >
      <div className="flex items-center gap-3 min-w-0">
        {emblemUrl ? (
          <div className="relative w-7 h-7 shrink-0 flex items-center justify-center">
            <Image
              src={emblemUrl}
              alt={name}
              width={28}
              height={28}
              className="object-contain max-h-7"
              loading="lazy"
              unoptimized
            />
          </div>
        ) : (
          <div className="w-7 h-7 rounded-lg bg-white/5 border border-white/10 flex items-center justify-center text-xs font-black text-gray-400 shrink-0">
            {name.slice(0, 2).toUpperCase()}
          </div>
        )}
        <h2 className="text-sm sm:text-base font-bold text-white tracking-tight truncate group-hover:text-[#7098F8] transition-colors">
          {name}
        </h2>
      </div>

      {onSeeAll && (
        <button
          type="button"
          onClick={(e) => {
            e.stopPropagation();
            onSeeAll();
          }}
          className="inline-flex items-center gap-1 px-3 py-1.5 rounded-xl text-xs font-bold text-[#7098F8] bg-[#1E3A8A]/30 border border-[#7098F8]/30 hover:bg-[#7098F8]/20 hover:border-[#7098F8]/60 transition-all shrink-0 cursor-pointer"
        >
          <span>{seeAllLabel}</span>
          <ChevronRight className="w-3.5 h-3.5 group-hover:translate-x-0.5 transition-transform" />
        </button>
      )}
    </div>
  );
}
