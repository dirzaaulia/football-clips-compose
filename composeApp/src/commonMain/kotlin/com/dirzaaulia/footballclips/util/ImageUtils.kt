package com.dirzaaulia.footballclips.util

/**
 * Returns a theme-aware logo URL based on current app theme mode.
 * - Dark Theme (isDarkTheme = true): Maps ESPN logo URLs to high-contrast white `/500-dark/` variants.
 * - Light Theme (isDarkTheme = false): Maps ESPN logo URLs to standard light-mode `/500/` variants.
 */
fun String?.toThemeAwareLogoUrl(isDarkTheme: Boolean = true): String {
    if (this.isNullOrBlank()) return ""
    return if (isDarkTheme) {
        this.replace("/soccer/500/", "/soccer/500-dark/")
            .replace("/teamlogos/soccer/500/", "/teamlogos/soccer/500-dark/")
    } else {
        this.replace("/soccer/500-dark/", "/soccer/500/")
            .replace("/teamlogos/soccer/500-dark/", "/teamlogos/soccer/500/")
    }
}
