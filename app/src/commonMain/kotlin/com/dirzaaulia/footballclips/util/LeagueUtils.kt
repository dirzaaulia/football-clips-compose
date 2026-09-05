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

        else -> trimmed
    }
}
