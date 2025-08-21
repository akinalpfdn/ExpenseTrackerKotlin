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
import com.example.expensetrackerkotlin.data.PreferencesManager
import com.example.expensetrackerkotlin.ui.screens.MainScreen
import com.example.expensetrackerkotlin.ui.theme.ExpenseTrackerKotlinTheme
import com.example.expensetrackerkotlin.viewmodel.ExpenseViewModel
import com.example.expensetrackerkotlin.viewmodel.ExpenseViewModelFactory

class MainActivity : ComponentActivity() {
    
    private val preferencesManager by lazy { PreferencesManager(this) }
    private val viewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory(preferencesManager)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpenseTrackerKotlinTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}