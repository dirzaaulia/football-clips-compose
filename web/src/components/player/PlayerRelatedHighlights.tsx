'use client';

import React from 'react';
import Image from 'next/image';
import { Play } from 'lucide-react';
import { MatchRecord } from '@/lib/database';

interface PlayerRelatedHighlightsProps {
  relatedMatches: MatchRecord[];
  onSelectMatch?: (match: MatchRecord) => void;
}

export default function PlayerRelatedHighlights({
  relatedMatches,
  onSelectMatch,
}: PlayerRelatedHighlightsProps) {
  if (relatedMatches.length === 0) return null;

  return (
    <div className="space-y-2.5 pt-1">
      <h3 className="text-xs font-extrabold text-white tracking-wide uppercase text-[#94A3B8]">
        More Highlights
      </h3>
      <div className="flex items-center gap-3 overflow-x-auto pb-2 scrollbar-none touch-pan-x">
        {relatedMatches.slice(0, 6).map((rel) => {
          const relThumb = rel.highlight_video_id
            ? `https://i.ytimg.com/vi/${rel.highlight_video_id}/mqdefault.jpg`
            : '/favicon.png';
          const relHome = rel.home_team_name || rel.home_team;
          const relAway = rel.away_team_name || rel.away_team;

          return (
            <div
              key={rel.id}
              onClick={() => onSelectMatch?.(rel)}
              className="w-[180px] shrink-0 bg-[#101418] hover:bg-[#1E2227] border border-[#334155] hover:border-[#7098F8]/60 rounded-xl overflow-hidden p-2 cursor-pointer transition-all flex flex-col justify-between group"
            >
              <div className="relative w-full aspect-video rounded-lg overflow-hidden bg-black mb-1.5">
                <Image
                  src={relThumb}
                  alt={`${relHome} vs ${relAway}`}
                  fill
                  className="object-cover group-hover:scale-105 transition-transform"
                  unoptimized
                />
                <div className="absolute inset-0 bg-black/20 group-hover:bg-black/0 transition-colors" />
                <div className="absolute bottom-1 right-1 px-1.5 py-0.5 rounded bg-black/75 text-[10px] font-bold text-white flex items-center gap-0.5">
                  <Play className="w-2.5 h-2.5 fill-white" />
                </div>
              </div>
              <p className="text-[11px] font-bold text-white line-clamp-1 group-hover:text-[#7098F8] transition-colors">
                {relHome} vs {relAway}
              </p>
              <span className="text-[10px] text-[#94A3B8] line-clamp-1 mt-0.5">
                {rel.competition_name}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
}
