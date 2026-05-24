package com.habitmind

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.habitmind.data.manager.AppUsageTracker
import com.habitmind.ui.theme.Accent
import com.habitmind.ui.theme.GlassSurface
import com.habitmind.ui.theme.TextMuted
import com.habitmind.ui.theme.TextPrimary
import com.habitmind.ui.theme.TextSecondary
import com.habitmind.ui.theme.Warning

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            com.habitmind.ui.theme.HabitMindTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HabitMindApp()
                }
            }
        }
    }
}

@Composable
fun PermissionGate(
    onAllGranted: @Composable () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as HabitMindApplication
    val tracker = remember { AppUsageTracker(context) }

    var hasUsagePermission by remember { mutableStateOf(tracker.hasUsagePermission()) }
    var isAccessibilityEnabled by remember { mutableStateOf(false) }
    var showUsageDialog by remember { mutableStateOf(!tracker.hasUsagePermission()) }
    var showAccessibilityDialog by remember { mutableStateOf(false) }
    var dialogStep by remember { mutableStateOf(if (tracker.hasUsagePermission()) 1 else 0) }

    LaunchedEffect(Unit) {
        hasUsagePermission = tracker.hasUsagePermission()
        val serviceStr = "${context.packageName}/com.habitmind.data.service.AppEventService"
        val enabled = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        isAccessibilityEnabled = enabled?.split(":")?.contains(serviceStr) == true
        if (hasUsagePermission && isAccessibilityEnabled) {
            dialogStep = 2
        } else if (hasUsagePermission) {
            dialogStep = 1
            showAccessibilityDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (dialogStep == 2) {
            onAllGranted()
        }
    }

    if (showUsageDialog && dialogStep == 0) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Usage Access Required", color = Warning) },
            text = {
                Column {
                    Text(
                        "HabitMind needs usage access to track which apps you use and for how long.",
                        color = TextSecondary, style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Only package names and screen time are recorded — no content is read.",
                        color = TextMuted, style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUsageDialog = false
                        context.startActivity(
                            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) { Text("Open Settings") }
            },
            containerColor = GlassSurface
        )
    }

    if (showAccessibilityDialog && dialogStep == 1) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Accessibility Service", color = Warning) },
            text = {
                Column {
                    Text(
                        "Enable the HabitMind accessibility service for real-time app tracking and automatic screenshots.",
                        color = TextSecondary, style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Screenshots are stored locally and uploaded to your personal server for later analysis.",
                        color = TextMuted, style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAccessibilityDialog = false
                        context.startActivity(
                            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) { Text("Open Settings") }
            },
            containerColor = GlassSurface
        )
    }
}
