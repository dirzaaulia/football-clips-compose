import React, { useState, useMemo } from 'react';
import {
  Activity,
  Calendar,
  Filter,
} from 'lucide-react';
import { PipelineInfo, TelemetryEvent } from '../types';
import { MatchLiveCard, GroupedMatchLive } from './MatchLiveCard';
import { CompetitionFixtureCard, GroupedCompetitionFixtures, FixtureItem } from './CompetitionFixtureCard';

interface LiveScoresPipelineViewProps {
  pipelineInfo?: PipelineInfo;
  events: TelemetryEvent[];
}

function parseEventMatchDetails(ev: TelemetryEvent) {
  let home = String(ev.attributes?.home || ev.attributes?.home_team || '');
  let away = String(ev.attributes?.away || ev.attributes?.away_team || '');
  let comp = String(ev.attributes?.competition || '');
  let status = String(ev.attributes?.status || '');
  let homeScore = ev.attributes?.home_score != null ? Number(ev.attributes.home_score) : null;
  let awayScore = ev.attributes?.away_score != null ? Number(ev.attributes.away_score) : null;

  const text = ev.event_text || '';
  if (!comp) {
    const compMatch = text.match(/\[([A-Z_ ]+)\]\s*\[(.*?)\]/);
    if (compMatch) comp = compMatch[2].trim();
  }
  if (!home || !away) {
    const vsMatch = text.match(/(?:for\s+|kicked off|underway for|break:\s*|preserved as\s*|fixture:\s*|^)?([A-Za-z0-9 .'-]+)\s+vs\s+([A-Za-z0-9 .'-]+?)(?:\s+(?:finished|status|kicked|score|is|underway|break|inserted|\(|\:|\-|\d)|$)/i);
    if (vsMatch) {
      if (!home) home = vsMatch[1].replace(/.*\]\s*(?:\([^)]+\)\s*)?/, '').trim();
      if (!away) away = vsMatch[2].trim();
    }
  }
  if (!status) {
    if (text.includes('[FULL TIME]')) status = 'FINISHED';
    else if (text.includes('[KICKOFF]') || text.includes('[SECOND HALF]')) status = 'IN_PLAY';
    else if (text.includes('[HALF TIME]')) status = 'PAUSED';
  }
  if (homeScore === null || awayScore === null) {
    const scoreMatch = text.match(/(?:finished:|Score updated for [^:]+:)\s*(\d+)\s*-\s*(\d+)/i);
    if (scoreMatch) {
      homeScore = parseInt(scoreMatch[1], 10);
      awayScore = parseInt(scoreMatch[2], 10);
    }
  }

  return { home, away, comp, status, homeScore, awayScore };
}

export function LiveScoresPipelineView({
  pipelineInfo,
  events,
}: LiveScoresPipelineViewProps) {
  const [subTab, setSubTab] = useState<'live' | 'fixtures'>('live');
  const [filterType, setFilterType] = useState<string>('ALL');

  // Filter events relevant to matches/fixtures
  const matchEvents = useMemo(() => {
    return events.filter((ev) => {
      // Exclude editorial story sync and RSS noise
      if (
        ev.code === 'EDITORIAL_SYNC' ||
        ev.event_text?.includes('EDITORIAL_SYNC') ||
        ev.event_text?.includes('[MATCH STORY]')
      ) {
        return false;
      }

      const p = (ev.pipeline || '').toLowerCase();
      const txt = ev.event_text || '';
      return (
        p.includes('match') ||
        p.includes('fixture') ||
        txt.includes('[LIVE SCORE]') ||
        txt.includes('[KICKOFF]') ||
        txt.includes('[FULL TIME]') ||
        txt.includes('[STATUS UPDATE]') ||
        txt.includes('[STATUS PROTECTED]') ||
        txt.includes('[GOAL]') ||
        txt.includes('[CARD]') ||
        txt.includes('[SUBSTITUTION]') ||
        txt.includes('[NEW FIXTURE]') ||
        txt.includes('[FIXTURES SYNCED]')
      );
    });
  }, [events]);

  // Group 1: Live Scores grouped by Match
  const groupedMatches = useMemo(() => {
    const map = new Map<string, GroupedMatchLive>();

    for (const ev of matchEvents) {
      const mId = String(ev.match_id || ev.attributes?.id || '');
      if (!mId) continue;

      const parsed = parseEventMatchDetails(ev);
      const clock = ev.attributes?.display_clock ? String(ev.attributes.display_clock) : undefined;

      if (!map.has(mId)) {
        map.set(mId, {
          matchId: mId,
          home: parsed.home || 'Home',
          away: parsed.away || 'Away',
          competition: parsed.comp || 'League',
          homeScore: parsed.homeScore,
          awayScore: parsed.awayScore,
          clock,
          status: parsed.status || 'UNKNOWN',
          events: [ev],
        });
      } else {
        const current = map.get(mId)!;
        current.events.push(ev);
        if (parsed.home && current.home === 'Home') current.home = parsed.home;
        if (parsed.away && current.away === 'Away') current.away = parsed.away;
        if (parsed.comp && current.competition === 'League') current.competition = parsed.comp;
        if (parsed.status && current.status === 'UNKNOWN') current.status = parsed.status;
        if (parsed.homeScore != null) current.homeScore = parsed.homeScore;
        if (parsed.awayScore != null) current.awayScore = parsed.awayScore;
        if (clock) current.clock = clock;
      }
    }

    return Array.from(map.values()).filter(
      (m) => !(m.home === 'Home' && m.away === 'Away' && (m.status === 'UNKNOWN' || !m.status))
    );
  }, [matchEvents]);

  // Group 2: Fixtures grouped by Competition
  const groupedCompetitions = useMemo(() => {
    const fixturesMap = new Map<string, FixtureItem>();

    for (const ev of matchEvents) {
      // Check if event carries fixtures array in attributes
      if (Array.isArray(ev.attributes?.fixtures)) {
        for (const item of ev.attributes.fixtures as FixtureItem[]) {
          if (item.id && !fixturesMap.has(item.id)) {
            fixturesMap.set(item.id, item);
          }
        }
      }

      // Check single fixture event
      if (ev.event_text?.includes('[NEW FIXTURE]') && ev.match_id) {
        const fId = String(ev.match_id);
        if (!fixturesMap.has(fId)) {
          fixturesMap.set(fId, {
            id: fId,
            home: String(ev.attributes?.home || 'Home'),
            away: String(ev.attributes?.away || 'Away'),
            competition: String(ev.attributes?.competition || 'League'),
            utc_date: ev.attributes?.utc_date ? String(ev.attributes.utc_date) : undefined,
            status: String(ev.attributes?.status || 'SCHEDULED'),
            round: ev.attributes?.round ? String(ev.attributes.round) : undefined,
          });
        }
      }
    }

    const compGroups = new Map<string, FixtureItem[]>();
    for (const fix of Array.from(fixturesMap.values())) {
      const comp = fix.competition || 'Other Competitions';
      if (!compGroups.has(comp)) compGroups.set(comp, []);
      compGroups.get(comp)!.push(fix);
    }

    const result: GroupedCompetitionFixtures[] = [];
    for (const [competition, fixtures] of Array.from(compGroups.entries())) {
      result.push({ competition, fixtures });
    }
    return result;
  }, [matchEvents]);

  const filteredMatches = useMemo(() => {
    if (filterType === 'ALL') return groupedMatches;
    return groupedMatches.filter((m) =>
      m.events.some((ev) => (ev.event_text || '').includes(`[${filterType}]`)),
    );
  }, [groupedMatches, filterType]);

  return (
    <div className="space-y-5">
      {/* Engine Status Banner */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div>
          <p className="text-[11px] font-mono text-slate-400 uppercase tracking-wider">Engine Status</p>
          <div className="flex items-center gap-1.5 mt-1">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
            <p className="text-sm font-bold text-slate-100">{pipelineInfo?.status || 'Active'}</p>
          </div>
        </div>
        <div>
          <p className="text-[11px] font-mono text-slate-400 uppercase tracking-wider">Live Ticker Cadence</p>
          <p className="text-sm font-bold text-emerald-400 mt-1">60s (Semi-Live)</p>
        </div>
        <div>
          <p className="text-[11px] font-mono text-slate-400 uppercase tracking-wider">Fixture Sync Cadence</p>
          <p className="text-sm font-bold text-indigo-400 mt-1">12h (H-2 to H+7)</p>
        </div>
        <div>
          <p className="text-[11px] font-mono text-slate-400 uppercase tracking-wider">Last Sync</p>
          <p className="text-xs font-mono text-slate-300 mt-1 truncate">
            {pipelineInfo?.last_sync ? new Date(pipelineInfo.last_sync).toLocaleTimeString() : 'Recent'}
          </p>
        </div>
      </div>

      {/* Sub-Tabs: Live Scores vs Fixture Sync */}
      <div className="flex items-center gap-2 border-b border-slate-800 pb-3">
        <button
          onClick={() => setSubTab('live')}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-mono font-bold transition ${
            subTab === 'live'
              ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/40 shadow-sm'
              : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900 border border-transparent'
          }`}
        >
          <Activity className="w-4 h-4" />
          <span>Live Scores ({groupedMatches.length} Matches)</span>
        </button>

        <button
          onClick={() => setSubTab('fixtures')}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-mono font-bold transition ${
            subTab === 'fixtures'
              ? 'bg-indigo-500/20 text-indigo-400 border border-indigo-500/40 shadow-sm'
              : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900 border border-transparent'
          }`}
        >
          <Calendar className="w-4 h-4" />
          <span>Fixture Sync ({groupedCompetitions.length} Competitions)</span>
        </button>
      </div>

      {/* View Content */}
      {subTab === 'live' ? (
        <div className="space-y-4">
          {/* Live Filters */}
          <div className="flex items-center gap-2 overflow-x-auto pb-1">
            <Filter className="w-3.5 h-3.5 text-slate-400 shrink-0 ml-1" />
            {['ALL', 'LIVE SCORE', 'GOAL', 'CARD', 'SUBSTITUTION', 'KICKOFF', 'FULL TIME', 'STATUS PROTECTED'].map((ft) => (
              <button
                key={ft}
                onClick={() => setFilterType(ft)}
                className={`px-3 py-1.5 rounded-lg text-xs font-mono font-semibold transition shrink-0 ${
                  filterType === ft
                    ? 'bg-emerald-600 text-white shadow-sm'
                    : 'bg-slate-900 text-slate-400 hover:text-slate-200 hover:bg-slate-800 border border-slate-800'
                }`}
              >
                {ft}
              </button>
            ))}
          </div>

          {filteredMatches.length === 0 ? (
            <div className="p-12 text-center text-slate-500 font-mono text-sm bg-slate-900/40 rounded-xl border border-slate-800">
              No live score match events found in the current buffer.
            </div>
          ) : (
            filteredMatches.map((m) => <MatchLiveCard key={m.matchId} match={m} />)
          )}
        </div>
      ) : (
        <div className="space-y-4">
          {groupedCompetitions.length === 0 ? (
            <div className="p-12 text-center text-slate-500 font-mono text-sm bg-slate-900/40 rounded-xl border border-slate-800">
              No upcoming fixture ingestions recorded in this cycle. Next fixture schedule sync runs on a 12-hour cadence.
            </div>
          ) : (
            groupedCompetitions.map((group) => (
              <CompetitionFixtureCard key={group.competition} group={group} />
            ))
          )}
        </div>
      )}
    </div>
  );
}
