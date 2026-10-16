'use client';

import React from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { ShieldCheck, Video, Database, Smartphone } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="mt-12 border-t border-[#334155]/60 bg-[#101418] text-[#94A3B8]">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 py-8 space-y-6">
        {/* Brand & Editorial Standards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 text-xs">
          {/* Col 1: Brand */}
          <div className="space-y-3">
            <div className="flex items-center gap-2.5 h-7">
              <div className="relative w-7 h-7 rounded-xl overflow-hidden border border-[#7098F8]/40 bg-[#181C20] shrink-0">
                <Image
                  src="/app_icon.png"
                  alt="Football Highlights & Clips"
                  width={28}
                  height={28}
                  className="object-cover"
                />
              </div>
              <span className="font-black text-white text-sm tracking-tight">
                Football <span className="text-[#7098F8]">Highlights & Clips</span>
              </span>
            </div>
            <p className="text-xs leading-relaxed text-[#94A3B8]">
              Verified official European match highlights, AI tactical reports, live schedules, and league tables. Powered by the Kinetic Editorial design system.
            </p>
            <div className="pt-1">
              <a
                href="https://play.google.com/store/apps/details?id=com.dirzaaulia.footballclips"
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-2 px-3 py-1.5 rounded-xl bg-[#181C20] hover:bg-[#1E2227] border border-[#7098F8]/40 hover:border-[#7098F8] text-white font-bold text-[11px] transition-all cursor-pointer shadow-sm"
              >
                <Smartphone className="w-3.5 h-3.5 text-[#7098F8]" />
                <span>Get App on Google Play</span>
              </a>
            </div>
          </div>

          {/* Col 2: Compliance & Technology */}
          <div className="space-y-3">
            <div className="flex items-center h-7">
              <h3 className="font-bold uppercase tracking-wider text-white text-[11px]">
                Ecosystem & Compliance
              </h3>
            </div>
            <ul className="space-y-2 text-xs">
              <li className="flex items-center gap-2">
                <Video className="w-3.5 h-3.5 text-[#7098F8] shrink-0" />
                <span>YouTube iFrame Player API (TOS Compliant)</span>
              </li>
              <li className="flex items-center gap-2">
                <Database className="w-3.5 h-3.5 text-[#4FC3F7] shrink-0" />
                <span>Football-Data.org Real-time Sports API</span>
              </li>
              <li className="flex items-center gap-2">
                <ShieldCheck className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
                <span>Paddle & RevenueCat Verified Web Billing</span>
              </li>
            </ul>
          </div>

          {/* Col 3: Legal & Trust Index */}
          <div className="space-y-3">
            <div className="flex items-center h-7">
              <h3 className="font-bold uppercase tracking-wider text-white text-[11px]">
                Resources & Legal
              </h3>
            </div>
            <div className="flex flex-col space-y-2 text-xs">
              <Link href="/articles" className="text-[#4FC3F7] font-bold hover:underline">Analysis Hub &amp; News</Link>
              <Link href="/info" className="text-[#7098F8] font-bold hover:underline">Info, Data Sources &amp; Support</Link>
              <Link href="/privacy" className="hover:text-white transition-colors">Privacy Policy &amp; Cookies</Link>
              <Link href="/terms" className="hover:text-white transition-colors">Terms of Service</Link>
              <Link href="/contact" className="hover:text-white transition-colors">Contact Editorial Desk</Link>
            </div>
          </div>
        </div>

        {/* Bottom Micro Copyright Bar */}
        <div className="pt-5 border-t border-[#334155]/40 flex flex-col sm:flex-row items-center justify-between gap-3 text-[11px] text-[#94A3B8]">
          <p>© 2026 Football Highlights & Clips. All rights reserved.</p>
          <div className="flex items-center gap-4">
            <Link href="/info" className="hover:text-white">Knowledge Hub</Link>
            <span className="text-[#334155]">&bull;</span>
            <Link href="/privacy" className="hover:text-white">Privacy</Link>
            <span className="text-[#334155]">&bull;</span>
            <Link href="/terms" className="hover:text-white">Terms</Link>
          </div>
        </div>
      </div>
    </footer>
  );
}
