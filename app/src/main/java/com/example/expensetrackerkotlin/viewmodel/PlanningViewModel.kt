package com.example.expensetrackerkotlin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerkotlin.data.*
import com.example.expensetrackerkotlin.utils.PlanningUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class PlanningViewModel(
    private val planRepository: PlanRepository
) : ViewModel() {
    
    private val _plans = MutableStateFlow<List<FinancialPlan>>(emptyList())
    val plans: StateFlow<List<FinancialPlan>> = _plans.asStateFlow()
    
    private val _plansWithBreakdowns = MutableStateFlow<List<PlanWithBreakdowns>>(emptyList())
    val plansWithBreakdowns: StateFlow<List<PlanWithBreakdowns>> = _plansWithBreakdowns.asStateFlow()
    
    private val _selectedPlan = MutableStateFlow<PlanWithBreakdowns?>(null)
    val selectedPlan: StateFlow<PlanWithBreakdowns?> = _selectedPlan.asStateFlow()
    
    private val _currentPosition = MutableStateFlow<PlanCurrentPosition?>(null)
    val currentPosition: StateFlow<PlanCurrentPosition?> = _currentPosition.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadPlans()
    }
    
    private fun loadPlans() {
        viewModelScope.launch {
            planRepository.allPlans.collect { plansList ->
                _plans.value = plansList
            }
        }
        
        viewModelScope.launch {
            planRepository.allPlansWithBreakdowns.collect { plansWithBreakdownsList ->
                _plansWithBreakdowns.value = plansWithBreakdownsList
            }
        }
    }
    
    fun selectPlan(planId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val planWithBreakdowns = planRepository.getPlanWithBreakdowns(planId)
                _selectedPlan.value = planWithBreakdowns
                
                // Load current position if plan is active
                if (planWithBreakdowns?.plan?.isActive() == true) {
                    val position = planRepository.getCurrentFinancialPosition(planId)
                    _currentPosition.value = position
                }
            } catch (e: Exception) {
                _error.value = "Plan yüklenirken hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun createPlan(
        name: String,
        startDate: LocalDateTime,
        durationInMonths: Int,
        monthlyIncome: Double,
        manualMonthlyExpenses: Double = 0.0,
        useAppExpenseData: Boolean = true,
        isInflationApplied: Boolean = false,
        inflationRate: Double = 0.0,
        isInterestApplied: Boolean = false,
        interestRate: Double = 0.0,
        defaultCurrency: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val validation = PlanningUtils.validatePlanInput(
                    name = name,
                    monthlyIncome = monthlyIncome,
                    durationInMonths = durationInMonths,
                    inflationRate = if (isInflationApplied) inflationRate else null
                )
                
                if (!validation.isValid) {
                    _error.value = validation.errors.first()
                    return@launch
                }
                
                val newPlan = FinancialPlan(
                    name = name,
                    startDate = startDate,
                    durationInMonths = durationInMonths,
                    monthlyIncome = monthlyIncome,
                    manualMonthlyExpenses = manualMonthlyExpenses,
                    useAppExpenseData = useAppExpenseData,
                    isInflationApplied = isInflationApplied,
                    inflationRate = inflationRate,
                    isInterestApplied = isInterestApplied,
                    interestRate = interestRate,
                    defaultCurrency = defaultCurrency
                )
                
                planRepository.insertPlan(newPlan)
                clearError()
            } catch (e: Exception) {
                _error.value = "Plan oluşturulurken hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    

    fun deletePlan(planId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                planRepository.deletePlan(planId)
                if (_selectedPlan.value?.plan?.id == planId) {
                    _selectedPlan.value = null
                    _currentPosition.value = null
                }
                clearError()
            } catch (e: Exception) {
                _error.value = "Plan silinirken hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun regeneratePlanBreakdowns(planId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                planRepository.regeneratePlanBreakdowns(planId)
                // Refresh selected plan if it's the current one
                if (_selectedPlan.value?.plan?.id == planId) {
                    selectPlan(planId)
                }
                clearError()
            } catch (e: Exception) {
                _error.value = "Plan hesaplamaları güncellenirken hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updatePlanBreakdown(updatedBreakdown: PlanMonthlyBreakdown) {
        viewModelScope.launch {
            try {
                planRepository.updateBreakdown(updatedBreakdown)
                
                // Recalculate cumulative amounts for all subsequent months
                val planId = updatedBreakdown.planId
                planRepository.recalculateCumulativeAmounts(planId)
                
                // Refresh the selected plan to show updated values
                if (_selectedPlan.value?.plan?.id == planId) {
                    selectPlan(planId)
                }
                clearError()
            } catch (e: Exception) {
                _error.value = "Değişiklikler kaydedilirken hata oluştu: ${e.message}"
            }
        }
    }
    
    fun clearSelectedPlan() {
        _selectedPlan.value = null
        _currentPosition.value = null
    }
    
    fun clearError() {
        _error.value = null
    }
    
    // Helper methods for UI
    fun getActivePlans(): List<FinancialPlan> {
        return _plans.value.filter { it.isActive() }
    }
    
    fun getUpcomingPlans(): List<FinancialPlan> {
        val now = LocalDateTime.now()
        return _plans.value.filter { it.startDate.isAfter(now) }
    }
    
    fun getCompletedPlans(): List<FinancialPlan> {
        val now = LocalDateTime.now()
        return _plans.value.filter { it.endDate.isBefore(now) }
    }
}