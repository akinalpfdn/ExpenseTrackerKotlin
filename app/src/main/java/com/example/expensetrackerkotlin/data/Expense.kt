package com.example.expensetrackerkotlin.data

import java.time.LocalDateTime
import java.util.*

data class Expense(
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