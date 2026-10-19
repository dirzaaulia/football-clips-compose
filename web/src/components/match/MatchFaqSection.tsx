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
      answer: `All highlight clips and videos featured on FootballClips are embedded from verified, official club channels and rights-holding league broadcasters (such as UEFA, Premier League, and official club media) via the official YouTube embed player.`,
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
    <section className="rounded-3xl bg-[#181C20] border border-[#334155] p-6 sm:p-8 space-y-5 shadow-xl">
      <div className="flex items-center gap-2 pb-3 border-b border-[#334155]/60 text-[#7098F8]">
        <HelpCircle className="w-5 h-5 text-[#4FC3F7]" />
        <h2 className="text-sm font-extrabold uppercase tracking-wider text-white">
          Frequently Asked Questions &amp; Match Facts
        </h2>
      </div>

      <div className="space-y-3">
        {faqs.map((faq, index) => (
          <details
            key={index}
            className="group rounded-2xl bg-[#101418] border border-[#334155]/70 p-4 transition-all open:border-[#7098F8]/50"
            open={index === 0}
          >
            <summary className="flex items-center justify-between cursor-pointer list-none text-xs sm:text-sm font-bold text-white group-hover:text-[#4FC3F7] transition-colors">
              <span>{faq.question}</span>
              <ChevronDown className="w-4 h-4 text-[#94A3B8] transition-transform duration-200 group-open:rotate-180" />
            </summary>
            <p className="mt-3 text-xs sm:text-sm text-[#94A3B8] leading-relaxed border-t border-[#334155]/40 pt-3">
              {faq.answer}
            </p>
          </details>
        ))}
      </div>
    </section>
  );
}
