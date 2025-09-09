package com.example.expensetrackerkotlin.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import com.example.expensetrackerkotlin.utils.NumberFormatter
import com.example.expensetrackerkotlin.data.*

data class SubCategoryAnalysisData(
    val subCategory: SubCategory,
    val parentCategory: Category,
    val totalAmount: Double,
    val expenseCount: Int,
    val percentage: Double,
    val expenses: List<Expense>
)

@Composable
fun CategorySummarySection(
    categoryData: List<CategoryAnalysisData>,
    subCategoryData: List<SubCategoryAnalysisData>,
    totalAmount: Double,
    defaultCurrency: String,
    isDarkTheme: Boolean,
    onCategoryClick: (CategoryAnalysisData) -> Unit,
    onSubCategoryClick: (SubCategoryAnalysisData) -> Unit
) {
    var showMainCategories by remember { mutableStateOf(true) }
    
    Column(
        modifier = Modifier.padding(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kategori Detayları",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeColors.getTextColor(isDarkTheme),
            )
            Text(
                text = "$defaultCurrency ${NumberFormatter.formatAmount(totalAmount)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryOrange
            )
        }

        // Radio buttons for category type selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { showMainCategories = true }
            ) {
                RadioButton(
                    selected = showMainCategories,
                    onClick = { showMainCategories = true },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = AppColors.PrimaryOrange,
                        unselectedColor = ThemeColors.getTextGrayColor(isDarkTheme)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ana Kategoriler",
                    fontSize = 16.sp,
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { showMainCategories = false }
            ) {
                RadioButton(
                    selected = !showMainCategories,
                    onClick = { showMainCategories = false },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = AppColors.PrimaryOrange,
                        unselectedColor = ThemeColors.getTextGrayColor(isDarkTheme)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Alt Kategoriler",
                    fontSize = 16.sp,
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
            }
        }

        // Display data based on selection
        if (showMainCategories) {
            categoryData.forEach { data ->
                CategorySummaryRow(
                    categoryData = data,
                    defaultCurrency = defaultCurrency,
                    isDarkTheme = isDarkTheme,
                    onClick = { onCategoryClick(data) }
                )
            }
        } else {
            subCategoryData.forEach { data ->
                SubCategorySummaryRow(
                    subCategoryData = data,
                    defaultCurrency = defaultCurrency,
                    isDarkTheme = isDarkTheme,
                    onClick = { onSubCategoryClick(data) }
                )
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun CategorySummaryRow(
    categoryData: CategoryAnalysisData,
    defaultCurrency: String,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 2.dp)
            .background(
                categoryData.category.getColor().copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        categoryData.category.getColor().copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryData.category.getIcon(),
                    contentDescription = categoryData.category.name,
                    tint = categoryData.category.getColor(),
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = categoryData.category.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = ThemeColors.getTextColor(isDarkTheme),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${categoryData.expenseCount} harcama • %${String.format("%.1f", categoryData.percentage * 100)}",
                    fontSize = 14.sp,
                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                )
            }
        }
        
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .padding(12.dp)
        ) {
            Text(
                text = "$defaultCurrency ${NumberFormatter.formatAmount(categoryData.totalAmount)}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = ThemeColors.getTextColor(isDarkTheme)
            )
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Detayları gör",
                tint = ThemeColors.getTextGrayColor(isDarkTheme),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun SubCategorySummaryRow(
    subCategoryData: SubCategoryAnalysisData,
    defaultCurrency: String,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 2.dp)
            .background(
                subCategoryData.parentCategory.getColor().copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        subCategoryData.parentCategory.getColor().copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = subCategoryData.parentCategory.getIcon(),
                    contentDescription = subCategoryData.subCategory.name,
                    tint = subCategoryData.parentCategory.getColor(),
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = subCategoryData.subCategory.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = ThemeColors.getTextColor(isDarkTheme),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${subCategoryData.expenseCount} harcama • %${String.format("%.1f", subCategoryData.percentage * 100)}",
                    fontSize = 14.sp,
                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                )
            }
        }
        
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .padding(12.dp)
        ) {
            Text(
                text = "$defaultCurrency ${NumberFormatter.formatAmount(subCategoryData.totalAmount)}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = ThemeColors.getTextColor(isDarkTheme)
            )
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Detayları gör",
                tint = ThemeColors.getTextGrayColor(isDarkTheme),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
