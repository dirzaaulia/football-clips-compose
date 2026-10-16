import React from 'react';
import type { Metadata } from 'next';
import { notFound } from 'next/navigation';
import Image from 'next/image';
import { getMatchById, getRelatedMatches, getRecentHighlights, MatchRecord, supabase } from '@/lib/supabase';
import { generateMatchRecap } from '@/lib/gemini';
import AdSenseBanner from '@/components/ads/AdSenseBanner';
import MatchCentreHeader from '@/components/match/MatchCentreHeader';
import MatchSidebar from '@/components/match/MatchSidebar';
import MatchScoreSection from '@/components/match/MatchScoreSection';
import MatchRecapSection from '@/components/match/MatchRecapSection';
import MatchTacticalBoxscore from '@/components/match/MatchTacticalBoxscore';
import { Trophy, Calendar } from 'lucide-react';

interface MatchPageProps {
  params: Promise<{ id: string }>;
}

async function resolveMatch(id: string): Promise<MatchRecord | null> {
  return await getMatchById(id);
}


export async function generateStaticParams() {
  try {
    const { data } = await supabase
      .from('matches')
      .select('id')
      .order('utc_date', { ascending: false })
      .limit(300);
    return (data || []).map((m) => ({ id: String(m.id) }));
  } catch {
    return [];
  }
}

export async function generateMetadata({ params }: MatchPageProps): Promise<Metadata> {
  const { id } = await params;
  const match = await resolveMatch(id);
  if (!match) return { title: 'Match Not Found' };


  const homeName = match.home_team_name || match.home_team || 'Home Team';
  const awayName = match.away_team_name || match.away_team || 'Away Team';
  const compName = match.competition_name || 'Official Highlights';
  const isFinished = match.status === 'FINISHED';
  const isLive = match.status === 'IN_PLAY' || match.status === 'PAUSED' || match.status === 'LIVE';

  const scoreInfo = isFinished
    ? `Final score: ${match.home_score ?? 0} - ${match.away_score ?? 0}.`
    : isLive
    ? `Live score: ${match.home_score ?? 0} - ${match.away_score ?? 0}.`
    : `Scheduled kickoff.`;

  const title = `${homeName} vs ${awayName} Match Centre & Tactical Analysis`;
  const description = `Watch highlights and read full tactical breakdown for ${homeName} vs ${awayName} in the ${compName}. ${scoreInfo}`;

  return {
    title,
    description,
    openGraph: {
      title,
      description,
      type: 'article',
      images: match.highlight_video_id
        ? [`https://i.ytimg.com/vi/${match.highlight_video_id}/hqdefault.jpg`]
        : [],
    },
  };
}

export default async function MatchDetailPage({ params }: MatchPageProps) {
  const { id } = await params;
  const match = await resolveMatch(id);


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

      <MatchCentreHeader videoId={match.highlight_video_id} title={`${homeName} vs ${awayName}`} />

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

              <MatchScoreSection
                homeScore={match.home_score}
                awayScore={match.away_score}
                status={match.status}
              />

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

          {/* Authentic Match Centre & Tactical Data (Boxscore, Lineups & Editorial Report) */}
          <MatchTacticalBoxscore match={match} />

          {/* AI Tactical Breakdown & Match Report (Spoiler Guarded) */}
          <MatchRecapSection recapAnalysis={recapAnalysis} />
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
