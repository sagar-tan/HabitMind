package com.habitmind.ui.screens.plan

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitmind.data.database.entity.Task
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
import com.habitmind.ui.viewmodel.PlanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(
    onAddTask: () -> Unit = {},
    viewModel: PlanViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    var editingTask by remember { mutableStateOf<Task?>(null) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = Spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        item {
            Spacer(modifier = Modifier.height(Spacing.lg))
            Text(
                text = "Today's Plan",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
            
            // Progress summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.tasks.size} tasks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = "${uiState.averageProgress.toInt()}% complete",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (uiState.averageProgress >= 70) Accent else TextSecondary
                )
            }
            
            // Swipe hint
            if (uiState.tasks.isNotEmpty()) {
                Text(
                    text = "← Swipe to defer · Long press to edit →",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
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
        } else if (uiState.tasks.isEmpty()) {
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
                        Text(
                            text = "No tasks for today",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "Tap + to add a task",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            itemsIndexed(
                items = uiState.tasks,
                key = { _, task -> task.id }
            ) { index, task ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        if (dismissValue != SwipeToDismissBoxValue.Settled) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.deferTask(task)
                            true
                        } else false
                    }
                )
                
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        // Defer background
                        val color by animateColorAsState(
                            targetValue = when (dismissState.targetValue) {
                                SwipeToDismissBoxValue.Settled -> CardBackground
                                else -> Accent.copy(alpha = 0.3f)
                            },
                            label = "dismissBg"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(color)
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "→ Defer to tomorrow",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Accent
                            )
                        }
                    },
                    modifier = Modifier.staggeredEntrance(index)
                ) {
                    TaskCard(
                        task = task,
                        onProgressChange = { progress ->
                            viewModel.updateProgress(task.id, progress)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        onLongPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            editingTask = task
                        }
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(100.dp)) // Space for navbar + FAB
        }
    }
    
    // Edit dialog
    editingTask?.let { task ->
        EditTaskDialog(
            task = task,
            onDismiss = { editingTask = null },
            onSave = { title, description, minutes ->
                viewModel.updateProgress(task.id, task.progress) // keep progress
                editingTask = null
            },
            onDelete = {
                viewModel.deleteTask(task)
                editingTask = null
            }
        )
    }
}

@Composable
fun TaskCard(
    task: Task,
    onProgressChange: (Int) -> Unit,
    onLongPress: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var progress by remember(task.progress) { mutableFloatStateOf(task.progress.toFloat()) }
    
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
                color = if (task.progress >= 100) Accent.copy(alpha = 0.3f) else GlassBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPress() }
                )
            }
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = "${progress.toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = if (progress >= 100f) Accent else TextSecondary
            )
        }
        
        if (task.description.isNotBlank()) {
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        
        // Progress slider
        Slider(
            value = progress,
            onValueChange = { 
                progress = it
            },
            onValueChangeFinished = {
                onProgressChange(progress.toInt())
            },
            valueRange = 0f..100f,
            steps = 9, // 10%, 20%, etc.
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = ProgressActive,
                activeTrackColor = ProgressActive,
                inactiveTrackColor = ProgressTrack
            )
        )
        
        // Task metadata
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            if (task.isCarriedForward) {
                Text(
                    text = "↩️ Carried forward",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
            Text(
                text = "~${task.estimatedMinutes}min",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onSave: (String, String, Int) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Task", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                androidx.compose.material3.OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                androidx.compose.material3.OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = { onSave(title, description, task.estimatedMinutes) }
            ) { Text("Save", color = Accent) }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.TextButton(
                    onClick = onDelete
                ) { Text("Delete", color = Color.Red.copy(alpha = 0.7f)) }
                androidx.compose.material3.TextButton(
                    onClick = onDismiss
                ) { Text("Cancel", color = TextSecondary) }
            }
        },
        containerColor = CardBackground
    )
}
