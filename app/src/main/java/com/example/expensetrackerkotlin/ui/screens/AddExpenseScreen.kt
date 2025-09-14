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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.expensetrackerkotlin.R
import com.example.expensetrackerkotlin.data.Expense
import com.example.expensetrackerkotlin.data.RecurrenceType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material3.MenuAnchorType

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
    editingExpense: Expense? = null,
    viewModel: com.example.expensetrackerkotlin.viewmodel.ExpenseViewModel
) {
    var amount by remember { 
        mutableStateOf(editingExpense?.amount?.toString() ?: "") 
    }
    var selectedCurrency by remember(defaultCurrency) { 
        mutableStateOf(editingExpense?.currency ?: defaultCurrency) 
    }
    var selectedSubCategoryId by remember { 
        mutableStateOf(editingExpense?.subCategoryId ?: "") 
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
    var categorySearchText by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var selectedRecurrenceType by remember { 
        mutableStateOf(editingExpense?.recurrenceType ?: RecurrenceType.NONE) 
    }
    var endDate by remember { 
        mutableStateOf(editingExpense?.endDate ?: LocalDateTime.now().plusYears(1)) 
    }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    // Collect categories and subcategories from ViewModel
    val categories by viewModel.categories.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()
    
    // Initialize with first subcategory if not editing
    LaunchedEffect(subCategories) {
        if (subCategories.isNotEmpty() && selectedSubCategoryId.isEmpty() && editingExpense == null) {
            selectedSubCategoryId = subCategories.first().id
        }
    }
    
    // Get the category ID from the selected subcategory
    val selectedCategoryId = remember(selectedSubCategoryId) {
        subCategories.find { it.id == selectedSubCategoryId }?.categoryId ?: ""
    }
    
    // Filter subcategories based on search and filter
    val filteredSubCategories = remember(subCategories, categories, categorySearchText, selectedCategoryFilter) {
        var filtered = subCategories
        

        // Apply search filter
        if (categorySearchText.isNotBlank()) {
            filtered = filtered.filter { subCategory ->
                categories.find { it.id == subCategory.categoryId }
                subCategory.name.contains(categorySearchText, ignoreCase = true)
            }
        }
        
        filtered.sortedBy { it.name }
    }
    
    val currencies = listOf("₺", "$", "€", "£")
    val recurrenceTypes = listOf(
        RecurrenceType.NONE to stringResource(R.string.one_time),
        RecurrenceType.DAILY to stringResource(R.string.every_day),
        RecurrenceType.WEEKDAYS to stringResource(R.string.weekdays_only),
        RecurrenceType.WEEKLY to stringResource(R.string.once_per_week),
        RecurrenceType.MONTHLY to stringResource(R.string.once_per_month)
    )
    
    val focusManager = LocalFocusManager.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeColors.getBackgroundColor(isDarkTheme))
            .verticalScroll(rememberScrollState())
            .clickable { focusManager.clearFocus() }
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
                text = if (editingExpense != null) stringResource(R.string.edit_expense) else stringResource(R.string.new_expense),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeColors.getTextColor(isDarkTheme)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.enter_expense_details),
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
                        .weight(0.6f)
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
                                        text = stringResource(R.string.expense_amount_placeholder),
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
                    onExpandedChange = { 
                        showCurrencyMenu = it
                        if (it) focusManager.clearFocus()
                    },
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
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable) // Changed this line
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
                
                // Category (Subcategory)
                Box(modifier = Modifier.weight(1f)) {
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
                            .clickable { 
                                showCategoryMenu = !showCategoryMenu
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = subCategories.find { it.id == selectedSubCategoryId }?.name ?: stringResource(R.string.selectCategory),
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

                    DropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { 
                            showCategoryMenu = false
                            categorySearchText = ""
                            selectedCategoryFilter = "ALL"
                        },
                        modifier = Modifier
                            .background(ThemeColors.getInputBackgroundColor(isDarkTheme))
                            .heightIn(max = 340.dp)
                            .width(with(androidx.compose.ui.platform.LocalDensity.current) { 170.dp })
                    ) {
                        // Search TextField

                        BasicTextField(
                            value = categorySearchText,
                            onValueChange = { categorySearchText = it },

                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 2.dp),
                            singleLine = true,
                              textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 12.sp,
                                color = ThemeColors.getTextColor(isDarkTheme)
                            ),
                            decorationBox = { innerTextField ->
                                Box(
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (categorySearchText.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.search_placeholder_category),
                                            fontSize = 12.sp,
                                            color = ThemeColors.getTextGrayColor(isDarkTheme)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        
                        filteredSubCategories.forEach { subCategory ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = subCategory.name,
                                        color = ThemeColors.getTextColor(isDarkTheme),
                                        fontSize = 14.sp
                                    ) 
                                },
                                onClick = {
                                    selectedSubCategoryId = subCategory.id
                                    showCategoryMenu = false
                                    categorySearchText = ""
                                    selectedCategoryFilter = "ALL"
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
                    text = stringResource(R.string.description_optional),
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
                                        text = stringResource(R.string.add_description_placeholder),
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
                         text = stringResource(R.string.recurrence_type),
                         fontSize = 18.sp,
                         fontWeight = FontWeight.Medium,
                         color = ThemeColors.getTextColor(isDarkTheme)
                     )
                     
                     ExposedDropdownMenuBox(
                         expanded = showRecurrenceMenu,
                         onExpandedChange = { 
                             showRecurrenceMenu = it
                             if (it) focusManager.clearFocus()
                         },
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
                                 .menuAnchor(MenuAnchorType.PrimaryNotEditable) // Changed this line
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = recurrenceTypes.find { it.first == selectedRecurrenceType }?.second ?: stringResource(R.string.single_time),
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
                        text = stringResource(R.string.end_date),
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
                            .clickable { 
                                showEndDatePicker = true
                                focusManager.clearFocus()
                            }
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
                                contentDescription = stringResource(R.string.select_date),
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
                        text = stringResource(R.string.exchange_rate_label, selectedCurrency, defaultCurrency),
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
                                            text = stringResource(R.string.exchange_rate_example, selectedCurrency, defaultCurrency),
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
                        text = stringResource(R.string.exchange_rate_note),
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
                    text = stringResource(R.string.cancel),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
            }
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue != null && amountValue > 0) {
                        val expense = if (editingExpense != null) {
                            // Update existing expense
                            editingExpense.copy(
                                amount = amountValue,
                                currency = selectedCurrency,
                                categoryId = selectedCategoryId,
                                subCategoryId = selectedSubCategoryId,
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
                                categoryId = selectedCategoryId,
                                subCategoryId = selectedSubCategoryId,
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
                        selectedSubCategoryId.isNotEmpty() &&
                        (selectedCurrency == defaultCurrency || (exchangeRate.isNotEmpty() && exchangeRate.toDoubleOrNull() != null && exchangeRate.toDoubleOrNull()!! > 0)),
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (amount.isNotEmpty() && amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0 &&
                        selectedSubCategoryId.isNotEmpty() &&
                        (selectedCurrency == defaultCurrency || (exchangeRate.isNotEmpty() && exchangeRate.toDoubleOrNull() != null && exchangeRate.toDoubleOrNull()!! > 0))) {
                        AppColors.PrimaryOrange
                    } else {
                        ThemeColors.getButtonDisabledColor(isDarkTheme)
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (editingExpense != null) stringResource(R.string.update_expense) else stringResource(R.string.add_expense),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
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
                         Text(
                             stringResource(R.string.okay),
                             color = AppColors.PrimaryOrange,
                             fontWeight = FontWeight.SemiBold
                         )
                     }
                 },
                 dismissButton = {
                     TextButton(
                         onClick = { showEndDatePicker = false }
                     ) {
                         Text(
                             stringResource(R.string.cancel),
                             color = if (isDarkTheme) ThemeColors.getTextGrayColor(true) else Color.White
                         )
                     }
                 }
             ) {
                           DatePicker(
                  state = datePickerState,
                  colors = DatePickerDefaults.colors(
                      containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White,
                      titleContentColor = if (isDarkTheme) Color.White else Color.Black,
                      headlineContentColor = if (isDarkTheme) Color.White else Color.Black,
                      weekdayContentColor = Color.Gray,
                      subheadContentColor = if (isDarkTheme) Color.White else Color.Black,
                      yearContentColor = if (isDarkTheme) Color.White else Color.Black,
                      currentYearContentColor = AppColors.PrimaryOrange,
                      selectedYearContentColor = if (isDarkTheme) Color.White else Color.White,
                      selectedYearContainerColor = AppColors.PrimaryOrange,
                      dayContentColor = if (isDarkTheme) Color.White else Color.Black,
                      selectedDayContentColor = if (isDarkTheme) Color.White else Color.White,
                      selectedDayContainerColor = AppColors.PrimaryOrange,
                      todayContentColor = AppColors.PrimaryOrange,
                      todayDateBorderColor = AppColors.PrimaryOrange
                  )
              )
         }
     }
}