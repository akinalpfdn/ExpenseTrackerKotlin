package com.example.expensetrackerkotlin.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun MonthlyAnalysisPieChart(
    modifier: Modifier = Modifier,
    categoryData: List<CategoryAnalysisData>,
    animatedPercentages: List<Float>,
    isDarkTheme: Boolean,
    selectedSegment: Int?,
    onSegmentSelected: (Int?) -> Unit,
) {
    // Remember the collapsed state - defaults to false (expanded)
    var isCollapsed by remember { mutableStateOf(false) }

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
            // Clickable header with expand/collapse functionality
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isCollapsed = !isCollapsed ;onSegmentSelected(null) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kategori Dağılımı",
                    fontSize = 18.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
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
                                    .pointerInput(selectedSegment) {
                                        detectTapGestures { tapOffset ->
                                            val center = Offset(size.width / 2f, size.height / 2f)
                                            val radius =
                                                minOf(size.width, size.height) / 2f - 20.dp.toPx()

                                            val distance = sqrt(
                                                (tapOffset.x - center.x).pow(2) +
                                                        (tapOffset.y - center.y).pow(2)
                                            )

                                            if (distance <= radius && distance >= radius * 0.45f) {
                                                val angle = atan2(
                                                    tapOffset.y - center.y,
                                                    tapOffset.x - center.x
                                                )
                                                var normalizedAngle =
                                                    ((angle * 180f / PI.toFloat()) + 90f) % 360f
                                                if (normalizedAngle < 0) normalizedAngle += 360f

                                                var currentAngle = 0f
                                                for (i in animatedPercentages.indices) {
                                                    val sweepAngle = animatedPercentages[i] * 360f
                                                    if (normalizedAngle >= currentAngle && normalizedAngle <= currentAngle + sweepAngle) {
                                                        // Toggle behavior: if clicking the same segment, deselect it
                                                        if (selectedSegment == i) {
                                                            onSegmentSelected(null) // Close popup
                                                        } else {
                                                            onSegmentSelected(i) // Open popup for this segment
                                                        }
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
                                    color = ThemeColors.getBackgroundColor(isDarkTheme),
                                    radius = baseRadius * 0.45f,
                                    center = center,
                                    //blendMode = BlendMode.Clear
                                )
                            }
                            
                            // Overlay category icons on pie chart segments
                            val density = LocalDensity.current
                            var iconAngle = -90f
                            animatedPercentages.forEachIndexed { index, animatedPercentage ->
                                val sweepAngle = animatedPercentage * 360f
                                if (sweepAngle > 15f) { // Only show icon if segment is large enough
                                    val middleAngle = iconAngle + (sweepAngle / 2f)
                                    val angleInRadians = Math.toRadians(middleAngle.toDouble())
                                    val iconRadius = 125.dp.value * 0.7 // Position icons at 70% of radius (125dp is half of 250dp)
                                    
                                    val iconX = iconRadius * cos(angleInRadians)
                                    val iconY = iconRadius * sin(angleInRadians)
                                    
                                    // Calculate icon size based on segment size
                                    val iconSize = (16 + (sweepAngle / 360f) * 8).coerceIn(16f, 24f)
                                    
                                    Icon(
                                        imageVector = categoryData[index].category.getIcon(),
                                        contentDescription = categoryData[index].category.name,
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(iconSize.dp)
                                            .offset(
                                                x = iconX.dp,
                                                y = iconY.dp
                                            )
                                    )
                                }
                                iconAngle += sweepAngle
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
                                        tint = ThemeColors.getTextGrayColor(isDarkTheme)
                                            .copy(alpha = 0.6f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Kategori seçmek için grafiğe dokunun",
                                        fontSize = 11.sp,
                                        color = ThemeColors.getTextGrayColor(isDarkTheme)
                                            .copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
