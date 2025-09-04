package com.example.expensetrackerkotlin.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    val id: String,
    val name: String,
    val colorHex: String, // Store color as hex string
    val iconName: String, // Store icon name as string
    val isDefault: Boolean = false, // Mark if it's a default category
    val isCustom: Boolean = false // Mark if it's user-created
) {
    // Convert hex string to Color
    fun getColor(): Color {
        return try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            Color.Blue // Default color if parsing fails
        }
    }
    
    // Convert icon name to ImageVector
    fun getIcon(): ImageVector {
        return when (iconName) {
            "restaurant" -> Icons.Default.Restaurant
            "home" -> Icons.Default.Home
            "directions_car" -> Icons.Default.DirectionsCar
            "local_hospital" -> Icons.Default.LocalHospital
            "movie" -> Icons.Default.Movie
            "school" -> Icons.Default.School
            "shopping_cart" -> Icons.Default.ShoppingCart
            "pets" -> Icons.Default.Pets
            "work" -> Icons.Default.Work
            "account_balance" -> Icons.Default.AccountBalance
            "favorite" -> Icons.Default.Favorite
            "category" -> Icons.Default.Category
            else -> Icons.Default.Category
        }
    }
    
    companion object {
        // Default categories that will be inserted on first app launch
        fun getDefaultCategories(): List<Category> {
            return listOf(
                Category(
                    id = "food",
                    name = "Gıda ve İçecek",
                    colorHex = "#FF9500",
                    iconName = "restaurant",
                    isDefault = true
                ),
                Category(
                    id = "housing",
                    name = "Konut",
                    colorHex = "#007AFF",
                    iconName = "home",
                    isDefault = true
                ),
                Category(
                    id = "transportation",
                    name = "Ulaşım",
                    colorHex = "#34C759",
                    iconName = "directions_car",
                    isDefault = true
                ),
                Category(
                    id = "health",
                    name = "Sağlık ve Kişisel Bakım",
                    colorHex = "#FF2D92",
                    iconName = "local_hospital",
                    isDefault = true
                ),
                Category(
                    id = "entertainment",
                    name = "Eğlence ve Hobiler",
                    colorHex = "#9D73E3",
                    iconName = "movie",
                    isDefault = true
                ),
                Category(
                    id = "education",
                    name = "Eğitim",
                    colorHex = "#5856D6",
                    iconName = "school",
                    isDefault = true
                ),
                Category(
                    id = "shopping",
                    name = "Alışveriş",
                    colorHex = "#FF3B30",
                    iconName = "shopping_cart",
                    isDefault = true
                ),
                Category(
                    id = "pets",
                    name = "Evcil Hayvan",
                    colorHex = "#64D2FF",
                    iconName = "pets",
                    isDefault = true
                ),
                Category(
                    id = "work",
                    name = "İş ve Profesyonel Harcamalar",
                    colorHex = "#5AC8FA",
                    iconName = "work",
                    isDefault = true
                ),
                Category(
                    id = "tax",
                    name = "Vergi ve Hukuki Harcamalar",
                    colorHex = "#FFD60A",
                    iconName = "account_balance",
                    isDefault = true
                ),
                Category(
                    id = "donations",
                    name = "Bağışlar ve Yardımlar",
                    colorHex = "#30D158",
                    iconName = "favorite",
                    isDefault = true
                ),
                Category(
                    id = "others",
                    name = "Diğer Ödemeler",
                    colorHex = "#3F51B5",
                    iconName = "category",
                    isDefault = true
                )
            )
        }
    }
}
