'use client';

import React from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { Tv, Sparkles, ExternalLink, Play, Calendar } from 'lucide-react';
import { MatchRecord } from '@/lib/supabase';
import ModalSideSheet from '@/components/common/ModalSideSheet';

interface VideoPlayerSideSheetProps {
  match: MatchRecord | null;
  isOpen: boolean;
  onClose: () => void;
  relatedMatches?: MatchRecord[];
  onSelectMatch?: (match: MatchRecord) => void;
}

function getMatchCentreUrl(m: MatchRecord): string {
  const home = m.home_team_name || m.home_team;
  const away = m.away_team_name || m.away_team;
  const params = new URLSearchParams();
  if (home) params.set('home', home);
  if (away) params.set('away', away);
  if (m.competition_name) params.set('comp', m.competition_name);
  if (m.highlight_video_id) params.set('vid', m.highlight_video_id);
  if (m.utc_date) params.set('date', m.utc_date);
  if (m.home_team_crest) params.set('homeCrest', m.home_team_crest);
  if (m.away_team_crest) params.set('awayCrest', m.away_team_crest);
  return `/match/${m.id}?${params.toString()}`;
}

export default function VideoPlayerSideSheet({
  match,
  isOpen,
  onClose,
  relatedMatches = [],
  onSelectMatch,
}: VideoPlayerSideSheetProps) {
  if (!match) return null;

  const homeName = match.home_team_name || match.home_team;
  const awayName = match.away_team_name || match.away_team;
  const videoId = match.highlight_video_id;
  const matchCentreUrl = getMatchCentreUrl(match);

  const titleHeader = (
    <div className="flex items-center gap-2">
      <div className="flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-black tracking-wider uppercase bg-[#1E3A8A]/50 text-[#7098F8] border border-[#7098F8]/40 shadow-[0_0_10px_rgba(112,152,248,0.2)]">
        <Tv className="w-3 h-3 text-[#7098F8]" />
        <span>Match Centre</span>
      </div>
      <span className="text-xs text-[#94A3B8] font-semibold truncate hidden sm:inline">
        {match.competition_name}
      </span>
    </div>
  );

  const headerAction = (
    <Link
      href={matchCentreUrl}
      onClick={onClose}
      className="flex items-center gap-1 px-2.5 py-1 rounded-xl text-xs font-bold bg-[#1E3A8A] hover:bg-[#7098F8] text-white border border-[#7098F8]/40 transition-all cursor-pointer shadow-sm"
    >
      <Sparkles className="w-3.5 h-3.5 text-[#4FC3F7]" />
      <span>Tactical Debrief</span>
      <ExternalLink className="w-3 h-3 ml-0.5 opacity-80" />
    </Link>
  );

  return (
    <ModalSideSheet
      isOpen={isOpen}
      onClose={onClose}
      title={titleHeader}
      headerAction={headerAction}
      widthClass="sm:max-w-[560px] md:max-w-[600px]"
    >
      <div className="flex flex-col space-y-4 p-4 sm:p-5">
        {/* 16:9 YouTube Player Box */}
        <div className="relative w-full pt-[56.25%] bg-black rounded-2xl overflow-hidden border border-[#334155] shadow-2xl">
          {videoId ? (
            <iframe
              className="absolute inset-0 w-full h-full"
              src={`https://www.youtube.com/embed/${videoId}?autoplay=1&rel=0&modestbranding=1`}
              title={`${homeName} vs ${awayName} Highlights`}
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
              allowFullScreen
            />
          ) : (
            <div className="absolute inset-0 flex items-center justify-center text-xs text-[#94A3B8]">
              Highlight video not available for this fixture.
            </div>
          )}
        </div>

        {/* Match Metadata & Scoreline Box */}
        <div className="bg-[#101418] border border-[#334155] rounded-2xl p-4 space-y-3">
          <div className="flex items-center justify-between text-xs text-[#94A3B8] pb-2 border-b border-[#334155]/60">
            <span className="font-extrabold text-[#7098F8] uppercase tracking-wider">
              {match.competition_name}
            </span>
            <div className="flex items-center gap-1.5">
              <Calendar className="w-3.5 h-3.5 text-[#94A3B8]" />
              <span>
                {new Date(match.utc_date).toLocaleDateString('en-GB', {
                  day: 'numeric',
                  month: 'short',
                  year: 'numeric',
                })}
              </span>
            </div>
          </div>

          {/* Teams & Score Bar */}
          <div className="flex items-center justify-between gap-3">
            <div className="flex items-center gap-2 flex-1 min-w-0">
              {match.home_team_crest && (
                <div className="relative w-7 h-7 shrink-0">
                  <Image src={match.home_team_crest} alt={homeName} fill className="object-contain" unoptimized />
                </div>
              )}
              <span className="font-extrabold text-sm text-white leading-tight break-words">
                {homeName}
              </span>
            </div>

            <div className="px-3.5 py-1.5 bg-[#181C20] border border-[#7098F8]/40 rounded-xl text-center shadow-inner shrink-0">
              <span className="font-black text-sm text-white">
                {match.home_score ?? 0} - {match.away_score ?? 0}
              </span>
            </div>

            <div className="flex items-center justify-end gap-2 flex-1 min-w-0 text-right">
              <span className="font-extrabold text-sm text-white leading-tight break-words">
                {awayName}
              </span>
              {match.away_team_crest && (
                <div className="relative w-7 h-7 shrink-0">
                  <Image src={match.away_team_crest} alt={awayName} fill className="object-contain" unoptimized />
                </div>
              )}
            </div>
          </div>

          {/* Dedicated Match Centre Navigation Link */}
          <div className="pt-2">
            <Link
              href={matchCentreUrl}
              onClick={onClose}
              className="w-full flex items-center justify-center gap-2 py-2 px-4 rounded-xl bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white font-bold text-xs shadow-[0_0_15px_rgba(112,152,248,0.25)] hover:brightness-110 transition-all cursor-pointer"
            >
              <Sparkles className="w-4 h-4 text-[#4FC3F7]" />
              <span>Open Match Centre &amp; Tactical Debrief</span>
            </Link>
          </div>
        </div>

        {/* Related Highlights LazyRow Parity */}
        {relatedMatches.length > 0 && (
          <div className="space-y-2.5 pt-1">
            <h3 className="text-xs font-extrabold text-white tracking-wide uppercase text-[#94A3B8]">
              More Highlights
            </h3>
            <div className="flex items-center gap-3 overflow-x-auto pb-2 scrollbar-none touch-pan-x">
              {relatedMatches.slice(0, 6).map((rel) => {
                const relThumb = rel.highlight_video_id
                  ? `https://i.ytimg.com/vi/${rel.highlight_video_id}/mqdefault.jpg`
                  : '/favicon.png';
                const relHome = rel.home_team_name || rel.home_team;
                const relAway = rel.away_team_name || rel.away_team;

                return (
                  <div
                    key={rel.id}
                    onClick={() => onSelectMatch?.(rel)}
                    className="w-[180px] shrink-0 bg-[#101418] hover:bg-[#1E2227] border border-[#334155] hover:border-[#7098F8]/60 rounded-xl overflow-hidden p-2 cursor-pointer transition-all flex flex-col justify-between group"
                  >
                    <div className="relative w-full aspect-video rounded-lg overflow-hidden bg-black mb-1.5">
                      <Image
                        src={relThumb}
                        alt={`${relHome} vs ${relAway}`}
                        fill
                        className="object-cover group-hover:scale-105 transition-transform"
                        unoptimized
                      />
                      <div className="absolute inset-0 bg-black/20 group-hover:bg-black/0 transition-colors" />
                      <div className="absolute bottom-1 right-1 px-1.5 py-0.5 rounded bg-black/75 text-[10px] font-bold text-white flex items-center gap-0.5">
                        <Play className="w-2.5 h-2.5 fill-white" />
                      </div>
                    </div>
                    <p className="text-[11px] font-bold text-white line-clamp-1 group-hover:text-[#7098F8] transition-colors">
                      {relHome} vs {relAway}
                    </p>
                    <span className="text-[10px] text-[#94A3B8] line-clamp-1 mt-0.5">
                      {rel.competition_name}
                    </span>
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </div>
    </ModalSideSheet>
  );
}
