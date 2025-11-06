package com.example.expensetrackerkotlin.data

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.min

data class DailyData(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDateTime,
    val totalAmount: Double,
    val progressAmount: Double,
    val expenseCount: Int,
    val dailyLimit: Double
) {
    val progressPercentage: Double
        get() {
            if (dailyLimit <= 0) return 0.0
            return min(progressAmount / dailyLimit, 1.0)
        }

    val isOverLimit: Boolean
        get() = progressAmount > dailyLimit && dailyLimit > 0



    val dayName: String
        get() {
            val formatter = DateTimeFormatter.ofPattern("E", Locale.getDefault())
            return date.format(formatter).first().toString().uppercase(Locale.getDefault())
        }

    val dayNumber: String
        get() {
            val formatter = DateTimeFormatter.ofPattern("d")
            return date.format(formatter)
        }

}