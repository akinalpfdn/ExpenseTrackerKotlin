package com.example.expensetrackerkotlin.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import java.util.*

class CategoryRepository(private val categoryDao: CategoryDao, private val context: Context) {
    
    // Category operations
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category)
    }
    
    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }
    
    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    // SubCategory operations
    val allSubCategories: Flow<List<SubCategory>> = categoryDao.getAllSubCategories()

    suspend fun insertSubCategory(subCategory: SubCategory) {
        categoryDao.insertSubCategory(subCategory)
    }
    
    suspend fun updateSubCategory(subCategory: SubCategory) {
        categoryDao.updateSubCategory(subCategory)
    }
    
    suspend fun deleteSubCategory(subCategory: SubCategory) {
        categoryDao.deleteSubCategory(subCategory)
    }

    // Helper methods for creating custom categories and subcategories
    suspend fun createCustomCategory(
        name: String,
        colorHex: String,
        iconName: String
    ): Category {
        val category = Category(
            id = UUID.randomUUID().toString(),
            name = name,
            colorHex = colorHex,
            iconName = iconName,
            isCustom = true
        )
        insertCategory(category)
        return category
    }
    
    suspend fun createCustomSubCategory(
        name: String,
        categoryId: String
    ): SubCategory {
        val subCategory = SubCategory(
            name = name,
            categoryId = categoryId,
            isCustom = true
        )
        insertSubCategory(subCategory)
        return subCategory
    }
    
    // Check if default data is initialized
    suspend fun isDefaultDataInitialized(): Boolean {
        val categoriesCount = categoryDao.getDefaultCategoriesCount()
        val subCategoriesCount = categoryDao.getDefaultSubCategoriesCount()
        return categoriesCount > 0 && subCategoriesCount > 0
    }
    
    // Initialize default data if not already done
    suspend fun initializeDefaultDataIfNeeded() {
        if (!isDefaultDataInitialized()) {
            categoryDao.insertCategories(Category.getDefaultCategories(context))
            categoryDao.insertSubCategories(SubCategory.getDefaultSubCategories(context))
        }
    }
}
