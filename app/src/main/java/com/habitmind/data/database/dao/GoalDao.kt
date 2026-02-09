package com.habitmind.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.habitmind.data.database.entity.Goal
import com.habitmind.data.database.entity.GoalUpdate
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface GoalDao {
    
    // --- Goals ---
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: Goal): Long
    
    @Update
    suspend fun update(goal: Goal)
    
    @Delete
    suspend fun delete(goal: Goal)
    
    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getById(id: Long): Goal?
    
    @Query("SELECT * FROM goals WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<Goal?>
    
    @Query("SELECT * FROM goals WHERE isCompleted = 0 ORDER BY CASE WHEN targetDate IS NULL THEN 1 ELSE 0 END, targetDate ASC, createdAt DESC")
    fun getActiveGoals(): Flow<List<Goal>>
    
    @Query("SELECT * FROM goals WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedGoals(): Flow<List<Goal>>
    
    @Query("SELECT * FROM goals ORDER BY isCompleted ASC, createdAt DESC")
    fun getAllGoals(): Flow<List<Goal>>
    
    // Update goal progress
    @Query("UPDATE goals SET progressPercent = :progress, isCompleted = CASE WHEN :progress >= 100 THEN 1 ELSE 0 END, completedAt = CASE WHEN :progress >= 100 THEN datetime('now') ELSE NULL END WHERE id = :goalId")
    suspend fun updateProgress(goalId: Long, progress: Int)
    
    // --- Goal Updates (Weekly Progress) ---
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdate(update: GoalUpdate)
    
    @Query("SELECT * FROM goal_updates WHERE goalId = :goalId ORDER BY weekStart DESC")
    fun getUpdatesForGoal(goalId: Long): Flow<List<GoalUpdate>>
    
    @Query("SELECT * FROM goal_updates WHERE weekStart = :weekStart")
    fun getUpdatesForWeek(weekStart: LocalDate): Flow<List<GoalUpdate>>
}
