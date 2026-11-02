import React, { useState, useMemo } from 'react';
import {
  PlayCircle,
  Clock,
  Sparkles,
  Copy,
  Check,
  Users,
  Smartphone,
} from 'lucide-react';
import { NotificationItem, NotificationsPayload, SubscriptionItem } from '../types';

interface NotificationsPipelineViewProps {
  notificationsPayload?: NotificationsPayload | null;
  subscriptions?: SubscriptionItem[];
}

export function NotificationsPipelineView({
  notificationsPayload,
  subscriptions = [],
}: NotificationsPipelineViewProps) {
  const [filterType, setFilterType] = useState<string>('ALL');
  const [copiedId, setCopiedId] = useState<string | null>(null);

  // Filter out any 0-device, dummy, or corrupt test items
  const realNotifications = useMemo(() => {
    const raw = notificationsPayload?.notifications || [];
    return raw.filter((n) => {
      const devCount = n.audience?.total_devices ?? n.audience?.target_tokens ?? 0;
      if (devCount <= 0) return false;
      const title = n.title || '';
      if (title.includes('Home vs Away') || title.includes('Home Team') || title.toLowerCase().includes('dummy')) {
        return false;
      }
      return true;
    });
  }, [notificationsPayload]);

  const metrics = useMemo(() => {
    const goals = realNotifications.filter((n) => n.event_type === 'GOAL' || n.event_type === 'score').length;
    const kickoffs = realNotifications.filter((n) => n.event_type === 'KICKOFF' || n.event_type === 'kickoff').length;
    const highlights = realNotifications.filter(
      (n) => n.event_type === 'HIGHLIGHT_READY' || n.event_type === 'highlight',
    ).length;
    return { total: realNotifications.length, goals, kickoffs, highlights };
  }, [realNotifications]);

  const filteredNotifications = useMemo(() => {
    if (filterType === 'ALL') return realNotifications;
    return realNotifications.filter((n) => {
      const et = (n.event_type || '').toUpperCase();
      if (filterType === 'GOAL') return et === 'GOAL' || et === 'SCORE';
      if (filterType === 'KICKOFF') return et === 'KICKOFF';
      if (filterType === 'HIGHLIGHT') return et === 'HIGHLIGHT_READY' || et === 'HIGHLIGHT';
      if (filterType === 'FULLTIME') return et === 'FULL_TIME' || et === 'FULLTIME';
      return true;
    });
  }, [realNotifications, filterType]);

  const handleCopyNotif = (notif: NotificationItem) => {
    navigator.clipboard.writeText(JSON.stringify(notif, null, 2));
    setCopiedId(notif.id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  return (
    <div className="space-y-5">
      {/* Metrics Row */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-4">
          <p className="text-[11px] font-mono text-slate-400 uppercase tracking-wider">Dispatched Alerts</p>
          <p className="text-xl font-bold font-mono text-white mt-1">{metrics.total}</p>
        </div>
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-4">
          <p className="text-[11px] font-mono text-slate-400 uppercase tracking-wider">Goal Alerts</p>
          <p className="text-xl font-bold font-mono text-emerald-400 mt-1">{metrics.goals}</p>
        </div>
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-4">
          <p className="text-[11px] font-mono text-slate-400 uppercase tracking-wider">Kickoff Alerts</p>
          <p className="text-xl font-bold font-mono text-indigo-400 mt-1">{metrics.kickoffs}</p>
        </div>
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-4">
          <p className="text-[11px] font-mono text-slate-400 uppercase tracking-wider">Active Devices</p>
          <p className="text-xl font-bold font-mono text-cyan-400 mt-1">{subscriptions.length}</p>
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1">
        {['ALL', 'GOAL', 'KICKOFF', 'HIGHLIGHT', 'FULLTIME'].map((ft) => (
          <button
            key={ft}
            onClick={() => setFilterType(ft)}
            className={`px-3 py-1.5 rounded-lg text-xs font-mono font-semibold transition shrink-0 ${
              filterType === ft
                ? 'bg-indigo-600 text-white shadow-sm'
                : 'bg-slate-900 text-slate-400 hover:text-slate-200 hover:bg-slate-800 border border-slate-800'
            }`}
          >
            {ft}
          </button>
        ))}
      </div>

      {/* Notification Stream */}
      <div className="space-y-3">
        {filteredNotifications.length === 0 ? (
          <div className="p-12 text-center text-slate-500 font-mono text-sm bg-slate-900/40 rounded-xl border border-slate-800">
            No FCM notifications dispatched yet. Real notifications will appear here when subscribed club match events trigger for registered devices.
          </div>
        ) : (
          filteredNotifications.map((notif) => {
            const et = (notif.event_type || '').toUpperCase();
            const isGoal = et === 'GOAL' || et === 'SCORE';
            const isKickoff = et === 'KICKOFF';
            const isHighlight = et === 'HIGHLIGHT_READY' || et === 'HIGHLIGHT';
            const totalDevices = notif.audience?.total_devices ?? notif.audience?.target_tokens ?? 0;
            const distinctUsers = notif.audience?.distinct_users ?? 0;

            return (
              <div
                key={notif.id}
                className="bg-slate-900/80 border border-slate-800 rounded-xl p-4 space-y-3 hover:bg-slate-800/40 transition"
              >
                <div className="flex items-center justify-between gap-3">
                  <div className="flex items-center gap-2">
                    <span
                      className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold font-mono ${
                        isGoal
                          ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/30'
                          : isKickoff
                          ? 'bg-indigo-500/15 text-indigo-400 border border-indigo-500/30'
                          : isHighlight
                          ? 'bg-amber-500/15 text-amber-400 border border-amber-500/30'
                          : 'bg-slate-700 text-slate-300'
                      }`}
                    >
                      {isGoal ? <PlayCircle className="w-3.5 h-3.5" /> : isKickoff ? <Clock className="w-3.5 h-3.5" /> : <Sparkles className="w-3.5 h-3.5" />}
                      {notif.event_type.toUpperCase()}
                    </span>
                    {notif.home_team && notif.away_team && notif.home_team !== 'Home' ? (
                      <span className="text-xs font-bold text-white">
                        {notif.home_team} vs {notif.away_team}
                      </span>
                    ) : (
                      <span className="text-xs font-mono text-slate-400">Match #{notif.match_id}</span>
                    )}
                  </div>

                  <div className="flex items-center gap-2">
                    <span className="text-[11px] font-mono text-slate-500">
                      {new Date(notif.timestamp).toLocaleTimeString('id-ID', {
                        timeZone: 'Asia/Jakarta',
                        hour: '2-digit',
                        minute: '2-digit',
                      })}{' '}
                      WIB
                    </span>
                    <button
                      onClick={() => handleCopyNotif(notif)}
                      title="Copy notification JSON"
                      className="p-1.5 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800 border border-slate-800"
                    >
                      {copiedId === notif.id ? (
                        <Check className="w-3.5 h-3.5 text-emerald-400" />
                      ) : (
                        <Copy className="w-3.5 h-3.5" />
                      )}
                    </button>
                  </div>
                </div>

                <div>
                  <h4 className="text-sm font-bold text-white">{notif.title}</h4>
                  <p className="text-xs text-slate-300 mt-0.5">{notif.body}</p>
                </div>

                <div className="pt-2 border-t border-slate-800/80 flex flex-wrap items-center justify-between gap-2 text-xs font-mono text-slate-400">
                  <div className="flex items-center gap-1.5">
                    <Users className="w-3.5 h-3.5 text-indigo-400" />
                    <span>Target Club IDs: [{notif.club_ids.join(', ') || 'Global'}]</span>
                  </div>

                  <div className="flex items-center gap-2 text-[11px]">
                    <Smartphone className="w-3.5 h-3.5 text-emerald-400" />
                    <span className="text-emerald-400 font-bold">
                      {totalDevices} device{totalDevices === 1 ? '' : 's'}
                    </span>
                    {distinctUsers > 0 && (
                      <span className="text-slate-400">({distinctUsers} user{distinctUsers === 1 ? '' : 's'})</span>
                    )}
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
