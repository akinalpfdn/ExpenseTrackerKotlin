package com.example.expensetrackerkotlin.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.data.Category

data class CategoryExpense(
    val category: Category,
    val amount: Double,
    val percentage: Double
)

@Composable
fun CategoryDistributionChart(
    categoryExpenses: List<CategoryExpense>,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (categoryExpenses.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                Text(
                    text = "Veri yok",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        } else {
            // Animation for pie chart segments
            val animatedPercentages = categoryExpenses.map { data ->
                val animatedValue = remember { Animatable(0f) }
                LaunchedEffect(data.percentage) {
                    animatedValue.animateTo(
                        targetValue = data.percentage.toFloat(),
                        animationSpec = tween(durationMillis = 1200, easing = EaseInOutCubic)
                    )
                }
                animatedValue.value
            }

            Canvas(
                modifier = Modifier.size(120.dp)
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2 - 20.dp.toPx()
                
                var currentAngle = -90f // Start from top like Swift
                
                categoryExpenses.forEachIndexed { index, categoryExpense ->
                    val animatedPercentage = if (index < animatedPercentages.size) animatedPercentages[index] else 0f
                    val sweepAngle = (animatedPercentage * 360f)
                    val color = categoryExpense.category.getColor()
                    
                    if (sweepAngle > 0f) {
                        drawArc(
                            color = color,
                            startAngle = currentAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2)
                        )
                    }
                    
                    currentAngle += (categoryExpense.percentage * 360).toFloat()
                }
                
                // Draw center circle to create donut effect
                drawCircle(
                    color = Color.Black,
                    radius = radius * 0.5f,
                    center = center
                )
            }
            
            Text(
                text = "Kategori",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}