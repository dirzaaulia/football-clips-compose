'use client';

import React, { useState } from 'react';
import {
  CheckCircle,
  XCircle,
  Zap,
  ExternalLink,
  ShieldCheck,
  ChevronDown,
  ChevronUp,
  Clock,
  Video,
  Radio,
} from 'lucide-react';

export interface UnifiedWebSubPush {
  videoId: string;
  title: string;
  channel: string;
  verdict: 'LINKED' | 'REJECTED' | 'PUSH';
  reason?: string;
  matchLabel?: string;
  competitionName?: string;
  evaluator?: string;
  publishedAt?: string;
  timestamp: string;
  steps: {
    name: string;
    level: string;
    text: string;
    time?: string;
  }[];
}

interface WebSubPushCardProps {
  push: UnifiedWebSubPush;
}

function formatWibDate(isoStr?: string): string {
  if (!isoStr) return '—';
  try {
    const d = new Date(isoStr);
    return d.toLocaleString('en-GB', {
      timeZone: 'Asia/Jakarta',
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    }) + ' WIB';
  } catch {
    return isoStr;
  }
}

export function WebSubPushCard({ push }: WebSubPushCardProps) {
  const [isExpanded, setIsExpanded] = useState(false);
  const isLink = push.verdict === 'LINKED';
  const isReject = push.verdict === 'REJECTED';

  return (
    <div className="bg-slate-900/90 border border-slate-800 rounded-xl overflow-hidden hover:border-slate-700 transition">
      {/* Top Header */}
      <div className="p-4 space-y-3">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <span
              className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold font-mono ${
                isLink
                  ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/30'
                  : isReject
                  ? 'bg-rose-500/15 text-rose-400 border border-rose-500/30'
                  : 'bg-cyan-500/15 text-cyan-400 border border-cyan-500/30'
              }`}
            >
              {isLink ? <CheckCircle className="w-3.5 h-3.5" /> : isReject ? <XCircle className="w-3.5 h-3.5" /> : <Zap className="w-3.5 h-3.5" />}
              {push.verdict}
            </span>

            {/* Official Channel Handle Badge */}
            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-slate-800 text-slate-200 border border-slate-700 text-xs font-mono font-semibold">
              <Video className="w-3.5 h-3.5 text-red-500" />
              <span>{push.channel.startsWith('@') ? push.channel : `@${push.channel}`}</span>
            </span>

            {push.evaluator && (
              <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-indigo-950/60 text-indigo-300 border border-indigo-800/40">
                {push.evaluator}
              </span>
            )}
          </div>

          <div className="flex items-center gap-1.5 text-slate-400 text-xs font-mono">
            <Clock className="w-3.5 h-3.5 text-slate-500" />
            <span>{formatWibDate(push.timestamp)}</span>
          </div>
        </div>

        {/* Video Title */}
        <div>
          <h4 className="text-sm font-bold text-white leading-snug">{push.title}</h4>
          {push.matchLabel && (
            <div className="flex items-center gap-1.5 mt-1.5 text-xs text-indigo-300 font-mono">
              <ShieldCheck className="w-3.5 h-3.5 text-indigo-400 shrink-0" />
              <span>Matched Fixture: {push.matchLabel}</span>
              {push.competitionName && <span className="text-slate-500 font-normal">({push.competitionName})</span>}
            </div>
          )}
          {push.reason && (
            <p className="text-xs text-rose-400/90 font-mono mt-1">Rejection Reason: {push.reason}</p>
          )}
        </div>

        {/* Footer Actions */}
        <div className="pt-2 border-t border-slate-800/80 flex items-center justify-between text-xs font-mono">
          <div className="flex items-center gap-2">
            <span className="text-slate-500">Video ID: {push.videoId}</span>
            {push.steps.length > 0 && (
              <button
                onClick={() => setIsExpanded((prev) => !prev)}
                className="inline-flex items-center gap-1 text-slate-400 hover:text-slate-200 py-0.5 px-1.5 rounded bg-slate-800/40 hover:bg-slate-800 transition"
              >
                <span>{isExpanded ? 'Hide Trace' : `Trace (${push.steps.length})`}</span>
                {isExpanded ? <ChevronUp className="w-3 h-3" /> : <ChevronDown className="w-3 h-3" />}
              </button>
            )}
          </div>

          {push.videoId && (
            <a
              href={`https://youtu.be/${push.videoId}`}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1 text-cyan-400 hover:text-cyan-300"
            >
              <span>Watch on YouTube</span>
              <ExternalLink className="w-3.5 h-3.5" />
            </a>
          )}
        </div>
      </div>

      {/* Expandable Step Trace */}
      {isExpanded && push.steps.length > 0 && (
        <div className="bg-slate-950/70 border-t border-slate-800/80 p-3 space-y-1.5 font-mono text-xs">
          <div className="text-[10px] uppercase text-slate-500 font-bold tracking-wider mb-2 flex items-center gap-1.5">
            <Radio className="w-3 h-3 text-cyan-400" />
            <span>Diagnostic Execution Waterfall</span>
          </div>
          {push.steps.map((st, i) => (
            <div key={i} className="flex items-start gap-2 text-[11px] py-1 border-b border-slate-900/60 last:border-0">
              <span className={`px-1.5 py-0.2 rounded text-[10px] shrink-0 font-bold ${
                st.level === 'SUCCESS'
                  ? 'bg-emerald-500/15 text-emerald-400'
                  : st.level === 'WARN'
                  ? 'bg-amber-500/15 text-amber-400'
                  : st.level === 'ERROR'
                  ? 'bg-rose-500/15 text-rose-400'
                  : 'bg-slate-800 text-slate-300'
              }`}>
                {st.name}
              </span>
              <span className="text-slate-300 flex-1">{st.text}</span>
              {st.time && <span className="text-slate-500 text-[10px] shrink-0">{st.time}</span>}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
