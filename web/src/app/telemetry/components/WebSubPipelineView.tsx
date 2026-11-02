import React, { useState, useMemo } from 'react';
import {
  Rss,
  Zap,
  RefreshCw,
  CheckCircle,
  Copy,
  Check,
  Radio,
} from 'lucide-react';
import { PipelineInfo, TelemetryEvent } from '../types';

interface WebSubPipelineViewProps {
  pipelineInfo?: PipelineInfo;
  events: TelemetryEvent[];
}

export function WebSubPipelineView({
  pipelineInfo,
  events,
}: WebSubPipelineViewProps) {
  const [filterType, setFilterType] = useState<string>('ALL');
  const [copiedId, setCopiedId] = useState<string | null>(null);
  const [isRenewing, setIsRenewing] = useState(false);
  const [renewMessage, setRenewMessage] = useState<string | null>(null);

  const websubEvents = useMemo(() => {
    return events.filter((ev) => {
      const p = (ev.pipeline || '').toLowerCase();
      const txt = (ev.event_text || '').toLowerCase();
      return p.includes('websub') || txt.includes('websub') || txt.includes('hub.challenge') || txt.includes('push received');
    });
  }, [events]);

  const filteredEvents = useMemo(() => {
    if (filterType === 'ALL') return websubEvents;
    return websubEvents.filter((ev) => (ev.event_text || '').toUpperCase().includes(filterType));
  }, [websubEvents, filterType]);

  const handleRenewSubscriptions = async () => {
    setIsRenewing(true);
    setRenewMessage(null);
    try {
      const res = await fetch('https://fc-worker-519310318914.us-central1.run.app/websub/renew', {
        method: 'POST',
      });
      if (res.ok) {
        setRenewMessage('WebSub renewal dispatched successfully for all channels!');
      } else {
        setRenewMessage('Renewal request returned non-200 status.');
      }
    } catch (e) {
      setRenewMessage(`Renewal failed: ${e instanceof Error ? e.message : 'Network error'}`);
    } finally {
      setIsRenewing(false);
      setTimeout(() => setRenewMessage(null), 5000);
    }
  };

  const handleCopyEvent = (ev: TelemetryEvent, idx: number) => {
    const key = `ws-${idx}-${ev.id || ''}`;
    navigator.clipboard.writeText(JSON.stringify(ev, null, 2));
    setCopiedId(key);
    setTimeout(() => setCopiedId(null), 2000);
  };

  return (
    <div className="space-y-5">
      {/* Gateway Status Banner */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-cyan-400 animate-ping" />
              <h3 className="text-sm font-bold text-slate-100 uppercase tracking-wide font-mono">
                YouTube WebSub Instant Push Gateway
              </h3>
            </div>
            <p className="text-xs text-slate-400">
              Direct Google PubSubHubbub receiver delivering highlights within 100ms of upload.
            </p>
          </div>

          <button
            onClick={handleRenewSubscriptions}
            disabled={isRenewing}
            className="inline-flex items-center gap-2 px-3.5 py-2 bg-cyan-600/20 text-cyan-400 hover:bg-cyan-600/30 border border-cyan-500/30 rounded-xl text-xs font-semibold font-mono transition shrink-0 disabled:opacity-50"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isRenewing ? 'animate-spin' : ''}`} />
            <span>{isRenewing ? 'Renewing...' : 'Renew All 96 Channel Leases'}</span>
          </button>
        </div>

        {renewMessage && (
          <div className="p-3 bg-cyan-950/40 border border-cyan-800/60 rounded-xl text-xs font-mono text-cyan-300">
            {renewMessage}
          </div>
        )}

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 pt-2 border-t border-slate-800">
          <div>
            <p className="text-[11px] font-mono text-slate-400 uppercase tracking-wider">Gateway Status</p>
            <p className="text-sm font-bold text-emerald-400 mt-1">{pipelineInfo?.status || 'Online'}</p>
          </div>
          <div>
            <p className="text-[11px] font-mono text-slate-400 uppercase tracking-wider">Lease Cadence</p>
            <p className="text-sm font-bold text-slate-200 mt-1">{pipelineInfo?.cadence || '72 Hours'}</p>
          </div>
          <div>
            <p className="text-[11px] font-mono text-slate-400 uppercase tracking-wider">Tracked Channels</p>
            <p className="text-sm font-bold text-cyan-400 mt-1">96 Club & League Hubs</p>
          </div>
          <div>
            <p className="text-[11px] font-mono text-slate-400 uppercase tracking-wider">Pushes Logged</p>
            <p className="text-sm font-bold text-indigo-400 mt-1">{websubEvents.length} Events</p>
          </div>
        </div>
      </div>

      {/* Filter Chips */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1">
        {['ALL', 'PUSH', 'LINKED', 'RENEWAL'].map((ft) => (
          <button
            key={ft}
            onClick={() => setFilterType(ft)}
            className={`px-3 py-1.5 rounded-lg text-xs font-mono font-semibold transition shrink-0 ${
              filterType === ft
                ? 'bg-cyan-600 text-white shadow-sm'
                : 'bg-slate-900 text-slate-400 hover:text-slate-200 hover:bg-slate-800 border border-slate-800'
            }`}
          >
            {ft}
          </button>
        ))}
      </div>

      {/* Event Stream */}
      <div className="space-y-2">
        {filteredEvents.length === 0 ? (
          <div className="p-12 text-center text-slate-500 font-mono text-sm bg-slate-900/40 rounded-xl border border-slate-800">
            No WebSub push events recorded in the current buffer.
          </div>
        ) : (
          filteredEvents.map((ev, idx) => {
            const key = `ws-${idx}-${ev.id || ''}`;
            const txt = ev.event_text || '';
            const isPush = txt.toLowerCase().includes('push') || txt.toLowerCase().includes('received');
            const isLink = txt.toLowerCase().includes('link');

            return (
              <div
                key={key}
                className="bg-slate-900/80 border border-slate-800 rounded-xl p-3.5 flex items-start justify-between gap-3 text-xs font-mono hover:bg-slate-800/40 transition"
              >
                <div className="flex items-start gap-2.5 min-w-0">
                  <div className="mt-0.5 shrink-0">
                    {isLink ? (
                      <CheckCircle className="w-4 h-4 text-emerald-400" />
                    ) : isPush ? (
                      <Zap className="w-4 h-4 text-cyan-400" />
                    ) : (
                      <Rss className="w-4 h-4 text-slate-400" />
                    )}
                  </div>
                  <div className="space-y-0.5 min-w-0">
                    <p className="text-slate-200 leading-relaxed break-words">{txt}</p>
                    {ev.created_at && (
                      <p className="text-[10px] text-slate-500">
                        {new Date(ev.created_at).toLocaleTimeString()}
                      </p>
                    )}
                  </div>
                </div>

                <button
                  onClick={() => handleCopyEvent(ev, idx)}
                  title="Copy event JSON"
                  className="p-1.5 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800 shrink-0 border border-slate-800"
                >
                  {copiedId === key ? (
                    <Check className="w-3.5 h-3.5 text-emerald-400" />
                  ) : (
                    <Copy className="w-3.5 h-3.5" />
                  )}
                </button>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
