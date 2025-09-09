package com.example.expensetrackerkotlin.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.data.*
import androidx.compose.ui.text.style.TextOverflow
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import com.example.expensetrackerkotlin.utils.NumberFormatter
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubCategoryDetailBottomSheet(
    subCategoryData: SubCategoryAnalysisData,
    defaultCurrency: String,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit
) {
    var selectedSortOption by remember { mutableStateOf(SortOption.AMOUNT_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }
    
    val sortedExpenses = remember(subCategoryData.expenses, selectedSortOption) {
        when (selectedSortOption) {
            SortOption.AMOUNT_DESC -> subCategoryData.expenses.sortedByDescending { it.getAmountInDefaultCurrency(defaultCurrency) }
            SortOption.AMOUNT_ASC -> subCategoryData.expenses.sortedBy { it.getAmountInDefaultCurrency(defaultCurrency) }
            SortOption.DATE_DESC -> subCategoryData.expenses.sortedByDescending { it.date }
            SortOption.DATE_ASC -> subCategoryData.expenses.sortedBy { it.date }
            SortOption.NAME_ASC -> subCategoryData.expenses.sortedBy { it.description }
            SortOption.NAME_DESC -> subCategoryData.expenses.sortedByDescending { it.description }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        subCategoryData.parentCategory.getColor().copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = subCategoryData.parentCategory.getIcon(),
                    contentDescription = subCategoryData.subCategory.name,
                    tint = subCategoryData.parentCategory.getColor(),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subCategoryData.subCategory.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeColors.getTextColor(isDarkTheme),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Ana Kategori: ${subCategoryData.parentCategory.name}",
                    fontSize = 14.sp,
                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                )
            }
        }

        // Summary section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = subCategoryData.parentCategory.getColor().copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Toplam Tutar",
                        fontSize = 14.sp,
                        color = ThemeColors.getTextGrayColor(isDarkTheme)
                    )
                    Text(
                        text = "$defaultCurrency ${NumberFormatter.formatAmount(subCategoryData.totalAmount)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeColors.getTextColor(isDarkTheme)
                    )
                }
                
                Divider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp),
                    color = ThemeColors.getTextGrayColor(isDarkTheme).copy(alpha = 0.3f)
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Harcama Sayısı",
                        fontSize = 14.sp,
                        color = ThemeColors.getTextGrayColor(isDarkTheme)
                    )
                    Text(
                        text = "${subCategoryData.expenseCount}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeColors.getTextColor(isDarkTheme)
                    )
                }
                
                Divider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp),
                    color = ThemeColors.getTextGrayColor(isDarkTheme).copy(alpha = 0.3f)
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ortalama",
                        fontSize = 14.sp,
                        color = ThemeColors.getTextGrayColor(isDarkTheme)
                    )
                    Text(
                        text = "$defaultCurrency ${NumberFormatter.formatAmount(subCategoryData.totalAmount / subCategoryData.expenseCount)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeColors.getTextColor(isDarkTheme)
                    )
                }
            }
        }

        // Sort section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Harcamalar",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeColors.getTextColor(isDarkTheme)
            )

            Box {
                TextButton(
                    onClick = { showSortMenu = true }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "Sort",
                        tint = ThemeColors.getTextColor(isDarkTheme),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = selectedSortOption.displayName,
                        color = ThemeColors.getTextColor(isDarkTheme),
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = ThemeColors.getTextColor(isDarkTheme),
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier.background(ThemeColors.getCardBackgroundColor(isDarkTheme))
                ) {
                    SortOption.values().forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.displayName,
                                    color = ThemeColors.getTextColor(isDarkTheme)
                                )
                            },
                            onClick = {
                                selectedSortOption = option
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Expenses list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxHeight()
        ) {
            items(sortedExpenses) { expense ->
                SubCategoryExpenseItem(
                    expense = expense,
                    defaultCurrency = defaultCurrency,
                    isDarkTheme = isDarkTheme,
                    categoryColor = subCategoryData.parentCategory.getColor()
                )
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun SubCategoryExpenseItem(
    expense: Expense,
    defaultCurrency: String,
    isDarkTheme: Boolean,
    categoryColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ThemeColors.getCardBackgroundColor(isDarkTheme)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(categoryColor, CircleShape)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.description,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = ThemeColors.getTextColor(isDarkTheme),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = expense.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")),
                    fontSize = 12.sp,
                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                )
                
                if (expense.recurrenceType != RecurrenceType.NONE) {
                    Text(
                        text = "Tekrarlayan: ${when (expense.recurrenceType) {
                            RecurrenceType.DAILY -> "Her gün"
                            RecurrenceType.WEEKDAYS -> "Hafta içi"
                            RecurrenceType.WEEKLY -> "Haftalık"
                            RecurrenceType.MONTHLY -> "Aylık" 
                            else -> ""
                        }}",
                        fontSize = 11.sp,
                        color = ThemeColors.getTextGrayColor(isDarkTheme),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$defaultCurrency ${NumberFormatter.formatAmount(expense.getAmountInDefaultCurrency(defaultCurrency))}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
            }
        }
    }
}