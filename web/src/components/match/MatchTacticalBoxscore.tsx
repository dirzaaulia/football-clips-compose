'use client';

import React, { useState, useMemo, useEffect } from 'react';
import ReactMarkdown from 'react-markdown';
import { BarChart3, Users, Newspaper, Shield, Crosshair, Swords, EyeOff, ChevronDown } from 'lucide-react';
import { MatchRecord } from '@/lib/database';
import { EspnSummaryResult } from '@/lib/espn';
import { useSpoilerFree } from '@/hooks/useSpoilerFree';
import MatchLineupsView from './MatchLineupsView';
import MatchStatsView from './MatchStatsView';
import MatchH2HAndLeadersView from './MatchH2HAndLeadersView';
import WebPitchRadar, { WebPitchPlay } from './WebPitchRadar';
import { derivePitchPlays } from './matchPlayHelper';

interface MatchTacticalBoxscoreProps {
  match: MatchRecord;
  espnSummary?: EspnSummaryResult | null;
  recapAnalysis?: string | null;
  isScoreRevealed?: boolean;
  onRevealScore?: () => void;
}

type TabKey = 'radar' | 'stats' | 'lineups' | 'h2h' | 'story';

export default function MatchTacticalBoxscore({
  match,
  espnSummary,
  recapAnalysis,
  isScoreRevealed = false,
  onRevealScore,
}: MatchTacticalBoxscoreProps) {
  const { isSpoilerFree } = useSpoilerFree();
  const [internalRevealed, setInternalRevealed] = useState(false);
  const [isReportRevealed, setIsReportRevealed] = useState(false);

  const { formations, editorial_headline: headline, editorial_story: story } = match;
  const activeFormations = formations || espnSummary?.formations;
  const isFinished = match.status === 'FINISHED';
  const isLive = ['IN_PLAY', 'PAUSED', 'LIVE'].includes(match.status);
  const isRevealed = isScoreRevealed || internalRevealed;
  const shouldHideSpoilers = isSpoilerFree && (isFinished || isLive) && !isRevealed;

  const hasLineups = Boolean(
    activeFormations &&
      ((activeFormations.home?.starters && activeFormations.home.starters.length > 0) ||
        (activeFormations.away?.starters && activeFormations.away.starters.length > 0)),
  );

  const cleanedRecap = useMemo(() => {
    if (!recapAnalysis) return '';
    return recapAnalysis.replace(/(?:\.\.\.)?\s*Continue\s+reading\.{0,3}/gi, '').trim();
  }, [recapAnalysis]);

  const reportText = cleanedRecap || (story && story.trim().length > 20 ? story.trim() : '');
  const hasReport = Boolean(reportText);

  const pitchPlays = useMemo<WebPitchPlay[]>(
    () => derivePitchPlays(match, espnSummary),
    [match, espnSummary],
  );

  const availableTabs = useMemo(() => {
    const list: { key: TabKey; label: string; icon: React.ReactNode }[] = [
      { key: 'radar', label: '2D Pitch View', icon: <Crosshair className="w-3.5 h-3.5" /> },
      { key: 'stats', label: 'Match Stats', icon: <BarChart3 className="w-3.5 h-3.5" /> },
    ];
    if (hasLineups)
      list.push({ key: 'lineups', label: 'Lineups & Formations', icon: <Users className="w-3.5 h-3.5" /> });
    list.push({ key: 'h2h', label: 'H2H & Club Leaders', icon: <Swords className="w-3.5 h-3.5" /> });
    if (hasReport)
      list.push({ key: 'story', label: 'Match Report & Debrief', icon: <Newspaper className="w-3.5 h-3.5" /> });
    return list;
  }, [hasLineups, hasReport]);

  const [activeTab, setActiveTab] = useState<TabKey>(availableTabs[0]?.key || 'radar');

  useEffect(() => {
    if (availableTabs.length > 0 && !availableTabs.some((t) => t.key === activeTab)) {
      const timer = setTimeout(() => setActiveTab(availableTabs[0].key), 0);
      return () => clearTimeout(timer);
    }
  }, [availableTabs, activeTab]);

  return (
    <section id="telemetry" className="rounded-2xl bg-[#121316] border border-white/10 overflow-hidden shadow-lg select-none">
      <div className="flex flex-wrap items-center justify-between gap-3 px-5 py-3.5 bg-[#16191E] border-b border-white/10">
        <div className="flex items-center gap-2">
          <Shield className="w-4 h-4 text-[#DFFF00]" />
          <h3 className="text-xs font-extrabold uppercase tracking-wider text-white">
            Match Centre Intelligence
          </h3>
        </div>
        <div className="flex items-center gap-1.5 bg-[#0D0F12] p-1 rounded-xl border border-white/5 overflow-x-auto no-scrollbar">
          {availableTabs.map((tab) => (
            <button
              key={tab.key}
              type="button"
              onClick={() => setActiveTab(tab.key)}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold transition-all shrink-0 cursor-pointer ${
                activeTab === tab.key ? 'bg-[#DFFF00] text-[#0A0A0C] shadow-sm' : 'text-slate-400 hover:text-white'
              }`}
            >
              {tab.icon}
              <span>{tab.label}</span>
            </button>
          ))}
        </div>
      </div>

      {shouldHideSpoilers ? (
        <div className="p-8 text-center space-y-3">
          <p className="text-xs sm:text-sm text-slate-400 max-w-md mx-auto">
            Match statistics, tactical radar, and post-match reports are hidden while Spoiler-Free mode is active.
          </p>
          <button
            type="button"
            onClick={() => {
              setInternalRevealed(true);
              onRevealScore?.();
            }}
            className="px-4 py-2 rounded-xl bg-[#DFFF00]/15 hover:bg-[#DFFF00]/25 text-[#DFFF00] text-xs font-bold border border-[#DFFF00]/30 transition-colors cursor-pointer"
          >
            Reveal Match Details
          </button>
        </div>
      ) : (
        <>
          {activeTab === 'radar' && (
            <div className="p-4 sm:p-5">
              <WebPitchRadar
                plays={pitchPlays}
                homeTeam={match.home_team_name || match.home_team}
                awayTeam={match.away_team_name || match.away_team}
                isLive={isLive}
                homeColor={match.home_color}
                awayColor={match.away_color}
                formations={activeFormations}
              />
            </div>
          )}
          {activeTab === 'stats' && (
            <MatchStatsView
              match={match}
              homeName={match.home_team_name || match.home_team}
              awayName={match.away_team_name || match.away_team}
              isLive={isLive}
              onOpenRadar={() => setActiveTab('radar')}
            />
          )}
          {activeTab === 'lineups' && activeFormations && (
            <MatchLineupsView
              formations={activeFormations}
              homeName={match.home_team_name || match.home_team}
              awayName={match.away_team_name || match.away_team}
            />
          )}
          {activeTab === 'h2h' && <MatchH2HAndLeadersView match={match} espnSummary={espnSummary} />}
          {activeTab === 'story' && hasReport && (
            <div className="p-5 sm:p-7 space-y-4">
              {headline && (
                <h4 className="text-base sm:text-lg font-extrabold text-white font-scoreboard uppercase tracking-wide">
                  {headline}
                </h4>
              )}
              {isSpoilerFree && isFinished && !isReportRevealed && !isRevealed ? (
                <div className="py-6 px-4 rounded-xl bg-[#0A0A0C] border border-white/10 text-center space-y-3 font-tech">
                  <div className="flex items-center justify-center gap-2 text-xs sm:text-sm text-white/60">
                    <EyeOff className="w-4 h-4 text-[#DFFF00]" />
                    <span>Report contains outcome, pivotal tactical shifts, and decisive goal moments.</span>
                  </div>
                  <button
                    onClick={() => setIsReportRevealed(true)}
                    className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-[#DFFF00] hover:bg-[#cbe800] text-[#0A0A0C] text-xs font-tech font-black uppercase tracking-wider transition-all shadow-[0_0_15px_rgba(223,255,0,0.3)] cursor-pointer"
                  >
                    <span>Reveal Match Report</span>
                    <ChevronDown className="w-3.5 h-3.5" />
                  </button>
                </div>
              ) : (
                <div className="prose prose-invert prose-headings:text-white prose-headings:font-scoreboard prose-h3:text-sm prose-h3:uppercase prose-h3:tracking-wider prose-h3:mt-3 prose-h3:mb-1 prose-strong:text-[#DFFF00] prose-em:text-white/60 prose-li:text-white/80 max-w-none text-white/80 text-xs sm:text-sm leading-relaxed font-tech">
                  <ReactMarkdown>{reportText}</ReactMarkdown>
                </div>
              )}
              {match.editorial_source && (
                <p className="text-[11px] text-slate-500 pt-2 border-t border-white/5 font-tech">
                  Source: {match.editorial_source}
                </p>
              )}
            </div>
          )}
        </>
      )}
    </section>
  );
}
