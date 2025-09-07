package com.example.expensetrackerkotlin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import com.example.expensetrackerkotlin.utils.NumberFormatter

@Composable
fun CategorySummarySection(
    categoryData: List<CategoryAnalysisData>,
    totalAmount: Double,
    defaultCurrency: String,
    isDarkTheme: Boolean,
    onCategoryClick: (CategoryAnalysisData) -> Unit,
    modifier: Modifier = Modifier
) {
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

        categoryData.forEach { data ->
            CategorySummaryRow(
                categoryData = data,
                defaultCurrency = defaultCurrency,
                isDarkTheme = isDarkTheme,
                onClick = { onCategoryClick(data) }
            )
        }
    }
}

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
