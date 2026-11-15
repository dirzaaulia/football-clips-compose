'use client';

import React, { useState, useMemo } from 'react';
import { Search, ShoppingBag, Star, Trophy, X, ShieldCheck } from 'lucide-react';
import { useMyClubs } from '@/hooks/useMyClubs';
import { useSupportedClubs } from '@/hooks/useSupportedClubs';
import { useCompetitions } from '@/hooks/useCompetitions';
import { useUserRegion } from '@/hooks/useUserRegion';
import CustomDropdown from '@/components/common/CustomDropdown';
import ClubStoreCard from '@/components/store/ClubStoreCard';
import { resolveAmazonMarketplace, AMAZON_AFFILIATE_DISCLOSURE } from '@/lib/affiliate/amazon';

export default function StorePage() {
  const { favoriteClubs } = useMyClubs();
  const allClubs = useSupportedClubs();
  const competitions = useCompetitions();
  const { country } = useUserRegion();
  const marketplace = resolveAmazonMarketplace(country);

  const [search, setSearch] = useState('');
  const [selectedLeague, setSelectedLeague] = useState('ALL');

  const followedClubs = useMemo(() => {
    return allClubs.filter((c) => favoriteClubs.has(c.id));
  }, [allClubs, favoriteClubs]);

  const filteredClubs = useMemo(() => {
    return allClubs.filter((club) => {
      const code = club.competition_code || club.leagueCode;
      const matchesLeague = selectedLeague === 'ALL' || code === selectedLeague;
      const s = search.toLowerCase().trim();
      const short = club.short_name || club.shortName || '';
      const matchesSearch =
        !s ||
        club.name.toLowerCase().includes(s) ||
        (short && short.toLowerCase().includes(s));
      return matchesLeague && matchesSearch;
    });
  }, [allClubs, selectedLeague, search]);

  return (
    <div className="max-w-[1600px] w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 space-y-6 select-none">
      {/* Hero Banner */}
      <div className="relative rounded-3xl p-6 sm:p-8 bg-gradient-to-r from-[#121316] via-[#1A1C22] to-[#121316] border border-white/10 overflow-hidden shadow-2xl">
        <div className="relative z-10 max-w-2xl space-y-2.5">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#DFFF00]/15 text-[#DFFF00] border border-[#DFFF00]/30 text-[10px] font-tech font-bold uppercase tracking-wider">
            <ShoppingBag className="w-3.5 h-3.5" />
            <span>Official Fan Storefront • {marketplace}</span>
          </div>
          <h1 className="font-scoreboard text-3xl sm:text-4xl text-white uppercase tracking-wide leading-none">
            Official Club Kits &amp; Gear
          </h1>
          <p className="text-xs sm:text-sm text-white/70 font-tech leading-relaxed">
            Browse authentic 2026/27 match jerseys, training wear, retro collections, and licensed accessories.
          </p>
        </div>
      </div>

      {/* Followed Clubs Shelf */}
      {followedClubs.length > 0 && !search && selectedLeague === 'ALL' && (
        <section className="space-y-3 bg-[#121316] border border-white/10 rounded-2xl p-5 shadow-lg">
          <div className="flex items-center justify-between pb-2 border-b border-white/5">
            <div className="flex items-center gap-2">
              <Star className="w-4 h-4 text-[#DFFF00] fill-current" />
              <h2 className="font-scoreboard text-lg sm:text-xl text-white uppercase tracking-wider">
                My Followed Clubs Gear
              </h2>
            </div>
            <span className="font-tech text-xs font-bold text-[#DFFF00]">{followedClubs.length} Clubs</span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {followedClubs.map((club) => (
              <ClubStoreCard key={club.id} club={club} country={country} isFavorite />
            ))}
          </div>
        </section>
      )}

      {/* Search & Filter Bar */}
      <div className="flex flex-col sm:flex-row items-center gap-3">
        <div className="relative flex-1 w-full">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-white/40" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search club, crest, or nickname..."
            className="w-full bg-[#1A1C22] border border-white/10 focus:border-[#DFFF00] rounded-xl pl-10 pr-10 py-2.5 text-xs text-white placeholder-white/40 outline-none font-tech transition-colors"
          />
          {search && (
            <button
              onClick={() => setSearch('')}
              className="absolute right-3.5 top-1/2 -translate-y-1/2 text-white/40 hover:text-white"
            >
              <X className="w-4 h-4" />
            </button>
          )}
        </div>

        <div className="w-full sm:w-auto shrink-0">
          <CustomDropdown<string>
            items={[
              { value: 'ALL', label: 'All Leagues' },
              ...competitions
                .filter((c) => c.competitionId !== 'ALL')
                .map((c) => ({ value: c.competitionId || '', label: c.name })),
            ]}
            value={selectedLeague}
            onChange={setSelectedLeague}
            prefix="League"
            variant="compact"
          />
        </div>
      </div>

      {/* All Clubs Grid */}
      {filteredClubs.length === 0 ? (
        <div className="text-center py-16 bg-[#121316] rounded-2xl border border-white/10 space-y-2">
          <p className="font-tech text-white/50 text-sm">No club merchandise found matching your filter.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {filteredClubs.map((club) => (
            <ClubStoreCard key={club.id} club={club} country={country} />
          ))}
        </div>
      )}

      {/* Affiliate Disclosure Footer */}
      <p className="text-[11px] text-white/40 font-tech text-center py-3 border-t border-white/5">
        {AMAZON_AFFILIATE_DISCLOSURE}
      </p>
    </div>
  );
}
