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
    const saved = localStorage.getItem('fc-sidebar-collapsed') === 'true';
    const shouldOpenPaywall =
      typeof window !== 'undefined' &&
      sessionStorage.getItem('fc-open-paywall-after-auth') === 'true';

    const timer = setTimeout(() => {
      setMounted(true);
      setIsCollapsed(saved);
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
    const nextVal = !isCollapsed;
    setIsCollapsed(nextVal);
    localStorage.setItem('fc-sidebar-collapsed', String(nextVal));
    window.dispatchEvent(new CustomEvent('fc-sidebar-toggle', { detail: { isCollapsed: nextVal } }));
  };

  const sidebarContent = (
    <div
      className={`h-full flex flex-col justify-between py-5 transition-all duration-300 ${
        isCollapsed ? 'px-2 items-center' : 'px-4'
      }`}
    >
      <div className="space-y-4 w-full">
        {/* Navigation & Header */}
        <SidebarNav
          isCollapsed={isCollapsed}
          onItemClick={() => setIsMobileOpen(false)}
        />

        {/* My Clubs Trigger Button (Android Parity) */}
        <div className="pt-1 w-full">
          <button
            onClick={() => {
              setIsMobileOpen(false);
              setIsMyClubsOpen(true);
            }}
            title={isCollapsed ? `My Clubs (${favoriteCount})` : undefined}
            className={`w-full flex items-center ${
              isCollapsed ? 'justify-center p-2.5' : 'justify-between px-3 py-2'
            } rounded-2xl text-xs font-bold bg-[#181C20] hover:bg-[#1E2227] border border-[#334155] text-white transition-all cursor-pointer group`}
          >
            <div className="flex items-center gap-2.5">
              <Star className="w-4 h-4 text-[#7098F8] group-hover:scale-110 transition-transform shrink-0" />
              {!isCollapsed && <span>My Clubs</span>}
            </div>
            {!isCollapsed && (
              <span
                suppressHydrationWarning
                className={`text-[10px] font-black px-2 py-0.5 rounded-full ${
                  mounted && favoriteCount > 0
                    ? 'bg-[#1E3A8A]/50 text-[#4FC3F7] border border-[#7098F8]/40'
                    : 'bg-zinc-800 text-zinc-400'
                }`}
              >
                {mounted ? favoriteCount : 0}
              </span>
            )}
          </button>
        </div>

        {/* Global Season Selector (Android Parity) */}
        <div className="pt-1 w-full">
          {!isCollapsed ? (
            <SeasonSelector variant="select" className="w-full justify-between" />
          ) : (
            <SeasonSelector variant="compact" className="w-full justify-center px-1" />
          )}
        </div>
      </div>

      {/* Bottom Controls: Spoiler-Free & Paddle PRO */}
      <SidebarBottomControls
        isCollapsed={isCollapsed}
        isSpoilerFree={isSpoilerFree}
        hasPro={hasPro}
        onToggleSpoilerFree={toggleSpoilerFree}
        onOpenPaywall={() => {
          setIsMobileOpen(false);
          setIsPaywallOpen(true);
        }}
      />
    </div>
  );

  return (
    <>
      {/* Desktop Left Sidebar (Collapsible: w-64/72 or w-20) */}
      <aside
        className={`relative hidden md:flex shrink-0 h-screen sticky top-0 bg-[#101418] border-r border-[#334155]/60 z-40 transition-all duration-300 ${
          isCollapsed ? 'w-20' : 'w-64 lg:w-72'
        }`}
      >
        {/* Floating Border Edge Toggle Button */}
        <button
          onClick={toggleCollapse}
          aria-label={isCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          title={isCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          className="absolute -right-3 top-6 z-50 w-6 h-6 rounded-full bg-[#181C20] hover:bg-[#1E2227] border border-[#334155] hover:border-[#7098F8]/60 text-[#94A3B8] hover:text-white flex items-center justify-center shadow-lg transition-all cursor-pointer"
        >
          {isCollapsed ? <ChevronRight className="w-3.5 h-3.5" /> : <ChevronLeft className="w-3.5 h-3.5" />}
        </button>

        {sidebarContent}
      </aside>

      {/* Mobile Top Header (Screens < md) */}
      <header className="md:hidden sticky top-0 z-50 bg-[#101418]/90 backdrop-blur-xl border-b border-[#334155]/60 px-4 h-14 flex items-center justify-between">
        <Link href="/" className="flex items-center gap-2.5">
          <div className="relative w-8 h-8 rounded-xl overflow-hidden border border-[#7098F8]/40 shadow-sm bg-[#181C20]">
            <Image
              src="/app_icon.png"
              alt="Football Highlights & Clips"
              width={32}
              height={32}
              className="object-cover"
              priority
            />
          </div>
          <div className="flex items-center gap-1 font-extrabold text-sm">
            <span className="text-white">Football</span>
            <span className="text-[#7098F8]">Highlights & Clips</span>
          </div>
        </Link>

        <div className="flex items-center gap-1.5">
          <SeasonSelector variant="compact" />
          <button
            onClick={() => setIsPaywallOpen(true)}
            className={`p-1.5 rounded-xl transition-all cursor-pointer ${
              hasPro
                ? 'bg-[#4CAF50] text-white shadow-[0_0_10px_rgba(76,175,80,0.4)]'
                : 'bg-[#D4AF37] text-black shadow-[0_0_10px_rgba(212,175,55,0.4)]'
            }`}
            title={hasPro ? 'PREMIUM Active' : 'GO PREMIUM'}
          >
            {hasPro ? (
              <CheckCircle2 className="w-4 h-4 text-white" />
            ) : (
              <Gem className="w-4 h-4 fill-current text-black" />
            )}
          </button>
          <button
            onClick={() => setIsMobileOpen(true)}
            className="p-1.5 rounded-xl bg-[#181C20] text-[#94A3B8] hover:text-white border border-[#334155]"
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
            className="fixed inset-0 bg-black/70 backdrop-blur-sm animate-in fade-in"
            onClick={() => setIsMobileOpen(false)}
          />
          <div className="relative w-4/5 max-w-xs bg-[#101418] border-r border-[#334155] shadow-2xl h-full flex flex-col z-10 animate-in slide-in-from-left duration-200">
            <div className="absolute top-4 right-4">
              <button
                onClick={() => setIsMobileOpen(false)}
                className="p-1.5 rounded-xl text-[#94A3B8] hover:text-white bg-[#181C20] border border-[#334155]"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
            {sidebarContent}
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
