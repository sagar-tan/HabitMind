package com.habitmind.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Shape tokens - medium radius per spec
 */
object Shapes {
    val None = RoundedCornerShape(0.dp)
    val ExtraSmall = RoundedCornerShape(4.dp)
    val Small = RoundedCornerShape(8.dp)
    val Medium = RoundedCornerShape(12.dp)
    val Large = RoundedCornerShape(16.dp)
    val ExtraLarge = RoundedCornerShape(24.dp)
    val Full = RoundedCornerShape(50)
    
    // Card shapes
    val Card = RoundedCornerShape(16.dp)
    val CardSmall = RoundedCornerShape(12.dp)
    
    // Button shapes
    val Button = RoundedCornerShape(12.dp)
    val ButtonSmall = RoundedCornerShape(8.dp)
    
    // Bottom sheet
    val BottomSheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
}

/**
 * Spacing tokens
 */
object Spacing {
    val none = 0.dp
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val huge = 48.dp
    
    // Screen padding
    val screenHorizontal = 20.dp
    val screenVertical = 16.dp
}

/**
 * Elevation tokens - soft shadows per spec
 */
object Elevation {
    val none = 0.dp
    val low = 1.dp
    val medium = 4.dp
    val high = 8.dp
    val card = 2.dp
    val cardPressed = 6.dp
}
