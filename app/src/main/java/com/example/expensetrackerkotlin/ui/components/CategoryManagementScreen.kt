package com.example.expensetrackerkotlin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.expensetrackerkotlin.R
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import androidx.core.graphics.toColorInt

@Composable
fun CategoryManagementScreen(
    modifier: Modifier = Modifier,
    viewModel: com.example.expensetrackerkotlin.viewmodel.ExpenseViewModel,
    isDarkTheme: Boolean = true,
) {
    var expandedCategories by remember { mutableStateOf(setOf<String>()) } // Store category IDs
    var showAddMainCategoryDialog by remember { mutableStateOf(false) }
    var showAddSubcategoryDialog by remember { mutableStateOf(false) }
    var selectedCategoryForSubcategory by remember { mutableStateOf<com.example.expensetrackerkotlin.data.Category?>(null) }
    var showEditCategoryDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<com.example.expensetrackerkotlin.data.Category?>(null) }
    var selectedSubcategory by remember { mutableStateOf<com.example.expensetrackerkotlin.data.SubCategory?>(null) }
    
    // Get categories and subcategories from ViewModel
    val categories by viewModel.categories.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()
    
    Column(
         modifier = modifier
             .fillMaxSize()
             .padding(20.dp)
     ) {
         // Tree View
                 LazyColumn(
             modifier = Modifier.weight(1f),
             verticalArrangement = Arrangement.spacedBy(8.dp)
         ) {
             items(categories) { category ->
                                 CategoryTreeItem(
                     category = category,
                     isExpanded = expandedCategories.contains(category.id),
                     onToggleExpanded = {
                         expandedCategories = if (expandedCategories.contains(category.id)) {
                             expandedCategories - category.id
                         } else {
                             expandedCategories + category.id
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
                     onAddSubcategory = { category ->
                         selectedCategoryForSubcategory = category
                         showAddSubcategoryDialog = true
                     },
                     allSubcategories = subCategories,
                     isDarkTheme = isDarkTheme
                 )
            }
        }
        
        // Add Buttons Row
        Column(
            modifier = Modifier.padding(top = 16.dp),
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
                    contentDescription = stringResource(R.string.add_main_category_content_desc),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.add_new_main_category),
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
                 onConfirm = { categoryName, iconName, colorHex ->
                     // Add new custom category
                     viewModel.createCustomCategory(
                         name = categoryName,
                         colorHex = colorHex,
                         iconName = iconName
                     )
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
                     // Add new subcategory to the selected category
                     selectedCategoryForSubcategory?.let { category ->
                         viewModel.createCustomSubCategory(
                             name = subcategoryName,
                             categoryId = category.id
                         )
                     }
                     showAddSubcategoryDialog = false 
                     selectedCategoryForSubcategory = null
                 },
                 selectedCategory = selectedCategoryForSubcategory,
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
                 onConfirm = { newName, iconName, colorHex ->
                     // Update category or subcategory
                     if (selectedSubcategory != null) {
                         // Update subcategory (only name)
                         val updatedSubCategory = selectedSubcategory!!.copy(name = newName)
                         viewModel.updateSubCategory(updatedSubCategory)
                     } else if (selectedCategory != null && iconName != null && colorHex != null) {
                         // Update category (name, icon, and color)
                         val updatedCategory = selectedCategory!!.copy(
                             name = newName,
                             iconName = iconName,
                             colorHex = colorHex
                         )
                         viewModel.updateCategory(updatedCategory)
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
                         // Delete subcategory
                         viewModel.deleteSubCategory(selectedSubcategory!!)
                     } else if (selectedCategory != null) {
                         // Delete category
                         viewModel.deleteCategory(selectedCategory!!)
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
    onConfirm: (String, String, String) -> Unit, // name, iconName, colorHex
    isDarkTheme: Boolean
) {
     var categoryName by remember { mutableStateOf("") }
     var selectedIconName by remember { mutableStateOf("category") }
     var selectedColorHex by remember { mutableStateOf("#FF9500") }
     
     // Available icons
     val availableIcons = listOf(
         "restaurant" to Icons.Default.Restaurant,
         "home" to Icons.Default.Home,
         "directions_car" to Icons.Default.DirectionsCar,
         "local_hospital" to Icons.Default.LocalHospital,
         "movie" to Icons.Default.Movie,
         "school" to Icons.Default.School,
         "shopping_cart" to Icons.Default.ShoppingCart,
         "pets" to Icons.Default.Pets,
         "work" to Icons.Default.Work,
         "account_balance" to Icons.Default.AccountBalance,
         "favorite" to Icons.Default.Favorite,
         "category" to Icons.Default.Category,
         "sports" to Icons.Default.Sports,
         "music_note" to Icons.Default.MusicNote,
         "flight" to Icons.Default.Flight,
         "hotel" to Icons.Default.Hotel,
         "restaurant_menu" to Icons.Default.RestaurantMenu,
         "local_gas_station" to Icons.Default.LocalGasStation,
         "phone" to Icons.Default.Phone,
         "computer" to Icons.Default.Computer,
         "book" to Icons.Default.Book,
         "cake" to Icons.Default.Cake,
         "coffee" to Icons.Default.Coffee,
         "directions_bus" to Icons.Default.DirectionsBus,
         "directions_walk" to Icons.AutoMirrored.Filled.DirectionsWalk,
         "eco" to Icons.Default.Eco,
         "fitness_center" to Icons.Default.FitnessCenter,
         "gavel" to Icons.Default.Gavel,
         "healing" to Icons.Default.Healing,
         "kitchen" to Icons.Default.Kitchen,
         "local_laundry_service" to Icons.Default.LocalLaundryService,
         "local_pharmacy" to Icons.Default.LocalPharmacy,
         "local_pizza" to Icons.Default.LocalPizza,
         "local_shipping" to Icons.Default.LocalShipping,
         "lunch_dining" to Icons.Default.LunchDining,
         "monetization_on" to Icons.Default.MonetizationOn,
         "palette" to Icons.Default.Palette,
         "park" to Icons.Default.Park,
         "pool" to Icons.Default.Pool,
         "psychology" to Icons.Default.Psychology,
         "receipt" to Icons.Default.Receipt,
         "security" to Icons.Default.Security,
         "spa" to Icons.Default.Spa,
         "star" to Icons.Default.Star,
         "theater_comedy" to Icons.Default.TheaterComedy,
         "toys" to Icons.Default.Toys,
         "volunteer_activism" to Icons.Default.VolunteerActivism,
         "water_drop" to Icons.Default.WaterDrop,
         "wifi" to Icons.Default.Wifi
     )
     
     // Available colors
     val availableColors = listOf(
         "#FF9500", "#007AFF", "#34C759", "#FF2D92", "#9D73E3",
         "#5856D6", "#FF3B30", "#64D2FF", "#5AC8FA", "#FFD60A",
         "#30D158", "#3F51B5", "#FF6B35", "#4ECDC4", "#45B7D1",
         "#96CEB4", "#FFEAA7", "#DDA0DD", "#98D8C8", "#F7DC6F"
     )
     
     AlertDialog(
         onDismissRequest = onDismiss,
         text = {
             Column(
                 verticalArrangement = Arrangement.spacedBy(16.dp)
             ) {
                 // Category Name Input
                 Column(
                     verticalArrangement = Arrangement.spacedBy(8.dp)
                 ) {

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
                                             text = stringResource(R.string.category_name_hint),
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
                         text = stringResource(R.string.unique_category_name_note),
                         fontSize = 14.sp,
                         color = ThemeColors.getTextGrayColor(isDarkTheme)
                     )
                 }
                 
                 // Icon Selection
                 Column(
                     verticalArrangement = Arrangement.spacedBy(8.dp)
                 ) {
                     Text(
                         text = stringResource(R.string.icon_selection),
                         fontSize = 18.sp,
                         fontWeight = FontWeight.Medium,
                         color = ThemeColors.getTextColor(isDarkTheme)
                     )
                     
                     LazyRow(
                         horizontalArrangement = Arrangement.spacedBy(8.dp)
                     ) {
                         items(availableIcons.size) { index ->
                             val (iconName, icon) = availableIcons[index]
                             val isSelected = selectedIconName == iconName
                             
                             Box(
                                 modifier = Modifier
                                     .size(48.dp)
                                     .background(
                                         if (isSelected) Color(selectedColorHex.toColorInt()).copy(alpha = 0.2f)
                                         else ThemeColors.getInputBackgroundColor(isDarkTheme),
                                         CircleShape
                                     )
                                     .border(
                                         width = if (isSelected) 2.dp else 1.dp,
                                         color = if (isSelected) Color(selectedColorHex.toColorInt())
                                                else ThemeColors.getTextGrayColor(isDarkTheme),
                                         shape = CircleShape
                                     )
                                     .clickable { selectedIconName = iconName },
                                 contentAlignment = Alignment.Center
                             ) {
                                 Icon(
                                     imageVector = icon,
                                     contentDescription = iconName,
                                     modifier = Modifier.size(24.dp),
                                     tint = if (isSelected) Color(selectedColorHex.toColorInt())
                                           else ThemeColors.getTextGrayColor(isDarkTheme)
                                 )
                             }
                         }
                     }
                 }
                 
                 // Color Selection
                 Column(
                     verticalArrangement = Arrangement.spacedBy(8.dp)
                 ) {
                     Text(
                         text = stringResource(R.string.color_selection),
                         fontSize = 18.sp,
                         fontWeight = FontWeight.Medium,
                         color = ThemeColors.getTextColor(isDarkTheme)
                     )
                     
                     LazyRow(
                         horizontalArrangement = Arrangement.spacedBy(8.dp)
                     ) {
                         items(availableColors.size) { index ->
                             val colorHex = availableColors[index]
                             val isSelected = selectedColorHex == colorHex
                             
                             Box(
                                 modifier = Modifier
                                     .size(40.dp)
                                     .background(
                                         Color(colorHex.toColorInt()),
                                         CircleShape
                                     )
                                     .border(
                                         width = if (isSelected) 3.dp else 1.dp,
                                         color = if (isSelected) Color.White else Color.Transparent,
                                         shape = CircleShape
                                     )
                                     .clickable { selectedColorHex = colorHex },
                                 contentAlignment = Alignment.Center
                             ) {
                                 if (isSelected) {
                                     Icon(
                                         imageVector = Icons.Default.Check,
                                         contentDescription = stringResource(R.string.selected),
                                         modifier = Modifier.size(20.dp),
                                         tint = Color.White
                                     )
                                 }
                             }
                         }
                     }
                 }
             }
         },
         confirmButton = {
             Button(
                 onClick = { onConfirm(categoryName, selectedIconName, selectedColorHex) },
                 enabled = categoryName.isNotBlank(),
                 colors = ButtonDefaults.buttonColors(
                     containerColor = AppColors.PrimaryOrange
                 ),
                 shape = RoundedCornerShape(16.dp)
             ) {
                 Text(
                     text = stringResource(R.string.add_button),
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
                     text = stringResource(R.string.cancel),
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
    selectedCategory: com.example.expensetrackerkotlin.data.Category?,
    isDarkTheme: Boolean
) {
     var subcategoryName by remember { mutableStateOf("") }
     
     AlertDialog(
         onDismissRequest = onDismiss,
         text = {
             Column(
                 verticalArrangement = Arrangement.spacedBy(16.dp)
             ) {
                 // Category Selection
                 Column(
                     verticalArrangement = Arrangement.spacedBy(8.dp)
                 ) {
                     Text(
                         text = stringResource(R.string.main_category),
                         fontSize = 18.sp,
                         fontWeight = FontWeight.Medium,
                         color = ThemeColors.getTextColor(isDarkTheme)
                     )
                     
                     Text(
                         text = selectedCategory?.name ?: stringResource(R.string.select_category),
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
                     

                 }
                 
                 // Subcategory Name Input
                 Column(
                     verticalArrangement = Arrangement.spacedBy(8.dp)
                 ) {
                     Text(
                         text = stringResource(R.string.subcategory_name),
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
                                             text = stringResource(R.string.subcategory_name_hint),
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
                         text = stringResource(R.string.unique_subcategory_name_note),
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
                     text = stringResource(R.string.add_button),
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
                     text = stringResource(R.string.cancel),
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
    category: com.example.expensetrackerkotlin.data.Category?,
    subcategory: com.example.expensetrackerkotlin.data.SubCategory?,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, String?) -> Unit, // name, iconName (null for subcategories), colorHex (null for subcategories)
    isDarkTheme: Boolean
) {
     var editName by remember { mutableStateOf("") }
     var selectedIconName by remember { mutableStateOf("category") }
     var selectedColorHex by remember { mutableStateOf("#FF9500") }
     
     // Available icons (same as AddMainCategoryDialog)
     val availableIcons = listOf(
         "restaurant" to Icons.Default.Restaurant,
         "home" to Icons.Default.Home,
         "directions_car" to Icons.Default.DirectionsCar,
         "local_hospital" to Icons.Default.LocalHospital,
         "movie" to Icons.Default.Movie,
         "school" to Icons.Default.School,
         "shopping_cart" to Icons.Default.ShoppingCart,
         "pets" to Icons.Default.Pets,
         "work" to Icons.Default.Work,
         "account_balance" to Icons.Default.AccountBalance,
         "favorite" to Icons.Default.Favorite,
         "category" to Icons.Default.Category,
         "sports" to Icons.Default.Sports,
         "music_note" to Icons.Default.MusicNote,
         "flight" to Icons.Default.Flight,
         "hotel" to Icons.Default.Hotel,
         "restaurant_menu" to Icons.Default.RestaurantMenu,
         "local_gas_station" to Icons.Default.LocalGasStation,
         "phone" to Icons.Default.Phone,
         "computer" to Icons.Default.Computer,
         "book" to Icons.Default.Book,
         "cake" to Icons.Default.Cake,
         "coffee" to Icons.Default.Coffee,
         "directions_bus" to Icons.Default.DirectionsBus,
         "directions_walk" to Icons.AutoMirrored.Filled.DirectionsWalk,
         "eco" to Icons.Default.Eco,
         "fitness_center" to Icons.Default.FitnessCenter,
         "gavel" to Icons.Default.Gavel,
         "healing" to Icons.Default.Healing,
         "kitchen" to Icons.Default.Kitchen,
         "local_laundry_service" to Icons.Default.LocalLaundryService,
         "local_pharmacy" to Icons.Default.LocalPharmacy,
         "local_pizza" to Icons.Default.LocalPizza,
         "local_shipping" to Icons.Default.LocalShipping,
         "lunch_dining" to Icons.Default.LunchDining,
         "monetization_on" to Icons.Default.MonetizationOn,
         "palette" to Icons.Default.Palette,
         "park" to Icons.Default.Park,
         "pool" to Icons.Default.Pool,
         "psychology" to Icons.Default.Psychology,
         "receipt" to Icons.Default.Receipt,
         "security" to Icons.Default.Security,
         "spa" to Icons.Default.Spa,
         "star" to Icons.Default.Star,
         "theater_comedy" to Icons.Default.TheaterComedy,
         "toys" to Icons.Default.Toys,
         "volunteer_activism" to Icons.Default.VolunteerActivism,
         "water_drop" to Icons.Default.WaterDrop,
         "wifi" to Icons.Default.Wifi
     )
     
     // Available colors (same as AddMainCategoryDialog)
     val availableColors = listOf(
         "#FF9500", "#007AFF", "#34C759", "#FF2D92", "#9D73E3",
         "#5856D6", "#FF3B30", "#64D2FF", "#5AC8FA", "#FFD60A",
         "#30D158", "#3F51B5", "#FF6B35", "#4ECDC4", "#45B7D1",
         "#96CEB4", "#FFEAA7", "#DDA0DD", "#98D8C8", "#F7DC6F"
     )
     
     LaunchedEffect(category, subcategory) {
         editName = when {
             subcategory != null -> subcategory.name
             category != null -> category.name
             else -> ""
         }
         
         // Set icon and color for categories (not subcategories)
         if (category != null) {
             selectedIconName = category.iconName
             selectedColorHex = category.colorHex
         }
     }
     
     AlertDialog(
         onDismissRequest = onDismiss,
         title = {
             Text(
                 text = when {
                     subcategory != null -> stringResource(R.string.edit_subcategory)
                     category != null -> stringResource(R.string.edit_category)
                     else -> stringResource(R.string.edit)
                 },
                 fontSize = 18.sp,
                 fontWeight = FontWeight.Bold,
                 color = ThemeColors.getTextColor(isDarkTheme)
             )
         },
         text = {
             Column(
                 verticalArrangement = Arrangement.spacedBy(16.dp)
             ) {
                 // Name Input
                 Column(
                     verticalArrangement = Arrangement.spacedBy(8.dp)
                 ) {

                     
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
                                             text = stringResource(R.string.enter_name_hint),
                                             fontSize = 14.sp,
                                             color = ThemeColors.getTextGrayColor(isDarkTheme)
                                         )
                                     }
                                     innerTextField()
                                 }
                             }
                         )
                     }
                 }
                 
                 // Icon and Color Selection (only for categories, not subcategories)
                 if (category != null) {
                     // Icon Selection
                     Column(
                         verticalArrangement = Arrangement.spacedBy(8.dp)
                     ) {
                         Text(
                             text = stringResource(R.string.icon_selection),
                             fontSize = 18.sp,
                             fontWeight = FontWeight.Medium,
                             color = ThemeColors.getTextColor(isDarkTheme)
                         )
                         
                         LazyRow(
                             horizontalArrangement = Arrangement.spacedBy(8.dp)
                         ) {
                             items(availableIcons.size) { index ->
                                 val (iconName, icon) = availableIcons[index]
                                 val isSelected = selectedIconName == iconName
                                 
                                 Box(
                                     modifier = Modifier
                                         .size(48.dp)
                                         .background(
                                             if (isSelected) Color(selectedColorHex.toColorInt()).copy(alpha = 0.2f)
                                             else ThemeColors.getInputBackgroundColor(isDarkTheme),
                                             CircleShape
                                         )
                                         .border(
                                             width = if (isSelected) 2.dp else 1.dp,
                                             color = if (isSelected) Color(selectedColorHex.toColorInt())
                                                    else ThemeColors.getTextGrayColor(isDarkTheme),
                                             shape = CircleShape
                                         )
                                         .clickable { selectedIconName = iconName },
                                     contentAlignment = Alignment.Center
                                 ) {
                                     Icon(
                                         imageVector = icon,
                                         contentDescription = iconName,
                                         modifier = Modifier.size(24.dp),
                                         tint = if (isSelected) Color(selectedColorHex.toColorInt())
                                               else ThemeColors.getTextGrayColor(isDarkTheme)
                                     )
                                 }
                             }
                         }
                     }
                     
                     // Color Selection
                     Column(
                         verticalArrangement = Arrangement.spacedBy(8.dp)
                     ) {
                         Text(
                             text = stringResource(R.string.color_selection),
                             fontSize = 18.sp,
                             fontWeight = FontWeight.Medium,
                             color = ThemeColors.getTextColor(isDarkTheme)
                         )
                         
                         LazyRow(
                             horizontalArrangement = Arrangement.spacedBy(8.dp)
                         ) {
                             items(availableColors.size) { index ->
                                 val colorHex = availableColors[index]
                                 val isSelected = selectedColorHex == colorHex
                                 
                                 Box(
                                     modifier = Modifier
                                         .size(40.dp)
                                         .background(
                                             Color(colorHex.toColorInt()),
                                             CircleShape
                                         )
                                         .border(
                                             width = if (isSelected) 3.dp else 1.dp,
                                             color = if (isSelected) Color.White else Color.Transparent,
                                             shape = CircleShape
                                         )
                                         .clickable { selectedColorHex = colorHex },
                                     contentAlignment = Alignment.Center
                                 ) {
                                     if (isSelected) {
                                         Icon(
                                             imageVector = Icons.Default.Check,
                                             contentDescription = stringResource(R.string.selected),
                                             modifier = Modifier.size(20.dp),
                                             tint = Color.White
                                         )
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         },
         confirmButton = {
             Button(
                 onClick = { 
                     onConfirm(
                         editName, 
                         if (category != null) selectedIconName else null,
                         if (category != null) selectedColorHex else null
                     ) 
                 },
                 enabled = editName.isNotBlank(),
                 colors = ButtonDefaults.buttonColors(
                     containerColor = AppColors.PrimaryOrange
                 ),
                 shape = RoundedCornerShape(16.dp)
             ) {
                 Text(
                     text = stringResource(R.string.save),
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
                     text = stringResource(R.string.cancel),
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
     category: com.example.expensetrackerkotlin.data.Category?,
     subcategory: com.example.expensetrackerkotlin.data.SubCategory?,
     onDismiss: () -> Unit,
     onConfirm: () -> Unit,
     isDarkTheme: Boolean
 ) {
     val itemName = when {
         subcategory != null -> subcategory.name
         category != null -> category.name
         else -> stringResource(R.string.this_item)
     }
     
     AlertDialog(
         onDismissRequest = onDismiss,
         title = {
             Text(
                 text = stringResource(R.string.delete_confirmation),
                 color = ThemeColors.getTextColor(isDarkTheme),
                 fontWeight = FontWeight.Bold
             )
         },
         text = {
             Text(
                 text = stringResource(R.string.delete_item_confirmation, itemName),
                 color = ThemeColors.getTextColor(isDarkTheme)
             )
         },
         confirmButton = {
             TextButton(
                 onClick = onConfirm
             ) {
                 Text(
                     stringResource(R.string.delete), 
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
                     stringResource(R.string.cancel), 
                     color = ThemeColors.getTextColor(isDarkTheme)
                 )
             }
         },
         containerColor = ThemeColors.getDialogBackgroundColor(isDarkTheme)
     )
 }

@Composable
private fun CategoryTreeItem(
    category: com.example.expensetrackerkotlin.data.Category,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onEditSubcategory: (com.example.expensetrackerkotlin.data.SubCategory) -> Unit,
    onDeleteSubcategory: (com.example.expensetrackerkotlin.data.SubCategory) -> Unit,
    onAddSubcategory: (com.example.expensetrackerkotlin.data.Category) -> Unit,
    allSubcategories: List<com.example.expensetrackerkotlin.data.SubCategory>,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val subCategories = allSubcategories.filter { it.categoryId == category.id }
    
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
                contentDescription = if (isExpanded) stringResource(R.string.collapse_desc) else stringResource(R.string.expand_desc),
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
                    contentDescription = category.name,
                    modifier = Modifier.size(20.dp),
                    tint = category.getColor()
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Category Name
            Text(
                text = category.name,
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
                        contentDescription = stringResource(R.string.edit_category_desc),
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
                        contentDescription = stringResource(R.string.delete_category_desc),
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
            
            // Add Subcategory Button
            OutlinedButton(
                onClick = { onAddSubcategory(category) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                    .height(36.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppColors.PrimaryOrange
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_subcategory_desc),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.add_subcategory),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SubCategoryItem(
    subCategory: com.example.expensetrackerkotlin.data.SubCategory,
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
        // Simple bullet point for subcategories (no icon)
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(
                    ThemeColors.getTextGrayColor(isDarkTheme),
                    CircleShape
                )
        )
        
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
                    contentDescription = stringResource(R.string.edit_subcategory_desc),
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
                    contentDescription = stringResource(R.string.delete_subcategory_desc),
                    modifier = Modifier.size(14.dp),
                    tint = Color.Red
                )
            }
        }
    }
}
