'use client';

import React from 'react';
import { Swords } from 'lucide-react';
import { MatchRecord } from '@/lib/database';

interface AnalysisMatchBarProps {
  matches: MatchRecord[];
  selectedMatchId: string;
  onSelectMatch: (matchId: string) => void;
}

function getTeamAbbr(name: string): string {
  if (!name) return 'FC';
  const clean = name.replace(/^(FC|CF|AFC|Real|Sporting)\s+/i, '').trim();
  const words = clean.split(/\s+/);
  if (words.length >= 2) {
    return (words[0].slice(0, 1) + words[1].slice(0, 2)).toUpperCase();
  }
  return clean.slice(0, 3).toUpperCase();
}

export default function AnalysisMatchBar({
  matches,
  selectedMatchId,
  onSelectMatch,
}: AnalysisMatchBarProps) {
  if (!matches || matches.length === 0) return null;

  const current = matches.find((m) => m.id === selectedMatchId) || matches[0];
  const homeName = current.home_team_name || current.home_team;
  const awayName = current.away_team_name || current.away_team;
  const isLive = current.status === 'IN_PLAY' || current.status === 'PAUSED' || current.status === 'LIVE';

  return (
    <div className="w-full bg-[#1A1C22] rounded-2xl border border-white/10 p-4 sm:p-5 shadow-xl flex flex-col md:flex-row md:items-center justify-between gap-4">
      {/* Current Match Headline */}
      <div className="flex flex-col gap-1 min-w-0">
        <div className="flex items-center gap-2 text-[10px] font-tech text-[#DFFF00] uppercase tracking-widest font-bold">
          <Swords className="w-3.5 h-3.5" />
          <span>MATCH TACTICAL LAB • {current.competition_name || 'UEFA MATRIX'}</span>
          {isLive && (
            <span className="flex items-center gap-1 text-red-400 font-bold bg-red-500/10 px-2 py-0.5 rounded border border-red-500/30">
              <span className="w-1.5 h-1.5 rounded-full bg-red-500 animate-ping" />
              LIVE
            </span>
          )}
        </div>
        <h2 className="font-scoreboard text-2xl sm:text-3xl text-white uppercase tracking-wide leading-none truncate">
          {homeName} vs {awayName}
        </h2>
        <span className="text-xs text-white/50 font-tech truncate">
          {current.stage || current.round_name || 'Official Matchday'} • Score: {current.home_score ?? 0} - {current.away_score ?? 0}
        </span>
      </div>

      {/* Quick Switch Pills */}
      <div className="flex flex-wrap items-center gap-2 shrink-0">
        <span className="text-[10px] font-tech text-white/40 uppercase font-bold mr-1 hidden sm:inline">
          Match Switch:
        </span>
        {matches.map((m) => {
          const isSelected = m.id === current.id;
          const hAbbr = getTeamAbbr(m.home_team_name || m.home_team);
          const aAbbr = getTeamAbbr(m.away_team_name || m.away_team);
          const scoreLabel = `${hAbbr} ${m.home_score ?? 0}-${m.away_score ?? 0} ${aAbbr}`;
          const mLive = m.status === 'IN_PLAY' || m.status === 'LIVE';

          return (
            <button
              key={m.id}
              type="button"
              onClick={() => onSelectMatch(m.id)}
              className={`px-3 py-1.5 rounded-lg font-tech text-xs font-bold uppercase tracking-wider transition-all cursor-pointer flex items-center gap-1.5 ${
                isSelected
                  ? 'bg-[#DFFF00] text-[#0A0A0C] shadow-[0_0_12px_rgba(223,255,0,0.25)]'
                  : 'bg-[#121316] text-white/70 hover:text-white border border-white/10'
              }`}
            >
              {mLive && <span className="w-1.5 h-1.5 rounded-full bg-red-500 animate-ping" />}
              <span>{scoreLabel}</span>
            </button>
          );
        })}
      </div>
    </div>
  );
}
