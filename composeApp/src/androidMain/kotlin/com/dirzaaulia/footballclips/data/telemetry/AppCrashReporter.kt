package com.dirzaaulia.footballclips.data.telemetry

import android.content.Context
import android.os.Build
import com.dirzaaulia.footballclips.data.constants.DatabaseConstants
import com.dirzaaulia.footballclips.util.getBuildConfigString
import com.dirzaaulia.footballclips.util.getBuildConfigLong
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.PrintWriter
import java.io.StringWriter

@Serializable
data class AppCrashPayload(
    @SerialName("device_id") val deviceId: String? = null,
    @SerialName("app_version") val appVersion: String,
    @SerialName("version_code") val versionCode: Long,
    val platform: String = "android",
    @SerialName("error_type") val errorType: String,
    @SerialName("error_message") val errorMessage: String,
    @SerialName("stack_trace") val stackTrace: String,
    @SerialName("device_model") val deviceModel: String,
    @SerialName("os_version") val osVersion: String
)

object AppCrashReporter {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var httpClient: HttpClient? = null
    private var defaultUncaughtHandler: Thread.UncaughtExceptionHandler? = null
    private var isInstalled = false

    fun install(context: Context, client: HttpClient? = null) {
        if (isInstalled) return
        isInstalled = true
        httpClient = client ?: HttpClient()
        defaultUncaughtHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                reportError(
                    errorType = throwable.javaClass.simpleName,
                    errorMessage = throwable.message ?: "Uncaught Exception",
                    throwable = throwable,
                    isFatal = true
                )
            } catch (_: Throwable) {
            } finally {
                defaultUncaughtHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    fun recordException(throwable: Throwable, customMessage: String? = null) {
        reportError(
            errorType = throwable.javaClass.simpleName,
            errorMessage = customMessage ?: throwable.message ?: "Non-Fatal Exception",
            throwable = throwable,
            isFatal = false
        )
    }

    private fun reportError(
        errorType: String,
        errorMessage: String,
        throwable: Throwable,
        isFatal: Boolean
    ) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        val stackTrace = sw.toString().take(4000)

        val appVer = getBuildConfigString("VERSION_NAME", "3.0.1")
        val verCode = getBuildConfigLong("VERSION_CODE", 89832569L)
        val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"
        val osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

        val payload = AppCrashPayload(
            appVersion = appVer,
            versionCode = verCode,
            errorType = if (isFatal) "FATAL: $errorType" else "NON_FATAL: $errorType",
            errorMessage = errorMessage.take(1000),
            stackTrace = stackTrace,
            deviceModel = deviceModel,
            osVersion = osVersion
        )

        scope.launch {
            try {
                httpClient?.post("${DatabaseConstants.DATABASE_URL}/rest/v1/app_crashes") {
                    contentType(ContentType.Application.Json)
                    header("Prefer", "return=minimal")
                    setBody(payload)
                }
            } catch (_: Throwable) {
            }
        }
    }
}
