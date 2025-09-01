package com.example.expensetrackerkotlin.ui.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    val PrimaryOrange = Color(0xFFFF6400)
    val PrimaryRed = Color(0xFFFF3B30)
    
    // Dark theme colors
    val BackgroundBlack = Color(0xFF000000)
    val TextWhite = Color(0xFFFFFFFF)
    val TextGray = Color(0xFF808080)
    val CardBackground = Color(0x0DFFFFFF)

    val InputBackground = Color(0x1AFFFFFF)

    val primaryButtonStart = Color(0xFFFF9500)

    val primaryButtonEnd = Color(0xFFFF3B30)
    val InputBackgroundFocused = Color(0x26FFFFFF)
    val ButtonDisabled = Color(0x4D808080)
    val DeleteRed = Color(0xCCFF0000)
    
    // Light theme colors
    val BackgroundWhite = Color(0xFFFFFFFF)
    val TextBlack = Color(0xFF000000)
    val TextGrayLight = Color(0xFF333333)
    val CardBackgroundLight = Color(0xFFF8F8F8)
    val InputBackgroundLight = Color(0xFFF0F0F0)
    val InputBackgroundFocusedLight = Color(0xFFE8E8E8)
    val ButtonDisabledLight = Color(0xFFDDDDDD)
    val DeleteRedLight = Color(0xCCFF0000)
    
    // Gradient colors for buttons
    val ButtonGradientStart = PrimaryOrange
    val ButtonGradientEnd = PrimaryRed
    
    // Recurring expenses button colors
    val RecurringButtonStart = Color(0xFF1EC3FC)
    val RecurringButtonEnd = Color(0xFF03AAE4)
    
    // Dialog background colors
    val DialogBackgroundDark = Color(0xFF1E1E1E)
    val DialogBackgroundLight = Color(0xFFFFFFFF)
}
