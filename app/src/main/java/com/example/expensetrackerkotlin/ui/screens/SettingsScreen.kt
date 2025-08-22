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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    defaultCurrency: String,
    dailyLimit: String,
    monthlyLimit: String,
    onCurrencyChanged: (String) -> Unit,
    onDailyLimitChanged: (String) -> Unit,
    onMonthlyLimitChanged: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newDefaultCurrency by remember { mutableStateOf(defaultCurrency) }
    var newDailyLimit by remember { mutableStateOf(dailyLimit) }
    var newMonthlyLimit by remember { mutableStateOf(monthlyLimit) }
    var showCurrencyMenu by remember { mutableStateOf(false) }
    
    val currencies = listOf("₺", "$", "€", "£")//, "¥", "₹", "₽", "₩", "₪", "₦", "₨", "₴", "₸", "₼", "₾", "₿")
    
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
                text = "Ayarlar",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Uygulamanızı kişiselleştirin",
                fontSize = 16.sp,
                color = AppColors.TextGray
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
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
                    color = Color.White
                )
                
                Box {
                    OutlinedButton(
                        onClick = { showCurrencyMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.1f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = newDefaultCurrency,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    DropdownMenu(
                        expanded = showCurrencyMenu,
                        onDismissRequest = { showCurrencyMenu = false }
                    ) {
                        currencies.forEach { currency ->
                            DropdownMenuItem(
                                text = { Text(currency) },
                                onClick = {
                                    newDefaultCurrency = currency
                                    showCurrencyMenu = false
                                }
                            )
                        }
                    }
                }
                
                Text(
                    text = "Yeni harcamalar için kullanılacak varsayılan para birimi",
                    fontSize = 14.sp,
                    color = Color.Gray
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
                    color = Color.White
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
                            color = AppColors.TextWhite
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (newDailyLimit.isEmpty()) {
                                    Text(
                                        text = "Günlük limit girin",
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
                    text = "Her güne özel harcama limitiniz. Boş bırakılırsa limit uygulanmaz",
                    fontSize = 14.sp,
                    color = Color.Gray
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
                    color = Color.White
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
                            color = AppColors.TextWhite
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (newMonthlyLimit.isEmpty()) {
                                    Text(
                                        text = "Aylık limit girin",
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
                    text = "Aylık toplam harcama limitiniz. Progress ring bu değere göre gösterilir",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            

        }

        
        // Buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Button(
                onClick = {
                    onCurrencyChanged(newDefaultCurrency)
                    onDailyLimitChanged(newDailyLimit)
                    onMonthlyLimitChanged(newMonthlyLimit)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(36.dp)
                    .align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.PrimaryOrange
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Kaydet",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextWhite
                )
            }
            
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(36.dp)
                    .align(Alignment.CenterHorizontally),
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