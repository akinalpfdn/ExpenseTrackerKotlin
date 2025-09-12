package com.example.expensetrackerkotlin.utils

import com.example.expensetrackerkotlin.data.FinancialPlan
import com.example.expensetrackerkotlin.data.PlanMonthlyBreakdown
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object PlanningUtils {
    
    /**
     * Formats a plan duration in a human-readable Turkish format
     */
    fun formatPlanDuration(durationInMonths: Int): String {
        return when {
            durationInMonths < 12 -> "$durationInMonths ay"
            durationInMonths == 12 -> "1 yıl"
            durationInMonths % 12 == 0 -> "${durationInMonths / 12} yıl"
            else -> {
                val years = durationInMonths / 12
                val months = durationInMonths % 12
                "$years yıl $months ay"
            }
        }
    }
    
    /**
     * Formats a plan's date range in Turkish
     */
    fun formatPlanDateRange(startDate: LocalDateTime, endDate: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("tr"))
        return "${startDate.format(formatter)} - ${endDate.format(formatter)}"
    }
    
    /**
     * Gets plan status text in Turkish
     */
    fun getPlanStatusText(plan: FinancialPlan): String {
        val now = LocalDateTime.now()
        return when {
            now.isBefore(plan.startDate) -> "Başlamadı"
            now.isAfter(plan.endDate) -> "Tamamlandı"
            else -> "Aktif"
        }
    }
    
    /**
     * Gets plan status color based on current state
     */
    fun getPlanStatusColor(plan: FinancialPlan): androidx.compose.ui.graphics.Color {
        val now = LocalDateTime.now()
        return when {
            now.isBefore(plan.startDate) -> androidx.compose.ui.graphics.Color(0xFF8E8E93) // Gray
            now.isAfter(plan.endDate) -> androidx.compose.ui.graphics.Color(0xFF34C759) // Green
            else -> androidx.compose.ui.graphics.Color(0xFF007AFF) // Blue
        }
    }
    
    /**
     * Calculates the total projected savings for a plan
     */
    fun calculateTotalProjectedSavings(breakdowns: List<PlanMonthlyBreakdown>): Double {
        return breakdowns.lastOrNull()?.cumulativeNet ?: 0.0
    }
    
    /**
     * Calculates average monthly savings rate for a plan
     */
    fun calculateAverageSavingsRate(breakdowns: List<PlanMonthlyBreakdown>): Float {
        if (breakdowns.isEmpty()) return 0f
        
        val averageRate = breakdowns.map { it.getSavingsRate() }.average()
        return averageRate.toFloat()
    }
    
    /**
     * Gets month name in Turkish for a given month index from plan start
     */
    fun getMonthName(plan: FinancialPlan, monthIndex: Int): String {
        val targetDate = plan.startDate.plusMonths(monthIndex.toLong())
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("tr"))
        return targetDate.format(formatter)
    }
    
    /**
     * Determines if a plan's projection is optimistic, realistic, or conservative
     */
    fun getPlanProjectionType(averageSavingsRate: Float): String {
        return when {
            averageSavingsRate >= 0.3f -> "İyimser"
            averageSavingsRate >= 0.1f -> "Gerçekçi"
            averageSavingsRate >= 0.0f -> "Muhafazakar"
            else -> "Risk Altında"
        }
    }
    
    /**
     * Validates plan input parameters
     */
    fun validatePlanInput(
        name: String,
        monthlyIncome: Double,
        durationInMonths: Int,
        inflationRate: Double?
    ): PlanValidationResult {
        val errors = mutableListOf<String>()
        
        if (name.isBlank()) {
            errors.add("Plan adı boş olamaz")
        }
        
        if (monthlyIncome <= 0) {
            errors.add("Aylık gelir 0'dan büyük olmalıdır")
        }
        
        if (durationInMonths <= 0) {
            errors.add("Plan süresi 0'dan büyük olmalıdır")
        }
        
        if (durationInMonths > 120) { // 10 years max
            errors.add("Plan süresi 10 yıldan fazla olamaz")
        }
        
        if (inflationRate != null && (inflationRate < -50 || inflationRate > 100)) {
            errors.add("Enflasyon oranı -50% ile 100% arasında olmalıdır")
        }
        
        return PlanValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Generates suggested plan durations
     */
    fun getSuggestedPlanDurations(): List<PlanDurationOption> {
        return listOf(
            PlanDurationOption(3, "3 Ay", "Kısa Vadeli"),
            PlanDurationOption(6, "6 Ay", "Kısa Vadeli"),
            PlanDurationOption(12, "1 Yıl", "Orta Vadeli"),
            PlanDurationOption(18, "1.5 Yıl", "Orta Vadeli"),
            PlanDurationOption(24, "2 Yıl", "Orta Vadeli"),
            PlanDurationOption(36, "3 Yıl", "Uzun Vadeli"),
            PlanDurationOption(60, "5 Yıl", "Uzun Vadeli")
        )
    }
}

data class PlanValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)

data class PlanDurationOption(
    val months: Int,
    val displayText: String,
    val category: String
)