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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.ui.components.*
import com.example.expensetrackerkotlin.viewmodel.ExpenseViewModel
import com.example.expensetrackerkotlin.data.RecurrenceType
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    viewModel: ExpenseViewModel
) {
    val expenses by viewModel.expenses.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val showingOverLimitAlert by viewModel.showingOverLimitAlert.collectAsState()
    val editingExpenseId by viewModel.editingExpenseId.collectAsState()
    
    var showingAddExpense by remember { mutableStateOf(false) }
    var showingSettings by remember { mutableStateOf(false) }
    var showingMonthlyCalendar by remember { mutableStateOf(false) }
    var showingRecurringExpenses by remember { mutableStateOf(false) }
    var currentCalendarMonth by remember { mutableStateOf(java.time.YearMonth.from(selectedDate)) }
    
    val scope = rememberCoroutineScope()
    
    // Get selected date expenses (including recurring expenses)
    val selectedDateExpenses = remember(expenses, selectedDate) {
        expenses.filter { expense ->
            expense.isActiveOnDate(selectedDate)
        }
    }
    //TODO add a button to manage repeating expenses by changing from there all the future of that expensive will change
    
    val isDarkTheme = viewModel.theme == "dark"
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeColors.getBackgroundColor(isDarkTheme))
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Daily History
            DailyHistoryView(
                dailyData = viewModel.dailyHistoryData,
                selectedDate = selectedDate,
                isDarkTheme = isDarkTheme,
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
                                totalSpent = viewModel.getMonthlyTotal(currentCalendarMonth),
                                progressPercentage = viewModel.getMonthlyProgressPercentage(currentCalendarMonth),
                                isOverLimit = viewModel.isMonthlyOverLimit(currentCalendarMonth),
                                onTap = { showingMonthlyCalendar = true },
                                currency = viewModel.defaultCurrency,
                                isDarkTheme = isDarkTheme,
                                month = currentCalendarMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMM", java.util.Locale.forLanguageTag("tr")))
                            )
                        }
                        1 -> {
                                                    // Daily Progress Ring
                        DailyProgressRingView(
                            dailyProgressPercentage = viewModel.dailyProgressPercentage,
                            isOverDailyLimit = viewModel.isOverDailyLimit,
                            dailyLimitValue = viewModel.dailyLimit.toDoubleOrNull() ?: 0.0,
                            selectedDateTotal = viewModel.getSelectedDayTotal(),
                            currency = viewModel.defaultCurrency,
                            isDarkTheme = isDarkTheme
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
                        tint = ThemeColors.getTextGrayColor(isDarkTheme)
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
                        color = ThemeColors.getTextColor(isDarkTheme),
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
                    items(selectedDateExpenses) { expense ->
                        ExpenseRowView(
                            expense = expense,
                            onUpdate = { updatedExpense ->
                                // Now each expense has its own ID, so we can update directly
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
                                // Now each expense has its own ID, so we can delete directly
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
        
        // Floating Action Buttons (Bottom)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomEnd)
                .padding(horizontal = 20.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Recurring Expenses Button (Above Add Button)
            FloatingActionButton(
                onClick = { showingRecurringExpenses = true },
                containerColor = Color.Transparent,
                modifier = Modifier.size(50.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(AppColors.RecurringButtonStart, AppColors.RecurringButtonEnd)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Recurring Expenses",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(26.dp))
            
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
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
        
        // Settings Button (Bottom Left)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 40.dp)
        ) {
            FloatingActionButton(
                onClick = { showingSettings = true },
                containerColor = Color.Transparent,
                modifier = Modifier.size(50.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF101010), Color(0xFF101010))
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
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
    
    // Recurring Expenses Bottom Sheet
    if (showingRecurringExpenses) {
        val recurringExpensesSheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { true }
        )
        
        LaunchedEffect(Unit) {
            recurringExpensesSheetState.expand()
        }
        
        ModalBottomSheet(
            onDismissRequest = { showingRecurringExpenses = false },
            sheetState = recurringExpensesSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(700.dp)
            ) {
                RecurringExpensesScreen(
                    viewModel = viewModel,
                    onDismiss = { showingRecurringExpenses = false }
                )
            }
        }
    }
    
    // Bottom Sheets
    if (showingAddExpense) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { true }
        )
        
        LaunchedEffect(Unit) {
            sheetState.expand()
        }
        
        ModalBottomSheet(
            onDismissRequest = { showingAddExpense = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
            ) {
                AddExpenseScreen(
                    selectedDate = selectedDate,
                    defaultCurrency = viewModel.defaultCurrency,
                    dailyLimit = viewModel.dailyLimit,
                    monthlyLimit = viewModel.monthlyLimit,
                    isDarkTheme = isDarkTheme,
                    onExpenseAdded = { expense ->
                        viewModel.addExpense(expense)
                    },
                    onDismiss = { showingAddExpense = false },
                    viewModel = viewModel
                )
            }
        }
    }
    
    if (showingSettings) {
        val settingsSheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { true }
        )
        
        LaunchedEffect(Unit) {
            settingsSheetState.expand()
        }
        
        ModalBottomSheet(
            onDismissRequest = { showingSettings = false },
            sheetState = settingsSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 750.dp)
            ) {
                SettingsScreen(
                    viewModel = viewModel,
                    defaultCurrency = viewModel.defaultCurrency,
                    dailyLimit = viewModel.dailyLimit,
                    monthlyLimit = viewModel.monthlyLimit,
                    theme = viewModel.theme,
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
                    onThemeChanged = { theme ->
                        scope.launch {
                            viewModel.updateTheme(theme)
                        }
                    },
                    onDismiss = { showingSettings = false }
                )
            }
        }
    }
    
    // Monthly Calendar Bottom Sheet
    if (showingMonthlyCalendar) {
        val monthlyCalendarSheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { true }
        )
        
        LaunchedEffect(Unit) {
            monthlyCalendarSheetState.expand()
        }
        
        ModalBottomSheet(
            onDismissRequest = { showingMonthlyCalendar = false },
            sheetState = monthlyCalendarSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(700.dp)
            ) {
                MonthlyCalendarView(
                    selectedDate = selectedDate,
                    expenses = expenses,
                    onDateSelected = { date ->
                        viewModel.updateSelectedDate(date)
                        showingMonthlyCalendar = false
                    },
                    defaultCurrency = viewModel.defaultCurrency,
                    dailyLimit = viewModel.dailyLimit,
                    isDarkTheme = isDarkTheme,
                    onMonthChanged = { month ->
                        currentCalendarMonth = month
                    }
                )
            }
        }
    }
}
