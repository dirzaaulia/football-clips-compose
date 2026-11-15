import React from 'react';
import { MatchRecord } from '@/lib/database';
import { FeedCompetitionGroup } from '@/components/feed/feedGroupHelpers';
import CompetitionFeedRail from '@/components/feed/CompetitionFeedRail';
import HighlightGridCard from '@/components/feed/HighlightGridCard';
import NativeInFeedAdCard from '@/components/ads/NativeInFeedAdCard';
import AdSenseBanner from '@/components/ads/AdSenseBanner';
import RecentMatchesSection from '@/components/feed/RecentMatchesSection';
import HomeShortsRail from '@/components/shorts/HomeShortsRail';
import { Trophy } from 'lucide-react';

interface HomeHighlightsFeedProps {
  displayedMatches: MatchRecord[];
  groupedFeedMatches: FeedCompetitionGroup[] | null;
  recentMatches: MatchRecord[];
  collapsedCompIds: Set<string>;
  toggleCollapse: (compId: string) => void;
  setSelectedComp: (compId: string) => void;
  onPlay: (match: MatchRecord) => void;
}

export default function HomeHighlightsFeed({
  displayedMatches,
  groupedFeedMatches,
  recentMatches,
  setSelectedComp,
  onPlay,
}: HomeHighlightsFeedProps) {
  if (displayedMatches.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center p-12 text-center rounded-2xl bg-[#121316] border border-white/10 my-6">
        <Trophy className="w-10 h-10 text-white/30 mb-3" />
        <h3 className="text-base font-bold text-white font-scoreboard uppercase tracking-wider">
          No Highlights Found
        </h3>
        <p className="text-xs text-white/50 mt-1 max-w-sm font-tech">
          No highlights match your selected competition or filters. Try clearing your filters or selecting another league.
        </p>
      </div>
    );
  }

  if (groupedFeedMatches) {
    return (
      <div className="space-y-6 my-4">
        <RecentMatchesSection matches={recentMatches} onPlay={onPlay} className="pb-4 border-b border-white/10" />

        {groupedFeedMatches.map((group, groupIdx) => {
          return (
            <React.Fragment key={group.compId}>
              <CompetitionFeedRail
                compId={group.compId}
                name={group.name}
                emblemUrl={group.emblem}
                matches={group.matches}
                onPlay={onPlay}
                onSeeAll={(id) => setSelectedComp(id)}
              />

              {/* 9:16 Shorts Rail after the top league */}
              {groupIdx === 0 && <HomeShortsRail />}

              {(groupIdx + 1) % 2 === 0 && groupIdx < groupedFeedMatches.length - 1 && (
                <AdSenseBanner slotId="5616816386" format="auto" className="my-4" />
              )}
            </React.Fragment>
          );
        })}
      </div>
    );
  }

  // Filtered view: Multi-column grid with infinite scroll
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 sm:gap-5 my-6 animate-in fade-in duration-300">
      {displayedMatches.map((m, idx) => (
        <React.Fragment key={m.id}>
          <HighlightGridCard match={m} onPlay={onPlay} />
          {idx > 0 && idx % 6 === 0 && <NativeInFeedAdCard slotId="5616816386" />}
        </React.Fragment>
      ))}
    </div>
  );
}
