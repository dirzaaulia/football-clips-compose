import {
  Sparkles,
  Activity,
  Rss,
  Bell,
  BarChart3,
  Radio,
  Film,
} from 'lucide-react';
import { TelemetryTab } from '../types';

interface TelemetryNavigationRailProps {
  activeTab: TelemetryTab;
  onTabChange: (tab: TelemetryTab) => void;
  unlinkedCount: number;
  liveMatchesCount: number;
  notificationsCount: number;
  analyticsCount?: number;
  websubStatus?: string;
  isLiveActive: boolean;
}

export function TelemetryNavigationRail({
  activeTab,
  onTabChange,
  unlinkedCount,
  liveMatchesCount,
  notificationsCount,
  analyticsCount = 0,
  websubStatus,
  isLiveActive,
}: TelemetryNavigationRailProps) {
  const navItems = [
    {
      id: 'highlights' as TelemetryTab,
      label: 'Highlights & AI Verifier',
      subtitle: '4-Tier Waterfall & AI Verifier',
      icon: Sparkles,
      badge: unlinkedCount > 0 ? `${unlinkedCount} seek` : undefined,
      badgeColor: 'bg-amber-500/20 text-amber-400 border border-amber-500/30',
    },
    {
      id: 'fixtures' as TelemetryTab,
      label: 'Live Scores & Fixtures',
      subtitle: 'ESPN 60s Live Ticker & Sync',
      icon: Activity,
      badge: liveMatchesCount > 0 ? `${liveMatchesCount} live` : undefined,
      badgeColor: 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 animate-pulse',
    },
    {
      id: 'shorts' as TelemetryTab,
      label: 'Shorts Pipeline & AI',
      subtitle: 'Zero-Quota RSS & AI Classifier',
      icon: Film,
      badge: 'active',
      badgeColor: 'bg-purple-500/20 text-purple-400 border border-purple-500/30',
    },
    {
      id: 'websub' as TelemetryTab,
      label: 'WebSub Push Webhooks',
      subtitle: 'YouTube Sub-100ms Push',
      icon: Rss,
      badge: websubStatus ? 'active' : undefined,
      badgeColor: 'bg-cyan-500/20 text-cyan-400 border border-cyan-500/30',
    },

    {
      id: 'notifications' as TelemetryTab,
      label: 'MyClubs & FCM Alerts',
      subtitle: 'Favorite Club Dispatches',
      icon: Bell,
      badge: notificationsCount > 0 ? `${notificationsCount}` : undefined,
      badgeColor: 'bg-indigo-500/20 text-indigo-400 border border-indigo-500/30',
    },
    {
      id: 'analytics' as TelemetryTab,
      label: 'Analytics',
      subtitle: 'User Events & Actions',
      icon: BarChart3,
      badge: analyticsCount > 0 ? `${analyticsCount}` : undefined,
      badgeColor: 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30',
    },
  ];

  return (
    <aside className="w-full lg:w-72 shrink-0 space-y-4">
      {/* Platform Status Card */}
      <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-4 space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="flex h-2.5 w-2.5 rounded-full bg-emerald-400 animate-ping" />
            <span className="text-xs font-mono font-bold uppercase tracking-wider text-slate-300">
              Ops Control Hub
            </span>
          </div>
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[10px] font-mono font-bold bg-slate-800 text-slate-300 border border-slate-700">
            <Radio className="w-3 h-3 text-emerald-400" />
            {isLiveActive ? 'Live Mode' : 'Idle'}
          </span>
        </div>
        <p className="text-xs text-slate-400 leading-relaxed">
          Industry-grade Go micro-daemon telemetry across YouTube highlights, live scores, shorts, WebSub, and FCM alerts.
        </p>
      </div>

      {/* Navigation Buttons */}
      <nav className="bg-slate-900/90 border border-slate-800 rounded-2xl p-2 flex flex-row lg:flex-col gap-1.5 overflow-x-auto lg:overflow-x-visible">
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = activeTab === item.id;
          return (
            <button
              key={item.id}
              onClick={() => onTabChange(item.id)}
              className={`w-full text-left p-3 rounded-xl transition flex items-center justify-between gap-3 shrink-0 lg:shrink select-none ${
                isActive
                  ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-600/20 border border-indigo-500'
                  : 'text-slate-300 hover:bg-slate-800/80 hover:text-white border border-transparent'
              }`}
            >
              <div className="flex items-center gap-3 min-w-0">
                <div
                  className={`p-2 rounded-lg shrink-0 ${
                    isActive ? 'bg-white/20 text-white' : 'bg-slate-800 text-slate-400'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                </div>
                <div className="min-w-0">
                  <p className="text-xs font-bold truncate leading-tight">{item.label}</p>
                  <p
                    className={`text-[10px] truncate leading-tight mt-0.5 ${
                      isActive ? 'text-indigo-200' : 'text-slate-400'
                    }`}
                  >
                    {item.subtitle}
                  </p>
                </div>
              </div>

              {item.badge && (
                <span
                  className={`px-2 py-0.5 rounded text-[10px] font-mono font-bold shrink-0 ${
                    isActive ? 'bg-white/20 text-white border border-white/30' : item.badgeColor
                  }`}
                >
                  {item.badge}
                </span>
              )}
            </button>
          );
        })}
      </nav>
    </aside>
  );
}
