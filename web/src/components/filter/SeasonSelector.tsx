'use client';

import React from 'react';
import { Calendar, Archive } from 'lucide-react';
import { useSeason, AVAILABLE_SEASONS } from '@/hooks/useSeason';
import CustomDropdown, { DropdownItem } from '@/components/common/CustomDropdown';

interface SeasonSelectorProps {
  variant?: 'pill' | 'select' | 'compact';
  className?: string;
  align?: 'left' | 'right';
}

export default function SeasonSelector({
  variant = 'select',
  className = '',
  align = 'left',
}: SeasonSelectorProps) {
  const { selectedSeason, setSeason, isArchiveSeason } = useSeason();

  if (variant === 'pill') {
    return (
      <div className={`flex items-center bg-[#181C20] border border-[#334155] p-1 rounded-xl shadow-sm ${className}`}>
        {AVAILABLE_SEASONS.map((s) => {
          const isSelected = s.year === selectedSeason;
          return (
            <button
              key={s.year}
              type="button"
              onClick={() => setSeason(s.year)}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer flex items-center gap-1.5 ${
                isSelected
                  ? s.isCurrent
                    ? 'bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white shadow-sm'
                    : 'bg-gradient-to-r from-amber-600 to-amber-500 text-white shadow-sm'
                  : 'text-[#94A3B8] hover:text-white'
              }`}
            >
              {!s.isCurrent && <Archive className="w-3 h-3" />}
              <span>{s.label}</span>
            </button>
          );
        })}
      </div>
    );
  }

  const dropdownItems: DropdownItem<number>[] = AVAILABLE_SEASONS.map((s) => ({
    value: s.year,
    label: s.shortLabel,
    shortLabel: s.shortLabel,
    badge: s.isCurrent ? 'Current' : 'Archive',
    badgeVariant: s.isCurrent ? 'primary' : 'amber',
    icon: s.isCurrent ? (
      <Calendar className="w-3.5 h-3.5" />
    ) : (
      <Archive className="w-3.5 h-3.5" />
    ),
  }));

  if (variant === 'compact') {
    return (
      <CustomDropdown<number>
        items={dropdownItems}
        value={selectedSeason}
        onChange={setSeason}
        variant={isArchiveSeason ? 'amber' : 'compact'}
        leadingIcon={
          isArchiveSeason ? (
            <Archive className="w-3.5 h-3.5" />
          ) : (
            <Calendar className="w-3.5 h-3.5" />
          )
        }
        align={align}
        className={className}
        ariaLabel="Select season"
      />
    );
  }

  // Default 'select' variant
  return (
    <CustomDropdown<number>
      items={dropdownItems}
      value={selectedSeason}
      onChange={setSeason}
      prefix="Season"
      variant={isArchiveSeason ? 'amber' : 'default'}
      leadingIcon={
        isArchiveSeason ? (
          <Archive className="w-4 h-4" />
        ) : (
          <Calendar className="w-4 h-4" />
        )
      }
      align={align}
      className={className}
      ariaLabel="Select season"
    />
  );
}
