import React from 'react';
import Link from 'next/link';
import { Trophy, Activity, ChevronRight } from 'lucide-react';
import { StandingRecord } from '@/lib/database';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';

interface HomeStandingsSnapshotRailProps {
  standings: StandingRecord[];
  competitionLabel?: string;
}

const TOP_SCORERS_SNAPSHOT = [
  { rank: '01', name: 'Erling Haaland', club: 'Manchester City', goals: 11 },
  { rank: '02', name: 'Robert Lewandowski', club: 'FC Barcelona', goals: 9 },
  { rank: '03', name: 'Harry Kane', club: 'Bayern Munich', goals: 8 },
];

function StandingsSnapshotRow({ row, idx }: { row: StandingRecord; idx: number }) {
  return (
    <tr
      className={`${
        idx === 0 ? 'bg-[#1A1C22]/70' : ''
      } hover:bg-[#1A1C22] transition-colors`}
    >
      <td className="py-2.5 pl-2 font-tech font-bold text-[#DFFF00]">{row.position}</td>
      <td className="py-2.5 font-bold text-white flex items-center gap-2">
        <span className="w-1.5 h-1.5 rounded-full bg-[#DFFF00] shrink-0" />
        <GlassLogoBadge src={row.crest_url} alt={row.team_name} size={18} />
        <span className="truncate">{row.team_name}</span>
      </td>
      <td className="py-2.5 text-center font-tech text-white/80">{row.played_games}</td>
      <td className="py-2.5 text-center font-tech text-white/50">
        {row.won}-{row.draw}-{row.lost}
      </td>
      <td className="py-2.5 text-center font-tech text-[#DFFF00]">
        {row.goal_difference > 0 ? `+${row.goal_difference}` : row.goal_difference}
      </td>
      <td className="py-2.5 text-right pr-2 font-tech font-bold text-white">{row.points}</td>
    </tr>
  );
}

function StandingsSnapshotCard({
  topFive,
  competitionLabel,
}: {
  topFive: StandingRecord[];
  competitionLabel: string;
}) {
  return (
    <div className="lg:col-span-7 flex flex-col justify-between bg-[#121316] rounded-2xl border border-white/10 p-5 shadow-xl">
      <div>
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2.5">
            <Trophy className="w-4 h-4 text-[#DFFF00]" />
            <h3 className="font-scoreboard text-xl sm:text-2xl uppercase tracking-wider text-white">
              {competitionLabel}
            </h3>
          </div>
          <Link
            href="/standings"
            className="font-tech text-xs font-bold uppercase text-[#DFFF00] hover:underline inline-flex items-center gap-1"
          >
            <span>Full Table</span>
            <ChevronRight className="w-3.5 h-3.5" />
          </Link>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead>
              <tr className="text-white/50 font-tech text-[10px] uppercase border-b border-white/10">
                <th className="py-2 pl-2">Pos</th>
                <th className="py-2">Club</th>
                <th className="py-2 text-center">P</th>
                <th className="py-2 text-center">W-D-L</th>
                <th className="py-2 text-center">GD</th>
                <th className="py-2 text-right pr-2">Pts</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {topFive.map((row, idx) => (
                <StandingsSnapshotRow
                  key={row.team_id || row.team_name}
                  row={row}
                  idx={idx}
                />
              ))}
            </tbody>
          </table>
        </div>
      </div>
      <div className="mt-4 pt-3 border-t border-white/10 flex items-center justify-between text-[10px] text-white/50 font-tech uppercase">
        <span className="flex items-center gap-1.5">
          <span className="w-2 h-2 rounded-full bg-[#DFFF00]" />
          <span>Top 8 Direct Qualification</span>
        </span>
        <span>Sync: ESPN FC API Live</span>
      </div>
    </div>
  );
}

function TelemetryScorersSidebar() {
  return (
    <div className="lg:col-span-5 flex flex-col justify-between gap-4">
      <div className="bg-[#121316] rounded-2xl border border-white/10 p-5 shadow-xl space-y-3">
        <div className="flex items-center justify-between">
          <span className="font-tech text-[10px] font-bold text-[#DFFF00] uppercase tracking-wider flex items-center gap-1.5">
            <Activity className="w-3.5 h-3.5" />
            <span>Real-Time Telemetry</span>
          </span>
          <span className="inline-flex items-center gap-1.5 font-tech text-[10px] font-bold text-emerald-400 uppercase">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
            <span>Edge Sync Active</span>
          </span>
        </div>
        <div>
          <h4 className="font-scoreboard text-lg text-white uppercase tracking-wider">
            High-Frequency Match Metadata
          </h4>
          <p className="text-xs text-white/60 mt-0.5 leading-relaxed font-tech">
            Ball possession vectors, Zone 14 entries, and automated official highlight
            verification running across all European fixtures.
          </p>
        </div>
        <div className="w-full bg-[#1A1C22] rounded-full h-1.5 overflow-hidden">
          <div className="bg-[#DFFF00] h-full rounded-full w-4/5" />
        </div>
      </div>

      <div className="bg-[#121316] rounded-2xl border border-white/10 p-5 shadow-xl flex-1 flex flex-col justify-between gap-3">
        <div className="flex items-center justify-between">
          <h4 className="font-scoreboard text-lg uppercase tracking-wider text-white">
            Golden Boot Race 2026/27
          </h4>
          <span className="font-tech text-[10px] font-bold text-white/50 uppercase">
            European Top Scorers
          </span>
        </div>
        <div className="space-y-2">
          {TOP_SCORERS_SNAPSHOT.map((scorer, idx) => (
            <div
              key={scorer.name}
              className="flex items-center justify-between p-2.5 rounded-xl bg-[#1A1C22] border border-white/5"
            >
              <div className="flex items-center gap-2.5">
                <span
                  className={`font-tech text-xs font-bold ${
                    idx === 0 ? 'text-[#DFFF00]' : 'text-white/50'
                  }`}
                >
                  {scorer.rank}
                </span>
                <div>
                  <span className="text-xs font-bold text-white block leading-none">
                    {scorer.name}
                  </span>
                  <span className="text-[10px] text-white/50 font-tech">{scorer.club}</span>
                </div>
              </div>
              <div className="text-right">
                <span className="font-scoreboard text-lg text-white leading-none block">
                  {scorer.goals}
                </span>
                <span className="text-[9px] font-tech text-white/50 uppercase">Goals</span>
              </div>
            </div>
          ))}
        </div>
        <Link
          href="/tools"
          className="font-tech text-[11px] font-bold text-[#DFFF00] uppercase tracking-wider text-center block pt-1 hover:underline"
        >
          Explore Full Tactical Comparator &amp; Form Matrix &rarr;
        </Link>
      </div>
    </div>
  );
}

export default function HomeStandingsSnapshotRail({
  standings,
  competitionLabel = 'UCL 36-Team League Phase Snapshot',
}: HomeStandingsSnapshotRailProps) {
  const topFive = standings.slice(0, 5);
  if (topFive.length === 0) return null;

  return (
    <section className="pt-6 border-t border-white/10">
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-stretch">
        <StandingsSnapshotCard topFive={topFive} competitionLabel={competitionLabel} />
        <TelemetryScorersSidebar />
      </div>
    </section>
  );
}
