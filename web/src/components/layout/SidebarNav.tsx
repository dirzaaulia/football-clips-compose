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
  Flame,
  Settings,
  Wrench,
  PanelLeftClose,
  PanelLeftOpen,
} from 'lucide-react';

interface SidebarNavProps {
  isCollapsed?: boolean;
  onItemClick?: () => void;
  onToggleCollapse?: () => void;
}

// During Google AdSense site approval review, affiliate store links are suppressed
// to adhere strictly to Google's "Thin affiliate site" policy guidelines.
const NAV_LINKS = [
  { href: '/', label: 'Highlights', icon: Play },
  { href: '/shorts', label: 'Shorts', icon: Flame },
  { href: '/fixtures', label: 'Scores & Fixtures', icon: Calendar },
  { href: '/standings', label: 'Standings & Stats', icon: Trophy },
  { href: '/tools', label: 'Tactical Tools', icon: Wrench },
  { href: '/articles', label: 'Analysis Hub', icon: Newspaper },
  { href: '/settings', label: 'Settings', icon: Settings },
];

export default function SidebarNav({
  isCollapsed = false,
  onItemClick,
  onToggleCollapse,
}: SidebarNavProps) {
  const pathname = usePathname();

  const links = NAV_LINKS;

  return (
    <div className="space-y-2.5 w-full">
      {/* Brand Header */}
      {isCollapsed ? (
        <div className="flex flex-col items-center gap-3 w-full pb-2 border-b border-white/5">
          <Link
            href="/"
            onClick={onItemClick}
            className="relative w-10 h-10 rounded-2xl overflow-hidden border border-[#DFFF00]/40 shadow-[0_0_15px_rgba(223,255,0,0.25)] hover:scale-105 transition-transform bg-[#0A0A0C] shrink-0 flex items-center justify-center cursor-pointer"
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
              className="w-8 h-8 rounded-xl flex items-center justify-center text-slate-400 hover:text-white bg-white/5 hover:bg-white/10 border border-white/5 hover:border-[#DFFF00]/40 shadow-sm transition-all cursor-pointer group"
            >
              <PanelLeftOpen className="w-4 h-4 text-[#DFFF00] group-hover:scale-110 transition-transform" />
            </button>
          )}
        </div>
      ) : (
        <div className="w-full flex items-center justify-between p-2 pl-2.5 rounded-2xl bg-[#121316] border border-white/10 shadow-sm hover:border-[#DFFF00]/30 transition-all">
          <Link
            href="/"
            onClick={onItemClick}
            className="flex items-center gap-3 group cursor-pointer min-w-0"
            title="Football Highlights & Clips"
          >
            {/* Logo Icon with luminous volt glow */}
            <div className="relative w-9 h-9 rounded-xl overflow-hidden border border-[#DFFF00]/40 shadow-[0_0_14px_rgba(223,255,0,0.25)] group-hover:scale-105 transition-transform bg-[#0A0A0C] shrink-0 flex items-center justify-center">
              <Image
                src="/app_icon.png"
                alt="Football Highlights & Clips"
                width={36}
                height={36}
                className="object-cover"
                priority
              />
            </div>

            {/* Brand Title: Modern OTT Stadium Scoreboard typography */}
            <div className="flex flex-col min-w-0">
              <div className="flex items-center gap-1.5">
                <span className="font-scoreboard text-base text-white tracking-wider leading-none group-hover:text-[#DFFF00] transition-colors">
                  FOOTBALL
                </span>
                <span className="text-xs font-black uppercase tracking-wider px-2 py-0.5 rounded-md bg-[#DFFF00]/15 text-[#DFFF00] border border-[#DFFF00]/30 leading-none">
                  CLIPS
                </span>
              </div>
              <span className="text-xs font-tech text-white/70 tracking-widest uppercase mt-1 leading-none font-semibold">
                OTT ARENA
              </span>
            </div>
          </Link>

          {/* Minimal Ghost Toggle Button */}
          {onToggleCollapse && (
            <button
              onClick={onToggleCollapse}
              title="Collapse sidebar"
              aria-label="Collapse sidebar"
              className="p-2 rounded-xl text-slate-400 hover:text-white hover:bg-white/10 border border-transparent hover:border-white/10 transition-all cursor-pointer shrink-0"
            >
              <PanelLeftClose className="w-4 h-4 text-[#DFFF00]" />
            </button>
          )}
        </div>
      )}

      {/* Navigation Links */}
      <nav className="space-y-1 w-full">
        {links.map((link) => {
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
                  ? 'text-[#0A0A0C] bg-[#DFFF00] font-black shadow-[0_0_16px_rgba(223,255,0,0.3)] border border-[#DFFF00]'
                  : 'text-[#A1A1AA] hover:text-white hover:bg-[#1A1C22] border border-transparent'
              }`}
            >
              <Icon className={`w-4 h-4 shrink-0 ${isActive ? 'text-[#0A0A0C]' : 'text-[#DFFF00]'}`} />
              {!isCollapsed && <span className="truncate">{link.label}</span>}
            </Link>
          );
        })}
      </nav>
    </div>
  );
}
