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

    private fun buildAppInfo(stat: android.app.usage.UsageStats): AppUsageInfo {
        val appName = try {
            val appInfo = packageManager.getApplicationInfo(stat.packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            stat.packageName.split(".").lastOrNull() ?: stat.packageName
        }

        return AppUsageInfo(
            packageName = stat.packageName,
            appName = appName,
            totalTimeInForegroundMs = stat.totalTimeInForeground,
            totalTimeInForegroundMinutes = stat.totalTimeInForeground / 60000,
            lastUsageTime = if (stat.lastTimeUsed > 0) Instant.ofEpochMilli(stat.lastTimeUsed).toString() else null
        )
    }

    fun getTodayUsageReport(): UsageDataReport {
        val now = System.currentTimeMillis()
        val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startOfDay, now
        ) ?: emptyList()

        val apps = stats
            .filter { it.totalTimeInForeground > 0 }
            .sortedByDescending { it.totalTimeInForeground }
            .map { buildAppInfo(it) }

        val deviceId = android.provider.Settings.Secure.getString(
            context.contentResolver, android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown"

        return UsageDataReport(
            deviceId = deviceId,
            exportedAt = Instant.now().toString(),
            periodStart = Instant.ofEpochMilli(startOfDay).toString(),
            periodEnd = Instant.ofEpochMilli(now).toString(),
            interval = "DAILY",
            apps = apps
        )
    }

    @Suppress("unused")
    fun getWeeklyUsageReport(): UsageDataReport {
        val now = System.currentTimeMillis()
        val startOfWeek = LocalDate.now().minusDays(6).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startOfWeek, now
        ) ?: emptyList()

        val apps = stats
            .filter { it.totalTimeInForeground > 0 }
            .sortedByDescending { it.totalTimeInForeground }
            .map { buildAppInfo(it) }

        val deviceId = android.provider.Settings.Secure.getString(
            context.contentResolver, android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown"

        return UsageDataReport(
            deviceId = deviceId,
            exportedAt = Instant.now().toString(),
            periodStart = Instant.ofEpochMilli(startOfWeek).toString(),
            periodEnd = Instant.ofEpochMilli(now).toString(),
            interval = "WEEKLY",
            apps = apps
        )
    }

    fun getUsageReport(startMillis: Long, endMillis: Long, interval: String = "CUSTOM"): UsageDataReport {
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startMillis, endMillis
        ) ?: emptyList()

        val apps = stats
            .filter { it.totalTimeInForeground > 0 }
            .sortedByDescending { it.totalTimeInForeground }
            .map { buildAppInfo(it) }

        val deviceId = android.provider.Settings.Secure.getString(
            context.contentResolver, android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown"

        return UsageDataReport(
            deviceId = deviceId,
            exportedAt = Instant.now().toString(),
            periodStart = Instant.ofEpochMilli(startMillis).toString(),
            periodEnd = Instant.ofEpochMilli(endMillis).toString(),
            interval = interval,
            apps = apps
        )
    }
}
