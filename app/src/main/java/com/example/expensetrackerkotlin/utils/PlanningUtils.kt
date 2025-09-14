package com.example.expensetrackerkotlin.utils

import android.content.Context
import com.example.expensetrackerkotlin.R
import com.example.expensetrackerkotlin.data.FinancialPlan
import com.example.expensetrackerkotlin.data.PlanMonthlyBreakdown
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object PlanningUtils {
    

    
    /**
     * Formats a plan's date range in Turkish
     */
    fun formatPlanDateRange(startDate: LocalDateTime, endDate: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.forLanguageTag("tr"))
        return "${startDate.format(formatter)} - ${endDate.format(formatter)}"
    }
    
    /**
     * Gets plan status text
     */
    fun getPlanStatusText(plan: FinancialPlan, context: Context): String {
        val now = LocalDateTime.now()
        return when {
            now.isBefore(plan.startDate) -> context.getString(R.string.plan_status_not_started)
            now.isAfter(plan.endDate) -> context.getString(R.string.plan_status_completed)
            else -> context.getString(R.string.plan_status_active)
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
     * Gets month name in Turkish for a given month index from plan start
     */
    fun getMonthName(plan: FinancialPlan, monthIndex: Int): String {
        val targetDate = plan.startDate.plusMonths(monthIndex.toLong())
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("tr"))
        return targetDate.format(formatter)
    }
    

    
    /**
     * Validates plan input parameters
     */
    fun validatePlanInput(
        name: String,
        monthlyIncome: Double,
        durationInMonths: Int,
        inflationRate: Double?,
        context: Context
    ): PlanValidationResult {
        val errors = mutableListOf<String>()

        if (name.isBlank()) {
            errors.add(context.getString(R.string.validation_plan_name_empty))
        }

        if (monthlyIncome <= 0) {
            errors.add(context.getString(R.string.validation_monthly_income_positive))
        }

        if (durationInMonths <= 0) {
            errors.add(context.getString(R.string.validation_duration_positive))
        }

        if (durationInMonths > 120) { // 10 years max
            errors.add(context.getString(R.string.validation_duration_max_10_years))
        }

        if (inflationRate != null && (inflationRate < -50 || inflationRate > 100)) {
            errors.add(context.getString(R.string.validation_inflation_rate_range))
        }

        return PlanValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Generates suggested plan durations
     */
    fun getSuggestedPlanDurations(context: Context): List<PlanDurationOption> {
        return listOf(
            PlanDurationOption(3, context.getString(R.string.duration_3_months)),
            PlanDurationOption(6, context.getString(R.string.duration_6_months)),
            PlanDurationOption(12, context.getString(R.string.duration_1_year)),
            PlanDurationOption(18, context.getString(R.string.duration_1_5_years)),
            PlanDurationOption(24, context.getString(R.string.duration_2_years)),
            PlanDurationOption(36, context.getString(R.string.duration_3_years)),
            PlanDurationOption(60, context.getString(R.string.duration_5_years))
        )
    }
}

data class PlanValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)

data class PlanDurationOption(
    val months: Int,
    val displayText: String
)