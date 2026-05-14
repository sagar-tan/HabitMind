package com.habitmind.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habitmind.HabitMindApplication
import com.habitmind.data.database.entity.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DailyJournalUiState(
    val journal: DailyJournal? = null,
    val slices: List<JournalStateSlice> = emptyList(),
    val events: List<JournalEvent> = emptyList(),
    val diagnostics: List<BehaviorDiagnostic> = emptyList(),
    val actionFixes: List<ActionFix> = emptyList(),
    val priorities: List<TomorrowPriority> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now()
)

class DailyJournalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as HabitMindApplication).dailyJournalRepository

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _isSaving = MutableStateFlow(false)
    private var saveJob: Job? = null

    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DailyJournalUiState> = _selectedDate.flatMapLatest { date ->
        val journalFlow = repository.getJournalFlowByDate(date)
        
        journalFlow.flatMapLatest { journal ->
            if (journal == null) {
                flowOf(DailyJournalUiState(selectedDate = date, isLoading = false))
            } else {
                combine(
                    repository.getStateSlicesForJournal(journal.id),
                    repository.getEventsForJournal(journal.id),
                    repository.getDiagnosticsForJournal(journal.id),
                    repository.getActionFixesForJournal(journal.id),
                    repository.getPrioritiesForJournal(journal.id)
                ) { slices, events, diagnostics, fixes, priorities ->
                    DailyJournalUiState(
                        journal = journal,
                        slices = slices,
                        events = events,
                        diagnostics = diagnostics,
                        actionFixes = fixes,
                        priorities = priorities,
                        selectedDate = date,
                        isLoading = false
                    )
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DailyJournalUiState()
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
     * Save journal with debounce
     */
    fun saveJournal(journal: DailyJournal) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            _isSaving.value = true
            delay(200)
            repository.saveJournal(journal.copy(updatedAt = java.time.LocalDateTime.now()))
            _isSaving.value = false
        }
    }

    /**
     * Ensure a journal entry exists for the selected date
     */
    fun ensureJournalExists() {
        viewModelScope.launch {
            val date = _selectedDate.value
            val existing = repository.getJournalByDate(date)
            if (existing == null) {
                repository.saveJournal(DailyJournal(date = date))
            }
        }
    }
    
    fun saveStateSlice(slice: JournalStateSlice) {
        viewModelScope.launch {
            repository.saveStateSlice(slice)
        }
    }
    
    fun addEvent(text: String) {
        val currentJournal = uiState.value.journal ?: return
        viewModelScope.launch {
            val position = uiState.value.events.size
            repository.saveEvent(JournalEvent(journalId = currentJournal.id, position = position, text = text))
        }
    }

    fun deleteEvent(event: JournalEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    fun addActionFix(problem: String, fix: String) {
        val currentJournal = uiState.value.journal ?: return
        viewModelScope.launch {
            val position = uiState.value.actionFixes.size
            repository.saveActionFix(ActionFix(journalId = currentJournal.id, problem = problem, fix = fix, position = position))
        }
    }

    fun deleteActionFix(fix: ActionFix) {
        viewModelScope.launch {
            repository.deleteActionFix(fix)
        }
    }

    fun savePriority(priority: TomorrowPriority) {
        viewModelScope.launch {
            repository.savePriority(priority)
        }
    }

    fun toggleDiagnostic(diagnostic: BehaviorDiagnostic) {
        viewModelScope.launch {
            repository.saveDiagnostic(diagnostic.copy(isChecked = !diagnostic.isChecked))
        }
    }
}
