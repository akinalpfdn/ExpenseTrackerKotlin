package com.example.expensetrackerkotlin.ui.theme

import androidx.compose.ui.graphics.Color

object ThemeColors {
    fun getBackgroundColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) AppColors.BackgroundBlack else AppColors.BackgroundWhite
    }
    
    fun getTextColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) AppColors.TextWhite else AppColors.TextBlack
    }
    
    fun getTextGrayColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) AppColors.TextGray else AppColors.TextGrayLight
    }
    
    fun getCardBackgroundColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) AppColors.CardBackground else AppColors.CardBackgroundLight
    }
    
    fun getInputBackgroundColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) AppColors.InputBackground else AppColors.InputBackgroundLight
    }
    
    fun getInputBackgroundFocusedColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) AppColors.InputBackgroundFocused else AppColors.InputBackgroundFocusedLight
    }
    
    fun getButtonDisabledColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) AppColors.ButtonDisabled else AppColors.ButtonDisabledLight
    }
    
    fun getDeleteRedColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) AppColors.DeleteRed else AppColors.DeleteRedLight
    }
    
    fun getSuccessGreenColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) AppColors.SuccessGreen else AppColors.SuccessGreenLight
    }
    
    fun getDialogBackgroundColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) AppColors.DialogBackgroundDark else AppColors.DialogBackgroundLight
    }
}
