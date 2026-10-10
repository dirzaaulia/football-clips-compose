'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { Play, Calendar, Trophy } from 'lucide-react';
import { MatchRecord } from '@/lib/supabase';

interface MatchSidebarProps {
  competitionName: string;
  highlights: MatchRecord[];
  fixtures: MatchRecord[];
}

export default function MatchSidebar({
  competitionName,
  highlights,
  fixtures,
}: MatchSidebarProps) {
  const [activeTab, setActiveTab] = useState<'highlights' | 'fixtures'>('highlights');

  return (
    <aside className="bg-[#181C20] border border-[#334155] rounded-3xl p-4 sm:p-5 flex flex-col space-y-4">
      {/* Competition Header */}
      <div className="flex items-center gap-2 pb-3 border-b border-[#334155]/60 text-xs">
        <Trophy className="w-4 h-4 text-[#7098F8]" />
        <span className="font-extrabold uppercase tracking-wider text-white truncate">
          {competitionName}
        </span>
      </div>

      {/* Tabs */}
      <div className="flex items-center gap-1.5 p-1 bg-[#101418] rounded-xl border border-[#334155]/60 text-xs font-bold">
        <button
          onClick={() => setActiveTab('highlights')}
          className={`flex-1 py-1.5 rounded-lg text-center transition-all cursor-pointer ${
            activeTab === 'highlights'
              ? 'bg-[#1E3A8A] text-white shadow-sm'
              : 'text-[#94A3B8] hover:text-white'
          }`}
        >
          Highlights ({highlights.length})
        </button>
        <button
          onClick={() => setActiveTab('fixtures')}
          className={`flex-1 py-1.5 rounded-lg text-center transition-all cursor-pointer ${
            activeTab === 'fixtures'
              ? 'bg-[#1E3A8A] text-white shadow-sm'
              : 'text-[#94A3B8] hover:text-white'
          }`}
        >
          Fixtures ({fixtures.length})
        </button>
      </div>

      {/* Tab Content */}
      <div className="space-y-2.5 max-h-[600px] overflow-y-auto pr-1">
        {activeTab === 'highlights' ? (
          highlights.length === 0 ? (
            <p className="text-xs text-[#94A3B8] text-center py-6">No other highlights found.</p>
          ) : (
            highlights.map((m) => {
              const home = m.home_team_name || m.home_team;
              const away = m.away_team_name || m.away_team;
              const thumb = m.highlight_video_id
                ? `https://i.ytimg.com/vi/${m.highlight_video_id}/mqdefault.jpg`
                : '/app_icon.png';

              return (
                <Link
                  key={m.id}
                  href={`/match/${m.id}`}
                  className="group flex items-center gap-3 p-2 rounded-2xl bg-[#1E2227] hover:bg-[#252B32] border border-[#334155]/60 hover:border-[#7098F8]/50 transition-all"
                >
                  <div className="relative w-20 h-12 rounded-xl overflow-hidden bg-black shrink-0">
                    <Image
                      src={thumb}
                      alt={`${home} vs ${away}`}
                      fill
                      className="object-cover group-hover:scale-105 transition-transform"
                      unoptimized
                    />
                    <div className="absolute inset-0 bg-black/30 flex items-center justify-center">
                      <Play className="w-3.5 h-3.5 text-white fill-current opacity-80 group-hover:opacity-100" />
                    </div>
                  </div>

                  <div className="min-w-0 flex-1">
                    <h4 className="text-xs font-bold text-white group-hover:text-[#7098F8] transition-colors truncate">
                      {home} vs {away}
                    </h4>
                    <span className="text-[10px] text-[#94A3B8]">
                      {m.home_score !== null ? `${m.home_score} - ${m.away_score}` : 'Full Time'}
                    </span>
                  </div>
                </Link>
              );
            })
          )
        ) : (
          fixtures.length === 0 ? (
            <p className="text-xs text-[#94A3B8] text-center py-6">No scheduled fixtures found.</p>
          ) : (
            fixtures.map((m) => {
              const home = m.home_team_name || m.home_team;
              const away = m.away_team_name || m.away_team;
              const isFinished = m.status === 'FINISHED';

              return (
                <Link
                  key={m.id}
                  href={`/match/${m.id}`}
                  className="block p-2.5 rounded-xl bg-[#1E2227] hover:bg-[#252B32] border border-[#334155]/60 hover:border-[#7098F8]/40 transition-all text-xs"
                >
                  <div className="flex items-center justify-between text-[10px] text-[#94A3B8] mb-1">
                    <span className="flex items-center gap-1">
                      <Calendar className="w-2.5 h-2.5" />
                      {new Date(m.utc_date).toLocaleDateString('en-GB', { day: 'numeric', month: 'short' })}
                    </span>
                    <span className={isFinished ? 'text-emerald-400 font-bold' : 'text-[#4FC3F7]'}>
                      {isFinished ? 'Finished' : 'Upcoming'}
                    </span>
                  </div>
                  <div className="flex items-center justify-between font-bold text-white">
                    <span className="truncate flex-1">{home}</span>
                    <span className="px-2 text-center text-[#7098F8]">
                      {isFinished ? `${m.home_score ?? 0} - ${m.away_score ?? 0}` : 'vs'}
                    </span>
                    <span className="truncate flex-1 text-right">{away}</span>
                  </div>
                </Link>
              );
            })
          )
        )}
      </div>
    </aside>
  );
}
