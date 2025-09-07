package com.example.expensetrackerkotlin.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.DialogProperties
import kotlin.math.*
import com.example.expensetrackerkotlin.data.*
import com.example.expensetrackerkotlin.ui.components.*
import com.example.expensetrackerkotlin.ui.components.ChartDataPoint
import com.example.expensetrackerkotlin.ui.components.CategoryAnalysisData
import com.example.expensetrackerkotlin.ui.components.SortOption
import com.example.expensetrackerkotlin.ui.theme.*
import com.example.expensetrackerkotlin.utils.NumberFormatter
import com.example.expensetrackerkotlin.viewmodel.ExpenseViewModel
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.launch
import java.time.chrono.ChronoLocalDateTime


fun getMonthlyChartData(viewModel: ExpenseViewModel, selectedMonth: YearMonth): List<ChartDataPoint> {
    val expenses = viewModel.expenses.value
    val startOfMonth = selectedMonth.atDay(1).atStartOfDay()
    val endOfMonth = selectedMonth.atEndOfMonth().atTime(23, 59, 59)
    
    val monthlyExpenses = expenses.filter { expense ->
        expense.date.toLocalDate().let { expenseDate ->
            !expenseDate.isBefore(startOfMonth.toLocalDate()) && 
            !expenseDate.isAfter(endOfMonth.toLocalDate())
        }
    }
    
    // Group expenses by day and sum amounts
    val dailyExpenses = monthlyExpenses.groupBy { expense ->
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

    val categoryAnalysisData = remember(monthlyExpenses, categories, subCategories) {
        if (monthlyExpenses.isEmpty()) return@remember emptyList()

        val totalAmount = monthlyExpenses.sumOf { it.getAmountInDefaultCurrency(viewModel.defaultCurrency) }
        
        val categoryTotals = monthlyExpenses.groupBy { it.categoryId }
            .mapNotNull { (categoryId, categoryExpenses) ->
                val category = categories.find { it.id == categoryId }
                if (category != null) {
                    val amount = categoryExpenses.sumOf { it.getAmountInDefaultCurrency(viewModel.defaultCurrency) }
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

        categoryTotals
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
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        MonthlyAnalysisPieChart(
                            categoryData = categoryAnalysisData,
                            animatedPercentages = animatedPercentages,
                            isDarkTheme = isDarkTheme,
                            selectedSegment = selectedSegment,
                            onSegmentSelected = { selectedSegment = it }
                        )
                    }
                    item {
                        MonthlyLineChart(
                            data = getMonthlyChartData(viewModel, selectedMonth),
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
                
                // Absolute positioned popup overlay
                if (selectedSegment != null && selectedSegment!! < categoryAnalysisData.size) {
                    val selected = categoryAnalysisData[selectedSegment!!]
                    
                    // Calculate arrow angle to point to selected segment
                    val segmentAngle = remember(selectedSegment) {
                        var currentAngle = 0f
                        for (i in 0 until selectedSegment!!) {
                            currentAngle += animatedPercentages[i] * 360f
                        }
                        currentAngle + (animatedPercentages[selectedSegment!!] * 360f / 2f) - 90f
                    }
                    
                    // Line and Popup Animation
                    val line1Progress = remember { Animatable(0f) }
                    val line2Progress = remember { Animatable(0f) }
                    val popupScale = remember { Animatable(0f) }
                    val popupAlpha = remember { Animatable(0f) }
                    
                    LaunchedEffect(selectedSegment) {
                        if (selectedSegment != null) {
                            // Reset all animations
                            line1Progress.snapTo(0f)
                            line2Progress.snapTo(0f)
                            popupScale.snapTo(0f)
                            popupAlpha.snapTo(0f)
                            
                            // Sequence animation: Line 1 → Line 2 → Popup
                            // 1. Animate Line 1 growing
                            line1Progress.animateTo(1f, animationSpec = tween(400, easing = EaseOutCubic))
                            
                            // 2. Animate Line 2 growing
                            line2Progress.animateTo(1f, animationSpec = tween(300, easing = EaseOutCubic))
                            
                            // 3. Show popup with bounce
                            popupAlpha.animateTo(1f, animationSpec = tween(200))
                            popupScale.animateTo(1f, animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ))
                        } else {
                            // Animate out when closing
                            popupAlpha.animateTo(0f, animationSpec = tween(150))
                            popupScale.animateTo(0f, animationSpec = tween(150))
                            line2Progress.animateTo(0f, animationSpec = tween(200, easing = EaseInCubic))
                            line1Progress.animateTo(0f, animationSpec = tween(200, easing = EaseInCubic))
                        }
                    }
                    
                    // Absolute positioned popup
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopStart)
                            .offset(y = 360.dp) // Position below pie chart
                            .padding(horizontal = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            // Two-line connector from pie segment to popup
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp) // Constrained height to prevent extending out
                                    .offset(y = (-180).dp) // Position to align with pie chart
                                    .graphicsLayer {
                                        alpha = maxOf(line1Progress.value, line2Progress.value)
                                    }
                            ) {
                                // Calculate pie chart center position (at top of canvas)
                                val pieChartCenterX = size.width / 2f
                                val pieChartCenterY = 40.dp.toPx() // Pie chart center position
                                val pieRadius = 125.dp.toPx() // Half of 250dp pie chart size
                                
                                // Calculate the actual center point of the selected segment
                                // First calculate the middle angle of the segment
                                val segmentIndex = selectedSegment!!
                                var segmentStartAngle = -90f // Start from top
                                for (i in 0 until segmentIndex) {
                                    segmentStartAngle += animatedPercentages[i] * 360f
                                }
                                val segmentMiddleAngle = segmentStartAngle + (animatedPercentages[segmentIndex] * 360f / 2f)
                                val segmentAngleRad = segmentMiddleAngle * PI.toFloat() / 180f
                                
                                // Position at the middle of the segment thickness (between inner and outer radius)
                                val segmentRadius = pieRadius * 0.725f // Middle of donut (between 0.45f inner and 1.0f outer)
                                val segmentCenterX = pieChartCenterX + cos(segmentAngleRad) * segmentRadius
                                val segmentCenterY = pieChartCenterY + sin(segmentAngleRad) * segmentRadius
                                
                                // Line 1: Angled connector going DOWN from segment center
                                val elbowDistance = 35.dp.toPx()
                                // Always go down and slightly outward based on which side of the chart we're on
                                val elbowAngle = if (segmentCenterX < pieChartCenterX) {
                                    150f // Down and left
                                } else {
                                    30f // Down and right
                                }
                                val elbowAngleRad = elbowAngle * PI.toFloat() / 180f
                                
                                val elbowX = segmentCenterX + cos(elbowAngleRad) * elbowDistance
                                val elbowY = segmentCenterY + sin(elbowAngleRad) * elbowDistance
                                
                                // Line 2: Vertical line from elbow to popup (270° = straight down)
                                val popupTopY = size.height - 10.dp.toPx() // End just at bottom of canvas
                                
                                // Ensure elbow point is within screen bounds
                                val constrainedElbowX = elbowX.coerceIn(20.dp.toPx(), size.width - 20.dp.toPx())
                                
                                // Draw Line 1 (angled, going down) with animation
                                if (line1Progress.value > 0f) {
                                    val line1End = Offset(
                                        x = segmentCenterX + (constrainedElbowX - segmentCenterX) * line1Progress.value,
                                        y = segmentCenterY + (elbowY - segmentCenterY) * line1Progress.value
                                    )
                                    
                                    drawLine(
                                        color = selected.category.getColor(),
                                        start = Offset(segmentCenterX, segmentCenterY),
                                        end = line1End,
                                        strokeWidth = 2.dp.toPx()
                                    )
                                }
                                
                                // Draw Line 2 (vertical down) with animation
                                if (line2Progress.value > 0f && line1Progress.value >= 1f) {
                                    val line2End = Offset(
                                        x = constrainedElbowX,
                                        y = elbowY + (popupTopY - elbowY) * line2Progress.value
                                    )
                                    
                                    drawLine(
                                        color = selected.category.getColor(),
                                        start = Offset(constrainedElbowX, elbowY),
                                        end = line2End,
                                        strokeWidth = 2.dp.toPx()
                                    )
                                    
                                    // Draw arrow tip only when Line 2 is complete
                                    if (line2Progress.value >= 1f) {
                                        val arrowSize = 6.dp.toPx()
                                        val arrowPath = Path().apply {
                                            moveTo(constrainedElbowX, popupTopY) // Arrow tip (bottom)
                                            lineTo(constrainedElbowX - arrowSize, popupTopY - arrowSize) // Left side
                                            lineTo(constrainedElbowX + arrowSize, popupTopY - arrowSize) // Right side
                                            close()
                                        }
                                        
                                        drawPath(
                                            path = arrowPath,
                                            color = selected.category.getColor(),
                                            style = Fill
                                        )
                                    }
                                }
                            }
                            
                            // Category Info Card with solid matte background
                            Card(
                                modifier = Modifier
                                    .width(280.dp)
                                    .height(100.dp)
                                    .graphicsLayer {
                                        alpha = popupAlpha.value
                                        scaleX = popupScale.value
                                        scaleY = popupScale.value
                                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = selected.category.getColor().copy(alpha = 0.95f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Icon
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                Color.White.copy(alpha = 0.3f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = selected.category.getIcon(),
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    
                                    // Content
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = selected.category.name,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        
                                        Text(
                                            text = "${selected.expenseCount} harcama • %${String.format("%.1f", selected.percentage * 100)}",
                                            fontSize = 14.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                    
                                    // Amount
                                    Text(
                                        text = "${viewModel.defaultCurrency} ${NumberFormatter.formatAmount(selected.totalAmount)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
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
                    onShowSortMenuChanged = { showSortMenu = it }
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
    var showMonthPicker by remember { mutableStateOf(false) }
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
                .clickable { showMonthPicker = true },
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
fun MonthlyAnalysisPieChart(
    categoryData: List<CategoryAnalysisData>,
    animatedPercentages: List<Float>,
    isDarkTheme: Boolean,
    selectedSegment: Int?,
    onSegmentSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {

    val segmentScales = categoryData.mapIndexed { index, _ ->
        val animatedScale = remember { Animatable(1f) }
        LaunchedEffect(selectedSegment) {
            if (selectedSegment == index) {
                animatedScale.animateTo(
                    targetValue = 1.1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            } else {
                animatedScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }
        animatedScale.value
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ThemeColors.getCardBackgroundColor(isDarkTheme)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {



            if (categoryData.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(265.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Pie Chart
                    Canvas(
                        modifier = Modifier
                            .size(250.dp)
                            .pointerInput(categoryData) {
                                detectTapGestures { tapOffset ->
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val radius = minOf(size.width, size.height) / 2f - 20.dp.toPx()
                                    
                                    val distance = sqrt(
                                        (tapOffset.x - center.x).pow(2) + 
                                        (tapOffset.y - center.y).pow(2)
                                    )
                                    
                                    if (distance <= radius && distance >= radius * 0.45f) {
                                        val angle = atan2(
                                            tapOffset.y - center.y,
                                            tapOffset.x - center.x
                                        )
                                        var normalizedAngle = ((angle * 180f / PI.toFloat()) + 90f) % 360f
                                        if (normalizedAngle < 0) normalizedAngle += 360f
                                        
                                        var currentAngle = 0f
                                        for (i in animatedPercentages.indices) {
                                            val sweepAngle = animatedPercentages[i] * 360f
                                            if (normalizedAngle >= currentAngle && normalizedAngle <= currentAngle + sweepAngle) {
                                                onSegmentSelected(if (selectedSegment == i) null else i)
                                                break
                                            }
                                            currentAngle += sweepAngle
                                        }
                                    }
                                }
                            }
                    ) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val baseRadius = minOf(size.width, size.height) / 2 - 20.dp.toPx()
                        
                        var currentAngle = -90f
                        
                        animatedPercentages.forEachIndexed { index, animatedPercentage ->
                            val sweepAngle = animatedPercentage * 360f
                            val color = categoryData[index].category.getColor()
                            val scale = segmentScales[index]
                            val radius = baseRadius * scale
                            
                            drawArc(
                                color = color,
                                startAngle = currentAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true,
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(radius * 2, radius * 2)
                            )
                            
                            currentAngle += sweepAngle
                        }
                        
                        drawCircle(
                            color = Color.Transparent,
                            radius = baseRadius * 0.45f,
                            center = center,
                            blendMode = BlendMode.Clear
                        )
                    }
                    
                    // Hint text when nothing is selected
                    if (selectedSegment == null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(top = 220.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = ThemeColors.getTextGrayColor(isDarkTheme).copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Kategori seçmek için grafiğe dokunun",
                                fontSize = 11.sp,
                                color = ThemeColors.getTextGrayColor(isDarkTheme).copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategorySummarySection(
    categoryData: List<CategoryAnalysisData>,
    totalAmount: Double,
    defaultCurrency: String,
    isDarkTheme: Boolean,
    onCategoryClick: (CategoryAnalysisData) -> Unit,
    modifier: Modifier = Modifier
) {

        Column(
            modifier = Modifier.padding(6.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kategori Detayları",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeColors.getTextColor(isDarkTheme),
                )
                Text(
                    text = "$defaultCurrency ${NumberFormatter.formatAmount(totalAmount)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryOrange
                )
            }

            categoryData.forEach { data ->
                CategorySummaryRow(
                    categoryData = data,
                    defaultCurrency = defaultCurrency,
                    isDarkTheme = isDarkTheme,
                    onClick = { onCategoryClick(data) }
                )
                

            }

            

        }

}

@Composable
fun CategorySummaryRow(
    categoryData: CategoryAnalysisData,
    defaultCurrency: String,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 2.dp)
            .background(
                categoryData.category.getColor().copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    )

    {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
                .padding( 12.dp)
        )
        {
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
                    contentDescription = categoryData.category.name,
                    tint = categoryData.category.getColor(),
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = categoryData.category.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = ThemeColors.getTextColor(isDarkTheme),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${categoryData.expenseCount} harcama • %${String.format("%.1f", categoryData.percentage * 100)}",
                    fontSize = 14.sp,
                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                )
            }
        }
        
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .padding( 12.dp)
        ) {
            Text(
                text = "$defaultCurrency ${NumberFormatter.formatAmount(categoryData.totalAmount)}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = ThemeColors.getTextColor(isDarkTheme)
            )
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Detayları gör",
                tint = ThemeColors.getTextGrayColor(isDarkTheme),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun RecurringExpenseCard(
    totalAmount: Double,
    defaultCurrency: String,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
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

