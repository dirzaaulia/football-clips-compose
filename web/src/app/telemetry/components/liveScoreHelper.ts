import { TelemetryEvent } from '../types';

export function parseEventMatchDetails(ev: TelemetryEvent) {
  let home = String(ev.attributes?.home || ev.attributes?.home_team || '');
  let away = String(ev.attributes?.away || ev.attributes?.away_team || '');
  let comp = String(ev.attributes?.competition || '');
  let status = String(ev.attributes?.status || '');
  let homeScore = ev.attributes?.home_score != null ? Number(ev.attributes.home_score) : null;
  let awayScore = ev.attributes?.away_score != null ? Number(ev.attributes.away_score) : null;

  const text = ev.event_text || '';
  if (!comp) {
    const compMatch = text.match(/\[([A-Z_ ]+)\]\s*\[(.*?)\]/);
    if (compMatch) comp = compMatch[2].trim();
  }
  if (!home || !away) {
    const vsMatch = text.match(
      /(?:for\s+|kicked off|underway for|break:\s*|preserved as\s*|fixture:\s*|^)?([A-Za-z0-9 .'-]+)\s+vs\s+([A-Za-z0-9 .'-]+?)(?:\s+(?:finished|status|kicked|score|is|underway|break|inserted|\(|\:|\-|\d)|$)/i
    );
    if (vsMatch) {
      if (!home) home = vsMatch[1].replace(/.*\]\s*(?:\([^)]+\)\s*)?/, '').trim();
      if (!away) away = vsMatch[2].trim();
    }
  }
  if (!status) {
    if (text.includes('[FULL TIME]')) status = 'FINISHED';
    else if (text.includes('[KICKOFF]') || text.includes('[SECOND HALF]')) status = 'IN_PLAY';
    else if (text.includes('[HALF TIME]')) status = 'PAUSED';
  }
  if (homeScore === null || awayScore === null) {
    const scoreMatch = text.match(/(?:finished:|Score updated for [^:]+:)\s*(\d+)\s*-\s*(\d+)/i);
    if (scoreMatch) {
      homeScore = parseInt(scoreMatch[1], 10);
      awayScore = parseInt(scoreMatch[2], 10);
    }
  }

  return { home, away, comp, status, homeScore, awayScore };
}
