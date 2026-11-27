'use client';

import React, { useMemo } from 'react';
import { DollarSign, Layers, AlertTriangle, Zap, CheckCircle2 } from 'lucide-react';
import { UserAnalyticsEvent } from '../../types';

interface AdMonetizationDeckProps {
  events: UserAnalyticsEvent[];
}

interface PlacementRow {
  placement: string;
  nativeFills: number;
  bannerFallbacks: number;
  noFills: number;
}

const AD_EVENT_NAMES = new Set([
  'ad_filled_native',
  'ad_fallback_banner',
  'ad_no_fill',
  'ad_interstitial_shown',
]);

export function AdMonetizationDeck({ events }: AdMonetizationDeckProps) {
  const metrics = useMemo(() => {
    let nativeFills = 0;
    let bannerFallbacks = 0;
    let noFills = 0;
    let interstitials = 0;
    const byPlacement = new Map<string, PlacementRow>();

    const ensureRow = (placement: string): PlacementRow => {
      const existing = byPlacement.get(placement);
      if (existing) return existing;
      const created: PlacementRow = { placement, nativeFills: 0, bannerFallbacks: 0, noFills: 0 };
      byPlacement.set(placement, created);
      return created;
    };

    for (const ev of events) {
      if (!AD_EVENT_NAMES.has(ev.event_name)) continue;
      const placement = String(ev.payload?.placement || 'general');
      if (ev.event_name === 'ad_filled_native') {
        nativeFills++;
        ensureRow(placement).nativeFills++;
      } else if (ev.event_name === 'ad_fallback_banner') {
        bannerFallbacks++;
        ensureRow(placement).bannerFallbacks++;
      } else if (ev.event_name === 'ad_no_fill') {
        noFills++;
        ensureRow(placement).noFills++;
      } else if (ev.event_name === 'ad_interstitial_shown') {
        interstitials++;
      }
    }

    const totalFilled = nativeFills + bannerFallbacks;
    const totalAttempts = totalFilled + noFills;
    const fillRatePct = totalAttempts > 0 ? Math.round((totalFilled / totalAttempts) * 100) : 0;
    const rows = Array.from(byPlacement.values()).sort(
      (a, b) => (b.nativeFills + b.bannerFallbacks + b.noFills) - (a.nativeFills + a.bannerFallbacks + a.noFills),
    );

    return { nativeFills, bannerFallbacks, noFills, interstitials, fillRatePct, totalAttempts, rows };
  }, [events]);

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-4 sm:p-5 space-y-4">
      <div className="flex items-center justify-between border-b border-slate-800 pb-3">
        <div className="flex items-center gap-2">
          <DollarSign className="w-4 h-4 text-lime-400" />
          <h4 className="text-xs font-bold font-mono text-slate-100 uppercase tracking-wider">
            AdMob Waterfall & Fill Telemetry
          </h4>
        </div>
        <span className="text-[11px] font-mono px-2.5 py-0.5 rounded-md bg-lime-500/15 text-lime-300 border border-lime-500/30 font-bold">
          Fill Rate: {metrics.totalAttempts > 0 ? `${metrics.fillRatePct}%` : 'Awaiting v89832611+'}
        </span>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <KpiCard
          label="Native Fills"
          value={metrics.nativeFills}
          icon={<CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />}
          accentClass="text-emerald-400"
        />
        <KpiCard
          label="Banner Fallbacks"
          value={metrics.bannerFallbacks}
          icon={<Layers className="w-3.5 h-3.5 text-cyan-400" />}
          accentClass="text-cyan-400"
        />
        <KpiCard
          label="NO_FILL Events"
          value={metrics.noFills}
          icon={<AlertTriangle className="w-3.5 h-3.5 text-rose-400" />}
          accentClass="text-rose-400"
        />
        <KpiCard
          label="Interstitials"
          value={metrics.interstitials}
          icon={<Zap className="w-3.5 h-3.5 text-amber-400" />}
          accentClass="text-amber-400"
        />
      </div>

      {metrics.rows.length > 0 && (
        <div className="overflow-x-auto">
          <table className="w-full text-left font-mono text-xs">
            <thead>
              <tr className="border-b border-slate-800 text-slate-400 text-[10px] uppercase">
                <th className="py-2 pr-3">Placement</th>
                <th className="py-2 px-2 text-right">Native</th>
                <th className="py-2 px-2 text-right">Banner</th>
                <th className="py-2 pl-2 text-right">NO_FILL</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {metrics.rows.map((row) => (
                <tr key={row.placement} className="text-slate-200">
                  <td className="py-2 pr-3 font-bold uppercase text-slate-300">{row.placement}</td>
                  <td className="py-2 px-2 text-right text-emerald-400">{row.nativeFills}</td>
                  <td className="py-2 px-2 text-right text-cyan-400">{row.bannerFallbacks}</td>
                  <td className="py-2 pl-2 text-right text-rose-400">{row.noFills}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

function KpiCard({
  label,
  value,
  icon,
  accentClass,
}: {
  label: string;
  value: number;
  icon: React.ReactNode;
  accentClass: string;
}) {
  return (
    <div className="bg-slate-950/70 border border-slate-800/80 rounded-xl p-3">
      <p className="text-[10px] font-mono text-slate-400 uppercase tracking-wider">{label}</p>
      <div className="flex items-center gap-1.5 mt-1">
        {icon}
        <p className={`text-base font-bold font-mono ${accentClass}`}>{value}</p>
      </div>
    </div>
  );
}
