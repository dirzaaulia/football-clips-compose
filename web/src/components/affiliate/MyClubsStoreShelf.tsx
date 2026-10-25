'use client';

import React from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { ShoppingBag, Star, ExternalLink, ArrowRight } from 'lucide-react';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';
import { useMyClubs } from '@/hooks/useMyClubs';
import { useSupportedClubs } from '@/hooks/useSupportedClubs';
import { useUserRegion } from '@/hooks/useUserRegion';
import {
  buildClubGearUrl,
  resolveClubKitPhoto,
  resolveAmazonMarketplace,
  AMAZON_AFFILIATE_DISCLOSURE,
} from '@/lib/affiliate/amazon';

interface MyClubsStoreShelfProps {
  onOpenMyClubs?: () => void;
  className?: string;
}

export default function MyClubsStoreShelf({
  onOpenMyClubs,
  className = '',
}: MyClubsStoreShelfProps) {
  const { favoriteClubs } = useMyClubs();
  const allClubs = useSupportedClubs();
  const { country } = useUserRegion();
  const marketplace = resolveAmazonMarketplace(country);

  const followedClubs = React.useMemo(() => {
    return allClubs.filter((c) => favoriteClubs.has(c.id));
  }, [allClubs, favoriteClubs]);

  if (followedClubs.length === 0) {
    return (
      <div className={`p-4 rounded-2xl bg-[#14181D] border border-[#334155]/60 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs ${className}`}>
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-[#1E3A8A]/30 text-[#7098F8] border border-[#7098F8]/30">
            <ShoppingBag className="w-5 h-5" />
          </div>
          <div>
            <h4 className="font-extrabold text-white text-sm">Personalized Club Stores</h4>
            <p className="text-[#94A3B8] text-xs">Follow your favorite clubs in My Clubs to unlock direct official gear and kit stores.</p>
          </div>
        </div>
        {onOpenMyClubs && (
          <button
            onClick={onOpenMyClubs}
            className="px-3.5 py-1.5 rounded-xl bg-[#1E3A8A] hover:bg-[#2563EB] text-white font-bold transition-all shrink-0 cursor-pointer"
          >
            Select My Clubs
          </button>
        )}
      </div>
    );
  }

  return (
    <section className={`space-y-3.5 my-6 ${className}`}>
      {/* Header */}
      <div className="flex items-center justify-between pb-2 border-b border-[#334155]/60">
        <div className="flex items-center gap-2">
          <ShoppingBag className="w-4 h-4 text-[#7098F8]" />
          <h2 className="text-sm sm:text-base font-extrabold text-white tracking-tight">
            My Clubs Official Stores &amp; Match Gear
          </h2>
          <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-[#1E3A8A]/40 text-[#7098F8] border border-[#7098F8]/30">
            {followedClubs.length} Followed
          </span>
        </div>
        <div className="flex items-center gap-3">
          <span className="hidden sm:inline text-[11px] text-[#94A3B8]">
            {marketplace}
          </span>
          <Link
            href="/store"
            className="flex items-center gap-1 text-xs font-bold text-[#7098F8] hover:text-[#4FC3F7] transition-colors"
          >
            <span>All Stores</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>
      </div>

      {/* Cards Shelf */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
        {followedClubs.map((club) => {
          const jerseyPhoto = resolveClubKitPhoto(club.name, club.crest_url);
          const affiliateUrl = buildClubGearUrl(club.name, 'jersey', country);
          const isCustomPhoto = jerseyPhoto && jerseyPhoto !== club.crest_url;

          return (
            <a
              key={club.id}
              href={affiliateUrl}
              target="_blank"
              rel="noopener noreferrer sponsored"
              className="group bg-[#181C20] hover:bg-[#1E2227] border border-[#334155]/60 hover:border-[#7098F8]/60 rounded-2xl p-3.5 transition-all shadow-sm hover:shadow-[0_0_16px_rgba(112,152,248,0.15)] flex flex-col justify-between"
            >
              <div>
                <div className="relative w-full h-32 rounded-xl bg-gradient-to-b from-[#101418] to-[#1E2227] border border-[#334155]/40 flex items-center justify-center overflow-hidden mb-3">
                  {isCustomPhoto ? (
                    <Image
                      src={jerseyPhoto}
                      alt={`${club.name} Kit`}
                      fill
                      className="object-contain p-2 group-hover:scale-105 transition-transform duration-300"
                      unoptimized
                    />
                  ) : club.crest_url ? (
                    <div className="group-hover:scale-105 transition-transform">
                      <GlassLogoBadge
                        src={club.crest_url}
                        alt={club.name}
                        size={56}
                      />
                    </div>
                  ) : (
                    <ShoppingBag className="w-8 h-8 text-[#7098F8]/40" />
                  )}

                  {club.crest_url && isCustomPhoto && (
                    <div className="absolute top-2 right-2">
                      <GlassLogoBadge
                        src={club.crest_url}
                        alt=""
                        size={28}
                      />
                    </div>
                  )}
                </div>

                <div className="space-y-1">
                  <h4 className="text-xs sm:text-sm font-bold text-white group-hover:text-[#7098F8] transition-colors line-clamp-1">
                    {club.name}
                  </h4>
                  <span className="text-[10px] text-[#94A3B8] uppercase tracking-wider font-semibold block">
                    {club.competition_name || 'Official Store'}
                  </span>
                  <div className="flex items-center gap-1.5 text-[11px] text-[#94A3B8] pt-1">
                    <div className="flex items-center text-amber-400">
                      <Star className="w-3 h-3 fill-current" />
                      <span className="font-bold ml-1 text-white">4.8</span>
                    </div>
                    <span>•</span>
                    <span className="text-[#4FC3F7] font-semibold">Authentic Gear</span>
                  </div>
                </div>
              </div>

              <div className="mt-3.5 w-full py-1.5 px-3 rounded-xl bg-[#1E3A8A]/50 group-hover:bg-[#1E3A8A] text-[#7098F8] group-hover:text-white border border-[#7098F8]/30 flex items-center justify-center gap-1.5 text-xs font-bold transition-all">
                <ShoppingBag className="w-3.5 h-3.5" />
                <span>Visit Store on Amazon</span>
                <ExternalLink className="w-3 h-3" />
              </div>
            </a>
          );
        })}
      </div>

      <p className="text-[10px] text-[#94A3B8]/60 leading-relaxed pt-1">
        {AMAZON_AFFILIATE_DISCLOSURE}
      </p>
    </section>
  );
}
