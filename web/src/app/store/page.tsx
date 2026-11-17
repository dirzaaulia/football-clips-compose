'use client';

import React, { useState, useMemo } from 'react';
import {
  Search,
  ShoppingBag,
  Star,
  X,
  ShieldCheck,
  Truck,
  Award,
  Sparkles,
} from 'lucide-react';
import { useMyClubs } from '@/hooks/useMyClubs';
import { useSupportedClubs } from '@/hooks/useSupportedClubs';
import { useCompetitions } from '@/hooks/useCompetitions';
import { useUserRegion } from '@/hooks/useUserRegion';
import CustomDropdown from '@/components/common/CustomDropdown';
import ClubStoreCard, { StoreKitCategory } from '@/components/store/ClubStoreCard';
import { resolveAmazonMarketplace, AMAZON_AFFILIATE_DISCLOSURE } from '@/lib/affiliate/amazon';

const CATEGORIES: Array<{ id: StoreKitCategory; label: string }> = [
  { id: 'ALL', label: 'All Kits' },
  { id: 'HOME', label: 'Home Kits 26/27' },
  { id: 'AWAY', label: 'Away & Third' },
  { id: 'RETRO', label: 'Retro Classic Editions' },
  { id: 'TRAINING', label: 'Training & Pre-Match' },
  { id: 'ACCESSORIES', label: 'Accessories & Scarf' },
];

const QUICK_CLUBS = ['Arsenal', 'Real Madrid', 'Man City', 'Barcelona', 'Liverpool', 'Bayern'];

export default function StorePage() {
  const { favoriteClubs } = useMyClubs();
  const allClubs = useSupportedClubs();
  const competitions = useCompetitions();
  const { country } = useUserRegion();
  const marketplace = resolveAmazonMarketplace(country);

  const [search, setSearch] = useState('');
  const [selectedLeague, setSelectedLeague] = useState('ALL');
  const [selectedCategory, setSelectedCategory] = useState<StoreKitCategory>('ALL');

  const followedClubs = useMemo(
    () => allClubs.filter((c) => favoriteClubs.has(c.id)),
    [allClubs, favoriteClubs],
  );

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
    <div className="w-full select-none">
      {/* Stitch Top Trust & Licensing Banner Bar */}
      <div className="border-b border-white/5 bg-[#0D0E11] px-4 sm:px-6 lg:px-8 py-2">
        <div className="max-w-[1600px] mx-auto flex flex-wrap items-center justify-between gap-4 text-[11px] font-tech uppercase tracking-wider text-white/60">
          <div className="flex items-center gap-1.5 text-[#DFFF00] font-bold">
            <ShieldCheck className="w-3.5 h-3.5" />
            <span>100% Official Licensed Match Kits</span>
          </div>
          <div className="hidden md:flex items-center gap-6">
            <span className="flex items-center gap-1.5">
              <Truck className="w-3.5 h-3.5 text-[#DFFF00]" />
              <span>Global Express Dispatch ({marketplace})</span>
            </span>
            <span className="flex items-center gap-1.5">
              <Award className="w-3.5 h-3.5 text-[#DFFF00]" />
              <span>Official Sleeve Badges &amp; Authentic Player Printing</span>
            </span>
          </div>
        </div>
      </div>

      {/* Stitch Hero Section with Quick Club Direct Filter Card */}
      <section className="relative overflow-hidden border-b border-white/10 bg-gradient-to-br from-[#202024] via-[#131317] to-[#1B1B1F]">
        <div className="absolute -top-24 -right-24 w-96 h-96 rounded-full bg-[#DFFF00]/10 blur-3xl pointer-events-none" />
        <div className="max-w-[1600px] mx-auto px-4 sm:px-6 lg:px-8 py-8 lg:py-10 relative z-10 flex flex-col lg:flex-row items-center justify-between gap-8">
          <div className="max-w-2xl space-y-3">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#DFFF00]/15 border border-[#DFFF00]/30 text-[#DFFF00] text-xs font-tech uppercase tracking-widest font-bold">
              <span className="w-2 h-2 rounded-full bg-[#DFFF00] animate-ping" />
              <span>Official 2026/27 Matchday Collection Live</span>
            </div>
            <h1 className="font-scoreboard text-4xl sm:text-6xl text-white uppercase tracking-wide leading-none">
              OFFICIAL CLUB STORE &amp;{' '}
              <span className="text-[#DFFF00]">AUTHENTIC KITS</span>
            </h1>
            <p className="text-white/65 text-xs sm:text-sm font-tech max-w-xl leading-relaxed">
              Shop authentic player-issue match jerseys, heritage retro kits, and official training gear with regional fulfillment via{' '}
              <span className="text-white font-bold">{marketplace}</span>.
            </p>
          </div>

          {/* Quick Club Direct Filter Card */}
          <div className="w-full lg:w-[420px] bg-[#131317]/90 border border-white/15 rounded-xl p-5 backdrop-blur-md shadow-2xl space-y-3">
            <div className="flex items-center justify-between">
              <span className="font-tech text-xs uppercase tracking-wider text-white/65 font-bold">
                Find Club Gear Fast
              </span>
              <span className="text-xs text-[#DFFF00] font-tech font-bold">
                {allClubs.length}+ Clubs
              </span>
            </div>

            <div className="relative flex items-center">
              <Search className="w-4 h-4 text-white/40 absolute left-3.5" />
              <input
                type="text"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search jersey, player, or club..."
                className="w-full pl-10 pr-9 py-2.5 bg-[#0D0E11] border border-white/10 rounded-lg text-xs sm:text-sm text-white placeholder-white/40 focus:outline-none focus:border-[#DFFF00] font-tech transition-colors"
              />
              {search && (
                <button
                  type="button"
                  onClick={() => setSearch('')}
                  className="absolute right-3 text-white/40 hover:text-white cursor-pointer"
                >
                  <X className="w-4 h-4" />
                </button>
              )}
            </div>

            <div className="flex flex-wrap gap-1.5">
              {QUICK_CLUBS.map((qClub) => {
                const active = search.toLowerCase() === qClub.toLowerCase();
                return (
                  <button
                    key={qClub}
                    type="button"
                    onClick={() => setSearch(active ? '' : qClub)}
                    className={`px-2.5 py-1 rounded text-xs font-tech transition-colors cursor-pointer ${
                      active
                        ? 'bg-[#DFFF00] text-[#0A0A0C] font-bold'
                        : 'bg-[#292A2E] hover:bg-[#32343A] text-white/85 border border-white/5'
                    }`}
                  >
                    {qClub}
                  </button>
                );
              })}
            </div>
          </div>
        </div>
      </section>

      {/* Sticky Category Filter Bar */}
      <div className="sticky top-12 z-30 bg-[#121316]/90 backdrop-blur-md border-b border-white/10 px-4 sm:px-6 lg:px-8 py-3">
        <div className="max-w-[1600px] mx-auto flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-2 overflow-x-auto no-scrollbar py-0.5">
            {CATEGORIES.map((cat) => {
              const active = selectedCategory === cat.id;
              return (
                <button
                  key={cat.id}
                  type="button"
                  onClick={() => setSelectedCategory(cat.id)}
                  className={`px-3.5 py-1.5 rounded-full font-tech text-xs uppercase tracking-wider font-bold shrink-0 transition-all cursor-pointer ${
                    active
                      ? 'bg-[#DFFF00] text-[#0A0A0C] shadow-[0_0_12px_rgba(223,255,0,0.25)]'
                      : 'bg-[#1B1B1F] hover:bg-[#292A2E] text-white/75 border border-white/10'
                  }`}
                >
                  {cat.label}
                </button>
              );
            })}
          </div>

          <div className="w-full sm:w-auto shrink-0">
            <CustomDropdown<string>
              items={[
                { value: 'ALL', label: 'All Competitions' },
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
      </div>

      {/* Main Store Grid Container */}
      <div className="max-w-[1600px] mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        {/* Followed Clubs Shelf */}
        {followedClubs.length > 0 && !search && selectedLeague === 'ALL' && (
          <section className="space-y-4 bg-[#1B1B1F] border border-white/10 rounded-xl p-5 shadow-lg">
            <div className="flex items-center justify-between pb-2 border-b border-white/5">
              <div className="flex items-center gap-2">
                <Star className="w-4 h-4 text-[#DFFF00] fill-[#DFFF00]" />
                <h2 className="font-scoreboard text-xl text-white uppercase tracking-wider">
                  My Followed Clubs Official Locker
                </h2>
              </div>
              <span className="font-tech text-xs font-bold text-[#DFFF00]">
                {followedClubs.length} Clubs
              </span>
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
              {followedClubs.map((club) => (
                <ClubStoreCard
                  key={club.id}
                  club={club}
                  country={country}
                  category={selectedCategory}
                  isFavorite
                />
              ))}
            </div>
          </section>
        )}

        {/* All Clubs Product Grid */}
        {filteredClubs.length === 0 ? (
          <div className="text-center py-16 bg-[#1B1B1F] rounded-xl border border-white/10">
            <p className="font-tech text-white/50 text-sm">
              No club merchandise found matching your filter.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {filteredClubs.map((club) => (
              <ClubStoreCard
                key={club.id}
                club={club}
                country={country}
                category={selectedCategory}
              />
            ))}
          </div>
        )}

        {/* Stitch Bottom 3-Card Official Verification & Licensing Section */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 pt-4 border-t border-white/10">
          <div className="p-5 rounded-xl bg-[#1B1B1F] border border-white/5 flex items-start gap-4">
            <div className="w-10 h-10 rounded-lg bg-[#DFFF00]/15 border border-[#DFFF00]/30 flex items-center justify-center shrink-0 text-[#DFFF00]">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-scoreboard text-lg text-white tracking-wide">
                100% Certified Authentic Drops
              </h3>
              <p className="text-xs text-white/60 font-tech mt-1 leading-relaxed">
                Every jersey links directly to official brand storefronts and verified club merchandise on Amazon.
              </p>
            </div>
          </div>

          <div className="p-5 rounded-xl bg-[#1B1B1F] border border-white/5 flex items-start gap-4">
            <div className="w-10 h-10 rounded-lg bg-[#DFFF00]/15 border border-[#DFFF00]/30 flex items-center justify-center shrink-0 text-[#DFFF00]">
              <Sparkles className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-scoreboard text-lg text-white tracking-wide">
                Federation Sleeve Badges &amp; Prints
              </h3>
              <p className="text-xs text-white/60 font-tech mt-1 leading-relaxed">
                Available in stadium replica and player-issue authentic cuts with official name and number printing.
              </p>
            </div>
          </div>

          <div className="p-5 rounded-xl bg-[#1B1B1F] border border-white/5 flex items-start gap-4">
            <div className="w-10 h-10 rounded-lg bg-[#DFFF00]/15 border border-[#DFFF00]/30 flex items-center justify-center shrink-0 text-[#DFFF00]">
              <ShoppingBag className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-scoreboard text-lg text-white tracking-wide">
                Global Amazon Buyer Protection
              </h3>
              <p className="text-xs text-white/60 font-tech mt-1 leading-relaxed">
                {AMAZON_AFFILIATE_DISCLOSURE}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
