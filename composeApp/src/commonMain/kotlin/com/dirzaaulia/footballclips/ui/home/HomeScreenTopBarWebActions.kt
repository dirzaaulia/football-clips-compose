package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onShowPaywall: () -> Unit
) {
    IconButton(
        onClick = onToggleSpoilerFree,
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Icon(
            imageVector = if (isSpoilerFreeMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            contentDescription = "Spoiler Free Mode",
            tint = if (isSpoilerFreeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
                        y = pos.y + size.height / 2f
                    )
                }
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Toggle Theme",
                tint = MaterialTheme.colorScheme.onSurface
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
            contentColor = if (isPremium) Color.White else Color.Black
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Icon(
            if (isPremium) Icons.Default.VideoLibrary else Icons.Default.Diamond,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            if (isPremium) "PREMIUM" else "GO PREMIUM",
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
