package com.habitmind.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.content.ContextCompat
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
import com.habitmind.ui.screens.onboarding.PrivacyConfirmScreen
import com.habitmind.ui.screens.onboarding.ProfileSetupScreen
import com.habitmind.ui.screens.onboarding.WalkthroughScreen
import com.habitmind.ui.screens.onboarding.SplashScreen
import com.habitmind.ui.screens.habits.HabitDetailScreen
import com.habitmind.ui.screens.review.WeeklyReviewScreen
import com.habitmind.ui.screens.goals.GoalsScreen
import com.habitmind.ui.screens.gantt.GanttTimelineScreen
import com.habitmind.ui.screens.settings.SettingsScreen
import com.habitmind.ui.theme.DarkBackground
import com.habitmind.ui.theme.Motion
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime

// Dialog type enum
enum class ActiveDialog {
    NONE, ADD_HABIT, ADD_TASK, ADD_JOURNAL, QUICK_NOTE
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
    
    // Request notification permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* granted or denied, no action needed */ }
    
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    
    // Media state for journal
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var voiceRecordingPath by remember { mutableStateOf<String?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            Toast.makeText(context, "Image selected!", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Audio permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Start recording
            try {
                val audioFile = File(context.filesDir, "voice_${System.currentTimeMillis()}.mp4")
                val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }
                recorder.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioSamplingRate(44100)
                    setAudioEncodingBitRate(128000)
                    setOutputFile(audioFile.absolutePath)
                    prepare()
                    start()
                }
                mediaRecorder = recorder
                voiceRecordingPath = audioFile.absolutePath
                isRecording = true
                Toast.makeText(context, "Recording started...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Recording failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Microphone permission needed for recording", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Cleanup media recorder  
    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaRecorder?.release()
            } catch (_: Exception) { }
        }
    }
    
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
                
                // Onboarding - collects name, then chains to privacy/profile/walkthrough
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onComplete = { name ->
                            scope.launch { preferences.saveUserName(name) }
                            navController.navigate(Screen.PrivacyConfirm.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }
                
                // Privacy confirmation
                composable(Screen.PrivacyConfirm.route) {
                    PrivacyConfirmScreen(
                        onContinue = {
                            navController.navigate(Screen.ProfileSetup.route) {
                                popUpTo(Screen.PrivacyConfirm.route) { inclusive = true }
                            }
                        }
                    )
                }
                
                // Profile setup (optional)
                composable(Screen.ProfileSetup.route) {
                    ProfileSetupScreen(
                        onContinue = {
                            navController.navigate(Screen.Walkthrough.route) {
                                popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                            }
                        },
                        onSkip = {
                            navController.navigate(Screen.Walkthrough.route) {
                                popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                            }
                        }
                    )
                }
                
                // Walkthrough slides
                composable(Screen.Walkthrough.route) {
                    WalkthroughScreen(
                        onComplete = {
                            scope.launch { preferences.setOnboardingCompleted(true) }
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Walkthrough.route) { inclusive = true }
                            }
                        },
                        onSkip = {
                            scope.launch { preferences.setOnboardingCompleted(true) }
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Walkthrough.route) { inclusive = true }
                            }
                        }
                    )
                }
                
                // Legacy welcome (redirects to onboarding)
                composable(Screen.Welcome.route) {
                    OnboardingScreen(
                        onComplete = { name ->
                            scope.launch { preferences.saveUserName(name) }
                            navController.navigate(Screen.PrivacyConfirm.route) {
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
                        onAddTask = { activeDialog = ActiveDialog.ADD_TASK }
                    )
                }
                
                composable(Screen.Habits.route) {
                    HabitsScreen(
                        onAddHabit = { activeDialog = ActiveDialog.ADD_HABIT },
                        onHabitClick = { habitId ->
                            navController.navigate(Screen.HabitDetail.createRoute(habitId))
                        }
                    )
                }
                
                composable(Screen.Journal.route) {
                    JournalScreen(
                        onAddEntry = { activeDialog = ActiveDialog.ADD_JOURNAL }
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
                
                // Habit Detail
                composable(
                    route = Screen.HabitDetail.route,
                    arguments = listOf(
                        androidx.navigation.navArgument("habitId") {
                            type = androidx.navigation.NavType.LongType
                        }
                    )
                ) { backStackEntry ->
                    val habitId = backStackEntry.arguments?.getLong("habitId") ?: return@composable
                    HabitDetailScreen(
                        habitId = habitId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                // Weekly Review
                composable(Screen.WeeklyReview.route) {
                    WeeklyReviewScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                // Goals
                composable(Screen.Goals.route) {
                    GoalsScreen(
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
        
        // Floating FAB - context-aware, directly opens relevant dialog
        if (showFAB) {
            FloatingGlassFAB(
                onClick = {
                    activeDialog = when (currentRoute) {
                        "plan" -> ActiveDialog.ADD_TASK
                        "habits" -> ActiveDialog.ADD_HABIT
                        "journal" -> ActiveDialog.ADD_JOURNAL
                        else -> ActiveDialog.ADD_TASK
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 100.dp)
                    .navigationBarsPadding()
            )
        }
        
        // Dialog handling
        when (activeDialog) {
            ActiveDialog.ADD_HABIT -> {
                AddHabitDialog(
                    onDismiss = { activeDialog = ActiveDialog.NONE },
                    onConfirm = { name, color, reminderTime ->
                        scope.launch {
                            try {
                                val habit = Habit(
                                    name = name,
                                    color = color,
                                    reminderTime = reminderTime
                                )
                                habitRepository.insertHabit(habit)
                                Toast.makeText(context, "Habit added!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
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
                            try {
                                val task = Task(
                                    title = title,
                                    description = description ?: "",
                                    date = dueDate,
                                    estimatedMinutes = estimatedMinutes
                                )
                                taskRepository.insertTask(task)
                                Toast.makeText(context, "Task added!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        activeDialog = ActiveDialog.NONE
                    }
                )
            }
            
            ActiveDialog.ADD_JOURNAL -> {
                AddJournalDialog(
                    onDismiss = {
                        // Cleanup media state
                        selectedImageUri = null
                        voiceRecordingPath = null
                        isRecording = false
                        try { mediaRecorder?.stop(); mediaRecorder?.release() } catch (_: Exception) { }
                        mediaRecorder = null
                        activeDialog = ActiveDialog.NONE
                    },
                    onConfirm = { content, type, tags, mood ->
                        scope.launch {
                            try {
                                // Copy image to app storage if selected
                                var savedMediaPath: String? = null
                                if (selectedImageUri != null) {
                                    try {
                                        val inputStream = context.contentResolver.openInputStream(selectedImageUri!!)
                                        val imageFile = File(context.filesDir, "journal_img_${System.currentTimeMillis()}.jpg")
                                        inputStream?.use { input ->
                                            imageFile.outputStream().use { output ->
                                                input.copyTo(output)
                                            }
                                        }
                                        savedMediaPath = imageFile.absolutePath
                                    } catch (_: Exception) { }
                                } else if (voiceRecordingPath != null) {
                                    savedMediaPath = voiceRecordingPath
                                }
                                
                                val entry = JournalEntry(
                                    type = type,
                                    content = content,
                                    tags = tags ?: "",
                                    mood = mood?.toIntOrNull(),
                                    mediaPath = savedMediaPath
                                )
                                journalRepository.insertEntry(entry)
                                Toast.makeText(context, "Journal entry saved!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        selectedImageUri = null
                        voiceRecordingPath = null
                        isRecording = false
                        activeDialog = ActiveDialog.NONE
                    },
                    onVoiceRecord = {
                        if (isRecording) {
                            // Stop recording
                            try {
                                mediaRecorder?.stop()
                                mediaRecorder?.release()
                                mediaRecorder = null
                                isRecording = false
                                Toast.makeText(context, "Recording saved!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                isRecording = false
                                Toast.makeText(context, "Recording error", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Check permission & start recording
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                                == PackageManager.PERMISSION_GRANTED
                            ) {
                                try {
                                    val audioFile = File(context.filesDir, "voice_${System.currentTimeMillis()}.mp4")
                                    val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        MediaRecorder(context)
                                    } else {
                                        @Suppress("DEPRECATION")
                                        MediaRecorder()
                                    }
                                    recorder.apply {
                                        setAudioSource(MediaRecorder.AudioSource.MIC)
                                        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                                        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                                        setAudioSamplingRate(44100)
                                        setAudioEncodingBitRate(128000)
                                        setOutputFile(audioFile.absolutePath)
                                        prepare()
                                        start()
                                    }
                                    mediaRecorder = recorder
                                    voiceRecordingPath = audioFile.absolutePath
                                    isRecording = true
                                    selectedImageUri = null // Clear image if switching to voice
                                    Toast.makeText(context, "🎙 Recording... tap again to stop", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Recording failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    onAddPhoto = {
                        voiceRecordingPath = null // Clear voice if switching to photo
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    selectedImageUri = selectedImageUri,
                    isRecording = isRecording,
                    voiceRecordingPath = voiceRecordingPath
                )
            }
            
            ActiveDialog.QUICK_NOTE -> {
                QuickNoteDialog(
                    onDismiss = { activeDialog = ActiveDialog.NONE },
                    onConfirm = { content ->
                        scope.launch {
                            try {
                                journalRepository.addQuickNote(content)
                                Toast.makeText(context, "Note saved!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        activeDialog = ActiveDialog.NONE
                    }
                )
            }
            
            ActiveDialog.NONE -> { /* No dialog shown */ }
        }
    }
}
