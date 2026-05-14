package com.habitmind.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitmind.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * A compact, premium habit toggle with a list-item style layout.
 * Habit name on the left, interactive slider on the right.
 */
@Composable
fun PremiumHabitToggle(
    name: String,
    isCompleted: Boolean,
    isNegative: Boolean,
    streak: Int,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    // Compact dimensions for list-style UI
    val trackWidth = 100.dp
    val thumbWidth = 50.dp
    val trackWidthPx = with(LocalDensity.current) { trackWidth.toPx() }
    val thumbWidthPx = with(LocalDensity.current) { thumbWidth.toPx() }
    val maxOffset = trackWidthPx - thumbWidthPx
    
    val offset = remember { Animatable(if (isCompleted) maxOffset else 0f) }
    
    LaunchedEffect(isCompleted) {
        offset.animateTo(
            targetValue = if (isCompleted) maxOffset else 0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
        )
    }

    // Color logic
    val posSuccess = Color(0xFF4ADE80)
    val negFailure = Color(0xFFF87171)
    
    val trackColor by animateColorAsState(
        targetValue = if (isCompleted) {
            if (isNegative) negFailure.copy(alpha = 0.1f) else posSuccess.copy(alpha = 0.1f)
        } else {
            DarkSurfaceVariant.copy(alpha = 0.5f)
        },
        label = "trackColor"
    )
    
    val thumbColor by animateColorAsState(
        targetValue = if (isCompleted) {
            if (isNegative) negFailure else posSuccess
        } else {
            // Neutral state
            Accent.copy(alpha = 0.6f)
        },
        label = "thumbColor"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground.copy(alpha = 0.4f))
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Habit Name & Streak
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = TextPrimary
            )
            if (streak > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$streak day streak",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Compact Slider Track
        Box(
            modifier = Modifier
                .width(trackWidth)
                .height(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(trackColor)
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                .clickable {
                    val newState = !isCompleted
                    onToggle(newState)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
        ) {
            // Background Labels
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "NO",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = if (isCompleted) TextMuted else OnAccent.copy(alpha = 0.9f)
                    )
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "YES",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = if (isCompleted) OnAccent.copy(alpha = 0.9f) else TextMuted
                    )
                }
            }
            
            // The Sliding Thumb
            Box(
                modifier = Modifier
                    .offset { IntOffset(offset.value.roundToInt(), 0) }
                    .width(thumbWidth)
                    .fillMaxHeight()
                    .padding(2.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(thumbColor, thumbColor.copy(alpha = 0.9f))
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            scope.launch {
                                val newOffset = (offset.value + delta).coerceIn(0f, maxOffset)
                                offset.snapTo(newOffset)
                            }
                        },
                        onDragStopped = {
                            val targetState = offset.value > maxOffset / 2
                            if (targetState != isCompleted) {
                                onToggle(targetState)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            } else {
                                offset.animateTo(if (isCompleted) maxOffset else 0f, spring(Spring.DampingRatioMediumBouncy))
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Label inside thumb for high contrast
                Text(
                    text = if (offset.value > maxOffset / 2) "YES" else "NO",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = OnAccent
                )
            }
        }
    }
}
