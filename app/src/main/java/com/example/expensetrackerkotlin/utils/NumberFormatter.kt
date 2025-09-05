package com.example.expensetrackerkotlin.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

object NumberFormatter {
    
    /**
     * Formats a number with thousand separators and conditional decimal display.
     * Shows decimals only if they are not .00
     * 
     * @param amount The amount to format
     * @return Formatted string with thousand separators and conditional decimals
     */
    fun formatAmount(amount: Double): String {
        val symbols = DecimalFormatSymbols(Locale.forLanguageTag("tr"))
        symbols.groupingSeparator = '.'
        symbols.decimalSeparator = ','
        
        // Check if the amount has decimal places that are not zero
        val hasDecimals = amount % 1 != 0.0
        
        val formatter = if (hasDecimals) {
            // Show decimals if they exist
            DecimalFormat("#,##0.##", symbols)
        } else {
            // Show no decimals if it's a whole number
            DecimalFormat("#,##0", symbols)
        }
        
        return formatter.format(amount)
    }
    
    /**
     * Formats a number with thousand separators and always shows 2 decimal places.
     * 
     * @param amount The amount to format
     * @return Formatted string with thousand separators and 2 decimal places
     */
    fun formatAmountWithDecimals(amount: Double): String {
        val symbols = DecimalFormatSymbols(Locale.forLanguageTag("tr"))
        symbols.groupingSeparator = '.'
        symbols.decimalSeparator = ','
        
        val formatter = DecimalFormat("#,##0.00", symbols)
        return formatter.format(amount)
    }
}
