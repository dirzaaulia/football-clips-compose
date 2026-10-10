import { GoogleGenAI } from '@google/genai';
import { MatchRecord, supabase } from './supabase';

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
  // If already generated and cached in Supabase, return it immediately
  if (match.recap_analysis && match.recap_analysis.length > 50) {
    return match.recap_analysis;
  }

  const defaultSummary = `Official match highlights and tactical breakdown between ${match.home_team} and ${match.away_team} in the ${match.competition_name}. Final score ended ${match.home_score ?? 0} - ${match.away_score ?? 0}. Key goals, pressing duels, and decisive match moments captured in full.`;

  if (!apiKey) {
    return defaultSummary;
  }

  const prompt = `
You are a senior sports journalist and football tactician writing for a premier sports publication like The Athletic or BBC Sport.
Write an engaging, authoritative, and factually accurate match summary and tactical analysis based on the following real match details:

- Match: ${match.home_team} vs ${match.away_team}
- Competition: ${match.competition_name}
- Score: ${match.home_team} ${match.home_score ?? 0} - ${match.away_score ?? 0} ${match.away_team}
- Date: ${new Date(match.utc_date).toLocaleDateString('en-GB', { dateStyle: 'full' })}
- Status: ${match.status}

Structure your article into 3 clean markdown sections:
1. ### Match Overview & Decisive Moments: A sharp narrative of how the match unfolded, the intensity of both teams, and how key events dictated the outcome.
2. ### Tactical Analysis: Break down the tactical battle (pressing triggers, midfield control, defensive structure, and substitutions).
3. ### Standings & Season Impact: What this specific result means for ${match.home_team} and ${match.away_team} moving forward in the ${match.competition_name}.

Tone: Professional, captivating, analytical, and respectful. Do not invent unrealistic players or events; focus on tactical momentum and the final scoreline.
`;

  // Resilient multi-tier model cascade
  for (const model of FALLBACK_MODELS) {
    try {
      const response = await generateWithTimeout(model, prompt, 3000);
      const recap = response.text?.trim() || '';
      if (recap) {
        // Cache to Supabase for permanent free retention
        if (match.id) {
          await supabase
            .from('matches')
            .update({ recap_analysis: recap })
            .eq('id', match.id);
        }
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

  return defaultSummary;
}
