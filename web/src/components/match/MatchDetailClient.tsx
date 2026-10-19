'use client';

import React, { useState, useEffect, useMemo } from 'react';
import { useParams, usePathname } from 'next/navigation';
import { Loader2, AlertCircle } from 'lucide-react';
import Link from 'next/link';
import { MatchRecord } from '@/lib/database';
import { useMatchDetailData } from '@/hooks/useMatchDetailData';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';
import AdSenseBanner from '@/components/ads/AdSenseBanner';
import MatchCentreHeader from '@/components/match/MatchCentreHeader';
import MatchSidebar from '@/components/match/MatchSidebar';
import MatchRecapSection from '@/components/match/MatchRecapSection';
import MatchFaqSection from '@/components/match/MatchFaqSection';
import MatchTacticalBoxscore from '@/components/match/MatchTacticalBoxscore';
import MatchCentreHero from '@/components/player/MatchCentreHero';
import HorizontalMatchTimeline from '@/components/match/HorizontalMatchTimeline';

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

  const [hasHydrated, setHasHydrated] = useState(false);
  const { isSpoilerFree } = useSpoilerFree();
  const [isScoreRevealed, setIsScoreRevealed] = useState(!isSpoilerFree);

  useEffect(() => {
    setHasHydrated(true);
  }, []);

  const { match, recapAnalysis, related, isLoading } = useMatchDetailData(
    initialMatch,
    initialRecap,
    initialRelated,
    activeUrlId
  );

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
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 flex flex-col items-center justify-center space-y-4">
        <Loader2 className="w-10 h-10 text-[#7098F8] animate-spin" />
        <p className="text-sm text-[#94A3B8] animate-pulse">Loading Match Centre &amp; Data...</p>
      </div>
    );
  }

  const homeName = match.home_team_name || match.home_team || 'Home';
  const awayName = match.away_team_name || match.away_team || 'Away';

  const compName = match.competition_name || 'Official League';
  const siteUrl = process.env.NEXT_PUBLIC_SITE_URL || 'https://fc.dirzaaulia.com';
  const matchUrl = `${siteUrl}/match/${match.id}`;

  const jsonLd = {
    '@context': 'https://schema.org',
    '@graph': [
      {
        '@type': 'SportsEvent',
        '@id': matchUrl,
        name: `${homeName} vs ${awayName}`,
        description: `Tactical analysis, highlights, and full match recap for ${homeName} vs ${awayName} in ${compName}.`,
        startDate: match.utc_date,
        eventStatus:
          match.status === 'FINISHED'
            ? 'https://schema.org/EventFinished'
            : ['IN_PLAY', 'PAUSED', 'LIVE'].includes(match.status)
            ? 'https://schema.org/EventLive'
            : 'https://schema.org/EventScheduled',
        competitor: [
          { '@type': 'SportsTeam', name: homeName, logo: match.home_team_crest },
          { '@type': 'SportsTeam', name: awayName, logo: match.away_team_crest },
        ],
      },
      {
        '@type': 'NewsArticle',
        headline: `${homeName} vs ${awayName} Tactical Match Report & Highlights`,
        datePublished: match.utc_date,
        dateModified: match.updated_at || match.utc_date,
        author: {
          '@type': 'Organization',
          name: 'FootballClips Tactical Editorial Desk',
          url: `${siteUrl}/editorial-standards`,
        },
        publisher: {
          '@type': 'Organization',
          name: 'Football Highlights & Clips',
          logo: {
            '@type': 'ImageObject',
            url: `${siteUrl}/favicon.png`,
          },
        },
        mainEntityOfPage: matchUrl,
      },
      {
        '@type': 'FAQPage',
        mainEntity: [
          {
            '@type': 'Question',
            name: `What was the score in ${homeName} vs ${awayName}?`,
            acceptedAnswer: {
              '@type': 'Answer',
              text: `The match concluded with ${homeName} ${match.home_score ?? 0} - ${match.away_score ?? 0} ${awayName} in ${compName}.`,
            },
          },
          {
            '@type': 'Question',
            name: `Where are official highlights for ${homeName} vs ${awayName} available?`,
            acceptedAnswer: {
              '@type': 'Answer',
              text: `Official highlights and decisive replay clips are available on FootballClips embedded from verified official broadcaster channels.`,
            },
          },
        ],
      },
      ...(match.highlight_video_id
        ? [
            {
              '@type': 'VideoObject',
              name: `${homeName} vs ${awayName} Official Highlights`,
              description: `Watch official match highlights from ${homeName} vs ${awayName} in ${compName}.`,
              thumbnailUrl: `https://i.ytimg.com/vi/${match.highlight_video_id}/hqdefault.jpg`,
              embedUrl: `https://www.youtube.com/embed/${match.highlight_video_id}`,
              uploadDate: match.utc_date,
            },
          ]
        : []),
    ],
  };

  return (
    <article className="max-w-7xl mx-auto px-3.5 sm:px-6 lg:px-8 py-4 sm:py-8 space-y-5 sm:space-y-8">
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
          {/* Visual Header: Video or MatchCentreHero */}
          {match.highlight_video_id ? (
            <div className="space-y-4">
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
                    Official Match Highlights
                  </span>
                  <span className="text-[#4FC3F7] font-semibold">{match.competition_name || match.competition_id}</span>
                </div>
              </div>
              <MatchCentreHero
                match={match}
                homeName={homeName}
                awayName={awayName}
                isSpoilerFree={isSpoilerFree}
                isScoreRevealed={isScoreRevealed}
                onRevealScore={() => setIsScoreRevealed(true)}
                onHideScore={() => setIsScoreRevealed(false)}
              />
            </div>
          ) : (
            <MatchCentreHero
              match={match}
              homeName={homeName}
              awayName={awayName}
              isSpoilerFree={isSpoilerFree}
              isScoreRevealed={isScoreRevealed}
              onRevealScore={() => setIsScoreRevealed(true)}
              onHideScore={() => setIsScoreRevealed(false)}
            />
          )}

          {/* Premier League Style Horizontal Event Timeline */}
          {match.events && match.events.length > 0 && (
            <HorizontalMatchTimeline
              events={match.events}
              homeName={homeName}
              awayName={awayName}
              isSpoilerFree={isSpoilerFree}
              isLive={['IN_PLAY', 'PAUSED', 'LIVE'].includes(match.status)}
            />
          )}

          <AdSenseBanner slotId="5616816386" format="horizontal" />

          {/* Unified Match Centre: Timeline, Stats, Lineups & Report */}
          <MatchTacticalBoxscore match={match} />

          {/* Tactical Breakdown & Match Report */}
          <MatchRecapSection
            recapAnalysis={recapAnalysis}
            status={match.status}
          />

          {/* Authentic Match FAQs & Facts */}
          <MatchFaqSection match={match} />
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
