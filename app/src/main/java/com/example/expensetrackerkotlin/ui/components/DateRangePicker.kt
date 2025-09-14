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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.R
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePicker(
    selectedMonth: YearMonth,
    selectedRange: Pair<LocalDate?, LocalDate?>,
    isDarkTheme: Boolean,
    onRangeSelected: (Pair<LocalDate?, LocalDate?>) -> Unit,
    onDismiss: () -> Unit
) {
    val dateRangePickerSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { true }
    )
    
    LaunchedEffect(Unit) {
        dateRangePickerSheetState.expand()
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = dateRangePickerSheetState,
        containerColor = ThemeColors.getBackgroundColor(isDarkTheme),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 0.dp, top = 0.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tarih Aralığı Seçin",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
                
                TextButton(
                    onClick = {
                        onRangeSelected(null to null)
                    }
                ) {
                    Text(
                        text = "Temizle",
                        color = AppColors.PrimaryOrange
                    )
                }
            }
            
            // Selected range display

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.PrimaryOrange.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Text(
                            text = "Seçili Aralık:",
                            fontSize = 14.sp,
                            color = ThemeColors.getTextGrayColor(isDarkTheme),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatDateRange(selectedRange,stringResource(R.string.select_date)),
                            fontSize = 16.sp,
                            color = AppColors.PrimaryOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

            
            // Month calendar
            DateRangeCalendar(
                selectedMonth = selectedMonth,
                selectedRange = selectedRange,
                isDarkTheme = isDarkTheme,
                onDateSelected = { date ->
                    val (start, end) = selectedRange
                    when {
                        start == null -> {
                            // First date selection
                            onRangeSelected(date to null)
                        }
                        end == null -> {
                            // Second date selection
                            if (date.isBefore(start)) {
                                onRangeSelected(date to start)
                            } else {
                                onRangeSelected(start to date)
                            }
                        }
                        else -> {
                            // Reset selection with new start date
                            onRangeSelected(date to null)
                        }
                    }
                }
            )
            /* no need for them
            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThemeColors.getButtonDisabledColor(isDarkTheme)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "İptal",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = ThemeColors.getTextColor(isDarkTheme)
                    )
                }
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.PrimaryOrange
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Seç", fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ThemeColors.getTextColor(isDarkTheme))
                }
            }
*/

        }
    }
}

@Composable
private fun DateRangeCalendar(
    selectedMonth: YearMonth,
    selectedRange: Pair<LocalDate?, LocalDate?>,
    isDarkTheme: Boolean,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = selectedMonth.atDay(1)
    val lastDayOfMonth = selectedMonth.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Make Sunday = 0
    
    // Generate calendar days
    val calendarDays = mutableListOf<LocalDate?>()
    
    // Add empty cells for days before the first day of month
    repeat(firstDayOfWeek) {
        calendarDays.add(null)
    }
    
    // Add all days of the month
    for (day in 1..lastDayOfMonth.dayOfMonth) {
        calendarDays.add(selectedMonth.atDay(day))
    }
    
    Column {
        // Month header
        Text(
            text = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("tr"))),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ThemeColors.getTextColor(isDarkTheme),
            modifier = Modifier
                .padding(start = 10.dp, end = 0.dp, top = 0.dp, bottom = 10.dp),
        )
        
        // Day of week headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val dayHeaders = listOf(
                stringResource(R.string.monday_short),
                stringResource(R.string.tuesday_short),
                stringResource(R.string.wednesday_short),
                stringResource(R.string.thursday_short),
                stringResource(R.string.friday_short),
                stringResource(R.string.saturday_short),
                stringResource(R.string.sunday_short)
            )// Turkish day abbreviations
            dayHeaders.forEach { dayHeader ->
                Text(
                    text = dayHeader,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeColors.getTextGrayColor(isDarkTheme),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Calendar grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(300.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(calendarDays) { date ->
                DateCell(
                    date = date,
                    selectedRange = selectedRange,
                    isDarkTheme = isDarkTheme,
                    onClick = { date?.let(onDateSelected) }
                )
            }
        }
    }
}

@Composable
private fun DateCell(
    date: LocalDate?,
    selectedRange: Pair<LocalDate?, LocalDate?>,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    val (startDate, endDate) = selectedRange
    
    val isInRange = date != null && startDate != null && endDate != null &&
            (date == startDate || date == endDate || (date.isAfter(startDate) && date.isBefore(endDate)))
    
    val isRangeStart = date == startDate
    val isRangeEnd = date == endDate
    val isSelected = isRangeStart || isRangeEnd
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable(enabled = date != null) { onClick() }
            .background(
                color = when {
                    isSelected -> AppColors.PrimaryOrange.copy(alpha = 0.8f)
                    isInRange -> AppColors.PrimaryOrange.copy(alpha = 0.3f)
                    else -> Color.Transparent
                },
                shape = CircleShape
            )
            ,
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 14.sp,
                color = when {
                    isSelected -> Color.White
                    isInRange -> AppColors.PrimaryOrange
                    else -> ThemeColors.getTextColor(isDarkTheme)
                },
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

private fun formatDateRange(range: Pair<LocalDate?, LocalDate?>,text:String): String {
    val (start, end) = range
    val formatter = DateTimeFormatter.ofPattern("dd MMM", Locale.forLanguageTag("tr"))
    
    return when {
        start != null && end != null -> "${start.format(formatter)} - ${end.format(formatter)}"
        start != null -> "${start.format(formatter)} - ?"
        else -> text
    }
}