package com.example.expensetrackerkotlin.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.data.Expense
import com.example.expensetrackerkotlin.data.getColor
import com.example.expensetrackerkotlin.data.getIcon
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseRowView(
    expense: Expense,
    onUpdate: (Expense) -> Unit,
    onEditingChanged: (Boolean) -> Unit,
    onDelete: () -> Unit,
    isCurrentlyEditing: Boolean,
    dailyExpenseRatio: Double,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var editAmount by remember { mutableStateOf(expense.amount.toString()) }
    var editDescription by remember { mutableStateOf(expense.description) }
    
    LaunchedEffect(isCurrentlyEditing) {
        isEditing = isCurrentlyEditing
        onEditingChanged(isEditing)
    }
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .clickable { 
                    if (!isEditing) {
                        isEditing = true
                        onEditingChanged(true)
                    }
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.05f)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Main expense row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category icon and info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Category icon - smaller for compact design
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    expense.category.getColor().copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = expense.category.getIcon(),
                                contentDescription = expense.category.displayName,
                                tint = expense.category.getColor(),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        // Expense info - more compact
                        Column {
                            Text(
                                text = expense.subCategory,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (expense.description.isNotBlank()) {
                                Text(
                                    text = expense.description,
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    
                    // Amount - more compact
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "${expense.currency} ${String.format("%.0f", expense.amount)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = expense.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                // Expanded editing section - like Swift
                AnimatedVisibility(
                    visible = isEditing,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .background(
                                Color.White.copy(alpha = 0.05f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        // Edit Amount
                        OutlinedTextField(
                            value = editAmount,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    editAmount = newValue
                                }
                            },
                            label = { Text("Miktar", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Edit Description
                        OutlinedTextField(
                            value = editDescription,
                            onValueChange = { editDescription = it },
                            label = { Text("Açıklama", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Action buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Save button
                            Button(
                                onClick = {
                                    val newAmount = editAmount.toDoubleOrNull()
                                    if (newAmount != null && newAmount > 0) {
                                        onUpdate(
                                            expense.copy(
                                                amount = newAmount,
                                                description = editDescription
                                            )
                                        )
                                    }
                                    isEditing = false
                                    onEditingChanged(false)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Green
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Save",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Kaydet")
                            }
                            
                            // Delete button
                            Button(
                                onClick = onDelete,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red
                                )
                            ) {
                                Text("Sil")
                            }
                            
                            // Cancel button
                            OutlinedButton(
                                onClick = {
                                    isEditing = false
                                    onEditingChanged(false)
                                    editAmount = expense.amount.toString()
                                    editDescription = expense.description
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("İptal")
                            }
                        }
                    }
                }
            }
        }
        
        // Thin progress bar underneath - like Swift version
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color.Gray.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(dailyExpenseRatio.toFloat())
                    .background(expense.category.getColor())
            )
        }
    }
}