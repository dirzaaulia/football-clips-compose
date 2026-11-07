'use client';

import React, { useState, useMemo } from 'react';
import { BROADCAST_DIRECTORIES } from '@/lib/tools-data';
import { Tv, Globe, ExternalLink } from 'lucide-react';

export default function BroadcastDirectory() {
  const [selectedCountryCode, setSelectedCountryCode] = useState('US');

  const activeCountry = useMemo(
    () => BROADCAST_DIRECTORIES.find((c) => c.countryCode === selectedCountryCode) || BROADCAST_DIRECTORIES[0],
    [selectedCountryCode],
  );

  return (
    <div className="space-y-6">
      {/* Country Selection */}
      <div className="flex gap-2 overflow-x-auto pb-2 scrollbar-none">
        {BROADCAST_DIRECTORIES.map((c) => (
          <button
            key={c.countryCode}
            type="button"
            onClick={() => setSelectedCountryCode(c.countryCode)}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all flex items-center gap-2 cursor-pointer whitespace-nowrap ${
              selectedCountryCode === c.countryCode
                ? 'bg-[#7098F8] text-[#101418] shadow-md shadow-[#7098F8]/20'
                : 'bg-[#181C20] text-[#94A3B8] border border-[#334155]/60 hover:text-white'
            }`}
          >
            <span>{c.flag}</span>
            <span>{c.countryName}</span>
          </button>
        ))}
      </div>

      {/* Broadcaster Directory Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {activeCountry.competitions.map((comp, idx) => (
          <BroadcasterCard key={idx} comp={comp} countryName={activeCountry.countryName} />
        ))}
      </div>
    </div>
  );
}

interface CompBroadcastProps {
  competitionName: string;
  competitionCode: string;
  broadcasterName: string;
  platformType: string;
  officialWebsite: string;
  commentaryLanguages: string[];
}

function BroadcasterCard({ comp, countryName }: { comp: CompBroadcastProps; countryName: string }) {
  return (
    <div className="p-5 rounded-2xl bg-[#181C20] border border-[#334155]/60 flex flex-col justify-between space-y-4 hover:border-[#7098F8]/40 transition-colors">
      <div>
        <div className="flex items-center justify-between gap-2 mb-2">
          <span className="text-xs font-extrabold text-[#7098F8] uppercase tracking-wider">
            {comp.competitionName}
          </span>
          <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-white/5 border border-white/10 text-[#94A3B8]">
            {comp.platformType}
          </span>
        </div>
        <h4 className="text-base font-black text-white">{comp.broadcasterName}</h4>
        <p className="text-xs text-[#94A3B8] mt-1">
          Official licensed carrier in <strong className="text-white">{countryName}</strong>.
        </p>
      </div>

      <div className="pt-3 border-t border-[#334155]/40 flex items-center justify-between">
        <div className="flex items-center gap-1.5 text-[11px] text-[#94A3B8]">
          <Globe className="w-3.5 h-3.5" />
          <span>Languages: {comp.commentaryLanguages.join(', ')}</span>
        </div>
        <a
          href={comp.officialWebsite}
          target="_blank"
          rel="noopener noreferrer"
          className="inline-flex items-center gap-1 px-3 py-1.5 rounded-xl bg-[#7098F8]/10 hover:bg-[#7098F8]/20 border border-[#7098F8]/30 text-xs font-bold text-[#7098F8] transition-all"
        >
          <span>Watch</span>
          <ExternalLink className="w-3.5 h-3.5" />
        </a>
      </div>
    </div>
  );
}
