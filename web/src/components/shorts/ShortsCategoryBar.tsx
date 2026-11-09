'use client';

import React from 'react';

const CATEGORIES = [
  { id: 'ALL', label: 'All Shorts' },
  { id: 'GOALS_SKILLS', label: 'Goals & Skills' },
  { id: 'MOMENTS', label: 'Iconic Moments' },
  { id: 'BEHIND_SCENES', label: 'Behind The Scenes' },
];

interface ShortsCategoryBarProps {
  selectedCategory: string;
  onSelectCategory: (category: string) => void;
}

export default function ShortsCategoryBar({
  selectedCategory,
  onSelectCategory,
}: ShortsCategoryBarProps) {
  return (
    <div className="flex items-center gap-2 overflow-x-auto no-scrollbar py-2 px-4 max-w-full">
      {CATEGORIES.map((cat) => {
        const isActive = selectedCategory === cat.id;
        return (
          <button
            key={cat.id}
            onClick={() => onSelectCategory(cat.id)}
            className={`px-3.5 py-1.5 rounded-full text-xs font-semibold whitespace-nowrap transition-all duration-200 ${
              isActive
                ? 'bg-[#7098F8] text-white shadow-[0_0_12px_rgba(112,152,248,0.4)] scale-105'
                : 'bg-white/10 hover:bg-white/20 text-slate-300 border border-white/5'
            }`}
          >
            {cat.label}
          </button>
        );
      })}
    </div>
  );
}
