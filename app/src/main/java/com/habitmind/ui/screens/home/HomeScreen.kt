package com.habitmind.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitmind.data.database.entity.*
import com.habitmind.ui.components.PremiumHabitToggle
import com.habitmind.ui.components.fadeScaleIn
import com.habitmind.ui.components.staggeredEntrance
import com.habitmind.ui.theme.*
import com.habitmind.ui.viewmodel.HomeViewModel
import com.habitmind.ui.viewmodel.StateCategory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Custom modifier for premium fading edges in horizontal scroll
 */
fun Modifier.horizontalFadingEdge(
    edgeWidth: Dp = 24.dp
): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        
        // Define the gradient
        val colors = listOf(Color.Transparent, Color.Black)
        
        // Draw left fade
        drawRect(
            brush = Brush.horizontalGradient(colors, endX = edgeWidth.toPx()),
            blendMode = BlendMode.DstIn
        )
        
        // Draw right fade
        drawRect(
            brush = Brush.horizontalGradient(colors.reversed(), startX = size.width - edgeWidth.toPx()),
            blendMode = BlendMode.DstIn
        )
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String? = null,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    onNavigateToPlan: () -> Unit = {},
    onShowQuickNote: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val haptic = LocalHapticFeedback.current
    
    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            // 1. HEADER
            item {
                HomeHeader(
                    displayName = uiState.userName ?: userName,
                    onNavigateToSettings = onNavigateToSettings,
                    modifier = Modifier.padding(horizontal = Spacing.screenHorizontal)
                )
            }
            
            // 2. TODAY PROGRESS (Fun Animated Version)
            item {
                TodayProgressSection(
                    progress = uiState.totalProgress,
                    pendingCount = uiState.pendingCount,
                    modifier = Modifier
                        .padding(horizontal = Spacing.screenHorizontal)
                        .fadeScaleIn()
                )
            }
            
            // 3. STATE CONTROL
            item {
                StateControlSection(
                    journal = uiState.todayJournal,
                    onSelectCategory = { viewModel.showStateEditor(it) },
                    modifier = Modifier
                        .padding(horizontal = Spacing.screenHorizontal)
                        .staggeredEntrance(1)
                )
            }
            
            // 4. TODAY STATS
            item {
                TactileStatGrid(
                    journal = uiState.todayJournal,
                    modifier = Modifier
                        .padding(horizontal = Spacing.screenHorizontal)
                        .staggeredEntrance(2)
                )
            }
            
            // 5. PRIMARY JOURNAL CTA
            item {
                StrongJournalCTA(
                    journal = uiState.todayJournal,
                    onAction = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateToJournal()
                    },
                    modifier = Modifier
                        .padding(horizontal = Spacing.screenHorizontal)
                        .staggeredEntrance(3)
                )
            }
            
            // 6. TOP 3
            item {
                TactileTop3List(
                    priorities = uiState.tomorrowPriorities,
                    onEdit = onNavigateToJournal,
                    modifier = Modifier
                        .padding(horizontal = Spacing.screenHorizontal)
                        .staggeredEntrance(4)
                )
            }
            
            // 7. PENDING CHECKLIST
            item {
                SatisfyingChecklist(
                    journal = uiState.todayJournal,
                    habits = uiState.habits,
                    onToggle = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleHabitCompletion(it)
                    },
                    modifier = Modifier
                        .padding(horizontal = Spacing.screenHorizontal)
                        .staggeredEntrance(5)
                )
            }
            
            // 8. QUICK ACTIONS
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "QUICK ACTIONS", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = TextSubtle, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = Spacing.screenHorizontal)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    QuickActionPills(
                        onAction = { /* Handle actions */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .staggeredEntrance(6)
                    )
                }
            }
            
            // 9. ATTENTION INSIGHT
            uiState.attentionLeak?.let { leak ->
                item {
                    ActionableInsightCard(
                        title = "${leak.appName} ${leak.usageTime}",
                        subtitle = leak.insight,
                        actionLabel = leak.actionableLabel,
                        icon = Icons.Rounded.Smartphone,
                        tint = Warning,
                        onAction = { /* Handle action */ },
                        modifier = Modifier
                            .padding(horizontal = Spacing.screenHorizontal)
                            .staggeredEntrance(7)
                    )
                }
            }
            
            // 10. REVIEW REMINDER
            uiState.reviewReminder?.let { reminder ->
                item {
                    ActionableInsightCard(
                        title = reminder.title,
                        subtitle = reminder.subtitle,
                        actionLabel = reminder.actionableLabel,
                        icon = Icons.Rounded.EventRepeat,
                        tint = Accent,
                        onAction = { /* Handle action */ },
                        modifier = Modifier
                            .padding(horizontal = Spacing.screenHorizontal)
                            .staggeredEntrance(8)
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(120.dp)) }
        }

        // State Editor Bottom Sheet
        if (uiState.isStateEditorVisible) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.hideStateEditor() },
                sheetState = sheetState,
                containerColor = DarkSurface
            ) {
                StateEditorContent(
                    category = uiState.activeStateCategory!!,
                    journal = uiState.todayJournal,
                    onUpdateEnergy = { viewModel.updateEnergyLevel(it); viewModel.hideStateEditor() },
                    onUpdateSocial = { viewModel.updateSocialBattery(it); viewModel.hideStateEditor() },
                    onUpdateFocus = { viewModel.updateFocusLevel(it); viewModel.hideStateEditor() }
                )
            }
        }
    }
}

@Composable
private fun TodayProgressSection(
    progress: Float,
    pendingCount: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text("TODAY PROGRESS", style = MaterialTheme.typography.labelSmall, color = TextSubtle, fontWeight = FontWeight.ExtraBold)
            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.titleLarge, color = Accent, fontWeight = FontWeight.Black)
        }
        
        FunAnimatedProgressBar(progress = progress)
        
        Text(
            text = if (pendingCount > 0) "$pendingCount actions remaining to unlock optimal state" else "Optimal state achieved",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

/**
 * Premium, Fun Animated Progress Bar with Spring Physics and Liquid Glow
 */
@Composable
fun FunAnimatedProgressBar(progress: Float) {
    // Spring physics for "liquid" momentum
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "progress_pulse"
    )

    // Infinite pulse for the "glow tip"
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val barWidth = size.width * animatedProgress
            val barHeight = size.height
            
            // 1. Draw Subdued Grid/Segments (Background)
            val segmentCount = 10
            val segmentWidth = size.width / segmentCount
            for (i in 1 until segmentCount) {
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(i * segmentWidth, 0f),
                    end = Offset(i * segmentWidth, barHeight),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 2. Draw Progress Fill with Gradient
            if (barWidth > 0) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Accent, AccentVariant),
                        startX = 0f,
                        endX = barWidth
                    ),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
                
                // 3. Draw "Liquid Glow" at the tip
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Accent.copy(alpha = 0.6f * pulseAlpha), Color.Transparent),
                        center = Offset(barWidth, barHeight / 2),
                        radius = 16.dp.toPx()
                    ),
                    radius = 16.dp.toPx(),
                    center = Offset(barWidth, barHeight / 2)
                )

                // 4. Draw Tip Highlight
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.3f),
                    topLeft = Offset(barWidth - 4.dp.toPx(), 0f),
                    size = Size(4.dp.toPx(), barHeight),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(
    displayName: String?,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Hello, ${displayName ?: "Sagar"}",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
        }
        IconButton(
            onClick = onNavigateToSettings,
            modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkSurface)
        ) {
            Icon(Icons.Outlined.Settings, "Settings", tint = TextPrimary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun StateControlSection(
    journal: DailyJournal?,
    onSelectCategory: (StateCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        StateSegment(
            label = "Energy",
            value = journal?.energyLevel?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Neutral",
            modifier = Modifier.weight(1f),
            onClick = { onSelectCategory(StateCategory.ENERGY) }
        )
        StateSegment(
            label = "Social",
            value = journal?.socialBattery?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Neutral",
            modifier = Modifier.weight(1f),
            onClick = { onSelectCategory(StateCategory.SOCIAL) }
        )
        StateSegment(
            label = "Focus",
            value = journal?.focusLevel?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Okay",
            modifier = Modifier.weight(1f),
            onClick = { onSelectCategory(StateCategory.FOCUS) }
        )
    }
}

@Composable
private fun StateSegment(label: String, value: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSubtle)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TactileStatGrid(
    journal: DailyJournal?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
            LargeStatBlock(label = "SLEEP", value = "${journal?.sleepHours ?: 0}h", icon = Icons.Rounded.NightsStay, modifier = Modifier.weight(1f))
            LargeStatBlock(label = "STEPS", value = "${(journal?.steps ?: 0).toString().replace("(\\d)(?=(\\d{3})+$)".toRegex(), "$1,")}", icon = Icons.Rounded.DirectionsWalk, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
            LargeStatBlock(label = "WORKOUT", value = if (journal?.workoutCompleted == true) "DONE ✅" else "PENDING", icon = Icons.Rounded.FitnessCenter, modifier = Modifier.weight(1f))
            LargeStatBlock(label = "DEEP FOCUS", value = "${journal?.deepWorkHours ?: 0}h", icon = Icons.Rounded.Psychology, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun LargeStatBlock(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .padding(16.dp)
    ) {
        Icon(icon, null, tint = TextSubtle, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(value, style = MaterialTheme.typography.headlineSmall, color = TextPrimary, fontWeight = FontWeight.Black)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StrongJournalCTA(
    journal: DailyJournal?,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isComplete = journal?.isComplete ?: false
    val text = when {
        isComplete -> "Review Today"
        journal != null -> "Continue Journal"
        else -> "Start Today's Journal"
    }
    
    Button(
        onClick = onAction,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Accent,
            contentColor = DarkBackground
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Icon(Icons.Rounded.ArrowForward, null, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun TactileTop3List(
    priorities: List<TomorrowPriority>,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = priorities.count { it.text.isNotBlank() }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(DarkSurface)
            .padding(20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("TODAY'S TOP 3", style = MaterialTheme.typography.labelSmall, color = TextSubtle, fontWeight = FontWeight.Bold)
                Text("$completedCount/3 complete", style = MaterialTheme.typography.bodySmall, color = Accent)
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Rounded.Edit, null, tint = TextSubtle, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            (1..3).forEach { slot ->
                val priority = priorities.find { it.slot == slot }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(24.dp).border(1.5.dp, if (priority != null) Accent else TextMuted, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (priority != null) {
                            Icon(Icons.Rounded.Check, null, tint = Accent, modifier = Modifier.size(14.dp))
                        } else {
                            Text(slot.toString(), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }
                    Text(
                        text = priority?.text ?: "Set priority $slot",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (priority != null) TextPrimary else TextMuted,
                        textDecoration = if (priority != null) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
            }
        }
    }
}

@Composable
private fun SatisfyingChecklist(
    journal: DailyJournal?,
    habits: List<com.habitmind.data.repository.HabitWithStreak>,
    onToggle: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Text("PENDING ACTIONS", style = MaterialTheme.typography.labelSmall, color = TextSubtle, fontWeight = FontWeight.Bold)
        
        ChecklistItem(
            label = "Daily Journal",
            isDone = journal?.isComplete ?: false,
            onClick = { /* Navigate */ }
        )
        
        habits.take(3).forEach { habit ->
            ChecklistItem(
                label = habit.habit.name,
                isDone = habit.isCompletedToday,
                onClick = { onToggle(habit.habit.id) }
            )
        }
    }
}

@Composable
private fun ChecklistItem(label: String, isDone: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDone) DarkSurface.copy(alpha = 0.5f) else DarkSurface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isDone) Accent else DarkBackground)
                .border(1.dp, if (isDone) Accent else TextMuted, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) Icon(Icons.Rounded.Check, null, tint = DarkBackground, modifier = Modifier.size(16.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDone) TextSubtle else TextPrimary,
            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
        )
    }
}

@Composable
private fun QuickActionPills(
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(horizontal = Spacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        ActionPill("Log", Icons.Rounded.Edit, onAction, Modifier.weight(1f))
        ActionPill("Task", Icons.Rounded.Add, onAction, Modifier.weight(1f))
        ActionPill("Workout", Icons.Rounded.FitnessCenter, onAction, Modifier.weight(1f))
    }
}

@Composable
private fun ActionPill(label: String, icon: ImageVector, onClick: (String) -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = { onClick(label) },
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(100.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = TextPrimary),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = Accent, modifier = Modifier.size(20.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun ActionableInsightCard(
    title: String,
    subtitle: String,
    actionLabel: String,
    icon: ImageVector,
    tint: Color,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(tint.copy(alpha = 0.08f))
            .border(1.dp, tint.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(tint.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = tint,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.clickable(onClick = onAction)
                )
            }
        }
    }
}

@Composable
private fun StateEditorContent(
    category: StateCategory,
    journal: DailyJournal?,
    onUpdateEnergy: (EnergyLevel) -> Unit,
    onUpdateSocial: (SocialBattery) -> Unit,
    onUpdateFocus: (FocusLevel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.lg)
            .padding(bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Set current ${category.name.lowercase()}",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        when (category) {
            StateCategory.ENERGY -> {
                EnergyLevel.values().forEach { level ->
                    EditorOption(level.name, journal?.energyLevel == level) { onUpdateEnergy(level) }
                }
            }
            StateCategory.SOCIAL -> {
                SocialBattery.values().forEach { battery ->
                    EditorOption(battery.name, journal?.socialBattery == battery) { onUpdateSocial(battery) }
                }
            }
            StateCategory.FOCUS -> {
                FocusLevel.values().forEach { level ->
                    EditorOption(level.name, journal?.focusLevel == level) { onUpdateFocus(level) }
                }
            }
        }
    }
}

@Composable
private fun EditorOption(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Accent else DarkSurface)
            .clickable { 
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick() 
            }
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.replace("_", " "),
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) DarkBackground else TextPrimary,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
        )
    }
}
