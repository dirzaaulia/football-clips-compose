// Official Club Kit Photography Registry for Supported Clubs
// Maps club keywords to verified, high-resolution official kit photography

const CLUB_KIT_REGISTRY: Record<string, string> = {
  // Premier League
  'manchester city': 'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_500,h_500/global/774984/01/fnd/EEA/fmt/png',
  'man city': 'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_500,h_500/global/774984/01/fnd/EEA/fmt/png',

  // Serie A
  'milan': 'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_500,h_500/global/775037/01/fnd/EEA/fmt/png',
  'ac milan': 'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_500,h_500/global/775037/01/fnd/EEA/fmt/png',

  // Bundesliga
  'dortmund': 'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_500,h_500/global/775075/01/fnd/EEA/fmt/png',
  'borussia dortmund': 'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_500,h_500/global/775075/01/fnd/EEA/fmt/png',

  // La Liga
  'valencia': 'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_500,h_500/global/774959/01/fnd/EEA/fmt/png',

  // Ligue 1
  'marseille': 'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_500,h_500/global/774996/01/fnd/EEA/fmt/png',
  'lens': 'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_500,h_500/global/775003/01/fnd/EEA/fmt/png',
};

/**
 * Resolves verified kit photography for a club, or gracefully falls back to crestUrl.
 */
export function resolveClubKitPhoto(clubName: string, crestUrl?: string | null): string {
  if (!clubName) return crestUrl || '';
  const clean = clubName
    .toLowerCase()
    .replace(/\b(fc|cf|afc|sc|sv|fk|ssc|osc|rc|cd|ca)\b/gi, '')
    .trim();

  for (const [key, photo] of Object.entries(CLUB_KIT_REGISTRY)) {
    if (photo && (clean.includes(key) || key.includes(clean))) {
      return photo;
    }
  }

  return crestUrl || '';
}
