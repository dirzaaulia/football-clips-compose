'use client';

import React, { useState, useMemo } from 'react';
import { ArrowLeftRight } from 'lucide-react';
import { MatchEvent } from '@/lib/database';

export function GoalBallIcon({ className = 'w-3.5 h-3.5' }: { className?: string }) {
  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <circle cx="12" cy="12" r="10" className="stroke-[#DFFF00] fill-[#DFFF00]/20" />
      <polygon points="12 6 15 9 14 13 10 13 9 9 12 6" className="fill-[#DFFF00] stroke-none" />
      <path
        d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"
        className="stroke-[#DFFF00]"
      />
    </svg>
  );
}

export function parseMinute(clock?: string): number {
  if (!clock) return 45;
  const clean = clock.replace(/'/g, '').trim();
  if (clean.includes('+')) {
    const parts = clean.split('+');
    return (parseInt(parts[0], 10) || 45) + (parseInt(parts[1], 10) || 1);
  }
  return Math.min(Math.max(parseInt(clean, 10) || 1, 1), 120);
}

interface ScrubberNode {
  id: string;
  minute: number;
  pct: number;
  origPct: number;
  tier: 0 | 1;
  side: 'home' | 'away';
  kind: 'goal' | 'red' | 'yellow' | 'sub';
  clockLabel: string;
  playerName: string;
  assistName?: string | null;
  teamName: string;
  typeLabel: string;
}

function resolveEventKind(e: MatchEvent): 'goal' | 'red' | 'yellow' | 'sub' | null {
  const t = (e.type || e.type_text || '').toLowerCase();
  const txt = (e.text || '').toLowerCase();
  if (
    e.scoring_play ||
    ((t.includes('goal') || txt.startsWith('goal!')) && !t.includes('kick') && !t.includes('attempt'))
  ) {
    return 'goal';
  }
  if (t.includes('red') || txt.includes('red card') || txt.includes('second yellow')) return 'red';
  if (t.includes('yellow') || txt.includes('yellow card')) return 'yellow';
  if (t.includes('sub')) return 'sub';
  return null;
}

export function extractScorerDisplayName(e: MatchEvent): string {
  const p = (e.player || '').trim();
  if (p && p.length <= 34 && !p.toLowerCase().startsWith('goal!')) return p;
  const raw = String(e.text || p || '');
  const m = raw.match(/\.\s*([A-ZÀ-Ž][a-zA-ZÀ-ž'.\-\s]{1,30}?)\s*\(/);
  if (m?.[1]) return m[1].trim();
  const m2 = raw.match(/^([A-ZÀ-Ž][a-zA-ZÀ-ž'.\-\s]{1,30}?)\s*\(/);
  if (m2?.[1]) return m2[1].trim();
  return 'Scorer';
}

interface MatchScrubberStripProps {
  events: MatchEvent[];
  homeName: string;
  awayName: string;
}

export default function MatchScrubberStrip({
  events,
  homeName,
  awayName,
}: MatchScrubberStripProps) {
  const nodes = useMemo<ScrubberNode[]>(() => {
    const raw: ScrubberNode[] = [];
    events.forEach((e, idx) => {
      const kind = resolveEventKind(e);
      if (!kind || kind === 'sub') return;
      const minute = parseMinute(e.clock);
      const side: 'home' | 'away' = e.side === 'away' ? 'away' : 'home';
      const pct = Math.min(Math.max((minute / 95) * 100, 4), 96);
      raw.push({
        id: e.id || `sc-${idx}`,
        minute,
        pct,
        origPct: pct,
        tier: 0,
        side,
        kind,
        clockLabel: e.clock || `${minute}'`,
        playerName: extractScorerDisplayName(e),
        assistName: e.assist,
        teamName: side === 'home' ? homeName : awayName,
        typeLabel: kind === 'goal' ? 'Goal' : kind === 'red' ? 'Red Card' : 'Yellow Card',
      });
    });

    (['home', 'away'] as const).forEach((lane) => {
      const laneNodes = raw.filter((n) => n.side === lane).sort((a, b) => a.minute - b.minute);
      for (let i = 0; i < laneNodes.length; i++) {
        if (i > 0) {
          const prev = laneNodes[i - 1];
          const dist = Math.abs(laneNodes[i].pct - prev.pct);
          if (dist < 6.8) {
            laneNodes[i].tier = prev.tier === 0 ? 1 : 0;
            if (laneNodes[i].tier === prev.tier) {
              laneNodes[i].pct = Math.min(96, prev.pct + 4.5);
            }
          }
        }
      }
    });

    return raw.sort((a, b) => a.minute - b.minute);
  }, [events, homeName, awayName]);

  const [selectedId, setSelectedId] = useState<string | null>(null);
  const activeNode = useMemo(
    () => nodes.find((n) => n.id === selectedId) || nodes.find((n) => n.kind === 'goal') || nodes[0] || null,
    [nodes, selectedId],
  );

  if (nodes.length === 0) return null;

  return (
    <div className="pt-3 border-t border-white/10 space-y-2.5 select-none">
      <div className="flex items-center justify-between text-[10px] font-tech uppercase tracking-wider text-white/50">
        <div className="flex items-center gap-3">
          <span className="font-bold text-white/75">Key Moments Timeline</span>
          <span className="hidden sm:inline text-white/35">• {homeName} (Top) / {awayName} (Bottom)</span>
        </div>
        <div className="flex items-center gap-3">
          <span className="inline-flex items-center gap-1">
            <GoalBallIcon className="w-3 h-3" />
            <span>Goal</span>
          </span>
          <span className="inline-flex items-center gap-1">
            <span className="w-2 h-2.5 rounded-[1px] bg-amber-400 inline-block" />
            <span>YC</span>
          </span>
          <span className="inline-flex items-center gap-1">
            <span className="w-2 h-2.5 rounded-[1px] bg-rose-500 inline-block" />
            <span>RC</span>
          </span>
        </div>
      </div>

      {/* Dual-Lane Anti-Collision Timeline Track */}
      <div className="relative w-full h-20 bg-[#0A0A0C] border border-white/10 rounded-xl px-6 flex items-center shadow-inner">
        {/* Center Axis Line */}
        <div className="relative w-full h-1 bg-white/10 rounded-full">
          <span className="absolute -left-4 -top-2.5 text-[9px] font-mono text-white/40 font-bold">0&apos;</span>
          <div className="absolute left-1/2 -top-2.5 -translate-x-1/2 flex flex-col items-center z-0">
            <span className="px-1.5 py-0.5 rounded bg-[#16181D] border border-white/10 text-[8px] font-mono font-bold text-white/50">
              HT
            </span>
          </div>
          <span className="absolute -right-4 -top-2.5 text-[9px] font-mono text-white/40 font-bold">90&apos;+</span>

          {nodes.map((node) => {
            const isHome = node.side === 'home';
            const isSelected = activeNode?.id === node.id;
            const isTier1 = node.tier === 1;

            return (
              <div
                key={node.id}
                style={{ left: `${node.pct}%` }}
                className={`absolute -translate-x-1/2 flex flex-col items-center z-10 transition-transform ${
                  isHome
                    ? isTier1 ? '-top-11' : '-top-6'
                    : isTier1 ? 'top-3.5' : 'top-1'
                } ${isSelected ? 'scale-110 z-30' : 'hover:scale-105 opacity-90 hover:opacity-100'}`}
              >
                {!isHome && (
                  <span
                    className={`w-px transition-colors ${isSelected ? 'bg-[#DFFF00]' : 'bg-white/30'} ${
                      isTier1 ? 'h-3.5 mb-0.5' : 'h-1.5 mb-0.5'
                    }`}
                  />
                )}
                <button
                  type="button"
                  onClick={() => setSelectedId(node.id)}
                  onMouseEnter={() => setSelectedId(node.id)}
                  className={`flex items-center gap-1 px-1.5 py-0.5 rounded-full border text-[9px] font-mono font-bold shadow-md cursor-pointer transition-all ${
                    node.kind === 'goal'
                      ? 'bg-[#DFFF00]/15 border-[#DFFF00]/70 text-[#DFFF00]'
                      : node.kind === 'red'
                      ? 'bg-rose-500/20 border-rose-500/70 text-rose-300'
                      : 'bg-amber-400/15 border-amber-400/60 text-amber-300'
                  } ${isSelected ? 'ring-2 ring-white/40 shadow-lg' : ''}`}
                >
                  {node.kind === 'goal' ? (
                    <GoalBallIcon className="w-2.5 h-2.5 shrink-0" />
                  ) : node.kind === 'red' ? (
                    <span className="w-2 h-2.5 rounded-[1px] bg-rose-500 shrink-0" />
                  ) : node.kind === 'yellow' ? (
                    <span className="w-2 h-2.5 rounded-[1px] bg-amber-400 shrink-0" />
                  ) : (
                    <ArrowLeftRight className="w-2.5 h-2.5 text-[#38BDF8] shrink-0" />
                  )}
                  <span>{node.clockLabel}</span>
                </button>
                {isHome && (
                  <span
                    className={`w-px transition-colors ${isSelected ? 'bg-[#DFFF00]' : 'bg-white/30'} ${
                      isTier1 ? 'h-3.5 mt-0.5' : 'h-1.5 mt-0.5'
                    }`}
                  />
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Selected Event Inspection Strip */}
      {activeNode && (
        <div className="flex items-center justify-between gap-2 px-3 py-2 rounded-xl bg-[#0A0A0C] border border-white/10 text-xs font-tech">
          <div className="flex items-center gap-2 min-w-0">
            <span className="px-1.5 py-0.5 rounded bg-[#DFFF00]/15 text-[#DFFF00] font-mono text-[10px] font-bold shrink-0">
              {activeNode.clockLabel}
            </span>
            <span className="font-bold text-white truncate">{activeNode.playerName}</span>
            {activeNode.assistName && (
              <span className="text-white/50 text-[11px] truncate hidden sm:inline">
                (Assist: {activeNode.assistName})
              </span>
            )}
          </div>
          <div className="flex items-center gap-2 shrink-0 text-[10px]">
            <span className="text-white/50">{activeNode.teamName}</span>
            <span className="px-2 py-0.5 rounded bg-white/5 text-white/80 font-bold uppercase">
              {activeNode.typeLabel}
            </span>
          </div>
        </div>
      )}
    </div>
  );
}
