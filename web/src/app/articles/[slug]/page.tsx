import React from 'react';
import type { Metadata } from 'next';
import { notFound } from 'next/navigation';
import Link from 'next/link';
import Image from 'next/image';
import { Clock, Calendar, ArrowLeft, Share2, Sparkles, BookOpen, CheckCircle, ChevronRight } from 'lucide-react';
import AdSenseBanner from '@/components/ads/AdSenseBanner';
import { getArticleBySlug, getAllArticles, EditorialArticle } from '@/lib/editorial-articles';

interface ArticlePageProps {
  params: Promise<{ slug: string }>;
}

export async function generateStaticParams() {
  const articles = getAllArticles();
  return articles.map((article) => ({
    slug: article.slug,
  }));
}

export async function generateMetadata({ params }: ArticlePageProps): Promise<Metadata> {
  const { slug } = await params;
  const article = getArticleBySlug(slug);

  if (!article) {
    return {
      title: 'Article Not Found | FootballClips',
      description: 'The requested tactical article could not be located.',
    };
  }

  const siteUrl = 'https://fc.dirzaaulia.com';
  const canonicalUrl = `${siteUrl}/articles/${article.slug}`;

  return {
    title: `${article.title} | FootballClips Editorial`,
    description: article.excerpt,
    keywords: article.tags,
    authors: [{ name: article.author.name }],
    alternates: {
      canonical: canonicalUrl,
    },
    openGraph: {
      title: article.title,
      description: article.excerpt,
      url: canonicalUrl,
      siteName: 'Football Highlights & Clips',
      type: 'article',
      publishedTime: article.publishedAt,
      modifiedTime: article.updatedAt,
      authors: [article.author.name],
      tags: article.tags,
      images: [
        {
          url: article.heroImage,
          width: 1200,
          height: 630,
          alt: article.title,
        },
      ],
    },
    twitter: {
      card: 'summary_large_image',
      title: article.title,
      description: article.excerpt,
      images: [article.heroImage],
      creator: '@dirzaaulia',
    },
  };
}

export default async function ArticleDetailPage({ params }: ArticlePageProps) {
  const { slug } = await params;
  const article = getArticleBySlug(slug);

  if (!article) {
    notFound();
  }

  const siteUrl = 'https://fc.dirzaaulia.com';
  const articleUrl = `${siteUrl}/articles/${article.slug}`;

  const jsonLd = {
    '@context': 'https://schema.org',
    '@type': 'NewsArticle',
    mainEntityOfPage: {
      '@type': 'WebPage',
      '@id': articleUrl,
    },
    headline: article.title,
    description: article.excerpt,
    image: [article.heroImage],
    datePublished: article.publishedAt,
    dateModified: article.updatedAt,
    author: {
      '@type': 'Person',
      name: article.author.name,
      jobTitle: article.author.role,
      url: `${siteUrl}/editorial-standards`,
    },
    publisher: {
      '@type': 'Organization',
      name: 'Football Highlights & Clips',
      logo: {
        '@type': 'ImageObject',
        url: `${siteUrl}/favicon.png`,
      },
    },
    articleSection: article.category,
    keywords: article.tags.join(', '),
  };

  const formattedDate = new Date(article.publishedAt).toLocaleDateString('en-GB', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  });

  return (
    <article className="max-w-4xl mx-auto px-4 sm:px-6 py-8">
      {/* Schema.org Structured Data */}
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
      />

      {/* Breadcrumb Navigation */}
      <nav className="flex items-center gap-2 text-xs text-[#94A3B8] mb-6">
        <Link href="/" className="hover:text-white transition-colors">Home</Link>
        <ChevronRight className="w-3.5 h-3.5 text-[#475569]" />
        <Link href="/articles" className="hover:text-white transition-colors">Intelligence Hub</Link>
        <ChevronRight className="w-3.5 h-3.5 text-[#475569]" />
        <span className="text-[#4FC3F7] font-semibold truncate max-w-[200px] sm:max-w-none">{article.category}</span>
      </nav>

      {/* Article Header */}
      <header className="space-y-4 pb-6 border-b border-[#334155]/60 mb-8">
        <div className="flex flex-wrap items-center gap-2">
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-[#1E3A8A]/50 text-[#4FC3F7] border border-[#7098F8]/40">
            <Sparkles className="w-3.5 h-3.5" />
            {article.category}
          </span>
          <span className="text-xs text-[#94A3B8] flex items-center gap-1">
            <Clock className="w-3.5 h-3.5" />
            {article.readTime}
          </span>
          <span className="text-xs text-[#94A3B8] flex items-center gap-1">
            <Calendar className="w-3.5 h-3.5" />
            {formattedDate}
          </span>
        </div>

        <h1 className="text-2xl sm:text-4xl font-black text-white tracking-tight leading-tight">
          {article.title}
        </h1>

        <p className="text-sm sm:text-base text-[#94A3B8] leading-relaxed">
          {article.subtitle}
        </p>

        {/* Author Byline */}
        <div className="flex items-center justify-between pt-4">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-[#1E3A8A] border border-[#7098F8]/50 flex items-center justify-center font-bold text-white text-sm">
              DA
            </div>
            <div>
              <div className="text-sm font-bold text-white flex items-center gap-1.5">
                {article.author.name}
                <span className="text-[10px] px-1.5 py-0.5 rounded bg-emerald-500/20 text-emerald-400 font-bold border border-emerald-500/30">
                  Verified Editor
                </span>
              </div>
              <div className="text-xs text-[#94A3B8]">{article.author.role}</div>
            </div>
          </div>

          <Link
            href="/editorial-standards"
            className="text-xs text-[#7098F8] hover:underline hidden sm:inline-block font-semibold"
          >
            Editorial Policy &amp; Standards
          </Link>
        </div>
      </header>

      {/* AdSense Top Banner */}
      <div className="my-6">
        <AdSenseBanner slotId="5616816386" format="horizontal" />
      </div>

      {/* Key Takeaways Callout Box */}
      <div className="my-8 p-5 sm:p-6 rounded-2xl bg-[#181C20] border border-[#334155] shadow-xl">
        <div className="flex items-center gap-2 mb-3">
          <BookOpen className="w-4 h-4 text-[#7098F8]" />
          <h2 className="text-sm font-black text-white uppercase tracking-wider">
            Executive Tactical Takeaways
          </h2>
        </div>
        <ul className="space-y-2 text-xs sm:text-sm text-[#94A3B8]">
          {article.keyTacticalTakeaways.map((takeaway, idx) => (
            <li key={idx} className="flex items-start gap-2.5">
              <CheckCircle className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
              <span>{takeaway}</span>
            </li>
          ))}
        </ul>
      </div>

      {/* Main Prose Content */}
      <div className="space-y-6 text-sm sm:text-base text-[#CBD5E1] leading-relaxed font-sans">
        {article.content.map((paragraph, index) => (
          <p key={index} className="tracking-wide">
            {paragraph}
          </p>
        ))}
      </div>

      {/* Tags */}
      <div className="mt-10 pt-6 border-t border-[#334155]/60">
        <div className="flex flex-wrap items-center gap-2">
          <span className="text-xs font-bold text-[#94A3B8]">Tags:</span>
          {article.tags.map((tag) => (
            <span
              key={tag}
              className="text-xs px-2.5 py-1 rounded-lg bg-[#1E293B] text-[#94A3B8] border border-[#334155]"
            >
              #{tag}
            </span>
          ))}
        </div>
      </div>

      {/* AdSense Bottom Banner */}
      <div className="my-8">
        <AdSenseBanner slotId="5616816386" format="horizontal" />
      </div>

      {/* Author Bio Card */}
      <div className="my-8 p-6 rounded-2xl bg-[#181C20] border border-[#334155]/80 flex flex-col sm:flex-row items-center sm:items-start gap-4 text-center sm:text-left">
        <div className="w-14 h-14 rounded-full bg-[#1E3A8A] border-2 border-[#7098F8] flex items-center justify-center font-bold text-white text-lg shrink-0">
          DA
        </div>
        <div className="space-y-1">
          <div className="text-sm font-bold text-white">{article.author.name}</div>
          <div className="text-xs text-[#4FC3F7]">{article.author.role}</div>
          <p className="text-xs text-[#94A3B8] leading-relaxed pt-1">
            {article.author.bio} FootballClips maintains rigorous original reporting standards, utilizing Opta-aligned data feeds and verified video highlight integrations.
          </p>
        </div>
      </div>

      {/* Navigation Footer */}
      <div className="mt-8 flex justify-between items-center">
        <Link
          href="/articles"
          className="inline-flex items-center gap-2 text-xs font-bold text-[#7098F8] hover:text-[#93C5FD] transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          Back to Intelligence Hub
        </Link>
        <Link
          href="/fixtures"
          className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-[#1E3A8A] hover:bg-[#2563EB] text-white text-xs font-bold transition-all shadow-md"
        >
          Explore Live Matches &amp; Fixtures
        </Link>
      </div>
    </article>
  );
}
