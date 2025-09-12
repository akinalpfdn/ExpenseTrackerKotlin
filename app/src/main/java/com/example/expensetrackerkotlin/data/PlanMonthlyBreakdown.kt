package com.example.expensetrackerkotlin.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.*

@Entity(
    tableName = "plan_monthly_breakdowns",
    foreignKeys = [
        ForeignKey(
            entity = FinancialPlan::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(Converters::class)
data class PlanMonthlyBreakdown(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val planId: String,
    val monthIndex: Int, // 0-based month index from start date
    val projectedIncome: Double,
    val fixedExpenses: Double,
    val averageExpenses: Double,
    val totalProjectedExpenses: Double,
    val netAmount: Double, // projectedIncome - totalProjectedExpenses
    val cumulativeNet: Double // Running total of net amounts
) {
    
    fun getSavingsRate(): Float {
        return if (projectedIncome > 0) {
            (netAmount / projectedIncome).toFloat()
        } else {
            0f
        }
    }
    
    fun getExpenseRatio(): Float {
        return if (projectedIncome > 0) {
            (totalProjectedExpenses / projectedIncome).toFloat()
        } else {
            0f
        }
    }
}