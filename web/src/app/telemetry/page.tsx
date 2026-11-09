'use client';

import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { database } from '@/lib/database';
import {
  TelemetryPayload, MatchWaterfall, NotificationsPayload,
  AudiencePayload, AppSessionItem, TelemetryTab, UserAnalyticsEvent, DbFinishedMatch,
} from './types';
import { buildMatchWaterfalls } from './mapper';
import { groupWaterfallsByWeekAndCompetition } from './grouping';
import { TelemetryHeader } from './components/TelemetryHeader';
import { QuotaGaugeCard } from './components/QuotaGaugeCard';
import { WaterfallCard } from './components/WaterfallCard';
import { GroupedWaterfallsView } from './components/GroupedWaterfallsView';
import PipelineFlowGraph from './components/PipelineFlowGraph';
import { TelemetryNavigationRail } from './components/TelemetryNavigationRail';
import { LiveScoresPipelineView } from './components/LiveScoresPipelineView';
import { WebSubPipelineView } from './components/WebSubPipelineView';
import { NotificationsPipelineView } from './components/NotificationsPipelineView';
import { AnalyticsPipelineView } from './components/AnalyticsPipelineView';
import { ShortsPipelineView, DbFootballShort } from './components/ShortsPipelineView';
import { TelemetryFilterBar } from './components/TelemetryFilterBar';
import { ChannelLookupMap } from './mapperHelper';
import { TaxonomyManagementModal } from './components/TaxonomyManagementModal';

export default function TelemetryPage() {
  const [activeTab, setActiveTab] = useState<TelemetryTab>('highlights');
  const [data, setData] = useState<TelemetryPayload | null>(null);
  const [dbFinishedMatches, setDbFinishedMatches] = useState<DbFinishedMatch[]>([]);
  const [dbShorts, setDbShorts] = useState<DbFootballShort[]>([]);
  const [channelLookup, setChannelLookup] = useState<ChannelLookupMap>({});
  const [notifications, setNotifications] = useState<NotificationsPayload | null>(null);
  const [audience, setAudience] = useState<AudiencePayload | null>(null);
  const [analyticsEvents, setAnalyticsEvents] = useState<UserAnalyticsEvent[]>([]);
  const [appSessions, setAppSessions] = useState<AppSessionItem[]>([]);
  const [totalAnalyticsCount, setTotalAnalyticsCount] = useState<number>(0);
  const [totalSessionsCount, setTotalSessionsCount] = useState<number>(0);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [isClearingLogs, setIsClearingLogs] = useState(false);
  const [isTaxonomyOpen, setIsTaxonomyOpen] = useState(false);
  const [autoRefreshInterval, setAutoRefreshInterval] = useState(15);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'LINKED' | 'COOLDOWN' | 'UNLINKED'>('ALL');
  const [viewMode, setViewMode] = useState<'grouped' | 'flat'>('grouped');

  const fetchTelemetry = useCallback(async () => {
    setIsRefreshing(true);
    try {
      const { data: rows, error } = await database
        .from('telemetry_state')
        .select('key, payload')
        .in('key', ['telemetry', 'notifications', 'audience']);

      let telemetryPayload: TelemetryPayload | null = null;
      if (!error && rows) {
        for (const row of rows) {
          if (row.key === 'telemetry') {
            telemetryPayload = row.payload as TelemetryPayload;
            setData(telemetryPayload);
          } else if (row.key === 'notifications') setNotifications(row.payload as NotificationsPayload);
          else if (row.key === 'audience') setAudience(row.payload as AudiencePayload);
        }
      }

      const now = new Date();
      const thirtyDaysAgo = new Date();
      thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
      const matchSelectCols = 'id, competition_id, competition_name, utc_date, status, home_team_name, away_team_name, home_score, away_score, highlight_video_id, last_youtube_check, youtube_region_info, highlight_videos';
      const { data: mRows } = await database
        .from('matches')
        .select(matchSelectCols)
        .eq('status', 'FINISHED')
        .gte('utc_date', thirtyDaysAgo.toISOString())
        .lte('utc_date', now.toISOString())
        .order('utc_date', { ascending: false });

      let allMatches = mRows || [];
      const loadedIds = new Set(allMatches.map((m: any) => String(m.id)));
      const extraIds = new Set<string>();
      if (telemetryPayload?.cycles) {
        for (const ev of (telemetryPayload as any)?.events || []) {
          if (ev.match_id && !loadedIds.has(String(ev.match_id))) extraIds.add(String(ev.match_id));
        }
      }
      if (extraIds.size > 0) {
        const { data: xRows } = await database
          .from('matches')
          .select(matchSelectCols)
          .eq('status', 'FINISHED')
          .lte('utc_date', now.toISOString())
          .in('id', Array.from(extraIds).slice(0, 50));
        if (xRows) allMatches = [...allMatches, ...xRows];
      }
      const finishedOnly = allMatches.filter((m: any) => m.status === 'FINISHED' && (!m.utc_date || new Date(m.utc_date).getTime() <= now.getTime()));
      setDbFinishedMatches(finishedOnly as DbFinishedMatch[]);

      const [{ data: compRows }, { data: clubRows }, { data: shortRows }, { data: bcastRows }] = await Promise.all([
        database.from('competitions').select('id, youtube_handle, backup_channel_handles'),
        database.from('supported_clubs').select('name, youtube_handle, aliases').limit(1000),
        database.from('football_shorts').select('youtube_video_id, title, channel_id, channel_name, category, duration_seconds, published_at').order('published_at', { ascending: false }).limit(50),
        database.from('broadcasters').select('broadcaster_id, name, channel_handle, channel_id, allowed_countries'),
      ]);
      const compHandles: Record<string, string[]> = {};
      for (const c of (compRows || []) as any[]) {
        const list = [c.youtube_handle, ...(Array.isArray(c.backup_channel_handles) ? c.backup_channel_handles : [])].filter(Boolean);
        if (c.id && list.length > 0) compHandles[String(c.id)] = list;
      }
      const clubHandles: Record<string, string> = {};
      for (const cl of (clubRows || []) as any[]) {
        if (cl.name && cl.youtube_handle) {
          clubHandles[String(cl.name).toLowerCase().trim()] = String(cl.youtube_handle);
          for (const al of (Array.isArray(cl.aliases) ? cl.aliases : [])) {
            clubHandles[String(al).toLowerCase().trim()] = String(cl.youtube_handle);
          }
        }
      }
      const broadcasters: Record<string, { name: string; handle: string; tier: number }> = {};
      for (const b of (bcastRows || []) as any[]) {
        if (b.channel_handle) {
          const isDom = Array.isArray(b.allowed_countries) && b.allowed_countries.length > 0 && b.allowed_countries.length <= 5;
          broadcasters[String(b.channel_handle).toLowerCase().trim()] = {
            name: b.name || b.channel_handle,
            handle: b.channel_handle,
            tier: isDom ? 3 : 2,
          };
        }
      }
      setChannelLookup({ compHandles, clubHandles, broadcasters });
      if (shortRows) setDbShorts(shortRows as DbFootballShort[]);

      const { data: aRows, count: aCount } = await database
        .from('user_analytics_events')
        .select('*', { count: 'exact' })
        .order('created_at', { ascending: false })
        .limit(200);
      if (aRows) setAnalyticsEvents(aRows as UserAnalyticsEvent[]);
      if (typeof aCount === 'number') setTotalAnalyticsCount(aCount);

      const { data: sRows, count: sCount } = await database
        .from('app_sessions')
        .select('*', { count: 'exact' })
        .order('created_at', { ascending: false })
        .limit(200);
      if (sRows) setAppSessions(sRows as AppSessionItem[]);
      if (typeof sCount === 'number') setTotalSessionsCount(sCount);
    } catch (e) {
      console.error('Failed to fetch telemetry data:', e);
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  }, []);

  useEffect(() => {
    fetchTelemetry();
  }, [fetchTelemetry]);

  const handleClearLogs = async () => {
    if (!confirm('Are you sure you want to clear all telemetry cycle traces?')) return;
    setIsClearingLogs(true);
    try {
      if (data) {
        const clearedPayload = { ...data, events: [], cycle_count: 0 };
        await database.from('telemetry_state').upsert({ key: 'telemetry', payload: clearedPayload });
      }
      await fetchTelemetry();
    } catch (e) {
      console.error('Failed to clear telemetry logs:', e);
    } finally {
      setIsClearingLogs(false);
    }
  };

  const waterfalls = useMemo(
    () => buildMatchWaterfalls(data?.events || [], dbFinishedMatches, channelLookup),
    [data, dbFinishedMatches, channelLookup]
  );

  const filteredWaterfalls = useMemo(() => {
    return waterfalls.filter((wf: MatchWaterfall) => {
      const matchesSearch = !searchQuery || wf.title.toLowerCase().includes(searchQuery.toLowerCase()) || wf.matchId.includes(searchQuery);
      const matchesStatus = statusFilter === 'ALL' || wf.status === statusFilter;
      return matchesSearch && matchesStatus;
    });
  }, [waterfalls, searchQuery, statusFilter]);

  const groupedWaterfalls = useMemo(() => groupWaterfallsByWeekAndCompetition(filteredWaterfalls), [filteredWaterfalls]);

  const unlinkedCount = useMemo(() => waterfalls.filter((w) => w.status === 'UNLINKED' || w.status === 'COOLDOWN').length, [waterfalls]);

  const liveMatchesCount = useMemo(() => {
    if (!data?.events) return 0;
    const liveSet = new Set<string>();
    for (const ev of data.events) {
      if (ev.attributes?.status && ['IN_PLAY', 'PAUSED', 'LIVE'].includes(String(ev.attributes.status))) {
        const id = String(ev.match_id || ev.attributes.id || '');
        if (id) liveSet.add(id);
      }
    }
    return liveSet.size;
  }, [data]);

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 font-sans p-4 sm:p-6 lg:p-8">
      <div className="max-w-7xl 2xl:max-w-[1440px] mx-auto space-y-6">
        <TelemetryHeader
          onRefresh={fetchTelemetry}
          isRefreshing={isRefreshing}
          autoRefreshInterval={autoRefreshInterval}
          setAutoRefreshInterval={setAutoRefreshInterval}
          lastUpdated={data?.updated_at ? new Date(data.updated_at).toLocaleTimeString() : undefined}
          onExportJson={() => navigator.clipboard.writeText(JSON.stringify({ telemetry: data, notifications, audience }, null, 2))}
          onClearLogs={handleClearLogs}
          isClearingLogs={isClearingLogs}
          onOpenTaxonomy={() => setIsTaxonomyOpen(true)}
        />

        {data?.quota && <QuotaGaugeCard quota={data.quota} />}

        <div className="flex flex-col lg:flex-row items-start gap-6">
          <TelemetryNavigationRail
            activeTab={activeTab}
            onTabChange={setActiveTab}
            unlinkedCount={unlinkedCount}
            liveMatchesCount={liveMatchesCount}
            notificationsCount={notifications?.notifications?.length ?? 0}
            analyticsCount={(totalSessionsCount || appSessions.length) + (totalAnalyticsCount || analyticsEvents.length)}
            websubStatus={data?.pipelines?.websub?.status}
            isLiveActive={liveMatchesCount > 0}
          />

          <main className="flex-1 min-w-0 w-full space-y-5">
            {activeTab === 'highlights' && (
              <>
                <PipelineFlowGraph
                  todayQuotaUsage={data?.quota?.hits_today ?? 0}
                  onNodeClick={(key) => setSearchQuery((prev) => (prev === key ? '' : key))}
                />

                <TelemetryFilterBar
                  searchQuery={searchQuery}
                  onSearchChange={setSearchQuery}
                  viewMode={viewMode}
                  onViewModeChange={setViewMode}
                  statusFilter={statusFilter}
                  onStatusFilterChange={setStatusFilter}
                />

                <div className="space-y-3">
                  <div className="flex items-center justify-between text-xs text-slate-400 px-1 font-mono">
                    <span>Diagnosed Match Waterfall Traces ({filteredWaterfalls.length} of {waterfalls.length})</span>
                    <span>Click any match card to inspect diagnostic steps</span>
                  </div>

                  {isLoading ? (
                    <div className="p-12 text-center text-slate-500 font-mono text-sm bg-slate-900/40 rounded-xl border border-slate-800">
                      Loading real-time telemetry stream...
                    </div>
                  ) : viewMode === 'grouped' ? (
                    <GroupedWaterfallsView
                      groups={groupedWaterfalls}
                      onOverrideSuccess={fetchTelemetry}
                    />
                  ) : filteredWaterfalls.length === 0 ? (
                    <div className="p-12 text-center text-slate-500 font-mono text-sm bg-slate-900/40 rounded-xl border border-slate-800">
                      No matching waterfalls found for current search/filter.
                    </div>
                  ) : (
                    filteredWaterfalls.map((wf) => (
                      <WaterfallCard key={wf.matchId} waterfall={wf} onOverrideSuccess={fetchTelemetry} />
                    ))
                  )}
                </div>
              </>
            )}

            {activeTab === 'fixtures' && <LiveScoresPipelineView pipelineInfo={data?.pipelines?.fixtures} events={data?.events || []} />}
            {activeTab === 'shorts' && <ShortsPipelineView dbShorts={dbShorts} events={data?.events || []} />}
            {activeTab === 'websub' && <WebSubPipelineView pipelineInfo={data?.pipelines?.websub} events={data?.events || []} />}

            {activeTab === 'notifications' && <NotificationsPipelineView notificationsPayload={notifications} audiencePayload={audience} />}
            {activeTab === 'analytics' && (
              <AnalyticsPipelineView
                events={analyticsEvents}
                appSessions={appSessions}
                totalAnalyticsCount={totalAnalyticsCount}
                totalSessionsCount={totalSessionsCount}
                isLoading={isLoading}
              />
            )}
          </main>
        </div>

        <TaxonomyManagementModal
          isOpen={isTaxonomyOpen}
          onClose={() => setIsTaxonomyOpen(false)}
        />
      </div>
    </div>
  );
}
