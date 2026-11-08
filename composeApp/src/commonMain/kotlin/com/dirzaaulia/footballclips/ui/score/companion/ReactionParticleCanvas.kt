package com.dirzaaulia.footballclips.ui.score.companion

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin
import kotlin.random.Random


data class LiveParticle(
    val id: Long = Random.nextLong(),
    val emoji: String,
    val startXRatio: Float,
    val birthTimeNanos: Long,
    val durationMillis: Long = 1800L,
    val wiggleFreq: Float = 0.005f,
    val wiggleAmp: Float = 25f,
)

@Composable
fun MatchReactionOverlay(
    modifier: Modifier = Modifier,
    onReactionSent: (String) -> Unit = {},
) {
    val particles = remember { mutableStateListOf<LiveParticle>() }
    var currentFrameNanos by remember { mutableStateOf(0L) }
    val haptic = LocalHapticFeedback.current
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { nowNanos ->
                currentFrameNanos = nowNanos
                val expired = particles.filter {
                    (nowNanos - it.birthTimeNanos) / 1_000_000L > it.durationMillis
                }
                if (expired.isNotEmpty()) {
                    particles.removeAll(expired)
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val nowNanos = currentFrameNanos
            val w = size.width
            val h = size.height

            for (p in particles) {
                val ageMs = (nowNanos - p.birthTimeNanos) / 1_000_000L
                val progress = (ageMs.toFloat() / p.durationMillis).coerceIn(0f, 1f)
                val alpha = (1f - progress).coerceIn(0f, 1f)
                val currentY = h * 0.9f - (progress * h * 0.7f)
                val currentX = (p.startXRatio * w) + (sin(ageMs * p.wiggleFreq.toDouble()).toFloat() * p.wiggleAmp)

                val measuredText = textMeasurer.measure(
                    text = p.emoji,
                    style = TextStyle(fontSize = 24.sp, color = Color.White.copy(alpha = alpha)),
                )
                drawText(textLayoutResult = measuredText, topLeft = Offset(currentX, currentY))

            }
        }

        ReactionDock(
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            onEmojiTap = { emoji ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                particles.add(
                    LiveParticle(
                        emoji = emoji,
                        startXRatio = 0.8f + Random.nextFloat() * 0.12f,
                        birthTimeNanos = currentFrameNanos,
                    )
                )
                onReactionSent(emoji)
            },
        )
    }
}

@Composable
private fun ReactionDock(
    modifier: Modifier = Modifier,
    onEmojiTap: (String) -> Unit,
) {
    Row(
        modifier = modifier
            .background(Color(0xFF0F172A).copy(alpha = 0.85f), RoundedCornerShape(24.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf("⚽", "🔥", "👏", "😱", "🟨").forEach { emoji ->
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { onEmojiTap(emoji) },
                contentAlignment = Alignment.Center,
            ) {
                Text(text = emoji, fontSize = 18.sp)
            }
        }
    }
}
