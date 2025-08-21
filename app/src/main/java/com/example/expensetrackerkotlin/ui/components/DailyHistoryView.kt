package com.example.expensetrackerkotlin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.data.DailyData
import java.time.LocalDateTime

@Composable
fun DailyHistoryView(
    dailyData: List<DailyData>,
    selectedDate: LocalDateTime,
    onDateSelected: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(dailyData) { data ->
            DailyHistoryItem(
                data = data,
                isSelected = selectedDate.toLocalDate() == data.date.toLocalDate(),
                onClick = { onDateSelected(data.date) }
            )
        }
    }
}

@Composable
private fun DailyHistoryItem(
    data: DailyData,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .background(
                if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Transparent
            )
            .padding(8.dp)
    ) {
        // Day letter
        Text(
            text = data.dayName,
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Progress ring (small)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(40.dp)
        ) {
            // Background ring
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.2f),
                        CircleShape
                    )
            )
            
            // Progress ring
            if (data.progressPercentage > 0) {
                ProgressRing(
                    progress = data.progressPercentage.toFloat(),
                    colors = data.progressColors,
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 3.dp
                )
            }
            
            // Day number
            Text(
                text = data.dayNumber,
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Amount
        Text(
            text = "₺${String.format("%.0f", data.totalAmount)}",
            fontSize = 10.sp,
            color = if (data.isOverLimit) Color.Red else Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}