package com.habitmind.data.sync

import android.content.Context
import android.graphics.Bitmap
import com.habitmind.data.manager.AppUsageTracker
import com.habitmind.data.service.AppEventStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
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

    suspend fun uploadScreenshotImmediately(bitmap: Bitmap, packageName: String): SyncResult {
        return withContext(Dispatchers.IO) {
            try {
                val boundary = "---Boundary${System.currentTimeMillis()}---"
                val lineEnd = "\r\n"
                val capturedAt = System.currentTimeMillis().toString()
                val filename = "ss_${capturedAt}_${packageName.replace(".", "_")}.jpg"

                val bos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, bos)
                val imageBytes = bos.toByteArray()

                val bodyBytes = ByteArrayOutputStream()

                bodyBytes.write("--$boundary$lineEnd".toByteArray())
                bodyBytes.write("Content-Disposition: form-data; name=\"packageName\"$lineEnd".toByteArray())
                bodyBytes.write(lineEnd.toByteArray())
                bodyBytes.write(packageName.toByteArray())
                bodyBytes.write(lineEnd.toByteArray())

                bodyBytes.write("--$boundary$lineEnd".toByteArray())
                bodyBytes.write("Content-Disposition: form-data; name=\"capturedAt\"$lineEnd".toByteArray())
                bodyBytes.write(lineEnd.toByteArray())
                bodyBytes.write(capturedAt.toByteArray())
                bodyBytes.write(lineEnd.toByteArray())

                bodyBytes.write("--$boundary$lineEnd".toByteArray())
                bodyBytes.write("Content-Disposition: form-data; name=\"screenshot\"; filename=\"$filename\"$lineEnd".toByteArray())
                bodyBytes.write("Content-Type: image/jpeg$lineEnd".toByteArray())
                bodyBytes.write("Content-Length: ${imageBytes.size}$lineEnd".toByteArray())
                bodyBytes.write(lineEnd.toByteArray())
                bodyBytes.write(imageBytes)
                bodyBytes.write(lineEnd.toByteArray())
                bodyBytes.write("--$boundary--$lineEnd".toByteArray())

                val url = URL("$SERVER_URL/api/upload/screenshot")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                connection.doOutput = true
                connection.connectTimeout = TIMEOUT_MS
                connection.readTimeout = TIMEOUT_MS

                DataOutputStream(connection.outputStream).use { it.write(bodyBytes.toByteArray()) }

                if (connection.responseCode == 200) {
                    SyncResult.Success("Uploaded screenshot for $packageName")
                } else {
                    SyncResult.Error("Server error ${connection.responseCode}")
                }
            } catch (e: Exception) {
                SyncResult.Error("Upload failed: ${e.localizedMessage ?: "No network"}")
            }
        }
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
