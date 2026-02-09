package com.habitmind.data.repository

import com.habitmind.data.database.dao.TaskDao
import com.habitmind.data.database.entity.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository for task operations
 * Handles carry-forward logic and progress tracking
 */
class TaskRepository(private val taskDao: TaskDao) {
    
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    
    fun getTasksForDate(date: LocalDate = LocalDate.now()): Flow<List<Task>> {
        return taskDao.getTasksForDate(date)
    }
    
    suspend fun getTaskById(id: Long): Task? = taskDao.getById(id)
    
    fun getTaskByIdFlow(id: Long): Flow<Task?> = taskDao.getByIdFlow(id)
    
    suspend fun insertTask(task: Task): Long = taskDao.insert(task)
    
    suspend fun updateTask(task: Task) = taskDao.update(task)
    
    suspend fun deleteTask(task: Task) = taskDao.delete(task)
    
    // Update task progress
    suspend fun updateProgress(taskId: Long, progress: Int) {
        taskDao.updateProgress(taskId, progress.coerceIn(0, 100))
    }
    
    // Carry forward incomplete tasks from previous day to today
    suspend fun carryForwardTasks(fromDate: LocalDate = LocalDate.now().minusDays(1)) {
        val today = LocalDate.now()
        taskDao.carryForwardTasks(fromDate, today)
    }
    
    // Get incomplete tasks that need to be carried forward
    suspend fun getIncompleteTasks(beforeDate: LocalDate = LocalDate.now()): List<Task> {
        return taskDao.getIncompleteTasks(beforeDate)
    }
    
    // Get task statistics
    suspend fun getCompletionStats(startDate: LocalDate, endDate: LocalDate): TaskStats {
        val completed = taskDao.getCompletedTaskCount(startDate, endDate)
        val total = taskDao.getTotalTaskCount(startDate, endDate)
        return TaskStats(
            completed = completed,
            total = total,
            completionRate = if (total > 0) completed.toFloat() / total else 0f
        )
    }
    
    // Get average progress for today
    suspend fun getTodayProgress(): Float {
        return taskDao.getAverageProgress(LocalDate.now())
    }
}

data class TaskStats(
    val completed: Int,
    val total: Int,
    val completionRate: Float
)
