package com.habitmind.ui.screens.insights

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitmind.HabitMindApplication
import com.habitmind.ui.components.fadeScaleIn
import com.habitmind.ui.components.staggeredEntrance
import com.habitmind.ui.theme.Accent
import com.habitmind.ui.theme.CardBackground
import com.habitmind.ui.theme.DarkBackground
import com.habitmind.ui.theme.GlassBorder
import com.habitmind.ui.theme.ProgressActive
import com.habitmind.ui.theme.ProgressTrack
import com.habitmind.ui.theme.Spacing
import com.habitmind.ui.theme.Success
import com.habitmind.ui.theme.TextMuted
import com.habitmind.ui.theme.TextPrimary
import com.habitmind.ui.theme.TextSecondary
import com.habitmind.ui.theme.Warning
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

data class InsightsUiState(
    val totalHabits: Int = 0,
    val habitsCompletedToday: Int = 0,
    val habitCompletionRate: Int = 0,
    val totalTasks: Int = 0,
    val tasksCompletedToday: Int = 0,
    val taskCompletionRate: Int = 0,
    val totalJournalEntries: Int = 0,
    val weeklyData: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f),
    val isLoading: Boolean = true
)

class InsightsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val app = application as HabitMindApplication
    private val habitRepository = app.habitRepository
    private val taskRepository = app.taskRepository
    private val journalRepository = app.journalRepository
    
    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()
    
    init {
        loadInsights()
    }
    
    private fun loadInsights() {
        viewModelScope.launch {
            val habits = habitRepository.allActiveHabits.first()
            val habitsWithStreak = habitRepository.getHabitsWithStreak(LocalDate.now()).first()
            val habitsCompleted = habitsWithStreak.count { it.isCompletedToday }
            
            val tasks = taskRepository.getTasksForDate(LocalDate.now()).first()
            val tasksCompleted = tasks.count { it.progress >= 100 }
            
            val journalEntries = journalRepository.allEntries.first()
            
            // Calculate completion rates
            val habitRate = if (habits.isNotEmpty()) (habitsCompleted * 100 / habits.size) else 0
            val taskRate = if (tasks.isNotEmpty()) (tasksCompleted * 100 / tasks.size) else 0
            
            // Generate weekly data based on actual habit completion
            val weeklyData = generateWeeklyData()
            
            _uiState.value = InsightsUiState(
                totalHabits = habits.size,
                habitsCompletedToday = habitsCompleted,
                habitCompletionRate = habitRate,
                totalTasks = tasks.size,
                tasksCompletedToday = tasksCompleted,
                taskCompletionRate = taskRate,
                totalJournalEntries = journalEntries.size,
                weeklyData = weeklyData,
                isLoading = false
            )
        }
    }
    
    private suspend fun generateWeeklyData(): List<Float> {
        val today = LocalDate.now()
        val data = mutableListOf<Float>()
        
        // Get data for last 7 days
        for (i in 6 downTo 0) {
            val date = today.minusDays(i.toLong())
            val tasks = taskRepository.getTasksForDate(date).first()
            val completed = tasks.count { it.progress >= 100 }
            val rate = if (tasks.isNotEmpty()) completed.toFloat() / tasks.size else 0f
            data.add(rate)
        }
        
        return data
    }
}

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = Spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        item {
            Spacer(modifier = Modifier.height(Spacing.lg))
            Text(
                text = "Insights",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                modifier = Modifier.fadeScaleIn()
            )
        }
        
        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Accent)
                }
            }
        } else {
            // Overview cards - row 1
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(0),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    InsightCard(
                        title = "Habits Today",
                        value = "${uiState.habitsCompletedToday}/${uiState.totalHabits}",
                        trend = "${uiState.habitCompletionRate}%",
                        isNegative = uiState.habitCompletionRate < 50,
                        modifier = Modifier.weight(1f)
                    )
                    InsightCard(
                        title = "Tasks Today",
                        value = "${uiState.tasksCompletedToday}/${uiState.totalTasks}",
                        trend = "${uiState.taskCompletionRate}%",
                        isNegative = uiState.taskCompletionRate < 50,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Overview cards - row 2
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(1),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    InsightCard(
                        title = "Journal Entries",
                        value = "${uiState.totalJournalEntries}",
                        trend = "total",
                        modifier = Modifier.weight(1f)
                    )
                    InsightCard(
                        title = "Active Habits",
                        value = "${uiState.totalHabits}",
                        trend = "tracking",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Weekly trends chart
            item {
                ChartCard(
                    title = "Weekly Task Completion",
                    data = uiState.weeklyData,
                    modifier = Modifier.staggeredEntrance(2)
                )
            }
            
            // Empty state message if no data
            if (uiState.totalHabits == 0 && uiState.totalTasks == 0) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.xxl),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "No data yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextSecondary
                            )
                            Text(
                                text = "Add habits and tasks to see insights",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(100.dp)) // Space for navbar
        }
    }
}

@Composable
fun InsightCard(
    title: String,
    value: String,
    trend: String,
    isNegative: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CardBackground,
                        CardBackground.copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = GlassBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(Spacing.lg)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(Spacing.sm))
        
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = Accent
        )
        
        Spacer(modifier = Modifier.height(Spacing.xs))
        
        Text(
            text = trend,
            style = MaterialTheme.typography.labelSmall,
            color = if (isNegative) Warning else Success
        )
    }
}

@Composable
fun ChartCard(
    title: String,
    data: List<Float>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CardBackground,
                        CardBackground.copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = GlassBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(Spacing.lg)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(Spacing.lg))
        
        // Chart bars based on real data
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
            data.forEachIndexed { index, value ->
                val height = (value.coerceIn(0f, 1f) * 100).dp
                val isToday = index == data.size - 1
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(height.coerceAtLeast(4.dp))
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                if (isToday) ProgressActive else ProgressTrack
                            )
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = dayLabels.getOrElse(index) { "" },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isToday) TextPrimary else TextSecondary
                    )
                }
            }
        }
    }
}
