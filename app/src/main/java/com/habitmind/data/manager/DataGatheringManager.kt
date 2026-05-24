package com.habitmind.data.manager

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class DataGatheringManager(private val context: Context) {

    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val healthConnectClient by lazy { 
        if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else null
    }

    /**
     * Checks if Usage Stats permission is granted
     */
    fun hasUsageStatsPermission(): Boolean {
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            System.currentTimeMillis() - 1000 * 60,
            System.currentTimeMillis()
        )
        return stats != null && stats.isNotEmpty()
    }

    /**
     * Gets the most used app today that might be a "distraction"
     */
    fun getTopDistractionApp(): Triple<String, Long, String>? {
        if (!hasUsageStatsPermission()) return null

        val endTime = System.currentTimeMillis()
        val startTime = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        if (stats.isNullOrEmpty()) return null

        // Distraction package list (simplified for demo)
        val distractions = setOf(
            "com.instagram.android",
            "com.zhiliaoapp.musically", // TikTok
            "com.twitter.android",
            "com.facebook.katana",
            "com.google.android.youtube",
            "com.reddit.frontpage"
        )

        val topDistraction = stats
            .filter { distractions.contains(it.packageName) }
            .maxByOrNull { it.totalTimeInForeground }

        return topDistraction?.let {
            val minutes = it.totalTimeInForeground / (1000 * 60)
            val name = it.packageName.split(".").last().replaceFirstChar { char -> char.uppercase() }
            Triple(name, minutes, "Your biggest distraction")
        }
    }

    /**
     * Fetches steps from Health Connect for today
     */
    suspend fun getTodaySteps(): Long {
        val client = healthConnectClient ?: return 0
        
        val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endOfDay = Instant.now()

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
                )
            )
            response.records.sumOf { it.count }
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Fetches sleep hours from Health Connect for the last 24 hours
     */
    suspend fun getLastNightSleep(): Float {
        val client = healthConnectClient ?: return 0f
        
        val endTime = Instant.now()
        val startTime = endTime.minus(24, ChronoUnit.HOURS)

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            val totalMinutes = response.records.sumOf { 
                ChronoUnit.MINUTES.between(it.startTime, it.endTime)
            }
            totalMinutes / 60f
        } catch (e: Exception) {
            0f
        }
    }
}
