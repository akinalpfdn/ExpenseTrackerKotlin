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
    val categoryId: String,  // Reference to Category table
    val subCategoryId: String,  // Reference to SubCategory table
    val description: String,
    val date: LocalDateTime,
    val dailyLimitAtCreation: Double,
    val monthlyLimitAtCreation: Double,
    val exchangeRate: Double? = null,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val endDate: LocalDateTime? = null,  // Tekrar eden harcamanın bitiş tarihi
    val recurrenceGroupId: String? = null  // Tekrar eden harcamalar için ortak ID
) {
    
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
        // For all expenses (including recurring ones), just check if the date matches
        // Since we now create individual records for each occurrence
        return date.toLocalDate() == targetDate.toLocalDate()
    }
}