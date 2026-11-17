import React from 'react';
import ShortsReelSkeleton from '@/components/shorts/ShortsReelSkeleton';

export default function ShortsLoading() {
  return (
    <div className="relative w-full h-[calc(100dvh-3rem)] bg-[#0A0A0C] overflow-hidden flex flex-col">
      <ShortsReelSkeleton />
    </div>
  );
}
