'use client';

import React, { useState } from 'react';
import { MatchRecord } from '@/lib/database';
import AnalysisMatchBar from './AnalysisMatchBar';
import AnalysisZone14Card from './AnalysisZone14Card';
import AnalysisScoutClips from './AnalysisScoutClips';

interface AnalysisHubClientProps {
  matches: MatchRecord[];
}

export default function AnalysisHubClient({ matches }: AnalysisHubClientProps) {
  const [selectedMatchId, setSelectedMatchId] = useState<string>(
    matches[0]?.id || '',
  );

  if (!matches || matches.length === 0) {
    return (
      <div className="w-full bg-[#1A1C22] rounded-2xl border border-white/10 p-8 text-center font-tech text-white/50 text-sm">
        No active or completed match records available for tactical deep-dive.
      </div>
    );
  }

  const currentMatch =
    matches.find((m) => m.id === selectedMatchId) || matches[0];

  return (
    <div className="space-y-6">
      {/* Dynamic Match Selector Bar */}
      <AnalysisMatchBar
        matches={matches}
        selectedMatchId={currentMatch.id}
        onSelectMatch={setSelectedMatchId}
      />

      {/* Tactical Lab Bento: Zone 14 & Real Match Incidents */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <AnalysisZone14Card match={currentMatch} />
        <AnalysisScoutClips match={currentMatch} />
      </div>
    </div>
  );
}
