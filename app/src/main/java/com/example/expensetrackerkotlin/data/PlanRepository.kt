package com.example.expensetrackerkotlin.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.YearMonth
import kotlin.math.pow

class PlanRepository(
    private val planDao: PlanDao,
    private val expenseRepository: ExpenseRepository
) {
    
    val allPlans: Flow<List<FinancialPlan>> = planDao.getAllPlans()
    val allPlansWithBreakdowns: Flow<List<PlanWithBreakdowns>> = planDao.getAllPlansWithBreakdowns()
    
    suspend fun getPlan(planId: String): FinancialPlan? {
        return planDao.getPlan(planId)
    }
    
    suspend fun getPlanWithBreakdowns(planId: String): PlanWithBreakdowns? {
        return planDao.getPlanWithBreakdowns(planId)
    }
    
    suspend fun insertPlan(plan: FinancialPlan) {
        planDao.insertPlan(plan)
        generatePlanBreakdowns(plan.id)
    }
    
    suspend fun updatePlan(plan: FinancialPlan) {
        planDao.updatePlan(plan.copy(updatedAt = LocalDateTime.now()))
        regeneratePlanBreakdowns(plan.id)
    }
    
    suspend fun deletePlan(planId: String) {
        planDao.deletePlanById(planId)
    }
    
    suspend fun regeneratePlanBreakdowns(planId: String) {
        planDao.deleteBreakdownsForPlan(planId)
        generatePlanBreakdowns(planId)
    }
    
    suspend fun updateBreakdown(updatedBreakdown: PlanMonthlyBreakdown) {
        planDao.updateBreakdown(updatedBreakdown)
    }
    
    suspend fun recalculateCumulativeAmounts(planId: String) {
        val breakdowns = planDao.getPlanBreakdowns(planId).sortedBy { it.monthIndex }
        
        var cumulativeNet = 0.0
        val updatedBreakdowns = breakdowns.map { breakdown ->
            cumulativeNet += breakdown.netAmount
            breakdown.copy(cumulativeNet = cumulativeNet)
        }
        
        // Update all breakdowns with new cumulative amounts
        updatedBreakdowns.forEach { breakdown ->
            planDao.updateBreakdown(breakdown)
        }
    }
    
    private suspend fun generatePlanBreakdowns(planId: String) {
        val plan = planDao.getPlan(planId) ?: return
        
        val breakdowns = mutableListOf<PlanMonthlyBreakdown>()
        var cumulativeNet = 0.0

        val allExpenses =
            expenseRepository.getAllExpensesDirect()

        
        for (monthIndex in 0 until plan.durationInMonths) {
            val projectedIncome = plan.getMonthlyIncomeAtMonth(monthIndex)
            
            // Calculate month-specific expenses
            val baseExpenses = if (plan.useAppExpenseData) {
                // Calculate month date for this breakdown
                val monthDate = plan.startDate.plusMonths(monthIndex.toLong())

                // Get recurring expenses for this specific month
                val recurringExpenses = getRecurringExpensesForMonth(allExpenses, monthDate, plan.defaultCurrency)

                // Get average of last 3 months one-time expenses
                val averageOneTimeExpenses = getAverageOneTimeExpenses()

                recurringExpenses + averageOneTimeExpenses
            } else {
                // Use manual expenses
                plan.manualMonthlyExpenses
            }
            
            // Apply inflation to expenses if enabled
            val adjustedExpenses = if (plan.isInflationApplied && plan.inflationRate > 0) {
                val monthlyInflationRate = plan.inflationRate / 12 / 100
                baseExpenses * (1 + monthlyInflationRate).pow(monthIndex.toDouble())
            } else {
                baseExpenses
            }
            
            val netAmount = projectedIncome - adjustedExpenses

            // Calculate interest earned on positive cumulative balance
            val interestEarned = if (plan.isInterestApplied && cumulativeNet > 0 && plan.interestRate > 0) {
                when (plan.interestType) {
                    InterestType.SIMPLE -> {
                        // Simple interest: (Principal * Rate * Time) / 12 for monthly calculation
                        // Use current balance as principal for each month
                        val annualRate = plan.interestRate / 100
                        val monthlySimpleRate = annualRate / 12
                        cumulativeNet * monthlySimpleRate
                    }
                    InterestType.COMPOUND -> {
                        // Compound interest: (1 + r)^(1/12) - 1 for true monthly compounding
                        val annualRate = plan.interestRate / 100
                        val monthlyCompoundRate = (1 + annualRate).pow(1.0 / 12.0) - 1
                        cumulativeNet * monthlyCompoundRate
                    }
                }
            } else {
                0.0
            }

            // Update cumulative net with this month's net amount plus any interest earned
            cumulativeNet += netAmount + interestEarned

            val breakdown = PlanMonthlyBreakdown(
                planId = planId,
                monthIndex = monthIndex,
                projectedIncome = projectedIncome,
                fixedExpenses = if (plan.manualMonthlyExpenses > 0) 0.0 else adjustedExpenses, // Keep for backward compatibility
                averageExpenses = if (plan.manualMonthlyExpenses > 0) adjustedExpenses else 0.0, // Use averageExpenses for manual input
                totalProjectedExpenses = adjustedExpenses,
                netAmount = netAmount,
                interestEarned = interestEarned,
                cumulativeNet = cumulativeNet
            )
            
            breakdowns.add(breakdown)
        }
        
        planDao.insertBreakdowns(breakdowns)
    }
    
    private fun getRecurringExpensesForMonth(
        allExpenses: List<Expense>,
        monthDate: LocalDateTime,
        defaultCurrency: String
    ): Double {
        // Filter recurring expenses that are active for this specific month
        // Similar to AnalysisScreen pattern
        val selectedMonth= YearMonth.of(monthDate.year,monthDate.month);

        return allExpenses.filter { expense ->
            expense.recurrenceType != RecurrenceType.NONE
                    && expense.date<selectedMonth.atEndOfMonth().atStartOfDay().plusDays(1)
                    &&  selectedMonth.atEndOfMonth().plusMonths(-1).atStartOfDay().plusDays(1)<=expense.date
        }.sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }


    }
    

    
    private suspend fun getAverageOneTimeExpenses(): Double {
        val endDate = LocalDateTime.now()
        val startDate = endDate.minusMonths(3)

        val allExpenses = expenseRepository.getAllExpensesDirect()
        val oneTimeExpenses = allExpenses.filter { expense ->
            expense.date.isAfter(startDate) &&
            expense.date.isBefore(endDate) &&
            expense.recurrenceType == RecurrenceType.NONE
        }

        val totalSpent = oneTimeExpenses.sumOf { it.amount }
        return totalSpent / 3
    }


    
    suspend fun getCurrentFinancialPosition(planId: String): PlanCurrentPosition? {
        val planWithBreakdowns = getPlanWithBreakdowns(planId) ?: return null
        val plan = planWithBreakdowns.plan
        
        if (!plan.isActive()) return null
        
        val monthsElapsed = plan.getMonthsElapsed()
        val currentBreakdown = planWithBreakdowns.breakdowns.getOrNull(monthsElapsed - 1)
        
        val expectedCumulativeNet = currentBreakdown?.cumulativeNet ?: 0.0
        
        // Calculate actual spending for elapsed months
        val actualExpenses = getActualExpensesForPlan(plan, monthsElapsed)
        val actualIncome = plan.monthlyIncome * monthsElapsed // Simplified - assumes consistent income
        val actualNet = actualIncome - actualExpenses
        
        return PlanCurrentPosition(
            planId = planId,
            monthsElapsed = monthsElapsed,
            expectedCumulativeNet = expectedCumulativeNet,
            actualCumulativeNet = actualNet,
            variance = actualNet - expectedCumulativeNet,
            isOnTrack = actualNet >= expectedCumulativeNet * 0.9 // 90% tolerance
        )
    }
    
    private suspend fun getActualExpensesForPlan(plan: FinancialPlan, monthsElapsed: Int): Double {
        val planStartDate = plan.startDate
        val endDate = planStartDate.plusMonths(monthsElapsed.toLong())
        
        val allExpenses = expenseRepository.getAllExpensesDirect()
        return allExpenses.filter { expense ->
            expense.date.isAfter(planStartDate) && expense.date.isBefore(endDate)
        }.sumOf { it.amount }
    }

    /**
     * Updates expense data for current and future months while preserving past months
     * Only recalculates if plan uses app expense data
     */
    suspend fun updateExpenseData(planId: String) {
        val plan = planDao.getPlan(planId) ?: return

        // Only update if plan uses app expense data
        if (!plan.useAppExpenseData) return

        val currentMonthIndex = plan.getMonthsElapsed()
        val existingBreakdowns = planDao.getPlanBreakdowns(planId)

        // Get all expenses for calculations
        val allExpenses = expenseRepository.getAllExpensesDirect()

        val updatedBreakdowns = mutableListOf<PlanMonthlyBreakdown>()
        var cumulativeNet = 0.0

        for (monthIndex in 0 until plan.durationInMonths) {
            val existingBreakdown = existingBreakdowns.find { it.monthIndex == monthIndex }

            if (monthIndex < currentMonthIndex && existingBreakdown != null) {
                // Keep past months unchanged, but track cumulative net
                updatedBreakdowns.add(existingBreakdown)
                cumulativeNet = existingBreakdown.cumulativeNet
            } else {
                // Recalculate current and future months
                val projectedIncome = plan.getMonthlyIncomeAtMonth(monthIndex)


                // Calculate month-specific expenses

                    // Calculate month date for this breakdown
                    val monthDate = plan.startDate.plusMonths(monthIndex.toLong())

                    // Get recurring expenses for this specific month
                    val recurringExpenses = getRecurringExpensesForMonth(allExpenses, monthDate, plan.defaultCurrency)

                    // Get average of last 3 months one-time expenses
                    val averageOneTimeExpenses = getAverageOneTimeExpenses()

                val baseExpenses =  recurringExpenses + averageOneTimeExpenses



                // Apply inflation if enabled
                val adjustedExpenses = if (plan.isInflationApplied && plan.inflationRate > 0) {
                    val monthlyInflationRate = plan.inflationRate / 12 / 100
                    baseExpenses * (1 + monthlyInflationRate).pow(monthIndex.toDouble())
                } else {
                    baseExpenses
                }

                val netAmount = projectedIncome - adjustedExpenses

                // Calculate interest earned on positive cumulative balance
                val interestEarned = if (plan.isInterestApplied && cumulativeNet > 0 && plan.interestRate > 0) {
                    when (plan.interestType) {
                        InterestType.SIMPLE -> {
                            val annualRate = plan.interestRate / 100
                            val monthlySimpleRate = annualRate / 12
                            cumulativeNet * monthlySimpleRate
                        }
                        InterestType.COMPOUND -> {
                            val annualRate = plan.interestRate / 100
                            val monthlyCompoundRate = (1 + annualRate).pow(1.0 / 12.0) - 1
                            cumulativeNet * monthlyCompoundRate
                        }
                    }
                } else {
                    0.0
                }

                // Update cumulative net with this month's net amount plus any interest earned
                cumulativeNet += netAmount + interestEarned

                val updatedBreakdown = PlanMonthlyBreakdown(
                    id = existingBreakdown?.id ?: java.util.UUID.randomUUID().toString(),
                    planId = planId,
                    monthIndex = monthIndex,
                    projectedIncome = projectedIncome,
                    fixedExpenses = 0.0, // Keep for backward compatibility
                    averageExpenses = adjustedExpenses, // Use averageExpenses for app data
                    totalProjectedExpenses = adjustedExpenses,
                    netAmount = netAmount,
                    interestEarned = interestEarned,
                    cumulativeNet = cumulativeNet
                )

                updatedBreakdowns.add(updatedBreakdown)
            }
        }

        // Update all breakdowns
        planDao.deleteBreakdownsForPlan(planId)
        planDao.insertBreakdowns(updatedBreakdowns)

        // Update plan timestamp
        planDao.updatePlan(plan.copy(updatedAt = LocalDateTime.now()))
    }
}

data class PlanCurrentPosition(
    val planId: String,
    val monthsElapsed: Int,
    val expectedCumulativeNet: Double,
    val actualCumulativeNet: Double,
    val variance: Double,
    val isOnTrack: Boolean
)