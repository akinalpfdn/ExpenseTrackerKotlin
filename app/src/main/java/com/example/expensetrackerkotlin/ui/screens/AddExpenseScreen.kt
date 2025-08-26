package com.example.expensetrackerkotlin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.data.CategoryHelper
import com.example.expensetrackerkotlin.data.Expense
import com.example.expensetrackerkotlin.data.RecurrenceType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    selectedDate: LocalDateTime,
    defaultCurrency: String,
    dailyLimit: String,
    monthlyLimit: String,
    isDarkTheme: Boolean = true,
    onExpenseAdded: (Expense) -> Unit,
    onDismiss: () -> Unit,
    editingExpense: Expense? = null
) {
    var amount by remember { 
        mutableStateOf(editingExpense?.amount?.toString() ?: "") 
    }
    var selectedCurrency by remember(defaultCurrency) { 
        mutableStateOf(editingExpense?.currency ?: defaultCurrency) 
    }
    var selectedSubCategory by remember { 
        mutableStateOf(editingExpense?.subCategory ?: "Restoran") 
    }
    var description by remember { 
        mutableStateOf(editingExpense?.description ?: "") 
    }
    var exchangeRate by remember { 
        mutableStateOf(editingExpense?.exchangeRate?.toString() ?: "") 
    }
    var showCurrencyMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showRecurrenceMenu by remember { mutableStateOf(false) }
    var selectedRecurrenceType by remember { 
        mutableStateOf(editingExpense?.recurrenceType ?: RecurrenceType.NONE) 
    }
    var endDate by remember { 
        mutableStateOf(editingExpense?.endDate ?: LocalDateTime.now().plusYears(1)) 
    }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    val currencies = listOf("₺", "$", "€", "£")
    val recurrenceTypes = listOf(
        RecurrenceType.NONE to "Tek seferlik",
        RecurrenceType.DAILY to "Her gün",
        RecurrenceType.WEEKDAYS to "Hafta içi her gün",
        RecurrenceType.WEEKLY to "Haftada 1 kez",
        RecurrenceType.MONTHLY to "Ayda 1 kez"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeColors.getBackgroundColor(isDarkTheme))
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = if (editingExpense != null) "Harcamayı Düzenle" else "Yeni Harcama",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeColors.getTextColor(isDarkTheme)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Harcama detaylarını girin",
                fontSize = 16.sp,
                color = ThemeColors.getTextGrayColor(isDarkTheme)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Form
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Amount, Currency and Category row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Amount
                Box(
                    modifier = Modifier
                        .weight(1f)
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
                        value = amount,
                        onValueChange = { newValue ->
                            val filtered = newValue.filter { "0123456789.,".contains(it) }
                            val components = filtered.split(",")
                            amount = if (components.size > 2) {
                                "${components[0]},${components[1]}"
                            } else {
                                filtered
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
                                if (amount.isEmpty()) {
                                    Text(
                                        text = "0.00",
                                        fontSize = 14.sp,
                                        color = ThemeColors.getTextGrayColor(isDarkTheme)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
                
                // Currency
                ExposedDropdownMenuBox(
                    expanded = showCurrencyMenu,
                    onExpandedChange = { showCurrencyMenu = it },
                    modifier = Modifier.width(70.dp)
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
                            .menuAnchor()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedCurrency,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ThemeColors.getTextColor(isDarkTheme),
                                modifier = Modifier.padding(start = 12.dp)
                            )
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = showCurrencyMenu,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                    
                    ExposedDropdownMenu(
                        expanded = showCurrencyMenu,
                        onDismissRequest = { showCurrencyMenu = false },
                        modifier = Modifier.background(ThemeColors.getInputBackgroundColor(isDarkTheme))
                    ) {
                        currencies.forEach { currency ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = currency,
                                        color = ThemeColors.getTextColor(isDarkTheme),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ) 
                                },
                                onClick = {
                                    selectedCurrency = currency
                                    showCurrencyMenu = false
                                },
                                modifier = Modifier.background(ThemeColors.getInputBackgroundColor(isDarkTheme))
                            )
                        }
                    }
                }
                
                // Category
                ExposedDropdownMenuBox(
                    expanded = showCategoryMenu,
                    onExpandedChange = { showCategoryMenu = it },
                    modifier = Modifier.weight(1f)
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
                            .menuAnchor()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedSubCategory,
                                fontSize = 14.sp,
                                color = ThemeColors.getTextColor(isDarkTheme),
                                modifier = Modifier.padding(start = 12.dp)
                            )
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = showCategoryMenu,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                    
                    ExposedDropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false },
                        modifier = Modifier
                            .background(ThemeColors.getInputBackgroundColor(isDarkTheme))
                            .heightIn(max = 200.dp)
                    ) {
                        CategoryHelper.subCategories.sortedBy { it.name }.forEach { subCategory ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = subCategory.name,
                                        color = ThemeColors.getTextColor(isDarkTheme),
                                        fontSize = 14.sp
                                    ) 
                                },
                                onClick = {
                                    selectedSubCategory = subCategory.name
                                    showCategoryMenu = false
                                },
                                modifier = Modifier.background(ThemeColors.getInputBackgroundColor(isDarkTheme))
                            )
                        }
                    }
                }
            }
            
            // Description
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Açıklama (İsteğe bağlı)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
                
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
                        value = description,
                        onValueChange = { description = it },
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
                                if (description.isEmpty()) {
                                    Text(
                                        text = "Açıklama ekleyin...",
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
            
            // Recurrence Type
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Tekrar Türü",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
                
                ExposedDropdownMenuBox(
                    expanded = showRecurrenceMenu,
                    onExpandedChange = { showRecurrenceMenu = it },
                    modifier = Modifier.fillMaxWidth()
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
                            .menuAnchor()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = recurrenceTypes.find { it.first == selectedRecurrenceType }?.second ?: "Tek seferlik",
                                fontSize = 14.sp,
                                color = ThemeColors.getTextColor(isDarkTheme),
                                modifier = Modifier.padding(start = 12.dp)
                            )
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = showRecurrenceMenu,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                    
                    ExposedDropdownMenu(
                        expanded = showRecurrenceMenu,
                        onDismissRequest = { showRecurrenceMenu = false },
                        modifier = Modifier.background(ThemeColors.getInputBackgroundColor(isDarkTheme))
                    ) {
                        recurrenceTypes.forEach { (recurrenceType, displayName) ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = displayName,
                                        color = ThemeColors.getTextColor(isDarkTheme),
                                        fontSize = 14.sp
                                    ) 
                                },
                                onClick = {
                                    selectedRecurrenceType = recurrenceType
                                    showRecurrenceMenu = false
                                },
                                modifier = Modifier.background(ThemeColors.getInputBackgroundColor(isDarkTheme))
                            )
                        }
                    }
                }
            }
            
            // End Date (only show if recurring)
            if (selectedRecurrenceType != RecurrenceType.NONE) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Bitiş Tarihi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = ThemeColors.getTextColor(isDarkTheme)
                    )
                    
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
                            .clickable { showEndDatePicker = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                fontSize = 14.sp,
                                color = ThemeColors.getTextColor(isDarkTheme)
                            )
                            
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Tarih Seç",
                                tint = ThemeColors.getTextGrayColor(isDarkTheme),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            
            // Exchange Rate (only show if currency is different from default)
            if (selectedCurrency != defaultCurrency) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Döviz Kuru (1 $selectedCurrency = ? $defaultCurrency)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = ThemeColors.getTextColor(isDarkTheme)
                    )
                    
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
                            value = exchangeRate,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    exchangeRate = newValue
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
                                    if (exchangeRate.isEmpty()) {
                                        Text(
                                            text = "Örn: 0.035 (1 $selectedCurrency = 0.035 $defaultCurrency)",
                                            fontSize = 14.sp,
                                            color = ThemeColors.getTextGrayColor(isDarkTheme)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                    
                    Text(
                        text = "Bu kur, progress hesaplamaları için kullanılacak",
                        fontSize = 14.sp,
                        color = ThemeColors.getTextGrayColor(isDarkTheme)
                    )
                }
            }
        }
        
        // Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue != null && amountValue > 0) {
                        val expense = if (editingExpense != null) {
                            // Update existing expense
                            editingExpense.copy(
                                amount = amountValue,
                                currency = selectedCurrency,
                                subCategory = selectedSubCategory,
                                description = description,
                                exchangeRate = if (selectedCurrency != defaultCurrency) {
                                    exchangeRate.toDoubleOrNull()
                                } else {
                                    null
                                },
                                recurrenceType = selectedRecurrenceType,
                                endDate = if (selectedRecurrenceType != RecurrenceType.NONE) endDate else null
                            )
                        } else {
                            // Create new expense
                            Expense(
                                amount = amountValue,
                                currency = selectedCurrency,
                                subCategory = selectedSubCategory,
                                description = description,
                                date = selectedDate,
                                dailyLimitAtCreation = dailyLimit.toDoubleOrNull() ?: 0.0,
                                monthlyLimitAtCreation = monthlyLimit.toDoubleOrNull() ?: 0.0,
                                exchangeRate = if (selectedCurrency != defaultCurrency) {
                                    exchangeRate.toDoubleOrNull()
                                } else {
                                    null
                                },
                                recurrenceType = selectedRecurrenceType,
                                endDate = if (selectedRecurrenceType != RecurrenceType.NONE) endDate else null,
                                recurrenceGroupId = if (selectedRecurrenceType != RecurrenceType.NONE) UUID.randomUUID().toString() else null
                            )
                        }
                        onExpenseAdded(expense)
                        onDismiss()
                    }
                },
                enabled = amount.isNotEmpty() && amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0 && 
                         (selectedCurrency == defaultCurrency || (exchangeRate.isNotEmpty() && exchangeRate.toDoubleOrNull() != null && exchangeRate.toDoubleOrNull()!! > 0)),
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (amount.isNotEmpty() && amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0 && 
                                        (selectedCurrency == defaultCurrency || (exchangeRate.isNotEmpty() && exchangeRate.toDoubleOrNull() != null && exchangeRate.toDoubleOrNull()!! > 0))) {
                        AppColors.PrimaryOrange
                    } else {
                        ThemeColors.getButtonDisabledColor(isDarkTheme)
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (editingExpense != null) "Güncelle" else "Harcama Ekle",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ThemeColors.getButtonDisabledColor(isDarkTheme)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "İptal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
            }
        }
    }
    
    // Date Picker Dialog
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate.toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
        )
        
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            endDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneOffset.UTC)
                                .toLocalDateTime()
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text("Tamam")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEndDatePicker = false }
                ) {
                    Text("İptal")
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = ThemeColors.getDialogBackgroundColor(isDarkTheme),
                titleContentColor = ThemeColors.getTextColor(isDarkTheme),
                headlineContentColor = ThemeColors.getTextColor(isDarkTheme),
                weekdayContentColor = ThemeColors.getTextGrayColor(isDarkTheme),
                subheadContentColor = ThemeColors.getTextColor(isDarkTheme),
                yearContentColor = ThemeColors.getTextColor(isDarkTheme),
                currentYearContentColor = AppColors.PrimaryOrange,
                selectedYearContentColor = ThemeColors.getTextColor(isDarkTheme),
                selectedYearContainerColor = AppColors.PrimaryOrange,
                dayContentColor = ThemeColors.getTextColor(isDarkTheme),
                selectedDayContentColor = ThemeColors.getTextColor(isDarkTheme),
                selectedDayContainerColor = AppColors.PrimaryOrange,
                todayContentColor = AppColors.PrimaryOrange,
                todayDateBorderColor = AppColors.PrimaryOrange
            )
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "Bitiş Tarihi Seçin",
                        color = ThemeColors.getTextColor(isDarkTheme),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    }
}