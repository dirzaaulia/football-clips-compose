'use client';

import React, { useEffect } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { X, ChevronRight, Newspaper, Tv, Trophy, Sparkles, SlidersHorizontal, Wrench, ShieldCheck, Mail, Info } from 'lucide-react';

interface MobileNavDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  pathname: string;
  links: Array<{ href: string; label: string }>;
}

const EXTRA_LINKS = [
  { href: '/about', label: 'About & Masthead', icon: Info },
  { href: '/editorial-standards', label: 'Editorial Standards', icon: ShieldCheck },
  { href: '/contact', label: 'Contact & Corrections', icon: Mail },
];

export default function MobileNavDrawer({
  isOpen,
  onClose,
  pathname,
  links,
}: MobileNavDrawerProps) {
  useEffect(() => {
    if (!isOpen) return;

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };

    document.body.style.overflow = 'hidden';
    window.addEventListener('keydown', handleKeyDown);

    return () => {
      document.body.style.overflow = '';
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 md:hidden flex flex-col justify-end">
      {/* Dim Backdrop */}
      <div
        className="fixed inset-0 bg-black/80 backdrop-blur-md transition-opacity animate-in fade-in duration-200"
        onClick={onClose}
        aria-hidden="true"
      />

      {/* Drawer Container */}
      <div
        role="dialog"
        aria-modal="true"
        aria-label="Mobile Navigation Menu"
        className="relative z-10 w-full max-h-[85vh] bg-[#121316] border-t border-white/10 rounded-t-3xl shadow-2xl flex flex-col overflow-hidden animate-in slide-in-from-bottom duration-300"
      >
        {/* Drawer Header */}
        <div className="flex items-center justify-between px-5 py-4 border-b border-white/10 shrink-0">
          <div className="flex items-center gap-2.5">
            <div className="relative w-7 h-7 shrink-0">
              <Image
                src="/favicon.png"
                alt="FootballClips"
                fill
                className="object-contain"
                unoptimized
              />
            </div>
            <span className="font-scoreboard text-lg tracking-wider uppercase text-white">
              FOOTBALL<span className="text-[#DFFF00]">CLIPS</span>
            </span>
          </div>

          <button
            onClick={onClose}
            className="w-10 h-10 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center text-white/70 hover:text-white transition-colors cursor-pointer"
            aria-label="Close menu"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Scrollable Links Body */}
        <div className="flex-1 overflow-y-auto px-4 py-4 space-y-4">
          <div className="space-y-1">
            <span className="px-3 text-[10px] font-tech font-bold uppercase tracking-widest text-white/40">
              Primary Navigation
            </span>
            <div className="space-y-1 pt-1">
              {links.map((link) => {
                const isActive =
                  link.href === '/'
                    ? pathname === '/'
                    : pathname.startsWith(link.href);

                return (
                  <Link
                    key={link.href}
                    href={link.href}
                    onClick={onClose}
                    className={`flex items-center justify-between px-3.5 py-3 rounded-2xl min-h-[48px] font-tech text-sm tracking-wide transition-all ${
                      isActive
                        ? 'bg-[#DFFF00]/15 text-[#DFFF00] font-bold border border-[#DFFF00]/30'
                        : 'text-white/80 hover:text-white hover:bg-white/5'
                    }`}
                  >
                    <span>{link.label}</span>
                    <ChevronRight className={`w-4 h-4 ${isActive ? 'text-[#DFFF00]' : 'text-white/30'}`} />
                  </Link>
                );
              })}
            </div>
          </div>

          {/* Editorial & Trust Links */}
          <div className="pt-2 border-t border-white/10 space-y-1">
            <span className="px-3 text-[10px] font-tech font-bold uppercase tracking-widest text-white/40">
              Publishing Standards &amp; Trust
            </span>
            <div className="space-y-1 pt-1">
              {EXTRA_LINKS.map((item) => {
                const Icon = item.icon;
                const isActive = pathname.startsWith(item.href);

                return (
                  <Link
                    key={item.href}
                    href={item.href}
                    onClick={onClose}
                    className={`flex items-center gap-3 px-3.5 py-2.5 rounded-xl min-h-[44px] font-tech text-xs transition-colors ${
                      isActive
                        ? 'bg-white/10 text-[#DFFF00] font-bold'
                        : 'text-white/60 hover:text-white hover:bg-white/5'
                    }`}
                  >
                    <Icon className="w-4 h-4 shrink-0 text-[#DFFF00]" />
                    <span>{item.label}</span>
                  </Link>
                );
              })}
            </div>
          </div>
        </div>

        {/* Drawer Footer Notice */}
        <div className="px-5 py-3 bg-[#0A0A0C] border-t border-white/5 flex items-center justify-between text-[11px] text-[#A1A1AA] font-tech shrink-0">
          <span>Verified European Sports Intelligence</span>
          <span className="text-[#DFFF00]">Opta &amp; ESPN</span>
        </div>
      </div>
    </div>
  );
}
