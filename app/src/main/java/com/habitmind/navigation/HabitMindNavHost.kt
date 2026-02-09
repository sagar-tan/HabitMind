package com.habitmind.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.habitmind.HabitMindApplication
import com.habitmind.data.database.entity.Habit
import com.habitmind.data.database.entity.JournalEntry
import com.habitmind.data.database.entity.JournalEntryType
import com.habitmind.data.database.entity.Task
import com.habitmind.data.database.entity.DailyLog
import com.habitmind.ui.components.FloatingGlassFAB
import com.habitmind.ui.components.GlassBottomNavBar
import com.habitmind.ui.components.glassNavItems
import com.habitmind.ui.screens.dialogs.AddHabitDialog
import com.habitmind.ui.screens.dialogs.AddJournalDialog
import com.habitmind.ui.screens.dialogs.AddTaskDialog
import com.habitmind.ui.screens.dialogs.QuickNoteDialog
import com.habitmind.ui.screens.home.HomeScreen
import com.habitmind.ui.screens.habits.HabitsScreen
import com.habitmind.ui.screens.insights.InsightsScreen
import com.habitmind.ui.screens.journal.JournalScreen
import com.habitmind.ui.screens.plan.PlanScreen
import com.habitmind.ui.screens.onboarding.OnboardingScreen
import com.habitmind.ui.screens.onboarding.SplashScreen
import com.habitmind.ui.screens.settings.SettingsScreen
import com.habitmind.ui.screens.sheets.AddItemSheet
import com.habitmind.ui.theme.DarkBackground
import com.habitmind.ui.theme.Motion
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

// Dialog type enum
enum class ActiveDialog {
    NONE, ADD_SHEET, ADD_HABIT, ADD_TASK, ADD_JOURNAL, QUICK_NOTE
}

@Composable
fun HabitMindNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as HabitMindApplication
    val preferences = app.userPreferences
    val habitRepository = app.habitRepository
    val taskRepository = app.taskRepository
    val journalRepository = app.journalRepository
    val scope = rememberCoroutineScope()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    
    // Track if we should show main UI elements
    val showBottomBar = currentRoute in glassNavItems.map { it.route }
    val showFAB = currentRoute in listOf("plan", "habits", "journal")
    
    // State for dialogs
    var activeDialog by remember { mutableStateOf(ActiveDialog.NONE) }
    
    // User preferences from DataStore
    val userName by preferences.userName.collectAsState(initial = null)
    val hasCompletedOnboarding by preferences.hasCompletedOnboarding.collectAsState(initial = false)
    
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = DarkBackground,
            bottomBar = { } // We handle this manually for floating effect
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                enterTransition = {
                    fadeIn(animationSpec = tween(Motion.SCREEN, easing = FastOutSlowInEasing)) +
                    scaleIn(
                        initialScale = 0.96f,
                        animationSpec = tween(Motion.SCREEN, easing = FastOutSlowInEasing)
                    )
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(Motion.SCREEN_FAST, easing = FastOutSlowInEasing)) +
                    scaleOut(
                        targetScale = 0.96f,
                        animationSpec = tween(Motion.SCREEN_FAST, easing = FastOutSlowInEasing)
                    )
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(Motion.SCREEN, easing = FastOutSlowInEasing)) +
                    scaleIn(
                        initialScale = 0.96f,
                        animationSpec = tween(Motion.SCREEN, easing = FastOutSlowInEasing)
                    )
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(Motion.SCREEN_FAST, easing = FastOutSlowInEasing)) +
                    scaleOut(
                        targetScale = 0.96f,
                        animationSpec = tween(Motion.SCREEN_FAST, easing = FastOutSlowInEasing)
                    )
                }
            ) {
                // Splash screen
                composable(Screen.Splash.route) {
                    SplashScreen(
                        userName = userName,
                        onComplete = {
                            if (hasCompletedOnboarding && userName != null) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Screen.Onboarding.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        }
                    )
                }
                
                // Onboarding
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onComplete = { name ->
                            scope.launch {
                                preferences.saveUserName(name)
                                preferences.setOnboardingCompleted(true)
                            }
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }
                
                // Legacy welcome (redirects to onboarding)
                composable(Screen.Welcome.route) {
                    OnboardingScreen(
                        onComplete = { name ->
                            scope.launch {
                                preferences.saveUserName(name)
                                preferences.setOnboardingCompleted(true)
                            }
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        }
                    )
                }
                
                // Main tabs
                composable(Screen.Home.route) {
                    HomeScreen(
                        userName = userName,
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateToJournal = { navController.navigate(Screen.Journal.route) },
                        onNavigateToPlan = { navController.navigate(Screen.Plan.route) },
                        onShowQuickNote = { activeDialog = ActiveDialog.QUICK_NOTE }
                    )
                }
                
                composable(Screen.Plan.route) {
                    PlanScreen(
                        onAddTask = { activeDialog = ActiveDialog.ADD_SHEET }
                    )
                }
                
                composable(Screen.Habits.route) {
                    HabitsScreen(
                        onAddHabit = { activeDialog = ActiveDialog.ADD_SHEET }
                    )
                }
                
                composable(Screen.Journal.route) {
                    JournalScreen(
                        onAddEntry = { activeDialog = ActiveDialog.ADD_SHEET }
                    )
                }
                
                composable(Screen.Insights.route) {
                    InsightsScreen()
                }
                
                // Settings
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
        
        // Floating glass navbar - positioned at bottom
        if (showBottomBar) {
            GlassBottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
        
        // Floating FAB - positioned above navbar, right side
        if (showFAB) {
            FloatingGlassFAB(
                onClick = { activeDialog = ActiveDialog.ADD_SHEET },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 100.dp)
                    .navigationBarsPadding()
            )
        }
        
        // Dialog handling
        when (activeDialog) {
            ActiveDialog.ADD_SHEET -> {
                AddItemSheet(
                    currentScreen = currentRoute ?: "home",
                    onDismiss = { activeDialog = ActiveDialog.NONE },
                    onAddTask = { activeDialog = ActiveDialog.ADD_TASK },
                    onAddHabit = { activeDialog = ActiveDialog.ADD_HABIT },
                    onAddJournalEntry = { activeDialog = ActiveDialog.ADD_JOURNAL }
                )
            }
            
            ActiveDialog.ADD_HABIT -> {
                AddHabitDialog(
                    onDismiss = { activeDialog = ActiveDialog.NONE },
                    onConfirm = { name, color, reminderTime ->
                        scope.launch {
                            val habit = Habit(
                                name = name,
                                color = color,
                                reminderTime = reminderTime
                            )
                            habitRepository.insertHabit(habit)
                        }
                        activeDialog = ActiveDialog.NONE
                    }
                )
            }
            
            ActiveDialog.ADD_TASK -> {
                AddTaskDialog(
                    onDismiss = { activeDialog = ActiveDialog.NONE },
                    onConfirm = { title, description, estimatedMinutes, dueDate ->
                        scope.launch {
                            val task = Task(
                                title = title,
                                description = description ?: "",
                                date = dueDate,
                                estimatedMinutes = estimatedMinutes
                            )
                            taskRepository.insertTask(task)
                        }
                        activeDialog = ActiveDialog.NONE
                    }
                )
            }
            
            ActiveDialog.ADD_JOURNAL -> {
                AddJournalDialog(
                    onDismiss = { activeDialog = ActiveDialog.NONE },
                    onConfirm = { content, type, tags, mood ->
                        scope.launch {
                            val entry = JournalEntry(
                                type = type,
                                content = content,
                                tags = tags ?: "",
                                mood = mood?.toIntOrNull()
                            )
                            journalRepository.insertEntry(entry)
                        }
                        activeDialog = ActiveDialog.NONE
                    }
                )
            }
            
            ActiveDialog.QUICK_NOTE -> {
                QuickNoteDialog(
                    onDismiss = { activeDialog = ActiveDialog.NONE },
                    onConfirm = { content ->
                        scope.launch {
                            journalRepository.addQuickNote(content)
                        }
                        activeDialog = ActiveDialog.NONE
                    }
                )
            }
            
            ActiveDialog.NONE -> { /* No dialog shown */ }
        }
    }
}
