export interface EditorialArticle {
  slug: string;
  title: string;
  subtitle: string;
  author: {
    name: string;
    role: string;
    avatar: string;
    bio: string;
  };
  publishedAt: string;
  updatedAt: string;
  readTime: string;
  category: string;
  tags: string[];
  excerpt: string;
  heroImage: string;
  content: string[];
  keyTacticalTakeaways: string[];
  relatedLeague: string;
}
