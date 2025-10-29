package com.example.expensetrackerkotlin.ui.tutorial

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.example.expensetrackerkotlin.R

enum class TutorialStepId {
    ADD_EXPENSE,
    RECURRING_EXPENSES,
    CALENDAR,
    DAILY_HISTORY,
    SETTINGS,
    SECRET_AREA,
    EXPENSE_LIST,
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
    fun getDefaultSteps(context: Context): List<TutorialStep> {
        return listOf(
            TutorialStep(
                id = TutorialStepId.ADD_EXPENSE,
                title = context.getString(R.string.tutorial_add_expense_title),
                message = context.getString(R.string.tutorial_add_expense_message),
                requiresTap = false,
                highlightRadius = 70f,
                tooltipPosition = TooltipPosition.TOP
            ),
            TutorialStep(
                id = TutorialStepId.RECURRING_EXPENSES,
                title = context.getString(R.string.tutorial_recurring_expenses_title),
                message = context.getString(R.string.tutorial_recurring_expenses_message),
                requiresTap = false,
                highlightRadius = 70f,
                tooltipPosition = TooltipPosition.TOP
            ),
            TutorialStep(
                id = TutorialStepId.CALENDAR,
                title = context.getString(R.string.tutorial_calendar_title),
                message = context.getString(R.string.tutorial_calendar_message),
                requiresTap = false,
                highlightRadius = 150f,
                tooltipPosition = TooltipPosition.TOP
            ),
            TutorialStep(
                id = TutorialStepId.DAILY_HISTORY,
                title = context.getString(R.string.tutorial_daily_history_title),
                message = context.getString(R.string.tutorial_daily_history_message),
                highlightShape = HighlightShape.ROUNDED_RECT,
                requiresTap = false,
                autoProgress = false,
                tooltipPosition = TooltipPosition.TOP
            ),
            TutorialStep(
                id = TutorialStepId.SETTINGS,
                title = context.getString(R.string.tutorial_settings_title),
                message = context.getString(R.string.tutorial_settings_message),
                requiresTap = false,
                highlightRadius = 55f,
                tooltipPosition = TooltipPosition.TOP
            ),
            TutorialStep(
                id = TutorialStepId.SECRET_AREA,
                title = context.getString(R.string.tutorial_secret_title),
                message = context.getString(R.string.tutorial_secret_message),
                highlightRadius = 55f,
                requiresTap = false,
                autoProgress = false,
                tooltipPosition = TooltipPosition.TOP
            ),
            TutorialStep(
                id = TutorialStepId.EXPENSE_LIST,
                title = context.getString(R.string.tutorial_expense_list_title),
                message = context.getString(R.string.tutorial_expense_list_message),
                highlightShape = HighlightShape.ROUNDED_RECT,
                requiresTap = false,
                autoProgress = false,
                tooltipPosition = TooltipPosition.TOP
            )
        )
    }
}
