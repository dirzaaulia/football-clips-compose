'use client';

import React, { useState, useMemo } from 'react';
import Image from 'next/image';
import { Search, ShoppingBag, Star, ExternalLink, Trophy, X, ShieldCheck } from 'lucide-react';
import { useMyClubs } from '@/hooks/useMyClubs';
import { useSupportedClubs } from '@/hooks/useSupportedClubs';
import { useCompetitions } from '@/hooks/useCompetitions';
import { useUserRegion } from '@/hooks/useUserRegion';
import CustomDropdown from '@/components/common/CustomDropdown';
import {
  buildClubGearUrl,
  resolveClubKitPhoto,
  resolveAmazonMarketplace,
  AMAZON_AFFILIATE_DISCLOSURE,
} from '@/lib/affiliate/amazon';

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

  const leagueOptions = useMemo(() => {
    return [
      { code: 'ALL', name: 'All Leagues' },
      ...competitions
        .filter((c) => c.competitionId !== 'ALL')
        .map((c) => ({ code: c.competitionId || '', name: c.name })),
    ];
  }, [competitions]);

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
    <div className="max-w-[1600px] w-full mx-auto px-3.5 sm:px-6 lg:px-8 2xl:px-10 py-6 space-y-6">
      {/* Hero Banner */}
      <div className="relative rounded-3xl p-6 sm:p-8 bg-gradient-to-r from-[#101418] via-[#1E2227] to-[#1E3A8A]/30 border border-[#334155] overflow-hidden shadow-xl">
        <div className="relative z-10 max-w-2xl space-y-3">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#1E3A8A]/40 text-[#7098F8] border border-[#7098F8]/40 text-xs font-bold">
            <ShoppingBag className="w-3.5 h-3.5" />
            <span>Official Amazon Fan Storefront • {marketplace}</span>
          </div>
          <h1 className="text-2xl sm:text-3xl lg:text-4xl font-black text-white tracking-tight">
            Official Club Merchandise &amp; Kits
          </h1>
          <p className="text-sm text-[#94A3B8] leading-relaxed">
            Browse authentic match jerseys, training apparel, fan scarves, and licensed gear for top international and domestic clubs, fulfilled directly via Amazon.
          </p>
        </div>
      </div>

      {/* 1. Personalized "My Followed Clubs" Section (if user has favorite clubs) */}
      {followedClubs.length > 0 && !search && selectedLeague === 'ALL' && (
        <section className="space-y-3 bg-[#14181D] border border-[#334155]/70 rounded-3xl p-5 shadow-lg">
          <div className="flex items-center justify-between pb-2 border-b border-[#334155]/60">
            <div className="flex items-center gap-2">
              <Star className="w-4 h-4 text-[#7098F8] fill-[#7098F8]" />
              <h2 className="text-sm sm:text-base font-extrabold text-white">
                My Followed Clubs Gear
              </h2>
            </div>
            <span className="text-xs font-bold text-[#7098F8]">{followedClubs.length} Clubs</span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {followedClubs.map((club) => (
              <ClubStoreCard key={club.id} club={club} country={country} isFavorite />
            ))}
          </div>
        </section>
      )}

      {/* 2. Search & Filter Bar */}
      <div className="flex flex-col sm:flex-row items-center gap-3">
        <div className="relative flex-1 w-full">
          <Search className="w-4 h-4 text-[#94A3B8] absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search club stores (e.g. Arsenal, Real Madrid, Persib)..."
            className="w-full bg-[#181C20] border border-[#334155] rounded-xl pl-10 pr-9 py-2.5 text-xs text-white placeholder-[#94A3B8]/60 focus:outline-none focus:border-[#7098F8]"
          />
          {search && (
            <button
              onClick={() => setSearch('')}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-[#94A3B8] hover:text-white"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          )}
        </div>

        <CustomDropdown<string>
          items={leagueOptions.map((opt) => ({
            value: opt.code,
            label: opt.name,
            icon: <Trophy className="w-3.5 h-3.5 text-[#7098F8]" />,
          }))}
          value={selectedLeague}
          onChange={setSelectedLeague}
          leadingIcon={<Trophy className="w-4 h-4 text-[#7098F8]" />}
          prefix="League"
          className="w-full sm:w-64"
          ariaLabel="Filter club stores by league"
        />
      </div>

      {/* 3. Clubs Storefront Grid */}
      <section className="space-y-4">
        <div className="flex items-center justify-between text-xs text-[#94A3B8]">
          <span>Showing {filteredClubs.length} official club stores</span>
          <span className="flex items-center gap-1.5 text-emerald-400 font-semibold">
            <ShieldCheck className="w-3.5 h-3.5" />
            Verified Licensed Merchandise
          </span>
        </div>

        {filteredClubs.length === 0 ? (
          <div className="py-20 text-center bg-[#181C20] rounded-3xl border border-[#334155] text-sm text-[#94A3B8]">
            No clubs match your search criteria.
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {filteredClubs.map((club) => (
              <ClubStoreCard
                key={club.id}
                club={club}
                country={country}
                isFavorite={favoriteClubs.has(club.id)}
              />
            ))}
          </div>
        )}
      </section>

      {/* Compliance Disclosure */}
      <div className="p-4 rounded-2xl bg-[#101418] border border-[#334155]/60 text-[11px] text-[#94A3B8]/70 leading-relaxed text-center sm:text-left">
        {AMAZON_AFFILIATE_DISCLOSURE}
      </div>
    </div>
  );
}

function ClubStoreCard({
  club,
  country,
  isFavorite = false,
}: {
  club: {
    name: string;
    crest_url?: string | null;
    crestUrl?: string | null;
    competition_name?: string | null;
    leagueName?: string | null;
  };
  country: string | null;
  isFavorite?: boolean;
}) {
  const crest = club.crest_url || club.crestUrl;
  const compName = club.competition_name || club.leagueName || 'Official Merchandise';
  const affiliateUrl = buildClubGearUrl(club.name, 'jersey', country);

  return (
    <a
      href={affiliateUrl}
      target="_blank"
      rel="noopener noreferrer sponsored"
      className="group bg-[#181C20] hover:bg-[#1E2227] border border-[#334155]/60 hover:border-[#7098F8]/60 rounded-2xl p-4 transition-all shadow-sm hover:shadow-[0_0_16px_rgba(112,152,248,0.15)] flex flex-col justify-between"
    >
      <div>
        <div className="relative w-full h-36 rounded-xl bg-gradient-to-b from-[#101418] via-[#1E2227] to-[#14181D] border border-[#334155]/40 flex items-center justify-center overflow-hidden mb-3">
          {crest ? (
            <div className="relative w-20 h-20 drop-shadow-[0_4px_12px_rgba(0,0,0,0.5)] group-hover:scale-110 transition-transform duration-300">
              <Image
                src={crest}
                alt={`${club.name} Crest`}
                fill
                className="object-contain"
                unoptimized
              />
            </div>
          ) : (
            <ShoppingBag className="w-10 h-10 text-[#7098F8]/50" />
          )}

          {isFavorite && (
            <div className="absolute top-2 left-2 p-1 rounded-md bg-[#1E3A8A]/80 border border-[#7098F8]/60">
              <Star className="w-3 h-3 text-[#7098F8] fill-[#7098F8]" />
            </div>
          )}
        </div>

        <div className="space-y-1">
          <h3 className="text-xs sm:text-sm font-bold text-white group-hover:text-[#7098F8] transition-colors line-clamp-2 leading-tight">
            {club.name}
          </h3>
          <span className="text-[10px] text-[#94A3B8] uppercase tracking-wider font-semibold block truncate">
            {compName}
          </span>
          <div className="flex items-center gap-1.5 text-[11px] text-[#94A3B8] pt-1">
            <div className="flex items-center text-amber-400">
              <Star className="w-3 h-3 fill-current" />
              <span className="font-bold ml-1 text-white">4.8</span>
            </div>
            <span>•</span>
            <span className="text-[#4FC3F7] font-semibold">Official Amazon Store</span>
          </div>
        </div>
      </div>

      <div className="mt-4 w-full py-2 px-3 rounded-xl bg-[#1E3A8A]/50 group-hover:bg-[#1E3A8A] text-[#7098F8] group-hover:text-white border border-[#7098F8]/30 flex items-center justify-center gap-1.5 text-xs font-bold transition-all">
        <ShoppingBag className="w-3.5 h-3.5" />
        <span>Shop Official Gear</span>
        <ExternalLink className="w-3 h-3" />
      </div>
    </a>
  );
}
