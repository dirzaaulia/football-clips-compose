'use client';

import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { Search, Filter } from 'lucide-react';
import { database } from '@/lib/database';
import {
  TelemetryPayload,
  MatchWaterfall,
  NotificationsPayload,
  SubscriptionsPayload,
  TelemetryTab,
  UserAnalyticsEvent,
} from './types';
import { buildMatchWaterfalls } from './mapper';
import { TelemetryHeader } from './components/TelemetryHeader';
import { QuotaGaugeCard } from './components/QuotaGaugeCard';
import { WaterfallCard } from './components/WaterfallCard';
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
  const [autoRefreshInterval, setAutoRefreshInterval] = useState(15);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'LINKED' | 'COOLDOWN' | 'UNLINKED'>('ALL');

  const fetchTelemetry = useCallback(async () => {
    setIsRefreshing(true);
    try {
      const { data: rows, error } = await database
        .from('telemetry_state')
        .select('key, payload')
        .in('key', ['telemetry', 'notifications', 'subscriptions']);

      if (!error && rows) {
        for (const row of rows) {
          if (row.key === 'telemetry') {
            setData(row.payload as TelemetryPayload);
          } else if (row.key === 'notifications') {
            setNotifications(row.payload as NotificationsPayload);
          } else if (row.key === 'subscriptions') {
            setSubscriptions(row.payload as SubscriptionsPayload);
          }
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

  const waterfalls = useMemo(() => {
    if (!data?.events) return [];
    return buildMatchWaterfalls(data.events);
  }, [data]);

  const filteredWaterfalls = useMemo(() => {
    return waterfalls.filter((wf: MatchWaterfall) => {
      const matchesSearch =
        !searchQuery ||
        wf.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        wf.matchId.includes(searchQuery);
      const matchesStatus = statusFilter === 'ALL' || wf.status === statusFilter;
      return matchesSearch && matchesStatus;
    });
  }, [waterfalls, searchQuery, statusFilter]);

  const unlinkedCount = useMemo(() => {
    return waterfalls.filter((w) => w.status === 'UNLINKED' || w.status === 'COOLDOWN').length;
  }, [waterfalls]);

  const liveMatchesCount = useMemo(() => {
    if (!data?.events) return 0;
    const liveSet = new Set<string>();
    for (const ev of data.events) {
      if (
        ev.attributes &&
        ev.attributes.status &&
        ['IN_PLAY', 'PAUSED', 'LIVE'].includes(String(ev.attributes.status))
      ) {
        const id = String(ev.match_id || ev.attributes.id || '');
        if (id) liveSet.add(id);
      }
    }
    return liveSet.size;
  }, [data]);

  const handleExportJson = () => {
    const fullState = { telemetry: data, notifications, subscriptions };
    navigator.clipboard.writeText(JSON.stringify(fullState, null, 2));
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 font-sans p-4 sm:p-6 lg:p-8">
      <div className="max-w-7xl 2xl:max-w-[1440px] mx-auto space-y-6">
        <TelemetryHeader
          onRefresh={fetchTelemetry}
          isRefreshing={isRefreshing}
          autoRefreshInterval={autoRefreshInterval}
          setAutoRefreshInterval={setAutoRefreshInterval}
          lastUpdated={data?.updated_at ? new Date(data.updated_at).toLocaleTimeString() : undefined}
          onExportJson={handleExportJson}
        />

        {data?.quota && <QuotaGaugeCard quota={data.quota} />}

        <div className="flex flex-col lg:flex-row items-start gap-6">
          {/* Left Navigation Console Rail */}
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

          {/* Right Active View Deck */}
          <main className="flex-1 min-w-0 w-full space-y-5">
            {activeTab === 'highlights' && (
              <>
                <PipelineFlowGraph
                  todayQuotaUsage={data?.quota?.hits_today ?? 0}
                  onNodeClick={(key) => setSearchQuery((prev) => (prev === key ? '' : key))}
                />

                {/* Filter & Search Bar */}
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
                    <Filter className="w-3.5 h-3.5 text-slate-400 shrink-0 ml-1" />
                    {(['ALL', 'LINKED', 'COOLDOWN', 'UNLINKED'] as const).map((st) => (
                      <button
                        key={st}
                        onClick={() => setStatusFilter(st)}
                        className={`px-3 py-1.5 rounded-lg text-xs font-mono font-semibold tracking-wide transition shrink-0 ${
                          statusFilter === st
                            ? 'bg-indigo-600 text-white shadow-sm'
                            : 'bg-slate-800 text-slate-400 hover:text-slate-200 hover:bg-slate-700'
                        }`}
                      >
                        {st}
                      </button>
                    ))}
                  </div>
                </div>

                {/* Waterfalls List */}
                <div className="space-y-3">
                  <div className="flex items-center justify-between text-xs text-slate-400 px-1 font-mono">
                    <span>
                      Diagnosed Match Waterfall Traces ({filteredWaterfalls.length} of {waterfalls.length})
                    </span>
                    <span>Click any match card to expand verification steps</span>
                  </div>

                  {isLoading ? (
                    <div className="p-12 text-center text-slate-500 font-mono text-sm bg-slate-900/40 rounded-xl border border-slate-800">
                      Loading real-time telemetry stream...
                    </div>
                  ) : filteredWaterfalls.length === 0 ? (
                    <div className="p-12 text-center text-slate-500 font-mono text-sm bg-slate-900/40 rounded-xl border border-slate-800">
                      No matching match waterfalls found for current search/filter.
                    </div>
                  ) : (
                    filteredWaterfalls.map((wf: MatchWaterfall) => (
                      <WaterfallCard
                        key={wf.matchId}
                        waterfall={wf}
                        onOverrideSuccess={fetchTelemetry}
                      />
                    ))
                  )}
                </div>
              </>
            )}

            {activeTab === 'fixtures' && (
              <LiveScoresPipelineView
                pipelineInfo={data?.pipelines?.fixtures}
                events={data?.events || []}
              />
            )}

            {activeTab === 'websub' && (
              <WebSubPipelineView
                pipelineInfo={data?.pipelines?.websub}
                events={data?.events || []}
              />
            )}

            {activeTab === 'notifications' && (
              <NotificationsPipelineView
                notificationsPayload={notifications}
                subscriptions={subscriptions?.items || []}
              />
            )}

            {activeTab === 'analytics' && (
              <AnalyticsPipelineView
                events={analyticsEvents}
                isLoading={isLoading}
              />
            )}
          </main>
        </div>
      </div>
    </div>
  );
}
