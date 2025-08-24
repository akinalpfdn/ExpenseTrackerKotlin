package com.example.expensetrackerkotlin.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerkotlin.data.*
import com.example.expensetrackerkotlin.ui.components.CategoryExpense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.min

class ExpenseViewModel(
    private val preferencesManager: PreferencesManager,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {
    
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()
    
    private val _selectedDate = MutableStateFlow(LocalDateTime.now())
    val selectedDate: StateFlow<LocalDateTime> = _selectedDate.asStateFlow()
    
    private val _showingOverLimitAlert = MutableStateFlow(false)
    val showingOverLimitAlert: StateFlow<Boolean> = _showingOverLimitAlert.asStateFlow()
    
    private val _editingExpenseId = MutableStateFlow<String?>(null)
    val editingExpenseId: StateFlow<String?> = _editingExpenseId.asStateFlow()
    
    // Settings
    var defaultCurrency by mutableStateOf("₺")
    var dailyLimit by mutableStateOf("")
    var monthlyLimit by mutableStateOf("")
    var theme by mutableStateOf("dark")
    
    init {
        // Load preferences
        viewModelScope.launch {
            preferencesManager.defaultCurrency.collect { currency ->
                defaultCurrency = currency
            }
        }
        viewModelScope.launch {
            preferencesManager.dailyLimit.collect { limit ->
                dailyLimit = limit
            }
        }
        viewModelScope.launch {
            preferencesManager.monthlyLimit.collect { limit ->
                monthlyLimit = limit
            }
        }
        viewModelScope.launch {
            preferencesManager.theme.collect { themeValue ->
                theme = themeValue
            }
        }
        
        // Load expenses from database
        viewModelScope.launch {
            expenseRepository.allExpenses.collect { expensesList ->
                _expenses.value = expensesList
            }
        }
    }
    
    // Computed properties
    val totalSpent: Double
        get() = _expenses.value.sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
    
    private val monthlyLimitValue: Double
        get() = monthlyLimit.toDoubleOrNull() ?: 10000.0
    
    val progressPercentage: Double
        get() {
            if (monthlyLimitValue <= 0) return 0.0
            return min(totalSpent / monthlyLimitValue, 1.0)
        }
    
    val isOverLimit: Boolean
        get() = totalSpent > monthlyLimitValue && monthlyLimitValue > 0
    
    val progressColors: List<Color>
        get() = when {
            isOverLimit -> listOf(Color.Red, Color.Red, Color.Red, Color.Red)
            progressPercentage < 0.3 -> listOf(Color.Green, Color.Green, Color.Green, Color.Green)
            progressPercentage < 0.6 -> listOf(Color.Green, Color.Green, Color.Yellow, Color.Yellow)
            progressPercentage < 0.9 -> listOf(Color.Green, Color.Yellow, Color(0xFFFFA500), Color(0xFFFFA500))
            else -> listOf(Color.Green, Color.Yellow, Color(0xFFFFA500), Color.Red)
        }
    
    private val dailyLimitValue: Double
        get() = dailyLimit.toDoubleOrNull() ?: 0.0
    
    val dailyProgressPercentage: Double
        get() {
            if (dailyLimitValue <= 0) return 0.0
            val selectedDayTotal = getSelectedDayTotal()
            return min(selectedDayTotal / dailyLimitValue, 1.0)
        }
    
    val isOverDailyLimit: Boolean
        get() {
            val selectedDayTotal = getSelectedDayTotal()
            return selectedDayTotal > dailyLimitValue && dailyLimitValue > 0
        }
    
    val dailyExpensesByCategory: List<CategoryExpense>
        get() {
            val selectedDayExpenses = getExpensesForDate(_selectedDate.value)
            val selectedDayTotal = selectedDayExpenses.sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
            
            if (selectedDayTotal <= 0) return emptyList()
            
            val categoryTotals = mutableMapOf<ExpenseCategory, Double>()
            
            selectedDayExpenses.forEach { expense ->
                categoryTotals[expense.category] = categoryTotals.getOrDefault(expense.category, 0.0) + expense.amount
            }
            
            return categoryTotals.map { (category, amount) ->
                CategoryExpense(
                    category = category,
                    amount = amount,
                    percentage = amount / selectedDayTotal
                )
            }.sortedByDescending { it.amount }
        }
    
    val dailyHistoryData: List<DailyData>
        get() {
            val selectedDate = _selectedDate.value
            
            return (0..6).map { dayOffset ->
                val date = selectedDate.minusDays(dayOffset.toLong())
                val dayExpenses = getExpensesForDate(date)
                val totalAmount = dayExpenses.sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
                val expenseCount = dayExpenses.size
                
                val averageDailyLimit = if (dayExpenses.isEmpty()) {
                    dailyLimitValue
                } else {
                    dayExpenses.sumOf { it.dailyLimitAtCreation } / dayExpenses.size
                }
                
                DailyData(
                    date = date,
                    totalAmount = totalAmount,
                    expenseCount = expenseCount,
                    dailyLimit = averageDailyLimit
                )
            }.reversed()
        }
    
    // Methods
    fun updateSelectedDate(date: LocalDateTime) {
        _selectedDate.value = date
    }
    
    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.insertExpense(expense)
            
            // Check if over limit
            if (!isOverLimit && totalSpent > monthlyLimitValue && monthlyLimitValue > 0) {
                _showingOverLimitAlert.value = true
            }
        }
    }
    
    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.updateExpense(expense)
        }
    }
    
    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            expenseRepository.deleteExpenseById(expenseId)
        }
    }
    
    fun setEditingExpenseId(id: String?) {
        _editingExpenseId.value = id
    }
    
    fun dismissOverLimitAlert() {
        _showingOverLimitAlert.value = false
    }
    
    fun getSelectedDayTotal(): Double {
        return getExpensesForDate(_selectedDate.value).sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
    }
    
    private fun getExpensesForDate(date: LocalDateTime): List<Expense> {
        return _expenses.value.filter { expense ->
            expense.isActiveOnDate(date)
        }
    }
    
    // Get all expenses that are active on any date (for display in list)
    private fun getAllActiveExpenses(): List<Expense> {
        val today = LocalDateTime.now()
        val endDate = today.plusYears(1) // Show expenses for next year
        
        return _expenses.value.filter { expense ->
            if (expense.recurrenceType == RecurrenceType.NONE) {
                // Single expense: show if it's today or in the future
                expense.date.toLocalDate() >= today.toLocalDate()
            } else {
                // Recurring expense: show if it's active on any date from today onwards
                var currentDate = today
                while (!currentDate.isAfter(endDate)) {
                    if (expense.isActiveOnDate(currentDate)) {
                        return@filter true
                    }
                    currentDate = currentDate.plusDays(1)
                }
                false
            }
        }
    }
    
    fun getDailyExpenseRatio(expense: Expense): Double {
        val sameDayExpenses = getExpensesForDate(expense.date)
        val dailyTotal = sameDayExpenses.sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
        
        return if (dailyTotal > 0) {
            expense.getAmountInDefaultCurrency(defaultCurrency) / dailyTotal
        } else {
            1.0
        }
    }
    
    // Settings methods
    suspend fun updateDefaultCurrency(currency: String) {
        preferencesManager.setDefaultCurrency(currency)
        defaultCurrency = currency
    }
    
    suspend fun updateDailyLimit(limit: String) {
        preferencesManager.setDailyLimit(limit)
        dailyLimit = limit
    }
    
    suspend fun updateMonthlyLimit(limit: String) {
        preferencesManager.setMonthlyLimit(limit)
        monthlyLimit = limit
    }

    suspend fun updateTheme(theme: String) {
        preferencesManager.setTheme(theme)
        this.theme = theme
    }
    
    // Monthly progress methods for specific month
    fun getMonthlyTotal(yearMonth: java.time.YearMonth): Double {
        val startOfMonth = yearMonth.atDay(1).atStartOfDay()
        val endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59)
        
        return _expenses.value
            .filter { expense ->
                // Check if this expense is active on any day in this month
                var currentDate = startOfMonth
                while (!currentDate.isAfter(endOfMonth)) {
                    if (expense.isActiveOnDate(currentDate)) {
                        return@filter true
                    }
                    currentDate = currentDate.plusDays(1)
                }
                false
            }
            .sumOf { expense ->
                // Calculate total for this expense in this month
                var total = 0.0
                var currentDate = startOfMonth
                while (!currentDate.isAfter(endOfMonth)) {
                    if (expense.isActiveOnDate(currentDate)) {
                        total += expense.getAmountInDefaultCurrency(defaultCurrency)
                    }
                    currentDate = currentDate.plusDays(1)
                }
                total
            }
    }
    
    fun getMonthlyProgressPercentage(yearMonth: java.time.YearMonth): Double {
        val monthlyTotal = getMonthlyTotal(yearMonth)
        if (monthlyLimitValue <= 0) return 0.0
        return min(monthlyTotal / monthlyLimitValue, 1.0)
    }
    
    fun isMonthlyOverLimit(yearMonth: java.time.YearMonth): Boolean {
        val monthlyTotal = getMonthlyTotal(yearMonth)
        return monthlyTotal > monthlyLimitValue && monthlyLimitValue > 0
    }
    
    fun getMonthlyProgressColors(yearMonth: java.time.YearMonth): List<Color> {
        val progressPercentage = getMonthlyProgressPercentage(yearMonth)
        val isOverLimit = isMonthlyOverLimit(yearMonth)
        
        return when {
            isOverLimit -> listOf(Color.Red, Color.Red, Color.Red, Color.Red)
            progressPercentage < 0.3 -> listOf(Color.Green, Color.Green, Color.Green, Color.Green)
            progressPercentage < 0.6 -> listOf(Color.Green, Color.Green, Color.Yellow, Color.Yellow)
            progressPercentage < 0.9 -> listOf(Color.Green, Color.Yellow, Color(0xFFFFA500), Color(0xFFFFA500))
            else -> listOf(Color.Green, Color.Yellow, Color(0xFFFFA500), Color.Red)
        }
    }
}