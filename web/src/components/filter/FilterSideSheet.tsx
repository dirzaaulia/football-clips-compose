'use client';

import React, { useEffect } from 'react';
import Image from 'next/image';
import { X, Search, RotateCcw, Filter, Check } from 'lucide-react';
import { FilterOption } from '@/lib/middleware-highlights';

interface FilterSideSheetProps {
  isOpen: boolean;
  onClose: () => void;
  searchQuery: string;
  onSearchChange: (q: string) => void;
  availableCountries: FilterOption[];
  selectedCountries: string[];
  onToggleCountry: (country: string) => void;
  availableLeagues: FilterOption[];
  selectedLeagues: string[];
  onToggleLeague: (league: string) => void;
  onReset: () => void;
  matchesFound: number;
  hasMore?: boolean;
  isLoadingMore?: boolean;
  onLoadMore?: () => void;
}

export default function FilterSideSheet({
  isOpen,
  onClose,
  searchQuery,
  onSearchChange,
  availableCountries,
  selectedCountries,
  onToggleCountry,
  availableLeagues,
  selectedLeagues,
  onToggleLeague,
  onReset,
  matchesFound,
  hasMore = false,
  isLoadingMore = false,
  onLoadMore,
}: FilterSideSheetProps) {
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'unset';
    }
    return () => {
      document.body.style.overflow = 'unset';
    };
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex justify-end">
      {/* Backdrop */}
      <div
        className="fixed inset-0 bg-black/60 backdrop-blur-xs transition-opacity duration-300"
        onClick={onClose}
      />

      {/* Side Sheet Content */}
      <div className="relative w-full max-w-md h-full bg-[#181C20] border-l border-[#334155] shadow-2xl flex flex-col z-10 animate-in slide-in-from-right duration-300">
        {/* Header */}
        <div className="flex items-center justify-between px-5 py-4 border-b border-[#334155]/60 bg-[#101418]">
          <div className="flex items-center gap-2">
            <Filter className="w-4 h-4 text-[#7098F8]" />
            <h2 className="text-base font-black text-white">Search & Filter</h2>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={onReset}
              className="text-xs font-bold text-red-400 hover:text-red-300 transition-colors px-2 py-1 rounded hover:bg-red-500/10 cursor-pointer"
            >
              Clear All
            </button>
            <button
              onClick={onClose}
              className="p-1.5 text-[#94A3B8] hover:text-white rounded-lg hover:bg-[#1E2227] transition-colors cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Search Input Bar */}
        <div className="p-4 border-b border-[#334155]/40 bg-[#14181D]">
          <div className="relative flex items-center">
            <Search className="w-4 h-4 text-[#94A3B8] absolute left-3.5 pointer-events-none" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => onSearchChange(e.target.value)}
              placeholder="Filter by team name..."
              className="w-full pl-9 pr-9 py-2 bg-[#101418] border border-[#334155] focus:border-[#7098F8] rounded-xl text-xs text-white placeholder-[#94A3B8] outline-none transition-colors"
            />
            {searchQuery && (
              <button
                onClick={() => onSearchChange('')}
                className="absolute right-3 p-0.5 text-[#94A3B8] hover:text-white cursor-pointer"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            )}
          </div>
        </div>

        {/* Scrollable Filters */}
        <div className="flex-1 overflow-y-auto p-5 space-y-6 scrollbar-thin">
          {/* Countries Filter */}
          {availableCountries.length > 0 && (
            <div className="space-y-2.5">
              <span className="text-xs font-bold uppercase tracking-wider text-[#7098F8]">
                Select Country
              </span>
              <div className="flex flex-wrap gap-2">
                {availableCountries.map((c) => {
                  const isSelected = selectedCountries.includes(c.name);
                  return (
                    <button
                      key={c.name}
                      onClick={() => onToggleCountry(c.name)}
                      className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold border transition-all cursor-pointer ${
                        isSelected
                          ? 'bg-[#1E3A8A]/50 text-white border-[#7098F8] shadow-[0_0_10px_rgba(112,152,248,0.25)]'
                          : 'bg-[#1E2227] text-[#94A3B8] hover:text-white border-[#334155]'
                      }`}
                    >
                      {c.logo && (
                        <div className="relative w-3.5 h-3.5 shrink-0">
                          <Image src={c.logo} alt={c.name} fill className="object-contain" unoptimized />
                        </div>
                      )}
                      <span>{c.name}</span>
                      <span className="text-[10px] text-[#94A3B8] font-normal">({c.count})</span>
                      {isSelected && <Check className="w-3 h-3 text-[#4FC3F7] ml-0.5" />}
                    </button>
                  );
                })}
              </div>
            </div>
          )}

          {/* Leagues Filter */}
          {availableLeagues.length > 0 && (
            <div className="space-y-2.5">
              <span className="text-xs font-bold uppercase tracking-wider text-[#7098F8]">
                Select League
              </span>
              <div className="flex flex-wrap gap-2">
                {availableLeagues.map((l) => {
                  const isSelected = selectedLeagues.includes(l.name);
                  return (
                    <button
                      key={l.name}
                      onClick={() => onToggleLeague(l.name)}
                      className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold border transition-all cursor-pointer ${
                        isSelected
                          ? 'bg-[#1E3A8A]/50 text-white border-[#7098F8] shadow-[0_0_10px_rgba(112,152,248,0.25)]'
                          : 'bg-[#1E2227] text-[#94A3B8] hover:text-white border-[#334155]'
                      }`}
                    >
                      {l.logo && (
                        <div className="relative w-3.5 h-3.5 shrink-0">
                          <Image src={l.logo} alt={l.name} fill className="object-contain" unoptimized />
                        </div>
                      )}
                      <span>{l.name}</span>
                      <span className="text-[10px] text-[#94A3B8] font-normal">({l.count})</span>
                      {isSelected && <Check className="w-3 h-3 text-[#4FC3F7] ml-0.5" />}
                    </button>
                  );
                })}
              </div>
            </div>
          )}

          {/* Load More Option within Filter Sheet (Android Parity) */}
          {hasMore && onLoadMore && (
            <div className="p-4 rounded-2xl bg-[#1E2227]/60 border border-[#334155] space-y-2.5">
              <p className="text-xs font-bold text-white">Looking for more matches?</p>
              <p className="text-[11px] text-[#94A3B8] leading-relaxed">
                Fetch older fixtures to discover more leagues and highlight clips.
              </p>
              <button
                disabled={isLoadingMore}
                onClick={onLoadMore}
                className="w-full flex items-center justify-center gap-2 py-2 rounded-xl bg-[#181C20] border border-[#7098F8]/40 hover:bg-[#1E3A8A]/30 text-[#7098F8] text-xs font-bold transition-colors cursor-pointer disabled:opacity-50"
              >
                <RotateCcw className={`w-3.5 h-3.5 ${isLoadingMore ? 'animate-spin' : ''}`} />
                <span>{isLoadingMore ? 'Loading More...' : 'Load 40 More Highlights'}</span>
              </button>
            </div>
          )}
        </div>

        {/* Footer Apply Button */}
        <div className="p-4 border-t border-[#334155]/60 bg-[#101418]">
          <button
            onClick={onClose}
            className="w-full py-2.5 rounded-xl bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white font-bold text-xs hover:brightness-110 transition-all shadow-[0_0_15px_rgba(112,152,248,0.3)] cursor-pointer"
          >
            Show Results ({matchesFound})
          </button>
        </div>
      </div>
    </div>
  );
}
