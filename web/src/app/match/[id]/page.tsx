import React from 'react';
import type { Metadata } from 'next';
import { notFound } from 'next/navigation';
import Image from 'next/image';
import { getMatchById, getRelatedMatches, getRecentHighlights, MatchRecord } from '@/lib/supabase';
import { generateMatchRecap } from '@/lib/gemini';
import AdSenseBanner from '@/components/ads/AdSenseBanner';
import MatchCentreHeader from '@/components/match/MatchCentreHeader';
import MatchSidebar from '@/components/match/MatchSidebar';
import { Trophy, Calendar, Sparkles } from 'lucide-react';

interface MatchPageProps {
  params: Promise<{ id: string }>;
  searchParams?: Promise<{ [key: string]: string | string[] | undefined }>;
}

async function resolveMatch(
  id: string,
  searchParams?: Promise<{ [key: string]: string | string[] | undefined }>
): Promise<MatchRecord | null> {
  const match = await getMatchById(id);
  if (match) return match;

  const sp = searchParams ? await searchParams : {};
  if (sp.home && sp.away) {
    const home = typeof sp.home === 'string' ? sp.home : 'Home Team';
    const away = typeof sp.away === 'string' ? sp.away : 'Away Team';
    const comp = typeof sp.comp === 'string' ? sp.comp : 'Official Highlights';
    const vid = typeof sp.vid === 'string' ? sp.vid : null;
    const date = typeof sp.date === 'string' ? sp.date : new Date().toISOString();
    const homeCrest = typeof sp.homeCrest === 'string' ? sp.homeCrest : null;
    const awayCrest = typeof sp.awayCrest === 'string' ? sp.awayCrest : null;

    return {
      id,
      competition_id: 'ALL',
      competition_name: comp,
      utc_date: date,
      status: 'FINISHED',
      home_team: home,
      away_team: away,
      home_team_name: home,
      away_team_name: away,
      home_team_crest: homeCrest,
      away_team_crest: awayCrest,
      home_score: null,
      away_score: null,
      highlight_video_id: vid,
      recap_analysis: null,
    };
  }

  return null;
}

export async function generateStaticParams() {
  try {
    const matches = await getRecentHighlights(20, 0);
    return matches.map((m) => ({ id: String(m.id) }));
  } catch {
    return [];
  }
}

export async function generateMetadata({ params, searchParams }: MatchPageProps): Promise<Metadata> {
  const { id } = await params;
  const match = await resolveMatch(id, searchParams);
  if (!match) return { title: 'Match Not Found' };

  const title = `${match.home_team} vs ${match.away_team} Match Centre & Tactical Analysis`;
  const description = `Watch highlights and read full tactical breakdown for ${match.home_team} vs ${match.away_team} in the ${match.competition_name}. Final score: ${match.home_score ?? 0} - ${match.away_score ?? 0}.`;

  return {
    title,
    description,
    openGraph: {
      title,
      description,
      type: 'article',
      images: match.highlight_video_id
        ? [`https://i.ytimg.com/vi/${match.highlight_video_id}/maxresdefault.jpg`]
        : [],
    },
  };
}

export default async function MatchDetailPage({ params, searchParams }: MatchPageProps) {
  const { id } = await params;
  const match = await resolveMatch(id, searchParams);

  if (!match) notFound();

  const recapPromise = generateMatchRecap(match);
  const relatedPromise = getRelatedMatches(match.competition_id, match.id, 8);
  const [recapAnalysis, related] = await Promise.all([recapPromise, relatedPromise]);

  const homeName = match.home_team_name || match.home_team;
  const awayName = match.away_team_name || match.away_team;

  const jsonLd = {
    '@context': 'https://schema.org',
    '@type': 'SportsEvent',
    name: `${homeName} vs ${awayName}`,
    sport: 'Football',
    startDate: match.utc_date,
    homeTeam: { '@type': 'SportsTeam', name: homeName },
    awayTeam: { '@type': 'SportsTeam', name: awayName },
    video: match.highlight_video_id
      ? {
          '@type': 'VideoObject',
          name: `${homeName} vs ${awayName} Highlights`,
          description: `Watch official highlights from ${match.competition_name}`,
          thumbnailUrl: `https://i.ytimg.com/vi/${match.highlight_video_id}/hqdefault.jpg`,
          embedUrl: `https://www.youtube.com/embed/${match.highlight_video_id}`,
          uploadDate: match.utc_date,
        }
      : undefined,
  };

  return (
    <article className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
      />

      <MatchCentreHeader videoId={match.highlight_video_id} />

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        <div className="lg:col-span-8 space-y-6">
          {match.highlight_video_id ? (
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
                  Official Match Broadcast
                </span>
                <span className="text-[#4FC3F7] font-semibold">1080p High Definition</span>
              </div>
            </div>
          ) : null}

          <div className="rounded-3xl bg-[#181C20] border border-[#334155] p-6 space-y-4 shadow-xl">
            <div className="flex items-center justify-between text-xs text-[#94A3B8] pb-3 border-b border-[#334155]/60">
              <div className="flex items-center gap-1.5 font-bold text-[#7098F8] uppercase tracking-wider">
                <Trophy className="w-3.5 h-3.5" />
                <span>{match.competition_name}</span>
              </div>
              <div className="flex items-center gap-1.5">
                <Calendar className="w-3.5 h-3.5 text-[#94A3B8]" />
                <span>{new Date(match.utc_date).toLocaleDateString('en-GB', { dateStyle: 'full' })}</span>
              </div>
            </div>

            <div className="flex items-center justify-between gap-4 py-2">
              <div className="flex items-center gap-3 flex-1 min-w-0">
                {match.home_team_crest && (
                  <div className="relative w-10 h-10 sm:w-12 sm:h-12 shrink-0">
                    <Image src={match.home_team_crest} alt={homeName} fill className="object-contain" unoptimized />
                  </div>
                )}
                <span className="text-base sm:text-xl font-black text-white leading-tight">{homeName}</span>
              </div>

              <div className="px-5 py-2.5 bg-[#101418] border border-[#7098F8]/40 rounded-2xl text-center shadow-inner shrink-0">
                <span className="text-xl sm:text-2xl font-black text-white">
                  {match.home_score ?? 0} - {match.away_score ?? 0}
                </span>
                <div className="text-[10px] font-bold uppercase tracking-wider text-[#4FC3F7] mt-0.5">Full Time</div>
              </div>

              <div className="flex items-center justify-end gap-3 flex-1 min-w-0 text-right">
                <span className="text-base sm:text-xl font-black text-white leading-tight">{awayName}</span>
                {match.away_team_crest && (
                  <div className="relative w-10 h-10 sm:w-12 sm:h-12 shrink-0">
                    <Image src={match.away_team_crest} alt={awayName} fill className="object-contain" unoptimized />
                  </div>
                )}
              </div>
            </div>
          </div>

          <AdSenseBanner slotId="5616816386" format="horizontal" />

          {/* AI Tactical Breakdown & Match Report */}
          <section className="rounded-3xl bg-[#181C20] border border-[#334155] p-6 sm:p-8 space-y-4">
            <div className="flex items-center gap-2 pb-3 border-b border-[#334155]/60 text-[#7098F8]">
              <Sparkles className="w-4 h-4 text-[#4FC3F7]" />
              <h2 className="text-sm font-extrabold uppercase tracking-wider text-white">
                Tactical Breakdown &amp; Match Analysis
              </h2>
            </div>

            <div className="prose prose-invert max-w-none text-[#F1F5F9] text-xs sm:text-sm leading-relaxed space-y-3 whitespace-pre-line">
              {recapAnalysis}
            </div>
          </section>
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
