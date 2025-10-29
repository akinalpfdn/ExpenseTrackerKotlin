package com.example.expensetrackerkotlin.ui.tutorial

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
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

    // Animated glow effect
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(999f)
    ) {
        // Semi-transparent overlay with cutout for highlighted element - VISUAL ONLY, non-interactive
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // Draw dark overlay
            drawRect(
                color = Color.Black.copy(alpha = 0.7f),
                size = size
            )

            // Cut out the highlighted area
            if (currentStep.targetBounds != null) {
                val highlightPath = Path().apply {
                    when (currentStep.highlightShape) {
                        HighlightShape.CIRCLE -> {
                            val center = currentStep.targetBounds.center
                            val radius = currentStep.highlightRadius * glowScale
                            addOval(
                                Rect(
                                    center = center,
                                    radius = radius
                                )
                            )
                        }
                        HighlightShape.ROUNDED_RECT -> {
                            val expandedBounds = currentStep.targetBounds.inflate(16f * glowScale)
                            addRoundRect(
                                RoundRect(
                                    rect = expandedBounds,
                                    cornerRadius = CornerRadius(16f, 16f)
                                )
                            )
                        }
                    }
                }

                // Clear the highlighted area
                drawPath(
                    path = highlightPath,
                    color = Color.Transparent,
                    blendMode = BlendMode.Clear
                )

                // Draw glowing border around highlighted area
                drawPath(
                    path = highlightPath,
                    color = Color(0xFFFF9500).copy(alpha = glowAlpha),
                    style = Stroke(width = 4.dp.toPx())
                )

                // Draw inner glow
                drawPath(
                    path = highlightPath,
                    color = Color(0xFFFF9500).copy(alpha = glowAlpha * 0.3f),
                    style = Stroke(width = 12.dp.toPx())
                )
            }
        }

        // Tooltip
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
