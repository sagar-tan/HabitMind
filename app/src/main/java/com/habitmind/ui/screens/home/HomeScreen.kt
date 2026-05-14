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
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitmind.data.database.entity.DailyJournal
import com.habitmind.data.database.entity.TomorrowPriority
import com.habitmind.ui.components.PremiumHabitToggle
import com.habitmind.ui.components.fadeScaleIn
import com.habitmind.ui.components.staggeredEntrance
import com.habitmind.ui.theme.*
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
    
    val displayName = uiState.userName ?: userName
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(horizontal = Spacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
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
            
            // Today Snapshot (CRITICAL)
            item {
                TodaySnapshotCard(
                    journal = uiState.todayJournal,
                    modifier = Modifier.staggeredEntrance(0)
                )
            }

            // Streak System
            item {
                StreakSystemDisplay(
                    journalStreak = uiState.journalStreak,
                    habits = uiState.habits,
                    modifier = Modifier.staggeredEntrance(1)
                )
            }
            
            // Energy State (Replacement for Mood)
            item {
                EnergyStateWidget(
                    currentEnergy = uiState.todayJournal?.socialBattery ?: com.habitmind.data.database.entity.SocialBattery.MODERATE,
                    modifier = Modifier.staggeredEntrance(2)
                )
            }
            
            // Daily Top 3
            item {
                DailyTop3Widget(
                    priorities = uiState.tomorrowPriorities,
                    modifier = Modifier.staggeredEntrance(3),
                    onEdit = onNavigateToJournal
                )
            }
            
            // Identity Alignment Prompt
            item {
                IdentityAlignmentPrompt(
                    currentAlignment = uiState.todayJournal?.identityAlignment ?: com.habitmind.data.database.entity.IdentityAlignment.MOSTLY,
                    modifier = Modifier.staggeredEntrance(4)
                )
            }

            // Journal Status Card
            item {
                JournalStatusCard(
                    journal = uiState.todayJournal,
                    modifier = Modifier.staggeredEntrance(5),
                    onContinueJournal = onNavigateToJournal
                )
            }
            
            // Habits preview
            if (uiState.habits.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.staggeredEntrance(6)) {
                        Text(
                            text = "Core Habits",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            uiState.habits.take(3).forEach { habitWithStreak ->
                                PremiumHabitToggle(
                                    name = habitWithStreak.habit.name,
                                    isCompleted = habitWithStreak.isCompletedToday,
                                    isNegative = habitWithStreak.habit.isNegative,
                                    streak = habitWithStreak.currentStreak,
                                    onToggle = { viewModel.toggleHabitCompletion(habitWithStreak.habit.id) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Quick Brain Dump FAB
        FloatingActionButton(
            onClick = onShowQuickNote,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = Spacing.screenHorizontal),
            containerColor = Accent,
            contentColor = DarkBackground,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Rounded.Psychology, contentDescription = "Quick Brain Dump")
        }
    }
}

@Composable
fun JournalStatusCard(
    journal: DailyJournal?,
    modifier: Modifier = Modifier,
    onContinueJournal: () -> Unit = {}
) {
    val isStarted = journal != null
    val isComplete = journal?.isComplete ?: false
    
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
                color = if (isComplete) Accent.copy(alpha = 0.5f) else GlassBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onContinueJournal)
            .padding(Spacing.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Journal",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            
            if (isComplete) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Accent.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "COMPLETE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Accent
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(Spacing.md))
        
        Text(
            text = when {
                isComplete -> "You've captured your day. Reflection complete."
                isStarted -> "You've started your journal. Keep going!"
                else -> "Take a moment to reflect on your day."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(Spacing.lg))
        
        // CTAs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isComplete) CardBackground else Accent)
                    .border(
                        width = 1.dp,
                        color = if (isComplete) GlassBorder else Accent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(onClick = onContinueJournal)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isStarted) "Continue" else "Start Journal",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isComplete) TextPrimary else DarkBackground
                )
            }
            
            if (!isComplete) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                        .clickable(onClick = onContinueJournal) 
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Minimum Mode",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun TodaySnapshotCard(
    journal: DailyJournal?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardBackground)
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .padding(Spacing.lg)
    ) {
        Text(
            text = "Today Snapshot",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SnapshotItem(label = "Sleep", value = "${journal?.sleepHours ?: 0}h", icon = Icons.Rounded.Bolt)
            SnapshotItem(label = "Deep Work", value = "${journal?.deepWorkHours ?: 0}h", icon = Icons.Rounded.CheckCircle)
            SnapshotItem(label = "Screen", value = "${journal?.screenTimeHours ?: 0}h", icon = Icons.Rounded.Bolt)
        }
    }
}

@Composable
private fun SnapshotItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Accent, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(value, style = MaterialTheme.typography.labelLarge, color = TextPrimary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
    }
}

@Composable
fun StreakSystemDisplay(
    journalStreak: Int,
    habits: List<com.habitmind.data.repository.HabitWithStreak>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        StreakCard(
            label = "Journal",
            streak = journalStreak,
            modifier = Modifier.weight(1f)
        )
        val bestHabit = habits.maxByOrNull { it.currentStreak }
        StreakCard(
            label = bestHabit?.habit?.name ?: "Habits",
            streak = bestHabit?.currentStreak ?: 0,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StreakCard(label: String, streak: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.EmojiEvents, null, tint = Accent, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(Spacing.sm))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text("$streak days", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
        }
    }
}

@Composable
fun EnergyStateWidget(
    currentEnergy: com.habitmind.data.database.entity.SocialBattery,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(Spacing.md)
    ) {
        Text("Current Energy", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
        Spacer(modifier = Modifier.height(Spacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            EnergyItem(label = "Low", isSelected = currentEnergy == com.habitmind.data.database.entity.SocialBattery.DRAINED)
            EnergyItem(label = "Stable", isSelected = currentEnergy == com.habitmind.data.database.entity.SocialBattery.MODERATE)
            EnergyItem(label = "High", isSelected = currentEnergy == com.habitmind.data.database.entity.SocialBattery.CHARGED)
        }
    }
}

@Composable
private fun EnergyItem(label: String, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Accent else DarkBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) DarkBackground else TextSecondary
        )
    }
}

@Composable
fun DailyTop3Widget(
    priorities: List<TomorrowPriority>,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(Spacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Top 3 Priorities", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            IconButton(onClick = onEdit) {
                Icon(Icons.Rounded.Edit, null, tint = TextMuted, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.height(Spacing.sm))
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            if (priorities.isEmpty()) {
                Text("No priorities set for tomorrow", style = MaterialTheme.typography.bodySmall, color = TextMuted)
            } else {
                priorities.sortedBy { it.slot }.forEach { priority ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Accent)
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(priority.text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun IdentityAlignmentPrompt(
    currentAlignment: com.habitmind.data.database.entity.IdentityAlignment,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Accent.copy(alpha = 0.1f), Color.Transparent)
                )
            )
            .border(1.dp, Accent.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(Spacing.md)
    ) {
        Text(
            text = "Did your actions match who you want to become?",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            AlignmentButton(label = "Yes", isSelected = currentAlignment == com.habitmind.data.database.entity.IdentityAlignment.ALWAYS, modifier = Modifier.weight(1f))
            AlignmentButton(label = "Partly", isSelected = currentAlignment == com.habitmind.data.database.entity.IdentityAlignment.MOSTLY, modifier = Modifier.weight(1f))
            AlignmentButton(label = "No", isSelected = currentAlignment == com.habitmind.data.database.entity.IdentityAlignment.RARELY, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun AlignmentButton(label: String, isSelected: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Accent else DarkBackground)
            .border(1.dp, if (isSelected) Accent else GlassBorder, RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) DarkBackground else TextSecondary
        )
    }
}
