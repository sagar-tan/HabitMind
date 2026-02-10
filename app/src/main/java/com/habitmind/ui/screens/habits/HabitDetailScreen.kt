package com.habitmind.ui.screens.habits

import android.app.Application
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitmind.HabitMindApplication
import com.habitmind.data.database.entity.Habit
import com.habitmind.data.database.entity.HabitCompletion
import com.habitmind.ui.theme.Accent
import com.habitmind.ui.theme.CardBackground
import com.habitmind.ui.theme.DarkBackground
import com.habitmind.ui.theme.GlassBorder
import com.habitmind.ui.theme.Spacing
import com.habitmind.ui.theme.TextMuted
import com.habitmind.ui.theme.TextPrimary
import com.habitmind.ui.theme.TextSecondary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class HabitDetailUiState(
    val habit: Habit? = null,
    val completions: List<HabitCompletion> = emptyList(),
    val currentStreak: Int = 0,
    val completionRate7d: Float = 0f,
    val completionRate30d: Float = 0f,
    val isLoading: Boolean = true
)

class HabitDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as HabitMindApplication).habitRepository
    private val _uiState = MutableStateFlow(HabitDetailUiState())
    val uiState: StateFlow<HabitDetailUiState> = _uiState.asStateFlow()
    
    fun loadHabit(habitId: Long) {
        viewModelScope.launch {
            repository.getHabitByIdFlow(habitId).collect { habit ->
                _uiState.value = _uiState.value.copy(habit = habit, isLoading = false)
            }
        }
        viewModelScope.launch {
            repository.getCompletionsForHabit(habitId).collect { completions ->
                _uiState.value = _uiState.value.copy(completions = completions)
            }
        }
        viewModelScope.launch {
            val today = LocalDate.now()
            val rate7 = repository.getCompletionRate(habitId, today.minusDays(7), today)
            val rate30 = repository.getCompletionRate(habitId, today.minusDays(30), today)
            _uiState.value = _uiState.value.copy(
                completionRate7d = rate7,
                completionRate30d = rate30
            )
        }
    }
}

/**
 * Screen 9: Habit Detail Screen
 * Streak visualization, completion history, stats
 */
@Composable
fun HabitDetailScreen(
    habitId: Long,
    onNavigateBack: () -> Unit,
    viewModel: HabitDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Load habit on first composition
    androidx.compose.runtime.LaunchedEffect(habitId) {
        viewModel.loadHabit(habitId)
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = Spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // Header with back button
        item {
            Spacer(modifier = Modifier.height(Spacing.md))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = uiState.habit?.name ?: "Habit",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
            }
        }
        
        // Streak card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(CardBackground, CardBackground.copy(alpha = 0.95f))
                        )
                    )
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                    .padding(Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "🔥", style = MaterialTheme.typography.displaySmall)
                Text(
                    text = "${uiState.currentStreak}",
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                    color = Accent
                )
                Text(
                    text = "day streak",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
        }
        
        // Stats row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                StatCard(
                    label = "7-day",
                    value = "${(uiState.completionRate7d * 100).toInt()}%",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "30-day",
                    value = "${(uiState.completionRate30d * 100).toInt()}%",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Total",
                    value = "${uiState.completions.size}",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Last 30 days completion grid
        item {
            Text(
                text = "Last 30 Days",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            val today = LocalDate.now()
            val completionDates = uiState.completions.map { it.date }.toSet()
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items((29 downTo 0).toList()) { daysAgo ->
                    val date = today.minusDays(daysAgo.toLong())
                    val isCompleted = completionDates.contains(date)
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isCompleted) Accent.copy(alpha = 0.8f)
                                    else CardBackground
                                )
                                .border(
                                    1.dp,
                                    if (date == today) Accent else GlassBorder,
                                    RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = DarkBackground,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        if (daysAgo % 7 == 0) {
                            Text(
                                text = date.format(DateTimeFormatter.ofPattern("d")),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
        
        // Completion history
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Recent Completions",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        }
        
        val recentCompletions = uiState.completions.take(20)
        items(recentCompletions.size) { index ->
            val completion = recentCompletions[index]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBackground)
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                    .padding(Spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = completion.date.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = DarkBackground,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .padding(Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Accent
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}
