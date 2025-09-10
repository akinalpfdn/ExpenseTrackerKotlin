package com.example.expensetrackerkotlin.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.data.Category
import com.example.expensetrackerkotlin.data.Expense
import com.example.expensetrackerkotlin.data.SubCategory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class DailyCategorySortType {
    AMOUNT_HIGH_TO_LOW,
    AMOUNT_LOW_TO_HIGH,
    TIME_NEWEST_FIRST,
    TIME_OLDEST_FIRST,
    DESCRIPTION_A_TO_Z,
    DESCRIPTION_Z_TO_A
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyCategoryDetailBottomSheet(
    category: Category,
    selectedDateExpenses: List<Expense>,
    subCategories: List<SubCategory>,
    selectedDate: LocalDateTime,
    defaultCurrency: String,
    onDismiss: () -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var currentSortType by remember { mutableStateOf(DailyCategorySortType.AMOUNT_HIGH_TO_LOW) }
    
    val categoryExpenses = remember(selectedDateExpenses, category) {
        selectedDateExpenses.filter { expense -> 
            expense.categoryId == category.id
        }
    }
    
    val sortedExpenses = remember(categoryExpenses, currentSortType, subCategories) {
        when (currentSortType) {
            DailyCategorySortType.AMOUNT_HIGH_TO_LOW -> categoryExpenses.sortedByDescending { it.getAmountInDefaultCurrency(defaultCurrency) }
            DailyCategorySortType.AMOUNT_LOW_TO_HIGH -> categoryExpenses.sortedBy { it.getAmountInDefaultCurrency(defaultCurrency) }
            DailyCategorySortType.TIME_NEWEST_FIRST -> categoryExpenses.sortedByDescending { it.date }
            DailyCategorySortType.TIME_OLDEST_FIRST -> categoryExpenses.sortedBy { it.date }
            DailyCategorySortType.DESCRIPTION_A_TO_Z -> categoryExpenses.sortedBy { it.description }
            DailyCategorySortType.DESCRIPTION_Z_TO_A -> categoryExpenses.sortedByDescending { it.description }
        }
    }
    
    val totalAmount = categoryExpenses.sumOf { it.getAmountInDefaultCurrency(defaultCurrency) }
    
    val sortMenuItems = listOf(
        "Tutar (Yüksek → Düşük)" to DailyCategorySortType.AMOUNT_HIGH_TO_LOW,
        "Tutar (Düşük → Yüksek)" to DailyCategorySortType.AMOUNT_LOW_TO_HIGH,
        "Zaman (Yeni → Eski)" to DailyCategorySortType.TIME_NEWEST_FIRST,
        "Zaman (Eski → Yeni)" to DailyCategorySortType.TIME_OLDEST_FIRST,
        "Açıklama (A → Z)" to DailyCategorySortType.DESCRIPTION_A_TO_Z,
        "Açıklama (Z → A)" to DailyCategorySortType.DESCRIPTION_Z_TO_A
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp),
                shape = RoundedCornerShape(2.dp),
                color = Color.Gray.copy(alpha = 0.5f)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = category.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("tr"))),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                // Sort button
                Box {
                    IconButton(onClick = { showSortMenu = !showSortMenu }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sırala"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        sortMenuItems.forEach { (label, sortType) ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = label,
                                        color = if (currentSortType == sortType) MaterialTheme.colorScheme.primary else Color.Unspecified
                                    )
                                },
                                onClick = {
                                    currentSortType = sortType
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Total amount
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = category.getColor().copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Toplam Harcama",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${String.format("%.2f", totalAmount)} $defaultCurrency",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = category.getColor()
                    )
                }
            }
            
            if (sortedExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bu kategoride harcama bulunamadı",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    text = "${sortedExpenses.size} Harcama",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(sortedExpenses) { expense ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = expense.description,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    val subCategory = subCategories.find { it.id == expense.subCategoryId }
                                    if (subCategory != null) {
                                        Text(
                                            text = subCategory.name,
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }

                                }
                                Text(
                                    text = "${String.format("%.2f", expense.getAmountInDefaultCurrency(defaultCurrency))} $defaultCurrency",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = category.getColor()
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}