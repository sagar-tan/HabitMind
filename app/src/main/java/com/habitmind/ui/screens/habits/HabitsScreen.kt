package com.habitmind.ui.screens.habits

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitmind.data.repository.HabitWithStreak
import com.habitmind.ui.components.staggeredEntrance
import com.habitmind.ui.theme.*
import com.habitmind.ui.viewmodel.HabitsViewModel

/**
 * Bento-style Habits Grid.
 * Research-driven design: Uses the 'Von Restorff Effect' (Isolation Effect) 
 * where the completed state is visually distinct but not 'harsh'.
 * Alignment follows a strict 8dp/12dp grid for reduced cognitive load.
 */
@Composable
fun HabitsScreen(
    onAddHabit: () -> Unit = {},
    onHabitClick: (Long) -> Unit = {},
    viewModel: HabitsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = Spacing.screenHorizontal)
    ) {
        Spacer(modifier = Modifier.height(Spacing.xxl))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "Habit Domains",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = TextPrimary
                )
                Text(
                    text = "Your neuro-circuits for today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(Spacing.xl))
        
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
        } else if (uiState.habits.isEmpty()) {
            EmptyHabitsState()
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                itemsIndexed(uiState.habits) { index, habitWithStreak ->
                    BentoHabitTile(
                        habitWithStreak = habitWithStreak,
                        onToggle = { viewModel.toggleCompletion(habitWithStreak.habit.id) },
                        onClick = { onHabitClick(habitWithStreak.habit.id) },
                        modifier = Modifier.staggeredEntrance(index, delayPerItem = 40)
                    )
                }
            }
        }
    }
}

@Composable
fun BentoHabitTile(
    habitWithStreak: HabitWithStreak,
    onToggle: () -> Unit,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isCompleted = habitWithStreak.isCompletedToday
    val habitColor = Color(android.graphics.Color.parseColor(habitWithStreak.habit.color))
    
    val animatedBgColor by animateColorAsState(
        targetValue = if (isCompleted) habitColor.copy(alpha = 0.15f) else DarkSurface,
        label = "bgColor"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .height(110.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(animatedBgColor)
            .border(
                width = 1.dp,
                color = if (isCompleted) habitColor.copy(alpha = 0.4f) else GlassBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onToggle // Direct toggle for speed, click handled elsewhere if needed
            )
    ) {
        // Left accent bar (Neural circuit indicator)
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight(0.4f)
                .align(Alignment.CenterStart)
                .padding(start = 0.dp)
                .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                .background(habitColor.copy(alpha = if (isCompleted) 1f else 0.4f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Habit Name
                Text(
                    text = habitWithStreak.habit.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 18.sp
                    ),
                    color = if (isCompleted) TextPrimary else TextSecondary,
                    modifier = Modifier.weight(1f)
                )

                // Corner checkmark
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = habitColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Streak & Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${habitWithStreak.currentStreak}d streak",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = if (habitWithStreak.currentStreak > 0) habitColor.copy(alpha = 0.8f) else TextMuted
                )
                
                // Small indicator for negative habits
                if (habitWithStreak.habit.isNegative) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Error.copy(alpha = 0.6f))
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyHabitsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Neural Grid Empty",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
            )
            Text(
                text = "Tap the '+' to initialize a habit circuit.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}
