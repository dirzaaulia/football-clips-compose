import { MatchRecord } from './database';
import { getVerifiedFootballNews, FeedArticle } from './articles';

/**
 * Searches real journalism feeds (BBC Sport, Sky Sports, The Guardian, The Independent)
 * for authentic match reports and tactical articles mentioning both teams.
 */
export async function findVerifiedMatchReport(
  homeTeam: string,
  awayTeam: string
): Promise<FeedArticle | null> {
  try {
    const articles = await getVerifiedFootballNews(25);
    const h = homeTeam.toLowerCase().trim();
    const a = awayTeam.toLowerCase().trim();

    // Simplify team names for fuzzy matching (e.g. "Arsenal FC" -> "arsenal")
    const cleanTeam = (name: string) =>
      name
        .toLowerCase()
        .replace(/\b(fc|afc|cf|bc|sc|united|city|hotspur)\b/gi, '')
        .trim();

    const hClean = cleanTeam(h);
    const aClean = cleanTeam(a);

    for (const art of articles) {
      const text = `${art.title} ${art.description}`.toLowerCase();
      const hasHome = (hClean.length >= 3 && text.includes(hClean)) || text.includes(h);
      const hasAway = (aClean.length >= 3 && text.includes(aClean)) || text.includes(a);

      if (hasHome && hasAway) {
        return art;
      }
    }
  } catch (err) {
    console.warn('Failed to match verified RSS report:', err);
  }
  return null;
}

/** Build a factual fallback from catalog fields when a sourced report is unavailable. */
export function generateAlgorithmicTacticalRecap(match: MatchRecord): string {
  const home = match.home_team_name || match.home_team || 'Home Team';
  const away = match.away_team_name || match.away_team || 'Away Team';
  const competition = match.competition_name || 'Football';
  const finished = match.status === 'FINISHED';
  const live = ['IN_PLAY', 'PAUSED', 'LIVE'].includes(match.status);
  const state = finished ? 'Full time' : live ? 'In progress' : match.status.replaceAll('_', ' ').toLowerCase();
  const scoreKnown = match.home_score != null && match.away_score != null;
  const score = scoreKnown
    ? `**${home} ${match.home_score} - ${match.away_score} ${away}**`
    : 'The score is not yet available.';
  return `### Match summary
${home} vs ${away} in ${competition}. Status: ${state}.

${score}

### Match coverage
${match.highlight_video_id ? 'The available match video is shown above.' : 'A match video is not currently linked.'}
A detailed match report is not available for this summary. Formations, substitutions and tactical conclusions have not been inferred from the score.

*Football Highlights & Clips · Catalog summary.*`;
}
