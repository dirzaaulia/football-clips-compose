'use client';

import React, { useState, useMemo } from 'react';
import { UserAnalyticsEvent, AppSessionItem } from '../../types';
import {
  Smartphone,
  Globe,
  Clock,
  ArrowRight,
  Eye,
  Layers,
  MousePointerClick,
  PlayCircle,
  ExternalLink,
  Filter,
  Heart,
  Sparkles,
  Calendar,
  Activity,
} from 'lucide-react';

interface SessionJourneyExplorerProps {
  events: UserAnalyticsEvent[];
  appSessions?: AppSessionItem[];
}

export function formatEventNarrative(ev: UserAnalyticsEvent) {
  const p = ev.payload || {};
  switch (ev.event_name) {
    case 'screen_view':
      return {
        title: `Viewed ${p.screen_name || 'Screen'}`,
        subtitle: p.tab ? `Tab: ${p.tab}` : undefined,
        Icon: Eye,
        color: 'text-indigo-400 bg-indigo-500/10 border-indigo-500/20',
      };
    case 'tab_switch':
      return {
        title: `Switched Tab to "${p.tab_name || p.tab || 'Tab'}"`,
        subtitle: undefined,
        Icon: Layers,
        color: 'text-cyan-400 bg-cyan-500/10 border-cyan-500/20',
      };
    case 'match_click':
      return {
        title: `Selected Match: ${p.match_title || `${p.home_team || 'Home'} vs ${p.away_team || 'Away'}`}`,
        subtitle: p.competition_name ? `Competition: ${p.competition_name}` : undefined,
        Icon: MousePointerClick,
        color: 'text-purple-400 bg-purple-500/10 border-purple-500/20',
      };
    case 'video_play':
      return {
        title: `Played Highlight: ${p.match_title || p.video_title || 'Match Video'}`,
        subtitle: p.duration ? `Duration: ${p.duration}s` : (p.video_id ? `Video ID: ${p.video_id}` : undefined),
        Icon: PlayCircle,
        color: 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20',
      };
    case 'watch_on_youtube':
      return {
        title: `Tapped "Watch on YouTube"`,
        subtitle: p.video_id ? `Video ID: ${p.video_id}` : undefined,
        Icon: ExternalLink,
        color: 'text-amber-400 bg-amber-500/10 border-amber-500/20',
      };
    case 'competition_filter':
      return {
        title: `Filtered Competition: ${p.competition_name || p.competition_id || 'Competition'}`,
        subtitle: undefined,
        Icon: Filter,
        color: 'text-blue-400 bg-blue-500/10 border-blue-500/20',
      };
    case 'club_favorite':
      return {
        title: `${p.action === 'remove' ? 'Removed' : 'Added'} Favorite Club: ${p.club_name || p.club_id || 'Club'}`,
        subtitle: undefined,
        Icon: Heart,
        color: 'text-rose-400 bg-rose-500/10 border-rose-500/20',
      };
    case 'session_start':
      return {
        title: `App Session Launched`,
        subtitle: p.app_version ? `Version: ${p.app_version}` : undefined,
        Icon: Sparkles,
        color: 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20',
      };
    case 'date_filter':
      return {
        title: `Changed Fixture Date: ${p.date || 'selected date'}`,
        subtitle: undefined,
        Icon: Calendar,
        color: 'text-teal-400 bg-teal-500/10 border-teal-500/20',
      };
    default:
      return {
        title: ev.event_name.replace(/_/g, ' '),
        subtitle: Object.keys(p).length > 0 ? JSON.stringify(p) : undefined,
        Icon: Activity,
        color: 'text-slate-400 bg-slate-800 border-slate-700',
      };
  }
}

interface GroupedSession {
  sessionId: string;
  platform: string;
  deviceId: string;
  startTime: string;
  events: UserAnalyticsEvent[];
}

export function SessionJourneyExplorer({ events, appSessions = [] }: SessionJourneyExplorerProps) {
  const sessions = useMemo(() => {
    const sessionMap = new Map<string, GroupedSession>();

    events.forEach((ev) => {
      const sId = (ev.payload?.session_id as string) || ev.device_id || `legacy-${ev.platform}-${ev.id}`;
      if (!sessionMap.has(sId)) {
        sessionMap.set(sId, {
          sessionId: sId,
          platform: ev.platform || 'android',
          deviceId: ev.device_id || 'anonymous',
          startTime: ev.created_at,
          events: [],
        });
      }
      sessionMap.get(sId)!.events.push(ev);
    });

    const list = Array.from(sessionMap.values()).map((s) => ({
      ...s,
      events: s.events.sort((a, b) => new Date(a.created_at).getTime() - new Date(b.created_at).getTime()),
    }));

    return list.sort((a, b) => new Date(b.startTime).getTime() - new Date(a.startTime).getTime());
  }, [events]);

  const [selectedSessionId, setSelectedSessionId] = useState<string | null>(null);

  const activeSession = useMemo(() => {
    if (!sessions.length) return null;
    return sessions.find((s) => s.sessionId === selectedSessionId) || sessions[0];
  }, [sessions, selectedSessionId]);

  if (sessions.length === 0) {
    return (
      <div className="p-8 text-center text-slate-500 font-mono text-xs bg-slate-900/40 rounded-xl border border-slate-800">
        No active user sessions recorded yet.
      </div>
    );
  }

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-3 border-b border-slate-800">
        <div>
          <h4 className="text-xs font-bold font-mono text-slate-100 uppercase tracking-wider">
            Session Journey Explorer
          </h4>
          <p className="text-[11px] text-slate-400 font-mono">
            Step-by-step chronological user pathways through the app
          </p>
        </div>
        <span className="text-xs font-mono text-cyan-400 font-semibold">
          {sessions.length} Recorded Sessions
        </span>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-12 gap-5">
        {/* Left: Session Picker */}
        <div className="md:col-span-5 space-y-2 max-h-[420px] overflow-y-auto pr-1">
          <p className="text-[10px] font-mono text-slate-500 uppercase tracking-wider">Recent Sessions</p>
          {sessions.map((s) => {
            const isSelected = activeSession?.sessionId === s.sessionId;
            const isAndroid = s.platform === 'android';
            const firstEvent = s.events[0];
            const lastEvent = s.events[s.events.length - 1];
            const durationSec = Math.round(
              (new Date(lastEvent.created_at).getTime() - new Date(firstEvent.created_at).getTime()) / 1000,
            );

            return (
              <button
                key={s.sessionId}
                onClick={() => setSelectedSessionId(s.sessionId)}
                className={`w-full text-left p-3 rounded-xl border transition flex flex-col gap-1.5 ${
                  isSelected
                    ? 'bg-cyan-500/10 border-cyan-500/40 shadow-sm'
                    : 'bg-slate-950/40 border-slate-800/80 hover:bg-slate-800/40'
                }`}
              >
                <div className="flex items-center justify-between">
                  <span
                    className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-[10px] font-mono font-bold ${
                      isAndroid ? 'bg-cyan-500/20 text-cyan-400' : 'bg-indigo-500/20 text-indigo-400'
                    }`}
                  >
                    {isAndroid ? <Smartphone className="w-3 h-3" /> : <Globe className="w-3 h-3" />}
                    {s.platform}
                  </span>
                  <span className="text-[10px] font-mono text-slate-400">
                    {new Date(s.startTime).toLocaleTimeString('id-ID', {
                      timeZone: 'Asia/Jakarta',
                      hour: '2-digit',
                      minute: '2-digit',
                    })}{' '}
                    WIB
                  </span>
                </div>

                <div className="flex items-center justify-between text-xs font-mono">
                  <span className="text-slate-300 font-bold truncate max-w-[140px]">
                    {s.deviceId.slice(0, 14)}...
                  </span>
                  <span className="text-slate-400">{s.events.length} steps · {durationSec}s</span>
                </div>
              </button>
            );
          })}
        </div>

        {/* Right: Chronological Pathway Timeline */}
        <div className="md:col-span-7 bg-slate-950/60 rounded-xl p-4 border border-slate-800/80 space-y-4 max-h-[420px] overflow-y-auto">
          {activeSession ? (
            <div className="space-y-4">
              <div className="flex items-center justify-between pb-2 border-b border-slate-800/60 text-xs font-mono">
                <span className="text-slate-300 font-bold">
                  User Journey: {activeSession.events.length} Actions
                </span>
                <span className="text-cyan-400">ID: {activeSession.sessionId.slice(0, 16)}</span>
              </div>

              <div className="relative pl-6 space-y-4 before:absolute before:left-2 before:top-2 before:bottom-2 before:w-0.5 before:bg-slate-800">
                {activeSession.events.map((ev, idx) => {
                  const { title, subtitle, Icon, color } = formatEventNarrative(ev);
                  return (
                    <div key={ev.id} className="relative group">
                      <div className={`absolute -left-6 top-1 p-1 rounded-full border ${color}`}>
                        <Icon className="w-2.5 h-2.5" />
                      </div>

                      <div className="space-y-0.5">
                        <div className="flex items-center justify-between gap-2">
                          <p className="text-xs font-bold text-slate-200">{title}</p>
                          <span className="text-[10px] font-mono text-slate-500">
                            {new Date(ev.created_at).toLocaleTimeString('id-ID', {
                              timeZone: 'Asia/Jakarta',
                              hour: '2-digit',
                              minute: '2-digit',
                              second: '2-digit',
                            })}
                          </span>
                        </div>
                        {subtitle && <p className="text-[11px] font-mono text-slate-400">{subtitle}</p>}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          ) : (
            <div className="p-8 text-center text-slate-500 font-mono text-xs">
              Select a session to view its chronological pathway.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
