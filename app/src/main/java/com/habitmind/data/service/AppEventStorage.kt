package com.habitmind.data.service

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Date

data class AppSessionEvent(
    val packageName: String,
    val appName: String,
    val openedAt: Long,
    val closedAt: Long? = null
)

data class ScreenshotRecord(
    val filename: String,
    val capturedAt: Long,
    val foregroundApp: String
)

class AppEventStorage(private val context: Context, private val dataDir: File? = null) {

    private val baseDir = dataDir ?: File(context.filesDir, "tracker")
    private val sessionsFile = File(baseDir, "sessions.jsonl")
    private val screenshotsDir = File(baseDir, "screenshots")

    init {
        baseDir.mkdirs()
        screenshotsDir.mkdirs()
    }

    fun recordAppOpen(packageName: String, appName: String) {
        closeCurrentSession()
        val session = JSONObject().apply {
            put("packageName", packageName)
            put("appName", appName)
            put("openedAt", System.currentTimeMillis())
        }
        sessionsFile.appendText(session.toString() + "\n")
    }

    fun closeCurrentSession() {
        if (!sessionsFile.exists()) return
        val lines = sessionsFile.readLines().toMutableList()
        if (lines.isEmpty()) return
        val lastLine = lines.last()
        val last = JSONObject(lastLine)
        if (last.has("closedAt")) return
        val now = System.currentTimeMillis()
        val duration = now - last.getLong("openedAt")
        last.put("closedAt", now)
        last.put("durationMs", duration)
        lines[lines.size - 1] = last.toString()
        sessionsFile.writeText(lines.joinToString("\n") + "\n")
    }

    fun getTodaySessions(): List<AppSessionEvent> {
        if (!sessionsFile.exists()) return emptyList()
        val startOfDay = java.time.LocalDate.now()
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        return sessionsFile.readLines().mapNotNull { line ->
            try {
                val obj = JSONObject(line)
                val opened = obj.getLong("openedAt")
                if (opened < startOfDay) return@mapNotNull null
                AppSessionEvent(
                    packageName = obj.getString("packageName"),
                    appName = obj.optString("appName", obj.getString("packageName")),
                    openedAt = opened,
                    closedAt = if (obj.has("closedAt")) obj.getLong("closedAt") else null
                )
            } catch (_: Exception) { null }
        }
    }

    fun getAllSessions(limitDays: Int = 1): List<AppSessionEvent> {
        if (!sessionsFile.exists()) return emptyList()
        val cutoff = System.currentTimeMillis() - limitDays * 86400000L
        return sessionsFile.readLines().mapNotNull { line ->
            try {
                val obj = JSONObject(line)
                val opened = obj.getLong("openedAt")
                if (opened < cutoff) return@mapNotNull null
                AppSessionEvent(
                    packageName = obj.getString("packageName"),
                    appName = obj.optString("appName", obj.getString("packageName")),
                    openedAt = opened,
                    closedAt = if (obj.has("closedAt")) obj.getLong("closedAt") else null
                )
            } catch (_: Exception) { null }
        }
    }

    fun saveScreenshot(bitmap: android.graphics.Bitmap, foregroundApp: String): String? {
        return try {
            val filename = "ss_${System.currentTimeMillis()}.jpg"
            val file = File(screenshotsDir, filename)
            file.outputStream().use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, out)
            }
            filename
        } catch (_: Exception) { null }
    }

    fun getScreenshotFile(filename: String): File? {
        val file = File(screenshotsDir, filename)
        return if (file.exists()) file else null
    }

    fun getRecentScreenshots(count: Int = 12): List<ScreenshotRecord> {
        val files = screenshotsDir.listFiles()
            ?.filter { it.name.endsWith(".jpg") }
            ?.sortedByDescending { it.lastModified() }
            ?.take(count) ?: emptyList()
        return files.map { file ->
            val name = file.nameWithoutExtension
            val ts = name.removePrefix("ss_").toLongOrNull() ?: file.lastModified()
            ScreenshotRecord(
                filename = file.name,
                capturedAt = ts,
                foregroundApp = ""
            )
        }
    }

    fun getAllScreenshotFiles(): List<File> {
        return screenshotsDir.listFiles()
            ?.filter { it.name.endsWith(".jpg") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    fun cleanup(keepDays: Int = 7) {
        val cutoff = System.currentTimeMillis() - keepDays * 86400000L
        screenshotsDir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoff) file.delete()
        }
        val sessions = sessionsFile.readLines().mapNotNull { line ->
            try {
                val obj = JSONObject(line)
                if (obj.getLong("openedAt") >= cutoff) line else null
            } catch (_: Exception) { null }
        }
        sessionsFile.writeText(sessions.joinToString("\n") + "\n")
    }
}
