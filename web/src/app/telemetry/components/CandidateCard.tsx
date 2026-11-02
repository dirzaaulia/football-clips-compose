'use client';

import React from 'react';
import { ExternalLink, CheckCircle2, XCircle } from 'lucide-react';
import { CandidateVerdict } from '../types';

interface CandidateCardProps {
  verdict: CandidateVerdict;
}

export function CandidateCard({ verdict }: CandidateCardProps) {
  const isAccepted = verdict.isAccepted;
  const borderColor = isAccepted ? 'border-emerald-500/40 bg-emerald-950/20' : 'border-rose-500/30 bg-rose-950/15';
  const badgeColor = isAccepted ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30' : 'bg-rose-500/20 text-rose-400 border-rose-500/30';

  return (
    <div className={`flex flex-col sm:flex-row sm:items-center justify-between p-3 rounded-lg border ${borderColor} transition-all duration-150 gap-2.5 my-1.5`}>
      <div className="flex flex-col gap-1 min-w-0 flex-1">
        <div className="flex items-center gap-2 flex-wrap">
          <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider border ${badgeColor}`}>
            {isAccepted ? <CheckCircle2 className="w-3 h-3" /> : <XCircle className="w-3 h-3" />}
            {isAccepted ? 'Accepted' : 'Rejected'}
          </span>
          <span className="text-xs font-mono font-semibold text-slate-300">
            Candidate #{verdict.index}: {verdict.channel}
          </span>
        </div>

        <p className="text-sm font-medium text-slate-100 truncate">
          &apos;{verdict.title}&apos;
        </p>

        {verdict.verdictReason && (
          <p className="text-xs text-slate-400 font-mono">
            Verdict: <span className="text-slate-300">{verdict.verdictReason}</span>
          </p>
        )}
      </div>

      {verdict.url && (
        <a
          href={verdict.url}
          target="_blank"
          rel="noopener noreferrer"
          className="inline-flex items-center gap-1.5 px-2.5 py-1.5 rounded-md text-xs font-medium text-slate-300 bg-slate-800 hover:bg-slate-700 hover:text-white border border-slate-700 transition self-start sm:self-center shrink-0"
        >
          <ExternalLink className="w-3.5 h-3.5 text-blue-400" />
          <span>Open Video</span>
        </a>
      )}
    </div>
  );
}
