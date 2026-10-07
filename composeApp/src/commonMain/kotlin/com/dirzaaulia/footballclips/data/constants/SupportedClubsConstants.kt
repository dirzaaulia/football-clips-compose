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
        c("afc_bournemouth", "AFC Bournemouth", "AFC Bournemouth", "PL", "Premier League", "https://crests.football-data.org/bournemouth.png"),
        c("arsenal_fc", "Arsenal FC", "Arsenal", "PL", "Premier League", "https://crests.football-data.org/57.png", "Arsenal"),
        c("aston_villa_fc", "Aston Villa FC", "Aston Villa", "PL", "Premier League", "https://crests.football-data.org/58.png", "Aston Villa"),
        c("brentford_fc", "Brentford FC", "Brentford", "PL", "Premier League", "https://crests.football-data.org/402.png", "Brentford"),
        c("brighton_hove_albion_fc", "Brighton & Hove Albion FC", "Brighton & Hove", "PL", "Premier League", "https://crests.football-data.org/397.png", "Brighton & Hove Albion"),
        c("chelsea_fc", "Chelsea FC", "Chelsea", "PL", "Premier League", "https://crests.football-data.org/61.png", "Chelsea"),
        c("coventry_city_fc", "Coventry City FC", "Coventry City", "PL", "Premier League", "https://crests.football-data.org/1076.png", "Coventry City"),
        c("crystal_palace_fc", "Crystal Palace FC", "Crystal Palace", "PL", "Premier League", "https://crests.football-data.org/354.png", "Crystal Palace"),
        c("everton_fc", "Everton FC", "Everton", "PL", "Premier League", "https://crests.football-data.org/62.png", "Everton"),
        c("fulham_fc", "Fulham FC", "Fulham", "PL", "Premier League", "https://crests.football-data.org/63.png", "Fulham"),
        c("hull_city_afc", "Hull City AFC", "Hull City", "PL", "Premier League", "https://crests.football-data.org/322.png", "Hull City"),
        c("ipswich_town_fc", "Ipswich Town FC", "Ipswich Town", "PL", "Premier League", "https://crests.football-data.org/349.png", "Ipswich Town"),
        c("leeds_united_fc", "Leeds United FC", "Leeds United", "PL", "Premier League", "https://crests.football-data.org/341.png", "Leeds United"),
        c("liverpool_fc", "Liverpool FC", "Liverpool", "PL", "Premier League", "https://crests.football-data.org/64.png", "Liverpool"),
        c("manchester_city_fc", "Manchester City FC", "Manchester City", "PL", "Premier League", "https://crests.football-data.org/65.png", "Manchester City"),
        c("manchester_united_fc", "Manchester United FC", "Manchester Unit", "PL", "Premier League", "https://crests.football-data.org/66.png", "Manchester United"),
        c("newcastle_united_fc", "Newcastle United FC", "Newcastle Unite", "PL", "Premier League", "https://crests.football-data.org/67.png", "Newcastle United"),
        c("nottingham_forest_fc", "Nottingham Forest FC", "Nottingham Fore", "PL", "Premier League", "https://crests.football-data.org/351.png", "Nottingham Forest"),
        c("sunderland_afc", "Sunderland AFC", "Sunderland", "PL", "Premier League", "https://crests.football-data.org/71.png", "Sunderland"),
        c("tottenham_hotspur_fc", "Tottenham Hotspur FC", "Tottenham Hotsp", "PL", "Premier League", "https://crests.football-data.org/73.png", "Tottenham Hotspur"),
        c("athletic_club", "Athletic Club", "Athletic Club", "PD", "La Liga", "https://crests.football-data.org/77.png"),
        c("ca_osasuna", "CA Osasuna", "CA Osasuna", "PD", "La Liga", "https://crests.football-data.org/79.png"),
        c("club_atl_tico_de_madrid", "Club Atlético de Madrid", "Club Atlético d", "PD", "La Liga", "https://crests.football-data.org/78.png"),
        c("deportivo_alav_s", "Deportivo Alavés", "Deportivo Alavé", "PD", "La Liga", "https://crests.football-data.org/263.png"),
        c("elche_cf", "Elche CF", "Elche", "PD", "La Liga", "https://crests.football-data.org/285.png", "Elche"),
        c("fc_barcelona", "FC Barcelona", "FC Barcelona", "PD", "La Liga", "https://crests.football-data.org/81.png"),
        c("getafe_cf", "Getafe CF", "Getafe", "PD", "La Liga", "https://crests.football-data.org/82.png", "Getafe"),
        c("levante_ud", "Levante UD", "Levante UD", "PD", "La Liga", "https://crests.football-data.org/88.png"),
        c("m_laga_cf", "Málaga CF", "Málaga", "PD", "La Liga", "https://crests.football-data.org/84.png", "Málaga"),
        c("rc_celta_de_vigo", "RC Celta de Vigo", "RC Celta de Vig", "PD", "La Liga", "https://crests.football-data.org/558.png"),
        c("rc_deportivo_la_coru_a", "RC Deportivo La Coruña", "RC Deportivo La", "PD", "La Liga", "https://crests.football-data.org/560.png"),
        c("rcd_espanyol_de_barcelona", "RCD Espanyol de Barcelona", "RCD Espanyol de", "PD", "La Liga", "https://crests.football-data.org/80.png"),
        c("rayo_vallecano_de_madrid", "Rayo Vallecano de Madrid", "Rayo Vallecano ", "PD", "La Liga", "https://crests.football-data.org/87.png"),
        c("real_betis_balompi", "Real Betis Balompié", "Real Betis Balo", "PD", "La Liga", "https://crests.football-data.org/90.png"),
        c("real_madrid_cf", "Real Madrid CF", "Real Madrid", "PD", "La Liga", "https://crests.football-data.org/86.png", "Real Madrid"),
        c("real_racing_club_de_santander", "Real Racing Club de Santander", "Real Racing Clu", "PD", "La Liga", "https://crests.football-data.org/5335.png"),
        c("real_sociedad_de_f_tbol", "Real Sociedad de Fútbol", "Real Sociedad d", "PD", "La Liga", "https://crests.football-data.org/92.png"),
        c("sevilla_fc", "Sevilla FC", "Sevilla", "PD", "La Liga", "https://crests.football-data.org/559.png", "Sevilla"),
        c("valencia_cf", "Valencia CF", "Valencia", "PD", "La Liga", "https://crests.football-data.org/95.png", "Valencia"),
        c("villarreal_cf", "Villarreal CF", "Villarreal", "PD", "La Liga", "https://crests.football-data.org/94.png", "Villarreal"),
        c("ac_milan", "AC Milan", "AC Milan", "SA", "Serie A", "https://crests.football-data.org/98.png"),
        c("ac_monza", "AC Monza", "AC Monza", "SA", "Serie A", "https://crests.football-data.org/5911.png"),
        c("acf_fiorentina", "ACF Fiorentina", "ACF Fiorentina", "SA", "Serie A", "https://crests.football-data.org/99.png"),
        c("as_roma", "AS Roma", "AS Roma", "SA", "Serie A", "https://crests.football-data.org/100.png"),
        c("atalanta_bc", "Atalanta BC", "Atalanta BC", "SA", "Serie A", "https://crests.football-data.org/102.png"),
        c("bologna_fc_1909", "Bologna FC 1909", "Bologna", "SA", "Serie A", "https://crests.football-data.org/103.png", "Bologna 1909"),
        c("cagliari_calcio", "Cagliari Calcio", "Cagliari Calcio", "SA", "Serie A", "https://crests.football-data.org/104.png"),
        c("como_1907", "Como 1907", "Como 1907", "SA", "Serie A", "https://crests.football-data.org/7397.png"),
        c("fc_internazionale_milano", "FC Internazionale Milano", "FC Internaziona", "SA", "Serie A", "https://crests.football-data.org/108.png"),
        c("frosinone_calcio", "Frosinone Calcio", "Frosinone Calci", "SA", "Serie A", "https://crests.football-data.org/470.png"),
        c("genoa_cfc", "Genoa CFC", "GenoaC", "SA", "Serie A", "https://crests.football-data.org/107.png", "GenoaC"),
        c("juventus_fc", "Juventus FC", "Juventus", "SA", "Serie A", "https://crests.football-data.org/109.png", "Juventus"),
        c("parma_calcio_1913", "Parma Calcio 1913", "Parma Calcio", "SA", "Serie A", "https://crests.football-data.org/112.png"),
        c("ss_lazio", "SS Lazio", "SS Lazio", "SA", "Serie A", "https://crests.football-data.org/110.png"),
        c("ssc_napoli", "SSC Napoli", "SSC Napoli", "SA", "Serie A", "https://crests.football-data.org/113.png"),
        c("torino_fc", "Torino FC", "Torino", "SA", "Serie A", "https://crests.football-data.org/586.png", "Torino"),
        c("us_lecce", "US Lecce", "US Lecce", "SA", "Serie A", "https://crests.football-data.org/5890.png"),
        c("us_sassuolo_calcio", "US Sassuolo Calcio", "US Sassuolo Cal", "SA", "Serie A", "https://crests.football-data.org/471.png"),
        c("udinese_calcio", "Udinese Calcio", "Udinese Calcio", "SA", "Serie A", "https://crests.football-data.org/115.png"),
        c("venezia_fc", "Venezia FC", "Venezia", "SA", "Serie A", "https://crests.football-data.org/454.png", "Venezia"),
        c("1_fc_k_ln", "1. FC Köln", "1. Köln", "BL1", "Bundesliga", "https://crests.football-data.org/1.png", "1. Köln"),
        c("1_fc_union_berlin", "1. FC Union Berlin", "1. Union Berlin", "BL1", "Bundesliga", "https://crests.football-data.org/28.png", "1. Union Berlin"),
        c("1_fsv_mainz_05", "1. FSV Mainz 05", "1. FSV Mainz", "BL1", "Bundesliga", "https://crests.football-data.org/15.png"),
        c("bayer_04_leverkusen", "Bayer 04 Leverkusen", "Bayer Leverkuse", "BL1", "Bundesliga", "https://crests.football-data.org/3.png"),
        c("borussia_dortmund", "Borussia Dortmund", "Borussia Dortmu", "BL1", "Bundesliga", "https://crests.football-data.org/4.png"),
        c("borussia_m_nchengladbach", "Borussia Mönchengladbach", "Borussia Mönche", "BL1", "Bundesliga", "https://crests.football-data.org/18.png"),
        c("eintracht_frankfurt", "Eintracht Frankfurt", "Eintracht Frank", "BL1", "Bundesliga", "https://crests.football-data.org/19.png"),
        c("fc_augsburg", "FC Augsburg", "FC Augsburg", "BL1", "Bundesliga", "https://crests.football-data.org/16.png"),
        c("fc_bayern_m_nchen", "FC Bayern München", "FC Bayern Münch", "BL1", "Bundesliga", "https://crests.football-data.org/5.png"),
        c("fc_schalke_04", "FC Schalke 04", "FC Schalke", "BL1", "Bundesliga", "https://crests.football-data.org/6.png"),
        c("hamburger_sv", "Hamburger SV", "Hamburger SV", "BL1", "Bundesliga", "https://crests.football-data.org/7.png"),
        c("rb_leipzig", "RB Leipzig", "RB Leipzig", "BL1", "Bundesliga", "https://crests.football-data.org/721.png"),
        c("sc_freiburg", "SC Freiburg", "SC Freiburg", "BL1", "Bundesliga", "https://crests.football-data.org/17.png"),
        c("sc_paderborn_07", "SC Paderborn 07", "SC Paderborn", "BL1", "Bundesliga", "https://crests.football-data.org/29.png"),
        c("sv_07_elversberg", "SV 07 Elversberg", "SV Elversberg", "BL1", "Bundesliga", "https://crests.football-data.org/719.png"),
        c("sv_werder_bremen", "SV Werder Bremen", "SV Werder Breme", "BL1", "Bundesliga", "https://crests.football-data.org/12.png"),
        c("tsg_1899_hoffenheim", "TSG 1899 Hoffenheim", "TSG 1899 Hoffen", "BL1", "Bundesliga", "https://crests.football-data.org/2.png"),
        c("vfb_stuttgart", "VfB Stuttgart", "VfB Stuttgart", "BL1", "Bundesliga", "https://crests.football-data.org/10.png"),
        c("aj_auxerre", "AJ Auxerre", "AJ Auxerre", "FL1", "Ligue 1", "https://crests.football-data.org/519.png"),
        c("as_monaco_fc", "AS Monaco FC", "AS Monaco", "FL1", "Ligue 1", "https://crests.football-data.org/548.png", "AS Monaco"),
        c("angers_sco", "Angers SCO", "Angers SCO", "FL1", "Ligue 1", "https://crests.football-data.org/532.png"),
        c("es_troyes_ac", "ES Troyes AC", "ES Troyes AC", "FL1", "Ligue 1", "https://crests.football-data.org/531.png"),
        c("fc_lorient", "FC Lorient", "FC Lorient", "FL1", "Ligue 1", "https://crests.football-data.org/525.png"),
        c("le_havre_ac", "Le Havre AC", "Le Havre AC", "FL1", "Ligue 1", "https://crests.football-data.org/533.png"),
        c("le_mans_fc", "Le Mans FC", "Le Mans", "FL1", "Ligue 1", "https://upload.wikimedia.org/wikipedia/en/5/57/Le_Mans_FC_logo.svg", "Le Mans"),
        c("lille_osc", "Lille OSC", "Lille OSC", "FL1", "Ligue 1", "https://crests.football-data.org/521.png"),
        c("ogc_nice", "OGC Nice", "OGC Nice", "FL1", "Ligue 1", "https://crests.football-data.org/522.png"),
        c("olympique_lyonnais", "Olympique Lyonnais", "Olympique Lyonn", "FL1", "Ligue 1", "https://crests.football-data.org/523.png"),
        c("olympique_de_marseille", "Olympique de Marseille", "Olympique de Ma", "FL1", "Ligue 1", "https://crests.football-data.org/516.png"),
        c("paris_fc", "Paris FC", "Paris", "FL1", "Ligue 1", "https://crests.football-data.org/1045.png", "Paris"),
        c("paris_saint_germain_fc", "Paris Saint-Germain FC", "Paris Saint-Ger", "FL1", "Ligue 1", "https://crests.football-data.org/524.png", "Paris Saint-Germain"),
        c("rc_strasbourg_alsace", "RC Strasbourg Alsace", "RC Strasbourg A", "FL1", "Ligue 1", "https://crests.football-data.org/576.png"),
        c("racing_club_de_lens", "Racing Club de Lens", "Racing Club de ", "FL1", "Ligue 1", "https://crests.football-data.org/546.png"),
        c("stade_brestois_29", "Stade Brestois 29", "Stade Brestois ", "FL1", "Ligue 1", "https://crests.football-data.org/512.png"),
        c("stade_rennais_fc_1901", "Stade Rennais FC 1901", "Stade Rennais 1", "FL1", "Ligue 1", "https://crests.football-data.org/529.png", "Stade Rennais 1901"),
        c("toulouse_fc", "Toulouse FC", "Toulouse", "FL1", "Ligue 1", "https://crests.football-data.org/511.png", "Toulouse"),
    )

    val premierLeagueClubs by lazy { allClubs.filter { it.leagueCode == "PL" } }
    val laLigaClubs by lazy { allClubs.filter { it.leagueCode == "PD" } }
    val serieAClubs by lazy { allClubs.filter { it.leagueCode == "SA" } }
    val bundesligaClubs by lazy { allClubs.filter { it.leagueCode == "BL1" } }
    val ligue1Clubs by lazy { allClubs.filter { it.leagueCode == "FL1" } }

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