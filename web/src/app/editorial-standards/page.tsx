import React from 'react';
import type { Metadata } from 'next';
import Link from 'next/link';
import { ShieldCheck, FileCheck, Video, HelpCircle, Mail, ArrowRight } from 'lucide-react';
import AdSenseBanner from '@/components/ads/AdSenseBanner';

export const metadata: Metadata = {
  title: 'Editorial Standards & Verification Policy - Football Highlights & Clips',
  description:
    'Our commitment to authentic sports journalism, tactical analysis accuracy, official broadcaster video licensing, and factual verification.',
  alternates: {
    canonical: 'https://fc.dirzaaulia.com/editorial-standards',
  },
};

export default function EditorialStandardsPage() {
  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 py-10 text-zinc-300 space-y-10">
      <header className="border-b border-[#334155]/60 pb-6 space-y-2">
        <span className="inline-block px-2.5 py-1 rounded-full text-xs font-semibold bg-[#1E3A8A]/50 text-[#4FC3F7] border border-[#7098F8]/40">
          Trust &amp; Transparency
        </span>
        <h1 className="text-3xl sm:text-4xl font-black text-white tracking-tight">
          Editorial Standards &amp; Verification Policy
        </h1>
        <p className="text-xs sm:text-sm text-[#94A3B8]">
          Delivering authentic tactical analysis, verified match data, and legally compliant official highlights.
        </p>
      </header>

      <AdSenseBanner slotId="5616816386" format="horizontal" />

      {/* Pillar 1: Factual Accuracy & Match Data Verification */}
      <section className="bg-[#181C20] border border-[#334155] rounded-3xl p-6 sm:p-8 space-y-4 shadow-xl">
        <div className="flex items-center gap-3 text-[#7098F8]">
          <FileCheck className="w-6 h-6 text-[#4FC3F7]" />
          <h2 className="text-lg font-bold text-white">1. Match Data &amp; Score Verification</h2>
        </div>
        <p className="text-xs sm:text-sm text-[#94A3B8] leading-relaxed">
          Every score, timeline event, goalscorer, substitution, and disciplinary record published across FootballClips is corroborated against official league match sheets provided by accredited international sports data providers (Football-Data.org and ESPN FC). We do not publish speculative match data or unconfirmed reports.
        </p>
        <p className="text-xs sm:text-sm text-[#94A3B8] leading-relaxed">
          During live fixtures, scores and timeline milestones update continuously via automated synchronizers with built-in regression guards to preserve historical immutability and record integrity.
        </p>
      </section>

      {/* Pillar 2: Tactical Analysis & Editorial Integrity */}
      <section className="bg-[#181C20] border border-[#334155] rounded-3xl p-6 sm:p-8 space-y-4 shadow-xl">
        <div className="flex items-center gap-3 text-[#7098F8]">
          <ShieldCheck className="w-6 h-6 text-emerald-400" />
          <h2 className="text-lg font-bold text-white">2. Tactical Analysis Methodology</h2>
        </div>
        <p className="text-xs sm:text-sm text-[#94A3B8] leading-relaxed">
          Our match reports and tactical breakdowns are authored and synthesized by the FootballClips Tactical Editorial Desk. We prioritize positional play analysis, pressing structures, spatial half-space overloads, and tactical substitutions rather than generic summaries.
        </p>
        <p className="text-xs sm:text-sm text-[#94A3B8] leading-relaxed">
          Each tactical report is tailored to the specific match events, tactical formations, and managerial decisions of the fixture to ensure every Match Centre page delivers substantial, unique value to football tacticians and enthusiasts.
        </p>
      </section>

      {/* Pillar 3: Official Broadcaster Video Licensing */}
      <section className="bg-[#181C20] border border-[#334155] rounded-3xl p-6 sm:p-8 space-y-4 shadow-xl">
        <div className="flex items-center gap-3 text-[#7098F8]">
          <Video className="w-6 h-6 text-[#7098F8]" />
          <h2 className="text-lg font-bold text-white">3. Official Rights &amp; Video Embed Licensing</h2>
        </div>
        <p className="text-xs sm:text-sm text-[#94A3B8] leading-relaxed">
          FootballClips is committed to upholding intellectual property rights and anti-piracy standards. We do not host, store, or stream pirated video files on our infrastructure.
        </p>
        <p className="text-xs sm:text-sm text-[#94A3B8] leading-relaxed">
          All highlight videos and decisive goal replays featured across our web and mobile applications are embedded strictly from verified, official club channels (such as Real Madrid, FC Barcelona, Arsenal, Manchester City, Juventus, Bayern Munich) and rights-holding league broadcasters through the official Google YouTube IFrame Embed API in full compliance with YouTube Developer Terms of Service.
        </p>
      </section>

      {/* Pillar 4: Corrections & DMCA Compliance */}
      <section className="bg-[#181C20] border border-[#334155] rounded-3xl p-6 sm:p-8 space-y-4 shadow-xl">
        <div className="flex items-center gap-3 text-[#7098F8]">
          <HelpCircle className="w-6 h-6 text-amber-400" />
          <h2 className="text-lg font-bold text-white">4. Corrections, Updates &amp; DMCA Compliance</h2>
        </div>
        <p className="text-xs sm:text-sm text-[#94A3B8] leading-relaxed">
          When an error is discovered in a match report, score, or lineup, we promptly update the record. If an official club video is taken down or geo-restricted by the broadcaster, our automated telemetry system replaces it with an alternative verified highlight.
        </p>
        <div className="pt-2 flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-t border-[#334155]/60">
          <div>
            <span className="text-xs text-white font-bold block">Editorial Feedback &amp; DMCA Agent</span>
            <a href="mailto:dirzaaulia11@gmail.com" className="text-xs text-[#7098F8] hover:underline font-mono">
              dirzaaulia11@gmail.com
            </a>
          </div>
          <Link
            href="/contact"
            className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-[#1E3A8A] hover:bg-[#2563EB] text-white text-xs font-bold transition-all shadow-md"
          >
            <span>Contact Editorial Desk</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>
      </section>
    </div>
  );
}
