package com.habitmind.ui.screens.dialogs

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.habitmind.data.database.entity.JournalEntryType
import com.habitmind.ui.theme.Accent
import com.habitmind.ui.theme.CardBackground
import com.habitmind.ui.theme.GlassBorder
import com.habitmind.ui.theme.GlassSurface
import com.habitmind.ui.theme.Motion
import com.habitmind.ui.theme.Spacing
import com.habitmind.ui.theme.TextMuted
import com.habitmind.ui.theme.TextPrimary
import com.habitmind.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun AddJournalDialog(
    onDismiss: () -> Unit,
    onConfirm: (content: String, type: JournalEntryType, tags: String?, mood: String?) -> Unit,
    onVoiceRecord: () -> Unit = {},
    onAddPhoto: () -> Unit = {},
    selectedImageUri: Uri? = null,
    isRecording: Boolean = false,
    voiceRecordingPath: String? = null
) {
    var isVisible by remember { mutableStateOf(false) }
    var journalContent by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(JournalEntryType.TEXT) }
    var tags by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        isVisible = true
        delay(100)
        try {
            focusRequester.requestFocus()
        } catch (e: Exception) {
            // Focus request may fail, ignore
        }
    }
    
    // Auto-detect type from media state
    LaunchedEffect(selectedImageUri) {
        if (selectedImageUri != null) selectedType = JournalEntryType.IMAGE
    }
    LaunchedEffect(isRecording, voiceRecordingPath) {
        if (isRecording || voiceRecordingPath != null) selectedType = JournalEntryType.VOICE
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(Motion.SCREEN, easing = FastOutSlowInEasing)) +
                    scaleIn(initialScale = 0.9f, animationSpec = tween(Motion.SCREEN, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(Motion.SCREEN_FAST)) +
                   scaleOut(targetScale = 0.9f, animationSpec = tween(Motion.SCREEN_FAST))
        ) {
            Column(
                modifier = Modifier
                    .padding(Spacing.xl)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(GlassSurface, GlassSurface.copy(alpha = 0.98f))
                        )
                    )
                    .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { }
                    )
                    .padding(Spacing.xl)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Journal Entry",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(Spacing.lg))
                
                // Entry type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    JournalTypeChip(
                        icon = Icons.Outlined.Book,
                        label = "Text",
                        isSelected = selectedType == JournalEntryType.TEXT,
                        onClick = { selectedType = JournalEntryType.TEXT },
                        modifier = Modifier.weight(1f)
                    )
                    JournalTypeChip(
                        icon = if (isRecording) Icons.Outlined.Stop else Icons.Outlined.Mic,
                        label = if (isRecording) "Stop" else "Voice",
                        isSelected = selectedType == JournalEntryType.VOICE || isRecording,
                        onClick = { 
                            selectedType = JournalEntryType.VOICE
                            onVoiceRecord()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    JournalTypeChip(
                        icon = Icons.Outlined.Photo,
                        label = "Photo",
                        isSelected = selectedType == JournalEntryType.IMAGE,
                        onClick = { 
                            selectedType = JournalEntryType.IMAGE
                            onAddPhoto()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(Spacing.md))
                
                // Media indicators
                if (isRecording) {
                    RecordingIndicator()
                    Spacer(modifier = Modifier.height(Spacing.md))
                } else if (voiceRecordingPath != null && !isRecording) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Accent.copy(alpha = 0.1f))
                            .border(1.dp, Accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            text = "Voice recording attached",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Accent
                        )
                    }
                    Spacer(modifier = Modifier.height(Spacing.md))
                }
                
                if (selectedImageUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Small badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Accent.copy(alpha = 0.9f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "✓ Attached",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.md))
                }
                
                // Content input
                Text(
                    text = if (selectedType == JournalEntryType.VOICE) "Add a caption (optional)" 
                           else "What's on your mind?",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                BasicTextField(
                    value = journalContent,
                    onValueChange = { journalContent = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (selectedImageUri != null || voiceRecordingPath != null) 80.dp else 150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                        .padding(Spacing.md)
                        .focusRequester(focusRequester),
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                    ),
                    cursorBrush = SolidColor(Accent),
                    decorationBox = { innerTextField ->
                        Box {
                            if (journalContent.isEmpty()) {
                                Text(
                                    text = "Start writing your thoughts...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextMuted
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(Spacing.md))
                
                // Tags input
                Text(
                    text = "Tags (optional)",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                BasicTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                        .padding(Spacing.md),
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    ),
                    cursorBrush = SolidColor(Accent),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box {
                            if (tags.isEmpty()) {
                                Text(
                                    text = "gratitude, reflection, goals",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(Spacing.xl))
                
                // Confirm button - allow save with media even if text is empty
                val hasContent = journalContent.isNotBlank() || selectedImageUri != null || voiceRecordingPath != null
                Button(
                    onClick = {
                        if (hasContent) {
                            onConfirm(
                                journalContent.trim().ifEmpty { 
                                    when (selectedType) {
                                        JournalEntryType.VOICE -> "Voice note"
                                        JournalEntryType.IMAGE -> "Photo entry"
                                        else -> journalContent.trim()
                                    }
                                },
                                selectedType,
                                tags.ifBlank { null },
                                null
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Accent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = hasContent && !isRecording
                ) {
                    Text(
                        text = "Save Entry",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun RecordingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "recording")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF991B1B).copy(alpha = 0.15f))
            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(Color(0xFFEF4444))
        )
        Spacer(modifier = Modifier.width(Spacing.sm))
        Text(
            text = "Recording... Tap Voice to stop",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFEF4444)
        )
    }
}

@Composable
fun JournalTypeChip(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) Accent.copy(alpha = 0.15f)
                else CardBackground
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Accent.copy(alpha = 0.3f) else GlassBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Accent else TextSecondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.size(Spacing.xs))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) TextPrimary else TextSecondary
        )
    }
}
