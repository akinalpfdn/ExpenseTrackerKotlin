package com.example.expensetrackerkotlin.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>
    
    @Query("SELECT * FROM expenses WHERE date >= :startDate AND date < :endDate ORDER BY date DESC")
    fun getExpensesForDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Expense>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)
    
    @Update
    suspend fun updateExpense(expense: Expense)
    
    @Delete
    suspend fun deleteExpense(expense: Expense)
    
    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpenseById(expenseId: String)
    
    @Query("SELECT SUM(amount) FROM expenses WHERE date >= :startDate AND date < :endDate")
    suspend fun getTotalForDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Double?
    
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    suspend fun getAllExpensesDirect(): List<Expense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<Expense>)

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()
}
