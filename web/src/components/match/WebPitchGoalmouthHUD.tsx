'use client';

import React from 'react';

interface WebPitchGoalmouthHUDProps {
  goalPosY: number | null | undefined;
  outcomeLabel: string;
  xg: number;
  xgot: number;
}

export default function WebPitchGoalmouthHUD({
  goalPosY,
  outcomeLabel,
  xg,
  xgot,
}: WebPitchGoalmouthHUDProps) {
  const normY = goalPosY != null ? goalPosY : 50;
  // Goal width is in range [44.8..55.2]. Map to SVG canvas width 100 [15..85]
  const ballX = Math.max(8, Math.min(92, 15 + ((normY - 44.8) / 10.4) * 70));
  const isGoal = outcomeLabel.toLowerCase() === 'goal';
  const isSaved = outcomeLabel.toLowerCase() === 'saved';
  const ballY = isGoal ? 32 : isSaved ? 42 : 12;
  const hitColor = isGoal ? '#DFFF00' : isSaved ? '#38BDF8' : '#F59E0B';

  return (
    <div className="bg-[#0B0D11] border border-white/10 rounded-xl p-2.5 space-y-2 select-none shadow-lg">
      <div className="flex items-center justify-between text-xs font-tech text-white/70">
        <span className="uppercase tracking-wider font-extrabold">Goalmouth Placement</span>
        <span className="font-mono text-[#DFFF00] font-black">{outcomeLabel}</span>
      </div>

      <div className="relative w-full aspect-[2.8/1] bg-[#06080A] rounded-lg border border-white/10 overflow-hidden">
        <svg viewBox="0 0 100 48" className="w-full h-full">
          <defs>
            <pattern id="netMesh" width="4" height="4" patternUnits="userSpaceOnUse">
              <path d="M 0 4 L 4 0 M 0 0 L 4 4" fill="none" stroke="rgba(255,255,255,0.08)" strokeWidth="0.4" />
            </pattern>
          </defs>

          {/* Hexagonal Goal Net Background */}
          <rect x="15" y="8" width="70" height="38" fill="url(#netMesh)" />

          {/* Goal Frame Posts and Crossbar */}
          <line x1="15" y1="8" x2="85" y2="8" stroke="#FFFFFF" strokeWidth="2.2" strokeLinecap="round" />
          <line x1="15" y1="8" x2="15" y2="46" stroke="#FFFFFF" strokeWidth="2.2" strokeLinecap="round" />
          <line x1="85" y1="8" x2="85" y2="46" stroke="#FFFFFF" strokeWidth="2.2" strokeLinecap="round" />
          <line x1="10" y1="46" x2="90" y2="46" stroke="rgba(255,255,255,0.3)" strokeWidth="0.8" />

          {/* Ball Entry Hit Marker */}
          <circle cx={ballX} cy={ballY} r="3.2" fill={hitColor} opacity="0.35">
            <animate attributeName="r" values="2.8;4.2;2.8" dur="1.5s" repeatCount="indefinite" />
          </circle>
          <circle cx={ballX} cy={ballY} r="2.2" fill={hitColor} stroke="#0B0D11" strokeWidth="0.6" />
        </svg>
      </div>

      <div className="grid grid-cols-2 gap-2 text-xs font-mono text-center">
        <div className="bg-white/5 rounded-lg px-2 py-1">
          <span className="text-white/50 block text-[11px] font-bold">xG</span>
          <span className="font-extrabold text-white text-xs sm:text-sm">{xg.toFixed(2)}</span>
        </div>
        <div className="bg-white/5 rounded-lg px-2 py-1">
          <span className="text-white/50 block text-[11px] font-bold">xGOT</span>
          <span className="font-extrabold text-[#DFFF00] text-xs sm:text-sm">{xgot.toFixed(2)}</span>
        </div>
      </div>
    </div>
  );
}
