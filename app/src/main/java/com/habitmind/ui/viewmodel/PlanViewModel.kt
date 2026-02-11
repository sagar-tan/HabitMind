package com.habitmind.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habitmind.HabitMindApplication
import com.habitmind.data.database.entity.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class PlanUiState(
    val tasks: List<Task> = emptyList(),
    val averageProgress: Float = 0f,
    val isLoading: Boolean = true,
    val selectedDate: LocalDate = LocalDate.now(),
    val error: String? = null
)

class PlanViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = (application as HabitMindApplication).taskRepository
    
    private val _uiState = MutableStateFlow(PlanUiState())
    val uiState: StateFlow<PlanUiState> = _uiState.asStateFlow()
    
    init {
        loadTasks()
    }
    
    private fun loadTasks(date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            repository.getTasksForDate(date).collect { tasks ->
                val avgProgress = repository.getTodayProgress()
                _uiState.value = PlanUiState(
                    tasks = tasks,
                    averageProgress = avgProgress,
                    isLoading = false,
                    selectedDate = date
                )
            }
        }
    }
    
    fun selectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date, isLoading = true)
        loadTasks(date)
    }
    
    fun addTask(title: String, description: String = "", estimatedMinutes: Int = 30) {
        viewModelScope.launch {
            repository.insertTask(
                Task(
                    title = title,
                    description = description,
                    date = _uiState.value.selectedDate,
                    estimatedMinutes = estimatedMinutes
                )
            )
        }
    }
    
    fun updateProgress(taskId: Long, progress: Int) {
        viewModelScope.launch {
            repository.updateProgress(taskId, progress)
        }
    }
    
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }
    
    fun carryForwardTasks() {
        viewModelScope.launch {
            repository.carryForwardTasks()
            loadTasks()
        }
    }
    
    fun deferTask(task: Task) {
        viewModelScope.launch {
            repository.insertTask(
                task.copy(
                    id = 0,
                    date = LocalDate.now().plusDays(1),
                    isCarriedForward = true,
                    completedAt = null
                )
            )
            repository.deleteTask(task)
        }
    }
}
