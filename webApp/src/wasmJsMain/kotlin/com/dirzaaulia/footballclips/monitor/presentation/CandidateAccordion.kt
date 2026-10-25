package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.monitor.domain.Candidate
import com.dirzaaulia.footballclips.monitor.domain.Check

@Composable
internal fun CandidateAccordion(video: Candidate, expanded: Boolean, action: (MonitorAction) -> Unit) {
    val uri = LocalUriHandler.current
    val hasRejected = video.checks.any { it.state.contains("reject", ignoreCase = true) }
    val overallStatus = when {
        hasRejected -> "error"
        video.checks.all { it.state.contains("pass", ignoreCase = true) } -> "success"
        else -> "warning"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.35f),
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        ),
    ) {
        Column(modifier = Modifier.padding(Space.small)) {
            TextButton(
                onClick = { action(MonitorAction.Expand(video.id)) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.small),
                ) {
                    Chevron(expanded)
                    SignalIcon(overallStatus)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = video.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Video ID: ${video.id} • Channel: ${video.channel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    EventStatusBadge(
                        kind = overallStatus,
                        text = if (hasRejected) "Rejected" else "Passed",
                    )
                    val candLogToCopy =
                        "${video.title}\nVideo ID: ${video.id}\nChannel: ${video.channel}\nPublished: ${displayTime(
                            video.published,
                        )}\nDuration: ${video.duration}\nChecks:\n" +
                            video.checks.joinToString("\n") { "Step: ${it.title} [${it.state}] - ${it.reason}" }
                    CopyButton(textToCopy = candLogToCopy)
                }
            }

            AnimatedVisibility(expanded) {
                Column(
                    modifier = Modifier.padding(top = Space.small),
                    verticalArrangement = Arrangement.spacedBy(Space.small),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Space.medium),
                    ) {
                        RemoteMark(
                            url = "https://i.ytimg.com/vi/${video.id}/mqdefault.jpg",
                            description = video.title,
                            modifier = Modifier
                                .width(140.dp)
                                .aspectRatio(MonitorPolicy.THUMBNAIL_ASPECT)
                                .background(Color.Black, RoundedCornerShape(6.dp)),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Detail(label("channel"), video.channel)
                            Detail(label("published"), displayTime(video.published))
                            Detail(label("duration"), video.duration)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Text(
                        text = "Pipeline Evaluation Steps:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    video.checks.forEachIndexed { index, check ->
                        StepCheckCard(index = index + 1, check = check)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(
                            onClick = { uri.openUri("https://www.youtube.com/watch?v=${video.id}") },
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(label("video"))
                        }
                        TextButton(
                            onClick = { action(MonitorAction.Correct(video.id)) },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        ) {
                            Text(label("wrong"))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepCheckCard(index: Int, check: Check) {
    val isRejected = check.state.contains("reject", ignoreCase = true)
    val isPassed = check.state.contains("pass", ignoreCase = true)
    val kind = when {
        isRejected -> "error"
        isPassed -> "success"
        else -> "warning"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(6.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(Space.small),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    if (isPassed) {
                        Color(0xFF22C55E).copy(alpha = 0.2f)
                    } else if (isRejected) {
                        Color(0xFFEF4444).copy(alpha = 0.2f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = index.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = if (isPassed) {
                    Color(0xFF22C55E)
                } else if (isRejected) {
                    Color(0xFFEF4444)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = check.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = check.reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (check.provider.isNotBlank()) {
                Text(
                    text = "Engine: ${check.provider} • Model: ${check.model}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        EventStatusBadge(kind = kind, text = check.state)
    }
}
