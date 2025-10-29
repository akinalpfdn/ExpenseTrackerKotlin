package com.example.expensetrackerkotlin.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    
    // Financial Plans
    @Query("SELECT * FROM financial_plans ORDER BY updatedAt DESC")
    fun getAllPlans(): Flow<List<FinancialPlan>>
    
    @Query("SELECT * FROM financial_plans WHERE id = :planId")
    suspend fun getPlan(planId: String): FinancialPlan?
    
    @Insert
    suspend fun insertPlan(plan: FinancialPlan)
    
    @Update
    suspend fun updatePlan(plan: FinancialPlan)
    
    @Delete
    suspend fun deletePlan(plan: FinancialPlan)
    
    @Query("DELETE FROM financial_plans WHERE id = :planId")
    suspend fun deletePlanById(planId: String)
    
    // Plan Monthly Breakdowns
    @Query("SELECT * FROM plan_monthly_breakdowns WHERE planId = :planId ORDER BY monthIndex ASC")
    suspend fun getPlanBreakdowns(planId: String): List<PlanMonthlyBreakdown>
    
    @Query("SELECT * FROM plan_monthly_breakdowns WHERE planId = :planId ORDER BY monthIndex ASC")
    fun getPlanBreakdownsFlow(planId: String): Flow<List<PlanMonthlyBreakdown>>
    
    @Insert
    suspend fun insertBreakdown(breakdown: PlanMonthlyBreakdown)
    
    @Insert
    suspend fun insertBreakdowns(breakdowns: List<PlanMonthlyBreakdown>)
    
    @Update
    suspend fun updateBreakdown(breakdown: PlanMonthlyBreakdown)
    
    @Delete
    suspend fun deleteBreakdown(breakdown: PlanMonthlyBreakdown)
    
    @Query("DELETE FROM plan_monthly_breakdowns WHERE planId = :planId")
    suspend fun deleteBreakdownsForPlan(planId: String)
    
    // Combined queries
    @Transaction
    @Query("SELECT * FROM financial_plans WHERE id = :planId")
    suspend fun getPlanWithBreakdowns(planId: String): PlanWithBreakdowns?

    @Transaction
    @Query("SELECT * FROM financial_plans ORDER BY updatedAt DESC")
    fun getAllPlansWithBreakdowns(): Flow<List<PlanWithBreakdowns>>

    // Get all data for export
    @Query("SELECT * FROM financial_plans")
    suspend fun getAllPlansDirect(): List<FinancialPlan>

    @Query("SELECT * FROM plan_monthly_breakdowns")
    suspend fun getAllMonthlyBreakdownsDirect(): List<PlanMonthlyBreakdown>

    // Bulk insert for import
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlans(plans: List<FinancialPlan>)

    // Clear all data for import
    @Query("DELETE FROM financial_plans")
    suspend fun deleteAllPlans()

    @Query("DELETE FROM plan_monthly_breakdowns")
    suspend fun deleteAllBreakdowns()
}

// Relation data class
data class PlanWithBreakdowns(
    @Embedded val plan: FinancialPlan,
    @Relation(
        parentColumn = "id",
        entityColumn = "planId"
    )
    val breakdowns: List<PlanMonthlyBreakdown>
)