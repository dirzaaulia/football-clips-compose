'use client';

import React from 'react';
import Image from 'next/image';
import { Trophy } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import FixtureItemCard from '@/components/fixtures/FixtureItemCard';
import FixturesSkeleton from '@/components/fixtures/FixturesSkeleton';
import FixturesInFeedAd from '@/components/fixtures/FixturesInFeedAd';

export interface CompetitionFixtureGroup {
  competitionId: string;
  competitionName: string;
  logo?: string;
  matches: MatchRecord[];
}

interface FixturesGroupListProps {
  isLoading: boolean;
  totalCount: number;
  groups: CompetitionFixtureGroup[];
}

export default function FixturesGroupList({
  isLoading,
  totalCount,
  groups,
}: FixturesGroupListProps) {
  if (isLoading) return <FixturesSkeleton />;

  if (totalCount === 0) {
    return (
      <div className="text-center py-16 bg-[#121316] rounded-2xl border border-white/10 space-y-2">
        <p className="font-tech text-white/50 text-sm">
          No fixtures scheduled matching the selected filters.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {groups.map((group, groupIdx) => {
        const sampleMatch = group.matches[0];
        const stageLabel = sampleMatch?.stage || sampleMatch?.round_name || (sampleMatch?.matchday ? `Matchday ${sampleMatch.matchday}` : 'Official Matchday');
        return (
          <React.Fragment key={group.competitionName}>
            <section className="space-y-3">
              {/* Stitch League Group Header */}
              <div className="flex items-center justify-between px-4 py-3 rounded-xl bg-[#1B1B1F] border-l-4 border-[#DFFF00] border-t border-r border-b border-white/5">
                <div className="flex items-center gap-3">
                  <div className="relative w-10 h-10 rounded-lg bg-[#292A2E] p-1.5 flex items-center justify-center shrink-0 border border-white/10">
                    {group.logo ? (
                      <Image
                        src={group.logo}
                        alt={group.competitionName}
                        fill
                        className="object-contain p-1"
                        unoptimized
                      />
                    ) : (
                      <Trophy className="w-5 h-5 text-[#DFFF00]" />
                    )}
                  </div>
                  <div>
                    <div className="flex items-center gap-2 flex-wrap">
                      <h2 className="font-scoreboard text-lg sm:text-xl font-bold text-white uppercase tracking-wider leading-none">
                        {group.competitionName}
                      </h2>
                      <span className="px-2 py-0.5 rounded text-[10px] font-tech uppercase bg-[#DFFF00]/15 text-[#DFFF00] border border-[#DFFF00]/30 font-bold">
                        {stageLabel}
                      </span>
                    </div>
                    <p className="text-xs text-white/45 font-tech mt-0.5">
                      Official European Broadcaster Feed • Calibrated Match Telemetry
                    </p>
                  </div>
                </div>
                <span className="font-tech text-xs font-bold text-white/60 bg-[#0D0E11] px-3 py-1 rounded-full border border-white/10 shrink-0">
                  {group.matches.length} {group.matches.length === 1 ? 'Match' : 'Matches'}
                </span>
              </div>

              {/* Match Cards Stack */}
              <div className="space-y-2.5">
                {group.matches.map((m) => (
                  <FixtureItemCard key={m.id} match={m} />
                ))}
              </div>
            </section>

            {(groupIdx + 1) % 2 === 0 && groupIdx < groups.length - 1 && <FixturesInFeedAd />}
          </React.Fragment>
        );
      })}
    </div>
  );
}
