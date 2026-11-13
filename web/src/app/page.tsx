import React from 'react';
import type { Metadata } from 'next';
import { supabase, getLiveMatches, MatchRecord } from '@/lib/database';
import { getAllArticles } from '@/lib/editorial-articles';
import HomeInteractiveFeed from '@/components/feed/HomeInteractiveFeed';
import RecentMatchesSection from '@/components/feed/RecentMatchesSection';
import { Wrench, BookOpen, ChevronRight, Sparkles } from 'lucide-react';
import Link from 'next/link';

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

  try {
    const [matchesRes, liveRes] = await Promise.all([
      supabase
        .from('matches')
        .select('*')
        .order('utc_date', { ascending: false })
        .limit(24),
      getLiveMatches(),
    ]);

    if (matchesRes.data) {
      initialMatches = matchesRes.data as MatchRecord[];
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
    <div className="max-w-[1600px] w-full mx-auto px-3.5 sm:px-6 lg:px-8 2xl:px-10 py-4 space-y-8">
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(websiteSchema) }}
      />

      {/* Featured Tools & Editorial Callout Banner */}
      <section className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Tools Suite Callout */}
        <Link
          href="/tools"
          className="group p-5 rounded-3xl bg-gradient-to-br from-[#1E293B]/70 via-[#181C20] to-[#1E293B]/40 border border-[#334155]/60 hover:border-[#7098F8]/50 transition-all flex items-center justify-between"
        >
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-2xl bg-[#7098F8]/10 border border-[#7098F8]/30 flex items-center justify-center text-[#7098F8] group-hover:scale-105 transition-transform">
              <Wrench className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="text-xs font-black text-[#7098F8] uppercase tracking-wider">New Interactive Suite</span>
                <Sparkles className="w-3.5 h-3.5 text-[#7098F8]" />
              </div>
              <h3 className="text-base font-black text-white group-hover:text-[#7098F8] transition-colors">
                Tactical Tools & H2H Comparator
              </h3>
              <p className="text-xs text-[#94A3B8] mt-0.5">
                Compare club archetypes, league form matrix, and find verified TV broadcasters.
              </p>
            </div>
          </div>
          <ChevronRight className="w-5 h-5 text-[#94A3B8] group-hover:text-white transition-colors shrink-0" />
        </Link>

        {/* Editorial Standards & Articles Callout */}
        <Link
          href="/articles"
          className="group p-5 rounded-3xl bg-gradient-to-br from-[#1E293B]/70 via-[#181C20] to-[#1E293B]/40 border border-[#334155]/60 hover:border-[#A855F7]/50 transition-all flex items-center justify-between"
        >
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-2xl bg-[#A855F7]/10 border border-[#A855F7]/30 flex items-center justify-center text-[#A855F7] group-hover:scale-105 transition-transform">
              <BookOpen className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="text-xs font-black text-[#A855F7] uppercase tracking-wider">Original Analysis</span>
              </div>
              <h3 className="text-base font-black text-white group-hover:text-[#A855F7] transition-colors">
                European Tactical Debriefs
              </h3>
              <p className="text-xs text-[#94A3B8] mt-0.5">
                Exhaustive analysis on high-press evolution, set-piece routines, and rest defense.
              </p>
            </div>
          </div>
          <ChevronRight className="w-5 h-5 text-[#94A3B8] group-hover:text-white transition-colors shrink-0" />
        </Link>
      </section>

      {/* Main Interactive Feed (Hydrated with SSR Initial Matches) */}
      <HomeInteractiveFeed
        initialMatches={initialMatches}
        initialLiveMatches={initialLiveMatches}
      />

      {/* 20 Recent Matches Across Competitions */}
      <RecentMatchesSection matches={initialMatches} className="pt-6 border-t border-[#334155]/60" />

      {/* Server-Side Rendered Editorial Spotlight for Search Crawlers & Readers */}
      <section className="pt-6 border-t border-[#334155]/60 space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg font-black text-white tracking-tight">Tactical Analysis & Debriefs</h2>
            <p className="text-xs text-[#94A3B8]">Curated tactical breakdowns written by verified editorial contributors.</p>
          </div>
          <Link
            href="/articles"
            className="text-xs font-bold text-[#7098F8] hover:underline flex items-center gap-1"
          >
            <span>View all articles</span>
            <ChevronRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {featuredArticles.map((article) => (
            <article
              key={article.slug}
              className="p-5 rounded-2xl bg-[#181C20] border border-[#334155]/60 flex flex-col justify-between space-y-3 hover:border-[#7098F8]/40 transition-colors"
            >
              <div>
                <div className="flex items-center justify-between text-[11px] font-bold text-[#7098F8] mb-2">
                  <span>{article.category}</span>
                  <span className="text-[#94A3B8]">{article.readTime}</span>
                </div>
                <h3 className="text-sm font-extrabold text-white line-clamp-2 hover:text-[#7098F8] transition-colors">
                  <Link href={`/articles/${article.slug}`}>{article.title}</Link>
                </h3>
                <p className="text-xs text-[#94A3B8] line-clamp-2 mt-1.5 leading-relaxed">{article.excerpt}</p>
              </div>
              <div className="pt-3 border-t border-[#334155]/30 flex items-center justify-between text-[11px] text-[#94A3B8]">
                <span>By {article.author.name}</span>
                <Link
                  href={`/articles/${article.slug}`}
                  className="font-bold text-[#7098F8] hover:underline inline-flex items-center gap-0.5"
                >
                  Read breakdown &rarr;
                </Link>
              </div>
            </article>
          ))}
        </div>
      </section>
    </div>
  );
}
