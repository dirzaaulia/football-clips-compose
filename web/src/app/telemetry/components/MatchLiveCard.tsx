import React, { useState } from 'react';
import {
  PlayCircle,
  Clock,
  CheckCircle,
  ShieldCheck,
  ShieldAlert,
  ArrowLeftRight,
  Activity,
  Copy,
  Check,
  ChevronDown,
  ChevronUp,
} from 'lucide-react';
import { TelemetryEvent } from '../types';

export interface GroupedMatchLive {
  matchId: string;
  home: string;
  away: string;
  competition: string;
  homeScore?: number | null;
  awayScore?: number | null;
  clock?: string;
  status: string;
  events: TelemetryEvent[];
}

interface MatchLiveCardProps {
  match: GroupedMatchLive;
}

export function MatchLiveCard({ match }: MatchLiveCardProps) {
  const [expanded, setExpanded] = useState(true);
  const [copied, setCopied] = useState(false);

  const handleCopy = (e: React.MouseEvent) => {
    e.stopPropagation();
    navigator.clipboard.writeText(JSON.stringify(match, null, 2));
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const isLive = ['IN_PLAY', 'PAUSED', 'LIVE'].includes(match.status);
  const isFinished = ['FINISHED', 'AWARDED'].includes(match.status);

  return (
    <div className="bg-slate-900/90 border border-slate-800 rounded-xl overflow-hidden shadow-sm hover:border-slate-700 transition">
      {/* Match Header Bar */}
      <div
        onClick={() => setExpanded(!expanded)}
        className="p-4 cursor-pointer flex flex-wrap items-center justify-between gap-3 bg-slate-900/60 hover:bg-slate-800/40 select-none"
      >
        <div className="flex items-center gap-3 min-w-0">
          <span
            className={`px-2 py-0.5 rounded font-mono text-[10px] font-bold uppercase tracking-wider ${
              isLive
                ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                : isFinished
                ? 'bg-purple-500/20 text-purple-400 border border-purple-500/30'
                : 'bg-slate-800 text-slate-400 border border-slate-700'
            }`}
          >
            {match.competition}
          </span>
          <div className="min-w-0">
            <h4 className="text-sm font-bold text-white truncate">
              {match.home} <span className="text-slate-400 font-normal">vs</span> {match.away}
            </h4>
            <p className="text-[11px] font-mono text-slate-400">
              Match #{match.matchId} · {match.events.length} event{match.events.length === 1 ? '' : 's'} logged
            </p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          {(match.homeScore != null || isLive || isFinished) && (
            <div className="text-right">
              <span className="text-base font-black font-mono text-white">
                {match.homeScore ?? 0} - {match.awayScore ?? 0}
              </span>
              {match.clock && (
                <p className="text-[10px] font-mono font-semibold text-emerald-400">{match.clock}</p>
              )}
            </div>
          )}

          <button
            onClick={handleCopy}
            title="Copy match JSON"
            className="p-1.5 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800 border border-slate-800"
          >
            {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
          </button>

          <div className="text-slate-400">
            {expanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
          </div>
        </div>
      </div>

      {/* Chronological Event Timeline */}
      {expanded && (
        <div className="p-4 pt-1 border-t border-slate-800/80 bg-slate-950/40 space-y-2">
          {match.events.map((ev, idx) => {
            const txt = ev.event_text || '';
            const isScore = txt.includes('[LIVE SCORE]') || txt.includes('[GOAL]');
            const isCard = txt.includes('[CARD]');
            const isSub = txt.includes('[SUBSTITUTION]');
            const isKickoff = txt.includes('[KICKOFF]');
            const isFullTime = txt.includes('[FULL TIME]');
            const isProtected = txt.includes('PROTECTED') || txt.includes('REGRESSION');

            return (
              <div
                key={idx}
                className="flex items-start gap-2.5 text-xs font-mono p-2 rounded-lg bg-slate-900/60 border border-slate-800/60"
              >
                <div className="mt-0.5 shrink-0">
                  {isScore ? (
                    <PlayCircle className="w-3.5 h-3.5 text-emerald-400" />
                  ) : isCard ? (
                    <ShieldAlert className="w-3.5 h-3.5 text-amber-400" />
                  ) : isSub ? (
                    <ArrowLeftRight className="w-3.5 h-3.5 text-cyan-400" />
                  ) : isKickoff ? (
                    <Clock className="w-3.5 h-3.5 text-indigo-400" />
                  ) : isFullTime ? (
                    <CheckCircle className="w-3.5 h-3.5 text-purple-400" />
                  ) : isProtected ? (
                    <ShieldCheck className="w-3.5 h-3.5 text-amber-400" />
                  ) : (
                    <Activity className="w-3.5 h-3.5 text-slate-400" />
                  )}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-slate-200 break-words leading-relaxed">{txt}</p>
                  {ev.created_at && (
                    <p className="text-[10px] text-slate-500 mt-0.5">
                      {new Date(ev.created_at).toLocaleTimeString('id-ID', {
                        timeZone: 'Asia/Jakarta',
                        hour: '2-digit',
                        minute: '2-digit',
                        second: '2-digit',
                      })}{' '}
                      WIB
                    </p>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
