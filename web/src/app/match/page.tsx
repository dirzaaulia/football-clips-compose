'use client';

import React from 'react';
import MatchDetailClient from '@/components/match/MatchDetailClient';

export default function GenericMatchFallbackPage() {
  return (
    <MatchDetailClient
      initialMatch={null}
      initialRecap={null}
      initialRelated={{ highlights: [], fixtures: [] }}
    />
  );
}
