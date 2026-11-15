'use client';

import React, { useEffect } from 'react';
import Image from 'next/image';
import { X, Sparkles, Bell, EyeOff, ShieldCheck, QrCode } from 'lucide-react';
import GooglePlayBadge from './GooglePlayBadge';

const PLAY_STORE_URL = 'https://play.google.com/store/apps/details?id=com.dirzaaulia.footballclips';
const QR_API_URL = `https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=${encodeURIComponent(PLAY_STORE_URL)}&bgcolor=10-14-18&color=ffffff&margin=2`;

interface GooglePlayDownloadModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function GooglePlayDownloadModal({
  isOpen,
  onClose,
}: GooglePlayDownloadModalProps) {
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    if (isOpen) {
      window.addEventListener('keydown', handleKeyDown);
      document.body.style.overflow = 'hidden';
    }
    return () => {
      window.removeEventListener('keydown', handleKeyDown);
      document.body.style.overflow = '';
    };
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-in fade-in duration-200">
      <div
        className="relative w-full max-w-lg rounded-3xl bg-[#0F141C] border border-white/10 shadow-[0_0_60px_rgba(59,130,246,0.25)] p-6 sm:p-8 space-y-6 overflow-hidden"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Ambient Cinema Lighting */}
        <div className="absolute -top-24 -right-24 w-60 h-60 bg-blue-600/20 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute -bottom-24 -left-24 w-60 h-60 bg-cyan-500/20 rounded-full blur-3xl pointer-events-none" />

        {/* Header Row */}
        <div className="flex items-start justify-between relative z-10">
          <div className="flex items-center gap-3.5">
            <div className="relative w-12 h-12 rounded-2xl overflow-hidden border border-cyan-500/30 bg-gradient-to-br from-blue-600/30 to-cyan-500/20 shadow-lg shadow-cyan-500/10 shrink-0">
              <Image
                src="/app_icon.png"
                alt="Football Highlights & Clips"
                width={48}
                height={48}
                className="object-cover"
              />
            </div>
            <div>
              <div className="flex items-center gap-1.5">
                <span className="text-[10px] font-black uppercase tracking-wider text-cyan-400 bg-cyan-950/80 px-2 py-0.5 rounded-full border border-cyan-500/30">
                  Official Android App
                </span>
                <span className="text-[10px] font-bold text-slate-400">v3.0</span>
              </div>
              <h3 className="text-lg font-black text-white tracking-tight mt-0.5">
                Football Highlights &amp; Clips
              </h3>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-xl text-slate-400 hover:text-white hover:bg-white/10 transition-colors cursor-pointer"
            aria-label="Close modal"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Feature Highlights Grid */}
        <div className="grid grid-cols-2 gap-3 relative z-10">
          <div className="p-3 rounded-2xl bg-[#161D26] border border-white/5 space-y-1">
            <div className="flex items-center gap-1.5 text-xs font-bold text-white">
              <Sparkles className="w-3.5 h-3.5 text-cyan-400" />
              <span>60FPS HD Highlights</span>
            </div>
            <p className="text-[11px] text-slate-400">Ad-free player with official club &amp; league feeds.</p>
          </div>
          <div className="p-3 rounded-2xl bg-[#161D26] border border-white/5 space-y-1">
            <div className="flex items-center gap-1.5 text-xs font-bold text-white">
              <Bell className="w-3.5 h-3.5 text-amber-400" />
              <span>Instant Goal Alerts</span>
            </div>
            <p className="text-[11px] text-slate-400">Live semi-realtime score push notifications.</p>
          </div>
          <div className="p-3 rounded-2xl bg-[#161D26] border border-white/5 space-y-1">
            <div className="flex items-center gap-1.5 text-xs font-bold text-white">
              <EyeOff className="w-3.5 h-3.5 text-emerald-400" />
              <span>Spoiler-Free Mode</span>
            </div>
            <p className="text-[11px] text-slate-400">Hide scores and blur thumbnails until ready.</p>
          </div>
          <div className="p-3 rounded-2xl bg-[#161D26] border border-white/5 space-y-1">
            <div className="flex items-center gap-1.5 text-xs font-bold text-white">
              <ShieldCheck className="w-3.5 h-3.5 text-purple-400" />
              <span>Google Play Verified</span>
            </div>
            <p className="text-[11px] text-slate-400">Target SDK 37, strict privacy &amp; zero background drain.</p>
          </div>
        </div>

        {/* Dual Mode: QR Code for Desktop, 1-Tap button for Mobile */}
        <div className="p-4 rounded-2xl bg-gradient-to-r from-blue-950/40 via-[#161D26] to-cyan-950/40 border border-white/10 flex flex-col sm:flex-row items-center justify-between gap-4 relative z-10">
          <div className="space-y-2 text-center sm:text-left">
            <span className="text-xs font-bold text-slate-300">Scan QR or tap to download:</span>
            <div>
              <GooglePlayBadge height={44} />
            </div>
          </div>

          {/* Desktop QR Code */}
          <div className="flex flex-col items-center gap-1 shrink-0 p-2 bg-[#0A0E14] rounded-xl border border-white/10">
            <img
              src={QR_API_URL}
              alt="Scan to download on Google Play"
              width={96}
              height={96}
              className="rounded-lg"
            />
            <span className="text-[9px] font-bold text-slate-400 flex items-center gap-1">
              <QrCode className="w-2.5 h-2.5" />
              Scan with camera
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}
