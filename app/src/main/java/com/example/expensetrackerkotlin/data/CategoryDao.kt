package com.example.expensetrackerkotlin.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    
    // Category operations
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>
    
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): Category?
    
    @Query("SELECT * FROM categories WHERE isDefault = 1")
    fun getDefaultCategories(): Flow<List<Category>>
    
    @Query("SELECT * FROM categories WHERE isCustom = 1")
    fun getCustomCategories(): Flow<List<Category>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)
    
    @Update
    suspend fun updateCategory(category: Category)
    
    @Delete
    suspend fun deleteCategory(category: Category)
    
    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: String)
    
    // SubCategory operations
    @Query("SELECT * FROM subcategories ORDER BY name ASC")
    fun getAllSubCategories(): Flow<List<SubCategory>>
    
    @Query("SELECT * FROM subcategories WHERE categoryId = :categoryId ORDER BY name ASC")
    fun getSubCategoriesByCategoryId(categoryId: String): Flow<List<SubCategory>>
    
    @Query("SELECT * FROM subcategories WHERE id = :subCategoryId")
    suspend fun getSubCategoryById(subCategoryId: String): SubCategory?
    
    @Query("SELECT * FROM subcategories WHERE isDefault = 1")
    fun getDefaultSubCategories(): Flow<List<SubCategory>>
    
    @Query("SELECT * FROM subcategories WHERE isCustom = 1")
    fun getCustomSubCategories(): Flow<List<SubCategory>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubCategory(subCategory: SubCategory)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubCategories(subCategories: List<SubCategory>)
    
    @Update
    suspend fun updateSubCategory(subCategory: SubCategory)
    
    @Delete
    suspend fun deleteSubCategory(subCategory: SubCategory)
    
    @Query("DELETE FROM subcategories WHERE id = :subCategoryId")
    suspend fun deleteSubCategoryById(subCategoryId: String)
    
    // Combined operations
    @Query("""
        SELECT c.*, s.* FROM categories c 
        LEFT JOIN subcategories s ON c.id = s.categoryId 
        ORDER BY c.name ASC, s.name ASC
    """)
    fun getCategoriesWithSubCategories(): Flow<Map<Category, List<SubCategory>>>
    
    // Check if categories are initialized
    @Query("SELECT COUNT(*) FROM categories WHERE isDefault = 1")
    suspend fun getDefaultCategoriesCount(): Int

    @Query("SELECT COUNT(*) FROM subcategories WHERE isDefault = 1")
    suspend fun getDefaultSubCategoriesCount(): Int

    // Get all data for export
    @Query("SELECT * FROM categories")
    suspend fun getAllCategoriesDirect(): List<Category>

    @Query("SELECT * FROM subcategories")
    suspend fun getAllSubCategoriesDirect(): List<SubCategory>

    // Clear all data for import
    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()

    @Query("DELETE FROM subcategories")
    suspend fun deleteAllSubCategories()
}
