package com.example.expensetrackerkotlin.data

import androidx.room.withTransaction
import kotlinx.serialization.json.Json

enum class ImportStrategy {
    REPLACE_ALL,  // Delete all existing data and import new
    MERGE         // Merge with existing data (replace by ID)
}

class ImportManager(private val database: ExpenseDatabase) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun importData(
        jsonString: String,
        strategy: ImportStrategy
    ): Result<ImportSummary> {
        return try {
            // Parse JSON
            val exportData = json.decodeFromString<ExportData>(jsonString)

            // Validate version compatibility
            if (exportData.databaseVersion > 11) {
                return Result.failure(
                    Exception("Import file is from a newer version of the app. Please update the app first.")
                )
            }

            // Perform import in a transaction
            database.withTransaction {
                val expenseDao = database.expenseDao()
                val categoryDao = database.categoryDao()
                val planDao = database.planDao()

                if (strategy == ImportStrategy.REPLACE_ALL) {
                    // Delete all existing data
                    planDao.deleteAllBreakdowns()
                    planDao.deleteAllPlans()
                    expenseDao.deleteAllExpenses()
                    categoryDao.deleteAllSubCategories()
                    categoryDao.deleteAllCategories()
                }

                // Import categories first (due to foreign keys)
                val categories = exportData.categories.map { it.toEntity() }
                categoryDao.insertCategories(categories)

                // Import subcategories
                val subCategories = exportData.subCategories.map { it.toEntity() }
                categoryDao.insertSubCategories(subCategories)

                // Import expenses
                val expenses = exportData.expenses.map { it.toEntity() }
                expenseDao.insertExpenses(expenses)

                // Import financial plans
                val plans = exportData.financialPlans.map { it.toEntity() }
                planDao.insertPlans(plans)

                // Import plan breakdowns
                val breakdowns = exportData.planMonthlyBreakdowns.map { it.toEntity() }
                planDao.insertBreakdowns(breakdowns)
            }

            // Return summary
            val summary = ImportSummary(
                categoriesImported = exportData.categories.size,
                subCategoriesImported = exportData.subCategories.size,
                expensesImported = exportData.expenses.size,
                financialPlansImported = exportData.financialPlans.size,
                planBreakdownsImported = exportData.planMonthlyBreakdowns.size,
                strategy = strategy
            )

            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to import data: ${e.message}", e))
        }
    }

    suspend fun validateImportFile(jsonString: String): Result<ExportData> {
        return try {
            val exportData = json.decodeFromString<ExportData>(jsonString)

            // Basic validation
            if (exportData.exportVersion < 1) {
                return Result.failure(Exception("Invalid export file format"))
            }

            if (exportData.databaseVersion > 11) {
                return Result.failure(
                    Exception("Import file is from a newer version of the app (DB v${exportData.databaseVersion}). Please update the app first.")
                )
            }

            Result.success(exportData)
        } catch (e: Exception) {
            Result.failure(Exception("Invalid import file: ${e.message}", e))
        }
    }
}

data class ImportSummary(
    val categoriesImported: Int,
    val subCategoriesImported: Int,
    val expensesImported: Int,
    val financialPlansImported: Int,
    val planBreakdownsImported: Int,
    val strategy: ImportStrategy
) {
    override fun toString(): String {
        return """
            Import completed successfully!

            Categories: $categoriesImported
            SubCategories: $subCategoriesImported
            Expenses: $expensesImported
            Financial Plans: $financialPlansImported
            Plan Breakdowns: $planBreakdownsImported

            Strategy: ${if (strategy == ImportStrategy.REPLACE_ALL) "Replace All" else "Merge"}
        """.trimIndent()
    }
}
