package com.example.expensetrackerkotlin.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.data.PlanWithBreakdowns
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import com.example.expensetrackerkotlin.utils.NumberFormatter
import com.example.expensetrackerkotlin.utils.PlanningUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlanCard(
    planWithBreakdowns: PlanWithBreakdowns,
    onCardClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isDarkTheme: Boolean,
    defaultCurrency: String,
    modifier: Modifier = Modifier
) {
    val plan = planWithBreakdowns.plan
    val breakdowns = planWithBreakdowns.breakdowns
    
    val totalProjectedSavings = PlanningUtils.calculateTotalProjectedSavings(breakdowns)
    val progressPercentage = plan.getProgressPercentage()
    val statusText = PlanningUtils.getPlanStatusText(plan)
    val statusColor = PlanningUtils.getPlanStatusColor(plan)
    
    // Calculate current net worth and averages
    val monthsElapsed = plan.getMonthsElapsed()
    val currentNet = if (monthsElapsed > 0 && monthsElapsed <= breakdowns.size) {
        breakdowns.getOrNull(monthsElapsed - 1)?.cumulativeNet ?: 0.0
    } else {
        0.0
    }
    
    // Calculate average monthly income and expenses
    val avgMonthlyIncome = if (breakdowns.isNotEmpty()) {
        breakdowns.sumOf { it.projectedIncome } / breakdowns.size
    } else {
        plan.monthlyIncome
    }
    
    val avgMonthlyExpenses = if (breakdowns.isNotEmpty()) {
        breakdowns.sumOf { it.totalProjectedExpenses } / breakdowns.size
    } else {
        0.0
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onCardClick() },
                onLongClick = { onDeleteClick() }
            ),
        colors = CardDefaults.cardColors(
            containerColor = ThemeColors.getCardBackgroundColor(isDarkTheme)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Text(
                text = plan.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeColors.getTextColor(isDarkTheme),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = PlanningUtils.formatPlanDateRange(plan.startDate, plan.endDate),
                fontSize = 14.sp,
                color = ThemeColors.getTextGrayColor(isDarkTheme)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Average Income and Expenses Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ort. Aylık Gelir",
                        fontSize = 12.sp,
                        color = ThemeColors.getTextGrayColor(isDarkTheme)
                    )
                    Text(
                        text = "${NumberFormatter.formatAmount(avgMonthlyIncome)} $defaultCurrency",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = ThemeColors.getSuccessGreenColor(isDarkTheme)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Ort. Aylık Gider",
                        fontSize = 12.sp,
                        color = ThemeColors.getTextGrayColor(isDarkTheme)
                    )
                    Text(
                        text = "${NumberFormatter.formatAmount(avgMonthlyExpenses)} $defaultCurrency",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = ThemeColors.getDeleteRedColor(isDarkTheme)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Net Worth Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Şu Anki Net",
                        fontSize = 12.sp,
                        color = ThemeColors.getTextGrayColor(isDarkTheme)
                    )
                    Text(
                        text = "${NumberFormatter.formatAmount(currentNet)} $defaultCurrency",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentNet >= 0) ThemeColors.getSuccessGreenColor(isDarkTheme) else ThemeColors.getDeleteRedColor(isDarkTheme)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (totalProjectedSavings >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (totalProjectedSavings >= 0) ThemeColors.getSuccessGreenColor(isDarkTheme) else ThemeColors.getDeleteRedColor(isDarkTheme),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Hedef Net",
                            fontSize = 12.sp,
                            color = ThemeColors.getTextGrayColor(isDarkTheme)
                        )
                    }
                    Text(
                        text = "${NumberFormatter.formatAmount(totalProjectedSavings)} $defaultCurrency",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (totalProjectedSavings >= 0) ThemeColors.getSuccessGreenColor(isDarkTheme) else ThemeColors.getDeleteRedColor(isDarkTheme)
                    )
                }

        }
    }
}
}
