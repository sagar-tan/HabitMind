package com.habitmind.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.habitmind.data.database.entity.Habit
import com.habitmind.data.database.entity.HabitCompletion
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HabitDao {
    
    // --- Habits ---
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: Habit): Long
    
    @Update
    suspend fun update(habit: Habit)
    
    @Delete
    suspend fun delete(habit: Habit)
    
    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllActive(): Flow<List<Habit>>
    
    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getById(id: Long): Habit?
    
    @Query("SELECT * FROM habits WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<Habit?>
    
    @Query("UPDATE habits SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)
    
    // --- Completions ---
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletion)
    
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun getCompletion(habitId: Long, date: LocalDate): HabitCompletion?
    
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY date DESC")
    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletion>>
    
    @Query("SELECT * FROM habit_completions WHERE date = :date")
    fun getCompletionsForDate(date: LocalDate): Flow<List<HabitCompletion>>
    
    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun deleteCompletion(habitId: Long, date: LocalDate)
    
    // Toggle completion for a habit on a specific date
    @Query("""
        INSERT OR REPLACE INTO habit_completions (habitId, date, isCompleted, completedAt)
        VALUES (:habitId, :date, :isCompleted, :completedAt)
    """)
    suspend fun setCompletion(habitId: Long, date: LocalDate, isCompleted: Boolean, completedAt: String?)
    
    // Get streak count - consecutive days completed ending on given date
    @Query("""
        WITH RECURSIVE streak AS (
            SELECT date, 1 as count
            FROM habit_completions
            WHERE habitId = :habitId AND date = :endDate AND isCompleted = 1
            UNION ALL
            SELECT hc.date, s.count + 1
            FROM habit_completions hc
            INNER JOIN streak s ON hc.date = date(s.date, '-1 day')
            WHERE hc.habitId = :habitId AND hc.isCompleted = 1
        )
        SELECT COALESCE(MAX(count), 0) FROM streak
    """)
    suspend fun getStreakCount(habitId: Long, endDate: LocalDate): Int
    
    // Count completions in date range
    @Query("""
        SELECT COUNT(*) FROM habit_completions 
        WHERE habitId = :habitId AND date BETWEEN :startDate AND :endDate AND isCompleted = 1
    """)
    suspend fun getCompletionCount(habitId: Long, startDate: LocalDate, endDate: LocalDate): Int
}
