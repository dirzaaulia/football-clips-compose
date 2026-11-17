'use client';

import React from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { usePathname } from 'next/navigation';
import { EyeOff, Eye, Sparkles, User } from 'lucide-react';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';
import { useSeason } from '@/hooks/useSeason';

interface TopHeaderNavProps {
  onOpenPaywall?: () => void;
  isPro?: boolean;
}

const NAV_LINKS = [
  { href: '/', label: 'Highlights' },
  { href: '/shorts', label: 'Shorts' },
  { href: '/fixtures', label: 'Fixtures' },
  { href: '/standings', label: 'Standings & Stats' },
  { href: '/articles', label: 'Analysis' },
  { href: '/tools', label: 'Tools' },
  { href: '/store', label: 'Store' },
  { href: '/settings', label: 'Settings' },
];

export default function TopHeaderNav({
  onOpenPaywall,
  isPro = false,
}: TopHeaderNavProps) {
  const pathname = usePathname();
  const { isSpoilerFree, toggleSpoilerFree } = useSpoilerFree();
  const { selectedSeason } = useSeason();

  return (
    <header className="fixed top-0 left-0 right-0 z-50 h-12 bg-[#121316]/75 backdrop-blur-md border-b border-white/10 shadow-[0_1px_8px_rgba(0,0,0,0.3)] select-none">
      <div className="h-full max-w-[1800px] mx-auto px-4 sm:px-6 lg:px-8 flex items-center justify-between gap-4">
        {/* Left: Brand Identity */}
        <div className="flex items-center gap-6 shrink-0">
          <Link href="/" className="flex items-center gap-2 group tracking-wider">
            <div className="relative w-6 h-6 shrink-0">
              <Image
                src="/favicon.png"
                alt="FootballClips"
                fill
                className="object-contain"
                unoptimized
              />
            </div>
            <span className="font-scoreboard text-xl tracking-wider uppercase text-white">
              FOOTBALL<span className="text-[#DFFF00]">CLIPS</span>
            </span>
          </Link>
        </div>

        {/* Center: Main OTT Navigation Links */}
        <nav className="hidden md:flex items-center gap-6 font-tech text-xs uppercase tracking-wider h-full">
          {NAV_LINKS.map((link) => {
            const isActive =
              link.href === '/'
                ? pathname === '/'
                : pathname.startsWith(link.href);
            return (
              <Link
                key={link.href}
                href={link.href}
                className={`h-full flex items-center transition-colors pt-0.5 ${
                  isActive
                    ? 'text-white font-bold border-b-2 border-[#DFFF00]'
                    : 'text-white/60 hover:text-white'
                }`}
              >
                {link.label}
              </Link>
            );
          })}
        </nav>

        {/* Right: Utility Controls & Profile */}
        <div className="flex items-center gap-2.5 shrink-0">
          {/* Season Badge */}
          <span className="hidden sm:inline-flex items-center text-[10px] font-tech font-bold px-2 py-0.5 rounded bg-white/5 text-white/80 border border-white/10 tracking-wider">
            {selectedSeason - 1}/{String(selectedSeason).slice(-2)}
          </span>

          {/* Spoiler Guard Toggle */}
          <button
            onClick={toggleSpoilerFree}
            className={`relative p-1.5 rounded-lg transition-colors flex items-center justify-center cursor-pointer ${
              isSpoilerFree
                ? 'text-[#DFFF00] bg-[#DFFF00]/10 border border-[#DFFF00]/30'
                : 'text-white/60 hover:text-white hover:bg-white/5'
            }`}
            title={isSpoilerFree ? 'Spoilers Hidden' : 'Spoilers Visible'}
          >
            {isSpoilerFree ? (
              <EyeOff className="w-4 h-4" />
            ) : (
              <Eye className="w-4 h-4" />
            )}
            {isSpoilerFree && (
              <span className="absolute top-1 right-1 w-1.5 h-1.5 rounded-full bg-[#DFFF00] shadow-[0_0_6px_#DFFF00]" />
            )}
          </button>

          {/* PRO Button */}
          <button
            onClick={onOpenPaywall}
            className="inline-flex items-center gap-1 px-2.5 py-1 rounded border border-[#DFFF00]/40 bg-[#DFFF00]/10 hover:bg-[#DFFF00] text-[#DFFF00] hover:text-[#0A0A0C] font-tech text-[11px] font-bold tracking-wider uppercase transition-all shadow-[0_0_10px_rgba(223,255,0,0.15)] cursor-pointer"
          >
            <Sparkles className="w-3 h-3" />
            <span>{isPro ? 'PRO ACTIVE' : 'PRO'}</span>
          </button>

          {/* User Avatar */}
          <Link
            href="/settings"
            className="w-7 h-7 rounded-full bg-[#1A1C22] border border-[#DFFF00]/50 text-[#DFFF00] font-tech text-[10px] font-bold flex items-center justify-center shadow-sm select-none hover:scale-105 transition-transform"
            title="User Profile & Settings"
          >
            <User className="w-3.5 h-3.5" />
          </Link>
        </div>
      </div>
    </header>
  );
}
