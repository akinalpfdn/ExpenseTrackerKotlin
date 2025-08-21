package com.example.expensetrackerkotlin.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class ExpenseCategory(val displayName: String) {
    FOOD("Gıda ve İçecek"),
    HOUSING("Konut"),
    TRANSPORTATION("Ulaşım"),
    HEALTH("Sağlık ve Kişisel Bakım"),
    ENTERTAINMENT("Eğlence ve Hobiler"),
    EDUCATION("Eğitim"),
    SHOPPING("Alışveriş"),
    PETS("Evcil Hayvan"),
    WORK("İş ve Profesyonel Harcamalar"),
    TAX("Vergi ve Hukuki Harcamalar"),
    DONATIONS("Bağışlar ve Yardımlar")
}

fun ExpenseCategory.getColor(): Color {
    return when (this) {
        ExpenseCategory.FOOD -> Color(0xFFFF9500)
        ExpenseCategory.HOUSING -> Color(0xFF007AFF)
        ExpenseCategory.TRANSPORTATION -> Color(0xFF34C759)
        ExpenseCategory.HEALTH -> Color(0xFFFF2D92)
        ExpenseCategory.ENTERTAINMENT -> Color(0xFF9D73E3)
        ExpenseCategory.EDUCATION -> Color(0xFF5856D6)
        ExpenseCategory.SHOPPING -> Color(0xFFFF3B30)
        ExpenseCategory.PETS -> Color(0xFF64D2FF)
        ExpenseCategory.WORK -> Color(0xFF5AC8FA)
        ExpenseCategory.TAX -> Color(0xFFFFD60A)
        ExpenseCategory.DONATIONS -> Color(0xFF30D158)
    }
}

fun ExpenseCategory.getIcon(): ImageVector {
    return when (this) {
        ExpenseCategory.FOOD -> Icons.Default.Restaurant
        ExpenseCategory.HOUSING -> Icons.Default.Home
        ExpenseCategory.TRANSPORTATION -> Icons.Default.DirectionsCar
        ExpenseCategory.HEALTH -> Icons.Default.FavoriteBorder
        ExpenseCategory.ENTERTAINMENT -> Icons.Default.SportsEsports
        ExpenseCategory.EDUCATION -> Icons.Default.Book
        ExpenseCategory.SHOPPING -> Icons.Default.ShoppingBag
        ExpenseCategory.PETS -> Icons.Default.Pets
        ExpenseCategory.WORK -> Icons.Default.Work
        ExpenseCategory.TAX -> Icons.Default.Description
        ExpenseCategory.DONATIONS -> Icons.Default.CardGiftcard
    }
}