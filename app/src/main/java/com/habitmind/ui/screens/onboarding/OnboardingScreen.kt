package com.habitmind.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitmind.ui.theme.Accent
import com.habitmind.ui.theme.CardBackground
import com.habitmind.ui.theme.DarkBackground
import com.habitmind.ui.theme.GlassBorder
import com.habitmind.ui.theme.Motion
import com.habitmind.ui.theme.Spacing
import com.habitmind.ui.theme.TextMuted
import com.habitmind.ui.theme.TextPrimary
import com.habitmind.ui.theme.TextSecondary
import kotlinx.coroutines.delay

/**
 * Onboarding walkthrough - collects user name
 * Minimal, focused, ADHD-friendly
 */
@Composable
fun OnboardingScreen(
    onComplete: (String) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var userName by remember { mutableStateOf("") }
    var isVisible by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
    }
    
    LaunchedEffect(currentStep) {
        if (currentStep == 0) {
            delay(500)
            focusRequester.requestFocus()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(Spacing.screenHorizontal)
            .imePadding()
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(Motion.SCREEN)) + 
                    slideInVertically(
                        initialOffsetY = { 60 },
                        animationSpec = tween(Motion.SCREEN, easing = FastOutSlowInEasing)
                    )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                when (currentStep) {
                    0 -> NameInputStep(
                        name = userName,
                        onNameChange = { userName = it },
                        onNext = { if (userName.isNotBlank()) currentStep = 1 },
                        focusRequester = focusRequester
                    )
                    1 -> WelcomeStep(
                        userName = userName,
                        onStart = { onComplete(userName) }
                    )
                }
            }
        }
        
        // Progress indicator
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(2) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == currentStep) 24.dp else 8.dp, 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (index == currentStep) Accent else TextMuted.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}

@Composable
fun NameInputStep(
    name: String,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit,
    focusRequester: FocusRequester
) {
    Column {
        Text(
            text = "What should we\ncall you?",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Light,
                lineHeight = 44.sp
            ),
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Name input field - minimal style
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .border(
                    width = 1.dp,
                    color = if (name.isNotBlank()) Accent.copy(alpha = 0.3f) else GlassBorder,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(Spacing.lg)
        ) {
            BasicTextField(
                value = name,
                onValueChange = onNameChange,
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(Accent),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                decorationBox = { innerTextField ->
                    if (name.isEmpty()) {
                        Text(
                            text = "Your name",
                            style = TextStyle(
                                color = TextMuted,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                    innerTextField()
                }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Continue button
        if (name.isNotBlank()) {
            ContinueButton(onClick = onNext)
        }
    }
}

@Composable
fun WelcomeStep(
    userName: String,
    onStart: () -> Unit
) {
    Column {
        Text(
            text = "Welcome,",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Light
            ),
            color = TextSecondary
        )
        
        Text(
            text = userName,
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = Accent
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Your journey to better habits starts here. Everything stays on your device, private and secure.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        ContinueButton(
            text = "Let's Begin",
            onClick = onStart
        )
    }
}

@Composable
fun ContinueButton(
    text: String = "Continue",
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "buttonScale"
    )
    
    Row(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Accent, Accent.copy(alpha = 0.9f))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = DarkBackground
        )
        Icon(
            imageVector = Icons.Rounded.ArrowForward,
            contentDescription = null,
            tint = DarkBackground,
            modifier = Modifier.size(20.dp)
        )
    }
}
