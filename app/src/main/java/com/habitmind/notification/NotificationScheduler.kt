package com.habitmind.notification

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.habitmind.HabitMindApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

/**
 * WorkManager-based notification scheduler
 * Handles periodic reminders and weekly reviews
 */

// Weekly review worker
class WeeklyReviewWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    
    override fun doWork(): Result {
        val app = applicationContext as HabitMindApplication
        val notificationHelper = HabitMindNotificationHelper(applicationContext)
        
        runBlocking {
            // Get stats for the past week
            val today = LocalDate.now()
            val weekAgo = today.minusDays(7)
            
            val habits = app.habitRepository.allActiveHabits.first()
            val habitsCompleted = habits.count { habit ->
                // Check if habit was completed at least 5 days this week
                var completedDays = 0
                for (i in 0..6) {
                    val date = weekAgo.plusDays(i.toLong())
                    val habitsWithStreak = app.habitRepository.getHabitsWithStreak(date).first()
                    if (habitsWithStreak.any { it.habit.id == habit.id && it.isCompletedToday }) {
                        completedDays++
                    }
                }
                completedDays >= 5
            }
            
            val taskStats = app.taskRepository.getCompletionStats(weekAgo, today)
            
            // Show notification
            notificationHelper.showWeeklyReview(
                habitsCompleted = habitsCompleted,
                totalHabits = habits.size,
                tasksCompleted = taskStats.completed
            )
        }
        
        return Result.success()
    }
}

// Habit reminder worker (checks for habits due)
class HabitReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    
    override fun doWork(): Result {
        val app = applicationContext as HabitMindApplication
        val notificationHelper = HabitMindNotificationHelper(applicationContext)
        
        runBlocking {
            val today = LocalDate.now()
            val habitsWithStreak = app.habitRepository.getHabitsWithStreak(today).first()
            
            // Find incomplete habits
            habitsWithStreak
                .filter { !it.isCompletedToday }
                .take(3) // Limit to avoid notification spam
                .forEach { habitWithStreak ->
                    notificationHelper.showHabitReminder(
                        habitId = habitWithStreak.habit.id,
                        habitName = habitWithStreak.habit.name
                    )
                }
        }
        
        return Result.success()
    }
}

/**
 * Scheduler for notification work
 */
object NotificationScheduler {
    
    private const val WEEKLY_REVIEW_WORK = "weekly_review_work"
    private const val HABIT_REMINDER_WORK = "habit_reminder_work"
    
    // Schedule weekly review for Sunday evening
    fun scheduleWeeklyReview(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        
        // Calculate initial delay to Sunday 7 PM
        val now = LocalDate.now()
        val nextSunday = now.with(DayOfWeek.SUNDAY)
        val daysUntilSunday = if (now.dayOfWeek == DayOfWeek.SUNDAY) 7 else ChronoUnit.DAYS.between(now, nextSunday)
        
        val weeklyReviewRequest = PeriodicWorkRequestBuilder<WeeklyReviewWorker>(
            7, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInitialDelay(daysUntilSunday, TimeUnit.DAYS)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WEEKLY_REVIEW_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            weeklyReviewRequest
        )
    }
    
    // Schedule daily habit reminders (evening)
    fun scheduleHabitReminders(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        
        val habitReminderRequest = PeriodicWorkRequestBuilder<HabitReminderWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInitialDelay(8, TimeUnit.HOURS) // Start 8 hours from now
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            HABIT_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            habitReminderRequest
        )
    }
    
    // Cancel all scheduled work
    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WEEKLY_REVIEW_WORK)
        WorkManager.getInstance(context).cancelUniqueWork(HABIT_REMINDER_WORK)
    }
    
    // Initialize all notifications
    fun initializeNotifications(context: Context) {
        scheduleWeeklyReview(context)
        scheduleHabitReminders(context)
    }
}
