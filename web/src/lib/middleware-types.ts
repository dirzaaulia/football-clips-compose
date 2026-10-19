export interface MiddlewareTeam {
  id: number;
  name: string;
  logo: string | null;
}

export interface MiddlewareLeague {
  id: number;
  name: string;
  logo: string | null;
  season?: number;
}

export interface MiddlewareCountry {
  code: string;
  name: string;
  logo: string | null;
}

export interface MiddlewareMatch {
  id: number;
  date: string;
  round?: string;
  homeTeam: MiddlewareTeam;
  awayTeam: MiddlewareTeam;
  league: MiddlewareLeague;
  country: MiddlewareCountry;
}

export interface MiddlewareHighlight {
  id: number;
  title: string;
  category: string;
  channel: string;
  url: string;
  embedUrl: string;
  imgUrl: string;
  source: string;
  type: string;
  match?: MiddlewareMatch;
}

export interface MiddlewareResponse {
  status: string;
  total_verified: number;
  pagination: { limit: number; offset: number; totalCount: number };
  data: MiddlewareHighlight[];
}

export interface MiddlewareFetchResult {
  items: MiddlewareHighlight[];
  hasMore: boolean;
  totalCount: number;
}
