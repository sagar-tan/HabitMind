package com.habitmind.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habitmind.HabitMindApplication
import com.habitmind.data.database.entity.Habit
import com.habitmind.data.repository.HabitWithStreak
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HabitsUiState(
    val habits: List<HabitWithStreak> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class HabitsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = (application as HabitMindApplication).habitRepository
    
    private val _uiState = MutableStateFlow(HabitsUiState())
    val uiState: StateFlow<HabitsUiState> = _uiState.asStateFlow()
    
    init {
        loadHabits()
    }
    
    private fun loadHabits() {
        viewModelScope.launch {
            repository.getHabitsWithStreak(LocalDate.now()).collect { habits ->
                _uiState.value = HabitsUiState(
                    habits = habits,
                    isLoading = false
                )
            }
        }
    }
    
    fun toggleCompletion(habitId: Long) {
        viewModelScope.launch {
            repository.toggleCompletion(habitId)
        }
    }
    
    fun addHabit(name: String, description: String = "") {
        viewModelScope.launch {
            repository.insertHabit(
                Habit(name = name, description = description)
            )
        }
    }
    
    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }
    
    fun archiveHabit(habitId: Long) {
        viewModelScope.launch {
            repository.archiveHabit(habitId)
        }
    }
}
