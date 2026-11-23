'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { Swords, Wrench, Play, Award, Activity, ArrowRight } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import { EspnSummaryResult } from '@/lib/espn';
import { fetchHistoricalH2HFromDatabase } from '@/lib/free-stats';
import GlassLogoBadge from '@/components/common/GlassLogoBadge';

interface MatchH2HAndLeadersViewProps {
  match: MatchRecord;
  espnSummary?: EspnSummaryResult | null;
}

export default function MatchH2HAndLeadersView({
  match,
  espnSummary,
}: MatchH2HAndLeadersViewProps) {
  const homeName = match.home_team_name || match.home_team;
  const awayName = match.away_team_name || match.away_team;
  const [dbH2H, setDbH2H] = useState<MatchRecord[]>([]);

  useEffect(() => {
    let isCancelled = false;
    fetchHistoricalH2HFromDatabase(homeName, awayName).then((rows) => {
      if (!isCancelled) {
        setDbH2H(rows.filter((r) => String(r.id) !== String(match.id)).slice(0, 5));
      }
    });
    return () => {
      isCancelled = true;
    };
  }, [homeName, awayName, match.id]);

  const espnH2H = espnSummary?.headToHead?.matches || [];
  const homeLastFive = espnSummary?.lastFive?.home || [];
  const awayLastFive = espnSummary?.lastFive?.away || [];
  const homeLeaders = espnSummary?.leaders?.home || [];
  const awayLeaders = espnSummary?.leaders?.away || [];

  const toolsUrl = `/tools?tab=h2h&matchId=${encodeURIComponent(String(match.id))}&home=${encodeURIComponent(homeName)}&away=${encodeURIComponent(awayName)}&comp=${encodeURIComponent(match.competition_id || 'PL')}`;

  return (
    <div className="p-4 sm:p-6 space-y-6 select-none">
      {/* Direct Deep-Link CTA Banner to Tactical Tools Lab */}
      <div className="rounded-xl bg-gradient-to-r from-[#1B1B1F] via-[#23252C] to-[#1B1B1F] border border-[#DFFF00]/30 p-4 sm:p-5 flex flex-col md:flex-row md:items-center justify-between gap-4 shadow-lg">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <span className="px-2.5 py-0.5 rounded bg-[#DFFF00] text-[#0A0A0C] font-tech text-xs font-black uppercase tracking-wider">
              Head-to-Head Hub
            </span>
            <span className="text-xs font-tech text-white/80">
              Multi-Season H2H • Season Form • Club Comparison
            </span>
          </div>
          <h4 className="font-scoreboard text-xl sm:text-2xl text-white uppercase tracking-wide">
            Compare {homeName} vs {awayName} Full Season Stats
          </h4>
        </div>
        <Link
          href={toolsUrl}
          className="px-4 py-2.5 rounded-xl bg-[#DFFF00] hover:bg-[#cbe800] text-[#0A0A0C] font-tech text-xs font-black uppercase tracking-wider flex items-center gap-2 shrink-0 self-start md:self-auto shadow-[0_0_15px_rgba(223,255,0,0.25)] transition-all"
        >
          <Wrench className="w-4 h-4" />
          <span>Open H2H Comparator</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </Link>
      </div>

      {/* Section 1: Match Key Player Leaders */}
      {(homeLeaders.length > 0 || awayLeaders.length > 0) && (
        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Award className="w-4 h-4 text-[#DFFF00]" />
              <h4 className="font-scoreboard text-lg text-white uppercase tracking-wider">
                Key Player Match Leaders
              </h4>
            </div>
            <span className="text-xs font-tech text-white/70">Official Match Boxscore</span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {[
              { title: `${homeName} Leaders`, crest: match.home_team_crest, list: homeLeaders, accent: 'text-[#DFFF00]' },
              { title: `${awayName} Leaders`, crest: match.away_team_crest, list: awayLeaders, accent: 'text-sky-400' },
            ].map((col) => (
              <div key={col.title} className="bg-[#16181D] rounded-xl border border-white/10 p-4 space-y-2.5">
                <div className="flex items-center gap-2 pb-2 border-b border-white/5">
                  <GlassLogoBadge src={col.crest} alt={col.title} size={24} />
                  <span className={`font-tech text-xs sm:text-sm font-extrabold uppercase ${col.accent}`}>
                    {col.title}
                  </span>
                </div>
                {col.list.slice(0, 3).map((ldr, idx) => (
                  <div
                    key={idx}
                    className="flex items-center justify-between py-1.5 border-b border-white/5 last:border-none font-tech text-xs sm:text-sm"
                  >
                    <div>
                      <span className="text-xs uppercase text-white/70 font-bold block">{ldr.displayName}</span>
                      <span className="font-extrabold text-white text-xs sm:text-sm">
                        {ldr.playerName} {ldr.jersey ? `#${ldr.jersey}` : ''}
                      </span>
                    </div>
                    <span className={`px-2.5 py-1 rounded bg-[#0D0E11] border border-white/10 font-black ${col.accent}`}>
                      {ldr.displayValue}
                    </span>
                  </div>
                ))}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Section 2: Previous Head-to-Head Meetings (Symmetric Centered Score Grid) */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Swords className="w-4 h-4 text-[#DFFF00]" />
            <h4 className="font-scoreboard text-lg text-white uppercase tracking-wider">
              Previous Head-to-Head Clashes
            </h4>
          </div>
          {espnSummary?.headToHead?.summary && (
            <span className="px-2.5 py-0.5 rounded bg-[#DFFF00]/15 text-[#DFFF00] border border-[#DFFF00]/30 font-tech text-xs font-bold">
              {espnSummary.headToHead.summary}
            </span>
          )}
        </div>

        {dbH2H.length > 0 ? (
          <div className="space-y-2">
            {dbH2H.map((past) => {
              const pHome = past.home_team_name || past.home_team;
              const pAway = past.away_team_name || past.away_team;
              const dateStr = new Date(past.utc_date).toLocaleDateString('en-GB', {
                day: 'numeric',
                month: 'short',
                year: 'numeric',
              });
              return (
                <Link
                  key={past.id}
                  href={`/match/${past.id}`}
                  className="grid grid-cols-[84px_1fr_80px] sm:grid-cols-[104px_1fr_110px] items-center gap-2 p-3 rounded-xl bg-[#16181D] hover:bg-[#1F2229] border border-white/10 transition-colors font-tech text-xs sm:text-sm"
                >
                  <span className="text-white/75 text-xs font-medium">{dateStr}</span>
                  <div className="grid grid-cols-[1fr_64px_1fr] items-center gap-2.5 min-w-0">
                    <span className="font-bold text-white text-right text-xs sm:text-sm">{pHome}</span>
                    <span className="w-16 py-0.5 rounded bg-[#0D0E11] border border-white/10 font-scoreboard text-base text-[#DFFF00] text-center shrink-0">
                      {past.home_score ?? 0} - {past.away_score ?? 0}
                    </span>
                    <span className="font-bold text-white text-left text-xs sm:text-sm">{pAway}</span>
                  </div>
                  <div className="flex items-center justify-end gap-1.5 shrink-0">
                    {past.highlight_video_id ? (
                      <span className="px-2.5 py-0.5 rounded bg-[#DFFF00] text-[#0A0A0C] text-xs font-black uppercase inline-flex items-center gap-1 shrink-0">
                        <Play className="w-3 h-3 fill-[#0A0A0C]" />
                        <span>VOD</span>
                      </span>
                    ) : (
                      <ArrowRight className="w-4 h-4 text-white/50" />
                    )}
                  </div>
                </Link>
              );
            })}
          </div>
        ) : espnH2H.length > 0 ? (
          <div className="space-y-2">
            {espnH2H.map((ev) => {
              const dStr = ev.date
                ? new Date(ev.date).toLocaleDateString('en-GB', {
                    day: 'numeric',
                    month: 'short',
                    year: 'numeric',
                  })
                : 'Past Meeting';
              return (
                <div
                  key={ev.id}
                  className="grid grid-cols-[84px_1fr_80px] sm:grid-cols-[104px_1fr_110px] items-center gap-2 p-3 rounded-xl bg-[#16181D] border border-white/10 font-tech text-xs sm:text-sm"
                >
                  <span className="text-white/75 text-xs font-medium">{dStr}</span>
                  <div className="grid grid-cols-[1fr_64px_1fr] items-center gap-2.5 min-w-0">
                    <span className="font-bold text-white text-right text-xs sm:text-sm">{ev.homeTeamName}</span>
                    <span className="w-16 py-0.5 rounded bg-[#0D0E11] border border-white/10 font-scoreboard text-base text-[#DFFF00] text-center shrink-0">
                      {ev.scoreText}
                    </span>
                    <span className="font-bold text-white text-left text-xs sm:text-sm">{ev.awayTeamName}</span>
                  </div>
                  <div className="flex items-center justify-end shrink-0">
                    <ArrowRight className="w-4 h-4 text-white/50" />
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <div className="p-4 rounded-xl bg-[#16181D] border border-white/10 text-xs font-tech text-white/50">
            Click &ldquo;Open H2H Comparator&rdquo; above to inspect full multi-season club metrics and records.
          </div>
        )}
      </div>

      {/* Section 3: Last 5 Matches Season Form Guide */}
      {(homeLastFive.length > 0 || awayLastFive.length > 0) && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {[
            { title: `${homeName} Last 5 Matches`, list: homeLastFive },
            { title: `${awayName} Last 5 Matches`, list: awayLastFive },
          ].map((col) => (
            <div
              key={col.title}
              className="bg-[#16181D] rounded-xl border border-white/10 p-4 space-y-2.5"
            >
              <div className="flex items-center gap-2 pb-2 border-b border-white/5">
                <Activity className="w-3.5 h-3.5 text-[#DFFF00]" />
                <span className="font-tech text-xs font-bold text-white uppercase">
                  {col.title}
                </span>
              </div>
              {col.list.map((gm) => (
                <div
                  key={gm.id}
                  className="flex items-center justify-between text-xs font-tech py-1 border-b border-white/5 last:border-none"
                >
                  <div className="flex items-center gap-2">
                    <span
                      className={`w-6 h-6 rounded flex items-center justify-center text-xs font-black ${
                        gm.result === 'W'
                          ? 'bg-emerald-500/20 text-emerald-400'
                          : gm.result === 'D'
                          ? 'bg-zinc-500/20 text-zinc-300'
                          : 'bg-red-500/20 text-red-400'
                      }`}
                    >
                      {gm.result}
                    </span>
                    <span className="text-white/70 font-semibold">{gm.atVs}</span>
                    <span className="font-bold text-white text-xs sm:text-sm">{gm.opponentName}</span>
                  </div>
                  <span className="font-mono text-white/90 font-bold">{gm.score}</span>
                </div>
              ))}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
