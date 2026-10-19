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
  PanelLeftClose,
  PanelLeftOpen,
} from 'lucide-react';

interface SidebarNavProps {
  isCollapsed?: boolean;
  onItemClick?: () => void;
  onToggleCollapse?: () => void;
}

const NAV_LINKS = [
  { href: '/', label: 'Highlights', icon: Play },
  { href: '/fixtures', label: 'Scores & Fixtures', icon: Calendar },
  { href: '/standings', label: 'League Standings', icon: Trophy },
  { href: '/articles', label: 'Analysis Hub', icon: Newspaper },
  { href: '/info', label: 'Info & Resources', icon: Info },
];

export default function SidebarNav({
  isCollapsed = false,
  onItemClick,
  onToggleCollapse,
}: SidebarNavProps) {
  const pathname = usePathname();

  return (
    <div className="space-y-2.5 w-full">
      {/* Brand Header */}
      {isCollapsed ? (
        <div className="flex flex-col items-center gap-2.5 w-full">
          <Link
            href="/"
            onClick={onItemClick}
            className="relative w-9 h-9 rounded-xl overflow-hidden border border-[#7098F8]/40 shadow-[0_0_12px_rgba(112,152,248,0.25)] hover:scale-105 transition-transform bg-[#181C20] shrink-0 flex items-center justify-center cursor-pointer"
            title="Football Highlights & Clips"
          >
            <Image
              src="/app_icon.png"
              alt="Football Highlights & Clips"
              width={36}
              height={36}
              className="object-cover"
              priority
            />
          </Link>
          {onToggleCollapse && (
            <button
              onClick={onToggleCollapse}
              title="Expand sidebar"
              aria-label="Expand sidebar"
              className="w-8 h-8 rounded-lg flex items-center justify-center text-[#94A3B8] hover:text-white bg-[#181C20] hover:bg-[#1E2227] border border-[#334155] hover:border-[#7098F8]/50 shadow-sm transition-all cursor-pointer group"
            >
              <PanelLeftOpen className="w-4 h-4 text-[#7098F8] group-hover:scale-110 transition-transform" />
            </button>
          )}
        </div>
      ) : (
        <div className="flex items-center justify-between px-0.5 w-full">
          <Link
            href="/"
            onClick={onItemClick}
            className="flex items-center gap-2.5 group min-w-0"
            title="Football Highlights & Clips"
          >
            <div className="relative w-8 h-8 rounded-xl overflow-hidden border border-[#7098F8]/40 shadow-[0_0_12px_rgba(112,152,248,0.25)] group-hover:scale-105 transition-transform bg-[#181C20] shrink-0 flex items-center justify-center">
              <Image
                src="/app_icon.png"
                alt="Football Highlights & Clips"
                width={32}
                height={32}
                className="object-cover"
                priority
              />
            </div>
            <div className="flex flex-col min-w-0">
              <span className="font-extrabold text-xs tracking-tight text-white leading-tight truncate">
                Football Highlights
              </span>
              <span className="text-[10px] font-black tracking-wider uppercase text-transparent bg-clip-text bg-gradient-to-r from-[#7098F8] to-[#4FC3F7]">
                & Clips
              </span>
            </div>
          </Link>
          {onToggleCollapse && (
            <button
              onClick={onToggleCollapse}
              title="Collapse sidebar"
              aria-label="Collapse sidebar"
              className="p-1.5 rounded-lg text-[#94A3B8] hover:text-white hover:bg-[#1E2227] transition-colors cursor-pointer"
            >
              <PanelLeftClose className="w-4 h-4" />
            </button>
          )}
        </div>
      )}

      {/* Navigation Links */}
      <nav className="space-y-1 w-full">
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
                isCollapsed
                  ? 'justify-center w-10 h-10 mx-auto rounded-xl'
                  : 'gap-2.5 px-3 py-2 rounded-xl'
              } text-xs font-bold tracking-wide transition-all ${
                isActive
                  ? 'text-white bg-gradient-to-r from-[#1E3A8A] to-[#7098F8]/80 shadow-[0_0_12px_rgba(112,152,248,0.25)] border border-[#7098F8]/40'
                  : 'text-[#94A3B8] hover:text-white hover:bg-[#1E2227] border border-transparent'
              }`}
            >
              <Icon className="w-4 h-4 shrink-0 text-[#7098F8]" />
              {!isCollapsed && <span className="truncate">{link.label}</span>}
            </Link>
          );
        })}
      </nav>
    </div>
  );
}
