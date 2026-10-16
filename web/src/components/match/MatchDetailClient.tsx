'use client';

import React, { useState, useEffect, useMemo } from 'react';
import Image from 'next/image';
import { useParams, usePathname } from 'next/navigation';
import { Trophy, Calendar, Loader2, AlertCircle } from 'lucide-react';
import Link from 'next/link';
import { MatchRecord, getMatchById, getRelatedMatches } from '@/lib/supabase';
import { fetchEspnMatchSummary } from '@/lib/espn';
import { generateAlgorithmicTacticalRecap } from '@/lib/match-analysis';
import AdSenseBanner from '@/components/ads/AdSenseBanner';
import MatchCentreHeader from '@/components/match/MatchCentreHeader';
import MatchSidebar from '@/components/match/MatchSidebar';
import MatchScoreSection from '@/components/match/MatchScoreSection';
import MatchRecapSection from '@/components/match/MatchRecapSection';
import MatchTacticalBoxscore from '@/components/match/MatchTacticalBoxscore';

interface MatchDetailClientProps {
  initialMatch: MatchRecord | null;
  initialRecap?: string | null;
  initialRelated?: { highlights: MatchRecord[]; fixtures: MatchRecord[] };
  expectedId?: string;
}

export default function MatchDetailClient({
  initialMatch,
  initialRecap,
  initialRelated,
  expectedId,
}: MatchDetailClientProps) {
  const params = useParams();
  const pathname = usePathname();

  // Extract the target match ID from route params, pathname, or window.location
  const activeUrlId = useMemo(() => {
    if (params?.id && typeof params.id === 'string' && params.id !== 'index') {
      return params.id;
    }
    if (pathname) {
      const m = pathname.match(/\/match\/([^/?#]+)/);
      if (m && m[1] && m[1] !== 'index' && m[1] !== 'index.html') {
        return m[1];
      }
    }
    if (typeof window !== 'undefined') {
      const m = window.location.pathname.match(/\/match\/([^/?#]+)/);
      if (m && m[1] && m[1] !== 'index' && m[1] !== 'index.html') {
        return m[1];
      }
    }
    return expectedId || null;
  }, [params?.id, pathname, expectedId]);

  const [match, setMatch] = useState<MatchRecord | null>(initialMatch);
  const [recapAnalysis, setRecapAnalysis] = useState<string | null>(initialRecap || null);
  const [related, setRelated] = useState(
    initialRelated || { highlights: [], fixtures: [] }
  );
  const [isLoading, setIsLoading] = useState(!initialMatch);
  const [hasHydrated, setHasHydrated] = useState(false);

  useEffect(() => {
    setHasHydrated(true);
  }, []);

  // Client-side hydration & sync when navigating or if statistics/summary need fetching
  useEffect(() => {
    let isCancelled = false;

    async function syncMatchData() {
      const targetId = activeUrlId || (match ? String(match.id) : null);
      if (!targetId) return;

      const needsFetchMatch = !match || String(match.id) !== targetId;

      if (needsFetchMatch) {
        setIsLoading(true);
        try {
          const fetched = await getMatchById(targetId);
          if (isCancelled) return;

          if (fetched) {
            // Fetch live ESPN boxscore & editorial in parallel with related matches
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
              statistics: espnData?.statistics || fetched.statistics,
              formations: espnData?.formations || fetched.formations,
              editorial_headline: espnData?.editorial_headline || fetched.editorial_headline,
              editorial_story: espnData?.editorial_story || fetched.editorial_story,
              editorial_source: espnData?.editorial_source || fetched.editorial_source,
            };

            const isFinished = mergedMatch.status === 'FINISHED';
            const isLive =
              mergedMatch.status === 'IN_PLAY' ||
              mergedMatch.status === 'PAUSED' ||
              mergedMatch.status === 'LIVE';
            const isUpcoming = !isFinished && !isLive;

            let recap = '';
            if (isUpcoming) {
              // NO AI FOR UPCOMING MATCHES
              recap =
                mergedMatch.editorial_story ||
                generateAlgorithmicTacticalRecap(mergedMatch);
            } else {
              // Finished or Live matches
              recap =
                mergedMatch.editorial_story ||
                mergedMatch.recap_analysis ||
                generateAlgorithmicTacticalRecap(mergedMatch);
            }

            setMatch(mergedMatch);
            setRecapAnalysis(recap);
            setRelated(relatedData);
          } else {
            // Match not in DB yet, check if search params in window.location have metadata
            if (typeof window !== 'undefined') {
              const sp = new URLSearchParams(window.location.search);
              const home = sp.get('home');
              const away = sp.get('away');
              const comp = sp.get('comp') || 'Official League';
              const vid = sp.get('vid');
              const date = sp.get('date') || new Date().toISOString();
              const homeCrest = sp.get('homeCrest');
              const awayCrest = sp.get('awayCrest');

              if (home && away) {
                const fallbackRecord: MatchRecord = {
                  id: targetId,
                  competition_id: comp.slice(0, 3).toUpperCase(),
                  competition_name: comp,
                  utc_date: date,
                  status: 'SCHEDULED',
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
          console.error('[MatchDetailClient] Failed to load match for URL:', targetId, err);
        } finally {
          if (!isCancelled) setIsLoading(false);
        }
      } else {
        // ID already matches; check if statistics or formations are missing for finished/live games
        const isFinished = match.status === 'FINISHED';
        const isLive =
          match.status === 'IN_PLAY' ||
          match.status === 'PAUSED' ||
          match.status === 'LIVE';

        if ((isFinished || isLive) && !match.statistics) {
          try {
            const espnData = await fetchEspnMatchSummary(match.id, match.competition_id);
            if (!isCancelled && espnData) {
              setMatch((prev) =>
                prev
                  ? {
                      ...prev,
                      statistics: espnData.statistics || prev.statistics,
                      formations: espnData.formations || prev.formations,
                      editorial_headline:
                        espnData.editorial_headline || prev.editorial_headline,
                      editorial_story:
                        espnData.editorial_story || prev.editorial_story,
                      editorial_source:
                        espnData.editorial_source || prev.editorial_source,
                    }
                  : null
              );

              if (espnData.editorial_story && (!recapAnalysis || recapAnalysis.length < 50)) {
                setRecapAnalysis(espnData.editorial_story);
              }
            }
          } catch (err) {
            console.warn('[MatchDetailClient] ESPN summary sync error:', err);
          }
        }
      }
    }

    syncMatchData();
    return () => {
      isCancelled = true;
    };
  }, [activeUrlId, match?.id]);

  // Loading skeleton state
  const isPendingMismatch =
    hasHydrated &&
    activeUrlId &&
    match &&
    String(match.id) !== String(activeUrlId);

  if (isLoading || isPendingMismatch || !match) {
    if (!isLoading && !match) {
      return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 text-center space-y-4">
          <div className="inline-flex p-3 rounded-full bg-rose-500/10 text-rose-400 border border-rose-500/20 mb-2">
            <AlertCircle className="w-8 h-8" />
          </div>
          <h1 className="text-xl font-black text-white">Match Not Found</h1>
          <p className="text-sm text-[#94A3B8] max-w-md mx-auto">
            The requested match could not be found or has not been synchronized yet.
          </p>
          <div className="pt-4">
            <Link
              href="/fixtures"
              className="inline-flex items-center px-4 py-2 rounded-xl bg-[#1E3A8A] hover:bg-[#2563EB] text-white text-xs font-bold transition-all shadow-md"
            >
              Browse Upcoming Fixtures
            </Link>
          </div>
        </div>
      );
    }

    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 flex flex-col items-center justify-center min-h-[50vh] text-center space-y-4">
        <Loader2 className="w-10 h-10 animate-spin text-[#7098F8]" />
        <p className="text-sm font-bold text-[#94A3B8]">
          Loading Match Centre &amp; Tactical Intelligence...
        </p>
      </div>
    );
  }

  const homeName = match.home_team_name || match.home_team || 'Home Team';
  const awayName = match.away_team_name || match.away_team || 'Away Team';

  const jsonLd = {
    '@context': 'https://schema.org',
    '@type': 'SportsEvent',
    name: `${homeName} vs ${awayName}`,
    sport: 'Football',
    startDate: match.utc_date,
    homeTeam: { '@type': 'SportsTeam', name: homeName },
    awayTeam: { '@type': 'SportsTeam', name: awayName },
    video: match.highlight_video_id
      ? {
          '@type': 'VideoObject',
          name: `${homeName} vs ${awayName} Highlights`,
          description: `Watch official highlights from ${match.competition_name}`,
          thumbnailUrl: `https://i.ytimg.com/vi/${match.highlight_video_id}/hqdefault.jpg`,
          embedUrl: `https://www.youtube.com/embed/${match.highlight_video_id}`,
          uploadDate: match.utc_date,
        }
      : undefined,
  };

  return (
    <article className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
      />

      <MatchCentreHeader
        videoId={match.highlight_video_id}
        title={`${homeName} vs ${awayName}`}
      />

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        <div className="lg:col-span-8 space-y-6">
          {match.highlight_video_id ? (
            <div className="rounded-3xl overflow-hidden bg-[#1E2227] border border-[#334155] shadow-2xl">
              <div className="relative w-full pt-[56.25%] bg-black">
                <iframe
                  className="absolute inset-0 w-full h-full"
                  src={`https://www.youtube.com/embed/${match.highlight_video_id}?rel=0&modestbranding=1&autoplay=0`}
                  title={`${homeName} vs ${awayName} Highlights`}
                  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                  allowFullScreen
                />
              </div>
              <div className="p-3 bg-[#101418] flex items-center justify-between text-xs text-[#94A3B8] border-t border-[#334155]/60">
                <span className="flex items-center gap-1.5 font-bold text-white">
                  <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
                  Official Match Broadcast
                </span>
                <span className="text-[#4FC3F7] font-semibold">1080p High Definition</span>
              </div>
            </div>
          ) : null}

          {/* Match Score & Teams Header */}
          <div className="rounded-3xl bg-[#181C20] border border-[#334155] p-6 space-y-4 shadow-xl">
            <div className="flex items-center justify-between text-xs text-[#94A3B8] pb-3 border-b border-[#334155]/60">
              <div className="flex items-center gap-1.5 font-bold text-[#7098F8] uppercase tracking-wider">
                <Trophy className="w-3.5 h-3.5" />
                <span>{match.competition_name}</span>
              </div>
              <div className="flex items-center gap-1.5">
                <Calendar className="w-3.5 h-3.5 text-[#94A3B8]" />
                <span>
                  {new Date(match.utc_date).toLocaleDateString('en-GB', {
                    dateStyle: 'full',
                  })}
                </span>
              </div>
            </div>

            <div className="flex items-center justify-between gap-4 py-2">
              <div className="flex items-center gap-3 flex-1 min-w-0">
                {match.home_team_crest && (
                  <div className="relative w-10 h-10 sm:w-12 sm:h-12 shrink-0">
                    <Image
                      src={match.home_team_crest}
                      alt={homeName}
                      fill
                      className="object-contain"
                      unoptimized
                    />
                  </div>
                )}
                <span className="text-base sm:text-xl font-black text-white leading-tight">
                  {homeName}
                </span>
              </div>

              <MatchScoreSection
                homeScore={match.home_score}
                awayScore={match.away_score}
                status={match.status}
              />

              <div className="flex items-center justify-end gap-3 flex-1 min-w-0 text-right">
                <span className="text-base sm:text-xl font-black text-white leading-tight">
                  {awayName}
                </span>
                {match.away_team_crest && (
                  <div className="relative w-10 h-10 sm:w-12 sm:h-12 shrink-0">
                    <Image
                      src={match.away_team_crest}
                      alt={awayName}
                      fill
                      className="object-contain"
                      unoptimized
                    />
                  </div>
                )}
              </div>
            </div>
          </div>

          <AdSenseBanner slotId="5616816386" format="horizontal" />

          {/* Authentic Match Centre & Tactical Data (Boxscore Statistics, Lineups & Editorial Report) */}
          <MatchTacticalBoxscore match={match} />

          {/* Tactical Breakdown & Match Report (Preview for upcoming / Summary for finished) */}
          <MatchRecapSection
            recapAnalysis={recapAnalysis}
            status={match.status}
          />
        </div>

        <div className="lg:col-span-4 space-y-6">
          <MatchSidebar
            competitionName={match.competition_name}
            highlights={related.highlights}
            fixtures={related.fixtures}
          />
          <AdSenseBanner slotId="5616816386" format="rectangle" />
        </div>
      </div>
    </article>
  );
}
