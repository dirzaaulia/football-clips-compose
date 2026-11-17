'use client';

import React from 'react';
import Link from 'next/link';
import { Swords, Zap, Play, ArrowRight } from 'lucide-react';
import { ClubTacticalProfile } from '@/lib/tools-data';
import { MatchRecord } from '@/lib/database';
import { EspnH2HMatch } from '@/lib/espn';

interface H2HHistoricalClashesProps {
  teamA: ClubTacticalProfile;
  teamB: ClubTacticalProfile;
  dbClashes: MatchRecord[];
  espnClashes: EspnH2HMatch[];
  seriesSummary?: string;
}

export default function H2HHistoricalClashes({
  teamA,
  teamB,
  dbClashes,
  espnClashes,
  seriesSummary,
}: H2HHistoricalClashesProps) {
  const powerA = Math.max(0.5, teamA.goalsPerMatch * 1.1 + teamA.xGPerMatch + teamA.cleanSheetPct / 100);
  const powerB = Math.max(0.5, teamB.goalsPerMatch * 1.1 + teamB.xGPerMatch + teamB.cleanSheetPct / 100);
  const rawTotal = powerA + powerB + 1.2;
  const winPctA = Math.round((powerA / rawTotal) * 100);
  const winPctB = Math.round((powerB / rawTotal) * 100);
  const drawPct = Math.max(10, 100 - winPctA - winPctB);

  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
      {/* Left 7 Cols: Last 5 Historical Head-to-Head Clashes */}
      <div className="lg:col-span-7 bg-[#1B1B1F] rounded-2xl border border-white/10 p-5 space-y-4">
        <div className="flex items-center justify-between border-b border-white/10 pb-3">
          <div className="flex items-center gap-2">
            <Swords className="w-4 h-4 text-[#DFFF00]" />
            <h4 className="font-scoreboard text-xl text-white uppercase tracking-wider">
              Historical Head-to-Head Clashes
            </h4>
          </div>
          <span className="px-2.5 py-0.5 rounded bg-[#DFFF00]/15 text-[#DFFF00] border border-[#DFFF00]/30 font-tech text-[11px] font-bold">
            {seriesSummary || `${teamA.shortName} vs ${teamB.shortName}`}
          </span>
        </div>

        {dbClashes.length > 0 ? (
          <div className="space-y-2.5">
            {dbClashes.slice(0, 5).map((m) => {
              const hName = m.home_team_name || m.home_team;
              const aName = m.away_team_name || m.away_team;
              const dStr = new Date(m.utc_date).toLocaleDateString('en-GB', {
                day: 'numeric',
                month: 'short',
                year: 'numeric',
              });
              return (
                <Link
                  key={m.id}
                  href={`/match/${m.id}`}
                  className="grid grid-cols-[76px_1fr_76px] sm:grid-cols-[90px_1fr_90px] items-center gap-2 p-3 rounded-xl bg-[#0D0E11] hover:bg-[#23252C] border border-white/10 transition-colors font-tech text-xs"
                >
                  <span className="text-white/45 truncate text-[11px]">{dStr}</span>
                  <div className="grid grid-cols-[1fr_64px_1fr] items-center gap-2.5 min-w-0">
                    <span className="font-bold text-white text-right truncate">{hName}</span>
                    <span className="w-16 py-0.5 rounded bg-[#1B1B1F] border border-white/10 font-scoreboard text-base text-[#DFFF00] text-center shrink-0">
                      {m.home_score ?? 0} - {m.away_score ?? 0}
                    </span>
                    <span className="font-bold text-white text-left truncate">{aName}</span>
                  </div>
                  <div className="flex items-center justify-end gap-2 shrink-0">
                    {m.highlight_video_id ? (
                      <span className="px-2 py-0.5 rounded bg-[#DFFF00] text-[#0A0A0C] text-[10px] font-extrabold uppercase inline-flex items-center gap-1">
                        <Play className="w-2.5 h-2.5 fill-[#0A0A0C]" />
                        <span>VOD</span>
                      </span>
                    ) : (
                      <ArrowRight className="w-3.5 h-3.5 text-white/40" />
                    )}
                  </div>
                </Link>
              );
            })}
          </div>
        ) : espnClashes.length > 0 ? (
          <div className="space-y-2.5">
            {espnClashes.slice(0, 5).map((ev) => {
              const dStr = ev.date
                ? new Date(ev.date).toLocaleDateString('en-GB', {
                    day: 'numeric',
                    month: 'short',
                    year: 'numeric',
                  })
                : 'Archive';
              return (
                <div
                  key={ev.id}
                  className="grid grid-cols-[76px_1fr_76px] sm:grid-cols-[90px_1fr_90px] items-center gap-2 p-3 rounded-xl bg-[#0D0E11] border border-white/10 font-tech text-xs"
                >
                  <span className="text-white/45 truncate text-[11px]">{dStr}</span>
                  <div className="grid grid-cols-[1fr_64px_1fr] items-center gap-2.5 min-w-0">
                    <span className="font-bold text-white text-right truncate">{ev.homeTeamName}</span>
                    <span className="w-16 py-0.5 rounded bg-[#1B1B1F] border border-white/10 font-scoreboard text-base text-[#DFFF00] text-center shrink-0">
                      {ev.scoreText}
                    </span>
                    <span className="font-bold text-white text-left truncate">{ev.awayTeamName}</span>
                  </div>
                  <span className="text-[11px] text-white/50 text-right truncate">{ev.competitionName}</span>
                </div>
              );
            })}
          </div>
        ) : (
          <div className="p-6 rounded-xl bg-[#0D0E11] border border-white/5 text-center space-y-1">
            <p className="font-tech text-xs text-white/70">
              No direct recent domestic encounters recorded between {teamA.name} and {teamB.name}.
            </p>
            <p className="font-tech text-[11px] text-white/45">
              Cross-league comparison calibrated using season points-per-game, goals scored, and clean sheet rates.
            </p>
          </div>
        )}
      </div>

      {/* Right 5 Cols: Win Probability Bar & Playing Style Breakdown */}
      <div className="lg:col-span-5 bg-[#1B1B1F] rounded-2xl border border-white/10 p-5 flex flex-col justify-between space-y-5">
        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <span className="font-tech text-xs font-bold uppercase tracking-widest text-[#DFFF00]">
              Win Probability Estimate
            </span>
            <Zap className="w-4 h-4 text-[#DFFF00]" />
          </div>

          <div className="flex items-center justify-between text-xs font-tech font-bold">
            <span className="text-[#DFFF00]">
              {teamA.shortName} {winPctA}%
            </span>
            <span className="text-white/60">Draw {drawPct}%</span>
            <span className="text-sky-400">
              {teamB.shortName} {winPctB}%
            </span>
          </div>

          <div className="w-full h-3 rounded-full bg-[#0D0E11] overflow-hidden flex gap-0.5 p-0.5 border border-white/10">
            <div
              className="h-full bg-[#DFFF00] rounded-l-full"
              style={{ width: `${winPctA}%` }}
            />
            <div className="h-full bg-zinc-500" style={{ width: `${drawPct}%` }} />
            <div
              className="h-full bg-sky-400 rounded-r-full"
              style={{ width: `${winPctB}%` }}
            />
          </div>
        </div>

        <div className="p-4 rounded-xl bg-[#0D0E11] border border-white/5 space-y-2">
          <h5 className="font-scoreboard text-base text-white uppercase tracking-wider">
            Playing Style Comparison
          </h5>
          <p className="text-xs text-white/65 font-tech leading-relaxed">
            <strong className="text-[#DFFF00]">{teamA.name}</strong> ({teamA.league}) plays a{' '}
            <strong className="text-white">{teamA.archetype}</strong> style with{' '}
            {teamA.pressIntensity.toLowerCase()} pressing intensity, while{' '}
            <strong className="text-sky-400">{teamB.name}</strong> ({teamB.league}) favors a{' '}
            <strong className="text-white">{teamB.archetype}</strong> approach. Final third efficiency and midfield control will shape the matchup.
          </p>
        </div>
      </div>
    </div>
  );
}
