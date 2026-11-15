'use client';

import React from 'react';
import { HelpCircle, ChevronDown } from 'lucide-react';
import { MatchRecord } from '@/lib/database';

interface MatchFaqSectionProps {
  match: MatchRecord;
}

export default function MatchFaqSection({ match }: MatchFaqSectionProps) {
  const homeName = match.home_team_name || match.home_team || 'Home Team';
  const awayName = match.away_team_name || match.away_team || 'Away Team';
  const compName = match.competition_name || 'Official Competition';
  const isFinished = match.status === 'FINISHED';
  const isLive = ['IN_PLAY', 'PAUSED', 'LIVE'].includes(match.status);

  const scoreSummary = isFinished
    ? `The match concluded with a final score of ${homeName} ${match.home_score ?? 0} - ${match.away_score ?? 0} ${awayName}.`
    : isLive
    ? `The match is currently in play with the live score standing at ${homeName} ${match.home_score ?? 0} - ${match.away_score ?? 0} ${awayName}.`
    : `This match between ${homeName} and ${awayName} is scheduled to kick off on ${new Date(match.utc_date).toLocaleDateString('en-GB', { dateStyle: 'full' })}.`;

  const faqs = [
    {
      question: `What was the score in ${homeName} vs ${awayName}?`,
      answer: `${scoreSummary} Comprehensive match highlights, decisive goal replays, and tactical analysis are available directly in this Match Centre.`,
    },
    {
      question: `Where are the official highlights for this match sourced from?`,
      answer: `All highlight clips and videos featured on Football Highlights & Clips are embedded from verified, official club channels and rights-holding league broadcasters (such as UEFA, Premier League, and official club media) via the official YouTube embed player.`,
    },
    {
      question: `What were the tactical systems deployed in ${homeName} vs ${awayName}?`,
      answer: `Both squads utilized structured tactical formations focusing on spatial control in midfield, high-intensity transition pressing, and half-space exploitation as documented in our detailed tactical match report above.`,
    },
    {
      question: `How does this result impact the ${compName} standings?`,
      answer: `This fixture directly influences the table hierarchy, continental qualification races, and title contention benchmarks for both ${homeName} and ${awayName}. View updated point totals and goal difference on our dedicated Standings page.`,
    },
  ];

  return (
    <section className="rounded-2xl bg-[#121316] border border-white/10 p-5 sm:p-7 space-y-4 shadow-xl select-none">
      <div className="flex items-center gap-2 pb-3 border-b border-white/10 text-[#DFFF00]">
        <HelpCircle className="w-5 h-5 text-[#DFFF00]" />
        <h2 className="text-sm font-tech font-black uppercase tracking-wider text-white">
          Frequently Asked Questions &amp; Match Facts
        </h2>
      </div>

      <div className="space-y-3 font-tech">
        {faqs.map((faq, index) => (
          <details
            key={index}
            className="group rounded-xl bg-[#0A0A0C] border border-white/10 p-4 transition-all open:border-[#DFFF00]/40"
            open={index === 0}
          >
            <summary className="flex items-center justify-between cursor-pointer list-none text-xs sm:text-sm font-bold text-white group-hover:text-[#DFFF00] transition-colors">
              <span>{faq.question}</span>
              <ChevronDown className="w-4 h-4 text-white/40 transition-transform duration-200 group-open:rotate-180" />
            </summary>
            <p className="mt-3 text-xs sm:text-sm text-white/70 leading-relaxed border-t border-white/5 pt-3">
              {faq.answer}
            </p>
          </details>
        ))}
      </div>
    </section>
  );
}
