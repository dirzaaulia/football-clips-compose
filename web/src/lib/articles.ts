export interface FeedArticle {
  id: string;
  title: string;
  description: string;
  link: string;
  source: string;
  pubDate: string;
  category?: string;
}

const RSS_FEEDS = [
  { url: 'https://feeds.bbci.co.uk/sport/football/rss.xml', source: 'BBC Sport' },
  { url: 'https://www.skysports.com/rss/12040', source: 'Sky Sports' },
  { url: 'https://www.theguardian.com/football/rss', source: 'The Guardian' },
  { url: 'https://www.independent.co.uk/sport/football/rss', source: 'The Independent' },
];

function cleanCdata(text: string): string {
  return text.replace(/<!\[CDATA\[([\s\S]*?)\]\]>/g, '$1').trim();
}

function extractTag(xml: string, tag: string): string {
  const regex = new RegExp(`<${tag}[^>]*>([\\s\\S]*?)<\\/${tag}>`, 'i');
  const match = xml.match(regex);
  return match ? cleanCdata(match[1]) : '';
}

export async function getVerifiedFootballNews(limit = 12): Promise<FeedArticle[]> {
  const articles: FeedArticle[] = [];

  for (const feed of RSS_FEEDS) {
    try {
      const res = await fetch(feed.url, {
        next: { revalidate: 300 }, // Cache 5 mins
      });

      if (!res.ok) continue;

      const xml = await res.text();
      const itemMatches = xml.match(/<item[\s\S]*?<\/item>/gi) || [];

      for (const itemXml of itemMatches) {
        const title = extractTag(itemXml, 'title');
        const description = extractTag(itemXml, 'description');
        const link = extractTag(itemXml, 'link') || extractTag(itemXml, 'guid');
        const pubDate = extractTag(itemXml, 'pubDate');
        const category = extractTag(itemXml, 'category') || 'Football News';

        if (title && link) {
          articles.push({
            id: `news-${articles.length}-${Buffer.from(link).toString('base64').slice(0, 10)}`,
            title,
            description: description.replace(/<[^>]*>?/gm, ''), // strip any inline html
            link,
            source: feed.source,
            pubDate: pubDate ? new Date(pubDate).toLocaleDateString('en-GB', { dateStyle: 'medium' }) : '',
            category,
          });
        }
      }
    } catch (err) {
      console.warn(`Failed fetching RSS feed ${feed.source}:`, err);
    }
  }

  // Deduplicate by title & return latest
  const seen = new Set<string>();
  const uniqueArticles: FeedArticle[] = [];

  for (const art of articles) {
    const key = art.title.toLowerCase().trim();
    if (!seen.has(key)) {
      seen.add(key);
      uniqueArticles.push(art);
    }
    if (uniqueArticles.length >= limit) break;
  }

  return uniqueArticles;
}
