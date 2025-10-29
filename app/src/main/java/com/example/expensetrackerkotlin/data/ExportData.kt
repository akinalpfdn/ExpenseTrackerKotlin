package com.example.expensetrackerkotlin.data

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class ExportData(
    val exportVersion: Int = 1,
    val appVersion: String,
    val exportDate: String, // ISO format
    val databaseVersion: Int = 11,
    val categories: List<CategoryDto>,
    val subCategories: List<SubCategoryDto>,
    val expenses: List<ExpenseDto>,
    val financialPlans: List<FinancialPlanDto>,
    val planMonthlyBreakdowns: List<PlanMonthlyBreakdownDto>
)

// DTOs for serialization
@Serializable
data class CategoryDto(
    val id: String,
    val name: String,
    val colorHex: String,
    val iconName: String,
    val isDefault: Boolean,
    val isCustom: Boolean
)

@Serializable
data class SubCategoryDto(
    val id: String,
    val name: String,
    val categoryId: String,
    val isDefault: Boolean,
    val isCustom: Boolean
)

@Serializable
data class ExpenseDto(
    val id: String,
    val amount: Double,
    val currency: String,
    val categoryId: String,
    val subCategoryId: String,
    val description: String,
    val date: String, // ISO format
    val dailyLimitAtCreation: Double,
    val monthlyLimitAtCreation: Double,
    val exchangeRate: Double?,
    val recurrenceType: String, // Enum as string
    val endDate: String?, // ISO format
    val recurrenceGroupId: String?
)

@Serializable
data class FinancialPlanDto(
    val id: String,
    val name: String,
    val startDate: String, // ISO format
    val durationInMonths: Int,
    val monthlyIncome: Double,
    val manualMonthlyExpenses: Double,
    val useAppExpenseData: Boolean,
    val isInflationApplied: Boolean,
    val inflationRate: Double,
    val isInterestApplied: Boolean,
    val interestRate: Double,
    val interestType: String, // Enum as string
    val createdAt: String, // ISO format
    val updatedAt: String, // ISO format
    val defaultCurrency: String
)

@Serializable
data class PlanMonthlyBreakdownDto(
    val id: String,
    val planId: String,
    val monthIndex: Int,
    val projectedIncome: Double,
    val fixedExpenses: Double,
    val averageExpenses: Double,
    val totalProjectedExpenses: Double,
    val netAmount: Double,
    val interestEarned: Double,
    val cumulativeNet: Double
)

// Extension functions to convert entities to DTOs
fun Category.toDto() = CategoryDto(
    id = id,
    name = name,
    colorHex = colorHex,
    iconName = iconName,
    isDefault = isDefault,
    isCustom = isCustom
)

fun SubCategory.toDto() = SubCategoryDto(
    id = id,
    name = name,
    categoryId = categoryId,
    isDefault = isDefault,
    isCustom = isCustom
)

fun Expense.toDto() = ExpenseDto(
    id = id,
    amount = amount,
    currency = currency,
    categoryId = categoryId,
    subCategoryId = subCategoryId,
    description = description,
    date = date.toString(),
    dailyLimitAtCreation = dailyLimitAtCreation,
    monthlyLimitAtCreation = monthlyLimitAtCreation,
    exchangeRate = exchangeRate,
    recurrenceType = recurrenceType.name,
    endDate = endDate?.toString(),
    recurrenceGroupId = recurrenceGroupId
)

fun FinancialPlan.toDto() = FinancialPlanDto(
    id = id,
    name = name,
    startDate = startDate.toString(),
    durationInMonths = durationInMonths,
    monthlyIncome = monthlyIncome,
    manualMonthlyExpenses = manualMonthlyExpenses,
    useAppExpenseData = useAppExpenseData,
    isInflationApplied = isInflationApplied,
    inflationRate = inflationRate,
    isInterestApplied = isInterestApplied,
    interestRate = interestRate,
    interestType = interestType.name,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
    defaultCurrency = defaultCurrency
)

fun PlanMonthlyBreakdown.toDto() = PlanMonthlyBreakdownDto(
    id = id,
    planId = planId,
    monthIndex = monthIndex,
    projectedIncome = projectedIncome,
    fixedExpenses = fixedExpenses,
    averageExpenses = averageExpenses,
    totalProjectedExpenses = totalProjectedExpenses,
    netAmount = netAmount,
    interestEarned = interestEarned,
    cumulativeNet = cumulativeNet
)

// Extension functions to convert DTOs back to entities
fun CategoryDto.toEntity() = Category(
    id = id,
    name = name,
    colorHex = colorHex,
    iconName = iconName,
    isDefault = isDefault,
    isCustom = isCustom
)

fun SubCategoryDto.toEntity() = SubCategory(
    id = id,
    name = name,
    categoryId = categoryId,
    isDefault = isDefault,
    isCustom = isCustom
)

fun ExpenseDto.toEntity() = Expense(
    id = id,
    amount = amount,
    currency = currency,
    categoryId = categoryId,
    subCategoryId = subCategoryId,
    description = description,
    date = LocalDateTime.parse(date),
    dailyLimitAtCreation = dailyLimitAtCreation,
    monthlyLimitAtCreation = monthlyLimitAtCreation,
    exchangeRate = exchangeRate,
    recurrenceType = RecurrenceType.valueOf(recurrenceType),
    endDate = endDate?.let { LocalDateTime.parse(it) },
    recurrenceGroupId = recurrenceGroupId
)

fun FinancialPlanDto.toEntity() = FinancialPlan(
    id = id,
    name = name,
    startDate = LocalDateTime.parse(startDate),
    durationInMonths = durationInMonths,
    monthlyIncome = monthlyIncome,
    manualMonthlyExpenses = manualMonthlyExpenses,
    useAppExpenseData = useAppExpenseData,
    isInflationApplied = isInflationApplied,
    inflationRate = inflationRate,
    isInterestApplied = isInterestApplied,
    interestRate = interestRate,
    interestType = InterestType.valueOf(interestType),
    createdAt = LocalDateTime.parse(createdAt),
    updatedAt = LocalDateTime.parse(updatedAt),
    defaultCurrency = defaultCurrency
)

fun PlanMonthlyBreakdownDto.toEntity() = PlanMonthlyBreakdown(
    id = id,
    planId = planId,
    monthIndex = monthIndex,
    projectedIncome = projectedIncome,
    fixedExpenses = fixedExpenses,
    averageExpenses = averageExpenses,
    totalProjectedExpenses = totalProjectedExpenses,
    netAmount = netAmount,
    interestEarned = interestEarned,
    cumulativeNet = cumulativeNet
)
