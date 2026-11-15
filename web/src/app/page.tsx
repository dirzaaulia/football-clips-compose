import React from 'react';
import type { Metadata } from 'next';
import Image from 'next/image';
import Link from 'next/link';
import { database, getLiveMatches, MatchRecord } from '@/lib/database';
import { getAllArticles } from '@/lib/editorial-articles';
import HomeInteractiveFeed from '@/components/feed/HomeInteractiveFeed';
import GooglePlayBadge from '@/components/common/GooglePlayBadge';
import { ChevronRight } from 'lucide-react';

export const revalidate = 60; // Revalidate at most every 60s for fresh scores

export const metadata: Metadata = {
  title: 'Football Highlights & Tactical Analysis - Live Scores, Clips & Tools',
  description:
    'Watch verified official football match highlights, decisive goals, tactical debriefs, and use interactive H2H comparators for Premier League, Champions League, La Liga, and Serie A.',
  alternates: {
    canonical: 'https://fc.dirzaaulia.com/',
  },
  openGraph: {
    title: 'Football Highlights & Tactical Analysis',
    description: 'Official highlights, live scores, interactive tactical comparator, and tactical debriefs.',
    url: 'https://fc.dirzaaulia.com',
    type: 'website',
  },
};

export default async function HomePage() {
  let initialMatches: MatchRecord[] = [];
  let initialLiveMatches: MatchRecord[] = [];
  let recentHighlights: MatchRecord[] = [];

  try {
    const [matchesRes, liveRes, recentHighlightsRes] = await Promise.all([
      database
        .from('matches')
        .select('*')
        .not('highlight_video_id', 'is', null)
        .order('utc_date', { ascending: false })
        .limit(40),
      getLiveMatches(),
      database
        .from('matches')
        .select('*')
        .not('highlight_video_id', 'is', null)
        .order('utc_date', { ascending: false })
        .limit(20),
    ]);

    if (matchesRes.data) {
      initialMatches = matchesRes.data as MatchRecord[];
    }
    if (recentHighlightsRes.data) {
      recentHighlights = recentHighlightsRes.data as MatchRecord[];
    }
    initialLiveMatches = liveRes || [];
  } catch (err) {
    console.error('Failed to pre-fetch matches for SSR:', err);
  }

  const featuredArticles = getAllArticles().slice(0, 3);

  const websiteSchema = {
    '@context': 'https://schema.org',
    '@type': 'WebSite',
    name: 'Football Highlights & Clips',
    url: 'https://fc.dirzaaulia.com',
    potentialAction: {
      '@type': 'SearchAction',
      target: 'https://fc.dirzaaulia.com/?q={search_term_string}',
      'query-input': 'required name=search_term_string',
    },
  };

  return (
    <div className="w-full">
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(websiteSchema) }}
      />

      {/* Main Interactive Feed (Full-Bleed DAZN Hero Billboard at top, followed by boxed highlights) */}
      <HomeInteractiveFeed
        initialMatches={initialMatches}
        initialLiveMatches={initialLiveMatches}
        recentMatches={recentHighlights}
      />

      {/* Boxed Bottom Section: Official App Promotion & Editorial Spotlight */}
      <div className="max-w-[1600px] w-full mx-auto px-3.5 sm:px-6 lg:px-8 2xl:px-10 py-8 space-y-8">
        {/* Official Google Play OTT Download Banner */}
        <section className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-[#121316] via-[#1A1C22] to-[#121316] border border-white/10 p-5 sm:p-6 shadow-2xl flex flex-col md:flex-row items-center justify-between gap-5">
          <div className="absolute -top-12 -left-12 w-48 h-48 bg-[#DFFF00]/10 rounded-full blur-3xl pointer-events-none" />
          <div className="flex items-center gap-4 relative z-10">
            <div className="relative w-14 h-14 rounded-2xl overflow-hidden border border-[#DFFF00]/40 bg-[#0A0A0C] shadow-lg shadow-[#DFFF00]/10 shrink-0">
              <Image
                src="/app_icon.png"
                alt="Football Highlights & Clips"
                width={56}
                height={56}
                className="object-cover"
              />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="text-[10px] font-tech font-black uppercase tracking-wider px-2 py-0.5 rounded-full bg-[#DFFF00]/15 text-[#DFFF00] border border-[#DFFF00]/30">
                  Official Android App
                </span>
              </div>
              <h3 className="font-scoreboard text-xl sm:text-2xl text-white tracking-wider uppercase mt-1">
                Stream Match Highlights Anywhere on Mobile
              </h3>
              <p className="text-xs text-[#A1A1AA] mt-0.5">
                Live score alerts, zero-buffering player, and spoiler-free mode on your Android device.
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3 relative z-10 shrink-0">
            <GooglePlayBadge height={42} />
          </div>
        </section>

        {/* Server-Side Rendered Editorial Spotlight for Search Crawlers & Readers */}
        <section className="pt-6 border-t border-white/10 space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <span className="w-1 h-5 rounded-full bg-[#DFFF00] shadow-[0_0_8px_rgba(223,255,0,0.4)]" />
              <div>
                <h2 className="font-scoreboard text-xl sm:text-2xl text-white tracking-wider uppercase">Tactical Analysis &amp; Debriefs</h2>
                <p className="text-xs text-[#A1A1AA]">Curated tactical breakdowns written by verified editorial contributors.</p>
              </div>
          </div>
          <Link
            href="/articles"
            className="font-tech text-xs font-bold text-[#DFFF00] hover:text-[#cbe800] transition flex items-center gap-1"
          >
            <span>View all articles</span>
            <ChevronRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {featuredArticles.map((article) => (
            <article
              key={article.slug}
              className="p-5 rounded-2xl bg-[#121316] border border-white/10 flex flex-col justify-between space-y-3 hover:border-[#DFFF00]/40 transition-all hover:-translate-y-0.5 shadow-sm"
            >
              <div>
                <div className="flex items-center justify-between text-[11px] font-bold text-[#DFFF00] mb-2 font-tech">
                  <span className="px-2 py-0.5 rounded-full bg-[#DFFF00]/10 border border-[#DFFF00]/20 text-[10px] uppercase">
                    {article.category}
                  </span>
                  <span className="text-[#A1A1AA]">{article.readTime}</span>
                </div>
                <h3 className="text-sm font-extrabold text-white line-clamp-2 hover:text-[#DFFF00] transition-colors leading-snug">
                  <Link href={`/articles/${article.slug}`}>{article.title}</Link>
                </h3>
                <p className="text-xs text-[#A1A1AA] line-clamp-2 mt-1.5 leading-relaxed font-tech">{article.excerpt}</p>
              </div>
              <div className="pt-3 border-t border-white/10 flex items-center justify-between text-[11px] text-[#A1A1AA] font-tech">
                <span>By {article.author.name}</span>
                <Link
                  href={`/articles/${article.slug}`}
                  className="font-bold text-[#DFFF00] hover:underline inline-flex items-center gap-0.5"
                >
                  Read breakdown &rarr;
                </Link>
              </div>
            </article>
          ))}
        </div>
      </section>
      </div>
    </div>
  );
}
