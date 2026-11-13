'use client';

import React from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { MatchRecord } from '@/lib/database';
import { Clock, ChevronRight } from 'lucide-react';

interface RecentMatchesSectionProps {
  matches: MatchRecord[];
  className?: string;
}

function formatMatchTime(dateStr?: string): string {
  if (!dateStr) return '';
  const dt = new Date(dateStr);
  const now = new Date();
  const isToday = dt.toDateString() === now.toDateString();
  const yesterday = new Date();
  yesterday.setDate(now.getDate() - 1);
  const isYesterday = dt.toDateString() === yesterday.toDateString();

  if (isToday) {
    return 'Today';
  }
  if (isYesterday) {
    return 'Yesterday';
  }
  return dt.toLocaleDateString('en-GB', { day: 'numeric', month: 'short' });
}

export default function RecentMatchesSection({ matches, className = '' }: RecentMatchesSectionProps) {
  const recent20 = matches.slice(0, 20);

  if (recent20.length === 0) {
    return null;
  }

  return (
    <section className={`space-y-3 ${className}`}>
      <div className="flex items-center justify-between pb-1 border-b border-[#334155]/40">
        <div className="flex items-center gap-2">
          <Clock className="w-4 h-4 text-[#7098F8]" />
          <h2 className="text-base font-extrabold text-white tracking-tight">Recent Matches</h2>
          <span className="text-[11px] font-mono px-2 py-0.5 rounded-full bg-[#1E293B] text-[#7098F8] border border-[#7098F8]/30 font-bold">
            Latest 20
          </span>
        </div>
        <Link
          href="/fixtures"
          className="text-xs font-bold text-[#7098F8] hover:text-white transition flex items-center gap-0.5"
        >
          <span>All Fixtures</span>
          <ChevronRight className="w-3.5 h-3.5" />
        </Link>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
        {recent20.map((m) => {
          const homeCrest = m.home_team_crest || '/favicon.png';
          const awayCrest = m.away_team_crest || '/favicon.png';
          const homeName = m.home_team_name || m.home_team || 'Home';
          const awayName = m.away_team_name || m.away_team || 'Away';
          const hasScore = m.home_score !== null && m.home_score !== undefined;

          return (
            <Link
              key={m.id}
              href={`/match/${m.id}`}
              className="group p-3 rounded-2xl bg-[#181C20] border border-[#334155]/60 hover:border-[#7098F8]/50 hover:bg-[#1E2227] transition-all flex flex-col justify-between gap-2.5 shadow-sm"
            >
              <div className="flex items-center justify-between text-[11px] text-[#94A3B8]">
                <span className="font-semibold truncate max-w-[150px]">
                  {m.competition_name || m.competition_id}
                </span>
                <span className="font-mono text-[10px] shrink-0">
                  {formatMatchTime(m.utc_date)}
                </span>
              </div>

              <div className="space-y-1.5">
                <div className="flex items-center justify-between gap-2">
                  <div className="flex items-center gap-2 min-w-0">
                    <div className="w-5 h-5 relative shrink-0">
                      <Image
                        src={homeCrest}
                        alt={homeName}
                        width={20}
                        height={20}
                        className="object-contain"
                        unoptimized
                      />
                    </div>
                    <span className="text-xs font-bold text-white truncate group-hover:text-[#7098F8] transition-colors">
                      {homeName}
                    </span>
                  </div>
                  {hasScore && (
                    <span className="font-mono text-xs font-black text-white shrink-0">
                      {m.home_score}
                    </span>
                  )}
                </div>

                <div className="flex items-center justify-between gap-2">
                  <div className="flex items-center gap-2 min-w-0">
                    <div className="w-5 h-5 relative shrink-0">
                      <Image
                        src={awayCrest}
                        alt={awayName}
                        width={20}
                        height={20}
                        className="object-contain"
                        unoptimized
                      />
                    </div>
                    <span className="text-xs font-bold text-white truncate group-hover:text-[#7098F8] transition-colors">
                      {awayName}
                    </span>
                  </div>
                  {hasScore && (
                    <span className="font-mono text-xs font-black text-white shrink-0">
                      {m.away_score ?? 0}
                    </span>
                  )}
                </div>
              </div>

              <div className="pt-1.5 border-t border-[#334155]/30 flex items-center justify-between text-[10px] text-[#94A3B8]">
                <span className="px-1.5 py-0.2 rounded bg-slate-800 text-slate-300 font-bold">
                  {m.status === 'FINISHED' ? 'FT' : m.status}
                </span>
                <span className="text-[#7098F8] group-hover:underline">
                  Match Centre &rarr;
                </span>
              </div>
            </Link>
          );
        })}
      </div>
    </section>
  );
}
