'use client';

import React, { useState, useMemo } from 'react';
import { Search, Trophy, Check, X, Globe, Layers, Award } from 'lucide-react';
import { Competition } from '@/hooks/useCompetitions';
import CompetitionLogo from '@/components/common/CompetitionLogo';

interface CompetitionDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  competitions: Competition[];
  selectedComp: string;
  onSelectComp: (comp: string) => void;
}

export default function CompetitionDrawer({
  isOpen,
  onClose,
  competitions,
  selectedComp,
  onSelectComp,
}: CompetitionDrawerProps) {
  const [search, setSearch] = useState('');

  const filtered = useMemo(() => {
    return competitions.filter((c) => {
      if (!c.competitionId || c.competitionId === 'ALL') return false;
      if (!search.trim()) return true;
      const q = search.toLowerCase();
      return (
        c.name.toLowerCase().includes(q) ||
        (c.shortName && c.shortName.toLowerCase().includes(q)) ||
        (c.countryCode && c.countryCode.toLowerCase().includes(q))
      );
    });
  }, [competitions, search]);

  const categories = useMemo(() => {
    const european = filtered.filter(
      (c) =>
        c.type === 'hybrid' ||
        ['CL', 'EL', 'ECL', 'UECL', 'EC'].includes(c.competitionId || '') ||
        c.name.toLowerCase().includes('champions') ||
        c.name.toLowerCase().includes('europa')
    );

    const cups = filtered.filter(
      (c) =>
        c.type === 'cup' ||
        c.name.toLowerCase().includes('cup') ||
        c.name.toLowerCase().includes('pokal') ||
        c.name.toLowerCase().includes('copa') ||
        c.name.toLowerCase().includes('coppa')
    );

    const leagues = filtered.filter(
      (c) => !european.includes(c) && !cups.includes(c)
    );

    return { european, leagues, cups };
  }, [filtered]);

  if (!isOpen) return null;

  const handleSelect = (compId: string) => {
    onSelectComp(compId);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 overflow-hidden select-none animate-in fade-in duration-200">
      {/* Blurred Dim Backdrop */}
      <div
        className="fixed inset-0 bg-black/75 backdrop-blur-md transition-opacity"
        onClick={onClose}
      />

      <div className="fixed inset-y-0 right-0 max-w-full flex">
        <div className="w-screen max-w-md sm:max-w-lg bg-[#121316] border-l border-white/10 shadow-2xl flex flex-col h-full animate-in slide-in-from-right duration-300">
          {/* Drawer Top Header */}
          <div className="p-5 sm:p-6 border-b border-white/10 flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="w-9 h-9 rounded-xl bg-[#DFFF00]/10 border border-[#DFFF00]/30 flex items-center justify-center text-[#DFFF00]">
                <Trophy className="w-5 h-5" />
              </div>
              <div>
                <h2 className="font-scoreboard text-base sm:text-lg uppercase text-white tracking-wider">
                  Supported Competitions
                </h2>
                <p className="font-tech text-xs text-white/50">
                  Select a league or tournament to filter highlights
                </p>
              </div>
            </div>
            <button
              onClick={onClose}
              className="p-2 rounded-xl text-white/60 hover:text-white hover:bg-white/5 transition-colors cursor-pointer"
              title="Close Drawer"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Search Bar & All Competitions Shortcut */}
          <div className="p-4 sm:p-6 pb-2 space-y-3">
            <div className="relative flex items-center bg-[#0A0A0C] rounded-xl px-3.5 py-2.5 border border-white/10 focus-within:border-[#DFFF00] transition-colors">
              <Search className="w-4 h-4 text-white/40 mr-2.5 shrink-0" />
              <input
                type="text"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Find competition or country..."
                className="bg-transparent text-xs sm:text-sm text-white placeholder:text-white/40 focus:outline-none w-full font-tech"
                autoFocus
              />
              {search && (
                <button
                  onClick={() => setSearch('')}
                  className="p-1 text-white/40 hover:text-white"
                >
                  <X className="w-3.5 h-3.5" />
                </button>
              )}
            </div>

            {/* Quick All Leagues Button */}
            <button
              onClick={() => handleSelect('ALL')}
              className={`w-full flex items-center justify-between p-3 rounded-xl border text-xs sm:text-sm font-tech font-bold uppercase tracking-wider transition-all cursor-pointer ${
                selectedComp === 'ALL'
                  ? 'bg-[#DFFF00] text-[#0A0A0C] border-[#DFFF00] shadow-[0_0_15px_rgba(223,255,0,0.3)]'
                  : 'bg-[#1A1C22] text-white/90 hover:text-white border-white/10 hover:border-white/20'
              }`}
            >
              <div className="flex items-center gap-3">
                <Globe className={`w-4 h-4 ${selectedComp === 'ALL' ? 'text-[#0A0A0C]' : 'text-[#DFFF00]'}`} />
                <span>All Competitions &amp; Leagues</span>
              </div>
              {selectedComp === 'ALL' && <Check className="w-4 h-4 text-[#0A0A0C] stroke-[3]" />}
            </button>
          </div>

          {/* Scrollable Categorized List */}
          <div className="flex-1 overflow-y-auto px-4 sm:px-6 py-2 space-y-5 scrollbar-thin">
            {/* European Competitions */}
            {categories.european.length > 0 && (
              <div className="space-y-2">
                <div className="flex items-center gap-1.5 text-xs font-tech font-black text-[#DFFF00] uppercase tracking-wider">
                  <Award className="w-3.5 h-3.5" />
                  <span>European &amp; International</span>
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                  {categories.european.map((c) => (
                    <CompetitionCard
                      key={c.competitionId || c.name}
                      competition={c}
                      isSelected={selectedComp === c.competitionId}
                      onSelect={() => handleSelect(c.competitionId || '')}
                    />
                  ))}
                </div>
              </div>
            )}

            {/* Domestic Leagues */}
            {categories.leagues.length > 0 && (
              <div className="space-y-2">
                <div className="flex items-center gap-1.5 text-xs font-tech font-black text-[#38BDF8] uppercase tracking-wider">
                  <Layers className="w-3.5 h-3.5" />
                  <span>Domestic Leagues</span>
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                  {categories.leagues.map((c) => (
                    <CompetitionCard
                      key={c.competitionId || c.name}
                      competition={c}
                      isSelected={selectedComp === c.competitionId}
                      onSelect={() => handleSelect(c.competitionId || '')}
                    />
                  ))}
                </div>
              </div>
            )}

            {/* Domestic Cups */}
            {categories.cups.length > 0 && (
              <div className="space-y-2">
                <div className="flex items-center gap-1.5 text-xs font-tech font-black text-amber-400 uppercase tracking-wider">
                  <Trophy className="w-3.5 h-3.5" />
                  <span>Cup Tournaments</span>
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                  {categories.cups.map((c) => (
                    <CompetitionCard
                      key={c.competitionId || c.name}
                      competition={c}
                      isSelected={selectedComp === c.competitionId}
                      onSelect={() => handleSelect(c.competitionId || '')}
                    />
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

function CompetitionCard({
  competition,
  isSelected,
  onSelect,
}: {
  competition: Competition;
  isSelected: boolean;
  onSelect: () => void;
}) {
  return (
    <button
      onClick={onSelect}
      className={`min-h-[48px] p-2.5 rounded-xl border flex items-center justify-between text-left transition-all cursor-pointer ${
        isSelected
          ? 'bg-[#DFFF00] text-[#0A0A0C] border-[#DFFF00] shadow-[0_0_12px_rgba(223,255,0,0.25)]'
          : 'bg-[#1A1C22] text-white/85 hover:text-white border-white/10 hover:border-white/20'
      }`}
    >
      <div className="flex items-center gap-2.5 min-w-0">
        <div className="w-6 h-6 relative shrink-0">
          <CompetitionLogo src={competition.logo} name={competition.name} />
        </div>
        <div className="flex flex-col min-w-0">
          <span className="font-tech font-bold text-xs uppercase tracking-wider truncate">
            {competition.shortName || competition.name}
          </span>
          {competition.countryCode && (
            <span className={`text-[10px] font-mono ${isSelected ? 'text-[#0A0A0C]/70' : 'text-white/40'}`}>
              {competition.countryCode}
            </span>
          )}
        </div>
      </div>
      {isSelected && <Check className="w-4 h-4 text-[#0A0A0C] shrink-0 ml-1 stroke-[3]" />}
    </button>
  );
}
