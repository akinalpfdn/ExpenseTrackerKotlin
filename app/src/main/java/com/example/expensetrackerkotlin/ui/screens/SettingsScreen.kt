package com.example.expensetrackerkotlin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    defaultCurrency: String,
    dailyLimit: String,
    monthlyLimit: String,
    theme: String,
    onCurrencyChanged: (String) -> Unit,
    onDailyLimitChanged: (String) -> Unit,
    onMonthlyLimitChanged: (String) -> Unit,
    onThemeChanged: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Genel Ayarlar", "Kategoriler")
    
    val isDarkTheme = theme == "dark"
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeColors.getBackgroundColor(isDarkTheme))
    ) {

        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.padding(horizontal = 20.dp),
            containerColor = ThemeColors.getBackgroundColor(isDarkTheme),
            contentColor = AppColors.PrimaryOrange,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    height = 3.dp,
                    color = AppColors.PrimaryOrange
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 22.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) AppColors.PrimaryOrange else ThemeColors.getTextGrayColor(isDarkTheme)
                        )
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab Content
        when (selectedTabIndex) {
            0 -> GeneralSettingsTab(
                defaultCurrency = defaultCurrency,
                dailyLimit = dailyLimit,
                monthlyLimit = monthlyLimit,
                theme = theme,
                onCurrencyChanged = onCurrencyChanged,
                onDailyLimitChanged = onDailyLimitChanged,
                onMonthlyLimitChanged = onMonthlyLimitChanged,
                onThemeChanged = onThemeChanged,
                onDismiss = onDismiss,
                isDarkTheme = isDarkTheme
            )
            1 -> CategoriesTab(
                isDarkTheme = isDarkTheme
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsTab(
    defaultCurrency: String,
    dailyLimit: String,
    monthlyLimit: String,
    theme: String,
    onCurrencyChanged: (String) -> Unit,
    onDailyLimitChanged: (String) -> Unit,
    onMonthlyLimitChanged: (String) -> Unit,
    onThemeChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean
) {
    var newDefaultCurrency by remember { mutableStateOf(defaultCurrency) }
    var newDailyLimit by remember { mutableStateOf(dailyLimit) }
    var newMonthlyLimit by remember { mutableStateOf(monthlyLimit) }
    var newTheme by remember { mutableStateOf(theme) }
    var showCurrencyMenu by remember { mutableStateOf(false) }
    
    val currencies = listOf("₺", "$", "€", "£")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Settings Form
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Currency Setting
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Varsayılan Para Birimi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
                
                ExposedDropdownMenuBox(
                    expanded = showCurrencyMenu,
                    onExpandedChange = { showCurrencyMenu = it },
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
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = newDefaultCurrency,
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
                                    newDefaultCurrency = currency
                                    showCurrencyMenu = false
                                },
                                modifier = Modifier.background(ThemeColors.getInputBackgroundColor(isDarkTheme))
                            )
                        }
                    }
                }
                
                Text(
                    text = "Yeni harcamalar için kullanılacak varsayılan para birimi",
                    fontSize = 14.sp,
                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                )
            }
            
            // Daily Limit Setting
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Günlük Harcama Limiti",
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
                        value = newDailyLimit,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() || it == '.' }) {
                                newDailyLimit = newValue
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            color = ThemeColors.getTextColor(isDarkTheme)
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (newDailyLimit.isEmpty()) {
                                    Text(
                                        text = "Günlük limit girin",
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
                    text = "Her güne özel harcama limitiniz. Boş bırakılırsa limit uygulanmaz",
                    fontSize = 14.sp,
                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                )
            }
            
            // Monthly Limit Setting
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Aylık Harcama Limiti",
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
                        value = newMonthlyLimit,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() || it == '.' }) {
                                newMonthlyLimit = newValue
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            color = ThemeColors.getTextColor(isDarkTheme)
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (newMonthlyLimit.isEmpty()) {
                                    Text(
                                        text = "Aylık limit girin",
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
                    text = "Aylık toplam harcama limitiniz. Progress ring bu değere göre gösterilir",
                    fontSize = 14.sp,
                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                )
            }
            
            // Theme Setting
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Tema",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (newTheme == "dark") "Koyu Tema" else "Açık Tema",
                        fontSize = 16.sp,
                        color = ThemeColors.getTextColor(isDarkTheme)
                    )
                    
                    Switch(
                        checked = newTheme == "light",
                        onCheckedChange = { isLight ->
                            newTheme = if (isLight) "light" else "dark"
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppColors.PrimaryOrange,
                            checkedTrackColor = AppColors.PrimaryOrange.copy(alpha = 0.5f),
                            uncheckedThumbColor = AppColors.TextGray,
                            uncheckedTrackColor = AppColors.TextGray.copy(alpha = 0.3f)
                        )
                    )
                }
                
                Text(
                    text = "Uygulamanın görünümünü değiştirin",
                    fontSize = 14.sp,
                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Button(
                onClick = {
                    onCurrencyChanged(newDefaultCurrency)
                    onDailyLimitChanged(newDailyLimit)
                    onMonthlyLimitChanged(newMonthlyLimit)
                    onThemeChanged(newTheme)
                    onDismiss()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.PrimaryOrange
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Kaydet",
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
}

@Composable
fun CategoriesTab(
    isDarkTheme: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Kategoriler",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = ThemeColors.getTextColor(isDarkTheme)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Kategori yönetimi yakında eklenecek",
            fontSize = 16.sp,
            color = ThemeColors.getTextGrayColor(isDarkTheme),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}