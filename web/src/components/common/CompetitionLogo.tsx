'use client';

import { useState } from 'react';
import Image from 'next/image';
import { Trophy } from 'lucide-react';

export default function CompetitionLogo({
  src,
  name,
  className = '',
  withGlassBadge = false,
}: {
  src?: string;
  name: string;
  className?: string;
  withGlassBadge?: boolean;
}) {
  const [failedSource, setFailedSource] = useState('');

  const content = (!src || src === failedSource) ? (
    <Trophy className="h-3.5 w-3.5 text-[#7098F8]" aria-label={`${name} logo unavailable`} role="img" />
  ) : (
    <Image
      src={src}
      alt={name}
      fill
      className="object-contain p-0.5"
      unoptimized
      onError={() => setFailedSource(src)}
    />
  );

  if (withGlassBadge) {
    return (
      <div className={`relative rounded-xl bg-white/[0.08] backdrop-blur-md border border-white/10 shadow-sm flex items-center justify-center shrink-0 overflow-hidden ${className}`}>
        {content}
      </div>
    );
  }

  return content;
}
