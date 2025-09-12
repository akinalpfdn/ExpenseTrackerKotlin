package com.example.expensetrackerkotlin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.expensetrackerkotlin.data.ExpenseRepository
import com.example.expensetrackerkotlin.data.PreferencesManager
import com.example.expensetrackerkotlin.data.CategoryRepository
import com.example.expensetrackerkotlin.data.PlanRepository

class ExpenseViewModelFactory(
    private val preferencesManager: PreferencesManager,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val planRepository: PlanRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ExpenseViewModel::class.java) -> {
                ExpenseViewModel(preferencesManager, expenseRepository, categoryRepository) as T
            }
            modelClass.isAssignableFrom(PlanningViewModel::class.java) -> {
                PlanningViewModel(planRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}