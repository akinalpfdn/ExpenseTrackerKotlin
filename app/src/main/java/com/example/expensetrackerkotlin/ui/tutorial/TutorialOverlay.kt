package com.example.expensetrackerkotlin.ui.tutorial

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex

@Composable
fun TutorialOverlay(
    tutorialState: TutorialState,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    if (!tutorialState.isActive || tutorialState.currentStep == null) {
        return
    }

    val currentStep = tutorialState.currentStep

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(999f)
    ) {
        // Tooltip only - highlighting is done via direct borders on elements
        TutorialTooltip(
            step = currentStep,
            stepIndex = tutorialState.currentStepIndex,
            totalSteps = tutorialState.totalSteps,
            onNext = onNext,
            onSkip = onSkip,
            isDarkTheme = isDarkTheme
        )
    }
}
