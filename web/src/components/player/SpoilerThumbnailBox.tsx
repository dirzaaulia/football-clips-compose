'use client';

import React from 'react';
import { EyeOff } from 'lucide-react';

interface SpoilerThumbnailBoxProps {
  isSpoilerFree: boolean;
  isRevealed: boolean;
  onRevealClick?: () => void;
  children: React.ReactNode;
  className?: string;
}

export default function SpoilerThumbnailBox({
  isSpoilerFree,
  isRevealed,
  onRevealClick,
  children,
  className = '',
}: SpoilerThumbnailBoxProps) {
  const isBlurred = isSpoilerFree && !isRevealed;

  return (
    <div className={`relative overflow-hidden ${className}`}>
      {/* Thumbnail Content with conditional 20px blur */}
      <div
        className={`w-full h-full transition-all duration-300 ${
          isBlurred ? 'filter blur-[18px] scale-105' : ''
        }`}
      >
        {children}
      </div>

      {/* Frosted Glass Overlay matching Android SpoilerThumbnailBox.kt */}
      {isBlurred && (
        <div
          onClick={(e) => {
            e.stopPropagation();
            onRevealClick?.();
          }}
          className="absolute inset-0 z-20 flex items-center justify-center bg-black/45 backdrop-blur-[2px] cursor-pointer transition-colors hover:bg-black/35"
        >
          <div className="flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-white/20 hover:bg-white/30 backdrop-blur-md border border-white/40 shadow-xl text-white transition-all transform hover:scale-105">
            <EyeOff className="w-4 h-4 text-white" />
            <span className="text-xs font-black tracking-wide">Spoiler Free</span>
          </div>
        </div>
      )}
    </div>
  );
}
