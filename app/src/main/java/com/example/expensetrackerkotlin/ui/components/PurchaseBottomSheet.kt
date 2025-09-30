package com.example.expensetrackerkotlin.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.R
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import kotlinx.coroutines.delay

data class PurchaseOption(
    val title: String,
    val price: String,
    val icon: ImageVector,
    val productId: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseBottomSheet(
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    onPurchase: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { true }
    )

    LaunchedEffect(Unit) {
        sheetState.expand()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ThemeColors.getBackgroundColor(isDarkTheme),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            // Typewriter Animation Text
            TypewriterText(
                text = "Senin saçma reklamlar ile boğmak istemiyorum ve tüm özelliklere premium versiyon olmadan ulaşabilmeni istiyorum." +
                " Ama geliştirme yapabilmem için gelire ihtiyacım var." +
                " Uygulamamı beğendiysen ve destek olmak istersen bir çayını içerim(Fiyatlara enflasyon etki etmiyor burada) :).",
                isDarkTheme = isDarkTheme
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Purchase Options
            PurchaseOptions(
                isDarkTheme = isDarkTheme,
                onPurchase = onPurchase
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TypewriterText(
    text: String,
    isDarkTheme: Boolean
) {
    var displayText by remember { mutableStateOf("") }
    var isAnimationComplete by remember { mutableStateOf(false) }

    LaunchedEffect(text) {
        displayText = ""
        isAnimationComplete = false

        text.forEachIndexed { index, _ ->
            delay(80) // Typewriter speed
            displayText = text.substring(0, index + 1)
        }

        isAnimationComplete = true
    }

    Text(
        text = displayText + if (!isAnimationComplete && displayText.isNotEmpty()) "|" else "",
        fontSize = 14.sp,
        color = ThemeColors.getTextColor(isDarkTheme),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PurchaseOptions(
    isDarkTheme: Boolean,
    onPurchase: (String) -> Unit
) {

    val doner: ImageVector = ImageVector.vectorResource(id = R.drawable.doner)
    val bagel: ImageVector = ImageVector.vectorResource(id = R.drawable.bagel)
    val burrito: ImageVector = ImageVector.vectorResource(id = R.drawable.burrito)
    val purchaseOptions = listOf(
        PurchaseOption(
            title = "Su ısmarla",
            price = "5 TL",
            icon = Icons.Default.LocalDrink,
            productId = "su_donation"
        ),
        PurchaseOption(
            title = "Çay ısmarla",
            price = "10 TL",
            icon = Icons.Default.LocalCafe,
            productId = "tea_donation"
        ),
        PurchaseOption(
            title = "Simit ısmarla",
            price = "25 TL",
            icon = bagel,
            productId = "bagel_donation"
        ),
        PurchaseOption(
            title = "Kahve ısmarla",
            price = "50 TL",
            icon = Icons.Default.LocalCafe,
            productId = "coffee_donation"
        ),
        PurchaseOption(
            title = "Hatay Dürüm ısmarla",
            price = "100 TL",
            icon = burrito,
            productId = "wrap_donation"
        ),
        PurchaseOption(
            title = "Burger ısmarla",
            price = "250 TL",
            icon = Icons.Default.LunchDining,
            productId = "burger_donation"
        ),
        PurchaseOption(
            title = "İskender ısmarla",
            price = "400 TL",
            icon = doner,
            productId = "doner_donation"
        ),
        PurchaseOption(
            title = "Şımart beni",
            price = "500 TL",
            icon = Icons.Default.Star,
            productId = "max_donation"
        )
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // First row with 2 cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PurchaseOptionCard(
                option = purchaseOptions[0],
                isDarkTheme = isDarkTheme,
                onClick = { onPurchase(purchaseOptions[0].productId) },
                modifier = Modifier.weight(1f)
            )
            PurchaseOptionCard(
                option = purchaseOptions[1],
                isDarkTheme = isDarkTheme,
                onClick = { onPurchase(purchaseOptions[1].productId) },
                modifier = Modifier.weight(1f)
            )
        }

        // Second row with 1 card centered
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            PurchaseOptionCard(
                option = purchaseOptions[2],
                isDarkTheme = isDarkTheme,
                onClick = { onPurchase(purchaseOptions[2].productId) },
                modifier = Modifier.fillMaxWidth(0.5f)
            )
            PurchaseOptionCard(
                option = purchaseOptions[3],
                isDarkTheme = isDarkTheme,
                onClick = { onPurchase(purchaseOptions[3].productId) },
                modifier = Modifier.weight(1f)
            )
        }
        // Third row with 1 card centered
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            PurchaseOptionCard(
                option = purchaseOptions[4],
                isDarkTheme = isDarkTheme,
                onClick = { onPurchase(purchaseOptions[4].productId) },
                modifier = Modifier.fillMaxWidth(0.5f)
            )
            PurchaseOptionCard(
                option = purchaseOptions[5],
                isDarkTheme = isDarkTheme,
                onClick = { onPurchase(purchaseOptions[5].productId) },
                modifier = Modifier.weight(1f)
            )
        }
        // Fifth row with 1 card centered
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            PurchaseOptionCard(
                option = purchaseOptions[6],
                isDarkTheme = isDarkTheme,
                onClick = { onPurchase(purchaseOptions[6].productId) },
                modifier = Modifier.fillMaxWidth(0.5f)
            )
            PurchaseOptionCard(
                option = purchaseOptions[7],
                isDarkTheme = isDarkTheme,
                onClick = { onPurchase(purchaseOptions[7].productId) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PurchaseOptionCard(
    option: PurchaseOption,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )

    Card(
        modifier = modifier
            .clickable {
                isPressed = true
                onClick()
            }
            .then(
                if (isPressed) {
                    Modifier.border(
                        2.dp,
                        AppColors.PrimaryOrange,
                        RoundedCornerShape(16.dp)
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 8.dp else 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        AppColors.PrimaryOrange.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = option.title,
                    modifier = Modifier.size(20.dp),
                    tint = AppColors.PrimaryOrange
                )
            }

            Text(
                text = option.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = ThemeColors.getTextColor(isDarkTheme),
                textAlign = TextAlign.Center
            )

            Text(
                text = option.price,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryOrange
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}

