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
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.expensetrackerkotlin.R
import com.example.expensetrackerkotlin.data.*
import com.example.expensetrackerkotlin.ui.screens.calculateCategoryComparison
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import com.example.expensetrackerkotlin.utils.NumberFormatter
import java.time.format.DateTimeFormatter

data class CategoryAnalysisData(
    val category: Category,
    val totalAmount: Double,
    val expenseCount: Int,
    val percentage: Double,
    val expenses: List<Expense>
)

enum class SortOption {
    AMOUNT_DESC,
    AMOUNT_ASC,
    DATE_DESC,
    DATE_ASC,
    NAME_ASC,
    NAME_DESC
}

@Composable
fun getSortOptionDisplayName(sortOption: SortOption): String {
    return when (sortOption) {
        SortOption.AMOUNT_DESC -> stringResource(R.string.sort_amount_desc)
        SortOption.AMOUNT_ASC -> stringResource(R.string.sort_amount_asc)
        SortOption.DATE_DESC -> stringResource(R.string.sort_date_desc)
        SortOption.DATE_ASC -> stringResource(R.string.sort_date_asc)
        SortOption.NAME_ASC -> stringResource(R.string.sort_name_asc)
        SortOption.NAME_DESC -> stringResource(R.string.sort_name_desc)
    }
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailBottomSheet(
    categoryData: CategoryAnalysisData,
    subCategories: List<SubCategory>,
    defaultCurrency: String,
    isDarkTheme: Boolean,
    sortOption: SortOption,
    showSortMenu: Boolean,
    onSortOptionChanged: (SortOption) -> Unit,
    onShowSortMenuChanged: (Boolean) -> Unit,
    viewModel: com.example.expensetrackerkotlin.viewmodel.ExpenseViewModel,
    selectedMonth: java.time.YearMonth,
    selectedFilterType: ExpenseFilterType
) {
    val sortedExpenses = remember(categoryData.expenses, sortOption) {
        when (sortOption) {
            SortOption.AMOUNT_DESC -> categoryData.expenses.sortedByDescending { it.amount }
            SortOption.AMOUNT_ASC -> categoryData.expenses.sortedBy { it.amount }
            SortOption.DATE_DESC -> categoryData.expenses.sortedByDescending { it.date }
            SortOption.DATE_ASC -> categoryData.expenses.sortedBy { it.date }
            SortOption.NAME_ASC -> categoryData.expenses.sortedBy { it.description }
            SortOption.NAME_DESC -> categoryData.expenses.sortedByDescending { it.description }
        }
    }
    
    val comparison = remember(categoryData.category.id, selectedMonth, selectedFilterType) {
        calculateCategoryComparison(viewModel, selectedMonth, categoryData.category.id, selectedFilterType)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(categoryData.category.getColor().copy(alpha = 0.1f))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            categoryData.category.getColor().copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryData.category.getIcon(),
                        contentDescription = null,
                        tint = categoryData.category.getColor(),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = categoryData.category.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeColors.getTextColor(isDarkTheme)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                        Text(
                            text = "$defaultCurrency ${NumberFormatter.formatAmount(categoryData.totalAmount)}",
                            fontSize = 18.sp,
                            color = ThemeColors.getTextGrayColor(isDarkTheme)
                        )
                        Text(
                            text = "  •  ${categoryData.expenseCount} ${stringResource(R.string.expense_lowercase)} • %${
                                String.format(
                                    "%.1f",
                                    categoryData.percentage * 100
                                )
                            }",
                            fontSize = 16.sp,
                            color = ThemeColors.getTextGrayColor(isDarkTheme)
                        )
                    }
                    // Comparison indicators
                    Spacer(modifier = Modifier.height(8.dp))

                        DetailComparisonIndicator(
                            amount = comparison.vsLastMonth,
                            currency = viewModel.defaultCurrency,
                            label = stringResource(R.string.vs_previous_month),
                            isDarkTheme = isDarkTheme
                        )
                        DetailComparisonIndicator(
                            amount = comparison.vsAverage,
                            currency = viewModel.defaultCurrency,
                            label = stringResource(R.string.vs_6_month_average),
                            isDarkTheme = isDarkTheme
                        )

                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sort Button
        Box {
            OutlinedButton(
                onClick = { onShowSortMenuChanged(true) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = ThemeColors.getTextColor(isDarkTheme)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = getSortOptionDisplayName(sortOption),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { onShowSortMenuChanged(false) },
                modifier = Modifier.background(ThemeColors.getCardBackgroundColor(isDarkTheme))
            ) {
                SortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = getSortOptionDisplayName(option),
                                color = ThemeColors.getTextColor(isDarkTheme),
                                fontSize = 14.sp
                            )
                        },
                        onClick = {
                            onSortOptionChanged(option)
                            onShowSortMenuChanged(false)
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Expenses List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(sortedExpenses) { expense ->
                val subCategory = subCategories.find { it.id == expense.subCategoryId }
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = ThemeColors.getCardBackgroundColor(isDarkTheme)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    )
                    {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = subCategory?.name ?: stringResource(R.string.unknown),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 17.sp,
                                    color = ThemeColors.getTextColor(isDarkTheme)
                                )
                                if (expense.description.isNotBlank()) {
                                    Text(
                                        text = expense.description,
                                        fontSize = 15.sp,
                                        color = ThemeColors.getTextGrayColor(isDarkTheme),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                Text(
                                    text = expense.date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                                    fontSize = 14.sp,
                                    color = ThemeColors.getTextGrayColor(isDarkTheme),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                if (expense.recurrenceType != RecurrenceType.NONE) {
                                    Text(
                                        text = "${stringResource(R.string.recurring_label)}: ${when (expense.recurrenceType) {
                                            RecurrenceType.DAILY -> stringResource(R.string.daily)
                                            RecurrenceType.WEEKDAYS -> stringResource(R.string.weekdays)
                                            RecurrenceType.WEEKLY -> stringResource(R.string.weekly)
                                            RecurrenceType.MONTHLY -> stringResource(R.string.monthly)
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
                                    text = "$defaultCurrency ${NumberFormatter.formatAmount(expense.amount)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = ThemeColors.getTextColor(isDarkTheme)
                                )
                                if (expense.exchangeRate != null && expense.currency != defaultCurrency) {
                                    Text(
                                        text = "${expense.currency} ${NumberFormatter.formatAmount(expense.amount)}",
                                        fontSize = 12.sp,
                                        color = ThemeColors.getTextGrayColor(isDarkTheme),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Add some bottom padding for better UX
    }
}

@Composable
private fun DetailComparisonIndicator(
    amount: Double,
    currency: String,
    label: String,
    isDarkTheme: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = ThemeColors.getTextGrayColor(isDarkTheme)
            )
        Text(
            text = if (amount == 0.0) "±0" else "${if (amount > 0) "+" else ""}$currency ${NumberFormatter.formatAmount(kotlin.math.abs(amount))}",
            fontSize = 16.sp,
            color = when {
                amount > 0 -> Color.Red
                amount < 0 -> Color.Green
                else -> ThemeColors.getTextColor(isDarkTheme)
            },
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )



    }
}

