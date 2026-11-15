'use client';

import React from 'react';
import Image from 'next/image';
import { ShoppingBag, Star, ExternalLink, ShieldCheck } from 'lucide-react';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';
import { buildClubGearUrl, resolveClubKitPhoto } from '@/lib/affiliate/amazon';

interface ClubStoreCardProps {
  club: any;
  country?: string | null;
  isFavorite?: boolean;
}

export default function ClubStoreCard({ club, country, isFavorite = false }: ClubStoreCardProps) {
  const kitPhoto = resolveClubKitPhoto(club.id, club.name);
  const gearUrl = buildClubGearUrl(club.name, club.short_name || club.name, country);
  const displayName = club.short_name || club.shortName || club.name;

  return (
    <a
      href={gearUrl}
      target="_blank"
      rel="noopener noreferrer"
      className="group relative rounded-2xl bg-[#1A1C22] border border-white/10 hover:border-[#DFFF00]/50 p-4 transition-all duration-300 hover:shadow-xl hover:shadow-[#DFFF00]/5 flex flex-col justify-between overflow-hidden cursor-pointer"
    >
      <div className="space-y-3">
        {/* Card Header */}
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center gap-2.5 min-w-0">
            <GlassLogoBadge
              src={club.crest_url || club.crest}
              alt={displayName}
              size={28}
              imageClassName="object-contain p-0.5"
            />
            <span className="font-tech font-bold text-sm text-white group-hover:text-[#DFFF00] transition-colors truncate">
              {displayName}
            </span>
          </div>

          {isFavorite && (
            <span className="flex items-center gap-1 px-2 py-0.5 rounded-full bg-[#DFFF00]/15 text-[#DFFF00] border border-[#DFFF00]/30 text-[10px] font-tech font-bold uppercase tracking-wider shrink-0">
              <Star className="w-2.5 h-2.5 fill-current" />
              <span>Fav</span>
            </span>
          )}
        </div>

        {/* Kit Preview Photo */}
        <div className="relative aspect-[4/3] w-full rounded-xl overflow-hidden bg-[#121316] border border-white/5 flex items-center justify-center">
          {kitPhoto ? (
            <Image
              src={kitPhoto}
              alt={`${displayName} Official Kit`}
              fill
              className="object-cover group-hover:scale-105 transition-transform duration-500"
              unoptimized
            />
          ) : (
            <div className="flex flex-col items-center justify-center gap-2 text-white/30">
              <ShoppingBag className="w-8 h-8 text-[#DFFF00]/40" />
              <span className="text-[10px] font-tech uppercase tracking-wider">Official Gear</span>
            </div>
          )}
          <div className="absolute inset-0 bg-gradient-to-t from-[#121316] via-transparent to-transparent opacity-60" />
        </div>
      </div>

      {/* Card Action Footer */}
      <div className="pt-3 mt-3 border-t border-white/5 flex items-center justify-between gap-2 text-xs">
        <span className="text-[11px] font-tech text-white/50 flex items-center gap-1">
          <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
          <span>Licensed Gear</span>
        </span>
        <span className="inline-flex items-center gap-1 font-tech font-bold text-xs text-[#DFFF00] group-hover:translate-x-1 transition-transform">
          <span>Shop Amazon</span>
          <ExternalLink className="w-3 h-3" />
        </span>
      </div>
    </a>
  );
}
