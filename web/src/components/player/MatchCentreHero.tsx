'use client';

import React from 'react';
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
}: MatchCentreHeroProps) {
  const isFinished = match.status === 'FINISHED' || match.status === 'AWARDED';
  const isLive = ['IN_PLAY', 'PAUSED', 'LIVE'].includes(match.status);
  const isScheduled = ['TIMED', 'SCHEDULED'].includes(match.status);
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
    <div className="relative overflow-hidden rounded-2xl bg-[#121316] border border-white/10 p-4 sm:p-6 shadow-2xl select-none">
      <div className="absolute -top-16 -left-16 w-48 h-48 bg-[#DFFF00]/5 rounded-full blur-3xl pointer-events-none" />

      {/* Top Competition & Status Header */}
      <div className="relative flex items-center justify-between pb-3.5 border-b border-white/10 text-xs">
        <div className="flex items-center gap-2">
          <span className="font-tech font-black text-[#DFFF00] tracking-widest uppercase text-[10px] sm:text-[11px]">
            {match.competition_name}
          </span>
          {match.matchday && (
            <span className="text-[10px] text-white/60 font-mono font-bold px-2 py-0.5 rounded-full bg-[#1A1C22] border border-white/5">
              MD {match.matchday}
            </span>
          )}
        </div>

        {/* Dynamic Status Indicator */}
        {isLive ? (
          <div className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-red-500/20 border border-red-500/50 text-red-400 text-[10px] font-tech font-black animate-pulse">
            <Radio className="w-3 h-3 text-red-400" />
            <span>LIVE</span>
          </div>
        ) : isFinished ? (
          <div className="px-3 py-0.5 rounded-full bg-white/5 border border-white/10 text-white/70 text-[10px] font-tech font-bold uppercase">
            Full Time
          </div>
        ) : (
          <div className="flex items-center gap-1 text-[#38BDF8] text-[11px] font-tech font-bold">
            <Clock className="w-3 h-3" />
            <span>{kickoffDate.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}</span>
          </div>
        )}
      </div>

      {/* Main Face-Off Section */}
      <div className="relative flex items-center justify-between py-5 sm:py-7 gap-3">
        {/* Home Team */}
        <div className="flex flex-col items-center flex-1 text-center min-w-0">
          <GlassLogoBadge
            src={match.home_team_crest}
            alt={homeName}
            size={60}
            className="mb-2.5"
            imageClassName="object-contain p-2 drop-shadow-md"
          />
          <span className="font-bold text-xs sm:text-base text-white leading-tight">
            {homeName}
          </span>
        </div>

        {/* Center: Score or VS */}
        <div className="flex flex-col items-center justify-center shrink-0 px-2 sm:px-4">
          {isScheduled && !isLive ? (
            <div className="flex flex-col items-center gap-1">
              <span className="px-4 py-1.5 bg-[#1A1C22] border border-white/10 rounded-xl text-xs font-tech font-black text-white/70 tracking-widest">
                VS
              </span>
              <span className="text-[10px] text-white/40 font-tech font-semibold mt-1">Scheduled</span>
            </div>
          ) : isLive ? (
            <div className="flex flex-col items-center px-4 sm:px-5 py-2 bg-[#0A0A0C] border border-red-500/50 rounded-2xl shadow-[0_0_15px_rgba(239,68,68,0.3)]">
              <div className="flex items-center gap-2.5 sm:gap-3.5">
                <span className="font-scoreboard text-2xl sm:text-3xl text-white">
                  {match.home_score ?? 0}
                </span>
                <span className="text-sm font-bold text-red-400">-</span>
                <span className="font-scoreboard text-2xl sm:text-3xl text-white">
                  {match.away_score ?? 0}
                </span>
              </div>
              <span className="text-[9px] uppercase font-tech font-bold text-red-400 tracking-wider flex items-center gap-1 mt-0.5">
                <Radio className="w-2.5 h-2.5 animate-pulse" />
                {match.display_clock || 'IN PLAY'}
              </span>
            </div>
          ) : isHidden ? (
            <button
              onClick={onRevealScore}
              className="flex items-center gap-2 px-4 py-2.5 bg-[#0A0A0C] border border-[#DFFF00]/40 hover:border-[#DFFF00] rounded-xl text-center shadow-lg transition-all cursor-pointer hover:scale-105"
            >
              <Eye className="w-4 h-4 text-[#DFFF00]" />
              <span className="font-tech font-black text-xs text-[#DFFF00] uppercase tracking-wider">Reveal Score</span>
            </button>
          ) : (
            <div
              onClick={() => isSpoilerFree && onHideScore()}
              className={`flex flex-col items-center px-4 sm:px-5 py-2 bg-[#0A0A0C] border border-[#DFFF00]/30 rounded-2xl ${
                isSpoilerFree ? 'cursor-pointer hover:border-[#DFFF00]' : ''
              }`}
            >
              <div className="flex items-center gap-2.5 sm:gap-3.5">
                <span className="font-scoreboard text-2xl sm:text-3xl text-white">
                  {match.home_score ?? 0}
                </span>
                <span className="text-sm font-bold text-white/40">-</span>
                <span className="font-scoreboard text-2xl sm:text-3xl text-white">
                  {match.away_score ?? 0}
                </span>
              </div>
              {isSpoilerFree && (
                <div className="flex items-center gap-1 text-[9px] text-white/50 mt-0.5 font-tech">
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
            size={60}
            className="mb-2.5"
            imageClassName="object-contain p-2 drop-shadow-md"
          />
          <span className="font-bold text-xs sm:text-base text-white leading-tight">
            {awayName}
          </span>
        </div>
      </div>

      {/* Goal Scorers Summary */}
      {!isHidden && match.events && match.events.length > 0 && (
        <div className="relative pt-3 pb-1 border-t border-white/10 grid grid-cols-1 sm:grid-cols-2 gap-2 sm:gap-4 text-xs font-tech">
          <div className="text-left space-y-1">
            {match.events
              .filter((e) => e.side === 'home' && (e.type?.toLowerCase().includes('goal') || e.text?.toLowerCase().includes('goal')))
              .map((g, idx) => (
                <div key={g.id || idx} className="flex items-center gap-1.5 text-white/90 font-medium text-[11px]">
                  <span className="text-[#DFFF00] text-xs">⚽</span>
                  <span className="font-bold truncate">{g.player || g.text}</span>
                  <span className="text-white/50 font-mono text-[10px] shrink-0">{g.clock}</span>
                </div>
              ))}
          </div>

          <div className="text-left sm:text-right space-y-1">
            {match.events
              .filter((e) => e.side === 'away' && (e.type?.toLowerCase().includes('goal') || e.text?.toLowerCase().includes('goal')))
              .map((g, idx) => (
                <div key={g.id || idx} className="flex items-center sm:justify-end gap-1.5 text-white/90 font-medium text-[11px]">
                  <span className="text-white/50 font-mono text-[10px] shrink-0">{g.clock}</span>
                  <span className="font-bold truncate">{g.player || g.text}</span>
                  <span className="text-[#DFFF00] text-xs">⚽</span>
                </div>
              ))}
          </div>
        </div>
      )}

      {/* Live Official Broadcaster Live Stream Redirect */}
      {isLive && (
        <div className="relative my-3 pt-3 border-t border-white/10">
          <WhereToWatchBanner competitionId={match.competition_id || match.competition_name || 'PL'} />
        </div>
      )}

      {/* Date & Embargo Explanatory Footer */}
      <div className="relative pt-3 border-t border-white/10 flex items-center justify-between text-[11px] text-white/60 font-tech">
        <div className="flex items-center gap-1.5">
          <Calendar className="w-3.5 h-3.5 text-[#DFFF00]" />
          <span>{formattedKickoff}</span>
        </div>
        {isFinished && !match.highlight_video_id && (
          <div className="flex items-center gap-1 text-[#DFFF00] font-semibold text-[10px]">
            <Sparkles className="w-3 h-3" />
            <span>Highlights Coming Soon</span>
          </div>
        )}
      </div>
    </div>
  );
}
