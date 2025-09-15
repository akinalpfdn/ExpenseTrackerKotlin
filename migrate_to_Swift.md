# Expense Tracker - Complete Kotlin to Swift Migration Guide

## Migration Strategy
- Bottom-up approach - build foundation first
- Each layer only depends on layers below it
- Test each layer thoroughly before proceeding
- One file per step for trackable progress

---

## Phase 1: Localization Layer

### Step 1.1: Analyze string resources
**Target:** Understand current localization structure
**Dependencies:** None
**Action:** Review English and Turkish string resources

### Step 1.2: Create English localization
**Target:** Create `en.lproj/Localizable.strings`
**Dependencies:** Step 1.1
**Action:** Convert values/strings.xml (368 strings) to iOS format

### Step 1.3: Create Turkish localization
**Target:** Create `tr.lproj/Localizable.strings` 
**Dependencies:** Step 1.1
**Action:** Convert values-tr/strings.xml (366 strings) to iOS format

### Step 1.4: Create LocalizationManager.swift
**Target:** Create `LocalizationManager.swift`
**Dependencies:** Localizable.strings files
**Action:** Create utility for accessing localized strings with NSLocalizedString

**Phase 1 Test:** Verify all 368 strings load correctly in both languages

---

## Phase 2: Theme System

### Step 2.1: Migrate base colors
**Target:** Create `AppColors.swift`
**Dependencies:** None
**Action:** Convert Color.kt and AppColors.kt to Swift color definitions

### Step 2.2: Create theme structure
**Target:** Create `ThemeColors.swift`
**Dependencies:** AppColors
**Action:** Convert ThemeColors.kt - light/dark theme color management

### Step 2.3: Create typography system
**Target:** Create `AppTypography.swift`
**Dependencies:** None
**Action:** Convert Type.kt to SwiftUI typography system

### Step 2.4: Create theme manager
**Target:** Create `AppTheme.swift`
**Dependencies:** ThemeColors, AppTypography
**Action:** Global theme management and switching with @Published properties

**Phase 2 Test:** Verify theme switching works and all colors/fonts are accessible

---

## Phase 3: Foundation Data Layer

### Step 3.1: Migrate InterestType.kt
**Target:** Create `InterestType.swift`
**Dependencies:** None
**Action:** Convert enum with SIMPLE/COMPOUND values

### Step 3.2: Migrate Converters.kt
**Target:** Create `DateConverters.swift`
**Dependencies:** None
**Action:** Convert Room type converters to Swift date/enum conversion utilities

### Step 3.3: Migrate DailyData.kt
**Target:** Create `DailyData.swift`
**Dependencies:** None
**Action:** Convert display data model for daily expense summaries

### Step 3.4: Migrate Category.kt
**Target:** Create `Category.swift`
**Dependencies:** AppColors, LocalizationManager
**Action:** Convert category model with color/icon support + default categories

### Step 3.5: Migrate SubCategory.kt
**Target:** Create `SubCategory.swift`
**Dependencies:** Category, LocalizationManager
**Action:** Convert subcategory model with categoryId reference + default subcategories

### Step 3.6: Migrate Expense.kt
**Target:** Create `Expense.swift`
**Dependencies:** Category, SubCategory, RecurrenceType (from Expense.kt)
**Action:** Convert expense model with all fields and business logic methods

### Step 3.7: Migrate FinancialPlan.kt
**Target:** Create `FinancialPlan.swift`
**Dependencies:** InterestType
**Action:** Convert financial plan model with calculation methods

### Step 3.8: Migrate PlanMonthlyBreakdown.kt
**Target:** Create `PlanMonthlyBreakdown.swift`
**Dependencies:** FinancialPlan
**Action:** Convert monthly breakdown model with financial calculations

**Phase 3 Test:** Create sample data objects and verify all model logic works

---

## Phase 4: Data Persistence Layer

### Step 4.1: Setup Core Data stack
**Target:** Create `CoreDataStack.swift` + `ExpenseTracker.xcdatamodeld`
**Dependencies:** All Phase 3 models
**Action:** Replace Room database with Core Data, create entities for all models

### Step 4.2: Migrate CategoryDao.kt
**Target:** Create `CategoryDataAccess.swift`
**Dependencies:** CoreDataStack, Category, SubCategory
**Action:** Convert category data access methods using Core Data

### Step 4.3: Migrate ExpenseDao.kt
**Target:** Create `ExpenseDataAccess.swift`
**Dependencies:** CoreDataStack, Expense
**Action:** Convert expense data access methods using Core Data

### Step 4.4: Migrate PlanDao.kt
**Target:** Create `PlanDataAccess.swift`
**Dependencies:** CoreDataStack, FinancialPlan, PlanMonthlyBreakdown
**Action:** Convert plan data access methods using Core Data

**Phase 4 Test:** Save, load, update, delete data - verify Core Data operations

---

## Phase 5: Business Logic Layer

### Step 5.1: Migrate PreferencesManager.kt
**Target:** Create `SettingsManager.swift`
**Dependencies:** None (uses UserDefaults)
**Action:** Convert preferences using UserDefaults and @Published properties

### Step 5.2: Migrate ExpenseRepository.kt
**Target:** Create `ExpenseRepository.swift`
**Dependencies:** ExpenseDataAccess, SettingsManager
**Action:** Convert repository with recurring expense generation logic

### Step 5.3: Migrate CategoryRepository.kt
**Target:** Create `CategoryRepository.swift`
**Dependencies:** CategoryDataAccess
**Action:** Convert category repository with initialization logic

### Step 5.4: Migrate PlanRepository.kt
**Target:** Create `PlanRepository.swift`
**Dependencies:** PlanDataAccess, ExpenseRepository
**Action:** Convert plan repository with complex business logic

### Step 5.5: Migrate PlanningUtils.kt
**Target:** Create `PlanningUtils.swift`
**Dependencies:** FinancialPlan models, LocalizationManager
**Action:** Convert utility functions for financial calculations

**Phase 5 Test:** Verify all business logic works correctly through repositories

---

## Phase 6: ViewModel Layer

### Step 6.1: Migrate ExpenseViewModel.kt
**Target:** Create `ExpenseViewModel.swift`
**Dependencies:** All repositories, SettingsManager
**Action:** Convert ExpenseViewModel.kt to ObservableObject with @Published

### Step 6.2: Migrate PlanningViewModel.kt
**Target:** Create `PlanningViewModel.swift`
**Dependencies:** PlanRepository, PlanningUtils
**Action:** Convert PlanningViewModel.kt to ObservableObject

**Phase 6 Test:** Verify ViewModels manage state correctly and UI can bind to them

---

## Phase 7: UI Components Layer

### Step 7.1: Migrate ProgressRingComponent.kt
**Target:** Create `ProgressRingView.swift`
**Dependencies:** AppTheme, AppColors
**Action:** Convert circular progress indicator component

### Step 7.2: Migrate ExpenseRowView.kt
**Target:** Create `ExpenseRowView.swift`
**Dependencies:** Expense model, AppTheme, LocalizationManager
**Action:** Convert individual expense list item component

### Step 7.3: Migrate DailyHistoryView.kt
**Target:** Create `DailyHistoryView.swift`
**Dependencies:** DailyData, AppTheme
**Action:** Convert weekly calendar/history view component

### Step 7.4: Migrate CategoryDistributionChart.kt
**Target:** Create `CategoryDistributionChart.swift`
**Dependencies:** Category model, AppTheme
**Action:** Convert pie chart component for category distribution

### Step 7.5: Migrate MonthlyCalendarView.kt
**Target:** Create `MonthlyCalendarView.swift`
**Dependencies:** Expense model, AppTheme, LocalizationManager
**Action:** Convert monthly calendar component with expense indicators

### Step 7.6: Migrate MonthlyAnalysisPieChart.kt
**Target:** Create `MonthlyAnalysisPieChart.swift`
**Dependencies:** Category model, AppTheme, LocalizationManager
**Action:** Convert monthly analysis pie chart component

### Step 7.7: Migrate MonthlyLineChart.kt
**Target:** Create `MonthlyLineChart.swift`
**Dependencies:** Expense model, AppTheme, LocalizationManager
**Action:** Convert line chart component for monthly trends

### Step 7.8: Migrate MonthlyExpensesView.kt
**Target:** Create `MonthlyExpensesView.swift`
**Dependencies:** Expense model, ExpenseRowView, AppTheme
**Action:** Convert monthly expenses list component

### Step 7.9: Migrate DateRangePicker.kt
**Target:** Create `DateRangePicker.swift`
**Dependencies:** AppTheme, LocalizationManager
**Action:** Convert date range picker component

### Step 7.10: Migrate CategoryPopupLines.kt
**Target:** Create `CategoryPopupLines.swift`
**Dependencies:** Category model, AppTheme, LocalizationManager
**Action:** Convert category comparison popup component

### Step 7.11: Migrate CategorySummarySection.kt
**Target:** Create `CategorySummarySection.swift`
**Dependencies:** Category model, AppTheme, LocalizationManager
**Action:** Convert category summary display component

### Step 7.12: Migrate PlanCard.kt
**Target:** Create `PlanCard.swift`
**Dependencies:** FinancialPlan model, AppTheme, LocalizationManager
**Action:** Convert financial plan card component

### Step 7.13: Migrate CategoryDetailBottomSheet.kt
**Target:** Create `CategoryDetailBottomSheet.swift`
**Dependencies:** Category, Expense models, ExpenseRowView, AppTheme
**Action:** Convert category detail modal component

### Step 7.14: Migrate SubCategoryDetailBottomSheet.kt
**Target:** Create `SubCategoryDetailBottomSheet.swift`
**Dependencies:** SubCategory, Expense models, ExpenseRowView, AppTheme
**Action:** Convert subcategory detail modal component

### Step 7.15: Migrate DailyCategoryDetailBottomSheet.kt
**Target:** Create `DailyCategoryDetailBottomSheet.swift`
**Dependencies:** Category, Expense models, ExpenseRowView, AppTheme
**Action:** Convert daily category detail modal component

### Step 7.16: Migrate PlanDetailBottomSheet.kt
**Target:** Create `PlanDetailBottomSheet.swift`
**Dependencies:** FinancialPlan, PlanMonthlyBreakdown models, AppTheme
**Action:** Convert plan detail modal component

### Step 7.17: Migrate CreatePlanDialog.kt
**Target:** Create `CreatePlanDialog.swift`
**Dependencies:** FinancialPlan model, AppTheme, LocalizationManager
**Action:** Convert plan creation form component

### Step 7.18: Migrate CategoryManagementScreen.kt
**Target:** Create `CategoryManagementView.swift`
**Dependencies:** Category, SubCategory models, AppTheme, LocalizationManager
**Action:** Convert category management interface component

**Phase 7 Test:** Verify all 18 components render correctly and handle interactions

---

## Phase 8: UI Screens Layer

### Step 8.1: Migrate MainActivity.kt
**Target:** Create `ExpenseTrackerApp.swift`
**Dependencies:** All ViewModels, AppTheme
**Action:** Convert MainActivity.kt - main app entry point with dependency setup

### Step 8.2: Migrate MainScreen.kt
**Target:** Create `MainContentView.swift`
**Dependencies:** ExpenseViewModel, AppTheme, UI components
**Action:** Convert MainScreen.kt - tab-based navigation with 3 screens

### Step 8.3: Migrate ExpensesScreen.kt
**Target:** Create `ExpensesView.swift`
**Dependencies:** ExpenseViewModel, UI components (DailyHistoryView, ExpenseRowView, etc.)
**Action:** Convert ExpensesScreen.kt - main expenses list and management

### Step 8.4: Migrate AddExpenseScreen.kt
**Target:** Create `AddExpenseView.swift`
**Dependencies:** ExpenseViewModel, form components, AppTheme
**Action:** Convert AddExpenseScreen.kt - expense creation/editing form

### Step 8.5: Migrate AnalysisScreen.kt
**Target:** Create `AnalysisView.swift`
**Dependencies:** ExpenseViewModel, chart components (MonthlyAnalysisPieChart, etc.)
**Action:** Convert AnalysisScreen.kt - expense analysis and charts

### Step 8.6: Migrate PlanningScreen.kt
**Target:** Create `PlanningView.swift`
**Dependencies:** PlanningViewModel, planning components (PlanCard, CreatePlanDialog)
**Action:** Convert PlanningScreen.kt - financial planning interface

### Step 8.7: Migrate SettingsScreen.kt
**Target:** Create `SettingsView.swift`
**Dependencies:** ExpenseViewModel, form components, CategoryManagementView
**Action:** Convert SettingsScreen.kt - app settings and category management

### Step 8.8: Migrate RecurringExpensesScreen.kt
**Target:** Create `RecurringExpensesView.swift`
**Dependencies:** ExpenseViewModel, ExpenseRowView, AppTheme
**Action:** Convert RecurringExpensesScreen.kt - recurring expenses management

**Phase 8 Test:** Verify all 8 screens work correctly and navigation flows properly

---

## Complete File Migration List

### **Data Layer (16 files):**
1. InterestType.kt → InterestType.swift
2. Converters.kt → DateConverters.swift
3. DailyData.kt → DailyData.swift
4. Category.kt → Category.swift
5. SubCategory.kt → SubCategory.swift
6. Expense.kt → Expense.swift
7. FinancialPlan.kt → FinancialPlan.swift
8. PlanMonthlyBreakdown.kt → PlanMonthlyBreakdown.swift
9. CategoryDao.kt → CategoryDataAccess.swift
10. ExpenseDao.kt → ExpenseDataAccess.swift
11. PlanDao.kt → PlanDataAccess.swift
12. ExpenseDatabase.kt → CoreDataStack.swift
13. PreferencesManager.kt → SettingsManager.swift
14. ExpenseRepository.kt → ExpenseRepository.swift
15. CategoryRepository.kt → CategoryRepository.swift
16. PlanRepository.kt → PlanRepository.swift

### **Business Logic (3 files):**
17. PlanningUtils.kt → PlanningUtils.swift
18. ExpenseViewModel.kt → ExpenseViewModel.swift
19. PlanningViewModel.kt → PlanningViewModel.swift

### **UI Components (18 files):**
20. ProgressRingComponent.kt → ProgressRingView.swift
21. ExpenseRowView.kt → ExpenseRowView.swift
22. DailyHistoryView.kt → DailyHistoryView.swift
23. CategoryDistributionChart.kt → CategoryDistributionChart.swift
24. MonthlyCalendarView.kt → MonthlyCalendarView.swift
25. MonthlyAnalysisPieChart.kt → MonthlyAnalysisPieChart.swift
26. MonthlyLineChart.kt → MonthlyLineChart.swift
27. MonthlyExpensesView.kt → MonthlyExpensesView.swift
28. DateRangePicker.kt → DateRangePicker.swift
29. CategoryPopupLines.kt → CategoryPopupLines.swift
30. CategorySummarySection.kt → CategorySummarySection.swift
31. PlanCard.kt → PlanCard.swift
32. CategoryDetailBottomSheet.kt → CategoryDetailBottomSheet.swift
33. SubCategoryDetailBottomSheet.kt → SubCategoryDetailBottomSheet.swift
34. DailyCategoryDetailBottomSheet.kt → DailyCategoryDetailBottomSheet.swift
35. PlanDetailBottomSheet.kt → PlanDetailBottomSheet.swift
36. CreatePlanDialog.kt → CreatePlanDialog.swift
37. CategoryManagementScreen.kt → CategoryManagementView.swift

### **UI Screens (8 files):**
38. MainActivity.kt → ExpenseTrackerApp.swift
39. MainScreen.kt → MainContentView.swift
40. ExpensesScreen.kt → ExpensesView.swift
41. AddExpenseScreen.kt → AddExpenseView.swift
42. AnalysisScreen.kt → AnalysisView.swift
43. PlanningScreen.kt → PlanningView.swift
44. SettingsScreen.kt → SettingsView.swift
45. RecurringExpensesScreen.kt → RecurringExpensesView.swift

### **Theme & Utilities (7 files):**
46. Color.kt → AppColors.swift
47. AppColors.kt → (merged into AppColors.swift)
48. ThemeColors.kt → ThemeColors.swift
49. Theme.kt → AppTheme.swift
50. Type.kt → AppTypography.swift
51. NumberFormatter.kt → NumberFormatter.swift
52. LocalizationManager.swift (new)

### **Localization (2 files):**
53. values/strings.xml → en.lproj/Localizable.strings
54. values-tr/strings.xml → tr.lproj/Localizable.strings

**Total: 54 files to migrate**

---

## Testing Strategy

### After Each Phase:
1. **Get Feedback** - Confirm phase completion before proceeding

### Between Phases:
1. **Document** - Note any changes or issues encountered

### Final Integration:
1. **End-to-End Testing** - Complete user workflows
2. **Performance Testing** - Ensure good performance
3. **iOS Platform Integration** - Platform-specific features

---


## Notes

- Document any deviations from the original Kotlin code
- Get feedback after each phase before proceeding to next layer