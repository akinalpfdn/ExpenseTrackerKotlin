package com.example.expensetrackerkotlin.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val gradient: List<Color>
)

@Composable
fun WelcomeScreen(
    onFinish: () -> Unit,
    isDarkTheme: Boolean = true
) {
    val pages = listOf(
        OnboardingPage(
            title = "Welcome to Expense Tracker",
            description = "Track your daily expenses easily and take control of your finances. Simple, fast, and completely private.",
            icon = Icons.Default.AccountBalance,
            gradient = listOf(Color(0xFF667eea), Color(0xFF764ba2))
        ),
        OnboardingPage(
            title = "Smart Analytics",
            description = "Visualize your spending with beautiful charts. See where your money goes with category breakdowns and monthly trends.",
            icon = Icons.Default.Analytics,
            gradient = listOf(Color(0xFFf093fb), Color(0xFFf5576c))
        ),
        OnboardingPage(
            title = "Your Privacy Matters",
            description = "All your data stays on your device. No cloud storage, no tracking, no ads. Your financial information is completely private.",
            icon = Icons.Default.Lock,
            gradient = listOf(Color(0xFF4facfe), Color(0xFF00f2fe))
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeColors.getBackgroundColor(isDarkTheme))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Skip button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                TextButton(
                    onClick = onFinish,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        "Skip",
                        color = ThemeColors.getTextGrayColor(isDarkTheme)
                    )
                }
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                OnboardingPageContent(
                    page = pages[page],
                    isDarkTheme = isDarkTheme
                )
            }

            // Page indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(
                                width = if (isSelected) 32.dp else 8.dp,
                                height = 8.dp
                            )
                            .clip(CircleShape)
                            .background(
                                if (isSelected)
                                    AppColors.PrimaryOrange
                                else
                                    ThemeColors.getTextGrayColor(isDarkTheme).copy(alpha = 0.3f)
                            )
                    )
                }
            }

            // Bottom button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onFinish()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.PrimaryOrange
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage < pages.size - 1) "Next" else "Get Started",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    isDarkTheme: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated icon with gradient background
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(
                    Brush.linearGradient(page.gradient),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Title
        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = ThemeColors.getTextColor(isDarkTheme),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = page.description,
            fontSize = 16.sp,
            color = ThemeColors.getTextGrayColor(isDarkTheme),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}
