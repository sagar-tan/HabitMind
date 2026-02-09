package com.habitmind.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.habitmind.MainActivity
import com.habitmind.R

/**
 * Notification helper for HabitMind
 * Handles habit reminders and weekly reviews
 */
class HabitMindNotificationHelper(private val context: Context) {
    
    companion object {
        const val CHANNEL_HABIT_REMINDERS = "habit_reminders"
        const val CHANNEL_WEEKLY_REVIEW = "weekly_review"
        const val CHANNEL_GENERAL = "general"
        
        private const val NOTIFICATION_ID_HABIT_BASE = 1000
        private const val NOTIFICATION_ID_WEEKLY_REVIEW = 2000
        private const val NOTIFICATION_ID_GENERAL = 3000
    }
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            
            // Habit reminders channel
            val habitChannel = NotificationChannel(
                CHANNEL_HABIT_REMINDERS,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to complete your daily habits"
            }
            
            // Weekly review channel
            val weeklyChannel = NotificationChannel(
                CHANNEL_WEEKLY_REVIEW,
                "Weekly Reviews",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Weekly progress summaries and reviews"
            }
            
            // General notifications channel
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
            }
            
            notificationManager.createNotificationChannels(
                listOf(habitChannel, weeklyChannel, generalChannel)
            )
        }
    }
    
    // Show habit reminder notification
    fun showHabitReminder(habitId: Long, habitName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "habits")
            putExtra("habit_id", habitId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_HABIT_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time for your habit!")
            .setContentText(habitName)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        if (hasNotificationPermission()) {
            NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID_HABIT_BASE + habitId.toInt(), notification)
        }
    }
    
    // Show weekly review notification
    fun showWeeklyReview(habitsCompleted: Int, totalHabits: Int, tasksCompleted: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "insights")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_WEEKLY_REVIEW,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val message = buildString {
            append("This week: ")
            append("$habitsCompleted/$totalHabits habits completed, ")
            append("$tasksCompleted tasks done")
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_WEEKLY_REVIEW)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Your Weekly Review")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        if (hasNotificationPermission()) {
            NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID_WEEKLY_REVIEW, notification)
        }
    }
    
    // Show general notification
    fun showGeneralNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_GENERAL,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_GENERAL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        if (hasNotificationPermission()) {
            NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID_GENERAL, notification)
        }
    }
    
    // Cancel habit reminder
    fun cancelHabitReminder(habitId: Long) {
        NotificationManagerCompat.from(context)
            .cancel(NOTIFICATION_ID_HABIT_BASE + habitId.toInt())
    }
    
    // Check notification permission
    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
