'use client';

import React, { useState, useMemo } from 'react';
import { UserAnalyticsEvent } from '../../types';
import { formatEventNarrative } from './SessionJourneyExplorer';
import {
  Filter,
  Copy,
  Check,
  ChevronDown,
  ChevronUp,
  Smartphone,
  Globe,
} from 'lucide-react';

interface ActivityNarrativeStreamProps {
  events: UserAnalyticsEvent[];
  isLoading?: boolean;
}

const CATEGORY_FILTERS = [
  { id: 'ALL', label: 'All Activities' },
  { id: 'screen_view', label: 'Screen Views' },
  { id: 'match_click', label: 'Match Clicks' },
  { id: 'video_play', label: 'Highlight Plays' },
  { id: 'watch_on_youtube', label: 'YouTube Ext' },
  { id: 'competition_filter', label: 'Filters' },
  { id: 'club_favorite', label: 'Favorites' },
  { id: 'session_start', label: 'Sessions' },
];

export function ActivityNarrativeStream({ events, isLoading }: ActivityNarrativeStreamProps) {
  const [filter, setFilter] = useState<string>('ALL');
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const [copiedId, setCopiedId] = useState<number | null>(null);

  const filteredEvents = useMemo(() => {
    if (filter === 'ALL') return events;
    return events.filter((e) => e.event_name === filter);
  }, [events, filter]);

  const handleCopy = (ev: UserAnalyticsEvent) => {
    navigator.clipboard.writeText(JSON.stringify(ev, null, 2));
    setCopiedId(ev.id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  return (
    <div className="space-y-4">
      {/* Filter Chips */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1">
        <Filter className="w-3.5 h-3.5 text-slate-400 shrink-0 ml-1" />
        {CATEGORY_FILTERS.map((f) => (
          <button
            key={f.id}
            onClick={() => setFilter(f.id)}
            className={`px-3 py-1.5 rounded-lg text-xs font-mono font-semibold transition shrink-0 ${
              filter === f.id
                ? 'bg-emerald-600 text-white shadow-sm'
                : 'bg-slate-900 text-slate-400 hover:text-slate-200 hover:bg-slate-800 border border-slate-800'
            }`}
          >
            {f.label}
          </button>
        ))}
      </div>

      {/* Stream List */}
      <div className="space-y-2.5">
        {isLoading ? (
          <div className="p-12 text-center text-slate-500 font-mono text-sm bg-slate-900/40 rounded-xl border border-slate-800">
            Streaming behavioral activities...
          </div>
        ) : filteredEvents.length === 0 ? (
          <div className="p-12 text-center text-slate-500 font-mono text-sm bg-slate-900/40 rounded-xl border border-slate-800">
            No activities matching selected category.
          </div>
        ) : (
          filteredEvents.map((ev) => {
            const { title, subtitle, Icon, color } = formatEventNarrative(ev);
            const isExpanded = expandedId === ev.id;
            const isAndroid = ev.platform === 'android';

            return (
              <div
                key={ev.id}
                className="bg-slate-900/80 border border-slate-800 hover:border-slate-700/80 rounded-xl p-3.5 transition flex flex-col gap-2"
              >
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-start gap-3">
                    <span className={`p-2 rounded-lg border mt-0.5 ${color}`}>
                      <Icon className="w-4 h-4" />
                    </span>

                    <div className="space-y-0.5">
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-bold text-slate-100">{title}</span>
                        <span
                          className={`px-1.5 py-0.2 rounded text-[9px] font-mono font-bold uppercase tracking-wider ${
                            isAndroid ? 'bg-cyan-500/20 text-cyan-400' : 'bg-indigo-500/20 text-indigo-400'
                          }`}
                        >
                          {ev.platform}
                        </span>
                      </div>
                      {subtitle && <p className="text-[11px] font-mono text-slate-400">{subtitle}</p>}
                      <p className="text-[10px] font-mono text-slate-500">
                        Device: {ev.device_id ? `${ev.device_id.slice(0, 16)}...` : 'anonymous'}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-1.5 shrink-0">
                    <span className="text-[11px] font-mono text-slate-400 mr-1">
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
                      title="Copy JSON Payload"
                      className="p-1 rounded-md text-slate-400 hover:text-slate-200 hover:bg-slate-800 border border-slate-800"
                    >
                      {copiedId === ev.id ? <Check className="w-3 h-3 text-emerald-400" /> : <Copy className="w-3 h-3" />}
                    </button>

                    <button
                      onClick={() => setExpandedId(isExpanded ? null : ev.id)}
                      className="p-1 rounded-md text-slate-400 hover:text-slate-200 hover:bg-slate-800 border border-slate-800"
                    >
                      {isExpanded ? <ChevronUp className="w-3 h-3" /> : <ChevronDown className="w-3 h-3" />}
                    </button>
                  </div>
                </div>

                {isExpanded && (
                  <div className="mt-1 pt-2 border-t border-slate-800/80 bg-slate-950/60 p-2.5 rounded-lg font-mono text-[11px] text-slate-300 overflow-x-auto">
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
