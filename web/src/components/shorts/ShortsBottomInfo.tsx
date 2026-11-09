'use client';

import React from 'react';
import { Sparkles } from 'lucide-react';

interface ShortsBottomInfoProps {
  channelName: string;
  category: string;
  title: string;
}

export default function ShortsBottomInfo({
  channelName,
  category,
  title,
}: ShortsBottomInfoProps) {
  const formattedCategory = category.replace(/_/g, ' ');

  return (
    <div className="space-y-2 flex-1 max-w-[80%] pr-2">
      {/* Category Pill and Channel */}
      <div className="flex items-center gap-2 flex-wrap">
        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-[#7098F8]/20 border border-[#7098F8]/40 text-[#7098F8] text-[11px] font-bold tracking-wide uppercase backdrop-blur-md">
          <Sparkles className="w-3 h-3 text-[#7098F8]" />
          {formattedCategory}
        </span>
        <span className="text-xs font-bold text-white tracking-wide drop-shadow-md">
          @{channelName}
        </span>
      </div>

      {/* Video Title */}
      <h2 className="text-sm font-semibold text-white/95 line-clamp-2 leading-snug drop-shadow-md">
        {title}
      </h2>
    </div>
  );
}
