'use client';

import React, { useMemo, useState, useEffect } from 'react';
import {
  BarChart3,
  Smartphone,
  Globe,
  Users,
  EyeOff,
  Eye,
  ShieldCheck,
} from 'lucide-react';
import { UserAnalyticsEvent, AppSessionItem } from '../types';
import { AnalyticsFunnel } from './analytics/AnalyticsFunnel';
import { SessionJourneyExplorer } from './analytics/SessionJourneyExplorer';
import { ActivityNarrativeStream } from './analytics/ActivityNarrativeStream';
import { DeviceLocationTable } from './analytics/DeviceLocationTable';

interface AnalyticsPipelineViewProps {
  events: UserAnalyticsEvent[];
  appSessions?: AppSessionItem[];
  totalAnalyticsCount?: number;
  totalSessionsCount?: number;
  isLoading?: boolean;
}

export function AnalyticsPipelineView({
  events,
  appSessions = [],
  totalAnalyticsCount = 0,
  totalSessionsCount = 0,
  isLoading = false,
}: AnalyticsPipelineViewProps) {
  const [platformTab, setPlatformTab] = useState<'all' | 'android' | 'web'>('all');
  const [excludeMyDevice, setExcludeMyDevice] = useState(false);
  const [myDeviceId, setMyDeviceId] = useState<string | null>(null);

  useEffect(() => {
    if (typeof window !== 'undefined') {
      const devId = localStorage.getItem('fc_device_id') || localStorage.getItem('fc-telemetry-device-id');
      if (devId) setMyDeviceId(devId);
    }
  }, []);

  // Filter out developer's own sessions if toggle is enabled
  const effectiveSessions = useMemo(() => {
    if (!excludeMyDevice || !myDeviceId) return appSessions;
    return appSessions.filter((s) => s.device_id !== myDeviceId);
  }, [appSessions, excludeMyDevice, myDeviceId]);

  const effectiveEvents = useMemo(() => {
    if (!excludeMyDevice || !myDeviceId) return events;
    return events.filter((e) => e.device_id !== myDeviceId);
  }, [events, excludeMyDevice, myDeviceId]);

  const filteredEvents = useMemo(() => {
    if (platformTab === 'android') return effectiveEvents.filter((e) => e.platform === 'android');
    if (platformTab === 'web') return effectiveEvents.filter((e) => e.platform === 'web');
    return effectiveEvents;
  }, [effectiveEvents, platformTab]);

  const filteredSessions = useMemo(() => {
    if (platformTab === 'web') return [];
    return effectiveSessions;
  }, [effectiveSessions, platformTab]);

  const stats = useMemo(() => {
    const realAndroidTotal = Math.max(totalSessionsCount, appSessions.length);
    const realWebTotal = Math.max(totalAnalyticsCount, events.length);
    const totalAggregate = realAndroidTotal + realWebTotal;

    const allDeviceIds = new Set<string>();
    effectiveSessions.forEach((s) => s.device_id && allDeviceIds.add(s.device_id));
    effectiveEvents.forEach((e) => (e.device_id || e.user_id) && allDeviceIds.add(e.device_id || e.user_id!));

    const versionCounts = new Map<string, number>();
    for (const s of effectiveSessions) {
      const v = s.app_version || '3.0.1';
      versionCounts.set(v, (versionCounts.get(v) || 0) + 1);
    }
    const topVersions = Array.from(versionCounts.entries())
      .map(([version, count]) => ({ version, count }))
      .sort((a, b) => b.count - a.count);

    return {
      realAndroidTotal,
      realWebTotal,
      totalAggregate,
      distinctDevices: allDeviceIds.size,
      topVersions,
    };
  }, [effectiveSessions, effectiveEvents, totalSessionsCount, totalAnalyticsCount, appSessions.length, events.length]);

  return (
    <div className="space-y-5">
      {/* High-Density Header Banner & Controls */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-4 sm:p-5 space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-slate-800 pb-3">
          {/* Sub-Tabs: All vs Android vs Web */}
          <div className="flex items-center gap-1.5 overflow-x-auto">
            <button
              onClick={() => setPlatformTab('all')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-mono font-bold transition shrink-0 ${
                platformTab === 'all'
                  ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/40 shadow-sm'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60 border border-transparent'
              }`}
            >
              <BarChart3 className="w-3.5 h-3.5" />
              <span>All ({stats.totalAggregate})</span>
            </button>

            <button
              onClick={() => setPlatformTab('android')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-mono font-bold transition shrink-0 ${
                platformTab === 'android'
                  ? 'bg-cyan-500/20 text-cyan-400 border border-cyan-500/40 shadow-sm'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60 border border-transparent'
              }`}
            >
              <Smartphone className="w-3.5 h-3.5" />
              <span>Android ({stats.realAndroidTotal})</span>
            </button>

            <button
              onClick={() => setPlatformTab('web')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-mono font-bold transition shrink-0 ${
                platformTab === 'web'
                  ? 'bg-indigo-500/20 text-indigo-400 border border-indigo-500/40 shadow-sm'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60 border border-transparent'
              }`}
            >
              <Globe className="w-3.5 h-3.5" />
              <span>Web ({stats.realWebTotal})</span>
            </button>
          </div>

          {/* Exclude My Device Toggle */}
          <button
            onClick={() => setExcludeMyDevice((prev) => !prev)}
            className={`inline-flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-mono font-semibold transition border shrink-0 ${
              excludeMyDevice
                ? 'bg-amber-500/20 text-amber-300 border-amber-500/40'
                : 'bg-slate-800/80 text-slate-300 hover:bg-slate-800 border-slate-700'
            }`}
            title={myDeviceId ? `Current Device: ${myDeviceId}` : 'Auto-detected browser device'}
          >
            {excludeMyDevice ? <EyeOff className="w-3.5 h-3.5 text-amber-400" /> : <Eye className="w-3.5 h-3.5 text-slate-400" />}
            <span>{excludeMyDevice ? 'Excluding You (External Only)' : 'Exclude My Device'}</span>
          </button>
        </div>

        {/* Compact 4-Metric Grid */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 sm:gap-4">
          <div className="bg-slate-950/60 border border-slate-800/80 rounded-xl p-3">
            <p className="text-[10px] font-mono text-slate-400 uppercase tracking-wider">Total Activity Events</p>
            <div className="flex items-center gap-1.5 mt-1">
              <BarChart3 className="w-4 h-4 text-emerald-400" />
              <p className="text-base font-bold font-mono text-slate-100">{stats.totalAggregate}</p>
            </div>
          </div>

          <div className="bg-slate-950/60 border border-slate-800/80 rounded-xl p-3">
            <p className="text-[10px] font-mono text-slate-400 uppercase tracking-wider">Android Sessions</p>
            <div className="flex items-center gap-1.5 mt-1">
              <Smartphone className="w-4 h-4 text-cyan-400" />
              <p className="text-base font-bold font-mono text-cyan-400">{stats.realAndroidTotal}</p>
            </div>
          </div>

          <div className="bg-slate-950/60 border border-slate-800/80 rounded-xl p-3">
            <p className="text-[10px] font-mono text-slate-400 uppercase tracking-wider">Web Portal Actions</p>
            <div className="flex items-center gap-1.5 mt-1">
              <Globe className="w-4 h-4 text-indigo-400" />
              <p className="text-base font-bold font-mono text-indigo-400">{stats.realWebTotal}</p>
            </div>
          </div>

          <div className="bg-slate-950/60 border border-slate-800/80 rounded-xl p-3">
            <p className="text-[10px] font-mono text-slate-400 uppercase tracking-wider">Distinct Devices</p>
            <div className="flex items-center gap-1.5 mt-1">
              <Users className="w-4 h-4 text-purple-400" />
              <p className="text-base font-bold font-mono text-purple-400">{stats.distinctDevices}</p>
            </div>
          </div>
        </div>

        {/* Active Versions Inline Chips */}
        {stats.topVersions.length > 0 && (
          <div className="flex flex-wrap items-center gap-2 pt-1">
            <span className="text-[11px] font-mono text-slate-400">Android Versions:</span>
            {stats.topVersions.slice(0, 5).map(({ version, count }) => (
              <span
                key={version}
                className="inline-flex items-center gap-1 px-2.5 py-0.5 bg-cyan-950/40 text-cyan-300 border border-cyan-800/60 rounded-md text-[11px] font-mono"
              >
                <span>v{version}</span>
                <span className="text-cyan-500 font-bold">({count})</span>
              </span>
            ))}
          </div>
        )}
      </div>

      {/* Device Models & Geographic Breakdown Table */}
      <DeviceLocationTable
        appSessions={filteredSessions}
        events={filteredEvents}
        myDeviceId={myDeviceId}
      />

      {/* Behavioral Conversion Funnel */}
      <AnalyticsFunnel events={filteredEvents} appSessions={filteredSessions} />

      {/* Session Journey Explorer */}
      <SessionJourneyExplorer events={filteredEvents} appSessions={filteredSessions} />

      {/* Live Behavioral Activity Feed */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h4 className="text-xs font-bold font-mono text-slate-100 uppercase tracking-wider">
            Live Behavioral Activity Feed
          </h4>
          <span className="text-xs font-mono text-slate-500">
            {filteredEvents.length} logged actions
          </span>
        </div>
        <ActivityNarrativeStream events={filteredEvents} isLoading={isLoading} />
      </div>
    </div>
  );
}
