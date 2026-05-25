package com.habitmind.data.service

import android.accessibilityservice.AccessibilityService
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import com.habitmind.data.sync.DataSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class AppEventService : AccessibilityService() {

    private val storage by lazy { AppEventStorage(this) }
    private val syncManager by lazy { DataSyncManager(this) }
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val handler = Handler(Looper.getMainLooper())
    private val screenshotInterval = 5 * 60 * 1000L
    private var currentForeground = ""
    private var isTakingScreenshot = false
    private var isAccessibilityReady = false

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == currentForeground || pkg.isBlank()) return
        currentForeground = pkg
        val appName = resolveAppName(pkg)
        storage.recordAppOpen(pkg, appName)
        takeScreenshotIfPossible()
        scheduleNextScreenshot()
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        isAccessibilityReady = true
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        ioScope.cancel()
        storage.closeCurrentSession()
        storage.cleanup()
        super.onDestroy()
    }

    private val screenshotRunnable = object : Runnable {
        override fun run() {
            takeScreenshotIfPossible()
            handler.postDelayed(this, screenshotInterval)
        }
    }

    private fun scheduleNextScreenshot() {
        handler.removeCallbacks(screenshotRunnable)
        handler.postDelayed(screenshotRunnable, screenshotInterval)
    }

    private fun takeScreenshotIfPossible() {
        if (isTakingScreenshot) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
        isTakingScreenshot = true
        val pkg = currentForeground
        try {
            takeScreenshot(
                android.view.Display.DEFAULT_DISPLAY,
                Executors.newSingleThreadExecutor(),
                object : TakeScreenshotCallback {
                    override fun onSuccess(result: ScreenshotResult) {
                        try {
                            val bitmap = extractBitmap(result)
                            if (bitmap != null) {
                                storage.saveScreenshot(bitmap, pkg)
                                ioScope.launch {
                                    syncManager.uploadScreenshotImmediately(bitmap, pkg)
                                }
                            }
                        } catch (_: Exception) { }
                        isTakingScreenshot = false
                    }
                    override fun onFailure(failureReason: Int) {
                        isTakingScreenshot = false
                    }
                }
            )
        } catch (_: Exception) {
            isTakingScreenshot = false
        }
    }

    private fun extractBitmap(result: ScreenshotResult): Bitmap? {
        return try {
            result.javaClass.getMethod("getBitmap").invoke(result) as Bitmap
        } catch (_: Exception) {
            null
        }
    }

    private fun resolveAppName(packageName: String): String {
        KNOWN_APPS[packageName]?.let { return it }
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString().trim()
        } catch (_: PackageManager.NameNotFoundException) {
            packageName.split(".").lastOrNull()?.replaceFirstChar { it.uppercase() } ?: packageName
        }
    }

    companion object {
        private val KNOWN_APPS = mapOf(
            "com.google.android.googlequicksearchbox" to "Google",
            "com.android.chrome" to "Chrome",
            "com.instagram.android" to "Instagram",
            "com.twitter.android" to "Twitter",
            "com.facebook.katana" to "Facebook",
            "com.whatsapp" to "WhatsApp",
            "com.google.android.youtube" to "YouTube",
            "com.spotify.music" to "Spotify",
            "com.netflix.mediaclient" to "Netflix"
        )
    }
}
