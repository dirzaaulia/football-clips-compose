package com.dirzaaulia.footballclips.ui.info

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InfoScreen(
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxHeight < 500.dp
        val isWide = maxWidth > 840.dp && !isLandscape

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Information",
                            style = if (isLandscape) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isLandscape) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 60.dp, start = 16.dp, end = 16.dp, top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SupportDeveloperCard(
                                initialExpanded = true,
                                uriHandler = uriHandler
                            )
                        }
                        item { DataSourceCard(initialExpanded = false) }
                        item { AccountPurchasesCard(initialExpanded = false) }
                        item { DisclaimerCard(initialExpanded = false) }
                        item { PrivacyPolicyCard(initialExpanded = false) }
                    }
                } else if (isWide) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        WasmHeroBanner()

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            maxItemsInEachRow = 2
                        ) {
                            SupportDeveloperCard(
                                initialExpanded = true,
                                uriHandler = uriHandler,
                                modifier = Modifier.fillMaxWidth()
                            )
                            DataSourceCard(
                                initialExpanded = false,
                                modifier = Modifier
                                    .weight(1f)
                                    .widthIn(min = 380.dp)
                            )
                            AccountPurchasesCard(
                                initialExpanded = false,
                                modifier = Modifier
                                    .weight(1f)
                                    .widthIn(min = 380.dp)
                            )
                            DisclaimerCard(
                                initialExpanded = false,
                                modifier = Modifier
                                    .weight(1f)
                                    .widthIn(min = 380.dp)
                            )
                            PrivacyPolicyCard(
                                initialExpanded = false,
                                modifier = Modifier
                                    .weight(1f)
                                    .widthIn(min = 380.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(120.dp))
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SupportDeveloperCard(
                            initialExpanded = true,
                            uriHandler = uriHandler
                        )
                        DataSourceCard(initialExpanded = false)
                        AccountPurchasesCard(initialExpanded = false)
                        DisclaimerCard(initialExpanded = false)
                        PrivacyPolicyCard(initialExpanded = false)

                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }
            }
        }
    }
}
