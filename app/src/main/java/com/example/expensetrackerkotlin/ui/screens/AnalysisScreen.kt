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
import com.example.expensetrackerkotlin.ui.components.SubCategoryAnalysisData
import com.example.expensetrackerkotlin.ui.components.SortOption
import com.example.expensetrackerkotlin.ui.components.ExpenseFilterType
import com.example.expensetrackerkotlin.ui.theme.*
import com.example.expensetrackerkotlin.utils.NumberFormatter
import com.example.expensetrackerkotlin.viewmodel.ExpenseViewModel
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*


fun getMonthlyChartData(
    viewModel: ExpenseViewModel, 
    selectedMonth: YearMonth, 
    selectedDateRange: Pair<LocalDate?, LocalDate?> = null to null,
    filterType: ExpenseFilterType = ExpenseFilterType.ALL
): List<ChartDataPoint> {
    val expenses = viewModel.expenses.value
    val (rangeStart, rangeEnd) = selectedDateRange
    
    val monthlyExpenses = if (rangeStart != null || rangeEnd != null) {
        // Use date range filtering
        val startDate = rangeStart ?: selectedMonth.atDay(1)
        val endDate = rangeEnd ?: selectedMonth.atEndOfMonth()
        
        expenses.filter { expense ->
            val expenseDate = expense.date.toLocalDate()
            !expenseDate.isBefore(startDate) && !expenseDate.isAfter(endDate)
        }
    } else {
        // Use month filtering (default behavior)
        val startOfMonth = selectedMonth.atDay(1).atStartOfDay()
        val endOfMonth = selectedMonth.atEndOfMonth().atTime(23, 59, 59)
        
        expenses.filter { expense ->
            expense.date.toLocalDate().let { expenseDate ->
                !expenseDate.isBefore(startOfMonth.toLocalDate()) && 
                !expenseDate.isAfter(endOfMonth.toLocalDate())
            }
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
    
    // Create a complete list for the selected range or all days in the month
    return if (rangeStart != null || rangeEnd != null) {
        // Generate chart data for selected date range only
        val startDate = rangeStart ?: selectedMonth.atDay(1)
        val endDate = rangeEnd ?: selectedMonth.atEndOfMonth()
        
        val rangeDays = mutableListOf<Int>()
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            rangeDays.add(currentDate.dayOfMonth)
            currentDate = currentDate.plusDays(1)
        }
        
        rangeDays.map { day ->
            dailyExpenses.find { it.day == day } ?: ChartDataPoint(day = day, amount = 0.0)
        }
    } else {
        // Generate chart data for all days in the month (default behavior)
        val daysInMonth = selectedMonth.lengthOfMonth()
        (1..daysInMonth).map { day ->
            dailyExpenses.find { it.day == day } ?: ChartDataPoint(day = day, amount = 0.0)
        }
    }
}

fun getFilteredSubCategoryAnalysisData(
    monthlyExpenses: List<Expense>,
    categories: List<Category>,
    subCategories: List<SubCategory>,
    defaultCurrency: String,
    filterType: ExpenseFilterType = ExpenseFilterType.ALL
): List<SubCategoryAnalysisData> {
    if (monthlyExpenses.isEmpty()) return emptyList()

    // Apply filter based on expense type
    val filteredExpenses = when (filterType) {
        ExpenseFilterType.ALL -> monthlyExpenses
        ExpenseFilterType.RECURRING -> monthlyExpenses.filter { it.recurrenceType != RecurrenceType.NONE }
        ExpenseFilterType.ONE_TIME -> monthlyExpenses.filter { it.recurrenceType == RecurrenceType.NONE }
    }

    val totalAmount = filteredExpenses.sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
    
    val subCategoryTotals = filteredExpenses.groupBy { it.subCategoryId }
        .mapNotNull { (subCategoryId, subCategoryExpenses) ->
            val subCategory = subCategories.find { it.id == subCategoryId }
            val parentCategory = categories.find { it.id == subCategory?.categoryId }
            if (subCategory != null && parentCategory != null) {
                val subCategoryTotal = subCategoryExpenses.sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
                SubCategoryAnalysisData(
                    subCategory = subCategory,
                    parentCategory = parentCategory,
                    totalAmount = subCategoryTotal,
                    expenseCount = subCategoryExpenses.size,
                    percentage = if (totalAmount > 0) subCategoryTotal / totalAmount else 0.0,
                    expenses = subCategoryExpenses
                )
            } else null
        }
        .sortedByDescending { it.totalAmount }

    return subCategoryTotals
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
    var showSubCategoryDialog by remember { mutableStateOf(false) }
    var selectedSubCategory by remember { mutableStateOf<SubCategoryAnalysisData?>(null) }
    var sortOption by remember { mutableStateOf(SortOption.DATE_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedSegment by remember { mutableStateOf<Int?>(null) }
    var selectedExpenseFilter by remember { mutableStateOf(ExpenseFilterType.ALL) }
    var selectedMonthlyExpenseType by remember { mutableStateOf(ExpenseFilterType.ALL) }
    
    // Date range picker state
    var showDateRangePicker by remember { mutableStateOf(false) }
    var selectedDateRange by remember { mutableStateOf<Pair<LocalDate?, LocalDate?>>(null to null) }

    val expenses by viewModel.expenses.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()

    val monthlyExpenses = remember(expenses, selectedMonth, selectedDateRange) {
        val (rangeStart, rangeEnd) = selectedDateRange
        
        if (rangeStart != null || rangeEnd != null) {
            // Use date range filtering
            val startDate = rangeStart ?: selectedMonth.atDay(1)
            val endDate = rangeEnd ?: selectedMonth.atEndOfMonth()
            
            expenses.filter { expense ->
                val expenseDate = expense.date.toLocalDate()
                !expenseDate.isBefore(startDate) && !expenseDate.isAfter(endDate)
            }
        } else {
            // Use month filtering (default behavior)
            val startOfMonth = selectedMonth.atDay(1).atStartOfDay()
            val endOfMonth = selectedMonth.atEndOfMonth().atTime(23, 59, 59)

            expenses.filter { expense ->
                expense.date.toLocalDate().let { expenseDate ->
                    !expenseDate.isBefore(startOfMonth.toLocalDate()) && 
                    !expenseDate.isAfter(endOfMonth.toLocalDate())
                }
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
    
    val subCategoryAnalysisData = remember(monthlyExpenses, categories, subCategories, selectedMonthlyExpenseType) {
        getFilteredSubCategoryAnalysisData(
            monthlyExpenses = monthlyExpenses,
            categories = categories,
            subCategories = subCategories,
            defaultCurrency = viewModel.defaultCurrency,
            filterType = selectedMonthlyExpenseType
        )
    }

    val recurringExpenseTotal = remember(expenses,selectedMonth) {

        val threeMonthsFromNow = selectedMonth.atEndOfMonth().plusMonths(3).atStartOfDay().plusDays(1)
        expenses.filter { expense ->
            expense.recurrenceType != RecurrenceType.NONE
                   &&(expense.endDate == null || expense.endDate.isAfter(threeMonthsFromNow))
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
            selectedDateRange = selectedDateRange,
            onMonthYearChanged = { selectedMonth = it },
            onRangePickerClick = { showDateRangePicker = true },
            isDarkTheme = isDarkTheme
        )

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
        if (categoryAnalysisData.isNotEmpty())
        {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(selectedSegment)
                    {
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
                                text = "Bu Dönem Toplam Harcama",
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
                            amount = totalComparison.vsLastMonth,
                            currency = viewModel.defaultCurrency,
                            label = "Önceki aya göre",
                            isDarkTheme = isDarkTheme
                        )
                        TotalComparisonIndicator(
                            amount = totalComparison.vsAverage,
                            currency = viewModel.defaultCurrency,
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
                            data = getMonthlyChartData(viewModel, selectedMonth, selectedDateRange, selectedMonthlyExpenseType),
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
                            subCategoryData = subCategoryAnalysisData,
                            totalAmount = totalMonthlyAmount,
                            defaultCurrency = viewModel.defaultCurrency,
                            isDarkTheme = isDarkTheme,
                            onCategoryClick = { categoryData ->
                                selectedCategory = categoryData
                                showCategoryDialog = true
                            },
                            onSubCategoryClick = { subCategoryData ->
                                selectedSubCategory = subCategoryData
                                showSubCategoryDialog = true
                            }
                        )
                    }


                }
                
                // Line and Popup Animation - moved outside conditional to allow closing animation
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

                // Overlay popup that appears above everything
                if (selectedSegment != null && selectedSegment!! < categoryAnalysisData.size) {
                    val selected = categoryAnalysisData[selectedSegment!!]
                    
                    // Get pie chart item position
                    val pieChartItemIndex = 1
                    val pieChartItemInfo = scrollState.layoutInfo.visibleItemsInfo
                        .find { it.index == pieChartItemIndex }
                    
                    // Only show popup if pie chart is visible
                    if (pieChartItemInfo != null) {
                        
                        // Fixed position relative to pie chart center
                        val pieChartTop = pieChartItemInfo.offset
                        val pieChartHeight = pieChartItemInfo.size
                        val popupY = pieChartTop + pieChartHeight -50// 20dp below pie chart
                        
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
                        text = "Bu dönem henüz harcama yok",
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
            containerColor = ThemeColors.getBackgroundColor(isDarkTheme),
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
    
    if (showSubCategoryDialog && selectedSubCategory != null) {
        val subCategoryDetailSheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { true }
        )
        
        LaunchedEffect(Unit) {
            subCategoryDetailSheetState.expand()
        }
        
        ModalBottomSheet(
            onDismissRequest = {
                showSubCategoryDialog = false
                selectedSubCategory = null
            },
            sheetState = subCategoryDetailSheetState,
            containerColor = ThemeColors.getBackgroundColor(isDarkTheme),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 700.dp)
            ) {
                SubCategoryDetailBottomSheet(
                    subCategoryData = selectedSubCategory!!,
                    defaultCurrency = viewModel.defaultCurrency,
                    isDarkTheme = isDarkTheme,
                    viewModel = viewModel,
                    selectedMonth = selectedMonth,
                    selectedFilterType = selectedMonthlyExpenseType,
                    onDismiss = {
                        showSubCategoryDialog = false
                        selectedSubCategory = null
                    }
                )
            }
        }
    }
    
    // Date range picker
    if (showDateRangePicker) {
        DateRangePicker(
            selectedMonth = selectedMonth,
            selectedRange = selectedDateRange,
            isDarkTheme = isDarkTheme,
            onRangeSelected = { newRange ->
                selectedDateRange = newRange
                // Clear range if both dates are null, otherwise keep it
                if (newRange.first == null && newRange.second == null) {
                    selectedDateRange = null to null
                }
            },
            onDismiss = { showDateRangePicker = false }
        )
    }
}

private fun formatDateRange(range: Pair<LocalDate?, LocalDate?>): String {
    val (start, end) = range
    val formatter = DateTimeFormatter.ofPattern("dd MMM", Locale.forLanguageTag("tr"))
    
    return when {
        start != null && end != null -> "${start.format(formatter)} - ${end.format(formatter)}"
        start != null -> "${start.format(formatter)} - ?"
        else -> "Tarih aralığı seçin"
    }
}

@Composable
fun MonthYearSelector(
    selectedMonth: YearMonth,
    selectedDateRange: Pair<LocalDate?, LocalDate?>,
    onMonthYearChanged: (YearMonth) -> Unit,
    onRangePickerClick: () -> Unit,
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
                .clickable { onRangePickerClick() },
            colors = CardDefaults.cardColors(
                containerColor = ThemeColors.getCardBackgroundColor(isDarkTheme)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("tr"))),
                    textAlign = TextAlign.Center,
                    color = ThemeColors.getTextColor(isDarkTheme),
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                )
                
                if (selectedDateRange.first != null || selectedDateRange.second != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatDateRange(selectedDateRange),
                        textAlign = TextAlign.Center,
                        color = AppColors.PrimaryOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        IconButton(
            onClick = {
                val nextMonth = selectedMonth.plusMonths(1)
               // if (!nextMonth.isAfter(currentMonth)) {
                    onMonthYearChanged(nextMonth)
               // }
            },
            //enabled = !selectedMonth.plusMonths(1).isAfter(currentMonth)
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Sonraki ay",
                tint = //if (!selectedMonth.plusMonths(1).isAfter(currentMonth))
                    ThemeColors.getTextColor(isDarkTheme) 
                //else ThemeColors.getTextGrayColor(isDarkTheme)
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

fun calculateSubCategoryComparison(
    viewModel: ExpenseViewModel,
    currentMonth: YearMonth,
    subCategoryId: String,
    filterType: ExpenseFilterType = ExpenseFilterType.ALL
): CategoryComparison {
    val expenses = viewModel.expenses.value
    val defaultCurrency = viewModel.defaultCurrency
    
    // Current month amount
    val currentMonthExpenses = expenses.filter { expense ->
        val expenseDate = expense.date.toLocalDate()
        expenseDate.year == currentMonth.year && expenseDate.month == currentMonth.month &&
        expense.subCategoryId == subCategoryId
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
        expense.subCategoryId == subCategoryId
    }.let { expenseList ->
        when (filterType) {
            ExpenseFilterType.ALL -> expenseList
            ExpenseFilterType.RECURRING -> expenseList.filter { it.recurrenceType != RecurrenceType.NONE }
            ExpenseFilterType.ONE_TIME -> expenseList.filter { it.recurrenceType == RecurrenceType.NONE }
        }
    }
    val previousAmount = previousMonthExpenses.sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }


    // Calculate amount difference vs previous month
    val vsLastMonth = currentAmount - previousAmount
    
    // Calculate 6-month average (current + 5 previous months)
    val monthsToCheck = listOf(currentMonth, currentMonth.minusMonths(1), currentMonth.minusMonths(2), currentMonth.minusMonths(3),
        currentMonth.minusMonths(4), currentMonth.minusMonths(5))
    val totalAmountLast6Months = monthsToCheck.sumOf { month ->
        expenses.filter { expense ->
            val expenseDate = expense.date.toLocalDate()
            expenseDate.year == month.year && expenseDate.month == month.month &&
            expense.subCategoryId == subCategoryId
        }.let { expenseList ->
            when (filterType) {
                ExpenseFilterType.ALL -> expenseList
                ExpenseFilterType.RECURRING -> expenseList.filter { it.recurrenceType != RecurrenceType.NONE }
                ExpenseFilterType.ONE_TIME -> expenseList.filter { it.recurrenceType == RecurrenceType.NONE }
            }
        }.sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
    }
    val avgAmount = totalAmountLast6Months / 6.0


    // Calculate amount difference vs average
    val vsAverage = currentAmount - avgAmount
    
    return CategoryComparison(vsLastMonth, vsAverage)
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
    
    // Calculate amount difference vs previous month
    val vsLastMonth = currentAmount - previousAmount
    
    // Calculate 6-month average (current + 5 previous months)
    val monthsToCheck = listOf(currentMonth, currentMonth.minusMonths(1), currentMonth.minusMonths(2), currentMonth.minusMonths(3)
        , currentMonth.minusMonths(4), currentMonth.minusMonths(5))
    val totalAmountLast6Months = monthsToCheck.sumOf { month ->
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
    val avgAmount = totalAmountLast6Months / 6.0
    
    // Calculate amount difference vs average
    val vsAverage = currentAmount - avgAmount
    
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
    
    // Calculate amount difference vs previous month
    val vsLastMonth = currentAmount - previousAmount
    
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
    
    // Calculate amount difference vs average
    val vsAverage = currentAmount - avgAmount
    
    return CategoryComparison(vsLastMonth, vsAverage)
}



@Composable
private fun TotalComparisonIndicator(
    amount: Double,
    currency: String,
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

