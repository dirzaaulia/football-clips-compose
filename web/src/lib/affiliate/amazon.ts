// Football Highlights & Clips Amazon Associates Engine & Geo-Routing
export const AMAZON_ASSOCIATE_TAG = 'drzfc-20';
export const DEFAULT_MARKETPLACE = 'amazon.com';
export const AMAZON_AFFILIATE_DISCLOSURE =
  'As an Amazon Associate, Football Highlights & Clips earns from qualifying purchases. We only recommend authentic gear and licensed club merchandise.';

export const AMAZON_MARKETPLACES: Record<string, string> = {
  US: 'amazon.com',
  CA: 'amazon.ca',
  MX: 'amazon.com.mx',
  BR: 'amazon.com.br',
  GB: 'amazon.co.uk',
  DE: 'amazon.de',
  FR: 'amazon.fr',
  ES: 'amazon.es',
  IT: 'amazon.it',
  NL: 'amazon.nl',
  PL: 'amazon.pl',
  SE: 'amazon.se',
  BE: 'amazon.com.be',
  TR: 'amazon.com.tr',
  JP: 'amazon.co.jp',
  IN: 'amazon.in',
  AU: 'amazon.com.au',
  SG: 'amazon.sg',
  AE: 'amazon.ae',
  SA: 'amazon.sa',
  EG: 'amazon.eg',
};

const REGIONAL_FALLBACK_MAP: Record<string, string> = {
  IE: 'amazon.co.uk',
  AT: 'amazon.de',
  CH: 'amazon.de',
  CZ: 'amazon.de',
  HU: 'amazon.de',
  SK: 'amazon.de',
  SI: 'amazon.de',
  HR: 'amazon.de',
  RO: 'amazon.de',
  BG: 'amazon.de',
  GR: 'amazon.de',
  CY: 'amazon.de',
  NO: 'amazon.se',
  DK: 'amazon.de',
  FI: 'amazon.de',
  EE: 'amazon.de',
  LV: 'amazon.de',
  LT: 'amazon.pl',
  PT: 'amazon.es',
  LU: 'amazon.fr',
  MC: 'amazon.fr',
  NZ: 'amazon.com.au',
  MY: 'amazon.sg',
  ID: 'amazon.com',
  PH: 'amazon.com',
  TH: 'amazon.com',
  VN: 'amazon.com',
  KR: 'amazon.co.jp',
  TW: 'amazon.co.jp',
  HK: 'amazon.com',
  QA: 'amazon.ae',
  KW: 'amazon.ae',
  BH: 'amazon.ae',
  OM: 'amazon.ae',
  JO: 'amazon.ae',
  LB: 'amazon.ae',
  AR: 'amazon.com.br',
  CL: 'amazon.com',
  CO: 'amazon.com',
  PE: 'amazon.com',
  UY: 'amazon.com.br',
};

export { resolveClubKitPhoto } from './clubKits';

export function resolveAmazonMarketplace(country?: string | null): string {
  if (!country) return DEFAULT_MARKETPLACE;
  const upper = country.toUpperCase();
  if (AMAZON_MARKETPLACES[upper]) return AMAZON_MARKETPLACES[upper];
  if (REGIONAL_FALLBACK_MAP[upper]) return REGIONAL_FALLBACK_MAP[upper];
  return DEFAULT_MARKETPLACE;
}

export function buildClubGearUrl(
  clubName: string,
  gearType: 'jersey' | 'scarf' | 'hat' | 'gear' = 'jersey',
  country?: string | null
): string {
  const marketplace = resolveAmazonMarketplace(country);
  const cleanClub = clubName.replace(/\b(FC|CF|AFC|SC|SV|FK|SSC)\b/gi, '').trim();
  const query = encodeURIComponent(`${cleanClub} official ${gearType}`);
  return `https://${marketplace}/s?k=${query}&tag=${AMAZON_ASSOCIATE_TAG}`;
}

