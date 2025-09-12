package com.example.expensetrackerkotlin.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.YearMonth

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
    
    private suspend fun generatePlanBreakdowns(planId: String) {
        val plan = planDao.getPlan(planId) ?: return
        
        // Use manual expenses if provided, otherwise use system data
        val baseExpenses = if (plan.manualMonthlyExpenses > 0) {
            plan.manualMonthlyExpenses
        } else {
            var systemExpenses = 0.0
            
            // Add fixed expenses if enabled
            if (plan.includeRecurringExpenses) {
                systemExpenses += getMonthlyFixedExpenses()
            }
            
            // Add average expenses if enabled
            if (plan.includeAverageExpenses) {
                systemExpenses += getAverageMonthlyExpenses(plan.averageMonthsToCalculate)
            }
            
            systemExpenses
        }
        
        val breakdowns = mutableListOf<PlanMonthlyBreakdown>()
        var cumulativeNet = 0.0
        
        for (monthIndex in 0 until plan.durationInMonths) {
            val projectedIncome = plan.getMonthlyIncomeAtMonth(monthIndex)
            
            // Apply inflation to expenses if enabled
            val adjustedExpenses = if (plan.isInflationApplied && plan.inflationRate > 0) {
                val monthlyInflationRate = plan.inflationRate / 12 / 100
                baseExpenses * Math.pow(1 + monthlyInflationRate, monthIndex.toDouble())
            } else {
                baseExpenses
            }
            
            val netAmount = projectedIncome - adjustedExpenses
            cumulativeNet += netAmount
            
            val breakdown = PlanMonthlyBreakdown(
                planId = planId,
                monthIndex = monthIndex,
                projectedIncome = projectedIncome,
                fixedExpenses = if (plan.manualMonthlyExpenses > 0) 0.0 else adjustedExpenses, // Keep for backward compatibility
                averageExpenses = if (plan.manualMonthlyExpenses > 0) adjustedExpenses else 0.0, // Use averageExpenses for manual input
                totalProjectedExpenses = adjustedExpenses,
                netAmount = netAmount,
                cumulativeNet = cumulativeNet
            )
            
            breakdowns.add(breakdown)
        }
        
        planDao.insertBreakdowns(breakdowns)
    }
    
    private suspend fun getMonthlyFixedExpenses(): Double {
        val allExpenses = expenseRepository.getAllExpensesDirect()
        return allExpenses
            .filter { it.recurrenceType == RecurrenceType.MONTHLY }
            .sumOf { it.amount }
    }
    
    private suspend fun getAverageMonthlyExpenses(monthsToCalculate: Int): Double {
        val endDate = LocalDateTime.now()
        val startDate = endDate.minusMonths(monthsToCalculate.toLong())
        
        val allExpenses = expenseRepository.getAllExpensesDirect()
        val recentExpenses = allExpenses.filter { expense ->
            expense.date.isAfter(startDate) && 
            expense.date.isBefore(endDate) &&
            expense.recurrenceType == RecurrenceType.NONE
        }
        
        val totalSpent = recentExpenses.sumOf { it.amount }
        return totalSpent / monthsToCalculate
    }
    
    fun getPlanBreakdownsFlow(planId: String): Flow<List<PlanMonthlyBreakdown>> {
        return planDao.getPlanBreakdownsFlow(planId)
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
}

data class PlanCurrentPosition(
    val planId: String,
    val monthsElapsed: Int,
    val expectedCumulativeNet: Double,
    val actualCumulativeNet: Double,
    val variance: Double,
    val isOnTrack: Boolean
)