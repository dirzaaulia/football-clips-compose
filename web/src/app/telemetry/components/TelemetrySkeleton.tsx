import React from 'react';

export function WaterfallCardSkeleton() {
  return (
    <div className="bg-slate-900/80 border border-slate-800 rounded-xl p-3.5 sm:p-4 space-y-3">
      <div className="flex items-center justify-between gap-2">
        <div className="flex items-center gap-2 min-w-0">
          <div className="w-40 sm:w-56 h-4.5 bg-slate-800 rounded" />
          <div className="w-12 h-4.5 bg-slate-800/80 rounded" />
        </div>
        <div className="flex items-center gap-2 shrink-0">
          <div className="w-20 h-5 bg-slate-800 rounded-full" />
          <div className="w-4 h-4 bg-slate-800 rounded" />
        </div>
      </div>
      <div className="flex items-center justify-between gap-2 pt-2 border-t border-slate-800/60 text-xs">
        <div className="flex items-center gap-2 flex-wrap min-w-0">
          <div className="w-20 h-3 bg-slate-800/60 rounded" />
          <div className="w-24 h-3 bg-slate-800/60 rounded" />
          <div className="w-16 h-4 bg-slate-800/80 rounded" />
          <div className="w-28 h-4 bg-slate-800/80 rounded" />
          <div className="w-16 h-4 bg-slate-800/80 rounded" />
        </div>
        <div className="flex items-center gap-1.5 shrink-0">
          <div className="w-4 h-4 bg-slate-800/60 rounded" />
          <div className="w-4 h-4 bg-slate-800/60 rounded" />
        </div>
      </div>
    </div>
  );
}

export function WaterfallGroupedSkeleton() {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between p-3 rounded-xl bg-slate-900/60 border border-slate-800 flex-wrap gap-2">
        <div className="w-40 h-3.5 bg-slate-800 rounded" />
        <div className="flex items-center gap-2 flex-wrap">
          <div className="w-24 h-5 bg-slate-800/70 rounded-full" />
          <div className="w-24 h-5 bg-slate-800/70 rounded-full" />
          <div className="w-24 h-5 bg-slate-800/70 rounded-full" />
          <div className="w-24 h-5 bg-slate-800/70 rounded-full" />
        </div>
      </div>
      <div className="space-y-3">
        <div className="p-3.5 rounded-xl bg-slate-900/90 border border-slate-800 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-4 h-4 bg-slate-800 rounded" />
            <div className="w-36 h-4 bg-slate-800 rounded" />
            <div className="w-24 h-3.5 bg-slate-800/60 rounded" />
          </div>
          <div className="w-20 h-5 bg-slate-800/80 rounded-full" />
        </div>
        <div className="pl-3 sm:pl-4 space-y-3 border-l-2 border-indigo-500/20 ml-2 sm:ml-4">
          <div className="p-3 rounded-xl bg-slate-900/70 border border-slate-800/90 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="w-3.5 h-3.5 bg-slate-800 rounded" />
              <div className="w-32 h-4 bg-slate-800 rounded" />
              <div className="w-16 h-4 bg-slate-800/60 rounded" />
            </div>
            <div className="w-16 h-4 bg-slate-800/60 rounded" />
          </div>
          <div className="space-y-2.5">
            {[1, 2, 3].map((i) => (
              <WaterfallCardSkeleton key={i} />
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

export function TelemetryHeaderSkeleton() {
  return (
    <header className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-4 border-b border-slate-800/80">
      <div>
        <div className="flex items-center gap-3">
          <span className="flex h-3 w-3 rounded-full bg-emerald-500/50" />
          <div className="w-48 h-6 bg-slate-800 rounded" />
          <div className="w-36 h-5 bg-slate-800/70 rounded" />
        </div>
        <div className="w-64 h-3.5 bg-slate-800/50 rounded mt-2" />
      </div>
      <div className="flex items-center gap-2 flex-wrap">
        <div className="w-28 h-8 bg-slate-900 border border-slate-800 rounded-lg" />
        <div className="w-16 h-8 bg-slate-900 border border-slate-800 rounded-lg" />
        <div className="w-24 h-8 bg-slate-900 border border-slate-800 rounded-lg" />
        <div className="w-24 h-8 bg-slate-900 border border-slate-800 rounded-lg" />
        <div className="w-20 h-8 bg-slate-800 rounded-lg" />
      </div>
    </header>
  );
}

export function QuotaGaugeCardSkeleton() {
  return (
    <div className="bg-slate-900/90 border border-slate-800 rounded-xl p-5 shadow-lg">
      <div className="flex items-center justify-between mb-3">
        <div className="w-48 h-4 bg-slate-800 rounded" />
        <div className="w-32 h-5 bg-slate-800/80 rounded-full" />
      </div>
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-4">
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className="bg-slate-950/60 p-3 rounded-lg border border-slate-800/80 space-y-2">
            <div className="w-20 h-3 bg-slate-800/50 rounded" />
            <div className="w-28 h-6 bg-slate-800 rounded" />
          </div>
        ))}
      </div>
      <div className="space-y-2">
        <div className="w-full h-3 bg-slate-950 rounded-full border border-slate-800 overflow-hidden" />
        <div className="flex justify-between">
          <div className="w-36 h-3 bg-slate-800/50 rounded" />
          <div className="w-28 h-3 bg-slate-800/50 rounded" />
        </div>
      </div>
    </div>
  );
}

export function PipelineFlowGraphSkeleton() {
  return (
    <div className="w-full h-64 sm:h-72 lg:h-80 bg-[#0B0F13] border border-white/10 rounded-2xl p-4 relative overflow-hidden shadow-xl flex items-center justify-between gap-4">
      <div className="absolute top-3 left-4 flex items-center gap-2">
        <span className="flex h-2 w-2 rounded-full bg-emerald-400/50" />
        <div className="w-56 h-3 bg-slate-800 rounded" />
      </div>
      <div className="w-full flex items-center justify-around gap-2 px-4 pt-6 overflow-x-auto no-scrollbar">
        {[1, 2, 3, 4, 5].map((i) => (
          <div key={i} className="bg-[#161B22] border border-white/10 rounded-xl p-3 min-w-[150px] sm:min-w-[180px] space-y-2 shrink-0">
            <div className="flex items-center justify-between">
              <div className="w-14 h-3 bg-slate-800 rounded" />
              <div className="w-12 h-3.5 bg-slate-800/70 rounded" />
            </div>
            <div className="w-28 h-4 bg-slate-800 rounded" />
            <div className="pt-2 border-t border-white/5 flex items-center justify-between">
              <div className="w-10 h-2.5 bg-slate-800/60 rounded" />
              <div className="w-14 h-2.5 bg-slate-800/80 rounded" />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export function TelemetryFilterBarSkeleton() {
  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 flex flex-col sm:flex-row gap-3 items-stretch sm:items-center justify-between">
      <div className="h-8 bg-slate-950 border border-slate-800 rounded-lg flex-1" />
      <div className="flex items-center gap-2">
        <div className="w-28 h-8 bg-slate-950 border border-slate-800 rounded-lg" />
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className="w-14 h-8 bg-slate-800 rounded-lg shrink-0" />
        ))}
      </div>
    </div>
  );
}

export default function TelemetrySkeleton() {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 font-sans p-4 sm:p-6 lg:p-8">
      <div className="max-w-7xl 2xl:max-w-[1440px] mx-auto space-y-6 select-none animate-pulse">
        <TelemetryHeaderSkeleton />
        <QuotaGaugeCardSkeleton />
        <div className="flex flex-col lg:flex-row items-start gap-6">
          <aside className="w-full lg:w-72 shrink-0 space-y-4">
            <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-4 space-y-3">
              <div className="flex items-center justify-between">
                <div className="w-28 h-3.5 bg-slate-800 rounded" />
                <div className="w-12 h-4 bg-slate-800/70 rounded" />
              </div>
              <div className="space-y-1.5">
                <div className="w-full h-2.5 bg-slate-800/50 rounded" />
                <div className="w-4/5 h-2.5 bg-slate-800/50 rounded" />
              </div>
            </div>
            <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-2 space-y-1.5 flex flex-row lg:flex-col overflow-x-auto lg:overflow-x-visible">
              {[1, 2, 3, 4, 5, 6].map((i) => (
                <div key={i} className="p-3 rounded-xl flex items-center justify-between gap-3 bg-slate-900/40 w-full shrink-0 lg:shrink">
                  <div className="flex items-center gap-3 min-w-0">
                    <div className="w-8 h-8 rounded-lg bg-slate-800 shrink-0" />
                    <div className="space-y-1">
                      <div className="w-24 h-3.5 bg-slate-800 rounded" />
                      <div className="w-32 h-2.5 bg-slate-800/50 rounded" />
                    </div>
                  </div>
                  <div className="w-12 h-4 bg-slate-800/70 rounded shrink-0" />
                </div>
              ))}
            </div>
          </aside>
          <main className="flex-1 min-w-0 w-full space-y-5">
            <div className="bg-slate-900/70 border border-slate-800 rounded-2xl p-4 space-y-3">
              <div className="flex items-center justify-between gap-2">
                <div className="w-44 h-4 bg-slate-800 rounded" />
                <div className="w-56 h-7 bg-slate-800/80 rounded-lg" />
              </div>
              <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-2.5">
                {[1, 2, 3, 4, 5].map((i) => (
                  <div key={i} className="p-3 rounded-xl border border-slate-800 bg-slate-950/70 space-y-2">
                    <div className="w-24 h-3 bg-slate-800 rounded" />
                    <div className="w-16 h-7 bg-slate-800/90 rounded" />
                    <div className="w-20 h-4 bg-slate-800/60 rounded" />
                  </div>
                ))}
              </div>
            </div>
            <PipelineFlowGraphSkeleton />
            <TelemetryFilterBarSkeleton />
            <div className="flex items-center justify-between text-xs px-1">
              <div className="w-56 h-3 bg-slate-800/60 rounded" />
              <div className="w-48 h-3 bg-slate-800/40 rounded" />
            </div>
            <WaterfallGroupedSkeleton />
          </main>
        </div>
      </div>
    </div>
  );
}
