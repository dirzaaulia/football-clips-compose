'use client';

import React from 'react';
import { Check, X, Globe } from 'lucide-react';

export interface MarketItem {
  code: string;
  name: string;
  flag: string;
}

export interface RegionGroup {
  name: string;
  icon: string;
  markets: MarketItem[];
}

export const REGIONAL_MARKETS: RegionGroup[] = [
  {
    name: 'Europe',
    icon: '🏰',
    markets: [
      { code: 'GB', name: 'UK', flag: '🇬🇧' },
      { code: 'ES', name: 'Spain', flag: '🇪🇸' },
      { code: 'DE', name: 'Germany', flag: '🇩🇪' },
      { code: 'IT', name: 'Italy', flag: '🇮🇹' },
      { code: 'FR', name: 'France', flag: '🇫🇷' },
      { code: 'PT', name: 'Portugal', flag: '🇵🇹' },
      { code: 'NL', name: 'Netherlands', flag: '🇳🇱' },
      { code: 'TR', name: 'Turkey', flag: '🇹🇷' },
      { code: 'GR', name: 'Greece', flag: '🇬🇷' },
      { code: 'PL', name: 'Poland', flag: '🇵🇱' },
      { code: 'CZ', name: 'Czechia', flag: '🇨🇿' },
      { code: 'RO', name: 'Romania', flag: '🇷🇴' },
      { code: 'HR', name: 'Croatia', flag: '🇭🇷' },
      { code: 'RS', name: 'Serbia', flag: '🇷🇸' },
      { code: 'UA', name: 'Ukraine', flag: '🇺🇦' },
      { code: 'SE', name: 'Sweden', flag: '🇸🇪' },
      { code: 'NO', name: 'Norway', flag: '🇳🇴' },
      { code: 'DK', name: 'Denmark', flag: '🇩🇰' },
      { code: 'BE', name: 'Belgium', flag: '🇧🇪' },
      { code: 'AT', name: 'Austria', flag: '🇦🇹' },
      { code: 'CH', name: 'Switzerland', flag: '🇨🇭' },
      { code: 'BG', name: 'Bulgaria', flag: '🇧🇬' },
    ],
  },
  {
    name: 'Americas',
    icon: '🌎',
    markets: [
      { code: 'US', name: 'USA', flag: '🇺🇸' },
      { code: 'BR', name: 'Brazil', flag: '🇧🇷' },
      { code: 'AR', name: 'Argentina', flag: '🇦🇷' },
      { code: 'MX', name: 'Mexico', flag: '🇲🇽' },
      { code: 'CO', name: 'Colombia', flag: '🇨🇴' },
      { code: 'CA', name: 'Canada', flag: '🇨🇦' },
      { code: 'CL', name: 'Chile', flag: '🇨🇱' },
    ],
  },
  {
    name: 'Asia & Middle East',
    icon: '🌏',
    markets: [
      { code: 'ID', name: 'Indonesia', flag: '🇮🇩' },
      { code: 'SA', name: 'Saudi Arabia', flag: '🇸🇦' },
      { code: 'JP', name: 'Japan', flag: '🇯🇵' },
      { code: 'KR', name: 'South Korea', flag: '🇰🇷' },
      { code: 'KZ', name: 'Kazakhstan', flag: '🇰🇿' },
      { code: 'IN', name: 'India', flag: '🇮🇳' },
    ],
  },
  {
    name: 'Africa & Oceania',
    icon: '🌍',
    markets: [
      { code: 'AU', name: 'Australia', flag: '🇦🇺' },
      { code: 'NG', name: 'Nigeria', flag: '🇳🇬' },
      { code: 'EG', name: 'Egypt', flag: '🇪🇬' },
      { code: 'MA', name: 'Morocco', flag: '🇲🇦' },
      { code: 'ZA', name: 'South Africa', flag: '🇿🇦' },
    ],
  },
];

interface RegionalDeckProps {
  coveredMarkets: string[];
}

export function RegionalTargetMarketsDeck({ coveredMarkets }: RegionalDeckProps) {
  const coveredSet = new Set(coveredMarkets.map((m) => m.toUpperCase()));

  return (
    <div className="space-y-3 pt-2">
      <div className="flex items-center justify-between text-xs font-mono text-slate-400">
        <span className="flex items-center gap-1.5 font-bold uppercase tracking-wider text-slate-300">
          <Globe className="w-3.5 h-3.5 text-indigo-400" />
          Regional Broadcast Market Rights Coverage
        </span>
        <span className="text-[10px] text-slate-500">
          {coveredSet.size} covered
        </span>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-2.5">
        {REGIONAL_MARKETS.map((region) => {
          const regionCoveredCount = region.markets.filter((m) => coveredSet.has(m.code)).length;
          const isFull = regionCoveredCount === region.markets.length;

          return (
            <div
              key={region.name}
              className="bg-slate-950/60 border border-slate-800/80 rounded-xl p-2.5 space-y-2"
            >
              <div className="flex items-center justify-between text-[11px] font-mono">
                <span className="font-bold text-slate-300 flex items-center gap-1.5">
                  <span>{region.icon}</span>
                  <span>{region.name}</span>
                </span>
                <span className={`px-1.5 py-0.5 rounded text-[10px] font-bold ${
                  isFull
                    ? 'bg-emerald-500/15 text-emerald-300 border border-emerald-500/30'
                    : 'bg-amber-500/15 text-amber-300 border border-amber-500/30'
                }`}>
                  {regionCoveredCount}/{region.markets.length}
                </span>
              </div>

              <div className="flex flex-wrap gap-1">
                {region.markets.map((m) => {
                  const isCovered = coveredSet.has(m.code);
                  return (
                    <span
                      key={m.code}
                      title={`${m.name} (${m.code}): ${isCovered ? 'Available' : 'Restricted'}`}
                      className={`inline-flex items-center gap-1 px-1.5 py-0.5 rounded text-[10px] font-mono font-medium transition border ${
                        isCovered
                          ? 'bg-emerald-500/10 text-emerald-300 border-emerald-500/20'
                          : 'bg-rose-950/30 text-rose-400/90 border-rose-800/40 opacity-70'
                      }`}
                    >
                      <span className="text-xs leading-none">{m.flag}</span>
                      <span className="font-bold">{m.code}</span>
                      {isCovered ? (
                        <Check className="w-2.5 h-2.5 text-emerald-400" />
                      ) : (
                        <X className="w-2.5 h-2.5 text-rose-400" />
                      )}
                    </span>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
