'use client';

import React, { useState, useEffect } from 'react';
import { Tv, ExternalLink, Globe, ShieldCheck } from 'lucide-react';
import { getOfficialBroadcaster, BroadcasterInfo, detectUserCountryCode } from '@/lib/broadcast';

interface WhereToWatchBannerProps {
  competitionId: string;
  className?: string;
}

export default function WhereToWatchBanner({
  competitionId,
  className = ''
}: WhereToWatchBannerProps) {
  const [broadcaster, setBroadcaster] = useState<BroadcasterInfo | null>(null);
  const [countryCode, setCountryCode] = useState<string>('US');

  useEffect(() => {
    const code = detectUserCountryCode();
    setCountryCode(code);
    getOfficialBroadcaster(competitionId, code).then(setBroadcaster);
  }, [competitionId]);

  if (!broadcaster) return null;

  return (
    <div
      className={`flex flex-col sm:flex-row sm:items-center justify-between gap-3.5 p-3.5 sm:p-4 rounded-2xl bg-gradient-to-r from-[#181C20] via-[#1A2230] to-[#181C20] border border-[#334155] shadow-lg ${className}`}
    >
      <div className="flex items-center gap-3">
        <div className="w-10 h-10 rounded-xl bg-[#1E3A8A]/40 border border-[#7098F8]/40 flex items-center justify-center shrink-0 shadow-sm">
          <Tv className="w-5 h-5 text-[#4FC3F7]" />
        </div>
        <div>
          <div className="flex items-center gap-2">
            <span className="text-xs font-bold text-[#94A3B8] uppercase tracking-wider flex items-center gap-1">
              <Globe className="w-3 h-3 text-[#7098F8]" />
              Official Live Stream ({broadcaster.country_name})
            </span>
            <span className="inline-flex items-center gap-1 px-1.5 py-0.5 rounded text-[9px] font-black bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
              <ShieldCheck className="w-2.5 h-2.5" />
              Verified Rights Holder
            </span>
          </div>
          <h3 className="text-sm sm:text-base font-extrabold text-white mt-0.5">
            {broadcaster.broadcaster_name}
          </h3>
        </div>
      </div>

      <div className="flex items-center gap-2 self-start sm:self-auto shrink-0">
        <a
          href={broadcaster.stream_url}
          target="_blank"
          rel="noopener noreferrer"
          className="flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-extrabold text-white bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] hover:from-[#1E3A8A] hover:to-[#4FC3F7] border border-[#7098F8]/50 shadow-[0_0_14px_rgba(112,152,248,0.3)] transition-all cursor-pointer group"
          title={`Watch ${competitionId} live on ${broadcaster.broadcaster_name}`}
        >
          <span>Watch Official Stream</span>
          <ExternalLink className="w-3.5 h-3.5 group-hover:translate-x-0.5 transition-transform" />
        </a>
      </div>
    </div>
  );
}
