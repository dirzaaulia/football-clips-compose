import React, { useState, useMemo, useEffect } from 'react';
import { Activity, Calendar, Filter } from 'lucide-react';
import { PipelineInfo, TelemetryEvent } from '../types';
import { MatchLiveCard, GroupedMatchLive } from './MatchLiveCard';
import { CompetitionFixtureCard, GroupedCompetitionFixtures, FixtureItem } from './CompetitionFixtureCard';
import { parseEventMatchDetails } from './liveScoreHelper';
import { supabase } from '@/lib/database';

interface LiveScoresPipelineViewProps {
  pipelineInfo?: PipelineInfo;
  events: TelemetryEvent[];
}

export function LiveScoresPipelineView({ pipelineInfo, events }: LiveScoresPipelineViewProps) {
  const [subTab, setSubTab] = useState<'live' | 'fixtures'>('live');
  const [filterType, setFilterType] = useState<string>('ALL');
  const [dbFixtures, setDbFixtures] = useState<GroupedCompetitionFixtures[]>([]);

  // Filter events relevant to matches/fixtures
  const matchEvents = useMemo(() => {
    return events.filter((ev) => {
      if (
        ev.code === 'EDITORIAL_SYNC' ||
        ev.event_text?.includes('EDITORIAL_SYNC') ||
        ev.event_text?.includes('[MATCH STORY]')
      ) {
        return false;
      }
      const p = (ev.pipeline || '').toLowerCase();
      const s = (ev.subsystem || '').toUpperCase();
      const txt = ev.event_text || '';
      return (
        p === 'fixtures' ||
        p === 'live_scores' ||
        s === 'LIVE_SCORES' ||
        s === 'FIXTURES' ||
        txt.includes('[LIVE SCORE]') ||
        txt.includes('[KICKOFF]') ||
        txt.includes('[GOAL]') ||
        txt.includes('[CARD]') ||
        txt.includes('[SUBSTITUTION]') ||
        txt.includes('[FULL TIME]') ||
        txt.includes('[STATUS UPDATE]') ||
        txt.includes('[REGRESSION BLOCKED]') ||
        txt.includes('[FIXTURES SYNCED]') ||
        txt.includes('[NEW FIXTURE]')
      );
    });
  }, [events]);

  // Group 1: Live Scores grouped by Match ID
  const groupedMatches = useMemo(() => {
    const map = new Map<string, TelemetryEvent[]>();
    for (const ev of matchEvents) {
      if (
        ev.event_text?.includes('[FIXTURES SYNCED]') ||
        ev.code === 'FIXTURES_SYNCED' ||
        ev.code === 'DB_UPSERT' ||
        ev.code === 'SUPPORTED_CLUBS' ||
        ev.code === 'NO_CHANGE'
      ) {
        continue;
      }
      const parsed = parseEventMatchDetails(ev);
      if (!ev.match_id && !parsed.home) continue;
      const key = ev.match_id ? String(ev.match_id) : parsed.home;
      if (!map.has(key)) map.set(key, []);
      map.get(key)!.push(ev);
    }

    const result: GroupedMatchLive[] = [];
    for (const [matchId, evList] of Array.from(map.entries())) {
      let home = '';
      let away = '';
      let comp = '';
      let status = '';
      let homeScore: number | null = null;
      let awayScore: number | null = null;

      for (const ev of evList) {
        const d = parseEventMatchDetails(ev);
        if (d.home && !home) home = d.home;
        if (d.away && !away) away = d.away;
        if (d.comp && !comp) comp = d.comp;
        if (d.status && !status) status = d.status;
        if (d.homeScore !== null && homeScore === null) homeScore = d.homeScore;
        if (d.awayScore !== null && awayScore === null) awayScore = d.awayScore;
      }

      if (!home) continue;

      result.push({
        matchId,
        home,
        away,
        competition: comp,
        status: status,
        homeScore,
        awayScore,
        events: evList,
      });
    }
    return result;
  }, [matchEvents]);

  // Group 2: Fixtures from live events buffer
  const groupedCompetitions = useMemo(() => {
    const fixturesMap = new Map<string, FixtureItem>();
    for (const ev of matchEvents) {
      if (Array.isArray(ev.attributes?.fixtures)) {
        for (const item of ev.attributes.fixtures as FixtureItem[]) {
          if (item.id && !fixturesMap.has(item.id)) fixturesMap.set(item.id, item);
        }
      }
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
      const comp = fix.competition || 'Supported Competition';
      if (!compGroups.has(comp)) compGroups.set(comp, []);
      compGroups.get(comp)!.push(fix);
    }

    const result: GroupedCompetitionFixtures[] = [];
    for (const [competition, fixtures] of Array.from(compGroups.entries())) {
      result.push({ competition, fixtures });
    }
    return result;
  }, [matchEvents]);

  // Load scheduled fixtures from DB if buffer has no upcoming fixtures
  useEffect(() => {
    async function loadScheduledFixtures() {
      try {
        const { data } = await supabase
          .from('matches')
          .select('id, home_team_name, away_team_name, competition_name, utc_date, status, round_name')
          .in('status', ['TIMED', 'SCHEDULED'])
          .order('utc_date', { ascending: true })
          .limit(40);

        if (data && data.length > 0) {
          const compMap = new Map<string, FixtureItem[]>();
          for (const m of data) {
            const comp = m.competition_name || 'Upcoming Fixtures';
            if (!compMap.has(comp)) compMap.set(comp, []);
            compMap.get(comp)!.push({
              id: String(m.id),
              home: m.home_team_name,
              away: m.away_team_name,
              competition: comp,
              utc_date: m.utc_date,
              status: m.status,
              round: m.round_name,
            });
          }
          const res: GroupedCompetitionFixtures[] = [];
          for (const [competition, fixtures] of Array.from(compMap.entries())) {
            res.push({ competition, fixtures });
          }
          setDbFixtures(res);
        }
      } catch {
        // Ignore DB query errors
      }
    }
    loadScheduledFixtures();
  }, []);

  const displayedCompetitions = groupedCompetitions.length > 0 ? groupedCompetitions : dbFixtures;

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
          <span>Fixture Sync ({displayedCompetitions.length} Competitions)</span>
        </button>
      </div>

      {/* View Content */}
      {subTab === 'live' ? (
        <div className="space-y-4">
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
          {displayedCompetitions.length === 0 ? (
            <div className="p-12 text-center text-slate-500 font-mono text-sm bg-slate-900/40 rounded-xl border border-slate-800">
              No upcoming fixture ingestions recorded in this cycle. Next fixture schedule sync runs on a 12-hour cadence.
            </div>
          ) : (
            displayedCompetitions.map((group) => (
              <CompetitionFixtureCard key={group.competition} group={group} />
            ))
          )}
        </div>
      )}
    </div>
  );
}
