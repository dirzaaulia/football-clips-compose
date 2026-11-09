'use client';

import React, { useState, useEffect } from 'react';
import { RefreshCw, Play, Pause, Radio, Server, Trash2, BookOpen } from 'lucide-react';

interface TelemetryHeaderProps {
  onRefresh: () => void;
  isRefreshing: boolean;
  autoRefreshInterval: number;
  setAutoRefreshInterval: (interval: number) => void;
  lastUpdated?: string;
  onExportJson?: () => void;
  onClearLogs?: () => void;
  isClearingLogs?: boolean;
  onOpenTaxonomy?: () => void;
}

export function TelemetryHeader({
  onRefresh,
  isRefreshing,
  autoRefreshInterval,
  setAutoRefreshInterval,
  lastUpdated,
  onExportJson,
  onClearLogs,
  isClearingLogs,
  onOpenTaxonomy,
}: TelemetryHeaderProps) {
  const [timeWib, setTimeWib] = useState('');
  const [countdown, setCountdown] = useState(autoRefreshInterval);
  const [copied, setCopied] = useState(false);

  const handleExport = () => {
    if (onExportJson) {
      onExportJson();
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      setTimeWib(
        new Intl.DateTimeFormat('en-GB', {
          timeZone: 'Asia/Jakarta',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
          day: '2-digit',
          month: 'short',
        }).format(now) + ' WIB'
      );
    };

    updateTime();
    const interval = setInterval(updateTime, 1000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    if (autoRefreshInterval === 0) return;
    setCountdown(autoRefreshInterval);
    const timer = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          onRefresh();
          return autoRefreshInterval;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(timer);
  }, [autoRefreshInterval, onRefresh]);

  return (
    <header className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-6 border-b border-slate-800">
      <div>
        <div className="flex items-center gap-2.5">
          <span className="relative flex h-3 w-3">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-3 w-3 bg-emerald-500"></span>
          </span>
          <h1 className="text-2xl font-black tracking-tight text-white font-mono">
            MISSION CONTROL
          </h1>
          <span className="px-2 py-0.5 rounded text-[10px] font-bold tracking-wider uppercase bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-mono">
            Go Micro-Daemon (Oracle VM)
          </span>
        </div>
        <p className="text-xs text-slate-400 mt-1 flex items-center gap-2">
          <Server className="w-3.5 h-3.5 text-slate-500" />
          <span>Oracle VM ARM64 · fc-daemon (7.5 MB RAM) · $0 Cost</span>
          {lastUpdated && <span className="text-slate-500 font-mono">· Synced: {lastUpdated}</span>}
        </p>
      </div>

      <div className="flex items-center gap-3 flex-wrap">
        <div className="bg-slate-900 border border-slate-800 px-3 py-1.5 rounded-lg text-xs font-mono text-slate-300 flex items-center gap-2">
          <Radio className="w-3.5 h-3.5 text-emerald-400 animate-pulse" />
          <span>{timeWib || 'Loading...'}</span>
        </div>

        <div className="flex items-center gap-1.5 bg-slate-900 border border-slate-800 p-1 rounded-lg text-xs font-mono">
          <button
            onClick={() => setAutoRefreshInterval(autoRefreshInterval === 0 ? 15 : 0)}
            className="p-1.5 rounded hover:bg-slate-800 text-slate-300 transition"
            title={autoRefreshInterval === 0 ? 'Resume Auto-refresh' : 'Pause Auto-refresh'}
          >
            {autoRefreshInterval === 0 ? <Play className="w-3.5 h-3.5 text-emerald-400" /> : <Pause className="w-3.5 h-3.5 text-amber-400" />}
          </button>
          <span className="text-slate-400 px-1">
            {autoRefreshInterval === 0 ? 'Paused' : `${countdown}s`}
          </span>
        </div>

        {onExportJson && (
          <button
            onClick={handleExport}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-mono bg-slate-900 hover:bg-slate-800 text-slate-300 transition border border-slate-800"
            title="Export full telemetry state JSON"
          >
            <span>{copied ? 'Copied Full JSON' : 'Export JSON'}</span>
          </button>
        )}

        {onClearLogs && (
          <button
            onClick={onClearLogs}
            disabled={isClearingLogs}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-mono bg-rose-950/40 hover:bg-rose-900/50 text-rose-300 transition border border-rose-800/60 disabled:opacity-50"
            title="Clear all telemetry cycle traces from memory/storage"
          >
            <Trash2 className="w-3.5 h-3.5 text-rose-400" />
            <span>{isClearingLogs ? 'Clearing...' : 'Clear Logs'}</span>
          </button>
        )}

        {onOpenTaxonomy && (
          <button
            onClick={onOpenTaxonomy}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-mono bg-slate-900 hover:bg-slate-800 text-indigo-300 transition border border-indigo-500/30"
            title="Manage Classification Taxonomy in PostgreSQL"
          >
            <BookOpen className="w-3.5 h-3.5 text-indigo-400" />
            <span>Taxonomy</span>
          </button>
        )}

        <button
          onClick={onRefresh}
          disabled={isRefreshing}
          className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-indigo-600 hover:bg-indigo-500 text-white transition disabled:opacity-50"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${isRefreshing ? 'animate-spin' : ''}`} />
          <span>Refresh</span>
        </button>
      </div>
    </header>
  );
}
