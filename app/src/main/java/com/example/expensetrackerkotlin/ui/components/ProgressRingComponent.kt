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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import com.example.expensetrackerkotlin.utils.NumberFormatter
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
        else -> {
                listOf(Color.Green, Color.Green, Color.Green, Color.Yellow,Color.Yellow, Color.Yellow,Color.Red,Color.Red,Color.Gray.copy(alpha = 0.2f))

        }
    }
    
    Canvas(
        modifier = modifier
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
        
        val sweepAngle = 360f * progress
        
        // Create gradient brush - start from right (0 degrees)
        val brush = if (colors.size > 1) {
            Brush.sweepGradient(
                colors = colors,
                center = center
            )
        } else {
            SolidColor(colors.firstOrNull() ?: Color.Blue)
        }
        
        // Rotate the canvas to align gradient with progress arc
        rotate(-90f, center) {
            // Progress arc - Start from top like Swift
            drawArc(
                brush = brush,
                startAngle = 0f, // Now starts from right (0 degrees) but canvas is rotated
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
                modifier = Modifier.size(120.dp),
                onClick = onTap
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$currency${NumberFormatter.formatAmount(totalSpent)}",
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
                isLimitOver = isOverDailyLimit,
                modifier = Modifier.size(120.dp)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$currency${NumberFormatter.formatAmount(selectedDateTotal)}",
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