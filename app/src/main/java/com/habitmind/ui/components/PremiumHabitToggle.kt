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
 * A premium, interactive habit toggle with glassmorphism and haptic feedback.
 * Supports positive (achievement) and negative (avoidance) habits.
 */
@Composable
fun PremiumHabitToggle(
    name: String,
    isCompleted: Boolean,
    isNegative: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    // Toggle track width and thumb width
    val trackWidth = 140.dp
    val thumbWidth = 70.dp
    val trackWidthPx = with(LocalDensity.current) { trackWidth.toPx() }
    val thumbWidthPx = with(LocalDensity.current) { thumbWidth.toPx() }
    val maxOffset = trackWidthPx - thumbWidthPx
    
    // Animation state for the thumb offset
    val offset = remember { Animatable(if (isCompleted) maxOffset else 0f) }
    
    // Synchronize offset with external isCompleted state
    LaunchedEffect(isCompleted) {
        offset.animateTo(
            targetValue = if (isCompleted) maxOffset else 0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
        )
    }

    // Color logic based on habit nature
    // Positive habit: Completed (Right) = Success (Green), Not Completed (Left) = Neutral/Muted
    // Negative habit: Completed (Right) = Failure (Red), Not Completed (Left) = Success (Green)
    
    val successColor = Color(0xFF4ADE80)
    val failureColor = Color(0xFFF87171)
    
    val trackColor by animateColorAsState(
        targetValue = if (isCompleted) {
            if (isNegative) failureColor.copy(alpha = 0.15f) else successColor.copy(alpha = 0.15f)
        } else {
            if (isNegative) successColor.copy(alpha = 0.15f) else CardBackground
        },
        label = "trackColor"
    )
    
    val thumbColor by animateColorAsState(
        targetValue = if (isCompleted) {
            if (isNegative) failureColor else successColor
        } else {
            if (isNegative) successColor else Accent.copy(alpha = 0.8f)
        },
        label = "thumbColor"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(CardBackground)
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // The custom toggle track
        Box(
            modifier = Modifier
                .width(trackWidth)
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(trackColor)
                .border(1.dp, GlassBorder, RoundedCornerShape(22.dp))
                .clickable {
                    val newState = !isCompleted
                    onToggle(newState)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
        ) {
            // Labels background
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isNegative) "NO" else "NO",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = if (isCompleted) TextMuted else (if (isNegative) successColor else TextSecondary)
                    )
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isNegative) "YES" else "YES",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = if (isCompleted) (if (isNegative) failureColor else successColor) else TextMuted
                    )
                }
            }
            
            // The Sliding Thumb
            Box(
                modifier = Modifier
                    .offset { IntOffset(offset.value.roundToInt(), 0) }
                    .width(thumbWidth)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                thumbColor,
                                thumbColor.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
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
                                // Snap back
                                offset.animateTo(
                                    if (isCompleted) maxOffset else 0f,
                                    spring(Spring.DampingRatioMediumBouncy)
                                )
                            }
                        }
                    )
            )
        }
    }
}
