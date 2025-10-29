package com.example.expensetrackerkotlin.ui.tutorial

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.expensetrackerkotlin.R

@Composable
fun TutorialTooltip(
    step: TutorialStep,
    stepIndex: Int,
    totalSteps: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Calculate tooltip position based on target bounds and preferred position
    val tooltipPosition = calculateTooltipPosition(
        targetBounds = step.targetBounds,
        preferredPosition = step.tooltipPosition,
        screenWidth = screenWidth.value,
        screenHeight = screenHeight.value
    )

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(300)) + scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(300)
        ),
        exit = fadeOut(animationSpec = tween(200)) + scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(200)
        )
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = (screenWidth.value * 0.85f).dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .widthIn(min = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Step indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = step.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color.White else Color.Black
                        )

                        Text(
                            text = "${stepIndex + 1}/$totalSteps",
                            fontSize = 12.sp,
                            color = if (isDarkTheme) Color.Gray else Color.DarkGray
                        )
                    }

                    // Message
                    Text(
                        text = step.message,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = if (isDarkTheme) Color.LightGray else Color.DarkGray
                    )

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onSkip) {
                            Text(
                                text = stringResource(R.string.tutorial_skip),
                                color = if (isDarkTheme) Color.Gray else Color.DarkGray
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        if (!step.requiresTap) {
                            TextButton(onClick = onNext) {
                                Text(
                                    text = if (stepIndex == totalSteps - 1)
                                        stringResource(R.string.tutorial_finish)
                                    else
                                        stringResource(R.string.tutorial_next),
                                    color = Color(0xFFFF9500),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.tutorial_tap_to_continue),
                                fontSize = 12.sp,
                                color = Color(0xFFFF9500),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun calculateTooltipPosition(
    targetBounds: Rect?,
    preferredPosition: TooltipPosition,
    screenWidth: Float,
    screenHeight: Float
): Offset {
    if (targetBounds == null) {
        // Default to center of screen if no target
        return Offset(screenWidth / 2 - 150f, screenHeight / 2)
    }

    val padding = 80f // Padding from target
    val tooltipWidth = screenWidth * 0.85f
    val tooltipHeight = 150f // Approximate height

    return when (preferredPosition) {
        TooltipPosition.TOP -> {
            val x = (targetBounds.center.x - tooltipWidth / 2).coerceIn(16f, screenWidth - tooltipWidth - 16f)
            val y = (targetBounds.top - tooltipHeight - padding).coerceAtLeast(16f)
            Offset(x, y)
        }
        TooltipPosition.BOTTOM -> {
            val x = (targetBounds.center.x - tooltipWidth / 2).coerceIn(16f, screenWidth - tooltipWidth - 16f)
            val y = (targetBounds.bottom + padding).coerceAtMost(screenHeight - tooltipHeight - 16f)
            Offset(x, y)
        }
        TooltipPosition.LEFT -> {
            val x = (targetBounds.left - tooltipWidth - padding).coerceAtLeast(16f)
            val y = (targetBounds.center.y - tooltipHeight / 2).coerceIn(16f, screenHeight - tooltipHeight - 16f)
            Offset(x, y)
        }
        TooltipPosition.RIGHT -> {
            val x = (targetBounds.right + padding).coerceAtMost(screenWidth - tooltipWidth - 16f)
            val y = (targetBounds.center.y - tooltipHeight / 2).coerceIn(16f, screenHeight - tooltipHeight - 16f)
            Offset(x, y)
        }
        TooltipPosition.AUTO -> {
            // Smart positioning: try top first, then bottom, then sides
            val spaceAbove = targetBounds.top
            val spaceBelow = screenHeight - targetBounds.bottom

            if (spaceAbove > tooltipHeight + padding + 32f) {
                // Place on top
                val x = (targetBounds.center.x - tooltipWidth / 2).coerceIn(16f, screenWidth - tooltipWidth - 16f)
                val y = targetBounds.top - tooltipHeight - padding
                Offset(x, y)
            } else if (spaceBelow > tooltipHeight + padding + 32f) {
                // Place on bottom
                val x = (targetBounds.center.x - tooltipWidth / 2).coerceIn(16f, screenWidth - tooltipWidth - 16f)
                val y = targetBounds.bottom + padding
                Offset(x, y)
            } else {
                // Place on left or right
                val spaceLeft = targetBounds.left
                val spaceRight = screenWidth - targetBounds.right

                if (spaceRight > spaceLeft) {
                    val x = (targetBounds.right + padding).coerceAtMost(screenWidth - tooltipWidth - 16f)
                    val y = (targetBounds.center.y - tooltipHeight / 2).coerceIn(16f, screenHeight - tooltipHeight - 16f)
                    Offset(x, y)
                } else {
                    val x = (targetBounds.left - tooltipWidth - padding).coerceAtLeast(16f)
                    val y = (targetBounds.center.y - tooltipHeight / 2).coerceIn(16f, screenHeight - tooltipHeight - 16f)
                    Offset(x, y)
                }
            }
        }
    }
}
