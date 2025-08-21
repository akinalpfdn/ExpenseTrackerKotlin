package com.example.expensetrackerkotlin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon and info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Category icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
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
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Expense info
                Column {
                    Text(
                        text = expense.subCategory,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (expense.description.isNotBlank()) {
                        Text(
                            text = expense.description,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = expense.date.format(DateTimeFormatter.ofPattern("HH:mm")),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            // Amount and actions
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (isEditing) {
                    // Edit mode
                    OutlinedTextField(
                        value = editAmount,
                        onValueChange = { editAmount = it },
                        modifier = Modifier.width(100.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    
                    Row {
                        IconButton(
                            onClick = {
                                val newAmount = editAmount.toDoubleOrNull()
                                if (newAmount != null) {
                                    onUpdate(
                                        expense.copy(
                                            amount = newAmount,
                                            description = editDescription
                                        )
                                    )
                                }
                                isEditing = false
                                onEditingChanged(false)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Save",
                                tint = Color.Green
                            )
                        }
                        
                        IconButton(
                            onClick = {
                                isEditing = false
                                onEditingChanged(false)
                                editAmount = expense.amount.toString()
                                editDescription = expense.description
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Cancel",
                                tint = Color.Gray
                            )
                        }
                    }
                } else {
                    // Display mode
                    Text(
                        text = "${expense.currency} ${String.format("%.2f", expense.amount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    
                    // Daily expense ratio bar
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(4.dp)
                            .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(dailyExpenseRatio.toFloat())
                                .background(
                                    expense.category.getColor(),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                    
                    // Actions
                    Row {
                        IconButton(
                            onClick = {
                                isEditing = true
                                onEditingChanged(true)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        IconButton(
                            onClick = onDelete
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.Red,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}