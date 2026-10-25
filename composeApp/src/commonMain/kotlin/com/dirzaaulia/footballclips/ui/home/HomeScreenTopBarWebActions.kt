package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.ui.theme.rememberThemeCapture
import com.dirzaaulia.footballclips.util.isWasmTarget
import kotlin.time.Clock

@Composable
internal fun HomeScreenTopBarWebActions(
    isDarkMode: Boolean,
    isPremium: Boolean,
    isSpoilerFreeMode: Boolean,
    favoriteClubsCount: Int,
    onDarkModeToggle: (Boolean) -> Unit,
    onToggleSpoilerFree: () -> Unit,
    onShowMyClubs: () -> Unit,
    onShowPaywall: () -> Unit,
) {
    IconButton(
        onClick = onToggleSpoilerFree,
        modifier = Modifier.padding(end = 4.dp),
    ) {
        Icon(
            imageVector = if (isSpoilerFreeMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            contentDescription = "Spoiler Free Mode",
            tint = if (isSpoilerFreeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }

    if (!isWasmTarget) {
        var lastThemeToggleTime by remember { mutableStateOf(0L) }
        val triggerThemeCapture = rememberThemeCapture()
        var toggleCenter by remember { mutableStateOf(Offset.Zero) }

        IconButton(
            onClick = {
                val now = Clock.System.now().toEpochMilliseconds()
                if (now - lastThemeToggleTime >= 750) {
                    lastThemeToggleTime = now
                    triggerThemeCapture(toggleCenter)
                    onDarkModeToggle(!isDarkMode)
                }
            },
            modifier = Modifier
                .padding(end = 4.dp)
                .onGloballyPositioned { coordinates ->
                    val pos = coordinates.positionInWindow()
                    val size = coordinates.size
                    toggleCenter = Offset(
                        x = pos.x + size.width / 2f,
                        y = pos.y + size.height / 2f,
                    )
                },
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Toggle Theme",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }

    Button(
        onClick = onShowPaywall,
        modifier = Modifier
            .padding(end = 24.dp)
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPremium) Color(0xFF4CAF50) else Color(0xFFD4AF37),
            contentColor = if (isPremium) Color.White else Color.Black,
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
    ) {
        Icon(
            imageVector = if (isPremium) Icons.Default.VideoLibrary else Icons.Default.Diamond,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isPremium) "PREMIUM" else "GO PREMIUM",
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
