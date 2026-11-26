'use client';

import React from 'react';
import Link from 'next/link';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';

export interface TickerMatchItem {
  id: number;
  home_team_name: string;
  home_team_crest?: string | null;
  away_team_name: string;
  away_team_crest?: string | null;
  home_score?: number | null;
  away_score?: number | null;
  display_clock?: string | null;
  status: string;
  competition_name: string;
  competition_id?: string;
  utc_date?: string;
}

export type TickerMode = 'LIVE' | 'UPCOMING' | 'RECENT';

export function formatKickoff(utcDate?: string): string {
  if (!utcDate) return 'TBD';
  try {
    const d = new Date(utcDate);
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false });
  } catch {
    return 'TBD';
  }
}

export default function TickerMarqueeRow({
  matches,
  mode,
  duration,
  isSpoilerFree,
  unblurredIds,
  onToggleUnblur,
  ariaHidden = false,
}: {
  matches: TickerMatchItem[];
  mode: TickerMode;
  duration: number;
  isSpoilerFree: boolean;
  unblurredIds: Set<number>;
  onToggleUnblur: (id: number, e: React.MouseEvent) => void;
  ariaHidden?: boolean;
}) {
  return (
    <div
      aria-hidden={ariaHidden}
      className="flex items-center gap-2.5 pr-2.5 shrink-0 animate-marquee group-hover:[animation-play-state:paused]"
      style={{ animationDuration: `${duration}s` }}
    >
      {matches.map((match, idx) => {
        const isLive = mode === 'LIVE' || match.status === 'IN_PLAY';
        const isUpcoming = mode === 'UPCOMING' || match.status === 'TIMED' || match.status === 'SCHEDULED';
        const shouldBlur = isSpoilerFree && !unblurredIds.has(match.id) && !isUpcoming;
        const scoreText =
          match.home_score !== null && match.away_score !== null
            ? `${match.home_score} - ${match.away_score}`
            : 'VS';

        return (
          <Link
            key={`${match.id}-${ariaHidden ? 'dup' : 'orig'}-${idx}`}
            href={`/match/${match.id}`}
            className="flex items-center gap-2 bg-[#121316] hover:bg-[#1A1C22] border border-white/10 hover:border-white/25 transition-all rounded-lg px-3 py-1 text-xs shrink-0 group select-none"
          >
            <span
              className={`text-[11px] font-tech font-black tracking-tight shrink-0 uppercase ${
                isLive ? 'text-[#FF2E4D]' : isUpcoming ? 'text-[#DFFF00]' : 'text-white/70'
              }`}
            >
              {isLive ? match.display_clock || 'LIVE' : isUpcoming ? formatKickoff(match.utc_date) : 'FT'}
            </span>

            <div className="flex items-center gap-1.5 text-gray-200 shrink-0">
              <GlassLogoBadge
                src={match.home_team_crest}
                alt={match.home_team_name}
                size={20}
                imageClassName="object-contain p-0.5"
              />
              <span className="whitespace-nowrap font-extrabold text-xs text-white">
                {match.home_team_name}
              </span>

              <button
                type="button"
                onClick={(e) => onToggleUnblur(match.id, e)}
                className={`font-black text-xs px-2 py-0.5 rounded transition-all cursor-pointer ${
                  shouldBlur
                    ? 'filter blur-xs hover:blur-none bg-[#1A1C22] text-[#DFFF00]'
                    : isUpcoming
                    ? 'bg-white/10 text-white/80 font-bold'
                    : 'bg-black/80 text-[#DFFF00] border border-white/15'
                }`}
                title={shouldBlur ? 'Click to unblur score' : undefined}
              >
                {scoreText}
              </button>

              <span className="whitespace-nowrap font-extrabold text-xs text-white">
                {match.away_team_name}
              </span>
              <GlassLogoBadge
                src={match.away_team_crest}
                alt={match.away_team_name}
                size={20}
                imageClassName="object-contain p-0.5"
              />
            </div>

            {match.competition_id && (
              <span className="text-[10px] font-tech font-bold text-white/60 uppercase ml-1">
                {match.competition_id}
              </span>
            )}
          </Link>
        );
      })}
    </div>
  );
}
