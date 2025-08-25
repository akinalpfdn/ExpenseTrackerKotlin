package com.example.expensetrackerkotlin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    
    var editingExpenseId by remember { mutableStateOf<String?>(null) }
    
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
                            isDarkTheme = isDarkTheme,
                            onEdit = { editingExpenseId = expense.id },
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
    
    // Edit Expense Bottom Sheet
    if (editingExpenseId != null) {
        val expenseToEdit = recurringExpenses.find { it.id == editingExpenseId }
        if (expenseToEdit != null) {
            val sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { true }
            )
            
            LaunchedEffect(Unit) {
                sheetState.expand()
            }
            
            ModalBottomSheet(
                onDismissRequest = { editingExpenseId = null },
                sheetState = sheetState,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 700.dp)
                ) {
                                         AddExpenseScreen(
                         selectedDate = expenseToEdit.date,
                         defaultCurrency = viewModel.defaultCurrency,
                         dailyLimit = viewModel.dailyLimit,
                         monthlyLimit = viewModel.monthlyLimit,
                         isDarkTheme = isDarkTheme,
                         onExpenseAdded = { updatedExpense ->
                             // Update all expenses with the same recurrence group ID that are from today onwards
                             val today = java.time.LocalDate.now()
                             val expensesToUpdate = expenses.filter { 
                                 it.recurrenceGroupId == expenseToEdit.recurrenceGroupId &&
                                 it.date.toLocalDate().isAfter(today.minusDays(1)) // Today and future
                             }
                             expensesToUpdate.forEach { 
                                 val updatedExpenseWithSameId = updatedExpense.copy(
                                     id = it.id,
                                     date = it.date, // Keep original date for each occurrence
                                     recurrenceType = it.recurrenceType, // Keep original recurrence type
                                     recurrenceGroupId = it.recurrenceGroupId // Keep original group ID
                                 )
                                 viewModel.updateExpense(updatedExpenseWithSameId)
                             }
                             editingExpenseId = null
                         },
                         onDismiss = { editingExpenseId = null },
                         editingExpense = expenseToEdit
                     )
                }
            }
        }
    }
}

@Composable
fun RecurringExpenseCard(
    expense: Expense,
    isDarkTheme: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ThemeColors.getCardBackgroundColor(isDarkTheme)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with edit/delete buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = expense.recurrenceType.name.lowercase().replaceFirstChar { it.uppercase() },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = ThemeColors.getTextGrayColor(isDarkTheme),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Expense details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = expense.subCategory,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeColors.getTextColor(isDarkTheme)
                    )
                    
                    if (expense.description.isNotEmpty()) {
                        Text(
                            text = expense.description,
                            fontSize = 14.sp,
                            color = ThemeColors.getTextGrayColor(isDarkTheme)
                        )
                    }
                    
                    Text(
                        text = "Başlangıç: ${expense.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("tr")))}",
                        fontSize = 12.sp,
                        color = ThemeColors.getTextGrayColor(isDarkTheme)
                    )
                    
                    expense.endDate?.let { endDate ->
                        Text(
                            text = "Bitiş: ${endDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("tr")))}",
                            fontSize = 12.sp,
                            color = ThemeColors.getTextGrayColor(isDarkTheme)
                        )
                    }
                }
                
                Text(
                    text = "${expense.currency} ${String.format("%.2f", expense.amount)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
            }
        }
    }
}
