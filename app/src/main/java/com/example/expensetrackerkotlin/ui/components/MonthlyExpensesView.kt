package com.example.expensetrackerkotlin.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import com.example.expensetrackerkotlin.data.Expense
import com.example.expensetrackerkotlin.viewmodel.ExpenseViewModel
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun MonthlyExpensesView(
    modifier: Modifier = Modifier,
    currentMonth: YearMonth,
    expenses: List<Expense>,
    viewModel: ExpenseViewModel,
    isDarkTheme: Boolean
) {
    val categories by viewModel.categories.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()
    val editingExpenseId by viewModel.editingExpenseId.collectAsState()
    
    // Get expenses for the current month (including recurring expenses)
    val monthlyExpenses = remember(expenses, currentMonth) {
        expenses.filter { expense ->
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
        }.sortedByDescending { it.date }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Month header
        Text(
            text = currentMonth.format(
                DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("tr"))
            ),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ThemeColors.getTextColor(isDarkTheme),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        // Expenses list
        if (monthlyExpenses.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "No expenses",
                    modifier = Modifier.size(60.dp),
                    tint = ThemeColors.getTextGrayColor(isDarkTheme)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Bu ayda harcama yok",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeColors.getTextColor(isDarkTheme),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Bu aya harcama eklemek için ana ekranda + butonuna basın",
                    fontSize = 16.sp,
                    color = ThemeColors.getTextGrayColor(isDarkTheme),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(monthlyExpenses) { expense ->
                    ExpenseRowView(
                        expense = expense,
                        onUpdate = { updatedExpense ->
                            viewModel.updateExpense(updatedExpense)
                        },
                        onEditingChanged = { isEditing ->
                            if (isEditing) {
                                viewModel.setEditingExpenseId(expense.id)
                            } else {
                                viewModel.setEditingExpenseId(null)
                            }
                        },
                        onDelete = {
                            viewModel.deleteExpense(expense.id)
                        },
                        isCurrentlyEditing = editingExpenseId == expense.id,
                        dailyExpenseRatio = viewModel.getDailyExpenseRatio(expense),
                        defaultCurrency = viewModel.defaultCurrency,
                        isDarkTheme = isDarkTheme,
                        isRecurringExpenseMode = false,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}