import React, { useState } from 'react';
import {
  Calendar,
  Copy,
  Check,
  ChevronDown,
  ChevronUp,
} from 'lucide-react';

export interface FixtureItem {
  id: string;
  home: string;
  away: string;
  competition: string;
  utc_date?: string;
  status: string;
  round?: string | number;
}

export interface GroupedCompetitionFixtures {
  competition: string;
  fixtures: FixtureItem[];
}

interface CompetitionFixtureCardProps {
  group: GroupedCompetitionFixtures;
}

export function CompetitionFixtureCard({ group }: CompetitionFixtureCardProps) {
  const [expanded, setExpanded] = useState(true);
  const [copiedComp, setCopiedComp] = useState(false);
  const [copiedFixtureId, setCopiedFixtureId] = useState<string | null>(null);

  const handleCopyComp = (e: React.MouseEvent) => {
    e.stopPropagation();
    navigator.clipboard.writeText(JSON.stringify(group, null, 2));
    setCopiedComp(true);
    setTimeout(() => setCopiedComp(false), 2000);
  };

  const handleCopyFixture = (fix: FixtureItem) => {
    navigator.clipboard.writeText(JSON.stringify(fix, null, 2));
    setCopiedFixtureId(fix.id);
    setTimeout(() => setCopiedFixtureId(null), 2000);
  };

  const formatWib = (utcDate?: string) => {
    if (!utcDate) return 'TBD WIB';
    try {
      const d = new Date(utcDate);
      if (isNaN(d.getTime())) return 'TBD WIB';
      const datePart = d.toLocaleDateString('en-GB', {
        timeZone: 'Asia/Jakarta',
        weekday: 'short',
        day: '2-digit',
        month: 'short',
      });
      const timePart = d.toLocaleTimeString('id-ID', {
        timeZone: 'Asia/Jakarta',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
      });
      return `${datePart}, ${timePart} WIB`;
    } catch {
      return 'TBD WIB';
    }
  };

  return (
    <div className="bg-slate-900/90 border border-slate-800 rounded-xl overflow-hidden shadow-sm hover:border-slate-700 transition">
      {/* Competition Header */}
      <div
        onClick={() => setExpanded(!expanded)}
        className="p-4 cursor-pointer flex items-center justify-between gap-3 bg-slate-900/60 hover:bg-slate-800/40 select-none"
      >
        <div className="flex items-center gap-3 min-w-0">
          <Calendar className="w-5 h-5 text-indigo-400 shrink-0" />
          <div>
            <h4 className="text-sm font-bold text-white truncate">{group.competition}</h4>
            <p className="text-[11px] font-mono text-slate-400">
              {group.fixtures.length} upcoming match{group.fixtures.length === 1 ? '' : 'es'} ingested
            </p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <span className="px-2.5 py-0.5 rounded-full text-xs font-mono font-bold bg-indigo-500/15 text-indigo-400 border border-indigo-500/30">
            {group.fixtures.length} Fixtures
          </span>

          <button
            onClick={handleCopyComp}
            title="Copy competition fixtures JSON"
            className="p-1.5 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800 border border-slate-800"
          >
            {copiedComp ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
          </button>

          <div className="text-slate-400">
            {expanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
          </div>
        </div>
      </div>

      {/* Fixtures List */}
      {expanded && (
        <div className="p-4 pt-1 border-t border-slate-800/80 bg-slate-950/40 divide-y divide-slate-800/60">
          {group.fixtures.map((fix) => (
            <div
              key={fix.id}
              className="py-2.5 flex flex-wrap items-center justify-between gap-2 text-xs font-mono first:pt-2 last:pb-1"
            >
              <div className="min-w-0 space-y-0.5">
                <p className="text-slate-200 font-bold">
                  {fix.home} <span className="text-slate-400 font-normal">vs</span> {fix.away}
                </p>
                <div className="flex items-center gap-2 text-[11px] text-slate-400">
                  <span className="text-indigo-400">{formatWib(fix.utc_date)}</span>
                  {fix.round && <span>· Round {fix.round}</span>}
                  <span>· #{fix.id}</span>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-slate-800 text-slate-300 border border-slate-700">
                  {fix.status}
                </span>

                <button
                  onClick={() => handleCopyFixture(fix)}
                  title="Copy fixture JSON"
                  className="p-1 rounded text-slate-400 hover:text-slate-200 hover:bg-slate-800"
                >
                  {copiedFixtureId === fix.id ? (
                    <Check className="w-3 h-3 text-emerald-400" />
                  ) : (
                    <Copy className="w-3 h-3" />
                  )}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
