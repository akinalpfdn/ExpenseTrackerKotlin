package com.example.expensetrackerkotlin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    val plansWithBreakdowns by planningViewModel.plansWithBreakdowns.collectAsState()
    val isLoading by planningViewModel.isLoading.collectAsState()
    val error by planningViewModel.error.collectAsState()
    
    var showCreatePlanDialog by remember { mutableStateOf(false) }
    var selectedPlanForDetail by remember { mutableStateOf<String?>(null) }
    
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
                text = "Finansal Planlama",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeColors.getTextColor(isDarkTheme),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Gelecek planlarınızı oluşturun ve takip edin",
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
                        text = "Henüz plan yok",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeColors.getTextColor(isDarkTheme),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "İlk finansal planınızı oluşturmak için + butonuna basın",
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
                    items(plansWithBreakdowns) { planWithBreakdowns ->
                        PlanCard(
                            planWithBreakdowns = planWithBreakdowns,
                            onCardClick = {
                                planningViewModel.selectPlan(planWithBreakdowns.plan.id)
                                selectedPlanForDetail = planWithBreakdowns.plan.id
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
                    contentDescription = "Plan Ekle",
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
                        onCreatePlan = { name, startDate, duration, income, expenses, inflationApplied, inflationRate, recurring, average, avgMonths ->
                            planningViewModel.createPlan(
                                name = name,
                                startDate = startDate,
                                durationInMonths = duration,
                                monthlyIncome = income,
                                manualMonthlyExpenses = expenses,
                                isInflationApplied = inflationApplied,
                                inflationRate = inflationRate,
                                includeRecurringExpenses = recurring,
                                includeAverageExpenses = average,
                                averageMonthsToCalculate = avgMonths
                            )
                            showCreatePlanDialog = false
                        },
                        isDarkTheme = isDarkTheme,
                        defaultCurrency = defaultCurrency
                    )
                }
            }
        }
    }
}
