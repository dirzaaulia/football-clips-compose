export interface SupportedClub {
  id: string;
  name: string;
  shortName: string;
  leagueCode: string;
  leagueName: string;
  crestUrl: string;
  aliases: string[];
}

function c(
  id: string,
  name: string,
  shortName: string,
  leagueCode: string,
  leagueName: string,
  crestUrl: string,
  ...aliases: string[]
): SupportedClub {
  return { id, name, shortName, leagueCode, leagueName, crestUrl, aliases };
}

export const SUPPORTED_CLUBS: SupportedClub[] = [
  // Premier League
  c('arsenal_fc', 'Arsenal FC', 'Arsenal', 'PL', 'Premier League', 'https://crests.football-data.org/57.png', 'Arsenal'),
  c('aston_villa_fc', 'Aston Villa FC', 'Aston Villa', 'PL', 'Premier League', 'https://crests.football-data.org/58.png', 'Aston Villa'),
  c('chelsea_fc', 'Chelsea FC', 'Chelsea', 'PL', 'Premier League', 'https://crests.football-data.org/61.png', 'Chelsea'),
  c('liverpool_fc', 'Liverpool FC', 'Liverpool', 'PL', 'Premier League', 'https://crests.football-data.org/64.png', 'Liverpool'),
  c('manchester_city_fc', 'Manchester City FC', 'Manchester City', 'PL', 'Premier League', 'https://crests.football-data.org/65.png', 'Man City', 'City'),
  c('manchester_united_fc', 'Manchester United FC', 'Man United', 'PL', 'Premier League', 'https://crests.football-data.org/66.png', 'Man Utd', 'United'),
  c('newcastle_united_fc', 'Newcastle United FC', 'Newcastle', 'PL', 'Premier League', 'https://crests.football-data.org/67.png', 'Newcastle'),
  c('tottenham_hotspur_fc', 'Tottenham Hotspur FC', 'Tottenham', 'PL', 'Premier League', 'https://crests.football-data.org/73.png', 'Spurs'),
  c('brighton_hove_albion_fc', 'Brighton & Hove Albion FC', 'Brighton', 'PL', 'Premier League', 'https://crests.football-data.org/397.png'),
  c('brentford_fc', 'Brentford FC', 'Brentford', 'PL', 'Premier League', 'https://crests.football-data.org/402.png'),
  c('crystal_palace_fc', 'Crystal Palace FC', 'Crystal Palace', 'PL', 'Premier League', 'https://crests.football-data.org/354.png'),
  c('everton_fc', 'Everton FC', 'Everton', 'PL', 'Premier League', 'https://crests.football-data.org/62.png'),
  c('fulham_fc', 'Fulham FC', 'Fulham', 'PL', 'Premier League', 'https://crests.football-data.org/63.png'),
  c('nottingham_forest_fc', 'Nottingham Forest FC', 'Nottm Forest', 'PL', 'Premier League', 'https://crests.football-data.org/351.png'),
  c('west_ham_united_fc', 'West Ham United FC', 'West Ham', 'PL', 'Premier League', 'https://crests.football-data.org/563.png'),

  // La Liga
  c('real_madrid_cf', 'Real Madrid CF', 'Real Madrid', 'PD', 'La Liga', 'https://crests.football-data.org/86.png', 'Madrid'),
  c('fc_barcelona', 'FC Barcelona', 'Barcelona', 'PD', 'La Liga', 'https://crests.football-data.org/81.png', 'Barca'),
  c('club_atl_tico_de_madrid', 'Club Atlético de Madrid', 'Atlético Madrid', 'PD', 'La Liga', 'https://crests.football-data.org/78.png', 'Atletico'),
  c('athletic_club', 'Athletic Club', 'Athletic Club', 'PD', 'La Liga', 'https://crests.football-data.org/77.png', 'Bilbao'),
  c('real_sociedad_de_f_tbol', 'Real Sociedad', 'Real Sociedad', 'PD', 'La Liga', 'https://crests.football-data.org/92.png'),
  c('sevilla_fc', 'Sevilla FC', 'Sevilla', 'PD', 'La Liga', 'https://crests.football-data.org/559.png'),
  c('valencia_cf', 'Valencia CF', 'Valencia', 'PD', 'La Liga', 'https://crests.football-data.org/95.png'),
  c('villarreal_cf', 'Villarreal CF', 'Villarreal', 'PD', 'La Liga', 'https://crests.football-data.org/94.png'),
  c('real_betis_balompi', 'Real Betis', 'Real Betis', 'PD', 'La Liga', 'https://crests.football-data.org/90.png', 'Betis'),

  // Serie A
  c('fc_internazionale_milano', 'FC Internazionale Milano', 'Inter', 'SA', 'Serie A', 'https://crests.football-data.org/108.png', 'Inter Milan'),
  c('ac_milan', 'AC Milan', 'AC Milan', 'SA', 'Serie A', 'https://crests.football-data.org/98.png', 'Milan'),
  c('juventus_fc', 'Juventus FC', 'Juventus', 'SA', 'Serie A', 'https://crests.football-data.org/109.png', 'Juve'),
  c('ssc_napoli', 'SSC Napoli', 'Napoli', 'SA', 'Serie A', 'https://crests.football-data.org/113.png'),
  c('as_roma', 'AS Roma', 'AS Roma', 'SA', 'Serie A', 'https://crests.football-data.org/100.png', 'Roma'),
  c('ss_lazio', 'SS Lazio', 'Lazio', 'SA', 'Serie A', 'https://crests.football-data.org/110.png'),
  c('atalanta_bc', 'Atalanta BC', 'Atalanta', 'SA', 'Serie A', 'https://crests.football-data.org/102.png'),
  c('acf_fiorentina', 'ACF Fiorentina', 'Fiorentina', 'SA', 'Serie A', 'https://crests.football-data.org/99.png'),

  // Bundesliga
  c('fc_bayern_m_nchen', 'FC Bayern München', 'Bayern Munich', 'BL1', 'Bundesliga', 'https://crests.football-data.org/5.png', 'Bayern'),
  c('bayer_04_leverkusen', 'Bayer 04 Leverkusen', 'Leverkusen', 'BL1', 'Bundesliga', 'https://crests.football-data.org/3.png'),
  c('borussia_dortmund', 'Borussia Dortmund', 'Dortmund', 'BL1', 'Bundesliga', 'https://crests.football-data.org/4.png', 'BVB'),
  c('rb_leipzig', 'RB Leipzig', 'RB Leipzig', 'BL1', 'Bundesliga', 'https://crests.football-data.org/721.png', 'Leipzig'),
  c('eintracht_frankfurt', 'Eintracht Frankfurt', 'Frankfurt', 'BL1', 'Bundesliga', 'https://crests.football-data.org/19.png'),
  c('vfb_stuttgart', 'VfB Stuttgart', 'Stuttgart', 'BL1', 'Bundesliga', 'https://crests.football-data.org/10.png'),

  // Ligue 1
  c('paris_saint_germain_fc', 'Paris Saint-Germain FC', 'PSG', 'FL1', 'Ligue 1', 'https://crests.football-data.org/524.png', 'Paris SG'),
  c('olympique_de_marseille', 'Olympique de Marseille', 'Marseille', 'FL1', 'Ligue 1', 'https://crests.football-data.org/516.png'),
  c('as_monaco_fc', 'AS Monaco FC', 'Monaco', 'FL1', 'Ligue 1', 'https://crests.football-data.org/548.png'),
  c('olympique_lyonnais', 'Olympique Lyonnais', 'Lyon', 'FL1', 'Ligue 1', 'https://crests.football-data.org/523.png'),

  // Saudi Pro League (SPL)
  c('al_hilal_sfc', 'Al Hilal SFC', 'Al Hilal', 'SPL', 'Saudi Pro League', 'https://a.espncdn.com/i/teamlogos/soccer/500/7339.png', 'Al-Hilal'),
  c('al_nassr_fc', 'Al Nassr FC', 'Al Nassr', 'SPL', 'Saudi Pro League', 'https://a.espncdn.com/i/teamlogos/soccer/500/7342.png', 'Al-Nassr'),
  c('al_ittihad_fc', 'Al Ittihad FC', 'Al Ittihad', 'SPL', 'Saudi Pro League', 'https://a.espncdn.com/i/teamlogos/soccer/500/7338.png', 'Al-Ittihad'),
  c('al_ahli_saudi_fc', 'Al Ahli Saudi FC', 'Al Ahli', 'SPL', 'Saudi Pro League', 'https://a.espncdn.com/i/teamlogos/soccer/500/7337.png', 'Al-Ahli'),
  c('al_shabab_fc', 'Al Shabab FC', 'Al Shabab', 'SPL', 'Saudi Pro League', 'https://a.espncdn.com/i/teamlogos/soccer/500/7343.png', 'Al-Shabab'),
  c('al_ettifaq_fc', 'Al Ettifaq FC', 'Al Ettifaq', 'SPL', 'Saudi Pro League', 'https://a.espncdn.com/i/teamlogos/soccer/500/7335.png', 'Al-Ettifaq'),

  // MLS (MLS)
  c('inter_miami_cf', 'Inter Miami CF', 'Inter Miami', 'MLS', 'MLS', 'https://a.espncdn.com/i/teamlogos/soccer/500/19106.png', 'Inter Miami'),
  c('la_galaxy', 'LA Galaxy', 'LA Galaxy', 'MLS', 'MLS', 'https://a.espncdn.com/i/teamlogos/soccer/500/187.png', 'Galaxy'),
  c('los_angeles_fc', 'Los Angeles FC', 'LAFC', 'MLS', 'MLS', 'https://a.espncdn.com/i/teamlogos/soccer/500/18966.png', 'LAFC'),
  c('columbus_crew', 'Columbus Crew', 'Columbus Crew', 'MLS', 'MLS', 'https://a.espncdn.com/i/teamlogos/soccer/500/183.png'),
  c('new_york_red_bulls', 'New York Red Bulls', 'NY Red Bulls', 'MLS', 'MLS', 'https://a.espncdn.com/i/teamlogos/soccer/500/190.png', 'Red Bulls'),
  c('new_york_city_fc', 'New York City FC', 'NYCFC', 'MLS', 'MLS', 'https://a.espncdn.com/i/teamlogos/soccer/500/17606.png', 'NYCFC'),
  c('atlanta_united_fc', 'Atlanta United FC', 'Atlanta United', 'MLS', 'MLS', 'https://a.espncdn.com/i/teamlogos/soccer/500/18418.png'),
  c('seattle_sounders_fc', 'Seattle Sounders FC', 'Seattle Sounders', 'MLS', 'MLS', 'https://a.espncdn.com/i/teamlogos/soccer/500/9723.png', 'Sounders'),

  // Eredivisie (DED)
  c('afc_ajax', 'AFC Ajax', 'Ajax', 'DED', 'Eredivisie', 'https://crests.football-data.org/678.png', 'Ajax'),
  c('psv_eindhoven', 'PSV Eindhoven', 'PSV', 'DED', 'Eredivisie', 'https://crests.football-data.org/674.png', 'PSV'),
  c('feyenoord_rotterdam', 'Feyenoord Rotterdam', 'Feyenoord', 'DED', 'Eredivisie', 'https://crests.football-data.org/675.png', 'Feyenoord'),
  c('az_alkmaar', 'AZ Alkmaar', 'AZ', 'DED', 'Eredivisie', 'https://crests.football-data.org/682.png', 'AZ'),
  c('fc_twente_65', "FC Twente '65", 'FC Twente', 'DED', 'Eredivisie', 'https://crests.football-data.org/666.png', 'Twente'),

  // Liga Portugal (PPL)
  c('sl_benfica', 'SL Benfica', 'Benfica', 'PPL', 'Liga Portugal', 'https://crests.football-data.org/1903.png', 'Benfica'),
  c('sporting_cp', 'Sporting CP', 'Sporting CP', 'PPL', 'Liga Portugal', 'https://crests.football-data.org/498.png', 'Sporting Lisbon', 'Sporting'),
  c('fc_porto', 'FC Porto', 'Porto', 'PPL', 'Liga Portugal', 'https://crests.football-data.org/503.png', 'Porto'),
  c('sc_braga', 'SC Braga', 'Braga', 'PPL', 'Liga Portugal', 'https://crests.football-data.org/5613.png', 'Braga'),
];

export function isMatchForFavoriteClubs(
  homeName: string,
  awayName: string,
  favIds: Set<string>
): boolean {
  if (favIds.size === 0) return true;

  const favClubs = SUPPORTED_CLUBS.filter((c) => favIds.has(c.id));
  if (favClubs.length === 0) return true;

  const hLower = homeName.toLowerCase();
  const aLower = awayName.toLowerCase();

  return favClubs.some((club) => {
    const clubName = club.name.toLowerCase();
    const shortName = club.shortName.toLowerCase();
    const matchesHome =
      hLower.includes(clubName) ||
      hLower.includes(shortName) ||
      club.aliases.some((a) => hLower.includes(a.toLowerCase()));
    const matchesAway =
      aLower.includes(clubName) ||
      aLower.includes(shortName) ||
      club.aliases.some((a) => aLower.includes(a.toLowerCase()));
    return matchesHome || matchesAway;
  });
}
