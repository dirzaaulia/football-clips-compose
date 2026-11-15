import React from 'react';
import ShortsReelSkeleton from '@/components/shorts/ShortsReelSkeleton';

export default function ShortsLoading() {
  return (
    <div className="relative w-full h-[calc(100vh-4rem)] md:h-screen bg-[#0A0E14] overflow-hidden flex flex-col">
      <ShortsReelSkeleton />
    </div>
  );
}
