'use client';

import React from 'react';
import Image from 'next/image';
import { Calendar, Clock, Eye, EyeOff, Radio, Sparkles } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import WhereToWatchBanner from '@/components/broadcast/WhereToWatchBanner';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';

interface MatchCentreHeroProps {
  match: MatchRecord;
  homeName: string;
  awayName: string;
  isSpoilerFree: boolean;
  isScoreRevealed: boolean;
  onRevealScore: () => void;
  onHideScore: () => void;
  onRefresh?: () => void;
}

export default function MatchCentreHero({
  match,
  homeName,
  awayName,
  isSpoilerFree,
  isScoreRevealed,
  onRevealScore,
  onHideScore,
  onRefresh,
}: MatchCentreHeroProps) {
  const isFinished = match.status === 'FINISHED' || match.status === 'AWARDED';
  const isLive = ['IN_PLAY', 'PAUSED', 'LIVE'].includes(match.status);
  const isScheduled = ['TIMED', 'SCHEDULED'].includes(match.status);
  // Live matches never hide score; spoiler mode is strictly for finished matches
  const isHidden = isSpoilerFree && !isScoreRevealed && isFinished;

  const kickoffDate = new Date(match.utc_date);
  const formattedKickoff = kickoffDate.toLocaleDateString('en-GB', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

  return (
    <div className="relative overflow-hidden rounded-2xl bg-gradient-to-b from-[#181C20] via-[#101418] to-[#0A0D10] border border-[#334155]/60 p-3.5 sm:p-5 shadow-2xl">
      {/* Background Ambience Glow */}
      <div className="absolute -top-16 -left-16 w-48 h-48 bg-[#7098F8]/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute -bottom-16 -right-16 w-48 h-48 bg-[#4FC3F7]/10 rounded-full blur-3xl pointer-events-none" />

      {/* Top Competition & Status Header */}
      <div className="relative flex items-center justify-between pb-3 border-b border-[#334155]/60 text-xs">
        <div className="flex items-center gap-2">
          <span className="font-black text-[#7098F8] tracking-widest uppercase text-[10px] sm:text-[11px]">
            {match.competition_name}
          </span>
          {match.matchday && (
            <span className="text-[10px] text-[#94A3B8] font-bold px-1.5 py-0.5 rounded bg-[#1E293B]">
              MD {match.matchday}
            </span>
          )}
        </div>

        {/* Dynamic Status Indicator */}
        {isLive ? (
          <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-[#EF4444]/20 border border-[#EF4444]/50 text-[#EF4444] text-[10px] font-black animate-pulse">
            <Radio className="w-3 h-3 text-[#EF4444]" />
            <span>LIVE</span>
          </div>
        ) : isFinished ? (
          <div className="px-2.5 py-0.5 rounded-full bg-[#334155]/40 border border-[#475569] text-[#94A3B8] text-[10px] font-extrabold uppercase">
            Full Time
          </div>
        ) : (
          <div className="flex items-center gap-1 text-[#4FC3F7] text-[11px] font-bold">
            <Clock className="w-3 h-3" />
            <span>{kickoffDate.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}</span>
          </div>
        )}
      </div>

      {/* Main Face-Off Section */}
      <div className="relative flex items-center justify-between py-4 sm:py-6 gap-2">
        {/* Home Team */}
        <div className="flex flex-col items-center flex-1 text-center min-w-0">
          <GlassLogoBadge
            src={match.home_team_crest}
            alt={homeName}
            size={56}
            className="mb-2"
            imageClassName="object-contain p-2 drop-shadow-[0_4px_12px_rgba(0,0,0,0.6)]"
          />
          <span className="font-extrabold text-xs sm:text-sm text-white leading-tight max-w-[110px] sm:max-w-[150px] line-clamp-2">
            {homeName}
          </span>
        </div>

        {/* Center: Score or VS */}
        <div className="flex flex-col items-center justify-center shrink-0 px-2 sm:px-3">
          {isScheduled && !isLive ? (
            <div className="flex flex-col items-center gap-1">
              <span className="px-3 py-1 bg-[#1E293B] border border-[#334155] rounded-xl text-xs font-black text-[#94A3B8] tracking-widest">
                VS
              </span>
              <span className="text-[10px] text-[#64748B] font-semibold mt-1">Scheduled</span>
            </div>
          ) : isLive ? (
            <div className="flex flex-col items-center px-3 sm:px-4 py-1.5 sm:py-2 bg-[#101418] border border-red-500/50 rounded-2xl shadow-[0_0_12px_rgba(239,68,68,0.25)]">
              <div className="flex items-center gap-2 sm:gap-3">
                <span className="font-black text-xl sm:text-2xl text-white font-mono">
                  {match.home_score ?? 0}
                </span>
                <span className="text-sm font-bold text-red-400">-</span>
                <span className="font-black text-xl sm:text-2xl text-white font-mono">
                  {match.away_score ?? 0}
                </span>
              </div>
              <span className="text-[9px] uppercase font-bold text-red-400 tracking-wider flex items-center gap-1 mt-0.5">
                <Radio className="w-2.5 h-2.5 animate-pulse" />
                {match.display_clock || 'IN PLAY'}
              </span>
            </div>
          ) : isHidden ? (
            <button
              onClick={onRevealScore}
              className="flex items-center gap-1.5 px-3 sm:px-4 py-2 bg-[#181C20] border border-[#7098F8]/40 hover:border-[#7098F8] rounded-xl text-center shadow-lg group transition-all cursor-pointer"
            >
              <Eye className="w-3.5 h-3.5 text-[#4FC3F7] group-hover:scale-110 transition-transform" />
              <span className="font-extrabold text-xs text-[#4FC3F7]">Reveal Score</span>
            </button>
          ) : (
            <div
              onClick={() => isSpoilerFree && onHideScore()}
              className={`flex flex-col items-center px-3 sm:px-4 py-1.5 sm:py-2 bg-[#101418] border border-[#7098F8]/40 rounded-2xl shadow-inner ${
                isSpoilerFree ? 'cursor-pointer hover:border-[#7098F8]' : ''
              }`}
            >
              <div className="flex items-center gap-2 sm:gap-3">
                <span className="font-black text-xl sm:text-2xl text-white font-mono">
                  {match.home_score ?? 0}
                </span>
                <span className="text-sm font-bold text-[#64748B]">-</span>
                <span className="font-black text-xl sm:text-2xl text-white font-mono">
                  {match.away_score ?? 0}
                </span>
              </div>
              {isSpoilerFree && (
                <div className="flex items-center gap-1 text-[9px] text-[#94A3B8] mt-0.5">
                  <EyeOff className="w-2.5 h-2.5" />
                  <span>Tap to hide</span>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Away Team */}
        <div className="flex flex-col items-center flex-1 text-center min-w-0">
          <GlassLogoBadge
            src={match.away_team_crest}
            alt={awayName}
            size={56}
            className="mb-2"
            imageClassName="object-contain p-2 drop-shadow-[0_4px_12px_rgba(0,0,0,0.6)]"
          />
          <span className="font-extrabold text-xs sm:text-sm text-white leading-tight max-w-[110px] sm:max-w-[150px] line-clamp-2">
            {awayName}
          </span>
        </div>
      </div>

      {/* Goal Scorers Summary below the score line */}
      {!isHidden && (match.events && match.events.length > 0) && (
        <div className="relative pt-2 pb-1 border-t border-[#334155]/40 grid grid-cols-1 sm:grid-cols-2 gap-1.5 sm:gap-4 text-xs">
          <div className="text-left space-y-1">
            {match.events
              .filter((e) => e.side === 'home' && (e.type?.toLowerCase().includes('goal') || e.text?.toLowerCase().includes('goal')))
              .map((g, idx) => (
                <div key={g.id || idx} className="flex items-center gap-1.5 text-white/90 font-medium text-[11px]">
                  <span className="text-emerald-400 text-xs">⚽</span>
                  <span className="font-bold truncate">{g.player || g.text}</span>
                  <span className="text-[#94A3B8] font-mono text-[10px] shrink-0">{g.clock}</span>
                </div>
              ))}
          </div>

          <div className="text-left sm:text-right space-y-1">
            {match.events
              .filter((e) => e.side === 'away' && (e.type?.toLowerCase().includes('goal') || e.text?.toLowerCase().includes('goal')))
              .map((g, idx) => (
                <div key={g.id || idx} className="flex items-center sm:justify-end gap-1.5 text-white/90 font-medium text-[11px]">
                  <span className="text-[#94A3B8] font-mono text-[10px] shrink-0">{g.clock}</span>
                  <span className="font-bold truncate">{g.player || g.text}</span>
                  <span className="text-emerald-400 text-xs">⚽</span>
                </div>
              ))}
          </div>
        </div>
      )}

      {/* Live Official Broadcaster Live Stream Redirect */}
      {isLive && (
        <div className="relative my-3 pt-3 border-t border-[#334155]/60">
          <WhereToWatchBanner competitionId={match.competition_id || match.competition_name || 'PL'} />
        </div>
      )}

      {/* Date & Embargo Explanatory Footer */}
      <div className="relative pt-3 border-t border-[#334155]/60 flex items-center justify-between text-[11px] text-[#94A3B8]">
        <div className="flex items-center gap-1.5">
          <Calendar className="w-3.5 h-3.5 text-[#7098F8]" />
          <span>{formattedKickoff}</span>
        </div>
        {isFinished && !match.highlight_video_id && (
          <div className="flex items-center gap-1 text-[#F59E0B] font-semibold text-[10px]">
            <Sparkles className="w-3 h-3" />
            <span>Embargo Active</span>
          </div>
        )}
      </div>

      {/* Graceful Finished Match Highlights In Processing Notice (ONLY when finished and video not yet available) */}
      {isFinished && !match.highlight_video_id && (
        <div className="relative mt-1 p-3 bg-amber-500/10 border border-amber-500/30 rounded-xl flex items-center gap-3">
          <div className="p-2 rounded-lg bg-amber-500/20 text-amber-400 shrink-0">
            <Clock className="w-4 h-4 animate-pulse" />
          </div>
          <div className="min-w-0 flex-1 text-left">
            <p className="text-xs font-bold text-amber-300">Official Highlights In Processing</p>
            <p className="text-[11px] text-amber-200/70 leading-snug">
              Broadcaster post-match embargo is active. Highlights will be automatically available once official club channels publish.
            </p>
          </div>
        </div>
      )}
    </div>
  );
}
