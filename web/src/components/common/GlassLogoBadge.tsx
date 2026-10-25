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
      className={`relative rounded-xl bg-gradient-to-b from-white/[0.22] via-white/[0.14] to-white/[0.10] backdrop-blur-md border border-white/20 shadow-[0_2px_8px_rgba(0,0,0,0.3)] flex items-center justify-center shrink-0 overflow-hidden ${className}`}
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
