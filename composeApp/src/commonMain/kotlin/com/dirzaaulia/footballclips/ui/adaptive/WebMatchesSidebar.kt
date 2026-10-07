package com.dirzaaulia.footballclips.ui.adaptive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WebMatchesSidebar(
    competition: String,
    relatedHighlights: List<HighlightUiItem>,
    relatedFixturesFinished: List<HighlightUiItem>,
    relatedFixturesUpcoming: List<HighlightUiItem>,
    onItemClick: (HighlightUiItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        color = Color(0xFF141414),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            var mainSidebarTab by remember { mutableStateOf(0) }
            var fixtureTab by remember { mutableStateOf(0) }

            val mainTabs = listOf("HIGHLIGHTS", "FIXTURES")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A))
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = competition.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                PrimaryTabRow(
                    selectedTabIndex = mainSidebarTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {}
                ) {
                    mainTabs.forEachIndexed { index, titleText ->
                        Tab(
                            selected = mainSidebarTab == index,
                            onClick = { mainSidebarTab = index },
                            text = {
                                Text(
                                    titleText,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (mainSidebarTab == index) Color.White else Color.White.copy(alpha = 0.4f),
                                    letterSpacing = 1.sp
                                )
                            }
                        )
                    }
                }

                if (mainSidebarTab == 1) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = fixtureTab == 0,
                            onClick = { fixtureTab = 0 },
                            label = { Text("RESULTS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White.copy(alpha = 0.05f),
                                labelColor = Color.White.copy(alpha = 0.6f)
                            )
                        )

                        FilterChip(
                            selected = fixtureTab == 1,
                            onClick = { fixtureTab = 1 },
                            label = { Text("UPCOMING", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White.copy(alpha = 0.05f),
                                labelColor = Color.White.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            if (mainSidebarTab == 0) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize().padding(vertical = 8.dp)
                ) {
                    items(
                        items = relatedHighlights,
                        key = { it.uniqueId }
                    ) { relatedItem ->
                        if (relatedItem is HighlightUiItem.BannerAd) {
                            TheaterAdCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        } else {
                            SidebarHighlightCard(
                                item = relatedItem,
                                onClick = { onItemClick(relatedItem) }
                            )
                        }
                    }
                }
            } else {
                val fixtures = if (fixtureTab == 0) relatedFixturesFinished else relatedFixturesUpcoming
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    modifier = Modifier.fillMaxSize().padding(vertical = 8.dp)
                ) {
                    items(
                        items = fixtures,
                        key = { it.uniqueId }
                    ) { fixtureItem ->
                        if (fixtureItem is HighlightUiItem.BannerAd) {
                            TheaterAdCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        } else if (fixtureItem is HighlightUiItem.SupabaseMatch) {
                            TheaterFixtureCard(
                                match = fixtureItem.match,
                                onClick = { }
                            )
                        }
                    }
                }
            }
        }
    }
}
