'use client';

import React, { useMemo } from 'react';
import { UserAnalyticsEvent, AppSessionItem } from '../../types';
import { Sparkles, Filter, MousePointerClick, PlayCircle, Heart } from 'lucide-react';

interface AnalyticsFunnelProps {
  events: UserAnalyticsEvent[];
  appSessions?: AppSessionItem[];
}

export function AnalyticsFunnel({ events, appSessions = [] }: AnalyticsFunnelProps) {
  const funnel = useMemo(() => {
    const totalSessions = appSessions.length + events.filter((e) => e.event_name === 'session_start').length || 1;
    const browseCount = events.filter((e) => e.event_name === 'screen_view' || e.event_name === 'competition_filter' || e.event_name === 'tab_switch').length;
    const matchClicks = events.filter((e) => e.event_name === 'match_click').length;
    const videoPlays = events.filter((e) => e.event_name === 'video_play').length;
    const retentionActions = events.filter((e) => e.event_name === 'club_favorite' || e.event_name === 'watch_on_youtube').length;

    const stages = [
      {
        name: '1. App Sessions',
        count: totalSessions,
        pct: 100,
        icon: Sparkles,
        color: 'text-cyan-400 bg-cyan-500/10 border-cyan-500/30',
        barColor: 'bg-cyan-500',
      },
      {
        name: '2. Browse & Filter',
        count: browseCount,
        pct: Math.min(100, Math.round((browseCount / totalSessions) * 100)),
        icon: Filter,
        color: 'text-indigo-400 bg-indigo-500/10 border-indigo-500/30',
        barColor: 'bg-indigo-500',
      },
      {
        name: '3. Match Card Click',
        count: matchClicks,
        pct: Math.min(100, Math.round((matchClicks / totalSessions) * 100)),
        icon: MousePointerClick,
        color: 'text-purple-400 bg-purple-500/10 border-purple-500/30',
        barColor: 'bg-purple-500',
      },
      {
        name: '4. Highlight Play',
        count: videoPlays,
        pct: Math.min(100, Math.round((videoPlays / totalSessions) * 100)),
        icon: PlayCircle,
        color: 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30',
        barColor: 'bg-emerald-500',
      },
      {
        name: '5. Retention Action',
        count: retentionActions,
        pct: Math.min(100, Math.round((retentionActions / totalSessions) * 100)),
        icon: Heart,
        color: 'text-rose-400 bg-rose-500/10 border-rose-500/30',
        barColor: 'bg-rose-500',
      },
    ];

    return stages;
  }, [events, appSessions]);

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
      <div className="flex items-center justify-between">
        <div className="space-y-0.5">
          <h4 className="text-xs font-bold font-mono text-slate-100 uppercase tracking-wider">
            Behavioral Conversion Funnel
          </h4>
          <p className="text-[11px] text-slate-400 font-mono">
            User journey drop-off across key football engagement milestones
          </p>
        </div>
      </div>

      <div className="space-y-3">
        {funnel.map((stage) => {
          const Icon = stage.icon;
          return (
            <div key={stage.name} className="space-y-1.5">
              <div className="flex items-center justify-between text-xs font-mono">
                <div className="flex items-center gap-2">
                  <span className={`p-1 rounded-md border ${stage.color}`}>
                    <Icon className="w-3 h-3" />
                  </span>
                  <span className="font-semibold text-slate-200">{stage.name}</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-slate-400 font-bold">{stage.count} events</span>
                  <span className="text-slate-500">({stage.pct}%)</span>
                </div>
              </div>
              <div className="w-full h-2 bg-slate-800 rounded-full overflow-hidden">
                <div
                  className={`h-full rounded-full ${stage.barColor} transition-all duration-500`}
                  style={{ width: `${Math.max(4, stage.pct)}%` }}
                />
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
