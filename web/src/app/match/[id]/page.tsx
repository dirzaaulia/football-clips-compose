import React from 'react';
import type { Metadata } from 'next';
import { notFound } from 'next/navigation';
import { getMatchById, getRelatedMatches, MatchRecord, supabase } from '@/lib/supabase';
import { generateMatchRecap } from '@/lib/gemini';
import { fetchEspnMatchSummary } from '@/lib/espn';
import MatchDetailClient from '@/components/match/MatchDetailClient';

interface MatchPageProps {
  params: Promise<{ id: string }>;
}

async function resolveMatch(id: string): Promise<MatchRecord | null> {
  const match = await getMatchById(id);
  if (!match) return null;

  // Enrich with ESPN live statistics & lineups if not yet in Supabase
  const isFinished = match.status === 'FINISHED';
  const isLive = match.status === 'IN_PLAY' || match.status === 'PAUSED' || match.status === 'LIVE';

  if (!match.statistics && (isFinished || isLive)) {
    try {
      const espnData = await fetchEspnMatchSummary(match.id, match.competition_id);
      if (espnData) {
        return {
          ...match,
          statistics: espnData.statistics || match.statistics,
          formations: espnData.formations || match.formations,
          editorial_headline: espnData.editorial_headline || match.editorial_headline,
          editorial_story: espnData.editorial_story || match.editorial_story,
          editorial_source: espnData.editorial_source || match.editorial_source,
        };
      }
    } catch {
      // Return base match
    }
  }

  return match;
}

export async function generateStaticParams() {
  try {
    const { data } = await supabase
      .from('matches')
      .select('id')
      .order('utc_date', { ascending: false })
      .limit(1000);
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

  return (
    <MatchDetailClient
      initialMatch={match}
      initialRecap={recapAnalysis}
      initialRelated={related}
      expectedId={id}
    />
  );
}
