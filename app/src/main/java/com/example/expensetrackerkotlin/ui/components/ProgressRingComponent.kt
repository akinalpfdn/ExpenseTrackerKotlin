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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ProgressRing(
    progress: Float,
    isLimitOver: Boolean = false,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 8.dp,
    onClick: (() -> Unit)? = null
) {
    val colors = when {
        isLimitOver -> listOf(Color.Red, Color.Red, Color.Red, Color.Red)
        else -> listOf(Color.Green, Color.Green, Color.Green, Color.Yellow,Color.Yellow, Color.Yellow,Color.Red,Color.Red,Color.Gray.copy(alpha = 0.2f))
    }
    Box(
        contentAlignment = Alignment.Center,
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
        Canvas(
            modifier = Modifier
                .size(120.dp)
                .rotate(-90f)
                .graphicsLayer {
                    rotationY = 360f
                }
        ) {
            val canvasSize = size.minDimension
            val strokeWidthPx = strokeWidth.toPx()
            val radius = (canvasSize - strokeWidthPx) / 2
            val center = Offset(size.width / 2, size.height / 2)
            
            // Background ring
            drawArc(
                color = Color.Gray.copy(alpha = 0.2f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
            
            if (progress > 0) {
                val sweepAngle = progress * 359f  // 360 yerine 359 kullanıyoruz
                
                // Create gradient brush with green-yellow-red colors
                val brush = Brush.sweepGradient(
                    colors = colors,
                    center = center
                )
                
                // Progress arc
                drawArc(
                    brush = brush,
                    startAngle = 0f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
fun MonthlyProgressRingView(
    totalSpent: Double,
    progressPercentage: Double,
    isOverLimit: Boolean,
    onTap: () -> Unit,
    currency: String = "₺",
    isDarkTheme: Boolean = true,
    modifier: Modifier = Modifier,
    month: String = ""
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
                isLimitOver = isOverLimit,
                onClick = onTap
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$currency${String.format("%.0f", totalSpent)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverLimit) Color.Red else ThemeColors.getTextColor(isDarkTheme)
                )
                Text(
                    text = if (month.isNotEmpty()) month else "Aylık",
                    fontSize = 12.sp,
                    color = ThemeColors.getTextGrayColor(isDarkTheme)
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
    isDarkTheme: Boolean = true,
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
                progress = dailyProgressPercentage.toFloat(),
                isLimitOver = isOverDailyLimit
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$currency${String.format("%.0f", selectedDateTotal)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverDailyLimit) Color.Red else ThemeColors.getTextColor(isDarkTheme)
                )
                Text(
                    text = "Günlük",
                    fontSize = 12.sp,
                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                )
            }
        }
    }
}