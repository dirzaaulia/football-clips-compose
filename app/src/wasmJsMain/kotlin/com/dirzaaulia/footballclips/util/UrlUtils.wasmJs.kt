package com.dirzaaulia.footballclips.util

actual fun String.toProxyUrl(): String {
    if (this.isEmpty()) return this
    if (!this.startsWith("http")) return this
    if (this.contains("wsrv.nl")) return this

    val cleanUrl = this.replace("https://", "").replace("http://", "")
    return "https://wsrv.nl/?url=$cleanUrl"
}
