import { GoogleGenAI } from '@google/genai';
import { MatchRecord } from './supabase';
import { findVerifiedMatchReport, generateAlgorithmicTacticalRecap } from './match-analysis';

const apiKey = process.env.GEMINI_API_KEY || '';
const ai = new GoogleGenAI({ apiKey });

const FALLBACK_MODELS = [
  'gemini-3.8-flash',
  'gemini-flash-latest',
  'gemini-3.7-flash',
];

async function generateWithTimeout(model: string, prompt: string, timeoutMs = 3000) {
  const apiCall = ai.models.generateContent({ model, contents: prompt });
  const timeout = new Promise<never>((_, reject) =>
    setTimeout(() => reject(new Error(`Timeout after ${timeoutMs}ms`)), timeoutMs)
  );
  return Promise.race([apiCall, timeout]);
}

export async function generateMatchRecap(match: MatchRecord): Promise<string> {
  const homeName = match.home_team_name || match.home_team || 'Home Team';
  const awayName = match.away_team_name || match.away_team || 'Away Team';
  const compName = match.competition_name || 'Official League';
  const isFinished = match.status === 'FINISHED';
  const isLive = match.status === 'IN_PLAY' || match.status === 'PAUSED' || match.status === 'LIVE';
  const isUpcoming = !isFinished && !isLive;

  // 1. Upcoming matches: Return preview summary without AI
  if (isUpcoming) {
    if (match.editorial_story && match.editorial_story.trim().length > 30) {
      const headline = match.editorial_headline ? `### ${match.editorial_headline}\n\n` : `### Match Preview: ${homeName} vs ${awayName}\n\n`;
      const source = match.editorial_source ? `\n\n*Source: ${match.editorial_source}.*` : '';
      return `${headline}${match.editorial_story}${source}`;
    }
    return generateAlgorithmicTacticalRecap(match);
  }

  // 2. Finished or Live matches: Prioritize authentic accredited ESPN recap/report if available
  if (match.editorial_story && match.editorial_story.trim().length > 30) {
    const headline = match.editorial_headline ? `### ${match.editorial_headline}\n\n` : `### Match Summary: ${homeName} vs ${awayName}\n\n`;
    const source = match.editorial_source ? `\n\n*Source: ${match.editorial_source}.*` : '';
    return `${headline}${match.editorial_story}${source}`;
  }

  // 3. If recap_analysis is already stored in DB, return immediately
  if (match.recap_analysis && match.recap_analysis.length > 50) {
    return match.recap_analysis;
  }

  // 4. Check for real verified journalism match report (BBC Sport, Sky Sports, The Guardian)
  const verifiedReport = await findVerifiedMatchReport(homeName, awayName);
  if (verifiedReport) {
    return `### Verified Match Journalism (${verifiedReport.source})
**${verifiedReport.title}**

${verifiedReport.description}

### Decisive Match Context
This encounter between **${homeName}** and **${awayName}** in ${compName} produced decisive moments documented by verified football correspondents.

### Tactical Overview & Impact
Both tactical setups reflected intense managerial preparation, dictating positional battles and shaping the ongoing race in ${compName}.

*Source: [Read full coverage on ${verifiedReport.source}](${verifiedReport.link})*`;
  }

  // 5. If Gemini API key is configured, attempt multi-tier AI generation for finished/live games
  if (apiKey && (isFinished || isLive)) {
    const scoreText = isFinished
      ? `Final Score: ${homeName} ${match.home_score ?? 0} - ${match.away_score ?? 0} ${awayName}`
      : `Live In-Play Score: ${homeName} ${match.home_score ?? 0} - ${match.away_score ?? 0} ${awayName}`;

    const prompt = `
You are a senior sports journalist and football tactician writing for a premier sports publication like The Athletic or BBC Sport.
Write an engaging, authoritative, and factually accurate match summary and tactical analysis based on the following real match details:

- Match: ${homeName} vs ${awayName}
- Competition: ${compName}
- Status: ${match.status} (${scoreText})
- Date: ${new Date(match.utc_date).toLocaleDateString('en-GB', { dateStyle: 'full' })}

Structure your article into 3 clean markdown sections:
1. ### Match Overview & Decisive Moments: A sharp narrative of how the match unfolded or key stakes.
2. ### Tactical Analysis: Break down the tactical battle (pressing triggers, midfield control, defensive structure).
3. ### Standings & Season Impact: What this specific result means for ${homeName} and ${awayName} in ${compName}.

Tone: Professional, analytical, captivating. Do not invent unrealistic players or events.
`;

    for (const model of FALLBACK_MODELS) {
      try {
        const response = await generateWithTimeout(model, prompt, 3000);
        const recap = response.text?.trim() || '';
        if (recap) {
          return recap;
        }
      } catch (error) {
        const errMsg = (error as Error).message || String(error);
        if (errMsg.includes('RESOURCE_EXHAUSTED') || errMsg.includes('429')) {
          break;
        }
        console.warn(`Gemini model ${model} skipped or timed out:`, errMsg);
      }
    }
  }

  // 4. Free, zero-cost Algorithmic Tactical Synthesis Engine fallback
  return generateAlgorithmicTacticalRecap(match);
}
