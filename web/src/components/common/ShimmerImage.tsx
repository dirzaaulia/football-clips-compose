'use client';

import React, { useState } from 'react';
import Image, { ImageProps } from 'next/image';

interface ShimmerImageProps extends Omit<ImageProps, 'onLoad' | 'onError'> {
  containerClassName?: string;
  fallbackIcon?: React.ReactNode;
}

export default function ShimmerImage({
  src,
  alt,
  className = '',
  containerClassName = '',
  fallbackIcon,
  fill,
  width,
  height,
  ...rest
}: ShimmerImageProps) {
  const [isLoaded, setIsLoaded] = useState(false);
  const [hasError, setHasError] = useState(false);

  if (hasError || !src) {
    return (
      <div
        className={`relative flex items-center justify-center bg-[#181C20] text-[#64748B] ${
          fill ? 'w-full h-full' : ''
        } ${containerClassName}`}
        style={!fill && width && height ? { width, height } : undefined}
      >
        {fallbackIcon || <span className="text-[10px] font-bold">FC</span>}
      </div>
    );
  }

  return (
    <div
      className={`relative overflow-hidden ${
        fill ? 'w-full h-full' : ''
      } ${containerClassName}`}
      style={!fill && width && height ? { width, height } : undefined}
    >
      {/* Animated Shimmer Skeleton Overlay */}
      {!isLoaded && (
        <div className="absolute inset-0 z-0 bg-[#141A23] shimmer-effect rounded-[inherit]" />
      )}

      {/* Crossfading Image */}
      <Image
        src={src}
        alt={alt}
        fill={fill}
        width={width}
        height={height}
        className={`transition-opacity duration-300 relative z-[1] ${
          isLoaded ? 'opacity-100' : 'opacity-0'
        } ${className}`}
        onLoad={() => setIsLoaded(true)}
        onError={() => setHasError(true)}
        {...rest}
      />
    </div>
  );
}
