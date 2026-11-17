'use client';

import React, { useState, useEffect } from 'react';
import Image from 'next/image';
import { Trophy, Zap, Target, Award } from 'lucide-react';
import {
  LeagueStatsPayload,
  LeaguePlayerLeader,
  fetchPlayerCutoutPhoto,
} from '@/lib/free-stats';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';

interface StandingsPlayerStatsTabProps {
  stats: LeagueStatsPayload | null;
  isLoading: boolean;
}

type LeaderBoardCategory = 'goals' | 'assists' | 'contributions';

export default function StandingsPlayerStatsTab({
  stats,
  isLoading,
}: StandingsPlayerStatsTabProps) {
  const [category, setCategory] = useState<LeaderBoardCategory>('goals');
  const [cutouts, setCutouts] = useState<Record<string, string>>({});

  const list: LeaguePlayerLeader[] = React.useMemo(
    () =>
      category === 'goals'
        ? stats?.scorers || []
        : category === 'assists'
        ? stats?.assists || []
        : stats?.contributions || [],
    [category, stats],
  );

  useEffect(() => {
    let isCancelled = false;
    const topThree = list.slice(0, 3);
    async function loadPhotos() {
      const updates: Record<string, string> = {};
      await Promise.all(
        topThree.map(async (p) => {
          const url = await fetchPlayerCutoutPhoto(p.name);
          if (url) updates[p.name] = url;
        }),
      );
      if (!isCancelled && Object.keys(updates).length > 0) {
        setCutouts((prev) => ({ ...prev, ...updates }));
      }
    }
    if (topThree.length > 0) loadPhotos();
    return () => {
      isCancelled = true;
    };
  }, [list]);

  if (isLoading) {
    return (
      <div className="space-y-4 animate-pulse">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-44 rounded-xl bg-[#1B1B1F] border border-white/10" />
          ))}
        </div>
        <div className="h-96 rounded-xl bg-[#1B1B1F] border border-white/10" />
      </div>
    );
  }

  if (list.length === 0) {
    return (
      <div className="text-center py-16 bg-[#1B1B1F] rounded-xl border border-white/10">
        <p className="font-tech text-sm text-white/50">
          No player statistics available for this competition yet.
        </p>
      </div>
    );
  }

  const maxPrimary = Math.max(
    1,
    category === 'goals'
      ? list[0]?.goals || 1
      : category === 'assists'
      ? list[0]?.assists || 1
      : list[0]?.contributions || 1,
  );

  return (
    <div className="space-y-6">
      {/* Category Sub-Switcher */}
      <div className="flex flex-wrap items-center justify-between gap-4 bg-[#1B1B1F] p-3.5 rounded-xl border border-white/10">
        <div className="flex items-center gap-2">
          {[
            { id: 'goals' as LeaderBoardCategory, label: 'Golden Boot (Top Scorers)', icon: Trophy },
            { id: 'assists' as LeaderBoardCategory, label: 'Playmaker Hub (Top Assists)', icon: Zap },
            { id: 'contributions' as LeaderBoardCategory, label: 'Goal Contributions (G + A)', icon: Target },
          ].map((tab) => {
            const Icon = tab.icon;
            const active = category === tab.id;
            return (
              <button
                key={tab.id}
                type="button"
                onClick={() => setCategory(tab.id)}
                className={`px-3.5 py-2 rounded-lg font-tech text-xs font-bold uppercase tracking-wider flex items-center gap-1.5 transition-all cursor-pointer ${
                  active
                    ? 'bg-[#DFFF00] text-[#0A0A0C] shadow-[0_0_12px_rgba(223,255,0,0.25)]'
                    : 'bg-[#0D0E11] text-white/65 hover:text-white border border-white/10'
                }`}
              >
                <Icon className="w-3.5 h-3.5" />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </div>
        <span className="text-xs font-tech text-white/50">
          {stats?.seasonDisplay} • Live Official Player Telemetry
        </span>
      </div>

      {/* Top 3 Podium Spotlight Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {list.slice(0, 3).map((leader, idx) => {
          const primaryValue =
            category === 'goals'
              ? leader.goals
              : category === 'assists'
              ? leader.assists
              : leader.contributions;
          const primaryLabel =
            category === 'goals' ? 'GOALS' : category === 'assists' ? 'ASSISTS' : 'G + A';
          const photo = cutouts[leader.name];

          return (
            <div
              key={leader.playerId}
              className={`relative rounded-xl p-5 bg-[#1B1B1F] border overflow-hidden flex items-center justify-between gap-4 ${
                idx === 0
                  ? 'border-[#DFFF00]/50 shadow-[0_0_20px_rgba(223,255,0,0.08)]'
                  : 'border-white/10'
              }`}
            >
              <div className="space-y-2 z-10 min-w-0">
                <div className="flex items-center gap-2">
                  <span
                    className={`px-2 py-0.5 rounded text-[10px] font-tech font-extrabold uppercase ${
                      idx === 0
                        ? 'bg-[#DFFF00] text-[#0A0A0C]'
                        : 'bg-white/10 text-white/80'
                    }`}
                  >
                    #{leader.rank} {idx === 0 ? 'LEADER' : 'CONTENDER'}
                  </span>
                  {leader.jersey && (
                    <span className="text-xs font-mono text-white/45">#{leader.jersey}</span>
                  )}
                </div>

                <div>
                  <h3 className="font-scoreboard text-2xl text-white tracking-wide truncate">
                    {leader.name}
                  </h3>
                  <div className="flex items-center gap-1.5 mt-0.5">
                    {leader.teamLogo && (
                      <GlassLogoBadge src={leader.teamLogo} alt={leader.teamName} size={18} />
                    )}
                    <span className="text-xs font-tech text-white/60 truncate">
                      {leader.teamName}
                    </span>
                  </div>
                </div>

                <div className="flex items-center gap-4 pt-1 text-xs font-tech text-white/60">
                  <span>{leader.appearances} Matches</span>
                  <span>•</span>
                  <span className="text-[#DFFF00] font-bold">{leader.perMatch} / 90</span>
                </div>
              </div>

              <div className="flex items-center gap-3 shrink-0 z-10">
                {photo ? (
                  <div className="relative w-20 h-24 rounded-lg overflow-hidden bg-[#0D0E11] border border-white/10">
                    <Image
                      src={photo}
                      alt={leader.name}
                      fill
                      className="object-contain object-bottom"
                      unoptimized
                    />
                  </div>
                ) : (
                  <div className="w-14 h-14 rounded-full bg-[#0D0E11] border border-white/10 flex items-center justify-center">
                    <Award className="w-6 h-6 text-[#DFFF00]" />
                  </div>
                )}
                <div className="text-right">
                  <span className="font-scoreboard text-4xl sm:text-5xl text-[#DFFF00] leading-none block">
                    {primaryValue}
                  </span>
                  <span className="text-[10px] font-tech font-bold text-white/50 uppercase tracking-widest">
                    {primaryLabel}
                  </span>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Full Leaderboard Table */}
      <div className="bg-[#1B1B1F] rounded-xl border border-white/10 overflow-hidden shadow-xl">
        <div className="overflow-x-auto scrollbar-none">
          <table className="w-full text-left border-collapse min-w-[680px]">
            <thead>
              <tr className="bg-[#292A2E]/70 border-b border-white/10 text-[11px] font-tech uppercase tracking-wider text-white/55">
                <th className="py-3 pl-5 pr-2 w-14">Rank</th>
                <th className="py-3 px-3">Player</th>
                <th className="py-3 px-3">Club</th>
                <th className="py-3 px-3 text-center">Matches</th>
                <th className="py-3 px-3 text-center">Goals</th>
                <th className="py-3 px-3 text-center">Assists</th>
                <th className="py-3 px-3 text-center">G + A</th>
                <th className="py-3 pl-3 pr-5 text-right">Per Match</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5 font-tech text-xs sm:text-sm">
              {list.slice(0, 20).map((row) => {
                const val =
                  category === 'goals'
                    ? row.goals
                    : category === 'assists'
                    ? row.assists
                    : row.contributions;
                const barWidth = Math.round((val / maxPrimary) * 100);
                return (
                  <tr key={row.playerId} className="hover:bg-[#24252A] transition-colors">
                    <td className="py-3 pl-5 pr-2 font-bold text-white/70">#{row.rank}</td>
                    <td className="py-3 px-3">
                      <div className="flex items-center gap-2">
                        <span className="font-bold text-white">{row.name}</span>
                        {row.jersey && (
                          <span className="text-[10px] px-1.5 py-0.5 rounded bg-white/5 text-white/50 font-mono">
                            #{row.jersey}
                          </span>
                        )}
                      </div>
                      <div className="w-32 h-1 rounded-full bg-[#0D0E11] overflow-hidden mt-1">
                        <div
                          className="h-full bg-[#DFFF00]"
                          style={{ width: `${barWidth}%` }}
                        />
                      </div>
                    </td>
                    <td className="py-3 px-3">
                      <div className="flex items-center gap-2">
                        {row.teamLogo && (
                          <GlassLogoBadge src={row.teamLogo} alt={row.teamName} size={22} />
                        )}
                        <span className="text-white/80">{row.teamName}</span>
                      </div>
                    </td>
                    <td className="py-3 px-3 text-center text-white/60">{row.appearances}</td>
                    <td className="py-3 px-3 text-center font-scoreboard text-xl text-white">
                      {row.goals}
                    </td>
                    <td className="py-3 px-3 text-center font-scoreboard text-xl text-sky-400">
                      {row.assists}
                    </td>
                    <td className="py-3 px-3 text-center font-scoreboard text-xl text-[#DFFF00]">
                      {row.contributions}
                    </td>
                    <td className="py-3 pl-3 pr-5 text-right font-mono text-xs text-white/70">
                      {row.perMatch.toFixed(2)}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
