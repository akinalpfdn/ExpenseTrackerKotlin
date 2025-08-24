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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.data.CategoryHelper
import com.example.expensetrackerkotlin.data.Expense
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    selectedDate: LocalDateTime,
    defaultCurrency: String,
    dailyLimit: String,
    monthlyLimit: String,
    onExpenseAdded: (Expense) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var selectedCurrency by remember(defaultCurrency) { mutableStateOf(defaultCurrency) }
    var selectedSubCategory by remember { mutableStateOf("Restoran") }
    var description by remember { mutableStateOf("") }
    var exchangeRate by remember { mutableStateOf("") }
    var showCurrencyMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    
    val currencies = listOf("₺", "$", "€", "£")//, "¥", "₹", "₽", "₩", "₪", "₦", "₨", "₴", "₸", "₼", "₾", "₿"
    //TODO add repeating expense option in which user will select the repeating options like daily weekly monthly and how long
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundBlack)
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
                text = "Yeni Harcama",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Harcama detaylarını girin",
                fontSize = 16.sp,
                color = AppColors.TextGray
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
                            AppColors.InputBackground,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = AppColors.TextGray,
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
                            color = AppColors.TextWhite
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (amount.isEmpty()) {
                                    Text(
                                        text = "0.00",
                                        fontSize = 14.sp,
                                        color = AppColors.TextGray
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
                                AppColors.InputBackground,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = AppColors.TextGray,
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
                                color = AppColors.TextWhite,
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
                        modifier = Modifier.background(AppColors.InputBackground)
                    ) {
                        currencies.forEach { currency ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = currency,
                                        color = AppColors.TextWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ) 
                                },
                                onClick = {
                                    selectedCurrency = currency
                                    showCurrencyMenu = false
                                },
                                modifier = Modifier.background(AppColors.InputBackground)
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
                                AppColors.InputBackground,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = AppColors.TextGray,
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
                                color = AppColors.TextWhite,
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
                            .background(AppColors.InputBackground)
                            .heightIn(max = 200.dp)
                    ) {
                        CategoryHelper.subCategories.sortedBy { it.name }.forEach { subCategory ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = subCategory.name,
                                        color = AppColors.TextWhite,
                                        fontSize = 14.sp
                                    ) 
                                },
                                onClick = {
                                    selectedSubCategory = subCategory.name
                                    showCategoryMenu = false
                                },
                                modifier = Modifier.background(AppColors.InputBackground)
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
                    color = AppColors.TextWhite
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            AppColors.InputBackground,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = AppColors.TextGray,
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
                            color = AppColors.TextWhite
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (description.isEmpty()) {
                                    Text(
                                        text = "Açıklama ekleyin...",
                                        fontSize = 14.sp,
                                        color = AppColors.TextGray
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
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
                        color = AppColors.TextWhite
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(
                                AppColors.InputBackground,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = AppColors.TextGray,
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
                                color = AppColors.TextWhite
                            ),
                            decorationBox = { innerTextField ->
                                Box(
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (exchangeRate.isEmpty()) {
                                        Text(
                                            text = "Örn: 0.035 (1 $selectedCurrency = 0.035 $defaultCurrency)",
                                            fontSize = 14.sp,
                                            color = AppColors.TextGray
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
                        color = AppColors.TextGray
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
                        val expense = Expense(
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
                            }
                        )
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
                        AppColors.ButtonDisabled
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Harcama Ekle",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextWhite
                )
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.ButtonDisabled
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "İptal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextWhite
                )
            }
        }
    }
}