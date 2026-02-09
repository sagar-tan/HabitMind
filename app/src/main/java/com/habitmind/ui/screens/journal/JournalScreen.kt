package com.habitmind.ui.screens.journal

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import com.habitmind.data.database.entity.JournalEntry
import com.habitmind.data.database.entity.JournalEntryType
import com.habitmind.ui.components.staggeredEntrance
import com.habitmind.ui.theme.Accent
import com.habitmind.ui.theme.CardBackground
import com.habitmind.ui.theme.DarkBackground
import com.habitmind.ui.theme.GlassBorder
import com.habitmind.ui.theme.Spacing
import com.habitmind.ui.theme.TextMuted
import com.habitmind.ui.theme.TextPrimary
import com.habitmind.ui.theme.TextSecondary
import com.habitmind.ui.viewmodel.JournalViewModel
import java.time.format.DateTimeFormatter

@Composable
fun JournalScreen(
    onAddEntry: () -> Unit = {},
    viewModel: JournalViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
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
                text = "Journal",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
        }
        
        // Filter chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                FilterChip(
                    selected = uiState.filter == null,
                    onClick = { viewModel.setFilter(null) },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Accent.copy(alpha = 0.2f),
                        selectedLabelColor = Accent
                    )
                )
                FilterChip(
                    selected = uiState.filter == JournalEntryType.TEXT,
                    onClick = { viewModel.setFilter(JournalEntryType.TEXT) },
                    label = { Text("Text") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Accent.copy(alpha = 0.2f),
                        selectedLabelColor = Accent
                    )
                )
                FilterChip(
                    selected = uiState.filter == JournalEntryType.VOICE,
                    onClick = { viewModel.setFilter(JournalEntryType.VOICE) },
                    label = { Text("Voice") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Accent.copy(alpha = 0.2f),
                        selectedLabelColor = Accent
                    )
                )
                FilterChip(
                    selected = uiState.filter == JournalEntryType.IMAGE,
                    onClick = { viewModel.setFilter(JournalEntryType.IMAGE) },
                    label = { Text("Image") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Accent.copy(alpha = 0.2f),
                        selectedLabelColor = Accent
                    )
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
        } else if (uiState.entries.isEmpty()) {
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
                            text = "No journal entries",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "Tap + to start journaling",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            itemsIndexed(uiState.entries) { index, entry ->
                JournalEntryCard(
                    entry = entry,
                    modifier = Modifier.staggeredEntrance(index)
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(100.dp)) // Space for navbar + FAB
        }
    }
}

@Composable
fun JournalEntryCard(
    entry: JournalEntry,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "entryScale"
    )
    
    val typeIcon: ImageVector = when (entry.type) {
        JournalEntryType.TEXT -> Icons.Outlined.TextFields
        JournalEntryType.VOICE -> Icons.Outlined.Mic
        JournalEntryType.IMAGE -> Icons.Outlined.Image
    }
    
    val typeLabel = when (entry.type) {
        JournalEntryType.TEXT -> "Text"
        JournalEntryType.VOICE -> "Voice"
        JournalEntryType.IMAGE -> "Image"
    }
    
    Row(
        modifier = modifier
            .scale(scale)
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
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { }
            )
            .padding(Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.Top
    ) {
        // Type icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Accent.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = typeIcon,
                contentDescription = typeLabel,
                tint = Accent,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            // Content preview
            Text(
                text = if (entry.content.isNotBlank()) entry.content else "$typeLabel note",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                maxLines = 3
            )
            
            // Timestamp
            Text(
                text = entry.timestamp.format(DateTimeFormatter.ofPattern("MMM d, h:mm a")),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            
            // Duration for voice notes
            if (entry.type == JournalEntryType.VOICE && entry.mediaDurationSeconds != null) {
                Text(
                    text = "${entry.mediaDurationSeconds}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            
            // Mood indicator
            entry.mood?.let { mood ->
                val moodEmoji = when (mood) {
                    1 -> "😢"
                    2 -> "😕"
                    3 -> "😐"
                    4 -> "🙂"
                    5 -> "😊"
                    else -> ""
                }
                Text(
                    text = moodEmoji,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
