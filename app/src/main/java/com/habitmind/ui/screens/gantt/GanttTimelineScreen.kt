package com.habitmind.ui.screens.gantt

import android.app.Application
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitmind.HabitMindApplication
import com.habitmind.data.database.entity.Task
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
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class GanttUiState(
    val tasks: List<Task> = emptyList(),
    val startDate: LocalDate = LocalDate.now().minusDays(3),
    val endDate: LocalDate = LocalDate.now().plusDays(10),
    val isLoading: Boolean = true
)

class GanttTimelineViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as HabitMindApplication).taskRepository
    private val _uiState = MutableStateFlow(GanttUiState())
    val uiState: StateFlow<GanttUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            repository.allTasks.collect { tasks ->
                val sorted = tasks.sortedBy { it.date }
                val start = sorted.firstOrNull()?.date?.minusDays(1) ?: LocalDate.now().minusDays(3)
                val end = sorted.lastOrNull()?.date?.plusDays(3) ?: LocalDate.now().plusDays(10)
                _uiState.value = GanttUiState(
                    tasks = sorted,
                    startDate = start,
                    endDate = end,
                    isLoading = false
                )
            }
        }
    }
}

/**
 * Gantt Timeline View
 * Horizontal scrollable timeline showing tasks across days
 */
@Composable
fun GanttTimelineScreen(
    onNavigateBack: () -> Unit,
    viewModel: GanttTimelineViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    
    val dayWidth = 80.dp
    val rowHeight = 48.dp
    val labelWidth = 120.dp
    val totalDays = ChronoUnit.DAYS.between(uiState.startDate, uiState.endDate).toInt() + 1
    val today = LocalDate.now()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextPrimary)
            }
            Text(
                text = "Timeline",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
        }
        
        if (uiState.tasks.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No tasks to display", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
            }
            return@Column
        }
        
        // Date header row
        Row(modifier = Modifier.fillMaxWidth()) {
            // Label column spacer
            Box(
                modifier = Modifier
                    .width(labelWidth)
                    .height(40.dp)
                    .background(DarkBackground)
                    .padding(start = Spacing.md),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("Task", style = MaterialTheme.typography.labelMedium, color = TextMuted)
            }
            
            // Scrollable date headers
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState)
            ) {
                for (i in 0 until totalDays) {
                    val date = uiState.startDate.plusDays(i.toLong())
                    val isToday = date == today
                    
                    Box(
                        modifier = Modifier
                            .width(dayWidth)
                            .height(40.dp)
                            .background(
                                if (isToday) Accent.copy(alpha = 0.1f) else DarkBackground
                            )
                            .border(
                                width = 0.5.dp,
                                color = GlassBorder.copy(alpha = 0.3f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = date.format(DateTimeFormatter.ofPattern("EEE")),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isToday) Accent else TextMuted
                            )
                            Text(
                                text = date.format(DateTimeFormatter.ofPattern("d")),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isToday) Accent else TextSecondary
                            )
                        }
                    }
                }
            }
        }
        
        // Task rows
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(uiState.tasks) { index, task ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Task label
                    Box(
                        modifier = Modifier
                            .width(labelWidth)
                            .height(rowHeight)
                            .background(CardBackground.copy(alpha = 0.3f))
                            .border(0.5.dp, GlassBorder.copy(alpha = 0.2f))
                            .padding(horizontal = Spacing.sm),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Timeline bar row
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(rowHeight)
                            .horizontalScroll(scrollState)
                    ) {
                        for (i in 0 until totalDays) {
                            val date = uiState.startDate.plusDays(i.toLong())
                            val isTaskDay = task.date == date
                            val isToday = date == today
                            
                            Box(
                                modifier = Modifier
                                    .width(dayWidth)
                                    .height(rowHeight)
                                    .background(
                                        if (isToday) Accent.copy(alpha = 0.05f)
                                        else DarkBackground.copy(alpha = 0.5f)
                                    )
                                    .border(0.5.dp, GlassBorder.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isTaskDay) {
                                    // Task bar
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f)
                                            .height(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                brush = Brush.horizontalGradient(
                                                    listOf(
                                                        ProgressActive.copy(alpha = 0.3f),
                                                        ProgressActive.copy(alpha = 0.7f)
                                                    ),
                                                    endX = (task.progress / 100f) * 200f
                                                )
                                            )
                                            .border(
                                                1.dp,
                                                if (task.progress >= 100) Accent.copy(alpha = 0.5f) 
                                                else ProgressActive.copy(alpha = 0.3f),
                                                RoundedCornerShape(6.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${task.progress}%",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
        
        // Today indicator line drawn via Canvas overlay
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val todayOffset = ChronoUnit.DAYS.between(uiState.startDate, today).toInt()
            if (todayOffset in 0 until totalDays) {
                val xPos = labelWidth.toPx() + (todayOffset * dayWidth.toPx()) + (dayWidth.toPx() / 2)
                drawLine(
                    color = Accent.copy(alpha = 0.5f),
                    start = Offset(xPos, 0f),
                    end = Offset(xPos, size.height),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))
                )
            }
        }
    }
}
