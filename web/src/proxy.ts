import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

export function proxy(request: NextRequest) {
  const response = NextResponse.next();

  // 1. Edge Security & Performance Headers
  response.headers.set('X-Frame-Options', 'SAMEORIGIN');
  response.headers.set('X-Content-Type-Options', 'nosniff');
  response.headers.set('Referrer-Policy', 'strict-origin-when-cross-origin');

  // 2. Cache-Control for CDN Edge Caching (Firebase / Edge CDN)
  const pathname = request.nextUrl.pathname;

  // Static assets and fonts
  if (pathname.startsWith('/_next/static') || pathname.match(/\.(ico|png|jpg|svg|css|js)$/)) {
    response.headers.set('Cache-Control', 'public, max-age=31536000, immutable');
  } 
  // Finished matches & articles - long edge cache, stale-while-revalidate
  else if (pathname.startsWith('/match/') || pathname.startsWith('/articles')) {
    response.headers.set('Cache-Control', 'public, s-maxage=86400, stale-while-revalidate=3600');
  } 
  // Fixtures & Standings - medium edge cache
  else if (pathname.startsWith('/fixtures') || pathname.startsWith('/standings')) {
    response.headers.set('Cache-Control', 'public, s-maxage=60, stale-while-revalidate=300');
  } 
  // Homepage Highlights feed
  else {
    response.headers.set('Cache-Control', 'public, s-maxage=120, stale-while-revalidate=600');
  }

  // 3. Pass Spoiler-Free state from cookie if present
  const spoilerFreeCookie = request.cookies.get('fc-spoiler-free');
  if (spoilerFreeCookie) {
    response.headers.set('x-spoiler-free', spoilerFreeCookie.value);
  }

  return response;
}

export const config = {
  matcher: [
    /*
     * Match all request paths except for:
     * - api routes (if any)
     * - _next/image (image optimization files)
     * - favicon.ico, sitemap.xml, robots.txt
     */
    '/((?!api|_next/image|favicon.ico|sitemap.xml|robots.txt).*)',
  ],
};
