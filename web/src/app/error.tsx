'use client';

import React, { useEffect } from 'react';
import Link from 'next/link';
import { AlertTriangle, RotateCcw, Home } from 'lucide-react';

interface ErrorProps {
  error: Error & { digest?: string };
  reset: () => void;
}

export default function ErrorBoundary({ error, reset }: ErrorProps) {
  useEffect(() => {
    // Log unexpected client-side runtime error
    console.error('Next.js Client Runtime Exception:', error);
  }, [error]);

  return (
    <div className="min-h-[70vh] max-w-xl mx-auto px-4 flex flex-col items-center justify-center text-center select-none py-16 space-y-6">
      <div className="w-16 h-16 rounded-2xl bg-amber-500/10 border border-amber-500/30 flex items-center justify-center text-amber-400 shadow-xl shadow-amber-500/10">
        <AlertTriangle className="w-8 h-8" />
      </div>

      <div className="space-y-2">
        <span className="text-[11px] font-tech font-bold uppercase tracking-widest text-amber-400">
          Telemetry Pipeline Interruption
        </span>
        <h1 className="font-scoreboard text-2xl sm:text-4xl uppercase tracking-wider text-white">
          Tactical Telemetry Error
        </h1>
        <p className="text-xs sm:text-sm text-[#A1A1AA] font-tech leading-relaxed max-w-md mx-auto">
          An operational exception occurred while calculating live fixture metrics. You can attempt to refresh the match state or return to the pitch homepage.
        </p>
      </div>

      <div className="flex items-center justify-center gap-3 font-tech pt-2">
        <button
          onClick={reset}
          className="inline-flex items-center gap-2 px-5 py-3 rounded-2xl bg-[#DFFF00] hover:bg-[#cbe800] text-[#0A0A0C] font-bold text-xs uppercase tracking-wider transition-all shadow-[0_0_15px_rgba(223,255,0,0.25)] cursor-pointer min-h-[48px]"
        >
          <RotateCcw className="w-4 h-4" />
          <span>Retry Operation</span>
        </button>
        <Link
          href="/"
          className="inline-flex items-center gap-2 px-5 py-3 rounded-2xl bg-white/5 hover:bg-white/10 border border-white/15 text-white font-bold text-xs uppercase tracking-wider transition-all cursor-pointer min-h-[48px]"
        >
          <Home className="w-4 h-4" />
          <span>Home Highlights</span>
        </Link>
      </div>
    </div>
  );
}
