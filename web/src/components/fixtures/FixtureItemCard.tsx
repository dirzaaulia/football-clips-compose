'use client';

import React, { useMemo } from 'react';
import { useRouter } from 'next/navigation';
import { Play, BarChart3 } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';
import { useVideoPlayer } from '@/hooks/useVideoPlayer';
import {
  FixtureStatusBlock,
  FixtureScoreboard,
  FixtureSubBar,
  getScorers,
  getKeyEvent,
} from './FixtureCardSubComponents';

interface FixtureItemCardProps {
  match: MatchRecord;
}

export default function FixtureItemCard({ match }: FixtureItemCardProps) {
  const router = useRouter();
  const { isSpoilerFree } = useSpoilerFree();
  const { playVideo } = useVideoPlayer();

  const isFinished = match.status === 'FINISHED';
  const isLive = match.status === 'IN_PLAY' || match.status === 'PAUSED' || match.status === 'LIVE';
  const isPaused = match.status === 'PAUSED';
  const hasHighlight = Boolean(match.highlight_video_id);
  const shouldHideScore = isSpoilerFree && isFinished;

  const homeName = match.home_team_name || match.home_team;
  const awayName = match.away_team_name || match.away_team;
  const matchTime = new Date(match.utc_date).toLocaleTimeString('en-GB', {
    hour: '2-digit',
    minute: '2-digit',
  });

  const homeScorers = useMemo(() => getScorers(match.events, 'home'), [match.events]);
  const awayScorers = useMemo(() => getScorers(match.events, 'away'), [match.events]);
  const keyEventText = useMemo(() => getKeyEvent(match, shouldHideScore), [match, shouldHideScore]);

  const rawHomePoss = parseFloat(match.statistics?.home?.['possessionPct'] || '0');
  const hasPossession = !shouldHideScore && rawHomePoss > 0;
  const homePoss = Math.round(rawHomePoss);
  const awayPoss = Math.max(0, 100 - homePoss);

  return (
    <article
      onClick={() => router.push(`/match/${match.id}`)}
      className={`group relative rounded-xl p-4 sm:p-5 transition-all border flex flex-col gap-3 cursor-pointer select-none overflow-hidden ${
        isLive
          ? 'bg-[#18191E] hover:bg-[#202127] border-[#DFFF00]/30 shadow-[0_0_20px_rgba(223,255,0,0.08)]'
          : 'bg-[#18191E] hover:bg-[#202127] border-white/5 hover:border-white/15 shadow-md'
      }`}
    >
      {isLive && (
        <div className="absolute left-0 top-0 bottom-0 w-1 bg-[#DFFF00] shadow-[0_0_10px_#DFFF00]" />
      )}

      <div className="flex flex-col xl:flex-row xl:items-center justify-between gap-4">
        <FixtureStatusBlock
          match={match}
          isLive={isLive}
          isPaused={isPaused}
          isFinished={isFinished}
          matchTime={matchTime}
        />

        <FixtureScoreboard
          match={match}
          homeName={homeName}
          awayName={awayName}
          homeScorers={homeScorers}
          awayScorers={awayScorers}
          shouldHideScore={shouldHideScore}
          isLive={isLive}
          isFinished={isFinished}
        />

        <div className="flex items-center gap-2 shrink-0 justify-end pt-2 xl:pt-0 border-t xl:border-t-0 border-white/5">
          {hasHighlight ? (
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                playVideo(match);
              }}
              className="px-3.5 py-2 rounded-lg bg-[#DFFF00] hover:bg-[#cbe800] text-[#0A0A0C] font-tech text-xs font-bold uppercase tracking-wider flex items-center gap-1.5 transition-all shadow-[0_0_12px_rgba(223,255,0,0.25)] cursor-pointer"
            >
              <Play className="w-3.5 h-3.5 fill-[#0A0A0C]" />
              <span>Watch Highlights</span>
            </button>
          ) : (
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                router.push(`/match/${match.id}`);
              }}
              className="px-3.5 py-2 rounded-lg bg-[#292A2E] hover:bg-[#32343A] text-white font-tech text-xs font-bold uppercase tracking-wider flex items-center gap-1.5 border border-white/10 transition-colors cursor-pointer"
            >
              <BarChart3 className="w-3.5 h-3.5 text-[#DFFF00]" />
              <span>Match Centre</span>
            </button>
          )}

          <button
            type="button"
            onClick={(e) => {
              e.stopPropagation();
              router.push(`/match/${match.id}`);
            }}
            className="p-2 rounded-lg bg-[#292A2E] hover:bg-[#32343A] text-white/70 hover:text-white border border-white/10 transition-colors cursor-pointer"
            title="Tactical Telemetry"
          >
            <BarChart3 className="w-4 h-4 text-[#DFFF00]" />
          </button>
        </div>
      </div>

      <FixtureSubBar
        keyEventText={keyEventText}
        hasPossession={hasPossession}
        homePoss={homePoss}
        awayPoss={awayPoss}
        isLive={isLive}
        isFinished={isFinished}
      />
    </article>
  );
}
