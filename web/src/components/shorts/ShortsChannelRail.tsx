'use client';

import React, { useMemo } from 'react';
import Image from 'next/image';
import { Zap, Compass, Bookmark, Star, Shield } from 'lucide-react';
import { useMyClubs } from '@/hooks/useMyClubs';
import { useSupportedClubs } from '@/hooks/useSupportedClubs';

interface ShortsChannelRailProps {
  activeCategory: string;
  onSelectCategory: (cat: string) => void;
  selectedClub?: string | null;
  onSelectClub?: (clubName: string | null) => void;
}

interface DefaultClub {
  id: string;
  name: string;
  crest: string;
}

const DEFAULT_POPULAR_CLUBS: DefaultClub[] = [
  { id: '86', name: 'Real Madrid', crest: 'https://a.espncdn.com/i/teamlogos/soccer/500/86.png' },
  { id: '83', name: 'FC Barcelona', crest: 'https://a.espncdn.com/i/teamlogos/soccer/500/83.png' },
  { id: '382', name: 'Man City', crest: 'https://a.espncdn.com/i/teamlogos/soccer/500/382.png' },
  { id: '364', name: 'Liverpool', crest: 'https://a.espncdn.com/i/teamlogos/soccer/500/364.png' },
  { id: '132', name: 'Bayern Munich', crest: 'https://a.espncdn.com/i/teamlogos/soccer/500/132.png' },
  { id: '359', name: 'Arsenal', crest: 'https://a.espncdn.com/i/teamlogos/soccer/500/359.png' },
];

export default function ShortsChannelRail({
  activeCategory,
  onSelectCategory,
  selectedClub = null,
  onSelectClub,
}: ShortsChannelRailProps) {
  const { favoriteClubs } = useMyClubs();
  const supportedClubs = useSupportedClubs();

  const userSavedClubs = useMemo(() => {
    if (!favoriteClubs || favoriteClubs.size === 0) return [];
    return supportedClubs.filter(
      (c) => favoriteClubs.has(c.id) || favoriteClubs.has(c.name) || favoriteClubs.has(c.short_name || '')
    );
  }, [favoriteClubs, supportedClubs]);

  const handleClubClick = (clubName: string) => {
    if (!onSelectClub) return;
    onSelectClub(selectedClub === clubName ? null : clubName);
  };

  return (
    <aside className="hidden lg:flex flex-col gap-2 w-60 shrink-0 select-none overflow-y-auto no-scrollbar max-h-[85vh]">
      <div className="flex flex-col gap-1">
        <button
          onClick={() => {
            onSelectCategory('all');
            if (onSelectClub) onSelectClub(null);
          }}
          className={`flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-tech text-xs uppercase tracking-wider font-bold transition-all cursor-pointer ${
            activeCategory === 'all' && !selectedClub
              ? 'bg-[#1A1C22] text-[#DFFF00] border border-[#DFFF00]/30 shadow-sm'
              : 'text-white/60 hover:text-white hover:bg-white/5'
          }`}
        >
          <Zap className="w-4 h-4 fill-current text-[#DFFF00]" />
          <span>Active Feed</span>
        </button>

        <button
          onClick={() => onSelectCategory('tactical')}
          className={`flex items-center gap-3 px-3.5 py-2.5 rounded-xl font-tech text-xs uppercase tracking-wider font-bold transition-all cursor-pointer ${
            activeCategory === 'tactical'
              ? 'bg-[#1A1C22] text-[#DFFF00] border border-[#DFFF00]/30 shadow-sm'
              : 'text-white/60 hover:text-white hover:bg-white/5'
          }`}
        >
          <Compass className="w-4 h-4 text-[#38BDF8]" />
          <span>Tactical Reels</span>
        </button>
      </div>

      {userSavedClubs.length > 0 && (
        <div className="flex flex-col gap-1 pt-2 border-t border-white/5">
          <div className="flex items-center gap-1.5 px-3 py-1 text-[10px] font-tech uppercase tracking-wider text-[#DFFF00] font-bold">
            <Star className="w-3 h-3 fill-current" />
            <span>My Clubs</span>
          </div>
          {userSavedClubs.map((club) => {
            const crest = club.crest_url || club.crestUrl || 'https://a.espncdn.com/i/teamlogos/soccer/500/default-team-logo.png';
            const isSelected = selectedClub === club.name;
            return (
              <button
                key={club.id}
                onClick={() => handleClubClick(club.name)}
                className={`flex items-center gap-2.5 px-3 py-1.5 rounded-xl transition-all text-left cursor-pointer ${
                  isSelected
                    ? 'bg-[#DFFF00]/15 border border-[#DFFF00]/30 text-white font-bold'
                    : 'hover:bg-white/5 text-white/80'
                }`}
              >
                <div className="relative w-5 h-5 shrink-0">
                  <Image src={crest} alt={club.name} fill className="object-contain" unoptimized />
                </div>
                <span className="text-xs truncate">{club.short_name || club.name}</span>
              </button>
            );
          })}
        </div>
      )}

      <div className="flex flex-col gap-1 pt-2 border-t border-white/5">
        <div className="flex items-center gap-1.5 px-3 py-1 text-[10px] font-tech uppercase tracking-wider text-white/40 font-bold">
          <Shield className="w-3 h-3" />
          <span>Popular Clubs</span>
        </div>
        {DEFAULT_POPULAR_CLUBS.map((club) => {
          const isSelected = selectedClub === club.name;
          return (
            <button
              key={club.id}
              onClick={() => handleClubClick(club.name)}
              className={`flex items-center gap-2.5 px-3 py-1.5 rounded-xl transition-all text-left cursor-pointer ${
                isSelected
                  ? 'bg-[#DFFF00]/15 border border-[#DFFF00]/30 text-white font-bold'
                  : 'hover:bg-white/5 text-white/80'
              }`}
            >
              <div className="relative w-5 h-5 shrink-0">
                <Image src={club.crest} alt={club.name} fill className="object-contain" unoptimized />
              </div>
              <span className="text-xs truncate">{club.name}</span>
            </button>
          );
        })}
      </div>
    </aside>
  );
}
