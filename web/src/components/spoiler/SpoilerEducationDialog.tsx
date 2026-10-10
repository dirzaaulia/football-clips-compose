'use client';

import React, { useState } from 'react';
import { EyeOff, Eye, Check, ShieldAlert } from 'lucide-react';
import { setSpoilerPreference } from '@/hooks/useSpoilerFree';

interface SpoilerEducationDialogProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function SpoilerEducationDialog({
  isOpen,
  onClose,
}: SpoilerEducationDialogProps) {
  const [selectedSpoilerFree, setSelectedSpoilerFree] = useState(true);

  if (!isOpen) return null;

  const handleSave = () => {
    setSpoilerPreference(selectedSpoilerFree);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Scrim */}
      <div
        className="fixed inset-0 bg-black/80 backdrop-blur-md transition-opacity animate-in fade-in duration-300"
        onClick={handleSave}
      />

      {/* Modal Dialog Card (Material 3 Parity) */}
      <div
        role="dialog"
        aria-modal="true"
        className="relative z-10 w-full max-w-md bg-[#181C20] border border-[#334155] rounded-3xl shadow-[0_0_50px_rgba(0,0,0,0.8)] overflow-hidden p-6 sm:p-7 space-y-5 animate-in zoom-in-95 duration-200"
      >
        {/* Top Icon Badge */}
        <div className="flex flex-col items-center text-center space-y-2.5">
          <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-[#1E3A8A] to-[#7098F8] flex items-center justify-center text-white shadow-[0_0_20px_rgba(112,152,248,0.35)]">
            <ShieldAlert className="w-7 h-7 text-white" />
          </div>
          <div>
            <h2 className="text-xl sm:text-2xl font-black text-white tracking-tight">
              Viewing Preference
            </h2>
            <p className="text-xs text-[#94A3B8] mt-1">
              How would you like to see match scores & highlights?
            </p>
          </div>
        </div>

        {/* Option Cards */}
        <div className="space-y-3">
          {/* Option 1: Spoiler-Free Mode */}
          <div
            onClick={() => setSelectedSpoilerFree(true)}
            className={`p-4 rounded-2xl border transition-all cursor-pointer select-none flex items-start gap-3.5 ${
              selectedSpoilerFree
                ? 'bg-[#1E3A8A]/30 border-[#7098F8] shadow-[0_0_16px_rgba(112,152,248,0.2)]'
                : 'bg-[#101418] border-[#334155]/60 hover:border-[#334155]'
            }`}
          >
            <div
              className={`p-2 rounded-xl shrink-0 mt-0.5 ${
                selectedSpoilerFree
                  ? 'bg-[#7098F8] text-[#101418]'
                  : 'bg-[#1E2227] text-[#94A3B8]'
              }`}
            >
              <EyeOff className="w-4 h-4" />
            </div>
            <div className="flex-1 min-w-0">
              <div className="flex items-center justify-between gap-1">
                <span className="text-sm font-bold text-white">Spoiler-Free Mode</span>
                <span className="text-[10px] font-black uppercase px-2 py-0.5 rounded-full bg-[#1E3A8A]/60 text-[#4FC3F7] border border-[#7098F8]/40">
                  Recommended
                </span>
              </div>
              <p className="text-xs text-[#94A3B8] mt-0.5 leading-relaxed">
                Scores and thumbnails are blurred until revealed. Watch highlights without knowing the result!
              </p>
            </div>
            {selectedSpoilerFree && (
              <Check className="w-4 h-4 text-[#7098F8] shrink-0 mt-1" />
            )}
          </div>

          {/* Option 2: Standard Mode */}
          <div
            onClick={() => setSelectedSpoilerFree(false)}
            className={`p-4 rounded-2xl border transition-all cursor-pointer select-none flex items-start gap-3.5 ${
              !selectedSpoilerFree
                ? 'bg-[#1E3A8A]/30 border-[#7098F8] shadow-[0_0_16px_rgba(112,152,248,0.2)]'
                : 'bg-[#101418] border-[#334155]/60 hover:border-[#334155]'
            }`}
          >
            <div
              className={`p-2 rounded-xl shrink-0 mt-0.5 ${
                !selectedSpoilerFree
                  ? 'bg-[#7098F8] text-[#101418]'
                  : 'bg-[#1E2227] text-[#94A3B8]'
              }`}
            >
              <Eye className="w-4 h-4" />
            </div>
            <div className="flex-1 min-w-0">
              <span className="text-sm font-bold text-white block">Standard Mode</span>
              <p className="text-xs text-[#94A3B8] mt-0.5 leading-relaxed">
                Scores and match results are immediately visible for instant check-ins.
              </p>
            </div>
            {!selectedSpoilerFree && (
              <Check className="w-4 h-4 text-[#7098F8] shrink-0 mt-1" />
            )}
          </div>
        </div>

        {/* CTA Button */}
        <button
          onClick={handleSave}
          className="w-full py-3 px-5 rounded-2xl bg-gradient-to-r from-[#1E3A8A] via-[#7098F8] to-[#4FC3F7] text-white font-extrabold text-sm shadow-[0_0_20px_rgba(112,152,248,0.35)] hover:brightness-110 transition-all cursor-pointer"
        >
          Save Preference & Continue
        </button>

        <p className="text-[11px] text-[#94A3B8] text-center">
          You can toggle this at any time in the sidebar menu.
        </p>
      </div>
    </div>
  );
}
