package com.example.expensetrackerkotlin.data

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime

class ExportManager(private val database: ExpenseDatabase) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun exportAllData(context: Context): Result<String> {
        return try {
            val expenseDao = database.expenseDao()
            val categoryDao = database.categoryDao()
            val planDao = database.planDao()

            // Fetch all data from database
            val expenses = expenseDao.getAllExpensesDirect()
            val categories = categoryDao.getAllCategoriesDirect()
            val subCategories = categoryDao.getAllSubCategoriesDirect()
            val financialPlans = planDao.getAllPlansDirect()
            val planBreakdowns = planDao.getAllMonthlyBreakdownsDirect()

            // Convert entities to DTOs
            val exportData = ExportData(
                exportVersion = 1,
                appVersion = getAppVersion(context),
                exportDate = LocalDateTime.now().toString(),
                databaseVersion = 11,
                categories = categories.map { it.toDto() },
                subCategories = subCategories.map { it.toDto() },
                expenses = expenses.map { it.toDto() },
                financialPlans = financialPlans.map { it.toDto() },
                planMonthlyBreakdowns = planBreakdowns.map { it.toDto() }
            )

            // Serialize to JSON
            val jsonString = json.encodeToString(exportData)
            Result.success(jsonString)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }
}
