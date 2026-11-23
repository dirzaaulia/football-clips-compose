'use client';

import React, { useState, useEffect, useMemo } from 'react';
import {
  ALL_SUPPORTED_COMPETITIONS,
  FALLBACK_TACTICAL_PROFILES,
  ClubTacticalProfile,
  fetchLiveStandings,
  standingToTacticalProfile,
} from '@/lib/tools-data';
import { MatchRecord } from '@/lib/database';
import { fetchEspnMatchSummary, EspnH2HMatch } from '@/lib/espn';
import { fetchHistoricalH2HFromDatabase } from '@/lib/free-stats';
import H2HHistoricalClashes from '@/components/tools/H2HHistoricalClashes';
import {
  TeamPickerCard,
  ClashHeader,
  MetricsComparison,
} from '@/components/tools/H2HMetricComponents';
import H2HSimulationCard from '@/components/tools/H2HSimulationCard';

interface H2HComparatorProps {
  initialComp?: string | null;
  initialHome?: string | null;
  initialAway?: string | null;
  initialMatchId?: string | null;
}

export default function H2HComparator({
  initialComp,
  initialHome,
  initialAway,
  initialMatchId,
}: H2HComparatorProps) {
  const validInitialComp = useMemo(() => {
    if (!initialComp) return 'PL';
    const found = ALL_SUPPORTED_COMPETITIONS.find(
      (c) => c.code.toUpperCase() === initialComp.toUpperCase(),
    );
    return found ? found.code : 'PL';
  }, [initialComp]);

  const [compACode, setCompACode] = useState(validInitialComp);
  const [compBCode, setCompBCode] = useState(validInitialComp);
  const [teamsA, setTeamsA] = useState<ClubTacticalProfile[]>(FALLBACK_TACTICAL_PROFILES);
  const [teamsB, setTeamsB] = useState<ClubTacticalProfile[]>(FALLBACK_TACTICAL_PROFILES);
  const [teamAId, setTeamAId] = useState<string>(FALLBACK_TACTICAL_PROFILES[0].id);
  const [teamBId, setTeamBId] = useState<string>(FALLBACK_TACTICAL_PROFILES[1].id);
  const [isLoading, setIsLoading] = useState(false);
  const [dbClashes, setDbClashes] = useState<MatchRecord[]>([]);
  const [espnClashes, setEspnClashes] = useState<EspnH2HMatch[]>([]);
  const [seriesSummary, setSeriesSummary] = useState<string | undefined>(undefined);

  useEffect(() => {
    let isMounted = true;
    async function loadTeams() {
      setIsLoading(true);
      const [standingsA, standingsB] = await Promise.all([
        fetchLiveStandings(compACode),
        compACode === compBCode ? Promise.resolve([]) : fetchLiveStandings(compBCode),
      ]);
      if (!isMounted) return;

      const compAName =
        ALL_SUPPORTED_COMPETITIONS.find((c) => c.code === compACode)?.name || compACode;
      const compBName =
        ALL_SUPPORTED_COMPETITIONS.find((c) => c.code === compBCode)?.name || compBCode;

      const mappedA =
        standingsA.length > 0
          ? standingsA.map((s) => standingToTacticalProfile(s, compAName))
          : FALLBACK_TACTICAL_PROFILES;
      const mappedB =
        compACode === compBCode
          ? mappedA
          : standingsB.length > 0
          ? standingsB.map((s) => standingToTacticalProfile(s, compBName))
          : FALLBACK_TACTICAL_PROFILES;

      setTeamsA(mappedA);
      setTeamsB(mappedB);

      const matchByName = (list: ClubTacticalProfile[], query?: string | null) => {
        if (!query) return null;
        const q = query.toLowerCase().trim();
        return (
          list.find(
            (t) =>
              t.name.toLowerCase().includes(q) ||
              q.includes(t.name.toLowerCase()) ||
              t.shortName.toLowerCase() === q,
          ) || null
        );
      };

      const matchedHome = matchByName(mappedA, initialHome);
      const matchedAway = matchByName(mappedB, initialAway);

      if (matchedHome) setTeamAId(matchedHome.id);
      else if (!mappedA.some((t) => t.id === teamAId)) setTeamAId(mappedA[0]?.id || '');

      if (matchedAway) setTeamBId(matchedAway.id);
      else if (!mappedB.some((t) => t.id === teamBId)) setTeamBId(mappedB[1]?.id || mappedB[0]?.id || '');

      setIsLoading(false);
    }
    loadTeams();
    return () => {
      isMounted = false;
    };
  }, [compACode, compBCode, initialHome, initialAway]);

  const teamA = useMemo(
    () => teamsA.find((c) => c.id === teamAId) || teamsA[0] || FALLBACK_TACTICAL_PROFILES[0],
    [teamsA, teamAId],
  );
  const teamB = useMemo(
    () =>
      teamsB.find((c) => c.id === teamBId) ||
      teamsB[1] ||
      teamsB[0] ||
      FALLBACK_TACTICAL_PROFILES[1],
    [teamsB, teamBId],
  );

  useEffect(() => {
    let isCancelled = false;
    async function loadH2H() {
      const [dbRows, espnData] = await Promise.all([
        fetchHistoricalH2HFromDatabase(teamA.name, teamB.name),
        initialMatchId
          ? fetchEspnMatchSummary(initialMatchId, compACode).catch(() => null)
          : Promise.resolve(null),
      ]);
      if (isCancelled) return;
      setDbClashes(dbRows);
      setEspnClashes(espnData?.headToHead?.matches || []);
      setSeriesSummary(espnData?.headToHead?.summary);
    }
    loadH2H();
    return () => {
      isCancelled = true;
    };
  }, [teamA.name, teamB.name, initialMatchId, compACode]);

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <TeamPickerCard
          label="Club A (Primary Focus)"
          compCode={compACode}
          onCompChange={setCompACode}
          teamId={teamAId}
          onTeamChange={setTeamAId}
          teams={teamsA}
          disabledTeamId={compACode === compBCode ? teamBId : undefined}
          isLoading={isLoading}
          accent="volt"
        />
        <TeamPickerCard
          label="Club B (Opponent Benchmark)"
          compCode={compBCode}
          onCompChange={setCompBCode}
          teamId={teamBId}
          onTeamChange={setTeamBId}
          teams={teamsB}
          disabledTeamId={compACode === compBCode ? teamAId : undefined}
          isLoading={isLoading}
          accent="sky"
        />
      </div>

      <ClashHeader teamA={teamA} teamB={teamB} />
      <H2HSimulationCard teamA={teamA} teamB={teamB} />
      <MetricsComparison teamA={teamA} teamB={teamB} />
      <H2HHistoricalClashes
        teamA={teamA}
        teamB={teamB}
        dbClashes={dbClashes}
        espnClashes={espnClashes}
        seriesSummary={seriesSummary}
      />
    </div>
  );
}
