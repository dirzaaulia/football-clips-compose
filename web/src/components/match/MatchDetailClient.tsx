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
import MatchTacticalBoxscore from '@/components/match/MatchTacticalBoxscore';
import MatchDayGearSection from '@/components/affiliate/MatchDayGearSection';
import MatchCentreHero from '@/components/player/MatchCentreHero';
import HorizontalMatchTimeline from '@/components/match/HorizontalMatchTimeline';
import WebPitchRadar, { WebPitchPlay } from '@/components/match/WebPitchRadar';
import MatchDetailVideoPlayer from '@/components/match/MatchDetailVideoPlayer';
import MatchDetailSpoilerCard from '@/components/match/MatchDetailSpoilerCard';
import { generateMatchJsonLd } from '@/components/match/matchJsonLd';
import { trackMatchView, trackMatchClick, trackVideoPlay, trackScreenView } from '@/lib/analytics';

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
    if (params?.id && typeof params.id === 'string' && params.id !== 'index') return params.id;
    if (pathname) {
      const m = pathname.match(/\/match\/([^/?#]+)/);
      if (m && m[1] && m[1] !== 'index' && m[1] !== 'index.html') return m[1];
    }
    if (typeof window !== 'undefined') {
      const m = window.location.pathname.match(/\/match\/([^/?#]+)/);
      if (m && m[1] && m[1] !== 'index' && m[1] !== 'index.html') return m[1];
    }
    return expectedId || null;
  }, [params?.id, pathname, expectedId]);

  const { match, recapAnalysis, related, isLoading } = useMatchDetailData(
    initialMatch,
    initialRecap,
    initialRelated,
    activeUrlId,
  );

  const [hasHydrated, setHasHydrated] = useState(false);
  const { isSpoilerFree } = useSpoilerFree();
  const [isScoreRevealed, setIsScoreRevealed] = useState(!isSpoilerFree);

  useEffect(() => {
    setHasHydrated(true);
    setIsScoreRevealed(!isSpoilerFree);
    if (match?.id) {
      const h = match.home_team_name || 'Home';
      const a = match.away_team_name || 'Away';
      trackScreenView('Match Detail', match.competition_name);
      trackMatchView(String(match.id), h, a);
      trackMatchClick(String(match.id), `${h} vs ${a}`, match.competition_name);
      if (match.highlight_video_id) {
        trackVideoPlay(match.highlight_video_id, `${h} vs ${a}`);
      }
    }
  }, [isSpoilerFree, match?.id]);

  const homeName = match?.home_team_name || match?.home_team || 'Home';
  const awayName = match?.away_team_name || match?.away_team || 'Away';
  const compName = match?.competition_name || 'Official League';
  const siteUrl = process.env.NEXT_PUBLIC_SITE_URL || 'https://fc.dirzaaulia.com';
  const matchUrl = match?.id ? `${siteUrl}/match/${match.id}` : siteUrl;

  const pitchPlays: WebPitchPlay[] = useMemo(() => {
    if (!match?.events || match.events.length === 0) return [];
    return match.events.map((e, idx) => ({
      id: e.id || `play-${idx}`,
      clock: e.clock || '',
      type: e.type || '',
      type_text: e.type,
      text: e.text || '',
      player: e.player,
      team_name: e.side === 'home' ? homeName : awayName,
      x_percent: e.side === 'home' ? 75 : 25,
      y_percent: 50 + ((idx % 3) - 1) * 18,
    }));
  }, [match?.events, homeName, awayName]);

  const jsonLd = useMemo(() => {
    if (!match) return null;
    return generateMatchJsonLd(match, homeName, awayName, compName, matchUrl);
  }, [match, homeName, awayName, compName, matchUrl]);

  const isPendingMismatch = hasHydrated && activeUrlId && match && String(match.id) !== String(activeUrlId);

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

  return (
    <article className="max-w-7xl mx-auto px-3.5 sm:px-6 lg:px-8 py-4 sm:py-8 space-y-5 sm:space-y-8">
      {jsonLd && (
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
        />
      )}

      <MatchCentreHeader
        videoId={match.highlight_video_id}
        title={`${homeName} vs ${awayName}`}
      />

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        <div className="lg:col-span-8 space-y-6">
          {match.highlight_video_id ? (
            <div className="space-y-4">
              <MatchDetailVideoPlayer
                videoId={match.highlight_video_id}
                playbackMode={match.playback_mode}
                homeName={homeName}
                awayName={awayName}
              />
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

          <AdSenseBanner slotId="5616816386" format="horizontal" />

          {(match.status === 'FINISHED' || match.status === 'AWARDED') && isSpoilerFree && !isScoreRevealed ? (
            <MatchDetailSpoilerCard onRevealScore={() => setIsScoreRevealed(true)} />
          ) : (
            <>
              {pitchPlays.length > 0 && (
                <WebPitchRadar plays={pitchPlays} homeTeam={homeName} awayTeam={awayName} />
              )}

              {match.events && match.events.length > 0 && (
                <HorizontalMatchTimeline
                  events={match.events}
                  homeName={homeName}
                  awayName={awayName}
                  isSpoilerFree={false}
                  isLive={['IN_PLAY', 'PAUSED', 'LIVE'].includes(match.status)}
                />
              )}

              <MatchTacticalBoxscore match={match} />
              <MatchRecapSection recapAnalysis={recapAnalysis} status={match.status} />
              <MatchDayGearSection
                homeName={homeName}
                homeCrest={match.home_team_crest}
                awayName={awayName}
                awayCrest={match.away_team_crest}
              />
            </>
          )}
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
