import React, { useState } from 'react';
import Image from 'next/image';
import { Play, Radio, Film, Clock, EyeOff } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';
import { useVideoPlayer } from '@/hooks/useVideoPlayer';
import { useRouter } from 'next/navigation';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';

interface FixtureItemCardProps {
  match: MatchRecord;
  showLeagueHeader?: boolean;
}

export default function FixtureItemCard({ match, showLeagueHeader = false }: FixtureItemCardProps) {
  const router = useRouter();
  const { isSpoilerFree } = useSpoilerFree();
  const { playVideo } = useVideoPlayer();
  const isFinished = match.status === 'FINISHED';
  const isLive = match.status === 'IN_PLAY' || match.status === 'PAUSED' || match.status === 'LIVE';
  const isPaused = match.status === 'PAUSED';
  const hasHighlight = Boolean(match.highlight_video_id);

  const homeName = match.home_team_name || match.home_team;
  const awayName = match.away_team_name || match.away_team;
  const matchTime = new Date(match.utc_date).toLocaleTimeString('en-GB', {
    hour: '2-digit',
    minute: '2-digit',
  });

  // Live matches never hide score; spoiler shielding only applies to finished matches
  const shouldHideScore = isSpoilerFree && isFinished;

  return (
    <div
      onClick={() => {
        if (shouldHideScore) {
          router.push(`/match/${match.id}`);
        } else {
          playVideo(match);
        }
      }}
      className={`py-2.5 px-3 sm:px-4 transition-colors flex items-center justify-between gap-2 sm:gap-4 hover:bg-[#22272E] cursor-pointer ${
        isLive
          ? 'bg-gradient-to-r from-red-950/20 via-[#181C20] to-red-950/20 border-l-2 border-l-red-500'
          : 'bg-transparent'
      }`}
    >
      {/* Left Column: Match Status & Time */}
      <div className="w-16 sm:w-28 shrink-0 flex flex-col items-start justify-center">
        {showLeagueHeader && (
          <span className="text-[9px] uppercase font-bold text-[#7098F8] tracking-wider line-clamp-2 leading-tight max-w-[110px] mb-0.5">
            {match.competition_name || match.competition_id}
          </span>
        )}

        {isLive ? (
          <div className="flex flex-col items-start gap-0.5">
            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[10px] font-black bg-red-500/20 text-red-400 border border-red-500/30 uppercase animate-pulse">
              <span className="w-1.5 h-1.5 rounded-full bg-red-500" />
              <span>{isPaused ? 'HT' : match.display_clock ? `LIVE ${match.display_clock}` : 'LIVE'}</span>
            </span>
            <span className="text-[9px] text-[#94A3B8] font-semibold pl-0.5">
              KO {matchTime}
            </span>
          </div>
        ) : isFinished ? (
          hasHighlight ? (
            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[10px] font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
              <Film className="w-3 h-3" />
              <span>FT</span>
            </span>
          ) : match.viewer_availability === 'country_blocked' ? (
            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[10px] font-semibold bg-amber-500/20 text-amber-300 border border-amber-500/30" title="Unavailable in your country">
              <span>FT · Blocked</span>
            </span>
          ) : match.viewer_availability === 'unknown' ? (
            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[10px] font-semibold bg-amber-500/20 text-amber-300 border border-amber-500/30" title="Availability not verified">
              <span>FT · Unverified</span>
            </span>
          ) : (
            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[10px] font-semibold bg-[#101418] text-[#94A3B8] border border-[#334155]">
              <span>FT</span>
            </span>
          )
        ) : (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[10px] font-semibold bg-[#101418] text-[#94A3B8] border border-[#334155]">
            <Clock className="w-3 h-3 text-[#7098F8]" />
            <span>{matchTime}</span>
          </span>
        )}
      </div>

      {/* Center Column: Teams & Scoreline / VS */}
      <div className="flex-1 min-w-0 grid grid-cols-[1fr_auto_1fr] items-center gap-1.5 sm:gap-3">
        {/* Home Team */}
        <div className="flex items-center justify-end gap-1.5 sm:gap-2.5 min-w-0">
          <span className="text-xs sm:text-sm font-bold text-white text-right line-clamp-2 leading-tight">{homeName}</span>
          <GlassLogoBadge
            src={match.home_team_crest}
            alt={homeName}
            size={28}
            imageClassName="object-contain p-0.5"
          />
        </div>

        {/* Score Badge */}
        <div className="flex justify-center shrink-0">
          {shouldHideScore ? (
            <button
              onClick={(e) => {
                e.stopPropagation();
                router.push(`/match/${match.id}`);
              }}
              title="Click to view match centre"
              className="flex items-center gap-1 px-2.5 py-1 rounded-lg bg-[#101418] border border-[#334155] hover:border-[#7098F8]/60 text-xs font-bold text-[#94A3B8] hover:text-white transition-all cursor-pointer min-w-[56px] sm:min-w-[64px] justify-center shadow-inner"
            >
              <EyeOff className="w-3 h-3 text-[#7098F8]" />
              <span className="tracking-wider">? - ?</span>
            </button>
          ) : (
            <div
              className={`px-2.5 py-1 rounded-lg text-center min-w-[56px] sm:min-w-[64px] flex items-center justify-center shadow-inner ${
                isLive
                  ? 'bg-[#101418] border border-red-500/50 shadow-[0_0_8px_rgba(239,68,68,0.25)]'
                  : 'bg-[#101418] border border-[#334155]'
              }`}
            >
              <span
                className={`text-xs sm:text-sm font-black tracking-wider ${
                  isLive ? 'text-red-400' : 'text-white'
                }`}
              >
                {isFinished || isLive ? `${match.home_score ?? 0} - ${match.away_score ?? 0}` : 'vs'}
              </span>
            </div>
          )}
        </div>

        {/* Away Team */}
        <div className="flex items-center justify-start gap-1.5 sm:gap-2.5 min-w-0">
          <GlassLogoBadge
            src={match.away_team_crest}
            alt={awayName}
            size={28}
            imageClassName="object-contain p-0.5"
          />
          <span className="text-xs sm:text-sm font-bold text-white text-left line-clamp-2 leading-tight">{awayName}</span>
        </div>
      </div>

      {/* Right Column: Contextual Action CTA */}
      <div className="w-auto sm:w-28 shrink-0 flex justify-end">
        {isLive ? (
          <button
            onClick={(e) => {
              e.stopPropagation();
              playVideo(match);
            }}
            className="flex items-center gap-1 px-2.5 sm:px-3 py-1 rounded-lg bg-gradient-to-r from-red-600 to-red-500 hover:from-red-500 hover:to-red-400 text-white text-[11px] sm:text-xs font-black shadow-[0_0_10px_rgba(239,68,68,0.3)] transition-all cursor-pointer"
          >
            <Radio className="w-3 h-3 animate-pulse" />
            <span className="hidden sm:inline">Centre</span>
            <span className="sm:hidden">Live</span>
          </button>
        ) : hasHighlight ? (
          <button
            onClick={(e) => {
              e.stopPropagation();
              playVideo(match);
            }}
            className="flex items-center gap-1 px-2.5 sm:px-3 py-1 rounded-lg bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white text-[11px] sm:text-xs font-bold shadow-[0_0_10px_rgba(112,152,248,0.3)] hover:brightness-110 transition-all cursor-pointer"
          >
            <Play className="w-3 h-3 fill-current" />
            <span>Watch</span>
          </button>
        ) : match.viewer_availability === 'unknown' && match.external_video_id ? (
          <a
            href={`https://www.youtube.com/watch?v=${match.external_video_id}`}
            target="_blank"
            rel="noopener noreferrer"
            onClick={(e) => e.stopPropagation()}
            className="flex items-center gap-1 px-2 sm:px-2.5 py-1 text-[11px] sm:text-xs text-amber-400 font-bold bg-[#101418] rounded-lg border border-amber-500/40 hover:border-amber-400 transition-all cursor-pointer"
            title="Availability not verified · Watch externally on YouTube"
          >
            <span>YouTube ↗</span>
          </a>
        ) : (
          <button
            onClick={(e) => {
              e.stopPropagation();
              playVideo(match);
            }}
            className="flex items-center gap-1 px-2 sm:px-2.5 py-1 text-[11px] sm:text-xs text-[#94A3B8] hover:text-white font-semibold bg-[#101418] rounded-lg border border-[#334155]/70 hover:border-[#7098F8]/50 transition-all cursor-pointer"
          >
            <span>Details</span>
          </button>
        )}
      </div>
    </div>
  );
}



