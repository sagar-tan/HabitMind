package com.habitmind.data.sync

import android.content.Context
import android.widget.Toast
import com.habitmind.data.manager.AppUsageTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
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

    private val appUsageTracker = AppUsageTracker(context)

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
        return JSONObject().apply {
            put("deviceId", report.deviceId)
            put("exportedAt", report.exportedAt)
            put("periodStart", report.periodStart)
            put("periodEnd", report.periodEnd)
            put("interval", report.interval)
            put("apps", appsArray)
        }
    }

    suspend fun uploadUsageData(serverUrl: String, authToken: String? = null): SyncResult {
        return withContext(Dispatchers.IO) {
            try {
                if (!appUsageTracker.hasUsagePermission()) {
                    return@withContext SyncResult.Error("Usage stats permission not granted. Grant it in Settings > Apps > Special Access > Usage Access.")
                }

                val payload = buildUsagePayload()
                val url = URL("${serverUrl.trimEnd('/')}/api/upload")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
                if (!authToken.isNullOrBlank()) {
                    connection.setRequestProperty("Authorization", "Bearer $authToken")
                }
                connection.doOutput = true
                connection.connectTimeout = 15000
                connection.readTimeout = 15000

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(payload.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode
                if (responseCode == 200 || responseCode == 201) {
                    val response = BufferedReader(InputStreamReader(connection.inputStream)).readText()
                    SyncResult.Success("Uploaded ${payload.getJSONArray("apps").length()} apps to server")
                } else {
                    val errorBody = try {
                        BufferedReader(InputStreamReader(connection.errorStream)).readText()
                    } catch (_: Exception) { "Unknown error" }
                    SyncResult.Error("Server returned $responseCode: $errorBody")
                }
            } catch (e: Exception) {
                SyncResult.Error("Connection failed: ${e.localizedMessage ?: "Check server URL and network"}")
            }
        }
    }

    suspend fun fetchDataList(serverUrl: String): List<ServerFile> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("${serverUrl.trimEnd('/')}/api/data")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                if (connection.responseCode == 200) {
                    val response = BufferedReader(InputStreamReader(connection.inputStream)).readText()
                    val arr = JSONArray(response)
                    (0 until arr.length()).map { i ->
                        val obj = arr.getJSONObject(i)
                        ServerFile(
                            filename = obj.getString("filename"),
                            size = obj.getLong("size"),
                            uploadedAt = obj.optString("uploadedAt", "")
                        )
                    }
                } else emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    suspend fun downloadData(serverUrl: String, filename: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("${serverUrl.trimEnd('/')}/api/data/$filename")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                if (connection.responseCode == 200) {
                    BufferedReader(InputStreamReader(connection.inputStream)).readText()
                } else null
            } catch (_: Exception) {
                null
            }
        }
    }
}
