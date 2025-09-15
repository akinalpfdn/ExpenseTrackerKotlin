package com.example.expensetrackerkotlin.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.expensetrackerkotlin.R
import com.example.expensetrackerkotlin.data.Category
import com.example.expensetrackerkotlin.data.Expense
import com.example.expensetrackerkotlin.data.SubCategory
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class DailyCategorySortType {
    AMOUNT_HIGH_TO_LOW,
    AMOUNT_LOW_TO_HIGH,
    TIME_NEWEST_FIRST,
    TIME_OLDEST_FIRST,
    DESCRIPTION_A_TO_Z,
    DESCRIPTION_Z_TO_A
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyCategoryDetailBottomSheet(
    category: Category,
    selectedDateExpenses: List<Expense>,
    subCategories: List<SubCategory>,
    selectedDate: LocalDateTime,
    defaultCurrency: String,
    isDarkTheme: Boolean = true,
    onDismiss: () -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var currentSortType by remember { mutableStateOf(DailyCategorySortType.AMOUNT_HIGH_TO_LOW) }
    
    val categoryExpenses = remember(selectedDateExpenses, category) {
        selectedDateExpenses.filter { expense -> 
            expense.categoryId == category.id
        }
    }
    
    val sortedExpenses = remember(categoryExpenses, currentSortType, subCategories) {
        when (currentSortType) {
            DailyCategorySortType.AMOUNT_HIGH_TO_LOW -> categoryExpenses.sortedByDescending { it.getAmountInDefaultCurrency(defaultCurrency) }
            DailyCategorySortType.AMOUNT_LOW_TO_HIGH -> categoryExpenses.sortedBy { it.getAmountInDefaultCurrency(defaultCurrency) }
            DailyCategorySortType.TIME_NEWEST_FIRST -> categoryExpenses.sortedByDescending { it.date }
            DailyCategorySortType.TIME_OLDEST_FIRST -> categoryExpenses.sortedBy { it.date }
            DailyCategorySortType.DESCRIPTION_A_TO_Z -> categoryExpenses.sortedBy { it.description }
            DailyCategorySortType.DESCRIPTION_Z_TO_A -> categoryExpenses.sortedByDescending { it.description }
        }
    }
    
    val totalAmount = categoryExpenses.sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
    
    val sortMenuItems = listOf(
        stringResource(R.string.amount_high_to_low_arrow) to DailyCategorySortType.AMOUNT_HIGH_TO_LOW,
        stringResource(R.string.amount_low_to_high_arrow) to DailyCategorySortType.AMOUNT_LOW_TO_HIGH,
        stringResource(R.string.time_newest_first_arrow) to DailyCategorySortType.TIME_NEWEST_FIRST,
        stringResource(R.string.time_oldest_first_arrow) to DailyCategorySortType.TIME_OLDEST_FIRST,
        stringResource(R.string.description_a_to_z_arrow) to DailyCategorySortType.DESCRIPTION_A_TO_Z,
        stringResource(R.string.description_z_to_a_arrow) to DailyCategorySortType.DESCRIPTION_Z_TO_A
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ThemeColors.getBackgroundColor(isDarkTheme),
        dragHandle = {
            Surface(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp),
                shape = RoundedCornerShape(2.dp),
                color = ThemeColors.getTextGrayColor(isDarkTheme).copy(alpha = 0.5f)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = category.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeColors.getTextColor(isDarkTheme)
                    )
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())),
                        fontSize = 14.sp,
                        color = ThemeColors.getTextGrayColor(isDarkTheme)
                    )
                }
                
                // Sort button
                Box {
                    IconButton(onClick = { showSortMenu = !showSortMenu }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.sort)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(ThemeColors.getCardBackgroundColor(isDarkTheme))
                    ) {
                        sortMenuItems.forEach { (label, sortType) ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = label,
                                        color = ThemeColors.getTextColor(isDarkTheme)
                                    )
                                },
                                onClick = {
                                    currentSortType = sortType
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Total amount
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = category.getColor().copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.total_spending),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = ThemeColors.getTextColor(isDarkTheme)
                    )
                    Text(
                        text = "${String.format("%.2f", totalAmount)} $defaultCurrency",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = category.getColor()
                    )
                }
            }
            
            if (sortedExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_expenses_in_category),
                        fontSize = 16.sp,
                        color = ThemeColors.getTextGrayColor(isDarkTheme),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    text = "${sortedExpenses.size} ${stringResource(R.string.expense_singular)}",
                    fontSize = 14.sp,
                    color = ThemeColors.getTextGrayColor(isDarkTheme),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(sortedExpenses) { expense ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = ThemeColors.getCardBackgroundColor(isDarkTheme)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = expense.description,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ThemeColors.getTextColor(isDarkTheme)
                                    )
                                    val subCategory = subCategories.find { it.id == expense.subCategoryId }
                                    if (subCategory != null) {
                                        Text(
                                            text = subCategory.name,
                                            fontSize = 12.sp,
                                            color = ThemeColors.getTextGrayColor(isDarkTheme)
                                        )
                                    }

                                }
                                Text(
                                    text = "${String.format("%.2f", expense.getAmountInDefaultCurrency(defaultCurrency))} $defaultCurrency",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = category.getColor()
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}