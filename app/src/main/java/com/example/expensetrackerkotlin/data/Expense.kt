package com.example.expensetrackerkotlin.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.LocalDateTime
import java.util.*

enum class RecurrenceType {
    NONE,           // Tek seferlik
    DAILY,          // Her gün
    WEEKDAYS,       // Hafta içi her gün (Pazartesi-Cuma)
    WEEKLY,         // Haftada 1 kez
    MONTHLY         // Ayda 1 kez
}

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
    val exchangeRate: Double? = null,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val endDate: LocalDateTime? = null,  // Tekrar eden harcamanın bitiş tarihi
    val recurrenceGroupId: String? = null  // Tekrar eden harcamalar için ortak ID
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
    
    // Check if this expense should be active on a given date
    fun isActiveOnDate(targetDate: LocalDateTime): Boolean {
        if (recurrenceType == RecurrenceType.NONE) {
            return date.toLocalDate() == targetDate.toLocalDate()
        }
        
        // Check if target date is before start date
        if (targetDate.isBefore(date)) {
            return false
        }
        
        // Check if target date is after end date
        if (endDate != null && targetDate.isAfter(endDate)) {
            return false
        }
        
        return when (recurrenceType) {
            RecurrenceType.DAILY -> true
            RecurrenceType.WEEKDAYS -> {
                val dayOfWeek = targetDate.dayOfWeek.value
                dayOfWeek in 1..5 // Monday = 1, Friday = 5
            }
            RecurrenceType.WEEKLY -> {
                val startWeek = date.toLocalDate().atStartOfDay()
                val targetWeek = targetDate.toLocalDate().atStartOfDay()
                val weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(startWeek, targetWeek)
                weeksBetween >= 0 && weeksBetween % 1 == 0L
            }
            RecurrenceType.MONTHLY -> {
                val startMonth = date.toLocalDate().withDayOfMonth(1)
                val targetMonth = targetDate.toLocalDate().withDayOfMonth(1)
                val monthsBetween = java.time.temporal.ChronoUnit.MONTHS.between(startMonth, targetMonth)
                monthsBetween >= 0 && monthsBetween % 1 == 0L
            }
            RecurrenceType.NONE -> false
        }
    }
}