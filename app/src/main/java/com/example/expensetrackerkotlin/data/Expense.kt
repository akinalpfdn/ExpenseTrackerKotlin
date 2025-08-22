package com.example.expensetrackerkotlin.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.LocalDateTime
import java.util.*

@Entity(tableName = "expenses")
@TypeConverters(Converters::class)
data class Expense(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val currency: String,
    val subCategory: String,
    val description: String,
    val date: LocalDateTime,
    val dailyLimitAtCreation: Double,
    val monthlyLimitAtCreation: Double,
    val exchangeRate: Double? = null
) {
    val category: ExpenseCategory
        get() = CategoryHelper.getCategoryForSubCategory(subCategory)
    
    // Convert amount to default currency for progress calculations
    fun getAmountInDefaultCurrency(defaultCurrency: String): Double {
        return if (currency == defaultCurrency || exchangeRate == null) {
            amount
        } else {
            amount * exchangeRate
        }
    }
}