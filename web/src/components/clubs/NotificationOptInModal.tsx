'use client';

import React from 'react';
import { Bell, Film, PlayCircle, Trophy, X, ShieldAlert } from 'lucide-react';

interface NotificationOptInModalProps {
  isOpen: boolean;
  onClose: () => void;
  onEnable: () => void;
  isDenied?: boolean;
}

export default function NotificationOptInModal({
  isOpen,
  onClose,
  onEnable,
  isDenied = false
}: NotificationOptInModalProps) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-fade-in">
      <div className="relative w-full max-w-md bg-[#181C20] border border-[#2B3545] rounded-2xl shadow-2xl p-6 overflow-hidden">
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition"
          aria-label="Close"
        >
          <X className="w-4 h-4" />
        </button>

        {/* Header Icon */}
        <div className="flex flex-col items-center text-center mb-6">
          <div className="w-14 h-14 rounded-full bg-blue-500/10 border border-blue-500/30 flex items-center justify-center mb-3">
            {isDenied ? (
              <ShieldAlert className="w-7 h-7 text-amber-400" />
            ) : (
              <Bell className="w-7 h-7 text-blue-400" />
            )}
          </div>
          <h2 className="text-xl font-bold text-white tracking-tight">
            {isDenied ? 'Notifications Are Blocked' : 'Never Miss a Match Moment'}
          </h2>
          <p className="text-xs text-slate-400 mt-1 max-w-xs">
            {isDenied
              ? 'Your browser currently blocks alerts. Click the lock/tune icon in your address bar to allow notifications.'
              : 'Receive real-time match and highlight alerts for your favorite clubs in this browser.'}
          </p>
        </div>

        {/* Feature List */}
        {!isDenied && (
          <div className="space-y-3 mb-6">
            <div className="flex items-center gap-3 p-3 rounded-xl bg-[#202734] border border-[#2B3545]/60">
              <div className="w-8 h-8 rounded-lg bg-blue-500/20 flex items-center justify-center shrink-0">
                <PlayCircle className="w-4 h-4 text-blue-400" />
              </div>
              <div className="text-left">
                <h4 className="text-xs font-semibold text-white">Kickoff & Full-Time Alerts</h4>
                <p className="text-[11px] text-slate-400">Get notified 30m before kickoff and at final whistle.</p>
              </div>
            </div>

            <div className="flex items-center gap-3 p-3 rounded-xl bg-[#202734] border border-[#2B3545]/60">
              <div className="w-8 h-8 rounded-lg bg-amber-500/20 flex items-center justify-center shrink-0">
                <Trophy className="w-4 h-4 text-amber-400" />
              </div>
              <div className="text-left">
                <h4 className="text-xs font-semibold text-white">Live Goal Updates</h4>
                <p className="text-[11px] text-slate-400">Instant notification when your saved club scores.</p>
              </div>
            </div>

            <div className="flex items-center gap-3 p-3 rounded-xl bg-[#202734] border border-[#2B3545]/60">
              <div className="w-8 h-8 rounded-lg bg-emerald-500/20 flex items-center justify-center shrink-0">
                <Film className="w-4 h-4 text-emerald-400" />
              </div>
              <div className="text-left">
                <h4 className="text-xs font-semibold text-white">Official Video Highlights</h4>
                <p className="text-[11px] text-slate-400">Stream broadcast highlight packages the second they drop.</p>
              </div>
            </div>
          </div>
        )}

        {/* Action Buttons */}
        <div className="flex flex-col gap-2">
          {!isDenied ? (
            <button
              onClick={onEnable}
              className="w-full py-2.5 px-4 bg-blue-600 hover:bg-blue-500 text-white font-semibold text-sm rounded-xl shadow-lg shadow-blue-500/20 transition cursor-pointer"
            >
              Turn On Club Alerts
            </button>
          ) : (
            <button
              onClick={onClose}
              className="w-full py-2.5 px-4 bg-slate-800 hover:bg-slate-700 text-white font-semibold text-sm rounded-xl transition cursor-pointer"
            >
              Understood
            </button>
          )}

          <button
            onClick={onClose}
            className="w-full py-2 px-4 text-slate-400 hover:text-white text-xs font-medium transition cursor-pointer"
          >
            {isDenied ? 'Close' : 'Maybe Later'}
          </button>
        </div>
      </div>
    </div>
  );
}
