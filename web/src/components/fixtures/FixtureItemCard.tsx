'use client';

import React from 'react';
import { Film, Clock, EyeOff, Play, Activity } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';
import { useVideoPlayer } from '@/hooks/useVideoPlayer';
import { useRouter } from 'next/navigation';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';

interface FixtureItemCardProps {
  match: MatchRecord;
  showLeagueHeader?: boolean;
}

export default function FixtureItemCard({ match }: FixtureItemCardProps) {
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

  const shouldHideScore = isSpoilerFree && isFinished;

  return (
    <article
      onClick={() => router.push(`/match/${match.id}`)}
      className={`rounded-xl p-3 sm:p-4 transition-all border flex flex-col md:flex-row md:items-center justify-between gap-3 sm:gap-4 hover:border-white/20 cursor-pointer select-none ${
        isLive
          ? 'bg-gradient-to-r from-[#FF2E4D]/10 via-[#1A1C22] to-[#121316] border-[#FF2E4D]/40 shadow-[0_0_15px_rgba(255,46,77,0.15)]'
          : 'bg-[#1A1C22] border-white/10 hover:bg-[#252830]'
      }`}
    >
      {/* Left: Match Status & Arena */}
      <div className="flex items-center gap-3 shrink-0">
        <div className="flex flex-col items-center justify-center bg-[#0A0A0C] px-3 py-1.5 rounded-lg min-w-[76px] sm:min-w-[84px] border border-white/5">
          {isLive ? (
            <div className="flex items-center gap-1 text-[#FF2E4D] font-tech text-xs uppercase tracking-wider font-black">
              <span className="w-2 h-2 rounded-full bg-[#FF2E4D] animate-ping" />
              <span>{isPaused ? 'HT' : match.display_clock || 'LIVE'}</span>
            </div>
          ) : isFinished ? (
            <span className="font-tech text-xs font-bold text-white/70 uppercase">
              FT
            </span>
          ) : (
            <div className="flex items-center gap-1 font-tech text-xs font-semibold text-white/60">
              <Clock className="w-3 h-3 text-[#38BDF8]" />
              <span>{matchTime}</span>
            </div>
          )}
          <span className="text-[9px] font-tech text-white/40 uppercase mt-0.5">
            {isLive ? '2nd Half' : isFinished ? 'Full Time' : 'Upcoming'}
          </span>
        </div>

        {match.competition_name && (
          <div className="hidden lg:flex flex-col">
            <span className="text-[10px] font-tech uppercase tracking-widest text-[#DFFF00] font-bold line-clamp-1">
              {match.competition_name}
            </span>
            <span className="text-[11px] text-white/50">Official Matchday Feed</span>
          </div>
        )}
      </div>

      {/* Center: Scoreboard Core */}
      <div className="grid grid-cols-[1fr_auto_1fr] items-center flex-1 max-w-2xl mx-auto w-full gap-2 sm:gap-4">
        {/* Home Team */}
        <div className="flex items-center justify-end gap-2 sm:gap-3 min-w-0">
          <span className="text-xs sm:text-sm font-bold text-white text-right line-clamp-1 font-body">
            {homeName}
          </span>
          <GlassLogoBadge
            src={match.home_team_crest}
            alt={homeName}
            size={32}
            imageClassName="object-contain p-0.5"
          />
        </div>

        {/* Score Centerpiece */}
        <div className="flex justify-center shrink-0">
          {shouldHideScore ? (
            <div className="flex items-center gap-1 px-3 py-1 rounded-lg bg-[#0A0A0C] border border-white/10 text-xs font-tech font-bold text-white/60">
              <EyeOff className="w-3 h-3 text-[#DFFF00]" />
              <span>? - ?</span>
            </div>
          ) : (
            <div
              className={`px-3 py-1 rounded-lg text-center min-w-[64px] sm:min-w-[72px] flex items-center justify-center ${
                isLive
                  ? 'bg-[#0A0A0C] border border-[#FF2E4D]/50 shadow-[0_0_8px_rgba(255,46,77,0.3)]'
                  : 'bg-[#0A0A0C] border border-white/10'
              }`}
            >
              <div className="flex flex-col items-center">
                <span
                  className={`font-scoreboard text-lg sm:text-xl tracking-wider leading-none ${
                    isLive ? 'text-[#FF2E4D]' : 'text-white'
                  }`}
                >
                  {isFinished || isLive ? `${match.home_score ?? 0} - ${match.away_score ?? 0}` : 'VS'}
                </span>
                {match.home_penalties != null && match.away_penalties != null && (
                  <span className="text-[9px] font-tech text-white/50 leading-tight">
                    ({match.home_penalties}-{match.away_penalties} p)
                  </span>
                )}
              </div>
            </div>
          )}
        </div>

        {/* Away Team */}
        <div className="flex items-center justify-start gap-2 sm:gap-3 min-w-0">
          <GlassLogoBadge
            src={match.away_team_crest}
            alt={awayName}
            size={32}
            imageClassName="object-contain p-0.5"
          />
          <span className="text-xs sm:text-sm font-bold text-white text-left line-clamp-1 font-body">
            {awayName}
          </span>
        </div>
      </div>

      {/* Right: Quick Action Links */}
      <div className="flex items-center justify-end gap-2 shrink-0 pt-2 md:pt-0 border-t md:border-t-0 border-white/5">
        {hasHighlight && (
          <button
            onClick={(e) => {
              e.stopPropagation();
              playVideo(match);
            }}
            className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg bg-[#DFFF00] hover:bg-[#c8e600] text-[#0A0A0C] font-tech text-xs font-bold uppercase transition-all shadow-[0_0_10px_rgba(223,255,0,0.25)] cursor-pointer"
          >
            <Play className="w-3.5 h-3.5 fill-[#0A0A0C]" />
            <span>Highlights</span>
          </button>
        )}

        <button
          onClick={(e) => {
            e.stopPropagation();
            router.push(`/match/${match.id}`);
          }}
          className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg bg-[#121316] hover:bg-[#1A1C22] text-white/80 hover:text-white font-tech text-xs font-semibold border border-white/10 transition-colors cursor-pointer"
        >
          <Activity className="w-3.5 h-3.5 text-[#DFFF00]" />
          <span>Match Centre</span>
        </button>
      </div>
    </article>
  );
}
