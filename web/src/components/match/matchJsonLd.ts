import { MatchRecord } from '@/lib/database';

export function generateMatchJsonLd(
  match: MatchRecord,
  homeName: string,
  awayName: string,
  compName: string,
  matchUrl: string,
) {
  return {
    '@context': 'https://schema.org',
    '@graph': [
      {
        '@type': 'SportsEvent',
        '@id': matchUrl,
        name: `${homeName} vs ${awayName}`,
        description: `Tactical analysis, highlights, and full match recap for ${homeName} vs ${awayName} in ${compName}.`,
        startDate: match.utc_date,
        eventStatus:
          match.status === 'FINISHED'
            ? 'https://schema.org/EventFinished'
            : ['IN_PLAY', 'PAUSED', 'LIVE'].includes(match.status)
            ? 'https://schema.org/EventLive'
            : 'https://schema.org/EventScheduled',
        competitor: [
          { '@type': 'SportsTeam', name: homeName, logo: match.home_team_crest },
          { '@type': 'SportsTeam', name: awayName, logo: match.away_team_crest },
        ],
        location: {
          '@type': 'Place',
          name: 'Stadium',
        },
        organizer: {
          '@type': 'Organization',
          name: compName,
        },
        offers: [
          {
            '@type': 'AggregateOffer',
            name: `${homeName} Official Fan Gear`,
            priceCurrency: 'USD',
            availability: 'https://schema.org/InStock',
            url: matchUrl,
          },
        ],
      },
      ...(match.highlight_video_id
        ? [
            {
              '@type': 'VideoObject',
              name: `${homeName} vs ${awayName} Official Highlights`,
              description: `Watch official match highlights from ${homeName} vs ${awayName} in ${compName}.`,
              thumbnailUrl: `https://i.ytimg.com/vi/${match.highlight_video_id}/hqdefault.jpg`,
              embedUrl: `https://www.youtube.com/embed/${match.highlight_video_id}`,
              uploadDate: match.utc_date,
            },
          ]
        : []),
    ],
  };
}
