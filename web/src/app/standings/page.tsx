'use client';

import React, { useState, useEffect } from 'react';
import Image from 'next/image';
import { getStandingsByCompetition, StandingRecord } from '@/lib/supabase';
import { Trophy } from 'lucide-react';
import AdSenseBanner from '@/components/ads/AdSenseBanner';

const LEAGUES = [
  { code: 'PL', name: 'Premier League' },
  { code: 'CL', name: 'Champions League' },
  { code: 'PD', name: 'La Liga' },
  { code: 'SA', name: 'Serie A' },
  { code: 'BL1', name: 'Bundesliga' },
  { code: 'FL1', name: 'Ligue 1' },
  { code: 'SPL', name: 'Saudi Pro League' },
  { code: 'MLS', name: 'MLS' },
  { code: 'DED', name: 'Eredivisie' },
  { code: 'PPL', name: 'Liga Portugal' },
];

import SeasonSelector from '@/components/filter/SeasonSelector';
import CustomDropdown from '@/components/common/CustomDropdown';
import { useSeason } from '@/hooks/useSeason';

function normalizeTeamNameForDeduplication(name: string): string {
  return name
    .toLowerCase()
    .replace(/\b(fc|afc|cf|sc|bc|ssc|as|ac|rc|ogc|vfb|tsv|sv|cd|ud|ca|rb|us)\b/gi, '')
    .replace(/balompie/gi, '')
    .replace(/calcio/gi, '')
    .replace(/de barcelona/gi, '')
    .replace(/münchen/gi, 'munich')
    .replace(/manchester/gi, 'man')
    .replace(/tottenham hotspur/gi, 'spurs')
    .replace(/tottenham/gi, 'spurs')
    .replace(/wolverhampton wanderers/gi, 'wolves')
    .replace(/wolverhampton/gi, 'wolves')
    .replace(/saint-germain/gi, 'sg')
    .replace(/saint germain/gi, 'sg')
    .replace(/atletico de madrid/gi, 'atletico')
    .replace(/atlético de madrid/gi, 'atletico')
    .replace(/atletico madrid/gi, 'atletico')
    .replace(/atlético madrid/gi, 'atletico')
    .replace(/athletic club/gi, 'bilbao')
    .replace(/athletic bilbao/gi, 'bilbao')
    .replace(/internazionale/gi, 'inter')
    .replace(/inter milan/gi, 'inter')
    .replace(/[^a-z0-9]/gi, '')
    .trim();
}

function deduplicateStandings(standings: StandingRecord[]): StandingRecord[] {
  if (standings.length <= 1) return standings;

  const grouped = new Map<string, StandingRecord[]>();
  for (const item of standings) {
    const key = normalizeTeamNameForDeduplication(item.team_name || String(item.team_id || item.id));
    if (!grouped.has(key)) {
      grouped.set(key, []);
    }
    grouped.get(key)!.push(item);
  }

  const result: StandingRecord[] = [];
  for (const duplicates of grouped.values()) {
    if (duplicates.length === 1) {
      result.push(duplicates[0]);
    } else {
      const best = duplicates.reduce((prev, curr) => {
        let prevScore = 0;
        let currScore = 0;
        if (prev.played_games > 0) prevScore += 50;
        if (curr.played_games > 0) currScore += 50;
        if (prev.crest_url) prevScore += 20;
        if (curr.crest_url) currScore += 20;
        if (prev.points > 0) prevScore += 10;
        if (curr.points > 0) currScore += 10;
        return currScore > prevScore ? curr : prev;
      }, duplicates[0]);
      result.push(best);
    }
  }

  return result
    .sort((a, b) => (a.position || 0) - (b.position || 0))
    .map((team, idx) => ({ ...team, position: idx + 1 }));
}

export default function StandingsPage() {
  const [selectedComp, setSelectedComp] = useState('PL');
  const { selectedSeason, setSeason } = useSeason();
  const [standings, setStandings] = useState<StandingRecord[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const hasFormData = React.useMemo(
    () => standings.some((t) => t.form && t.form.trim().length > 0),
    [standings]
  );

  useEffect(() => {
    async function load() {
      setIsLoading(true);
      const data = await getStandingsByCompetition(selectedComp, selectedSeason);
      setStandings(deduplicateStandings(data));
      setIsLoading(false);
    }
    load();
  }, [selectedComp, selectedSeason]);

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 py-6">
      {/* Title & Compact Selectors */}
      <header className="mb-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight flex items-center gap-2.5">
            <Trophy className="w-7 h-7 text-[#7098F8]" />
            <span>League Tables & Standings</span>
          </h1>
          <p className="text-xs sm:text-sm text-[#94A3B8] mt-1">Official domestic leagues & UEFA Champions League table standings.</p>
        </div>

        {/* Compact, Intuitive Selectors */}
        <div className="flex flex-wrap items-center gap-2 self-start sm:self-auto">
          {/* Global Synchronized Season Selector */}
          <SeasonSelector variant="select" />

          {/* League Selector */}
          <CustomDropdown<string>
            items={LEAGUES.map((l) => ({
              value: l.code,
              label: l.name,
              icon: <Trophy className="w-4 h-4 text-[#7098F8]" />,
            }))}
            value={selectedComp}
            onChange={setSelectedComp}
            leadingIcon={<Trophy className="w-4 h-4 text-[#7098F8]" />}
            prefix="League"
            className="w-auto"
            ariaLabel="Select competition standings"
          />
        </div>
      </header>

      <AdSenseBanner slotId="5616816386" format="horizontal" />

      {/* Standings Table */}
      <div className="bg-[#181C20] border border-[#334155] rounded-3xl overflow-hidden my-6 shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs sm:text-sm text-[#F1F5F9]">
            <thead className="bg-[#101418] text-[11px] uppercase tracking-wider text-[#94A3B8] border-b border-[#334155]">
              <tr>
                <th className="py-3.5 px-3 sm:px-4 font-bold text-center w-12">Pos</th>
                <th className="py-3.5 px-4 font-bold">Club</th>
                <th className="py-3.5 px-3 font-semibold text-center">P</th>
                <th className="py-3.5 px-3 font-semibold text-center">W</th>
                <th className="py-3.5 px-3 font-semibold text-center">D</th>
                <th className="py-3.5 px-3 font-semibold text-center">L</th>
                <th className="py-3.5 px-3 font-semibold text-center hidden md:table-cell">GD</th>
                <th className="py-3.5 px-4 font-bold text-center text-white">Pts</th>
                {hasFormData && (
                  <th className="py-3.5 px-4 font-semibold text-center hidden sm:table-cell">Form</th>
                )}
              </tr>
            </thead>
            <tbody className="divide-y divide-[#334155]/50 font-medium">
              {isLoading ? (
                [...Array(10)].map((_, i) => (
                  <tr key={i} className="animate-pulse">
                    <td colSpan={hasFormData ? 9 : 8} className="py-4 px-4 bg-[#1E2227]/30"></td>
                  </tr>
                ))
              ) : standings.length === 0 ? (
                <tr>
                  <td colSpan={hasFormData ? 9 : 8} className="py-12 text-center text-[#94A3B8]">
                    No standings data available for this competition currently.
                  </td>
                </tr>
              ) : (
                standings.map((team) => (
                  <tr key={team.id} className="hover:bg-[#1E2227]/80 transition-colors">
                    <td className="py-3 px-3 sm:px-4 text-center font-bold text-[#94A3B8]">
                      {team.position}
                    </td>
                    <td className="py-3 px-4 font-bold text-white flex items-center gap-3">
                      {team.crest_url && (
                        <div className="relative w-5 h-5 shrink-0">
                          <Image
                            src={team.crest_url}
                            alt={team.team_name}
                            fill
                            className="object-contain"
                            unoptimized
                          />
                        </div>
                      )}
                      <span className="font-bold text-white whitespace-normal">{team.team_name}</span>
                    </td>
                    <td className="py-3 px-3 text-center text-[#94A3B8]">{team.played_games}</td>
                    <td className="py-3 px-3 text-center">{team.won}</td>
                    <td className="py-3 px-3 text-center text-[#94A3B8]">{team.draw}</td>
                    <td className="py-3 px-3 text-center text-[#94A3B8]">{team.lost}</td>
                    <td className="py-3 px-3 text-center hidden md:table-cell text-[#94A3B8] font-mono">
                      {team.goal_difference > 0 ? `+${team.goal_difference}` : team.goal_difference}
                    </td>
                    <td className="py-3 px-4 text-center font-black text-white text-base">
                      {team.points}
                    </td>
                    {hasFormData && (
                      <td className="py-3 px-4 text-center hidden sm:table-cell">
                        {team.form ? (
                          <div className="flex items-center justify-center gap-1">
                            {team.form
                              .replace(/,/g, ' ')
                              .split(/\s+/)
                              .filter(Boolean)
                              .slice(-5)
                              .map((f, i) => {
                                const clean = f.trim().toUpperCase();
                                return (
                                  <span
                                    key={i}
                                    className={`w-4 h-4 rounded-full flex items-center justify-center text-[9px] font-black text-white ${
                                      clean === 'W'
                                        ? 'bg-emerald-500'
                                        : clean === 'D'
                                        ? 'bg-gray-400'
                                        : 'bg-rose-500'
                                    }`}
                                  >
                                    {clean}
                                  </span>
                                );
                              })}
                          </div>
                        ) : (
                          <span className="text-[#334155]">-</span>
                        )}
                      </td>
                    )}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
