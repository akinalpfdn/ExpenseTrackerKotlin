package com.example.expensetrackerkotlin.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.expensetrackerkotlin.R
import com.example.expensetrackerkotlin.data.PlanWithBreakdowns
import com.example.expensetrackerkotlin.data.PlanMonthlyBreakdown
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import com.example.expensetrackerkotlin.utils.NumberFormatter
import com.example.expensetrackerkotlin.utils.PlanningUtils

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetailBottomSheet(
    planWithBreakdowns: PlanWithBreakdowns,
    onUpdateBreakdown: (PlanMonthlyBreakdown) -> Unit,
    onUpdateExpenseData: () -> Unit,
    isDarkTheme: Boolean,
    defaultCurrency: String,
    modifier: Modifier = Modifier
) {
    val plan = planWithBreakdowns.plan
    val breakdowns = planWithBreakdowns.breakdowns
    
    var editingCell by remember { mutableStateOf<Pair<Int, String>?>(null) } // (rowIndex, "income"/"expenses")
    var editedValue by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = plan.name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = ThemeColors.getTextColor(isDarkTheme),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = PlanningUtils.formatPlanDateRange(plan.startDate, plan.endDate),
            fontSize = 14.sp,
            color = ThemeColors.getTextGrayColor(isDarkTheme),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Update Expenses Button (only show if plan uses app expense data)
        if (plan.useAppExpenseData) {
            OutlinedButton(
                onClick = onUpdateExpenseData,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppColors.PrimaryOrange
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = AppColors.PrimaryOrange
                )
            ) {
                Text(
                    text = stringResource(R.string.update_expense_data),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.update_expense_data_description),
                fontSize = 12.sp,
                color = ThemeColors.getTextGrayColor(isDarkTheme),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.PrimaryOrange.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.total_net_value),
                    fontSize = 14.sp,
                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                )
                
                val totalNet = breakdowns.lastOrNull()?.cumulativeNet ?: 0.0
                Text(
                    text = "${NumberFormatter.formatAmount(totalNet)} $defaultCurrency",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (totalNet >= 0) ThemeColors.getSuccessGreenColor(isDarkTheme) else ThemeColors.getDeleteRedColor(isDarkTheme)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Table Header
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .background(
                    ThemeColors.getTextGrayColor(isDarkTheme).copy(alpha = 0.1f),
                    RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
                .padding(4.dp)
                .width(400.dp), // Fixed width to enable horizontal scrolling
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Spacer(modifier = Modifier.width(26.dp)) // For edit button space
            Text(
                text = stringResource(R.string.income),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeColors.getTextColor(isDarkTheme),
                modifier = Modifier.weight(1.6f),
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.expense),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeColors.getTextColor(isDarkTheme),
                modifier = Modifier.weight(1.6f),
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.net),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeColors.getTextColor(isDarkTheme),
                modifier = Modifier.weight(1.6f),
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.total),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeColors.getTextColor(isDarkTheme),
                modifier = Modifier.weight(1.6f),
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.month),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeColors.getTextColor(isDarkTheme),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = ThemeColors.getTextGrayColor(isDarkTheme).copy(alpha = 0.2f)
        )

        // Table Content
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            itemsIndexed(breakdowns) { index, breakdown ->
                Row(
                    modifier = Modifier
                        .horizontalScroll(scrollState)
                        .background(
                            if (index % 2 == 0) Color.Transparent
                            else ThemeColors.getTextGrayColor(isDarkTheme).copy(alpha = 0.05f)
                        )
                        .padding(4.dp)
                        .width(400.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isEditingExpenses = editingCell?.first == index && editingCell?.second == "expenses"
                    val isEditingIncome = editingCell?.first == index && editingCell?.second == "income"
// Save Button (only show when editing)
                    if (isEditingIncome || isEditingExpenses) {
                        IconButton(
                            onClick = {
                                // Save the current edit
                                val newValue = editedValue.toDoubleOrNull()
                                if (newValue != null) {
                                    val updatedBreakdown = if (isEditingIncome) {
                                        breakdown.copy(
                                            projectedIncome = newValue,
                                            netAmount = newValue - breakdown.totalProjectedExpenses
                                        )
                                    } else {
                                        breakdown.copy(
                                            totalProjectedExpenses = newValue,
                                            netAmount = breakdown.projectedIncome - newValue
                                        )
                                    }
                                    onUpdateBreakdown(updatedBreakdown)
                                }
                                editingCell = null
                                editedValue = ""
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = stringResource(R.string.save),
                                tint = AppColors.PrimaryOrange,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        // Empty space to maintain layout
                        Spacer(modifier = Modifier.size(26.dp))
                    }
                    
                    // Income (clickable cell)
                    
                    if (isEditingIncome) {
                        BasicTextField(
                            value = editedValue,
                            onValueChange = { editedValue = it },
                            modifier = Modifier
                                .weight(1.6f)
                                .background(
                                    ThemeColors.getInputBackgroundColor(isDarkTheme),
                                    RoundedCornerShape(6.dp)
                                )
                                .border(
                                    1.dp,
                                    AppColors.PrimaryOrange,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 12.sp,
                                color = ThemeColors.getTextColor(isDarkTheme),
                                textAlign = TextAlign.Center
                            )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1.6f)
                                .clickable {
                                    editingCell = Pair(index, "income")
                                    editedValue =  String.format("%.2f", breakdown.projectedIncome)
                                }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = NumberFormatter.formatAmount(breakdown.projectedIncome),
                                fontSize = 12.sp,
                                color = ThemeColors.getTextColor(isDarkTheme),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    // Expenses (clickable cell)
                    
                    if (isEditingExpenses) {
                        BasicTextField(
                            value = editedValue,
                            onValueChange = { editedValue = it },
                            modifier = Modifier
                                .weight(1.6f)
                                .background(
                                    ThemeColors.getInputBackgroundColor(isDarkTheme),
                                    RoundedCornerShape(6.dp)
                                )
                                .border(
                                    1.dp,
                                    AppColors.PrimaryOrange,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 12.sp,
                                color = ThemeColors.getTextColor(isDarkTheme),
                                textAlign = TextAlign.Center
                            )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1.6f)
                                .clickable {
                                    editingCell = Pair(index, "expenses")
                                    editedValue =  String.format("%.2f", breakdown.totalProjectedExpenses)
                                }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = NumberFormatter.formatAmount(breakdown.totalProjectedExpenses),
                                fontSize = 12.sp,
                                color = ThemeColors.getTextColor(isDarkTheme),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    // Net (calculated)
                    val currentIncome = if (isEditingIncome) {
                        editedValue.toDoubleOrNull() ?: breakdown.projectedIncome
                    } else {
                        breakdown.projectedIncome
                    }
                    
                    val currentExpenses = if (isEditingExpenses) {
                        editedValue.toDoubleOrNull() ?: breakdown.totalProjectedExpenses
                    } else {
                        breakdown.totalProjectedExpenses
                    }
                    
                    val netAmount = currentIncome - currentExpenses
                    
                    Text(
                        text = NumberFormatter.formatAmount(netAmount),
                        fontSize = 12.sp,
                        color = if (netAmount >= 0) ThemeColors.getSuccessGreenColor(isDarkTheme) else ThemeColors.getDeleteRedColor(isDarkTheme),
                        modifier = Modifier.weight(1.5f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                    
                    // Cumulative (calculated)
                    Text(
                        text = NumberFormatter.formatAmount(breakdown.cumulativeNet),
                        fontSize = 12.sp,
                        color = if (breakdown.cumulativeNet >= 0) ThemeColors.getSuccessGreenColor(isDarkTheme) else ThemeColors.getDeleteRedColor(isDarkTheme),
                        modifier = Modifier.weight(1.6f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    // Month
                    Text(
                        text = PlanningUtils.getMonthName(plan, breakdown.monthIndex),
                        fontSize = 12.sp,
                        color = ThemeColors.getTextColor(isDarkTheme),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )

                }
            }
        }
    }
}