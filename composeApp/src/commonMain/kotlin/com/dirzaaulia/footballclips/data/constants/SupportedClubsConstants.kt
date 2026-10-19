package com.dirzaaulia.footballclips.data.constants

import com.dirzaaulia.footballclips.domain.model.Match

data class SupportedClub(
    val id: String,
    val name: String,
    val shortName: String,
    val leagueCode: String,
    val leagueName: String,
    val crestUrl: String,
    val aliases: List<String> = emptyList()
) {
    fun matchesTeamName(teamName: String): Boolean {
        val lower = teamName.lowercase().trim()
        if (lower.contains(name.lowercase()) || lower.contains(shortName.lowercase())) return true
        return aliases.any { lower.contains(it.lowercase()) }
    }
}

object SupportedClubsConstants {

    private fun c(id: String, n: String, s: String, code: String, lName: String, crest: String, vararg a: String) =
        SupportedClub(id, n, s, code, lName, crest, a.toList())

    val allClubs: List<SupportedClub> = listOf(
        c("afc_bournemouth", "AFC Bournemouth", "AFC Bournemouth", "PL", "Premier League", "https://a.espncdn.com/i/teamlogos/soccer/500/349.png"),
        c("arsenal_fc", "Arsenal FC", "Arsenal", "PL", "Premier League", "https://a.espncdn.com/i/teamlogos/soccer/500/359.png", "Arsenal"),
        c("aston_villa_fc", "Aston Villa FC", "Aston Villa", "PL", "Premier League", "https://a.espncdn.com/i/teamlogos/soccer/500/362.png", "Aston Villa"),
        c("brentford_fc", "Brentford FC", "Brentford", "PL", "Premier League", "https://a.espncdn.com/i/teamlogos/soccer/500/337.png", "Brentford"),
        c("brighton_hove_albion_fc", "Brighton & Hove Albion FC", "Brighton & Hove", "PL", "Premier League", "https://a.espncdn.com/i/teamlogos/soccer/500/331.png", "Brighton & Hove Albion"),
        c("chelsea_fc", "Chelsea FC", "Chelsea", "PL", "Premier League", "https://a.espncdn.com/i/teamlogos/soccer/500/363.png", "Chelsea"),
        c("coventry_city_fc", "Coventry City FC", "Coventry City", "PL", "Premier League", "https://a.espncdn.com/i/teamlogos/soccer/500/392.png", "Coventry City"),
        c("crystal_palace_fc", "Crystal Palace FC", "Crystal Palace", "PL", "Premier League", "https://a.espncdn.com/i/teamlogos/soccer/500/384.png", "Crystal Palace"),
        c("everton_fc", "Everton FC", "Everton", "PL", "Premier League", "https://a.espncdn.com/i/teamlogos/soccer/500/368.png", "Everton"),
        c("fulham_fc", "Fulham FC", "Fulham", "PL", "Premier League", "https://a.espncdn.com/i/teamlogos/soccer/500/370.png", "Fulham"),
        c("hull_city_afc", "Hull City AFC", "Hull City", "PL", "Premier League", "https://a.espncdn.com/i/teamlogos/soccer/500/306.png", "Hull City"),
        c("ipswich_town_fc", "Ipswich Town FC", "Ipswich Town", "PL", "Premier League", "https://a.espncdn.com/i/teamlogos/soccer/500/373.png", "Ipswich Town"),
        c("leeds_united_fc", "Leeds United FC", "Leeds United", "PL", "Premier League", "https://a.espncdn.com/i/teamlogos/soccer/500/357.png", "Leeds United"),
        c("liverpool_fc", "Liverpool FC", "Liverpool", "PL", "Premier League", "https://a.espncdn.com/i/teamlogos/soccer/500/364.png", "Liverpool"),
        c("manchester_city_fc", "Manchester City FC", "Manchester City", "PL", "Premier League", "https://a.espncdn.com/i/teamlogos/soccer/500/382.png", "Manchester City"),
        c("manchester_united_fc", "Manchester United FC", "Manchester Unit", "PL", "Premier League", "https://a.espncdn.com/i/teamlogos/soccer/500/360.png", "Manchester United"),
        c("newcastle_united_fc", "Newcastle United FC", "Newcastle Unite", "PL", "Premier League", "https://a.espncdn.com/i/teamlogos/soccer/500/361.png", "Newcastle United"),
        c("nottingham_forest_fc", "Nottingham Forest FC", "Nottingham Fore", "PL", "Premier League", "https://a.espncdn.com/i/teamlogos/soccer/500/393.png", "Nottingham Forest"),
        c("sunderland_afc", "Sunderland AFC", "Sunderland", "PL", "Premier League", "https://a.espncdn.com/i/teamlogos/soccer/500/366.png", "Sunderland"),
        c("tottenham_hotspur_fc", "Tottenham Hotspur FC", "Tottenham Hotsp", "PL", "Premier League", "https://a.espncdn.com/i/teamlogos/soccer/500/367.png", "Tottenham Hotspur"),
        c("athletic_club", "Athletic Club", "Athletic Club", "PD", "La Liga", "https://a.espncdn.com/i/teamlogos/soccer/500/93.png"),
        c("ca_osasuna", "CA Osasuna", "CA Osasuna", "PD", "La Liga", "https://a.espncdn.com/i/teamlogos/soccer/500/97.png"),
        c("club_atl_tico_de_madrid", "Club Atlético de Madrid", "Club Atlético d", "PD", "La Liga", "https://a.espncdn.com/i/teamlogos/soccer/500/1068.png"),
        c("deportivo_alav_s", "Deportivo Alavés", "Deportivo Alavé", "PD", "La Liga", "https://a.espncdn.com/i/teamlogos/soccer/500/96.png"),
        c("elche_cf", "Elche CF", "Elche", "PD", "La Liga", "https://a.espncdn.com/i/teamlogos/soccer/500/3751.png", "Elche"),
        c("fc_barcelona", "FC Barcelona", "FC Barcelona", "PD", "La Liga", "https://a.espncdn.com/i/teamlogos/soccer/500/83.png"),
        c("getafe_cf", "Getafe CF", "Getafe", "PD", "La Liga", "https://a.espncdn.com/i/teamlogos/soccer/500/2922.png", "Getafe"),
        c("levante_ud", "Levante UD", "Levante UD", "PD", "La Liga", "https://a.espncdn.com/i/teamlogos/soccer/500/95.png"),
        c("m_laga_cf", "Málaga CF", "Málaga", "PD", "La Liga", "https://a.espncdn.com/i/teamlogos/soccer/500/88.png", "Málaga"),
        c("rc_celta_de_vigo", "RC Celta de Vigo", "RC Celta de Vig", "PD", "La Liga", "https://a.espncdn.com/i/teamlogos/soccer/500/85.png"),
        c("rc_deportivo_la_coru_a", "RC Deportivo La Coruña", "RC Deportivo La", "PD", "La Liga", "https://a.espncdn.com/i/teamlogos/soccer/500/87.png"),
        c("rcd_espanyol_de_barcelona", "RCD Espanyol de Barcelona", "RCD Espanyol de", "PD", "La Liga", "https://a.espncdn.com/i/teamlogos/soccer/500/84.png"),
        c("rayo_vallecano_de_madrid", "Rayo Vallecano de Madrid", "Rayo Vallecano ", "PD", "La Liga", "https://a.espncdn.com/i/teamlogos/soccer/500/101.png"),
        c("real_betis_balompi", "Real Betis Balompié", "Real Betis Balo", "PD", "La Liga", "https://a.espncdn.com/i/teamlogos/soccer/500/244.png"),
        c("real_madrid_cf", "Real Madrid CF", "Real Madrid", "PD", "La Liga", "https://a.espncdn.com/i/teamlogos/soccer/500/86.png", "Real Madrid"),
        c("real_racing_club_de_santander", "Real Racing Club de Santander", "Real Racing Clu", "PD", "La Liga", "https://a.espncdn.com/i/teamlogos/soccer/500/90.png"),
        c("real_sociedad_de_f_tbol", "Real Sociedad de Fútbol", "Real Sociedad d", "PD", "La Liga", "https://a.espncdn.com/i/teamlogos/soccer/500/89.png"),
        c("sevilla_fc", "Sevilla FC", "Sevilla", "PD", "La Liga", "https://a.espncdn.com/i/teamlogos/soccer/500/243.png", "Sevilla"),
        c("valencia_cf", "Valencia CF", "Valencia", "PD", "La Liga", "https://a.espncdn.com/i/teamlogos/soccer/500/94.png", "Valencia"),
        c("villarreal_cf", "Villarreal CF", "Villarreal", "PD", "La Liga", "https://a.espncdn.com/i/teamlogos/soccer/500/102.png", "Villarreal"),
        c("ac_milan", "AC Milan", "AC Milan", "SA", "Serie A", "https://a.espncdn.com/i/teamlogos/soccer/500/103.png"),
        c("ac_monza", "AC Monza", "AC Monza", "SA", "Serie A", "https://a.espncdn.com/i/teamlogos/soccer/500/19183.png"),
        c("acf_fiorentina", "ACF Fiorentina", "ACF Fiorentina", "SA", "Serie A", "https://a.espncdn.com/i/teamlogos/soccer/500/109.png"),
        c("as_roma", "AS Roma", "AS Roma", "SA", "Serie A", "https://a.espncdn.com/i/teamlogos/soccer/500/104.png"),
        c("atalanta_bc", "Atalanta BC", "Atalanta BC", "SA", "Serie A", "https://a.espncdn.com/i/teamlogos/soccer/500/125.png"),
        c("bologna_fc_1909", "Bologna FC 1909", "Bologna", "SA", "Serie A", "https://a.espncdn.com/i/teamlogos/soccer/500/107.png", "Bologna 1909"),
        c("cagliari_calcio", "Cagliari Calcio", "Cagliari Calcio", "SA", "Serie A", "https://a.espncdn.com/i/teamlogos/soccer/500/2925.png"),
        c("como_1907", "Como 1907", "Como 1907", "SA", "Serie A", "https://a.espncdn.com/i/teamlogos/soccer/500/2618.png"),
        c("fc_internazionale_milano", "FC Internazionale Milano", "FC Internaziona", "SA", "Serie A", "https://a.espncdn.com/i/teamlogos/soccer/500/110.png"),
        c("frosinone_calcio", "Frosinone Calcio", "Frosinone Calci", "SA", "Serie A", "https://a.espncdn.com/i/teamlogos/soccer/500/3894.png"),
        c("genoa_cfc", "Genoa CFC", "GenoaC", "SA", "Serie A", "https://a.espncdn.com/i/teamlogos/soccer/500/3263.png", "GenoaC"),
        c("juventus_fc", "Juventus FC", "Juventus", "SA", "Serie A", "https://a.espncdn.com/i/teamlogos/soccer/500/111.png", "Juventus"),
        c("parma_calcio_1913", "Parma Calcio 1913", "Parma Calcio", "SA", "Serie A", "https://a.espncdn.com/i/teamlogos/soccer/500/113.png"),
        c("ss_lazio", "SS Lazio", "SS Lazio", "SA", "Serie A", "https://a.espncdn.com/i/teamlogos/soccer/500/112.png"),
        c("ssc_napoli", "SSC Napoli", "SSC Napoli", "SA", "Serie A", "https://a.espncdn.com/i/teamlogos/soccer/500/114.png"),
        c("torino_fc", "Torino FC", "Torino", "SA", "Serie A", "https://a.espncdn.com/i/teamlogos/soccer/500/239.png", "Torino"),
        c("us_lecce", "US Lecce", "US Lecce", "SA", "Serie A", "https://a.espncdn.com/i/teamlogos/soccer/500/3444.png"),
        c("us_sassuolo_calcio", "US Sassuolo Calcio", "US Sassuolo Cal", "SA", "Serie A", "https://a.espncdn.com/i/teamlogos/soccer/500/4642.png"),
        c("udinese_calcio", "Udinese Calcio", "Udinese Calcio", "SA", "Serie A", "https://a.espncdn.com/i/teamlogos/soccer/500/115.png"),
        c("venezia_fc", "Venezia FC", "Venezia", "SA", "Serie A", "https://a.espncdn.com/i/teamlogos/soccer/500/2053.png", "Venezia"),
        c("1_fc_k_ln", "1. FC Köln", "1. Köln", "BL1", "Bundesliga", "https://a.espncdn.com/i/teamlogos/soccer/500/130.png", "1. Köln"),
        c("1_fc_union_berlin", "1. FC Union Berlin", "1. Union Berlin", "BL1", "Bundesliga", "https://a.espncdn.com/i/teamlogos/soccer/500/8361.png", "1. Union Berlin"),
        c("1_fsv_mainz_05", "1. FSV Mainz 05", "1. FSV Mainz", "BL1", "Bundesliga", "https://a.espncdn.com/i/teamlogos/soccer/500/2950.png"),
        c("bayer_04_leverkusen", "Bayer 04 Leverkusen", "Bayer Leverkuse", "BL1", "Bundesliga", "https://a.espncdn.com/i/teamlogos/soccer/500/131.png"),
        c("borussia_dortmund", "Borussia Dortmund", "Borussia Dortmu", "BL1", "Bundesliga", "https://a.espncdn.com/i/teamlogos/soccer/500/124.png"),
        c("borussia_m_nchengladbach", "Borussia Mönchengladbach", "Borussia Mönche", "BL1", "Bundesliga", "https://a.espncdn.com/i/teamlogos/soccer/500/268.png"),
        c("eintracht_frankfurt", "Eintracht Frankfurt", "Eintracht Frank", "BL1", "Bundesliga", "https://a.espncdn.com/i/teamlogos/soccer/500/125.png"),
        c("fc_augsburg", "FC Augsburg", "FC Augsburg", "BL1", "Bundesliga", "https://a.espncdn.com/i/teamlogos/soccer/500/3841.png"),
        c("fc_bayern_m_nchen", "FC Bayern München", "FC Bayern Münch", "BL1", "Bundesliga", "https://a.espncdn.com/i/teamlogos/soccer/500/132.png"),
        c("fc_schalke_04", "FC Schalke 04", "FC Schalke", "BL1", "Bundesliga", "https://a.espncdn.com/i/teamlogos/soccer/500/133.png"),
        c("hamburger_sv", "Hamburger SV", "Hamburger SV", "BL1", "Bundesliga", "https://a.espncdn.com/i/teamlogos/soccer/500/128.png"),
        c("rb_leipzig", "RB Leipzig", "RB Leipzig", "BL1", "Bundesliga", "https://a.espncdn.com/i/teamlogos/soccer/500/11420.png"),
        c("sc_freiburg", "SC Freiburg", "SC Freiburg", "BL1", "Bundesliga", "https://a.espncdn.com/i/teamlogos/soccer/500/126.png"),
        c("sc_paderborn_07", "SC Paderborn 07", "SC Paderborn", "BL1", "Bundesliga", "https://a.espncdn.com/i/teamlogos/soccer/500/5312.png"),
        c("sv_07_elversberg", "SV 07 Elversberg", "SV Elversberg", "BL1", "Bundesliga", "https://a.espncdn.com/i/teamlogos/soccer/500/17409.png"),
        c("sv_werder_bremen", "SV Werder Bremen", "SV Werder Breme", "BL1", "Bundesliga", "https://a.espncdn.com/i/teamlogos/soccer/500/137.png"),
        c("tsg_1899_hoffenheim", "TSG 1899 Hoffenheim", "TSG 1899 Hoffen", "BL1", "Bundesliga", "https://a.espncdn.com/i/teamlogos/soccer/500/7911.png"),
        c("vfb_stuttgart", "VfB Stuttgart", "VfB Stuttgart", "BL1", "Bundesliga", "https://a.espncdn.com/i/teamlogos/soccer/500/134.png"),
        c("aj_auxerre", "AJ Auxerre", "AJ Auxerre", "FL1", "Ligue 1", "https://a.espncdn.com/i/teamlogos/soccer/500/170.png"),
        c("as_monaco_fc", "AS Monaco FC", "AS Monaco", "FL1", "Ligue 1", "https://a.espncdn.com/i/teamlogos/soccer/500/174.png", "AS Monaco"),
        c("angers_sco", "Angers SCO", "Angers SCO", "FL1", "Ligue 1", "https://a.espncdn.com/i/teamlogos/soccer/500/3243.png"),
        c("es_troyes_ac", "ES Troyes AC", "ES Troyes AC", "FL1", "Ligue 1", "https://a.espncdn.com/i/teamlogos/soccer/500/164.png"),
        c("fc_lorient", "FC Lorient", "FC Lorient", "FL1", "Ligue 1", "https://a.espncdn.com/i/teamlogos/soccer/500/274.png"),
        c("le_havre_ac", "Le Havre AC", "Le Havre AC", "FL1", "Ligue 1", "https://a.espncdn.com/i/teamlogos/soccer/500/3244.png"),
        c("le_mans_fc", "Le Mans FC", "Le Mans", "FL1", "Ligue 1", "https://upload.wikimedia.org/wikipedia/en/5/57/Le_Mans_FC_logo.svg", "Le Mans"),
        c("lille_osc", "Lille OSC", "Lille OSC", "FL1", "Ligue 1", "https://a.espncdn.com/i/teamlogos/soccer/500/165.png"),
        c("ogc_nice", "OGC Nice", "OGC Nice", "FL1", "Ligue 1", "https://a.espncdn.com/i/teamlogos/soccer/500/168.png"),
        c("olympique_lyonnais", "Olympique Lyonnais", "Olympique Lyonn", "FL1", "Ligue 1", "https://a.espncdn.com/i/teamlogos/soccer/500/167.png"),
        c("olympique_de_marseille", "Olympique de Marseille", "Olympique de Ma", "FL1", "Ligue 1", "https://a.espncdn.com/i/teamlogos/soccer/500/166.png"),
        c("paris_fc", "Paris FC", "Paris", "FL1", "Ligue 1", "https://a.espncdn.com/i/teamlogos/soccer/500/6833.png", "Paris"),
        c("paris_saint_germain_fc", "Paris Saint-Germain FC", "Paris Saint-Ger", "FL1", "Ligue 1", "https://a.espncdn.com/i/teamlogos/soccer/500/160.png", "Paris Saint-Germain"),
        c("rc_strasbourg_alsace", "RC Strasbourg Alsace", "RC Strasbourg A", "FL1", "Ligue 1", "https://a.espncdn.com/i/teamlogos/soccer/500/169.png"),
        c("racing_club_de_lens", "Racing Club de Lens", "Racing Club de ", "FL1", "Ligue 1", "https://a.espncdn.com/i/teamlogos/soccer/500/175.png"),
        c("stade_brestois_29", "Stade Brestois 29", "Stade Brestois ", "FL1", "Ligue 1", "https://a.espncdn.com/i/teamlogos/soccer/500/3307.png"),
        c("stade_rennais_fc_1901", "Stade Rennais FC 1901", "Stade Rennais 1", "FL1", "Ligue 1", "https://a.espncdn.com/i/teamlogos/soccer/500/162.png", "Stade Rennais 1901"),
        c("toulouse_fc", "Toulouse FC", "Toulouse", "FL1", "Ligue 1", "https://a.espncdn.com/i/teamlogos/soccer/500/178.png", "Toulouse"),
        // Saudi Pro League (SPL)
        c("al_hilal_sfc", "Al Hilal SFC", "Al Hilal", "SPL", "Saudi Pro League", "https://a.espncdn.com/i/teamlogos/soccer/500/7339.png", "Al-Hilal"),
        c("al_nassr_fc", "Al Nassr FC", "Al Nassr", "SPL", "Saudi Pro League", "https://a.espncdn.com/i/teamlogos/soccer/500/7342.png", "Al-Nassr"),
        c("al_ittihad_fc", "Al Ittihad FC", "Al Ittihad", "SPL", "Saudi Pro League", "https://a.espncdn.com/i/teamlogos/soccer/500/7338.png", "Al-Ittihad"),
        c("al_ahli_saudi_fc", "Al Ahli Saudi FC", "Al Ahli", "SPL", "Saudi Pro League", "https://a.espncdn.com/i/teamlogos/soccer/500/7337.png", "Al-Ahli"),
        c("al_shabab_fc", "Al Shabab FC", "Al Shabab", "SPL", "Saudi Pro League", "https://a.espncdn.com/i/teamlogos/soccer/500/7343.png", "Al-Shabab"),
        c("al_ettifaq_fc", "Al Ettifaq FC", "Al Ettifaq", "SPL", "Saudi Pro League", "https://a.espncdn.com/i/teamlogos/soccer/500/7335.png", "Al-Ettifaq"),
        c("al_taawoun_fc", "Al Taawoun FC", "Al Taawoun", "SPL", "Saudi Pro League", "https://a.espncdn.com/i/teamlogos/soccer/500/10940.png", "Al-Taawoun"),
        c("al_fateh_sc", "Al Fateh SC", "Al Fateh", "SPL", "Saudi Pro League", "https://a.espncdn.com/i/teamlogos/soccer/500/10939.png", "Al-Fateh"),
        // MLS (MLS)
        c("inter_miami_cf", "Inter Miami CF", "Inter Miami", "MLS", "MLS", "https://a.espncdn.com/i/teamlogos/soccer/500/19106.png", "Inter Miami"),
        c("la_galaxy", "LA Galaxy", "LA Galaxy", "MLS", "MLS", "https://a.espncdn.com/i/teamlogos/soccer/500/187.png", "Galaxy"),
        c("los_angeles_fc", "Los Angeles FC", "LAFC", "MLS", "MLS", "https://a.espncdn.com/i/teamlogos/soccer/500/18966.png", "LAFC"),
        c("columbus_crew", "Columbus Crew", "Columbus Crew", "MLS", "MLS", "https://a.espncdn.com/i/teamlogos/soccer/500/183.png"),
        c("new_york_red_bulls", "New York Red Bulls", "NY Red Bulls", "MLS", "MLS", "https://a.espncdn.com/i/teamlogos/soccer/500/190.png", "Red Bulls"),
        c("new_york_city_fc", "New York City FC", "NYCFC", "MLS", "MLS", "https://a.espncdn.com/i/teamlogos/soccer/500/17606.png", "NYCFC"),
        c("atlanta_united_fc", "Atlanta United FC", "Atlanta United", "MLS", "MLS", "https://a.espncdn.com/i/teamlogos/soccer/500/18418.png"),
        c("seattle_sounders_fc", "Seattle Sounders FC", "Seattle Sounders", "MLS", "MLS", "https://a.espncdn.com/i/teamlogos/soccer/500/9723.png", "Sounders"),
        c("philadelphia_union", "Philadelphia Union", "Philadelphia", "MLS", "MLS", "https://a.espncdn.com/i/teamlogos/soccer/500/10972.png"),
        c("fc_cincinnati", "FC Cincinnati", "FC Cincinnati", "MLS", "MLS", "https://a.espncdn.com/i/teamlogos/soccer/500/18267.png", "Cincinnati"),
        // Eredivisie (DED)
        c("afc_ajax", "AFC Ajax", "Ajax", "DED", "Eredivisie", "https://a.espncdn.com/i/teamlogos/soccer/500/139.png", "Ajax"),
        c("psv_eindhoven", "PSV Eindhoven", "PSV", "DED", "Eredivisie", "https://a.espncdn.com/i/teamlogos/soccer/500/148.png", "PSV"),
        c("feyenoord_rotterdam", "Feyenoord Rotterdam", "Feyenoord", "DED", "Eredivisie", "https://a.espncdn.com/i/teamlogos/soccer/500/141.png", "Feyenoord"),
        c("az_alkmaar", "AZ Alkmaar", "AZ", "DED", "Eredivisie", "https://a.espncdn.com/i/teamlogos/soccer/500/140.png", "AZ"),
        c("fc_twente_65", "FC Twente '65", "FC Twente", "DED", "Eredivisie", "https://a.espncdn.com/i/teamlogos/soccer/500/149.png", "Twente"),
        c("fc_utrecht", "FC Utrecht", "FC Utrecht", "DED", "Eredivisie", "https://a.espncdn.com/i/teamlogos/soccer/500/150.png", "Utrecht"),
        // Liga Portugal (PPL)
        c("sl_benfica", "SL Benfica", "Benfica", "PPL", "Liga Portugal", "https://a.espncdn.com/i/teamlogos/soccer/500/298.png", "Benfica"),
        c("sporting_cp", "Sporting CP", "Sporting CP", "PPL", "Liga Portugal", "https://a.espncdn.com/i/teamlogos/soccer/500/5040.png", "Sporting Lisbon", "Sporting"),
        c("fc_porto", "FC Porto", "Porto", "PPL", "Liga Portugal", "https://a.espncdn.com/i/teamlogos/soccer/500/300.png", "Porto"),
        c("sc_braga", "SC Braga", "Braga", "PPL", "Liga Portugal", "https://a.espncdn.com/i/teamlogos/soccer/500/299.png", "Braga"),
        c("vit_ria_sc", "Vitória SC", "Vitória SC", "PPL", "Liga Portugal", "https://a.espncdn.com/i/teamlogos/soccer/500/3122.png", "Guimaraes")
    )

    val premierLeagueClubs by lazy { allClubs.filter { it.leagueCode == "PL" } }
    val laLigaClubs by lazy { allClubs.filter { it.leagueCode == "PD" } }
    val serieAClubs by lazy { allClubs.filter { it.leagueCode == "SA" } }
    val bundesligaClubs by lazy { allClubs.filter { it.leagueCode == "BL1" } }
    val ligue1Clubs by lazy { allClubs.filter { it.leagueCode == "FL1" } }
    val saudiProLeagueClubs by lazy { allClubs.filter { it.leagueCode == "SPL" } }
    val mlsClubs by lazy { allClubs.filter { it.leagueCode == "MLS" } }
    val eredivisieClubs by lazy { allClubs.filter { it.leagueCode == "DED" } }
    val ligaPortugalClubs by lazy { allClubs.filter { it.leagueCode == "PPL" } }

    private val clubsById: Map<String, SupportedClub> by lazy { allClubs.associateBy { it.id } }

    fun findClubById(id: String, clubs: List<SupportedClub> = allClubs): SupportedClub? =
        clubs.find { it.id == id } ?: clubsById[id]

    fun isMatchForFavoriteClubs(
        match: Match,
        favoriteClubIds: Set<String>,
        clubs: List<SupportedClub> = allClubs
    ): Boolean {
        if (favoriteClubIds.isEmpty()) return false
        val userClubs = clubs.filter { it.id in favoriteClubIds }
        return userClubs.any { club ->
            club.matchesTeamName(match.homeTeamName) || club.matchesTeamName(match.awayTeamName)
        }
    }
}