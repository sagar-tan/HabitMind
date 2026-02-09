package com.habitmind.ui.screens.sheets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.habitmind.ui.theme.Accent
import com.habitmind.ui.theme.DarkBackground
import com.habitmind.ui.theme.GlassBorder
import com.habitmind.ui.theme.GlassSurface
import com.habitmind.ui.theme.Motion
import com.habitmind.ui.theme.Spacing
import com.habitmind.ui.theme.TextPrimary
import com.habitmind.ui.theme.TextSecondary

/**
 * Glassmorphic add item bottom sheet with smooth fade animation
 * Context-aware options based on current screen
 */
@Composable
fun AddItemSheet(
    currentScreen: String,
    onDismiss: () -> Unit,
    onAddTask: () -> Unit,
    onAddHabit: () -> Unit,
    onAddJournalEntry: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    // Background scrim with fade
    val scrimAlpha by animateFloatAsState(
        targetValue = if (isVisible) 0.5f else 0f,
        animationSpec = tween(Motion.SCREEN, easing = FastOutSlowInEasing),
        label = "scrimAlpha"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = scrimAlpha))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = tween(Motion.SCREEN, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(Motion.SCREEN, easing = FastOutSlowInEasing)),
            exit = slideOutVertically(
                targetOffsetY = { it / 3 },
                animationSpec = tween(Motion.SCREEN_FAST, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(Motion.SCREEN_FAST, easing = FastOutSlowInEasing)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                GlassSurface,
                                GlassSurface.copy(alpha = 0.98f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(GlassBorder, Color.Transparent)
                        ),
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { } // Prevent dismiss when clicking sheet
                    )
                    .padding(Spacing.xxl)
                    .navigationBarsPadding()
            ) {
                // Handle bar
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(TextSecondary.copy(alpha = 0.3f))
                        .align(Alignment.CenterHorizontally)
                )
                
                Spacer(modifier = Modifier.height(Spacing.xxl))
                
                Text(
                    text = "Create New",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(Spacing.lg))
                
                // Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    AddOption(
                        icon = Icons.Outlined.ListAlt,
                        label = "Task",
                        isHighlighted = currentScreen == "plan",
                        onClick = onAddTask,
                        modifier = Modifier.weight(1f)
                    )
                    AddOption(
                        icon = Icons.Outlined.CheckCircle,
                        label = "Habit",
                        isHighlighted = currentScreen == "habits",
                        onClick = onAddHabit,
                        modifier = Modifier.weight(1f)
                    )
                    AddOption(
                        icon = Icons.Outlined.Book,
                        label = "Journal",
                        isHighlighted = currentScreen == "journal",
                        onClick = onAddJournalEntry,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(Spacing.lg))
            }
        }
    }
}

@Composable
fun AddOption(
    icon: ImageVector,
    label: String,
    isHighlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Smooth fade for press state instead of bouncy scale
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.7f else 1f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "optionAlpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "optionScale"
    )
    
    Column(
        modifier = modifier
            .scale(scale)
            .alpha(alpha)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isHighlighted) Accent.copy(alpha = 0.15f)
                else DarkBackground.copy(alpha = 0.5f)
            )
            .border(
                width = 1.dp,
                color = if (isHighlighted) Accent.copy(alpha = 0.3f) else GlassBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isHighlighted) Accent else TextSecondary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isHighlighted) TextPrimary else TextSecondary
        )
    }
}
