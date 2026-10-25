'use client';

import React, { useState } from 'react';
import Image from 'next/image';

interface GlassLogoBadgeProps {
  src?: string | null;
  alt: string;
  size?: number;
  className?: string;
  imageClassName?: string;
  fallbackText?: string;
  children?: React.ReactNode;
}

export default function GlassLogoBadge({
  src,
  alt,
  size = 28,
  className = '',
  imageClassName = 'object-contain p-1',
  fallbackText,
  children,
}: GlassLogoBadgeProps) {
  const [hasError, setHasError] = useState(false);

  return (
    <div
      style={{ width: size, height: size }}
      className={`relative rounded-xl bg-white/[0.08] backdrop-blur-md border border-white/10 shadow-sm flex items-center justify-center shrink-0 overflow-hidden ${className}`}
    >
      {children ? (
        children
      ) : src && !hasError ? (
        <div className="relative w-full h-full">
          <Image
            src={src}
            alt={alt}
            fill
            className={imageClassName}
            unoptimized
            onError={() => setHasError(true)}
          />
        </div>
      ) : (
        <span className="text-[10px] font-bold text-[#7098F8] select-none">
          {fallbackText || (alt ? alt.charAt(0) : '?')}
        </span>
      )}
    </div>
  );
}
