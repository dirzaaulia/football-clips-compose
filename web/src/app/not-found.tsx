import React from 'react';
import Link from 'next/link';
import { Flag, ArrowLeft, BookOpen, Calendar, Search } from 'lucide-react';

export const metadata = {
  title: 'Page Not Found | Football Highlights & Clips',
  description: 'The requested football highlight, tactical breakdown, or match telemetry is unavailable.',
};

export default function NotFound() {
  return (
    <div className="min-h-[75vh] max-w-4xl mx-auto px-4 sm:px-6 flex flex-col items-center justify-center text-center select-none py-16 space-y-8">
      {/* Visual Indicator Glyph */}
      <div className="relative">
        <div className="w-20 h-20 rounded-3xl bg-[#121316] border border-[#DFFF00]/30 shadow-2xl shadow-[#DFFF00]/10 flex items-center justify-center text-[#DFFF00]">
          <Flag className="w-10 h-10" />
        </div>
        <span className="absolute -top-2 -right-2 px-2 py-0.5 rounded-full text-[10px] font-tech font-black bg-[#DFFF00] text-[#0A0A0C] uppercase tracking-wider">
          Offside
        </span>
      </div>

      {/* Copy */}
      <div className="space-y-3 max-w-lg">
        <span className="text-xs font-tech font-bold uppercase tracking-widest text-[#DFFF00]">
          HTTP 404 • Resource Not Located
        </span>
        <h1 className="font-scoreboard text-3xl sm:text-5xl uppercase tracking-wider text-white">
          Pitch Out of Bounds
        </h1>
        <p className="text-xs sm:text-sm text-[#A1A1AA] font-tech leading-relaxed">
          The tactical dossier, fixture telemetry, or highlight reel you were looking for does not exist or has been relocated to another pitch zone.
        </p>
      </div>

      {/* Action Navigation Matrix */}
      <div className="flex flex-wrap items-center justify-center gap-3 font-tech pt-2">
        <Link
          href="/"
          className="inline-flex items-center gap-2 px-5 py-3 rounded-2xl bg-[#DFFF00] hover:bg-[#cbe800] text-[#0A0A0C] font-bold text-xs uppercase tracking-wider shadow-[0_0_20px_rgba(223,255,0,0.3)] transition-all cursor-pointer min-h-[48px]"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Home Highlights</span>
        </Link>
        <Link
          href="/articles"
          className="inline-flex items-center gap-2 px-5 py-3 rounded-2xl bg-white/5 hover:bg-white/10 border border-white/15 text-white font-bold text-xs uppercase tracking-wider transition-all cursor-pointer min-h-[48px]"
        >
          <BookOpen className="w-4 h-4 text-[#DFFF00]" />
          <span>Tactical Hub</span>
        </Link>
        <Link
          href="/fixtures"
          className="inline-flex items-center gap-2 px-5 py-3 rounded-2xl bg-white/5 hover:bg-white/10 border border-white/15 text-white font-bold text-xs uppercase tracking-wider transition-all cursor-pointer min-h-[48px]"
        >
          <Calendar className="w-4 h-4 text-[#38BDF8]" />
          <span>Live Fixtures</span>
        </Link>
      </div>
    </div>
  );
}
