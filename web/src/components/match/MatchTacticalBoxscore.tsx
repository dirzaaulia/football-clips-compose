'use client';

import React, { useState, useMemo, useEffect } from 'react';
import { BarChart3, Users, Newspaper, Shield, Crosshair, Swords } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import { EspnSummaryResult } from '@/lib/espn';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';
import MatchLineupsView from './MatchLineupsView';
import MatchStatsView from './MatchStatsView';
import MatchH2HAndLeadersView from './MatchH2HAndLeadersView';
import WebPitchRadar, { WebPitchPlay } from './WebPitchRadar';
import { extractScorerDisplayName } from './MatchScrubberStrip';

interface MatchTacticalBoxscoreProps {
  match: MatchRecord;
  espnSummary?: EspnSummaryResult | null;
  isScoreRevealed?: boolean;
  onRevealScore?: () => void;
}

type TabKey = 'radar' | 'stats' | 'lineups' | 'h2h' | 'story';

export default function MatchTacticalBoxscore({
  match,
  espnSummary,
  isScoreRevealed = false,
  onRevealScore,
}: MatchTacticalBoxscoreProps) {
  const { isSpoilerFree } = useSpoilerFree();
  const [internalRevealed, setInternalRevealed] = useState(false);

  const {
    formations,
    editorial_headline: headline,
    editorial_story: story,
    events,
  } = match;
  const isFinished = match.status === 'FINISHED';
  const isLive = ['IN_PLAY', 'PAUSED', 'LIVE'].includes(match.status);
  const isRevealed = isScoreRevealed || internalRevealed;
  const shouldHideSpoilers = isSpoilerFree && (isFinished || isLive) && !isRevealed;

  const hasLineups = Boolean(
    formations &&
      ((formations.home?.starters && formations.home.starters.length > 0) ||
        (formations.away?.starters && formations.away.starters.length > 0)),
  );
  const hasStory = Boolean(story && story.trim().length > 20);

  const pitchPlays = useMemo<WebPitchPlay[]>(() => {
    const rawEvents = events || [];
    const homeLabel = match.home_team_name || match.home_team;
    const awayLabel = match.away_team_name || match.away_team;

    if (rawEvents.length > 0) {
      return rawEvents.map((e, idx) => {
        const isHome = e.side !== 'away';
        const t = (e.type || e.type_text || 'Play').toLowerCase();
        const isGoal = Boolean(
          e.scoring_play || (t.includes('goal') && !t.includes('kick') && !t.includes('attempt')),
        );
        const isShot = t.includes('shot') || t.includes('attempt') || t.includes('saved');

        // Realistic tactical position: Home attacks right, Away attacks left
        const defaultX = isGoal
          ? isHome ? 87 : 13
          : isShot
          ? isHome ? 78 : 22
          : isHome ? 45 : 55;
        const defaultY = isGoal
          ? (idx % 2 === 0 ? 38 : 31)
          : (25 + ((idx * 7) % 20));

        const xPct =
          e.x_coord != null ? Math.round(e.x_coord > 1 ? e.x_coord : e.x_coord * 100) : defaultX;
        const yPct =
          e.y_coord != null ? Math.round(e.y_coord > 1 ? e.y_coord : e.y_coord * 100) : defaultY;
        const txPct =
          e.target_x != null
            ? Math.round(e.target_x > 1 ? e.target_x : e.target_x * 100)
            : isGoal || isShot
            ? isHome ? 99 : 1
            : null;
        const tyPct =
          e.target_y != null
            ? Math.round(e.target_y > 1 ? e.target_y : e.target_y * 100)
            : isGoal || isShot
            ? 50
            : null;

        const cleanPlayer = extractScorerDisplayName(e);
        return {
          id: e.id || `evt-${idx}`,
          clock: e.clock || '',
          type: isGoal ? 'Goal' : isShot ? 'Shot' : e.type || 'Play',
          type_text: isGoal ? 'Goal' : isShot ? 'Shot' : e.type_text || e.type || 'Play',
          text: e.text || e.type_text || 'Match Event',
          player: cleanPlayer !== 'Scorer' ? cleanPlayer : isHome ? homeLabel : awayLabel,
          assist: e.assist || undefined,
          team_name: isHome ? homeLabel : awayLabel,
          x_percent: Math.max(4, Math.min(96, xPct)),
          y_percent: Math.max(6, Math.min(94, yPct)),
          target_x: txPct,
          target_y: tyPct,
          period: e.period,
        };
      });
    }

    // If no events exist yet, synthesize only actual recorded goals from match score
    const hGoals = match.home_score ?? 0;
    const aGoals = match.away_score ?? 0;
    const scorePlays: WebPitchPlay[] = [];
    for (let i = 0; i < hGoals; i++) {
      scorePlays.push({
        id: `hg-${i}`,
        clock: `${22 + i * 24}'`,
        type: 'Goal',
        type_text: 'Goal',
        text: `Goal — ${homeLabel}`,
        player: homeLabel,
        team_name: homeLabel,
        x_percent: 87,
        y_percent: 42 + (i % 3) * 8,
        target_x: 98,
        target_y: 50,
      });
    }
    for (let i = 0; i < aGoals; i++) {
      scorePlays.push({
        id: `ag-${i}`,
        clock: `${34 + i * 23}'`,
        type: 'Goal',
        type_text: 'Goal',
        text: `Goal — ${awayLabel}`,
        player: awayLabel,
        team_name: awayLabel,
        x_percent: 13,
        y_percent: 44 + (i % 3) * 8,
        target_x: 2,
        target_y: 50,
      });
    }
    return scorePlays;
  }, [
    events,
    match.home_score,
    match.away_score,
    match.home_team,
    match.away_team,
    match.home_team_name,
    match.away_team_name,
  ]);

  const availableTabs = useMemo(() => {
    const list: { key: TabKey; label: string; icon: React.ReactNode }[] = [
      { key: 'radar', label: '2D Pitch View', icon: <Crosshair className="w-3.5 h-3.5" /> },
      { key: 'stats', label: 'Match Stats', icon: <BarChart3 className="w-3.5 h-3.5" /> },
    ];
    if (hasLineups)
      list.push({
        key: 'lineups',
        label: 'Lineups & Formations',
        icon: <Users className="w-3.5 h-3.5" />,
      });
    list.push({
      key: 'h2h',
      label: 'H2H, Form & Leaders',
      icon: <Swords className="w-3.5 h-3.5" />,
    });
    if (hasStory)
      list.push({
        key: 'story',
        label: 'Match Report',
        icon: <Newspaper className="w-3.5 h-3.5" />,
      });
    return list;
  }, [hasLineups, hasStory]);

  const [activeTab, setActiveTab] = useState<TabKey>(availableTabs[0]?.key || 'radar');

  useEffect(() => {
    if (availableTabs.length > 0 && !availableTabs.some((t) => t.key === activeTab)) {
      const timer = setTimeout(() => setActiveTab(availableTabs[0].key), 0);
      return () => clearTimeout(timer);
    }
  }, [availableTabs, activeTab]);

  return (
    <section
      id="telemetry"
      className="rounded-2xl bg-[#121316] border border-white/10 overflow-hidden shadow-lg"
    >
      <div className="flex flex-wrap items-center justify-between gap-3 px-5 py-3.5 bg-[#16191E] border-b border-white/10">
        <div className="flex items-center gap-2">
          <Shield className="w-4 h-4 text-[#DFFF00]" />
          <h3 className="text-xs font-extrabold uppercase tracking-wider text-white">
            Match Centre Pitch, Stats &amp; Lineups
          </h3>
        </div>
        <div className="flex items-center gap-1.5 bg-[#0D0F12] p-1 rounded-xl border border-white/5 overflow-x-auto no-scrollbar">
          {availableTabs.map((tab) => (
            <button
              key={tab.key}
              type="button"
              onClick={() => setActiveTab(tab.key)}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold transition-all shrink-0 cursor-pointer ${
                activeTab === tab.key
                  ? 'bg-[#DFFF00] text-[#0A0A0C] shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              {tab.icon}
              <span>{tab.label}</span>
            </button>
          ))}
        </div>
      </div>

      {shouldHideSpoilers ? (
        <div className="p-8 text-center space-y-3">
          <p className="text-xs sm:text-sm text-slate-400 max-w-md mx-auto">
            Match statistics, shot maps, and post-match reports are hidden while Spoiler-Free mode is active.
          </p>
          <button
            type="button"
            onClick={() => {
              setInternalRevealed(true);
              onRevealScore?.();
            }}
            className="px-4 py-2 rounded-xl bg-[#DFFF00]/15 hover:bg-[#DFFF00]/25 text-[#DFFF00] text-xs font-bold border border-[#DFFF00]/30 transition-colors cursor-pointer"
          >
            Reveal Match Details
          </button>
        </div>
      ) : (
        <>
          {activeTab === 'radar' && (
            <div className="p-4 sm:p-5">
              <WebPitchRadar
                plays={pitchPlays}
                homeTeam={match.home_team_name || match.home_team}
                awayTeam={match.away_team_name || match.away_team}
                isLive={isLive}
                homeColor={match.home_color}
                awayColor={match.away_color}
                formations={formations}
              />
            </div>
          )}
          {activeTab === 'stats' && (
            <MatchStatsView
              match={match}
              homeName={match.home_team_name || match.home_team}
              awayName={match.away_team_name || match.away_team}
              isLive={isLive}
              onOpenRadar={() => setActiveTab('radar')}
            />
          )}
          {activeTab === 'lineups' && formations && (
            <MatchLineupsView
              formations={formations}
              homeName={match.home_team_name || match.home_team}
              awayName={match.away_team_name || match.away_team}
            />
          )}
          {activeTab === 'h2h' && (
            <MatchH2HAndLeadersView match={match} espnSummary={espnSummary} />
          )}
          {activeTab === 'story' && story && (
            <div className="p-5 sm:p-6 space-y-3">
              {headline && (
                <h4 className="text-base sm:text-lg font-extrabold text-white leading-snug">
                  {headline}
                </h4>
              )}
              <div className="text-xs sm:text-sm text-slate-300 leading-relaxed whitespace-pre-line space-y-2 max-h-96 overflow-y-auto pr-2">
                {story}
              </div>
              {match.editorial_source && (
                <p className="text-[11px] text-slate-500 pt-2 border-t border-white/5">
                  Source: {match.editorial_source}
                </p>
              )}
            </div>
          )}
        </>
      )}
    </section>
  );
}
