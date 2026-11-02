import React, { useState } from 'react';
import {
  ChevronDown,
  ChevronUp,
  CheckCircle,
  Clock,
  AlertTriangle,
  ExternalLink,
  Edit3,
  Copy,
  Check,
  ShieldAlert,
} from 'lucide-react';
import { MatchWaterfall } from '../types';
import { CandidateCard } from './CandidateCard';
import { ManualOverrideModal } from './ManualOverrideModal';

interface WaterfallCardProps {
  waterfall: MatchWaterfall;
  onOverrideSuccess?: () => void;
}

export function WaterfallCard({ waterfall, onOverrideSuccess }: WaterfallCardProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [isOverrideOpen, setIsOverrideOpen] = useState(false);
  const [copied, setCopied] = useState(false);

  const isLinked = waterfall.status === 'LINKED';
  const isCooldown = waterfall.status === 'COOLDOWN';

  const handleCopyCardJson = (e: React.MouseEvent) => {
    e.stopPropagation();
    navigator.clipboard.writeText(JSON.stringify(waterfall, null, 2));
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const statusBadge = isLinked ? (
    <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
      <CheckCircle className="w-3.5 h-3.5" />
      LINKED
    </span>
  ) : isCooldown ? (
    <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-amber-500/15 text-amber-400 border border-amber-500/30">
      <Clock className="w-3.5 h-3.5" />
      COOLDOWN
    </span>
  ) : (
    <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold bg-slate-500/15 text-slate-400 border border-slate-500/30">
      <AlertTriangle className="w-3.5 h-3.5" />
      {waterfall.status}
    </span>
  );

  return (
    <div className="bg-slate-900/80 border border-slate-800 rounded-xl overflow-hidden transition-all duration-200">
      <div
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center justify-between p-4 cursor-pointer hover:bg-slate-800/40 select-none transition"
      >
        <div className="flex flex-col gap-1 min-w-0 pr-3">
          <h4 className="text-base font-bold text-slate-100 truncate">
            {waterfall.title}
          </h4>
          <p className="text-xs font-mono text-slate-400">
            Match ID: {waterfall.matchId} {waterfall.date ? `· ${waterfall.date}` : ''}
          </p>
        </div>

        <div className="flex items-center gap-2.5 shrink-0">
          <button
            onClick={handleCopyCardJson}
            title="Copy this match's trace JSON"
            className="p-1.5 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800 transition border border-slate-800/80"
          >
            {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
          </button>
          {statusBadge}
          {isOpen ? <ChevronUp className="w-5 h-5 text-slate-400" /> : <ChevronDown className="w-5 h-5 text-slate-400" />}
        </div>
      </div>

      {isOpen && (
        <div className="px-4 pb-4 pt-1 border-t border-slate-800/80 space-y-3.5 bg-slate-950/40">
          {/* Tier Cooldown Guardrail Card: Isolated on Cooldown */}
          {isCooldown ? (
            <div className="bg-amber-950/30 border border-amber-800/50 rounded-xl p-3.5 space-y-2 text-xs">
              <div className="flex items-center justify-between gap-2">
                <div className="flex items-center gap-2 text-amber-400 font-semibold uppercase tracking-wide text-[11px]">
                  <ShieldAlert className="w-4 h-4 shrink-0" />
                  <span>Tier Cooldown Guardrail Active</span>
                </div>
                {waterfall.cooldownRemainingMinutes !== undefined && (
                  <span className="px-2 py-0.5 rounded bg-amber-500/20 text-amber-300 font-mono text-[11px] font-bold border border-amber-500/30">
                    ~{waterfall.cooldownRemainingMinutes}m cooldown remaining
                  </span>
                )}
              </div>
              <p className="text-slate-300 text-[12px] leading-relaxed">
                {waterfall.cooldownReason || 'YouTube search bypassed to conserve daily API quota.'} Zero candidate videos were queried or AI-evaluated because this match is currently embargoed or awaiting its next scheduled cycle.
              </p>
              <div className="pt-1 flex items-center justify-end">
                <button
                  onClick={() => setIsOverrideOpen(true)}
                  className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-amber-600 hover:bg-amber-500 text-white rounded-lg text-xs font-semibold shadow-sm transition"
                >
                  <Edit3 className="w-3.5 h-3.5" />
                  <span>Manual Override / Force Search</span>
                </button>
              </div>
            </div>
          ) : (
            <>
              {/* Step 1: Official Broadcaster Targets */}
              <div className="text-xs space-y-1">
                <div className="flex items-center justify-between">
                  <p className="font-semibold text-slate-300 uppercase tracking-wide text-[11px] flex items-center gap-1.5">
                    <span className="w-4 h-4 rounded-full bg-slate-800 inline-flex items-center justify-center text-[10px] text-blue-400 font-mono">1</span>
                    Official Target Channels
                  </p>
                  {waterfall.stoppedAtStep === 1 && (
                    <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-500/15 text-amber-300 border border-amber-500/30">
                      ⚠️ Stopped at Step 1
                    </span>
                  )}
                </div>
                {waterfall.stoppedAtStep === 1 && waterfall.stoppedReason && (
                  <div className="p-2 rounded bg-amber-950/40 border border-amber-800/40 text-amber-200 text-xs font-medium">
                    {waterfall.stoppedReason}
                  </div>
                )}
                <div className="bg-slate-900/90 rounded p-2 text-slate-400 font-mono space-y-0.5 border border-slate-800">
                  {waterfall.step1Channels.length > 0 ? (
                    waterfall.step1Channels.slice(0, 4).map((ch, idx) => <p key={idx} className="truncate">{ch}</p>)
                  ) : (
                    <p className="text-slate-500 italic">Target channels awaiting sweep</p>
                  )}
                </div>
              </div>

              {/* Step 2: Candidates Discovered */}
              <div className="text-xs space-y-1">
                <div className="flex items-center justify-between">
                  <p className="font-semibold text-slate-300 uppercase tracking-wide text-[11px] flex items-center gap-1.5">
                    <span className="w-4 h-4 rounded-full bg-slate-800 inline-flex items-center justify-center text-[10px] text-indigo-400 font-mono">2</span>
                    Candidate Discovery ({waterfall.step2Candidates.length} detected)
                  </p>
                  {waterfall.stoppedAtStep === 2 && (
                    <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-500/15 text-amber-300 border border-amber-500/30">
                      ⚠️ Stopped at Step 2
                    </span>
                  )}
                </div>
                {waterfall.stoppedAtStep === 2 && waterfall.stoppedReason && (
                  <div className="p-2 rounded bg-amber-950/40 border border-amber-800/40 text-amber-200 text-xs font-medium">
                    {waterfall.stoppedReason}
                  </div>
                )}
                {waterfall.step2Candidates.length > 0 ? (
                  <div className="bg-slate-900/90 rounded p-2 text-slate-400 font-mono space-y-1 border border-slate-800 max-h-32 overflow-y-auto">
                    {waterfall.step2Candidates.map((cand, idx) => (
                      <p key={idx} className="truncate">{cand}</p>
                    ))}
                  </div>
                ) : (
                  <p className="text-slate-500 italic pl-5">
                    Zero preliminary uploads detected within match kickoff window
                  </p>
                )}
              </div>

              {/* Step 3: AI / Reranking Evaluation */}
              <div className="text-xs space-y-1.5">
                <div className="flex items-center justify-between">
                  <p className="font-semibold text-slate-200 uppercase tracking-wide text-[11px] flex items-center gap-1.5">
                    <span className="w-4 h-4 rounded-full bg-slate-800 inline-flex items-center justify-center text-[10px] text-emerald-400 font-mono">3</span>
                    AI Reranking Verdicts ({waterfall.step3Verdicts.length} evaluated)
                  </p>
                  {waterfall.stoppedAtStep === 3 && (
                    <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-500/15 text-amber-300 border border-amber-500/30">
                      ⚠️ Stopped at Step 3
                    </span>
                  )}
                </div>
                {waterfall.stoppedAtStep === 3 && waterfall.stoppedReason && (
                  <div className="p-2 rounded bg-amber-950/40 border border-amber-800/40 text-amber-200 text-xs font-medium">
                    {waterfall.stoppedReason}
                  </div>
                )}
                {waterfall.step3Verdicts.length > 0 ? (
                  <div className="space-y-1">
                    {waterfall.step3Verdicts.map((verdict, idx) => (
                      <CandidateCard key={idx} verdict={verdict} />
                    ))}
                  </div>
                ) : (
                  <p className="text-slate-500 italic pl-5">
                    No candidate videos submitted for evaluation
                  </p>
                )}
              </div>

              {/* Step 4: Outcome & Action Bar */}
              <div className="text-xs space-y-1 pt-1 border-t border-slate-800/80">
                <div className="flex items-center justify-between">
                  <p className="font-semibold text-slate-300 uppercase tracking-wide text-[11px] flex items-center gap-1.5">
                    <span className="w-4 h-4 rounded-full bg-slate-800 inline-flex items-center justify-center text-[10px] text-purple-400 font-mono">4</span>
                    Final Outcome & Video Linking
                  </p>
                  {waterfall.stoppedAtStep === 4 && (
                    <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-rose-500/15 text-rose-300 border border-rose-500/30">
                      🚫 Blocked at Step 4
                    </span>
                  )}
                </div>
                {waterfall.stoppedAtStep === 4 && waterfall.stoppedReason && (
                  <div className="p-2 rounded bg-rose-950/40 border border-rose-800/40 text-rose-200 text-xs font-medium">
                    {waterfall.stoppedReason}
                  </div>
                )}
                <div className="bg-slate-900/90 rounded p-3 flex flex-col sm:flex-row sm:items-center justify-between gap-3 border border-slate-800">
                  <div className="space-y-0.5">
                    <p className="font-mono text-slate-200 text-xs">{waterfall.step4Outcome}</p>
                    {waterfall.outcomeVideoId && (
                      <p className="text-slate-400 font-mono text-[11px]">YouTube ID: {waterfall.outcomeVideoId}</p>
                    )}
                  </div>

                  <div className="flex items-center gap-2">
                    {waterfall.outcomeVideoUrl && (
                      <a
                        href={waterfall.outcomeVideoUrl}
                        target="_blank"
                        rel="noreferrer"
                        className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-indigo-600/20 text-indigo-400 hover:bg-indigo-600/30 border border-indigo-500/30 rounded-lg text-xs font-semibold transition"
                      >
                        <ExternalLink className="w-3.5 h-3.5" />
                        <span>Watch Highlight</span>
                      </a>
                    )}

                    <button
                      onClick={() => setIsOverrideOpen(true)}
                      className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-slate-800 text-slate-300 hover:text-white hover:bg-slate-700 border border-slate-700 rounded-lg text-xs font-semibold transition"
                    >
                      <Edit3 className="w-3.5 h-3.5" />
                      <span>{isLinked ? 'Change Video' : 'Manual Link'}</span>
                    </button>
                  </div>
                </div>
              </div>
            </>
          )}
        </div>
      )}

      {isOverrideOpen && (
        <ManualOverrideModal
          isOpen={isOverrideOpen}
          matchId={waterfall.matchId}
          matchTitle={waterfall.title}
          onClose={() => setIsOverrideOpen(false)}
          onSuccess={() => {
            setIsOverrideOpen(false);
            if (onOverrideSuccess) onOverrideSuccess();
          }}
        />
      )}
    </div>
  );
}
