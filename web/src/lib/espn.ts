export const ESPN_COMPETITION_SLUGS: Record<string, string> = {
  CL: 'uefa.champions',
  EL: 'uefa.europa',
  ECL: 'uefa.ecl',
  PL: 'eng.1',
  PD: 'esp.1',
  SA: 'ita.1',
  BL1: 'ger.1',
  FL1: 'fra.1',
  SPL: 'ksa.1',
  MLS: 'usa.1',
  DED: 'ned.1',
  PPL: 'por.1',
};

export interface EspnSummaryResult {
  statistics?: {
    home?: Record<string, string>;
    away?: Record<string, string>;
  } | null;
  formations?: {
    home?: { formation?: string; starters?: Array<{ name: string; jersey?: string; position?: string }> };
    away?: { formation?: string; starters?: Array<{ name: string; jersey?: string; position?: string }> };
  } | null;
  editorial_headline?: string | null;
  editorial_story?: string | null;
  editorial_source?: string | null;
}

export async function fetchEspnMatchSummary(
  eventId: string | number,
  competitionCode?: string
): Promise<EspnSummaryResult | null> {
  const primarySlug = competitionCode ? ESPN_COMPETITION_SLUGS[competitionCode.toUpperCase()] : undefined;
  const slugsToTry = primarySlug
    ? [primarySlug]
    : ['uefa.champions', 'uefa.europa', 'eng.1', 'esp.1', 'ita.1', 'ger.1', 'fra.1', 'ksa.1', 'usa.1', 'ned.1', 'por.1'];

  for (const slug of slugsToTry) {
    try {
      const url = `https://site.api.espn.com/apis/site/v2/sports/soccer/${slug}/summary?event=${eventId}`;
      const res = await fetch(url, { headers: { 'User-Agent': 'Mozilla/5.0' }, next: { revalidate: 300 } });
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
        formationsObj[side] = {
          formation: r.formation,
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

      return {
        statistics: hasStats ? statsObj : null,
        formations: hasFormations ? formationsObj : null,
        editorial_headline: headline,
        editorial_story: story,
        editorial_source: headline ? 'ESPN' : null,
      };
    } catch (err) {
      console.warn(`[ESPN Summary] Failed to fetch slug ${slug} for event ${eventId}:`, err);
    }
  }

  return null;
}
