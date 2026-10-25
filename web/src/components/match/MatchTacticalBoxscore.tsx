'use client';

import React, { useState } from 'react';
import { BarChart3, Users, Newspaper, Clock, Shield } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';
import MatchEventsTimeline from './MatchEventsTimeline';
import MatchLineupsView from './MatchLineupsView';

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
  const events = match.events;

  const isFinished = match.status === 'FINISHED';
  const isLive = ['IN_PLAY', 'PAUSED', 'LIVE'].includes(match.status);
  const isUpcoming = !isFinished && !isLive;
  // Spoiler protection applies only to finished matches, never live in-play
  const isGuarded = isFinished && isSpoilerFree && !isRevealed;

  const hasEvents = Boolean(events && events.length > 0);
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
  const cleanStory = React.useMemo(() => {
    if (!story) return '';
    return story.replace(/(?:\.\.\.)?\s*Continue\s+reading\.{0,3}/gi, '').trim();
  }, [story]);

  const availableTabs = React.useMemo(() => {
    const list: Array<'timeline' | 'stats' | 'lineups' | 'story'> = [];
    if (hasEvents || isLive) list.push('timeline');
    if (hasStats || isLive) list.push('stats');
    if (hasFormations) list.push('lineups');
    if (hasStory) list.push('story');
    return list;
  }, [hasEvents, hasStats, hasFormations, hasStory, isLive]);

  const getInitialTab = (): 'timeline' | 'stats' | 'lineups' | 'story' => {
    if (hasEvents || isLive) return 'timeline';
    if (hasStats) return 'stats';
    if (hasFormations) return 'lineups';
    if (hasStory) return 'story';
    return 'timeline';
  };

  const [activeTab, setActiveTab] = useState<'timeline' | 'stats' | 'lineups' | 'story'>(getInitialTab);

  const effectiveTab = availableTabs.includes(activeTab)
    ? activeTab
    : availableTabs[0] || 'timeline';

  React.useEffect(() => {
    if (availableTabs.length > 0 && !availableTabs.includes(activeTab)) {
      setActiveTab(availableTabs[0]);
    }
  }, [availableTabs, activeTab]);

  if (!hasEvents && !hasStats && !hasFormations && !hasStory && !isLive) {
    return null;
  }


  const homeName = match.home_team_name || match.home_team;
  const awayName = match.away_team_name || match.away_team;

  const formatStatDisplay = (val: string, isPct?: boolean, key?: string) => {
    const num = parseFloat(val);
    if (isNaN(num)) return val;
    if (isPct) {
      if (key === 'possessionPct') return `${num.toFixed(1)}%`;
      if (num <= 1.0 && num > 0) return `${Math.round(num * 100)}%`;
      return `${Math.round(num)}%`;
    }
    return val;
  };

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
    <section className="rounded-3xl bg-[#181C20] border border-[#334155] p-4 sm:p-6 space-y-4 shadow-xl">
      {/* Header & Tabs */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-[#334155]/60">
        <div className="flex items-center gap-2">
          <Shield className="w-4 h-4 text-[#7098F8]" />
          <h2 className="text-xs font-black uppercase tracking-wider text-white">
            {isUpcoming ? 'Pre-Match Intelligence' : isLive ? 'Live Match Centre' : 'Match Intelligence Hub'}
          </h2>
        </div>

        {/* Tab Controls */}
        <div className="flex items-center gap-1 p-1 bg-[#101418] rounded-xl border border-[#334155]/60 text-xs font-bold overflow-x-auto">
          {(hasEvents || isLive) && (
            <button
              onClick={() => setActiveTab('timeline')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg transition-all cursor-pointer whitespace-nowrap ${
                effectiveTab === 'timeline' ? 'bg-[#1E3A8A] text-white shadow-sm' : 'text-[#94A3B8] hover:text-white'
              }`}
            >
              <Clock className="w-3.5 h-3.5 text-[#4FC3F7]" />
              <span>TIMELINE</span>
            </button>
          )}

          {(hasStats || isLive) && (
            <button
              onClick={() => setActiveTab('stats')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg transition-all cursor-pointer whitespace-nowrap ${
                effectiveTab === 'stats' ? 'bg-[#1E3A8A] text-white shadow-sm' : 'text-[#94A3B8] hover:text-white'
              }`}
            >
              <BarChart3 className="w-3.5 h-3.5 text-[#4FC3F7]" />
              <span>STATS</span>
            </button>
          )}

          {hasFormations && (
            <button
              onClick={() => setActiveTab('lineups')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg transition-all cursor-pointer whitespace-nowrap ${
                effectiveTab === 'lineups' ? 'bg-[#1E3A8A] text-white shadow-sm' : 'text-[#94A3B8] hover:text-white'
              }`}
            >
              <Users className="w-3.5 h-3.5 text-[#4FC3F7]" />
              <span>LINEUPS</span>
            </button>
          )}

          {hasStory && (
            <button
              onClick={() => setActiveTab('story')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg transition-all cursor-pointer whitespace-nowrap ${
                effectiveTab === 'story' ? 'bg-[#1E3A8A] text-white shadow-sm' : 'text-[#94A3B8] hover:text-white'
              }`}
            >
              <Newspaper className="w-3.5 h-3.5 text-[#4FC3F7]" />
              <span>REPORT</span>
            </button>
          )}
        </div>
      </div>

      {/* Main Content Area */}
      {isGuarded ? (
        <div
          onClick={() => setIsRevealed(true)}
          className="p-5 rounded-2xl bg-[#101418] border border-[#7098F8]/40 hover:border-[#7098F8] flex items-center justify-between cursor-pointer transition-all group"
        >
          <div>
            <div className="text-xs font-bold text-white">Tactical Stats &amp; Formations Guarded</div>
            <div className="text-[11px] text-[#94A3B8]">Spoiler-free mode enabled • Tap to reveal live data</div>
          </div>
          <span className="px-3 py-1.5 rounded-xl bg-[#7098F8]/20 text-[#7098F8] text-xs font-bold">
            Reveal ▾
          </span>
        </div>
      ) : (
        <div>
          {/* TAB 1: Match Events Timeline */}
          {effectiveTab === 'timeline' && (
            hasEvents ? (
              <MatchEventsTimeline
                events={events}
                homeName={homeName}
                awayName={awayName}
                isSpoilerFree={isFinished && isSpoilerFree}
                isRevealed={isRevealed}
                onReveal={() => setIsRevealed(true)}
              />
            ) : isLive ? (
              <div className="py-6 px-4 text-center rounded-2xl bg-[#101418] border border-[#334155]/60 space-y-1.5">
                <div className="flex items-center justify-center gap-1.5 text-red-400 text-xs font-black uppercase tracking-wider">
                  <span className="w-2 h-2 rounded-full bg-red-500 animate-pulse" />
                  <span>Match in Play</span>
                </div>
                <p className="text-xs text-[#94A3B8]">
                  Key match incidents (goals, bookings, substitutions) will appear here live as they happen.
                </p>
              </div>
            ) : null
          )}

          {/* TAB 2: Comparative Statistics */}
          {effectiveTab === 'stats' && (
            hasStats ? (
              <div className="space-y-4">
                <div className="flex items-center justify-between text-xs font-black text-white px-2">
                  <span className="text-[#7098F8] truncate max-w-[45%]">{homeName}</span>
                  <span className="text-[#94A3B8] text-[10px] uppercase tracking-wider">Metrics</span>
                  <span className="text-[#4FC3F7] truncate max-w-[45%] text-right">{awayName}</span>
                </div>

                <div className="space-y-3">
                  {STAT_METRICS.map(({ key, label, isPct }) => {
                    const rawHome = stats?.home?.[key];
                    const rawAway = stats?.away?.[key];
                    if (!rawHome && !rawAway) return null;

                    const numH = parseFloat(rawHome || '0');
                    const numA = parseFloat(rawAway || '0');
                    const sum = numH + numA;
                    const homeWidth = sum > 0 ? (numH / sum) * 100 : 50;
                    const awayWidth = sum > 0 ? (numA / sum) * 100 : 50;

                    return (
                      <div key={key} className="space-y-1">
                        <div className="flex justify-between items-center text-xs">
                          <span className="font-mono text-[#7098F8]">{formatStatDisplay(rawHome || '0', isPct, key)}</span>
                          <span className="text-[11px] text-[#94A3B8] font-medium">{label}</span>
                          <span className="font-mono text-[#4FC3F7]">{formatStatDisplay(rawAway || '0', isPct, key)}</span>
                        </div>
                        <div className="h-1.5 w-full bg-[#101418] rounded-full overflow-hidden flex gap-0.5">
                          <div className="h-full bg-[#7098F8] rounded-l-full transition-all duration-500" style={{ width: `${homeWidth}%` }} />
                          <div className="h-full bg-[#4FC3F7] rounded-r-full transition-all duration-500" style={{ width: `${awayWidth}%` }} />
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            ) : isLive ? (
              <div className="py-6 px-4 text-center rounded-2xl bg-[#101418] border border-[#334155]/60 space-y-1.5">
                <div className="flex items-center justify-center gap-1.5 text-red-400 text-xs font-black uppercase tracking-wider">
                  <span className="w-2 h-2 rounded-full bg-red-500 animate-ping" />
                  <span>Telemetry Initializing</span>
                </div>
                <p className="text-xs text-[#94A3B8]">
                  Collecting live possession, shot accuracy, and pitch metrics directly from stadium feeds...
                </p>
              </div>
            ) : (
              <div className="py-6 text-center text-xs text-[#94A3B8]">
                No detailed statistics recorded for this fixture.
              </div>
            )
          )}

          {/* TAB 3: Lineups */}
          {effectiveTab === 'lineups' && hasFormations && (
            <MatchLineupsView formations={formations} homeName={homeName} awayName={awayName} />
          )}

          {/* TAB 4: Journalistic Story */}
          {effectiveTab === 'story' && hasStory && (
            <div className="bg-[#101418] border border-[#334155]/70 rounded-2xl p-4 space-y-2">
              {headline && <h3 className="text-sm sm:text-base font-black text-white leading-snug">{headline}</h3>}
              <div className="text-[11px] text-[#7098F8] font-semibold pb-2 border-b border-[#334155]/40">
                By {source} &bull; Accredited Journalism
              </div>
              <div className="prose prose-invert max-w-none text-[#F1F5F9] text-xs sm:text-sm leading-relaxed whitespace-pre-line">
                {cleanStory}
              </div>
            </div>
          )}
        </div>
      )}
    </section>
  );
}
