package com.example.expensetrackerkotlin.ui.tutorial

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

enum class TutorialStepId {
    ADD_EXPENSE,
    EXPENSE_LIST,
    CALENDAR,
    SETTINGS,
    SWIPE_ANALYTICS,
    SWIPE_PLANNING,
    COMPLETED
}

enum class TooltipPosition {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT,
    AUTO
}

data class TutorialStep(
    val id: TutorialStepId,
    val title: String,
    val message: String,
    val targetBounds: Rect? = null, // Position of the highlighted element
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val highlightRadius: Float = 60f, // Radius for circular highlight
    val highlightShape: HighlightShape = HighlightShape.CIRCLE,
    val requiresTap: Boolean = true, // If true, user must tap the highlighted element
    val autoProgress: Boolean = false, // If true, automatically progress after delay
    val autoProgressDelay: Long = 3000L // Delay in milliseconds for auto progress
)

enum class HighlightShape {
    CIRCLE,
    ROUNDED_RECT
}

// Predefined tutorial steps with default messages
object TutorialSteps {
    fun getDefaultSteps(): List<TutorialStep> {
        return listOf(
            TutorialStep(
                id = TutorialStepId.ADD_EXPENSE,
                title = "Add Expense",
                message = "Tap here to add your first expense. Track all your spending in one place!",
                highlightRadius = 70f,
                tooltipPosition = TooltipPosition.TOP
            ),
            TutorialStep(
                id = TutorialStepId.EXPENSE_LIST,
                title = "Your Expenses",
                message = "All your expenses appear here. Swipe to see charts and analytics.",
                highlightShape = HighlightShape.ROUNDED_RECT,
                requiresTap = false,
                autoProgress = false,
                tooltipPosition = TooltipPosition.TOP
            ),
            TutorialStep(
                id = TutorialStepId.CALENDAR,
                title = "Calendar View",
                message = "Tap the center ring to view your monthly calendar and spending history.",
                highlightRadius = 80f,
                tooltipPosition = TooltipPosition.TOP
            ),
            TutorialStep(
                id = TutorialStepId.SETTINGS,
                title = "Settings",
                message = "Customize currency, spending limits, theme, and manage your data backup.",
                highlightRadius = 50f,
                tooltipPosition = TooltipPosition.TOP
            ),
            TutorialStep(
                id = TutorialStepId.SWIPE_ANALYTICS,
                title = "Analytics",
                message = "Swipe left on the main screen to see spending analytics and charts.",
                requiresTap = false,
                autoProgress = false,
                tooltipPosition = TooltipPosition.TOP
            ),
            TutorialStep(
                id = TutorialStepId.SWIPE_PLANNING,
                title = "Financial Planning",
                message = "Swipe left again to access financial planning and savings tools.",
                requiresTap = false,
                autoProgress = false,
                tooltipPosition = TooltipPosition.TOP
            )
        )
    }
}
