'use client';

import React, { useState } from 'react';
import { ChevronDown } from 'lucide-react';

interface CollapsibleCardProps {
  title: string;
  icon: React.ReactNode;
  initialExpanded?: boolean;
  children: React.ReactNode;
}

export default function CollapsibleCard({
  title,
  icon,
  initialExpanded = false,
  children,
}: CollapsibleCardProps) {
  const [expanded, setExpanded] = useState(initialExpanded);

  return (
    <div
      className={`rounded-2xl border transition-all duration-300 ${
        expanded
          ? 'bg-[#181C20] border-[#7098F8]/50 shadow-[0_0_20px_rgba(112,152,248,0.1)]'
          : 'bg-[#181C20] border-[#334155]/60 hover:border-[#334155]'
      }`}
    >
      <button
        onClick={() => setExpanded(!expanded)}
        className="w-full flex items-center justify-between p-4 sm:p-5 cursor-pointer text-left"
      >
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-[#7098F8]/15 border border-[#7098F8]/40 flex items-center justify-center shrink-0">
            {icon}
          </div>
          <span className="text-sm sm:text-base font-bold text-white">{title}</span>
        </div>
        <ChevronDown
          className={`w-4 h-4 text-[#94A3B8] transition-transform duration-300 ${
            expanded ? 'rotate-180 text-[#7098F8]' : ''
          }`}
        />
      </button>

      {expanded && (
        <div className="px-4 pb-5 sm:px-5 sm:pb-6 pt-1 border-t border-[#334155]/40 text-xs sm:text-sm text-[#94A3B8] leading-relaxed">
          {children}
        </div>
      )}
    </div>
  );
}
