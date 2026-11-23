'use client';

import React, { useState, useEffect, useMemo } from 'react';
import { Network, Sparkles, Filter } from 'lucide-react';
import { ALL_SUPPORTED_COMPETITIONS, fetchLiveStandings } from '@/lib/tools-data';
import Zone14TacticalCanvas, {
  CorridorType,
  DynamicClubTactics,
} from '@/components/tools/Zone14TacticalCanvas';

export default function TacticalZone14OverloadEngine() {
  const [selectedComp, setSelectedComp] = useState('PL');
  const [clubs, setClubs] = useState<DynamicClubTactics[]>([]);
  const [selectedClubId, setSelectedClubId] = useState('');
  const [activeCorridor, setActiveCorridor] = useState<CorridorType>('zone14');
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    let isMounted = true;
    async function loadLeagueTactics() {
      setIsLoading(true);
      const standings = await fetchLiveStandings(selectedComp);
      if (!isMounted) return;

      if (standings.length > 0) {
        const computed: DynamicClubTactics[] = standings.map((s, idx) => {
          const played = Math.max(1, s.played_games || 1);
          const gf = s.goals_for || 0;
          const pts = s.points || 0;
          const xgVal = (gf * 0.92 + (s.won || 0) * 0.25) / played;
          const posEst = Math.min(68, Math.max(42, 50 + (pts / played - 1.3) * 8));
          const z14 = Math.round(28 + (gf / played) * 8);

          return {
            id: String(s.team_id || idx),
            name: s.team_name,
            shortName: (s.team_name || 'CLB').slice(0, 3).toUpperCase(),
            league: selectedComp,
            zone14Infiltration: Math.round(30 + (gf / played) * 7),
            passRetention: `${posEst.toFixed(1)}%`,
            leftFlankPct: Math.round(30 + ((s.won || 0) % 5)),
            zone14Pct: z14,
            rightFlankPct: Math.max(20, 100 - Math.round(30 + ((s.won || 0) % 5)) - z14),
            xgCreated: `${xgVal.toFixed(2)} xG/90`,
            boxEntries: Math.round(20 + (gf / played) * 5),
          };
        });

        setClubs(computed);
        if (!computed.some((c) => c.id === selectedClubId)) {
          setSelectedClubId(computed[0]?.id || '');
        }
      }
      setIsLoading(false);
    }

    loadLeagueTactics();
    return () => {
      isMounted = false;
    };
  }, [selectedComp, selectedClubId]);

  const currentClub = useMemo(() => {
    return clubs.find((c) => c.id === selectedClubId) || clubs[0] || {
      id: 'default',
      name: 'Top Tier Club',
      shortName: 'CLB',
      league: selectedComp,
      zone14Infiltration: 45,
      passRetention: '89.5%',
      leftFlankPct: 32,
      zone14Pct: 44,
      rightFlankPct: 24,
      xgCreated: '2.15 xG/90',
      boxEntries: 32,
    };
  }, [clubs, selectedClubId, selectedComp]);

  return (
    <div className="w-full bg-[#1A1C22] rounded-2xl border border-white/10 p-5 shadow-xl flex flex-col gap-4">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <Network className="w-4 h-4 text-[#DFFF00]" />
          <h3 className="font-scoreboard text-lg sm:text-xl text-white uppercase tracking-wider">
            Attacking Zones &amp; Final Third Breakdown
          </h3>
        </div>
        <span className="text-[10px] font-tech text-[#DFFF00] bg-[#DFFF00]/10 px-2.5 py-0.5 rounded-full border border-[#DFFF00]/30 font-bold uppercase self-start sm:self-auto flex items-center gap-1">
          <Sparkles className="w-3 h-3" />
          OPTA SPATIAL GRID
        </span>
      </div>

      {/* League & Club Selection Bar */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 font-tech text-xs">
        <div className="flex items-center gap-2">
          <Filter className="w-3.5 h-3.5 text-white/40 shrink-0" />
          <select
            value={selectedComp}
            onChange={(e) => setSelectedComp(e.target.value)}
            className="w-full px-3 py-2 rounded-xl bg-[#0D0E11] border border-white/10 text-white font-semibold focus:outline-none focus:border-[#DFFF00]"
          >
            {ALL_SUPPORTED_COMPETITIONS.map((c) => (
              <option key={c.code} value={c.code}>
                {c.name}
              </option>
            ))}
          </select>
        </div>
        <select
          value={selectedClubId}
          onChange={(e) => setSelectedClubId(e.target.value)}
          disabled={isLoading || clubs.length === 0}
          className="w-full px-3 py-2 rounded-xl bg-[#0D0E11] border border-white/10 text-white font-semibold focus:outline-none focus:border-[#DFFF00]"
        >
          {clubs.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name} ({c.passRetention} Retention)
            </option>
          ))}
        </select>
      </div>

      {/* Corridor Switch Buttons */}
      <div className="flex flex-wrap gap-2">
        {(['zone14', 'leftHalf', 'rightHalf', 'leftWing', 'rightWing', 'box'] as CorridorType[]).map((type) => (
          <button
            key={type}
            type="button"
            onClick={() => setActiveCorridor(type)}
            className={`px-3 py-1.5 rounded-xl font-tech text-xs font-bold uppercase tracking-wider transition-all cursor-pointer ${
              activeCorridor === type
                ? 'bg-[#DFFF00] text-[#0A0A0C] shadow-[0_0_12px_rgba(223,255,0,0.25)]'
                : 'bg-[#121316] text-white/60 hover:text-white border border-white/10'
            }`}
          >
            {type === 'zone14'
              ? 'Zone 14 Danger'
              : type === 'leftHalf'
              ? 'Left Half-Space'
              : type === 'rightHalf'
              ? 'Right Half-Space'
              : type === 'leftWing'
              ? 'Left Wing'
              : type === 'rightWing'
              ? 'Right Wing'
              : 'Penalty Box'}
          </button>
        ))}
      </div>

      {/* Pitch Simulator View */}
      <Zone14TacticalCanvas currentClub={currentClub} activeCorridor={activeCorridor} />
    </div>
  );
}
