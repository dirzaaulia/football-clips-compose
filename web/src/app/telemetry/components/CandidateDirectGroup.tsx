'use client';

import React from 'react';
import { ExternalLink, ChevronDown, ChevronRight } from 'lucide-react';

interface DirectCandidateRowProps {
  cand: string;
}

export function DirectCandidateRow({ cand }: DirectCandidateRowProps) {
  const urlMatch = cand.match(/->\s*(https:\/\/youtu\.be\/[a-zA-Z0-9_-]{11})/);
  let displayLine = cand
    .replace(/^(\[[^\]]+\]\s*)+/, '')
    .replace(/\[(RSS|API)[^\]]*\]\s*/i, '')
    .trim();
  if (urlMatch) {
    displayLine = displayLine.replace(/->\s*https:\/\/youtu\.be\/[a-zA-Z0-9_-]{11}/, '').trim();
  }

  return (
    <div className="flex items-center justify-between gap-2 p-1.5 rounded bg-slate-950/60 hover:bg-slate-800/50 text-[11px] border border-slate-800/40">
      <span className="truncate text-slate-300 font-sans flex-1 min-w-0" title={displayLine}>
        {displayLine}
      </span>
      {urlMatch && (
        <a
          href={urlMatch[1]}
          target="_blank"
          rel="noopener noreferrer"
          className="inline-flex items-center gap-1 px-2 py-0.5 rounded bg-slate-800 text-blue-400 hover:text-blue-300 text-[10px] font-bold shrink-0 border border-slate-700 font-mono transition"
        >
          <span>Watch</span>
          <ExternalLink className="w-2.5 h-2.5" />
        </a>
      )}
    </div>
  );
}

interface DirectCandidatesAccordionItemProps {
  candidates: string[];
  isExpanded: boolean;
  onToggle: () => void;
}

export function DirectCandidatesAccordionItem({
  candidates,
  isExpanded,
  onToggle,
}: DirectCandidatesAccordionItemProps) {
  return (
    <div className="rounded-lg border border-slate-800/60 bg-slate-950/40 overflow-hidden">
      <button
        type="button"
        onClick={onToggle}
        className="w-full flex items-center justify-between gap-2 p-2 hover:bg-slate-800/50 transition cursor-pointer text-left"
      >
        <div className="flex items-center gap-1.5 font-sans font-medium text-slate-200">
          {isExpanded ? (
            <ChevronDown className="w-3.5 h-3.5 text-slate-400 shrink-0" />
          ) : (
            <ChevronRight className="w-3.5 h-3.5 text-slate-400 shrink-0" />
          )}
          <span className="text-[11px] font-mono text-purple-300 font-semibold">
            Direct Query Matches
          </span>
          <span className="text-slate-500 font-normal text-[11px]">
            ({candidates.length})
          </span>
        </div>
        <span className="text-[10px] font-mono text-slate-500">
          {isExpanded ? 'Hide' : 'Show'}
        </span>
      </button>

      {isExpanded && (
        <div className="p-2 pt-0 space-y-1 border-t border-slate-800/40 mt-1">
          {candidates.map((cand, idx) => (
            <DirectCandidateRow key={idx} cand={cand} />
          ))}
        </div>
      )}
    </div>
  );
}
