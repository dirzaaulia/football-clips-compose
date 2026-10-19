import React, { useState } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { Play, Radio, Film, Clock, EyeOff } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';
import { useVideoPlayer } from '@/hooks/useVideoPlayer';

interface FixtureItemCardProps {
  match: MatchRecord;
}

export default function FixtureItemCard({ match }: FixtureItemCardProps) {
  const { isSpoilerFree } = useSpoilerFree();
  const { playVideo } = useVideoPlayer();
  const [isRevealed, setIsRevealed] = useState(false);
  const isFinished = match.status === 'FINISHED';
  const isLive = match.status === 'IN_PLAY' || match.status === 'PAUSED' || match.status === 'LIVE';
  const isPaused = match.status === 'PAUSED';
  const isUpcoming = !isFinished && !isLive;
  const hasHighlight = Boolean(match.highlight_video_id);

  const homeName = match.home_team_name || match.home_team;
  const awayName = match.away_team_name || match.away_team;
  const matchTime = new Date(match.utc_date).toLocaleTimeString('en-GB', {
    hour: '2-digit',
    minute: '2-digit',
  });

  // Live matches never hide score; spoiler shielding only applies to finished matches
  const shouldHideScore = isSpoilerFree && !isRevealed && isFinished;

  return (
    <div
      className={`rounded-2xl p-4 transition-all flex flex-col sm:grid sm:grid-cols-[200px_1fr_200px] items-center gap-4 ${
        isLive
          ? 'bg-gradient-to-r from-[#1E2227] via-[#211d24] to-[#1E2227] border border-red-500/40 shadow-[0_0_15px_rgba(239,68,68,0.15)] hover:border-red-500/60'
          : 'bg-[#1E2227] border border-[#334155] hover:border-[#7098F8]/50'
      }`}
    >
      {/* Left: League & Clearly Stated Match Status */}
      <div className="flex sm:flex-col items-center sm:items-start justify-between w-full sm:w-auto gap-2">
        <span className="text-[11px] uppercase font-black text-[#7098F8] tracking-wider truncate max-w-[190px]">
          {match.competition_name || match.competition_id}
        </span>

        {/* Clear Match Status Badges */}
        {isLive ? (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-[10px] font-black bg-red-500/20 text-red-400 border border-red-500/30 tracking-wider uppercase animate-pulse">
            <span className="w-2 h-2 rounded-full bg-red-500" />
            <span>{isPaused ? 'Half Time' : 'LIVE IN PLAY'}</span>
          </span>
        ) : isFinished ? (
          hasHighlight ? (
            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-[10px] font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
              <Film className="w-3 h-3" />
              <span>Full Time • Highlights Ready</span>
            </span>
          ) : (
            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-[10px] font-semibold bg-[#101418] text-[#94A3B8] border border-[#334155]">
              <span>Full Time</span>
            </span>
          )
        ) : (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-[10px] font-semibold bg-[#101418] text-[#94A3B8] border border-[#334155]">
            <Clock className="w-3 h-3 text-[#7098F8]" />
            <span>Upcoming • {matchTime}</span>
          </span>
        )}
      </div>

      {/* Centre: Perfectly Centered Scoreline & Balanced Teams */}
      <div className="w-full grid grid-cols-[1fr_auto_1fr] items-center gap-3 sm:gap-4">
        {/* Home Team */}
        <div className="flex items-center justify-end gap-2.5 min-w-0">
          <span className="text-xs sm:text-sm font-bold text-white text-right truncate">{homeName}</span>
          {match.home_team_crest ? (
            <div className="relative w-6 h-6 shrink-0">
              <Image src={match.home_team_crest} alt={homeName} fill className="object-contain" unoptimized />
            </div>
          ) : null}
        </div>

        {/* Score Badge (Mathematically Dead-Centered at container 50%) */}
        <div className="flex justify-center shrink-0">
          {shouldHideScore ? (
            <button
              onClick={() => setIsRevealed(true)}
              title="Click to reveal score"
              className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-xl bg-[#101418] border border-[#334155] hover:border-[#7098F8]/60 text-xs font-bold text-[#94A3B8] hover:text-white transition-all cursor-pointer min-w-[76px] justify-center group shadow-inner"
            >
              <EyeOff className="w-3 h-3 text-[#7098F8] group-hover:scale-110 transition-transform" />
              <span className="tracking-widest">? - ?</span>
            </button>
          ) : (
            <div
              className={`px-3.5 py-1.5 rounded-xl text-center min-w-[76px] flex items-center justify-center shadow-inner ${
                isLive
                  ? 'bg-[#101418] border border-red-500/50 shadow-[0_0_10px_rgba(239,68,68,0.25)]'
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
        <div className="flex items-center justify-start gap-2.5 min-w-0">
          {match.away_team_crest ? (
            <div className="relative w-6 h-6 shrink-0">
              <Image src={match.away_team_crest} alt={awayName} fill className="object-contain" unoptimized />
            </div>
          ) : null}
          <span className="text-xs sm:text-sm font-bold text-white text-left truncate">{awayName}</span>
        </div>
      </div>

      {/* Right: Contextual Action CTA */}
      <div className="flex items-center justify-center sm:justify-end w-full sm:w-auto">
        {isLive ? (
          <button
            onClick={() => playVideo(match)}
            className="flex items-center justify-center gap-1.5 px-4 py-2 rounded-xl bg-gradient-to-r from-red-600 to-red-500 hover:from-red-500 hover:to-red-400 text-white text-xs font-black shadow-[0_0_12px_rgba(239,68,68,0.35)] transition-all cursor-pointer w-full sm:w-auto"
          >
            <Radio className="w-3.5 h-3.5 animate-pulse" />
            <span>Match Centre</span>
          </button>
        ) : hasHighlight ? (
          <button
            onClick={() => playVideo(match)}
            className="flex items-center justify-center gap-1.5 px-4 py-2 rounded-xl bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white text-xs font-bold shadow-[0_0_12px_rgba(112,152,248,0.35)] hover:brightness-110 transition-all cursor-pointer w-full sm:w-auto"
          >
            <Play className="w-3.5 h-3.5 fill-current" />
            <span>Watch Highlight</span>
          </button>
        ) : isFinished ? (
          <button
            onClick={() => playVideo(match)}
            className="flex items-center justify-center gap-1 px-3.5 py-1.5 text-xs text-[#94A3B8] hover:text-white font-semibold bg-[#101418] rounded-xl border border-[#334155]/70 hover:border-[#7098F8]/50 transition-all cursor-pointer w-full sm:w-auto"
          >
            <span>Match Centre</span>
          </button>
        ) : (
          <button
            onClick={() => playVideo(match)}
            className="flex items-center justify-center gap-1 px-3.5 py-1.5 text-xs text-[#94A3B8] hover:text-white font-semibold bg-[#101418] rounded-xl border border-[#334155]/70 hover:border-[#7098F8]/50 transition-all cursor-pointer w-full sm:w-auto"
          >
            <span>Match Preview</span>
          </button>
        )}
      </div>
    </div>
  );
}
