'use client';

import React, { useState, useEffect } from 'react';
import { Zap } from 'lucide-react';
import { getShorts, FootballShortRecord } from '@/lib/shorts';
import ShortsReelContainer from '@/components/shorts/ShortsReelContainer';
import ShortsReelSkeleton from '@/components/shorts/ShortsReelSkeleton';

export default function ShortsPage() {
  const [shorts, setShorts] = useState<FootballShortRecord[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;
    async function loadShorts() {
      setIsLoading(true);
      try {
        const data = await getShorts({ limit: 50 });
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
  }, []);

  return (
    <div className="relative w-full h-[calc(100vh-3rem)] md:h-screen bg-[#0A0A0C] overflow-hidden flex flex-col select-none">
      {isLoading ? (
        <ShortsReelSkeleton />
      ) : shorts.length === 0 ? (
        <div className="w-full h-full flex flex-col items-center justify-center gap-3 p-6 text-center">
          <Zap className="w-10 h-10 text-[#DFFF00] opacity-80" />
          <h3 className="font-scoreboard text-lg uppercase tracking-wider text-white">No Shorts Available</h3>
          <p className="font-tech text-xs text-white/50 max-w-sm">
            No clips found right now. Check back soon for fresh European wondergoals and decisive match moments.
          </p>
        </div>
      ) : (
        <ShortsReelContainer shorts={shorts} />
      )}
    </div>
  );
}
