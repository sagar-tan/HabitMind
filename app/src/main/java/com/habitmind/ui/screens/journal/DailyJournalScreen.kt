package com.habitmind.ui.screens.journal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitmind.data.database.entity.*
import com.habitmind.ui.theme.*
import com.habitmind.ui.viewmodel.DailyJournalViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

            // 2. State (Morning/Afternoon/Evening)
            item {
                CollapsibleSection(title = "Energy & Mood") {
                    StateSection(
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
                        onAddEvent = { vm.addEvent(it) }
                    )
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
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Daily Journal",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Accent
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Prev", tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
            TextButton(onClick = onToday) {
                Text("Today", color = Accent, style = MaterialTheme.typography.labelLarge)
            }
            IconButton(onClick = onNext) {
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, "Next", tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
        }
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
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
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
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        MetricInput(
            label = "Sleep Hours",
            value = journal.sleepHours.toString(),
            unit = "hrs",
            onValueChange = { onUpdate(journal.copy(sleepHours = it.toFloatOrNull() ?: 0f)) }
        )
        
        MetricInput(
            label = "Deep Work",
            value = journal.deepWorkHours.toString(),
            unit = "hrs",
            onValueChange = { onUpdate(journal.copy(deepWorkHours = it.toFloatOrNull() ?: 0f)) }
        )
    }
}

@Composable
private fun StateSection(
    slices: List<JournalStateSlice>,
    onUpdate: (JournalStateSlice) -> Unit
) {
    // Placeholder for state snapshots
    Text("State tracking coming soon...", color = TextMuted, style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun TimelineSection(
    events: List<JournalEvent>,
    onAddEvent: (String) -> Unit
) {
    // Placeholder for timeline
    Text("Timeline bullets coming soon...", color = TextMuted, style = MaterialTheme.typography.bodySmall)
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
