package com.example.expensetrackerkotlin.data

import androidx.compose.ui.graphics.Color
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.min

data class DailyData(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDateTime,
    val totalAmount: Double,
    val expenseCount: Int,
    val dailyLimit: Double
) {
    val progressPercentage: Double
        get() {
            if (dailyLimit <= 0) return 0.0
            return min(totalAmount / dailyLimit, 1.0)
        }

    val isOverLimit: Boolean
        get() = totalAmount > dailyLimit && dailyLimit > 0

    val progressColors: List<Color>
        get() = when {
            isOverLimit -> listOf(Color.Red, Color.Red, Color.Red, Color.Red)
            progressPercentage < 0.3 -> listOf(Color.Green, Color.Green, Color.Green, Color.Green)
            progressPercentage < 0.6 -> listOf(Color.Green, Color.Green, Color.Yellow, Color.Yellow)
            progressPercentage < 0.9 -> listOf(Color.Green, Color.Yellow, Color(0xFFFFA500), Color(0xFFFFA500))
            else -> listOf(Color.Green, Color.Yellow, Color(0xFFFFA500), Color.Red)
        }

    val dayName: String
        get() {
            val formatter = DateTimeFormatter.ofPattern("E", Locale.forLanguageTag("tr"))
            return date.format(formatter).first().toString().uppercase()
        }

    val dayNumber: String
        get() {
            val formatter = DateTimeFormatter.ofPattern("d")
            return date.format(formatter)
        }

    val isToday: Boolean
        get() {
            val today = LocalDateTime.now()
            return date.toLocalDate() == today.toLocalDate()
        }
}