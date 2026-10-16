'use client';

import React, { useState } from 'react';
import { BarChart3, Users, Newspaper, EyeOff, ChevronDown, Shield } from 'lucide-react';
import { MatchRecord } from '@/lib/supabase';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';

interface MatchTacticalBoxscoreProps {
  match: MatchRecord;
}

export default function MatchTacticalBoxscore({ match }: MatchTacticalBoxscoreProps) {
  const { isSpoilerFree } = useSpoilerFree();
  const [isRevealed, setIsRevealed] = useState(false);

  const stats = match.statistics;
  const formations = match.formations;
  const headline = match.editorial_headline;
  const story = match.editorial_story;
  const source = match.editorial_source || 'ESPN Accredited';

  const hasStats = Boolean(
    stats &&
      ((stats.home && Object.keys(stats.home).length > 0) ||
        (stats.away && Object.keys(stats.away).length > 0))
  );
  const hasFormations = Boolean(
    formations &&
      ((formations.home?.starters && formations.home.starters.length > 0) ||
        (formations.away?.starters && formations.away.starters.length > 0))
  );
  const hasStory = Boolean(story && story.trim().length > 0);

  const [activeTab, setActiveTab] = useState<'stats' | 'lineups' | 'story'>(() => {
    if (hasStats) return 'stats';
    if (hasFormations) return 'lineups';
    return 'story';
  });

  React.useEffect(() => {
    if (activeTab === 'stats' && !hasStats) {
      if (hasFormations) setActiveTab('lineups');
      else if (hasStory) setActiveTab('story');
    } else if (activeTab === 'lineups' && !hasFormations) {
      if (hasStats) setActiveTab('stats');
      else if (hasStory) setActiveTab('story');
    } else if (activeTab === 'story' && !hasStory) {
      if (hasStats) setActiveTab('stats');
      else if (hasFormations) setActiveTab('lineups');
    }
  }, [hasStats, hasFormations, hasStory, activeTab]);

  if (!hasStats && !hasFormations && !hasStory) {
    return null;
  }

  const isFinished = match.status === 'FINISHED';
  const isLive =
    match.status === 'IN_PLAY' ||
    match.status === 'PAUSED' ||
    match.status === 'LIVE';
  const isUpcoming = !isFinished && !isLive;

  // Never guard tactical preview for upcoming matches
  const isGuarded = !isUpcoming && isSpoilerFree && !isRevealed;

  const homeName = match.home_team_name || match.home_team;
  const awayName = match.away_team_name || match.away_team;

  const formatStatDisplay = (val: string, isPct?: boolean, key?: string) => {
    const num = parseFloat(val);
    if (isNaN(num)) return val;
    if (isPct) {
      if (key === 'possessionPct') {
        return `${num.toFixed(1)}%`;
      }
      if (num <= 1.0 && num > 0) {
        return `${Math.round(num * 100)}%`;
      }
      return `${Math.round(num)}%`;
    }
    return val;
  };

  // Stat metrics to display with user-friendly labels
  const STAT_METRICS: Array<{ key: string; label: string; isPct?: boolean }> = [
    { key: 'possessionPct', label: 'Possession %', isPct: true },
    { key: 'totalShots', label: 'Total Shots' },
    { key: 'shotsOnTarget', label: 'Shots on Target' },
    { key: 'wonCorners', label: 'Corners' },
    { key: 'foulsCommitted', label: 'Fouls Committed' },
    { key: 'yellowCards', label: 'Yellow Cards' },
    { key: 'redCards', label: 'Red Cards' },
    { key: 'offsides', label: 'Offsides' },
    { key: 'saves', label: 'Goalkeeper Saves' },
    { key: 'passPct', label: 'Pass Accuracy %', isPct: true },
    { key: 'totalPasses', label: 'Total Passes' },
  ];

  return (
    <section className="rounded-3xl bg-[#181C20] border border-[#334155] p-5 sm:p-7 space-y-5 shadow-xl">
      {/* Header & Tabs */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-[#334155]/60">
        <div className="flex items-center gap-2">
          <Shield className="w-4 h-4 text-[#7098F8]" />
          <h2 className="text-sm font-extrabold uppercase tracking-wider text-white">
            Authentic Match Centre &amp; Tactical Data
          </h2>
        </div>

        {/* Tab Controls */}
        <div className="flex items-center gap-1.5 p-1 bg-[#101418] rounded-xl border border-[#334155]/60 text-xs font-bold self-start sm:self-auto">
          {hasStats && (
            <button
              onClick={() => setActiveTab('stats')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg transition-all cursor-pointer ${
                activeTab === 'stats'
                  ? 'bg-[#1E3A8A] text-white shadow-sm'
                  : 'text-[#94A3B8] hover:text-white'
              }`}
            >
              <BarChart3 className="w-3.5 h-3.5" />
              <span>Statistics</span>
            </button>
          )}

          {hasFormations && (
            <button
              onClick={() => setActiveTab('lineups')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg transition-all cursor-pointer ${
                activeTab === 'lineups'
                  ? 'bg-[#1E3A8A] text-white shadow-sm'
                  : 'text-[#94A3B8] hover:text-white'
              }`}
            >
              <Users className="w-3.5 h-3.5" />
              <span>Lineups</span>
            </button>
          )}

          {hasStory && (
            <button
              onClick={() => setActiveTab('story')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg transition-all cursor-pointer ${
                activeTab === 'story'
                  ? 'bg-[#1E3A8A] text-white shadow-sm'
                  : 'text-[#94A3B8] hover:text-white'
              }`}
            >
              <Newspaper className="w-3.5 h-3.5" />
              <span>Report</span>
            </button>
          )}
        </div>
      </div>

      {isGuarded ? (
        <div className="py-8 px-4 rounded-2xl bg-[#101418] border border-[#334155] text-center space-y-3">
          <div className="flex items-center justify-center gap-2 text-sm font-bold text-[#94A3B8]">
            <EyeOff className="w-4 h-4 text-[#7098F8]" />
            <span>Tactical boxscore contains in-play statistics and match progression metrics.</span>
          </div>
          <button
            onClick={() => setIsRevealed(true)}
            className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-[#1E3A8A] hover:bg-[#2563EB] text-white text-xs font-bold transition-all shadow-md cursor-pointer"
          >
            <span>Reveal Tactical Statistics &amp; Lineups</span>
            <ChevronDown className="w-3.5 h-3.5" />
          </button>
        </div>
      ) : (
        <div>
          {/* TAB 1: Match Statistics */}
          {activeTab === 'stats' && hasStats && (
            <div className="space-y-4">
              {/* Teams Header */}
              <div className="flex items-center justify-between text-xs font-black text-white px-2 pb-1 border-b border-[#334155]/40">
                <span className="truncate max-w-[140px] text-left">{homeName}</span>
                <span className="text-[#94A3B8] uppercase text-[10px] tracking-widest font-semibold">Match Stat</span>
                <span className="truncate max-w-[140px] text-right">{awayName}</span>
              </div>

              <div className="space-y-3">
                {STAT_METRICS.map(({ key, label, isPct }) => {
                  const rawHome = stats?.home?.[key] ?? '0';
                  const rawAway = stats?.away?.[key] ?? '0';

                  const numHome = parseFloat(rawHome) || 0;
                  const numAway = parseFloat(rawAway) || 0;
                  const total = numHome + numAway;

                  const homeWidth = total > 0 ? (numHome / total) * 100 : 50;
                  const awayWidth = total > 0 ? (numAway / total) * 100 : 50;

                  return (
                    <div key={key} className="space-y-1">
                      <div className="flex items-center justify-between text-xs font-bold text-[#F1F5F9] px-2">
                        <span className="font-mono text-[#7098F8]">
                          {formatStatDisplay(rawHome, isPct, key)}
                        </span>
                        <span className="text-[11px] text-[#94A3B8] font-medium">{label}</span>
                        <span className="font-mono text-[#4FC3F7]">
                          {formatStatDisplay(rawAway, isPct, key)}
                        </span>
                      </div>

                      {/* Comparative Dual Progress Bar */}
                      <div className="h-1.5 w-full bg-[#101418] rounded-full overflow-hidden flex gap-0.5">
                        <div
                          className="h-full bg-[#7098F8] rounded-l-full transition-all duration-500"
                          style={{ width: `${homeWidth}%` }}
                        />
                        <div
                          className="h-full bg-[#4FC3F7] rounded-r-full transition-all duration-500"
                          style={{ width: `${awayWidth}%` }}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* TAB 2: Tactical Formations & Starting XI */}
          {activeTab === 'lineups' && hasFormations && (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Home Team Lineup */}
              <div className="bg-[#101418] border border-[#334155]/70 rounded-2xl p-4 space-y-3">
                <div className="flex items-center justify-between pb-2 border-b border-[#334155]/50">
                  <span className="font-bold text-white text-xs sm:text-sm truncate">{homeName}</span>
                  {formations?.home?.formation && (
                    <span className="px-2 py-0.5 rounded-lg bg-[#1E3A8A] text-[#7098F8] text-[10px] font-mono font-black">
                      {formations.home.formation}
                    </span>
                  )}
                </div>

                <div className="space-y-1.5">
                  {(formations?.home?.starters || []).map((p, idx) => (
                    <div key={idx} className="flex items-center justify-between text-xs py-1 px-1.5 rounded-lg hover:bg-[#1E2227] transition-colors">
                      <div className="flex items-center gap-2">
                        <span className="w-5 text-center font-mono font-bold text-[#7098F8] text-[11px]">
                          {p.jersey || '-'}
                        </span>
                        <span className="font-medium text-white">{p.name}</span>
                      </div>
                      <span className="text-[10px] text-[#94A3B8] font-bold font-mono">
                        {p.position || ''}
                      </span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Away Team Lineup */}
              <div className="bg-[#101418] border border-[#334155]/70 rounded-2xl p-4 space-y-3">
                <div className="flex items-center justify-between pb-2 border-b border-[#334155]/50">
                  <span className="font-bold text-white text-xs sm:text-sm truncate">{awayName}</span>
                  {formations?.away?.formation && (
                    <span className="px-2 py-0.5 rounded-lg bg-[#0E7490] text-[#4FC3F7] text-[10px] font-mono font-black">
                      {formations.away.formation}
                    </span>
                  )}
                </div>

                <div className="space-y-1.5">
                  {(formations?.away?.starters || []).map((p, idx) => (
                    <div key={idx} className="flex items-center justify-between text-xs py-1 px-1.5 rounded-lg hover:bg-[#1E2227] transition-colors">
                      <div className="flex items-center gap-2">
                        <span className="w-5 text-center font-mono font-bold text-[#4FC3F7] text-[11px]">
                          {p.jersey || '-'}
                        </span>
                        <span className="font-medium text-white">{p.name}</span>
                      </div>
                      <span className="text-[10px] text-[#94A3B8] font-bold font-mono">
                        {p.position || ''}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* TAB 3: Authentic Journalistic Report */}
          {activeTab === 'story' && hasStory && (
            <div className="bg-[#101418] border border-[#334155]/70 rounded-2xl p-5 space-y-3">
              {headline && (
                <h3 className="text-base sm:text-lg font-black text-white leading-snug">
                  {headline}
                </h3>
              )}
              <div className="text-xs text-[#7098F8] font-semibold flex items-center gap-2 pb-2 border-b border-[#334155]/40">
                <span>By {source}</span>
                <span>&bull;</span>
                <span className="text-[#94A3B8]">Accredited Journalism</span>
              </div>
              <div className="prose prose-invert max-w-none text-[#F1F5F9] text-xs sm:text-sm leading-relaxed whitespace-pre-line">
                {story}
              </div>
            </div>
          )}
        </div>
      )}
    </section>
  );
}
