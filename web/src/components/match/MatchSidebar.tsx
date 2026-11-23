'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { Play, Calendar, Trophy, Zap } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';

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
  const { isSpoilerFree } = useSpoilerFree();
  const [activeTab, setActiveTab] = useState<'highlights' | 'fixtures'>('highlights');

  return (
    <aside className="bg-[#121316] border border-white/10 rounded-2xl p-4 sm:p-5 flex flex-col space-y-4 select-none">
      {/* Competition Header */}
      <div className="flex items-center gap-2 pb-3 border-b border-white/10 text-xs">
        <Trophy className="w-4 h-4 text-[#DFFF00]" />
        <span className="font-scoreboard uppercase tracking-wider text-white line-clamp-2 leading-tight">
          {competitionName}
        </span>
      </div>

      {/* Tabs */}
      <div className="flex items-center gap-1.5 p-1 bg-[#0A0A0C] rounded-xl border border-white/10 text-xs font-tech font-bold">
        <button
          onClick={() => setActiveTab('highlights')}
          className={`flex-1 py-1.5 rounded-lg text-center uppercase tracking-wider transition-all cursor-pointer ${
            activeTab === 'highlights'
              ? 'bg-[#DFFF00] text-[#0A0A0C] font-black shadow-sm'
              : 'text-white/60 hover:text-white'
          }`}
        >
          Highlights ({highlights.length})
        </button>
        <button
          onClick={() => setActiveTab('fixtures')}
          className={`flex-1 py-1.5 rounded-lg text-center uppercase tracking-wider transition-all cursor-pointer ${
            activeTab === 'fixtures'
              ? 'bg-[#DFFF00] text-[#0A0A0C] font-black shadow-sm'
              : 'text-white/60 hover:text-white'
          }`}
        >
          Fixtures ({fixtures.length})
        </button>
      </div>

      {/* Tab Content */}
      <div className="space-y-2.5 max-h-[600px] overflow-y-auto pr-1 scrollbar-thin">
        {activeTab === 'highlights' ? (
          highlights.length === 0 ? (
            <p className="text-xs font-tech text-white/40 text-center py-6">No other highlights found.</p>
          ) : (
            highlights.map((m) => {
              const home = m.home_team_name || m.home_team;
              const away = m.away_team_name || m.away_team;
              const thumb = m.highlight_video_id
                ? `https://i.ytimg.com/vi/${m.highlight_video_id}/mqdefault.jpg`
                : null;

              return (
                <Link
                  key={m.id}
                  href={`/match/${m.id}`}
                  className="group flex items-center gap-3 p-2.5 rounded-xl bg-[#1A1C22] hover:bg-[#252830] border border-white/10 hover:border-[#DFFF00]/40 transition-all"
                >
                  <div className="relative w-20 h-12 rounded-lg overflow-hidden bg-black shrink-0">
                    {thumb ? (
                      <Image
                        src={thumb}
                        alt={`${home} vs ${away}`}
                        fill
                        className="object-cover group-hover:scale-105 transition-transform"
                        unoptimized
                      />
                    ) : (
                      <div className="w-full h-full bg-gradient-to-tr from-[#121316] to-[#1A1C22] flex items-center justify-center">
                        <Play className="w-3.5 h-3.5 text-[#DFFF00]" />
                      </div>
                    )}
                    <div className="absolute inset-0 bg-black/30 flex items-center justify-center">
                      <Play className="w-3.5 h-3.5 text-white fill-current opacity-80 group-hover:opacity-100" />
                    </div>
                  </div>

                  <div className="min-w-0 flex-1">
                    <h4 className="text-xs sm:text-sm font-extrabold text-white group-hover:text-[#DFFF00] transition-colors line-clamp-2 leading-snug">
                      {home} vs {away}
                    </h4>
                    <span className="text-xs font-tech text-white/75 font-semibold">
                      {isSpoilerFree
                        ? 'Highlight Available'
                        : m.home_score !== null
                        ? `${m.home_score} - ${m.away_score}`
                        : 'Full Time'}
                    </span>
                  </div>
                </Link>
              );
            })
          )
        ) : (
          fixtures.length === 0 ? (
            <p className="text-xs font-tech text-white/70 text-center py-6">No scheduled fixtures found.</p>
          ) : (
            fixtures.map((m) => {
              const home = m.home_team_name || m.home_team;
              const away = m.away_team_name || m.away_team;
              const isFinished = m.status === 'FINISHED';
              const isLive = ['IN_PLAY', 'PAUSED', 'LIVE'].includes(m.status);

              return (
                <Link
                  key={m.id}
                  href={`/match/${m.id}`}
                  className="block p-3 rounded-xl bg-[#1A1C22] hover:bg-[#252830] border border-white/10 hover:border-[#DFFF00]/40 transition-all text-xs"
                >
                  <div className="flex items-center justify-between text-xs text-white/75 font-tech mb-1.5">
                    <span className="flex items-center gap-1 font-medium">
                      <Calendar className="w-3 h-3 text-[#DFFF00]" />
                      {new Date(m.utc_date).toLocaleDateString('en-GB', { day: 'numeric', month: 'short' })}
                    </span>
                    <span
                      className={
                        isLive
                          ? 'text-red-400 font-black animate-pulse flex items-center gap-1'
                          : isFinished
                          ? 'text-white/80 font-bold'
                          : 'text-[#38BDF8] font-bold'
                      }
                    >
                      {isLive && <span className="w-1.5 h-1.5 rounded-full bg-red-500 animate-ping" />}
                      {isLive ? (m.status === 'PAUSED' ? 'HT' : m.display_clock ? `LIVE ${m.display_clock}` : 'LIVE') : isFinished ? 'FT' : 'Upcoming'}
                    </span>
                  </div>
                  <div className="flex items-center justify-between font-extrabold text-white text-xs sm:text-sm gap-2">
                    <span className="line-clamp-2 leading-snug flex-1">{home}</span>
                    <span className="px-2 text-center text-[#DFFF00] font-scoreboard shrink-0 text-sm">
                      {isLive || (isFinished && !isSpoilerFree)
                        ? `${m.home_score ?? 0} - ${m.away_score ?? 0}`
                        : 'vs'}
                    </span>
                    <span className="line-clamp-2 leading-snug flex-1 text-right">{away}</span>
                  </div>
                </Link>
              );
            })
          )
        )}
      </div>

      {/* Vertical Shorts Vault Card */}
      <div className="pt-2 border-t border-white/10">
        <Link
          href="/shorts"
          className="relative rounded-xl overflow-hidden p-3 bg-[#1A1C22] hover:bg-[#252830] border border-white/10 hover:border-[#DFFF00]/40 flex items-center justify-between group cursor-pointer transition-all"
        >
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-[#DFFF00] text-[#0A0A0C] flex items-center justify-center font-bold shrink-0">
              <Zap className="w-4 h-4 fill-[#0A0A0C]" />
            </div>
            <div className="flex flex-col min-w-0">
              <span className="font-tech text-xs sm:text-sm font-bold text-white group-hover:text-[#DFFF00] transition-colors">
                SHORTS VAULT
              </span>
              <span className="font-tech text-xs text-white/75">
                60-Second Wondergoals &amp; Fast Snips
              </span>
            </div>
          </div>
          <Play className="w-4 h-4 text-[#DFFF00] shrink-0" />
        </Link>
      </div>
    </aside>
  );
}
