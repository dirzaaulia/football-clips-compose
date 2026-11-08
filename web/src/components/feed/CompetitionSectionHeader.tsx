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
      className={`w-full flex items-center justify-between px-4 py-3 rounded-2xl bg-[#181C20]/80 border border-white/10 hover:border-white/20 transition-all select-none backdrop-blur-md shadow-sm cursor-pointer group ${className}`}
    >
      <div className="flex items-center gap-3 min-w-0">
        <GlassLogoBadge
          src={emblemUrl}
          alt={name}
          size={32}
          imageClassName="object-contain p-1"
          fallbackText={name.slice(0, 2).toUpperCase()}
        />
        <h2 className="text-sm sm:text-base font-bold text-white tracking-tight truncate group-hover:text-[#7098F8] transition-colors">
          {name}
        </h2>
        {onToggleExpand && (
          <ChevronDown
            className={`w-4 h-4 text-[#94A3B8] transition-transform duration-300 ${
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
          className="inline-flex items-center gap-1 px-3 py-1.5 rounded-xl text-xs font-bold text-[#7098F8] bg-[#1E3A8A]/30 border border-[#7098F8]/30 hover:bg-[#7098F8]/20 hover:border-[#7098F8]/60 transition-all shrink-0 cursor-pointer"
        >
          <span>{seeAllLabel}</span>
          <ChevronRight className="w-3.5 h-3.5 group-hover:translate-x-0.5 transition-transform" />
        </button>
      )}
    </div>
  );
}
