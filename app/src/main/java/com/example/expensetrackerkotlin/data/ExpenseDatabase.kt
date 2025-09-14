package com.example.expensetrackerkotlin.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.launch

@Database(entities = [Expense::class, Category::class, SubCategory::class, FinancialPlan::class, PlanMonthlyBreakdown::class], version = 11, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun planDao(): PlanDao
    
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
        
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // First, create a temporary table with the new structure
                database.execSQL("""
                    CREATE TABLE expenses_new (
                        id TEXT PRIMARY KEY NOT NULL,
                        amount REAL NOT NULL,
                        currency TEXT NOT NULL,
                        categoryId TEXT NOT NULL,
                        subCategoryId TEXT NOT NULL,
                        description TEXT NOT NULL,
                        date TEXT NOT NULL,
                        dailyLimitAtCreation REAL NOT NULL,
                        monthlyLimitAtCreation REAL NOT NULL,
                        exchangeRate REAL,
                        recurrenceType TEXT NOT NULL DEFAULT 'NONE',
                        endDate TEXT,
                        recurrenceGroupId TEXT
                    )
                """)
                
                // Copy data from old table to new table, mapping subCategory strings to IDs
                // We'll use a default mapping for existing data
                database.execSQL("""
                    INSERT INTO expenses_new (
                        id, amount, currency, categoryId, subCategoryId, description, 
                        date, dailyLimitAtCreation, monthlyLimitAtCreation, exchangeRate,
                        recurrenceType, endDate, recurrenceGroupId
                    )
                    SELECT 
                        id, amount, currency, 
                        CASE 
                            WHEN subCategory IN ('Restoran', 'Market alışverişi', 'Kafeler') THEN 
                                (SELECT id FROM categories WHERE name = 'Gıda ve İçecek' LIMIT 1)
                            WHEN subCategory IN ('Kira', 'Aidat', 'Mortgage ödemesi', 'Elektrik faturası', 'Su faturası', 'Isınma (doğalgaz, kalorifer)', 'İnternet ve telefon', 'Diğer Faturalar', 'Temizlik malzemeleri') THEN 
                                (SELECT id FROM categories WHERE name = 'Konut' LIMIT 1)
                            WHEN subCategory IN ('Benzin/Dizel', 'Toplu taşıma', 'Araç bakımı', 'Oto kiralama', 'Taksi/Uber', 'Araç sigortası', 'MTV', 'Park ücretleri') THEN 
                                (SELECT id FROM categories WHERE name = 'Ulaşım' LIMIT 1)
                            WHEN subCategory IN ('Doktor randevusu', 'İlaçlar', 'Spor salonu üyeliği', 'Cilt bakım ürünleri', 'Diş bakımı', 'Giyim ve aksesuar') THEN 
                                (SELECT id FROM categories WHERE name = 'Sağlık ve Kişisel Bakım' LIMIT 1)
                            WHEN subCategory IN ('Sinema ve tiyatro', 'Konser ve etkinlikler', 'Abonelikler (Netflix, Spotify vb.)', 'Kitaplar ve dergiler', 'Seyahat ve tatil', 'Oyunlar ve uygulamalar') THEN 
                                (SELECT id FROM categories WHERE name = 'Eğlence ve Hobiler' LIMIT 1)
                            WHEN subCategory IN ('Kurs ücretleri', 'Eğitim materyalleri', 'Seminerler', 'Online kurslar') THEN 
                                (SELECT id FROM categories WHERE name = 'Eğitim' LIMIT 1)
                            WHEN subCategory IN ('Elektronik', 'Giyim', 'Ev eşyaları', 'Hediyeler', 'Takı ve aksesuar', 'Parfüm') THEN 
                                (SELECT id FROM categories WHERE name = 'Alışveriş' LIMIT 1)
                            WHEN subCategory IN ('Mama ve oyuncaklar', 'Veteriner hizmetleri', 'Evcil hayvan sigortası') THEN 
                                (SELECT id FROM categories WHERE name = 'Evcil Hayvan' LIMIT 1)
                            WHEN subCategory IN ('İş yemekleri', 'Ofis malzemeleri', 'İş seyahatleri', 'Eğitim ve seminerler', 'Freelance iş ödemeleri') THEN 
                                (SELECT id FROM categories WHERE name = 'İş ve Profesyonel Harcamalar' LIMIT 1)
                            WHEN subCategory IN ('Vergi ödemeleri', 'Avukat ve danışman ücretleri') THEN 
                                (SELECT id FROM categories WHERE name = 'Vergi ve Hukuki Harcamalar' LIMIT 1)
                            WHEN subCategory IN ('Hayır kurumları', 'Yardımlar ve bağışlar', 'Çevre ve toplum projeleri') THEN 
                                (SELECT id FROM categories WHERE name = 'Bağışlar ve Yardımlar' LIMIT 1)
                            ELSE 
                                (SELECT id FROM categories WHERE name = 'Diğer Ödemeler' LIMIT 1)
                        END as categoryId,
                        CASE 
                            WHEN subCategory = 'Restoran' THEN (SELECT id FROM subcategories WHERE name = 'Restoran' LIMIT 1)
                            WHEN subCategory = 'Market alışverişi' THEN (SELECT id FROM subcategories WHERE name = 'Market alışverişi' LIMIT 1)
                            WHEN subCategory = 'Kafeler' THEN (SELECT id FROM subcategories WHERE name = 'Kafeler' LIMIT 1)
                            WHEN subCategory = 'Kira' THEN (SELECT id FROM subcategories WHERE name = 'Kira' LIMIT 1)
                            WHEN subCategory = 'Aidat' THEN (SELECT id FROM subcategories WHERE name = 'Aidat' LIMIT 1)
                            WHEN subCategory = 'Mortgage ödemesi' THEN (SELECT id FROM subcategories WHERE name = 'Mortgage ödemesi' LIMIT 1)
                            WHEN subCategory = 'Elektrik faturası' THEN (SELECT id FROM subcategories WHERE name = 'Elektrik faturası' LIMIT 1)
                            WHEN subCategory = 'Su faturası' THEN (SELECT id FROM subcategories WHERE name = 'Su faturası' LIMIT 1)
                            WHEN subCategory = 'Isınma (doğalgaz, kalorifer)' THEN (SELECT id FROM subcategories WHERE name = 'Isınma (doğalgaz, kalorifer)' LIMIT 1)
                            WHEN subCategory = 'İnternet ve telefon' THEN (SELECT id FROM subcategories WHERE name = 'İnternet ve telefon' LIMIT 1)
                            WHEN subCategory = 'Diğer Faturalar' THEN (SELECT id FROM subcategories WHERE name = 'Diğer Faturalar' LIMIT 1)
                            WHEN subCategory = 'Temizlik malzemeleri' THEN (SELECT id FROM subcategories WHERE name = 'Temizlik malzemeleri' LIMIT 1)
                            WHEN subCategory = 'Benzin/Dizel' THEN (SELECT id FROM subcategories WHERE name = 'Benzin/Dizel' LIMIT 1)
                            WHEN subCategory = 'Toplu taşıma' THEN (SELECT id FROM subcategories WHERE name = 'Toplu taşıma' LIMIT 1)
                            WHEN subCategory = 'Araç bakımı' THEN (SELECT id FROM subcategories WHERE name = 'Araç bakımı' LIMIT 1)
                            WHEN subCategory = 'Oto kiralama' THEN (SELECT id FROM subcategories WHERE name = 'Oto kiralama' LIMIT 1)
                            WHEN subCategory = 'Taksi/Uber' THEN (SELECT id FROM subcategories WHERE name = 'Taksi/Uber' LIMIT 1)
                            WHEN subCategory = 'Araç sigortası' THEN (SELECT id FROM subcategories WHERE name = 'Araç sigortası' LIMIT 1)
                            WHEN subCategory = 'MTV' THEN (SELECT id FROM subcategories WHERE name = 'MTV' LIMIT 1)
                            WHEN subCategory = 'Park ücretleri' THEN (SELECT id FROM subcategories WHERE name = 'Park ücretleri' LIMIT 1)
                            WHEN subCategory = 'Doktor randevusu' THEN (SELECT id FROM subcategories WHERE name = 'Doktor randevusu' LIMIT 1)
                            WHEN subCategory = 'İlaçlar' THEN (SELECT id FROM subcategories WHERE name = 'İlaçlar' LIMIT 1)
                            WHEN subCategory = 'Spor salonu üyeliği' THEN (SELECT id FROM subcategories WHERE name = 'Spor salonu üyeliği' LIMIT 1)
                            WHEN subCategory = 'Cilt bakım ürünleri' THEN (SELECT id FROM subcategories WHERE name = 'Cilt bakım ürünleri' LIMIT 1)
                            WHEN subCategory = 'Diş bakımı' THEN (SELECT id FROM subcategories WHERE name = 'Diş bakımı' LIMIT 1)
                            WHEN subCategory = 'Giyim ve aksesuar' THEN (SELECT id FROM subcategories WHERE name = 'Giyim ve aksesuar' LIMIT 1)
                            WHEN subCategory = 'Sinema ve tiyatro' THEN (SELECT id FROM subcategories WHERE name = 'Sinema ve tiyatro' LIMIT 1)
                            WHEN subCategory = 'Konser ve etkinlikler' THEN (SELECT id FROM subcategories WHERE name = 'Konser ve etkinlikler' LIMIT 1)
                            WHEN subCategory = 'Abonelikler (Netflix, Spotify vb.)' THEN (SELECT id FROM subcategories WHERE name = 'Abonelikler (Netflix, Spotify vb.)' LIMIT 1)
                            WHEN subCategory = 'Kitaplar ve dergiler' THEN (SELECT id FROM subcategories WHERE name = 'Kitaplar ve dergiler' LIMIT 1)
                            WHEN subCategory = 'Seyahat ve tatil' THEN (SELECT id FROM subcategories WHERE name = 'Seyahat ve tatil' LIMIT 1)
                            WHEN subCategory = 'Oyunlar ve uygulamalar' THEN (SELECT id FROM subcategories WHERE name = 'Oyunlar ve uygulamalar' LIMIT 1)
                            WHEN subCategory = 'Kurs ücretleri' THEN (SELECT id FROM subcategories WHERE name = 'Kurs ücretleri' LIMIT 1)
                            WHEN subCategory = 'Eğitim materyalleri' THEN (SELECT id FROM subcategories WHERE name = 'Eğitim materyalleri' LIMIT 1)
                            WHEN subCategory = 'Seminerler' THEN (SELECT id FROM subcategories WHERE name = 'Seminerler' LIMIT 1)
                            WHEN subCategory = 'Online kurslar' THEN (SELECT id FROM subcategories WHERE name = 'Online kurslar' LIMIT 1)
                            WHEN subCategory = 'Elektronik' THEN (SELECT id FROM subcategories WHERE name = 'Elektronik' LIMIT 1)
                            WHEN subCategory = 'Giyim' THEN (SELECT id FROM subcategories WHERE name = 'Giyim' LIMIT 1)
                            WHEN subCategory = 'Ev eşyaları' THEN (SELECT id FROM subcategories WHERE name = 'Ev eşyaları' LIMIT 1)
                            WHEN subCategory = 'Hediyeler' THEN (SELECT id FROM subcategories WHERE name = 'Hediyeler' LIMIT 1)
                            WHEN subCategory = 'Takı ve aksesuar' THEN (SELECT id FROM subcategories WHERE name = 'Takı ve aksesuar' LIMIT 1)
                            WHEN subCategory = 'Parfüm' THEN (SELECT id FROM subcategories WHERE name = 'Parfüm' LIMIT 1)
                            WHEN subCategory = 'Mama ve oyuncaklar' THEN (SELECT id FROM subcategories WHERE name = 'Mama ve oyuncaklar' LIMIT 1)
                            WHEN subCategory = 'Veteriner hizmetleri' THEN (SELECT id FROM subcategories WHERE name = 'Veteriner hizmetleri' LIMIT 1)
                            WHEN subCategory = 'Evcil hayvan sigortası' THEN (SELECT id FROM subcategories WHERE name = 'Evcil hayvan sigortası' LIMIT 1)
                            WHEN subCategory = 'İş yemekleri' THEN (SELECT id FROM subcategories WHERE name = 'İş yemekleri' LIMIT 1)
                            WHEN subCategory = 'Ofis malzemeleri' THEN (SELECT id FROM subcategories WHERE name = 'Ofis malzemeleri' LIMIT 1)
                            WHEN subCategory = 'İş seyahatleri' THEN (SELECT id FROM subcategories WHERE name = 'İş seyahatleri' LIMIT 1)
                            WHEN subCategory = 'Eğitim ve seminerler' THEN (SELECT id FROM subcategories WHERE name = 'Eğitim ve seminerler' LIMIT 1)
                            WHEN subCategory = 'Freelance iş ödemeleri' THEN (SELECT id FROM subcategories WHERE name = 'Freelance iş ödemeleri' LIMIT 1)
                            WHEN subCategory = 'Vergi ödemeleri' THEN (SELECT id FROM subcategories WHERE name = 'Vergi ödemeleri' LIMIT 1)
                            WHEN subCategory = 'Avukat ve danışman ücretleri' THEN (SELECT id FROM subcategories WHERE name = 'Avukat ve danışman ücretleri' LIMIT 1)
                            WHEN subCategory = 'Hayır kurumları' THEN (SELECT id FROM subcategories WHERE name = 'Hayır kurumları' LIMIT 1)
                            WHEN subCategory = 'Yardımlar ve bağışlar' THEN (SELECT id FROM subcategories WHERE name = 'Yardımlar ve bağışlar' LIMIT 1)
                            WHEN subCategory = 'Çevre ve toplum projeleri' THEN (SELECT id FROM subcategories WHERE name = 'Çevre ve toplum projeleri' LIMIT 1)
                            WHEN subCategory = 'Diğer Harcamalar' THEN (SELECT id FROM subcategories WHERE name = 'Diğer Harcamalar' LIMIT 1)
                            ELSE (SELECT id FROM subcategories WHERE name = 'Diğer Harcamalar' LIMIT 1)
                        END as subCategoryId,
                        description, date, dailyLimitAtCreation, monthlyLimitAtCreation, 
                        exchangeRate, recurrenceType, endDate, recurrenceGroupId
                    FROM expenses
                """)
                
                // Drop the old table
                database.execSQL("DROP TABLE expenses")
                
                // Rename the new table
                database.execSQL("ALTER TABLE expenses_new RENAME TO expenses")
            }
        }
        
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create financial_plans table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS financial_plans (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        startDate TEXT NOT NULL,
                        durationInMonths INTEGER NOT NULL,
                        monthlyIncome REAL NOT NULL,
                        manualMonthlyExpenses REAL NOT NULL DEFAULT 0.0,
                        isInflationApplied INTEGER NOT NULL DEFAULT 0,
                        inflationRate REAL NOT NULL DEFAULT 0.0,
                        includeRecurringExpenses INTEGER NOT NULL DEFAULT 1,
                        includeAverageExpenses INTEGER NOT NULL DEFAULT 0,
                        averageMonthsToCalculate INTEGER NOT NULL DEFAULT 3,
                        createdAt TEXT NOT NULL,
                        updatedAt TEXT NOT NULL
                    )
                """)
                
                // Create plan_monthly_breakdowns table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS plan_monthly_breakdowns (
                        id TEXT PRIMARY KEY NOT NULL,
                        planId TEXT NOT NULL,
                        monthIndex INTEGER NOT NULL,
                        projectedIncome REAL NOT NULL,
                        fixedExpenses REAL NOT NULL,
                        averageExpenses REAL NOT NULL,
                        totalProjectedExpenses REAL NOT NULL,
                        netAmount REAL NOT NULL,
                        cumulativeNet REAL NOT NULL,
                        FOREIGN KEY(planId) REFERENCES financial_plans(id) ON DELETE CASCADE
                    )
                """)
                
                // Create index for plan_monthly_breakdowns
                database.execSQL("CREATE INDEX IF NOT EXISTS index_plan_monthly_breakdowns_planId ON plan_monthly_breakdowns(planId)")
            }
        }
        
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add defaultCurrency field to financial_plans table
                database.execSQL("ALTER TABLE financial_plans ADD COLUMN defaultCurrency TEXT NOT NULL DEFAULT '₺'")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Replace multiple expense flags with single useAppExpenseData boolean
                database.execSQL("ALTER TABLE financial_plans ADD COLUMN useAppExpenseData INTEGER NOT NULL DEFAULT 1")

                // Remove old columns by creating new table and copying data
                database.execSQL("""
                    CREATE TABLE financial_plans_new (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        startDate TEXT NOT NULL,
                        durationInMonths INTEGER NOT NULL,
                        monthlyIncome REAL NOT NULL,
                        manualMonthlyExpenses REAL NOT NULL DEFAULT 0.0,
                        useAppExpenseData INTEGER NOT NULL DEFAULT 1,
                        isInflationApplied INTEGER NOT NULL DEFAULT 0,
                        inflationRate REAL NOT NULL DEFAULT 0.0,
                        createdAt TEXT NOT NULL,
                        updatedAt TEXT NOT NULL,
                        defaultCurrency TEXT NOT NULL DEFAULT '₺'
                    )
                """)

                // Copy data from old table, combining logic from old flags
                database.execSQL("""
                    INSERT INTO financial_plans_new (
                        id, name, startDate, durationInMonths, monthlyIncome,
                        manualMonthlyExpenses, useAppExpenseData, isInflationApplied,
                        inflationRate, createdAt, updatedAt, defaultCurrency
                    )
                    SELECT
                        id, name, startDate, durationInMonths, monthlyIncome,
                        manualMonthlyExpenses,
                        CASE WHEN manualMonthlyExpenses > 0 THEN 0 ELSE 1 END,
                        isInflationApplied, inflationRate, createdAt, updatedAt, defaultCurrency
                    FROM financial_plans
                """)

                // Drop old table and rename new one
                database.execSQL("DROP TABLE financial_plans")
                database.execSQL("ALTER TABLE financial_plans_new RENAME TO financial_plans")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add interest rate fields to financial_plans
                database.execSQL("ALTER TABLE financial_plans ADD COLUMN isInterestApplied INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE financial_plans ADD COLUMN interestRate REAL NOT NULL DEFAULT 0.0")

                // Add interestEarned field to plan_monthly_breakdowns
                database.execSQL("ALTER TABLE plan_monthly_breakdowns ADD COLUMN interestEarned REAL NOT NULL DEFAULT 0.0")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add interest type field to financial_plans (COMPOUND is default)
                database.execSQL("ALTER TABLE financial_plans ADD COLUMN interestType TEXT NOT NULL DEFAULT 'COMPOUND'")
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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Initialize with default categories and subcategories
                        INSTANCE?.let { database ->
                            kotlinx.coroutines.GlobalScope.launch {
                                initializeDefaultData(database.categoryDao(), context)
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        private suspend fun initializeDefaultData(categoryDao: CategoryDao, context: Context) {
            // Check if default categories already exist
            val defaultCategoriesCount = categoryDao.getDefaultCategoriesCount()
            val defaultSubCategoriesCount = categoryDao.getDefaultSubCategoriesCount()

            if (defaultCategoriesCount == 0) {
                // Insert default categories
                categoryDao.insertCategories(Category.getDefaultCategories(context))
            }

            if (defaultSubCategoriesCount == 0) {
                // Insert default subcategories
                categoryDao.insertSubCategories(SubCategory.getDefaultSubCategories(context))
            }
        }
    }
}
