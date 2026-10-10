'use client';

import React, { useState, useEffect } from 'react';
import { SlidersHorizontal, ArrowUp } from 'lucide-react';

interface FloatingActionControlsProps {
  activeFilterCount: number;
  onOpenFilter: () => void;
}

export default function FloatingActionControls({
  activeFilterCount,
  onOpenFilter,
}: FloatingActionControlsProps) {
  const [isScrolled, setIsScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 300);
    };

    window.addEventListener('scroll', handleScroll, { passive: true });
    handleScroll();

    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  if (!isScrolled) return null;

  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <div className="fixed bottom-20 md:bottom-8 right-4 sm:right-6 z-40 flex flex-col items-end gap-2.5 animate-in fade-in slide-in-from-bottom-4 duration-300">
      {/* Floating Filter Button (Appears when scrolled past top filter bar) */}
      <button
        onClick={onOpenFilter}
        className={`flex items-center gap-2 px-3.5 py-2.5 rounded-full shadow-2xl backdrop-blur-md transition-all cursor-pointer hover:scale-105 active:scale-95 ${
          activeFilterCount > 0
            ? 'bg-[#1E3A8A]/90 text-white border border-[#7098F8] shadow-[0_0_15px_rgba(112,152,248,0.4)]'
            : 'bg-[#181C20]/90 text-[#F1F5F9] hover:bg-[#1E2227] border border-[#334155]'
        }`}
        title="Open Search & Filter"
      >
        <SlidersHorizontal className="w-4 h-4 text-[#7098F8]" />
        <span className="text-xs font-bold">Filter</span>
        {activeFilterCount > 0 && (
          <span className="px-1.5 py-0.2 bg-[#7098F8] text-[#101418] rounded-full text-[10px] font-black">
            {activeFilterCount}
          </span>
        )}
      </button>

      {/* Floating Scroll to Top FAB (Android WebScrollToTopFab Parity) */}
      <button
        onClick={scrollToTop}
        className="w-10 h-10 rounded-full bg-[#181C20]/90 hover:bg-[#7098F8] text-[#94A3B8] hover:text-[#101418] border border-[#334155] hover:border-[#7098F8] shadow-2xl flex items-center justify-center transition-all cursor-pointer hover:scale-105 active:scale-95 backdrop-blur-md"
        title="Scroll to Top"
        aria-label="Scroll to top"
      >
        <ArrowUp className="w-4 h-4" />
      </button>
    </div>
  );
}
