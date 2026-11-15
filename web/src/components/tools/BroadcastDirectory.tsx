'use client';

import React, { useState, useEffect, useMemo } from 'react';
import { Tv, Globe, ExternalLink, Search, ShieldCheck, Video } from 'lucide-react';
import { detectUserCountryCode } from '@/lib/broadcast';

interface BroadcasterRight {
  competition_id: string;
  country_code: string;
  country_name: string;
  broadcaster_name: string;
  stream_url: string;
  official_youtube_channel?: string | null;
  tier?: string;
}

interface CountryDirectory {
  name: string;
  rights: Record<string, BroadcasterRight>;
}

const POPULAR_COUNTRIES = ['ID', 'US', 'GB', 'ES', 'DE', 'FR', 'IT', 'BR', 'JP', 'AU', 'SA', 'CA'];

const COMP_NAMES: Record<string, string> = {
  PL: 'Premier League',
  PD: 'La Liga EA Sports',
  SA: 'Serie A Enilive',
  BL1: 'Bundesliga',
  FL1: 'Ligue 1 McDonald’s',
  CL: 'UEFA Champions League',
  EL: 'UEFA Europa League',
  ECL: 'UEFA Conference League',
  FAC: 'Emirates FA Cup',
  EFL: 'Carabao Cup',
  CDR: 'Copa del Rey',
  DFB: 'DFB-Pokal',
  CIT: 'Coppa Italia',
  SPL: 'Saudi Pro League',
  MLS: 'Major League Soccer',
  DED: 'Eredivisie',
  PPL: 'Liga Portugal',
};

function getCountryFlag(code: string): string {
  if (!code || code.length !== 2) return '🌐';
  try {
    const codePoints = code
      .toUpperCase()
      .split('')
      .map((c) => 127397 + c.charCodeAt(0));
    return String.fromCodePoint(...codePoints);
  } catch {
    return '🌐';
  }
}

export default function BroadcastDirectory() {
  const [directory, setDirectory] = useState<Record<string, CountryDirectory>>({});
  const [selectedCode, setSelectedCode] = useState('ID');
  const [searchQuery, setSearchQuery] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function loadDirectory() {
      try {
        const detected = detectUserCountryCode();
        setSelectedCode(detected);
        const res = await fetch('/broadcast_directory.json');
        if (res.ok) {
          const json = await res.json();
          setDirectory(json.countries || json || {});
        }
      } catch (err) {
        console.warn('Failed to load global broadcast directory:', err);
      } finally {
        setIsLoading(false);
      }
    }
    loadDirectory();
  }, []);

  const countryList = useMemo(() => {
    return Object.entries(directory).map(([code, data]) => ({
      code,
      name: data.name || code,
      flag: getCountryFlag(code),
      rightsCount: Object.keys(data.rights || {}).length,
    })).sort((a, b) => a.name.localeCompare(b.name));
  }, [directory]);

  const filteredCountries = useMemo(() => {
    if (!searchQuery.trim()) return countryList;
    const q = searchQuery.toLowerCase().trim();
    return countryList.filter(
      (c) => c.name.toLowerCase().includes(q) || c.code.toLowerCase().includes(q)
    );
  }, [countryList, searchQuery]);

  const activeCountry = directory[selectedCode] || directory['US'] || { name: 'Global', rights: {} };
  const rightsList = useMemo(() => {
    return Object.entries(activeCountry.rights || {}).map(([compId, r]) => ({
      compId,
      compName: COMP_NAMES[compId] || compId,
      ...r,
    }));
  }, [activeCountry]);

  return (
    <div className="space-y-6">
      {/* Search & Selector Bar */}
      <div className="p-4 rounded-3xl bg-[#0F141C] border border-white/10 space-y-3.5 shadow-lg">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <Tv className="w-5 h-5 text-cyan-400" />
            <h3 className="text-sm font-black text-white tracking-tight">
              Global Verified Broadcasters ({countryList.length} Countries)
            </h3>
          </div>

          {/* Quick Search Input */}
          <div className="relative w-full sm:w-72">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search country or code (e.g. Japan, ID, UK)..."
              className="w-full pl-9 pr-3 py-1.5 rounded-xl bg-[#161D26] border border-white/10 text-xs text-white placeholder-slate-400 focus:outline-none focus:border-cyan-500/50 transition-colors"
            />
          </div>
        </div>

        {/* Popular Country Chips or Filtered Matches */}
        <div className="flex gap-2 overflow-x-auto pb-1 scrollbar-none">
          {searchQuery ? (
            filteredCountries.slice(0, 15).map((c) => (
              <button
                key={c.code}
                onClick={() => { setSelectedCode(c.code); setSearchQuery(''); }}
                className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all flex items-center gap-1.5 whitespace-nowrap cursor-pointer ${
                  selectedCode === c.code
                    ? 'bg-cyan-500 text-slate-950 shadow-md shadow-cyan-500/20'
                    : 'bg-[#161D26] text-slate-300 border border-white/5 hover:text-white'
                }`}
              >
                <span>{c.flag}</span>
                <span>{c.name}</span>
              </button>
            ))
          ) : (
            POPULAR_COUNTRIES.map((code) => {
              const item = countryList.find((c) => c.code === code);
              if (!item) return null;
              return (
                <button
                  key={code}
                  onClick={() => setSelectedCode(code)}
                  className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all flex items-center gap-1.5 whitespace-nowrap cursor-pointer ${
                    selectedCode === code
                      ? 'bg-cyan-500 text-slate-950 shadow-md shadow-cyan-500/20'
                      : 'bg-[#161D26] text-slate-300 border border-white/5 hover:text-white'
                  }`}
                >
                  <span>{item.flag}</span>
                  <span>{item.name}</span>
                </button>
              );
            })
          )}
        </div>
      </div>

      {/* Broadcaster Directory Cards */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="h-40 rounded-2xl bg-[#161D26] animate-pulse border border-white/5" />
          ))}
        </div>
      ) : rightsList.length === 0 ? (
        <div className="p-8 rounded-2xl bg-[#0F141C] border border-white/10 text-center text-slate-400">
          No official broadcast data listed for {activeCountry.name}. Check back soon.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {rightsList.map((comp) => (
            <div
              key={comp.compId}
              className="p-5 rounded-2xl bg-[#0F141C] border border-white/10 hover:border-cyan-500/40 transition-all flex flex-col justify-between space-y-4 shadow-sm"
            >
              <div>
                <div className="flex items-center justify-between gap-2 mb-2">
                  <span className="text-xs font-black text-cyan-400 uppercase tracking-wider">
                    {comp.compName}
                  </span>
                  <span className="flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/10 border border-emerald-500/20 text-emerald-400">
                    <ShieldCheck className="w-2.5 h-2.5" />
                    Verified
                  </span>
                </div>
                <h4 className="text-base font-extrabold text-white">{comp.broadcaster_name}</h4>
                <p className="text-xs text-slate-400 mt-1">
                  Official carrier for <strong className="text-white">{activeCountry.name}</strong>.
                </p>
              </div>

              <div className="pt-3 border-t border-white/5 flex items-center justify-between">
                {comp.official_youtube_channel ? (
                  <span className="flex items-center gap-1 text-[11px] text-rose-400">
                    <Video className="w-3.5 h-3.5" />
                    <span>{comp.official_youtube_channel}</span>
                  </span>
                ) : (
                  <span className="text-[11px] text-slate-400 flex items-center gap-1">
                    <Globe className="w-3.5 h-3.5" />
                    <span>Territorial Rights</span>
                  </span>
                )}
                <a
                  href={comp.stream_url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-1 px-3 py-1.5 rounded-xl bg-cyan-500/10 hover:bg-cyan-500/20 border border-cyan-500/30 text-xs font-bold text-cyan-400 transition-all"
                >
                  <span>Stream</span>
                  <ExternalLink className="w-3.5 h-3.5" />
                </a>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
