package com.habitmind.data.export

import android.content.Context
import com.habitmind.HabitMindApplication
import com.habitmind.data.database.entity.Habit
import com.habitmind.data.database.entity.HabitCompletion
import com.habitmind.data.database.entity.JournalEntry
import com.habitmind.data.database.entity.Task
import com.habitmind.data.media.MediaStorageHelper
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Data export/import manager for HabitMind
 * Exports data to JSON for backup and portability
 */
class DataExportManager(private val context: Context) {
    
    private val mediaHelper = MediaStorageHelper(context)
    
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
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
            
            // Create export data structure
            val exportData = ExportData(
                version = 1,
                exportedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                userName = userName,
                habits = habits.map { it.toExportHabit() },
                tasks = tasks.map { it.toExportTask() },
                journalEntries = journalEntries.map { it.toExportJournal() }
            )
            
            // Write to file
            val filename = "habitmind_backup_${System.currentTimeMillis()}.json"
            val exportFile = File(mediaHelper.getExportDirectory(), filename)
            exportFile.writeText(json.encodeToString(exportData))
            
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
            val exportData = json.decodeFromString<ExportData>(jsonContent)
            
            val app = context.applicationContext as HabitMindApplication
            
            // Import habits
            var habitsImported = 0
            exportData.habits.forEach { exportHabit ->
                val habit = Habit(
                    name = exportHabit.name,
                    color = exportHabit.color,
                    reminderTime = exportHabit.reminderTime
                )
                app.habitRepository.insertHabit(habit)
                habitsImported++
            }
            
            // Import tasks
            var tasksImported = 0
            exportData.tasks.forEach { exportTask ->
                val task = Task(
                    title = exportTask.title,
                    description = exportTask.description,
                    estimatedMinutes = exportTask.estimatedMinutes,
                    progress = exportTask.progress
                )
                app.taskRepository.insertTask(task)
                tasksImported++
            }
            
            // Import journal entries
            var entriesImported = 0
            exportData.journalEntries.forEach { exportEntry ->
                val entry = JournalEntry(
                    content = exportEntry.content,
                    tags = exportEntry.tags ?: ""
                )
                app.journalRepository.insertEntry(entry)
                entriesImported++
            }
            
            // Update user name if provided
            exportData.userName?.let { name ->
                app.userPreferences.saveUserName(name)
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

// Export data classes
@Serializable
data class ExportData(
    val version: Int,
    val exportedAt: String,
    val userName: String?,
    val habits: List<ExportHabit>,
    val tasks: List<ExportTask>,
    val journalEntries: List<ExportJournalEntry>
)

@Serializable
data class ExportHabit(
    val name: String,
    val color: String,
    val reminderTime: String?,
    val isArchived: Boolean
)

@Serializable
data class ExportTask(
    val title: String,
    val description: String,
    val estimatedMinutes: Int,
    val progress: Int,
    val priority: Int
)

@Serializable
data class ExportJournalEntry(
    val content: String,
    val tags: String?,
    val type: String
)

// Extension functions for export conversion
fun Habit.toExportHabit() = ExportHabit(
    name = name,
    color = color,
    reminderTime = reminderTime,
    isArchived = isArchived
)

fun Task.toExportTask() = ExportTask(
    title = title,
    description = description,
    estimatedMinutes = estimatedMinutes,
    progress = progress,
    priority = priority
)

fun JournalEntry.toExportJournal() = ExportJournalEntry(
    content = content,
    tags = tags,
    type = type.name
)

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
