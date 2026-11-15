'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { ArrowLeft, ExternalLink, Share2, Check, Home } from 'lucide-react';

interface MatchCentreHeaderProps {
  videoId?: string | null;
  title?: string;
  competitionName?: string;
  homeName?: string;
  awayName?: string;
  matchId?: string | number;
  status?: string;
  stage?: string | null;
  season?: number | string | null;
}

export default function MatchCentreHeader({
  videoId,
  title,
  competitionName,
  homeName,
  awayName,
  matchId,
  status = 'FINISHED',
  stage,
  season,
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

  return (
    <div className="flex flex-col gap-2.5 pb-3 border-b border-white/10 select-none">
      {/* Top Broadcast Breadcrumbs & Live Meta */}
      <div className="flex flex-wrap items-center justify-between gap-2 text-xs">
        {/* Breadcrumb Trail */}
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

        {/* Live Status & Venue Meta */}
        <div className="flex items-center gap-2.5 shrink-0 text-[11px] font-tech">
          <div className="flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-[#1A1C22] text-white border border-white/10">
            <span
              className={`w-1.5 h-1.5 rounded-full ${
                isLive ? 'bg-red-500 animate-ping' : isFinished ? 'bg-[#DFFF00]' : 'bg-[#38BDF8]'
              }`}
            />
            <span className="font-bold">
              {isLive ? 'LIVE' : isFinished ? 'FULL TIME • FT' : 'SCHEDULED'}
            </span>
          </div>

          {stage && (
            <span className="hidden md:inline text-white/50">
              {stage}
            </span>
          )}

          <span className="px-2 py-0.5 rounded bg-white/5 text-white/60 border border-white/5 text-[10px] font-mono">
            {season ? `${season}` : '2026/27'}
          </span>
        </div>
      </div>

      {/* Action Sub-Bar */}
      <div className="flex items-center justify-between pt-1">
        <Link
          href="/"
          className="inline-flex items-center gap-1.5 text-xs font-tech font-bold text-white/60 hover:text-[#DFFF00] transition-colors"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Back to Highlights</span>
        </Link>

        <div className="flex items-center gap-2">
          <button
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
