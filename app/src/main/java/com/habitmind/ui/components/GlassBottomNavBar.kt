package com.habitmind.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitmind.ui.theme.*

data class GlassNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val glassNavItems = listOf(
    GlassNavItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    GlassNavItem("journal", "Journal", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    GlassNavItem("domains", "Domains", Icons.Filled.GridView, Icons.Outlined.GridView),
    GlassNavItem("analytics", "Analytics", Icons.Filled.Insights, Icons.Outlined.Insights),
    GlassNavItem("reviews", "Reviews", Icons.Filled.History, Icons.Outlined.History)
)

/**
 * Glassmorphic floating island bottom navigation bar
 * Properly aligned with 5 evenly spaced items.
 */
@Composable
fun GlassBottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp) // Adjusted outer padding
            .navigationBarsPadding()
    ) {
        // Glass container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp) // Slightly taller for better label spacing
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.3f),
                    spotColor = Color.Black.copy(alpha = 0.3f)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GlassSurface.copy(alpha = 0.95f),
                            GlassSurface.copy(alpha = 0.90f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GlassBorder,
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center, // Each item will use weight(1f) to space evenly
                verticalAlignment = Alignment.CenterVertically
            ) {
                glassNavItems.forEach { item ->
                    GlassNavItemButton(
                        item = item,
                        selected = currentRoute == item.route,
                        onClick = { onNavigate(item.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun GlassNavItemButton(
    item: GlassNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.92f
            selected -> 1.05f
            else -> 1f
        },
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "navScale"
    )
    
    val iconAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.45f,
        animationSpec = tween(150),
        label = "iconAlpha"
    )
    
    Column(
        modifier = modifier
            .fillMaxHeight()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.label,
            modifier = Modifier.size(24.dp), // Consistent icon size
            tint = if (selected) Accent else TextPrimary.copy(alpha = iconAlpha)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                letterSpacing = 0.2.sp
            ),
            color = if (selected) Accent else TextMuted,
            maxLines = 1
        )
    }
}
