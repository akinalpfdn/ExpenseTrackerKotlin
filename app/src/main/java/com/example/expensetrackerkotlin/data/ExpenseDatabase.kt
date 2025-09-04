package com.example.expensetrackerkotlin.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.launch

@Database(entities = [Expense::class, Category::class, SubCategory::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    
    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the exchangeRate column with a default value of NULL
                database.execSQL("ALTER TABLE expenses ADD COLUMN exchangeRate REAL")
            }
        }
        
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add recurring expense fields
                database.execSQL("ALTER TABLE expenses ADD COLUMN recurrenceType TEXT DEFAULT 'NONE'")
                database.execSQL("ALTER TABLE expenses ADD COLUMN endDate TEXT")
            }
        }
        
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add recurrence group ID field
                database.execSQL("ALTER TABLE expenses ADD COLUMN recurrenceGroupId TEXT")
            }
        }
        
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create categories table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS categories (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        colorHex TEXT NOT NULL,
                        iconName TEXT NOT NULL,
                        isDefault INTEGER NOT NULL DEFAULT 0,
                        isCustom INTEGER NOT NULL DEFAULT 0
                    )
                """)
                
                // Create subcategories table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS subcategories (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        categoryId TEXT NOT NULL,
                        isDefault INTEGER NOT NULL DEFAULT 0,
                        isCustom INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE CASCADE
                    )
                """)
                
                // Create index for subcategories
                database.execSQL("CREATE INDEX IF NOT EXISTS index_subcategories_categoryId ON subcategories(categoryId)")
            }
        }
        
        @Volatile
        private var INSTANCE: ExpenseDatabase? = null
        
        fun getDatabase(context: Context): ExpenseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDatabase::class.java,
                    "expense_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Initialize with default categories and subcategories
                        INSTANCE?.let { database ->
                            kotlinx.coroutines.GlobalScope.launch {
                                initializeDefaultData(database.categoryDao())
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        private suspend fun initializeDefaultData(categoryDao: CategoryDao) {
            // Check if default categories already exist
            val defaultCategoriesCount = categoryDao.getDefaultCategoriesCount()
            val defaultSubCategoriesCount = categoryDao.getDefaultSubCategoriesCount()
            
            if (defaultCategoriesCount == 0) {
                // Insert default categories
                categoryDao.insertCategories(Category.getDefaultCategories())
            }
            
            if (defaultSubCategoriesCount == 0) {
                // Insert default subcategories
                categoryDao.insertSubCategories(SubCategory.getDefaultSubCategories())
            }
        }
    }
}
