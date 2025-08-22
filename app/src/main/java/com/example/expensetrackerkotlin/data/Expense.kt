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
    val monthlyLimitAtCreation: Double
) {
    val category: ExpenseCategory
        get() = CategoryHelper.getCategoryForSubCategory(subCategory)
}