package com.habitmind.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.habitmind.data.database.dao.GoalDao
import com.habitmind.data.database.dao.HabitDao
import com.habitmind.data.database.dao.JournalDao
import com.habitmind.data.database.dao.TaskDao
import com.habitmind.data.database.entity.DailyLog
import com.habitmind.data.database.entity.Goal
import com.habitmind.data.database.entity.GoalUpdate
import com.habitmind.data.database.entity.Habit
import com.habitmind.data.database.entity.HabitCompletion
import com.habitmind.data.database.entity.JournalEntry
import com.habitmind.data.database.entity.Task

/**
 * HabitMind Room Database
 * Contains all data for habits, tasks, journal entries, goals
 */
@Database(
    entities = [
        Habit::class,
        HabitCompletion::class,
        Task::class,
        JournalEntry::class,
        DailyLog::class,
        Goal::class,
        GoalUpdate::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class HabitMindDatabase : RoomDatabase() {
    
    abstract fun habitDao(): HabitDao
    abstract fun taskDao(): TaskDao
    abstract fun journalDao(): JournalDao
    abstract fun goalDao(): GoalDao
    
    companion object {
        @Volatile
        private var INSTANCE: HabitMindDatabase? = null
        
        fun getDatabase(context: Context): HabitMindDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HabitMindDatabase::class.java,
                    "habitmind_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
