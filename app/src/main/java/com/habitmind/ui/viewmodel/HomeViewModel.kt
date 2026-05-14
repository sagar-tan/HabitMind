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
import kotlinx.coroutines.flow.first
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
    val tomorrowPriorities: List<TomorrowPriority> = emptyList()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val app = application as HabitMindApplication
    private val habitRepository = app.habitRepository
    private val taskRepository = app.taskRepository
    private val journalRepository = app.journalRepository
    private val dailyJournalRepository = app.dailyJournalRepository
    private val preferences = app.userPreferences
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        // Load user name
        viewModelScope.launch {
            preferences.userName.collect { name ->
                _uiState.value = _uiState.value.copy(userName = name)
            }
        }
        
        // Load habits
        viewModelScope.launch {
            habitRepository.getHabitsWithStreak(LocalDate.now()).collect { habits ->
                val completed = habits.count { it.isCompletedToday }
                _uiState.value = _uiState.value.copy(
                    habits = habits,
                    habitsCompleted = completed,
                    totalHabits = habits.size,
                    isLoading = false
                )
            }
        }
        
        // Load today's tasks
        viewModelScope.launch {
            taskRepository.getTasksForDate(LocalDate.now()).collect { tasks ->
                val avgProgress = taskRepository.getTodayProgress()
                _uiState.value = _uiState.value.copy(
                    todayTasks = tasks,
                    taskProgress = avgProgress
                )
            }
        }
        
        // Load recent journal entries (legacy)
        viewModelScope.launch {
            journalRepository.getRecentEntries(3).collect { entries ->
                _uiState.value = _uiState.value.copy(recentJournalEntries = entries)
            }
        }

        // Load today's journal status and tomorrow's priorities
        viewModelScope.launch {
            dailyJournalRepository.getJournalFlowByDate(LocalDate.now()).collect { journal ->
                _uiState.value = _uiState.value.copy(todayJournal = journal)
                if (journal != null) {
                    dailyJournalRepository.getPrioritiesForJournal(journal.id).collect { priorities ->
                        _uiState.value = _uiState.value.copy(tomorrowPriorities = priorities)
                    }
                }
            }
        }

        // Calculate journal streak
        calculateJournalStreak()
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
}
