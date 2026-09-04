package com.dirzaaulia.footballclips.data.constants

object AdConfiguration {
    // Google AdSense (WASM)
    const val ADSENSE_CLIENT_ID = "ca-pub-6717632447198427"
    const val ADSENSE_BANNER_SLOT_ID = "5616816386"

    val ALLOWED_PRODUCTION_HOSTS = listOf(
        "fc.dirzaaulia.com",
        "dirzaaulia.com",
        "football-clips-51f56.web.app",
        "football-clips-51f56.firebaseapp.com"
    )

    // Google AdMob (Android)
    const val ADMOB_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"

    // RevenueCat Paddle Sandbox (WASM)
    // URL format: https://pay.rev.cat/[ENTITLEMENT_ID]/[APP_USER_ID]
    const val SANDBOX_REMOVE_ADS_PURCHASE_URL = "https://pay.rev.cat/ppwchgbfqppzavxg/testuser"
}
