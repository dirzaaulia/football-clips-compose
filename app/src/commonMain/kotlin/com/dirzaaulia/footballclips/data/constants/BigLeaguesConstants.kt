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
            logo = "https://crests.football-data.org/CL.png"
        ),
        BigLeague(
            name = "Europa League",
            leagueId = 3337,
            countryCode = "EU",
            competitionId = "EL",
            logo = "https://crests.football-data.org/EL.png"
        ),
        BigLeague(
            name = "Premier League",
            leagueId = 33973,
            countryCode = "GB-ENG",
            competitionId = "PL",
            logo = "https://crests.football-data.org/PL.png"
        ),
        BigLeague(
            name = "La Liga",
            leagueId = 119924,
            countryCode = "ES",
            competitionId = "PD",
            logo = "https://crests.football-data.org/PD.png"
        ),
        BigLeague(
            name = "Serie A",
            leagueId = 115669,
            countryCode = "IT",
            competitionId = "SA",
            logo = "https://crests.football-data.org/SA.png"
        ),
        BigLeague(
            name = "Bundesliga",
            leagueId = 67162,
            countryCode = "DE",
            competitionId = "BL1",
            logo = "https://crests.football-data.org/BL1.png"
        ),
        BigLeague(
            name = "Ligue 1",
            leagueId = 52695,
            countryCode = "FR",
            competitionId = "FL1",
            logo = "https://crests.football-data.org/FL1.png"
        )
    )
}
