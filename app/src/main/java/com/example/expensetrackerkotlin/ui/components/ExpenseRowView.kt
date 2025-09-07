package com.example.expensetrackerkotlin.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.data.Expense
import com.example.expensetrackerkotlin.data.RecurrenceType
import com.example.expensetrackerkotlin.utils.NumberFormatter
import androidx.compose.runtime.collectAsState
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
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
    isRecurringExpenseMode: Boolean = false,
    viewModel: com.example.expensetrackerkotlin.viewmodel.ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var editAmount by remember { mutableStateOf(expense.amount.toString()) }
    var editDescription by remember { mutableStateOf(expense.description) }
    var editExchangeRate by remember { mutableStateOf(expense.exchangeRate?.toString() ?: "") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    // Collect categories and subcategories from ViewModel
    val categories by viewModel.categories.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()
    
    // Get category and subcategory for this expense
    val category = categories.find { it.id == expense.categoryId }
    val subCategory = subCategories.find { it.id == expense.subCategoryId }
    

    LaunchedEffect(isCurrentlyEditing) {
        isEditing = isCurrentlyEditing
        onEditingChanged(isEditing)
        
        // Update edit fields when editing starts
        if (isCurrentlyEditing) {
            editAmount = expense.amount.toString()
            editDescription = expense.description
            editExchangeRate = expense.exchangeRate?.toString() ?: ""
        }
    }
    
    // Update edit fields when expense changes
    LaunchedEffect(expense) {
        if (isEditing) {
            editAmount = expense.amount.toString()
            editDescription = expense.description
            editExchangeRate = expense.exchangeRate?.toString() ?: ""
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
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                // Long press to show delete confirmation
                                showDeleteConfirmation = true
                            }
                        )
                    }
                    .clickable {
                        if (isEditing) {
                            // If already editing, close the edit mode
                            isEditing = false
                            onEditingChanged(false)
                            // Reset edit fields to original values
                            editAmount = expense.amount.toString()
                            editDescription = expense.description
                            editExchangeRate = expense.exchangeRate?.toString() ?: ""
                        } else {
                            // If not editing, open edit mode
                            isEditing = true
                            onEditingChanged(true)
                        }
                    },
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
                                    imageVector = category?.getIcon() ?: androidx.compose.material.icons.Icons.Default.Category,
                                    contentDescription = category?.name ?: "Category",
                                    tint = category?.getColor() ?: Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    text = subCategory?.name ?: "Unknown",
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
                                    text = "Kur: ${String.format("%.4f", expense.exchangeRate)}",
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
                                             RecurrenceType.DAILY -> "Her gün"
                                             RecurrenceType.WEEKDAYS -> "Hafta içi"
                                             RecurrenceType.WEEKLY -> "Haftalık"
                                             RecurrenceType.MONTHLY -> "Aylık"
                                             RecurrenceType.NONE -> ""
                                         },
                                         fontSize = 11.sp,
                                         color = ThemeColors.getTextGrayColor(isDarkTheme)
                                     )
                                 }
                             }
                        }
                    }
                    
                    AnimatedVisibility(
                        visible = isEditing,
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
                                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                            editAmount = newValue
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
                                    decorationBox = { innerTextField ->
                                        Box(
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (editAmount.isEmpty()) {
                                                Text(
                                                    text = "Miktar",
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
                                                    text = "Açıklama",
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
                                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                                editExchangeRate = newValue
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
                                        decorationBox = { innerTextField ->
                                            Box(
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                if (editExchangeRate.isEmpty()) {
                                                    Text(
                                                        text = "Döviz Kuru",
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
                                        Text("Sil", fontSize = 12.sp, color = Color.White)
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
                                        isEditing = false
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
                                        Text("Kaydet", fontSize = 12.sp, color = ThemeColors.getTextColor(isDarkTheme))
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
                    text = "Harcamayı Sil",
                    color = ThemeColors.getTextColor(isDarkTheme),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Bu harcamayı silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.",
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
                        "Sil", 
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
                        "İptal", 
                        color = ThemeColors.getTextColor(isDarkTheme)
                    )
                }
            },
            containerColor = ThemeColors.getDialogBackgroundColor(isDarkTheme)
        )
    }
}