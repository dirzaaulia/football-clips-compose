package com.dirzaaulia.footballclips.data.constants

data class BigLeague(
    val name: String,
    val leagueId: Int?,
    val countryCode: String?,
    val competitionId: String?, // For Supabase Live Score
    val logo: String
)

object BigLeaguesConstants {
    val leagues = listOf(
        BigLeague(
            name = "All Leagues",
            leagueId = null,
            countryCode = null,
            competitionId = null,
            logo = ""
        ),
        BigLeague(
            name = "Champions League",
            leagueId = 2486,
            countryCode = "EU",
            competitionId = "CL",
            logo = "https://a.espncdn.com/i/leaguelogos/soccer/500/2.png"
        ),
        BigLeague(
            name = "Europa League",
            leagueId = 3337,
            countryCode = "EU",
            competitionId = "EL",
            logo = "https://a.espncdn.com/i/leaguelogos/soccer/500/2487.png"
        ),
        BigLeague(
            name = "Conference League",
            leagueId = null,
            countryCode = "EU",
            competitionId = "ECL",
            logo = "https://a.espncdn.com/i/leaguelogos/soccer/500/20349.png"
        ),
        BigLeague(
            name = "Premier League",
            leagueId = 33973,
            countryCode = "GB-ENG",
            competitionId = "PL",
            logo = "https://a.espncdn.com/i/leaguelogos/soccer/500/23.png"
        ),
        BigLeague(
            name = "La Liga",
            leagueId = 119924,
            countryCode = "ES",
            competitionId = "PD",
            logo = "https://a.espncdn.com/i/leaguelogos/soccer/500/15.png"
        ),
        BigLeague(
            name = "Serie A",
            leagueId = 115669,
            countryCode = "IT",
            competitionId = "SA",
            logo = "https://a.espncdn.com/i/leaguelogos/soccer/500/12.png"
        ),
        BigLeague(
            name = "Bundesliga",
            leagueId = 67162,
            countryCode = "DE",
            competitionId = "BL1",
            logo = "https://a.espncdn.com/i/leaguelogos/soccer/500/10.png"
        ),
        BigLeague(
            name = "Ligue 1",
            leagueId = 52695,
            countryCode = "FR",
            competitionId = "FL1",
            logo = "https://a.espncdn.com/i/leaguelogos/soccer/500/9.png"
        ),
        BigLeague(
            name = "Saudi Pro League",
            leagueId = null,
            countryCode = "SA",
            competitionId = "SPL",
            logo = "https://a.espncdn.com/i/leaguelogos/soccer/500/2488.png"
        ),
        BigLeague(
            name = "MLS",
            leagueId = null,
            countryCode = "US",
            competitionId = "MLS",
            logo = "https://a.espncdn.com/i/leaguelogos/soccer/500/19.png"
        ),
        BigLeague(
            name = "Eredivisie",
            leagueId = null,
            countryCode = "NL",
            competitionId = "DED",
            logo = "https://a.espncdn.com/i/leaguelogos/soccer/500/11.png"
        ),
        BigLeague(
            name = "Liga Portugal",
            leagueId = null,
            countryCode = "PT",
            competitionId = "PPL",
            logo = "https://a.espncdn.com/i/leaguelogos/soccer/500/14.png"
        ),
        BigLeague(
            name = "FA Cup",
            leagueId = null,
            countryCode = "GB-ENG",
            competitionId = "FAC",
            logo = "https://a.espncdn.com/i/leaguelogos/soccer/500/38.png"
        ),
        BigLeague(
            name = "Carabao Cup",
            leagueId = null,
            countryCode = "GB-ENG",
            competitionId = "EFL",
            logo = "https://a.espncdn.com/i/leaguelogos/soccer/500/37.png"
        ),
        BigLeague(
            name = "Copa del Rey",
            leagueId = null,
            countryCode = "ES",
            competitionId = "CDR",
            logo = "https://a.espncdn.com/i/leaguelogos/soccer/500/274.png"
        ),
        BigLeague(
            name = "DFB-Pokal",
            leagueId = null,
            countryCode = "DE",
            competitionId = "DFB",
            logo = "https://a.espncdn.com/i/leaguelogos/soccer/500/2061.png"
        ),
        BigLeague(
            name = "Coppa Italia",
            leagueId = null,
            countryCode = "IT",
            competitionId = "CIT",
            logo = "https://a.espncdn.com/i/leaguelogos/soccer/500/270.png"
        )
    )

    fun isEuropeanHybrid(competitionId: String?): Boolean =
        competitionId in listOf("CL", "EL", "ECL")

    fun isKnockoutCup(competitionId: String?): Boolean =
        competitionId in listOf("FAC", "EFL", "CDR", "DFB", "CIT")
}

