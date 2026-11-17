'use client';

import React from 'react';
import { getQualificationZones } from '@/lib/standingsQualification';

interface StandingsLegendProps {
  competitionCode: string;
}

export default function StandingsLegend({ competitionCode }: StandingsLegendProps) {
  const zones = getQualificationZones(competitionCode);
  if (!zones || zones.length === 0) return null;

  return (
    <div className="flex flex-wrap items-center gap-4 text-xs text-white/60 bg-[#1B1B1F] px-4 py-2 rounded-lg border border-white/5 font-tech shrink-0">
      <span className="font-bold text-white/40 uppercase tracking-wider text-[10px]">Zones:</span>
      {zones.map((zone) => (
        <div key={zone.label} className="flex items-center gap-1.5">
          <span
            className="w-2.5 h-2.5 rounded-sm shrink-0"
            style={{ backgroundColor: zone.dotColor }}
          />
          <span>{zone.label}</span>
        </div>
      ))}
    </div>
  );
}
