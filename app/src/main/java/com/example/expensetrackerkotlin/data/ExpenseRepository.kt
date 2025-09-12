package com.example.expensetrackerkotlin.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    
    fun getExpensesForDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Expense>> {
        return expenseDao.getExpensesForDateRange(startDate, endDate)
    }
    
    suspend fun insertExpense(expense: Expense) {
        expenseDao.insertExpense(expense)
    }
    
    suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense)
    }
    
    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }
    
    suspend fun deleteExpenseById(expenseId: String) {
        expenseDao.deleteExpenseById(expenseId)
    }
    
    suspend fun getTotalForDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Double {
        return expenseDao.getTotalForDateRange(startDate, endDate) ?: 0.0
    }
    
    suspend fun getAllExpensesDirect(): List<Expense> {
        return expenseDao.getAllExpensesDirect()
    }
}
