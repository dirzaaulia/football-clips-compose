import React from 'react';
import Link from 'next/link';
import { Sparkles, ArrowRight, BookOpen, Clock, Newspaper, ExternalLink } from 'lucide-react';
import AdSenseBanner from '@/components/ads/AdSenseBanner';
import { database, MatchRecord, getRecentAnalysisMatches } from '@/lib/database';
import { getVerifiedFootballNews, FeedArticle } from '@/lib/articles';
import { getAllArticles, EditorialArticle } from '@/lib/editorial-articles';
import AnalysisHubClient from '@/components/articles/AnalysisHubClient';

export default async function ArticlesPage() {
  const editorialArticles: EditorialArticle[] = getAllArticles();
  const newsArticles: FeedArticle[] = await getVerifiedFootballNews(6);
  const recentMatches: MatchRecord[] = await getRecentAnalysisMatches(8);

  return (
    <div className="max-w-[1600px] w-full mx-auto px-4 sm:px-6 lg:px-8 2xl:px-10 py-6 select-none space-y-8">
      {/* Header */}
      <header className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-white/10">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-md text-[10px] font-tech font-bold uppercase tracking-wider bg-[#DFFF00]/15 text-[#DFFF00] border border-[#DFFF00]/30">
              <Sparkles className="w-3 h-3" />
              Tactical Lab
            </span>
            <span className="text-xs text-white/50 font-tech">UEFA Elite Matrix &amp; Journalism</span>
          </div>
          <h1 className="font-scoreboard text-3xl sm:text-4xl uppercase text-white tracking-wide leading-none">
            Tactical Analysis &amp; Scouting Hub
          </h1>
        </div>
        <p className="text-xs text-white/50 max-w-sm sm:text-right hidden sm:block font-tech">
          Algorithmic tactical dossiers, lidar-calibrated passing models, and verified sports journalism.
        </p>
      </header>

      {/* Dynamic Match-Driven Tactical Analysis Suite */}
      <AnalysisHubClient matches={recentMatches} />

      <AdSenseBanner slotId="5616816386" format="horizontal" />

      {/* Original Feature Articles */}
      <section className="space-y-4">
        <div className="flex items-center justify-between pb-3 border-b border-white/10">
          <div className="flex items-center gap-2">
            <BookOpen className="w-4 h-4 text-[#DFFF00]" />
            <h2 className="font-scoreboard text-2xl text-white uppercase tracking-wider">
              Original Tactical Feature Articles
            </h2>
          </div>
          <span className="text-xs text-[#DFFF00] font-tech uppercase tracking-wider font-bold">
            Long-Form Dossiers
          </span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          {editorialArticles.map((art) => (
            <Link
              key={art.slug}
              href={`/articles/${art.slug}`}
              className="group bg-[#1A1C22] hover:bg-[#252830] border border-white/10 hover:border-[#DFFF00]/40 rounded-2xl p-5 transition-all flex flex-col justify-between cursor-pointer shadow-lg"
            >
              <div>
                <div className="flex items-center justify-between text-xs text-white/50 mb-2.5">
                  <span className="font-tech font-bold text-[#DFFF00] uppercase tracking-wider text-[11px]">
                    {art.category}
                  </span>
                  <span className="flex items-center gap-1 font-tech">
                    <Clock className="w-3 h-3" />
                    {art.readTime}
                  </span>
                </div>

                <h3 className="font-tech text-base font-bold text-white group-hover:text-[#DFFF00] transition-colors mb-2 leading-snug">
                  {art.title}
                </h3>

                <p className="text-xs text-white/60 line-clamp-3 leading-relaxed mb-4">
                  {art.excerpt}
                </p>
              </div>

              <div className="flex items-center justify-between pt-3 border-t border-white/5 text-xs text-white/50">
                <span>By {art.author.name}</span>
                <span className="flex items-center gap-1 font-tech font-bold text-[#DFFF00] group-hover:translate-x-1 transition-transform">
                  Read Analysis <ArrowRight className="w-3.5 h-3.5" />
                </span>
              </div>
            </Link>
          ))}
        </div>
      </section>

      {/* Verified Football Journalism */}
      <section className="space-y-4">
        <div className="flex items-center justify-between pb-3 border-b border-white/10">
          <div className="flex items-center gap-2">
            <Newspaper className="w-4 h-4 text-[#DFFF00]" />
            <h2 className="font-scoreboard text-2xl text-white uppercase tracking-wider">
              Verified European Sports Journalism
            </h2>
          </div>
          <span className="text-xs text-white/50 font-tech">BBC Sport &amp; Sky Sports Feeds</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {newsArticles.map((news) => (
            <a
              key={news.link || news.id}
              href={news.link}
              target="_blank"
              rel="noopener noreferrer"
              className="group bg-[#121316] hover:bg-[#1A1C22] border border-white/10 rounded-xl p-4 transition-all flex flex-col justify-between cursor-pointer"
            >
              <div>
                <div className="flex items-center justify-between text-[11px] text-white/40 mb-2 font-tech">
                  <span className="font-bold text-[#DFFF00]">{news.source}</span>
                  <span>{new Date(news.pubDate).toLocaleDateString('en-GB')}</span>
                </div>
                <h4 className="text-sm font-bold text-white group-hover:text-[#DFFF00] transition-colors line-clamp-2 leading-snug">
                  {news.title}
                </h4>
              </div>

              <div className="flex items-center justify-end pt-3 text-xs text-white/40 group-hover:text-white transition-colors">
                <ExternalLink className="w-3.5 h-3.5" />
              </div>
            </a>
          ))}
        </div>
      </section>
    </div>
  );
}
