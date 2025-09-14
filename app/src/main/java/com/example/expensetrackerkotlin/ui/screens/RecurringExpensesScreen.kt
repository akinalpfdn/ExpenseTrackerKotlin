package com.example.expensetrackerkotlin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.expensetrackerkotlin.R
import com.example.expensetrackerkotlin.data.Expense
import com.example.expensetrackerkotlin.data.RecurrenceType
import androidx.compose.runtime.collectAsState
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import com.example.expensetrackerkotlin.utils.NumberFormatter
import com.example.expensetrackerkotlin.viewmodel.ExpenseViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs

enum class SortType {
    AMOUNT_HIGH_TO_LOW,
    AMOUNT_LOW_TO_HIGH,
    DESCRIPTION_A_TO_Z,
    DESCRIPTION_Z_TO_A,
    CATEGORY_A_TO_Z,
    CATEGORY_Z_TO_A
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringExpensesScreen(
    viewModel: ExpenseViewModel,
    onDismiss: () -> Unit
) {
    val expenses by viewModel.expenses.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()
    val isDarkTheme = viewModel.theme == "dark"
    
    // Search and filter state
    var searchText by remember { mutableStateOf("") }
    var showSortMenu by remember { mutableStateOf(false) }
    var currentSortType by remember { mutableStateOf(SortType.AMOUNT_HIGH_TO_LOW) }
    
    // Get only recurring expenses that still have future occurrences
    val baseRecurringExpenses = remember(expenses) {
        val today = LocalDateTime.now()
        expenses.filter { it.recurrenceType != RecurrenceType.NONE }
            .groupBy { it.recurrenceGroupId }
            .mapNotNull { (_, groupExpenses) -> 
                val baseExpense = groupExpenses.first()
                // Check if this recurring expense still has future occurrences
                val hasFutureOccurrences = groupExpenses.any { 
                    it.date.toLocalDate().isAfter(today.toLocalDate().minusDays(1)) // Today and future
                }
                if (hasFutureOccurrences) baseExpense else null
            }
    }
    
    // Filter and sort expenses
    val recurringExpenses = remember(baseRecurringExpenses, searchText, currentSortType, categories, subCategories) {
        var filteredExpenses = baseRecurringExpenses
        
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
            SortType.AMOUNT_HIGH_TO_LOW -> filteredExpenses.sortedByDescending { it.getAmountInDefaultCurrency(viewModel.defaultCurrency) }
            SortType.AMOUNT_LOW_TO_HIGH -> filteredExpenses.sortedBy { it.getAmountInDefaultCurrency(viewModel.defaultCurrency) }
            SortType.DESCRIPTION_A_TO_Z -> filteredExpenses.sortedBy { it.description.lowercase() }
            SortType.DESCRIPTION_Z_TO_A -> filteredExpenses.sortedByDescending { it.description.lowercase() }
            SortType.CATEGORY_A_TO_Z -> filteredExpenses.sortedBy { expense ->
                val subCategory = subCategories.find { it.id == expense.subCategoryId }
                subCategory?.name?.lowercase() ?: "zzz"
            }
            SortType.CATEGORY_Z_TO_A -> filteredExpenses.sortedByDescending { expense ->
                val subCategory = subCategories.find { it.id == expense.subCategoryId }
                subCategory?.name?.lowercase() ?: ""
            }
        }
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = ThemeColors.getTextColor(isDarkTheme)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = stringResource(R.string.recurring_expenses),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeColors.getTextColor(isDarkTheme),
                    modifier = Modifier.weight(1f)
                )
                
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
                            text = { Text(stringResource(R.string.amount_high_to_low), color = ThemeColors.getTextColor(isDarkTheme)) },
                            onClick = { 
                                currentSortType = SortType.AMOUNT_HIGH_TO_LOW
                                showSortMenu = false 
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.amount_low_to_high), color = ThemeColors.getTextColor(isDarkTheme)) },
                            onClick = { 
                                currentSortType = SortType.AMOUNT_LOW_TO_HIGH
                                showSortMenu = false 
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.description_a_to_z), color = ThemeColors.getTextColor(isDarkTheme)) },
                            onClick = { 
                                currentSortType = SortType.DESCRIPTION_A_TO_Z
                                showSortMenu = false 
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.description_z_to_a), color = ThemeColors.getTextColor(isDarkTheme)) },
                            onClick = { 
                                currentSortType = SortType.DESCRIPTION_Z_TO_A
                                showSortMenu = false 
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.category_a_to_z), color = ThemeColors.getTextColor(isDarkTheme)) },
                            onClick = { 
                                currentSortType = SortType.CATEGORY_A_TO_Z
                                showSortMenu = false 
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.category_z_to_a), color = ThemeColors.getTextColor(isDarkTheme)) },
                            onClick = { 
                                currentSortType = SortType.CATEGORY_Z_TO_A
                                showSortMenu = false 
                            }
                        )
                    }
                }
            }
            
            // Search bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
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
                                contentDescription = stringResource(R.string.search),
                                tint = ThemeColors.getTextGrayColor(isDarkTheme),
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchText.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.search_placeholder),
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
                        if (searchText.isNotBlank()) {
                            Text(
                                text = stringResource(R.string.no_search_results),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = ThemeColors.getTextColor(isDarkTheme),
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = stringResource(R.string.search_no_results_description, searchText),
                                fontSize = 14.sp,
                                color = ThemeColors.getTextGrayColor(isDarkTheme),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.no_recurring_expenses),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = ThemeColors.getTextColor(isDarkTheme),
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = stringResource(R.string.recurring_expenses_hint),
                                fontSize = 14.sp,
                                color = ThemeColors.getTextGrayColor(isDarkTheme),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                Column {
                    // Results count (only show when searching)
                    if (searchText.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.results_found, recurringExpenses.size),
                            fontSize = 12.sp,
                            color = ThemeColors.getTextGrayColor(isDarkTheme),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringExpenseCard(
    expense: Expense,
    viewModel: ExpenseViewModel,
    isDarkTheme: Boolean,
    onDelete: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editAmount by remember { mutableStateOf(expense.amount.toString()) }
    var editDescription by remember { mutableStateOf(expense.description) }
    var editExchangeRate by remember { mutableStateOf(expense.exchangeRate?.toString() ?: "") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var tempEndDate by remember { mutableStateOf(expense.endDate ?: LocalDateTime.now().plusYears(1)) }
    
    // Collect categories and subcategories from ViewModel
    val categories by viewModel.categories.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()
    
    // Get category and subcategory for this expense
    val category = categories.find { it.id == expense.categoryId }
    val subCategory = subCategories.find { it.id == expense.subCategoryId }
    
    // Swipe animation state
    var offsetX by remember { mutableStateOf(0f) }
    var isSwiped by remember { mutableStateOf(false) }
    
    // Animated offset for smooth movement
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "swipe_offset"
    )
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        ) {
            // Delete background that appears when swiping
            if (abs(offsetX) > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.Red.copy(alpha = 0.8f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(R.string.delete),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            
            // Main card content
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = animatedOffsetX.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (abs(offsetX) > 150f) {
                                    // Swipe threshold reached, show confirmation
                                    showDeleteConfirmation = true
                                    isSwiped = true
                                } else {
                                    // Reset position if not swiped enough
                                    offsetX = 0f
                                    isSwiped = false
                                }
                            }
                        ) { _, dragAmount ->
                            // Only allow left swipe (negative drag)
                            if (dragAmount < 0) {
                                offsetX = (offsetX + dragAmount).coerceAtMost(0f)
                            }
                        }
                    }
                    .clickable {
                        if (!isSwiped) {
                            if (isEditing) {
                                // If already editing, close the edit mode
                                isEditing = false
                                // Reset edit fields to original values
                                editAmount = expense.amount.toString()
                                editDescription = expense.description
                                editExchangeRate = expense.exchangeRate?.toString() ?: ""
                            } else {
                                // If not editing, open edit mode
                                isEditing = true
                            }
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ThemeColors.getCardBackgroundColor(isDarkTheme)
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        (category?.getColor() ?: Color.Gray).copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = category?.getIcon() ?: Icons.Default.Category,
                                    contentDescription = category?.name ?: "Category",
                                    tint = category?.getColor() ?: Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    text = subCategory?.name ?: "Unknown",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = ThemeColors.getTextColor(isDarkTheme),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (expense.description.isNotBlank()) {
                                    Text(
                                        text = expense.description,
                                        fontSize = 13.sp,
                                        color = ThemeColors.getTextGrayColor(isDarkTheme),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "${expense.currency} ${NumberFormatter.formatAmount(expense.amount)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = ThemeColors.getTextColor(isDarkTheme)
                            )
                            if (expense.exchangeRate != null) {
                                Text(
                                    text = stringResource(R.string.exchange_rate, String.format("%.4f", expense.exchangeRate)),
                                    fontSize = 11.sp,
                                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                                )
                            }
                        }
                    }
                    
                    // Recurrence info row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (expense.recurrenceType) {
                                RecurrenceType.DAILY -> stringResource(R.string.daily)
                                RecurrenceType.WEEKDAYS -> stringResource(R.string.weekdays)
                                RecurrenceType.WEEKLY -> stringResource(R.string.weekly)
                                RecurrenceType.MONTHLY -> stringResource(R.string.monthly)
                                RecurrenceType.NONE -> ""
                            },
                            fontSize = 12.sp,
                            color = ThemeColors.getTextGrayColor(isDarkTheme)
                        )
                        
                        expense.endDate?.let { endDate ->
                            Text(
                                text = stringResource(R.string.end_date_recurring, endDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("tr")))),
                                fontSize = 12.sp,
                                color = ThemeColors.getTextGrayColor(isDarkTheme)
                            )
                        }
                    }
                    
                    AnimatedVisibility(
                        visible = isEditing,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .background(
                                    ThemeColors.getCardBackgroundColor(isDarkTheme),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            // Amount field
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .background(
                                        ThemeColors.getInputBackgroundColor(isDarkTheme),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = ThemeColors.getTextGrayColor(isDarkTheme),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                BasicTextField(
                                    value = editAmount,
                                    onValueChange = { newValue ->
                                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                            editAmount = newValue
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 14.sp,
                                        color = ThemeColors.getTextColor(isDarkTheme)
                                    ),
                                    decorationBox = { innerTextField ->
                                        Box(
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (editAmount.isEmpty()) {
                                                Text(
                                                    text = stringResource(R.string.amount),
                                                    fontSize = 14.sp,
                                                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Description field
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .background(
                                        ThemeColors.getInputBackgroundColor(isDarkTheme),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = ThemeColors.getTextGrayColor(isDarkTheme),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                BasicTextField(
                                    value = editDescription,
                                    onValueChange = { editDescription = it },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 14.sp,
                                        color = ThemeColors.getTextColor(isDarkTheme)
                                    ),
                                    decorationBox = { innerTextField ->
                                        Box(
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (editDescription.isEmpty()) {
                                                Text(
                                                    text = stringResource(R.string.description),
                                                    fontSize = 14.sp,
                                                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                            
                            // Exchange Rate (only show if currency is different from default)
                            if (expense.currency != viewModel.defaultCurrency) {
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .background(
                                            ThemeColors.getInputBackgroundColor(isDarkTheme),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = ThemeColors.getTextGrayColor(isDarkTheme),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    BasicTextField(
                                        value = editExchangeRate,
                                        onValueChange = { newValue ->
                                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                                editExchangeRate = newValue
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 12.dp),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            fontSize = 14.sp,
                                            color = ThemeColors.getTextColor(isDarkTheme)
                                        ),
                                        decorationBox = { innerTextField ->
                                            Box(
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                if (editExchangeRate.isEmpty()) {
                                                    Text(
                                                        text = stringResource(R.string.exchange_rate_field),
                                                        fontSize = 14.sp,
                                                        color = ThemeColors.getTextGrayColor(isDarkTheme)
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        }
                                    )
                                }
                            }
                            
                            // End Date field
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .background(
                                        ThemeColors.getInputBackgroundColor(isDarkTheme),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = ThemeColors.getTextGrayColor(isDarkTheme),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { showEndDatePicker = true }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = tempEndDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                        fontSize = 14.sp,
                                        color = ThemeColors.getTextColor(isDarkTheme)
                                    )
                                    
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = stringResource(R.string.select_date),
                                        tint = ThemeColors.getTextGrayColor(isDarkTheme),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {


                                Button(
                                    onClick = {
                                        isEditing = false
                                        editAmount = expense.amount.toString()
                                        editDescription = expense.description
                                        editExchangeRate = expense.exchangeRate?.toString() ?: ""
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ThemeColors.getButtonDisabledColor(isDarkTheme)
                                    ),
                                    shape = RoundedCornerShape(26.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.cancel),
                                            tint = ThemeColors.getTextColor(isDarkTheme),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(R.string.cancel), fontSize = 12.sp, color = ThemeColors.getTextColor(isDarkTheme))
                                    }
                                }
                                Button(
                                    onClick = {
                                        val newAmount = editAmount.toDoubleOrNull()
                                        if (newAmount != null && newAmount > 0) {
                                            val newEndDate = tempEndDate
                                            val newExchangeRate = if (expense.currency != viewModel.defaultCurrency) {
                                                editExchangeRate.toDoubleOrNull()
                                            } else {
                                                null
                                            }

                                            // Use the new method to handle end date changes properly
                                            viewModel.updateRecurringExpenseEndDate(
                                                baseExpense = expense,
                                                newEndDate = newEndDate,
                                                newAmount = newAmount,
                                                newDescription = editDescription,
                                                newExchangeRate = newExchangeRate
                                            )
                                        }
                                        isEditing = false
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppColors.PrimaryOrange
                                    ),
                                    shape = RoundedCornerShape(26.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = stringResource(R.string.save),
                                            tint = ThemeColors.getTextColor(isDarkTheme),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(R.string.save), fontSize = 12.sp, color = ThemeColors.getTextColor(isDarkTheme))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
                // Reset swipe state when dialog is dismissed
                offsetX = 0f
                isSwiped = false
            },
            title = {
                Text(
                    text = stringResource(R.string.delete_recurring_expense),
                    color = ThemeColors.getTextColor(isDarkTheme),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.delete_recurring_confirmation),
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                        // Reset swipe state after deletion
                        offsetX = 0f
                        isSwiped = false
                    }
                ) {
                    Text(
                        stringResource(R.string.delete),
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        // Reset swipe state when canceling
                        offsetX = 0f
                        isSwiped = false
                    }
                ) {
                    Text(
                        stringResource(R.string.cancel),
                        color = ThemeColors.getTextColor(isDarkTheme)
                    )
                }
            },
            containerColor = ThemeColors.getDialogBackgroundColor(isDarkTheme)
        )
    }
    
    // Date Picker Dialog
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = tempEndDate.toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
        )
        
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            tempEndDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneOffset.UTC)
                                .toLocalDateTime()
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text(
                        stringResource(R.string.ok),
                        color = AppColors.PrimaryOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEndDatePicker = false }
                ) {
                    Text(
                        stringResource(R.string.cancel),
                        color = if (isDarkTheme) ThemeColors.getTextGrayColor(true) else Color.White
                    )
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White,
                    titleContentColor = if (isDarkTheme) Color.White else Color.Black,
                    headlineContentColor = if (isDarkTheme) Color.White else Color.Black,
                    weekdayContentColor = Color.Gray,
                    subheadContentColor = if (isDarkTheme) Color.White else Color.Black,
                    yearContentColor = if (isDarkTheme) Color.White else Color.Black,
                    currentYearContentColor = AppColors.PrimaryOrange,
                    selectedYearContentColor = if (isDarkTheme) Color.White else Color.White,
                    selectedYearContainerColor = AppColors.PrimaryOrange,
                    dayContentColor = if (isDarkTheme) Color.White else Color.Black,
                    selectedDayContentColor = if (isDarkTheme) Color.White else Color.White,
                    selectedDayContainerColor = AppColors.PrimaryOrange,
                    todayContentColor = AppColors.PrimaryOrange,
                    todayDateBorderColor = AppColors.PrimaryOrange
                )
            )
        }
    }
}
