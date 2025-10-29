package com.example.expensetrackerkotlin.ui.tutorial

import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerkotlin.data.PreferencesManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TutorialState(
    val isActive: Boolean = false,
    val currentStep: TutorialStep? = null,
    val currentStepIndex: Int = 0,
    val totalSteps: Int = 0,
    val canSkip: Boolean = true
)

class TutorialManager(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(TutorialState())
    val state: StateFlow<TutorialState> = _state.asStateFlow()

    private val steps = mutableListOf<TutorialStep>()
    private var autoProgressJob: kotlinx.coroutines.Job? = null

    init {
        // Load default steps
        steps.addAll(TutorialSteps.getDefaultSteps())
    }

    fun startTutorial() {
        if (steps.isEmpty()) return

        _state.value = TutorialState(
            isActive = true,
            currentStep = steps[0],
            currentStepIndex = 0,
            totalSteps = steps.size,
            canSkip = true
        )

        // Start auto-progress if needed
        handleAutoProgress()
    }

    fun nextStep() {
        val currentIndex = _state.value.currentStepIndex

        // Cancel any ongoing auto-progress
        autoProgressJob?.cancel()

        if (currentIndex >= steps.size - 1) {
            // Tutorial completed
            completeTutorial()
        } else {
            val nextIndex = currentIndex + 1
            _state.value = _state.value.copy(
                currentStep = steps[nextIndex],
                currentStepIndex = nextIndex
            )

            // Start auto-progress for new step if needed
            handleAutoProgress()
        }
    }

    fun skipTutorial() {
        autoProgressJob?.cancel()
        completeTutorial()
    }

    fun updateStepTargetBounds(stepId: TutorialStepId, bounds: Rect) {
        val stepIndex = steps.indexOfFirst { it.id == stepId }
        if (stepIndex != -1) {
            steps[stepIndex] = steps[stepIndex].copy(targetBounds = bounds)

            // If this is the current step, update the state
            if (_state.value.currentStepIndex == stepIndex) {
                _state.value = _state.value.copy(
                    currentStep = steps[stepIndex]
                )
            }
        }
    }

    private fun handleAutoProgress() {
        val currentStep = _state.value.currentStep
        if (currentStep?.autoProgress == true) {
            autoProgressJob = viewModelScope.launch {
                delay(currentStep.autoProgressDelay)
                nextStep()
            }
        }
    }

    private fun completeTutorial() {
        _state.value = TutorialState(
            isActive = false,
            currentStep = null,
            currentStepIndex = 0,
            totalSteps = steps.size,
            canSkip = false
        )

        // Save tutorial completion
        viewModelScope.launch {
            preferencesManager.setTutorialCompleted()
        }
    }

    fun resetTutorial() {
        viewModelScope.launch {
            preferencesManager.resetTutorial()
            startTutorial()
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoProgressJob?.cancel()
    }
}
