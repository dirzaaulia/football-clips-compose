'use client';

import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { database } from '@/lib/database';
import {
  TelemetryPayload, MatchWaterfall, NotificationsPayload,
  AudiencePayload, AppSessionItem, TelemetryTab, UserAnalyticsEvent, DbFinishedMatch,
  CoverageFilterType, SeasonFilterType,
} from './types';
import {
  fetchFinishedMatchesBySeason,
  fetchTelemetryChannels,
  computeCoverageMetrics,
  matchesCoverageFilter,
} from './coverageMetricsHelper';
import { CoverageMetricsDeck } from './components/CoverageMetricsDeck';
import { buildMatchWaterfalls } from './mapper';
import { WaterfallTraceRow } from './waterfallTraceHelper';
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
import TelemetrySkeleton, {
  WaterfallGroupedSkeleton,
  WaterfallCardSkeleton,
} from './components/TelemetrySkeleton';

export default function TelemetryPage() {
  const [activeTab, setActiveTab] = useState<TelemetryTab>('highlights');
  const [data, setData] = useState<TelemetryPayload | null>(null);
  const [dbFinishedMatches, setDbFinishedMatches] = useState<DbFinishedMatch[]>([]);
  const [dbShorts, setDbShorts] = useState<DbFootballShort[]>([]);
  const [waterfallTraces, setWaterfallTraces] = useState<WaterfallTraceRow[]>([]);
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
  const [statusFilter, setStatusFilter] = useState<CoverageFilterType>('ALL');
  const [seasonFilter, setSeasonFilter] = useState<SeasonFilterType>('2026');
  const [isSeasonLoading, setIsSeasonLoading] = useState(false);
  const [viewMode, setViewMode] = useState<'grouped' | 'flat'>('grouped');

  const handleSeasonChange = (nextSeason: SeasonFilterType) => {
    if (nextSeason === seasonFilter) return;
    setIsSeasonLoading(true);
    setSeasonFilter(nextSeason);
  };

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

      const extraIds = Array.from(new Set<string>(
        ((telemetryPayload as any)?.events || [])
          .map((ev: any) => (ev.match_id ? String(ev.match_id) : ''))
          .filter(Boolean),
      ));
      const seasonMatches = await fetchFinishedMatchesBySeason(seasonFilter, extraIds);
      setDbFinishedMatches(seasonMatches);

      const { channelLookup: resolvedChannels, shortRows } = await fetchTelemetryChannels();
      setChannelLookup(resolvedChannels);
      if (shortRows) setDbShorts(shortRows as DbFootballShort[]);

      const [{ data: aRows, count: aCount }, { data: sRows, count: sCount }, { data: traceRows }] = await Promise.all([
        database.from('user_analytics_events').select('*', { count: 'exact' }).order('created_at', { ascending: false }).limit(200),
        database.from('app_sessions').select('*', { count: 'exact' }).order('last_active', { ascending: false }).limit(200),
        database.from('worker_events').select('event_id, timestamp, metadata').eq('action', 'WATERFALL_TRACE').order('timestamp', { ascending: false }).limit(250),
      ]);
      if (aRows) setAnalyticsEvents(aRows as UserAnalyticsEvent[]);
      if (typeof aCount === 'number') setTotalAnalyticsCount(aCount);
      if (sRows) setAppSessions(sRows as AppSessionItem[]);
      if (typeof sCount === 'number') setTotalSessionsCount(sCount);
      if (traceRows) setWaterfallTraces(traceRows as WaterfallTraceRow[]);
    } catch (err) {
      console.error('Failed to load telemetry state:', err);
    } finally {
      setIsRefreshing(false);
      setIsLoading(false);
      setIsSeasonLoading(false);
    }
  }, [seasonFilter]);

  useEffect(() => {
    fetchTelemetry();
    if (autoRefreshInterval <= 0) return;
    const interval = setInterval(fetchTelemetry, autoRefreshInterval * 1000);
    return () => clearInterval(interval);
  }, [fetchTelemetry, autoRefreshInterval]);

  const handleClearLogs = async () => {
    if (!window.confirm('Clear all telemetry events from the live log stream?')) return;
    setIsClearingLogs(true);
    try {
      if (data) {
        const clearedPayload = { ...data, events: [] };
        await database.from('telemetry_state').upsert({ key: 'telemetry', payload: clearedPayload });
        setData(clearedPayload);
      }
    } finally {
      setIsClearingLogs(false);
    }
  };

  const waterfalls: MatchWaterfall[] = useMemo(
    () => buildMatchWaterfalls(data?.events || [], dbFinishedMatches, channelLookup, waterfallTraces),
    [data, dbFinishedMatches, channelLookup, waterfallTraces],
  );

  const coverageMetrics = useMemo(() => computeCoverageMetrics(waterfalls), [waterfalls]);

  const filteredWaterfalls = useMemo(() => {
    return waterfalls.filter((wf: MatchWaterfall) => {
      const matchesSearch = !searchQuery || wf.title.toLowerCase().includes(searchQuery.toLowerCase()) || wf.matchId.includes(searchQuery);
      return matchesSearch && matchesCoverageFilter(wf, statusFilter);
    });
  }, [waterfalls, searchQuery, statusFilter]);

  const groupedWaterfalls = useMemo(() => groupWaterfallsByWeekAndCompetition(filteredWaterfalls), [filteredWaterfalls]);

  const unlinkedCount = coverageMetrics.unlinkedCount;

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

  if (isLoading && !data) {
    return <TelemetrySkeleton />;
  }

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
                <CoverageMetricsDeck
                  metrics={coverageMetrics}
                  activeFilter={statusFilter}
                  onSelectFilter={setStatusFilter}
                  seasonFilter={seasonFilter}
                  onSeasonChange={handleSeasonChange}
                  isLoadingSeason={isSeasonLoading}
                />

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

                  {isLoading || isSeasonLoading ? (
                    viewMode === 'grouped' ? (
                      <WaterfallGroupedSkeleton />
                    ) : (
                      <div className="space-y-2.5 animate-pulse">
                        {[1, 2, 3].map((i) => (
                          <WaterfallCardSkeleton key={i} />
                        ))}
                      </div>
                    )
                  ) : viewMode === 'grouped' ? (
                    <GroupedWaterfallsView
                      groups={groupedWaterfalls}
                      channelLookup={channelLookup}
                      onOverrideSuccess={fetchTelemetry}
                    />
                  ) : filteredWaterfalls.length === 0 ? (
                    <div className="p-12 text-center text-slate-500 font-mono text-sm bg-slate-900/40 rounded-xl border border-slate-800">
                      No matching waterfalls found for current search/filter.
                    </div>
                  ) : (
                    filteredWaterfalls.map((wf) => (
                      <WaterfallCard
                        key={wf.matchId}
                        waterfall={wf}
                        channelLookup={channelLookup}
                        onOverrideSuccess={fetchTelemetry}
                      />
                    ))
                  )}
                </div>
              </>
            )}

            {activeTab === 'fixtures' && <LiveScoresPipelineView pipelineInfo={data?.pipelines?.fixtures} events={data?.events || []} />}
            {activeTab === 'shorts' && <ShortsPipelineView dbShorts={dbShorts} events={data?.events || []} />}
            {activeTab === 'websub' && <WebSubPipelineView pipelineInfo={data?.pipelines?.websub} events={data?.events || []} />}
            {activeTab === 'notifications' && (
              <NotificationsPipelineView
                notificationsPayload={notifications}
                audiencePayload={audience}
                clubNamesById={channelLookup.clubNamesById}
              />
            )}
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
