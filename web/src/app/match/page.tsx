'use client';

import React, { Suspense, useEffect, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Loader2 } from 'lucide-react';
import MatchDetailClient from '@/components/match/MatchDetailClient';

function MatchRouteResolver() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [resolvedId, setResolvedId] = useState<string | null>(null);
  const [isResolved, setIsResolved] = useState(false);

  useEffect(() => {
    let id = searchParams.get('id');
    if (!id && typeof window !== 'undefined') {
      const match = window.location.pathname.match(/\/match\/([^/?#]+)/);
      if (match && match[1] && match[1] !== 'index' && match[1] !== 'index.html') {
        id = match[1];
      }
    }

    if (id) {
      setResolvedId(id);
      setIsResolved(true);
    } else {
      router.replace('/fixtures');
    }
  }, [searchParams, router]);

  if (!isResolved || !resolvedId) {
    return (
      <div className="min-h-[60vh] flex flex-col items-center justify-center space-y-3">
        <Loader2 className="w-8 h-8 text-[#7098F8] animate-spin" />
        <span className="text-xs text-[#94A3B8]">Loading match centre...</span>
      </div>
    );
  }

  return (
    <MatchDetailClient
      initialMatch={null}
      expectedId={resolvedId}
    />
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
      <MatchRouteResolver />
    </Suspense>
  );
}
