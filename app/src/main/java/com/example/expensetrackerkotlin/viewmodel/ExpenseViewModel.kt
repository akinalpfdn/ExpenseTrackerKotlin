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
            val today = LocalDateTime.now()
            
            return (0..6).map { dayOffset ->
                val date = today.minusDays(dayOffset.toLong())
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
    
    private fun getSelectedDayTotal(): Double {
        return getExpensesForDate(_selectedDate.value).sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
    }
    
    private fun getExpensesForDate(date: LocalDateTime): List<Expense> {
        val startOfDay = date.truncatedTo(ChronoUnit.DAYS)
        val endOfDay = startOfDay.plusDays(1)
        
        return _expenses.value.filter { expense ->
            expense.date.isAfter(startOfDay.minusSeconds(1)) && expense.date.isBefore(endOfDay)
        }
    }
    
    fun getDailyExpenseRatio(expense: Expense): Double {
        val sameDayExpenses = getExpensesForDate(expense.date)
        val dailyTotal = sameDayExpenses.sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
        
        return if (dailyTotal > 0) {
            expense.amount / dailyTotal
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
}