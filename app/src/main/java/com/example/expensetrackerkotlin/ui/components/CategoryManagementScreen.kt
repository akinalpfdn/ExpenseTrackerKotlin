package com.example.expensetrackerkotlin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.data.ExpenseCategory
import com.example.expensetrackerkotlin.data.CategoryHelper
import com.example.expensetrackerkotlin.data.getColor
import com.example.expensetrackerkotlin.data.getIcon
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors

@Composable
fun CategoryManagementScreen(
    isDarkTheme: Boolean = true,
    modifier: Modifier = Modifier
) {
    var expandedCategories by remember { mutableStateOf(setOf<ExpenseCategory>()) }
    var showAddMainCategoryDialog by remember { mutableStateOf(false) }
    var showAddSubcategoryDialog by remember { mutableStateOf(false) }
    var showEditCategoryDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    var selectedSubcategory by remember { mutableStateOf<com.example.expensetrackerkotlin.data.ExpenseSubCategory?>(null) }
    
    // Temporary in-memory lists for demo purposes
    var customCategories by remember { mutableStateOf(listOf<ExpenseCategory>()) }
    var customSubcategories by remember { mutableStateOf(listOf<com.example.expensetrackerkotlin.data.ExpenseSubCategory>()) }
    
    // Combine default and custom categories
    val allCategories = ExpenseCategory.values().toList() + customCategories
    val allSubcategories = CategoryHelper.subCategories + customSubcategories
    
         Column(
         modifier = modifier
             .fillMaxSize()
             .padding(20.dp)
     ) {
         // Tree View
                 LazyColumn(
             verticalArrangement = Arrangement.spacedBy(8.dp)
         ) {
             items(allCategories) { category ->
                                 CategoryTreeItem(
                     category = category,
                     isExpanded = expandedCategories.contains(category),
                     onToggleExpanded = {
                         expandedCategories = if (expandedCategories.contains(category)) {
                             expandedCategories - category
                         } else {
                             expandedCategories + category
                         }
                     },
                     onEdit = { 
                         selectedCategory = category
                         selectedSubcategory = null
                         showEditCategoryDialog = true 
                     },
                     onDelete = { 
                         selectedCategory = category
                         selectedSubcategory = null
                         showDeleteConfirmationDialog = true 
                     },
                     onEditSubcategory = { subcategory ->
                         selectedSubcategory = subcategory
                         selectedCategory = null
                         showEditCategoryDialog = true
                     },
                     onDeleteSubcategory = { subcategory ->
                         selectedSubcategory = subcategory
                         selectedCategory = null
                         showDeleteConfirmationDialog = true
                     },
                     allSubcategories = allSubcategories,
                     isDarkTheme = isDarkTheme
                 )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Add Buttons Row
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Add Main Category Button
                         Button(
                 onClick = { showAddMainCategoryDialog = true },
                 modifier = Modifier
                     .fillMaxWidth()
                     .height(48.dp),
                 colors = ButtonDefaults.buttonColors(
                     containerColor = AppColors.PrimaryOrange
                 ),
                 shape = RoundedCornerShape(16.dp)
             ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Main Category",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Yeni Ana Kategori Ekle",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Add Subcategory Button
                         OutlinedButton(
                 onClick = { showAddSubcategoryDialog = true },
                 modifier = Modifier
                     .fillMaxWidth()
                     .height(44.dp),
                 colors = ButtonDefaults.outlinedButtonColors(
                     contentColor = AppColors.PrimaryOrange
                 ),
                 shape = RoundedCornerShape(16.dp)
             ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Subcategory",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Yeni Alt Kategori Ekle",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                         }
         }
         
         // Dialogs
         if (showAddMainCategoryDialog) {
             AddMainCategoryDialog(
                 onDismiss = { 
                     showAddMainCategoryDialog = false 
                 },
                 onConfirm = { categoryName ->
                     // Add new custom category
                     val newCategory = ExpenseCategory.values().firstOrNull { it.displayName == categoryName }
                     if (newCategory == null) {
                         // Create a new custom category (simplified for demo)
                         customCategories = customCategories + ExpenseCategory.FOOD // Placeholder
                     }
                     showAddMainCategoryDialog = false 
                 },
                 isDarkTheme = isDarkTheme
             )
         }
         
         if (showAddSubcategoryDialog) {
             AddSubcategoryDialog(
                 onDismiss = { 
                     showAddSubcategoryDialog = false 
                 },
                 onConfirm = { subcategoryName ->
                     // Add new subcategory
                     val newSubcategory = com.example.expensetrackerkotlin.data.ExpenseSubCategory(
                         name = subcategoryName,
                         category = ExpenseCategory.FOOD // Default category for demo
                     )
                     customSubcategories = customSubcategories + newSubcategory
                     showAddSubcategoryDialog = false 
                 },
                 isDarkTheme = isDarkTheme
             )
         }
         
         if (showEditCategoryDialog) {
             EditCategoryDialog(
                 category = selectedCategory,
                 subcategory = selectedSubcategory,
                 onDismiss = { 
                     showEditCategoryDialog = false
                     selectedCategory = null
                     selectedSubcategory = null
                 },
                 onConfirm = { newName ->
                     // Update category or subcategory name
                     if (selectedSubcategory != null) {
                         // Update subcategory
                         customSubcategories = customSubcategories.map { 
                             if (it == selectedSubcategory) {
                                 it.copy(name = newName)
                             } else it
                         }
                     } else if (selectedCategory != null) {
                         // Update category (simplified for demo)
                         // In real app, you'd update the enum or database
                     }
                     showEditCategoryDialog = false
                     selectedCategory = null
                     selectedSubcategory = null
                 },
                 isDarkTheme = isDarkTheme
             )
         }
         
         if (showDeleteConfirmationDialog) {
             DeleteConfirmationDialog(
                 category = selectedCategory,
                 subcategory = selectedSubcategory,
                 onDismiss = { 
                     showDeleteConfirmationDialog = false
                     selectedCategory = null
                     selectedSubcategory = null
                 },
                 onConfirm = { 
                     // Delete category or subcategory
                     if (selectedSubcategory != null) {
                         // Remove subcategory from custom list
                         customSubcategories = customSubcategories.filter { it != selectedSubcategory }
                     } else if (selectedCategory != null) {
                         // Remove category from custom list
                         customCategories = customCategories.filter { it != selectedCategory }
                     }
                     showDeleteConfirmationDialog = false
                     selectedCategory = null
                     selectedSubcategory = null
                 },
                 isDarkTheme = isDarkTheme
             )
         }
     }
 }
 
 @Composable
private fun AddMainCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isDarkTheme: Boolean
) {
     var categoryName by remember { mutableStateOf("") }
     var selectedIcon by remember { mutableStateOf(Icons.Default.Category) }
     var selectedColor by remember { mutableStateOf(Color.Blue) }
     
     AlertDialog(
         onDismissRequest = onDismiss,
         title = {
             Text(
                 text = "Yeni Ana Kategori Ekle",
                 fontSize = 18.sp,
                 fontWeight = FontWeight.Bold,
                 color = ThemeColors.getTextColor(isDarkTheme)
             )
         },
         text = {
             Column(
                 verticalArrangement = Arrangement.spacedBy(16.dp)
             ) {
                 // Category Name Input
                 Column(
                     verticalArrangement = Arrangement.spacedBy(8.dp)
                 ) {
                     Text(
                         text = "Kategori Adı",
                         fontSize = 18.sp,
                         fontWeight = FontWeight.Medium,
                         color = ThemeColors.getTextColor(isDarkTheme)
                     )
                     
                     Box(
                         modifier = Modifier
                             .fillMaxWidth()
                             .height(40.dp)
                             .background(
                                 ThemeColors.getInputBackgroundColor(isDarkTheme),
                                 RoundedCornerShape(12.dp)
                             )
                             .border(
                                 width = 1.dp,
                                 color = ThemeColors.getTextGrayColor(isDarkTheme),
                                 shape = RoundedCornerShape(12.dp)
                             )
                     ) {
                         BasicTextField(
                             value = categoryName,
                             onValueChange = { categoryName = it },
                             modifier = Modifier
                                 .fillMaxSize()
                                 .padding(horizontal = 12.dp),
                             singleLine = true,
                             textStyle = androidx.compose.ui.text.TextStyle(
                                 fontSize = 14.sp,
                                 color = ThemeColors.getTextColor(isDarkTheme)
                             ),
                             decorationBox = { innerTextField ->
                                 Box(
                                     contentAlignment = Alignment.CenterStart
                                 ) {
                                     if (categoryName.isEmpty()) {
                                         Text(
                                             text = "Kategori adı girin",
                                             fontSize = 14.sp,
                                             color = ThemeColors.getTextGrayColor(isDarkTheme)
                                         )
                                     }
                                     innerTextField()
                                 }
                             }
                         )
                     }
                     
                     Text(
                         text = "Kategori için benzersiz bir isim belirleyin",
                         fontSize = 14.sp,
                         color = ThemeColors.getTextGrayColor(isDarkTheme)
                     )
                 }
                 
                 // Icon and Color Selection (simplified for now)
                 Text(
                     text = "Icon ve renk seçimi yakında eklenecek",
                     fontSize = 14.sp,
                     color = ThemeColors.getTextGrayColor(isDarkTheme)
                 )
             }
         },
         confirmButton = {
             Button(
                 onClick = { onConfirm(categoryName) },
                 enabled = categoryName.isNotBlank(),
                 colors = ButtonDefaults.buttonColors(
                     containerColor = AppColors.PrimaryOrange
                 ),
                 shape = RoundedCornerShape(16.dp)
             ) {
                 Text(
                     text = "Ekle",
                     fontSize = 18.sp,
                     fontWeight = FontWeight.SemiBold,
                     color = ThemeColors.getTextColor(isDarkTheme)
                 )
             }
         },
         dismissButton = {
             Button(
                 onClick = onDismiss,
                 colors = ButtonDefaults.buttonColors(
                     containerColor = ThemeColors.getButtonDisabledColor(isDarkTheme)
                 ),
                 shape = RoundedCornerShape(16.dp)
             ) {
                 Text(
                     text = "İptal",
                     fontSize = 18.sp,
                     fontWeight = FontWeight.Medium,
                     color = ThemeColors.getTextColor(isDarkTheme)
                 )
             }
         }
     )
 }
 
 @Composable
private fun AddSubcategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isDarkTheme: Boolean
) {
     var subcategoryName by remember { mutableStateOf("") }
     var selectedCategory by remember { mutableStateOf(ExpenseCategory.FOOD) }
     
     AlertDialog(
         onDismissRequest = onDismiss,
         title = {
             Text(
                 text = "Yeni Alt Kategori Ekle",
                 fontSize = 18.sp,
                 fontWeight = FontWeight.Bold,
                 color = ThemeColors.getTextColor(isDarkTheme)
             )
         },
         text = {
             Column(
                 verticalArrangement = Arrangement.spacedBy(16.dp)
             ) {
                 // Category Selection
                 Column(
                     verticalArrangement = Arrangement.spacedBy(8.dp)
                 ) {
                     Text(
                         text = "Ana Kategori",
                         fontSize = 18.sp,
                         fontWeight = FontWeight.Medium,
                         color = ThemeColors.getTextColor(isDarkTheme)
                     )
                     
                     Text(
                         text = selectedCategory.displayName,
                         fontSize = 14.sp,
                         color = ThemeColors.getTextColor(isDarkTheme),
                         modifier = Modifier
                             .fillMaxWidth()
                             .background(
                                 ThemeColors.getInputBackgroundColor(isDarkTheme),
                                 RoundedCornerShape(12.dp)
                             )
                             .border(
                                 width = 1.dp,
                                 color = ThemeColors.getTextGrayColor(isDarkTheme),
                                 shape = RoundedCornerShape(12.dp)
                             )
                             .padding(12.dp)
                     )
                     
                     Text(
                         text = "Alt kategori eklenecek ana kategori",
                         fontSize = 14.sp,
                         color = ThemeColors.getTextGrayColor(isDarkTheme)
                     )
                 }
                 
                 // Subcategory Name Input
                 Column(
                     verticalArrangement = Arrangement.spacedBy(8.dp)
                 ) {
                     Text(
                         text = "Alt Kategori Adı",
                         fontSize = 18.sp,
                         fontWeight = FontWeight.Medium,
                         color = ThemeColors.getTextColor(isDarkTheme)
                     )
                     
                     Box(
                         modifier = Modifier
                             .fillMaxWidth()
                             .height(40.dp)
                             .background(
                                 ThemeColors.getInputBackgroundColor(isDarkTheme),
                                 RoundedCornerShape(12.dp)
                             )
                             .border(
                                 width = 1.dp,
                                 color = ThemeColors.getTextGrayColor(isDarkTheme),
                                 shape = RoundedCornerShape(12.dp)
                             )
                     ) {
                         BasicTextField(
                             value = subcategoryName,
                             onValueChange = { subcategoryName = it },
                             modifier = Modifier
                                 .fillMaxSize()
                                 .padding(horizontal = 12.dp),
                             singleLine = true,
                             textStyle = androidx.compose.ui.text.TextStyle(
                                 fontSize = 14.sp,
                                 color = ThemeColors.getTextColor(isDarkTheme)
                             ),
                             decorationBox = { innerTextField ->
                                 Box(
                                     contentAlignment = Alignment.CenterStart
                                 ) {
                                     if (subcategoryName.isEmpty()) {
                                         Text(
                                             text = "Alt kategori adı girin",
                                             fontSize = 14.sp,
                                             color = ThemeColors.getTextGrayColor(isDarkTheme)
                                         )
                                     }
                                     innerTextField()
                                 }
                             }
                         )
                     }
                     
                     Text(
                         text = "Alt kategori için benzersiz bir isim belirleyin",
                         fontSize = 14.sp,
                         color = ThemeColors.getTextGrayColor(isDarkTheme)
                     )
                 }
             }
         },
         confirmButton = {
             Button(
                 onClick = { onConfirm(subcategoryName) },
                 enabled = subcategoryName.isNotBlank(),
                 colors = ButtonDefaults.buttonColors(
                     containerColor = AppColors.PrimaryOrange
                 ),
                 shape = RoundedCornerShape(16.dp)
             ) {
                 Text(
                     text = "Ekle",
                     fontSize = 18.sp,
                     fontWeight = FontWeight.SemiBold,
                     color = ThemeColors.getTextColor(isDarkTheme)
                 )
             }
         },
         dismissButton = {
             Button(
                 onClick = onDismiss,
                 colors = ButtonDefaults.buttonColors(
                     containerColor = ThemeColors.getButtonDisabledColor(isDarkTheme)
                 ),
                 shape = RoundedCornerShape(16.dp)
             ) {
                 Text(
                     text = "İptal",
                     fontSize = 18.sp,
                     fontWeight = FontWeight.Medium,
                     color = ThemeColors.getTextColor(isDarkTheme)
                 )
             }
         }
     )
 }
 
 @Composable
private fun EditCategoryDialog(
    category: ExpenseCategory?,
    subcategory: com.example.expensetrackerkotlin.data.ExpenseSubCategory?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isDarkTheme: Boolean
) {
     var editName by remember { mutableStateOf("") }
     
     LaunchedEffect(category, subcategory) {
         editName = when {
             subcategory != null -> subcategory.name
             category != null -> category.displayName
             else -> ""
         }
     }
     
     AlertDialog(
         onDismissRequest = onDismiss,
         title = {
             Text(
                 text = when {
                     subcategory != null -> "Alt Kategori Düzenle"
                     category != null -> "Kategori Düzenle"
                     else -> "Düzenle"
                 },
                 fontSize = 18.sp,
                 fontWeight = FontWeight.Bold,
                 color = ThemeColors.getTextColor(isDarkTheme)
             )
         },
         text = {
             Column(
                 verticalArrangement = Arrangement.spacedBy(8.dp)
             ) {
                 Text(
                     text = "Ad",
                     fontSize = 18.sp,
                     fontWeight = FontWeight.Medium,
                     color = ThemeColors.getTextColor(isDarkTheme)
                 )
                 
                 Box(
                     modifier = Modifier
                         .fillMaxWidth()
                         .height(40.dp)
                         .background(
                             ThemeColors.getInputBackgroundColor(isDarkTheme),
                             RoundedCornerShape(12.dp)
                         )
                         .border(
                             width = 1.dp,
                             color = ThemeColors.getTextGrayColor(isDarkTheme),
                             shape = RoundedCornerShape(12.dp)
                         )
                 ) {
                     BasicTextField(
                         value = editName,
                         onValueChange = { editName = it },
                         modifier = Modifier
                             .fillMaxSize()
                             .padding(horizontal = 12.dp),
                         singleLine = true,
                         textStyle = androidx.compose.ui.text.TextStyle(
                             fontSize = 14.sp,
                             color = ThemeColors.getTextColor(isDarkTheme)
                         ),
                         decorationBox = { innerTextField ->
                             Box(
                                 contentAlignment = Alignment.CenterStart
                             ) {
                                 if (editName.isEmpty()) {
                                     Text(
                                         text = "Ad girin",
                                         fontSize = 14.sp,
                                         color = ThemeColors.getTextGrayColor(isDarkTheme)
                                     )
                                 }
                                 innerTextField()
                             }
                         }
                     )
                 }
                 
                 Text(
                     text = "Kategori veya alt kategori için yeni ad belirleyin",
                     fontSize = 14.sp,
                     color = ThemeColors.getTextGrayColor(isDarkTheme)
                 )
             }
         },
         confirmButton = {
             Button(
                 onClick = { onConfirm(editName) },
                 enabled = editName.isNotBlank(),
                 colors = ButtonDefaults.buttonColors(
                     containerColor = AppColors.PrimaryOrange
                 ),
                 shape = RoundedCornerShape(16.dp)
             ) {
                 Text(
                     text = "Kaydet",
                     fontSize = 18.sp,
                     fontWeight = FontWeight.SemiBold,
                     color = ThemeColors.getTextColor(isDarkTheme)
                 )
             }
         },
         dismissButton = {
             Button(
                 onClick = onDismiss,
                 colors = ButtonDefaults.buttonColors(
                     containerColor = ThemeColors.getButtonDisabledColor(isDarkTheme)
                 ),
                 shape = RoundedCornerShape(16.dp)
             ) {
                 Text(
                     text = "İptal",
                     fontSize = 18.sp,
                     fontWeight = FontWeight.Medium,
                     color = ThemeColors.getTextColor(isDarkTheme)
                 )
             }
         }
     )
 }
 
 @Composable
 private fun DeleteConfirmationDialog(
     category: ExpenseCategory?,
     subcategory: com.example.expensetrackerkotlin.data.ExpenseSubCategory?,
     onDismiss: () -> Unit,
     onConfirm: () -> Unit,
     isDarkTheme: Boolean
 ) {
     val itemName = when {
         subcategory != null -> subcategory.name
         category != null -> category.displayName
         else -> "Bu öğe"
     }
     
     AlertDialog(
         onDismissRequest = onDismiss,
         title = {
             Text(
                 text = "Silme Onayı",
                 color = ThemeColors.getTextColor(isDarkTheme),
                 fontWeight = FontWeight.Bold
             )
         },
         text = {
             Text(
                 text = "\"$itemName\" öğesini silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.",
                 color = ThemeColors.getTextColor(isDarkTheme)
             )
         },
         confirmButton = {
             TextButton(
                 onClick = onConfirm
             ) {
                 Text(
                     "Sil", 
                     color = Color.Red,
                     fontWeight = FontWeight.Bold
                 )
             }
         },
         dismissButton = {
             TextButton(
                 onClick = onDismiss
             ) {
                 Text(
                     "İptal", 
                     color = ThemeColors.getTextColor(isDarkTheme)
                 )
             }
         },
         containerColor = ThemeColors.getDialogBackgroundColor(isDarkTheme)
     )
 }

@Composable
private fun CategoryTreeItem(
    category: ExpenseCategory,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onEditSubcategory: (com.example.expensetrackerkotlin.data.ExpenseSubCategory) -> Unit,
    onDeleteSubcategory: (com.example.expensetrackerkotlin.data.ExpenseSubCategory) -> Unit,
    allSubcategories: List<com.example.expensetrackerkotlin.data.ExpenseSubCategory>,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val subCategories = allSubcategories.filter { it.category == category }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                ThemeColors.getInputBackgroundColor(isDarkTheme),
                RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = ThemeColors.getTextGrayColor(isDarkTheme).copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        // Main Category Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpanded() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Expand/Collapse Icon
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                modifier = Modifier.size(20.dp),
                tint = ThemeColors.getTextGrayColor(isDarkTheme)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Category Icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        category.getColor().copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.getIcon(),
                    contentDescription = category.displayName,
                    modifier = Modifier.size(20.dp),
                    tint = category.getColor()
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Category Name
            Text(
                text = category.displayName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = ThemeColors.getTextColor(isDarkTheme),
                modifier = Modifier.weight(1f)
            )
            
            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Edit Button
                IconButton(
                    onClick = { onEdit() },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.Transparent ,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Category",
                        modifier = Modifier.size(16.dp),
                        tint = AppColors.RecurringButtonStart
                    )
                }
                
                // Delete Button
                IconButton(
                    onClick = { onDelete() },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.Transparent ,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Category",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Red
                    )
                }
            }
        }
        
        // Subcategories (when expanded)
        if (isExpanded) {
                         subCategories.forEach { subCategory ->
                 SubCategoryItem(
                     subCategory = subCategory,
                     onEdit = { onEditSubcategory(subCategory) },
                     onDelete = { onDeleteSubcategory(subCategory) },
                     isDarkTheme = isDarkTheme,
                     modifier = Modifier.padding(start = 48.dp, end = 16.dp, bottom = 8.dp)
                 )
             }
            

        }
    }
}

@Composable
private fun SubCategoryItem(
    subCategory: com.example.expensetrackerkotlin.data.ExpenseSubCategory,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                ThemeColors.getBackgroundColor(isDarkTheme),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Subcategory Icon (using parent category icon)
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    subCategory.category.getColor().copy(alpha = 0.1f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = subCategory.category.getIcon(),
                contentDescription = subCategory.name,
                modifier = Modifier.size(14.dp),
                tint = subCategory.category.getColor()
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Subcategory Name
        Text(
            text = subCategory.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = ThemeColors.getTextColor(isDarkTheme),
            modifier = Modifier.weight(1f)
        )
        
        // Action Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Edit Button
            IconButton(
                onClick = { onEdit() },
                modifier = Modifier
                    .size(28.dp)

                    .background(Color.Transparent ,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Subcategory",
                    modifier = Modifier.size(14.dp),
                    tint = AppColors.RecurringButtonStart
                )
            }
            
            // Delete Button
            IconButton(
                onClick = { onDelete() },
                modifier = Modifier
                    .size(28.dp)

                    .background(Color.Transparent ,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Subcategory",
                    modifier = Modifier.size(14.dp),
                    tint = Color.Red
                )
            }
        }
    }
}
