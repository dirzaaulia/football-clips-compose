'use client';

import React, { useState } from 'react';
import { BarChart3, Users, Newspaper, Clock, Shield, Crosshair } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';
import MatchEventsTimeline from './MatchEventsTimeline';
import MatchLineupsView from './MatchLineupsView';
import MatchStatsView from './MatchStatsView';
import WebPitchRadar, { WebPitchPlay } from './WebPitchRadar';

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

  const homeName = match.home_team_name || match.home_team;
  const awayName = match.away_team_name || match.away_team;

  const pitchPlays: WebPitchPlay[] = React.useMemo(() => {
    if (!events || events.length === 0) {
      if (isLive) {
        return [
          {
            id: 'live-kickoff',
            clock: match.display_clock || 'LIVE',
            type: 'match_in_play',
            type_text: 'Match in Play',
            text: `${homeName} vs ${awayName} is in play!`,
            team_name: homeName,
            x_percent: 50,
            y_percent: 50,
          },
        ];
      }
      return [];
    }
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
  }, [events, isLive, match.display_clock, homeName, awayName]);

  const availableTabs = React.useMemo(() => {
    const list: Array<'timeline' | 'radar' | 'stats' | 'lineups' | 'story'> = [];
    if (hasEvents || isLive) list.push('timeline');
    if (hasEvents || isLive) list.push('radar');
    if (hasStats || isLive) list.push('stats');
    if (hasFormations) list.push('lineups');
    if (hasStory) list.push('story');
    return list;
  }, [hasEvents, hasStats, hasFormations, hasStory, isLive]);

  const getInitialTab = (): 'timeline' | 'radar' | 'stats' | 'lineups' | 'story' => {
    if (hasEvents || isLive) return 'timeline';
    if (hasStats) return 'stats';
    if (hasFormations) return 'lineups';
    if (hasStory) return 'story';
    return 'timeline';
  };

  const [activeTab, setActiveTab] = useState<'timeline' | 'radar' | 'stats' | 'lineups' | 'story'>(getInitialTab);

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

  return (
    <section aria-label="Tactical Boxscore & Formations" className="space-y-4">
      {/* Modern Glassmorphic Pill Segmented Bar */}
      <div className="flex items-center justify-between gap-3 flex-wrap">
        <h2 className="text-xs font-black uppercase tracking-wider text-[#94A3B8] flex items-center gap-1.5 shrink-0">
          <Shield className="w-3.5 h-3.5 text-[#7098F8]" />
          <span>Match Intelligence</span>
        </h2>

        <div className="flex items-center gap-1 p-1 rounded-2xl bg-[#14181F]/90 backdrop-blur-md border border-[#334155]/60 overflow-x-auto scrollbar-none text-xs">
          {(hasEvents || isLive) && (
            <button
              type="button"
              onClick={() => setActiveTab('timeline')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl font-bold transition-all cursor-pointer whitespace-nowrap ${
                effectiveTab === 'timeline'
                  ? 'bg-[#7098F8] text-white shadow-[0_2px_10px_rgba(112,152,248,0.4)]'
                  : 'text-[#94A3B8] hover:text-white hover:bg-white/5'
              }`}
            >
              <Clock className={`w-3.5 h-3.5 ${effectiveTab === 'timeline' ? 'text-white' : 'text-[#4FC3F7]'}`} />
              <span>Timeline</span>
            </button>
          )}

          {(hasEvents || isLive) && (
            <button
              type="button"
              onClick={() => setActiveTab('radar')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl font-bold transition-all cursor-pointer whitespace-nowrap ${
                effectiveTab === 'radar'
                  ? 'bg-[#7098F8] text-white shadow-[0_2px_10px_rgba(112,152,248,0.4)]'
                  : 'text-[#94A3B8] hover:text-white hover:bg-white/5'
              }`}
            >
              <Crosshair className={`w-3.5 h-3.5 ${effectiveTab === 'radar' ? 'text-white' : 'text-cyan-400'}`} />
              <span>Pitch Radar</span>
            </button>
          )}

          {(hasStats || isLive) && (
            <button
              type="button"
              onClick={() => setActiveTab('stats')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl font-bold transition-all cursor-pointer whitespace-nowrap ${
                effectiveTab === 'stats'
                  ? 'bg-[#7098F8] text-white shadow-[0_2px_10px_rgba(112,152,248,0.4)]'
                  : 'text-[#94A3B8] hover:text-white hover:bg-white/5'
              }`}
            >
              <BarChart3 className={`w-3.5 h-3.5 ${effectiveTab === 'stats' ? 'text-white' : 'text-[#4FC3F7]'}`} />
              <span>Statistics</span>
            </button>
          )}

          {hasFormations && (
            <button
              type="button"
              onClick={() => setActiveTab('lineups')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl font-bold transition-all cursor-pointer whitespace-nowrap ${
                effectiveTab === 'lineups'
                  ? 'bg-[#7098F8] text-white shadow-[0_2px_10px_rgba(112,152,248,0.4)]'
                  : 'text-[#94A3B8] hover:text-white hover:bg-white/5'
              }`}
            >
              <Users className={`w-3.5 h-3.5 ${effectiveTab === 'lineups' ? 'text-white' : 'text-[#4FC3F7]'}`} />
              <span>Lineups</span>
            </button>
          )}

          {hasStory && (
            <button
              type="button"
              onClick={() => setActiveTab('story')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl font-bold transition-all cursor-pointer whitespace-nowrap ${
                effectiveTab === 'story'
                  ? 'bg-[#7098F8] text-white shadow-[0_2px_10px_rgba(112,152,248,0.4)]'
                  : 'text-[#94A3B8] hover:text-white hover:bg-white/5'
              }`}
            >
              <Newspaper className={`w-3.5 h-3.5 ${effectiveTab === 'story' ? 'text-white' : 'text-[#4FC3F7]'}`} />
              <span>Report</span>
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
            <MatchStatsView match={match} homeName={homeName} awayName={awayName} isLive={isLive} />
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
