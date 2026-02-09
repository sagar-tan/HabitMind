package com.habitmind.data.repository

import com.habitmind.data.database.dao.GoalDao
import com.habitmind.data.database.entity.Goal
import com.habitmind.data.database.entity.GoalUpdate
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * Repository for goal operations
 * Handles weekly progress updates
 */
class GoalRepository(private val goalDao: GoalDao) {
    
    val activeGoals: Flow<List<Goal>> = goalDao.getActiveGoals()
    val completedGoals: Flow<List<Goal>> = goalDao.getCompletedGoals()
    val allGoals: Flow<List<Goal>> = goalDao.getAllGoals()
    
    suspend fun getGoalById(id: Long): Goal? = goalDao.getById(id)
    
    fun getGoalByIdFlow(id: Long): Flow<Goal?> = goalDao.getByIdFlow(id)
    
    suspend fun insertGoal(goal: Goal): Long = goalDao.insert(goal)
    
    suspend fun updateGoal(goal: Goal) = goalDao.update(goal)
    
    suspend fun deleteGoal(goal: Goal) = goalDao.delete(goal)
    
    // Update goal progress
    suspend fun updateProgress(goalId: Long, progress: Int) {
        goalDao.updateProgress(goalId, progress.coerceIn(0, 100))
    }
    
    // Add weekly progress update
    suspend fun addWeeklyUpdate(goalId: Long, progressDelta: Int, notes: String = "") {
        val weekStart = getWeekStart(LocalDate.now())
        val update = GoalUpdate(
            goalId = goalId,
            weekStart = weekStart,
            progressDelta = progressDelta,
            notes = notes
        )
        goalDao.insertUpdate(update)
        
        // Also update the goal's overall progress
        val goal = goalDao.getById(goalId)
        if (goal != null) {
            val newProgress = (goal.progressPercent + progressDelta).coerceIn(0, 100)
            goalDao.updateProgress(goalId, newProgress)
        }
    }
    
    fun getUpdatesForGoal(goalId: Long): Flow<List<GoalUpdate>> {
        return goalDao.getUpdatesForGoal(goalId)
    }
    
    fun getUpdatesForCurrentWeek(): Flow<List<GoalUpdate>> {
        val weekStart = getWeekStart(LocalDate.now())
        return goalDao.getUpdatesForWeek(weekStart)
    }
    
    // Get the Monday of the week containing the given date
    private fun getWeekStart(date: LocalDate): LocalDate {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
}
