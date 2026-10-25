'use client';

import React, { useState } from 'react';
import Image from 'next/image';
import { Radio, ArrowRight, Shield } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import { useVideoPlayer } from '@/hooks/useVideoPlayer';

interface LiveMatchSectionProps {
  liveMatches: MatchRecord[];
  className?: string;
}

const ESPN_COMPETITION_LOGOS: Record<string, string> = {
  CL: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2.png',
  EL: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2310.png',
  ECL: 'https://a.espncdn.com/i/leaguelogos/soccer/500/20296.png',
  PL: 'https://a.espncdn.com/i/leaguelogos/soccer/500/23.png',
  PD: 'https://a.espncdn.com/i/leaguelogos/soccer/500/15.png',
  SA: 'https://a.espncdn.com/i/leaguelogos/soccer/500/12.png',
  BL1: 'https://a.espncdn.com/i/leaguelogos/soccer/500/10.png',
  FL1: 'https://a.espncdn.com/i/leaguelogos/soccer/500/9.png',
  FAC: 'https://a.espncdn.com/i/leaguelogos/soccer/500/40.png',
  EFL: 'https://a.espncdn.com/i/leaguelogos/soccer/500/41.png',
  CDR: 'https://a.espncdn.com/i/leaguelogos/soccer/500/80.png',
  DFB: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2061.png',
  CIT: 'https://a.espncdn.com/i/leaguelogos/soccer/500/83.png',
  PPL: 'https://a.espncdn.com/i/leaguelogos/soccer/500/14.png',
  DED: 'https://a.espncdn.com/i/leaguelogos/soccer/500/11.png',
  MLS: 'https://a.espncdn.com/i/leaguelogos/soccer/500/19.png',
  SPL: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2488.png',
  IDN: 'https://a.espncdn.com/i/leaguelogos/soccer/500/2336.png',
};

function TeamLiveCrest({ crestUrl, teamName }: { crestUrl?: string | null; teamName: string }) {
  const [error, setError] = useState(false);
  if (!crestUrl || error) {
    return (
      <div className="w-11 h-11 rounded-full bg-[#181C20] border border-[#334155] flex items-center justify-center text-[#7098F8] font-black text-sm shadow-md">
        {teamName.charAt(0) || 'T'}
      </div>
    );
  }
  return (
    <div className="relative w-11 h-11 drop-shadow-[0_2px_8px_rgba(0,0,0,0.6)]">
      <Image
        src={crestUrl}
        alt={teamName}
        fill
        className="object-contain"
        unoptimized
        onError={() => setError(true)}
      />
    </div>
  );
}

export default function LiveMatchSection({ liveMatches, className = '' }: LiveMatchSectionProps) {
  const { playVideo } = useVideoPlayer();

  const groupedLive = React.useMemo(() => {
    if (!liveMatches || liveMatches.length === 0) return [];
    const map = new Map<string, MatchRecord[]>();
    liveMatches.forEach(m => {
      const comp = m.competition_name || 'Other Competitions';
      if (!map.has(comp)) map.set(comp, []);
      map.get(comp)!.push(m);
    });
    return Array.from(map.entries());
  }, [liveMatches]);

  if (!liveMatches || liveMatches.length === 0) return null;

  return (
    <section className={`mb-8 ${className}`}>
      {/* Section Header */}
      <div className="flex items-center justify-between pb-3 border-b border-red-500/20 mb-4">
        <div className="flex items-center gap-2.5">
          <div className="relative flex items-center justify-center">
            <span className="absolute w-3.5 h-3.5 rounded-full bg-red-500/40 animate-ping" />
            <span className="relative w-2.5 h-2.5 rounded-full bg-red-500 shadow-[0_0_10px_#ef4444]" />
          </div>
          <div className="flex items-center gap-2">
            <h2 className="text-base sm:text-lg font-black text-white tracking-tight uppercase">
              Live Match Centre
            </h2>
            <span className="px-2 py-0.5 rounded-full text-xs font-black uppercase bg-red-500/20 text-red-400 border border-red-500/40 tracking-wider">
              {liveMatches.length} In-Play
            </span>
          </div>
        </div>
      </div>

      {/* Grouped Live Matches */}
      <div className="space-y-6">
        {groupedLive.map(([competitionName, matches]) => {
          const compCode = matches[0]?.competition_id;
          const compLogo = compCode ? ESPN_COMPETITION_LOGOS[compCode] : null;

          return (
            <div key={competitionName} className="space-y-3">
              <div className="flex items-center gap-2 px-1">
                {compLogo ? (
                  <div className="relative w-5 h-5 shrink-0">
                    <Image src={compLogo} alt={competitionName} fill className="object-contain" unoptimized />
                  </div>
                ) : (
                  <Shield className="w-4 h-4 text-[#7098F8]" />
                )}
                <h3 className="text-xs sm:text-sm font-bold text-white uppercase tracking-wider">{competitionName}</h3>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {matches.map((m) => {
                  const homeName = m.home_team_name || m.home_team;
                  const awayName = m.away_team_name || m.away_team;
                  const isPaused = m.status === 'PAUSED';
                  const statusText = isPaused ? 'Half Time' : m.display_clock ? `${m.display_clock}'` : 'In Play';
                  const kickoffTime = new Date(m.utc_date).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });

                  return (
                    <div
                      key={m.id}
                      className="relative overflow-hidden rounded-2xl bg-gradient-to-b from-[#1E2227] via-[#1A1F26] to-[#12151B] border border-red-500/30 p-4 transition-all hover:border-red-500/60 hover:shadow-[0_0_20px_rgba(239,68,68,0.2)] flex flex-col justify-between"
                    >
                      {/* Top Bar: League & Live Pill */}
                      <div className="flex items-center justify-between gap-2 mb-3">
                        <span className="text-xs uppercase font-bold text-[#7098F8] tracking-wider truncate">
                          {m.competition_name || m.competition_id}
                        </span>

                        <div className="flex items-center gap-2">
                          <span className="text-xs text-[#94A3B8] font-bold">
                            KO {kickoffTime}
                          </span>
                          <div className="flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-red-500/15 border border-red-500/40 shadow-[0_0_8px_rgba(239,68,68,0.25)]">
                            <span className="w-1.5 h-1.5 rounded-full bg-red-500 animate-pulse" />
                            <span className="text-xs font-black uppercase text-red-400 tracking-wider">
                              {statusText}
                            </span>
                          </div>
                        </div>
                      </div>

                      {/* Match Teams and Big Score */}
                      <div className="flex items-center justify-between gap-3 py-2">
                        {/* Home Team */}
                        <div className="flex flex-col items-center text-center flex-1 min-w-0">
                          <div className="mb-1.5 flex items-center justify-center">
                            <TeamLiveCrest crestUrl={m.home_team_crest} teamName={homeName} />
                          </div>
                          <span className="text-xs font-bold text-white line-clamp-2 leading-tight">
                            {homeName}
                          </span>
                        </div>

                        {/* Live Score Counter */}
                        <div className="flex flex-col items-center justify-center px-4 py-2 rounded-xl bg-[#101418] border border-red-500/40 shadow-[0_0_12px_rgba(239,68,68,0.2)] shrink-0">
                          <span className="text-xl sm:text-2xl font-black text-white tracking-wider font-mono">
                            {m.home_score ?? 0} - {m.away_score ?? 0}
                          </span>
                          <span className="text-[0.65rem] sm:text-xs uppercase font-bold text-red-400 mt-0.5 tracking-widest flex items-center gap-1">
                            <Radio className="w-2.5 h-2.5 animate-pulse" />
                            LIVE
                          </span>
                          <span className="text-xs text-red-400 mt-1 font-semibold">{statusText}</span>
                        </div>

                        {/* Away Team */}
                        <div className="flex flex-col items-center text-center flex-1 min-w-0">
                          <div className="mb-1.5 flex items-center justify-center">
                            <TeamLiveCrest crestUrl={m.away_team_crest} teamName={awayName} />
                          </div>
                          <span className="text-xs font-bold text-white line-clamp-2 leading-tight">
                            {awayName}
                          </span>
                        </div>
                      </div>

                      {/* Action Button */}
                      <div className="mt-3 pt-2.5 border-t border-[#334155]/60 flex items-center justify-between">
                        <span className="text-xs text-[#94A3B8] font-medium">
                          {isPaused ? 'Half-time interval' : m.display_clock ? `${m.display_clock}' in progress` : 'Match in progress'}
                        </span>
                        <button
                          onClick={() => playVideo(m)}
                          className="flex items-center gap-1 px-3 py-1.5 rounded-xl bg-gradient-to-r from-red-600 to-red-500 hover:from-red-500 hover:to-red-400 text-white text-xs font-bold shadow-[0_0_12px_rgba(239,68,68,0.35)] transition-all cursor-pointer"
                        >
                          <span>Match Centre</span>
                          <ArrowRight className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>
    </section>
  );
}
