package com.example.expensetrackerkotlin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors

@Composable
fun PlanningScreen(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ThemeColors.getBackgroundColor(isDarkTheme)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📋",
                fontSize = 64.sp
            )
            Text(
                text = "Planlama",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeColors.getTextColor(isDarkTheme)
            )
            Text(
                text = "Harcama planlaması burada görünecek",
                fontSize = 16.sp,
                color = ThemeColors.getTextGrayColor(isDarkTheme)
            )
        }
    }
}
