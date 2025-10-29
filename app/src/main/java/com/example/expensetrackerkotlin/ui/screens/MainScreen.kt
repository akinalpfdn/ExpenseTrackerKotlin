package com.example.expensetrackerkotlin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import androidx.compose.ui.unit.dp
import com.example.expensetrackerkotlin.viewmodel.ExpenseViewModel
import com.example.expensetrackerkotlin.viewmodel.PlanningViewModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.platform.LocalContext
import com.example.expensetrackerkotlin.ui.tutorial.TutorialManager
import com.example.expensetrackerkotlin.ui.tutorial.TutorialOverlay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ExpenseViewModel,
    planningViewModel: PlanningViewModel
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 3 },initialPage = 0)
    val isDarkTheme = viewModel.theme == "dark"
    val isFirstLaunch by viewModel.isFirstLaunch.collectAsState()
    val isTutorialCompleted by viewModel.isTutorialCompleted.collectAsState()
    val scope = rememberCoroutineScope()

    // Tutorial manager
    val tutorialManager = remember { TutorialManager(viewModel.preferencesManager, context) }
    val tutorialState by tutorialManager.state.collectAsState()

    // Start tutorial after welcome screen if not completed
    LaunchedEffect(isFirstLaunch, isTutorialCompleted) {
       // if (isFirstLaunch == false && isTutorialCompleted == false) {
            tutorialManager.startTutorial()
        //}
    }

    // Show loading or welcome screen based on state
    when (isFirstLaunch) {
        null -> {
            // Loading state - show nothing or a splash
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ThemeColors.getBackgroundColor(false))
            )
            return
        }
        true -> {
            // First launch - show welcome screen
            WelcomeScreen(
                onFinish = {
                    scope.launch {
                        viewModel.completeFirstLaunch()
                    }
                },
                isDarkTheme = isDarkTheme
            )
            return
        }
        false -> {
            // Not first launch - show main app
        }
    }

    // Main app content (isFirstLaunch == false)
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
                    0 -> ExpensesScreen(
                        viewModel = viewModel,
                        tutorialManager = tutorialManager
                    )
                    1 -> AnalysisScreen(viewModel = viewModel, isDarkTheme = isDarkTheme)
                    2 -> PlanningScreen(
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

            // Tutorial overlay
            TutorialOverlay(
                tutorialState = tutorialState,
                onNext = { tutorialManager.nextStep() },
                onSkip = { tutorialManager.skipTutorial() },
                isDarkTheme = isDarkTheme
            )
        }
    }

