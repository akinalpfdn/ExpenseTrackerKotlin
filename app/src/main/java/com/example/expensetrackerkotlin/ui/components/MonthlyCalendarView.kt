package com.example.expensetrackerkotlin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.expensetrackerkotlin.data.DailyData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun MonthlyCalendarView(
    selectedDate: LocalDateTime,
    expenses: List<com.example.expensetrackerkotlin.data.Expense>,
    onDateSelected: (LocalDateTime) -> Unit,
    defaultCurrency: String,
    dailyLimit: String,
    modifier: Modifier = Modifier
) {
    val currentMonth = YearMonth.from(selectedDate)
    val daysInMonth = currentMonth.lengthOfMonth()
    
    // Get expenses for the current month
    val monthlyExpenses = remember(expenses, currentMonth) {
        expenses.filter { expense ->
            val expenseDate = expense.date.toLocalDate()
            expenseDate.year == currentMonth.year && expenseDate.month == currentMonth.month
        }
    }
    
    // Group expenses by day
    val expensesByDay = remember(monthlyExpenses) {
        monthlyExpenses.groupBy { it.date.toLocalDate() }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.CardBackground
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
                    text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("tr"))),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextWhite
                )
                
                // Navigation buttons (previous/next month)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            val previousMonth = currentMonth.minusMonths(1)
                            val newDate = previousMonth.atDay(1).atStartOfDay()
                            onDateSelected(newDate)
                        }
                    ) {
                        Text(
                            text = "‹",
                            fontSize = 24.sp,
                            color = AppColors.TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            val nextMonth = currentMonth.plusMonths(1)
                            val newDate = nextMonth.atDay(1).atStartOfDay()
                            onDateSelected(newDate)
                        }
                    ) {
                        Text(
                            text = "›",
                            fontSize = 24.sp,
                            color = AppColors.TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Daily Progress Rings - Grid Layout like Calendar
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                modifier = Modifier.height(400.dp)
            ) {
                items(daysInMonth) { dayIndex ->
                    val day = dayIndex + 1
                    val date = currentMonth.atDay(day)
                    val dayExpenses = expensesByDay[date] ?: emptyList()
                    val dayTotal = dayExpenses.sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
                    val dailyLimitValue = dailyLimit.toDoubleOrNull() ?: 0.0
                    val progressPercentage = if (dailyLimitValue > 0) {
                        minOf(dayTotal / dailyLimitValue, 1.0)
                    } else {
                        0.0
                    }
                    val isOverLimit = dayTotal > dailyLimitValue && dailyLimitValue > 0
                    val isSelected = date == selectedDate.toLocalDate()
                    val isToday = date == LocalDate.now()
                    
                    // Create DailyData-like object for consistent styling
                    val dailyData = DailyData(
                        date = date.atStartOfDay(),
                        totalAmount = dayTotal,
                        expenseCount = dayExpenses.size,
                        dailyLimit = dailyLimitValue
                    )
                    
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
                                     colors = dailyData.progressColors,
                                     strokeWidth = 3.dp,
                                     modifier = Modifier.size(50.dp)
                                 )
                            }
                            
                                                         // Day number
                             Text(
                                 text = day.toString(),
                                 fontSize = 14.sp,
                                 fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isSelected -> AppColors.TextWhite
                                    isToday -> AppColors.PrimaryOrange
                                    dayExpenses.isNotEmpty() -> AppColors.TextWhite
                                    else -> AppColors.TextGray
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                                                 // Amount
                         Text(
                             text = "$defaultCurrency${String.format("%.0f", dayTotal)}",
                             fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                isOverLimit -> Color.Red
                                dayExpenses.isNotEmpty() -> AppColors.TextWhite
                                else -> AppColors.TextGray
                            },
                            textAlign = TextAlign.Center
                        )
                        
                                                 // Day name (short)
                         Text(
                             text = date.format(DateTimeFormatter.ofPattern("E", Locale.forLanguageTag("tr"))).first().toString().uppercase(),
                             fontSize = 8.sp,
                            color = AppColors.TextGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
