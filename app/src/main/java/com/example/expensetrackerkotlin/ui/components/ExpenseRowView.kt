package com.example.expensetrackerkotlin.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.expensetrackerkotlin.R
import com.example.expensetrackerkotlin.data.Expense
import com.example.expensetrackerkotlin.data.RecurrenceType
import com.example.expensetrackerkotlin.utils.NumberFormatter
import androidx.compose.runtime.collectAsState
import java.time.format.DateTimeFormatter
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import java.text.DecimalFormatSymbols
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExpenseRowView(
    expense: Expense,
    onUpdate: (Expense) -> Unit,
    onEditingChanged: (Boolean) -> Unit,
    onDelete: () -> Unit,
    isCurrentlyEditing: Boolean,
    dailyExpenseRatio: Double,
    defaultCurrency: String,
    isDarkTheme: Boolean = true,
    viewModel: com.example.expensetrackerkotlin.viewmodel.ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    var editAmount by remember { mutableStateOf(String.format("%.2f", expense.amount).removeSuffix(".00").replace(",", ".")) }
    var editDescription by remember { mutableStateOf(expense.description) }
    var editExchangeRate by remember { mutableStateOf(expense.exchangeRate?.let { String.format("%.2f", it).removeSuffix(".00").replace(",", ".") } ?: "") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Collect categories and subcategories from ViewModel
    val categories by viewModel.categories.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()

    // Get category and subcategory for this expense
    val category = categories.find { it.id == expense.categoryId }
    val subCategory = subCategories.find { it.id == expense.subCategoryId }

    // Update edit fields when editing starts
    LaunchedEffect(isCurrentlyEditing) {
        if (isCurrentlyEditing) {
            editAmount = String.format("%.2f", expense.amount).removeSuffix(".00").replace(",", ".")
            editDescription = expense.description
            editExchangeRate = expense.exchangeRate?.let { String.format("%.2f", it).removeSuffix(".00").replace(",", ".") } ?: ""
        }
    }


    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        ) {
            
            // Main card content
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            if (isCurrentlyEditing) {
                                // If already editing, close the edit mode
                                onEditingChanged(false)
                                // Reset edit fields to original values
                                editAmount = String.format("%.2f", expense.amount).removeSuffix(".00").replace(",", ".")
                                editDescription = expense.description
                                editExchangeRate = expense.exchangeRate?.let { String.format("%.2f", it).removeSuffix(".00").replace(",", ".") } ?: ""
                            } else {
                                // If not editing, open edit mode
                                onEditingChanged(true)
                            }
                        },
                        onLongClick = {
                            showDeleteConfirmation = true
                        }
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ThemeColors.getCardBackgroundColor(isDarkTheme)
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        (category?.getColor() ?: Color.Gray).copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = category?.getIcon() ?: Icons.Default.Category,
                                    contentDescription = category?.name ?: "Category",
                                    tint = category?.getColor() ?: Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    text = subCategory?.name ?: stringResource(R.string.unknown),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = ThemeColors.getTextColor(isDarkTheme),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (expense.description.isNotBlank()) {
                                    Text(
                                        text = expense.description,
                                        fontSize = 14.sp,
                                        color = ThemeColors.getTextGrayColor(isDarkTheme),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "${expense.currency} ${NumberFormatter.formatAmount(expense.amount)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = ThemeColors.getTextColor(isDarkTheme)
                            )
                            if (expense.exchangeRate != null) {
                                Text(
                                    text = "${stringResource(R.string.exchange_rate)}: ${String.format("%.4f", expense.exchangeRate)}",
                                    fontSize = 12.sp,
                                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                                )
                                Text(
                                    text = "$defaultCurrency ${NumberFormatter.formatAmount(expense.getAmountInDefaultCurrency(defaultCurrency))}",
                                    fontSize = 12.sp,
                                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                                )
                            }
                                                         Text(
                                 text = expense.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                 fontSize = 13.sp,
                                 color = ThemeColors.getTextGrayColor(isDarkTheme)
                             )
                             if (expense.recurrenceType != RecurrenceType.NONE) {
                                 Row(
                                     verticalAlignment = Alignment.CenterVertically,
                                     horizontalArrangement = Arrangement.spacedBy(4.dp)
                                 ) {
                                     Icon(
                                         imageVector = Icons.Default.Refresh,
                                         contentDescription = "Recurring",
                                         tint = ThemeColors.getTextGrayColor(isDarkTheme),
                                         modifier = Modifier.size(10.dp)
                                     )
                                     Text(
                                         text = when (expense.recurrenceType) {
                                             RecurrenceType.DAILY -> stringResource(R.string.daily)
                                             RecurrenceType.WEEKDAYS -> stringResource(R.string.weekdays)
                                             RecurrenceType.WEEKLY -> stringResource(R.string.weekly)
                                             RecurrenceType.MONTHLY -> stringResource(R.string.monthly)
                                             else -> ""
                                         },
                                         fontSize = 11.sp,
                                         color = ThemeColors.getTextGrayColor(isDarkTheme)
                                     )
                                 }
                             }
                        }
                    }
                    
                    AnimatedVisibility(
                        visible = isCurrentlyEditing,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .background(
                                    ThemeColors.getCardBackgroundColor(isDarkTheme),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                                                        Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .background(
                                        ThemeColors.getInputBackgroundColor(isDarkTheme),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = ThemeColors.getTextGrayColor(isDarkTheme),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                BasicTextField(
                                    value = editAmount,
                                    onValueChange = { newValue ->
                                        val filtered = newValue.filter { "0123456789.,".contains(it) }
                                        val components = filtered.split(",", ".")
                                        val integerPart = components[0].filter { it.isDigit() }
                                        val decimalPart = if (components.size > 1) components[1].filter { it.isDigit() } else ""

                                        // Limit: 9 digits integer + 2 digits decimal
                                        if (integerPart.length <= 9 && decimalPart.length <= 2) {
                                            editAmount = if (components.size > 2 || (decimalPart.isNotEmpty() && components.size > 1)) {
                                                if (decimalPart.isNotEmpty()) "${integerPart}.${decimalPart}" else "${integerPart}."
                                            } else if (filtered.contains(",") || filtered.contains(".")) {
                                                "${integerPart}."
                                            } else {
                                                integerPart
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 14.sp,
                                        color = ThemeColors.getTextColor(isDarkTheme)
                                    ),
                                    visualTransformation = ThousandSeparatorTransformation(),
                                    decorationBox = { innerTextField ->
                                        Box(
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (editAmount.isEmpty()) {
                                                Text(
                                                    text = stringResource(R.string.amount),
                                                    fontSize = 14.sp,
                                                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .background(
                                        ThemeColors.getInputBackgroundColor(isDarkTheme),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = ThemeColors.getTextGrayColor(isDarkTheme),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                BasicTextField(
                                    value = editDescription,
                                    onValueChange = { editDescription = it },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 14.sp,
                                        color = ThemeColors.getTextColor(isDarkTheme)
                                    ),
                                    decorationBox = { innerTextField ->
                                        Box(
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (editDescription.isEmpty()) {
                                                Text(
                                                    text = stringResource(R.string.description),
                                                    fontSize = 14.sp,
                                                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                            
                            // Exchange Rate (only show if currency is different from default)
                            if (expense.currency != defaultCurrency) {
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .background(
                                            ThemeColors.getInputBackgroundColor(isDarkTheme),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = ThemeColors.getTextGrayColor(isDarkTheme),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    BasicTextField(
                                        value = editExchangeRate,
                                        onValueChange = { newValue ->
                                            val filtered = newValue.filter { "0123456789.,".contains(it) }
                                            val components = filtered.split(",", ".")
                                            val integerPart = components[0].filter { it.isDigit() }
                                            val decimalPart = if (components.size > 1) components[1].filter { it.isDigit() } else ""

                                            // Limit: 9 digits integer + 2 digits decimal
                                            if (integerPart.length <= 9 && decimalPart.length <= 2) {
                                                editExchangeRate = if (components.size > 2 || (decimalPart.isNotEmpty() && components.size > 1)) {
                                                    if (decimalPart.isNotEmpty()) "${integerPart}.${decimalPart}" else "${integerPart}."
                                                } else if (filtered.contains(",") || filtered.contains(".")) {
                                                    "${integerPart}."
                                                } else {
                                                    integerPart
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 12.dp),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            fontSize = 14.sp,
                                            color = ThemeColors.getTextColor(isDarkTheme)
                                        ),
                                        visualTransformation = ThousandSeparatorTransformation(),
                                        decorationBox = { innerTextField ->
                                            Box(
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                if (editExchangeRate.isEmpty()) {
                                                    Text(
                                                        text = stringResource(R.string.exchange_rate_field),
                                                        fontSize = 14.sp,
                                                        color = ThemeColors.getTextGrayColor(isDarkTheme)
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        }
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Button(
                                    onClick = {
                                        showDeleteConfirmation = true
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Red
                                    ),
                                    shape = RoundedCornerShape(26.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(R.string.delete), fontSize = 12.sp, color = Color.White)
                                    }
                                }
                                Button(
                                    onClick = {
                                        val newAmount = editAmount.toDoubleOrNull()
                                        if (newAmount != null && newAmount > 0) {
                                            onUpdate(
                                                expense.copy(
                                                    amount = newAmount,
                                                    description = editDescription,
                                                    exchangeRate = if (expense.currency != defaultCurrency) {
                                                        editExchangeRate.toDoubleOrNull()
                                                    } else {
                                                        null
                                                    }
                                                )
                                            )
                                        }
                                        onEditingChanged(false)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppColors.PrimaryOrange
                                    ),
                                    shape = RoundedCornerShape(26.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Save",
                                            tint = ThemeColors.getTextColor(isDarkTheme),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(R.string.save), fontSize = 12.sp, color = ThemeColors.getTextColor(isDarkTheme))
                                    }
                                }


                                

                            }
                        }
                    }
                }
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color.Gray.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(dailyExpenseRatio.toFloat())
                    .background(category?.getColor() ?: Color.Gray)
            )
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
            },
            title = {
                Text(
                    text = stringResource(R.string.delete_expense),
                    color = ThemeColors.getTextColor(isDarkTheme),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.delete_expense_confirmation),
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text(
                        stringResource(R.string.delete), 
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                    }
                ) {
                    Text(
                        stringResource(R.string.cancel), 
                        color = ThemeColors.getTextColor(isDarkTheme)
                    )
                }
            },
            containerColor = ThemeColors.getDialogBackgroundColor(isDarkTheme)
        )
    }
}

class ThousandSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text

        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        // Get locale-specific decimal separator
        val symbols = DecimalFormatSymbols.getInstance(Locale.getDefault())
        val decimalSeparator = symbols.decimalSeparator
        val groupingSeparator = symbols.groupingSeparator

        // Split by decimal separator
        val parts = originalText.split(decimalSeparator)
        val integerPart = parts[0].filter { it.isDigit() }
        val decimalPart = if (parts.size > 1) parts[1] else ""

        // Format integer part with thousand separators
        val formattedInteger = if (integerPart.isNotEmpty()) {
            integerPart.reversed().chunked(3).joinToString(groupingSeparator.toString()).reversed()
        } else {
            ""
        }

        // Combine integer and decimal parts
        val formattedText = if (decimalPart.isNotEmpty() || originalText.contains(decimalSeparator)) {
            "$formattedInteger$decimalSeparator$decimalPart"
        } else {
            formattedInteger
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset == 0) return 0

                val beforeDecimal = originalText.substringBefore(decimalSeparator)
                val digitsBeforeOffset = beforeDecimal.substring(0, minOf(offset, beforeDecimal.length)).filter { it.isDigit() }

                if (digitsBeforeOffset.isEmpty()) return 0

                // Calculate how many separators are before this offset
                val separatorsCount = (digitsBeforeOffset.length - 1) / 3
                val baseOffset = digitsBeforeOffset.length + separatorsCount

                // If offset is after decimal separator
                return if (offset > beforeDecimal.length) {
                    baseOffset + 1 + (offset - beforeDecimal.length - 1)
                } else {
                    baseOffset
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset == 0) return 0

                val formattedBeforeDecimal = formattedText.substringBefore(decimalSeparator)

                if (offset <= formattedBeforeDecimal.length) {
                    // Count only digits up to this offset in formatted text
                    val digitsCount = formattedBeforeDecimal.substring(0, offset).count { it.isDigit() }
                    return digitsCount
                } else {
                    // After decimal separator
                    val digitsInInteger = formattedBeforeDecimal.count { it.isDigit() }
                    val offsetAfterDecimal = offset - formattedBeforeDecimal.length - 1
                    return digitsInInteger + 1 + offsetAfterDecimal
                }
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}