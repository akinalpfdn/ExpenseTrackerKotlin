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
import androidx.compose.ui.res.stringResource
import com.example.expensetrackerkotlin.R
import com.example.expensetrackerkotlin.ui.components.*
import com.example.expensetrackerkotlin.ui.components.PurchaseBottomSheet
import com.example.expensetrackerkotlin.billing.BillingManager
import com.example.expensetrackerkotlin.viewmodel.ExpenseViewModel
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.expensetrackerkotlin.utils.NumberFormatter
import java.util.Locale

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
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    val expenses by viewModel.expenses.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val showingOverLimitAlert by viewModel.showingOverLimitAlert.collectAsState()
    val editingExpenseId by viewModel.editingExpenseId.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()
    val weeklyHistoryData by viewModel.weeklyHistoryData.collectAsState()

    var showingAddExpense by remember { mutableStateOf(false) }
    var showingSettings by remember { mutableStateOf(false) }
    var showingMonthlyCalendar by remember { mutableStateOf(false) }
    var showingRecurringExpenses by remember { mutableStateOf(false) }
    var showingPurchase by remember { mutableStateOf(false) }
    var currentCalendarMonth by remember { mutableStateOf(java.time.YearMonth.from(selectedDate)) }

    // Daily category detail bottom sheet state
    var showingDailyCategoryDetail by remember { mutableStateOf(false) }
    var selectedCategoryForDetail by remember { mutableStateOf<com.example.expensetrackerkotlin.data.Category?>(null) }

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
    // Billing Manager
    val billingManager = remember { BillingManager(context) }
    val purchaseState by billingManager.purchaseState.collectAsState()

    // Initialize billing client
    DisposableEffect(Unit) {
        billingManager.initialize()
        onDispose {
            billingManager.destroy()
        }
    }

    // Purchase result state for showing messages
    var showPurchaseMessage by remember { mutableStateOf<String?>(null) }

    // Handle purchase state
    LaunchedEffect(purchaseState) {
        when (val state = purchaseState) {
            is BillingManager.PurchaseState.Success -> {
                showPurchaseMessage = "🎉 Teşekkürler! Satın alma başarılı: ${state.productId}"
                billingManager.resetPurchaseState()
            }
            is BillingManager.PurchaseState.Error -> {
                showPurchaseMessage = "❌ Hata: ${state.message}"
                billingManager.resetPurchaseState()
            }
            is BillingManager.PurchaseState.Cancelled -> {
                showPurchaseMessage = "❌ Satın alma iptal edildi"
                billingManager.resetPurchaseState()
            }
            is BillingManager.PurchaseState.Loading -> {
                showPurchaseMessage = "⏳ Satın alma işlemi başlatılıyor..."
            }
            else -> { /* Do nothing */ }
        }
    }

    // Auto-hide message after 3 seconds
    LaunchedEffect(showPurchaseMessage) {
        if (showPurchaseMessage != null) {
            kotlinx.coroutines.delay(3000)
            showPurchaseMessage = null
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
                weeklyData = weeklyHistoryData,
                selectedDate = selectedDate,
                isDarkTheme = isDarkTheme,
                onDateSelected = { date ->
                    viewModel.updateSelectedDate(date)
                },
                onWeekNavigate = { direction ->
                    viewModel.navigateToWeek(direction)
                }
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Charts TabView - Horizontal Pager like Swift
            val pagerState = rememberPagerState(pageCount = { 3 })
            
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
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
                                month = currentCalendarMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMMM", Locale.getDefault())),

                                selectedDate = viewModel.selectedDate
                            )
                        }
                        1 -> {
                                                    // Daily Progress Ring
                        DailyProgressRingView(
                            dailyProgressPercentage = viewModel.dailyProgressPercentage,
                            isOverDailyLimit = viewModel.isOverDailyLimit,
                            selectedDateTotal = viewModel.getSelectedDayTotal(),
                            currency = viewModel.defaultCurrency,
                            isDarkTheme = isDarkTheme
                        )
                        }
                        2 -> {
                            // Category Distribution
                            CategoryDistributionChart(
                                categoryExpenses = viewModel.dailyExpensesByCategory,
                                onCategoryClick = { category ->
                                    selectedCategoryForDetail = category
                                    showingDailyCategoryDetail = true
                                },
                                isDarkTheme = isDarkTheme
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
                            contentDescription = stringResource(R.string.search),
                            tint = if (showSearchBar) AppColors.PrimaryOrange else ThemeColors.getTextColor(isDarkTheme)
                        )
                    }
                    
                    // Sort button
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = stringResource(R.string.sort),
                                tint = ThemeColors.getTextColor(isDarkTheme)
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(ThemeColors.getCardBackgroundColor(isDarkTheme))
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.time_newest_first), color = ThemeColors.getTextColor(isDarkTheme)) },
                                onClick = { 
                                    currentSortType = ExpenseSortType.TIME_NEWEST_FIRST
                                    showSortMenu = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.time_oldest_first), color = ThemeColors.getTextColor(isDarkTheme)) },
                                onClick = { 
                                    currentSortType = ExpenseSortType.TIME_OLDEST_FIRST
                                    showSortMenu = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.amount_high_to_low), color = ThemeColors.getTextColor(isDarkTheme)) },
                                onClick = { 
                                    currentSortType = ExpenseSortType.AMOUNT_HIGH_TO_LOW
                                    showSortMenu = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.amount_low_to_high), color = ThemeColors.getTextColor(isDarkTheme)) },
                                onClick = { 
                                    currentSortType = ExpenseSortType.AMOUNT_LOW_TO_HIGH
                                    showSortMenu = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.description_a_to_z), color = ThemeColors.getTextColor(isDarkTheme)) },
                                onClick = { 
                                    currentSortType = ExpenseSortType.DESCRIPTION_A_TO_Z
                                    showSortMenu = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.description_z_to_a), color = ThemeColors.getTextColor(isDarkTheme)) },
                                onClick = { 
                                    currentSortType = ExpenseSortType.DESCRIPTION_Z_TO_A
                                    showSortMenu = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.category_a_to_z), color = ThemeColors.getTextColor(isDarkTheme)) },
                                onClick = { 
                                    currentSortType = ExpenseSortType.CATEGORY_A_TO_Z
                                    showSortMenu = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.category_z_to_a), color = ThemeColors.getTextColor(isDarkTheme)) },
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
                            text = stringResource(R.string.results_count, selectedDateExpenses.size),
                            fontSize = 12.sp,
                            color = ThemeColors.getTextGrayColor(isDarkTheme)
                        )
                        Text(
                            text = "${viewModel.defaultCurrency} ${NumberFormatter.formatAmount(( selectedDateExpenses.sumOf { it.amount }))}" ,
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
                                                text = stringResource(R.string.search_expenses_placeholder),
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

                    
                    Text(
                        text = if (searchText.isNotBlank()) {
                            stringResource(R.string.no_search_results_found)
                        } else if (selectedDate.toLocalDate() == LocalDateTime.now().toLocalDate()) {
                            stringResource(R.string.no_expenses_yet)
                        } else {
                            stringResource(R.string.no_expenses_today)
                        },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = ThemeColors.getTextColor(isDarkTheme),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (searchText.isNotBlank()) {
                            stringResource(R.string.no_search_results_description, searchText)
                        } else if (selectedDate.toLocalDate() == LocalDateTime.now().toLocalDate()) {
                            stringResource(R.string.first_expense_hint)
                        } else {
                            stringResource(R.string.add_expense_for_day_hint)
                        },
                        fontSize = 18.sp,
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
            // Left side buttons (vertical stack)
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Purchase/Donation Button (Top)
                FloatingActionButton(
                    onClick = { showingPurchase = true },
                    elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                    containerColor = Color.Transparent,
                    modifier = Modifier.size(60.dp)
                        .offset(y = 140.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                ThemeColors.getCardBackgroundColor(isDarkTheme)
                                ,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Support",
                        tint = ThemeColors.getTextColor(isDarkTheme),
                        modifier = Modifier.size(30.dp)
                    )
                }
                }

                // Settings Button (Bottom)
                FloatingActionButton(
                    onClick = { showingSettings = true },
                    elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                    containerColor = Color.Transparent,
                    modifier = Modifier.size(60.dp)
                        .offset(y = 140.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                    ThemeColors.getCardBackgroundColor(isDarkTheme)
                                 ,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = ThemeColors.getTextColor(isDarkTheme),
                            modifier = Modifier.size(30.dp)
                        )
                    }
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
                    elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
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
                    elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
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
                        text = stringResource(R.string.monthly_limit_exceeded),
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

        // Purchase Message Alert - Full screen overlay
        if (showPurchaseMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showPurchaseMessage = null },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clickable { }, // Prevent click through
                    colors = CardDefaults.cardColors(
                        containerColor = if (showPurchaseMessage!!.contains("🎉"))
                            Color(0xFF4CAF50) else AppColors.PrimaryOrange
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = showPurchaseMessage!!,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showPurchaseMessage = null },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            )
                        ) {
                            Text("OK", color = Color.White)
                        }
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
            containerColor = ThemeColors.getBackgroundColor(isDarkTheme),
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
            containerColor = ThemeColors.getBackgroundColor(isDarkTheme),
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
            containerColor = ThemeColors.getBackgroundColor(isDarkTheme),
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
        
        var selectedTabIndex by remember { mutableStateOf(0) }
        val tabs = listOf(stringResource(R.string.calendar_tab), stringResource(R.string.expenses_tab))
        
        LaunchedEffect(Unit) {
            monthlyCalendarSheetState.expand()
        }
        
        ModalBottomSheet(
            onDismissRequest = { showingMonthlyCalendar = false },
            sheetState = monthlyCalendarSheetState,
            containerColor = ThemeColors.getBackgroundColor(isDarkTheme),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(700.dp)
            ) {
                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.padding(horizontal = 10.dp),
                    containerColor = Color.Transparent,

                    contentColor = AppColors.PrimaryOrange,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            height = 2.dp,
                            color = AppColors.PrimaryOrange
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 22.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) AppColors.PrimaryOrange else ThemeColors.getTextGrayColor(isDarkTheme)
                                )
                            },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Tab Content
                when (selectedTabIndex) {
                    0 -> {
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
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    1 -> {
                        MonthlyExpensesView(
                            currentMonth = currentCalendarMonth,
                            expenses = expenses,
                            viewModel = viewModel,
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
    
    // Daily Category Detail Bottom Sheet
    if (showingDailyCategoryDetail && selectedCategoryForDetail != null) {
        DailyCategoryDetailBottomSheet(
            category = selectedCategoryForDetail!!,
            selectedDateExpenses = selectedDateExpenses,
            subCategories = subCategories,
            selectedDate = selectedDate,
            defaultCurrency = viewModel.defaultCurrency,
            isDarkTheme = isDarkTheme,
            onDismiss = {
                showingDailyCategoryDetail = false
                selectedCategoryForDetail = null
            }
        )
    }

    // Purchase Bottom Sheet
    if (showingPurchase) {
        PurchaseBottomSheet(
            isDarkTheme = isDarkTheme,
            onDismiss = { showingPurchase = false },
            onPurchase = { productId ->
                activity?.let { act ->
                    billingManager.launchPurchaseFlow(act, productId)
                }
            }
        )
    }
}
