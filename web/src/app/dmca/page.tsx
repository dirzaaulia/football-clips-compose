import React from 'react';
import type { Metadata } from 'next';
import Link from 'next/link';
import {
  ShieldCheck,
  FileText,
  Mail,
  Clock,
  AlertTriangle,
  ChevronRight,
  ExternalLink,
} from 'lucide-react';

export const metadata: Metadata = {
  title: 'DMCA & Copyright Takedown Policy | Football Highlights & Clips',
  description:
    'Notice and takedown procedure under the Digital Millennium Copyright Act (17 U.S.C. § 512) for Football Highlights & Clips.',
  alternates: {
    canonical: 'https://fc.dirzaaulia.com/dmca',
  },
};

export default function DmcaPage() {
  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 py-10 text-zinc-300 space-y-10 select-none">
      {/* Breadcrumb Navigation */}
      <nav className="flex items-center gap-2 text-xs text-[#94A3B8]">
        <Link href="/" className="hover:text-white transition-colors">Home</Link>
        <ChevronRight className="w-3.5 h-3.5 text-[#475569]" />
        <span className="text-[#DFFF00] font-semibold">DMCA Policy</span>
      </nav>

      {/* Header */}
      <header className="border-b border-white/10 pb-6 space-y-2">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-tech font-bold bg-[#1E3A8A]/50 text-[#4FC3F7] border border-[#7098F8]/40 uppercase tracking-wider">
          <ShieldCheck className="w-3.5 h-3.5" />
          Intellectual Property Governance
        </div>
        <h1 className="text-3xl sm:text-4xl font-black text-white tracking-tight">
          DMCA &amp; Copyright Compliance Policy
        </h1>
        <p className="text-xs sm:text-sm text-[#94A3B8] font-tech">
          Digital Millennium Copyright Act (17 U.S.C. § 512) Notice and Takedown Framework
        </p>
      </header>

      {/* 1. Sourcing & YouTube API Integration */}
      <section className="bg-[#121316] border border-white/10 rounded-2xl p-6 sm:p-7 space-y-3 shadow-xl">
        <div className="flex items-center gap-2 text-white font-bold text-lg">
          <FileText className="w-5 h-5 text-[#DFFF00]" />
          <h2>1. Video Sourcing &amp; YouTube API Terms Compliance</h2>
        </div>
        <p className="text-xs sm:text-sm text-[#A1A1AA] leading-relaxed font-tech">
          Football Highlights &amp; Clips is an independent sports analytics and editorial portal. We do <strong>not</strong> host, stream, upload, or store video files on our proprietary servers. All video highlights displayed across our platform are integrated through the official <strong>YouTube Embedded Player API</strong> directly referencing broadcasts published by verified sports networks, tournament organizers, and official club channels.
        </p>
        <p className="text-xs sm:text-sm text-[#A1A1AA] leading-relaxed font-tech">
          All embeds fully adhere to the{' '}
          <a
            href="https://www.youtube.com/t/terms"
            target="_blank"
            rel="noopener noreferrer"
            className="text-[#DFFF00] hover:underline inline-flex items-center gap-1 font-bold"
          >
            YouTube Terms of Service <ExternalLink className="w-3 h-3" />
          </a>
          . Video rights and monetization remain 100% with the respective copyright holders.
        </p>
      </section>

      {/* 2. Notice and Takedown Procedure */}
      <section className="bg-[#121316] border border-white/10 rounded-2xl p-6 sm:p-7 space-y-4 shadow-xl">
        <div className="flex items-center gap-2 text-white font-bold text-lg">
          <AlertTriangle className="w-5 h-5 text-amber-400" />
          <h2>2. Filing a DMCA Takedown Notice</h2>
        </div>
        <p className="text-xs sm:text-sm text-[#A1A1AA] leading-relaxed font-tech">
          If you are a copyright owner or an authorized agent and believe that content embedded or linked on Football Highlights &amp; Clips infringes your copyright under 17 U.S.C. § 512(c), please submit a written notification containing:
        </p>
        <ul className="list-disc list-inside space-y-2 text-xs sm:text-sm text-[#CBD5E1] font-tech pl-2">
          <li>A physical or electronic signature of a person authorized to act on behalf of the owner of an exclusive right that is allegedly infringed.</li>
          <li>Identification of the copyrighted work claimed to have been infringed, or a representative list of such works.</li>
          <li>Identification of the material that is claimed to be infringing (including exact match URL and embedded video ID).</li>
          <li>Information reasonably sufficient to permit us to contact you, such as address, telephone number, and email.</li>
          <li>A statement that you have a good faith belief that use of the material is not authorized by the copyright owner, its agent, or the law.</li>
          <li>A statement that the information in the notification is accurate, and under penalty of perjury, that you are authorized to act.</li>
        </ul>
      </section>

      {/* 3. Designated Copyright Agent */}
      <section className="bg-[#121316] border border-white/10 rounded-2xl p-6 sm:p-7 space-y-4 shadow-xl">
        <div className="flex items-center gap-2 text-white font-bold text-lg">
          <Mail className="w-5 h-5 text-[#38BDF8]" />
          <h2>3. Designated Copyright Agent Contact</h2>
        </div>
        <p className="text-xs sm:text-sm text-[#A1A1AA] leading-relaxed font-tech">
          All DMCA notifications, licensing inquiries, and intellectual property notices must be directed to our designated agent:
        </p>
        <div className="p-4 rounded-xl bg-white/[0.03] border border-white/10 space-y-1.5 text-xs font-tech">
          <p><strong className="text-white">Designated Agent:</strong> Dirza Aulia</p>
          <p><strong className="text-white">Title:</strong> Publisher &amp; Copyright Compliance Officer</p>
          <p><strong className="text-white">Organization:</strong> Football Highlights &amp; Clips</p>
          <p>
            <strong className="text-white">Email:</strong>{' '}
            <a href="mailto:dirzaaulia11@gmail.com?subject=DMCA%20Takedown%20Notice" className="text-[#DFFF00] hover:underline font-bold">
              dirzaaulia11@gmail.com
            </a>
          </p>
        </div>
      </section>

      {/* 4. SLA & Expeditious Action */}
      <section className="bg-[#121316] border border-white/10 rounded-2xl p-6 sm:p-7 space-y-3 shadow-xl">
        <div className="flex items-center gap-2 text-white font-bold text-lg">
          <Clock className="w-5 h-5 text-emerald-400" />
          <h2>4. Expeditious Action (24-Hour SLA)</h2>
        </div>
        <p className="text-xs sm:text-sm text-[#A1A1AA] leading-relaxed font-tech">
          Upon receipt of a valid and complete DMCA notice, Football Highlights &amp; Clips will act expeditiously to remove or disable access to the infringing video link within <strong>24 to 48 hours</strong>.
        </p>
      </section>
    </div>
  );
}
