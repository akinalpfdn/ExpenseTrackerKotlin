package com.example.expensetrackerkotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.expensetrackerkotlin.data.ExpenseDatabase
import com.example.expensetrackerkotlin.data.ExpenseRepository
import com.example.expensetrackerkotlin.data.CategoryRepository
import com.example.expensetrackerkotlin.data.PlanRepository
import com.example.expensetrackerkotlin.data.PreferencesManager
import com.example.expensetrackerkotlin.ui.screens.MainScreen
import com.example.expensetrackerkotlin.ui.theme.ExpenseTrackerKotlinTheme
import com.example.expensetrackerkotlin.viewmodel.ExpenseViewModel
import com.example.expensetrackerkotlin.viewmodel.ExpenseViewModelFactory
import com.example.expensetrackerkotlin.viewmodel.PlanningViewModel
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    
    private val preferencesManager by lazy { PreferencesManager(this) }
    private val database by lazy { ExpenseDatabase.getDatabase(this) }
    private val expenseRepository by lazy { ExpenseRepository(database.expenseDao()) }
    private val categoryRepository by lazy { CategoryRepository(database.categoryDao(), this) }
    private val planRepository by lazy { PlanRepository(database.planDao(), expenseRepository) }
    private val viewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory(preferencesManager, expenseRepository, categoryRepository, planRepository)
    }
    private val planningViewModel: PlanningViewModel by viewModels {
        ExpenseViewModelFactory(preferencesManager, expenseRepository, categoryRepository, planRepository)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val isDarkTheme = viewModel.theme == "dark"
            
            // Configure system UI colors based on theme
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            
            if (isDarkTheme) {
                // Dark theme: light system UI (white text/icons)
                windowInsetsController.isAppearanceLightStatusBars = false
                windowInsetsController.isAppearanceLightNavigationBars = false
            } else {
                // Light theme: dark system UI (black text/icons)
                windowInsetsController.isAppearanceLightStatusBars = true
                windowInsetsController.isAppearanceLightNavigationBars = true
            }
            
            ExpenseTrackerKotlinTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        viewModel = viewModel,
                        planningViewModel = planningViewModel
                    )
                }
            }
        }
    }
}