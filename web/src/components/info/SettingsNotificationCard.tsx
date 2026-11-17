'use client';

import React, { useState, useEffect } from 'react';
import { Bell } from 'lucide-react';

interface NotificationToggleItemProps {
  label: string;
  description: string;
  checked: boolean;
  onChange: (nextValue: boolean) => void;
}

function NotificationToggleItem({
  label,
  description,
  checked,
  onChange,
}: NotificationToggleItemProps) {
  return (
    <button
      type="button"
      onClick={() => onChange(!checked)}
      className="flex items-center justify-between p-3.5 rounded-xl bg-[#121316] border border-white/10 hover:border-[#DFFF00]/40 transition-all text-left cursor-pointer group"
    >
      <div className="min-w-0 pr-3">
        <div className="text-xs font-bold text-white font-tech group-hover:text-[#DFFF00] transition-colors">
          {label}
        </div>
        <div className="text-[11px] text-white/50 truncate font-tech">{description}</div>
      </div>
      <div
        className={`relative inline-flex h-5 w-9 shrink-0 items-center rounded-full transition-colors ${
          checked ? 'bg-[#DFFF00]' : 'bg-white/10'
        }`}
      >
        <span
          className={`inline-block h-3.5 w-3.5 transform rounded-full bg-[#0A0A0C] transition-transform ${
            checked ? 'translate-x-4' : 'translate-x-1'
          }`}
        />
      </div>
    </button>
  );
}

export default function SettingsNotificationCard() {
  const [notifHighlights, setNotifHighlights] = useState(true);
  const [notifKickoff, setNotifKickoff] = useState(true);
  const [notifGoals, setNotifGoals] = useState(true);
  const [notifFullTime, setNotifFullTime] = useState(true);

  useEffect(() => {
    const timer = setTimeout(() => {
      setNotifHighlights(localStorage.getItem('notif_hl') !== 'false');
      setNotifKickoff(localStorage.getItem('notif_ko') !== 'false');
      setNotifGoals(localStorage.getItem('notif_goals') !== 'false');
      setNotifFullTime(localStorage.getItem('notif_ft') !== 'false');
    }, 0);
    return () => clearTimeout(timer);
  }, []);

  const handleToggle = (key: string, val: boolean, setter: (v: boolean) => void) => {
    setter(val);
    if (typeof window !== 'undefined') {
      localStorage.setItem(key, String(val));
    }
  };

  return (
    <div className="p-4 sm:p-5 rounded-2xl bg-[#1A1C22] border border-white/10 shadow-md space-y-4">
      <div className="flex items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-[#DFFF00]/10 border border-[#DFFF00]/30 flex items-center justify-center shrink-0">
            <Bell className="w-5 h-5 text-[#DFFF00]" />
          </div>
          <div>
            <h3 className="text-sm font-bold text-white font-tech">
              Match &amp; Highlight Notifications
            </h3>
            <p className="text-xs text-white/50 font-tech">
              Real-time alerts tailored for your selected favorite clubs
            </p>
          </div>
        </div>
        <span className="hidden sm:inline-flex px-2.5 py-0.5 rounded-full bg-white/5 border border-white/10 text-[10px] font-tech font-bold text-white/70 uppercase tracking-wider">
          Push Topics
        </span>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5 pt-2 border-t border-white/10">
        <NotificationToggleItem
          label="Highlights Ready"
          description="Instant alert when official match video is linked"
          checked={notifHighlights}
          onChange={(val) => handleToggle('notif_hl', val, setNotifHighlights)}
        />
        <NotificationToggleItem
          label="Kickoff Imminent (15m)"
          description="Pre-match reminder before your club kicks off"
          checked={notifKickoff}
          onChange={(val) => handleToggle('notif_ko', val, setNotifKickoff)}
        />
        <NotificationToggleItem
          label="Live Goal Alerts"
          description="Real-time score updates during active matches"
          checked={notifGoals}
          onChange={(val) => handleToggle('notif_goals', val, setNotifGoals)}
        />
        <NotificationToggleItem
          label="Full Time Result"
          description="Final whistle score summary for followed clubs"
          checked={notifFullTime}
          onChange={(val) => handleToggle('notif_ft', val, setNotifFullTime)}
        />
      </div>
    </div>
  );
}
