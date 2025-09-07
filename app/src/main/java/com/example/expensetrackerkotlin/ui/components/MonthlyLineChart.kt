package com.example.expensetrackerkotlin.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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

@Composable
fun MonthlyLineChart(
    data: List<ChartDataPoint>,
    currency: String,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val maxAmount = data.maxOfOrNull { it.amount } ?: 0.0
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ThemeColors.getCardBackgroundColor(isDarkTheme))
            .padding(16.dp)
    ) {
        Text(
            text = "Aylık Harcama Trendi",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ThemeColors.getTextColor(isDarkTheme)
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        if (data.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Veri bulunamadı",
                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                )
            }
        } else {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
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
                    text = "En yüksek: $currency ${NumberFormatter.formatAmount(maxAmount)}",
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

private fun DrawScope.drawLineChart(
    data: List<ChartDataPoint>,
    maxAmount: Double,
    isDarkTheme: Boolean
) {
    val width = size.width
    val height = size.height
    val padding = 10.dp.toPx()
    
    val chartWidth = width - padding * 2
    val chartHeight = height - padding * 2
    
    // Draw grid lines
    val gridColor = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)
    for (i in 1..4) {
        val y = padding + (chartHeight / 4) * i
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(width - padding, y),
            strokeWidth = 1.dp.toPx()
        )
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

}