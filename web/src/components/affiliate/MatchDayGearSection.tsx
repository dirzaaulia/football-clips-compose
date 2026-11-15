'use client';

import React from 'react';
import Image from 'next/image';
import { ShoppingBag, Star, ExternalLink } from 'lucide-react';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';
import { useUserRegion } from '@/hooks/useUserRegion';
import { useMerchandisePreference } from '@/hooks/useMerchandisePreference';
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
  const { isMerchandiseEnabled } = useMerchandisePreference();
  const { country } = useUserRegion();
  const marketplace = resolveAmazonMarketplace(country);

  if (!isMerchandiseEnabled) {
    return null;
  }

  return (
    <section className={`bg-[#121316] border border-white/10 rounded-2xl p-4 sm:p-5 space-y-4 shadow-xl ${className}`}>
      {/* Header */}
      <div className="flex items-center justify-between border-b border-white/10 pb-3">
        <div className="flex items-center gap-2">
          <ShoppingBag className="w-4 h-4 text-[#DFFF00]" />
          <h3 className="text-xs sm:text-sm font-scoreboard text-white tracking-wider uppercase">
            Match-Day Fan Gear &amp; Official Kits
          </h3>
        </div>
        <span className="text-[10px] font-tech font-bold px-2.5 py-0.5 rounded-full bg-[#1A1C22] text-[#DFFF00] border border-[#DFFF00]/30 uppercase">
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
      <p className="text-[10px] font-tech text-white/40 leading-relaxed pt-1">
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
      className="group block bg-[#0A0A0C] hover:bg-[#1A1C22] border border-white/10 hover:border-[#DFFF00]/50 rounded-xl p-3.5 transition-all shadow-sm hover:shadow-[0_0_20px_rgba(223,255,0,0.15)] flex flex-col justify-between cursor-pointer"
    >
      <div>
        {/* Product Image Frame */}
        <div className="relative w-full h-32 sm:h-36 rounded-lg bg-[#121316] border border-white/5 flex items-center justify-center overflow-hidden mb-3">
          {isCustomPhoto ? (
            <Image
              src={jerseyPhoto}
              alt={`${teamName} Official Jersey`}
              fill
              className="object-contain p-2 group-hover:scale-105 transition-transform duration-300"
              unoptimized
            />
          ) : crestUrl ? (
            <div className="group-hover:scale-105 transition-transform duration-300">
              <GlassLogoBadge
                src={crestUrl}
                alt={teamName}
                size={64}
              />
            </div>
          ) : (
            <ShoppingBag className="w-10 h-10 text-white/20" />
          )}

          {/* Crest Overlay */}
          {crestUrl && isCustomPhoto && (
            <div className="absolute top-2 right-2">
              <GlassLogoBadge
                src={crestUrl}
                alt=""
                size={28}
              />
            </div>
          )}
        </div>

        {/* Title & Ratings */}
        <div className="space-y-1">
          <h4 className="text-xs font-tech font-bold text-white group-hover:text-[#DFFF00] transition-colors line-clamp-2 leading-tight">
            {teamName} Official Jersey &amp; Fan Gear
          </h4>
          <div className="flex items-center gap-1.5 text-[11px] font-tech text-white/50">
            <div className="flex items-center text-amber-400">
              <Star className="w-3 h-3 fill-current" />
              <span className="font-bold ml-1 text-white">4.8</span>
            </div>
            <span>•</span>
            <span className="text-[#38BDF8] font-semibold">Official Gear</span>
          </div>
        </div>
      </div>

      {/* CTA Button */}
      <div className="mt-3.5 w-full py-2 px-3 rounded-lg bg-[#DFFF00] group-hover:bg-[#cbe800] text-[#0A0A0C] font-tech text-xs font-black uppercase tracking-wider flex items-center justify-center gap-1.5 shadow-[0_0_12px_rgba(223,255,0,0.3)] transition-all">
        <ShoppingBag className="w-3.5 h-3.5 fill-[#0A0A0C]" />
        <span>Shop on Amazon</span>
        <ExternalLink className="w-3 h-3" />
      </div>
    </a>
  );
}
