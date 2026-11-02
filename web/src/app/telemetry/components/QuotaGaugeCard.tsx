'use client';

import React from 'react';
import { Activity, ShieldCheck, Zap } from 'lucide-react';
import { QuotaStatus } from '../types';

interface QuotaGaugeCardProps {
  quota?: QuotaStatus;
}

export function QuotaGaugeCard({ quota }: QuotaGaugeCardProps) {
  const limit = quota?.limit || 60000;
  const hits = quota?.hits_today || 0;
  const remaining = quota?.remaining ?? (limit - hits);
  const hoursLeft = quota?.hours_until_reset ?? 0;
  const usedPercent = Math.min(100, Math.max(0, (hits / limit) * 100));

  return (
    <div className="bg-slate-900/90 border border-slate-800 rounded-xl p-5 shadow-lg backdrop-blur-sm">
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-2">
          <Activity className="w-5 h-5 text-indigo-400" />
          <h3 className="text-sm font-semibold tracking-wide text-slate-200 uppercase">
            YouTube API Quota Guardrail
          </h3>
        </div>
        <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
          <ShieldCheck className="w-3.5 h-3.5" />
          60,000 Units Tier
        </span>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-4">
        <div className="bg-slate-950/60 p-3 rounded-lg border border-slate-800/80">
          <p className="text-[11px] font-medium text-slate-400 uppercase">Today&apos;s Usage</p>
          <p className="text-lg font-bold font-mono text-indigo-400">{hits.toLocaleString()} <span className="text-xs text-slate-500">units</span></p>
        </div>
        <div className="bg-slate-950/60 p-3 rounded-lg border border-slate-800/80">
          <p className="text-[11px] font-medium text-slate-400 uppercase">Remaining Safe</p>
          <p className="text-lg font-bold font-mono text-emerald-400">{remaining.toLocaleString()} <span className="text-xs text-slate-500">units</span></p>
        </div>
        <div className="bg-slate-950/60 p-3 rounded-lg border border-slate-800/80">
          <p className="text-[11px] font-medium text-slate-400 uppercase">Daily Capacity</p>
          <p className="text-lg font-bold font-mono text-slate-200">{limit.toLocaleString()} <span className="text-xs text-slate-500">units</span></p>
        </div>
        <div className="bg-slate-950/60 p-3 rounded-lg border border-slate-800/80">
          <p className="text-[11px] font-medium text-slate-400 uppercase">Reset In</p>
          <p className="text-lg font-bold font-mono text-amber-400">{hoursLeft} <span className="text-xs text-slate-500">hours</span></p>
        </div>
      </div>

      <div>
        <div className="flex justify-between text-xs text-slate-400 font-mono mb-1.5">
          <span>Capacity Consumed: {usedPercent.toFixed(1)}%</span>
          <span>Headroom: {(100 - usedPercent).toFixed(1)}%</span>
        </div>
        <div className="w-full bg-slate-800/90 h-2.5 rounded-full overflow-hidden">
          <div
            className="h-full bg-gradient-to-r from-indigo-500 via-blue-500 to-emerald-400 rounded-full transition-all duration-500"
            style={{ width: `${Math.max(usedPercent, 2)}%` }}
          />
        </div>
        <p className="text-[11px] text-slate-500 mt-2 flex items-center gap-1.5">
          <Zap className="w-3.5 h-3.5 text-indigo-400" />
          Quota refreshed autonomously via Google Cloud Monitoring. 0 quota spent on RSS &amp; WebSub.
        </p>
      </div>
    </div>
  );
}
