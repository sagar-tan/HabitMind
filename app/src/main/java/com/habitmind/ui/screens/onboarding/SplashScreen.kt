package com.habitmind.ui.screens.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitmind.ui.theme.Accent
import com.habitmind.ui.theme.DarkBackground
import com.habitmind.ui.theme.Motion
import com.habitmind.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Animated splash screen
 * Shows "Hello" then user's name with smooth transitions
 */
@Composable
fun SplashScreen(
    userName: String?,
    onComplete: () -> Unit
) {
    var phase by remember { mutableIntStateOf(0) } // 0: Hello, 1: Name, 2: Done
    val scope = rememberCoroutineScope()
    
    val helloAlpha = remember { Animatable(0f) }
    val helloScale = remember { Animatable(0.8f) }
    val nameAlpha = remember { Animatable(0f) }
    val nameOffset = remember { Animatable(40f) }
    
    LaunchedEffect(Unit) {
        // Phase 1: Show "Hello"
        scope.launch {
            helloAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(Motion.SCREEN, easing = FastOutSlowInEasing)
            )
        }
        helloScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(Motion.SCREEN, easing = FastOutSlowInEasing)
        )
        
        delay(800)
        
        // Phase 2: Show name (if available)
        if (!userName.isNullOrBlank()) {
            phase = 1
            scope.launch {
                nameAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(Motion.SCREEN, easing = FastOutSlowInEasing)
                )
            }
            nameOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(Motion.SCREEN, easing = FastOutSlowInEasing)
            )
            
            delay(1200)
        } else {
            delay(400)
        }
        
        // Phase 3: Fade out and complete
        scope.launch {
            helloAlpha.animateTo(0f, animationSpec = tween(Motion.SCREEN_FAST))
        }
        nameAlpha.animateTo(0f, animationSpec = tween(Motion.SCREEN_FAST))
        
        delay(300)
        onComplete()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hello text
            Text(
                text = "Hello",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Light
                ),
                color = TextPrimary,
                modifier = Modifier
                    .graphicsLayer {
                        alpha = helloAlpha.value
                        scaleX = helloScale.value
                        scaleY = helloScale.value
                    }
            )
            
            // User name
            if (!userName.isNullOrBlank() && phase >= 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = userName,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Accent,
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = nameAlpha.value
                            translationY = nameOffset.value
                        }
                )
            }
        }
    }
}
