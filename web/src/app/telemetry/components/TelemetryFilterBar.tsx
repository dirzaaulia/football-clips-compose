import React from 'react';
import { Search, Filter, Layers, List } from 'lucide-react';

interface TelemetryFilterBarProps {
  searchQuery: string;
  onSearchChange: (q: string) => void;
  viewMode: 'grouped' | 'flat';
  onViewModeChange: (m: 'grouped' | 'flat') => void;
  statusFilter: 'ALL' | 'LINKED' | 'COOLDOWN' | 'UNLINKED';
  onStatusFilterChange: (st: 'ALL' | 'LINKED' | 'COOLDOWN' | 'UNLINKED') => void;
}

export function TelemetryFilterBar({
  searchQuery,
  onSearchChange,
  viewMode,
  onViewModeChange,
  statusFilter,
  onStatusFilterChange,
}: TelemetryFilterBarProps) {
  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 flex flex-col sm:flex-row gap-3 items-stretch sm:items-center justify-between">
      <div className="relative flex-1">
        <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
        <input
          type="text"
          placeholder="Search match or team (e.g. Arsenal, Real Madrid, 401912257)..."
          value={searchQuery}
          onChange={(e) => onSearchChange(e.target.value)}
          className="w-full bg-slate-950 border border-slate-800 rounded-lg pl-9 pr-3 py-2 text-xs font-mono text-slate-200 placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition"
        />
      </div>

      <div className="flex items-center gap-2 overflow-x-auto">
        <div className="flex items-center bg-slate-950 border border-slate-800 rounded-lg p-0.5 mr-1">
          <button
            onClick={() => onViewModeChange('grouped')}
            className={`p-1.5 rounded text-xs font-mono flex items-center gap-1 transition ${
              viewMode === 'grouped' ? 'bg-indigo-600 text-white' : 'text-slate-400 hover:text-slate-200'
            }`}
            title="Grouped by Week & Priority"
          >
            <Layers className="w-3.5 h-3.5" />
            <span className="hidden sm:inline">Grouped</span>
          </button>
          <button
            onClick={() => onViewModeChange('flat')}
            className={`p-1.5 rounded text-xs font-mono flex items-center gap-1 transition ${
              viewMode === 'flat' ? 'bg-indigo-600 text-white' : 'text-slate-400 hover:text-slate-200'
            }`}
            title="Flat List View"
          >
            <List className="w-3.5 h-3.5" />
            <span className="hidden sm:inline">Flat</span>
          </button>
        </div>

        <Filter className="w-3.5 h-3.5 text-slate-400 shrink-0 ml-1" />
        {(['ALL', 'LINKED', 'COOLDOWN', 'UNLINKED'] as const).map((st) => (
          <button
            key={st}
            onClick={() => onStatusFilterChange(st)}
            className={`px-3 py-1.5 rounded-lg text-xs font-mono font-semibold transition shrink-0 ${
              statusFilter === st
                ? 'bg-indigo-600 text-white shadow-sm'
                : 'bg-slate-800 text-slate-400 hover:text-slate-200 hover:bg-slate-700'
            }`}
          >
            {st}
          </button>
        ))}
      </div>
    </div>
  );
}
