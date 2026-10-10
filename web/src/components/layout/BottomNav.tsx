'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Play, Calendar, Trophy, Info } from 'lucide-react';

interface NavItem {
  href: string;
  label: string;
  icon: React.ComponentType<{ className?: string }>;
}

const NAV_ITEMS: NavItem[] = [
  { href: '/', label: 'Highlights', icon: Play },
  { href: '/fixtures', label: 'Fixtures', icon: Calendar },
  { href: '/standings', label: 'Standings', icon: Trophy },
  { href: '/info', label: 'Info', icon: Info },
];

export default function BottomNav() {
  const pathname = usePathname();

  return (
    <nav
      aria-label="Mobile Navigation"
      className="md:hidden fixed bottom-4 inset-x-0 mx-auto w-fit z-40 h-16 rounded-full bg-[#181C20]/85 backdrop-blur-xl border border-white/10 shadow-[0_8px_32px_rgba(0,0,0,0.65)] px-3 flex items-center gap-1 sm:gap-2 pointer-events-auto"
    >
      {NAV_ITEMS.map((item) => {
        const Icon = item.icon;
        const isActive =
          item.href === '/'
            ? pathname === '/'
            : pathname.startsWith(item.href);

        return (
          <Link
            key={item.href}
            href={item.href}
            aria-label={item.label}
            aria-current={isActive ? 'page' : undefined}
            className={`relative flex flex-col items-center justify-center min-w-[56px] h-12 px-2.5 rounded-2xl transition-all duration-300 select-none ${
              isActive
                ? 'scale-110 text-[#7098F8] bg-[#1E3A8A]/30'
                : 'text-[#94A3B8] hover:text-white active:scale-95'
            }`}
          >
            <Icon
              className={`transition-colors duration-200 ${
                isActive ? 'w-5 h-5 text-[#7098F8]' : 'w-5 h-5 text-[#94A3B8]'
              }`}
            />
            {isActive && (
              <span className="text-[10px] font-bold tracking-tight leading-none mt-1 animate-in fade-in zoom-in-95 duration-200">
                {item.label}
              </span>
            )}
          </Link>
        );
      })}
    </nav>
  );
}
