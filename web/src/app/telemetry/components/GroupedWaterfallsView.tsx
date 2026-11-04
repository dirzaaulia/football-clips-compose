'use client';

import React from 'react';
import { Trophy, Calendar } from 'lucide-react';
import { WeekCompetitionGroup } from '../types';
import { WaterfallCard } from './WaterfallCard';

interface GroupedWaterfallsViewProps {
  groups: WeekCompetitionGroup[];
  onOverrideSuccess?: () => void;
}

export function GroupedWaterfallsView({ groups, onOverrideSuccess }: GroupedWaterfallsViewProps) {
  if (groups.length === 0) {
    return (
      <div className="p-12 text-center text-slate-500 font-mono text-sm bg-slate-900/40 rounded-xl border border-slate-800">
        No finished match waterfalls found for current search/filter.
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {groups.map((group) => (
        <section key={group.monthWeekKey} className="space-y-4">
          {/* Week of Month Banner */}
          <div className="flex items-center justify-between pb-2 border-b border-slate-800/80">
            <div className="flex items-center gap-2">
              <Calendar className="w-4 h-4 text-indigo-400" />
              <h3 className="text-sm font-bold font-mono text-slate-100 tracking-wide uppercase">
                {group.monthWeekKey}
              </h3>
            </div>
            <span className="text-xs font-mono text-slate-400">
              {group.weekStart} – {group.weekEnd}
            </span>
          </div>

          {/* Competitions Sorted by Priority */}
          <div className="space-y-6">
            {group.competitions.map((comp) => (
              <div key={comp.competitionId} className="space-y-3 pl-1">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Trophy className="w-3.5 h-3.5 text-amber-400" />
                    <h4 className="text-xs font-bold text-slate-200 uppercase tracking-wider font-mono">
                      {comp.competitionName}
                    </h4>
                    <span className="px-1.5 py-0.5 rounded text-[10px] font-mono font-bold bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
                      Priority #{comp.priority}
                    </span>
                  </div>
                  <span className="text-[11px] font-mono text-slate-500">
                    {comp.matches.length} {comp.matches.length === 1 ? 'match' : 'matches'}
                  </span>
                </div>

                <div className="space-y-2.5">
                  {comp.matches.map((wf) => (
                    <WaterfallCard
                      key={wf.matchId}
                      waterfall={wf}
                      onOverrideSuccess={onOverrideSuccess}
                    />
                  ))}
                </div>
              </div>
            ))}
          </div>
        </section>
      ))}
    </div>
  );
}
