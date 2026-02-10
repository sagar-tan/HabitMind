package com.habitmind.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitmind.ui.theme.Accent
import com.habitmind.ui.theme.DarkBackground
import com.habitmind.ui.theme.Motion
import com.habitmind.ui.theme.Spacing
import com.habitmind.ui.theme.TextMuted
import com.habitmind.ui.theme.TextPrimary
import com.habitmind.ui.theme.TextSecondary

private data class WalkthroughSlide(
    val icon: ImageVector,
    val title: String,
    val description: String
)

private val slides = listOf(
    WalkthroughSlide(
        icon = Icons.Outlined.CheckCircle,
        title = "Track habits\nand plans",
        description = "Build daily routines with simple habit tracking and flexible task planning"
    ),
    WalkthroughSlide(
        icon = Icons.Outlined.CalendarMonth,
        title = "Weekly\nreviews",
        description = "Reflect on your week every Sunday. Carry forward tasks, celebrate wins"
    ),
    WalkthroughSlide(
        icon = Icons.Outlined.BarChart,
        title = "Local\nanalytics",
        description = "See your patterns over time. All data stays on your device, always"
    )
)

/**
 * Screen 4: Quick Walkthrough
 * 3 swipeable slides showing app features
 */
@Composable
fun WalkthroughScreen(
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    var currentSlide by remember { mutableIntStateOf(0) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(Spacing.screenHorizontal)
    ) {
        // Slide content
        AnimatedContent(
            targetState = currentSlide,
            transitionSpec = {
                (slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(Motion.SCREEN, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(Motion.SCREEN))) togetherWith
                (slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(Motion.SCREEN, easing = FastOutSlowInEasing)
                ) + fadeOut(tween(Motion.SCREEN_FAST)))
            },
            label = "slideTransition",
            modifier = Modifier.fillMaxSize()
        ) { slideIndex ->
            val slide = slides[slideIndex]
            
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Accent.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = slide.icon,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Text(
                    text = slide.title,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Light,
                        lineHeight = 44.sp
                    ),
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = slide.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
        
        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(slides.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == currentSlide) 24.dp else 8.dp, 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (index == currentSlide) Accent else TextMuted.copy(alpha = 0.3f)
                            )
                    )
                }
            }
            
            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextMuted,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onSkip)
                        .padding(12.dp)
                )
                
                // Next / Get Started
                if (currentSlide < slides.size - 1) {
                    ContinueButton(
                        text = "Next",
                        onClick = { currentSlide++ }
                    )
                } else {
                    ContinueButton(
                        text = "Get Started",
                        onClick = onComplete
                    )
                }
            }
        }
    }
}

