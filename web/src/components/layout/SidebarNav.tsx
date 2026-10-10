'use client';

import React from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { usePathname } from 'next/navigation';
import {
  Trophy,
  Calendar,
  Newspaper,
  Play,
  Info,
} from 'lucide-react';

interface SidebarNavProps {
  isCollapsed: boolean;
  onItemClick?: () => void;
}

const NAV_LINKS = [
  { href: '/', label: 'Highlights', icon: Play },
  { href: '/fixtures', label: 'Scores & Fixtures', icon: Calendar },
  { href: '/standings', label: 'League Standings', icon: Trophy },
  { href: '/articles', label: 'Analysis Hub', icon: Newspaper },
  { href: '/info', label: 'Info & Resources', icon: Info },
];

export default function SidebarNav({
  isCollapsed,
  onItemClick,
}: SidebarNavProps) {
  const pathname = usePathname();

  return (
    <div className="space-y-4 w-full">
      {/* Brand Header */}
      <div className={`flex items-center ${isCollapsed ? 'justify-center' : 'justify-start px-1'} w-full`}>
        <Link
          href="/"
          onClick={onItemClick}
          className="flex items-center gap-3 group min-w-0"
          title="Football Highlights & Clips"
        >
          <div className="relative w-9 h-9 rounded-2xl overflow-hidden border border-[#7098F8]/40 shadow-[0_0_16px_rgba(112,152,248,0.3)] group-hover:scale-105 transition-transform bg-[#181C20] shrink-0 flex items-center justify-center">
            <Image
              src="/app_icon.png"
              alt="Football Highlights & Clips"
              width={36}
              height={36}
              className="object-cover"
              priority
            />
          </div>
          {!isCollapsed && (
            <div className="flex flex-col min-w-0">
              <span className="font-extrabold text-sm tracking-tight text-white leading-tight truncate">
                Football Highlights
              </span>
              <span className="text-xs font-black tracking-wider uppercase text-transparent bg-clip-text bg-gradient-to-r from-[#7098F8] to-[#4FC3F7]">
                & Clips
              </span>
            </div>
          )}
        </Link>
      </div>

      {/* Navigation Links */}
      <nav className="space-y-1 pt-1 w-full">
        {NAV_LINKS.map((link) => {
          const Icon = link.icon;
          const isActive = pathname === link.href;
          return (
            <Link
              key={link.href}
              href={link.href}
              onClick={onItemClick}
              title={isCollapsed ? link.label : undefined}
              className={`flex items-center ${
                isCollapsed ? 'justify-center px-0 py-2.5' : 'gap-3 px-3 py-2'
              } rounded-2xl text-xs sm:text-sm font-bold tracking-wide transition-all ${
                isActive
                  ? 'text-white bg-gradient-to-r from-[#1E3A8A] to-[#7098F8]/80 shadow-[0_0_16px_rgba(112,152,248,0.3)] border border-[#7098F8]/40'
                  : 'text-[#94A3B8] hover:text-white hover:bg-[#1E2227] border border-transparent'
              }`}
            >
              <Icon className={`w-4 h-4 shrink-0 ${isActive ? 'text-white' : 'text-[#7098F8]'}`} />
              {!isCollapsed && <span className="truncate">{link.label}</span>}
            </Link>
          );
        })}
      </nav>
    </div>
  );
}
