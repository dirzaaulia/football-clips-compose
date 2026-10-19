'use client';

import { useState, useEffect } from 'react';
import { MatchRecord, getMatchById, getRelatedMatches } from '@/lib/database';
import { fetchEspnMatchSummary } from '@/lib/espn';
import { generateAlgorithmicTacticalRecap } from '@/lib/match-analysis';

export function useMatchDetailData(
  initialMatch: MatchRecord | null,
  initialRecap?: string | null,
  initialRelated?: { highlights: MatchRecord[]; fixtures: MatchRecord[] },
  activeUrlId?: string | null
) {
  const [match, setMatch] = useState<MatchRecord | null>(initialMatch);
  const [recapAnalysis, setRecapAnalysis] = useState<string | null>(initialRecap || null);
  const [related, setRelated] = useState(
    initialRelated || { highlights: [], fixtures: [] }
  );
  const [isLoading, setIsLoading] = useState(!initialMatch);
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const refetch = () => setRefreshTrigger((t) => t + 1);

  useEffect(() => {
    let isCancelled = false;

    async function syncMatchData() {
      const targetId = activeUrlId || (match ? String(match.id) : null);
      if (!targetId) return;

      const isNonFinished = match && match.status !== 'FINISHED';
      const needsFetchMatch = !match || String(match.id) !== targetId || isNonFinished || !match.statistics;

      if (needsFetchMatch) {
        setIsLoading(true);
        try {
          const fetched = await getMatchById(targetId);
          if (isCancelled) return;

          if (fetched) {
            const [espnData, relatedData] = await Promise.all([
              fetchEspnMatchSummary(fetched.id, fetched.competition_id).catch(() => null),
              getRelatedMatches(fetched.competition_id, fetched.id, 8).catch(() => ({
                highlights: [],
                fixtures: [],
              })),
            ]);

            if (isCancelled) return;

            const mergedMatch: MatchRecord = {
              ...fetched,
              status: espnData?.status || fetched.status,
              home_score: espnData?.home_score != null ? espnData.home_score : fetched.home_score,
              away_score: espnData?.away_score != null ? espnData.away_score : fetched.away_score,
              display_clock: espnData?.display_clock || fetched.display_clock,
              statistics: espnData?.statistics || fetched.statistics,
              formations: espnData?.formations || fetched.formations,
              editorial_headline: espnData?.editorial_headline || fetched.editorial_headline,
              editorial_story: espnData?.editorial_story || fetched.editorial_story,
              editorial_source: espnData?.editorial_source || fetched.editorial_source,
              events: espnData?.events || fetched.events,
            };

            const recap =
              mergedMatch.editorial_story ||
              generateAlgorithmicTacticalRecap(mergedMatch);

            setMatch(mergedMatch);
            setRecapAnalysis(recap);
            setRelated(relatedData);
          } else {
            if (typeof window !== 'undefined') {
              const urlParams = new URLSearchParams(window.location.search);
              const home = urlParams.get('home');
              const away = urlParams.get('away');
              const comp = urlParams.get('comp') || 'Match Centre';
              const vid = urlParams.get('vid');
              const date = urlParams.get('date') || new Date().toISOString();
              const homeCrest = urlParams.get('homeCrest');
              const awayCrest = urlParams.get('awayCrest');

              if (home && away) {
                const fallbackRecord: MatchRecord = {
                  id: targetId,
                  competition_id: 'CUP',
                  competition_name: comp,
                  utc_date: date,
                  status: 'FINISHED',
                  home_team: home,
                  away_team: away,
                  home_team_name: home,
                  away_team_name: away,
                  home_team_crest: homeCrest,
                  away_team_crest: awayCrest,
                  highlight_video_id: vid,
                };

                const espnData = await fetchEspnMatchSummary(targetId).catch(() => null);
                if (espnData) {
                  fallbackRecord.statistics = espnData.statistics;
                  fallbackRecord.formations = espnData.formations;
                  fallbackRecord.editorial_headline = espnData.editorial_headline;
                  fallbackRecord.editorial_story = espnData.editorial_story;
                  fallbackRecord.editorial_source = espnData.editorial_source;
                  fallbackRecord.events = espnData.events;
                }

                const recap =
                  fallbackRecord.editorial_story ||
                  generateAlgorithmicTacticalRecap(fallbackRecord);

                setMatch(fallbackRecord);
                setRecapAnalysis(recap);
              }
            }
          }
        } catch (err) {
          console.error('[useMatchDetailData] Failed to load match for URL:', targetId, err);
        } finally {
          if (!isCancelled) setIsLoading(false);
        }
      } else {
        const isNotFinished = match.status !== 'FINISHED';
        const isMissingData = !match.statistics || !match.events;

        if (isNotFinished || isMissingData) {
          try {
            const [freshMatch, espnData] = await Promise.all([
              getMatchById(String(match.id)).catch(() => null),
              fetchEspnMatchSummary(match.id, match.competition_id, isNotFinished).catch(() => null),
            ]);

            if (!isCancelled && (freshMatch || espnData)) {
              setMatch((prev) => {
                if (!prev) return null;
                const base = freshMatch || prev;
                return {
                  ...base,
                  status: espnData?.status || base.status || prev.status,
                  home_score: espnData?.home_score != null ? espnData.home_score : (base.home_score ?? prev.home_score),
                  away_score: espnData?.away_score != null ? espnData.away_score : (base.away_score ?? prev.away_score),
                  display_clock: espnData?.display_clock || base.display_clock || prev.display_clock,
                  statistics: espnData?.statistics || base.statistics || prev.statistics,
                  formations: espnData?.formations || base.formations || prev.formations,
                  editorial_headline: espnData?.editorial_headline || base.editorial_headline || prev.editorial_headline,
                  editorial_story: espnData?.editorial_story || base.editorial_story || prev.editorial_story,
                  editorial_source: espnData?.editorial_source || base.editorial_source || prev.editorial_source,
                  events: espnData?.events || base.events || prev.events,
                };
              });

              const story = espnData?.editorial_story || freshMatch?.editorial_story;
              if (story && (!recapAnalysis || recapAnalysis.length < 50)) {
                setRecapAnalysis(story);
              }
            }
          } catch (err) {
            console.warn('[useMatchDetailData] Match sync error:', err);
          }
        }
      }
    }

    syncMatchData();
    return () => {
      isCancelled = true;
    };
  }, [activeUrlId, match?.id, refreshTrigger]);

  // Real-time live match polling: polls every 8s while game is active for near 99% real-time score/events/clock
  useEffect(() => {
    if (!match) return;
    const isLive = ['IN_PLAY', 'PAUSED', 'LIVE'].includes(match.status);
    if (!isLive) return;

    let isCancelled = false;
    async function pollLiveUpdate() {
      if (!match) return;
      try {
        const [freshMatch, espnData] = await Promise.all([
          getMatchById(String(match.id)).catch(() => null),
          fetchEspnMatchSummary(match.id, match.competition_id, true).catch(() => null),
        ]);
        if (isCancelled || (!freshMatch && !espnData)) return;

        setMatch((prev) => {
          if (!prev) return null;
          const base = freshMatch || prev;
          return {
            ...base,
            status: espnData?.status || base.status || prev.status,
            display_clock: espnData?.display_clock || base.display_clock || prev.display_clock,
            home_score: espnData?.home_score != null ? espnData.home_score : (base.home_score ?? prev.home_score),
            away_score: espnData?.away_score != null ? espnData.away_score : (base.away_score ?? prev.away_score),
            statistics: espnData?.statistics || base.statistics || prev.statistics,
            formations: espnData?.formations || base.formations || prev.formations,
            events: espnData?.events || base.events || prev.events,
            editorial_headline: espnData?.editorial_headline || base.editorial_headline || prev.editorial_headline,
            editorial_story: espnData?.editorial_story || base.editorial_story || prev.editorial_story,
          };
        });
      } catch (err) {
        console.warn('[useMatchDetailData] Live poll error:', err);
      }
    }

    pollLiveUpdate();
    const interval = setInterval(pollLiveUpdate, 8_000);
    return () => {
      isCancelled = true;
      clearInterval(interval);
    };
  }, [match?.id, match?.status, match?.competition_id]);

  return { match, recapAnalysis, related, isLoading, refetch };
}
