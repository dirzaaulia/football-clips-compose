'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { ArrowLeft, ExternalLink, Tv, Share2, Check } from 'lucide-react';

interface MatchCentreHeaderProps {
  videoId?: string | null;
  title?: string;
}

export default function MatchCentreHeader({ videoId, title }: MatchCentreHeaderProps) {
  const [copied, setCopied] = useState(false);

  const handleShare = async () => {
    const shareTitle = title ? `${title} - Football Highlights & Clips` : 'Football Highlights & Clips';
    const shareText = title ? `Watch highlights for ${title} on Football Highlights & Clips!` : 'Watch official football match highlights & tactical analysis!';
    const currentUrl = typeof window !== 'undefined' ? window.location.href : 'https://fc.dirzaaulia.com';

    if (typeof navigator !== 'undefined' && navigator.share) {
      try {
        await navigator.share({
          title: shareTitle,
          text: shareText,
          url: currentUrl,
        });
      } catch {
        // User cancelled or share dismissed
      }
    } else if (typeof navigator !== 'undefined' && navigator.clipboard) {
      try {
        await navigator.clipboard.writeText(currentUrl);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
      } catch {}
    }
  };

  return (
    <div className="flex items-center justify-between py-3 mb-4 border-b border-[#334155]/60">
      <Link
        href="/"
        className="inline-flex items-center gap-1.5 text-xs font-bold text-[#94A3B8] hover:text-white transition-colors"
      >
        <ArrowLeft className="w-3.5 h-3.5" />
        <span className="hidden sm:inline">Back to Highlights</span>
        <span className="sm:hidden">Back</span>
      </Link>

      <div className="flex items-center gap-2">
        <span className="flex items-center gap-1.5 px-3 py-1 rounded-full text-[11px] font-black tracking-widest uppercase bg-[#181C20] text-[#7098F8] border border-[#7098F8]/40 shadow-[0_0_12px_rgba(112,152,248,0.2)]">
          <Tv className="w-3 h-3 text-[#7098F8]" />
          <span>Match Centre</span>
        </span>
      </div>

      <div className="flex items-center gap-2">
        <button
          onClick={handleShare}
          className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#1E2227] hover:bg-[#252B32] text-xs font-bold text-[#94A3B8] hover:text-white border border-[#334155] transition-all cursor-pointer"
          title="Share Match"
          aria-label="Share Match"
        >
          {copied ? (
            <>
              <Check className="w-3.5 h-3.5 text-emerald-400" />
              <span className="hidden sm:inline text-emerald-400">Copied!</span>
            </>
          ) : (
            <>
              <Share2 className="w-3.5 h-3.5 text-[#7098F8]" />
              <span className="hidden sm:inline">Share</span>
            </>
          )}
        </button>

        {videoId && (
          <a
            href={`https://www.youtube.com/watch?v=${videoId}`}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#1E2227] hover:bg-[#252B32] text-xs font-bold text-[#94A3B8] hover:text-white border border-[#334155] transition-all"
          >
            <span className="hidden sm:inline">Watch on YouTube</span>
            <ExternalLink className="w-3.5 h-3.5 text-[#7098F8]" />
          </a>
        )}
      </div>
    </div>
  );
}
