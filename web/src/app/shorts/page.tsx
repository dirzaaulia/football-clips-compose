'use client';

import React, { useState, useEffect } from 'react';
import { Sparkles, Film } from 'lucide-react';
import { getShorts, FootballShortRecord } from '@/lib/shorts';
import ShortsReelContainer from '@/components/shorts/ShortsReelContainer';
import ShortsCategoryBar from '@/components/shorts/ShortsCategoryBar';

export default function ShortsPage() {
  const [shorts, setShorts] = useState<FootballShortRecord[]>([]);
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;
    async function loadShorts() {
      setIsLoading(true);
      try {
        const data = await getShorts({
          category: selectedCategory === 'ALL' ? undefined : selectedCategory,
          limit: 50,
        });
        if (isMounted) {
          setShorts(data);
        }
      } catch (err) {
        console.error('Failed to load shorts:', err);
      } finally {
        if (isMounted) setIsLoading(false);
      }
    }
    loadShorts();
    return () => {
      isMounted = false;
    };
  }, [selectedCategory]);

  return (
    <div className="relative w-full h-[calc(100vh-4rem)] md:h-screen bg-[#0A0E14] overflow-hidden flex flex-col">
      {/* Floating Top Category Bar */}
      <div className="absolute top-3 left-0 right-0 z-30 flex justify-center pointer-events-none">
        <div className="pointer-events-auto backdrop-blur-md bg-black/40 rounded-full border border-white/10 shadow-lg px-2">
          <ShortsCategoryBar
            selectedCategory={selectedCategory}
            onSelectCategory={setSelectedCategory}
          />
        </div>
      </div>

      {/* Main Content Area */}
      {isLoading ? (
        <div className="w-full h-full flex flex-col items-center justify-center gap-3">
          <div className="relative flex items-center justify-center">
            <div className="w-12 h-12 rounded-full border-2 border-[#7098F8]/20 border-t-[#7098F8] animate-spin" />
            <Film className="w-5 h-5 text-[#7098F8] absolute fill-[#7098F8]/20" />
          </div>
          <p className="text-xs font-mono text-slate-400 tracking-wide">
            Loading official football shorts...
          </p>
        </div>
      ) : shorts.length === 0 ? (
        <div className="w-full h-full flex flex-col items-center justify-center gap-3 p-6 text-center">
          <Sparkles className="w-10 h-10 text-[#7098F8] opacity-60" />
          <h3 className="text-base font-bold text-white">No Shorts Available</h3>
          <p className="text-xs text-slate-400 max-w-sm">
            No clips found in this category. Select another category or check back soon for fresh match moments.
          </p>
          <button
            onClick={() => setSelectedCategory('ALL')}
            className="mt-2 px-4 py-2 rounded-xl bg-[#7098F8] text-white text-xs font-semibold hover:bg-blue-600 transition"
          >
            Show All Shorts
          </button>
        </div>
      ) : (
        <ShortsReelContainer shorts={shorts} />
      )}
    </div>
  );
}
