package com.habitmind.ui.screens.review

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import com.habitmind.data.database.entity.Task
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class WeeklyReviewUiState(
    val weekTasks: List<Task> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val habitsCompletedRate: Float = 0f,
    val isLoading: Boolean = true
)

class WeeklyReviewViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as HabitMindApplication
    private val _uiState = MutableStateFlow(WeeklyReviewUiState())
    val uiState: StateFlow<WeeklyReviewUiState> = _uiState.asStateFlow()
    
    init {
        loadWeekData()
    }
    
    private fun loadWeekData() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val weekAgo = today.minusDays(7)
            
            app.taskRepository.allTasks.collect { tasks ->
                val weekTasks = tasks.filter { task ->
                    task.date?.isAfter(weekAgo.minusDays(1)) == true &&
                    task.date?.isBefore(today.plusDays(1)) == true
                }
                val completed = weekTasks.count { it.progress >= 100 }
                _uiState.value = WeeklyReviewUiState(
                    weekTasks = weekTasks,
                    completedCount = completed,
                    totalCount = weekTasks.size,
                    isLoading = false
                )
            }
        }
    }
    
    fun carryForwardTask(task: Task) {
        viewModelScope.launch {
            app.taskRepository.insertTask(
                task.copy(
                    id = 0,
                    date = LocalDate.now(),
                    progress = 0,
                    isCarriedForward = true,
                    completedAt = null
                )
            )
        }
    }
}

/**
 * Screen 13: Weekly Review Screen
 * Week summary, task status, reflection area
 */
@Composable
fun WeeklyReviewScreen(
    onNavigateBack: () -> Unit,
    viewModel: WeeklyReviewViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var reflectionText by remember { mutableStateOf("") }
    val carryForwardChecked = remember { mutableStateMapOf<Long, Boolean>() }
    
    val today = LocalDate.now()
    val weekAgo = today.minusDays(7)
    
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Column {
                    Text(
                        text = "Weekly Review",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "${weekAgo.format(DateTimeFormatter.ofPattern("MMM d"))} - ${today.format(DateTimeFormatter.ofPattern("MMM d"))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
        
        // Week summary card
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // Tasks completed
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(CardBackground, CardBackground.copy(alpha = 0.95f))
                            )
                        )
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                        .padding(Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${uiState.completedCount}/${uiState.totalCount}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Accent
                    )
                    Text("Tasks done", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                }
                
                // Completion rate
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(CardBackground, CardBackground.copy(alpha = 0.95f))
                            )
                        )
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                        .padding(Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val rate = if (uiState.totalCount > 0) 
                        (uiState.completedCount * 100 / uiState.totalCount) else 0
                    Text(
                        text = "$rate%",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Accent
                    )
                    Text("Completion", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                }
            }
        }
        
        // Incomplete tasks — offer carry forward
        val incompleteTasks = uiState.weekTasks.filter { it.progress < 100 }
        if (incompleteTasks.isNotEmpty()) {
            item {
                Text(
                    text = "Incomplete Tasks",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Check to carry forward to next week",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            
            items(incompleteTasks) { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = carryForwardChecked[task.id] == true,
                        onCheckedChange = { carryForwardChecked[task.id] = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Accent,
                            uncheckedColor = TextMuted
                        )
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(task.title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        Text(
                            "${task.progress}% done",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
        
        // Reflection text area
        item {
            Text(
                text = "Reflection",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBackground)
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                    .padding(Spacing.md)
            ) {
                BasicTextField(
                    value = reflectionText,
                    onValueChange = { reflectionText = it },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp),
                    cursorBrush = SolidColor(Accent),
                    modifier = Modifier.fillMaxSize(),
                    decorationBox = { innerTextField ->
                        if (reflectionText.isEmpty()) {
                            Text(
                                "How did this week go? What will you do differently?",
                                style = TextStyle(color = TextMuted, fontSize = 16.sp)
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }
        
        // Save button
        item {
            Button(
                onClick = {
                    // Carry forward selected tasks
                    incompleteTasks
                        .filter { carryForwardChecked[it.id] == true }
                        .forEach { viewModel.carryForwardTask(it) }
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Save Review",
                    style = MaterialTheme.typography.titleMedium,
                    color = DarkBackground,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
