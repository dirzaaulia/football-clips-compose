package com.dirzaaulia.footballclips.ui.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.ui.home.HomeViewModel
import com.dirzaaulia.footballclips.util.extractVideoId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MatchTheater(
    item: HighlightUiItem?,
    onClose: () -> Unit,
    onItemClick: (HighlightUiItem) -> Unit,
    viewModel: HomeViewModel
) {
    if (item == null) return

    val uriHandler = LocalUriHandler.current
    var showIssueDialog by remember { mutableStateOf(false) }

    val relatedHighlights by viewModel.relatedHighlights.collectAsState()
    val relatedFixturesFinished by viewModel.relatedFixturesFinished.collectAsState()
    val relatedFixturesUpcoming by viewModel.relatedFixturesUpcoming.collectAsState()
    val isSpoilerFreeMode by viewModel.isSpoilerFreeMode.collectAsState()

    val videoId = when (item) {
        is HighlightUiItem.SupabaseMatch -> item.match.highlightVideoId
        is HighlightUiItem.Highlight -> extractVideoId(item.highlight.embedHtml)
        else -> null
    }

    val title: String
    val competition: String
    val competitionId: String?
    val date: String
    val thumbnail: String

    when (item) {
        is HighlightUiItem.SupabaseMatch -> {
            title = "${item.match.homeTeamName} vs ${item.match.awayTeamName}"
            competition = item.match.competitionName
            competitionId = item.match.competitionId
            date = item.match.dateOnly
            thumbnail = "https://img.youtube.com/vi/${item.match.highlightVideoId}/maxresdefault.jpg"
        }
        is HighlightUiItem.Highlight -> {
            title = item.highlight.title
            competition = item.highlight.leagueName
            competitionId = null
            date = item.highlight.date
            thumbnail = item.highlight.thumbnail
        }
        else -> return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "MATCH THEATER",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White.copy(alpha = 0.9f),
                            letterSpacing = 2.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0F0F0F).copy(alpha = 0.95f),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    modifier = Modifier.zIndex(10f)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.70f)
                        .fillMaxHeight()
                        .padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    MatchTheaterPlayer(
                        thumbnail = thumbnail,
                        videoId = videoId
                    )

                    MatchTheaterMetadataBar(
                        item = item,
                        title = title,
                        date = date,
                        isSpoilerFree = isSpoilerFreeMode,
                        onIssueClick = { showIssueDialog = true }
                    )
                }

                WebMatchesSidebar(
                    competition = competition,
                    relatedHighlights = relatedHighlights,
                    relatedFixturesFinished = relatedFixturesFinished,
                    relatedFixturesUpcoming = relatedFixturesUpcoming,
                    onItemClick = onItemClick,
                    modifier = Modifier.weight(0.30f)
                )
            }
        }
    }

    if (showIssueDialog) {
        AlertDialog(
            onDismissRequest = { showIssueDialog = false },
            containerColor = Color(0xFF1A1A1A),
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.8f),
            title = { Text("Playback Issue", fontWeight = FontWeight.Black) },
            text = { Text("This video might have embedding restrictions. Would you like to watch it directly on YouTube?") },
            confirmButton = {
                Button(
                    onClick = {
                        showIssueDialog = false
                        videoId?.let { id ->
                            uriHandler.openUri("https://www.youtube.com/watch?v=$id")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Yes, Open YouTube", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showIssueDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }
}
