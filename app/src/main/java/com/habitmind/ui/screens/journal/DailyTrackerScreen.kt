package com.habitmind.ui.screens.journal

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.habitmind.data.database.entity.DailyTracker
import com.habitmind.ui.theme.Accent
import com.habitmind.ui.theme.CardBackground
import com.habitmind.ui.theme.DarkBackground
import com.habitmind.ui.theme.GlassBorder
import com.habitmind.ui.theme.ProgressActive
import com.habitmind.ui.theme.ProgressTrack
import com.habitmind.ui.theme.Spacing
import com.habitmind.ui.theme.TextMuted
import com.habitmind.ui.theme.TextPrimary
import com.habitmind.ui.theme.TextSecondary
import com.habitmind.ui.viewmodel.DailyTrackerViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Notion-style Daily Tracker Screen
 * Combined viewer + editor with auto-save
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyTrackerScreen(
    dateString: String? = null,
    onNavigateBack: () -> Unit,
    vm: DailyTrackerViewModel = viewModel()
) {
    val haptic = LocalHapticFeedback.current
    val selectedDate by vm.selectedDate.collectAsState()
    val tracker by vm.tracker.collectAsState()
    
    // Parse incoming date or use today
    LaunchedEffect(dateString) {
        dateString?.let {
            try { vm.selectDate(LocalDate.parse(it)) } catch (_: Exception) {}
        }
        vm.ensureTrackerExists()
    }
    
    // Also ensure tracker exists when date changes
    LaunchedEffect(selectedDate) {
        vm.ensureTrackerExists()
    }
    
    // Local state mirrors the tracked entity for responsive editing
    val t = tracker ?: DailyTracker(date = selectedDate)
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = Spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ─── Header with date nav ───
        item {
            Spacer(Modifier.height(Spacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextPrimary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Discipline Update",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "@${selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Accent
                    )
                }
            }
            
            // Date navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { vm.previousDay() }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Previous", tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardBackground)
                        .clickable { vm.goToToday() }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Today", style = MaterialTheme.typography.labelMedium, color = Accent)
                }
                IconButton(onClick = { vm.nextDay() }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, "Next", tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
            }
        }
        
        // ─── Discipline Score Ring ───
        item {
            val score = t.computeDisciplineScore()
            val animatedScore by animateFloatAsState(
                targetValue = score / 10f,
                animationSpec = tween(800),
                label = "scoreAnim"
            )
            val scoreColor = when {
                score >= 8 -> Color(0xFF4CAF50)
                score >= 5 -> Accent
                else -> Color(0xFFFF5722)
            }
            
            SectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Discipline Score", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                        Text(
                            text = "$score / 10",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = scoreColor
                        )
                    }
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
                        CircularProgressIndicator(
                            progress = { animatedScore },
                            modifier = Modifier.size(56.dp),
                            color = scoreColor,
                            trackColor = ProgressTrack,
                            strokeWidth = 5.dp,
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            text = "$score",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = scoreColor
                        )
                    }
                }
            }
        }
        
        // ─── Checkboxes Section ───
        item {
            SectionCard {
                SectionHeader("Daily Habits")
                CheckboxRow("🧘 Meditation", t.meditation) {
                    vm.save(t.copy(meditation = it)); haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                CheckboxRow("🥗 No Junk Food", t.noJunkFood) {
                    vm.save(t.copy(noJunkFood = it)); haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                CheckboxRow("🔇 No Music", t.noMusic) {
                    vm.save(t.copy(noMusic = it)); haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                CheckboxRow("📵 No Mbt", t.noMbt) {
                    vm.save(t.copy(noMbt = it)); haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                CheckboxRow("💪 Workout", t.workout) {
                    vm.save(t.copy(workout = it)); haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
        }
        
        // ─── Rating Sliders (1-10) ───
        item {
            SectionCard {
                SectionHeader("How You Feel")
                RatingSlider("⚡ Energy", t.energy, Color(0xFF4CAF50)) {
                    vm.save(t.copy(energy = it))
                }
                RatingSlider("🎯 Focus", t.focus, Color(0xFF2196F3)) {
                    vm.save(t.copy(focus = it))
                }
                RatingSlider("😊 Mood", t.mood, Color(0xFFFF9800)) {
                    vm.save(t.copy(mood = it))
                }
                RatingSlider("😤 Stress", t.stress, Color(0xFFF44336)) {
                    vm.save(t.copy(stress = it))
                }
            }
        }
        
        // ─── Numeric Inputs ───
        item {
            SectionCard {
                SectionHeader("Metrics")
                NumericRow("📱 ScreenTime", t.screenTimeHours, "hrs") {
                    vm.save(t.copy(screenTimeHours = it))
                }
                NumericRow("😴 Sleep", t.sleepHours, "hrs") {
                    vm.save(t.copy(sleepHours = it))
                }
                NumericRow("📲 Social Media", t.socialMediaMin.toFloat(), "min") {
                    vm.save(t.copy(socialMediaMin = it.toInt()))
                }
                NumericRow("💧 Water Intake", t.waterIntakeLiters, "L") {
                    vm.save(t.copy(waterIntakeLiters = it))
                }
                NumericRow("📚 Study/Work", t.studyWorkHours, "hrs") {
                    vm.save(t.copy(studyWorkHours = it))
                }
            }
        }
        
        // ─── Workout Section ───
        item {
            SectionCard {
                SectionHeader("Workout")
                
                // Type selector
                val types = listOf("Walk", "Run", "Gym", "Yoga", "Swim", "Other")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    types.forEach { type ->
                        val selected = t.workoutType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) Accent.copy(0.2f) else CardBackground)
                                .border(1.dp, if (selected) Accent.copy(0.4f) else GlassBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    vm.save(t.copy(workoutType = if (selected) "" else type))
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(type, style = MaterialTheme.typography.labelMedium, color = if (selected) Accent else TextSecondary)
                        }
                    }
                }
                
                NumericRow("⏱ Duration", t.workoutDurationMin.toFloat(), "min") {
                    vm.save(t.copy(workoutDurationMin = it.toInt()))
                }
            }
        }
        
        // ─── Today's Pics ───
        item {
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                uri?.let { vm.addPhoto(it.toString()) }
            }
            
            SectionCard {
                SectionHeader("Today's Pic")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    t.photoList.forEach { path ->
                        Box(modifier = Modifier.size(72.dp)) {
                            AsyncImage(
                                model = Uri.parse(path),
                                contentDescription = "Photo",
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                            // Remove button
                            Icon(
                                Icons.Rounded.Close,
                                "Remove",
                                tint = Color.White,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(0.6f))
                                    .clickable { vm.removePhoto(path) }
                                    .padding(2.dp)
                            )
                        }
                    }
                    
                    // Add photo button
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CardBackground)
                            .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Add, "Add Photo", tint = TextMuted)
                    }
                }
            }
        }
        
        // ─── Text Fields ───
        item {
            SectionCard {
                SectionHeader("Reflection")
                TextArea("🙏 Gratitude", t.gratitude) { vm.save(t.copy(gratitude = it)) }
                Spacer(Modifier.height(12.dp))
                TextArea("🏆 Win of the Day", t.winOfTheDay) { vm.save(t.copy(winOfTheDay = it)) }
                Spacer(Modifier.height(12.dp))
                TextArea("📝 Notes & Emotions", t.notesEmotions) { vm.save(t.copy(notesEmotions = it)) }
            }
        }
        
        item { Spacer(Modifier.height(100.dp)) }
    }
}

// ═══════════════════════════════════════════════
// Reusable composables for the tracker
// ═══════════════════════════════════════════════

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    listOf(CardBackground, CardBackground.copy(alpha = 0.95f))
                )
            )
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        content()
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = TextMuted,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun CheckboxRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    val bgColor by animateColorAsState(
        targetValue = if (checked) Accent.copy(alpha = 0.1f) else Color.Transparent,
        label = "cbBg"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable { onToggle(!checked) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (checked) Accent else Color.Transparent)
                .border(
                    2.dp,
                    if (checked) Accent else TextMuted.copy(0.4f),
                    RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Text("✓", color = DarkBackground, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RatingSlider(label: String, value: Int, color: Color, onChange: (Int) -> Unit) {
    var localValue by remember(value) { mutableIntStateOf(value) }
    
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            Text(
                "$localValue",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
        Slider(
            value = localValue.toFloat(),
            onValueChange = { localValue = it.toInt() },
            onValueChangeFinished = { onChange(localValue) },
            valueRange = 0f..10f,
            steps = 9,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = ProgressTrack
            )
        )
    }
}

@Composable
private fun NumericRow(label: String, value: Float, suffix: String, onChange: (Float) -> Unit) {
    var text by remember(value) {
        mutableStateOf(if (value == 0f) "" else if (value == value.toLong().toFloat()) value.toLong().toString() else value.toString())
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BasicTextField(
                value = text,
                onValueChange = { newVal ->
                    // Allow digits and one decimal point
                    val filtered = newVal.filter { c -> c.isDigit() || c == '.' }
                    if (filtered.count { it == '.' } <= 1) {
                        text = filtered
                        filtered.toFloatOrNull()?.let { onChange(it) }
                    }
                },
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                ),
                cursorBrush = SolidColor(Accent),
                singleLine = true,
                modifier = Modifier
                    .width(70.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkBackground)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                decorationBox = { inner ->
                    if (text.isEmpty()) {
                        Text("0", style = TextStyle(color = TextMuted, fontSize = 16.sp, textAlign = TextAlign.End), modifier = Modifier.fillMaxWidth())
                    }
                    inner()
                }
            )
            Text(suffix, style = MaterialTheme.typography.labelMedium, color = TextMuted)
        }
    }
}

@Composable
private fun TextArea(label: String, value: String, onChange: (String) -> Unit) {
    var text by remember(value) { mutableStateOf(value) }
    
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = TextPrimary)
        BasicTextField(
            value = text,
            onValueChange = {
                text = it
                onChange(it)
            },
            textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp, lineHeight = 20.sp),
            cursorBrush = SolidColor(Accent),
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkBackground)
                .padding(12.dp),
            decorationBox = { inner ->
                if (text.isEmpty()) {
                    Text("Tap to write...", style = TextStyle(color = TextMuted, fontSize = 14.sp))
                }
                inner()
            }
        )
    }
}
