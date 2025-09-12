package com.example.expensetrackerkotlin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetrackerkotlin.viewmodel.ExpenseViewModel
import com.example.expensetrackerkotlin.viewmodel.PlanningViewModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ExpenseViewModel,
    planningViewModel: PlanningViewModel
) {
    val pagerState = rememberPagerState(pageCount = { 3 },initialPage = 0)
    val isDarkTheme = viewModel.theme == "dark"
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeColors.getBackgroundColor(isDarkTheme))
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Main content with HorizontalPager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> ExpensesScreen(viewModel = viewModel)
                1 -> AnalysisScreen(viewModel = viewModel,isDarkTheme = isDarkTheme)
                2-> PlanningScreen(
                    isDarkTheme = isDarkTheme,
                    planningViewModel = planningViewModel,
                    defaultCurrency = viewModel.defaultCurrency
                )
            }
        }
        
        // Page indicator at the bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(
                            width = if (pagerState.currentPage == index) 24.dp else 8.dp,
                            height = 8.dp
                        )
                        .background(
                            color = if (pagerState.currentPage == index) {
                                AppColors.PrimaryOrange
                            } else {
                                ThemeColors.getTextGrayColor(isDarkTheme).copy(alpha = 0.5f)
                            },
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}
