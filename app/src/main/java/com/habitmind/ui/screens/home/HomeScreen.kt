package com.habitmind.ui.screens.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Note
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitmind.ui.components.fadeScaleIn
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
import com.habitmind.ui.viewmodel.HomeViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    userName: String? = null,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    onNavigateToPlan: () -> Unit = {},
    onShowQuickNote: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val today = LocalDate.now()
    val dayName = today.format(DateTimeFormatter.ofPattern("EEEE"))
    val dateFormatted = today.format(DateTimeFormatter.ofPattern("MMM d"))
    
    // Use viewModel userName if available, otherwise use parameter
    val displayName = uiState.userName ?: userName
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = Spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        // Greeting header with fade-scale animation
        item {
            Spacer(modifier = Modifier.height(Spacing.lg))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fadeScaleIn(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    // Personalized greeting
                    Text(
                        text = if (!displayName.isNullOrBlank()) "Hello, $displayName" else dayName,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (!displayName.isNullOrBlank()) Accent else TextSecondary
                    )
                    Text(
                        text = if (!displayName.isNullOrBlank()) dayName else dateFormatted,
                        style = if (!displayName.isNullOrBlank()) 
                            MaterialTheme.typography.titleLarge 
                        else 
                            MaterialTheme.typography.headlineLarge,
                        color = TextPrimary
                    )
                    if (!displayName.isNullOrBlank()) {
                        Text(
                            text = dateFormatted,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = TextSecondary
                    )
                }
            }
        }
        
        // Today Summary Card with staggered animation
        item {
            TodaySummaryCard(
                habitsCompleted = uiState.habitsCompleted,
                totalHabits = uiState.totalHabits,
                taskProgress = uiState.taskProgress.toInt(),
                modifier = Modifier.staggeredEntrance(0)
            )
        }
        
        // Quick Actions
        item {
            Column(modifier = Modifier.staggeredEntrance(1)) {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    QuickActionButton(
                        icon = Icons.Outlined.Book,
                        label = "Journal",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToJournal
                    )
                    QuickActionButton(
                        icon = Icons.AutoMirrored.Outlined.ListAlt,
                        label = "Add Task",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToPlan
                    )
                    QuickActionButton(
                        icon = Icons.Outlined.Note,
                        label = "Quick Note",
                        modifier = Modifier.weight(1f),
                        onClick = onShowQuickNote
                    )
                }
            }
        }
        
        // Habits preview
        if (uiState.habits.isNotEmpty()) {
            item {
                Column(modifier = Modifier.staggeredEntrance(2)) {
                    Text(
                        text = "Today's Habits",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        uiState.habits.take(4).forEach { habitWithStreak ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (habitWithStreak.isCompletedToday) 
                                            Accent.copy(alpha = 0.2f) 
                                        else 
                                            CardBackground
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (habitWithStreak.isCompletedToday) 
                                            Accent.copy(alpha = 0.3f) 
                                        else 
                                            GlassBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.toggleHabitCompletion(habitWithStreak.habit.id) }
                                    .padding(Spacing.md),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = habitWithStreak.habit.name.take(8),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (habitWithStreak.isCompletedToday) Accent else TextSecondary,
                                    maxLines = 1
                                )
                            }
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
fun TodaySummaryCard(
    habitsCompleted: Int,
    totalHabits: Int,
    taskProgress: Int,
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
            text = "Today's Progress",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(Spacing.lg))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatItem(
                value = "$habitsCompleted/$totalHabits",
                label = "Habits"
            )
            StatItem(
                value = "$taskProgress%",
                label = "Tasks"
            )
        }
        
        Spacer(modifier = Modifier.height(Spacing.lg))
        
        // Progress bar - monochrome
        LinearProgressIndicator(
            progress = { taskProgress / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = ProgressActive,
            trackColor = ProgressTrack
        )
    }
}

@Composable
fun StatItem(
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = Accent
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )
    
    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(
                width = 1.dp,
                color = GlassBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Accent,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
    }
}
