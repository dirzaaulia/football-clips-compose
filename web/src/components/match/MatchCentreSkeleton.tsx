'use client';

import React from 'react';

export default function MatchCentreSkeleton() {
  return (
    <div className="w-full max-w-7xl mx-auto px-3.5 sm:px-6 lg:px-8 py-4 sm:py-6 space-y-6 animate-pulse select-none">
      {/* Top Meta Sub-Bar Skeleton */}
      <div className="w-full bg-[#0A0A0C] border border-white/10 rounded-xl p-3 flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <div className="w-16 h-3 rounded bg-white/10" />
          <div className="w-2 h-3 text-white/20">/</div>
          <div className="w-24 h-3 rounded bg-white/10" />
          <div className="w-2 h-3 text-white/20">/</div>
          <div className="w-36 h-3 rounded bg-[#DFFF00]/20" />
        </div>
        <div className="flex items-center gap-3">
          <div className="w-28 h-6 rounded-full bg-white/10" />
          <div className="hidden md:block w-32 h-4 rounded bg-white/5" />
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Left Column (8 cols) */}
        <div className="lg:col-span-8 space-y-6">
          {/* 16:9 Cinema Viewport Skeleton */}
          <div className="relative w-full aspect-video rounded-2xl bg-[#0A0A0C] border border-white/10 overflow-hidden shadow-2xl flex flex-col justify-between p-4 sm:p-6">
            {/* Top Badges */}
            <div className="flex items-center justify-between z-10">
              <div className="flex items-center gap-2">
                <div className="w-28 h-6 rounded-md bg-[#DFFF00]/20" />
                <div className="w-32 h-6 rounded-md bg-white/10" />
              </div>
              <div className="w-20 h-6 rounded-md bg-white/5" />
            </div>

            {/* Center Play Button Anchor */}
            <div className="absolute inset-0 flex items-center justify-center">
              <div className="w-20 h-20 rounded-full bg-white/10 border border-white/20 flex items-center justify-center">
                <div className="w-0 h-0 border-y-8 border-y-transparent border-l-12 border-l-white/40 ml-1" />
              </div>
            </div>

            {/* Bottom Scrubber & Controls */}
            <div className="space-y-2 z-10 pt-4">
              <div className="w-full h-1.5 rounded-full bg-white/10 relative">
                <div className="w-1/2 h-full bg-[#DFFF00]/30 rounded-full" />
              </div>
              <div className="flex items-center justify-between pt-1">
                <div className="flex items-center gap-3">
                  <div className="w-6 h-6 rounded-full bg-white/10" />
                  <div className="w-16 h-3 rounded bg-white/10" />
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-12 h-5 rounded bg-white/10" />
                  <div className="w-6 h-6 rounded bg-white/10" />
                </div>
              </div>
            </div>
          </div>

          {/* Action Toolbar Skeleton */}
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 py-2 border-b border-white/10">
            <div className="space-y-2 max-w-lg">
              <div className="w-72 sm:w-96 h-6 rounded bg-white/20" />
              <div className="w-48 h-3.5 rounded bg-white/10" />
            </div>
            <div className="flex items-center gap-2">
              <div className="w-24 h-9 rounded-xl bg-white/10" />
              <div className="w-24 h-9 rounded-xl bg-white/10" />
              <div className="w-28 h-9 rounded-xl bg-[#DFFF00]/20" />
            </div>
          </div>

          {/* Scoreboard Banner Skeleton */}
          <div className="w-full bg-[#121316] border border-white/10 rounded-2xl p-5 sm:p-6 shadow-xl space-y-5">
            <div className="grid grid-cols-12 items-center gap-3">
              <div className="col-span-5 flex items-center gap-3">
                <div className="w-12 h-12 rounded-xl bg-white/10 shrink-0" />
                <div className="space-y-1.5 min-w-0">
                  <div className="w-28 h-5 rounded bg-white/20" />
                  <div className="w-20 h-3 rounded bg-white/10" />
                </div>
              </div>

              <div className="col-span-2 flex flex-col items-center justify-center space-y-1.5">
                <div className="w-16 h-9 rounded-xl bg-white/15" />
                <div className="w-14 h-4 rounded bg-[#DFFF00]/20" />
              </div>

              <div className="col-span-5 flex items-center justify-end gap-3 text-right">
                <div className="space-y-1.5 min-w-0 flex flex-col items-end">
                  <div className="w-28 h-5 rounded bg-white/20" />
                  <div className="w-20 h-3 rounded bg-white/10" />
                </div>
                <div className="w-12 h-12 rounded-xl bg-white/10 shrink-0" />
              </div>
            </div>

            {/* 90-Minute Scrubber Skeleton */}
            <div className="pt-2 space-y-2">
              <div className="flex justify-between">
                <div className="w-24 h-3 rounded bg-white/10" />
                <div className="w-32 h-3 rounded bg-white/10" />
              </div>
              <div className="w-full h-8 rounded-lg bg-[#0A0A0C] border border-white/5" />
            </div>
          </div>

          {/* Tactical Analytics Multi-Tab Hub Skeleton */}
          <div className="w-full bg-[#121316] border border-white/10 rounded-2xl p-5 sm:p-6 shadow-xl space-y-5">
            {/* Tab strip */}
            <div className="flex items-center gap-2 overflow-x-auto pb-2">
              <div className="w-36 h-9 rounded-xl bg-[#DFFF00]/25 shrink-0" />
              <div className="w-32 h-9 rounded-xl bg-white/10 shrink-0" />
              <div className="w-32 h-9 rounded-xl bg-white/10 shrink-0" />
              <div className="w-28 h-9 rounded-xl bg-white/10 shrink-0" />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-12 gap-6 pt-2">
              <div className="md:col-span-7 space-y-4">
                <div className="w-full h-12 rounded-xl bg-white/5 p-3 space-y-2">
                  <div className="flex justify-between">
                    <div className="w-10 h-3 rounded bg-white/20" />
                    <div className="w-10 h-3 rounded bg-white/20" />
                  </div>
                  <div className="w-full h-2 rounded-full bg-white/10" />
                </div>
                <div className="space-y-3">
                  {[1, 2, 3, 4].map((i) => (
                    <div key={i} className="space-y-1.5">
                      <div className="flex justify-between">
                        <div className="w-12 h-3 rounded bg-white/15" />
                        <div className="w-12 h-3 rounded bg-white/15" />
                      </div>
                      <div className="w-full h-1.5 rounded-full bg-white/10" />
                    </div>
                  ))}
                </div>
              </div>

              <div className="md:col-span-5">
                <div className="w-full aspect-[4/3] rounded-xl bg-[#0A0A0C] border border-white/10" />
              </div>
            </div>
          </div>
        </div>

        {/* Right Sidebar Skeleton (4 cols) */}
        <div className="lg:col-span-4 space-y-6">
          {/* Title Race Card Skeleton */}
          <div className="w-full bg-[#121316] border border-white/10 rounded-2xl p-5 space-y-4 shadow-xl">
            <div className="flex items-center justify-between">
              <div className="w-32 h-5 rounded bg-white/20" />
              <div className="w-16 h-4 rounded bg-[#DFFF00]/20" />
            </div>
            <div className="space-y-2">
              {[1, 2, 3].map((i) => (
                <div key={i} className="w-full h-8 rounded-lg bg-white/5" />
              ))}
            </div>
          </div>

          {/* Related Highlights Skeleton */}
          <div className="w-full bg-[#121316] border border-white/10 rounded-2xl p-5 space-y-3 shadow-xl">
            <div className="w-36 h-5 rounded bg-white/20" />
            <div className="space-y-3">
              {[1, 2, 3].map((i) => (
                <div key={i} className="flex gap-3">
                  <div className="w-28 aspect-video rounded bg-white/10 shrink-0" />
                  <div className="flex-1 space-y-2">
                    <div className="w-full h-3.5 rounded bg-white/15" />
                    <div className="w-2/3 h-3 rounded bg-white/10" />
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Pro Pass Skeleton */}
          <div className="w-full rounded-2xl bg-[#0A0A0C] border border-white/10 p-5 space-y-3">
            <div className="w-28 h-4 rounded bg-[#DFFF00]/20" />
            <div className="w-44 h-6 rounded bg-white/20" />
            <div className="w-full h-10 rounded-xl bg-[#DFFF00]/25" />
          </div>
        </div>
      </div>
    </div>
  );
}
