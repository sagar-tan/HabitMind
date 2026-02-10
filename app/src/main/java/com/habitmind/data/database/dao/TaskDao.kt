package com.habitmind.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.habitmind.data.database.entity.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TaskDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long
    
    @Update
    suspend fun update(task: Task)
    
    @Delete
    suspend fun delete(task: Task)
    
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): Task?
    
    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<Task?>
    
    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY priority DESC, createdAt ASC")
    fun getTasksForDate(date: LocalDate): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks ORDER BY date DESC, priority DESC")
    fun getAllTasks(): Flow<List<Task>>
    
    // Update progress
    @Query("UPDATE tasks SET progress = :progress, completedAt = CASE WHEN :progress >= 100 THEN strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime') ELSE NULL END WHERE id = :taskId")
    suspend fun updateProgress(taskId: Long, progress: Int)
    
    // Get incomplete tasks before a date (for carry forward)
    @Query("SELECT * FROM tasks WHERE date < :date AND progress < 100")
    suspend fun getIncompleteTasks(date: LocalDate): List<Task>
    
    // Carry forward incomplete tasks to a new date
    @Query("""
        UPDATE tasks 
        SET date = :newDate, 
            isCarriedForward = 1, 
            originalDate = CASE WHEN originalDate IS NULL THEN date ELSE originalDate END
        WHERE date = :oldDate AND progress < 100
    """)
    suspend fun carryForwardTasks(oldDate: LocalDate, newDate: LocalDate)
    
    // Get task statistics for a date range
    @Query("""
        SELECT COUNT(*) FROM tasks 
        WHERE date BETWEEN :startDate AND :endDate AND progress >= 100
    """)
    suspend fun getCompletedTaskCount(startDate: LocalDate, endDate: LocalDate): Int
    
    @Query("""
        SELECT COUNT(*) FROM tasks 
        WHERE date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalTaskCount(startDate: LocalDate, endDate: LocalDate): Int
    
    // Get average progress for tasks on a date
    @Query("SELECT COALESCE(AVG(progress), 0) FROM tasks WHERE date = :date")
    suspend fun getAverageProgress(date: LocalDate): Float
}
