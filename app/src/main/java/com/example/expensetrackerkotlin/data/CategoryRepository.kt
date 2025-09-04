package com.example.expensetrackerkotlin.data

import kotlinx.coroutines.flow.Flow
import java.util.*

class CategoryRepository(private val categoryDao: CategoryDao) {
    
    // Category operations
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val defaultCategories: Flow<List<Category>> = categoryDao.getDefaultCategories()
    val customCategories: Flow<List<Category>> = categoryDao.getCustomCategories()
    
    suspend fun getCategoryById(categoryId: String): Category? {
        return categoryDao.getCategoryById(categoryId)
    }
    
    suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category)
    }
    
    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }
    
    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }
    
    suspend fun deleteCategoryById(categoryId: String) {
        categoryDao.deleteCategoryById(categoryId)
    }
    
    // SubCategory operations
    val allSubCategories: Flow<List<SubCategory>> = categoryDao.getAllSubCategories()
    val defaultSubCategories: Flow<List<SubCategory>> = categoryDao.getDefaultSubCategories()
    val customSubCategories: Flow<List<SubCategory>> = categoryDao.getCustomSubCategories()
    
    fun getSubCategoriesByCategoryId(categoryId: String): Flow<List<SubCategory>> {
        return categoryDao.getSubCategoriesByCategoryId(categoryId)
    }
    
    suspend fun getSubCategoryById(subCategoryId: String): SubCategory? {
        return categoryDao.getSubCategoryById(subCategoryId)
    }
    
    suspend fun insertSubCategory(subCategory: SubCategory) {
        categoryDao.insertSubCategory(subCategory)
    }
    
    suspend fun updateSubCategory(subCategory: SubCategory) {
        categoryDao.updateSubCategory(subCategory)
    }
    
    suspend fun deleteSubCategory(subCategory: SubCategory) {
        categoryDao.deleteSubCategory(subCategory)
    }
    
    suspend fun deleteSubCategoryById(subCategoryId: String) {
        categoryDao.deleteSubCategoryById(subCategoryId)
    }
    
    // Combined operations
    val categoriesWithSubCategories: Flow<Map<Category, List<SubCategory>>> = 
        categoryDao.getCategoriesWithSubCategories()
    
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
            categoryDao.insertCategories(Category.getDefaultCategories())
            categoryDao.insertSubCategories(SubCategory.getDefaultSubCategories())
        }
    }
}
