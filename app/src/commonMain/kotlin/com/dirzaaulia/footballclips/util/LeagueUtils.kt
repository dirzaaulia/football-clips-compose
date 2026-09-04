package com.dirzaaulia.footballclips.util

fun String.normalizeLeagueName(): String {
    return if (this.equals("Primera Division", ignoreCase = true) || 
        this.equals("Primera División", ignoreCase = true) ||
        this.equals("Spanish Primera Division", ignoreCase = true)) {
        "La Liga"
    } else {
        this
    }
}
