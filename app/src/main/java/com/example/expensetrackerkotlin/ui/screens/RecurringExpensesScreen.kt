package com.example.expensetrackerkotlin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.data.Expense
import com.example.expensetrackerkotlin.data.RecurrenceType
import com.example.expensetrackerkotlin.ui.components.ExpenseRowView
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import com.example.expensetrackerkotlin.viewmodel.ExpenseViewModel
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringExpensesScreen(
    viewModel: ExpenseViewModel,
    onDismiss: () -> Unit
) {
    val expenses by viewModel.expenses.collectAsState()
    val isDarkTheme = viewModel.theme == "dark"
    
    // Get only recurring expenses
    val recurringExpenses = remember(expenses) {
        expenses.filter { it.recurrenceType != RecurrenceType.NONE }
            .groupBy { it.recurrenceGroupId }
            .map { (_, groupExpenses) -> groupExpenses.first() } // Take one from each group
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeColors.getBackgroundColor(isDarkTheme))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = ThemeColors.getTextColor(isDarkTheme)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Tekrar Eden Harcamalar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
            }
            
            // Content
            if (recurringExpenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "Henüz tekrar eden harcama yok",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = ThemeColors.getTextColor(isDarkTheme),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Tekrar eden harcama eklemek için + butonuna basın",
                            fontSize = 14.sp,
                            color = ThemeColors.getTextGrayColor(isDarkTheme),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recurringExpenses) { expense ->
                        RecurringExpenseCard(
                            expense = expense,
                            viewModel = viewModel,
                            isDarkTheme = isDarkTheme,
                            onDelete = {
                                // Delete all expenses with the same recurrence group ID that are from today onwards
                                val today = java.time.LocalDate.now()
                                val expensesToDelete = expenses.filter { 
                                    it.recurrenceGroupId == expense.recurrenceGroupId &&
                                    it.date.toLocalDate().isAfter(today.minusDays(1)) // Today and future
                                }
                                expensesToDelete.forEach { viewModel.deleteExpense(it.id) }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecurringExpenseCard(
    expense: Expense,
    viewModel: ExpenseViewModel,
    isDarkTheme: Boolean,
    onDelete: () -> Unit
) {
    // Create a modified expense for display with recurrence info
    val displayExpense = expense.copy(
        description = buildString {
            append(expense.description)
            if (expense.description.isNotEmpty()) append("\n")
            append("${expense.recurrenceType.name.lowercase().replaceFirstChar { it.uppercase() }} • ")
            append("Başlangıç: ${expense.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("tr")))}")
            expense.endDate?.let { endDate ->
                append(" • Bitiş: ${endDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("tr")))}")
            }
        }
    )
    
    ExpenseRowView(
        expense = displayExpense,
        onUpdate = { updatedExpense ->
            // Update all expenses with the same recurrence group ID that are from today onwards
            val today = java.time.LocalDate.now()
            val expensesToUpdate = viewModel.expenses.value.filter { 
                it.recurrenceGroupId == expense.recurrenceGroupId &&
                it.date.toLocalDate().isAfter(today.minusDays(1)) // Today and future
            }
            expensesToUpdate.forEach { 
                val updatedExpenseWithSameId = updatedExpense.copy(
                    id = it.id,
                    date = it.date, // Keep original date for each occurrence
                    recurrenceType = it.recurrenceType, // Keep original recurrence type
                    recurrenceGroupId = it.recurrenceGroupId, // Keep original group ID
                    description = expense.description // Keep original description
                )
                viewModel.updateExpense(updatedExpenseWithSameId)
            }
        },
        onEditingChanged = { isEditing ->
            if (isEditing) {
                // Handle editing state if needed
            }
        },
        onDelete = onDelete,
        isCurrentlyEditing = false,
        dailyExpenseRatio = 1.0, // Not relevant for recurring expenses
        defaultCurrency = viewModel.defaultCurrency,
        isDarkTheme = isDarkTheme,
        isRecurringExpenseMode = true
    )
}
