'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { Star, Menu, X, Gem, CheckCircle2, ChevronLeft, ChevronRight } from 'lucide-react';
import SidebarNav from './SidebarNav';
import SidebarBottomControls from './SidebarBottomControls';
import PaywallModal from '../billing/PaywallModal';
import MyClubsModal from '../clubs/MyClubsModal';
import SeasonSelector from '../filter/SeasonSelector';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';
import { useProStatus } from '@/hooks/useProStatus';
import { useMyClubs } from '@/hooks/useMyClubs';

export default function Sidebar() {
  const { isSpoilerFree, toggleSpoilerFree } = useSpoilerFree();
  const hasPro = useProStatus();
  const { favoriteCount } = useMyClubs();

  const [isCollapsed, setIsCollapsed] = useState(false);
  const [isMobileOpen, setIsMobileOpen] = useState(false);
  const [isPaywallOpen, setIsPaywallOpen] = useState(false);
  const [isMyClubsOpen, setIsMyClubsOpen] = useState(false);
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    if (typeof window !== 'undefined') {
      const saved = localStorage.getItem('fc-sidebar-collapsed');
      if (saved === 'true') {
        setIsCollapsed(true);
      }
    }

    const shouldOpenPaywall =
      typeof window !== 'undefined' &&
      sessionStorage.getItem('fc-open-paywall-after-auth') === 'true';

    const timer = setTimeout(() => {
      setMounted(true);
      if (shouldOpenPaywall) {
        sessionStorage.removeItem('fc-open-paywall-after-auth');
        setIsPaywallOpen(true);
      }
    }, 0);

    const handleOpenPaywall = () => setIsPaywallOpen(true);
    window.addEventListener('fc-open-paywall', handleOpenPaywall);

    return () => {
      clearTimeout(timer);
      window.removeEventListener('fc-open-paywall', handleOpenPaywall);
    };
  }, []);

  const toggleCollapse = () => {
    setIsCollapsed((prev) => {
      const next = !prev;
      if (typeof window !== 'undefined') {
        localStorage.setItem('fc-sidebar-collapsed', String(next));
      }
      return next;
    });
  };

  const renderContent = (onClose?: () => void, forceExpanded: boolean = false) => {
    const collapsed = !forceExpanded && isCollapsed;
    return (
      <div
        className={`w-full h-full flex flex-col justify-between py-3.5 ${
          collapsed ? 'px-0 items-center' : 'px-3'
        } overflow-y-auto overscroll-contain`}
      >
        <div className="space-y-2 w-full flex flex-col items-center">
          {/* Navigation & Header */}
          <SidebarNav
            isCollapsed={collapsed}
            onItemClick={onClose}
            onToggleCollapse={forceExpanded ? undefined : toggleCollapse}
          />

          {/* Context & Filters: My Clubs + Season */}
          {collapsed ? (
            <div className="space-y-2 pt-2 border-t border-[#334155]/40 flex flex-col items-center justify-center w-full">
              <button
                onClick={() => {
                  if (onClose) onClose();
                  setIsMyClubsOpen(true);
                }}
                className="relative flex items-center justify-center w-10 h-10 rounded-xl bg-[#181C20] hover:bg-[#1E2227] border border-[#334155] text-white hover:border-[#7098F8]/50 transition-all cursor-pointer group"
                title={mounted && favoriteCount > 0 ? `My Clubs (${favoriteCount})` : 'My Clubs'}
                aria-label="My Clubs"
              >
                <Star className="w-4 h-4 text-[#7098F8] group-hover:scale-110 transition-transform" />
                {mounted && favoriteCount > 0 && (
                  <span
                    suppressHydrationWarning
                    className="absolute -top-1 -right-1 flex h-3.5 min-w-[14px] px-0.5 items-center justify-center rounded-full bg-[#1E3A8A] text-[#4FC3F7] text-[8px] font-black border border-[#7098F8]/60 shadow-[0_0_8px_rgba(112,152,248,0.5)]"
                  >
                    {favoriteCount}
                  </span>
                )}
              </button>

              <div className="w-full flex items-center justify-center">
                <SeasonSelector variant="icon" align="popout" />
              </div>
            </div>
          ) : (
            <div className="pt-2 border-t border-[#334155]/40 grid grid-cols-2 gap-1.5 w-full">
              {/* Season Selector */}
              <SeasonSelector variant="compact" className="w-full" />

              {/* My Clubs Trigger Button */}
              <button
                onClick={() => {
                  if (onClose) onClose();
                  setIsMyClubsOpen(true);
                }}
                className="flex items-center justify-between px-2.5 py-1.5 rounded-xl text-xs font-bold bg-[#181C20] hover:bg-[#1E2227] border border-[#334155] text-white transition-all cursor-pointer group min-w-0"
              >
                <div className="flex items-center gap-1.5 truncate">
                  <Star className="w-3.5 h-3.5 text-[#7098F8] group-hover:scale-110 transition-transform shrink-0" />
                  <span className="truncate">Clubs</span>
                </div>
                <span
                  suppressHydrationWarning
                  className={`text-[9px] font-black px-1.5 py-0.2 rounded-full shrink-0 ${
                    mounted && favoriteCount > 0
                      ? 'bg-[#1E3A8A]/50 text-[#4FC3F7] border border-[#7098F8]/40'
                      : 'bg-zinc-800 text-zinc-400'
                  }`}
                >
                  {mounted ? favoriteCount : 0}
                </span>
              </button>
            </div>
          )}
        </div>

        {/* Bottom Controls: Spoiler-Free & Paddle PRO */}
        <SidebarBottomControls
          isCollapsed={collapsed}
          isSpoilerFree={isSpoilerFree}
          hasPro={hasPro}
          onToggleSpoilerFree={toggleSpoilerFree}
          onOpenPaywall={() => {
            if (onClose) onClose();
            setIsPaywallOpen(true);
          }}
        />
      </div>
    );
  };

  return (
    <>
      {/* Desktop Left Sidebar: Collapsible rail (w-16) or full width (w-60/64) */}
      <aside
        className={`hidden md:flex shrink-0 ${
          isCollapsed ? 'w-16' : 'w-60 lg:w-64'
        } h-dvh h-screen sticky top-0 bg-[#101418] border-r border-[#334155]/60 z-40 transition-all duration-300 ease-in-out`}
      >
        {renderContent(undefined, false)}
      </aside>

      {/* Mobile Top Header (Screens < md) */}
      <header className="md:hidden sticky top-0 z-50 bg-[#101418]/90 backdrop-blur-xl border-b border-[#334155]/60 px-3 sm:px-4 h-14 flex items-center justify-between">
        <Link href="/" className="flex items-center gap-2 min-w-0">
          <div className="relative w-8 h-8 rounded-xl overflow-hidden border border-[#7098F8]/40 shadow-sm bg-[#181C20] shrink-0">
            <Image
              src="/app_icon.png"
              alt="Football Highlights & Clips"
              width={32}
              height={32}
              className="object-cover"
              priority
            />
          </div>
          <div className="flex items-center gap-1 font-extrabold text-sm truncate">
            <span className="text-white">Football</span>
            <span className="text-[#7098F8] hidden xs:inline sm:inline">Highlights &amp;</span>
            <span className="text-[#7098F8]">Clips</span>
          </div>
        </Link>

        <div className="flex items-center gap-1.5 shrink-0">
          <SeasonSelector variant="compact" />
          <button
            onClick={() => setIsPaywallOpen(true)}
            className={`p-1.5 rounded-xl transition-all cursor-pointer ${
              hasPro
                ? 'bg-[#4CAF50] text-white shadow-[0_0_10px_rgba(76,175,80,0.4)]'
                : 'bg-[#D4AF37] text-black shadow-[0_0_10px_rgba(212,175,55,0.4)]'
            }`}
            title={hasPro ? 'PREMIUM Active' : 'GO PREMIUM'}
            aria-label={hasPro ? 'PREMIUM Active' : 'GO PREMIUM'}
          >
            {hasPro ? (
              <CheckCircle2 className="w-4 h-4 text-white" />
            ) : (
              <Gem className="w-4 h-4 fill-current text-black" />
            )}
          </button>
          <button
            onClick={() => setIsMobileOpen(true)}
            className="p-1.5 rounded-xl bg-[#181C20] text-[#94A3B8] hover:text-white border border-[#334155] cursor-pointer"
            aria-label="Open Navigation Menu"
          >
            <Menu className="w-5 h-5" />
          </button>
        </div>
      </header>

      {/* Mobile Slide-over Drawer */}
      {isMobileOpen && (
        <div className="md:hidden fixed inset-0 z-50 flex">
          <div
            className="fixed inset-0 bg-black/75 backdrop-blur-sm animate-in fade-in"
            onClick={() => setIsMobileOpen(false)}
          />
          <div className="relative w-4/5 max-w-[280px] sm:max-w-xs bg-[#101418] border-r border-[#334155] shadow-2xl h-full flex flex-col z-10 animate-in slide-in-from-left duration-200">
            {/* Mobile Drawer Top Bar with Brand & Close Button */}
            <div className="flex items-center justify-between px-4 pt-4 pb-2 border-b border-[#334155]/60 shrink-0">
              <div className="flex items-center gap-2">
                <div className="relative w-7 h-7 rounded-xl overflow-hidden border border-[#7098F8]/40 bg-[#181C20] shrink-0">
                  <Image src="/app_icon.png" alt="FC Logo" width={28} height={28} className="object-cover" />
                </div>
                <span className="font-extrabold text-sm text-white">Menu</span>
              </div>
              <button
                onClick={() => setIsMobileOpen(false)}
                className="p-1.5 rounded-xl text-[#94A3B8] hover:text-white bg-[#181C20] border border-[#334155] cursor-pointer"
                aria-label="Close menu"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Mobile Navigation Content (Always expanded) */}
            <div className="flex-1 overflow-y-auto">
              {renderContent(() => setIsMobileOpen(false), true)}
            </div>
          </div>
        </div>
      )}

      {/* Paywall & RevenueCat Modal */}
      <PaywallModal isOpen={isPaywallOpen} onClose={() => setIsPaywallOpen(false)} />

      {/* My Clubs Modal */}
      <MyClubsModal isOpen={isMyClubsOpen} onClose={() => setIsMyClubsOpen(false)} />
    </>
  );
}
