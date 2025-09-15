package com.example.expensetrackerkotlin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.data.DailyData
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import com.example.expensetrackerkotlin.utils.NumberFormatter
import java.time.LocalDateTime

@Composable
fun DailyHistoryView(
    modifier: Modifier = Modifier,
    weeklyData: List<List<DailyData>>, // 3 weeks of data
    selectedDate: LocalDateTime,
    onDateSelected: (LocalDateTime) -> Unit,
    onWeekNavigate: (Int) -> Unit, // -1 for previous, +1 for next
    isDarkTheme: Boolean = true,
) {
    val scope = rememberCoroutineScope()

    // Infinite pager - start at a high number to allow scrolling both ways
    val initialPage = 50000
    val pagerState = rememberPagerState(initialPage = initialPage) { Int.MAX_VALUE }

    // Find which week contains the selected date
    val selectedWeekIndex = weeklyData.indexOfFirst { week ->
        week.any { day -> day.date.toLocalDate() == selectedDate.toLocalDate() }
    }

    weeklyData.forEachIndexed { index, week ->
        val weekStart = week.firstOrNull()?.date?.toLocalDate()
        val weekEnd = week.lastOrNull()?.date?.toLocalDate()
    }

    // Monitor page changes for week navigation - only when user settles on a new page
    LaunchedEffect(pagerState.settledPage) {
        val currentOffset = pagerState.settledPage - initialPage
        if (currentOffset != 0) {
            val direction = if (currentOffset > 0) 1 else -1

            // Navigate to new week
            onWeekNavigate(direction)

            // Reset pager to center immediately after navigation
            pagerState.scrollToPage(initialPage)
        }
    }

    // Don't auto-navigate based on selected week index during manual scrolling
    // This was causing the infinite loop - let manual navigation work independently

    // Get current week data (middle week)
    val currentWeekData = if (weeklyData.size >= 2) weeklyData[1] else emptyList()

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        pageSpacing = 16.dp
    ) { pageIndex ->
        // Calculate which week to show based on page offset
        val pageOffset = pageIndex - initialPage

        // Show the week that corresponds to this page offset
        // -1 page = week 0, 0 page = week 1, +1 page = week 2
        val weekIndex = when {
            pageOffset <= -1 -> 0 // Previous week
            pageOffset == 0 -> 1 // Current week (center)
            else -> 2 // Next week
        }

        val weekData = if (weekIndex < weeklyData.size) {
            weeklyData[weekIndex]
        } else {
            emptyList()
        }

        weekData.firstOrNull()?.let { firstDay ->
        }

        // Display week as a row of days
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            weekData.forEach { dayData ->
                DailyHistoryItem(
                    data = dayData,
                    isSelected = selectedDate.toLocalDate() == dayData.date.toLocalDate(),
                    isDarkTheme = isDarkTheme,
                    onClick = { onDateSelected(dayData.date) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DailyHistoryItem(
    data: DailyData,
    isSelected: Boolean,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .background(
                if (isSelected) ThemeColors.getTextColor(isDarkTheme).copy(alpha = 0.2f) else Color.Transparent
            )
            .padding(1.dp)
    ) {
        // Day letter
        Text(
            text = data.dayName,
            fontSize = 12.sp,
            color = if (isSelected) ThemeColors.getTextColor(isDarkTheme) else ThemeColors.getTextGrayColor(isDarkTheme),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Progress ring (small)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(40.dp)
        ) {
            // Progress ring - always show, even if progress is 0
            ProgressRing(
                progress = data.progressPercentage.toFloat(),
                isLimitOver = data.isOverLimit,
                modifier = Modifier.size(50.dp),
                strokeWidth = 5.dp
            )
            
            // Day number
            Text(
                text = data.dayNumber,
                fontSize = 12.sp,
                color = ThemeColors.getTextColor(isDarkTheme),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Amount
        Text(
            text = NumberFormatter.formatAmount(data.totalAmount),
            fontSize = 10.sp,
            color = if (data.isOverLimit) Color.Red else ThemeColors.getTextColor(isDarkTheme),
            fontWeight = FontWeight.Medium
        )
    }
}