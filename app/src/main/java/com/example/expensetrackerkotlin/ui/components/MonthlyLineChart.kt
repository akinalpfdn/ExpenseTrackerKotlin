package com.example.expensetrackerkotlin.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import com.example.expensetrackerkotlin.utils.NumberFormatter
import kotlin.math.max

data class ChartDataPoint(
    val day: Int,
    val amount: Double
)

enum class ExpenseFilterType(val displayName: String) {
    ALL("Tümü"),
    RECURRING("Tekrarlayan"),
    ONE_TIME("Tek Seferlik")
}

@Composable
fun MonthlyLineChart(
    modifier: Modifier = Modifier,
    data: List<ChartDataPoint>,
    currency: String,
    isDarkTheme: Boolean,
) {
    // Remember the collapsed state - defaults to false (expanded)
    var isCollapsed by remember { mutableStateOf(false) }
    val maxAmount = data.maxOfOrNull { it.amount } ?: 0.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ThemeColors.getCardBackgroundColor(isDarkTheme))
            .padding(16.dp)
    ) {
        // Clickable header with expand/collapse functionality
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isCollapsed = !isCollapsed },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Aylık Harcama Trendi",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeColors.getTextColor(isDarkTheme)
            )
            Icon(
                imageVector = if (isCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                contentDescription = if (isCollapsed) "Genişlet" else "Daralt",
                tint = ThemeColors.getTextGrayColor(isDarkTheme),
                modifier = Modifier.size(24.dp)
            )
        }

        // Collapsible content
        AnimatedVisibility(
            visible = !isCollapsed,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {


                if (data.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Veri bulunamadı",
                            color = ThemeColors.getTextGrayColor(isDarkTheme)
                        )
                    }
                } else {
                    // Y-axis label
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {


                        // Chart area
                        Canvas(
                            modifier = Modifier
                                .weight(1f)
                                .height(250.dp)
                        ) {
                            drawLineChart(
                                data = data,
                                maxAmount = maxAmount,
                                isDarkTheme = isDarkTheme
                            )
                        }
                    }

                    // Legend
                    if (data.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "En yüksek: $currency ${
                                    NumberFormatter.formatAmount(
                                        maxAmount
                                    )
                                }",
                                fontSize = 12.sp,
                                color = ThemeColors.getTextGrayColor(isDarkTheme)
                            )

                            val avgAmount = data.map { it.amount }.average()
                            Text(
                                text = "Ortalama: $currency ${NumberFormatter.formatAmount(avgAmount)}",
                                fontSize = 12.sp,
                                color = ThemeColors.getTextGrayColor(isDarkTheme)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawLineChart(
        data: List<ChartDataPoint>,
        maxAmount: Double,
        isDarkTheme: Boolean,
    ) {
        val width = size.width
        val height = size.height
        val padding = 25.dp.toPx()

        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        val textColor = if (isDarkTheme) Color.White else Color.Black
        val textColorArgb = textColor.toArgb()

        // Draw Y-axis labels and grid lines
        val gridColor =
            if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)
        for (i in 0..4) {
            val y = padding + (chartHeight / 4) * i
            val amount = maxAmount * (4 - i) / 4

            // Draw grid line
            if (i > 0) {
                drawLine(
                    color = gridColor,
                    start = Offset(padding, y),
                    end = Offset(width - padding, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw Y-axis label
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = textColorArgb
                    textSize = 10.dp.toPx()
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
                drawText(
                    NumberFormatter.formatAmount(amount),
                    padding - 1.dp.toPx(),
                    y + 4.dp.toPx(),
                    paint
                )
            }
        }

        // Draw X-axis labels (every 3rd day)
        val xAxisStep = max(1, data.size / 10) // Show about 10 labels max
        for (i in data.indices step xAxisStep) {
            val x = padding + (chartWidth / max(1, data.size - 1)) * i
            val day = data[i].day

            // Draw X-axis label
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = textColorArgb
                    textSize = 10.dp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(
                    day.toString(),
                    x,
                    height - padding + 15.dp.toPx(),
                    paint
                )
            }
        }

        if (data.isEmpty() || maxAmount == 0.0) return

        // Create path for line
        val path = Path()
        val points = mutableListOf<Offset>()

        data.forEachIndexed { index, point ->
            val x = padding + (chartWidth / max(1, data.size - 1)) * index
            val y = padding + chartHeight - (point.amount / maxAmount * chartHeight).toFloat()

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            points.add(Offset(x, y))
        }

        // Draw gradient area under line
        val gradientPath = Path().apply {
            addPath(path)
            lineTo(points.last().x, height - padding)
            lineTo(padding, height - padding)
            close()
        }

        drawPath(
            path = gradientPath,
            color = AppColors.PrimaryOrange.copy(alpha = 0.1f)
        )

        // Draw line
        drawPath(
            path = path,
            color = AppColors.PrimaryOrange,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw points
        points.forEach { point ->
            drawCircle(
                color = AppColors.PrimaryOrange,
                radius = 3.dp.toPx(),
                center = point
            )
            drawCircle(
                color = if (isDarkTheme) Color.Black else Color.White,
                radius = 1.dp.toPx(),
                center = point
            )
        }
    }
