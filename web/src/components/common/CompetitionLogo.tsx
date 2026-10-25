'use client';

import { useState } from 'react';
import Image from 'next/image';
import { Trophy } from 'lucide-react';

export default function CompetitionLogo({ src, name }: { src?: string; name: string }) {
  const [failedSource, setFailedSource] = useState('');
  if (!src || src === failedSource) return <Trophy className="h-4 w-4 text-[#7098F8]" aria-label={`${name} logo unavailable`} role="img" />;
  return <Image src={src} alt={name} fill className="object-contain" unoptimized onError={() => setFailedSource(src)} />;
}
