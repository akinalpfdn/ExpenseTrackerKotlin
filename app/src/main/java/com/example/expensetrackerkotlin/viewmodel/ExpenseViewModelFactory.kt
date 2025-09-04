package com.example.expensetrackerkotlin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.expensetrackerkotlin.data.ExpenseRepository
import com.example.expensetrackerkotlin.data.PreferencesManager
import com.example.expensetrackerkotlin.data.CategoryRepository

class ExpenseViewModelFactory(
    private val preferencesManager: PreferencesManager,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            return ExpenseViewModel(preferencesManager, expenseRepository, categoryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}