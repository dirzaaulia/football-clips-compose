package com.dirzaaulia.footballclips.ui.home

internal fun getCompetitionPriority(competitionNameOrCode: String): Int {
    val name = competitionNameOrCode.trim()
    return when {
        name.contains("Champions League", true) || name.equals("CL", true) -> 1
        name.contains("Europa League", true) || name.equals("EL", true) -> 2
        name.contains("Conference League", true) || name.equals("ECL", true) -> 3
        name.contains("Premier League", true) || name.equals("PL", true) -> 4
        name.contains("La Liga", true) || name.contains("Primera Division", true) || name.equals("PD", true) -> 5
        name.contains("Serie A", true) || name.equals("SA", true) -> 6
        name.contains("Bundesliga", true) || name.equals("BL1", true) -> 7
        name.contains("Ligue 1", true) || name.equals("FL1", true) -> 8
        name.contains("FA Cup", true) || name.equals("FAC", true) -> 9
        name.contains("Copa del Rey", true) || name.equals("CDR", true) -> 10
        name.contains("Coppa Italia", true) || name.equals("CIT", true) -> 11
        name.contains("DFB", true) -> 12
        name.contains("EFL", true) || name.contains("Carabao", true) -> 13
        name.contains("Eredivisie", true) || name.equals("DED", true) -> 14
        name.contains("Liga Portugal", true) || name.contains("Primeira Liga", true) || name.equals("PPL", true) -> 15
        name.contains("Saudi", true) || name.equals("SPL", true) -> 16
        name.contains("MLS", true) || name.contains("Major League Soccer", true) -> 17
        name.contains("Brasileir", true) || name.equals("BRA", true) -> 18
        name.contains("Argentin", true) || name.equals("ARG", true) -> 19
        name.contains("Libertadores", true) || name.equals("LIB", true) -> 20
        name.contains("Sudamericana", true) || name.equals("SUD", true) -> 21
        name.contains("Recopa", true) || name.equals("REC", true) -> 22
        name.contains("Indonesia", true) || name.equals("IDN", true) -> 23
        else -> 99
    }
}
