import React from 'react';
import { Shield, Sparkles, Trophy, User, CheckCircle, Database } from 'lucide-react';
import Link from 'next/link';

export const metadata = {
  title: 'About Us & Editorial Standards - Football Highlights & Clips',
  description: 'Learn about Football Highlights & Clips (FC), our publisher identity, editorial methodology, sourcing principles, and commitment to verified sports journalism.',
};

export default function AboutPage() {
  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 py-10 text-zinc-300 space-y-10">
      {/* Header */}
      <header className="border-b border-[#334155]/60 pb-6 space-y-2">
        <span className="inline-block px-2.5 py-1 rounded-full text-xs font-semibold bg-[#1E3A8A]/50 text-[#4FC3F7] border border-[#7098F8]/40">
          Editorial Masthead &amp; Standards
        </span>
        <h1 className="text-3xl sm:text-4xl font-black text-white tracking-tight">
          About Football Highlights &amp; Clips
        </h1>
        <p className="text-xs sm:text-sm text-[#94A3B8]">
          Our Mission, Editorial Methodology, Sourcing Integrity, and E-E-A-T Commitment
        </p>
      </header>

      {/* 1. Publisher Identity & Masthead */}
      <section className="space-y-4">
        <div className="flex items-center gap-2 pb-2 border-b border-[#334155]/40 text-white">
          <User className="w-5 h-5 text-[#7098F8]" />
          <h2 className="text-xl font-bold">Publisher Identity &amp; Editorial Leadership</h2>
        </div>
        <p className="text-sm leading-relaxed">
          <strong className="text-white">Football Highlights &amp; Clips</strong> (also recognized as{' '}
          <strong className="text-white">FC</strong>, online at{' '}
          <a href="https://fc.dirzaaulia.com" className="text-[#7098F8] hover:underline">
            fc.dirzaaulia.com
          </a>
          ) is an independent digital sports media publication created and published by{' '}
          <strong className="text-white">Dirza Aulia</strong>.
        </p>
        <p className="text-sm leading-relaxed">
          The publication delivers real-time football fixture telemetry, authenticated European match highlights,
          and tactical match debriefs designed for passionate supporters across mobile and web platforms.
        </p>
      </section>

      {/* 2. Mission & Problem Statement */}
      <section className="space-y-4">
        <div className="flex items-center gap-2 pb-2 border-b border-[#334155]/40 text-white">
          <Trophy className="w-5 h-5 text-[#4FC3F7]" />
          <h2 className="text-xl font-bold">Our Mission</h2>
        </div>
        <p className="text-sm leading-relaxed">
          Modern football fans face an endless maze of clickbait links, spammy piracy streams, and deceptive clip channels.
          Our mission is to establish a singular, distraction-free home that catalogs genuine, official matchday broadcasts
          from verified rights-holders across the Premier League, UEFA Champions League, La Liga, Serie A, Bundesliga, and Ligue 1.
        </p>
      </section>

      {/* 3. Sourcing Integrity & Copyright Compliance */}
      <section className="space-y-4">
        <div className="flex items-center gap-2 pb-2 border-b border-[#334155]/40 text-white">
          <Shield className="w-5 h-5 text-emerald-400" />
          <h2 className="text-xl font-bold">Sourcing Integrity &amp; Strict Legality</h2>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs leading-relaxed">
          <div className="p-4 rounded-2xl bg-[#181C20] border border-[#334155] space-y-2">
            <h3 className="font-bold text-white text-sm flex items-center gap-1.5">
              <CheckCircle className="w-4 h-4 text-emerald-400" />
              <span>YouTube Developer API Compliance</span>
            </h3>
            <p className="text-[#94A3B8]">
              We do not host or distribute raw video media. Every highlight video is embedded strictly via the official YouTube
              iFrame Player API from verified broadcaster and club channels. All copyright rights, views, and ad revenue flow
              directly to original copyright owners.
            </p>
          </div>

          <div className="p-4 rounded-2xl bg-[#181C20] border border-[#334155] space-y-2">
            <h3 className="font-bold text-white text-sm flex items-center gap-1.5">
              <Database className="w-4 h-4 text-[#4FC3F7]" />
              <span>Real-Time Sports Telemetry</span>
            </h3>
            <p className="text-[#94A3B8]">
              Live match scores, schedules, and standings tables are ingested through verified sports data providers including{' '}
              <a href="https://www.football-data.org" target="_blank" rel="noopener noreferrer" className="text-[#7098F8] hover:underline">
                Football-Data.org
              </a>
              , ensuring factual scoreline accuracy and prompt match updates.
            </p>
          </div>
        </div>
      </section>

      {/* 4. Editorial Standards & AI Methodology */}
      <section className="space-y-4">
        <div className="flex items-center gap-2 pb-2 border-b border-[#334155]/40 text-white">
          <Sparkles className="w-5 h-5 text-[#7098F8]" />
          <h2 className="text-xl font-bold">Tactical Debriefs &amp; Journalism Standards</h2>
        </div>
        <p className="text-sm leading-relaxed">
          Beyond visual highlights, we believe supporters deserve meaningful tactical understanding. Our match analysis
          combines expected goals (xG) frameworks, pressing structure breakdowns, and substitution impact analysis, synthesized
          through structured AI assistance and verified against live journalism feeds from BBC Sport and Sky Sports.
        </p>
        <p className="text-sm leading-relaxed">
          We maintain strict factual integrity: tactical reports reflect verified match outcomes and real tactical momentum
          rather than speculative commentary.
        </p>
      </section>

      {/* 5. Commercial Transparency & Contact */}
      <section className="space-y-3 border-t border-[#334155]/60 pt-6 text-xs text-[#94A3B8]">
        <h3 className="font-bold text-white uppercase tracking-wider text-[11px]">
          Commercial Transparency
        </h3>
        <p className="leading-relaxed">
          Football Highlights &amp; Clips is funded through Google AdSense advertising on web and Google AdMob on mobile devices.
          We offer an optional cross-platform digital purchase to remove advertisements. Advertisers have zero influence on
          our factual reporting or match cataloging.
        </p>
        <div className="pt-2 flex items-center gap-4 text-xs font-semibold">
          <Link href="/contact" className="text-[#7098F8] hover:underline">
            Contact Editorial Desk &rarr;
          </Link>
          <Link href="/privacy" className="hover:text-white transition-colors">
            Privacy Policy
          </Link>
          <Link href="/terms" className="hover:text-white transition-colors">
            Terms of Service
          </Link>
        </div>
      </section>
    </div>
  );
}
