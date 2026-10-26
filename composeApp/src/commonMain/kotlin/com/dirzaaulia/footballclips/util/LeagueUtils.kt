package com.dirzaaulia.footballclips.util

fun String.normalizeLeagueName(): String {
    val trimmed = this.trim()
    return when {
        trimmed.equals("Primera Division", ignoreCase = true) || 
        trimmed.equals("Primera División", ignoreCase = true) ||
        trimmed.equals("Spanish Primera Division", ignoreCase = true) ||
        trimmed.equals("LaLiga", ignoreCase = true) ||
        trimmed.equals("La Liga", ignoreCase = true) ||
        trimmed.equals("PD", ignoreCase = true) -> "La Liga"

        trimmed.equals("Premier League", ignoreCase = true) ||
        trimmed.equals("EPL", ignoreCase = true) ||
        trimmed.equals("PL", ignoreCase = true) -> "Premier League"

        trimmed.equals("Serie A", ignoreCase = true) ||
        trimmed.equals("SA", ignoreCase = true) -> "Serie A"

        trimmed.equals("Bundesliga", ignoreCase = true) ||
        trimmed.equals("BL1", ignoreCase = true) -> "Bundesliga"

        trimmed.equals("Ligue 1", ignoreCase = true) ||
        trimmed.equals("FL1", ignoreCase = true) -> "Ligue 1"

        trimmed.equals("UEFA Champions League", ignoreCase = true) ||
        trimmed.equals("Champions League", ignoreCase = true) ||
        trimmed.equals("CL", ignoreCase = true) -> "Champions League"

        trimmed.equals("UEFA Europa League", ignoreCase = true) ||
        trimmed.equals("Europa League", ignoreCase = true) ||
        trimmed.equals("EL", ignoreCase = true) -> "Europa League"

        trimmed.equals("UEFA Conference League", ignoreCase = true) ||
        trimmed.equals("UEFA Europa Conference League", ignoreCase = true) ||
        trimmed.equals("Conference League", ignoreCase = true) ||
        trimmed.equals("ECL", ignoreCase = true) ||
        trimmed.equals("UECL", ignoreCase = true) -> "Conference League"

        trimmed.equals("Saudi Pro League", ignoreCase = true) ||
        trimmed.equals("Saudi Professional League", ignoreCase = true) ||
        trimmed.equals("SPL", ignoreCase = true) -> "Saudi Pro League"

        trimmed.equals("Major League Soccer", ignoreCase = true) ||
        trimmed.equals("MLS", ignoreCase = true) -> "MLS"

        trimmed.equals("Eredivisie", ignoreCase = true) ||
        trimmed.equals("DED", ignoreCase = true) ||
        trimmed.equals("ED", ignoreCase = true) -> "Eredivisie"

        trimmed.equals("Liga Portugal", ignoreCase = true) ||
        trimmed.equals("Primeira Liga", ignoreCase = true) ||
        trimmed.equals("PPL", ignoreCase = true) -> "Liga Portugal"

        trimmed.equals("FA Cup", ignoreCase = true) ||
        trimmed.equals("FAC", ignoreCase = true) -> "FA Cup"

        trimmed.equals("Carabao Cup", ignoreCase = true) ||
        trimmed.equals("EFL Cup", ignoreCase = true) ||
        trimmed.equals("EFL", ignoreCase = true) -> "Carabao Cup"

        trimmed.equals("Copa del Rey", ignoreCase = true) ||
        trimmed.equals("CDR", ignoreCase = true) -> "Copa del Rey"

        trimmed.equals("DFB-Pokal", ignoreCase = true) ||
        trimmed.equals("DFB Pokal", ignoreCase = true) ||
        trimmed.equals("DFB", ignoreCase = true) -> "DFB-Pokal"

        trimmed.equals("Coppa Italia", ignoreCase = true) ||
        trimmed.equals("CIT", ignoreCase = true) -> "Coppa Italia"

        trimmed.equals("CONMEBOL Libertadores", ignoreCase = true) ||
        trimmed.equals("Copa Libertadores", ignoreCase = true) ||
        trimmed.equals("Libertadores", ignoreCase = true) ||
        trimmed.equals("LIB", ignoreCase = true) -> "CONMEBOL Libertadores"

        trimmed.equals("CONMEBOL Sudamericana", ignoreCase = true) ||
        trimmed.equals("Copa Sudamericana", ignoreCase = true) ||
        trimmed.equals("Sudamericana", ignoreCase = true) ||
        trimmed.equals("SUD", ignoreCase = true) -> "CONMEBOL Sudamericana"

        trimmed.equals("CONMEBOL Recopa", ignoreCase = true) ||
        trimmed.equals("Recopa Sudamericana", ignoreCase = true) ||
        trimmed.equals("Recopa", ignoreCase = true) ||
        trimmed.equals("REC", ignoreCase = true) -> "CONMEBOL Recopa"

        trimmed.equals("Brasileirão Série A", ignoreCase = true) ||
        trimmed.equals("Brasileirao Serie A", ignoreCase = true) ||
        trimmed.equals("Brasileirão", ignoreCase = true) ||
        trimmed.equals("Brasileirao", ignoreCase = true) ||
        trimmed.equals("BRA", ignoreCase = true) -> "Brasileirão Série A"

        trimmed.equals("Argentine Liga Profesional", ignoreCase = true) ||
        trimmed.equals("Liga Profesional de Fútbol", ignoreCase = true) ||
        trimmed.equals("Liga Argentina", ignoreCase = true) ||
        trimmed.equals("ARG", ignoreCase = true) -> "Argentine Liga Profesional"

        else -> trimmed
    }
}
