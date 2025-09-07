package com.example.expensetrackerkotlin.ui.components
import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import kotlin.math.*
import com.example.expensetrackerkotlin.utils.NumberFormatter
import com.example.expensetrackerkotlin.viewmodel.ExpenseViewModel
@Composable
fun CategoryPopupLines(
    line1Progress: Animatable<Float, AnimationVector1D>,
    line2Progress: Animatable<Float, AnimationVector1D>,
    segmentIndex: Int,
    animatedPercentages: List<Float>,
    selected: CategoryAnalysisData
)
{
    // Two-line connector from pie segment to popup
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp) // Constrained height to prevent extending out
            .offset(y = (-250).dp) // Position to align with pie chart
            .graphicsLayer {
                alpha = maxOf(line1Progress.value, line2Progress.value)
            }
    )
    {
        // Calculate pie chart center position (at top of canvas)
        val pieChartCenterX = size.width / 2f
        val pieChartCenterY = 40.dp.toPx() // Pie chart center position
        val pieRadius = 125.dp.toPx() // Half of 250dp pie chart size

        // Calculate the actual center point of the selected segment
        // First calculate the middle angle of the segment

        var segmentStartAngle = -90f // Start from top
        for (i in 0 until segmentIndex) {
            segmentStartAngle += animatedPercentages[i] * 360f
        }
        val segmentMiddleAngle = segmentStartAngle + (animatedPercentages[segmentIndex] * 360f / 2f)
        val segmentAngleRad = segmentMiddleAngle * PI.toFloat() / 180f

        // Position at the middle of the segment thickness (between inner and outer radius)
        val segmentRadius = pieRadius * 0.725f // Middle of donut (between 0.45f inner and 1.0f outer)
        val segmentCenterX = pieChartCenterX + cos(segmentAngleRad) * segmentRadius
        val segmentCenterY = pieChartCenterY + sin(segmentAngleRad) * segmentRadius

        // Line 1: Angled connector going DOWN from segment center
        val elbowDistance = 35.dp.toPx()
        // Always go down and slightly outward based on which side of the chart we're on
        val elbowAngle = if (segmentCenterX < pieChartCenterX) {
            150f // Down and left
        } else {
            30f // Down and right
        }
        val elbowAngleRad = elbowAngle * PI.toFloat() / 180f

        val elbowX = segmentCenterX + cos(elbowAngleRad) * elbowDistance
        val elbowY = segmentCenterY + sin(elbowAngleRad) * elbowDistance

        // Line 2: Vertical line from elbow to popup (270° = straight down)
        val popupTopY = size.height - 10.dp.toPx() // End just at bottom of canvas

        // Ensure elbow point is within screen bounds
        val constrainedElbowX = elbowX.coerceIn(20.dp.toPx(), size.width - 20.dp.toPx())

        // Draw Line 1 (angled, going down) with animation
        if (line1Progress.value > 0f) {
            val line1End = Offset(
                x = segmentCenterX + (constrainedElbowX - segmentCenterX) * line1Progress.value,
                y = segmentCenterY + (elbowY - segmentCenterY) * line1Progress.value
            )

            drawLine(
                color = selected.category.getColor(),
                start = Offset(segmentCenterX, segmentCenterY),
                end = line1End,
                strokeWidth = 2.dp.toPx()
            )
        }

        // Draw Line 2 (vertical down) with animation
        if (line2Progress.value > 0f && line1Progress.value >= 1f) {
            val line2End = Offset(
                x = constrainedElbowX,
                y = elbowY + (popupTopY - elbowY) * line2Progress.value
            )

            drawLine(
                color = selected.category.getColor(),
                start = Offset(constrainedElbowX, elbowY),
                end = line2End,
                strokeWidth = 2.dp.toPx()
            )

            // Draw arrow tip only when Line 2 is complete
            if (line2Progress.value >= 1f) {
                val arrowSize = 6.dp.toPx()
                val arrowPath = Path().apply {
                    moveTo(constrainedElbowX, popupTopY) // Arrow tip (bottom)
                    lineTo(constrainedElbowX - arrowSize, popupTopY - arrowSize) // Left side
                    lineTo(constrainedElbowX + arrowSize, popupTopY - arrowSize) // Right side
                    close()
                }

                drawPath(
                    path = arrowPath,
                    color = selected.category.getColor(),
                    style = Fill
                )
            }
        }
    }
}
@SuppressLint("DefaultLocale")
@Composable
fun   CategoryPopupCard(popupScale:  Animatable<Float,
        AnimationVector1D>, selected:  CategoryAnalysisData,
                        viewModel: ExpenseViewModel,
                        onCategoryClick: (CategoryAnalysisData) -> Unit,)
{// Category Info Card with solid matte background
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .graphicsLayer {
                scaleX = popupScale.value
                scaleY = popupScale.value
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            }, onClick = {  onCategoryClick(selected)
        },
        colors = CardDefaults.cardColors(
            containerColor = selected.category.getColor().copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    )
    {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        Color.White.copy(alpha = 0.3f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = selected.category.getIcon(),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = selected.category.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${selected.expenseCount} harcama • %${String.format("%.1f", selected.percentage * 100)}",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // Amount
            Text(
                text = "${viewModel.defaultCurrency} ${NumberFormatter.formatAmount(selected.totalAmount)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
