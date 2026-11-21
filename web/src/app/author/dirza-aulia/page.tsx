import React from 'react';
import type { Metadata } from 'next';
import Link from 'next/link';
import {
  ShieldCheck,
  Award,
  BookOpen,
  Clock,
  ArrowRight,
  Mail,
  Globe,
  Code,
  ChevronRight,
  ExternalLink,
} from 'lucide-react';
import { getAllArticles } from '@/lib/editorial-articles';

export const metadata: Metadata = {
  title: 'Dirza Aulia - Editorial Lead & Tactical Analyst | Football Highlights & Clips',
  description:
    'Profile, credentials, tactical methodology, and published football intelligence dossiers by Dirza Aulia, accredited tactical analyst for Football Highlights & Clips.',
  alternates: {
    canonical: 'https://fc.dirzaaulia.com/author/dirza-aulia',
  },
  openGraph: {
    title: 'Dirza Aulia - Editorial Lead & Tactical Analyst',
    description:
      'Author profile and published tactical analysis dossiers covering European football structures, Opta metrics, and match dynamics.',
    url: 'https://fc.dirzaaulia.com/author/dirza-aulia',
    type: 'profile',
  },
};

export default function AuthorProfilePage() {
  const articles = getAllArticles();
  const siteUrl = 'https://fc.dirzaaulia.com';

  const profileSchema = {
    '@context': 'https://schema.org',
    '@type': 'ProfilePage',
    mainEntity: {
      '@type': 'Person',
      name: 'Dirza Aulia',
      jobTitle: 'Editorial Lead & Tactical Analyst',
      worksFor: {
        '@type': 'Organization',
        name: 'Football Highlights & Clips',
        url: siteUrl,
      },
      url: `${siteUrl}/author/dirza-aulia`,
      sameAs: [
        'https://github.com/dirzaaulia',
        'https://x.com/dirzaaulia',
        'https://dirzaaulia.com',
      ],
      knowsAbout: [
        'Football Tactical Analysis',
        'Opta Sports Analytics',
        'Expected Goals (xG) Modeling',
        'Zone 14 Spatial Geometry',
        'European Football Competitions',
      ],
      description:
        'Accredited tactical analyst covering European tactical structures, Opta metrics, and match dynamics across the Premier League, UEFA Champions League, and continental competitions.',
    },
  };

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8 select-none">
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(profileSchema) }}
      />

      {/* Breadcrumb Navigation */}
      <nav className="flex items-center gap-2 text-xs text-[#94A3B8]">
        <Link href="/" className="hover:text-white transition-colors">Home</Link>
        <ChevronRight className="w-3.5 h-3.5 text-[#475569]" />
        <Link href="/articles" className="hover:text-white transition-colors">Tactical Lab</Link>
        <ChevronRight className="w-3.5 h-3.5 text-[#475569]" />
        <span className="text-[#DFFF00] font-semibold">Dirza Aulia</span>
      </nav>

      {/* Author Bio Card */}
      <header className="rounded-3xl bg-[#121316] border border-white/10 p-6 sm:p-8 shadow-2xl relative overflow-hidden space-y-6">
        <div className="flex flex-col sm:flex-row items-center sm:items-start gap-6 text-center sm:text-left">
          <div className="w-24 h-24 rounded-2xl bg-gradient-to-tr from-[#1E3A8A] to-[#38BDF8] border-2 border-[#DFFF00]/50 flex items-center justify-center font-scoreboard text-3xl font-black text-white shadow-lg shrink-0">
            DA
          </div>
          <div className="space-y-2 flex-1">
            <div className="flex flex-wrap items-center justify-center sm:justify-start gap-2">
              <h1 className="font-scoreboard text-2xl sm:text-3xl text-white tracking-wide uppercase">
                Dirza Aulia
              </h1>
              <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 font-tech text-xs font-bold border border-emerald-500/30">
                <ShieldCheck className="w-3.5 h-3.5" />
                Verified Tactical Editor
              </span>
            </div>
            <p className="text-xs sm:text-sm font-tech text-[#DFFF00] font-bold uppercase tracking-wider">
              Editorial Lead &amp; Senior Football Tactician
            </p>
            <p className="text-xs sm:text-sm text-[#A1A1AA] leading-relaxed font-tech max-w-2xl">
              Specialist in European football spatial geometry, Expected Goals (xG) variance, and matchday tactical deconstruction. Directing analytical reporting across Premier League, UEFA Champions League, La Liga, and continental fixtures.
            </p>
            {/* Social Verification Channels */}
            <div className="flex flex-wrap items-center justify-center sm:justify-start gap-3 pt-2 text-xs font-tech text-white/70">
              <a
                href="https://github.com/dirzaaulia"
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-1.5 px-3 py-1 rounded-lg bg-white/5 hover:bg-white/10 border border-white/10 hover:border-[#DFFF00]/40 transition-colors"
              >
                <Code className="w-3.5 h-3.5 text-[#DFFF00]" />
                <span>GitHub</span>
              </a>
              <a
                href="https://x.com/dirzaaulia"
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-1.5 px-3 py-1 rounded-lg bg-white/5 hover:bg-white/10 border border-white/10 hover:border-[#DFFF00]/40 transition-colors"
              >
                <Globe className="w-3.5 h-3.5 text-[#38BDF8]" />
                <span>X / Twitter</span>
              </a>
              <a
                href="mailto:dirzaaulia11@gmail.com"
                className="inline-flex items-center gap-1.5 px-3 py-1 rounded-lg bg-white/5 hover:bg-white/10 border border-white/10 hover:border-[#DFFF00]/40 transition-colors"
              >
                <Mail className="w-3.5 h-3.5 text-emerald-400" />
                <span>Editorial Desk</span>
              </a>
              <Link
                href="/editorial-standards"
                className="inline-flex items-center gap-1.5 px-3 py-1 rounded-lg bg-[#DFFF00]/10 text-[#DFFF00] border border-[#DFFF00]/30 hover:bg-[#DFFF00]/20 transition-colors font-bold"
              >
                <Award className="w-3.5 h-3.5" />
                <span>Editorial Policy</span>
              </Link>
            </div>
          </div>
        </div>
      </header>

      {/* Published Works Section */}
      <section className="space-y-4">
        <div className="flex items-center justify-between pb-3 border-b border-white/10">
          <div className="flex items-center gap-2">
            <BookOpen className="w-4 h-4 text-[#DFFF00]" />
            <h2 className="font-scoreboard text-2xl text-white uppercase tracking-wider">
              Published Tactical Intelligence Dossiers ({articles.length})
            </h2>
          </div>
          <span className="text-xs text-[#DFFF00] font-tech uppercase tracking-wider font-bold">
            Author Archive
          </span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {articles.map((art) => (
            <Link
              key={art.slug}
              href={`/articles/${art.slug}`}
              className="p-5 rounded-2xl bg-[#121316] border border-white/10 hover:border-[#DFFF00]/40 transition-all flex flex-col justify-between space-y-3 group shadow-sm hover:-translate-y-0.5"
            >
              <div>
                <div className="flex items-center justify-between text-[11px] font-bold text-[#DFFF00] mb-2 font-tech">
                  <span className="px-2 py-0.5 rounded-full bg-[#DFFF00]/10 border border-[#DFFF00]/20 text-[10px] uppercase">
                    {art.category}
                  </span>
                  <span className="text-[#A1A1AA] flex items-center gap-1">
                    <Clock className="w-3 h-3" />
                    {art.readTime}
                  </span>
                </div>
                <h3 className="text-sm font-extrabold text-white group-hover:text-[#DFFF00] transition-colors leading-snug line-clamp-2">
                  {art.title}
                </h3>
                <p className="text-xs text-[#A1A1AA] line-clamp-2 mt-1.5 leading-relaxed font-tech">
                  {art.excerpt}
                </p>
              </div>
              <div className="pt-3 border-t border-white/10 flex items-center justify-between text-xs text-[#A1A1AA] font-tech">
                <span>{new Date(art.publishedAt).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })}</span>
                <span className="font-bold text-[#DFFF00] group-hover:translate-x-1 transition-transform inline-flex items-center gap-1">
                  Read Dossier <ArrowRight className="w-3.5 h-3.5" />
                </span>
              </div>
            </Link>
          ))}
        </div>
      </section>
    </div>
  );
}
