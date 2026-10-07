package com.dirzaaulia.footballclips.util

/**
 * Image URL Proxy helper.
 * Android loads image URLs directly. WASM proxies via wsrv.nl to bypass CORS.
 */
expect fun String.toProxyUrl(): String
