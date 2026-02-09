package com.habitmind.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habitmind.HabitMindApplication
import com.habitmind.data.database.entity.DailyLog
import com.habitmind.data.database.entity.JournalEntry
import com.habitmind.data.database.entity.JournalEntryType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class JournalUiState(
    val entries: List<JournalEntry> = emptyList(),
    val dailyLogs: List<DailyLog> = emptyList(),
    val isLoading: Boolean = true,
    val filter: JournalEntryType? = null, // null = all
    val error: String? = null
)

class JournalViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = (application as HabitMindApplication).journalRepository
    
    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()
    
    init {
        loadEntries()
    }
    
    private fun loadEntries() {
        viewModelScope.launch {
            repository.allEntries.collect { entries ->
                _uiState.value = _uiState.value.copy(
                    entries = entries,
                    isLoading = false
                )
            }
        }
        
        viewModelScope.launch {
            repository.allDailyLogs.collect { logs ->
                _uiState.value = _uiState.value.copy(
                    dailyLogs = logs
                )
            }
        }
    }
    
    fun setFilter(type: JournalEntryType?) {
        _uiState.value = _uiState.value.copy(filter = type)
        
        viewModelScope.launch {
            val flow = if (type != null) {
                repository.getEntriesByType(type)
            } else {
                repository.allEntries
            }
            
            flow.collect { entries ->
                _uiState.value = _uiState.value.copy(entries = entries)
            }
        }
    }
    
    fun addTextEntry(content: String, tags: String = "", mood: Int? = null) {
        viewModelScope.launch {
            repository.insertEntry(
                JournalEntry(
                    type = JournalEntryType.TEXT,
                    content = content,
                    tags = tags,
                    mood = mood
                )
            )
        }
    }
    
    fun addVoiceEntry(mediaPath: String, durationSeconds: Int, caption: String = "") {
        viewModelScope.launch {
            repository.insertEntry(
                JournalEntry(
                    type = JournalEntryType.VOICE,
                    content = caption,
                    mediaPath = mediaPath,
                    mediaDurationSeconds = durationSeconds
                )
            )
        }
    }
    
    fun addImageEntry(mediaPath: String, caption: String = "") {
        viewModelScope.launch {
            repository.insertEntry(
                JournalEntry(
                    type = JournalEntryType.IMAGE,
                    content = caption,
                    mediaPath = mediaPath
                )
            )
        }
    }
    
    fun addQuickNote(content: String) {
        viewModelScope.launch {
            repository.addQuickNote(content)
        }
    }
    
    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }
    
    fun deleteDailyLog(log: DailyLog) {
        viewModelScope.launch {
            repository.deleteDailyLog(log)
        }
    }
}
