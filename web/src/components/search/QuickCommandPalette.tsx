'use client';

import React, { useEffect, useState, useRef } from 'react';
import { useRouter } from 'next/navigation';

interface SearchResult {
  id: number;
  home_team_name: string;
  away_team_name: string;
  competition_name: string;
  status: string;
  utc_date: string;
}

export default function QuickCommandPalette() {
  const [isOpen, setIsOpen] = useState(false);
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<SearchResult[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const inputRef = useRef<HTMLInputElement>(null);
  const router = useRouter();

  // Listen for Cmd+K / Ctrl+K keyboard shortcut
  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
        e.preventDefault();
        setIsOpen((prev) => !prev);
      } else if (e.key === 'Escape') {
        setIsOpen(false);
      }
    }
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  // Focus input when opened
  useEffect(() => {
    if (isOpen) {
      setTimeout(() => inputRef.current?.focus(), 50);
      setSelectedIndex(0);
    }
  }, [isOpen]);

  // Debounced live search via PostgREST
  useEffect(() => {
    if (!query.trim() || query.length < 2) {
      setResults([]);
      return;
    }
    const timer = setTimeout(async () => {
      setLoading(true);
      try {
        const clean = encodeURIComponent(query.trim());
        const url = `https://edge.fc.dirzaaulia.com/rest/v1/matches?or=(home_team_name.ilike.*${clean}*,away_team_name.ilike.*${clean}*,competition_name.ilike.*${clean}*)&order=utc_date.desc&limit=8`;
        const res = await fetch(url);
        if (res.ok) {
          const data = await res.json();
          setResults(Array.isArray(data) ? data : []);
          setSelectedIndex(0);
        }
      } catch {
        // Ignore network errors
      } finally {
        setLoading(false);
      }
    }, 200);

    return () => clearTimeout(timer);
  }, [query]);

  // Keyboard navigation inside palette
  function handleInputKeyDown(e: React.KeyboardEvent) {
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setSelectedIndex((prev) => (prev < results.length - 1 ? prev + 1 : prev));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setSelectedIndex((prev) => (prev > 0 ? prev - 1 : 0));
    } else if (e.key === 'Enter' && results[selectedIndex]) {
      e.preventDefault();
      router.push(`/match?id=${results[selectedIndex].id}`);
      setIsOpen(false);
    }
  }

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-start justify-center pt-20 px-4 bg-black/70 backdrop-blur-sm">
      <div
        className="w-full max-w-xl bg-[#161B22] border border-white/10 rounded-2xl shadow-2xl overflow-hidden flex flex-col"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Search Input Bar */}
        <div className="flex items-center px-4 py-3 border-b border-white/10 gap-3">
          <svg className="w-5 h-5 text-gray-400 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input
            ref={inputRef}
            type="text"
            placeholder="Search club, competition, or match (e.g. Arsenal, Real Madrid, UCL)..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={handleInputKeyDown}
            className="w-full bg-transparent text-white placeholder-gray-500 text-sm focus:outline-none"
          />
          <kbd className="hidden sm:inline-block px-1.5 py-0.5 text-[10px] font-mono text-gray-400 bg-white/5 border border-white/10 rounded">
            ESC
          </kbd>
        </div>

        {/* Results List */}
        <div className="max-h-80 overflow-y-auto p-2 divide-y divide-white/5">
          {loading && (
            <div className="py-6 text-center text-xs text-gray-400 animate-pulse">
              Searching European football database...
            </div>
          )}

          {!loading && results.length === 0 && query.length >= 2 && (
            <div className="py-6 text-center text-xs text-gray-500">
              No matches found matching &quot;{query}&quot;
            </div>
          )}

          {!loading && query.length < 2 && (
            <div className="py-6 text-center text-xs text-gray-500">
              Type at least 2 characters to search clubs or fixtures
            </div>
          )}

          {results.map((match, idx) => (
            <div
              key={match.id}
              onClick={() => {
                router.push(`/match?id=${match.id}`);
                setIsOpen(false);
              }}
              onMouseEnter={() => setSelectedIndex(idx)}
              className={`flex items-center justify-between p-3 rounded-xl cursor-pointer transition-colors ${
                selectedIndex === idx ? 'bg-[#21262D] text-white' : 'text-gray-300 hover:bg-[#1C2128]'
              }`}
            >
              <div className="flex flex-col gap-0.5">
                <span className="text-xs font-semibold">
                  {match.home_team_name} vs {match.away_team_name}
                </span>
                <span className="text-[10px] text-gray-400 font-mono">
                  {match.competition_name} · {match.status}
                </span>
              </div>
              <span className="text-[10px] font-mono bg-white/5 px-2 py-0.5 rounded text-gray-400">
                View
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
