'use client';

import React, { useState, useMemo, useEffect } from 'react';
import { BarChart3, Users, Newspaper, Shield, Crosshair } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';
import MatchLineupsView from './MatchLineupsView';
import MatchStatsView from './MatchStatsView';
import WebPitchRadar, { WebPitchPlay } from './WebPitchRadar';

interface MatchTacticalBoxscoreProps {
  match: MatchRecord;
  isScoreRevealed?: boolean;
  onRevealScore?: () => void;
}

type TabKey = 'radar' | 'stats' | 'lineups' | 'story';

export default function MatchTacticalBoxscore({
  match,
  isScoreRevealed = false,
  onRevealScore,
}: MatchTacticalBoxscoreProps) {
  const { isSpoilerFree } = useSpoilerFree();
  const [internalRevealed, setInternalRevealed] = useState(false);

  const { statistics: stats, formations, editorial_headline: headline, editorial_story: story, events } = match;
  const isFinished = match.status === 'FINISHED';
  const isLive = ['IN_PLAY', 'PAUSED', 'LIVE'].includes(match.status);
  const isGuarded = isFinished && isSpoilerFree && !isScoreRevealed && !internalRevealed;

  const hasEvents = Boolean(events && events.length > 0);
  const hasStats = Boolean(stats && (Object.keys(stats.home || {}).length > 0 || Object.keys(stats.away || {}).length > 0));
  const hasFormations = Boolean(formations && (formations.home?.starters?.length || formations.away?.starters?.length));
  const hasStory = Boolean(story && story.trim().length > 0);

  const cleanStory = useMemo(() => {
    if (!story) return '';
    return story.replace(/(?:\.\.\.)?\s*Continue\s+reading\.{0,3}/gi, '').trim();
  }, [story]);

  const homeName = match.home_team_name || match.home_team;
  const awayName = match.away_team_name || match.away_team;

  const pitchPlays: WebPitchPlay[] = useMemo(() => {
    if (!events || events.length === 0) return [];
    return events.map((e, idx) => ({
      id: e.id || `play-${idx}`,
      clock: e.clock || '',
      type: e.type || '',
      type_text: e.type_text || e.type,
      text: e.text || '',
      player: e.player,
      team_name: e.side === 'home' ? homeName : awayName,
      x_percent: e.x_coord ?? (e.side === 'home' ? 75 : 25),
      y_percent: e.y_coord ?? (50 + ((idx % 3) - 1) * 18),
      target_x: e.target_x ?? null,
      target_y: e.target_y ?? null,
    }));
  }, [events, homeName, awayName]);

  const availableTabs: TabKey[] = useMemo(() => {
    const list: TabKey[] = [];
    if (hasEvents || isLive) list.push('radar');
    if (hasStats || isLive) list.push('stats');
    if (hasFormations) list.push('lineups');
    if (hasStory) list.push('story');
    return list;
  }, [hasEvents, hasStats, hasFormations, hasStory, isLive]);

  const [activeTab, setActiveTab] = useState<TabKey>(availableTabs[0] || 'radar');
  const effectiveTab = availableTabs.includes(activeTab) ? activeTab : availableTabs[0] || 'radar';

  useEffect(() => {
    if (availableTabs.length > 0 && !availableTabs.includes(activeTab)) {
      setActiveTab(availableTabs[0]);
    }
  }, [availableTabs, activeTab]);

  if (!hasEvents && !hasStats && !hasFormations && !hasStory && !isLive) return null;

  return (
    <section aria-label="Tactical Boxscore & Formations" className="space-y-4 select-none">
      <div className="flex items-center justify-between gap-3 flex-wrap">
        <h2 className="text-xs font-tech font-black uppercase tracking-wider text-white/60 flex items-center gap-1.5 shrink-0">
          <Shield className="w-3.5 h-3.5 text-[#DFFF00]" />
          <span>Tactical Intelligence Hub</span>
        </h2>

        <div className="flex items-center gap-1 p-1 rounded-2xl bg-[#0A0A0C] border border-white/10 overflow-x-auto text-xs font-tech">
          {availableTabs.map((tab) => {
            const isSelected = effectiveTab === tab;
            const labels: Record<TabKey, { name: string; icon: React.ReactNode }> = {
              radar: { name: 'Pitch Radar', icon: <Crosshair className="w-3.5 h-3.5" /> },
              stats: { name: 'Stats', icon: <BarChart3 className="w-3.5 h-3.5" /> },
              lineups: { name: 'Lineups', icon: <Users className="w-3.5 h-3.5" /> },
              story: { name: 'Report', icon: <Newspaper className="w-3.5 h-3.5" /> },
            };
            return (
              <button
                key={tab}
                type="button"
                onClick={() => setActiveTab(tab)}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl font-bold uppercase tracking-wider transition-all cursor-pointer whitespace-nowrap ${
                  isSelected
                    ? 'bg-[#DFFF00] text-[#0A0A0C] font-black shadow-[0_0_12px_rgba(223,255,0,0.3)]'
                    : 'text-white/60 hover:text-white hover:bg-white/5'
                }`}
              >
                {labels[tab].icon}
                <span>{labels[tab].name}</span>
              </button>
            );
          })}
        </div>
      </div>

      {isGuarded ? (
        <div
          onClick={() => {
            setInternalRevealed(true);
            onRevealScore?.();
          }}
          className="p-5 rounded-2xl bg-[#121316] border border-[#DFFF00]/30 hover:border-[#DFFF00] flex items-center justify-between cursor-pointer transition-all group"
        >
          <div>
            <div className="text-xs font-tech font-bold text-white uppercase tracking-wider">Tactical Stats Guarded</div>
            <div className="text-[11px] font-tech text-white/50">Spoiler-free mode active • Click to reveal all</div>
          </div>
          <span className="px-3.5 py-1.5 rounded-xl bg-[#DFFF00] text-[#0A0A0C] text-xs font-tech font-black uppercase tracking-wider">
            Reveal All ▾
          </span>
        </div>
      ) : (
        <div className="space-y-4">
          {effectiveTab === 'radar' && (
            <WebPitchRadar
              plays={pitchPlays}
              homeTeam={homeName}
              awayTeam={awayName}
              isLive={isLive}
              homeColor={match.home_color}
              awayColor={match.away_color}
            />
          )}

          {effectiveTab === 'stats' && (
            <MatchStatsView
              match={match}
              homeName={homeName}
              awayName={awayName}
              isLive={isLive}
              onOpenRadar={() => setActiveTab('radar')}
            />
          )}

          {effectiveTab === 'lineups' && hasFormations && (
            <MatchLineupsView
              formations={formations}
              homeName={homeName}
              awayName={awayName}
              homeColor={match.home_color}
              awayColor={match.away_color}
            />
          )}

          {effectiveTab === 'story' && hasStory && (
            <div className="bg-[#121316] border border-white/10 rounded-2xl p-4 sm:p-5 space-y-2.5 font-tech">
              {headline && <h3 className="font-scoreboard text-base sm:text-lg text-white leading-snug uppercase tracking-wide">{headline}</h3>}
              <div className="text-[11px] text-[#DFFF00] font-semibold pb-2 border-b border-white/10 uppercase tracking-wider">
                Official Debrief &amp; Match Analysis
              </div>
              <div className="prose prose-invert max-w-none text-white/90 text-xs sm:text-sm leading-relaxed whitespace-pre-line">
                {cleanStory}
              </div>
            </div>
          )}

          {/* Lineup Quick Teaser Accordion Strip matching Stitch 1:1 */}
          {hasFormations && effectiveTab !== 'lineups' && (
            <div className="w-full bg-[#121316] border border-white/10 rounded-xl p-3 flex flex-wrap items-center justify-between gap-3 text-xs font-tech">
              <div className="flex items-center gap-2.5 truncate">
                <span className="px-2 py-0.5 rounded bg-[#1A1C22] text-[#DFFF00] font-mono font-bold text-[10px]">
                  {formations?.home?.formation || 'XI'}
                </span>
                <span className="font-bold text-white truncate">{homeName} Starting XI:</span>
                <span className="text-white/50 truncate max-w-sm hidden sm:inline">
                  {(formations?.home?.starters || []).slice(0, 6).map((p) => p.name).join(', ')}...
                </span>
              </div>
              <button
                onClick={() => setActiveTab('lineups')}
                className="px-3 py-1 rounded-lg bg-[#1A1C22] hover:bg-[#252830] text-[#DFFF00] text-[11px] font-bold uppercase tracking-wider transition-colors cursor-pointer"
              >
                Full Tactical Bench &amp; Radar &rarr;
              </button>
            </div>
          )}
        </div>
      )}
    </section>
  );
}
