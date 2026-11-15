'use client';

import React, { useState } from 'react';
import { Share2, Bookmark, BookmarkCheck, Check } from 'lucide-react';

interface MatchActionToolbarProps {
  title: string;
  subtitle: string;
  matchId: string | number;
}

export default function MatchActionToolbar({
  title,
  subtitle,
  matchId,
}: MatchActionToolbarProps) {
  const [isSaved, setIsSaved] = useState(false);
  const [copied, setCopied] = useState(false);

  const handleShare = async () => {
    const url = typeof window !== 'undefined' ? `${window.location.origin}/match/${matchId}` : '';
    if (typeof navigator !== 'undefined' && navigator.share) {
      try {
        await navigator.share({ title, url });
      } catch {}
    } else if (typeof navigator !== 'undefined' && navigator.clipboard) {
      try {
        await navigator.clipboard.writeText(url);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
      } catch {}
    }
  };

  return (
    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-1 select-none">
      <div className="min-w-0">
        <h1 className="font-scoreboard text-2xl sm:text-3xl tracking-wider text-white uppercase leading-tight truncate">
          {title}
        </h1>
        <p className="font-tech text-xs text-white/50 flex items-center gap-2 mt-0.5 flex-wrap">
          <span>{subtitle}</span>
          <span className="text-white/20">•</span>
          <span className="text-[#DFFF00]">Official Verified Highlights</span>
        </p>
      </div>

      <div className="flex items-center gap-2 shrink-0">
        <button
          onClick={handleShare}
          className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-[#121316] hover:bg-[#1A1C22] text-white/80 hover:text-white border border-white/10 text-xs font-tech font-bold uppercase tracking-wider transition-all cursor-pointer"
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

        <button
          onClick={() => setIsSaved((prev) => !prev)}
          className={`flex items-center gap-1.5 px-3.5 py-2 rounded-xl border text-xs font-tech font-bold uppercase tracking-wider transition-all cursor-pointer ${
            isSaved
              ? 'bg-[#DFFF00]/15 text-[#DFFF00] border-[#DFFF00]/40'
              : 'bg-[#121316] hover:bg-[#1A1C22] text-white/80 hover:text-white border-white/10'
          }`}
          title={isSaved ? 'Saved to Playlist' : 'Save Highlight'}
        >
          {isSaved ? (
            <>
              <BookmarkCheck className="w-3.5 h-3.5 text-[#DFFF00]" />
              <span>Saved</span>
            </>
          ) : (
            <>
              <Bookmark className="w-3.5 h-3.5" />
              <span>Save</span>
            </>
          )}
        </button>
      </div>
    </div>
  );
}
