package com.dirzaaulia.footballclips.ui.score

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday

@Composable
fun DateSelectorBar(
    dates: List<DateOption>,
    selectedDate: String?,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedDate) {
        val selectedIndex = dates.indexOfFirst { it.date == selectedDate }
        if (selectedIndex >= 0) {
            val scrollIndex = (selectedIndex - 1).coerceAtLeast(0)
            listState.animateScrollToItem(scrollIndex)
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(dates) { option ->
            DateItem(
                option = option,
                isSelected = option.date == selectedDate,
                onClick = { onDateSelected(option.date) }
            )
        }
    }
}

@Composable
fun DateItem(
    option: DateOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier.height(36.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            val dateNum = option.displayDate.split(" ").first()
            val text = when (option.displayDay.uppercase()) {
                "TODAY", "YEST", "TMRW" -> "${option.displayDay.uppercase()} $dateNum"
                else -> "${option.displayDay} $dateNum"
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
fun DateSelectorCard(
    dates: List<DateOption>,
    selectedDate: String?,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    WebDateSelectorCard(
        dates = dates,
        selectedDate = selectedDate,
        onDateSelected = onDateSelected,
        modifier = modifier
    )
}

@Composable
fun WebDateSelectorCard(
    dates: List<DateOption>,
    selectedDate: String?,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    val selectedIndex = dates.indexOfFirst { it.date == selectedDate }.takeIf { it >= 0 } ?: dates.indexOfFirst { it.isToday }
    val currentOption = dates.getOrNull(selectedIndex) ?: return

    val canGoBack = selectedIndex > 0
    val canGoForward = selectedIndex < dates.size - 1

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = modifier.height(48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight().padding(horizontal = 4.dp)
        ) {
            IconButton(
                onClick = { if (canGoBack) onDateSelected(dates[selectedIndex - 1].date) },
                enabled = canGoBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous Day",
                    tint = if (canGoBack) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            Surface(
                onClick = { showDatePicker = true },
                color = Color.Transparent,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Calendar",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentOption.isToday) "TODAY, ${currentOption.displayDate}" else "${currentOption.displayDay}, ${currentOption.displayDate}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            IconButton(
                onClick = { if (canGoForward) onDateSelected(dates[selectedIndex + 1].date) },
                enabled = canGoForward
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Day",
                    tint = if (canGoForward) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
    }

    if (showDatePicker) {
        DropdownMenu(
            expanded = showDatePicker,
            onDismissRequest = { showDatePicker = false },
            modifier = Modifier.heightIn(max = 400.dp).width(200.dp),
            shape = RoundedCornerShape(12.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            dates.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (option.isToday) "Today, ${option.displayDate}" else "${option.displayDay}, ${option.displayDate}",
                            fontWeight = if (option.date == selectedDate) FontWeight.Black else FontWeight.Normal,
                            color = if (option.date == selectedDate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onDateSelected(option.date)
                        showDatePicker = false
                    }
                )
            }
        }
    }
}
