'use client';

import React, { Suspense, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Loader2 } from 'lucide-react';

function MatchRedirectHandler() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const id = searchParams.get('id');

  useEffect(() => {
    if (id) {
      router.replace(`/match/${id}`);
    } else {
      router.replace('/fixtures');
    }
  }, [id, router]);

  return (
    <div className="min-h-[60vh] flex flex-col items-center justify-center space-y-3">
      <Loader2 className="w-8 h-8 text-[#7098F8] animate-spin" />
      <span className="text-xs text-[#94A3B8]">Loading match centre...</span>
    </div>
  );
}

export default function GenericMatchFallbackPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-[60vh] flex flex-col items-center justify-center space-y-3">
          <Loader2 className="w-8 h-8 text-[#7098F8] animate-spin" />
          <span className="text-xs text-[#94A3B8]">Loading match centre...</span>
        </div>
      }
    >
      <MatchRedirectHandler />
    </Suspense>
  );
}
