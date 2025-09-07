package com.example.expensetrackerkotlin.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun MonthlyAnalysisPieChart(
    categoryData: List<CategoryAnalysisData>,
    animatedPercentages: List<Float>,
    isDarkTheme: Boolean,
    selectedSegment: Int?,
    onSegmentSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {

    val segmentScales = categoryData.mapIndexed { index, _ ->
        val animatedScale = remember { Animatable(1f) }
        LaunchedEffect(selectedSegment) {
            if (selectedSegment == index) {
                animatedScale.animateTo(
                    targetValue = 1.1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            } else {
                animatedScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }
        animatedScale.value
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ThemeColors.getCardBackgroundColor(isDarkTheme)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {



            if (categoryData.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(265.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Pie Chart
                    Canvas(
                        modifier = Modifier
                            .size(250.dp)
                            .pointerInput(categoryData) {
                                detectTapGestures { tapOffset ->
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val radius = minOf(size.width, size.height) / 2f - 20.dp.toPx()

                                    val distance = sqrt(
                                        (tapOffset.x - center.x).pow(2) +
                                                (tapOffset.y - center.y).pow(2)
                                    )

                                    if (distance <= radius && distance >= radius * 0.45f) {
                                        val angle = atan2(
                                            tapOffset.y - center.y,
                                            tapOffset.x - center.x
                                        )
                                        var normalizedAngle = ((angle * 180f / PI.toFloat()) + 90f) % 360f
                                        if (normalizedAngle < 0) normalizedAngle += 360f

                                        var currentAngle = 0f
                                        for (i in animatedPercentages.indices) {
                                            val sweepAngle = animatedPercentages[i] * 360f
                                            if (normalizedAngle >= currentAngle && normalizedAngle <= currentAngle + sweepAngle) {
                                                onSegmentSelected(if (selectedSegment == i) null else i)
                                                break
                                            }
                                            currentAngle += sweepAngle
                                        }
                                    }
                                }
                            }
                    ) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val baseRadius = minOf(size.width, size.height) / 2 - 20.dp.toPx()

                        var currentAngle = -90f

                        animatedPercentages.forEachIndexed { index, animatedPercentage ->
                            val sweepAngle = animatedPercentage * 360f
                            val color = categoryData[index].category.getColor()
                            val scale = segmentScales[index]
                            val radius = baseRadius * scale

                            drawArc(
                                color = color,
                                startAngle = currentAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true,
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(radius * 2, radius * 2)
                            )

                            currentAngle += sweepAngle
                        }

                        drawCircle(
                            color = Color.Transparent,
                            radius = baseRadius * 0.45f,
                            center = center,
                            blendMode = BlendMode.Clear
                        )
                    }

                    // Hint text when nothing is selected
                    if (selectedSegment == null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(top = 220.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = ThemeColors.getTextGrayColor(isDarkTheme).copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Kategori seçmek için grafiğe dokunun",
                                fontSize = 11.sp,
                                color = ThemeColors.getTextGrayColor(isDarkTheme).copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
