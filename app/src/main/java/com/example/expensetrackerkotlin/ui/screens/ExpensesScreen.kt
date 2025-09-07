package com.example.expensetrackerkotlin.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
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
import kotlin.math.min

enum class ExpenseSortType {
    AMOUNT_HIGH_TO_LOW,
    AMOUNT_LOW_TO_HIGH, 
    DESCRIPTION_A_TO_Z,
    DESCRIPTION_Z_TO_A,
    CATEGORY_A_TO_Z,
    CATEGORY_Z_TO_A,
    TIME_NEWEST_FIRST,
    TIME_OLDEST_FIRST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    viewModel: ExpenseViewModel
) {
    val expenses by viewModel.expenses.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val showingOverLimitAlert by viewModel.showingOverLimitAlert.collectAsState()
    val editingExpenseId by viewModel.editingExpenseId.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()
    
    var showingAddExpense by remember { mutableStateOf(false) }
    var showingSettings by remember { mutableStateOf(false) }
    var showingMonthlyCalendar by remember { mutableStateOf(false) }
    var showingRecurringExpenses by remember { mutableStateOf(false) }
    var currentCalendarMonth by remember { mutableStateOf(java.time.YearMonth.from(selectedDate)) }
    
    // Search and sorting state
    var searchText by remember { mutableStateOf("") }
    var showSortMenu by remember { mutableStateOf(false) }
    var currentSortType by remember { mutableStateOf(ExpenseSortType.TIME_NEWEST_FIRST) }
    var showSearchBar by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    // Get selected date expenses (including recurring expenses)
    val baseSelectedDateExpenses = remember(expenses, selectedDate) {
        expenses.filter { expense ->
            expense.isActiveOnDate(selectedDate)
        }
    }
    
    // Filter and sort expenses
    val selectedDateExpenses = remember(baseSelectedDateExpenses, searchText, currentSortType, categories, subCategories) {
        var filteredExpenses = baseSelectedDateExpenses
        
        // Apply search filter
        if (searchText.isNotBlank()) {
            filteredExpenses = filteredExpenses.filter { expense ->
                val category = categories.find { it.id == expense.categoryId }
                val subCategory = subCategories.find { it.id == expense.subCategoryId }
                
                expense.description.contains(searchText, ignoreCase = true) ||
                expense.amount.toString().contains(searchText) ||
                (category?.name?.contains(searchText, ignoreCase = true) == true) ||
                (subCategory?.name?.contains(searchText, ignoreCase = true) == true)
            }
        }
        
        // Apply sorting
        when (currentSortType) {
            ExpenseSortType.AMOUNT_HIGH_TO_LOW -> filteredExpenses.sortedByDescending { it.getAmountInDefaultCurrency(viewModel.defaultCurrency) }
            ExpenseSortType.AMOUNT_LOW_TO_HIGH -> filteredExpenses.sortedBy { it.getAmountInDefaultCurrency(viewModel.defaultCurrency) }
            ExpenseSortType.DESCRIPTION_A_TO_Z -> filteredExpenses.sortedBy { it.description.lowercase() }
            ExpenseSortType.DESCRIPTION_Z_TO_A -> filteredExpenses.sortedByDescending { it.description.lowercase() }
            ExpenseSortType.CATEGORY_A_TO_Z -> filteredExpenses.sortedBy { expense ->
                val subCategory = subCategories.find { it.id == expense.subCategoryId }
                subCategory?.name?.lowercase() ?: "zzz"
            }
            ExpenseSortType.CATEGORY_Z_TO_A -> filteredExpenses.sortedByDescending { expense ->
                val subCategory = subCategories.find { it.id == expense.subCategoryId }
                subCategory?.name?.lowercase() ?: ""
            }
            ExpenseSortType.TIME_NEWEST_FIRST -> filteredExpenses.sortedByDescending { it.date }
            ExpenseSortType.TIME_OLDEST_FIRST -> filteredExpenses.sortedBy { it.date }
        }
    }
    
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
            
            // Search and Sort controls
            if (baseSelectedDateExpenses.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search toggle button
                    IconButton(
                        onClick = { 
                            showSearchBar = !showSearchBar 
                            if (!showSearchBar) {
                                searchText = ""
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (showSearchBar) AppColors.PrimaryOrange else ThemeColors.getTextColor(isDarkTheme)
                        )
                    }
                    
                    // Sort button
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort",
                                tint = ThemeColors.getTextColor(isDarkTheme)
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(ThemeColors.getCardBackgroundColor(isDarkTheme))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Zaman: Yeniden Eskiye", color = ThemeColors.getTextColor(isDarkTheme)) },
                                onClick = { 
                                    currentSortType = ExpenseSortType.TIME_NEWEST_FIRST
                                    showSortMenu = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Zaman: Eskiden Yeniye", color = ThemeColors.getTextColor(isDarkTheme)) },
                                onClick = { 
                                    currentSortType = ExpenseSortType.TIME_OLDEST_FIRST
                                    showSortMenu = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Miktar: Büyükten Küçüğe", color = ThemeColors.getTextColor(isDarkTheme)) },
                                onClick = { 
                                    currentSortType = ExpenseSortType.AMOUNT_HIGH_TO_LOW
                                    showSortMenu = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Miktar: Küçükten Büyüğe", color = ThemeColors.getTextColor(isDarkTheme)) },
                                onClick = { 
                                    currentSortType = ExpenseSortType.AMOUNT_LOW_TO_HIGH
                                    showSortMenu = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Açıklama: A-Z", color = ThemeColors.getTextColor(isDarkTheme)) },
                                onClick = { 
                                    currentSortType = ExpenseSortType.DESCRIPTION_A_TO_Z
                                    showSortMenu = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Açıklama: Z-A", color = ThemeColors.getTextColor(isDarkTheme)) },
                                onClick = { 
                                    currentSortType = ExpenseSortType.DESCRIPTION_Z_TO_A
                                    showSortMenu = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Kategori: A-Z", color = ThemeColors.getTextColor(isDarkTheme)) },
                                onClick = { 
                                    currentSortType = ExpenseSortType.CATEGORY_A_TO_Z
                                    showSortMenu = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Kategori: Z-A", color = ThemeColors.getTextColor(isDarkTheme)) },
                                onClick = { 
                                    currentSortType = ExpenseSortType.CATEGORY_Z_TO_A
                                    showSortMenu = false 
                                }
                            )
                        }
                    }
                    
                    // Search results count
                    if (searchText.isNotBlank()) {
                        Text(
                            text = "${selectedDateExpenses.size} sonuç",
                            fontSize = 12.sp,
                            color = ThemeColors.getTextGrayColor(isDarkTheme)
                        )
                    }
                }
                
                // Search bar
                AnimatedVisibility(
                    visible = showSearchBar,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .background(
                                ThemeColors.getInputBackgroundColor(isDarkTheme),
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = ThemeColors.getTextGrayColor(isDarkTheme).copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        BasicTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 14.sp,
                                color = ThemeColors.getTextColor(isDarkTheme)
                            ),
                            decorationBox = { innerTextField ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = ThemeColors.getTextGrayColor(isDarkTheme),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (searchText.isEmpty()) {
                                            Text(
                                                text = "Açıklama, miktar veya kategoriye göre ara...",
                                                fontSize = 14.sp,
                                                color = ThemeColors.getTextGrayColor(isDarkTheme)
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            }
                        )
                    }
                }
            }
            
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
                        text = if (searchText.isNotBlank()) {
                            "Arama sonucu bulunamadı"
                        } else if (selectedDate.toLocalDate() == LocalDateTime.now().toLocalDate()) {
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
                        text = if (searchText.isNotBlank()) {
                            "\"$searchText\" için sonuç bulunamadı. Farklı bir arama terimi deneyin."
                        } else if (selectedDate.toLocalDate() == LocalDateTime.now().toLocalDate()) {
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
        
        // Floating Action Buttons (Over Charts)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp), // Position higher up to avoid expense list overlap
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Settings Button (Left)
            FloatingActionButton(
                onClick = { showingSettings = true },
                containerColor = Color.Transparent,
                modifier = Modifier.size(60.dp)
                    .offset(y = 175.dp)
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
            
            // Right side buttons (vertical stack)
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Recurring Expenses Button (Top)
                FloatingActionButton(
                    onClick = { showingRecurringExpenses = true },
                    containerColor = Color.Transparent,
                    modifier = Modifier.size(60.dp)
                        .offset(y = 140.dp)
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
                
                // Add Expense Button (Bottom)
                FloatingActionButton(
                    onClick = { showingAddExpense = true },
                    containerColor = Color.Transparent,
                    modifier = Modifier.size(60.dp)
                        .offset(y = 140.dp)
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
