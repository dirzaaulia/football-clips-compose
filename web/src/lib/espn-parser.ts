/* eslint-disable @typescript-eslint/no-explicit-any */
import type { MatchEvent, EspnSummaryResult, EspnH2HMatch, EspnTeamFormMatch, EspnLeaderEntry, EspnGameInfo } from './espn';

function stripHtml(html: string): string {
  return html
    .replace(/<\/p>/gi, '\n\n')
    .replace(/<br\s*\/?>/gi, '\n')
    .replace(/<[^>]+>/g, '')
    .replace(/&nbsp;/g, ' ')
    .replace(/&amp;/g, '&')
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .trim();
}

function mapRoster(r: any) {
  if (!r) return undefined;
  const allRoster: any[] = r.roster || [];
  const toPlayer = (p: any) => ({
    name: p.athlete?.displayName || p.athlete?.shortName || 'Player',
    jersey: p.jersey || p.athlete?.jersey || '',
    position: p.position?.abbreviation || p.position?.displayName || '',
  });
  return {
    formation: r.formation || '4-3-3',
    color: r.team?.color || 'DFFF00',
    starters: allRoster.filter((p) => p.starter).map(toPlayer),
    substitutes: allRoster.filter((p) => !p.starter).map(toPlayer),
  };
}

function mapLastFive(entry: any): EspnTeamFormMatch[] {
  return (entry?.events || []).slice(0, 5).map((ev: any, i: number) => ({
    id: String(ev.id || i),
    date: ev.gameDate || '',
    opponentName: ev.opponent?.displayName || ev.opponent?.abbreviation || 'Opponent',
    opponentLogo: ev.opponent?.logo || ev.opponent?.logos?.[0]?.href,
    atVs: ev.atVs || 'vs',
    score: ev.score || '0-0',
    result: (ev.gameResult === 'W' || ev.gameResult === 'L' ? ev.gameResult : 'D') as 'W' | 'D' | 'L',
  }));
}

function mapLeaders(teamLeaders: any): EspnLeaderEntry[] {
  return (teamLeaders?.leaders || []).map((cat: any) => {
    const top = cat.leaders?.[0];
    return {
      category: cat.name || 'stat',
      displayName: cat.displayName || cat.name || 'Leader',
      playerName: top?.athlete?.displayName || 'Player',
      shortName: top?.athlete?.shortName,
      jersey: top?.athlete?.jersey,
      position: top?.athlete?.position?.abbreviation,
      headshot: top?.athlete?.headshot?.href,
      displayValue: top?.displayValue || String(top?.value ?? 0),
      value: Number(top?.value ?? 0),
    };
  });
}

function extractCleanPlayerName(ev: any): string {
  const fromParticipant = ev.participants?.[0]?.athlete?.displayName || ev.participants?.[0]?.athlete?.shortName;
  if (fromParticipant) return String(fromParticipant).trim();

  const rawText = String(ev.shortText || ev.text || '');
  // Extract name from patterns like "Goal! Team A 1, Team B 0. Bukayo Saka (Arsenal) left footed shot..."
  const goalMatch = rawText.match(/\.\s*([A-ZÀ-Ž][a-zA-ZÀ-ž'.\-\s]{1,32}?)\s*\(/);
  if (goalMatch?.[1]) return goalMatch[1].trim();

  const cardMatch = rawText.match(/^([A-ZÀ-Ž][a-zA-ZÀ-ž'.\-\s]{1,32}?)\s*\(/);
  if (cardMatch?.[1]) return cardMatch[1].trim();

  return '';
}

function resolveEventSide(
  ev: any,
  homeId: string,
  awayId: string,
  homeName: string,
  awayName: string,
): 'home' | 'away' | null {
  const teamId = String(ev.team?.id || ev.participants?.[0]?.team?.id || '');
  if (teamId && homeId && teamId === homeId) return 'home';
  if (teamId && awayId && teamId === awayId) return 'away';

  const teamDisplay = String(ev.team?.displayName || ev.team?.name || '').toLowerCase();
  const hLower = homeName.toLowerCase();
  const aLower = awayName.toLowerCase();
  if (teamDisplay && hLower && (teamDisplay.includes(hLower) || hLower.includes(teamDisplay))) return 'home';
  if (teamDisplay && aLower && (teamDisplay.includes(aLower) || aLower.includes(teamDisplay))) return 'away';

  // Check "(Team Name)" right after player name in commentary text
  const rawText = String(ev.text || ev.shortText || '');
  const parenTeams = Array.from(rawText.matchAll(/\(([^)]+)\)/g)).map((m) => m[1].toLowerCase());
  for (const pt of parenTeams) {
    if (hLower && (pt.includes(hLower) || hLower.includes(pt))) return 'home';
    if (aLower && (pt.includes(aLower) || aLower.includes(pt))) return 'away';
  }
  return null;
}

function derivePitchCoords(
  ev: any,
  side: 'home' | 'away' | null,
  typeText: string,
  idx: number,
): { x: number; y: number; tx: number | null; ty: number | null } {
  const rawX = ev.fieldPositionX ?? ev.coordinate?.x;
  const rawY = ev.fieldPositionY ?? ev.coordinate?.y;
  if (typeof rawX === 'number' && typeof rawY === 'number' && rawX > 0 && rawY > 0) {
    const normX = rawX > 1 ? rawX / 100 : rawX;
    const normY = rawY > 1 ? rawY / 100 : rawY;
    return { x: Math.max(0.04, Math.min(0.96, normX)), y: Math.max(0.06, Math.min(0.94, normY)), tx: null, ty: null };
  }

  const isHome = side !== 'away';
  const t = typeText.toLowerCase();
  const spread = ((idx * 19 + 7) % 100) / 100;
  const ySpread = 0.2 + (((idx * 31 + 11) % 60) / 100);

  if (t.includes('goal') && !t.includes('kick')) {
    const x = isHome ? 0.86 + spread * 0.08 : 0.14 - spread * 0.08;
    const y = 0.34 + (spread - 0.5) * 0.24;
    return { x, y, tx: isHome ? 0.98 : 0.02, ty: 0.5 };
  }
  if (t.includes('shot') || t.includes('attempt') || t.includes('saved') || t.includes('miss')) {
    const x = isHome ? 0.76 + spread * 0.14 : 0.24 - spread * 0.14;
    return { x, y: ySpread, tx: isHome ? 0.97 : 0.03, ty: 0.45 + (spread - 0.5) * 0.15 };
  }
  if (t.includes('corner')) {
    return { x: isHome ? 0.96 : 0.04, y: idx % 2 === 0 ? 0.06 : 0.94, tx: isHome ? 0.88 : 0.12, ty: 0.5 };
  }
  if (t.includes('sub')) {
    return { x: 0.5, y: 0.92, tx: null, ty: null };
  }
  const x = isHome ? 0.35 + spread * 0.45 : 0.65 - spread * 0.45;
  return { x, y: ySpread, tx: null, ty: null };
}

export function parseEspnSummaryPayload(data: any): EspnSummaryResult {
  const comp = data.header?.competitions?.[0];
  const competitors: any[] = comp?.competitors || [];
  const homeComp = competitors.find((c) => c.homeAway === 'home') || competitors[0];
  const awayComp = competitors.find((c) => c.homeAway === 'away') || competitors[1];

  const homeId = String(homeComp?.id || homeComp?.team?.id || data.boxscore?.teams?.[0]?.team?.id || '');
  const awayId = String(awayComp?.id || awayComp?.team?.id || data.boxscore?.teams?.[1]?.team?.id || '');
  const homeName = String(homeComp?.team?.displayName || data.boxscore?.teams?.[0]?.team?.displayName || '');
  const awayName = String(awayComp?.team?.displayName || data.boxscore?.teams?.[1]?.team?.displayName || '');

  const statsHome: Record<string, string> = {};
  const statsAway: Record<string, string> = {};
  const teams = data.boxscore?.teams || [];
  const homeTeamBox =
    teams.find((t: any) => t.homeAway === 'home' || (homeId && String(t.team?.id) === homeId)) || teams[0];
  const awayTeamBox =
    teams.find((t: any) => t.homeAway === 'away' || (awayId && String(t.team?.id) === awayId)) || teams[1];

  if (homeTeamBox) {
    for (const s of homeTeamBox.statistics || []) {
      if (s.name && s.displayValue !== undefined) statsHome[s.name] = String(s.displayValue);
    }
  }
  if (awayTeamBox) {
    for (const s of awayTeamBox.statistics || []) {
      if (s.name && s.displayValue !== undefined) statsAway[s.name] = String(s.displayValue);
    }
  }

  const rosters = data.rosters || [];
  const gameInfo: EspnGameInfo = {
    venueName: data.gameInfo?.venue?.fullName,
    city: data.gameInfo?.venue?.address?.city,
    country: data.gameInfo?.venue?.address?.country,
    attendance: data.gameInfo?.attendance,
    referee: data.gameInfo?.officials?.[0]?.displayName || data.gameInfo?.officials?.[0]?.fullName,
  };

  const series = data.seasonseries?.[0];
  const h2hMatches: EspnH2HMatch[] = (series?.events || []).map((ev: any, idx: number) => ({
    id: String(ev.id || idx),
    date: ev.date || ev.gameDate || '',
    homeTeamId: String(ev.homeTeamId || ev.competitors?.[0]?.id || ''),
    awayTeamId: String(ev.awayTeamId || ev.competitors?.[1]?.id || ''),
    homeTeamName: ev.competitors?.[0]?.team?.displayName || ev.homeTeam?.displayName || 'Home',
    awayTeamName: ev.competitors?.[1]?.team?.displayName || ev.awayTeam?.displayName || 'Away',
    homeScore: String(ev.homeTeamScore ?? ev.competitors?.[0]?.score ?? '0'),
    awayScore: String(ev.awayTeamScore ?? ev.competitors?.[1]?.score ?? '0'),
    scoreText: ev.score || `${ev.homeTeamScore ?? 0}-${ev.awayTeamScore ?? 0}`,
    competitionName: ev.leagueName || series?.title || 'Head-to-Head',
  }));

  const rawEvents = data.keyEvents || comp?.details || data.commentary || [];
  const events: MatchEvent[] = rawEvents.slice(0, 60).map((ev: any, idx: number) => {
    const typeText = String(ev.type?.text || ev.play?.type?.text || 'Event');
    const isScoring = Boolean(
      ev.scoringPlay ||
        (typeText.toLowerCase().includes('goal') &&
          !typeText.toLowerCase().includes('kick') &&
          !typeText.toLowerCase().includes('attempt')),
    );
    const side = resolveEventSide(ev, homeId, awayId, homeName, awayName);
    const coords = derivePitchCoords(ev, side, typeText, idx);
    const cleanPlayer = extractCleanPlayerName(ev);
    const assistPlayer = ev.participants?.[1]?.athlete?.displayName || null;

    return {
      id: String(ev.id || idx),
      type: typeText,
      type_text: typeText,
      clock: ev.clock?.displayValue || '',
      period: ev.period?.number,
      side,
      scoring_play: isScoring,
      player: cleanPlayer,
      assist: assistPlayer,
      text: ev.text || ev.shortText || typeText,
      x_coord: coords.x,
      y_coord: coords.y,
      target_x: coords.tx,
      target_y: coords.ty,
    };
  });

  return {
    statistics:
      Object.keys(statsHome).length > 0 || Object.keys(statsAway).length > 0
        ? { home: statsHome, away: statsAway }
        : null,
    formations: rosters.length >= 2 ? { home: mapRoster(rosters[0]), away: mapRoster(rosters[1]) } : null,
    editorial_headline: data.article?.headline || null,
    editorial_story: data.article?.story ? stripHtml(data.article.story) : null,
    editorial_source: data.article?.source || 'ESPN Match Report',
    events: events.length > 0 ? events : null,
    gameInfo,
    headToHead:
      h2hMatches.length > 0 || series
        ? { summary: series?.summary, seriesScore: series?.seriesScore, matches: h2hMatches }
        : null,
    lastFive:
      data.lastFiveGames?.length >= 2
        ? { home: mapLastFive(data.lastFiveGames[0]), away: mapLastFive(data.lastFiveGames[1]) }
        : null,
    leaders:
      data.leaders?.length >= 2 ? { home: mapLeaders(data.leaders[0]), away: mapLeaders(data.leaders[1]) } : null,
  };
}
