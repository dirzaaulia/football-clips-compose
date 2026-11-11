export const ESPN_COMPETITION_SLUGS: Record<string, string> = {
  CL: 'uefa.champions', EL: 'uefa.europa', ECL: 'uefa.ecl',
  PL: 'eng.1', PD: 'esp.1', SA: 'ita.1', BL1: 'ger.1', FL1: 'fra.1',
  SPL: 'ksa.1', MLS: 'usa.1', DED: 'ned.1', PPL: 'por.1',
  FAC: 'eng.fa', CDR: 'esp.copa_del_rey', DFB: 'ger.dfb_pokal', CI: 'ita.coppa_italia', CDF: 'fra.coupe_de_france',
};

export interface MatchEvent {
  id: string;
  type: string;
  type_text: string;
  clock: string;
  period?: number;
  side: 'home' | 'away' | null;
  scoring_play?: boolean;
  player: string;
  assist?: string | null;
  text: string;
  x_coord?: number | null;
  y_coord?: number | null;
  target_x?: number | null;
  target_y?: number | null;
}

export interface EspnSummaryResult {
  statistics?: {
    home?: Record<string, string>;
    away?: Record<string, string>;
  } | null;
  formations?: {
    home?: { formation?: string; color?: string; starters?: Array<{ name: string; jersey?: string; position?: string }> };
    away?: { formation?: string; color?: string; starters?: Array<{ name: string; jersey?: string; position?: string }> };
  } | null;
  editorial_headline?: string | null;
  editorial_story?: string | null;
  editorial_source?: string | null;
  events?: MatchEvent[] | null;
  home_score?: number | null;
  away_score?: number | null;
  home_color?: string | null;
  away_color?: string | null;
  home_penalties?: number | null;
  away_penalties?: number | null;
  display_clock?: string | null;
  status?: string | null;
}

export async function fetchEspnMatchSummary(
  eventId: string | number,
  competitionCode?: string,
  isLive: boolean = false
): Promise<EspnSummaryResult | null> {
  // ESPN API restricts direct browser CORS requests; skip in browser to avoid network errors
  if (typeof window !== 'undefined') {
    return null;
  }

  const primarySlug = competitionCode ? ESPN_COMPETITION_SLUGS[competitionCode.toUpperCase()] : undefined;
  const slugsToTry = primarySlug
    ? [primarySlug]
    : ['uefa.champions', 'uefa.europa', 'eng.1', 'esp.1', 'ita.1', 'ger.1', 'fra.1', 'ksa.1', 'usa.1', 'ned.1', 'por.1', 'eng.fa'];

  for (const slug of slugsToTry) {
    try {
      const url = `https://site.api.espn.com/apis/site/v2/sports/soccer/${slug}/summary?event=${eventId}`;
      const res = await fetch(url, {
        headers: { 'User-Agent': 'Mozilla/5.0' },
        cache: isLive ? 'no-store' : 'default',
        next: isLive ? { revalidate: 0 } : { revalidate: 120 },
      });
      if (!res.ok) continue;

      const data = await res.json();
      if (!data || (!data.boxscore && !data.header && !data.article)) continue;

      // Extract statistics
      const statsObj: Record<string, Record<string, string>> = { home: {}, away: {} };
      const teams = data.boxscore?.teams || [];
      for (const t of teams) {
        const side = t.homeAway === 'home' ? 'home' : 'away';
        for (const s of t.statistics || []) {
          if (s.name && s.displayValue != null) {
            statsObj[side][s.name] = String(s.displayValue);
          }
        }
      }

      // Extract formations
      const formationsObj: Record<string, any> = { home: {}, away: {} };
      for (const r of data.rosters || []) {
        const side = r.homeAway === 'home' ? 'home' : 'away';
        const uColor = r.uniform?.color ? `#${r.uniform.color.replace(/^#/, '')}` : null;
        formationsObj[side] = {
          formation: r.formation,
          color: uColor,
          starters: (r.roster || [])
            .filter((p: any) => p.starter)
            .map((p: any) => ({
              name: p.athlete?.displayName || 'Player',
              jersey: p.jersey,
              position: p.position?.abbreviation,
            })),
        };
      }

      const article = data.article || {};
      const headline = article.headline || null;
      const story = article.story || null;

      const hasStats = Boolean(statsObj.home && Object.keys(statsObj.home).length > 0);
      const hasFormations = Boolean(formationsObj.home?.starters?.length || formationsObj.away?.starters?.length);

      // Extract key events and rich play commentary
      const rawKeyEvents = data.keyEvents || [];
      const rawCommentary = data.commentary || [];
      const headerComps = data.header?.competitions || [];
      const competitors = headerComps[0]?.competitors || [];
      const homeTeam = competitors.find((c: any) => c.homeAway === 'home');
      const homeTeamId = homeTeam?.team?.id ? String(homeTeam.team.id) : null;
      const homeTeamName = homeTeam?.team?.displayName || '';

      const parsedEvents: MatchEvent[] = [];
      const seenIds = new Set<string>();

      for (const e of rawKeyEvents) {
        const tInfo = e.type || {};
        const tType = String(tInfo.type || '').toLowerCase();
        const tText = String(tInfo.text || '');
        if (
          tType.includes('goal') ||
          tType.includes('card') ||
          tType.includes('sub') ||
          tType.includes('penalty') ||
          tText.toLowerCase().includes('goal') ||
          tText.toLowerCase().includes('card')
        ) {
          const teamId = e.team?.id ? String(e.team.id) : null;
          const side = teamId === homeTeamId ? 'home' : teamId ? 'away' : null;
          const participants = (e.participants || []).map((p: any) => p.athlete?.displayName || '');
          const isHome = side === 'home';
          const eId = String(e.id || `${e.clock?.displayValue || '0'}-${tType}`);
          seenIds.add(eId);
          parsedEvents.push({
            id: eId,
            type: tType,
            type_text: tText,
            clock: e.clock?.displayValue || '',
            period: e.period?.number,
            side,
            scoring_play: Boolean(e.scoringPlay),
            player: participants[0] || '',
            assist: participants.length > 1 ? participants[1] : null,
            text: e.shortText || e.text || '',
            x_coord: tType.includes('goal') ? (isHome ? 92 : 8) : (isHome ? 60 : 40),
            y_coord: 50,
          });
        }
      }

      for (const c of rawCommentary) {
        const p = c.play;
        if (!p) continue;
        const pId = String(p.id || c.sequence || '');
        if (seenIds.has(pId)) continue;
        const pType = String(p.type?.type || p.type?.text || '').toLowerCase();
        const isShot = pType.includes('shot') || pType.includes('attempt');
        const isCorner = pType.includes('corner');
        const isGoal = pType.includes('goal');
        const isCard = pType.includes('card');
        const isSub = pType.includes('sub');
        if (!isShot && !isCorner && !isGoal && !isCard && !isSub) continue;

        const isHome = p.team?.displayName ? p.team.displayName === homeTeamName : false;
        const side = isHome ? 'home' : 'away';
        const parts = (p.participants || []).map((pt: any) => pt.athlete?.displayName || '');
        let x = p.fieldPositionX != null ? Math.round(p.fieldPositionX * 100) : null;
        let y = p.fieldPositionY != null ? Math.round(p.fieldPositionY * 100) : null;
        if (x == null || y == null) {
          if (isGoal) { x = isHome ? 92 : 8; y = 50; }
          else if (isShot) { x = isHome ? 80 : 20; y = 48; }
          else if (isCorner) { x = isHome ? 98 : 2; y = 6; }
          else if (isCard) { x = isHome ? 58 : 42; y = 50; }
          else { x = 50; y = isHome ? 90 : 10; }
        }
        seenIds.add(pId);
        parsedEvents.push({
          id: pId,
          type: isShot ? 'shot' : isCorner ? 'corner' : isGoal ? 'goal' : pType,
          type_text: p.type?.text || pType,
          clock: p.clock?.displayValue || c.time?.displayValue || '',
          period: p.period?.number || 1,
          side,
          scoring_play: isGoal,
          player: parts[0] || '',
          assist: parts.length > 1 ? parts[1] : null,
          text: c.text || p.text || '',
          x_coord: x,
          y_coord: y,
          target_x: isShot ? (isHome ? 100 : 0) : null,
          target_y: isShot ? 50 : null,
        });
      }

      const statusType = data.header?.competitions?.[0]?.status?.type;
      const displayClock = statusType?.detail || statusType?.shortDetail || null;
      const hScore = homeTeam?.score != null ? parseInt(String(homeTeam.score), 10) : null;
      const awayTeam = competitors.find((c: any) => c.homeAway === 'away');
      const aScore = awayTeam?.score != null ? parseInt(String(awayTeam?.score), 10) : null;
      const hPen = homeTeam?.shootoutScore != null ? parseInt(String(homeTeam.shootoutScore), 10) : null;
      const aPen = awayTeam?.shootoutScore != null ? parseInt(String(awayTeam?.shootoutScore), 10) : null;
      let liveStatus: string | null = null;
      if (statusType?.state === 'in') {
        liveStatus = statusType?.description?.toLowerCase().includes('half') ? 'PAUSED' : 'IN_PLAY';
      } else if (statusType?.state === 'post') {
        liveStatus = 'FINISHED';
      }

      const hColor = homeTeam?.team?.color ? `#${homeTeam.team.color.replace(/^#/, '')}` : null;
      const aColor = awayTeam?.team?.color ? `#${awayTeam.team.color.replace(/^#/, '')}` : null;

      return {
        statistics: hasStats ? statsObj : null,
        formations: hasFormations ? formationsObj : null,
        editorial_headline: headline,
        editorial_story: story,
        editorial_source: headline ? 'ESPN' : null,
        events: parsedEvents.length > 0 ? parsedEvents : null,
        home_score: hScore,
        away_score: aScore,
        home_color: hColor || formationsObj.home?.color || null,
        away_color: aColor || formationsObj.away?.color || null,
        home_penalties: hPen,
        away_penalties: aPen,
        display_clock: displayClock,
        status: liveStatus,
      };
    } catch (err) {
      console.warn(`[ESPN Summary] Failed to fetch slug ${slug} for event ${eventId}:`, err);
    }
  }

  return null;
}
