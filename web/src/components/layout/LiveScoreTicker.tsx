'use client';

import React, { useEffect, useState, useMemo, useCallback } from 'react';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';
import { Clock, CheckCircle2 } from 'lucide-react';
import TickerMarqueeRow, {
  TickerMatchItem,
  TickerMode,
} from '@/components/layout/TickerMarqueeRow';

export default function LiveScoreTicker() {
  const [matches, setMatches] = useState<TickerMatchItem[]>([]);
  const [mode, setMode] = useState<TickerMode>('RECENT');
  const [loading, setLoading] = useState(true);
  const [selectedComp] = useState<string>('ALL');
  const [unblurredIds, setUnblurredIds] = useState<Set<number>>(new Set());
  const { isSpoilerFree } = useSpoilerFree();

  const toggleUnblur = useCallback((id: number, e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setUnblurredIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }, []);

  const fetchTickerData = useCallback(async () => {
    try {
      const liveRes = await fetch(
        'https://edge.fc.dirzaaulia.com/rest/v1/matches?status=in.(IN_PLAY,PAUSED,LIVE)&order=utc_date.asc&limit=15',
        { cache: 'no-store' }
      );
      if (liveRes.ok) {
        const liveData = await liveRes.json();
        if (Array.isArray(liveData) && liveData.length > 0) {
          setMatches(liveData);
          setMode('LIVE');
          setLoading(false);
          return;
        }
      }

      const now = new Date();
      const startOfDay = new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), now.getUTCDate(), 0, 0, 0)).toISOString();
      const endOfDay = new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), now.getUTCDate(), 23, 59, 59)).toISOString();

      const todayRes = await fetch(
        `https://edge.fc.dirzaaulia.com/rest/v1/matches?status=in.(TIMED,SCHEDULED)&utc_date=gte.${startOfDay}&utc_date=lte.${endOfDay}&order=utc_date.asc&limit=15`,
        { cache: 'no-store' }
      );
      if (todayRes.ok) {
        const todayData = await todayRes.json();
        if (Array.isArray(todayData) && todayData.length > 0) {
          setMatches(todayData);
          setMode('UPCOMING');
          setLoading(false);
          return;
        }
      }

      const recentRes = await fetch(
        'https://edge.fc.dirzaaulia.com/rest/v1/matches?status=in.(FINISHED,FT)&order=utc_date.desc&limit=15',
        { cache: 'no-store' }
      );
      if (recentRes.ok) {
        const recentData = await recentRes.json();
        if (Array.isArray(recentData) && recentData.length > 0) {
          setMatches(recentData);
          setMode('RECENT');
          setLoading(false);
          return;
        }
      }
    } catch {
      // Keep existing state on transient edge errors
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchTickerData();
    const timer = setInterval(fetchTickerData, 30_000);
    return () => clearInterval(timer);
  }, [fetchTickerData]);

  const filteredMatches = useMemo(() => {
    if (selectedComp === 'ALL') return matches;
    return matches.filter((m) => m.competition_id === selectedComp);
  }, [matches, selectedComp]);

  const marqueeMatches = useMemo(() => {
    if (filteredMatches.length === 0) return [];
    let result = [...filteredMatches];
    while (result.length < 8) {
      result = [...result, ...filteredMatches];
    }
    return result;
  }, [filteredMatches]);

  const marqueeDuration = useMemo(() => {
    return Math.max(25, Math.round(marqueeMatches.length * 4.5));
  }, [marqueeMatches.length]);

  if (loading && matches.length === 0) return null;
  if (filteredMatches.length === 0) return null;

  return (
    <div className="sticky top-12 z-40 bg-[#0A0A0C]/95 backdrop-blur-md border-b border-white/10 select-none overflow-hidden h-9">
      <div className="h-full max-w-[1800px] mx-auto px-2 sm:px-4 flex items-center gap-3">
        {/* Pinned Left Mode Pill */}
        <div className="shrink-0 flex items-center gap-2">
          {mode === 'LIVE' ? (
            <div className="flex items-center gap-1.5 bg-[#FF2E4D]/15 border border-[#FF2E4D]/40 px-2.5 py-0.5 rounded-full shadow-sm">
              <span className="relative flex h-2 w-2">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-[#FF2E4D] opacity-75" />
                <span className="relative inline-flex rounded-full h-2 w-2 bg-[#FF2E4D]" />
              </span>
              <span className="text-[10px] font-tech font-black tracking-wider text-[#FF2E4D] uppercase">
                LIVE ({matches.length})
              </span>
            </div>
          ) : mode === 'UPCOMING' ? (
            <div className="flex items-center gap-1.5 bg-[#DFFF00]/15 border border-[#DFFF00]/40 px-2.5 py-0.5 rounded-full shadow-sm">
              <Clock className="w-3 h-3 text-[#DFFF00]" />
              <span className="text-[10px] font-tech font-black tracking-wider text-[#DFFF00] uppercase">
                TODAY&apos;S MATCHES ({matches.length})
              </span>
            </div>
          ) : (
            <div className="flex items-center gap-1.5 bg-sky-500/15 border border-sky-500/40 px-2.5 py-0.5 rounded-full shadow-sm">
              <CheckCircle2 className="w-3 h-3 text-sky-400" />
              <span className="text-[10px] font-tech font-black tracking-wider text-sky-400 uppercase">
                LATEST RESULTS
              </span>
            </div>
          )}
        </div>

        {/* Marquee Track Container with Fade Masks */}
        <div className="group flex items-center overflow-hidden flex-1 relative [mask-image:linear-gradient(to_right,transparent,black_15px,black_calc(100%-15px),transparent)]">
          <TickerMarqueeRow
            matches={marqueeMatches}
            mode={mode}
            duration={marqueeDuration}
            isSpoilerFree={isSpoilerFree}
            unblurredIds={unblurredIds}
            onToggleUnblur={toggleUnblur}
          />
          <TickerMarqueeRow
            matches={marqueeMatches}
            mode={mode}
            duration={marqueeDuration}
            isSpoilerFree={isSpoilerFree}
            unblurredIds={unblurredIds}
            onToggleUnblur={toggleUnblur}
            ariaHidden
          />
        </div>
      </div>
    </div>
  );
}
