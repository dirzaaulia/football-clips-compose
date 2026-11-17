'use client';

import React, { useState } from 'react';
import Image from 'next/image';
import { ShoppingBag, Star, ExternalLink, ShieldCheck } from 'lucide-react';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';
import { buildAmazonSearchUrl, resolveClubKitPhoto } from '@/lib/affiliate/amazon';

export type StoreKitCategory = 'ALL' | 'HOME' | 'AWAY' | 'RETRO' | 'TRAINING' | 'ACCESSORIES';

interface ClubStoreCardProps {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  club: any;
  country?: string | null;
  category?: StoreKitCategory;
  isFavorite?: boolean;
}

const AVAILABLE_SIZES = ['S', 'M', 'L', 'XL', '2XL'];

const STAR_PLAYER_PRINTS: Record<string, { print: string; tech: string; accent: string }> = {
  arsenal: { print: 'Custom Saka #7 • Ødegaard #8', tech: 'AEROREADY', accent: 'from-red-600/20' },
  'real madrid': { print: 'Custom Bellingham #5 • Mbappé #9', tech: 'HEAT.RDY', accent: 'from-amber-500/20' },
  'manchester city': { print: 'Custom Haaland #9 • Foden #47', tech: 'ULTRAWEAVE', accent: 'from-sky-500/20' },
  'man city': { print: 'Custom Haaland #9 • Foden #47', tech: 'ULTRAWEAVE', accent: 'from-sky-500/20' },
  barcelona: { print: 'Custom L. Yamal #19 • Pedri #8', tech: 'DRI-FIT ADV', accent: 'from-blue-600/20' },
  liverpool: { print: 'Custom Salah #11 • Van Dijk #4', tech: 'DRI-FIT ADV', accent: 'from-red-700/20' },
  bayern: { print: 'Custom Kane #9 • Musiala #42', tech: 'HEAT.RDY', accent: 'from-rose-600/20' },
  inter: { print: 'Custom Lautaro #10 • Barella #23', tech: 'DRI-FIT ADV', accent: 'from-blue-700/20' },
  milan: { print: 'Custom Leão #10 • Retro Kaká #22', tech: 'ULTRAWEAVE', accent: 'from-red-800/20' },
  juventus: { print: 'Custom Yildiz #10 • Vlahović #9', tech: 'AEROREADY', accent: 'from-zinc-500/20' },
  psg: { print: 'Custom Barcola #29 • Hakimi #2', tech: 'DRI-FIT ADV', accent: 'from-indigo-600/20' },
  chelsea: { print: 'Custom Palmer #20 • Enzo #8', tech: 'DRI-FIT ADV', accent: 'from-blue-600/20' },
  'manchester united': { print: 'Custom Bruno #8 • Garnacho #17', tech: 'AEROREADY', accent: 'from-red-600/20' },
  dortmund: { print: 'Custom Brandt #10 • Guirassy #9', tech: 'ULTRAWEAVE', accent: 'from-yellow-500/20' },
};

function getClubMerchMeta(clubName: string, category: StoreKitCategory) {
  const lower = clubName.toLowerCase();
  const matchedKey = Object.keys(STAR_PLAYER_PRINTS).find((k) => lower.includes(k));
  const preset = matchedKey
    ? STAR_PLAYER_PRINTS[matchedKey]
    : {
        print: 'Official Squad Name & Number Print',
        tech: 'AUTHENTIC PRO',
        accent: 'from-[#DFFF00]/10',
      };

  const catMeta: Record<
    StoreKitCategory,
    { badge: string; titleSuffix: string; querySuffix: string; estRange: string }
  > = {
    ALL: {
      badge: 'Home 26/27',
      titleSuffix: '2026/27 Authentic Home Match Shirt',
      querySuffix: 'official football jersey kit',
      estRange: 'Est. $90 – $115',
    },
    HOME: {
      badge: 'Home 26/27',
      titleSuffix: '2026/27 Authentic Home Match Shirt',
      querySuffix: 'home football jersey shirt',
      estRange: 'Est. $90 – $115',
    },
    AWAY: {
      badge: 'Away / Third',
      titleSuffix: '2026/27 Official Away & Third Kit',
      querySuffix: 'away third football jersey shirt',
      estRange: 'Est. $90 – $115',
    },
    RETRO: {
      badge: 'Retro Remake',
      titleSuffix: 'Heritage Vintage Collector Jersey',
      querySuffix: 'retro vintage classic football shirt',
      estRange: 'Est. $65 – $95',
    },
    TRAINING: {
      badge: 'Pre-Match Pro',
      titleSuffix: '2026/27 Pro Training & Sideline Top',
      querySuffix: 'football training top jacket',
      estRange: 'Est. $55 – $85',
    },
    ACCESSORIES: {
      badge: 'Matchday Gear',
      titleSuffix: 'Official Stadium Scarf & Match Cap',
      querySuffix: 'official football club scarf cap',
      estRange: 'Est. $25 – $45',
    },
  };

  return { ...preset, ...catMeta[category] };
}

export default function ClubStoreCard({
  club,
  country,
  category = 'ALL',
  isFavorite = false,
}: ClubStoreCardProps) {
  const displayName = club.short_name || club.shortName || club.name;
  const crestUrl = club.crest_url || club.crestUrl || club.crest || '';
  const kitPhoto = resolveClubKitPhoto(club.name);
  const [imgError, setImgError] = useState(false);
  const meta = getClubMerchMeta(club.name, category);
  const gearUrl = buildAmazonSearchUrl(`${club.name} ${meta.querySuffix}`, country);
  const leagueCode = club.competition_code || club.leagueCode || 'OFFICIAL';
  const hasCustomPhoto = Boolean(kitPhoto && !imgError);

  return (
    <a
      href={gearUrl}
      target="_blank"
      rel="noopener noreferrer"
      className="group flex flex-col rounded-xl bg-[#1B1B1F] border border-white/10 overflow-hidden hover:border-[#DFFF00]/50 transition-all duration-300 shadow-lg hover:shadow-[#DFFF00]/5 cursor-pointer"
    >
      {/* Top Jersey Visual Showcase (h-60) */}
      <div
        className={`relative bg-gradient-to-b ${meta.accent} to-[#222328] p-5 flex flex-col items-center justify-center h-60 overflow-hidden`}
      >
        <span className="absolute top-3 left-3 px-2 py-0.5 rounded bg-black/70 backdrop-blur-sm border border-white/10 text-[#DFFF00] font-tech text-[10px] uppercase font-bold tracking-wider z-10">
          {meta.tech}
        </span>
        <span className="absolute top-3 right-3 px-2 py-0.5 rounded bg-[#DFFF00] text-[#0A0A0C] font-tech text-[10px] uppercase font-extrabold z-10">
          {isFavorite ? 'Followed Club' : meta.badge}
        </span>

        {hasCustomPhoto && kitPhoto ? (
          <div className="relative w-40 h-44 group-hover:scale-105 transition-transform duration-300">
            <Image
              src={kitPhoto}
              alt={`${displayName} ${meta.titleSuffix}`}
              fill
              className="object-contain drop-shadow-2xl"
              onError={() => setImgError(true)}
              unoptimized
            />
          </div>
        ) : (
          /* Stitch Jersey Visual Styling Card */
          <div className="w-36 h-44 rounded-lg bg-[#121316] border-2 border-white/15 shadow-2xl flex flex-col items-center justify-between p-3 relative transform group-hover:scale-105 transition-transform duration-300">
            <div className="w-full flex justify-between items-center text-[10px] text-white/70 font-tech font-bold">
              <span>{displayName.slice(0, 3).toUpperCase()}</span>
              <GlassLogoBadge
                src={crestUrl}
                alt={displayName}
                size={20}
                imageClassName="object-contain"
              />
            </div>
            <div className="text-center my-auto">
              <GlassLogoBadge
                src={crestUrl}
                alt={displayName}
                size={44}
                className="mx-auto mb-1"
                imageClassName="object-contain p-1"
              />
              <span className="text-[9px] font-tech tracking-widest uppercase text-[#DFFF00] block font-bold">
                AUTHENTIC KIT
              </span>
            </div>
            <div className="w-full h-1.5 bg-gradient-to-r from-transparent via-[#DFFF00]/60 to-transparent rounded" />
          </div>
        )}
      </div>

      {/* Bottom Details & Amazon CTA */}
      <div className="p-5 flex-1 flex flex-col justify-between bg-[#1B1B1F]">
        <div>
          <div className="flex items-center justify-between text-xs text-white/50 font-tech mb-1">
            <span>{displayName} • {leagueCode}</span>
            <span className="flex items-center gap-0.5 text-amber-400 font-bold">
              <Star className="w-3 h-3 fill-amber-400" />
              <span>Verified</span>
            </span>
          </div>

          <h3 className="font-bold text-white text-sm sm:text-base leading-snug group-hover:text-[#DFFF00] transition-colors line-clamp-2">
            {displayName} {meta.titleSuffix}
          </h3>

          <div className="mt-3 flex flex-wrap items-center gap-1.5">
            <span className="text-[11px] px-2 py-0.5 rounded bg-[#292A2E] border border-white/5 text-white/80 font-tech">
              {meta.print}
            </span>
          </div>

          {/* Size Pills */}
          <div className="flex items-center justify-between pt-3 text-[10px] font-tech text-white/45">
            <span>Sizes:</span>
            <div className="flex items-center gap-1">
              {AVAILABLE_SIZES.map((s) => (
                <span
                  key={s}
                  className="px-1.5 py-0.5 rounded bg-[#0D0E11] border border-white/10 text-white/70"
                >
                  {s}
                </span>
              ))}
            </div>
          </div>
        </div>

        {/* Price Estimate & Shop Button */}
        <div className="mt-4 pt-3.5 border-t border-white/10 flex items-center justify-between gap-2">
          <div>
            <span className="text-[10px] text-white/45 uppercase font-tech block flex items-center gap-1">
              <ShieldCheck className="w-3 h-3 text-emerald-400" />
              <span>Official Licensed</span>
            </span>
            <span className="font-scoreboard text-xl text-white leading-none">
              {meta.estRange}
            </span>
          </div>

          <span className="px-3.5 py-2 rounded-lg bg-[#DFFF00] group-hover:bg-[#cbe800] text-[#0A0A0C] font-tech text-xs font-extrabold uppercase tracking-wider transition-colors flex items-center gap-1.5 shadow-sm">
            <ShoppingBag className="w-3.5 h-3.5" />
            <span>Shop Amazon</span>
            <ExternalLink className="w-3 h-3" />
          </span>
        </div>
      </div>
    </a>
  );
}
