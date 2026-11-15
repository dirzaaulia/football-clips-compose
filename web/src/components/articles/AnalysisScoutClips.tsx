'use client';

import React from 'react';
import { Film, Play, Bookmark, Clock } from 'lucide-react';

interface ScoutObservation {
  minute: string;
  title: string;
  description: string;
  impactScore: string;
}

const DEFAULT_OBSERVATIONS: ScoutObservation[] = [
  {
    minute: "24'",
    title: 'High-Press Trigger on Right Center-Back',
    description: 'Bellingham curved pressing run forces an errant turnover into central half-space.',
    impactScore: 'xT +0.34',
  },
  {
    minute: "58'",
    title: 'Overload to Isolate Dynamic Winger',
    description: 'Box midfield tilt pulls 3 defenders, leaving 1v1 corridor on the back post.',
    impactScore: 'xG 0.48',
  },
  {
    minute: "81'",
    title: 'Low-Block Counter Compression',
    description: '3-second vertical release following defensive box clearance.',
    impactScore: 'Goal Probability 74%',
  },
];

export default function AnalysisScoutClips() {
  return (
    <div className="w-full bg-[#1A1C22] rounded-2xl border border-white/10 p-5 shadow-xl flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Film className="w-4 h-4 text-[#DFFF00]" />
          <h3 className="font-scoreboard text-lg sm:text-xl text-white uppercase tracking-wider">
            Algorithmic Scout Observations &amp; Clips
          </h3>
        </div>
        <span className="text-[10px] font-tech text-[#DFFF00] bg-[#DFFF00]/10 px-2.5 py-0.5 rounded-full border border-[#DFFF00]/30 font-bold uppercase">
          3 Verified Events
        </span>
      </div>

      <div className="divide-y divide-white/5">
        {DEFAULT_OBSERVATIONS.map((obs) => (
          <div key={obs.minute} className="py-3 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div className="flex items-start gap-3">
              <span className="px-2 py-1 rounded bg-[#0A0A0C] border border-[#DFFF00]/30 text-[#DFFF00] font-tech text-xs font-bold shrink-0">
                {obs.minute}
              </span>
              <div className="flex flex-col">
                <span className="font-tech text-sm font-bold text-white">
                  {obs.title}
                </span>
                <span className="text-xs text-white/60 leading-relaxed">
                  {obs.description}
                </span>
              </div>
            </div>

            <div className="flex items-center gap-2 shrink-0 self-end sm:self-auto">
              <span className="text-[10px] font-tech font-bold text-white/50 bg-[#121316] px-2.5 py-1 rounded-lg border border-white/10">
                {obs.impactScore}
              </span>
              <button
                className="w-8 h-8 rounded-lg bg-[#DFFF00] hover:bg-[#c8e600] text-[#0A0A0C] flex items-center justify-center transition-all cursor-pointer shadow-sm"
                title="Watch Breakdown Clip"
              >
                <Play className="w-3.5 h-3.5 fill-current" />
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
