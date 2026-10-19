package com.dirzaaulia.footballclips.telemetry.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dirzaaulia.footballclips.telemetry.model.TelemetryNavSection
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import org.w3c.dom.events.Event

@Serializable
sealed interface TelemetryDestination {
    val hash: String
    val section: TelemetryNavSection

    @Serializable
    data object Cycles : TelemetryDestination {
        override val hash: String = "#/cycles"
        override val section: TelemetryNavSection = TelemetryNavSection.PIPELINE_CYCLES
    }

    @Serializable
    data object Verifier : TelemetryDestination {
        override val hash: String = "#/verifier"
        override val section: TelemetryNavSection = TelemetryNavSection.HIGHLIGHT_VERIFIER
    }

    @Serializable
    data object Fixtures : TelemetryDestination {
        override val hash: String = "#/fixtures"
        override val section: TelemetryNavSection = TelemetryNavSection.FIXTURE_SYNC
    }

    @Serializable
    data object Standings : TelemetryDestination {
        override val hash: String = "#/standings"
        override val section: TelemetryNavSection = TelemetryNavSection.STANDINGS_AUDIT
    }

    @Serializable
    data object Notifications : TelemetryDestination {
        override val hash: String = "#/notifications"
        override val section: TelemetryNavSection = TelemetryNavSection.NOTIFICATIONS
    }

    @Serializable
    data object Versions : TelemetryDestination {
        override val hash: String = "#/versions"
        override val section: TelemetryNavSection = TelemetryNavSection.USER_VERSIONS
    }

    @Serializable
    data object Crashlytics : TelemetryDestination {
        override val hash: String = "#/crashlytics"
        override val section: TelemetryNavSection = TelemetryNavSection.CRASHLYTICS
    }

    @Serializable
    data object Config : TelemetryDestination {
        override val hash: String = "#/config"
        override val section: TelemetryNavSection = TelemetryNavSection.CONFIG
    }

    companion object {
        fun fromHash(hash: String): TelemetryDestination {
            return when {
                hash.contains("verifier", ignoreCase = true) -> Verifier
                hash.contains("fixture", ignoreCase = true) -> Fixtures
                hash.contains("standing", ignoreCase = true) -> Standings
                hash.contains("notification", ignoreCase = true) -> Notifications
                hash.contains("version", ignoreCase = true) -> Versions
                hash.contains("crash", ignoreCase = true) -> Crashlytics
                hash.contains("config", ignoreCase = true) -> Config
                else -> Cycles
            }
        }

        fun fromSection(section: TelemetryNavSection): TelemetryDestination {
            return when (section) {
                TelemetryNavSection.HIGHLIGHT_VERIFIER -> Verifier
                TelemetryNavSection.FIXTURE_SYNC -> Fixtures
                TelemetryNavSection.STANDINGS_AUDIT -> Standings
                TelemetryNavSection.NOTIFICATIONS -> Notifications
                TelemetryNavSection.USER_VERSIONS -> Versions
                TelemetryNavSection.CRASHLYTICS -> Crashlytics
                TelemetryNavSection.CONFIG -> Config
                else -> Cycles
            }
        }
    }
}

class TelemetryNavigationState(
    initialDestination: TelemetryDestination = TelemetryDestination.Cycles,
) {
    val backStack = mutableStateListOf(initialDestination)

    var currentDestination by mutableStateOf(initialDestination)
        private set

    fun navigateTo(destination: TelemetryDestination) {
        if (currentDestination == destination) return
        backStack.add(destination)
        currentDestination = destination
        runCatching {
            if (window.location.hash != destination.hash) {
                window.location.hash = destination.hash
            }
        }
    }

    fun navigateToSection(section: TelemetryNavSection) {
        navigateTo(TelemetryDestination.fromSection(section))
    }

    fun popBackStack(): Boolean {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
            val previous = backStack.last()
            currentDestination = previous
            runCatching {
                window.location.hash = previous.hash
            }
            return true
        }
        return false
    }

    fun syncFromHash(hash: String) {
        val dest = TelemetryDestination.fromHash(hash)
        if (currentDestination != dest) {
            currentDestination = dest
            if (backStack.isEmpty() || backStack.last() != dest) {
                backStack.add(dest)
            }
        }
    }
}

@Composable
fun rememberTelemetryNavigationState(): TelemetryNavigationState {
    val initialHash = runCatching { window.location.hash }.getOrDefault("")
    val initialDestination = TelemetryDestination.fromHash(initialHash)
    val navState = remember { TelemetryNavigationState(initialDestination) }

    DisposableEffect(navState) {
        val listener: (Event) -> Unit = {
            val currentHash = runCatching { window.location.hash }.getOrDefault("")
            navState.syncFromHash(currentHash)
        }

        runCatching {
            window.addEventListener("hashchange", listener)
            window.addEventListener("popstate", listener)
        }

        onDispose {
            runCatching {
                window.removeEventListener("hashchange", listener)
                window.removeEventListener("popstate", listener)
            }
        }
    }

    return navState
}
