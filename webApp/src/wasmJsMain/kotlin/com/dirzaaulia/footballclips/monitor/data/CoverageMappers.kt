package com.dirzaaulia.footballclips.monitor.data

import com.dirzaaulia.footballclips.monitor.domain.CountryDirectory
import com.dirzaaulia.footballclips.monitor.domain.CountryStatus
import com.dirzaaulia.footballclips.monitor.domain.CoverageSummary
import com.dirzaaulia.footballclips.monitor.domain.SourceChannel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

internal fun JsonObject.toCoverage() = CoverageSummary(
    text("required_covered"),
    text("required_total"),
    text("covered_count"),
    text("total", "249"),
    text("unrestricted") == "true",
    records("countries").map {
        CountryStatus(
            it.text("code"),
            it.text("name"),
            it.text("state"),
            it.text("required") == "true",
            it.text("platform_limited") == "true",
        )
    }.toImmutableList(),
)

internal fun JsonObject.toMatchCoverage(): CoverageSummary {
    val videoId = text("highlight_video_id", "")
    if (videoId.isBlank() || videoId == "Unknown" || videoId == "null") {
        return CoverageSummary(
            priority = "0",
            priorityTotal = "0",
            worldwide = "0",
            total = "0",
            unrestricted = false,
            countries = persistentListOf(),
        )
    }

    val regionInfo = obj("youtube_region_info")
    val blockedCodes = (regionInfo["blocked"] as? JsonArray)
        ?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
        ?.toSet()
        .orEmpty()
    val allowedCodes = (regionInfo["allowed"] as? JsonArray)
        ?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
        ?.toSet()
        .orEmpty()

    val priorityCodes = setOf("ID", "GB", "US", "ES", "DE")
    val countryStatuses = CountryDirectory.ALL.map { (code, name) ->
        val isAvailable = when {
            allowedCodes.isNotEmpty() -> allowedCodes.contains(code)
            blockedCodes.isNotEmpty() -> !blockedCodes.contains(code)
            else -> true
        }
        CountryStatus(
            code = code,
            name = name,
            state = if (isAvailable) "available" else "blocked",
            required = priorityCodes.contains(code),
            platformLimited = false,
        )
    }.toImmutableList()

    val coveredCount = countryStatuses.count { it.state == "available" }
    val requiredCovered = countryStatuses.count { it.required && it.state == "available" }
    val isUnrestricted = blockedCodes.isEmpty() && allowedCodes.isEmpty()

    return CoverageSummary(
        priority = requiredCovered.toString(),
        priorityTotal = priorityCodes.size.toString(),
        worldwide = coveredCount.toString(),
        total = CountryDirectory.ALL.size.toString(),
        unrestricted = isUnrestricted,
        countries = countryStatuses,
    )
}
internal fun JsonObject.toSource() = SourceChannel(
    text("id"), text("label"), text("kind"), text("handle"), text("channel_id", ""), text("verification"),
    text("approved") == "true", text("highlight_rights", "unknown"), text("territories", ""),
    text("rights_evidence", ""), text("rights_verified_at", ""), text("mapping_basis", ""),
)
