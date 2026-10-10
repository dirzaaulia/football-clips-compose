package com.dirzaaulia.footballclips.data.constants

object AdConfiguration {
    // Adsterra Network (WASM)
    const val ADSTERRA_ENABLED = true

    /**
     * When TRUE, displays simulated test ads on localhost to prevent invalid click/traffic bans.
     * When FALSE, allows testing live Adsterra script integration (DO NOT click your own live ads!).
     */
    const val ADSTERRA_TEST_MODE = false

    /**
     * Controls whether real Adsterra ads can load on localhost when ADSTERRA_TEST_MODE is false.
     */
    const val ADSTERRA_ENABLE_ON_LOCALHOST = true

    // Adsterra Native 1:1 Ad Placement
    const val ADSTERRA_NATIVE_KEY = "02f0950d1e419a21d6f6a3658772400c"
    const val ADSTERRA_NATIVE_SCRIPT_URL = "https://pl31719707.profitableratecpmnetwork.com/02f0950d1e419a21d6f6a3658772400c/invoke.js"

    // Adsterra Social Bar Placement
    const val ADSTERRA_SOCIAL_BAR_SCRIPT_URL = "https://pl31719708.profitableratecpmnetwork.com/ea/16/26/ea1626c7a906b622a51ffed9c7cb0216.js"

    val ALLOWED_PRODUCTION_HOSTS = listOf(
        "fc.dirzaaulia.com",
        "dirzaaulia.com",
        "footballclips.web.app",
        "footballclips.firebaseapp.com",
        "highlightdata.web.app",
        "highlightdata.firebaseapp.com",
        "football-clips-51f56.web.app",
        "football-clips-51f56.firebaseapp.com"
    )

    // RevenueCat Paddle Sandbox (WASM)
    // URL format: https://pay.rev.cat/[ENTITLEMENT_ID]/[APP_USER_ID]
    const val SANDBOX_REMOVE_ADS_PURCHASE_URL = "https://pay.rev.cat/ppwchgbfqppzavxg/testuser"
}
