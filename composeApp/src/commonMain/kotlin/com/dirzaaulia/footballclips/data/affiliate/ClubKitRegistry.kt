package com.dirzaaulia.footballclips.data.affiliate

object ClubKitRegistry {
    private val CLUB_KIT_IMAGES: Map<String, String> = mapOf(
        // Premier League
        "manchester city" to "https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_500,h_500/global/774984/01/fnd/EEA/fmt/png",
        "man city" to "https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_500,h_500/global/774984/01/fnd/EEA/fmt/png",

        // Serie A
        "milan" to "https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_500,h_500/global/775037/01/fnd/EEA/fmt/png",
        "ac milan" to "https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_500,h_500/global/775037/01/fnd/EEA/fmt/png",

        // Bundesliga
        "dortmund" to "https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_500,h_500/global/775075/01/fnd/EEA/fmt/png",
        "borussia dortmund" to "https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_500,h_500/global/775075/01/fnd/EEA/fmt/png",

        // La Liga
        "valencia" to "https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_500,h_500/global/774959/01/fnd/EEA/fmt/png",

        // Ligue 1
        "marseille" to "https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_500,h_500/global/774996/01/fnd/EEA/fmt/png",
        "lens" to "https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_500,h_500/global/775003/01/fnd/EEA/fmt/png",
    )

    fun resolveKitPhoto(clubName: String): String? {
        val clean = clubName
            .lowercase()
            .replace(Regex("\\b(fc|cf|afc|sc|sv|fk|ssc|osc|rc|cd|ca)\\b"), "")
            .trim()

        for ((key, photo) in CLUB_KIT_IMAGES) {
            if (photo.isNotBlank() && (clean.contains(key) || key.contains(clean))) {
                return photo
            }
        }
        return null
    }
}
