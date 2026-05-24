package com.habitmind.data.sync

import android.content.Context
import com.habitmind.data.manager.AppUsageTracker
import com.habitmind.data.service.AppEventStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

sealed class SyncResult {
    data class Success(val message: String) : SyncResult()
    data class Error(val message: String) : SyncResult()
}

data class ServerFile(
    val filename: String,
    val size: Long,
    val uploadedAt: String
)

class DataSyncManager(private val context: Context) {

    companion object {
        private const val SERVER_URL = "https://moveet-control-center.onrender.com"
        private const val TIMEOUT_MS = 30000
    }

    private val appUsageTracker = AppUsageTracker(context)
    private val eventStorage = AppEventStorage(context)

    fun buildUsagePayload(): JSONObject {
        val report = appUsageTracker.getTodayUsageReport()
        val appsArray = JSONArray()
        report.apps.forEach { app ->
            appsArray.put(JSONObject().apply {
                put("packageName", app.packageName)
                put("appName", app.appName)
                put("totalTimeInForegroundMs", app.totalTimeInForegroundMs)
                put("totalTimeInForegroundMinutes", app.totalTimeInForegroundMinutes)
                put("lastUsageTime", app.lastUsageTime ?: JSONObject.NULL)
            })
        }

        val sessionsArray = JSONArray()
        eventStorage.getTodaySessions().forEach { s ->
            sessionsArray.put(JSONObject().apply {
                put("packageName", s.packageName)
                put("appName", s.appName)
                put("openedAt", s.openedAt)
                put("openedAtISO", java.time.Instant.ofEpochMilli(s.openedAt).toString())
                if (s.closedAt != null) {
                    put("closedAt", s.closedAt)
                    put("closedAtISO", java.time.Instant.ofEpochMilli(s.closedAt).toString())
                    put("durationMs", s.closedAt - s.openedAt)
                }
            })
        }

        return JSONObject().apply {
            put("deviceId", report.deviceId)
            put("exportedAt", report.exportedAt)
            put("periodStart", report.periodStart)
            put("periodEnd", report.periodEnd)
            put("interval", report.interval)
            put("apps", appsArray)
            put("sessions", sessionsArray)
        }
    }

    suspend fun uploadUsageData(): SyncResult {
        return withContext(Dispatchers.IO) {
            try {
                if (!appUsageTracker.hasUsagePermission()) {
                    return@withContext SyncResult.Error("Grant Usage Access in Settings > Apps > Special Access")
                }

                val payload = buildUsagePayload()
                val url = URL("$SERVER_URL/api/upload")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true
                connection.connectTimeout = TIMEOUT_MS
                connection.readTimeout = TIMEOUT_MS

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(payload.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode
                if (responseCode == 200 || responseCode == 201) {
                    val info = "Uploaded ${payload.getJSONArray("apps").length()} apps, ${payload.getJSONArray("sessions").length()} sessions"
                    SyncResult.Success(info)
                } else {
                    SyncResult.Error("Server error $responseCode")
                }
            } catch (e: Exception) {
                SyncResult.Error("Failed: ${e.localizedMessage ?: "No network"}")
            }
        }
    }

    suspend fun uploadScreenshots(): SyncResult {
        return withContext(Dispatchers.IO) {
            try {
                val screenshots = eventStorage.getAllScreenshotFiles()
                if (screenshots.isEmpty()) return@withContext SyncResult.Success("No new screenshots")
                var uploaded = 0
                val boundary = "---Boundary${System.currentTimeMillis()}---"
                val lineEnd = "\r\n"

                for (file in screenshots) {
                    try {
                        val url = URL("$SERVER_URL/api/upload/screenshot")
                        val connection = url.openConnection() as HttpURLConnection
                        connection.requestMethod = "POST"
                        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                        connection.doOutput = true
                        connection.connectTimeout = TIMEOUT_MS
                        connection.readTimeout = 60000

                        val bytes = file.readBytes()
                        val body = buildMultipartBody(boundary, lineEnd, file.name, bytes)

                        DataOutputStream(connection.outputStream).use { it.write(body) }

                        if (connection.responseCode == 200) {
                            uploaded++
                            file.delete()
                        }
                    } catch (_: Exception) { }
                }
                SyncResult.Success("Uploaded $uploaded screenshots")
            } catch (e: Exception) {
                SyncResult.Error("Screenshot upload failed: ${e.localizedMessage}")
            }
        }
    }

    private fun buildMultipartBody(boundary: String, lineEnd: String, filename: String, data: ByteArray): ByteArray {
        val bos = java.io.ByteArrayOutputStream()
        bos.write("--$boundary$lineEnd".toByteArray())
        bos.write("Content-Disposition: form-data; name=\"screenshot\"; filename=\"$filename\"$lineEnd".toByteArray())
        bos.write("Content-Type: image/jpeg$lineEnd".toByteArray())
        bos.write("Content-Length: ${data.size}$lineEnd".toByteArray())
        bos.write(lineEnd.toByteArray())
        bos.write(data)
        bos.write(lineEnd.toByteArray())
        bos.write("--$boundary--$lineEnd".toByteArray())
        return bos.toByteArray()
    }

    suspend fun fetchDataList(): List<ServerFile> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$SERVER_URL/api/data")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = TIMEOUT_MS
                connection.readTimeout = TIMEOUT_MS
                if (connection.responseCode == 200) {
                    val response = BufferedReader(InputStreamReader(connection.inputStream)).readText()
                    val arr = JSONArray(response)
                    (0 until arr.length()).map { i ->
                        val obj = arr.getJSONObject(i)
                        ServerFile(obj.getString("filename"), obj.getLong("size"), obj.optString("uploadedAt", ""))
                    }
                } else emptyList()
            } catch (_: Exception) { emptyList() }
        }
    }

    suspend fun downloadData(filename: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$SERVER_URL/api/data/$filename")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = TIMEOUT_MS
                connection.readTimeout = TIMEOUT_MS
                if (connection.responseCode == 200) {
                    BufferedReader(InputStreamReader(connection.inputStream)).readText()
                } else null
            } catch (_: Exception) { null }
        }
    }
}
