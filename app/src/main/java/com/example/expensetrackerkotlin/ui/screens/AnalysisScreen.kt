package com.example.expensetrackerkotlin.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlin.math.*
import com.example.expensetrackerkotlin.data.*
import com.example.expensetrackerkotlin.ui.components.*
import com.example.expensetrackerkotlin.ui.components.ChartDataPoint
import com.example.expensetrackerkotlin.ui.components.CategoryAnalysisData
import com.example.expensetrackerkotlin.ui.components.SortOption
import com.example.expensetrackerkotlin.ui.components.ExpenseFilterType
import com.example.expensetrackerkotlin.ui.theme.*
import com.example.expensetrackerkotlin.utils.NumberFormatter
import com.example.expensetrackerkotlin.viewmodel.ExpenseViewModel
import java.time.*
import java.time.format.DateTimeFormatter


fun getMonthlyChartData(viewModel: ExpenseViewModel, selectedMonth: YearMonth, filterType: ExpenseFilterType = ExpenseFilterType.ALL): List<ChartDataPoint> {
    val expenses = viewModel.expenses.value
    val startOfMonth = selectedMonth.atDay(1).atStartOfDay()
    val endOfMonth = selectedMonth.atEndOfMonth().atTime(23, 59, 59)
    
    val monthlyExpenses = expenses.filter { expense ->
        expense.date.toLocalDate().let { expenseDate ->
            !expenseDate.isBefore(startOfMonth.toLocalDate()) && 
            !expenseDate.isAfter(endOfMonth.toLocalDate())
        }
    }
    
    // Apply filter based on expense type
    val filteredExpenses = when (filterType) {
        ExpenseFilterType.ALL -> monthlyExpenses
        ExpenseFilterType.RECURRING -> monthlyExpenses.filter { it.recurrenceType != RecurrenceType.NONE }
        ExpenseFilterType.ONE_TIME -> monthlyExpenses.filter { it.recurrenceType == RecurrenceType.NONE }
    }
    
    // Group expenses by day and sum amounts
    val dailyExpenses = filteredExpenses.groupBy { expense ->
        expense.date.toLocalDate().dayOfMonth
    }.map { (day, dayExpenses) ->
        ChartDataPoint(
            day = day,
            amount = dayExpenses.sumOf { it.getAmountInDefaultCurrency(viewModel.defaultCurrency) }
        )
    }
    
    // Create a complete list for all days in the month, filling missing days with 0
    val daysInMonth = selectedMonth.lengthOfMonth()
    return (1..daysInMonth).map { day ->
        dailyExpenses.find { it.day == day } ?: ChartDataPoint(day = day, amount = 0.0)
    }
}

fun getFilteredCategoryAnalysisData(
    monthlyExpenses: List<Expense>,
    categories: List<Category>,
    defaultCurrency: String,
    filterType: ExpenseFilterType = ExpenseFilterType.ALL
): List<CategoryAnalysisData> {
    if (monthlyExpenses.isEmpty()) return emptyList()

    // Apply filter based on expense type
    val filteredExpenses = when (filterType) {
        ExpenseFilterType.ALL -> monthlyExpenses
        ExpenseFilterType.RECURRING -> monthlyExpenses.filter { it.recurrenceType != RecurrenceType.NONE }
        ExpenseFilterType.ONE_TIME -> monthlyExpenses.filter { it.recurrenceType == RecurrenceType.NONE }
    }

    val totalAmount = filteredExpenses.sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
    
    val categoryTotals = filteredExpenses.groupBy { it.categoryId }
        .mapNotNull { (categoryId, categoryExpenses) ->
            val category = categories.find { it.id == categoryId }
            if (category != null) {
                val amount = categoryExpenses.sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
                CategoryAnalysisData(
                    category = category,
                    totalAmount = amount,
                    expenseCount = categoryExpenses.size,
                    percentage = if (totalAmount > 0) amount / totalAmount else 0.0,
                    expenses = categoryExpenses.sortedByDescending { it.date }
                )
            } else null
        }
        .sortedByDescending { it.totalAmount }

    return categoryTotals
}

data class CategoryComparison(
    val vsLastMonth: Double,
    val vsAverage: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true
) {
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<CategoryAnalysisData?>(null) }
    var sortOption by remember { mutableStateOf(SortOption.DATE_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedSegment by remember { mutableStateOf<Int?>(null) }
    var selectedExpenseFilter by remember { mutableStateOf(ExpenseFilterType.ALL) }
    var selectedMonthlyExpenseType by remember { mutableStateOf(ExpenseFilterType.ALL) }

    val expenses by viewModel.expenses.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()

    val monthlyExpenses = remember(expenses, selectedMonth) {
        val startOfMonth = selectedMonth.atDay(1).atStartOfDay()
        val endOfMonth = selectedMonth.atEndOfMonth().atTime(23, 59, 59)

        expenses.filter { expense ->
            expense.date.toLocalDate().let { expenseDate ->
                !expenseDate.isBefore(startOfMonth.toLocalDate()) && 
                !expenseDate.isAfter(endOfMonth.toLocalDate())
            }
        }
    }

    val categoryAnalysisData = remember(monthlyExpenses, categories, subCategories, selectedMonthlyExpenseType) {
        getFilteredCategoryAnalysisData(
            monthlyExpenses = monthlyExpenses,
            categories = categories,
            defaultCurrency = viewModel.defaultCurrency,
            filterType = selectedMonthlyExpenseType
        )
    }

    val recurringExpenseTotal = remember(expenses) {

        val threeMonthsFromNow = selectedMonth.atEndOfMonth().plusMonths(3).atStartOfDay().plusDays(1)
        expenses.filter { expense ->
            expense.recurrenceType != RecurrenceType.NONE && 
            (expense.endDate == null || expense.endDate.isAfter(threeMonthsFromNow))
                    && expense.date<selectedMonth.atEndOfMonth().atStartOfDay().plusDays(1)
                    &&  selectedMonth.atEndOfMonth().plusMonths(-1).atStartOfDay().plusDays(1)<=expense.date
        }.sumOf { it.getAmountInDefaultCurrency(viewModel.defaultCurrency) }
    }

    val totalMonthlyAmount = categoryAnalysisData.sumOf { it.totalAmount }

    val animatedPercentages = categoryAnalysisData.map { data ->
        val animatedValue = remember { Animatable(0f) }
        LaunchedEffect(data.percentage) {
            animatedValue.animateTo(
                targetValue = data.percentage.toFloat(),
                animationSpec = tween(durationMillis = 1000, easing = EaseInOutCubic)
            )
        }
        animatedValue.value
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ThemeColors.getBackgroundColor(isDarkTheme))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MonthYearSelector(
            selectedMonth = selectedMonth,
            onMonthYearChanged = { selectedMonth = it },
            isDarkTheme = isDarkTheme
        )

        if (categoryAnalysisData.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(selectedSegment) {
                        // Close popup when clicking outside
                        detectTapGestures { tapOffset ->
                            if (selectedSegment != null) {
                                // Check if tap is outside the popup area
                                val popupLeft = (size.width - 280.dp.toPx()) / 2f
                                val popupRight = popupLeft + 280.dp.toPx()
                                val popupTop = 360.dp.toPx()
                                val popupBottom = popupTop + 100.dp.toPx()

                                val isOutsidePopup = tapOffset.x < popupLeft ||
                                        tapOffset.x > popupRight ||
                                        tapOffset.y < popupTop ||
                                        tapOffset.y > popupBottom

                                // Also check if tap is outside pie chart area
                                val pieChartCenterX = size.width / 2f
                                val pieChartCenterY = 200.dp.toPx() // Approximate pie chart center
                                val pieRadius = 125.dp.toPx()
                                val distanceFromPieCenter = sqrt(
                                    (tapOffset.x - pieChartCenterX).pow(2) +
                                            (tapOffset.y - pieChartCenterY).pow(2)
                                )
                                val isOutsidePieChart = distanceFromPieCenter > pieRadius

                                if (isOutsidePopup && isOutsidePieChart) {
                                    selectedSegment = null
                                }
                            }
                        }
                    }
            ) {
                val scrollState = rememberLazyListState()
                
                LazyColumn(
                    state = scrollState,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    )
                    {
                        ExpenseFilterType.entries.forEach { filterType ->
                            Row(
                                modifier = Modifier
                                    .selectable(
                                        selected = selectedMonthlyExpenseType == filterType,
                                        onClick = { selectedMonthlyExpenseType = filterType }
                                    )
                                    .padding(horizontal = 1.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedMonthlyExpenseType == filterType,
                                    onClick = { selectedMonthlyExpenseType = filterType },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = AppColors.PrimaryOrange,
                                        unselectedColor = ThemeColors.getTextGrayColor(isDarkTheme)
                                    )
                                )
                                Spacer(modifier = Modifier.width(1.dp))
                                Text(
                                    text = filterType.displayName,
                                    fontSize = 12.sp,
                                    color = if (selectedMonthlyExpenseType == filterType)
                                        ThemeColors.getTextColor(isDarkTheme)
                                    else ThemeColors.getTextGrayColor(isDarkTheme)
                                )
                            }
                        }
                    }
                }

                    item {
                        val totalComparison = remember(totalMonthlyAmount, selectedMonth, selectedMonthlyExpenseType) {
                            calculateTotalComparison(viewModel, selectedMonth, selectedMonthlyExpenseType)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bu Ayki Toplam Harcama",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ThemeColors.getTextColor(isDarkTheme),
                            )
                            Text(
                                text = "${viewModel.defaultCurrency} ${NumberFormatter.formatAmount(totalMonthlyAmount)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryOrange
                            )
                        }
                        TotalComparisonIndicator(
                            percentage = totalComparison.vsLastMonth,
                            label = "Önceki aya göre",
                            isDarkTheme = isDarkTheme
                        )
                        TotalComparisonIndicator(
                            percentage = totalComparison.vsAverage,
                            label = "6 ay ortalamasına göre",
                            isDarkTheme = isDarkTheme
                        )
                    }

                    item {
                        MonthlyAnalysisPieChart(
                            categoryData = categoryAnalysisData,
                            animatedPercentages = animatedPercentages,
                            isDarkTheme = isDarkTheme,
                            selectedSegment = selectedSegment,
                            onSegmentSelected = { selectedSegment = it },
                        )
                    }
                    item {
                        MonthlyLineChart(
                            data = getMonthlyChartData(viewModel, selectedMonth, selectedMonthlyExpenseType),
                            currency = viewModel.defaultCurrency,
                            isDarkTheme = isDarkTheme
                        )
                    }
                    if (recurringExpenseTotal > 0) {
                        item {
                            RecurringExpenseCard(
                                totalAmount = recurringExpenseTotal,
                                defaultCurrency = viewModel.defaultCurrency,
                                isDarkTheme = isDarkTheme
                            )
                        }
                    }
                    item {
                        CategorySummarySection(
                            categoryData = categoryAnalysisData,
                            totalAmount = totalMonthlyAmount,
                            defaultCurrency = viewModel.defaultCurrency,
                            isDarkTheme = isDarkTheme,
                            onCategoryClick = { categoryData ->
                                selectedCategory = categoryData
                                showCategoryDialog = true
                            }
                        )
                    }


                }
                
                // Overlay popup that appears above everything
                if (selectedSegment != null && selectedSegment!! < categoryAnalysisData.size) {
                    val selected = categoryAnalysisData[selectedSegment!!]
                    
                    // Get pie chart item position
                    val pieChartItemIndex = 1
                    val pieChartItemInfo = scrollState.layoutInfo.visibleItemsInfo
                        .find { it.index == pieChartItemIndex }
                    
                    // Only show popup if pie chart is visible
                    if (pieChartItemInfo != null) {
                        // Line and Popup Animation
                        val line1Progress = remember { Animatable(0f) }
                        val line2Progress = remember { Animatable(0f) }
                        val popupScale = remember { Animatable(0f) }
                        
                        LaunchedEffect(selectedSegment) {
                            if (selectedSegment != null) {
                                line1Progress.snapTo(0f)
                                line2Progress.snapTo(0f)
                                popupScale.snapTo(0f)
                                
                                line1Progress.animateTo(1f, animationSpec = tween(400, easing = EaseOutCubic))
                                line2Progress.animateTo(1f, animationSpec = tween(300, easing = EaseOutCubic))
                                popupScale.animateTo(1f, animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ))
                            } else {
                                popupScale.animateTo(0f, animationSpec = tween(150))
                                line2Progress.animateTo(0f, animationSpec = tween(200, easing = EaseInCubic))
                                line1Progress.animateTo(0f, animationSpec = tween(200, easing = EaseInCubic))
                            }
                        }
                        
                        // Fixed position relative to pie chart center
                        val pieChartTop = pieChartItemInfo.offset
                        val pieChartHeight = pieChartItemInfo.size
                        val popupY = pieChartTop + pieChartHeight + 800// 20dp below pie chart
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = with(LocalDensity.current) { popupY.toDp() })
                                .padding(horizontal = 16.dp)
                                .zIndex(10f)
                        ) {
                            Box(
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                val segmentIndex = selectedSegment!!
                                CategoryPopupLines(
                                    line1Progress,
                                    line2Progress,
                                    segmentIndex,
                                    animatedPercentages,
                                    selected
                                )
                                CategoryPopupCard(
                                    popupScale,
                                    selected,
                                    viewModel,
                                    selectedMonth,
                                    selectedMonthlyExpenseType,
                                    onCategoryClick = { categoryData ->
                                        selectedCategory = categoryData
                                        showCategoryDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "📊",
                        fontSize = 64.sp
                    )
                    Text(
                        text = "Bu ay henüz harcama yok",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = ThemeColors.getTextColor(isDarkTheme),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Harcama eklediğinizde analiz burada görünecek",
                        fontSize = 14.sp,
                        color = ThemeColors.getTextGrayColor(isDarkTheme),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (showCategoryDialog && selectedCategory != null) {
        val categoryDetailSheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { true }
        )
        
        LaunchedEffect(Unit) {
            categoryDetailSheetState.expand()
        }
        
        ModalBottomSheet(
            onDismissRequest = {
                showCategoryDialog = false
                selectedCategory = null
                showSortMenu = false
            },
            sheetState = categoryDetailSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 700.dp)
            ) {
                CategoryDetailBottomSheet(
                    categoryData = selectedCategory!!,
                    subCategories = subCategories,
                    defaultCurrency = viewModel.defaultCurrency,
                    isDarkTheme = isDarkTheme,
                    sortOption = sortOption,
                    showSortMenu = showSortMenu,
                    onSortOptionChanged = { sortOption = it },
                    onShowSortMenuChanged = { showSortMenu = it },
                    viewModel = viewModel,
                    selectedMonth = selectedMonth,
                    selectedFilterType = selectedMonthlyExpenseType
                )
            }
        }
    }
}

@Composable
fun MonthYearSelector(
    selectedMonth: YearMonth,
    onMonthYearChanged: (YearMonth) -> Unit,
    isDarkTheme: Boolean
) {
    val currentMonth = YearMonth.now()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                val previousMonth = selectedMonth.minusMonths(1)
                onMonthYearChanged(previousMonth)
            }
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Önceki ay",
                tint = ThemeColors.getTextColor(isDarkTheme)
            )
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { },
            colors = CardDefaults.cardColors(
                containerColor = ThemeColors.getCardBackgroundColor(isDarkTheme)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
                color = ThemeColors.getTextColor(isDarkTheme),
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp
            )
        }

        IconButton(
            onClick = {
                val nextMonth = selectedMonth.plusMonths(1)
                if (!nextMonth.isAfter(currentMonth)) {
                    onMonthYearChanged(nextMonth)
                }
            },
            enabled = !selectedMonth.plusMonths(1).isAfter(currentMonth)
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Sonraki ay",
                tint = if (!selectedMonth.plusMonths(1).isAfter(currentMonth)) 
                    ThemeColors.getTextColor(isDarkTheme) 
                else ThemeColors.getTextGrayColor(isDarkTheme)
            )
        }
    }
}




@Composable
fun RecurringExpenseCard(
    totalAmount: Double,
    defaultCurrency: String,
    isDarkTheme: Boolean,
) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Tekrarlayan harcamalar",
                    tint = AppColors.PrimaryOrange,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Sabit Harcamalar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ThemeColors.getTextColor(isDarkTheme)
                    )
                    Text(
                        text = "3+ ay devam edecek olan harcamalar",
                        fontSize = 12.sp,
                        color = ThemeColors.getTextGrayColor(isDarkTheme)
                    )
                }
            }
            
            Text(
                text = "$defaultCurrency ${NumberFormatter.formatAmount(totalAmount)}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = AppColors.PrimaryOrange
            )
        }

}

fun calculateCategoryComparison(
    viewModel: ExpenseViewModel,
    currentMonth: YearMonth,
    categoryId: String,
    filterType: ExpenseFilterType = ExpenseFilterType.ALL
): CategoryComparison {
    val expenses = viewModel.expenses.value
    val defaultCurrency = viewModel.defaultCurrency
    
    // Current month amount
    val currentMonthExpenses = expenses.filter { expense ->
        val expenseDate = expense.date.toLocalDate()
        expenseDate.year == currentMonth.year && expenseDate.month == currentMonth.month &&
        expense.categoryId == categoryId
    }.let { expenseList ->
        when (filterType) {
            ExpenseFilterType.ALL -> expenseList
            ExpenseFilterType.RECURRING -> expenseList.filter { it.recurrenceType != RecurrenceType.NONE }
            ExpenseFilterType.ONE_TIME -> expenseList.filter { it.recurrenceType == RecurrenceType.NONE }
        }
    }
    val currentAmount = currentMonthExpenses.sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
    
    // Previous month amount
    val previousMonth = currentMonth.minusMonths(1)
    val previousMonthExpenses = expenses.filter { expense ->
        val expenseDate = expense.date.toLocalDate()
        expenseDate.year == previousMonth.year && expenseDate.month == previousMonth.month &&
        expense.categoryId == categoryId
    }.let { expenseList ->
        when (filterType) {
            ExpenseFilterType.ALL -> expenseList
            ExpenseFilterType.RECURRING -> expenseList.filter { it.recurrenceType != RecurrenceType.NONE }
            ExpenseFilterType.ONE_TIME -> expenseList.filter { it.recurrenceType == RecurrenceType.NONE }
        }
    }
    val previousAmount = previousMonthExpenses.sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
    
    // Calculate percentage change vs previous month
    val vsLastMonth = if (previousAmount > 0) {
        ((currentAmount - previousAmount) / previousAmount) * 100
    } else if (currentAmount > 0) {
        100.0 // If no previous data but current exists, it's 100% increase
    } else {
        0.0
    }
    
    // Calculate 3-month average (current + 2 previous months)
    val monthsToCheck = listOf(currentMonth, currentMonth.minusMonths(1), currentMonth.minusMonths(2), currentMonth.minusMonths(3)
        , currentMonth.minusMonths(4), currentMonth.minusMonths(5))
    val totalAmountLast3Months = monthsToCheck.sumOf { month ->
        expenses.filter { expense ->
            val expenseDate = expense.date.toLocalDate()
            expenseDate.year == month.year && expenseDate.month == month.month &&
            expense.categoryId == categoryId
        }.let { expenseList ->
            when (filterType) {
                ExpenseFilterType.ALL -> expenseList
                ExpenseFilterType.RECURRING -> expenseList.filter { it.recurrenceType != RecurrenceType.NONE }
                ExpenseFilterType.ONE_TIME -> expenseList.filter { it.recurrenceType == RecurrenceType.NONE }
            }
        }.sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
    }
    val avgAmount = totalAmountLast3Months / 6.0
    
    // Calculate percentage change vs average
    val vsAverage = if (avgAmount > 0) {
        ((currentAmount - avgAmount) / avgAmount) * 100
    } else if (currentAmount > 0) {
        100.0
    } else {
        0.0
    }
    
    return CategoryComparison(vsLastMonth, vsAverage)
}

fun calculateTotalComparison(
    viewModel: ExpenseViewModel,
    currentMonth: YearMonth,
    filterType: ExpenseFilterType = ExpenseFilterType.ALL
): CategoryComparison {
    val expenses = viewModel.expenses.value
    val defaultCurrency = viewModel.defaultCurrency
    
    // Current month total
    val currentMonthExpenses = expenses.filter { expense ->
        val expenseDate = expense.date.toLocalDate()
        expenseDate.year == currentMonth.year && expenseDate.month == currentMonth.month
    }.let { expenseList ->
        when (filterType) {
            ExpenseFilterType.ALL -> expenseList
            ExpenseFilterType.RECURRING -> expenseList.filter { it.recurrenceType != RecurrenceType.NONE }
            ExpenseFilterType.ONE_TIME -> expenseList.filter { it.recurrenceType == RecurrenceType.NONE }
        }
    }
    val currentAmount = currentMonthExpenses.sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
    
    // Previous month total
    val previousMonth = currentMonth.minusMonths(1)
    val previousMonthExpenses = expenses.filter { expense ->
        val expenseDate = expense.date.toLocalDate()
        expenseDate.year == previousMonth.year && expenseDate.month == previousMonth.month
    }.let { expenseList ->
        when (filterType) {
            ExpenseFilterType.ALL -> expenseList
            ExpenseFilterType.RECURRING -> expenseList.filter { it.recurrenceType != RecurrenceType.NONE }
            ExpenseFilterType.ONE_TIME -> expenseList.filter { it.recurrenceType == RecurrenceType.NONE }
        }
    }
    val previousAmount = previousMonthExpenses.sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
    
    // Calculate percentage change vs previous month
    val vsLastMonth = if (previousAmount > 0) {
        ((currentAmount - previousAmount) / previousAmount) * 100
    } else if (currentAmount > 0) {
        100.0
    } else {
        0.0
    }
    
    // Calculate 6-month average
    val monthsToCheck = listOf(currentMonth, currentMonth.minusMonths(1), currentMonth.minusMonths(2), currentMonth.minusMonths(3), currentMonth.minusMonths(4), currentMonth.minusMonths(5))
    val totalAmountLast6Months = monthsToCheck.sumOf { month ->
        expenses.filter { expense ->
            val expenseDate = expense.date.toLocalDate()
            expenseDate.year == month.year && expenseDate.month == month.month
        }.let { expenseList ->
            when (filterType) {
                ExpenseFilterType.ALL -> expenseList
                ExpenseFilterType.RECURRING -> expenseList.filter { it.recurrenceType != RecurrenceType.NONE }
                ExpenseFilterType.ONE_TIME -> expenseList.filter { it.recurrenceType == RecurrenceType.NONE }
            }
        }.sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
    }
    val avgAmount = totalAmountLast6Months / 6.0
    
    // Calculate percentage change vs average
    val vsAverage = if (avgAmount > 0) {
        ((currentAmount - avgAmount) / avgAmount) * 100
    } else if (currentAmount > 0) {
        100.0
    } else {
        0.0
    }
    
    return CategoryComparison(vsLastMonth, vsAverage)
}



@Composable
private fun TotalComparisonIndicator(
    percentage: Double,
    label: String,
    isDarkTheme: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = ThemeColors.getTextGrayColor(isDarkTheme),
            textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
        )
        
        Spacer(modifier = Modifier.height(14.dp))
        
        Text(
            text = if (percentage == 0.0) "±0%" else "${if (percentage > 0) "+" else ""}${String.format("%.1f", percentage)}%",
            fontSize = 16.sp,
            color = when {
                percentage > 0 -> Color.Red
                percentage < 0 -> Color.Green 
                else -> ThemeColors.getTextColor(isDarkTheme)
            },
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )


}
}

