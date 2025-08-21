package com.example.expensetrackerkotlin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.ui.components.*
import com.example.expensetrackerkotlin.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ExpenseViewModel
) {
    val expenses by viewModel.expenses.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val showingOverLimitAlert by viewModel.showingOverLimitAlert.collectAsState()
    val editingExpenseId by viewModel.editingExpenseId.collectAsState()
    
    var showingAddExpense by remember { mutableStateOf(false) }
    var showingSettings by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    // Get selected date expenses
    val selectedDateExpenses = remember(expenses, selectedDate) {
        expenses.filter { expense ->
            expense.date.toLocalDate() == selectedDate.toLocalDate()
        }
    }
    
    val selectedDayTotal = selectedDateExpenses.sumOf { it.amount }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Daily History
            DailyHistoryView(
                dailyData = viewModel.dailyHistoryData,
                selectedDate = selectedDate,
                onDateSelected = { date ->
                    viewModel.updateSelectedDate(date)
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Charts TabView - Horizontal Pager like Swift
            val pagerState = rememberPagerState(pageCount = { 3 })
            
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(horizontal = 20.dp)
            ) { page ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (page) {
                        0 -> {
                            // Monthly Progress Ring
                            MonthlyProgressRingView(
                                totalSpent = viewModel.totalSpent,
                                progressPercentage = viewModel.progressPercentage,
                                progressColors = viewModel.progressColors,
                                isOverLimit = viewModel.isOverLimit,
                                onTap = { /* TODO: Implement monthly calendar */ }
                            )
                        }
                        1 -> {
                            // Daily Progress Ring
                            DailyProgressRingView(
                                dailyProgressPercentage = viewModel.dailyProgressPercentage,
                                isOverDailyLimit = viewModel.isOverDailyLimit,
                                dailyLimitValue = viewModel.dailyLimit.toDoubleOrNull() ?: 0.0,
                                selectedDateTotal = selectedDayTotal
                            )
                        }
                        2 -> {
                            // Category Distribution
                            CategoryDistributionChart(
                                categoryExpenses = viewModel.dailyExpensesByCategory
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Expenses List
            if (selectedDateExpenses.isEmpty()) {
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
                        tint = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (selectedDate.toLocalDate() == LocalDateTime.now().toLocalDate()) {
                            "Henüz harcama yok"
                        } else {
                            "Bu günde harcama yok"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (selectedDate.toLocalDate() == LocalDateTime.now().toLocalDate()) {
                            "İlk harcamanızı eklemek için + butonuna basın"
                        } else {
                            "Bu güne harcama eklemek için + butonuna basın"
                        },
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(selectedDateExpenses) { expense ->
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
                            dailyExpenseRatio = viewModel.getDailyExpenseRatio(expense)
                        )
                    }
                }
            }
        }
        
        // Floating Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Settings Button
            FloatingActionButton(
                onClick = { showingSettings = true },
                containerColor = Color.White.copy(alpha = 0.3f),
                contentColor = Color.White,
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
            
            // Add Expense Button
            FloatingActionButton(
                onClick = { showingAddExpense = true },
                containerColor = Color.Transparent,
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFF9500), Color(0xFFFF3B30))
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Expense",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        // Over Limit Alert
        if (showingOverLimitAlert) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .align(Alignment.TopCenter),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Red
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚠️ Aylık harcama limitinizi aştınız!",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    TextButton(
                        onClick = { viewModel.dismissOverLimitAlert() }
                    ) {
                        Text("✕", color = Color.White)
                    }
                }
            }
        }
    }
    
    // Bottom Sheets
    if (showingAddExpense) {
        ModalBottomSheet(
            onDismissRequest = { showingAddExpense = false }
        ) {
            AddExpenseScreen(
                selectedDate = selectedDate,
                defaultCurrency = viewModel.defaultCurrency,
                dailyLimit = viewModel.dailyLimit,
                monthlyLimit = viewModel.monthlyLimit,
                onExpenseAdded = { expense ->
                    viewModel.addExpense(expense)
                },
                onDismiss = { showingAddExpense = false }
            )
        }
    }
    
    if (showingSettings) {
        ModalBottomSheet(
            onDismissRequest = { showingSettings = false }
        ) {
            SettingsScreen(
                defaultCurrency = viewModel.defaultCurrency,
                dailyLimit = viewModel.dailyLimit,
                monthlyLimit = viewModel.monthlyLimit,
                onCurrencyChanged = { currency ->
                    scope.launch {
                        viewModel.updateDefaultCurrency(currency)
                    }
                },
                onDailyLimitChanged = { limit ->
                    scope.launch {
                        viewModel.updateDailyLimit(limit)
                    }
                },
                onMonthlyLimitChanged = { limit ->
                    scope.launch {
                        viewModel.updateMonthlyLimit(limit)
                    }
                },
                onDismiss = { showingSettings = false }
            )
        }
    }
}