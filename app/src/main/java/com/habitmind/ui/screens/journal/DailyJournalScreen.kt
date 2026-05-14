package com.habitmind.ui.screens.journal

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitmind.data.database.entity.*
import com.habitmind.ui.theme.*
import com.habitmind.ui.viewmodel.DailyJournalViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Time utilities for Metric Conversion
private fun floatToTime(value: Float): Pair<Int, Int> {
    val hours = value.toInt()
    val minutes = ((value - hours) * 60).toInt()
    return hours to minutes
}

private fun timeToFloat(hours: Int, minutes: Int): Float {
    return hours + (minutes / 60f)
}

@Composable
fun DailyJournalScreen(
    dateString: String? = null,
    onNavigateBack: () -> Unit,
    vm: DailyJournalViewModel = viewModel()
) {
    val selectedDate by vm.selectedDate.collectAsState()
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(dateString) {
        dateString?.let {
            try { vm.selectDate(LocalDate.parse(it)) } catch (_: Exception) {}
        }
        vm.ensureJournalExists()
    }

    LaunchedEffect(selectedDate) {
        vm.ensureJournalExists()
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            JournalHeader(
                selectedDate = selectedDate,
                onBack = onNavigateBack,
                onPrevious = { vm.previousDay() },
                onNext = { vm.nextDay() },
                onToday = { vm.goToToday() }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item { Spacer(modifier = Modifier.height(Spacing.sm)) }

            // 1. Daily Snapshot
            item {
                CollapsibleSection(title = "Daily Snapshot") {
                    DailySnapshotSection(
                        journal = uiState.journal ?: DailyJournal(date = selectedDate),
                        onUpdate = { vm.saveJournal(it) }
                    )
                }
            }

            // 2. State (Energy, Mood, Stress)
            item {
                CollapsibleSection(title = "Energy & Mood") {
                    StateSection(
                        journalId = uiState.journal?.id ?: 0,
                        slices = uiState.slices,
                        onUpdate = { vm.saveStateSlice(it) }
                    )
                }
            }

            // 3. Timeline
            item {
                CollapsibleSection(title = "Timeline") {
                    TimelineSection(
                        events = uiState.events,
                        onAddEvent = { vm.addEvent(it) },
                        onDeleteEvent = { vm.deleteEvent(it) }
                    )
                }
            }

            // 4. Diagnostics
            item {
                CollapsibleSection(title = "Diagnostics") {
                    DiagnosticsSection(
                        diagnostics = uiState.diagnostics,
                        onToggle = { vm.toggleDiagnostic(it) }
                    )
                }
            }

            // 5. Emotional Truth & Reflection
            item {
                CollapsibleSection(title = "Reflection") {
                    ReflectionSection(
                        journal = uiState.journal ?: DailyJournal(date = selectedDate),
                        onUpdate = { vm.saveJournal(it) }
                    )
                }
            }

            // 6. Action Fixes
            item {
                CollapsibleSection(title = "Action Fixes") {
                    ActionFixesSection(
                        fixes = uiState.actionFixes,
                        onAddFix = { p, f -> vm.addActionFix(p, f) },
                        onDeleteFix = { vm.deleteActionFix(it) }
                    )
                }
            }

            // 7. Tomorrow Priorities
            item {
                CollapsibleSection(title = "Tomorrow Top 3") {
                    TomorrowPrioritiesSection(
                        journalId = uiState.journal?.id ?: 0,
                        priorities = uiState.priorities,
                        onSave = { vm.savePriority(it) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { vm.saveJournal(uiState.journal?.copy(isComplete = true) ?: return@Button) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = DarkBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Complete Day")
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun JournalHeader(
    selectedDate: LocalDate,
    onBack: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .padding(top = Spacing.md)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextPrimary)
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "DAILY JOURNAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = Accent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
                )
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(onClick = onToday) {
                Icon(Icons.Rounded.History, "Today", tint = Accent, modifier = Modifier.size(20.dp))
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onPrevious,
                colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Yesterday", style = MaterialTheme.typography.labelMedium)
            }
            
            TextButton(
                onClick = onNext,
                colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
            ) {
                Text("Tomorrow", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, modifier = Modifier.size(16.dp))
            }
        }
        HorizontalDivider(color = GlassBorder, thickness = 1.dp)
    }
}

@Composable
private fun CollapsibleSection(
    title: String,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground.copy(alpha = 0.6f))
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = Accent.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
                letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
            )
            Icon(
                imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
        
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm)
            ) {
                content()
                Spacer(modifier = Modifier.height(Spacing.md))
            }
        }
    }
}

@Composable
private fun DailySnapshotSection(
    journal: DailyJournal,
    onUpdate: (DailyJournal) -> Unit
) {
    var editingMetric by remember { mutableStateOf<Pair<String, Float>?>(null) }
    var metricKey by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        MetricCard(
            label = "Sleep",
            value = journal.sleepHours,
            icon = Icons.Rounded.NightsStay,
            modifier = Modifier.weight(1f),
            onClick = { 
                editingMetric = "Sleep" to journal.sleepHours
                metricKey = "sleep"
            }
        )
        MetricCard(
            label = "Deep Work",
            value = journal.deepWorkHours,
            icon = Icons.Rounded.Psychology,
            modifier = Modifier.weight(1f),
            onClick = { 
                editingMetric = "Deep Work" to journal.deepWorkHours
                metricKey = "work"
            }
        )
        MetricCard(
            label = "Screen",
            value = journal.screenTimeHours,
            icon = Icons.Rounded.Smartphone,
            modifier = Modifier.weight(1f),
            onClick = { 
                editingMetric = "Screen Time" to journal.screenTimeHours
                metricKey = "screen"
            }
        )
    }

    editingMetric?.let { (label, value) ->
        MetricEditDialog(
            label = label,
            initialValue = value,
            onDismiss = { editingMetric = null },
            onConfirm = { newValue ->
                when (metricKey) {
                    "sleep" -> onUpdate(journal.copy(sleepHours = newValue))
                    "work" -> onUpdate(journal.copy(deepWorkHours = newValue))
                    "screen" -> onUpdate(journal.copy(screenTimeHours = newValue))
                }
                editingMetric = null
            }
        )
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val (h, m) = floatToTime(value)
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.md, horizontal = Spacing.xs),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Accent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted, maxLines = 1)
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = "%02d:%02d".format(h, m),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text("hrs", style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
    }
}

@Composable
private fun MetricEditDialog(
    label: String,
    initialValue: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var currentValue by remember { mutableStateOf(initialValue) }
    var isManual by remember { mutableStateOf(false) }
    val (h, m) = floatToTime(currentValue)

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        confirmButton = {
            TextButton(onClick = { onConfirm(currentValue) }) {
                Text("SAVE", color = Accent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TextMuted)
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(48.dp)) // Balance the icon
                Text(
                    text = "Edit $label",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                IconButton(onClick = { isManual = !isManual }) {
                    Icon(
                        if (isManual) Icons.Rounded.History else Icons.Rounded.Add, 
                        contentDescription = "Toggle Input Mode",
                        tint = Accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isManual) {
                        var editH by remember { mutableStateOf(h.toString()) }
                        var editM by remember { mutableStateOf(m.toString()) }
                        
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BasicTextField(
                                value = editH,
                                onValueChange = { 
                                    if (it.length <= 2) {
                                        editH = it
                                        currentValue = timeToFloat(it.toIntOrNull() ?: 0, m)
                                    }
                                },
                                modifier = Modifier.width(60.dp).background(DarkSurface, RoundedCornerShape(12.dp)).padding(vertical = 12.dp),
                                textStyle = MaterialTheme.typography.headlineMedium.copy(color = TextPrimary, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                cursorBrush = SolidColor(Accent)
                            )
                            Text(":", style = MaterialTheme.typography.headlineLarge, color = TextMuted)
                            BasicTextField(
                                value = editM,
                                onValueChange = { 
                                    if (it.length <= 2) {
                                        editM = it
                                        currentValue = timeToFloat(h, it.toIntOrNull() ?: 0)
                                    }
                                },
                                modifier = Modifier.width(60.dp).background(DarkSurface, RoundedCornerShape(12.dp)).padding(vertical = 12.dp),
                                textStyle = MaterialTheme.typography.headlineMedium.copy(color = TextPrimary, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                cursorBrush = SolidColor(Accent)
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            VerticalWheelPicker(
                                range = 0..23,
                                selectedValue = h,
                                onValueSelected = { currentValue = timeToFloat(it, m) },
                                modifier = Modifier.width(60.dp)
                            )
                            Text(":", style = MaterialTheme.typography.headlineLarge, color = TextMuted, modifier = Modifier.padding(horizontal = 8.dp))
                            VerticalWheelPicker(
                                range = 0..59,
                                selectedValue = m,
                                step = 5,
                                onValueSelected = { currentValue = timeToFloat(h, it) },
                                modifier = Modifier.width(60.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    if (isManual) "Tap icon to use scroll" else "Tap icon to type manually",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted.copy(alpha = 0.6f)
                )
            }
        },
        containerColor = DarkBackground,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .border(1.dp, GlassBorder, RoundedCornerShape(28.dp))
    )
}

// Wheel Picker and Input logic moved to Dialog

@Composable
private fun VerticalWheelPicker(
    range: IntRange,
    selectedValue: Int,
    onValueSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    step: Int = 1
) {
    val items = remember(range, step) { range.filter { it % step == 0 } }
    val virtualCount = 10000 // Large multiple for infinite feel
    val totalVirtualItems = items.size * virtualCount
    
    // Find initial index in the middle of the virtual range
    val initialRealIndex = items.indexOfFirst { it >= selectedValue }.coerceAtLeast(0)
    val initialVirtualIndex = (totalVirtualItems / 2) - ((totalVirtualItems / 2) % items.size) + initialRealIndex
    
    val listState = rememberLazyListState(initialVirtualIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    
    // Track center item
    val centerIndex by remember {
        derivedStateOf { 
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) 0
            else {
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                visibleItemsInfo.minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - viewportCenter) }?.index ?: 0
            }
        }
    }
    
    LaunchedEffect(centerIndex) {
        val realIndex = centerIndex % items.size
        if (realIndex in items.indices) {
            onValueSelected(items[realIndex])
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
        }
    }

    LazyColumn(
        state = listState,
        flingBehavior = snapFlingBehavior,
        modifier = modifier.height(120.dp),
        contentPadding = PaddingValues(vertical = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(totalVirtualItems) { index ->
            val realIndex = index % items.size
            val itemValue = items[realIndex]
            
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .graphicsLayer {
                        val layoutInfo = listState.layoutInfo
                        val visibleItem = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
                        if (visibleItem != null) {
                            val viewportCenter = (layoutInfo.viewportEndOffset + layoutInfo.viewportStartOffset) / 2f
                            val itemCenter = visibleItem.offset + visibleItem.size / 2f
                            val distance = itemCenter - viewportCenter
                            val fraction = (distance / (layoutInfo.viewportEndOffset / 2f)).coerceIn(-1f, 1f)
                            
                            rotationX = -fraction * 60f
                            scaleX = 1f - kotlin.math.abs(fraction) * 0.2f
                            scaleY = 1f - kotlin.math.abs(fraction) * 0.2f
                            alpha = (1f - kotlin.math.abs(fraction) * 0.7f).coerceIn(0f, 1f)
                            cameraDistance = 8 * density
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "%02d".format(itemValue),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StateSection(
    journalId: Long,
    slices: List<JournalStateSlice>,
    onUpdate: (JournalStateSlice) -> Unit
) {
    val periods = listOf(DayPeriod.MORNING, DayPeriod.AFTERNOON, DayPeriod.EVENING)
    
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        periods.forEach { period ->
            val slice = slices.find { it.period == period } ?: JournalStateSlice(journalId = journalId, period = period)
            Column {
                Text(period.name, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Spacer(modifier = Modifier.height(Spacing.xs))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    EnergyChip(label = "Drained", isSelected = slice.energy == EnergyLevel.DRAINED) { onUpdate(slice.copy(energy = EnergyLevel.DRAINED)) }
                    EnergyChip(label = "Low", isSelected = slice.energy == EnergyLevel.LOW) { onUpdate(slice.copy(energy = EnergyLevel.LOW)) }
                    EnergyChip(label = "Stable", isSelected = slice.energy == EnergyLevel.NEUTRAL) { onUpdate(slice.copy(energy = EnergyLevel.NEUTRAL)) }
                    EnergyChip(label = "High", isSelected = slice.energy == EnergyLevel.HIGH) { onUpdate(slice.copy(energy = EnergyLevel.HIGH)) }
                }
            }
        }
    }
}

@Composable
private fun EnergyChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Accent.copy(alpha = 0.2f) else DarkSurface)
            .border(
                1.dp, 
                if (isSelected) Accent.copy(alpha = 0.5f) else Color.Transparent, 
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label, 
            style = MaterialTheme.typography.labelSmall, 
            color = if (isSelected) Accent else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun TimelineSection(
    events: List<JournalEvent>,
    onAddEvent: (String) -> Unit,
    onDeleteEvent: (JournalEvent) -> Unit
) {
    var newEventText by remember { mutableStateOf("") }
    
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        events.forEach { event ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(6.dp).background(Accent, CircleShape))
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(event.text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                }
                IconButton(onClick = { onDeleteEvent(event) }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Rounded.Delete, null, tint = TextSubtle, modifier = Modifier.size(16.dp))
                }
            }
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            modifier = Modifier.padding(top = Spacing.sm)
        ) {
            PremiumTextField(
                value = newEventText,
                onValueChange = { newEventText = it },
                modifier = Modifier.weight(1f),
                placeholder = "What happened?",
                singleLine = true
            )
            IconButton(
                onClick = { if (newEventText.isNotBlank()) { onAddEvent(newEventText); newEventText = "" } },
                modifier = Modifier.size(40.dp).background(AccentContainer, RoundedCornerShape(10.dp))
            ) {
                Icon(Icons.Rounded.Add, null, tint = Accent)
            }
        }
    }
}

@Composable
private fun DiagnosticsSection(
    diagnostics: List<BehaviorDiagnostic>,
    onToggle: (BehaviorDiagnostic) -> Unit
) {
    val categories = DiagnosticCategory.entries
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        categories.forEach { category ->
            Text(category.name, style = MaterialTheme.typography.labelSmall, color = Accent)
            // Simplified diagnostics - usually we'd have a fixed list of keys per category
            val keys = when(category) {
                DiagnosticCategory.COGNITIVE -> listOf("Brain Fog", "Overthought", "Indecisive")
                DiagnosticCategory.EMOTIONAL -> listOf("Anxious", "Irritable", "Low Mood")
                DiagnosticCategory.BEHAVIORAL -> listOf("Procrastinated", "Avoided Work", "Distracted")
                DiagnosticCategory.ENERGY -> listOf("Energy Crash", "Physically Tired", "Sleepy")
            }
            
            keys.forEach { key ->
                val diag = diagnostics.find { it.category == category && it.key == key } ?: BehaviorDiagnostic(journalId = 0, category = category, key = key)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggle(diag) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = diag.isChecked,
                        onCheckedChange = { onToggle(diag) },
                        colors = CheckboxDefaults.colors(checkedColor = Accent)
                    )
                    Text(key, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun ReflectionSection(
    journal: DailyJournal,
    onUpdate: (DailyJournal) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        ReflectionField(label = "Best part of day", value = journal.bestPart) { onUpdate(journal.copy(bestPart = it)) }
        ReflectionField(label = "Worst part of day", value = journal.worstPart) { onUpdate(journal.copy(worstPart = it)) }
        ReflectionField(label = "What went well?", value = journal.whatWentWell) { onUpdate(journal.copy(whatWentWell = it)) }
        ReflectionField(label = "What went badly?", value = journal.whatWentBadly) { onUpdate(journal.copy(whatWentBadly = it)) }
        ReflectionField(label = "Pattern noticed", value = journal.patternNoticed) { onUpdate(journal.copy(patternNoticed = it)) }
    }
}

@Composable
private fun ReflectionField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(vertical = Spacing.xs)) {
        Text(
            text = label, 
            style = MaterialTheme.typography.labelSmall, 
            color = TextSecondary,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        PremiumTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = "Type your thoughts..."
        )
    }
}

@Composable
private fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    singleLine: Boolean = false
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface),
        placeholder = { Text(placeholder, color = TextMuted, style = MaterialTheme.typography.bodyMedium) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = DarkSurface,
            unfocusedContainerColor = DarkSurface,
            disabledContainerColor = DarkSurface,
            focusedIndicatorColor = Accent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Accent
        ),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
        singleLine = singleLine
    )
}

@Composable
private fun ActionFixesSection(
    fixes: List<ActionFix>,
    onAddFix: (String, String) -> Unit,
    onDeleteFix: (ActionFix) -> Unit
) {
    var problem by remember { mutableStateOf("") }
    var fixText by remember { mutableStateOf("") }
    
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        fixes.forEach { fix ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface.copy(alpha = 0.5f))
                    .padding(Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("PROBLEM", style = MaterialTheme.typography.labelSmall, color = TextSubtle, fontWeight = FontWeight.Bold)
                    Text(fix.problem, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("FIX", style = MaterialTheme.typography.labelSmall, color = Accent.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                    Text(fix.fix, style = MaterialTheme.typography.bodySmall, color = Accent)
                }
                IconButton(onClick = { onDeleteFix(fix) }) {
                    Icon(Icons.Rounded.Delete, null, tint = Error.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                }
            }
        }
        
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            PremiumTextField(value = problem, onValueChange = { problem = it }, placeholder = "Describe the friction point...")
            PremiumTextField(value = fixText, onValueChange = { fixText = it }, placeholder = "Proposed behavioral fix...")
            Button(
                onClick = { if (problem.isNotBlank()) { onAddFix(problem, fixText); problem = ""; fixText = "" } },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Accent.copy(alpha = 0.1f), contentColor = Accent),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Action Fix")
            }
        }
    }
}

@Composable
private fun TomorrowPrioritiesSection(
    journalId: Long,
    priorities: List<TomorrowPriority>,
    onSave: (TomorrowPriority) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        (1..3).forEach { slot ->
            val priority = priorities.find { it.slot == slot } ?: TomorrowPriority(journalId = journalId, slot = slot, text = "")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AccentContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = slot.toString(),
                        color = Accent,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.md))
                PremiumTextField(
                    value = priority.text,
                    onValueChange = { onSave(priority.copy(text = it)) },
                    placeholder = "Focus #$slot for tomorrow...",
                    singleLine = true
                )
            }
        }
    }
}

@Composable
private fun MetricInput(
    label: String,
    value: String,
    unit: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.width(60.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DarkBackground,
                    unfocusedContainerColor = DarkBackground,
                    focusedIndicatorColor = Accent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text(unit, color = TextMuted, style = MaterialTheme.typography.labelSmall)
        }
    }
}
