package com.example.expensetrackerkotlin.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.LocalDateTime
import java.util.*

@Entity(tableName = "financial_plans")
@TypeConverters(Converters::class)
data class FinancialPlan(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val startDate: LocalDateTime,
    val durationInMonths: Int,
    val monthlyIncome: Double,
    val manualMonthlyExpenses: Double = 0.0, // User-defined monthly expenses
    val isInflationApplied: Boolean = false,
    val inflationRate: Double = 0.0,
    val includeRecurringExpenses: Boolean = true,
    val includeAverageExpenses: Boolean = false,
    val averageMonthsToCalculate: Int = 3,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    
    val endDate: LocalDateTime
        get() = startDate.plusMonths(durationInMonths.toLong())
    
    fun getMonthlyIncomeAtMonth(monthIndex: Int): Double {
        return if (isInflationApplied && inflationRate > 0) {
            val monthlyInflationRate = inflationRate / 12 / 100
            monthlyIncome * Math.pow(1 + monthlyInflationRate, monthIndex.toDouble())
        } else {
            monthlyIncome
        }
    }
    
    fun getTotalExpectedIncome(): Double {
        return if (isInflationApplied && inflationRate > 0) {
            var total = 0.0
            for (month in 0 until durationInMonths) {
                total += getMonthlyIncomeAtMonth(month)
            }
            total
        } else {
            monthlyIncome * durationInMonths
        }
    }
    
    fun isActive(): Boolean {
        val now = LocalDateTime.now()
        return now.isAfter(startDate) && now.isBefore(endDate)
    }
    
    fun getMonthsElapsed(): Int {
        val now = LocalDateTime.now()
        return when {
            now.isBefore(startDate) -> 0
            now.isAfter(endDate) -> durationInMonths
            else -> {
                val startYear = startDate.year
                val startMonth = startDate.monthValue
                val currentYear = now.year
                val currentMonth = now.monthValue
                (currentYear - startYear) * 12 + (currentMonth - startMonth) + 1
            }
        }
    }
    
    fun getProgressPercentage(): Float {
        val elapsed = getMonthsElapsed()
        return (elapsed.toFloat() / durationInMonths.toFloat()).coerceIn(0f, 1f)
    }
}