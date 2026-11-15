'use client';

import React from 'react';
import { ChevronRight, ChevronDown } from 'lucide-react';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';

interface CompetitionSectionHeaderProps {
  name: string;
  emblemUrl?: string | null;
  onSeeAll?: () => void;
  seeAllLabel?: string;
  isExpanded?: boolean;
  onToggleExpand?: () => void;
  className?: string;
}

export default function CompetitionSectionHeader({
  name,
  emblemUrl,
  onSeeAll,
  seeAllLabel = 'See all',
  isExpanded = true,
  onToggleExpand,
  className = '',
}: CompetitionSectionHeaderProps) {
  const handleHeaderClick = () => {
    if (onToggleExpand) {
      onToggleExpand();
    } else if (onSeeAll) {
      onSeeAll();
    }
  };

  return (
    <div
      onClick={handleHeaderClick}
      className={`w-full flex items-center justify-between px-4 py-3 rounded-2xl bg-[#121316] border border-white/10 hover:border-[#DFFF00]/40 transition-all select-none backdrop-blur-md shadow-sm cursor-pointer group ${className}`}
    >
      <div className="flex items-center gap-3 min-w-0">
        <GlassLogoBadge
          src={emblemUrl}
          alt={name}
          size={32}
          imageClassName="object-contain p-1"
          fallbackText={name.slice(0, 2).toUpperCase()}
        />
        <h2 className="font-scoreboard text-base sm:text-lg text-white tracking-wider uppercase truncate group-hover:text-[#DFFF00] transition-colors">
          {name}
        </h2>
        {onToggleExpand && (
          <ChevronDown
            className={`w-4 h-4 text-[#A1A1AA] transition-transform duration-300 ${
              isExpanded ? 'rotate-0' : '-rotate-90'
            }`}
          />
        )}
      </div>

      {onSeeAll && (
        <button
          type="button"
          onClick={(e) => {
            e.stopPropagation();
            onSeeAll();
          }}
          className="inline-flex items-center gap-1 px-3 py-1.5 rounded-xl font-tech text-xs font-bold text-[#DFFF00] bg-[#DFFF00]/15 border border-[#DFFF00]/30 hover:bg-[#DFFF00]/25 hover:border-[#DFFF00]/60 transition-all shrink-0 cursor-pointer"
        >
          <span>{seeAllLabel}</span>
          <ChevronRight className="w-3.5 h-3.5 group-hover:translate-x-0.5 transition-transform" />
        </button>
      )}
    </div>
  );
}
