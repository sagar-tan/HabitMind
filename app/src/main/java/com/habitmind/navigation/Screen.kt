package com.habitmind.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

/**
 * Destinations for HabitMind app
 */
sealed class Screen(val route: String) {
    // Splash and Onboarding
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Welcome : Screen("welcome")
    data object PrivacyConfirm : Screen("privacy_confirm")
    data object ProfileSetup : Screen("profile_setup")
    data object Walkthrough : Screen("walkthrough")
    
    // Main tabs (Journal-First Architecture)
    data object Home : Screen("home")
    data object Journal : Screen("journal")
    data object Domains : Screen("domains")
    data object Analytics : Screen("analytics")
    data object Reviews : Screen("reviews")
    
    // Internal/Secondary screens (Legacy or repurposed)
    data object Plan : Screen("plan")
    data object Habits : Screen("habits")
    data object Insights : Screen("insights")
    
    // Detail screens
    data object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: Long) = "habit_detail/$habitId"
    }
    data object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: Long) = "task_detail/$taskId"
    }
    data object JournalEntry : Screen("journal_entry/{entryId}") {
        fun createRoute(entryId: Long?) = "journal_entry/${entryId ?: "new"}"
    }
    data object DailyJournal : Screen("daily_journal?date={date}") {
        fun createRoute(date: String? = null) = if (date != null) "daily_journal?date=$date" else "daily_journal"
    }
    data object GanttTimeline : Screen("gantt_timeline")
    data object WeeklyReview : Screen("weekly_review")
    data object Goals : Screen("goals")
    data object Settings : Screen("settings")
}

/**
 * Bottom navigation items - strictly follows the new ritual-first model
 */
enum class BottomNavItem(
    val screen: Screen,
    val label: String,
    val iconName: String
) {
    HOME(Screen.Home, "Home", "home"),
    JOURNAL(Screen.Journal, "Journal", "menu_book"),
    DOMAINS(Screen.Domains, "Domains", "grid_view"),
    ANALYTICS(Screen.Analytics, "Analytics", "insights"),
    REVIEWS(Screen.Reviews, "Reviews", "history")
}

@Composable
fun rememberHabitMindNavController(): NavHostController {
    return rememberNavController()
}
