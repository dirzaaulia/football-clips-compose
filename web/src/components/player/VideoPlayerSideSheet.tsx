'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { Sparkles, Calendar, Minimize2, Eye, EyeOff, ExternalLink } from 'lucide-react';
import { MatchRecord, isRegionSupported, getMatchById } from '@/lib/database';
import { fetchEspnMatchSummary } from '@/lib/espn';
import ModalSideSheet from '@/components/common/ModalSideSheet';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';
import { useVideoPlayer } from '@/hooks/useVideoPlayer';
import { useUserRegion } from '@/hooks/useUserRegion';
import { PlayerErrorType } from './PlayerErrorOverlay';
import PlayerVideoFrame from './PlayerVideoFrame';
import MatchCentreHero from './MatchCentreHero';
import MatchTacticalBoxscore from '@/components/match/MatchTacticalBoxscore';
import PlayerRelatedHighlights from './PlayerRelatedHighlights';

interface VideoPlayerSideSheetProps {
  match: MatchRecord | null;
  isOpen: boolean;
  onClose: () => void;
  relatedMatches?: MatchRecord[];
  onSelectMatch?: (match: MatchRecord) => void;
}

export default function VideoPlayerSideSheet({
  match,
  isOpen,
  onClose,
  relatedMatches = [],
  onSelectMatch,
}: VideoPlayerSideSheetProps) {
  const { isSpoilerFree } = useSpoilerFree();
  const { minimize } = useVideoPlayer();
  const { country } = useUserRegion();
  const [isScoreRevealed, setIsScoreRevealed] = useState(false);
  const [enrichedMatch, setEnrichedMatch] = useState<MatchRecord | null>(match);

  const homeName = match?.home_team_name || match?.home_team || 'Home';
  const awayName = match?.away_team_name || match?.away_team || 'Away';
  const primaryVideoId = match?.highlight_video_id || null;
  const backupVideoId = match?.backup_highlight_video_id || null;
  const isPrimaryAllowed = isRegionSupported(match?.youtube_region_info, country);
  const isBackupAllowed = isRegionSupported(match?.backup_youtube_region_info, country);
  const [activeVideoId, setActiveVideoId] = useState<string | null>(primaryVideoId);
  const [hasSwitchedToBackup, setHasSwitchedToBackup] = useState(false);
  const [errorState, setErrorState] = useState<PlayerErrorType>('NONE');
  const [retryNonce, setRetryNonce] = useState(0);

  // Sync and enrich match data if opened with partial data
  useEffect(() => {
    setEnrichedMatch(match);
    if (!match) return;

    let isCancelled = false;
    const isFinished = match.status === 'FINISHED' || match.status === 'AWARDED';
    const isNotFinished = !isFinished;
    const isMissingData = !match.statistics || !match.events;

    if (isNotFinished || isMissingData) {
      Promise.all([
        getMatchById(String(match.id)).catch(() => null),
        fetchEspnMatchSummary(match.id, match.competition_id, isNotFinished).catch(() => null),
      ]).then(([freshMatch, data]) => {
        if (!isCancelled && (freshMatch || data)) {
          setEnrichedMatch((prev) => {
            if (!prev) return null;
            const base = freshMatch || prev;
            return {
              ...base,
              status: data?.status || base.status || prev.status,
              home_score: data?.home_score != null ? data.home_score : (base.home_score ?? prev.home_score),
              away_score: data?.away_score != null ? data.away_score : (base.away_score ?? prev.away_score),
              display_clock: data?.display_clock || base.display_clock || prev.display_clock,
              statistics: data?.statistics || base.statistics || prev.statistics,
              formations: data?.formations || base.formations || prev.formations,
              editorial_headline: data?.editorial_headline || base.editorial_headline || prev.editorial_headline,
              editorial_story: data?.editorial_story || base.editorial_story || prev.editorial_story,
              editorial_source: data?.editorial_source || base.editorial_source || prev.editorial_source,
              events: data?.events || base.events || prev.events,
            };
          });
        }
      });
    }

    return () => {
      isCancelled = true;
    };
  }, [match?.id, match?.status]);

  // Real-time polling while live match SideSheet is open
  useEffect(() => {
    if (!isOpen || !enrichedMatch) return;
    const isLive = ['IN_PLAY', 'PAUSED', 'LIVE'].includes(enrichedMatch.status);
    if (!isLive) return;

    let isCancelled = false;
    const pollInterval = setInterval(async () => {
      try {
        const [freshMatch, data] = await Promise.all([
          getMatchById(String(enrichedMatch.id)).catch(() => null),
          fetchEspnMatchSummary(enrichedMatch.id, enrichedMatch.competition_id, true).catch(() => null),
        ]);
        if (!isCancelled && (freshMatch || data)) {
          setEnrichedMatch((prev) => {
            if (!prev) return null;
            const base = freshMatch || prev;
            return {
              ...base,
              status: data?.status || base.status || prev.status,
              home_score: data?.home_score != null ? data.home_score : (base.home_score ?? prev.home_score),
              away_score: data?.away_score != null ? data.away_score : (base.away_score ?? prev.away_score),
              display_clock: data?.display_clock || base.display_clock || prev.display_clock,
              statistics: data?.statistics || base.statistics || prev.statistics,
              formations: data?.formations || base.formations || prev.formations,
              events: data?.events || base.events || prev.events,
            };
          });
        }
      } catch {}
    }, 8000);

    return () => {
      isCancelled = true;
      clearInterval(pollInterval);
    };
  }, [isOpen, enrichedMatch?.id, enrichedMatch?.status]);

  // Smart Region-Aware Stream Selection
  useEffect(() => {
    if (!match) return;
    if (!primaryVideoId && !backupVideoId) {
      setActiveVideoId(null);
      setErrorState('NONE');
      return;
    }

    if (!isPrimaryAllowed && isBackupAllowed && backupVideoId) {
      setActiveVideoId(backupVideoId);
      setHasSwitchedToBackup(true);
      setErrorState('NONE');
    } else if (!isPrimaryAllowed && !isBackupAllowed) {
      setErrorState('REGION_BLOCKED');
    } else {
      setActiveVideoId(primaryVideoId);
      setHasSwitchedToBackup(false);
      setErrorState('NONE');
    }
  }, [match?.id, country, primaryVideoId, backupVideoId, isPrimaryAllowed, isBackupAllowed]);

  // YouTube Iframe PostMessage Error Interceptor
  useEffect(() => {
    function handleMessage(e: MessageEvent) {
      try {
        const data = typeof e.data === 'string' ? JSON.parse(e.data) : e.data;
        if (data?.event === 'onError' || data?.info === 101 || data?.info === 150) {
          const code = Number(data?.info);
          if (code === 101 || code === 150 || code === 100) {
            if (backupVideoId && !hasSwitchedToBackup && backupVideoId !== activeVideoId && isBackupAllowed) {
              setHasSwitchedToBackup(true);
              setActiveVideoId(backupVideoId);
              setErrorState('NONE');
            } else {
              setErrorState('NOT_EMBEDDABLE');
            }
          } else if (code === 2 || code === 5) {
            setErrorState('USER_SIDE_ERROR');
          }
        }
      } catch {
        // Non-JSON postMessage ignored
      }
    }
    window.addEventListener('message', handleMessage);
    return () => window.removeEventListener('message', handleMessage);
  }, [activeVideoId, backupVideoId, hasSwitchedToBackup, isBackupAllowed]);

  if (!match) return null;

  const currentMatch = enrichedMatch || match;
  const isFinished = currentMatch.status === 'FINISHED' || currentMatch.status === 'AWARDED';
  // Live matches never hide score; spoiler mode is strictly for finished matches
  const isHidden = isSpoilerFree && !isScoreRevealed && isFinished;
  const hasVideo = Boolean(activeVideoId);

  return (
    <ModalSideSheet
      isOpen={isOpen}
      onClose={onClose}
      title={`${homeName} vs ${awayName}`}
      widthClass="w-full sm:max-w-[620px]"
      headerAction={
        <div className="flex items-center gap-1">
          <Link
            href={`/match/${currentMatch.id}`}
            onClick={onClose}
            className="p-2 text-[#94A3B8] hover:text-white hover:bg-[#1E2227] rounded-xl transition-colors cursor-pointer"
            title="Open Dedicated Match Centre"
          >
            <ExternalLink className="w-4 h-4 text-[#7098F8]" />
          </Link>
          {hasVideo && (
            <button
              onClick={minimize}
              className="p-2 text-[#94A3B8] hover:text-white hover:bg-[#1E2227] rounded-xl transition-colors cursor-pointer"
              title="Minimize to Picture-in-Picture"
            >
              <Minimize2 className="w-4 h-4 text-[#7098F8]" />
            </button>
          )}
        </div>
      }
    >
      <div className="space-y-5 pb-8">
        {/* Top Hero: Embedded Video or Scheduled/Live Match Hero */}
        {hasVideo ? (
          <div className="space-y-4">
            <PlayerVideoFrame
              activeVideoId={activeVideoId}
              homeName={homeName}
              awayName={awayName}
              country={country}
              errorState={errorState}
              retryNonce={retryNonce}
              onRetry={() => {
                setErrorState('NONE');
                setRetryNonce((n) => n + 1);
              }}
              hasSwitchedToBackup={hasSwitchedToBackup}
            />

            {/* Score & Teams Pill beneath video */}
            <div className="bg-[#101418] border border-[#334155]/70 rounded-2xl p-3.5 flex items-center justify-between gap-3 shadow-md">
              <div className="flex items-center gap-2 flex-1 min-w-0">
                {currentMatch.home_team_crest && (
                  <div className="relative w-6 h-6 shrink-0">
                    <Image src={currentMatch.home_team_crest} alt={homeName} fill className="object-contain" unoptimized />
                  </div>
                )}
                <span className="font-extrabold text-xs text-white truncate">{homeName}</span>
              </div>

              {isHidden ? (
                <button
                  onClick={() => setIsScoreRevealed(true)}
                  className="flex items-center gap-1 px-3 py-1 bg-[#181C20] border border-[#7098F8]/40 hover:border-[#7098F8] rounded-xl text-xs font-bold text-[#4FC3F7] cursor-pointer"
                >
                  <Eye className="w-3.5 h-3.5" />
                  <span>Reveal</span>
                </button>
              ) : (
                <div
                  onClick={() => isSpoilerFree && setIsScoreRevealed(false)}
                  className={`px-3 py-1 bg-[#181C20] border border-[#7098F8]/40 rounded-xl text-xs font-black text-white ${
                    isSpoilerFree ? 'cursor-pointer hover:border-[#7098F8]' : ''
                  }`}
                >
                  {currentMatch.home_score ?? 0} - {currentMatch.away_score ?? 0}
                </div>
              )}

              <div className="flex items-center justify-end gap-2 flex-1 min-w-0 text-right">
                <span className="font-extrabold text-xs text-white truncate">{awayName}</span>
                {currentMatch.away_team_crest && (
                  <div className="relative w-6 h-6 shrink-0">
                    <Image src={currentMatch.away_team_crest} alt={awayName} fill className="object-contain" unoptimized />
                  </div>
                )}
              </div>
            </div>
          </div>
        ) : (
          <MatchCentreHero
            match={currentMatch}
            homeName={homeName}
            awayName={awayName}
            isSpoilerFree={isSpoilerFree}
            isScoreRevealed={isScoreRevealed}
            onRevealScore={() => setIsScoreRevealed(true)}
            onHideScore={() => setIsScoreRevealed(false)}
          />
        )}

        {/* Unified Tactical Boxscore (Timeline, Stats, Lineups, Report) */}
        <MatchTacticalBoxscore match={currentMatch} />

        {/* Related Highlights */}
        <PlayerRelatedHighlights
          relatedMatches={relatedMatches}
          onSelectMatch={onSelectMatch}
        />
      </div>
    </ModalSideSheet>
  );
}
