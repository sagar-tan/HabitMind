package com.habitmind

import android.app.Application
import com.habitmind.data.database.HabitMindDatabase
import com.habitmind.data.datastore.UserPreferencesDataStore
import com.habitmind.data.repository.GoalRepository
import com.habitmind.data.repository.HabitRepository
import com.habitmind.data.repository.JournalRepository
import com.habitmind.data.repository.TaskRepository

/**
 * Application class for HabitMind
 * Provides singleton instances of database and repositories
 */
class HabitMindApplication : Application() {
    
    // Database
    val database: HabitMindDatabase by lazy { HabitMindDatabase.getDatabase(this) }
    
    // DataStore
    val userPreferences: UserPreferencesDataStore by lazy { UserPreferencesDataStore(this) }
    
    // Repositories
    val habitRepository: HabitRepository by lazy { HabitRepository(database.habitDao()) }
    val taskRepository: TaskRepository by lazy { TaskRepository(database.taskDao()) }
    val journalRepository: JournalRepository by lazy { JournalRepository(database.journalDao()) }
    val goalRepository: GoalRepository by lazy { GoalRepository(database.goalDao()) }
}
