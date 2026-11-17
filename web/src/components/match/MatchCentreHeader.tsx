'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import {
  ArrowLeft,
  ExternalLink,
  Share2,
  Check,
  Home,
  MapPin,
  ShieldCheck,
  Wrench,
} from 'lucide-react';
import { EspnGameInfo } from '@/lib/espn';

interface MatchCentreHeaderProps {
  videoId?: string | null;
  title?: string;
  competitionName?: string;
  competitionId?: string;
  homeName?: string;
  awayName?: string;
  matchId?: string | number;
  status?: string;
  stage?: string | null;
  season?: number | string | null;
  gameInfo?: EspnGameInfo | null;
}

export default function MatchCentreHeader({
  videoId,
  title,
  competitionName,
  competitionId,
  homeName,
  awayName,
  matchId,
  status = 'FINISHED',
  stage,
  season,
  gameInfo,
}: MatchCentreHeaderProps) {
  const [copied, setCopied] = useState(false);

  const isLive = ['IN_PLAY', 'PAUSED', 'LIVE'].includes(status);
  const isFinished = status === 'FINISHED' || status === 'AWARDED';

  const handleShare = async () => {
    const shareTitle = title ? `${title} - Football Highlights & Clips` : 'Football Highlights & Clips';
    const currentUrl = typeof window !== 'undefined' ? window.location.href : 'https://fc.dirzaaulia.com';

    if (typeof navigator !== 'undefined' && navigator.share) {
      try {
        await navigator.share({ title: shareTitle, url: currentUrl });
      } catch {}
    } else if (typeof navigator !== 'undefined' && navigator.clipboard) {
      try {
        await navigator.clipboard.writeText(currentUrl);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
      } catch {}
    }
  };

  const toolsHref = `/tools?tab=h2h&matchId=${encodeURIComponent(String(matchId || ''))}&home=${encodeURIComponent(homeName || '')}&away=${encodeURIComponent(awayName || '')}&comp=${encodeURIComponent(competitionId || 'PL')}`;

  return (
    <div className="flex flex-col gap-2.5 pb-3 border-b border-white/10 select-none">
      {/* Stitch Top Broadcast Breadcrumbs & Live Venue/Referee Meta */}
      <div className="flex flex-wrap items-center justify-between gap-2 text-xs">
        <div className="flex items-center gap-2 overflow-x-auto whitespace-nowrap text-white/50 font-tech text-[11px]">
          <Link href="/" className="hover:text-[#DFFF00] transition-colors flex items-center gap-1">
            <Home className="w-3.5 h-3.5 text-white/60" />
            <span>HOME</span>
          </Link>
          <span className="text-white/20">/</span>
          <Link href="/fixtures" className="hover:text-[#DFFF00] transition-colors">
            FIXTURES
          </Link>
          <span className="text-white/20">/</span>
          <span className="text-white/80 uppercase tracking-wider font-semibold">
            {competitionName || 'FOOTBALL'}
          </span>
          <span className="text-white/20">/</span>
          <span className="text-[#DFFF00] font-bold uppercase truncate max-w-[200px] sm:max-w-xs">
            {homeName && awayName ? `${homeName} vs ${awayName}` : title || 'MATCH'}
            {matchId ? ` (#${matchId})` : ''}
          </span>
        </div>

        {/* Right: Stitch Status Badge + Stadium + Referee + Season */}
        <div className="flex flex-wrap items-center gap-2.5 shrink-0 text-[11px] font-tech">
          <div className="flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
            <span
              className={`w-1.5 h-1.5 rounded-full ${
                isLive ? 'bg-red-500 animate-ping' : isFinished ? 'bg-emerald-400' : 'bg-[#38BDF8]'
              }`}
            />
            <span className="font-bold">
              {isLive ? 'LIVE' : isFinished ? 'FULL TIME • FT' : 'SCHEDULED'}
            </span>
          </div>

          {gameInfo?.venueName && (
            <span className="hidden md:inline-flex items-center gap-1 text-white/60">
              <MapPin className="w-3 h-3 text-[#DFFF00]" />
              <span>
                {gameInfo.venueName}
                {gameInfo.attendance ? ` (${gameInfo.attendance.toLocaleString()})` : ''}
              </span>
            </span>
          )}

          {gameInfo?.referee && (
            <span className="hidden lg:inline-flex items-center gap-1 text-white/60">
              <ShieldCheck className="w-3 h-3 text-[#DFFF00]" />
              <span>Ref: {gameInfo.referee}</span>
            </span>
          )}

          {stage && !gameInfo?.venueName && (
            <span className="hidden md:inline text-white/50">{stage}</span>
          )}

          <span className="px-2 py-0.5 rounded bg-white/5 text-white/60 border border-white/5 text-[10px] font-mono">
            {season ? `${season}` : '2026/27'}
          </span>
        </div>
      </div>

      {/* Action Sub-Bar */}
      <div className="flex flex-wrap items-center justify-between gap-2 pt-1">
        <Link
          href="/"
          className="inline-flex items-center gap-1.5 text-xs font-tech font-bold text-white/60 hover:text-[#DFFF00] transition-colors"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Back to Highlights</span>
        </Link>

        <div className="flex items-center gap-2">
          <Link
            href={toolsHref}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#DFFF00]/15 hover:bg-[#DFFF00] text-[#DFFF00] hover:text-[#0A0A0C] text-xs font-tech font-bold border border-[#DFFF00]/40 transition-all"
            title="Compare Clubs in Tactical Tools Lab"
          >
            <Wrench className="w-3.5 h-3.5" />
            <span>Tactical Tools &amp; H2H Lab</span>
          </Link>

          <button
            type="button"
            onClick={handleShare}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#121316] hover:bg-[#1A1C22] text-xs font-tech font-bold text-white/80 hover:text-white border border-white/10 hover:border-white/20 transition-all cursor-pointer"
            title="Share Match"
          >
            {copied ? (
              <>
                <Check className="w-3.5 h-3.5 text-[#DFFF00]" />
                <span className="text-[#DFFF00]">Copied!</span>
              </>
            ) : (
              <>
                <Share2 className="w-3.5 h-3.5 text-[#DFFF00]" />
                <span>Share</span>
              </>
            )}
          </button>

          {videoId && (
            <a
              href={`https://www.youtube.com/watch?v=${videoId}`}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#121316] hover:bg-[#1A1C22] text-xs font-tech font-bold text-white/80 hover:text-white border border-white/10 hover:border-white/20 transition-all"
            >
              <span className="hidden sm:inline">Watch on YouTube</span>
              <ExternalLink className="w-3.5 h-3.5 text-[#DFFF00]" />
            </a>
          )}
        </div>
      </div>
    </div>
  );
}
