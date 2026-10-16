export interface FeedArticle {
  id: string;
  title: string;
  description: string;
  link: string;
  source: string;
  pubDate: string;
  timestamp: number;
  category?: string;
}

const RSS_FEEDS = [
  { url: 'https://feeds.bbci.co.uk/sport/football/rss.xml', source: 'BBC Sport' },
  { url: 'https://www.skysports.com/rss/12040', source: 'Sky Sports' },
  { url: 'https://www.theguardian.com/football/rss', source: 'The Guardian' },
  { url: 'https://www.espn.com/espn/rss/soccer/news', source: 'ESPN FC' },
  { url: 'https://www.independent.co.uk/sport/football/rss', source: 'The Independent' },
  { url: 'https://football-italia.net/feed/', source: 'Football Italia' },
];

function cleanCdata(text: string): string {
  return text.replace(/<!\[CDATA\[([\s\S]*?)\]\]>/g, '$1').trim();
}

function extractTag(xml: string, tag: string): string {
  const regex = new RegExp(`<${tag}[^>]*>([\\s\\S]*?)<\\/${tag}>`, 'i');
  const match = xml.match(regex);
  return match ? cleanCdata(match[1]) : '';
}

export async function getVerifiedFootballNews(limit = 16): Promise<FeedArticle[]> {
  const feedPromises = RSS_FEEDS.map(async (feed) => {
    try {
      const res = await fetch(feed.url, {
        headers: {
          'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
          'Accept': 'application/rss+xml, application/xml, text/xml',
        },
        next: { revalidate: 300 }, // Cache 5 mins
      });

      if (!res.ok) return [];

      const xml = await res.text();
      const itemMatches = xml.match(/<item[\s\S]*?<\/item>/gi) || [];
      const feedArticles: FeedArticle[] = [];

      for (const itemXml of itemMatches.slice(0, 10)) {
        const title = extractTag(itemXml, 'title');
        const description = extractTag(itemXml, 'description');
        const link = extractTag(itemXml, 'link') || extractTag(itemXml, 'guid');
        const pubDateRaw = extractTag(itemXml, 'pubDate');
        const category = extractTag(itemXml, 'category') || 'Football News';

        if (title && link) {
          const parsedTime = pubDateRaw ? new Date(pubDateRaw).getTime() : 0;
          feedArticles.push({
            id: `news-${feed.source.replace(/\s+/g, '-').toLowerCase()}-${Buffer.from(link).toString('base64').slice(0, 10)}`,
            title,
            description: description.replace(/<[^>]*>?/gm, '').replace(/&nbsp;/g, ' ').trim(),
            link,
            source: feed.source,
            pubDate: pubDateRaw ? new Date(pubDateRaw).toLocaleDateString('en-GB', { dateStyle: 'medium' }) : '',
            timestamp: !isNaN(parsedTime) && parsedTime > 0 ? parsedTime : Date.now(),
            category,
          });
        }
      }
      return feedArticles;
    } catch (err) {
      console.warn(`[Articles] Failed fetching feed ${feed.source}:`, err);
      return [];
    }
  });

  const results = await Promise.allSettled(feedPromises);
  const feedBuckets: FeedArticle[][] = [];

  for (const r of results) {
    if (r.status === 'fulfilled' && r.value.length > 0) {
      feedBuckets.push(r.value);
    }
  }

  // Interleave/round-robin across distinct sources so no single source dominates
  const interleaved: FeedArticle[] = [];
  const seenTitles = new Set<string>();
  let hasMore = true;
  let round = 0;

  while (hasMore && interleaved.length < limit) {
    hasMore = false;
    for (const bucket of feedBuckets) {
      if (round < bucket.length) {
        hasMore = true;
        const art = bucket[round];
        const key = art.title.toLowerCase().trim();
        if (!seenTitles.has(key)) {
          seenTitles.add(key);
          interleaved.push(art);
          if (interleaved.length >= limit) break;
        }
      }
    }
    round++;
  }

  return interleaved;
}
