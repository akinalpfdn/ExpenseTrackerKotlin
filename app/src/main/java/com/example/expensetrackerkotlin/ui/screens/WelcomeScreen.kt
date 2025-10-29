package com.example.expensetrackerkotlin.ui.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.R
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
    val context = LocalContext.current
    val pages = listOf(
        OnboardingPage(
            title = context.getString(R.string.welcome_page1_title),
            description = context.getString(R.string.welcome_page1_description),
            icon = Icons.Default.AccountBalance,
            gradient = listOf(Color(0xFF667eea), Color(0xFF764ba2))
        ),
        OnboardingPage(
            title = context.getString(R.string.welcome_page2_title),
            description = context.getString(R.string.welcome_page2_description),
            icon = Icons.Default.Analytics,
            gradient = listOf(Color(0xFFf093fb), Color(0xFFf5576c))
        ),
        OnboardingPage(
            title = context.getString(R.string.welcome_page3_title),
            description = context.getString(R.string.welcome_page3_description),
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
                        stringResource(R.string.welcome_skip),
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
                        text = if (pagerState.currentPage < pages.size - 1)
                            stringResource(R.string.welcome_next)
                        else
                            stringResource(R.string.welcome_get_started),
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
