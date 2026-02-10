package com.habitmind.ui.screens.goals

import android.app.Application
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitmind.HabitMindApplication
import com.habitmind.data.database.entity.Goal
import com.habitmind.ui.components.staggeredEntrance
import com.habitmind.ui.theme.Accent
import com.habitmind.ui.theme.CardBackground
import com.habitmind.ui.theme.DarkBackground
import com.habitmind.ui.theme.GlassBorder
import com.habitmind.ui.theme.ProgressActive
import com.habitmind.ui.theme.ProgressTrack
import com.habitmind.ui.theme.Spacing
import com.habitmind.ui.theme.TextMuted
import com.habitmind.ui.theme.TextPrimary
import com.habitmind.ui.theme.TextSecondary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class GoalsUiState(
    val goals: List<Goal> = emptyList(),
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false
)

class GoalsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as HabitMindApplication).goalRepository
    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            repository.allGoals.collect { goals ->
                _uiState.value = GoalsUiState(goals = goals, isLoading = false)
            }
        }
    }
    
    fun addGoal(title: String, description: String) {
        viewModelScope.launch {
            repository.insertGoal(
                Goal(title = title, description = description)
            )
        }
    }
    
    fun updateProgress(goalId: Long, progress: Int) {
        viewModelScope.launch {
            repository.updateProgress(goalId, progress.coerceIn(0, 100))
        }
    }
    
    fun toggleAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = !_uiState.value.showAddDialog)
    }
}

/**
 * Screen 14: Goals Screen
 * Goal cards with progress bars, add new goals
 */
@Composable
fun GoalsScreen(
    onNavigateBack: () -> Unit,
    viewModel: GoalsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var newGoalTitle by remember { mutableStateOf("") }
    var newGoalDescription by remember { mutableStateOf("") }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = Spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(Spacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextPrimary)
                    }
                    Text(
                        text = "Goals",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary
                    )
                }
                IconButton(onClick = { viewModel.toggleAddDialog() }) {
                    Icon(Icons.Rounded.Add, "Add Goal", tint = Accent)
                }
            }
        }
        
        // Add goal form
        if (uiState.showAddDialog) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBackground)
                        .border(1.dp, Accent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("New Goal", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    
                    BasicTextField(
                        value = newGoalTitle,
                        onValueChange = { newGoalTitle = it },
                        textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp),
                        cursorBrush = SolidColor(Accent),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBackground)
                            .padding(12.dp),
                        decorationBox = { inner ->
                            if (newGoalTitle.isEmpty()) Text("Goal title", style = TextStyle(color = TextMuted, fontSize = 16.sp))
                            inner()
                        }
                    )
                    
                    BasicTextField(
                        value = newGoalDescription,
                        onValueChange = { newGoalDescription = it },
                        textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                        cursorBrush = SolidColor(Accent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBackground)
                            .padding(12.dp),
                        decorationBox = { inner ->
                            if (newGoalDescription.isEmpty()) Text("Description (optional)", style = TextStyle(color = TextMuted, fontSize = 14.sp))
                            inner()
                        }
                    )
                    
                    Button(
                        onClick = {
                            if (newGoalTitle.isNotBlank()) {
                                viewModel.addGoal(newGoalTitle.trim(), newGoalDescription.trim())
                                newGoalTitle = ""
                                newGoalDescription = ""
                                viewModel.toggleAddDialog()
                            }
                        },
                        enabled = newGoalTitle.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add Goal", color = DarkBackground)
                    }
                }
            }
        }
        
        // Empty state
        if (!uiState.isLoading && uiState.goals.isEmpty() && !uiState.showAddDialog) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("No goals yet", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                        Text("Tap + to add your first goal", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                    }
                }
            }
        }
        
        // Goal cards
        itemsIndexed(uiState.goals) { index, goal ->
            GoalCard(
                goal = goal,
                onProgressUpdate = { viewModel.updateProgress(goal.id, it) },
                modifier = Modifier.staggeredEntrance(index)
            )
        }
        
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
private fun GoalCard(
    goal: Goal,
    onProgressUpdate: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = goal.progressPercent / 100f,
        animationSpec = tween(600),
        label = "goalProgress"
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    listOf(CardBackground, CardBackground.copy(alpha = 0.95f))
                )
            )
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = goal.title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${goal.progressPercent}%",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (goal.progressPercent >= 100) Accent else TextSecondary
            )
        }
        
        if (goal.description.isNotBlank()) {
            Text(
                text = goal.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        
        // Progress bar
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = ProgressActive,
            trackColor = ProgressTrack
        )
        
        // Quick progress buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(25, 50, 75, 100).forEach { value ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (goal.progress >= value) Accent.copy(alpha = 0.2f)
                            else CardBackground
                        )
                        .border(
                            1.dp,
                            if (goal.progress >= value) Accent.copy(alpha = 0.3f) else GlassBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onProgressUpdate(value) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$value%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (goal.progress >= value) Accent else TextMuted
                    )
                }
            }
        }
    }
}
