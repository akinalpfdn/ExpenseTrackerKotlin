package com.example.expensetrackerkotlin.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerkotlin.data.*
import com.example.expensetrackerkotlin.ui.components.CategoryExpense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import kotlin.math.min

class ExpenseViewModel(
    private val preferencesManager: PreferencesManager,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()
    
    private val _selectedDate = MutableStateFlow(LocalDateTime.now())
    val selectedDate: StateFlow<LocalDateTime> = _selectedDate.asStateFlow()
    
    private val _showingOverLimitAlert = MutableStateFlow(false)
    val showingOverLimitAlert: StateFlow<Boolean> = _showingOverLimitAlert.asStateFlow()
    
    private val _editingExpenseId = MutableStateFlow<String?>(null)
    val editingExpenseId: StateFlow<String?> = _editingExpenseId.asStateFlow()
    
    // Category management
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    
    private val _subCategories = MutableStateFlow<List<SubCategory>>(emptyList())
    val subCategories: StateFlow<List<SubCategory>> = _subCategories.asStateFlow()
    
    // Settings
    var defaultCurrency by mutableStateOf("₺")
    var dailyLimit by mutableStateOf("")
    var monthlyLimit by mutableStateOf("")
    var theme by mutableStateOf("dark")

    private val _isFirstLaunch = MutableStateFlow(true)
    val isFirstLaunch: StateFlow<Boolean> = _isFirstLaunch.asStateFlow()
    
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
        viewModelScope.launch {
            preferencesManager.isFirstLaunch.collect { isFirst ->
                _isFirstLaunch.value = isFirst
            }
        }

        // Load expenses from database
        viewModelScope.launch {
            expenseRepository.allExpenses.collect { expensesList ->
                _expenses.value = expensesList
            }
        }
        
        // Load categories from database
        viewModelScope.launch {
            categoryRepository.allCategories.collect { categoriesList ->
                _categories.value = categoriesList
            }
        }
        
        // Load subcategories from database
        viewModelScope.launch {
            categoryRepository.allSubCategories.collect { subCategoriesList ->
                _subCategories.value = subCategoriesList
            }
        }
        
        // Initialize default data if needed
        viewModelScope.launch {
            categoryRepository.initializeDefaultDataIfNeeded()
        }
    }
    
    // Computed properties
    val totalSpent: Double
        get() = _expenses.value.sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
    
    private val monthlyLimitValue: Double
        get() = monthlyLimit.toDoubleOrNull() ?: 10000.0

    val isOverLimit: Boolean
        get() = totalSpent > monthlyLimitValue && monthlyLimitValue > 0
    

    
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
            
            val categoryTotals = mutableMapOf<String, Double>()
            
            selectedDayExpenses.forEach { expense ->
                categoryTotals[expense.categoryId] = categoryTotals.getOrDefault(expense.categoryId, 0.0) + expense.getAmountInDefaultCurrency(defaultCurrency)
            }
            
            return categoryTotals.mapNotNull { (categoryId, amount) ->
                val category = _categories.value.find { it.id == categoryId }
                if (category != null) {
                    CategoryExpense(
                        category = category,
                        amount = amount,
                        percentage = amount / selectedDayTotal
                    )
                } else {
                    null
                }
            }.sortedByDescending { it.amount }
        }
    
    // Track current week for infinite scrolling
    private val _currentWeekOffset = MutableStateFlow(0) // 0 = this week, -1 = previous, +1 = next
    val currentWeekOffset: StateFlow<Int> = _currentWeekOffset.asStateFlow()

    // Generate 3 weeks of data for infinite pager (previous, current, next)
    val weeklyHistoryData: StateFlow<List<List<DailyData>>> = combine(
        _selectedDate,
        _currentWeekOffset,
        expenses
    ) { selectedDate, weekOffset, allExpenses ->
        println("ViewModel Debug: Generating 3 weeks data - selectedDate: ${selectedDate.toLocalDate()}, weekOffset: $weekOffset")

        // Calculate the start of the week (Monday) for the selected date
        val dayOfWeek = selectedDate.dayOfWeek.value // 1=Monday, 7=Sunday
        val daysFromMonday = dayOfWeek - 1 // 0=Monday, 6=Sunday
        val startOfSelectedWeek = selectedDate.minusDays(daysFromMonday.toLong())

        println("ViewModel Debug: Start of selected week: ${startOfSelectedWeek.toLocalDate()}")

        // IMPORTANT: The baseWeek should be the currently displayed week, not always the selected date's week
        // weekOffset should move relative to the current displayed week
        val baseWeek = startOfSelectedWeek.plusWeeks(weekOffset.toLong())
        println("ViewModel Debug: Base week (with offset): ${baseWeek.toLocalDate()}")

        // Generate 3 weeks: previous (-1), current (0), next (+1)
        (-1..1).map { weekIndex ->
            val startOfWeek = baseWeek.plusWeeks(weekIndex.toLong())
            println("ViewModel Debug: Week $weekIndex starts at: ${startOfWeek.toLocalDate()}")

            // Generate 7 days for this week
            (0..6).map { dayOffset ->
                val date = startOfWeek.plusDays(dayOffset.toLong())
                val dayExpenses = getExpensesForDate(date)
                val totalAmount = dayExpenses.sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
                val progressAmount = dayExpenses.filter { it.recurrenceType == RecurrenceType.NONE }
                    .sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
                val expenseCount = dayExpenses.size

                val averageDailyLimit = dailyLimitValue

                DailyData(
                    date = date,
                    totalAmount = totalAmount,
                    progressAmount = progressAmount,
                    expenseCount = expenseCount,
                    dailyLimit = averageDailyLimit
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Backward compatibility - current week data
    val dailyHistoryData: StateFlow<List<DailyData>> = weeklyHistoryData.map { weeks ->
        if (weeks.isNotEmpty() && weeks.size >= 2) {
            weeks[1] // Return current week (middle week)
        } else {
            emptyList()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Methods
    fun updateSelectedDate(date: LocalDateTime) {
        val currentSelected = _selectedDate.value
        println("ViewModel Debug: updateSelectedDate called - from: ${currentSelected.toLocalDate()} to: ${date.toLocalDate()}")

        _selectedDate.value = date

        // Reset week offset when selecting a new date - let the data generation handle showing the correct week
        println("ViewModel Debug: Resetting week offset to 0 for new selected date")
        _currentWeekOffset.value = 0
    }

    fun navigateToWeek(direction: Int) {
        // direction: -1 for previous week, +1 for next week
        val oldOffset = _currentWeekOffset.value
        _currentWeekOffset.value += direction
        println("ViewModel Debug: navigateToWeek($direction) - offset: $oldOffset -> ${_currentWeekOffset.value}")

        // DON'T update selected date here - let the data generation handle showing different weeks
        // The selected date should stay the same, but we show different weeks around it
        println("ViewModel Debug: Selected date stays: ${_selectedDate.value.toLocalDate()}")
    }

    fun resetWeekOffset() {
        _currentWeekOffset.value = 0
    }
    
    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            if (expense.recurrenceType != RecurrenceType.NONE && expense.recurrenceGroupId != null) {
                // For recurring expenses, create individual records for each occurrence
                val recurringExpenses = generateRecurringExpenses(expense)
                recurringExpenses.forEach { individualExpense ->
                    expenseRepository.insertExpense(individualExpense)
                }
            } else {
                // For single expenses, just insert the expense
                expenseRepository.insertExpense(expense)
            }
            
            // Check if over limit
            if (!isOverLimit && totalSpent > monthlyLimitValue && monthlyLimitValue > 0) {
                _showingOverLimitAlert.value = true
            }
        }
    }
    
    private fun generateRecurringExpenses(baseExpense: Expense): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val endDate = baseExpense.endDate ?: LocalDateTime.now().plusYears(1)
        var currentDate = baseExpense.date
        
        while (!currentDate.isAfter(endDate)) {
            if (isRecurringExpenseActiveOnDate(baseExpense, currentDate)) {
                val individualExpense = baseExpense.copy(
                    id = UUID.randomUUID().toString(), // Each occurrence gets a unique ID
                    date = currentDate // Set the specific date for this occurrence
                )
                expenses.add(individualExpense)
            }
            currentDate = currentDate.plusDays(1)
        }
        
        return expenses
    }
    
    fun isRecurringExpenseActiveOnDate(expense: Expense, targetDate: LocalDateTime): Boolean {
        // Check if target date is before start date (exclude the start date itself)
        if (targetDate.isBefore(expense.date.toLocalDate().atStartOfDay())) {
            return false
        }
        
        // Check if target date is after end date
        if (expense.endDate != null && targetDate.isAfter(expense.endDate)) {
            return false
        }
        
        return when (expense.recurrenceType) {
            RecurrenceType.DAILY -> true
            RecurrenceType.WEEKDAYS -> {
                val dayOfWeek = targetDate.dayOfWeek.value
                dayOfWeek in 1..5 // Monday = 1, Friday = 5
            }
            RecurrenceType.WEEKLY -> {
                // Check if it's the same day of week
                val startDayOfWeek = expense.date.dayOfWeek
                val targetDayOfWeek = targetDate.dayOfWeek
                
                if (startDayOfWeek != targetDayOfWeek) {
                    return false
                }
                
                // Check if it's the same week or a future week
                val startWeek = expense.date.toLocalDate().with(java.time.DayOfWeek.MONDAY)
                val targetWeek = targetDate.toLocalDate().with(java.time.DayOfWeek.MONDAY)
                val weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(startWeek, targetWeek)
                weeksBetween >= 0
            }
            RecurrenceType.MONTHLY -> {
                val startDayOfMonth = expense.date.dayOfMonth
                val targetDayOfMonth = targetDate.dayOfMonth
                
                // Check if it's the same day of month
                if (startDayOfMonth != targetDayOfMonth) {
                    return false
                }
                
                // Check if it's the same month or a future month
                val startMonth = expense.date.toLocalDate().withDayOfMonth(1)
                val targetMonth = targetDate.toLocalDate().withDayOfMonth(1)
                val monthsBetween = java.time.temporal.ChronoUnit.MONTHS.between(startMonth, targetMonth)
                monthsBetween >= 0
            }
            RecurrenceType.NONE -> false
        }
    }
    
    // Similar to isRecurringExpenseActiveOnDate but ignores end date check
    private fun isRecurringExpenseActiveOnDateIgnoringEndDate(expense: Expense, targetDate: LocalDateTime): Boolean {
        // Check if target date is before start date (exclude the start date itself)
        if (targetDate.isBefore(expense.date.toLocalDate().atStartOfDay())) {
            return false
        }
        
        // Don't check end date - this is used when generating new expenses
        
        return when (expense.recurrenceType) {
            RecurrenceType.DAILY -> true
            RecurrenceType.WEEKDAYS -> {
                val dayOfWeek = targetDate.dayOfWeek.value
                dayOfWeek in 1..5 // Monday = 1, Friday = 5
            }
            RecurrenceType.WEEKLY -> {
                // Check if it's the same day of week
                val startDayOfWeek = expense.date.dayOfWeek
                val targetDayOfWeek = targetDate.dayOfWeek
                
                if (startDayOfWeek != targetDayOfWeek) {
                    return false
                }
                
                // Check if it's the same week or a future week
                val startWeek = expense.date.toLocalDate().with(java.time.DayOfWeek.MONDAY)
                val targetWeek = targetDate.toLocalDate().with(java.time.DayOfWeek.MONDAY)
                val weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(startWeek, targetWeek)
                weeksBetween >= 0
            }
            RecurrenceType.MONTHLY -> {
                val startDayOfMonth = expense.date.dayOfMonth
                val targetDayOfMonth = targetDate.dayOfMonth
                
                // Check if it's the same day of month
                if (startDayOfMonth != targetDayOfMonth) {
                    return false
                }
                
                // Check if it's the same month or a future month
                val startMonth = expense.date.toLocalDate().withDayOfMonth(1)
                val targetMonth = targetDate.toLocalDate().withDayOfMonth(1)
                val monthsBetween = java.time.temporal.ChronoUnit.MONTHS.between(startMonth, targetMonth)
                monthsBetween >= 0
            }
            RecurrenceType.NONE -> false
        }
    }
    
    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.updateExpense(expense)
        }
    }
    
    fun updateRecurringExpenseEndDate(
        baseExpense: Expense,
        newEndDate: LocalDateTime,
        newAmount: Double,
        newDescription: String,
        newExchangeRate: Double?
    ) {
        viewModelScope.launch {
            val oldEndDate = baseExpense.endDate ?: LocalDateTime.now().plusYears(1)
            val today = LocalDateTime.now()
            
            // Get all existing expenses with the same recurrence group ID
            val existingExpenses = _expenses.value.filter { 
                it.recurrenceGroupId == baseExpense.recurrenceGroupId 
            }.sortedBy { it.date }
            
            if (newEndDate.isBefore(oldEndDate)) {
                // End date decreased - delete expenses after new end date
                val expensesToDelete = existingExpenses.filter { 
                    it.date.isAfter(newEndDate) 
                }
                expensesToDelete.forEach { 
                    expenseRepository.deleteExpenseById(it.id) 
                }
                
                // Update remaining expenses with new values (only from today onwards)
                val expensesToUpdate = existingExpenses.filter { 
                    !it.date.isAfter(newEndDate) && 
                    it.date.toLocalDate().isAfter(selectedDate.value.toLocalDate().minusDays(1)) // Today and future
                }
                expensesToUpdate.forEach { existingExpense ->
                    val updatedExpense = existingExpense.copy(
                        amount = newAmount,
                        description = newDescription,
                        exchangeRate = newExchangeRate,
                        endDate = newEndDate
                    )
                    expenseRepository.updateExpense(updatedExpense)
                }
            } else if (newEndDate.isAfter(oldEndDate)) {
                // End date increased - update existing expenses and add new ones
                
                // Update existing expenses with new values (only from today onwards)
                val expensesToUpdate = existingExpenses.filter { 
                    it.date.toLocalDate().isAfter(selectedDate.value.toLocalDate().minusDays(1)) // Today and future
                }
                expensesToUpdate.forEach { existingExpense ->
                    val updatedExpense = existingExpense.copy(
                        amount = newAmount,
                        description = newDescription,
                        exchangeRate = newExchangeRate,
                        endDate = newEndDate
                    )
                    expenseRepository.updateExpense(updatedExpense)
                }
                
                // Generate new expenses from old end date to new end date
                val startDate = oldEndDate.plusDays(1)
                
                // Get existing dates to avoid duplicates
                val existingDates = existingExpenses.map { it.date.toLocalDate() }.toSet()
                
                var currentDate = startDate
                while (!currentDate.isAfter(newEndDate)) {
                    // Check if this date should have a recurring expense and doesn't already exist
                    if (isRecurringExpenseActiveOnDateIgnoringEndDate(baseExpense, currentDate) && 
                        !existingDates.contains(currentDate.toLocalDate())) {
                        val newExpense = baseExpense.copy(
                            id = UUID.randomUUID().toString(),
                            date = currentDate,
                            amount = newAmount,
                            description = newDescription,
                            exchangeRate = newExchangeRate,
                            endDate = newEndDate,
                            recurrenceGroupId = baseExpense.recurrenceGroupId // Keep the same group ID
                        )
                        expenseRepository.insertExpense(newExpense)
                    }
                    currentDate = currentDate.plusDays(1)
                }
            } else {
                // End date unchanged - just update existing expenses (only from today onwards)
                val expensesToUpdate = existingExpenses.filter { 
                    it.date.toLocalDate().isAfter(selectedDate.value.toLocalDate().minusDays(1)) // Today and future
                }
                expensesToUpdate.forEach { existingExpense ->
                    val updatedExpense = existingExpense.copy(
                        amount = newAmount,
                        description = newDescription,
                        exchangeRate = newExchangeRate,
                        endDate = newEndDate
                    )
                    expenseRepository.updateExpense(updatedExpense)
                }
            }
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

    suspend fun completeFirstLaunch() {
        preferencesManager.setFirstLaunchCompleted()
        _isFirstLaunch.value = false
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

    fun createCustomCategory(name: String, colorHex: String, iconName: String) {
        viewModelScope.launch {
            categoryRepository.createCustomCategory(name, colorHex, iconName)
        }
    }
    
    fun createCustomSubCategory(name: String, categoryId: String) {
        viewModelScope.launch {
            categoryRepository.createCustomSubCategory(name, categoryId)
        }
    }
    
    fun updateCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.updateCategory(category)
        }
    }
    
    fun updateSubCategory(subCategory: SubCategory) {
        viewModelScope.launch {
            categoryRepository.updateSubCategory(subCategory)
        }
    }
    
    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(category)
        }
    }
    
    fun deleteSubCategory(subCategory: SubCategory) {
        viewModelScope.launch {
            categoryRepository.deleteSubCategory(subCategory)
        }
    }
    


}