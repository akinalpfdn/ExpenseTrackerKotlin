package com.example.expensetrackerkotlin.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import com.example.expensetrackerkotlin.R
import com.example.expensetrackerkotlin.ui.components.PlanCard
import com.example.expensetrackerkotlin.ui.components.CreatePlanBottomSheet
import com.example.expensetrackerkotlin.ui.components.PlanDetailBottomSheet
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import com.example.expensetrackerkotlin.viewmodel.PlanningViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningScreen(
    isDarkTheme: Boolean,
    planningViewModel: PlanningViewModel,
    defaultCurrency: String = "₺",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val plansWithBreakdowns by planningViewModel.plansWithBreakdowns.collectAsState()
    val isLoading by planningViewModel.isLoading.collectAsState()
    val error by planningViewModel.error.collectAsState()
    
    var showCreatePlanDialog by remember { mutableStateOf(false) }
    var selectedPlanForDetail by remember { mutableStateOf<String?>(null) }
    var planToDelete by remember { mutableStateOf<String?>(null) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ThemeColors.getBackgroundColor(isDarkTheme))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Header
            Text(
                text = stringResource(R.string.financial_planning),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeColors.getTextColor(isDarkTheme),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.planning_description),
                fontSize = 16.sp,
                color = ThemeColors.getTextGrayColor(isDarkTheme),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Content
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = AppColors.PrimaryOrange
                    )
                }
            } else if (plansWithBreakdowns.isEmpty()) {
                // Empty State
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 40.dp)
                ) {
                    Text(
                        text = "📊",
                        fontSize = 64.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = stringResource(R.string.no_plans_yet),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeColors.getTextColor(isDarkTheme),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = stringResource(R.string.create_first_plan),
                        fontSize = 16.sp,
                        color = ThemeColors.getTextGrayColor(isDarkTheme),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Plans List
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(plansWithBreakdowns) { index, planWithBreakdowns ->
                        AnimatedPlanCard(
                            index = index,
                            planWithBreakdowns = planWithBreakdowns,
                            onCardClick = {
                                planningViewModel.selectPlan(planWithBreakdowns.plan.id)
                                selectedPlanForDetail = planWithBreakdowns.plan.id
                            },
                            onDeleteClick = {
                                planToDelete = planWithBreakdowns.plan.id
                            },
                            isDarkTheme = isDarkTheme,
                            defaultCurrency = defaultCurrency
                        )
                    }
                }
            }
        }
        
        // Floating Action Button
        FloatingActionButton(
            onClick = { showCreatePlanDialog = true },
            containerColor = Color.Transparent,
            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),

            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(60.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(AppColors.ButtonGradientStart, AppColors.ButtonGradientEnd)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_plan),
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        
        // Error Snackbar
        error?.let { errorMessage ->
            LaunchedEffect(errorMessage) {
                // Show snackbar or handle error display
                planningViewModel.clearError()
            }
        }
        
        // Plan Detail Bottom Sheet
        selectedPlanForDetail?.let { planId ->
            val selectedPlan by planningViewModel.selectedPlan.collectAsState()
            
            selectedPlan?.let { planWithBreakdowns ->
                val planDetailSheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = true,
                    confirmValueChange = { true }
                )
                
                LaunchedEffect(Unit) {
                    planDetailSheetState.expand()
                }
                
                ModalBottomSheet(
                    onDismissRequest = { 
                        selectedPlanForDetail = null
                        planningViewModel.clearSelectedPlan()
                    },
                    containerColor = ThemeColors.getBackgroundColor(isDarkTheme),
                    sheetState = planDetailSheetState,
                    dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 700.dp)
                    ) {
                        PlanDetailBottomSheet(
                            planWithBreakdowns = planWithBreakdowns,
                            onUpdateBreakdown = { updatedBreakdown ->
                                planningViewModel.updatePlanBreakdown(updatedBreakdown)
                            },
                            onUpdateExpenseData = {
                                planningViewModel.updateExpenseData(planWithBreakdowns.plan.id)
                            },
                            isDarkTheme = isDarkTheme,
                            defaultCurrency = defaultCurrency
                        )
                    }
                }
            }
        }
        
        // Create Plan Bottom Sheet
        if (showCreatePlanDialog) {
            val createPlanSheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { true }
            )
            
            LaunchedEffect(Unit) {
                createPlanSheetState.expand()
            }
            
            ModalBottomSheet(
                onDismissRequest = { showCreatePlanDialog = false },
                containerColor = ThemeColors.getBackgroundColor(isDarkTheme),
                sheetState = createPlanSheetState,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 700.dp)
                ) {
                    CreatePlanBottomSheet(
                        onDismiss = { showCreatePlanDialog = false },
                        onCreatePlan = { name, duration, income, expenses, useAppData, inflationApplied, inflationRate, interestApplied, interestRate, interestType ->
                            planningViewModel.createPlan(
                                name = name,
                                startDate = java.time.LocalDateTime.now(),
                                durationInMonths = duration,
                                monthlyIncome = income,
                                manualMonthlyExpenses = expenses,
                                useAppExpenseData = useAppData,
                                isInflationApplied = inflationApplied,
                                inflationRate = inflationRate,
                                isInterestApplied = interestApplied,
                                interestRate = interestRate,
                                interestType = interestType,
                                defaultCurrency = defaultCurrency,
                                context = context
                            )
                            showCreatePlanDialog = false
                        },
                        isDarkTheme = isDarkTheme,
                        defaultCurrency = defaultCurrency
                    )
                }
            }
        }
        
        // Delete confirmation dialog
        planToDelete?.let { planId ->
            val planName = plansWithBreakdowns.find { it.plan.id == planId }?.plan?.name ?: "Plan"
            
            AlertDialog(
                onDismissRequest = {
                    planToDelete = null
                },
                title = {
                    Text(
                        text = stringResource(R.string.delete_plan),
                        color = ThemeColors.getTextColor(isDarkTheme),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.delete_plan_confirmation, planName),
                        color = ThemeColors.getTextColor(isDarkTheme)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            planningViewModel.deletePlan(planId)
                            planToDelete = null
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.delete),
                            color = ThemeColors.getDeleteRedColor(isDarkTheme),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            planToDelete = null
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            color = ThemeColors.getTextColor(isDarkTheme)
                        )
                    }
                },
                containerColor = ThemeColors.getDialogBackgroundColor(isDarkTheme)
            )
        }
    }
}

@Composable
private fun AnimatedPlanCard(
    index: Int,
    planWithBreakdowns: com.example.expensetrackerkotlin.data.PlanWithBreakdowns,
    onCardClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isDarkTheme: Boolean,
    defaultCurrency: String
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * 100L) // Stagger animation
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight+1200 },
            animationSpec = tween(
                durationMillis = 600,
                easing = FastOutSlowInEasing
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = 600,
                easing = FastOutSlowInEasing
            )
        )
    ) {
        PlanCard(
            planWithBreakdowns = planWithBreakdowns,
            onCardClick = onCardClick,
            onDeleteClick = onDeleteClick,
            isDarkTheme = isDarkTheme,
            defaultCurrency = defaultCurrency
        )
    }
}
