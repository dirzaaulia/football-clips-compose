'use client';

import React from 'react';
import Image from 'next/image';

interface GooglePlayBadgeProps {
  className?: string;
  onClick?: () => void;
  height?: number;
}

/**
 * Official Google Play Store Badge compliant with Google Brand Guidelines
 * Uses Google's official canonical web badge asset.
 */
export default function GooglePlayBadge({
  className = '',
  onClick,
  height = 42,
}: GooglePlayBadgeProps) {
  // Official badge ratio: 646 x 250 (approx 2.584:1)
  const width = Math.round(height * 2.584);

  return (
    <a
      href="https://play.google.com/store/apps/details?id=com.dirzaaulia.footballclips"
      target="_blank"
      rel="noopener noreferrer"
      onClick={onClick}
      className={`inline-block transition-transform hover:scale-105 active:scale-95 cursor-pointer shrink-0 ${className}`}
      title="Get Football Highlights & Clips on Google Play"
    >
      <Image
        src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png"
        alt="Get it on Google Play"
        width={width}
        height={height}
        className="h-auto object-contain drop-shadow-md"
        unoptimized
      />
    </a>
  );
}
