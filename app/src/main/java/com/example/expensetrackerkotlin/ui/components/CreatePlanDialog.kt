package com.example.expensetrackerkotlin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import com.example.expensetrackerkotlin.utils.NumberFormatter
import com.example.expensetrackerkotlin.utils.PlanningUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlanBottomSheet(
    onDismiss: () -> Unit,
    onCreatePlan: (
        name: String,
        durationInMonths: Int,
        monthlyIncome: Double,
        monthlyExpenses: Double,
        useAppExpenseData: Boolean,
        isInflationApplied: Boolean,
        inflationRate: Double
    ) -> Unit,
    isDarkTheme: Boolean,
    defaultCurrency: String
) {
    var planName by remember { mutableStateOf("") }
    var monthlyIncome by remember { mutableStateOf("") }
    var monthlyExpenses by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableStateOf(12) }
    var useAppExpenseData by remember { mutableStateOf(true) }
    var isInflationApplied by remember { mutableStateOf(false) }
    var inflationRate by remember { mutableStateOf("") }
    
    val suggestedDurations = PlanningUtils.getSuggestedPlanDurations()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
                // Header
                Text(
                    text = "Yeni Plan Oluştur",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeColors.getTextColor(isDarkTheme),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Plan Name
                Text(
                    text = "Plan Adı",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            ThemeColors.getInputBackgroundColor(isDarkTheme),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = ThemeColors.getTextGrayColor(isDarkTheme).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    BasicTextField(
                        value = planName,
                        onValueChange = { planName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 16.sp,
                            color = ThemeColors.getTextColor(isDarkTheme)
                        ),
                        decorationBox = { innerTextField ->
                            if (planName.isEmpty()) {
                                Text(
                                    text = "Örn: 2024 Tasarruf Planı",
                                    fontSize = 16.sp,
                                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                                )
                            }
                            innerTextField()
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
        Spacer(modifier = Modifier.height(16.dp))
        // Duration
        Text(
            text = "Plan Süresi",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = ThemeColors.getTextColor(isDarkTheme)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Duration chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            suggestedDurations.forEach { option ->
                FilterChip(
                    onClick = { selectedDuration = option.months },
                    label = {
                        Text(
                            text = option.displayText,
                            fontSize = 12.sp
                        )
                    },
                    selected = selectedDuration == option.months,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = ThemeColors.getInputBackgroundColor(isDarkTheme),
                        selectedContainerColor = AppColors.PrimaryOrange.copy(alpha = 0.2f),
                        labelColor = ThemeColors.getTextColor(isDarkTheme),
                        selectedLabelColor = AppColors.PrimaryOrange
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Monthly Income
                Text(
                    text = "Aylık Gelir ($defaultCurrency)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            ThemeColors.getInputBackgroundColor(isDarkTheme),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = ThemeColors.getTextGrayColor(isDarkTheme).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    BasicTextField(
                        value = monthlyIncome,
                        onValueChange = { monthlyIncome = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 16.sp,
                            color = ThemeColors.getTextColor(isDarkTheme)
                        ),
                        decorationBox = { innerTextField ->
                            if (monthlyIncome.isEmpty()) {
                                Text(
                                    text = "0",
                                    fontSize = 16.sp,
                                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                                )
                            }
                            innerTextField()
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Use App Expense Data Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = useAppExpenseData,
                        onCheckedChange = { useAppExpenseData = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = AppColors.PrimaryOrange,
                            uncheckedColor = ThemeColors.getTextGrayColor(isDarkTheme)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Uygulamanın harcama verilerini kullan",
                        fontSize = 14.sp,
                        color = ThemeColors.getTextColor(isDarkTheme)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Manual Monthly Expenses (only shown when checkbox is unchecked)
                if (!useAppExpenseData) {
                Text(
                    text = "Aylık Harcama ($defaultCurrency)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            ThemeColors.getInputBackgroundColor(isDarkTheme),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = ThemeColors.getTextGrayColor(isDarkTheme).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    BasicTextField(
                        value = monthlyExpenses,
                        onValueChange = { monthlyExpenses = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 16.sp,
                            color = ThemeColors.getTextColor(isDarkTheme)
                        ),
                        decorationBox = { innerTextField ->
                            if (monthlyExpenses.isEmpty()) {
                                Text(
                                    text = "Manuel aylık harcama miktarı",
                                    fontSize = 14.sp,
                                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                                )
                            }
                            innerTextField()
                        }
                    )
                }
                
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Manuel harcama miktarını girin",
                        fontSize = 12.sp,
                        color = ThemeColors.getTextGrayColor(isDarkTheme)
                    )
                }


        // Inflation Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Enflasyon Uygula",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = ThemeColors.getTextColor(isDarkTheme)
            )
            Switch(
                checked = isInflationApplied,
                onCheckedChange = { isInflationApplied = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AppColors.PrimaryOrange,
                    uncheckedThumbColor = ThemeColors.getTextGrayColor(isDarkTheme),
                    uncheckedTrackColor = ThemeColors.getTextGrayColor(isDarkTheme).copy(alpha = 0.3f)
                )
            )
        }

        if (isInflationApplied) {
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        ThemeColors.getInputBackgroundColor(isDarkTheme),
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = ThemeColors.getTextGrayColor(isDarkTheme).copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                BasicTextField(
                    value = inflationRate,
                    onValueChange = { inflationRate = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 16.sp,
                        color = ThemeColors.getTextColor(isDarkTheme)
                    ),
                    decorationBox = { innerTextField ->
                        Row {
                            if (inflationRate.isEmpty()) {
                                Text(
                                    text = "Yıllık % oran",
                                    fontSize = 16.sp,
                                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }


        Spacer(modifier = Modifier.height(16.dp))
                

                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ThemeColors.getTextColor(isDarkTheme)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = ThemeColors.getTextGrayColor(isDarkTheme).copy(alpha = 0.5f)
                        )
                    ) {
                        Text("İptal")
                    }
                    
                    Button(
                        onClick = {
                            val income = monthlyIncome.toDoubleOrNull() ?: 0.0
                            val expenses = monthlyExpenses.toDoubleOrNull() ?: 0.0
                            val inflation = if (isInflationApplied) inflationRate.toDoubleOrNull() ?: 0.0 else 0.0
                            
                            onCreatePlan(
                                planName.trim(),
                                selectedDuration,
                                income,
                                expenses,
                                useAppExpenseData,
                                isInflationApplied,
                                inflation
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.PrimaryOrange,
                            contentColor = Color.White
                        ),
                        enabled = planName.isNotBlank() && monthlyIncome.isNotBlank()
                    ) {
                        Text("Oluştur")
                    }
                }
            }
        }
