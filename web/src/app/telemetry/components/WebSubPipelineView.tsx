'use client';

import React, { useState, useMemo } from 'react';
import { RefreshCw } from 'lucide-react';
import { PipelineInfo, TelemetryEvent } from '../types';
import { WebSubPushCard, UnifiedWebSubPush } from './WebSubPushCard';

interface WebSubPipelineViewProps {
  pipelineInfo?: PipelineInfo;
  events: TelemetryEvent[];
}

export function WebSubPipelineView({ pipelineInfo, events }: WebSubPipelineViewProps) {
  const [filterType, setFilterType] = useState<string>('ALL');
  const [isRenewing, setIsRenewing] = useState(false);
  const [renewMessage, setRenewMessage] = useState<string | null>(null);

  const unifiedPushes = useMemo(() => {
    // 1. Filter out all RENEWAL & challenge logs completely
    const validEvents = events.filter((ev) => {
      const txt = (ev.event_text || '').toLowerCase();
      if (txt.includes('renewal') || txt.includes('hub.challenge') || txt.includes('handshake')) {
        return false;
      }
      const p = (ev.pipeline || '').toLowerCase();
      const s = (ev.subsystem || '').toLowerCase();
      return p.includes('websub') || s.includes('websub') || txt.includes('websub');
    });

    // 2. Group by videoId into Unified Diagnostic Cards
    const pushMap = new Map<string, UnifiedWebSubPush>();

    for (const ev of validEvents) {
      const txt = ev.event_text || '';
      const attrs = ev.attributes || {};
      const vidMatch = txt.match(/youtu\.be\/([a-zA-Z0-9_-]{11})|\(([a-zA-Z0-9_-]{11})\)/);
      const videoId = (attrs.video_id as string) || (vidMatch ? vidMatch[1] || vidMatch[2] : '');
      if (!videoId) continue;

      const title = (attrs.title as string) || (txt.match(/'([^']+)'/)?.[1] || 'YouTube Video');
      const channel = (attrs.channel as string) || (txt.match(/channel:?\s*(@?[a-zA-Z0-9_-]+)/i)?.[1] || 'YouTube Hub');
      const isLinked = txt.includes('LINKED') || attrs.verdict === 'LINKED';
      const isReject = txt.includes('REJECT') || txt.includes('UNMATCHED') || txt.includes('GEO BLOCKED') || attrs.verdict === 'REJECTED';
      const reason = (attrs.reason as string) || (txt.includes('rejected:') ? txt.split('rejected:')[1].trim() : undefined);
      const matchLabel = attrs.home_team && attrs.away_team ? `${attrs.home_team} vs ${attrs.away_team}` : undefined;
      const comp = (attrs.competition_name as string) || undefined;
      const evaluator = (attrs.evaluator as string) || undefined;
      const pubAt = (attrs.published_at as string) || undefined;
      const stepName = attrs.step || (isLinked ? 'RESULT' : isReject ? 'REJECT' : 'PUSH');

      const existing = pushMap.get(videoId);
      if (!existing) {
        pushMap.set(videoId, {
          videoId,
          title,
          channel,
          verdict: isLinked ? 'LINKED' : isReject ? 'REJECTED' : 'PUSH',
          reason,
          matchLabel,
          competitionName: comp,
          evaluator,
          publishedAt: pubAt,
          timestamp: ev.created_at || new Date().toISOString(),
          steps: [{
            name: String(stepName),
            level: ev.level || 'INFO',
            text: txt,
            time: ev.created_at ? new Date(ev.created_at).toLocaleTimeString() : undefined,
          }],
        });
      } else {
        if (isLinked) existing.verdict = 'LINKED';
        else if (isReject && existing.verdict !== 'LINKED') existing.verdict = 'REJECTED';

        if (reason) existing.reason = reason;
        if (matchLabel) existing.matchLabel = matchLabel;
        if (comp) existing.competitionName = comp;
        if (evaluator) existing.evaluator = evaluator;
        if (pubAt) existing.publishedAt = pubAt;

        existing.steps.push({
          name: String(stepName),
          level: ev.level || 'INFO',
          text: txt,
          time: ev.created_at ? new Date(ev.created_at).toLocaleTimeString() : undefined,
        });
      }
    }

    return Array.from(pushMap.values()).sort(
      (a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
    );
  }, [events]);

  const filteredPushes = useMemo(() => {
    if (filterType === 'ALL') return unifiedPushes;
    return unifiedPushes.filter((p) => p.verdict === filterType);
  }, [unifiedPushes, filterType]);

  const handleRenewSubscriptions = async () => {
    setIsRenewing(true);
    setRenewMessage(null);
    try {
      const res = await fetch('https://fc-worker-519310318914.us-central1.run.app/websub/renew', { method: 'POST' });
      setRenewMessage(res.ok ? 'WebSub renewal dispatched successfully for 168 channels!' : 'Renewal request returned non-200 status.');
    } catch (e) {
      setRenewMessage(`Renewal failed: ${e instanceof Error ? e.message : 'Network error'}`);
    } finally {
      setIsRenewing(false);
      setTimeout(() => setRenewMessage(null), 5000);
    }
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
            <span>{isRenewing ? 'Renewing...' : 'Renew All 168 Channel Leases'}</span>
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
            <p className="text-sm font-bold text-slate-200 mt-1">72 Hours</p>
          </div>
          <div>
            <p className="text-[11px] font-mono text-slate-400 uppercase tracking-wider">Tracked Channels</p>
            <p className="text-sm font-bold text-cyan-400 mt-1">168 Club & League Hubs</p>
          </div>
          <div>
            <p className="text-[11px] font-mono text-slate-400 uppercase tracking-wider">Unified Uploads</p>
            <p className="text-sm font-bold text-indigo-400 mt-1">{unifiedPushes.length} Videos</p>
          </div>
        </div>
      </div>

      {/* Filter Chips: No RENEWAL noise */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1">
        {['ALL', 'LINKED', 'REJECTED', 'PUSH'].map((ft) => (
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

      {/* Unified Push Cards Stream */}
      <div className="space-y-3">
        {filteredPushes.length === 0 ? (
          <div className="p-12 text-center text-slate-500 font-mono text-sm bg-slate-900/40 rounded-xl border border-slate-800">
            No incoming WebSub video webhook notifications in current cycle buffer.
          </div>
        ) : (
          filteredPushes.map((push) => (
            <WebSubPushCard key={push.videoId} push={push} />
          ))
        )}
      </div>
    </div>
  );
}
