package com.habitmind.data.repository

import com.habitmind.data.database.dao.HabitDao
import com.habitmind.data.database.entity.Habit
import com.habitmind.data.database.entity.HabitCompletion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Habit with calculated streak
 */
data class HabitWithStreak(
    val habit: Habit,
    val isCompletedToday: Boolean,
    val currentStreak: Int
)

/**
 * Repository for habit operations
 * Handles streak calculations and completion toggling
 */
class HabitRepository(private val habitDao: HabitDao) {
    
    val allActiveHabits: Flow<List<Habit>> = habitDao.getAllActive()
    
    suspend fun getHabitById(id: Long): Habit? = habitDao.getById(id)
    
    fun getHabitByIdFlow(id: Long): Flow<Habit?> = habitDao.getByIdFlow(id)
    
    suspend fun insertHabit(habit: Habit): Long = habitDao.insert(habit)
    
    suspend fun updateHabit(habit: Habit) = habitDao.update(habit)
    
    suspend fun deleteHabit(habit: Habit) = habitDao.delete(habit)
    
    suspend fun archiveHabit(id: Long) = habitDao.archive(id)
    
    // Get habits with their completion status and streak for a specific date
    fun getHabitsWithStreak(date: LocalDate = LocalDate.now()): Flow<List<HabitWithStreak>> {
        return habitDao.getAllActive().map { habits ->
            habits.map { habit ->
                val completion = habitDao.getCompletion(habit.id, date)
                val streak = calculateStreak(habit.id, date)
                HabitWithStreak(
                    habit = habit,
                    isCompletedToday = completion?.isCompleted == true,
                    currentStreak = streak
                )
            }
        }
    }
    
    // Toggle habit completion for today
    suspend fun toggleCompletion(habitId: Long, date: LocalDate = LocalDate.now()) {
        val existing = habitDao.getCompletion(habitId, date)
        if (existing != null && existing.isCompleted) {
            habitDao.deleteCompletion(habitId, date)
        } else {
            habitDao.insertCompletion(
                HabitCompletion(
                    habitId = habitId,
                    date = date,
                    isCompleted = true,
                    completedAt = LocalDateTime.now()
                )
            )
        }
    }
    
    // Calculate current streak for a habit
    private suspend fun calculateStreak(habitId: Long, endDate: LocalDate): Int {
        var streak = 0
        var currentDate = endDate
        
        // Check if completed today/endDate first
        val todayCompletion = habitDao.getCompletion(habitId, currentDate)
        if (todayCompletion?.isCompleted != true) {
            // Check yesterday - if not completed, streak is 0
            currentDate = currentDate.minusDays(1)
            val yesterdayCompletion = habitDao.getCompletion(habitId, currentDate)
            if (yesterdayCompletion?.isCompleted != true) {
                return 0
            }
        } else {
            streak = 1
            currentDate = currentDate.minusDays(1)
        }
        
        // Count consecutive days
        while (true) {
            val completion = habitDao.getCompletion(habitId, currentDate)
            if (completion?.isCompleted == true) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else {
                break
            }
        }
        
        return streak
    }
    
    // Get completion rate for a date range
    suspend fun getCompletionRate(habitId: Long, startDate: LocalDate, endDate: LocalDate): Float {
        val completed = habitDao.getCompletionCount(habitId, startDate, endDate)
        val totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
        return if (totalDays > 0) completed.toFloat() / totalDays else 0f
    }
    
    // Get completions for a habit
    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletion>> {
        return habitDao.getCompletionsForHabit(habitId)
    }
}
