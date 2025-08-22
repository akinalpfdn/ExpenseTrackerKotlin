package com.example.expensetrackerkotlin.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Expense::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    
    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the exchangeRate column with a default value of NULL
                database.execSQL("ALTER TABLE expenses ADD COLUMN exchangeRate REAL")
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
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
