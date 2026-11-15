'use client';

import React from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { ShieldCheck, Video, Database, Smartphone } from 'lucide-react';
import GooglePlayBadge from '@/components/common/GooglePlayBadge';

export default function Footer() {
  return (
    <footer className="mt-16 border-t border-white/10 bg-[#0A0A0C] text-white/60 select-none">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
        {/* Brand & Editorial Standards Grid */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8 text-xs">
          {/* Col 1: Brand & App Download */}
          <div className="space-y-4 md:col-span-2">
            <div className="flex items-center gap-3">
              <div className="relative w-8 h-8 rounded-xl overflow-hidden border border-[#DFFF00]/40 bg-[#121316] shadow-sm shrink-0">
                <Image
                  src="/app_icon.png"
                  alt="Football Highlights & Clips"
                  width={32}
                  height={32}
                  className="object-cover"
                />
              </div>
              <span className="font-scoreboard text-base text-white tracking-wider uppercase">
                Football <span className="text-[#DFFF00]">Highlights & Clips</span>
              </span>
            </div>
            <p className="text-xs leading-relaxed text-[#A1A1AA] max-w-md font-tech">
              Official verified match highlights, AI-powered tactical debriefs, live scores, and head-to-head comparators across Premier League, UEFA Champions League, LaLiga, Serie A, and Bundesliga.
            </p>
            <div className="pt-2 flex items-center gap-3">
              <GooglePlayBadge height={38} />
            </div>
          </div>

          {/* Col 2: Ecosystem & Live Standards */}
          <div className="space-y-3">
            <h3 className="font-scoreboard text-sm uppercase tracking-wider text-white">
              Ecosystem
            </h3>
            <ul className="space-y-2.5 text-xs font-tech">
              <li className="flex items-center gap-2 text-white/80">
                <Video className="w-3.5 h-3.5 text-[#DFFF00] shrink-0" />
                <span>YouTube iFrame API (TOS Compliant)</span>
              </li>
              <li className="flex items-center gap-2 text-white/80">
                <Database className="w-3.5 h-3.5 text-[#38BDF8] shrink-0" />
                <span>ESPN FC & Live Sports Telemetry</span>
              </li>
              <li className="flex items-center gap-2 text-white/80">
                <ShieldCheck className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
                <span>Paddle & RevenueCat Billing</span>
              </li>
            </ul>
          </div>

          {/* Col 3: Resources & Trust */}
          <div className="space-y-3">
            <h3 className="font-scoreboard text-sm uppercase tracking-wider text-white">
              Navigation
            </h3>
            <div className="flex flex-col space-y-2 text-xs font-tech">
              <Link href="/fixtures" className="text-white hover:text-[#DFFF00] transition-colors">
                Live Fixtures & Scores
              </Link>
              <Link href="/articles" className="text-white hover:text-[#DFFF00] transition-colors">
                Tactical Analysis Hub
              </Link>
              <Link href="/tools" className="text-white hover:text-[#DFFF00] transition-colors">
                Interactive H2H Pitch Radar
              </Link>
              <Link href="/about" className="text-white hover:text-[#DFFF00] transition-colors">
                About Us & Masthead
              </Link>
              <Link href="/editorial-standards" className="text-white hover:text-[#DFFF00] transition-colors">
                Editorial Standards
              </Link>
            </div>
          </div>
        </div>

        {/* Bottom Bar: Copyright & Privacy */}
        <div className="pt-6 border-t border-white/10 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-[#A1A1AA] font-tech">
          <p>© 2026 Football Highlights & Clips. All rights reserved.</p>
          <div className="flex items-center gap-4">
            <Link href="/info" className="hover:text-white transition-colors">Support Hub</Link>
            <span>•</span>
            <Link href="/privacy" className="hover:text-white transition-colors">Privacy Policy</Link>
            <span>•</span>
            <Link href="/terms" className="hover:text-white transition-colors">Terms of Service</Link>
          </div>
        </div>
      </div>
    </footer>
  );
}
