'use client';

import React, { useState, useMemo } from 'react';
import {
  BarChart3,
  Smartphone,
  Globe,
  Users,
  Copy,
  Check,
  ChevronDown,
  ChevronUp,
  Filter,
} from 'lucide-react';
import { UserAnalyticsEvent } from '../types';

interface AnalyticsPipelineViewProps {
  events: UserAnalyticsEvent[];
  isLoading?: boolean;
}

export function AnalyticsPipelineView({
  events,
  isLoading = false,
}: AnalyticsPipelineViewProps) {
  const [filterType, setFilterType] = useState<string>('ALL');
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const [copiedId, setCopiedId] = useState<number | null>(null);

  const stats = useMemo(() => {
    const total = events.length;
    const android = events.filter((e) => e.platform === 'android').length;
    const web = events.filter((e) => e.platform === 'web').length;
    const distinctUsers = new Set(events.map((e) => e.user_id).filter(Boolean)).size;
    return { total, android, web, distinctUsers };
  }, [events]);

  const eventTypes = useMemo(() => {
    const types = Array.from(new Set(events.map((e) => e.event_name)));
    return ['ALL', ...types];
  }, [events]);

  const filteredEvents = useMemo(() => {
    if (filterType === 'ALL') return events;
    return events.filter((e) => e.event_name === filterType);
  }, [events, filterType]);

  const handleCopy = (ev: UserAnalyticsEvent) => {
    navigator.clipboard.writeText(JSON.stringify(ev, null, 2));
    setCopiedId(ev.id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  return (
    <div className="space-y-5">
      {/* Metrics Banner */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div>
          <p className="text-[11px] font-mono text-slate-400 uppercase tracking-wider">Total Events</p>
          <div className="flex items-center gap-1.5 mt-1">
            <BarChart3 className="w-4 h-4 text-emerald-400" />
            <p className="text-sm font-bold text-slate-100">{stats.total}</p>
          </div>
        </div>
        <div>
          <p className="text-[11px] font-mono text-slate-400 uppercase tracking-wider">Android App</p>
          <div className="flex items-center gap-1.5 mt-1">
            <Smartphone className="w-4 h-4 text-cyan-400" />
            <p className="text-sm font-bold text-cyan-400">{stats.android}</p>
          </div>
        </div>
        <div>
          <p className="text-[11px] font-mono text-slate-400 uppercase tracking-wider">Web Portal</p>
          <div className="flex items-center gap-1.5 mt-1">
            <Globe className="w-4 h-4 text-indigo-400" />
            <p className="text-sm font-bold text-indigo-400">{stats.web}</p>
          </div>
        </div>
        <div>
          <p className="text-[11px] font-mono text-slate-400 uppercase tracking-wider">Distinct Users</p>
          <div className="flex items-center gap-1.5 mt-1">
            <Users className="w-4 h-4 text-purple-400" />
            <p className="text-sm font-bold text-purple-400">{stats.distinctUsers}</p>
          </div>
        </div>
      </div>

      {/* Filter Chips */}
      {eventTypes.length > 1 && (
        <div className="flex items-center gap-2 overflow-x-auto pb-1">
          <Filter className="w-3.5 h-3.5 text-slate-400 shrink-0 ml-1" />
          {eventTypes.map((t) => (
            <button
              key={t}
              onClick={() => setFilterType(t)}
              className={`px-3 py-1.5 rounded-lg text-xs font-mono font-semibold transition shrink-0 ${
                filterType === t
                  ? 'bg-emerald-600 text-white shadow-sm'
                  : 'bg-slate-900 text-slate-400 hover:text-slate-200 hover:bg-slate-800 border border-slate-800'
              }`}
            >
              {t}
            </button>
          ))}
        </div>
      )}

      {/* Event Stream List */}
      <div className="space-y-3">
        {isLoading ? (
          <div className="p-12 text-center text-slate-500 font-mono text-sm bg-slate-900/40 rounded-xl border border-slate-800">
            Loading analytics events...
          </div>
        ) : filteredEvents.length === 0 ? (
          <div className="p-12 text-center text-slate-500 font-mono text-sm bg-slate-900/40 rounded-xl border border-slate-800">
            No analytics events logged yet. Real user actions (screen views, club follows, video plays) will stream here.
          </div>
        ) : (
          filteredEvents.map((ev) => {
            const isExpanded = expandedId === ev.id;
            const isAndroid = ev.platform === 'android';

            return (
              <div
                key={ev.id}
                className="bg-slate-900/80 border border-slate-800 rounded-xl p-4 hover:border-slate-700 transition"
              >
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div className="flex items-center gap-2.5">
                    <span
                      className={`px-2 py-0.5 rounded font-mono text-[10px] font-bold uppercase tracking-wider ${
                        isAndroid
                          ? 'bg-cyan-500/20 text-cyan-400 border border-cyan-500/30'
                          : 'bg-indigo-500/20 text-indigo-400 border border-indigo-500/30'
                      }`}
                    >
                      {ev.platform}
                    </span>

                    <span className="font-mono text-xs font-bold text-white bg-slate-800 px-2.5 py-0.5 rounded border border-slate-700">
                      {ev.event_name}
                    </span>

                    {ev.user_id && (
                      <span className="text-[11px] font-mono text-slate-400 truncate max-w-[120px]">
                        user:{ev.user_id}
                      </span>
                    )}
                  </div>

                  <div className="flex items-center gap-2 text-xs font-mono text-slate-400">
                    <span>
                      {new Date(ev.created_at).toLocaleTimeString('id-ID', {
                        timeZone: 'Asia/Jakarta',
                        hour: '2-digit',
                        minute: '2-digit',
                        second: '2-digit',
                      })}{' '}
                      WIB
                    </span>

                    <button
                      onClick={() => handleCopy(ev)}
                      title="Copy JSON"
                      className="p-1.5 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800 border border-slate-800"
                    >
                      {copiedId === ev.id ? (
                        <Check className="w-3.5 h-3.5 text-emerald-400" />
                      ) : (
                        <Copy className="w-3.5 h-3.5" />
                      )}
                    </button>

                    <button
                      onClick={() => setExpandedId(isExpanded ? null : ev.id)}
                      className="p-1.5 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800 border border-slate-800"
                    >
                      {isExpanded ? (
                        <ChevronUp className="w-3.5 h-3.5" />
                      ) : (
                        <ChevronDown className="w-3.5 h-3.5" />
                      )}
                    </button>
                  </div>
                </div>

                {isExpanded && (
                  <div className="mt-3 pt-3 border-t border-slate-800/80 bg-slate-950/40 p-3 rounded-lg font-mono text-[11px] text-slate-300 overflow-x-auto">
                    <pre>{JSON.stringify(ev.payload, null, 2)}</pre>
                  </div>
                )}
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
