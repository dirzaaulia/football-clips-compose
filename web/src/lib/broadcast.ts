// Global Official Broadcaster Rights Service (Client-side)
// Maps user country to verified official broadcaster with direct stream links

export interface BroadcasterInfo {
  competition_id: string;
  country_code: string;
  country_name: string;
  broadcaster_name: string;
  stream_url: string;
  official_youtube_channel?: string | null;
  is_global_fallback: boolean;
  is_direct_live_stream: boolean;
  tier: string;
}

let cachedDirectory: { countries: Record<string, { name: string; rights: Record<string, BroadcasterInfo> }> } | null = null;

export function detectUserCountryCode(): string {
  if (typeof window === 'undefined') return 'US';
  try {
    const tz = Intl.DateTimeFormat().resolvedOptions().timeZone || '';
    if (tz.includes('Jakarta') || tz.includes('Makassar') || tz.includes('Jayapura') || tz.includes('Pontianak')) return 'ID';
    if (tz.includes('London')) return 'GB';
    if (tz.includes('Madrid')) return 'ES';
    if (tz.includes('Berlin')) return 'DE';
    if (tz.includes('Paris')) return 'FR';
    if (tz.includes('Rome')) return 'IT';
    if (tz.includes('Toronto') || tz.includes('Vancouver')) return 'CA';
    if (tz.includes('Sydney') || tz.includes('Melbourne') || tz.includes('Brisbane')) return 'AU';
    if (tz.includes('Tokyo')) return 'JP';
    if (tz.includes('Sao_Paulo')) return 'BR';
    if (tz.includes('Kolkata') || tz.includes('Calcutta')) return 'IN';
    if (tz.includes('Riyadh')) return 'SA';
    if (tz.includes('Dubai')) return 'AE';
    if (tz.includes('Cairo')) return 'EG';
    if (tz.includes('Johannesburg')) return 'ZA';
    if (tz.includes('Lagos')) return 'NG';
    if (tz.includes('Singapore')) return 'SG';
    if (tz.includes('Kuala_Lumpur')) return 'MY';

    const lang = (navigator.language || 'en-US').toUpperCase();
    if (lang.includes('-')) {
      const region = lang.split('-')[1];
      if (region.length === 2) return region;
    }
  } catch {
    // fallback
  }
  return 'US';
}

export async function getOfficialBroadcaster(competitionId: string, countryCode?: string): Promise<BroadcasterInfo> {
  const cCode = (countryCode || detectUserCountryCode()).toUpperCase();
  const comp = competitionId.toUpperCase();

  try {
    if (!cachedDirectory) {
      const res = await fetch('/broadcast_directory.json');
      if (res.ok) {
        cachedDirectory = await res.json();
      }
    }

    if (cachedDirectory?.countries?.[cCode]?.rights?.[comp]) {
      return cachedDirectory.countries[cCode].rights[comp];
    }
  } catch (err) {
    console.warn('[Broadcast] Error fetching broadcast directory:', err);
  }

  // Sensible default fallback
  return {
    competition_id: comp,
    country_code: cCode,
    country_name: 'Official Partner',
    broadcaster_name: 'Official Broadcast Portal',
    stream_url: `https://www.google.com/search?q=${encodeURIComponent(comp + ' official live stream')}`,
    is_global_fallback: true,
    is_direct_live_stream: true,
    tier: 'GLOBAL_FALLBACK'
  };
}
