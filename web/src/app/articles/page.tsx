import React from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { Sparkles, ArrowRight, Trophy, ExternalLink, Newspaper, BookOpen, Clock } from 'lucide-react';
import AdSenseBanner from '@/components/ads/AdSenseBanner';
import { supabase, MatchRecord } from '@/lib/database';
import { getVerifiedFootballNews, FeedArticle } from '@/lib/articles';
import { generateAlgorithmicTacticalRecap } from '@/lib/match-analysis';
import { getAllArticles, EditorialArticle } from '@/lib/editorial-articles';

function buildMatchUrl(match: MatchRecord): string {
  const home = match.home_team_name || match.home_team;
  const away = match.away_team_name || match.away_team;
  const params = new URLSearchParams();
  if (home) params.set('home', home);
  if (away) params.set('away', away);
  if (match.competition_name) params.set('comp', match.competition_name);
  if (match.highlight_video_id) params.set('vid', match.highlight_video_id);
  if (match.utc_date) params.set('date', match.utc_date);
  if (match.home_team_crest) params.set('homeCrest', match.home_team_crest);
  if (match.away_team_crest) params.set('awayCrest', match.away_team_crest);
  return `/match/${match.id}?${params.toString()}`;
}

export default async function ArticlesPage() {
  // 1. Original long-form editorial feature articles (AdSense Tier 1 Value)
  const editorialArticles: EditorialArticle[] = getAllArticles();

  // 2. Fetch real European matches for tactical debriefs
  const { data: rawMatches } = await supabase
    .from('matches')
    .select('*')
    .not('highlight_video_id', 'is', null)
    .order('utc_date', { ascending: false })
    .limit(8);

  const matches: MatchRecord[] = (rawMatches as unknown as MatchRecord[]) || [];

  // 3. Fetch verified live football news from BBC Sport & Sky Sports
  const newsArticles: FeedArticle[] = await getVerifiedFootballNews(8);

  return (
    <div className="max-w-[1600px] w-full mx-auto px-4 sm:px-6 lg:px-8 2xl:px-10 py-6">
      {/* Compact Header */}
      <header className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 mb-4 border-b border-[#334155]/60">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[11px] font-bold bg-[#1E3A8A]/50 text-[#4FC3F7] border border-[#7098F8]/40">
              <Sparkles className="w-3 h-3" />
              Intelligence Hub
            </span>
            <span className="text-xs text-[#94A3B8]">Tactical Intelligence &amp; Live News</span>
          </div>
          <h1 className="text-xl sm:text-2xl font-black text-white tracking-tight">
            Tactical Analysis &amp; Verified News
          </h1>
        </div>
        <p className="text-xs text-[#94A3B8] max-w-sm sm:text-right hidden sm:block">
          Authentic tactical breakdowns and verified sports journalism across top competitions.
        </p>
      </header>

      <AdSenseBanner slotId="5616816386" format="horizontal" />

      {/* Section 1: Original In-Depth Editorial Analyses */}
      <section className="my-8">
        <div className="flex items-center justify-between pb-3 border-b border-[#334155]/60 mb-5">
          <div className="flex items-center gap-2">
            <BookOpen className="w-4 h-4 text-[#7098F8]" />
            <h2 className="text-lg font-black text-white">Original Tactical Feature Articles</h2>
          </div>
          <span className="text-xs text-[#4FC3F7] font-semibold">Long-Form Journalism</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {editorialArticles.map((art) => (
            <Link
              key={art.slug}
              href={`/articles/${art.slug}`}
              className="group bg-[#1E2227] hover:bg-[#23282F] border border-[#334155] hover:border-[#7098F8]/60 rounded-2xl p-5 transition-all flex flex-col justify-between cursor-pointer"
            >
              <div>
                <div className="flex items-center justify-between text-xs text-[#94A3B8] mb-2.5">
                  <span className="font-bold text-[#7098F8] uppercase tracking-wider text-[11px]">
                    {art.category}
                  </span>
                  <span className="flex items-center gap-1">
                    <Clock className="w-3 h-3" />
                    {art.readTime}
                  </span>
                </div>

                <h3 className="text-base font-bold text-white group-hover:text-[#4FC3F7] transition-colors mb-2 leading-snug">
                  {art.title}
                </h3>

                <p className="text-xs text-[#94A3B8] line-clamp-3 leading-relaxed mb-4">
                  {art.excerpt}
                </p>
              </div>

              <div className="flex items-center justify-between pt-3 border-t border-[#334155]/40 text-xs">
                <span className="text-[#64748B] font-medium">By {art.author.name}</span>
                <span className="text-[#7098F8] group-hover:translate-x-1 transition-transform inline-flex items-center gap-1 font-bold">
                  Read Analysis <ArrowRight className="w-3 h-3" />
                </span>
              </div>
            </Link>
          ))}
        </div>
      </section>

      {/* Section 1: Tactical Match Debriefs */}
      <section className="my-8">
        <div className="flex items-center justify-between pb-3 border-b border-[#334155]/60 mb-5">
          <div className="flex items-center gap-2">
            <Trophy className="w-4 h-4 text-[#7098F8]" />
            <h2 className="text-lg font-black text-white">Tactical Match Debriefs</h2>
          </div>
          <span className="text-xs text-[#4FC3F7] font-semibold">AI Match Architecture</span>
        </div>

        {matches.length === 0 ? (
          <div className="text-center py-10 bg-[#181C20] rounded-2xl border border-[#334155]">
            <p className="text-[#94A3B8] text-sm">No match debriefs available currently.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {matches.map((match) => {
              const homeName = match.home_team_name || match.home_team;
              const awayName = match.away_team_name || match.away_team;
              const analysis =
                match.recap_analysis ||
                generateAlgorithmicTacticalRecap(match);

              const excerpt = analysis.slice(0, 160).replace(/[*#]/g, '') + '...';

              return (
                <Link
                  key={match.id}
                  href={buildMatchUrl(match)}
                  className="group bg-[#1E2227] hover:bg-[#23282F] border border-[#334155] hover:border-[#7098F8]/60 rounded-2xl p-5 transition-all flex flex-col justify-between cursor-pointer"
                >
                  <div>
                    <div className="flex items-center justify-between text-xs text-[#94A3B8] mb-2.5">
                      <span className="font-bold text-[#7098F8] uppercase tracking-wider">
                        {match.competition_name}
                      </span>
                      <span>{new Date(match.utc_date).toLocaleDateString('en-GB', { dateStyle: 'medium' })}</span>
                    </div>

                    <div className="flex items-center gap-2.5 mb-2">
                      {match.home_team_crest && (
                        <div className="relative w-5 h-5 shrink-0">
                          <Image src={match.home_team_crest} alt={homeName} fill className="object-contain" unoptimized />
                        </div>
                      )}
                      <h3 className="text-sm sm:text-base font-bold text-white group-hover:text-[#7098F8] transition-colors line-clamp-1">
                        {homeName} {match.home_score != null ? match.home_score : ''} {match.home_score != null && match.away_score != null ? '-' : 'vs'} {match.away_score != null ? match.away_score : ''} {awayName}
                      </h3>
                      {match.away_team_crest && (
                        <div className="relative w-5 h-5 shrink-0">
                          <Image src={match.away_team_crest} alt={awayName} fill className="object-contain" unoptimized />
                        </div>
                      )}
                    </div>

                    <p className="text-xs text-[#94A3B8] leading-relaxed mb-4">{excerpt}</p>
                  </div>

                  <div className="pt-3 border-t border-[#334155]/60 flex items-center justify-between text-xs text-[#7098F8] font-bold group-hover:translate-x-0.5 transition-transform">
                    <span>Read Full Debrief &amp; Highlights</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </div>
                </Link>
              );
            })}
          </div>
        )}
      </section>

      {/* Section 2: Live Verified European Club News */}
      <section className="my-8">
        <div className="flex items-center justify-between pb-3 border-b border-[#334155]/60 mb-5">
          <div className="flex items-center gap-2">
            <Newspaper className="w-4 h-4 text-[#4FC3F7]" />
            <h2 className="text-lg font-black text-white">Live Verified Sports Journalism</h2>
          </div>
          <span className="text-xs text-[#94A3B8]">Verified Football Broadcast Feeds</span>
        </div>

        {newsArticles.length === 0 ? (
          <div className="text-center py-10 bg-[#181C20] rounded-2xl border border-[#334155]">
            <p className="text-[#94A3B8] text-sm">Live news feed updating...</p>
          </div>
        ) : (
          <div className="space-y-3">
            {newsArticles.map((art) => (
              <a
                key={art.id}
                href={art.link}
                target="_blank"
                rel="noopener noreferrer"
                className="group block bg-[#181C20] hover:bg-[#1E2227] border border-[#334155] hover:border-[#7098F8]/50 rounded-2xl p-4 transition-all"
              >
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-1.5">
                  <div className="flex items-center gap-2">
                    <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-[#1E3A8A]/40 text-[#4FC3F7] border border-[#7098F8]/30">
                      {art.source}
                    </span>
                    <h3 className="text-sm font-bold text-white group-hover:text-[#7098F8] transition-colors">
                      {art.title}
                    </h3>
                  </div>
                  <div className="flex items-center gap-2 text-xs text-[#94A3B8] shrink-0">
                    <span>{art.pubDate}</span>
                    <ExternalLink className="w-3.5 h-3.5 text-[#7098F8]" />
                  </div>
                </div>
                {art.description && (
                  <p className="text-xs text-[#94A3B8] line-clamp-2 leading-relaxed">
                    {art.description}
                  </p>
                )}
              </a>
            ))}
          </div>
        )}
      </section>

      <AdSenseBanner slotId="5616816386" format="auto" />
    </div>
  );
}
