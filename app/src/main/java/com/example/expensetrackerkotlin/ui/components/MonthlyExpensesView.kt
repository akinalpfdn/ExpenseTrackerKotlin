package com.example.expensetrackerkotlin.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.expensetrackerkotlin.R
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import com.example.expensetrackerkotlin.data.Expense
import com.example.expensetrackerkotlin.ui.screens.ExpenseSortType
import com.example.expensetrackerkotlin.viewmodel.ExpenseViewModel
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun MonthlyExpensesView(
    modifier: Modifier = Modifier,
    currentMonth: YearMonth,
    expenses: List<Expense>,
    viewModel: ExpenseViewModel,
    isDarkTheme: Boolean
) {
    val categories by viewModel.categories.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()
    val editingExpenseId by viewModel.editingExpenseId.collectAsState()
    
    // Search and sorting state
    var searchText by remember { mutableStateOf("") }
    var showSortMenu by remember { mutableStateOf(false) }
    var currentSortType by remember { mutableStateOf(ExpenseSortType.TIME_NEWEST_FIRST) }
    var showSearchBar by remember { mutableStateOf(false) }
    
    // Get base expenses for the current month (including recurring expenses)
    val baseMonthlyExpenses = remember(expenses, currentMonth) {
        expenses.filter { expense ->
            val startOfMonth = currentMonth.atDay(1)
            val endOfMonth = currentMonth.atEndOfMonth()
            
            var currentDate = startOfMonth
            while (!currentDate.isAfter(endOfMonth)) {
                if (expense.isActiveOnDate(currentDate.atStartOfDay())) {
                    return@filter true
                }
                currentDate = currentDate.plusDays(1)
            }
            false
        }
    }
    
    // Filter and sort expenses
    val monthlyExpenses = remember(baseMonthlyExpenses, searchText, currentSortType, categories, subCategories) {
        var filteredExpenses = baseMonthlyExpenses
        
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
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {


        // Search and Sort controls
        if (baseMonthlyExpenses.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side controls
                Row(
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
                                imageVector = Icons.AutoMirrored.Filled.Sort,
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

                        Text(
                            text = stringResource(R.string.results_count, monthlyExpenses.size),
                            fontSize = 12.sp,
                            color = ThemeColors.getTextGrayColor(isDarkTheme)
                        )

                }
                // Month header
                Text(
                    text = currentMonth.format(
                        DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
                    ),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeColors.getTextColor(isDarkTheme),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
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
        
        // Expenses list
        if (monthlyExpenses.isEmpty()) {
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
                        stringResource(R.string.no_search_results)
                    } else {
                        stringResource(R.string.no_expenses_this_month)
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeColors.getTextColor(isDarkTheme),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (searchText.isNotBlank()) {
                        stringResource(R.string.search_no_results_description, searchText)
                    } else {
                        stringResource(R.string.add_expense_for_month)
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
                items(monthlyExpenses) { expense ->
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
                        dailyExpenseRatio = viewModel.getDailyExpenseRatio(expense),
                        defaultCurrency = viewModel.defaultCurrency,
                        isDarkTheme = isDarkTheme,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}