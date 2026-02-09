package com.habitmind.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.habitmind.HabitMindApplication
import com.habitmind.data.export.DataExportManager
import com.habitmind.data.export.ExportResult
import com.habitmind.data.media.MediaStorageHelper
import com.habitmind.notification.NotificationScheduler
import com.habitmind.ui.theme.Accent
import com.habitmind.ui.theme.CardBackground
import com.habitmind.ui.theme.DarkBackground
import com.habitmind.ui.theme.GlassBorder
import com.habitmind.ui.theme.GlassSurface
import com.habitmind.ui.theme.Motion
import com.habitmind.ui.theme.Spacing
import com.habitmind.ui.theme.Success
import com.habitmind.ui.theme.TextMuted
import com.habitmind.ui.theme.TextPrimary
import com.habitmind.ui.theme.TextSecondary
import com.habitmind.ui.theme.Warning
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as HabitMindApplication
    val preferences = app.userPreferences
    val scope = rememberCoroutineScope()
    
    val userName by preferences.userName.collectAsState(initial = "User")
    val isDarkMode by preferences.isDarkMode.collectAsState(initial = true)
    val notificationsEnabled by preferences.notificationsEnabled.collectAsState(initial = true)
    
    var isVisible by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showDeleteDataDialog by remember { mutableStateOf(false) }
    var exportStatus by remember { mutableStateOf<String?>(null) }
    var isExporting by remember { mutableStateOf(false) }
    
    val mediaHelper = remember { MediaStorageHelper(context) }
    val exportManager = remember { DataExportManager(context) }
    
    val storageUsed = remember { mediaHelper.formatStorageSize(mediaHelper.getTotalStorageUsed()) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(Motion.SCREEN, easing = FastOutSlowInEasing)),
        exit = fadeOut(tween(Motion.SCREEN_FAST))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = Spacing.sm)
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            // Profile Section
            SettingsSection(title = "Profile") {
                SettingsItem(
                    icon = Icons.Outlined.Person,
                    title = "Display Name",
                    subtitle = userName ?: "Not set",
                    onClick = { /* TODO: Name edit dialog */ }
                )
            }
            
            // Preferences Section
            SettingsSection(title = "Preferences") {
                SettingsToggle(
                    icon = Icons.Outlined.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Use dark theme",
                    isChecked = isDarkMode,
                    onToggle = { enabled ->
                        scope.launch {
                            preferences.setDarkMode(enabled)
                        }
                    }
                )
                
                SettingsToggle(
                    icon = Icons.Outlined.Notifications,
                    title = "Notifications",
                    subtitle = "Habit reminders & weekly reviews",
                    isChecked = notificationsEnabled,
                    onToggle = { enabled ->
                        scope.launch {
                            preferences.setNotificationsEnabled(enabled)
                            if (enabled) {
                                NotificationScheduler.initializeNotifications(context)
                            } else {
                                NotificationScheduler.cancelAll(context)
                            }
                        }
                    }
                )
            }
            
            // Data Section
            SettingsSection(title = "Data & Storage") {
                SettingsItem(
                    icon = Icons.Outlined.Storage,
                    title = "Storage Used",
                    subtitle = storageUsed,
                    onClick = { }
                )
                
                SettingsItem(
                    icon = Icons.Outlined.CloudUpload,
                    title = "Export Data",
                    subtitle = "Create a backup of all your data",
                    onClick = { showExportDialog = true }
                )
                
                SettingsItem(
                    icon = Icons.Outlined.CloudDownload,
                    title = "Import Data",
                    subtitle = "Restore from a backup",
                    onClick = { /* TODO: File picker */ }
                )
                
                SettingsItem(
                    icon = Icons.Outlined.Delete,
                    title = "Delete All Data",
                    subtitle = "Permanently remove all data",
                    tint = Warning,
                    onClick = { showDeleteDataDialog = true }
                )
            }
            
            // About Section
            SettingsSection(title = "About") {
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    title = "Version",
                    subtitle = "1.0.0",
                    onClick = { }
                )
                
                SettingsItem(
                    icon = Icons.AutoMirrored.Outlined.HelpOutline,
                    title = "Help & Feedback",
                    subtitle = "Get help or share feedback",
                    onClick = { }
                )
            }
            
            Spacer(modifier = Modifier.height(120.dp)) // Bottom padding for nav bar
        }
    }
    
    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!isExporting) showExportDialog = false 
            },
            title = { Text("Export Data", color = TextPrimary) },
            text = {
                Column {
                    if (isExporting) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Accent
                            )
                            Text("Exporting your data...", color = TextSecondary)
                        }
                    } else if (exportStatus != null) {
                        Text(exportStatus!!, color = if (exportStatus!!.startsWith("Success")) Success else Warning)
                    } else {
                        Text(
                            "This will create a JSON backup of all your habits, tasks, and journal entries.",
                            color = TextSecondary
                        )
                    }
                }
            },
            confirmButton = {
                if (!isExporting && exportStatus == null) {
                    Button(
                        onClick = {
                            isExporting = true
                            scope.launch {
                                val result = exportManager.exportAllData()
                                exportStatus = when (result) {
                                    is ExportResult.Success -> "Success! Saved to ${result.filePath}"
                                    is ExportResult.Error -> "Error: ${result.message}"
                                }
                                isExporting = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Accent)
                    ) {
                        Text("Export")
                    }
                } else if (exportStatus != null) {
                    TextButton(onClick = { 
                        showExportDialog = false 
                        exportStatus = null
                    }) {
                        Text("Done")
                    }
                }
            },
            dismissButton = {
                if (!isExporting && exportStatus == null) {
                    TextButton(onClick = { showExportDialog = false }) {
                        Text("Cancel")
                    }
                }
            },
            containerColor = GlassSurface
        )
    }
    
    // Delete Data Dialog
    if (showDeleteDataDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDataDialog = false },
            title = { Text("Delete All Data?", color = Warning) },
            text = {
                Text(
                    "This will permanently delete all your habits, tasks, journal entries, and settings. This action cannot be undone.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // TODO: Implement data deletion
                        showDeleteDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Warning)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDataDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = GlassSurface
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = Accent,
            modifier = Modifier.padding(bottom = Spacing.sm)
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(GlassSurface, GlassSurface.copy(alpha = 0.85f))
                    )
                )
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
        ) {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color = Accent,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(Spacing.md))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}

@Composable
fun SettingsToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!isChecked) }
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Accent.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(Spacing.md))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
        
        Switch(
            checked = isChecked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Accent,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = CardBackground
            )
        )
    }
}
