package com.habitmind.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habitmind.HabitMindApplication
import com.habitmind.data.database.entity.DailyLog
import com.habitmind.data.database.entity.JournalEntry
import com.habitmind.data.database.entity.JournalEntryType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class JournalUiState(
    val entries: List<JournalEntry> = emptyList(),
    val dailyLogs: List<DailyLog> = emptyList(),
    val isLoading: Boolean = true,
    val filter: JournalEntryType? = null, // null = all
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class JournalViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = (application as HabitMindApplication).journalRepository
    
    private val _filter = MutableStateFlow<JournalEntryType?>(null)
    
    /**
     * Single reactive pipeline:
     * filter changes → flatMapLatest to correct query → combine with dailyLogs → emit UiState
     */
    val uiState: StateFlow<JournalUiState> = combine(
        _filter.flatMapLatest { type ->
            if (type != null) repository.getEntriesByType(type)
            else repository.allEntries
        },
        repository.allDailyLogs,
        _filter
    ) { entries, logs, filter ->
        JournalUiState(
            entries = entries,
            dailyLogs = logs,
            isLoading = false,
            filter = filter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = JournalUiState()
    )
    
    fun setFilter(type: JournalEntryType?) {
        _filter.value = type
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
