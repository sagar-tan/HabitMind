package com.habitmind.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.habitmind.HabitMindApplication
import java.time.LocalDate

/**
 * WorkManager worker that runs at midnight to carry forward
 * incomplete tasks from the previous day to the current day.
 */
class MidnightRolloverWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as HabitMindApplication
            val yesterday = LocalDate.now().minusDays(1)
            app.taskRepository.carryForwardTasks(yesterday)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
