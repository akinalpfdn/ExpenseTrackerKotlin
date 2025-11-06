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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.example.expensetrackerkotlin.R
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import com.example.expensetrackerkotlin.data.InterestType
import com.example.expensetrackerkotlin.utils.PlanningUtils
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import java.text.DecimalFormatSymbols
import java.util.Locale

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
        inflationRate: Double,
        isInterestApplied: Boolean,
        interestRate: Double,
        interestType: InterestType
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
    var isInterestApplied by remember { mutableStateOf(false) }
    var interestRate by remember { mutableStateOf("") }
    var selectedInterestType by remember { mutableStateOf(InterestType.COMPOUND) }
    
    val context = LocalContext.current
    val suggestedDurations = PlanningUtils.getSuggestedPlanDurations(context)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
                // Header
                Text(
                    text = stringResource(R.string.create_new_plan),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeColors.getTextColor(isDarkTheme),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Plan Name
                Text(
                    text = stringResource(R.string.plan_name),
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
                                    text = stringResource(R.string.plan_name_example),
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
            text = stringResource(R.string.plan_duration),
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

        // Interest Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.apply_interest_to_savings),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = ThemeColors.getTextColor(isDarkTheme)
            )
            Switch(
                checked = isInterestApplied,
                onCheckedChange = { isInterestApplied = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AppColors.PrimaryOrange,
                    uncheckedThumbColor = ThemeColors.getTextGrayColor(isDarkTheme),
                    uncheckedTrackColor = ThemeColors.getTextGrayColor(isDarkTheme).copy(alpha = 0.3f)
                )
            )
        }

        if (isInterestApplied) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    onClick = { selectedInterestType = InterestType.SIMPLE },
                    label = {
                        Text(
                            text = stringResource(R.string.simple_interest),
                            fontSize = 12.sp
                        )
                    },
                    selected = selectedInterestType == InterestType.SIMPLE,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = ThemeColors.getInputBackgroundColor(isDarkTheme),
                        selectedContainerColor = AppColors.PrimaryOrange.copy(alpha = 0.2f),
                        labelColor = ThemeColors.getTextColor(isDarkTheme),
                        selectedLabelColor = AppColors.PrimaryOrange
                    )
                )

                FilterChip(
                    onClick = { selectedInterestType = InterestType.COMPOUND },
                    label = {
                        Text(
                            text = stringResource(R.string.compound_interest),
                            fontSize = 12.sp
                        )
                    },
                    selected = selectedInterestType == InterestType.COMPOUND,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = ThemeColors.getInputBackgroundColor(isDarkTheme),
                        selectedContainerColor = AppColors.PrimaryOrange.copy(alpha = 0.2f),
                        labelColor = ThemeColors.getTextColor(isDarkTheme),
                        selectedLabelColor = AppColors.PrimaryOrange
                    )
                )
            }
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
                    value = interestRate,
                    onValueChange = { interestRate = it },
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
                            if (interestRate.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.annual_interest_rate),
                                    fontSize = 16.sp,
                                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.interest_applied_to_positive_savings),
                fontSize = 12.sp,
                color = ThemeColors.getTextGrayColor(isDarkTheme)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Interest Type Selection
            Text(
                text = stringResource(R.string.interest_type),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = ThemeColors.getTextColor(isDarkTheme)
            )



        }

        Spacer(modifier = Modifier.height(16.dp))

        // Monthly Income
                Text(
                    text = stringResource(R.string.monthly_income_currency, defaultCurrency),
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
                        onValueChange = { newValue ->
                            val filtered = newValue.filter { "0123456789.,".contains(it) }
                            val components = filtered.split(",", ".")
                            val integerPart = components[0].filter { it.isDigit() }
                            val decimalPart = if (components.size > 1) components[1].filter { it.isDigit() } else ""

                            // Limit: 9 digits integer + 2 digits decimal
                            if (integerPart.length <= 9 && decimalPart.length <= 2) {
                                monthlyIncome = if (components.size > 2 || (decimalPart.isNotEmpty() && components.size > 1)) {
                                    if (decimalPart.isNotEmpty()) "${integerPart}.${decimalPart}" else "${integerPart}."
                                } else if (filtered.contains(",") || filtered.contains(".")) {
                                    "${integerPart}."
                                } else {
                                    integerPart
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 16.sp,
                            color = ThemeColors.getTextColor(isDarkTheme)
                        ),
                        visualTransformation = ThousandSeparatorTransformation(),
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
                        text = stringResource(R.string.use_app_expense_data),
                        fontSize = 14.sp,
                        color = ThemeColors.getTextColor(isDarkTheme)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Manual Monthly Expenses (only shown when checkbox is unchecked)
                if (!useAppExpenseData) {
                Text(
                    text = stringResource(R.string.monthly_expenses_currency, defaultCurrency),
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
                        onValueChange = { newValue ->
                            val filtered = newValue.filter { "0123456789.,".contains(it) }
                            val components = filtered.split(",", ".")
                            val integerPart = components[0].filter { it.isDigit() }
                            val decimalPart = if (components.size > 1) components[1].filter { it.isDigit() } else ""

                            // Limit: 9 digits integer + 2 digits decimal
                            if (integerPart.length <= 9 && decimalPart.length <= 2) {
                                monthlyExpenses = if (components.size > 2 || (decimalPart.isNotEmpty() && components.size > 1)) {
                                    if (decimalPart.isNotEmpty()) "${integerPart}.${decimalPart}" else "${integerPart}."
                                } else if (filtered.contains(",") || filtered.contains(".")) {
                                    "${integerPart}."
                                } else {
                                    integerPart
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 16.sp,
                            color = ThemeColors.getTextColor(isDarkTheme)
                        ),
                        visualTransformation = ThousandSeparatorTransformation(),
                        decorationBox = { innerTextField ->
                            if (monthlyExpenses.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.manual_monthly_expense_amount),
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
                        text = stringResource(R.string.enter_manual_expense_amount),
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
                text = stringResource(R.string.apply_inflation),
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
                                    text = stringResource(R.string.annual_percentage_rate),
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
                        Text(stringResource(R.string.cancel))
                    }
                    
                    Button(
                        onClick = {
                            val income = monthlyIncome.toDoubleOrNull() ?: 0.0
                            val expenses = monthlyExpenses.toDoubleOrNull() ?: 0.0
                            val inflation = if (isInflationApplied) inflationRate.toDoubleOrNull() ?: 0.0 else 0.0
                            val interest = if (isInterestApplied) interestRate.toDoubleOrNull() ?: 0.0 else 0.0

                            onCreatePlan(
                                planName.trim(),
                                selectedDuration,
                                income,
                                expenses,
                                useAppExpenseData,
                                isInflationApplied,
                                inflation,
                                isInterestApplied,
                                interest,
                                selectedInterestType
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.PrimaryOrange,
                            contentColor = Color.White
                        ),
                        enabled = planName.isNotBlank() && monthlyIncome.isNotBlank()
                    ) {
                        Text(stringResource(R.string.create))
                    }
                }
            }
        }

