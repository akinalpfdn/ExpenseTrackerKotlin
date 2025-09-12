package com.example.expensetrackerkotlin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

@Composable
fun PlanCard(
    planWithBreakdowns: PlanWithBreakdowns,
    onCardClick: () -> Unit,
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
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
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
            
            // Status and Duration Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Badge
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = statusColor.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                ) {
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                
                Text(
                    text = PlanningUtils.formatPlanDuration(plan.durationInMonths),
                    fontSize = 14.sp,
                    color = ThemeColors.getTextGrayColor(isDarkTheme),
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar (for active plans)
            if (plan.isActive()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "İlerleme",
                            fontSize = 12.sp,
                            color = ThemeColors.getTextGrayColor(isDarkTheme)
                        )
                        Text(
                            text = "${(progressPercentage * 100).toInt()}%",
                            fontSize = 12.sp,
                            color = ThemeColors.getTextGrayColor(isDarkTheme),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = progressPercentage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = AppColors.PrimaryOrange,
                        trackColor = ThemeColors.getTextGrayColor(isDarkTheme).copy(alpha = 0.2f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // Financial Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Aylık Gelir",
                        fontSize = 12.sp,
                        color = ThemeColors.getTextGrayColor(isDarkTheme)
                    )
                    Text(
                        text = "${NumberFormatter.formatAmount(plan.monthlyIncome)} $defaultCurrency",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeColors.getTextColor(isDarkTheme)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (totalProjectedSavings >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (totalProjectedSavings >= 0) Color(0xFF34C759) else Color(0xFFFF3B30),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Toplam Net",
                            fontSize = 12.sp,
                            color = ThemeColors.getTextGrayColor(isDarkTheme)
                        )
                    }
                    Text(
                        text = "${NumberFormatter.formatAmount(totalProjectedSavings)} $defaultCurrency",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (totalProjectedSavings >= 0) Color(0xFF34C759) else Color(0xFFFF3B30)
                    )
                }
            }
        }
    }
}

