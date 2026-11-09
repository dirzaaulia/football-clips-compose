'use client';

import React, { useState } from 'react';
import { Trophy, Calendar, ChevronDown, ChevronRight, ChevronsUpDown } from 'lucide-react';
import { WeekCompetitionGroup } from '../types';
import { WaterfallCard } from './WaterfallCard';

interface GroupedWaterfallsViewProps {
  groups: WeekCompetitionGroup[];
  onOverrideSuccess?: () => void;
}

export function GroupedWaterfallsView({ groups, onOverrideSuccess }: GroupedWaterfallsViewProps) {
  const [expandedWeeks, setExpandedWeeks] = useState<Set<string>>(
    () => new Set(groups.map((g) => g.monthWeekKey)),
  );
  const [expandedComps, setExpandedComps] = useState<Set<string>>(() => new Set());

  if (groups.length === 0) {
    return (
      <div className="p-12 text-center text-slate-500 font-mono text-sm bg-slate-900/40 rounded-xl border border-slate-800">
        No finished match waterfalls found for current search/filter.
      </div>
    );
  }

  const toggleWeek = (weekKey: string) => {
    setExpandedWeeks((prev) => {
      const next = new Set(prev);
      if (next.has(weekKey)) next.delete(weekKey);
      else next.add(weekKey);
      return next;
    });
  };

  const toggleComp = (compKey: string) => {
    setExpandedComps((prev) => {
      const next = new Set(prev);
      if (next.has(compKey)) next.delete(compKey);
      else next.add(compKey);
      return next;
    });
  };

  const toggleAllCompsInWeek = (group: WeekCompetitionGroup, e: React.MouseEvent) => {
    e.stopPropagation();
    const allCompKeys = group.competitions.map((c) => `${group.monthWeekKey}-${c.competitionId}`);
    const areAllExpanded = allCompKeys.every((k) => expandedComps.has(k));

    setExpandedComps((prev) => {
      const next = new Set(prev);
      for (const k of allCompKeys) {
        if (areAllExpanded) next.delete(k);
        else next.add(k);
      }
      return next;
    });
  };

  return (
    <div className="space-y-6">
      {/* Ingestion Source Legend */}
      <div className="flex items-center justify-between p-3 rounded-xl bg-slate-900/60 border border-slate-800 text-xs text-slate-400 flex-wrap gap-2">
        <div className="flex items-center gap-1.5 font-semibold text-slate-300">
          <span>Highlight Discovery Sources:</span>
        </div>
        <div className="flex items-center gap-2 flex-wrap text-[11px] font-mono">
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-amber-500/15 text-amber-300 border border-amber-500/30 font-semibold">
            ⚡ WebSub Push <span className="text-[10px] text-amber-400/70">(Instant Push)</span>
          </span>
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-emerald-500/15 text-emerald-300 border border-emerald-500/30 font-semibold">
            📡 RSS Feed <span className="text-[10px] text-emerald-400/70">(0 Quota Channel Scan)</span>
          </span>
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-sky-500/15 text-sky-300 border border-sky-500/30 font-semibold">
            🔑 YouTube API <span className="text-[10px] text-sky-400/70">(Tiered Search Quota)</span>
          </span>
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-purple-500/15 text-purple-300 border border-purple-500/30 font-semibold">
            👤 Manual <span className="text-[10px] text-purple-400/70">(Operator Override)</span>
          </span>
        </div>
      </div>

      {groups.map((group) => {
        const isWeekExpanded = expandedWeeks.has(group.monthWeekKey);
        const totalMatches = group.competitions.reduce((acc, c) => acc + c.matches.length, 0);
        const linkedMatches = group.competitions.reduce(
          (acc, c) => acc + c.matches.filter((m) => m.status === 'LINKED' || Boolean(m.outcomeVideoId)).length,
          0,
        );
        const coveragePercent = totalMatches > 0 ? Math.round((linkedMatches / totalMatches) * 100) : 0;
        const allCompKeys = group.competitions.map((c) => `${group.monthWeekKey}-${c.competitionId}`);
        const areAllCompsExpanded = allCompKeys.length > 0 && allCompKeys.every((k) => expandedComps.has(k));

        return (
          <section key={group.monthWeekKey} className="space-y-4">
            {/* Week of Month Banner (Sticky Level 1) */}
            <div
              onClick={() => toggleWeek(group.monthWeekKey)}
              className="sticky top-0 z-30 bg-slate-950/95 backdrop-blur-md py-2.5 px-3 border-b border-slate-800/90 rounded-lg shadow-sm flex items-center justify-between cursor-pointer select-none transition hover:bg-slate-900/50"
            >
              <div className="flex items-center gap-2">
                {isWeekExpanded ? (
                  <ChevronDown className="w-4 h-4 text-indigo-400 shrink-0" />
                ) : (
                  <ChevronRight className="w-4 h-4 text-slate-500 shrink-0" />
                )}
                <Calendar className="w-4 h-4 text-indigo-400 shrink-0" />
                <h3 className="text-sm font-bold font-mono text-slate-100 tracking-wide uppercase">
                  {group.monthWeekKey}
                </h3>
              </div>

              <div className="flex items-center gap-2.5 flex-wrap">
                <span className="text-xs font-mono text-slate-400 hidden sm:inline">
                  {group.weekStart} – {group.weekEnd}
                </span>

                <span
                  className={`px-2 py-0.5 rounded text-[11px] font-mono font-semibold border ${
                    coveragePercent === 100
                      ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                      : coveragePercent >= 80
                        ? 'bg-cyan-500/10 text-cyan-400 border-cyan-500/30'
                        : 'bg-amber-500/10 text-amber-400 border-amber-500/30'
                  }`}
                >
                  {linkedMatches} / {totalMatches} Linked · {coveragePercent}%
                </span>

                {isWeekExpanded && (
                  <button
                    onClick={(e) => toggleAllCompsInWeek(group, e)}
                    className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[11px] font-mono font-medium text-slate-300 hover:text-white bg-slate-800/80 hover:bg-slate-700/80 border border-slate-700 transition"
                    title={areAllCompsExpanded ? 'Collapse All Competitions' : 'Expand All Competitions'}
                  >
                    <ChevronsUpDown className="w-3 h-3 text-indigo-400" />
                    <span>{areAllCompsExpanded ? 'Collapse All' : 'Expand All'}</span>
                  </button>
                )}
              </div>
            </div>

            {/* Competitions Sorted by Priority (Rendered when Week is expanded) */}
            {isWeekExpanded && (
              <div className="space-y-4">
                {group.competitions.map((comp) => {
                  const compKey = `${group.monthWeekKey}-${comp.competitionId}`;
                  const isCompExpanded = expandedComps.has(compKey);
                  const compTotal = comp.matches.length;
                  const compLinked = comp.matches.filter(
                    (m) => m.status === 'LINKED' || Boolean(m.outcomeVideoId),
                  ).length;

                  return (
                    <div key={comp.competitionId} className="space-y-2.5">
                      {/* Competition Accordion Header (Sticky Level 2) */}
                      <div
                        onClick={() => toggleComp(compKey)}
                        className={`sticky top-11 z-20 backdrop-blur-md border rounded-xl px-4 py-2.5 shadow-md select-none transition cursor-pointer flex items-center justify-between ${
                          isCompExpanded
                            ? 'bg-slate-900/95 border-indigo-500/40 shadow-indigo-950/20'
                            : 'bg-slate-900/80 border-slate-800 hover:border-slate-700'
                        }`}
                      >
                        <div className="flex items-center gap-2.5 min-w-0 pr-2">
                          {isCompExpanded ? (
                            <ChevronDown className="w-4 h-4 text-indigo-400 shrink-0" />
                          ) : (
                            <ChevronRight className="w-4 h-4 text-slate-500 shrink-0" />
                          )}
                          <Trophy className="w-4 h-4 text-amber-400 shrink-0" />
                          <h4 className="text-xs sm:text-sm font-bold text-slate-200 uppercase tracking-wider font-mono truncate">
                            {comp.competitionName}
                          </h4>
                          <span className="px-1.5 py-0.5 rounded text-[10px] font-mono font-bold bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 shrink-0">
                            Priority #{comp.priority}
                          </span>
                        </div>

                        <div className="flex items-center gap-2 shrink-0">
                          <span className="text-[11px] font-mono text-emerald-400/90 font-medium">
                            {compLinked} / {compTotal} linked
                          </span>
                          <span className="text-slate-600 hidden xs:inline">·</span>
                          <span className="text-[11px] font-mono text-slate-400 hidden xs:inline">
                            {compTotal} {compTotal === 1 ? 'match' : 'matches'}
                          </span>
                        </div>
                      </div>

                      {/* Matches inside Competition (Rendered when Competition is expanded) */}
                      {isCompExpanded && (
                        <div className="space-y-2.5 pl-1 sm:pl-3">
                          {comp.matches.map((wf) => (
                            <WaterfallCard
                              key={wf.matchId}
                              waterfall={wf}
                              onOverrideSuccess={onOverrideSuccess}
                            />
                          ))}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </section>
        );
      })}
    </div>
  );
}
