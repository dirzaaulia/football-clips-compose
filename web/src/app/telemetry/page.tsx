'use client';

import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { Search, Filter, Layers, List } from 'lucide-react';
import { database } from '@/lib/database';
import {
  TelemetryPayload, MatchWaterfall, NotificationsPayload,
  SubscriptionsPayload, TelemetryTab, UserAnalyticsEvent,
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

export default function TelemetryPage() {
  const [activeTab, setActiveTab] = useState<TelemetryTab>('highlights');
  const [data, setData] = useState<TelemetryPayload | null>(null);
  const [notifications, setNotifications] = useState<NotificationsPayload | null>(null);
  const [subscriptions, setSubscriptions] = useState<SubscriptionsPayload | null>(null);
  const [analyticsEvents, setAnalyticsEvents] = useState<UserAnalyticsEvent[]>([]);

  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [isClearingLogs, setIsClearingLogs] = useState(false);
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
        .in('key', ['telemetry', 'notifications', 'subscriptions']);

      if (!error && rows) {
        for (const row of rows) {
          if (row.key === 'telemetry') setData(row.payload as TelemetryPayload);
          else if (row.key === 'notifications') setNotifications(row.payload as NotificationsPayload);
          else if (row.key === 'subscriptions') setSubscriptions(row.payload as SubscriptionsPayload);
        }
      }

      const { data: aRows } = await database
        .from('user_analytics_events')
        .select('*')
        .order('created_at', { ascending: false })
        .limit(100);
      if (aRows) setAnalyticsEvents(aRows as UserAnalyticsEvent[]);
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

  const waterfalls = useMemo(() => (!data?.events ? [] : buildMatchWaterfalls(data.events)), [data]);

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
          onExportJson={() => navigator.clipboard.writeText(JSON.stringify({ telemetry: data, notifications, subscriptions }, null, 2))}
          onClearLogs={handleClearLogs}
          isClearingLogs={isClearingLogs}
        />

        {data?.quota && <QuotaGaugeCard quota={data.quota} />}

        <div className="flex flex-col lg:flex-row items-start gap-6">
          <TelemetryNavigationRail
            activeTab={activeTab}
            onTabChange={setActiveTab}
            unlinkedCount={unlinkedCount}
            liveMatchesCount={liveMatchesCount}
            notificationsCount={notifications?.notifications?.length ?? 0}
            analyticsCount={analyticsEvents.length}
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

                <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 flex flex-col sm:flex-row gap-3 items-stretch sm:items-center justify-between">
                  <div className="relative flex-1">
                    <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                    <input
                      type="text"
                      placeholder="Search match or team (e.g. Arsenal, Real Madrid, 401912257)..."
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                      className="w-full bg-slate-950 border border-slate-800 rounded-lg pl-9 pr-3 py-2 text-xs font-mono text-slate-200 placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition"
                    />
                  </div>

                  <div className="flex items-center gap-2 overflow-x-auto">
                    <div className="flex items-center bg-slate-950 border border-slate-800 rounded-lg p-0.5 mr-1">
                      <button
                        onClick={() => setViewMode('grouped')}
                        className={`p-1.5 rounded text-xs font-mono flex items-center gap-1 transition ${viewMode === 'grouped' ? 'bg-indigo-600 text-white' : 'text-slate-400 hover:text-slate-200'}`}
                        title="Grouped by Week & Priority"
                      >
                        <Layers className="w-3.5 h-3.5" />
                        <span className="hidden sm:inline">Grouped</span>
                      </button>
                      <button
                        onClick={() => setViewMode('flat')}
                        className={`p-1.5 rounded text-xs font-mono flex items-center gap-1 transition ${viewMode === 'flat' ? 'bg-indigo-600 text-white' : 'text-slate-400 hover:text-slate-200'}`}
                        title="Flat List View"
                      >
                        <List className="w-3.5 h-3.5" />
                        <span className="hidden sm:inline">Flat</span>
                      </button>
                    </div>

                    <Filter className="w-3.5 h-3.5 text-slate-400 shrink-0 ml-1" />
                    {(['ALL', 'LINKED', 'COOLDOWN', 'UNLINKED'] as const).map((st) => (
                      <button
                        key={st}
                        onClick={() => setStatusFilter(st)}
                        className={`px-3 py-1.5 rounded-lg text-xs font-mono font-semibold transition shrink-0 ${statusFilter === st ? 'bg-indigo-600 text-white shadow-sm' : 'bg-slate-800 text-slate-400 hover:text-slate-200 hover:bg-slate-700'}`}
                      >
                        {st}
                      </button>
                    ))}
                  </div>
                </div>

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
            {activeTab === 'websub' && <WebSubPipelineView pipelineInfo={data?.pipelines?.websub} events={data?.events || []} />}
            {activeTab === 'notifications' && <NotificationsPipelineView notificationsPayload={notifications} subscriptions={subscriptions?.items || []} />}
            {activeTab === 'analytics' && <AnalyticsPipelineView events={analyticsEvents} isLoading={isLoading} />}
          </main>
        </div>
      </div>
    </div>
  );
}
