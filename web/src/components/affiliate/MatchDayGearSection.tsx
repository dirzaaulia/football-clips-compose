'use client';

import React from 'react';
import Image from 'next/image';
import { ShoppingBag, Star, ExternalLink } from 'lucide-react';
import { useUserRegion } from '@/hooks/useUserRegion';
import {
  buildClubGearUrl,
  resolveClubKitPhoto,
  resolveAmazonMarketplace,
  AMAZON_AFFILIATE_DISCLOSURE,
} from '@/lib/affiliate/amazon';

interface MatchDayGearSectionProps {
  homeName: string;
  homeCrest?: string | null;
  awayName: string;
  awayCrest?: string | null;
  className?: string;
}

export default function MatchDayGearSection({
  homeName,
  homeCrest,
  awayName,
  awayCrest,
  className = '',
}: MatchDayGearSectionProps) {
  const { country } = useUserRegion();
  const marketplace = resolveAmazonMarketplace(country);

  return (
    <section className={`bg-[#14181D] border border-[#334155]/70 rounded-2xl p-4 sm:p-5 space-y-4 shadow-lg ${className}`}>
      {/* Header */}
      <div className="flex items-center justify-between border-b border-[#334155]/60 pb-3">
        <div className="flex items-center gap-2">
          <ShoppingBag className="w-4 h-4 text-[#7098F8]" />
          <h3 className="text-xs sm:text-sm font-extrabold text-white tracking-wide uppercase">
            Match-Day Fan Gear &amp; Official Kits
          </h3>
        </div>
        <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-[#1E3A8A]/50 text-[#7098F8] border border-[#7098F8]/30">
          {marketplace}
        </span>
      </div>

      {/* Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
        <TeamMerchandiseCard
          teamName={homeName}
          crestUrl={homeCrest}
          country={country}
        />
        <TeamMerchandiseCard
          teamName={awayName}
          crestUrl={awayCrest}
          country={country}
        />
      </div>

      {/* Compliance Disclosure */}
      <p className="text-[10px] text-[#94A3B8]/70 leading-relaxed pt-1">
        {AMAZON_AFFILIATE_DISCLOSURE}
      </p>
    </section>
  );
}

function TeamMerchandiseCard({
  teamName,
  crestUrl,
  country,
}: {
  teamName: string;
  crestUrl?: string | null;
  country: string | null;
}) {
  const jerseyPhoto = resolveClubKitPhoto(teamName, crestUrl);
  const affiliateUrl = buildClubGearUrl(teamName, 'jersey', country);
  const isCustomPhoto = jerseyPhoto && jerseyPhoto !== crestUrl;

  return (
    <a
      href={affiliateUrl}
      target="_blank"
      rel="noopener noreferrer sponsored"
      className="group block bg-[#181C20] hover:bg-[#1E2227] border border-[#334155]/60 hover:border-[#7098F8]/60 rounded-xl p-3 transition-all shadow-sm hover:shadow-[0_0_16px_rgba(112,152,248,0.15)] flex flex-col justify-between"
    >
      <div>
        {/* Product Image Frame */}
        <div className="relative w-full h-32 sm:h-36 rounded-lg bg-gradient-to-b from-[#101418] to-[#1E2227] border border-[#334155]/40 flex items-center justify-center overflow-hidden mb-3">
          {isCustomPhoto ? (
            <Image
              src={jerseyPhoto}
              alt={`${teamName} Official Jersey`}
              fill
              className="object-contain p-2 group-hover:scale-105 transition-transform duration-300"
              unoptimized
            />
          ) : crestUrl ? (
            <div className="relative w-16 h-16 opacity-85 group-hover:opacity-100 group-hover:scale-110 transition-transform">
              <Image
                src={crestUrl}
                alt={teamName}
                fill
                className="object-contain"
                unoptimized
              />
            </div>
          ) : (
            <ShoppingBag className="w-10 h-10 text-[#7098F8]/40" />
          )}

          {/* Crest Overlay */}
          {crestUrl && isCustomPhoto && (
            <div className="absolute top-2 right-2 w-7 h-7 rounded-md bg-black/60 p-1 border border-[#334155]/60 flex items-center justify-center">
              <Image
                src={crestUrl}
                alt=""
                width={20}
                height={20}
                className="object-contain"
                unoptimized
              />
            </div>
          )}
        </div>

        {/* Title & Ratings */}
        <div className="space-y-1">
          <h4 className="text-xs font-bold text-white group-hover:text-[#7098F8] transition-colors line-clamp-2 leading-tight">
            {teamName} Official Jersey &amp; Fan Gear
          </h4>
          <div className="flex items-center gap-1.5 text-[11px] text-[#94A3B8]">
            <div className="flex items-center text-amber-400">
              <Star className="w-3 h-3 fill-current" />
              <span className="font-bold ml-1 text-white">4.8</span>
            </div>
            <span>•</span>
            <span className="text-[#4FC3F7] font-semibold">Official Gear</span>
          </div>
        </div>
      </div>

      {/* CTA Button */}
      <div className="mt-3 w-full py-1.5 px-3 rounded-lg bg-[#1E3A8A]/50 group-hover:bg-[#1E3A8A] text-[#7098F8] group-hover:text-white border border-[#7098F8]/30 flex items-center justify-center gap-1.5 text-xs font-bold transition-all">
        <ShoppingBag className="w-3.5 h-3.5" />
        <span>Shop on Amazon</span>
        <ExternalLink className="w-3 h-3" />
      </div>
    </a>
  );
}
