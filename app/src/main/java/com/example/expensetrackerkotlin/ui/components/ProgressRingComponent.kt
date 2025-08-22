package com.example.expensetrackerkotlin.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ProgressRing(
    progress: Float,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 8.dp,
    onClick: (() -> Unit)? = null
) {
    Canvas(
        modifier = modifier
            .size(120.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
    ) {
        val canvasSize = size.minDimension
        val strokeWidthPx = strokeWidth.toPx()
        val radius = (canvasSize - strokeWidthPx) / 2
        val center = Offset(size.width / 2, size.height / 2)
        
        // Background ring
        drawCircle(
            color = Color.Gray.copy(alpha = 0.2f),
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )
        
        if (progress > 0) {
            val sweepAngle = 360f * progress
            
            // Create gradient brush - rotate colors to start from top
            val brush = if (colors.size > 1) {
                val rotatedColors = colors.drop(1) + colors.take(1) // Rotate colors to start from top
                Brush.sweepGradient(
                    colors = rotatedColors,
                    center = center
                )
            } else {
                SolidColor(colors.firstOrNull() ?: Color.Blue)
            }
            
            // Progress arc - Start from top like Swift
            drawArc(
                brush = brush,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun MonthlyProgressRingView(
    totalSpent: Double,
    progressPercentage: Double,
    progressColors: List<Color>,
    isOverLimit: Boolean,
    onTap: () -> Unit,
    currency: String = "₺",
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            ProgressRing(
                progress = progressPercentage.toFloat(),
                colors = progressColors,
                onClick = onTap
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$currency${String.format("%.0f", totalSpent)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverLimit) Color.Red else Color.White
                )
                Text(
                    text = "Aylık",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun DailyProgressRingView(
    dailyProgressPercentage: Double,
    isOverDailyLimit: Boolean,
    dailyLimitValue: Double,
    selectedDateTotal: Double,
    currency: String = "₺",
    modifier: Modifier = Modifier
) {
    val progressColors = when {
        isOverDailyLimit -> listOf(Color.Red, Color.Red, Color.Red, Color.Red)
        dailyProgressPercentage < 0.3 -> listOf(Color.Green, Color.Green, Color.Green, Color.Green)
        dailyProgressPercentage < 0.6 -> listOf(Color.Green, Color.Green, Color.Yellow, Color.Yellow)
        dailyProgressPercentage < 0.9 -> listOf(Color.Green, Color.Yellow, Color(0xFFFFA500), Color(0xFFFFA500))
        else -> listOf(Color.Green, Color.Yellow, Color(0xFFFFA500), Color.Red)
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            ProgressRing(
                progress = dailyProgressPercentage.toFloat(),
                colors = progressColors
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$currency${String.format("%.0f", selectedDateTotal)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverDailyLimit) Color.Red else Color.White
                )
                Text(
                    text = "Günlük",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}