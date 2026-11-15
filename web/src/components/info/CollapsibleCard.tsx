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
          ? 'bg-[#1A1C22] border-[#DFFF00]/40 shadow-[0_0_20px_rgba(223,255,0,0.1)]'
          : 'bg-[#1A1C22] border-white/10 hover:border-white/20'
      }`}
    >
      <button
        onClick={() => setExpanded(!expanded)}
        className="w-full flex items-center justify-between p-4 sm:p-5 cursor-pointer text-left"
      >
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center shrink-0">
            {icon}
          </div>
          <span className="text-sm sm:text-base font-bold text-white font-tech">{title}</span>
        </div>
        <ChevronDown
          className={`w-4 h-4 text-white/40 transition-transform duration-300 ${
            expanded ? 'rotate-180 text-[#DFFF00]' : ''
          }`}
        />
      </button>

      {expanded && (
        <div className="px-4 pb-5 sm:px-5 sm:pb-6 pt-1 border-t border-white/5 text-xs sm:text-sm text-white/70 leading-relaxed font-tech">
          {children}
        </div>
      )}
    </div>
  );
}
