package com.habitmind.data.export

import android.content.Context
import com.habitmind.HabitMindApplication
import com.habitmind.data.database.entity.Habit
import com.habitmind.data.database.entity.JournalEntry
import com.habitmind.data.database.entity.Task
import com.habitmind.data.media.MediaStorageHelper
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Data export/import manager for HabitMind
 * Exports data to JSON for backup and portability
 * Uses org.json for simplicity (no additional dependencies needed)
 */
class DataExportManager(private val context: Context) {
    
    private val mediaHelper = MediaStorageHelper(context)
    
    // Export all data to JSON file
    suspend fun exportAllData(): ExportResult {
        return try {
            val app = context.applicationContext as HabitMindApplication
            
            // Collect all data
            val habits = app.habitRepository.allActiveHabits.first()
            val tasks = app.taskRepository.allTasks.first()
            val journalEntries = app.journalRepository.allEntries.first()
            
            // Get user preferences
            val userName = app.userPreferences.userName.first()
            
            // Create JSON export
            val exportJson = JSONObject().apply {
                put("version", 1)
                put("exportedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                put("userName", userName ?: "")
                
                // Habits
                put("habits", JSONArray().apply {
                    habits.forEach { habit ->
                        put(JSONObject().apply {
                            put("name", habit.name)
                            put("color", habit.color)
                            put("reminderTime", habit.reminderTime ?: "")
                            put("isArchived", habit.isArchived)
                        })
                    }
                })
                
                // Tasks
                put("tasks", JSONArray().apply {
                    tasks.forEach { task ->
                        put(JSONObject().apply {
                            put("title", task.title)
                            put("description", task.description)
                            put("estimatedMinutes", task.estimatedMinutes)
                            put("progress", task.progress)
                            put("priority", task.priority)
                        })
                    }
                })
                
                // Journal entries
                put("journalEntries", JSONArray().apply {
                    journalEntries.forEach { entry ->
                        put(JSONObject().apply {
                            put("content", entry.content)
                            put("tags", entry.tags)
                            put("type", entry.type.name)
                        })
                    }
                })
            }
            
            // Write to file
            val filename = "habitmind_backup_${System.currentTimeMillis()}.json"
            val exportFile = File(mediaHelper.getExportDirectory(), filename)
            exportFile.writeText(exportJson.toString(2)) // Pretty print with 2-space indent
            
            ExportResult.Success(exportFile.absolutePath)
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "Export failed")
        }
    }
    
    // Import data from JSON file
    suspend fun importData(filePath: String): ImportResult {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                return ImportResult.Error("File not found")
            }
            
            val jsonContent = file.readText()
            val exportJson = JSONObject(jsonContent)
            
            val app = context.applicationContext as HabitMindApplication
            
            // Import habits
            var habitsImported = 0
            val habitsArray = exportJson.optJSONArray("habits") ?: JSONArray()
            for (i in 0 until habitsArray.length()) {
                val habitJson = habitsArray.getJSONObject(i)
                val habit = Habit(
                    name = habitJson.getString("name"),
                    color = habitJson.optString("color", "#6366F1"),
                    reminderTime = habitJson.optString("reminderTime").takeIf { it.isNotEmpty() }
                )
                app.habitRepository.insertHabit(habit)
                habitsImported++
            }
            
            // Import tasks
            var tasksImported = 0
            val tasksArray = exportJson.optJSONArray("tasks") ?: JSONArray()
            for (i in 0 until tasksArray.length()) {
                val taskJson = tasksArray.getJSONObject(i)
                val task = Task(
                    title = taskJson.getString("title"),
                    description = taskJson.optString("description", ""),
                    estimatedMinutes = taskJson.optInt("estimatedMinutes", 30),
                    progress = taskJson.optInt("progress", 0)
                )
                app.taskRepository.insertTask(task)
                tasksImported++
            }
            
            // Import journal entries
            var entriesImported = 0
            val entriesArray = exportJson.optJSONArray("journalEntries") ?: JSONArray()
            for (i in 0 until entriesArray.length()) {
                val entryJson = entriesArray.getJSONObject(i)
                val entry = JournalEntry(
                    content = entryJson.getString("content"),
                    tags = entryJson.optString("tags", "")
                )
                app.journalRepository.insertEntry(entry)
                entriesImported++
            }
            
            // Update user name if provided
            val userName = exportJson.optString("userName")
            if (userName.isNotEmpty()) {
                app.userPreferences.saveUserName(userName)
            }
            
            ImportResult.Success(
                habitsImported = habitsImported,
                tasksImported = tasksImported,
                entriesImported = entriesImported
            )
        } catch (e: Exception) {
            ImportResult.Error(e.message ?: "Import failed")
        }
    }
    
    // Get list of available backups
    fun getAvailableBackups(): List<BackupInfo> {
        return mediaHelper.getExportDirectory()
            .listFiles()
            ?.filter { it.extension == "json" }
            ?.sortedByDescending { it.lastModified() }
            ?.map { file ->
                BackupInfo(
                    path = file.absolutePath,
                    filename = file.name,
                    size = mediaHelper.formatStorageSize(file.length()),
                    date = LocalDateTime.ofEpochSecond(
                        file.lastModified() / 1000,
                        0,
                        java.time.ZoneOffset.UTC
                    ).format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                )
            }
            ?: emptyList()
    }
    
    // Delete a backup file
    fun deleteBackup(path: String): Boolean {
        return try {
            File(path).delete()
        } catch (e: Exception) {
            false
        }
    }
}

// Result types
sealed class ExportResult {
    data class Success(val filePath: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}

sealed class ImportResult {
    data class Success(
        val habitsImported: Int,
        val tasksImported: Int,
        val entriesImported: Int
    ) : ImportResult()
    data class Error(val message: String) : ImportResult()
}

data class BackupInfo(
    val path: String,
    val filename: String,
    val size: String,
    val date: String
)
