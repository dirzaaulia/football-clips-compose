import { CandidateVerdict, TelemetryEvent } from './types';

const HIGHLIGHT_CODES = new Set([
  'SEARCH_START', 'CHANNEL_SCAN', 'CANDIDATE_ITEM', 'AI_EVAL', 'COOLDOWN',
  'LINKED', 'NOT_FOUND', 'DISCOVERY', 'RSS_DISCOVERY', 'API_CHANNEL_FALLBACK',
  'API_GENERAL_SEARCH', 'CANDIDATE_ACCEPTED', 'CANDIDATE_REJECTED', 'UPGRADE_ATTEMPT',
  'RESULT_EMPTY', 'RESULT_UNLINKED', 'CHANNEL_EMPTY',
]);

export function isHighlightEvent(ev: TelemetryEvent): boolean {
  const pipe = (ev.pipeline || '').toLowerCase();
  const sub = (ev.subsystem || '').toUpperCase();
  if (pipe === 'youtube' || pipe === 'highlight' || sub === 'YOUTUBE' || sub === 'HIGHLIGHT') return true;
  if (ev.code && HIGHLIGHT_CODES.has(ev.code)) return true;
  const text = ev.event_text || '';
  return text.includes('YOUTUBE') || text.includes('Searching official') || text.includes('Cooldown') ||
         text.includes('CANDIDATE') || text.includes('CHANNEL SCAN') || text.includes('RSS DISCOVERY');
}

export function parseCandidateVerdict(text: string, index: number): CandidateVerdict | null {
  const isAccepted = text.includes('[ACCEPTED]');
  const isRejected = text.includes('[REJECTED]');
  if (!isAccepted && !isRejected) return null;

  const titleMatch = text.match(/'([^']+)'/);
  const channelMatch = text.match(/\(([^)]+)\)/);
  const urlMatch = text.match(/->\s*(https:\/\/youtu\.be\/[a-zA-Z0-9_-]{11})/);
  const url = urlMatch ? urlMatch[1] : '';
  const candidateId = url ? url.substring(url.lastIndexOf('/') + 1) : `cand-${index}`;
  const verdictReason = text.includes('| Verdict: ')
    ? text.split('| Verdict: ')[1].trim()
    : isAccepted ? 'Accepted by AI evaluation' : 'Not selected';

  return {
    candidateId,
    index,
    title: titleMatch ? titleMatch[1] : 'Candidate Video',
    channel: channelMatch ? channelMatch[1] : 'Official Channel',
    url,
    isAccepted,
    verdictReason,
    evaluator: isAccepted ? 'AI Evaluator' : 'Scorer',
    score: isAccepted ? 100 : 0,
  };
}

export function extractMatchTitleFromText(text: string): string | null {
  if (text.includes('Searching official highlights for:')) {
    return text.replace(/.*Searching official highlights for:\s*/i, '').replace(/\s*\(\d{4}-\d{2}-\d{2}\).*/, '').trim();
  }
  const cd = text.match(/(?:Cooldown guardrail active for|\[COOLDOWN\])\s*([^:]+):/i);
  if (cd) return cd[1].trim();

  // Contextual extraction for result, skip, and claimed logs
  const contextual = text.match(/(?:found for|mapped for:?|candidates \(|guardrail active for)\s*([\p{L}\p{N}\s.'-]+ vs [\p{L}\p{N}\s.'-]+?)(?:\.|\)|:|$)/iu);
  if (contextual) return contextual[1].trim();

  // Support Unicode letters to prevent slicing names like "Atlético" -> "tico"
  const vsMatch = text.match(/([\p{L}\p{N}\s.'-]+ vs [\p{L}\p{N}\s.'-]+)/iu);
  if (vsMatch) {
    let clean = vsMatch[1].replace(/\.+$/, '').trim();
    clean = clean.replace(/^(?:No eligible highlight candidates found for|All detected candidates \(|No official YouTube handles mapped for:?)\s*/i, '').trim();
    return clean;
  }

  return null;
}

export function extractTimestampFromText(text: string, fallback?: string): string {
  const match = text.match(/\[(\d{2}:\d{2}:\d{2}\s*WIB)\]/);
  if (match) return match[1];
  if (fallback) {
    try {
      return new Date(fallback).toLocaleTimeString('en-GB', { timeZone: 'Asia/Jakarta' }) + ' WIB';
    } catch {
      return fallback;
    }
  }
  return '';
}
