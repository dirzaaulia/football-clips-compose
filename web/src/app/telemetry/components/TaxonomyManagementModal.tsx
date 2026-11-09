'use client';

import React, { useState, useEffect } from 'react';
import { X, Search, Plus, Trash2, BookOpen, Check, AlertCircle, Loader2 } from 'lucide-react';
import { database } from '@/lib/database';

interface TaxonomyEntry {
  id: string;
  category: string;
  language: string;
  terms: string[];
  rejection_label?: string;
  override_if_positive_highlight?: boolean;
}

interface TaxonomyManagementModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export function TaxonomyManagementModal({ isOpen, onClose }: TaxonomyManagementModalProps) {
  const [entries, setEntries] = useState<TaxonomyEntry[]>([]);
  const [search, setSearch] = useState('');
  const [selectedEntryId, setSelectedEntryId] = useState<string>('interview_global');
  const [newTerm, setNewTerm] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [statusMsg, setStatusMsg] = useState<{ text: string; error?: boolean } | null>(null);

  useEffect(() => {
    if (!isOpen) return;
    async function loadTaxonomy() {
      setIsLoading(true);
      try {
        const { data, error } = await database
          .from('highlight_taxonomy')
          .select('id, category, language, terms, rejection_label, override_if_positive_highlight')
          .order('category', { ascending: true });

        if (!error && data) {
          setEntries(data as TaxonomyEntry[]);
          if (data.length > 0 && !data.some((d) => d.id === selectedEntryId)) {
            setSelectedEntryId(data[0].id);
          }
        }
      } catch (err) {
        console.error('Failed to load taxonomy:', err);
      } finally {
        setIsLoading(false);
      }
    }
    loadTaxonomy();
  }, [isOpen]);

  if (!isOpen) return null;

  const currentEntry = entries.find((e) => e.id === selectedEntryId);

  const handleAddTerm = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTerm.trim() || !currentEntry) return;

    const termClean = newTerm.toLowerCase().trim();
    if (currentEntry.terms.includes(termClean)) {
      setStatusMsg({ text: `"${termClean}" already exists in this category`, error: true });
      return;
    }

    const updatedTerms = [...currentEntry.terms, termClean];
    setIsSaving(true);
    try {
      const { error } = await database
        .from('highlight_taxonomy')
        .update({ terms: updatedTerms })
        .eq('id', currentEntry.id);

      if (error) throw new Error(error.message);

      setEntries((prev) =>
        prev.map((e) => (e.id === currentEntry.id ? { ...e, terms: updatedTerms } : e))
      );
      setNewTerm('');
      setStatusMsg({ text: `Added "${termClean}" successfully` });
      setTimeout(() => setStatusMsg(null), 3000);
    } catch (err: any) {
      setStatusMsg({ text: err.message || 'Failed to add term', error: true });
    } finally {
      setIsSaving(false);
    }
  };

  const handleRemoveTerm = async (termToRemove: string) => {
    if (!currentEntry) return;

    const updatedTerms = currentEntry.terms.filter((t) => t !== termToRemove);
    setIsSaving(true);
    try {
      const { error } = await database
        .from('highlight_taxonomy')
        .update({ terms: updatedTerms })
        .eq('id', currentEntry.id);

      if (error) throw new Error(error.message);

      setEntries((prev) =>
        prev.map((e) => (e.id === currentEntry.id ? { ...e, terms: updatedTerms } : e))
      );
      setStatusMsg({ text: `Removed "${termToRemove}"` });
      setTimeout(() => setStatusMsg(null), 2500);
    } catch (err: any) {
      setStatusMsg({ text: err.message || 'Failed to remove term', error: true });
    } finally {
      setIsSaving(false);
    }
  };

  const filteredTerms = (currentEntry?.terms || []).filter((t) =>
    t.toLowerCase().includes(search.toLowerCase().trim())
  );

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-4xl max-h-[85vh] flex flex-col shadow-2xl overflow-hidden font-sans">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-slate-800 bg-slate-900/90">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-indigo-500/15 text-indigo-400 border border-indigo-500/30 flex items-center justify-center">
              <BookOpen className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-base font-bold text-white flex items-center gap-2">
                Classification Taxonomy Registry
                <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 uppercase">
                  Dynamic DB
                </span>
              </h3>
              <p className="text-xs text-slate-400 font-mono">
                PostgreSQL public.highlight_taxonomy · Go Worker Hot-Reload Active
              </p>
            </div>
          </div>
          <button onClick={onClose} className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content Body */}
        <div className="flex-1 flex flex-col md:flex-row min-h-0 overflow-hidden">
          {/* Categories Sidebar */}
          <div className="w-full md:w-64 border-b md:border-b-0 md:border-r border-slate-800 bg-slate-950/40 p-3 overflow-y-auto space-y-1">
            <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider px-2 font-mono">
              Taxonomy Categories ({entries.length})
            </span>
            {isLoading ? (
              <div className="p-4 text-center text-xs text-slate-500 flex items-center justify-center gap-2">
                <Loader2 className="w-4 h-4 animate-spin text-indigo-400" />
                Loading rules...
              </div>
            ) : (
              entries.map((entry) => (
                <button
                  key={entry.id}
                  onClick={() => setSelectedEntryId(entry.id)}
                  className={`w-full text-left px-2.5 py-1.5 rounded-lg text-xs font-mono transition flex items-center justify-between ${
                    selectedEntryId === entry.id
                      ? 'bg-indigo-600/20 text-indigo-300 border border-indigo-500/40 font-semibold'
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
                  }`}
                >
                  <span className="truncate">{entry.category}</span>
                  <span className="text-[10px] px-1.5 py-0.2 rounded bg-slate-800 text-slate-400">
                    {entry.terms.length}
                  </span>
                </button>
              ))
            )}
          </div>

          {/* Active Category Terms Pane */}
          <div className="flex-1 flex flex-col min-h-0 p-4 space-y-3 overflow-hidden">
            {/* Top Toolbar */}
            <div className="flex items-center gap-2">
              <div className="relative flex-1">
                <Search className="w-3.5 h-3.5 absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
                <input
                  type="text"
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  placeholder="Search terms in this category..."
                  className="w-full pl-8 pr-3 py-1.5 bg-slate-950 border border-slate-800 rounded-lg text-xs text-slate-200 focus:outline-none focus:border-indigo-500 font-mono"
                />
              </div>

              {/* Add Phrase Form */}
              <form onSubmit={handleAddTerm} className="flex items-center gap-1.5">
                <input
                  type="text"
                  value={newTerm}
                  onChange={(e) => setNewTerm(e.target.value)}
                  placeholder="Add phrase (e.g. commenta)"
                  className="w-48 px-3 py-1.5 bg-slate-950 border border-slate-800 rounded-lg text-xs text-slate-200 focus:outline-none focus:border-indigo-500 font-mono"
                />
                <button
                  type="submit"
                  disabled={isSaving || !newTerm.trim()}
                  className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold disabled:opacity-50 transition"
                >
                  {isSaving ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <Plus className="w-3.5 h-3.5" />}
                  <span>Add</span>
                </button>
              </form>
            </div>

            {/* Status Feedback */}
            {statusMsg && (
              <div
                className={`p-2 rounded-lg text-xs font-mono flex items-center gap-1.5 ${
                  statusMsg.error
                    ? 'bg-rose-500/15 text-rose-300 border border-rose-500/30'
                    : 'bg-emerald-500/15 text-emerald-300 border border-emerald-500/30'
                }`}
              >
                {statusMsg.error ? <AlertCircle className="w-3.5 h-3.5" /> : <Check className="w-3.5 h-3.5" />}
                <span>{statusMsg.text}</span>
              </div>
            )}

            {/* Terms List Grid */}
            <div className="flex-1 overflow-y-auto pr-1">
              <div className="flex flex-wrap gap-1.5">
                {filteredTerms.length === 0 ? (
                  <p className="text-xs text-slate-500 p-4 font-mono">No terms found matching search.</p>
                ) : (
                  filteredTerms.map((term) => (
                    <span
                      key={term}
                      className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-xs font-mono bg-slate-950 border border-slate-800 text-slate-200 hover:border-slate-700 transition"
                    >
                      <span>{term}</span>
                      <button
                        onClick={() => handleRemoveTerm(term)}
                        title={`Remove "${term}"`}
                        className="text-slate-500 hover:text-rose-400 transition"
                      >
                        <X className="w-3 h-3" />
                      </button>
                    </span>
                  ))
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
