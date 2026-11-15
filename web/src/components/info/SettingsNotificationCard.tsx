'use client';

import React, { useState, useEffect } from 'react';
import { Bell } from 'lucide-react';

export default function SettingsNotificationCard() {
  const [notifHighlights, setNotifHighlights] = useState(true);
  const [notifKickoff, setNotifKickoff] = useState(true);
  const [notifGoals, setNotifGoals] = useState(true);
  const [notifFullTime, setNotifFullTime] = useState(true);

  useEffect(() => {
    if (typeof window !== 'undefined') {
      setNotifHighlights(localStorage.getItem('notif_hl') !== 'false');
      setNotifKickoff(localStorage.getItem('notif_ko') !== 'false');
      setNotifGoals(localStorage.getItem('notif_goals') !== 'false');
      setNotifFullTime(localStorage.getItem('notif_ft') !== 'false');
    }
  }, []);

  const handleToggle = (key: string, val: boolean, setter: (v: boolean) => void) => {
    setter(val);
    if (typeof window !== 'undefined') {
      localStorage.setItem(key, String(val));
    }
  };

  return (
    <div className="p-4 sm:p-5 rounded-2xl bg-[#181C20] border border-[#334155]/80 shadow-md space-y-3">
      <div className="flex items-center gap-3">
        <div className="w-10 h-10 rounded-xl bg-[#7098F8]/15 border border-[#7098F8]/30 flex items-center justify-center shrink-0">
          <Bell className="w-5 h-5 text-[#7098F8]" />
        </div>
        <div>
          <h3 className="text-sm font-bold text-white">Match &amp; Highlight Notifications</h3>
          <p className="text-xs text-[#94A3B8]">Alerts tailored for your selected favorite clubs</p>
        </div>
      </div>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 pt-2 border-t border-[#334155]/40 text-xs">
        <label className="flex items-center justify-between p-2.5 rounded-xl bg-[#101418] border border-white/5 cursor-pointer">
          <span className="text-gray-300 font-medium">Highlights Ready</span>
          <input
            type="checkbox"
            checked={notifHighlights}
            onChange={(e) => handleToggle('notif_hl', e.target.checked, setNotifHighlights)}
            className="accent-[#7098F8] w-4 h-4 rounded cursor-pointer"
          />
        </label>
        <label className="flex items-center justify-between p-2.5 rounded-xl bg-[#101418] border border-white/5 cursor-pointer">
          <span className="text-gray-300 font-medium">Kickoff Imminent (15m)</span>
          <input
            type="checkbox"
            checked={notifKickoff}
            onChange={(e) => handleToggle('notif_ko', e.target.checked, setNotifKickoff)}
            className="accent-[#7098F8] w-4 h-4 rounded cursor-pointer"
          />
        </label>
        <label className="flex items-center justify-between p-2.5 rounded-xl bg-[#101418] border border-white/5 cursor-pointer">
          <span className="text-gray-300 font-medium">Live Goal Alerts</span>
          <input
            type="checkbox"
            checked={notifGoals}
            onChange={(e) => handleToggle('notif_goals', e.target.checked, setNotifGoals)}
            className="accent-[#7098F8] w-4 h-4 rounded cursor-pointer"
          />
        </label>
        <label className="flex items-center justify-between p-2.5 rounded-xl bg-[#101418] border border-white/5 cursor-pointer">
          <span className="text-gray-300 font-medium">Full Time Result</span>
          <input
            type="checkbox"
            checked={notifFullTime}
            onChange={(e) => handleToggle('notif_ft', e.target.checked, setNotifFullTime)}
            className="accent-[#7098F8] w-4 h-4 rounded cursor-pointer"
          />
        </label>
      </div>
    </div>
  );
}
