package com.habitmind.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habitmind.HabitMindApplication
import com.habitmind.data.database.entity.DailyTracker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DailyTrackerUiState(
    val tracker: DailyTracker? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now()
)

class DailyTrackerViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = (application as HabitMindApplication).dailyTrackerRepository
    
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _isSaving = MutableStateFlow(false)
    private var saveJob: Job? = null
    
    val selectedDate: StateFlow<LocalDate> = _selectedDate
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val tracker: StateFlow<DailyTracker?> = _selectedDate.flatMapLatest { date ->
        repository.getByDate(date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    
    val recentTrackers = repository.getRecent(30).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }
    
    fun goToToday() {
        _selectedDate.value = LocalDate.now()
    }
    
    fun previousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }
    
    fun nextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }
    
    /**
     * Save tracker with debounce (300ms) for smooth auto-save
     */
    fun save(tracker: DailyTracker) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            _isSaving.value = true
            delay(300) // debounce
            repository.save(tracker)
            _isSaving.value = false
        }
    }
    
    /**
     * Initialize a new tracker for the selected date if none exists
     */
    fun ensureTrackerExists() {
        viewModelScope.launch {
            val date = _selectedDate.value
            val existing = repository.getByDateSync(date)
            if (existing == null) {
                repository.save(DailyTracker(date = date))
            }
        }
    }
    
    fun addPhoto(path: String) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val current = repository.getByDateSync(date) ?: DailyTracker(date = date)
            val paths = if (current.photoPaths.isBlank()) path
                        else "${current.photoPaths},$path"
            repository.save(current.copy(photoPaths = paths))
        }
    }
    
    fun removePhoto(path: String) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val current = repository.getByDateSync(date) ?: return@launch
            val paths = current.photoList.filter { it != path }.joinToString(",")
            repository.save(current.copy(photoPaths = paths))
        }
    }
    
    fun deleteTracker() {
        viewModelScope.launch {
            val date = _selectedDate.value
            val current = repository.getByDateSync(date) ?: return@launch
            repository.delete(current)
        }
    }
}
