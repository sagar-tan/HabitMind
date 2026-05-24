package com.habitmind.data.manager

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val totalTimeInForegroundMs: Long,
    val totalTimeInForegroundMinutes: Long,
    val lastUsageTime: String?
)

data class UsageDataReport(
    val deviceId: String,
    val exportedAt: String,
    val periodStart: String,
    val periodEnd: String,
    val interval: String,
    val apps: List<AppUsageInfo>
)

class AppUsageTracker(private val context: Context) {

    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val packageManager = context.packageManager

    companion object {
        private val KNOWN_APPS = mapOf(
            "com.google.android.googlequicksearchbox" to "Google",
            "com.samsung.android.launcher" to "One UI Home",
            "com.sec.android.app.launcher" to "One UI Home",
            "com.android.incallui" to "Phone",
            "com.samsung.android.dialer" to "Phone",
            "com.android.dialer" to "Phone",
            "com.google.android.dialer" to "Phone",
            "com.samsung.android.messaging" to "Messages",
            "com.google.android.apps.messaging" to "Messages",
            "com.android.mms" to "Messages",
            "com.google.android.apps.photos" to "Photos",
            "com.samsung.android.app.gallery" to "Gallery",
            "com.android.gallery3d" to "Gallery",
            "com.google.android.youtube" to "YouTube",
            "com.spotify.music" to "Spotify",
            "com.google.android.apps.maps" to "Maps",
            "com.google.android.gm" to "Gmail",
            "com.android.vending" to "Play Store",
            "com.google.android.apps.docs" to "Drive",
            "com.google.android.calendar" to "Calendar",
            "com.samsung.android.calendar" to "Calendar",
            "com.android.chrome" to "Chrome",
            "com.google.android.apps.tachyon" to "Duo",
            "com.google.android.apps.meetings" to "Meet",
            "com.google.android.keep" to "Keep",
            "com.samsung.android.app.notes" to "Samsung Notes",
            "com.samsung.android.email.provider" to "Email",
            "com.samsung.android.contacts" to "Contacts",
            "com.google.android.contacts" to "Contacts",
            "com.samsung.android.clock" to "Clock",
            "com.google.android.deskclock" to "Clock",
            "com.samsung.android.camera" to "Camera",
            "com.google.android.apps.camera" to "Camera",
            "com.sec.android.app.camera" to "Camera",
            "com.samsung.android.video" to "Video Player",
            "com.google.android.apps.youtube.music" to "YouTube Music",
            "com.netflix.mediaclient" to "Netflix",
            "com.primevideo" to "Prime Video",
            "com.zhiliaoapp.musically" to "TikTok",
            "com.instagram.android" to "Instagram",
            "com.twitter.android" to "Twitter",
            "com.facebook.katana" to "Facebook",
            "com.facebook.orca" to "Messenger",
            "com.whatsapp" to "WhatsApp",
            "com.whatsapp.w4b" to "WhatsApp Business",
            "com.tencent.mm" to "WeChat",
            "com.snapchat.android" to "Snapchat",
            "com.reddit.frontpage" to "Reddit",
            "com.linkedin.android" to "LinkedIn",
            "com.google.android.apps.plus" to "Google+",
            "com.pinterest" to "Pinterest",
            "com.ubercab" to "Uber",
            "com.didiglobal.goober" to "DiDi",
            "com.olacabs.customer" to "Ola",
            "com.phonepe.app" to "PhonePe",
            "com.google.android.apps.nbu.paisa.user" to "GPay",
            "com.paypal.android.p2pmobile" to "PayPal",
            "com.amazon.amazonpay.lwa" to "Amazon Pay",
            "in.amazon.mShop.android.shopping" to "Amazon",
            "com.flipkart.android" to "Flipkart",
            "com.android.settings" to "Settings",
            "com.google.android.gms" to "Google Play Services",
            "com.google.android.gsf" to "Google Services",
            "com.sec.android.app.shealth" to "Samsung Health",
            "com.google.android.wearable.app" to "Wear OS",
            "com.android.systemui" to "System UI",
            "com.google.android.inputmethod.latin" to "Gboard",
            "com.sec.android.inputmethod" to "Samsung Keyboard",
            "com.google.android.apps.walletnfcrel" to "Google Wallet",
            "com.samsung.android.wallet" to "Samsung Wallet",
            "com.android.documentsui" to "Files",
            "com.google.android.apps.nbu.files" to "Files by Google",
            "com.sec.android.app.myfiles" to "My Files",
            "netmirrornew" to "Net Mirror",
            "imobile" to "iMobile",
            "com.google.android.apps.recorder" to "Recorder"
        )
    }

    fun hasUsagePermission(): Boolean {
        return try {
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                System.currentTimeMillis() - 60000,
                System.currentTimeMillis()
            )
            stats != null && stats.isNotEmpty()
        } catch (_: SecurityException) {
            false
        }
    }

    private fun resolveAppName(packageName: String): String {
        KNOWN_APPS[packageName]?.let { return it }
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val label = packageManager.getApplicationLabel(appInfo).toString().trim()
            if (label.isNotBlank() && !label.contains(".")) label
            else packageName.split(".").lastOrNull()?.replaceFirstChar { it.uppercase() } ?: packageName
        } catch (_: PackageManager.NameNotFoundException) {
            packageName.split(".").lastOrNull()?.replaceFirstChar { it.uppercase() } ?: packageName
        }
    }

    private fun getReport(startMillis: Long, endMillis: Long, interval: String): UsageDataReport {
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startMillis, endMillis
        ) ?: emptyList()

        val merged = stats
            .filter { it.totalTimeInForeground > 0 }
            .groupBy { it.packageName }
            .map { (pkg, entries) ->
                val totalTime = entries.sumOf { it.totalTimeInForeground }
                val lastUsed = entries.maxOf { it.lastTimeUsed }
                AppUsageInfo(
                    packageName = pkg,
                    appName = resolveAppName(pkg),
                    totalTimeInForegroundMs = totalTime,
                    totalTimeInForegroundMinutes = totalTime / 60000,
                    lastUsageTime = if (lastUsed > 0) Instant.ofEpochMilli(lastUsed).toString() else null
                )
            }
            .filter { it.appName !in setOf("System UI", "SystemUgo", "System") }
            .sortedByDescending { it.totalTimeInForegroundMinutes }

        val deviceId = android.provider.Settings.Secure.getString(
            context.contentResolver, android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown"

        return UsageDataReport(
            deviceId = deviceId,
            exportedAt = Instant.now().toString(),
            periodStart = Instant.ofEpochMilli(startMillis).toString(),
            periodEnd = Instant.ofEpochMilli(endMillis).toString(),
            interval = interval,
            apps = merged
        )
    }

    fun getTodayUsageReport(): UsageDataReport {
        val now = System.currentTimeMillis()
        val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return getReport(startOfDay, now, "DAILY")
    }

    @Suppress("unused")
    fun getWeeklyUsageReport(): UsageDataReport {
        val now = System.currentTimeMillis()
        val startOfWeek = LocalDate.now().minusDays(6).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return getReport(startOfWeek, now, "WEEKLY")
    }

    fun getUsageReport(startMillis: Long, endMillis: Long, interval: String = "CUSTOM"): UsageDataReport {
        return getReport(startMillis, endMillis, interval)
    }
}
