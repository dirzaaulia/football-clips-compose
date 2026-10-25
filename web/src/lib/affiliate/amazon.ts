// FootballClips Amazon Associates Engine & Geo-Routing
export const AMAZON_ASSOCIATE_TAG = 'drzfc-20';
export const DEFAULT_MARKETPLACE = 'amazon.com';
export const AMAZON_AFFILIATE_DISCLOSURE =
  'As an Amazon Associate, FootballClips earns from qualifying purchases. We only recommend authentic gear and licensed club merchandise.';

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

const CLUB_KIT_IMAGES: Record<string, string> = {
  'arsenal': 'https://images.footballfanatics.com/arsenal/arsenal-adidas-home-shirt-2024-25_ss5_p-200924976+u-2xvdubqffy8gq6qvycct+v-3mffoeyy4lffh8hlyptn.jpg',
  'real madrid': 'https://images.footballfanatics.com/real-madrid/real-madrid-adidas-home-shirt-2024-25_ss5_p-200924956+u-7n36hvy7wqqi9o4y0zpt+v-mcvgflzrt8f8045vbt4q.jpg',
  'manchester city': 'https://images.footballfanatics.com/manchester-city/manchester-city-puma-home-shirt-2024-25_ss5_p-200924933+u-8f37h8d7j3j19i2z8vbt+v-2mv8flzrt8f8045vbt4q.jpg',
  'barcelona': 'https://images.footballfanatics.com/barcelona/barcelona-nike-home-stadium-shirt-2024-25_ss5_p-200924922+u-4f37h8d7j3j19i2z8vbt+v-4mv8flzrt8f8045vbt4q.jpg',
  'liverpool': 'https://images.footballfanatics.com/liverpool/liverpool-nike-home-stadium-shirt-2024-25_ss5_p-200924911+u-3f37h8d7j3j19i2z8vbt+v-1mv8flzrt8f8045vbt4q.jpg',
  'chelsea': 'https://images.footballfanatics.com/chelsea/chelsea-nike-home-stadium-shirt-2024-25_ss5_p-200924944+u-9f37h8d7j3j19i2z8vbt+v-5mv8flzrt8f8045vbt4q.jpg',
  'manchester united': 'https://images.footballfanatics.com/manchester-united/manchester-united-adidas-home-shirt-2024-25_ss5_p-200924901+u-1f37h8d7j3j19i2z8vbt+v-6mv8flzrt8f8045vbt4q.jpg',
  'bayern munich': 'https://images.footballfanatics.com/bayern-munich/fc-bayern-munich-adidas-home-shirt-2024-25_ss5_p-200924966+u-5f37h8d7j3j19i2z8vbt+v-7mv8flzrt8f8045vbt4q.jpg',
  'paris saint-germain': 'https://images.footballfanatics.com/paris-saint-germain/paris-saint-germain-nike-home-stadium-shirt-2024-25_ss5_p-200924988+u-6f37h8d7j3j19i2z8vbt+v-8mv8flzrt8f8045vbt4q.jpg',
  'juventus': 'https://images.footballfanatics.com/juventus/juventus-adidas-home-shirt-2024-25_ss5_p-200924999+u-1f37h8d7j3j19i2z8vbt+v-9mv8flzrt8f8045vbt4q.jpg',
  'ac milan': 'https://images.footballfanatics.com/ac-milan/ac-milan-puma-home-shirt-2024-25_ss5_p-200924981+u-2f37h8d7j3j19i2z8vbt+v-0mv8flzrt8f8045vbt4q.jpg',
  'inter milan': 'https://images.footballfanatics.com/inter-milan/inter-milan-nike-home-stadium-shirt-2024-25_ss5_p-200924970+u-3f37h8d7j3j19i2z8vbt+v-1mv8flzrt8f8045vbt4q.jpg',
  'tottenham hotspur': 'https://images.footballfanatics.com/tottenham-hotspur/tottenham-hotspur-nike-home-stadium-shirt-2024-25_ss5_p-200924950+u-4f37h8d7j3j19i2z8vbt+v-2mv8flzrt8f8045vbt4q.jpg',
  'borussia dortmund': 'https://images.footballfanatics.com/borussia-dortmund/borussia-dortmund-puma-home-shirt-2024-25_ss5_p-200924940+u-5f37h8d7j3j19i2z8vbt+v-3mv8flzrt8f8045vbt4q.jpg',
  'atletico madrid': 'https://images.footballfanatics.com/atletico-de-madrid/atletico-de-madrid-nike-home-stadium-shirt-2024-25_ss5_p-200924930+u-6f37h8d7j3j19i2z8vbt+v-4mv8flzrt8f8045vbt4q.jpg',
};

export function resolveClubKitPhoto(clubName: string, crestUrl?: string | null): string {
  if (!clubName) return crestUrl || '';
  const clean = clubName.toLowerCase().replace(/\b(fc|cf|afc|sc|sv|fk|ssc)\b/gi, '').trim();
  for (const [key, photo] of Object.entries(CLUB_KIT_IMAGES)) {
    if (clean.includes(key) || key.includes(clean)) {
      return photo;
    }
  }
  return crestUrl || '';
}

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

