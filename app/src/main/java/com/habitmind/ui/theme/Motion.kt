package com.habitmind.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween

/**
 * Motion tokens per specification:
 * - Micro interactions: 120-180ms
 * - Screen transitions: 250-350ms
 * - Heavy transitions: max 400ms
 * 
 * Easing:
 * - Ease out for entering elements
 * - Ease in for exiting elements
 */
object Motion {
    // Timing durations
    const val MICRO_FAST = 120
    const val MICRO = 150
    const val MICRO_SLOW = 180
    
    const val SCREEN_FAST = 250
    const val SCREEN = 300
    const val SCREEN_SLOW = 350
    
    const val HEAVY = 400
    
    // Easing curves
    val EaseOut = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    val EaseIn = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
    val EaseInOut = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    
    // Pre-built animation specs
    fun <T> microEnter() = tween<T>(durationMillis = MICRO, easing = EaseOut)
    fun <T> microExit() = tween<T>(durationMillis = MICRO, easing = EaseIn)
    
    fun <T> screenEnter() = tween<T>(durationMillis = SCREEN, easing = EaseOut)
    fun <T> screenExit() = tween<T>(durationMillis = SCREEN, easing = EaseIn)
    
    fun <T> heavyEnter() = tween<T>(durationMillis = HEAVY, easing = EaseOut)
    fun <T> heavyExit() = tween<T>(durationMillis = HEAVY, easing = EaseIn)
    
    // Button press scale (96% per spec)
    const val BUTTON_PRESS_SCALE = 0.96f
    
    // Screen scale for push navigation (96% per spec)
    const val SCREEN_SCALE_FACTOR = 0.96f
    
    // Progress bar trail delay
    const val PROGRESS_TRAIL_DELAY = 40
}
