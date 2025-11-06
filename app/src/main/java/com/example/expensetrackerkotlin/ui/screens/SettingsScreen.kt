package com.example.expensetrackerkotlin.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.expensetrackerkotlin.ui.theme.AppColors
import com.example.expensetrackerkotlin.ui.theme.ThemeColors
import com.example.expensetrackerkotlin.ui.components.CategoryManagementScreen
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.res.stringResource
import com.example.expensetrackerkotlin.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: com.example.expensetrackerkotlin.viewmodel.ExpenseViewModel,
    defaultCurrency: String,
    dailyLimit: String,
    monthlyLimit: String,
    theme: String,
    onCurrencyChanged: (String) -> Unit,
    onDailyLimitChanged: (String) -> Unit,
    onMonthlyLimitChanged: (String) -> Unit,
    onThemeChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    dataManagementViewModel: com.example.expensetrackerkotlin.viewmodel.DataManagementViewModel? = null
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.general_settings),
        stringResource(R.string.categories)
    )
    
    val isDarkTheme = theme == "dark"
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeColors.getBackgroundColor(isDarkTheme))
    ) {

        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.padding(horizontal = 20.dp),
            containerColor = ThemeColors.getBackgroundColor(isDarkTheme),
            contentColor = AppColors.PrimaryOrange,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    height = 3.dp,
                    color = AppColors.PrimaryOrange
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 22.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) AppColors.PrimaryOrange else ThemeColors.getTextGrayColor(isDarkTheme)
                        )
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab Content
        when (selectedTabIndex) {
            0 -> GeneralSettingsTab(
                defaultCurrency = defaultCurrency,
                dailyLimit = dailyLimit,
                monthlyLimit = monthlyLimit,
                theme = theme,
                onCurrencyChanged = onCurrencyChanged,
                onDailyLimitChanged = onDailyLimitChanged,
                onMonthlyLimitChanged = onMonthlyLimitChanged,
                onThemeChanged = onThemeChanged,
                onDismiss = onDismiss,
                isDarkTheme = isDarkTheme,
                dataManagementViewModel = dataManagementViewModel
            )
            1 -> CategoryManagementScreen(
                viewModel = viewModel,
                isDarkTheme = isDarkTheme
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsTab(
    defaultCurrency: String,
    dailyLimit: String,
    monthlyLimit: String,
    theme: String,
    onCurrencyChanged: (String) -> Unit,
    onDailyLimitChanged: (String) -> Unit,
    onMonthlyLimitChanged: (String) -> Unit,
    onThemeChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean,
    dataManagementViewModel: com.example.expensetrackerkotlin.viewmodel.DataManagementViewModel? = null
) {
    var newDefaultCurrency by remember { mutableStateOf(defaultCurrency) }
    var newDailyLimit by remember { mutableStateOf(dailyLimit) }
    var newMonthlyLimit by remember { mutableStateOf(monthlyLimit) }
    var newTheme by remember { mutableStateOf(theme) }
    var showCurrencyMenu by remember { mutableStateOf(false) }
    
    val currencies = listOf("₺", "$", "€", "£")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Settings Form
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Currency Setting
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.default_currency),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
                
                ExposedDropdownMenuBox(
                    expanded = showCurrencyMenu,
                    onExpandedChange = { showCurrencyMenu = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(
                                ThemeColors.getInputBackgroundColor(isDarkTheme),
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = ThemeColors.getTextGrayColor(isDarkTheme),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = newDefaultCurrency,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ThemeColors.getTextColor(isDarkTheme),
                                modifier = Modifier.padding(start = 12.dp)
                            )
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = showCurrencyMenu,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                    
                    ExposedDropdownMenu(
                        expanded = showCurrencyMenu,
                        onDismissRequest = { showCurrencyMenu = false },
                        modifier = Modifier.background(ThemeColors.getInputBackgroundColor(isDarkTheme))
                    ) {
                        currencies.forEach { currency ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = currency,
                                        color = ThemeColors.getTextColor(isDarkTheme),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ) 
                                },
                                onClick = {
                                    newDefaultCurrency = currency
                                    showCurrencyMenu = false
                                },
                                modifier = Modifier.background(ThemeColors.getInputBackgroundColor(isDarkTheme))
                            )
                        }
                    }
                }
                
                Text(
                    text = stringResource(R.string.currency_description),
                    fontSize = 14.sp,
                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                )
            }
            
            // Daily Limit Setting
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.daily_spending_limit),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            ThemeColors.getInputBackgroundColor(isDarkTheme),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = ThemeColors.getTextGrayColor(isDarkTheme),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    BasicTextField(
                        value = newDailyLimit,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() || it == '.' }) {
                                newDailyLimit = newValue
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            color = ThemeColors.getTextColor(isDarkTheme)
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (newDailyLimit.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.enter_daily_limit),
                                        fontSize = 14.sp,
                                        color = ThemeColors.getTextGrayColor(isDarkTheme)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
                
                Text(
                    text = stringResource(R.string.daily_limit_description),
                    fontSize = 14.sp,
                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                )
            }
            
            // Monthly Limit Setting
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.monthly_spending_limit),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            ThemeColors.getInputBackgroundColor(isDarkTheme),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = ThemeColors.getTextGrayColor(isDarkTheme),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    BasicTextField(
                        value = newMonthlyLimit,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() || it == '.' }) {
                                newMonthlyLimit = newValue
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            color = ThemeColors.getTextColor(isDarkTheme)
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (newMonthlyLimit.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.enter_monthly_limit),
                                        fontSize = 14.sp,
                                        color = ThemeColors.getTextGrayColor(isDarkTheme)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
                
                Text(
                    text = stringResource(R.string.monthly_limit_description),
                    fontSize = 14.sp,
                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                )
            }
            
            // Theme Setting
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.theme),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (newTheme == "dark") stringResource(R.string.dark_theme) else stringResource(R.string.light_theme),
                        fontSize = 16.sp,
                        color = ThemeColors.getTextColor(isDarkTheme)
                    )
                    
                    Switch(
                        checked = newTheme == "light",
                        onCheckedChange = { isLight ->
                            newTheme = if (isLight) "light" else "dark"
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppColors.PrimaryOrange,
                            checkedTrackColor = AppColors.PrimaryOrange.copy(alpha = 0.5f),
                            uncheckedThumbColor = AppColors.TextGray,
                            uncheckedTrackColor = AppColors.TextGray.copy(alpha = 0.3f)
                        )
                    )
                }
                
                Text(
                    text = stringResource(R.string.theme_description),
                    fontSize = 14.sp,
                    color = ThemeColors.getTextGrayColor(isDarkTheme)
                )
            }

            // Data Management Section
            dataManagementViewModel?.let { dmViewModel ->
                Spacer(modifier = Modifier.height(8.dp))

                DataManagementSection(
                    viewModel = dmViewModel,
                    isDarkTheme = isDarkTheme
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        
        // Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ThemeColors.getButtonDisabledColor(isDarkTheme)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
            }
            Button(
                onClick = {
                    onCurrencyChanged(newDefaultCurrency)
                    onDailyLimitChanged(newDailyLimit)
                    onMonthlyLimitChanged(newMonthlyLimit)
                    onThemeChanged(newTheme)
                    onDismiss()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.PrimaryOrange
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.save),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ThemeColors.getTextColor(isDarkTheme)
                )
            }


        }
    }
}

@Composable
fun DataManagementSection(
    viewModel: com.example.expensetrackerkotlin.viewmodel.DataManagementViewModel,
    isDarkTheme: Boolean
) {
    val state by viewModel.state.collectAsState()

    var showImportDialog by remember { mutableStateOf(false) }
    var showImportSuccessDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var importSummary by remember { mutableStateOf<com.example.expensetrackerkotlin.data.ImportSummary?>(null) }
    var pendingImportUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val importFilePickerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        pendingImportUri = uri
        uri?.let {
            showImportDialog = true
        }
    }

    val saveToStorageLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            viewModel.saveToStorage(uri)
        } else {
            // User canceled - reset state to prevent re-launching
            viewModel.resetState()
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(state) {
        when (val currentState = state) {
            is com.example.expensetrackerkotlin.viewmodel.DataManagementState.ExportSuccess -> {
                context.startActivity(currentState.shareIntent)
                viewModel.cleanupExportFile()
                viewModel.resetState()
            }
            is com.example.expensetrackerkotlin.viewmodel.DataManagementState.ExportReadyForStorage -> {
                saveToStorageLauncher.launch(currentState.saveIntent.getStringExtra(android.content.Intent.EXTRA_TITLE) ?: "backup.json")
            }
            is com.example.expensetrackerkotlin.viewmodel.DataManagementState.StorageSaveSuccess -> {
                successMessage = currentState.message
                showSuccessDialog = true
                viewModel.resetState()
            }
            is com.example.expensetrackerkotlin.viewmodel.DataManagementState.ImportSuccess -> {
                importSummary = currentState.summary
                showImportSuccessDialog = true
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.backupRestore),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = com.example.expensetrackerkotlin.ui.theme.ThemeColors.getTextColor(isDarkTheme)
        )

        Text(
            text = stringResource(R.string.backupText),
            fontSize = 14.sp,
            color = com.example.expensetrackerkotlin.ui.theme.ThemeColors.getTextGrayColor(isDarkTheme)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Export Button
        Button(
            onClick = { showExportDialog = true },
            enabled = state !is com.example.expensetrackerkotlin.viewmodel.DataManagementState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = com.example.expensetrackerkotlin.ui.theme.AppColors.PrimaryOrange.copy(alpha = 0.8f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (state is com.example.expensetrackerkotlin.viewmodel.DataManagementState.Loading) {
                CircularProgressIndicator(
                    color = com.example.expensetrackerkotlin.ui.theme.ThemeColors.getTextColor(isDarkTheme),
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = stringResource(R.string.exportData),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = com.example.expensetrackerkotlin.ui.theme.ThemeColors.getTextColor(isDarkTheme)
                )
            }
        }

        // Import Button
        Button(
            onClick = { importFilePickerLauncher.launch("application/json") },
            enabled = state !is com.example.expensetrackerkotlin.viewmodel.DataManagementState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = com.example.expensetrackerkotlin.ui.theme.AppColors.PrimaryOrange.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = stringResource(R.string.importData),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = com.example.expensetrackerkotlin.ui.theme.ThemeColors.getTextColor(isDarkTheme)
            )
        }

        // Error Message
        if (state is com.example.expensetrackerkotlin.viewmodel.DataManagementState.Error) {
            val error = (state as com.example.expensetrackerkotlin.viewmodel.DataManagementState.Error).message
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = error,
                    color = androidx.compose.ui.graphics.Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }

    // Export Options Dialog
    if (showExportDialog) {
         AlertDialog(
            onDismissRequest = {
                showExportDialog = false
            },
            title = {
                Text(
                    text = stringResource(R.string.exportData) ,
                    color = ThemeColors.getTextColor(isDarkTheme),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(stringResource(R.string.chooseExport) ,
                        color = ThemeColors.getTextColor(isDarkTheme))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.shareText),
                        fontSize = 14.sp,
                        color = com.example.expensetrackerkotlin.ui.theme.ThemeColors.getTextGrayColor(isDarkTheme)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.saveText),
                        fontSize = 14.sp,
                        color = com.example.expensetrackerkotlin.ui.theme.ThemeColors.getTextGrayColor(isDarkTheme)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.exportAndShare()
                        showExportDialog = false
                    },
                    modifier = Modifier
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThemeColors.getButtonDisabledColor(isDarkTheme)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.share),
                        color = ThemeColors.getTextColor(isDarkTheme))
                }
            },
            dismissButton = {
                    Button(
                        onClick = {
                            viewModel.exportToStorage()
                            showExportDialog = false
                        },
                        modifier = Modifier
                            .height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.PrimaryOrange
                        ),
                        shape = RoundedCornerShape(16.dp)

                    ) {
                        Text(stringResource(R.string.saveToStorage))
                    }


            },
             containerColor = ThemeColors.getDialogBackgroundColor(isDarkTheme)
        )
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                successMessage = ""
            },
            title = {
                Text(
                    text = stringResource(R.string.success),
                    color = ThemeColors.getTextColor(isDarkTheme),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(successMessage,
                    color = ThemeColors.getTextColor(isDarkTheme))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        successMessage = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.example.expensetrackerkotlin.ui.theme.AppColors.PrimaryOrange.copy(alpha = 0.8f)
                    )
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            containerColor = ThemeColors.getDialogBackgroundColor(isDarkTheme)
        )
    }

    // Import Strategy Dialog
    if (showImportDialog) {
       AlertDialog(
            onDismissRequest = {
                showImportDialog = false
                pendingImportUri = null
            },
            title = {
                Text(
                    text = stringResource(R.string.importApprove),
                    color = ThemeColors.getTextColor(isDarkTheme),
                    fontWeight = FontWeight.Bold
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingImportUri?.let { uri ->
                            viewModel.importData(uri, com.example.expensetrackerkotlin.data.ImportStrategy.REPLACE_ALL)
                        }
                        showImportDialog = false
                        pendingImportUri = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.example.expensetrackerkotlin.ui.theme.AppColors.PrimaryOrange.copy(alpha = 0.8f)
                    )
                ) {
                    Text(stringResource(R.string.importButton))
                }
            },
            dismissButton = {

                    TextButton(onClick = {
                        showImportDialog = false
                        pendingImportUri = null
                    }) {
                        Text(stringResource(R.string.cancel),
                            color = ThemeColors.getTextColor(isDarkTheme))
                    }

            },
           containerColor = ThemeColors.getDialogBackgroundColor(isDarkTheme)
        )
    }

    // Import Success Dialog
    if (showImportSuccessDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                showImportSuccessDialog = false
                importSummary = null
            },
            title = {
                Text(
                    text = stringResource(R.string.importSucceed),
                    color = ThemeColors.getTextColor(isDarkTheme),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                importSummary?.let { summary ->
                    Column {
                        Text("Data imported successfully!",
                            color = ThemeColors.getTextColor(isDarkTheme))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Categories: ${summary.categoriesImported}", fontSize = 14.sp,
                            color = ThemeColors.getTextColor(isDarkTheme))
                        Text("SubCategories: ${summary.subCategoriesImported}", fontSize = 14.sp,
                            color = ThemeColors.getTextColor(isDarkTheme))
                        Text("Expenses: ${summary.expensesImported}", fontSize = 14.sp,
                            color = ThemeColors.getTextColor(isDarkTheme))
                        Text("Financial Plans: ${summary.financialPlansImported}", fontSize = 14.sp,
                            color = ThemeColors.getTextColor(isDarkTheme))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showImportSuccessDialog = false
                        importSummary = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.example.expensetrackerkotlin.ui.theme.AppColors.PrimaryOrange.copy(alpha = 0.8f)
                    )
                ) {
                    Text(stringResource(R.string.ok),
                        color = ThemeColors.getTextColor(isDarkTheme))
                }
            },
            containerColor = ThemeColors.getDialogBackgroundColor(isDarkTheme)
        )
    }
}

