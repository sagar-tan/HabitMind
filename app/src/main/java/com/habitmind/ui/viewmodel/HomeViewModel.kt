package com.habitmind.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habitmind.HabitMindApplication
import com.habitmind.data.database.entity.DailyJournal
import com.habitmind.data.database.entity.JournalEntry
import com.habitmind.data.database.entity.Task
import com.habitmind.data.database.entity.TomorrowPriority
import com.habitmind.data.repository.HabitWithStreak
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val userName: String? = null,
    val habitsCompleted: Int = 0,
    val totalHabits: Int = 0,
    val taskProgress: Float = 0f,
    val habits: List<HabitWithStreak> = emptyList(),
    val todayTasks: List<Task> = emptyList(),
    val recentJournalEntries: List<JournalEntry> = emptyList(),
    val todayJournal: DailyJournal? = null,
    val journalStreak: Int = 0,
    val isLoading: Boolean = true,
    val tomorrowPriorities: List<TomorrowPriority> = emptyList(),
    val attentionLeak: AttentionLeak? = null,
    val reviewReminder: ReviewReminder? = null,
    val isStateEditorVisible: Boolean = false,
    val activeStateCategory: StateCategory? = null,
    val totalProgress: Float = 0f,
    val pendingCount: Int = 0
)

enum class StateCategory { ENERGY, SOCIAL, FOCUS }
data class AttentionLeak(val appName: String, val usageTime: String, val insight: String, val actionableLabel: String)
data class ReviewReminder(val title: String, val subtitle: String, val actionableLabel: String)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val app = application as HabitMindApplication
    private val habitRepository = app.habitRepository
    private val taskRepository = app.taskRepository
    private val journalRepository = app.journalRepository
    private val dailyJournalRepository = app.dailyJournalRepository
    private val preferences = app.userPreferences
    private val dataGatheringManager = app.dataGatheringManager
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
        refreshSystemData()
    }
    
    private fun loadData() {
        // Load user name
        viewModelScope.launch {
            preferences.userName.collect { name ->
                _uiState.value = _uiState.value.copy(userName = name)
            }
        }
        
        // Combine multiple flows for progress calculation
        viewModelScope.launch {
            combine(
                habitRepository.getHabitsWithStreak(LocalDate.now()),
                dailyJournalRepository.getJournalFlowByDate(LocalDate.now()),
                taskRepository.getTasksForDate(LocalDate.now())
            ) { habits, journal, tasks ->
                Triple(habits, journal, tasks)
            }.collect { (habits, journal, tasks) ->
                val completedHabits = habits.count { it.isCompletedToday }
                val totalHabits = habits.size
                
                // Calculate progress
                var progressSum = 0f
                var totalWeights = 0
                
                if (totalHabits > 0) {
                    progressSum += (completedHabits.toFloat() / totalHabits)
                    totalWeights++
                }
                
                if (journal != null) {
                    if (journal.isComplete) progressSum += 1f
                    totalWeights++
                } else {
                    totalWeights++ // Placeholder for journal
                }

                val finalProgress = if (totalWeights > 0) progressSum / totalWeights else 0f
                
                // Calculate pending count
                var pending = 0
                if (journal == null || !journal.isComplete) pending++
                pending += (totalHabits - completedHabits)
                
                _uiState.value = _uiState.value.copy(
                    habits = habits,
                    habitsCompleted = completedHabits,
                    totalHabits = totalHabits,
                    todayJournal = journal,
                    todayTasks = tasks,
                    totalProgress = finalProgress,
                    pendingCount = pending,
                    isLoading = false
                )
                
                if (journal != null) {
                    dailyJournalRepository.getPrioritiesForJournal(journal.id).collect { priorities ->
                        _uiState.value = _uiState.value.copy(tomorrowPriorities = priorities)
                    }
                }
            }
        }
        
        // Load recent journal entries (legacy)
        viewModelScope.launch {
            journalRepository.getRecentEntries(3).collect { entries ->
                _uiState.value = _uiState.value.copy(recentJournalEntries = entries)
            }
        }

        // Calculate journal streak
        calculateJournalStreak()
    }

    /**
     * Refreshes data from system sensors and health APIs
     */
    fun refreshSystemData() {
        viewModelScope.launch {
            // 1. Fetch distraction app usage
            val leak = dataGatheringManager.getTopDistractionApp()
            _uiState.value = _uiState.value.copy(
                attentionLeak = leak?.let {
                    AttentionLeak(it.first, "${it.second}m", it.third, "Set focus limit →")
                }
            )

            // 2. Fetch health data
            val steps = dataGatheringManager.getTodaySteps()
            val sleep = dataGatheringManager.getLastNightSleep()
            
            // 3. Update DailyJournal with system data
            val currentJournal = _uiState.value.todayJournal ?: DailyJournal(date = LocalDate.now())
            
            // Only update if data has changed to avoid unnecessary Room writes
            if (currentJournal.steps != steps || currentJournal.sleepHours != sleep) {
                dailyJournalRepository.saveJournal(
                    currentJournal.copy(
                        steps = steps,
                        sleepHours = sleep
                    )
                )
            }

            // 4. Mock Review Reminder if needed
            _uiState.value = _uiState.value.copy(
                reviewReminder = ReviewReminder("Weekly Review Due", "3 patterns waiting to reflect on", "Start Review →")
            )
        }
    }

    private fun calculateJournalStreak() {
        viewModelScope.launch {
            dailyJournalRepository.getAllJournalsFlow().collect { journals ->
                var streak = 0
                var checkDate = LocalDate.now()
                
                // Sort by date just in case
                val sortedJournals = journals.filter { it.isComplete }.sortedByDescending { it.date }
                
                // If today is not complete, start checking from yesterday
                val completedToday = sortedJournals.any { it.date == checkDate }
                if (!completedToday) {
                    checkDate = checkDate.minusDays(1)
                }

                while (sortedJournals.any { it.date == checkDate }) {
                    streak++
                    checkDate = checkDate.minusDays(1)
                }
                
                _uiState.value = _uiState.value.copy(journalStreak = streak)
            }
        }
    }
    
    fun toggleHabitCompletion(habitId: Long) {
        viewModelScope.launch {
            habitRepository.toggleCompletion(habitId)
        }
    }
    
    fun updateTaskProgress(taskId: Long, progress: Int) {
        viewModelScope.launch {
            taskRepository.updateProgress(taskId, progress)
        }
    }
    
    fun addQuickNote(content: String) {
        viewModelScope.launch {
            journalRepository.addQuickNote(content)
        }
    }
    
    fun updateEnergyLevel(level: com.habitmind.data.database.entity.EnergyLevel) {
        viewModelScope.launch {
            val currentJournal = uiState.value.todayJournal ?: DailyJournal(date = LocalDate.now())
            dailyJournalRepository.saveJournal(currentJournal.copy(energyLevel = level))
        }
    }
    
    fun updateSocialBattery(battery: com.habitmind.data.database.entity.SocialBattery) {
        viewModelScope.launch {
            val currentJournal = uiState.value.todayJournal ?: DailyJournal(date = LocalDate.now())
            dailyJournalRepository.saveJournal(currentJournal.copy(socialBattery = battery))
        }
    }
    
    fun updateFocusLevel(level: com.habitmind.data.database.entity.FocusLevel) {
        viewModelScope.launch {
            val currentJournal = uiState.value.todayJournal ?: DailyJournal(date = LocalDate.now())
            dailyJournalRepository.saveJournal(currentJournal.copy(focusLevel = level))
        }
    }
    
    fun updateIdentityAlignment(alignment: com.habitmind.data.database.entity.IdentityAlignment) {
        viewModelScope.launch {
            val currentJournal = uiState.value.todayJournal ?: DailyJournal(date = LocalDate.now())
            dailyJournalRepository.saveJournal(currentJournal.copy(identityAlignment = alignment))
        }
    }

    fun showStateEditor(category: StateCategory) {
        _uiState.value = _uiState.value.copy(
            isStateEditorVisible = true,
            activeStateCategory = category
        )
    }

    fun hideStateEditor() {
        _uiState.value = _uiState.value.copy(
            isStateEditorVisible = false,
            activeStateCategory = null
        )
    }
}
