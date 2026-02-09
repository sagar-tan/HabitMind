package com.habitmind.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.habitmind.ui.theme.Motion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ADHD-friendly animation modifiers
 * Smooth, fluid, satisfying micro-interactions
 */

/**
 * Bouncy press animation - satisfying tactile feedback
 */
fun Modifier.bouncyPress(
    onClick: () -> Unit,
    pressScale: Float = 0.96f
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bouncyPress"
    )
    
    this
        .scale(scale)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                    onClick()
                }
            )
        }
}

/**
 * Staggered entrance animation for list items
 */
@Composable
fun Modifier.staggeredEntrance(
    index: Int,
    delayPerItem: Int = 50,
    duration: Int = Motion.SCREEN
): Modifier = composed {
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(30f) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        delay((index * delayPerItem).toLong())
        scope.launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(duration, easing = FastOutSlowInEasing)
            )
        }
        offsetY.animateTo(
            targetValue = 0f,
            animationSpec = tween(duration, easing = FastOutSlowInEasing)
        )
    }
    
    this.graphicsLayer {
        this.alpha = alpha.value
        translationY = offsetY.value
    }
}

/**
 * Smooth fade-scale entrance
 */
@Composable
fun Modifier.fadeScaleIn(
    duration: Int = Motion.SCREEN
): Modifier = composed {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.95f) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(duration, easing = FastOutSlowInEasing)
            )
        }
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
    
    this.graphicsLayer {
        this.alpha = alpha.value
        scaleX = scale.value
        scaleY = scale.value
    }
}

/**
 * Pulse animation for loading states
 */
@Composable
fun Modifier.pulseAnimation(): Modifier = composed {
    val alpha = remember { Animatable(0.6f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(800, easing = FastOutSlowInEasing)
            )
            alpha.animateTo(
                targetValue = 0.6f,
                animationSpec = tween(800, easing = FastOutSlowInEasing)
            )
        }
    }
    
    this.graphicsLayer {
        this.alpha = alpha.value
    }
}
