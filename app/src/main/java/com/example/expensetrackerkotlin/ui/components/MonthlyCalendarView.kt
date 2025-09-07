package com.example.expensetrackerkotlin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import com.example.expensetrackerkotlin.data.DailyData
import com.example.expensetrackerkotlin.data.Expense
import com.example.expensetrackerkotlin.data.RecurrenceType
import com.example.expensetrackerkotlin.utils.NumberFormatter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun MonthlyCalendarView(
    modifier: Modifier = Modifier,
    selectedDate: LocalDateTime,
    expenses: List<Expense>,
    onDateSelected: (LocalDateTime) -> Unit,
    defaultCurrency: String,
    dailyLimit: String,
    isDarkTheme: Boolean = true,
    onMonthChanged: (YearMonth) -> Unit = {},
) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    currentMonth.lengthOfMonth()

    // Notify parent of initial month
    LaunchedEffect(Unit) {
        onMonthChanged(currentMonth)
    }

    // Get expenses for the current month (including recurring expenses)
    val monthlyExpenses = remember(expenses, currentMonth) {
        expenses.filter { expense ->
            // Check if this expense is active on any day in this month
            val startOfMonth = currentMonth.atDay(1)
            val endOfMonth = currentMonth.atEndOfMonth()

            var currentDate = startOfMonth
            while (!currentDate.isAfter(endOfMonth)) {
                if (expense.isActiveOnDate(currentDate.atStartOfDay())) {
                    return@filter true
                }
                currentDate = currentDate.plusDays(1)
            }
            false
        }
    }

    // Group expenses by day (including recurring expenses)
    val expensesByDay = remember(monthlyExpenses, currentMonth) {
        val startOfMonth = currentMonth.atDay(1)
        val endOfMonth = currentMonth.atEndOfMonth()

        val groupedExpenses = mutableMapOf<LocalDate, MutableList<Expense>>()

        // Initialize all days in the month
        var currentDate = startOfMonth
        while (!currentDate.isAfter(endOfMonth)) {
            groupedExpenses[currentDate] = mutableListOf()
            currentDate = currentDate.plusDays(1)
        }

        // Add expenses to their active days
        monthlyExpenses.forEach { expense ->
            var checkDate = startOfMonth
            while (!checkDate.isAfter(endOfMonth)) {
                if (expense.isActiveOnDate(checkDate.atStartOfDay())) {
                    groupedExpenses[checkDate]?.add(expense)
                }
                checkDate = checkDate.plusDays(1)
            }
        }

        groupedExpenses
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ThemeColors.getCardBackgroundColor(isDarkTheme)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Month and Year Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentMonth.format(
                        DateTimeFormatter.ofPattern(
                            "MMMM yyyy",
                            Locale.forLanguageTag("tr")
                        )
                    ),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeColors.getTextColor(isDarkTheme)
                )

                // Navigation buttons (previous/next month)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            currentMonth = currentMonth.minusMonths(1)
                            onMonthChanged(currentMonth)
                        }
                    ) {
                        Text(
                            text = "‹",
                            fontSize = 24.sp,
                            color = ThemeColors.getTextColor(isDarkTheme),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = {
                            currentMonth = currentMonth.plusMonths(1)
                            onMonthChanged(currentMonth)
                        }
                    ) {
                        Text(
                            text = "›",
                            fontSize = 24.sp,
                            color = ThemeColors.getTextColor(isDarkTheme),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Day headers (Pazartesi'den Pazar'a)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("P", "S", "Ç", "P", "C", "C", "P").forEach { dayName ->
                    Text(
                        text = dayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ThemeColors.getTextGrayColor(isDarkTheme),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Daily Progress Rings - Grid Layout like Calendar
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                modifier = Modifier.height(600.dp)
            ) {
                // Calculate the start of the week (Monday) for the first day of the month
                val firstDayOfMonth = currentMonth.atDay(1)
                val dayOfWeek = firstDayOfMonth.dayOfWeek.value // 1=Monday, 7=Sunday
                val daysFromMonday = dayOfWeek - 1 // 0=Monday, 6=Sunday
                val startOfWeek = firstDayOfMonth.minusDays(daysFromMonday.toLong())

                // Calculate total days to show (including previous month's days and next month's days)
                val endOfMonth = currentMonth.atEndOfMonth()
                val lastDayOfMonth = endOfMonth.dayOfWeek.value // 1=Monday, 7=Sunday
                val daysToEndOfWeek = 7 - lastDayOfMonth // Days needed to complete the week
                val endOfWeek = endOfMonth.plusDays(daysToEndOfWeek.toLong())

                val totalDaysToShow =
                    java.time.temporal.ChronoUnit.DAYS.between(startOfWeek, endOfWeek) + 1

                items(totalDaysToShow.toInt()) { dayIndex ->
                    val date = startOfWeek.plusDays(dayIndex.toLong())
                    val isCurrentMonth = date.month == currentMonth.month
                    val dayExpenses = expensesByDay[date] ?: emptyList()
                    val dayTotal =
                        dayExpenses.sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
                    val dayProgressTotal = dayExpenses.filter { it.recurrenceType == RecurrenceType.NONE }
                        .sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
                    val dailyLimitValue = dailyLimit.toDoubleOrNull() ?: 0.0
                    val progressPercentage = if (dailyLimitValue > 0) {
                        minOf(dayProgressTotal / dailyLimitValue, 1.0)
                    } else {
                        0.0
                    }
                    val isOverLimit = dayProgressTotal > dailyLimitValue && dailyLimitValue > 0
                    val isSelected = date == selectedDate.toLocalDate()
                    val isToday = date == LocalDate.now()

                    // Create DailyData-like object for consistent styling
                    DailyData(
                        date = date.atStartOfDay(),
                        totalAmount = dayTotal,
                        progressAmount = dayProgressTotal,
                        expenseCount = dayExpenses.size,
                        dailyLimit = dailyLimitValue
                    )

                    // Skip rendering if not current month and no expenses
                    if (!isCurrentMonth && dayExpenses.isEmpty()) {
                        // Empty space for previous/next month days
                        Box(modifier = Modifier.size(50.dp))
                    } else {

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDateSelected(date.atStartOfDay())
                                }
                        ) {
                            // Progress Ring
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(50.dp)
                            ) {
                                // Background circle
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            color = when {
                                                isToday -> AppColors.PrimaryOrange.copy(alpha = 0.3f)
                                                else -> Color.Transparent
                                            },
                                            shape = CircleShape
                                        )
                                )

                                // Progress ring overlay
                                if (dayExpenses.isNotEmpty()) {
                                    ProgressRing(
                                        progress = progressPercentage.toFloat(),
                                        isLimitOver = isOverLimit,
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(50.dp)
                                    )
                                }

                                // Day number
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> ThemeColors.getTextColor(isDarkTheme)
                                        isToday -> AppColors.PrimaryOrange
                                        !isCurrentMonth -> ThemeColors.getTextGrayColor(isDarkTheme)
                                            .copy(alpha = 0.3f)

                                        dayExpenses.isNotEmpty() -> ThemeColors.getTextColor(
                                            isDarkTheme
                                        )

                                        else -> ThemeColors.getTextGrayColor(isDarkTheme)
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Amount
                            Text(
                                text = "$defaultCurrency${NumberFormatter.formatAmount(dayTotal)}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = when {
                                    isOverLimit -> Color.Red
                                    !isCurrentMonth -> ThemeColors.getTextGrayColor(isDarkTheme)
                                        .copy(alpha = 0.3f)

                                    dayExpenses.isNotEmpty() -> ThemeColors.getTextColor(isDarkTheme)
                                    else -> ThemeColors.getTextGrayColor(isDarkTheme)
                                },
                                textAlign = TextAlign.Center
                            )

                            
                        }
                    }
                }
            }
        }
    }
}
