'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { Trophy, ArrowRight } from 'lucide-react';
import { getStandingsByCompetition, StandingRecord } from '@/lib/database';

interface MatchTitleRaceCardProps {
  competitionId?: string;
  competitionName?: string;
  homeName: string;
  awayName: string;
}

export default function MatchTitleRaceCard({
  competitionId,
  competitionName = 'Premier League',
  homeName,
  awayName,
}: MatchTitleRaceCardProps) {
  const [standings, setStandings] = useState<StandingRecord[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;
    const targetComp = competitionId || 'PL';
    setLoading(true);

    getStandingsByCompetition(targetComp).then((data) => {
      if (isMounted) {
        if (data && data.length > 0) {
          setStandings(data.slice(0, 4));
        } else {
          setStandings([]);
        }
        setLoading(false);
      }
    });

    return () => {
      isMounted = false;
    };
  }, [competitionId]);

  if (!loading && standings.length === 0) {
    return null;
  }

  return (
    <div className="w-full bg-[#121316] rounded-2xl p-4 sm:p-5 border border-white/10 shadow-lg flex flex-col gap-3 select-none">
      {/* Card Header */}
      <div className="flex items-center justify-between pb-2 border-b border-white/10">
        <div className="flex items-center gap-2">
          <Trophy className="w-4 h-4 text-[#DFFF00]" />
          <h3 className="font-scoreboard text-base sm:text-lg tracking-wider text-white uppercase">
            Title Race Shift
          </h3>
        </div>
        <span className="font-tech text-[10px] text-[#DFFF00] bg-[#DFFF00]/10 border border-[#DFFF00]/20 px-2 py-0.5 rounded font-bold uppercase">
          {competitionName.toUpperCase()}
        </span>
      </div>

      <p className="font-tech text-xs text-white/50 leading-relaxed">
        Live standings impact showing current summit positions and goal differentials.
      </p>

      {/* Micro Standings Table */}
      <div className="w-full flex flex-col gap-1 text-xs pt-1">
        <div className="grid grid-cols-12 text-white/40 font-tech text-[10px] uppercase font-bold px-2 py-1">
          <span className="col-span-1">#</span>
          <span className="col-span-6">CLUB</span>
          <span className="col-span-2 text-center">PL</span>
          <span className="col-span-1 text-center">GD</span>
          <span className="col-span-2 text-right">PTS</span>
        </div>

        {loading ? (
          <div className="space-y-1.5 py-1">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-7 w-full rounded-lg bg-white/5 animate-pulse" />
            ))}
          </div>
        ) : (
          standings.map((team, idx) => {
            const isHome = team.team_name.toLowerCase().includes(homeName.toLowerCase());
            const isAway = team.team_name.toLowerCase().includes(awayName.toLowerCase());
            const isFeatured = isHome || isAway || idx === 0;

            return (
              <div
                key={`standings-micro-${team.position || idx}`}
                className={`grid grid-cols-12 items-center px-2 py-1.5 rounded-lg font-tech text-xs transition-colors ${
                  isFeatured
                    ? 'bg-[#DFFF00]/10 text-white font-bold border border-[#DFFF00]/20'
                    : 'hover:bg-white/5 text-white/80'
                }`}
              >
                <span className={`col-span-1 font-bold ${isFeatured ? 'text-[#DFFF00]' : 'text-white/40'}`}>
                  {team.position || idx + 1}
                </span>
                <span className="col-span-6 flex items-center gap-1.5 truncate">
                  <span
                    className={`w-1.5 h-1.5 rounded-full shrink-0 ${
                      isFeatured ? 'bg-[#DFFF00]' : 'bg-white/20'
                    }`}
                  />
                  <span className="truncate">{team.team_name}</span>
                </span>
                <span className="col-span-2 text-center text-white/50">{team.played_games}</span>
                <span className={`col-span-1 text-center ${isFeatured ? 'text-[#DFFF00]' : 'text-white/50'}`}>
                  {team.goal_difference > 0 ? `+${team.goal_difference}` : team.goal_difference}
                </span>
                <span className={`col-span-2 text-right font-black ${isFeatured ? 'text-[#DFFF00]' : 'text-white'}`}>
                  {team.points}
                </span>
              </div>
            );
          })
        )}
      </div>

      {/* Footer Navigation */}
      <Link
        href="/standings"
        className="flex items-center justify-center gap-1.5 pt-2 text-xs font-tech font-bold text-[#DFFF00] hover:text-white transition-colors border-t border-white/5"
      >
        <span>View Full Standings Table</span>
        <ArrowRight className="w-3.5 h-3.5" />
      </Link>
    </div>
  );
}
