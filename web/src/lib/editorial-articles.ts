import { EditorialArticle } from './editorial-articles/types';
import { PREMIER_LEAGUE_ARTICLES } from './editorial-articles/premier-league';
import { CHAMPIONS_LEAGUE_ARTICLES } from './editorial-articles/champions-league';
import { LA_LIGA_ARTICLES } from './editorial-articles/la-liga';
import { SERIE_A_BUNDESLIGA_ARTICLES } from './editorial-articles/serie-a-bundesliga';
import { TACTICAL_SCIENCE_ARTICLES } from './editorial-articles/tactical-science';
import { EUROPEAN_INNOVATIONS_ARTICLES } from './editorial-articles/european-innovations';

export type { EditorialArticle };

export const EDITORIAL_ARTICLES: EditorialArticle[] = [
  ...PREMIER_LEAGUE_ARTICLES,
  ...CHAMPIONS_LEAGUE_ARTICLES,
  ...LA_LIGA_ARTICLES,
  ...SERIE_A_BUNDESLIGA_ARTICLES,
  ...TACTICAL_SCIENCE_ARTICLES,
  ...EUROPEAN_INNOVATIONS_ARTICLES,
];

export function getAllArticles(): EditorialArticle[] {
  return EDITORIAL_ARTICLES;
}

export function getArticleBySlug(slug: string): EditorialArticle | undefined {
  return EDITORIAL_ARTICLES.find((article) => article.slug === slug);
}
